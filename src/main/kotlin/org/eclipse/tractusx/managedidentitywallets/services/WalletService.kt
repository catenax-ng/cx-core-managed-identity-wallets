/********************************************************************************
 * Copyright (c) 2021,2022 Contributors to the CatenaX (ng) GitHub Organisation
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

package org.eclipse.tractusx.managedidentitywallets.services

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.observer.*
import io.ktor.client.statement.*
import org.eclipse.tractusx.managedidentitywallets.models.*
import org.eclipse.tractusx.managedidentitywallets.models.ssi.*
import org.eclipse.tractusx.managedidentitywallets.models.ssi.acapy.VerifyResponse
import org.eclipse.tractusx.managedidentitywallets.models.ssi.acapy.WalletAndAcaPyConfig
import org.eclipse.tractusx.managedidentitywallets.persistence.repositories.CredentialRepository
import org.eclipse.tractusx.managedidentitywallets.persistence.repositories.WalletRepository

interface WalletService {

    fun getWallet(identifier: String, withCredentials: Boolean = false): WalletDto

    fun getAll(): List<WalletDto>

    fun getAllBpns(): List<String>

    suspend fun createWallet(walletCreateDto: WalletCreateDto): WalletDto

    suspend fun deleteWallet(identifier: String): Boolean

    fun storeCredential(identifier: String, issuedCredential: IssuedVerifiableCredentialRequestDto): Boolean

    suspend fun issueCredential(vcRequest: VerifiableCredentialRequestDto): VerifiableCredentialDto

    suspend fun issueCatenaXCredential(vcCatenaXRequest: VerifiableCredentialRequestWithoutIssuerDto): VerifiableCredentialDto

    suspend fun resolveDocument(identifier: String): DidDocumentDto

    suspend fun registerBaseWallet(verKey: String): Boolean

    suspend fun issuePresentation(vpRequest: VerifiablePresentationRequestDto): VerifiablePresentationDto

    fun getCredentials(
        issuerIdentifier: String?,
        holderIdentifier: String?,
        type: String?,
        credentialId: String?
    ): List<VerifiableCredentialDto>

    suspend fun addService(identifier: String, serviceDto: DidServiceDto): DidDocumentDto

    suspend fun updateService(
        identifier: String,
        id: String,
        serviceUpdateRequestDto: DidServiceUpdateRequestDto
    ): DidDocumentDto

    suspend fun deleteService(identifier: String, id: String): DidDocumentDto

    fun isCatenaXWallet(bpn: String): Boolean

    suspend fun verifyVerifiablePresentation(vpDto: VerifiablePresentationDto,
                                             withDateValidation: Boolean = false): VerifyResponse

    companion object {
        fun createWithAcaPyService(
            walletAndAcaPyConfig: WalletAndAcaPyConfig,
            walletRepository: WalletRepository,
            credentialRepository: CredentialRepository,
        ): WalletService {
            val acaPyService = IAcaPyService.create(
                walletAndAcaPyConfig = walletAndAcaPyConfig,
                client = HttpClient() {
                    expectSuccess = true
                    install(ResponseObserver) {
                        onResponse { response ->
                            println("HTTP status: ${response.status.value}")
                            println("HTTP description: ${response.status.description}")
                        }
                    }
                    HttpResponseValidator {
                        validateResponse { response: HttpResponse ->
                            val statusCode = response.status.value
                            when (statusCode) {
                                in 300..399 -> throw RedirectResponseException(response, response.status.description)
                                in 400..499 -> throw ClientRequestException(response, response.status.description)
                                in 500..599 -> throw ServerResponseException(response, response.status.description)
                            }
                            if (statusCode >= 600) {
                                throw ResponseException(response, response.status.description)
                            }
                        }
                        handleResponseException { cause: Throwable ->
                            when (cause) {
                                is ClientRequestException -> {
                                    if ("already exists." in cause.message) {
                                        throw ConflictException("Aca-Py Error: ${cause.response.status.description}")
                                    }
                                    if ("Unprocessable Entity" in cause.message) {
                                        throw UnprocessableEntityException("Aca-Py Error: ${cause.response.status.description}")
                                    }
                                    throw cause
                                }
                                else -> throw cause
                            }
                        }
                    }
                    install(Logging) {
                        logger = Logger.DEFAULT
                        level = LogLevel.BODY
                    }
                    install(JsonFeature) {
                        serializer = JacksonSerializer() {
                            enable(SerializationFeature.INDENT_OUTPUT)
                            serializationConfig.defaultPrettyPrinter
                            setSerializationInclusion(JsonInclude.Include.NON_NULL)
                        }
                    }
                }
            )
            return AcaPyWalletServiceImpl(acaPyService, walletRepository, credentialRepository)
        }
    }
}
