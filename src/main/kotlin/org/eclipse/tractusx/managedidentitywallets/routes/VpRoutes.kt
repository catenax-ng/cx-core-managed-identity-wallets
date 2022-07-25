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

package org.eclipse.tractusx.managedidentitywallets.routes

import io.bkbn.kompendium.annotations.Field
import io.bkbn.kompendium.annotations.Param
import io.bkbn.kompendium.annotations.ParamType
import io.bkbn.kompendium.auth.Notarized.notarizedAuthenticate
import io.bkbn.kompendium.core.Notarized.notarizedPost
import io.bkbn.kompendium.core.metadata.ParameterExample
import io.bkbn.kompendium.core.metadata.RequestInfo
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.core.metadata.method.PostInfo

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable

import org.eclipse.tractusx.managedidentitywallets.models.semanticallyInvalidInputException
import org.eclipse.tractusx.managedidentitywallets.models.ssi.*
import org.eclipse.tractusx.managedidentitywallets.models.ssi.acapy.VerifyResponse
import org.eclipse.tractusx.managedidentitywallets.models.syntacticallyInvalidInputException
import org.eclipse.tractusx.managedidentitywallets.plugins.AuthConstants
import org.eclipse.tractusx.managedidentitywallets.services.WalletService

import org.slf4j.LoggerFactory

fun Route.vpRoutes(walletService: WalletService) {

    val log = LoggerFactory.getLogger(this::class.java)

    route("/presentations") {
        notarizedAuthenticate(AuthConstants.JWT_AUTH_UPDATE, AuthConstants.JWT_AUTH_UPDATE_SINGLE) {
            notarizedPost(
                PostInfo<Unit, VerifiablePresentationRequestDto, VerifiablePresentationDto>(
                    summary = "Create Verifiable Presentation ",
                    description = "Create a verifiable presentation from a list of verifiable credentials, signed by the holder",
                    requestInfo = RequestInfo(
                        description = "The verifiable presentation input data",
                        examples = verifiablePresentationRequestDtoExample
                    ),
                    responseInfo = ResponseInfo(
                        status = HttpStatusCode.Created,
                        description = "The created verifiable presentation",
                        examples = verifiablePresentationResponseDtoExample
                    ),
                    canThrow = setOf(semanticallyInvalidInputException),
                    tags = setOf("VerifiablePresentations"),
                    securitySchemes = setOf(AuthConstants.JWT_AUTH_UPDATE.name)
                )
            ) {
                val verifiableCredentialDto = call.receive<VerifiablePresentationRequestDto>()

                // verify requested holder with bpn in principal, if only ROLE_UPDATE_WALLET is given
                val principal = AuthConstants.getPrincipal(call.attributes)
                if (principal?.role == AuthConstants.ROLE_UPDATE_WALLET && verifiableCredentialDto.holderIdentifier == principal.bpn) {
                    log.debug("Authorization successful: holder identifier BPN ${verifiableCredentialDto.holderIdentifier} does match requestors BPN ${principal.bpn}!")
                }
                if (principal?.role == AuthConstants.ROLE_UPDATE_WALLET && verifiableCredentialDto.holderIdentifier != principal.bpn) {
                    log.error("Error: Holder identifier BPN ${verifiableCredentialDto.holderIdentifier} does not match requestors BPN ${principal.bpn}!")
                    return@notarizedPost call.respondText("Holder identifier BPN ${verifiableCredentialDto.holderIdentifier} does not match requestors BPN ${principal.bpn}!", ContentType.Text.Plain, HttpStatusCode.Unauthorized)
                }

                call.respond(HttpStatusCode.Created, walletService.issuePresentation(verifiableCredentialDto))
            }
        }

        route("/validation") {
            notarizedAuthenticate(AuthConstants.JWT_AUTH_VIEW) {
                notarizedPost(
                    PostInfo<WithDateValidation, VerifiablePresentationDto, VerifyResponse>(
                        summary = "Validate Verifiable Presentation",
                        description = "Validate Verifiable Presentation with all included credentials",
                        parameterExamples = setOf(
                            ParameterExample("withDateValidation", "withDateValidation", "false")
                        ),
                        requestInfo = RequestInfo(
                            description = "The verifiable presentation to validate",
                            examples = verifiablePresentationResponseDtoExample
                        ),
                        responseInfo = ResponseInfo(
                            status = HttpStatusCode.OK,
                            description = "The verification value",
                            examples = mapOf("demo" to VerifyResponse(
                                error = null,
                                valid = true,
                                vp = verifiablePresentationResponseDtoExample["demo"]!!
                            ))
                        ),
                        canThrow = setOf(semanticallyInvalidInputException, syntacticallyInvalidInputException),
                        tags = setOf("VerifiablePresentations"),
                        securitySchemes = setOf(AuthConstants.JWT_AUTH_VIEW.name)
                    )
                ) {
                    var withDateValidation = false
                    val verifiablePresentation = call.receive<VerifiablePresentationDto>()
                    if (call.request.queryParameters["withDateValidation"] != null) {
                        withDateValidation = call.request.queryParameters["withDateValidation"].toBoolean()
                    }
                    val verifyResponse =  walletService.verifyVerifiablePresentation(
                        vpDto = verifiablePresentation,
                        withDateValidation = withDateValidation
                    )
                    call.respond(HttpStatusCode.OK, verifyResponse)
                }
            }
        }
    }
}

val verifiablePresentationRequestDtoExample = mapOf(
    "demo" to VerifiablePresentationRequestDto(
        holderIdentifier = "did:example:76e12ec712ebc6f1c221ebfeb1f",
        verifiableCredentials = listOf(
            VerifiableCredentialDto(
                context = listOf(
                    JsonLdContexts.JSONLD_CONTEXT_W3C_2018_CREDENTIALS_V1,
                    JsonLdContexts.JSONLD_CONTEXT_W3C_2018_CREDENTIALS_EXAMPLES_V1
                ),
                id = "http://example.edu/credentials/333",
                type = listOf("University-Degree-Credential, VerifiableCredential"),
                issuer = "did:example:76e12ec712ebc6f1c221ebfeb1f",
                issuanceDate = "2019-06-16T18:56:59Z",
                expirationDate = "2019-06-17T18:56:59Z",
                credentialSubject = mapOf("college" to "Test-University"),
                proof = LdProofDto(
                    type = "Ed25519Signature2018",
                    created = "2021-11-17T22:20:27Z",
                    proofPurpose = "assertionMethod",
                    verificationMethod = "did:example:76e12ec712ebc6f1c221ebfeb1f#keys-1",
                    jws = "eyJiNjQiOmZhbHNlLCJjcml0IjpbImI2NCJdLCJhbGciOiJFZERTQSJ9..JNerzfrK46Mq4XxYZEnY9xOK80xsEaWCLAHuZsFie1-NTJD17wWWENn_DAlA_OwxGF5dhxUJ05P6Dm8lcmF5Cg"
                )
            )
        )
    )
)

val verifiablePresentationResponseDtoExample = mapOf(
    "demo" to VerifiablePresentationDto(
        context = listOf(JsonLdContexts.JSONLD_CONTEXT_W3C_2018_CREDENTIALS_V1),
        type =  listOf("VerifiablePresentation"),
        holder = "did:example:76e12ec712ebc6f1c221ebfeb1f",
        verifiableCredential = listOf(
            VerifiableCredentialDto(
                context = listOf(
                    JsonLdContexts.JSONLD_CONTEXT_W3C_2018_CREDENTIALS_V1,
                    JsonLdContexts.JSONLD_CONTEXT_W3C_2018_CREDENTIALS_EXAMPLES_V1
                ),
                id = "http://example.edu/credentials/3732",
                type = listOf("University-Degree-Credential, VerifiableCredential"),
                issuer = "did:example:76e12ec712ebc6f1c221ebfeb1f",
                issuanceDate = "2019-06-16T18:56:59Z",
                expirationDate = "2019-06-17T18:56:59Z",
                credentialSubject = mapOf("college" to "Test-University"),
                proof = LdProofDto(
                    type = "Ed25519Signature2018",
                    created = "2021-11-17T22:20:27Z",
                    proofPurpose = "assertionMethod",
                    verificationMethod = "did:example:76e12ec712ebc6f1c221ebfeb1f#keys-1",
                    jws = "eyJiNjQiOmZhbHNlLCJjcml0IjpbImI2NCJdLCJhbGciOiJFZERTQSJ9..JNerzfrK46Mq4XxYZEnY9xOK80xsEaWCLAHuZsFie1-NTJD17wWWENn_DAlA_OwxGF5dhxUJ05P6Dm8lcmF5Cg"
                )
            )
        ),
        proof = LdProofDto(
            type = "Ed25519Signature2018",
            created = "2021-11-17T22:20:27Z",
            proofPurpose = "assertionMethod",
            verificationMethod = "did:example:76e12ec712ebc6f1c221ebfeb1f#keys-1",
            jws = "eyJiNjQiOmZhbHNlLCJjcml0IjpbImI2NCJdLCJhbGciOiJFZERTQSJ9..JNerzfrK46Mq4XxYZEnY9xOK80xsEaWCLAHuZsFie1-NTJD17wWWENn_DAlA_OwxGF5dhxUJ05P6Dm8lcmF5Cg"
        )
    )
)

@Serializable
data class WithDateValidation(
    @Param(type = ParamType.QUERY)
    @Field(
        description = "Flag whether issuance and expiration date of all credentials should be validated",
        name = "withDateValidation"
    )
    val withDateValidation: Boolean? = false
)
