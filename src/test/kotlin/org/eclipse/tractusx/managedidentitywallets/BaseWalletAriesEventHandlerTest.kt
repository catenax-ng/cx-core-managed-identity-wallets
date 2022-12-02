/********************************************************************************
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.managedidentitywallets

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import okhttp3.internal.toImmutableList
import org.eclipse.tractusx.managedidentitywallets.models.*
import org.eclipse.tractusx.managedidentitywallets.models.ssi.acapy.WalletAndAcaPyConfig
import org.eclipse.tractusx.managedidentitywallets.persistence.repositories.ConnectionRepository
import org.eclipse.tractusx.managedidentitywallets.persistence.repositories.CredentialRepository
import org.eclipse.tractusx.managedidentitywallets.persistence.repositories.WalletRepository
import org.eclipse.tractusx.managedidentitywallets.persistence.repositories.WebhookRepository
import org.eclipse.tractusx.managedidentitywallets.plugins.configurePersistence
import org.eclipse.tractusx.managedidentitywallets.services.*
import org.hyperledger.acy_py.generated.model.AttachDecorator
import org.hyperledger.acy_py.generated.model.AttachDecoratorData
import org.hyperledger.acy_py.generated.model.V20CredIssue
import org.hyperledger.aries.api.connection.ConnectionRecord
import org.hyperledger.aries.api.connection.ConnectionState
import org.hyperledger.aries.api.issue_credential_v1.CredentialExchangeState
import org.hyperledger.aries.api.issue_credential_v2.V20CredExRecord
import org.hyperledger.aries.webhook.TenantAwareEventHandler
import org.jetbrains.exposed.sql.transactions.transaction
import org.mockito.kotlin.*
import java.io.File
import java.lang.RuntimeException
import kotlin.test.*

@kotlinx.serialization.ExperimentalSerializationApi
class BaseWalletAriesEventHandlerTest {

    private val issuerWallet = WalletExtendedData(
        id = 1,
        name = "CatenaX_Wallet",
        bpn = EnvironmentTestSetup.DEFAULT_BPN,
        did = "did:sov:catenax1",
        walletId = "walletId",
        walletKey = "walletId",
        walletToken = "walletId",
        revocationListName = null,
        pendingMembershipIssuance = false
    )

    private val connectionThreadId = "connection-thread-id"
    private val connectionId = "connection-id"

    private val credentialThreadId = "credential-thread-id"
    private val credentialId = "http://example.edu/credentials/3735"

    private val holderWallet = WalletExtendedData(
        id = 2,
        name = "target wallet",
        bpn = "BPNLholder",
        did = "did:sov:holder1",
        walletId = "walletId",
        walletKey = "walletId",
        walletToken = "walletId",
        revocationListName = null,
        pendingMembershipIssuance = true
    )

    private lateinit var mockEngine: MockEngine
    private lateinit var walletRepo: WalletRepository
    private lateinit var connectionRepository: ConnectionRepository
    private lateinit var webhookRepository: WebhookRepository
    private lateinit var credentialRepository: CredentialRepository
    private lateinit var bpdService: IBusinessPartnerDataService
    private lateinit var utilsService: UtilsService
    private lateinit var revocationService: IRevocationService
    private lateinit var webhookService: IWebhookService
    private lateinit var walletServiceSpy: IWalletService
    private lateinit var ariesEventHandler: BaseWalletAriesEventHandler

    @BeforeTest
    fun setup() {
        walletRepo = WalletRepository()
        webhookRepository = WebhookRepository()
        connectionRepository = ConnectionRepository()
        credentialRepository = CredentialRepository()
        mockEngine = MockEngine {
            respond(
                content = ByteReadChannel("""{true}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        bpdService = BusinessPartnerDataMockedService()
        utilsService = UtilsService("")
        revocationService = mock<IRevocationService>()
        webhookService = WebhookServiceImpl(
            webhookRepository, HttpClient(mockEngine)
        )
        val config = WalletAndAcaPyConfig("test", "", issuerWallet.bpn, "")
        val walletService = IWalletService.createWithAcaPyService(
            walletAndAcaPyConfig = config,
            walletRepository = walletRepo,
            credentialRepository = credentialRepository,
            utilsService = utilsService,
            revocationService = revocationService,
            webhookService = webhookService,
            connectionRepository = connectionRepository
        )
        walletServiceSpy = spy(walletService)

        doThrow(RuntimeException("mockedException"))
            .doCallRealMethod()
            .whenever(walletServiceSpy).storeCredential(any(), any())

        ariesEventHandler = BaseWalletAriesEventHandler(
            bpdService,
            walletServiceSpy,
            webhookService
        )
    }

    @AfterTest
    fun clean() {
        // remove Wallets and connection
        transaction {
            walletRepo.deleteWallet(issuerWallet.bpn)
            walletRepo.deleteWallet(holderWallet.bpn)
            connectionRepository.deleteConnections(issuerWallet.did)
            webhookRepository.deleteWebhook(connectionThreadId)
            webhookRepository.deleteWebhook(credentialThreadId)
            credentialRepository.deleteCredentialByCredentialId(credentialId)
        }
    }

    @Test
    fun testHandleAriesEvents() {
        withTestApplication({
            EnvironmentTestSetup.setupEnvironment(environment)
            configurePersistence()
        }) {
            runBlocking {
                // Setup
                addWallets(walletRepo, listOf(issuerWallet, holderWallet))
                addConnection(
                    connectionRepository, ConnectionState.REQUEST, connectionId,
                    issuerWallet.did, holderWallet.did
                )
                addWebhook(
                    webhookRepository, connectionThreadId,
                    "mocked-url", ConnectionState.REQUEST.toString()
                )
                addWebhook(
                    webhookRepository, credentialThreadId,
                    "mocked-url", CredentialExchangeState.OFFER_SENT.toString()
                )
                // Mock super call to tenantAwareEventHandler
                val tenantAwareEventHandler = mock<TenantAwareEventHandler>()
                doNothing().whenever(tenantAwareEventHandler).handleConnection(any(), any())


                val newConnectionRecord = ConnectionRecord()
                newConnectionRecord.state = ConnectionState.COMPLETED
                newConnectionRecord.connectionId = connectionId
                newConnectionRecord.requestId = connectionThreadId
                // Test `handleConnection` for state COMPLETED
                ariesEventHandler.handleConnection(issuerWallet.bpn, newConnectionRecord)
                transaction {
                    val updatedConnectionObj = connectionRepository.toObject(connectionRepository.get(connectionId))
                    assertEquals(ConnectionState.COMPLETED.toString(), updatedConnectionObj.state)
                    val updatedWebhook = webhookRepository.get(connectionThreadId)
                    assertEquals(ConnectionState.COMPLETED.toString(), updatedWebhook.state)
                }

                // Test `handleCredentials`

                var v20CredentialExchange = createCredentialExchange(credentialThreadId, CredentialExchangeState.DECLINED)

                // Test state != CREDENTIAL_ISSUED
                ariesEventHandler.handleCredentialV2(issuerWallet.bpn, v20CredentialExchange)
                transaction {
                    val credentials = credentialRepository.getCredentials(
                        null,
                        null,
                        null,
                        credentialId = credentialId
                    )
                    assertEquals(emptyList(), credentials)
                    val updatedWebhook = webhookRepository.get(credentialThreadId)
                    assertEquals(CredentialExchangeState.DECLINED.toString(), updatedWebhook.state)
                }

                // Test state == CREDENTIAL_ISSUED
                v20CredentialExchange.state = CredentialExchangeState.CREDENTIAL_ISSUED
                // Test (mocked-failed) store of credential
                ariesEventHandler.handleCredentialV2(issuerWallet.bpn, v20CredentialExchange)
                transaction {
                    val credentials = credentialRepository.getCredentials(
                        null,
                        null,
                        null,
                        credentialId = credentialId
                    )
                    assertEquals(emptyList(), credentials)
                }

                // Test success store credential
                ariesEventHandler.handleCredentialV2(issuerWallet.bpn, v20CredentialExchange)
                transaction {
                    val credentials = credentialRepository.getCredentials(
                        null,
                        null,
                        null,
                        credentialId = credentialId
                    )
                    assertEquals(credentialId, credentials[0].id)
                    val updatedWebhook = webhookRepository.get(credentialThreadId)
                    assertEquals(CredentialExchangeState.CREDENTIAL_ISSUED.toString(), updatedWebhook.state)
                }
            }
        }
    }

    private fun addWallets(walletRepo: WalletRepository, wallets: List<WalletExtendedData>) {
        transaction {
            wallets.forEach {
                walletRepo.addWallet(it)
            }
        }
    }

    private fun addConnection(
        connectionRepository: ConnectionRepository,
        state: ConnectionState,
        connectionId: String,
        myDid: String,
        theirDid: String
    ) {
        transaction {
            val connection = ConnectionRecord()
            connection.state = state
            connection.connectionId = connectionId
            connectionRepository.add(myDid, theirDid, connection)
        }
    }

    private fun addWebhook(
        webhookRepository: WebhookRepository,
        webhookThreadId: String,
        url: String,
        state: String
    ) {
        transaction {
            webhookRepository.add(
                webhookThreadId = webhookThreadId,
                url = url,
                stateOfRequest = state
            )
        }
    }

    private fun createCredentialExchange(threadId: String, state: CredentialExchangeState): V20CredExRecord {
        val data = AttachDecoratorData()
        data.base64 = File("./src/test/resources/credentials-test-data/vcBase64.txt")
            .readText(Charsets.UTF_8)
        val dataDecorator = AttachDecorator()
        dataDecorator.data = data
        val credentialTildeAttach = listOf(dataDecorator)
        val credIssue = V20CredIssue()
        credIssue.credentialsTildeAttach = credentialTildeAttach.toImmutableList()
        var v20CredentialExchange = V20CredExRecord()
        v20CredentialExchange.credIssue = credIssue
        v20CredentialExchange.state = state
        v20CredentialExchange.threadId = threadId
        return v20CredentialExchange
    }

}
