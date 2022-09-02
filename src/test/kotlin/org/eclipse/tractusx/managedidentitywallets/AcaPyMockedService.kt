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

package org.eclipse.tractusx.managedidentitywallets

import org.eclipse.tractusx.managedidentitywallets.models.*
import org.eclipse.tractusx.managedidentitywallets.models.ssi.*
import org.eclipse.tractusx.managedidentitywallets.models.ssi.acapy.*

import org.eclipse.tractusx.managedidentitywallets.services.IAcaPyService
import java.security.SecureRandom

class AcaPyMockedService(val baseWalletBpn: String,
                         val networkIdentifier: String): IAcaPyService {

    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private var currentDid: String = "EXAMPLE"

    private var didToVerKey = mapOf(
        "did:sov:AA5EEDcn8yTfMobaTcabj9" to "5zTG9qLF8DEzR7fmCa9jy6L5Efi5QvpWEvMXszh9jStA",
        "did:sov:LCNSw1JxSTDw7EpR1UMG7D" to "BTppBmURHHqg6PKf7ryv8VS7hrKg8nEhwmjuD9ciGssz",
        "did:sov:M6Mis1fZKuhEw71GNY3TAb" to "BxAExpSNdKQ4KA7ocjH7qgphkbdKva8kKy1pDn5ksWxV",
        "did:sov:YHXZLLSLnKxz5D2HQaKXcP" to "J3ymiVmkB6yEWZ9qsp62kHzGmGm2phdvapRA6bkoJmBW"
    )

    override fun getWalletAndAcaPyConfig(): WalletAndAcaPyConfig {
        return WalletAndAcaPyConfig(
            apiAdminUrl = "",
            networkIdentifier = networkIdentifier,
            baseWalletBpn = baseWalletBpn,
            adminApiKey = "Hj23iQUsstG!dde"
        )
    }

    override suspend fun getWallets(): WalletList = WalletList(results = emptyList())

    override suspend fun createSubWallet(subWallet: CreateSubWallet): CreatedSubWalletResult {
        return CreatedSubWalletResult(
            createdAt = "createdAt",
            walletId = "walletId",
            keyManagementMode = "managed",
            updatedAt = "updatedAt",
            WalletSettings(
                walletType = "walletType",
                walletName = "walletName",
                walletWebhookUrls = emptyList(),
                walletDispatchType = "walletDispatchType",
                walletId = "walletId",
                defaultLabel = "defaultLabel",
                imageUrl = "imageUrl"
            ),
            token = "token"
        )
    }

    override suspend fun assignDidToPublic(didIdentifier: String, token: String) {
        if (didIdentifier.contains(getWalletAndAcaPyConfig().networkIdentifier)) {
            throw Exception("Cannot process did containing network identifier!")
        }
        if (didIdentifier.indexOf(":") == 0) {
            throw Exception("Cannot process did starting with a colon!")
        }
    }

    override suspend fun deleteSubWallet(walletData: WalletExtendedData) {}

    override suspend fun getTokenByWalletIdAndKey(id: String, key: String): CreateWalletTokenResponse =
        CreateWalletTokenResponse(token = "token")

    override suspend fun createLocalDidForWallet(didCreateDto: DidCreate, token: String): DidResult {
        currentDid = createRandomString()
        return DidResult(
            result = DidResultDetails(
                did = currentDid,
                keyType = "",
                method = "",
                posture = "",
                verkey = "abc"
            )
        )
    }

    override suspend fun registerDidOnLedger(
        didRegistration: DidRegistration,
        endorserWalletToken: String
    ): DidRegistrationResult {
        if (didRegistration.did.contains(getWalletAndAcaPyConfig().networkIdentifier)) {
            throw Exception("Cannot process did containing network identifier!")
        }
        if (didRegistration.did.indexOf(":") == 0) {
            throw Exception("Cannot process did starting with a colon!")
        }
        return DidRegistrationResult(success = true)
    }

    override suspend fun <T> signJsonLd(signRequest: SignRequest<T>, token: String): String {
        if (SingletonTestData.signCredentialResponse.isNullOrEmpty()) {
            return ""
        }
        return SingletonTestData.signCredentialResponse
    }

    override suspend fun <T> verifyJsonLd(verifyRequest: VerifyRequest<T>, token: String): VerifyResponse {
        if (verifyRequest.signedDoc is VerifiablePresentationDto) {
            return if (SingletonTestData.isValidVerifiablePresentation) {
                VerifyResponse(error = null, valid = true)
            } else {
                VerifyResponse(error = "error", valid = false)
            }
        }
        if (verifyRequest.signedDoc is VerifiableCredentialDto) {
            return if (SingletonTestData.isValidVerifiableCredential) {
                VerifyResponse(error = null, valid = true)
            } else {
                VerifyResponse(error = "error", valid = false)
            }
        }
        return VerifyResponse(error = null, valid = true)
    }

    override suspend fun resolveDidDoc(did: String, token: String): ResolutionResult {
        var metadata = ResolutionMetaData(resolverType = "", resolver = "", retrievedTime = "", duration = 0)
        for (key in didToVerKey.keys) {
            if (did == key) {
                return ResolutionResult(
                    didDoc = DidDocumentDto(
                        id = did,
                        context = emptyList(),
                        verificationMethods = listOf(
                            DidVerificationMethodDto(
                                id = "did:indy:${getWalletAndAcaPyConfig().networkIdentifier}:${getIdentifierOfDid(did)}#key-1",
                                type = "Ed25519VerificationKey2018",
                                controller = "did:indy:${getWalletAndAcaPyConfig().networkIdentifier}:${getIdentifierOfDid(did)}",
                                publicKeyBase58= "${didToVerKey[key]}"
                            )
                        ),
                        services = listOf(
                            DidServiceDto(
                                id = "did:indy:${getWalletAndAcaPyConfig().networkIdentifier}:${getIdentifierOfDid(did)}#did-communication",
                                type = "did-communication",
                                serviceEndpoint = "http://localhost:8000/",
                            ),
                            DidServiceDto(
                                id = "did:indy:${getWalletAndAcaPyConfig().networkIdentifier}:${getIdentifierOfDid(did)}#linked_domains",
                                type = "linked_domains",
                                serviceEndpoint = "https://myhost:1111",
                            )
                        )
                    ),
                    metadata = metadata
                )
            }
        }
        if (!SingletonTestData.baseWalletDID.isNullOrEmpty() &&
            getIdentifierOfDid(did) == getIdentifierOfDid(SingletonTestData.baseWalletDID)) {
            return ResolutionResult(
                didDoc = DidDocumentDto(
                    id = did,
                    context = emptyList(),
                    verificationMethods = listOf(
                        DidVerificationMethodDto(
                            id = "did:indy:${getWalletAndAcaPyConfig().networkIdentifier}:${getIdentifierOfDid(did)}#key-1",
                            type = "Ed25519VerificationKey2018",
                            controller = "did:indy:${getWalletAndAcaPyConfig().networkIdentifier}:${getIdentifierOfDid(did)}",
                            publicKeyBase58= "${SingletonTestData.baseWalletVerKey}"
                        )
                    ),
                    services = listOf(
                        DidServiceDto(
                            id = "did:indy:${getWalletAndAcaPyConfig().networkIdentifier}:${getIdentifierOfDid(did)}#did-communication",
                            type = "did-communication",
                            serviceEndpoint = "http://localhost:8000/",
                        )
                    )
                ),
                metadata = metadata
            )
        }
        return ResolutionResult(
            didDoc = DidDocumentDto(
                id = did,
                context = emptyList(),
                services = listOf(
                    DidServiceDto(
                        id = "did:indy:${getWalletAndAcaPyConfig().networkIdentifier}:${getIdentifierOfDid(did)}#did-communication",
                        type = "did-communication",
                        serviceEndpoint = "http://localhost:8000/",
                    )
                )
            ),
            metadata = metadata
        )
    }

    override suspend fun updateService(serviceEndPoint: DidEndpointWithType, token: String) {}

    private fun createRandomString(): String {
        return (1..25)
            .map { SecureRandom().nextInt(charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    private fun getIdentifierOfDid(did: String): String {
        val elementsOfDid: List<String> = did.split(":")
        return elementsOfDid[elementsOfDid.size - 1]
    }
}
