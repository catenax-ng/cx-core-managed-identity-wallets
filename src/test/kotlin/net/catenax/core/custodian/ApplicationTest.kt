package net.catenax.core.managedidentitywallets

import io.ktor.http.*

// for 2.0.0-beta
// import io.ktor.server.response.*
// import io.ktor.server.request.*
// import io.ktor.server.routing.*
// import io.ktor.server.websocket.*
// import io.ktor.websocket.*
// import io.ktor.serialization.kotlinx.json.*
// import io.ktor.server.plugins.*
// import io.ktor.server.application.*

// for 1.6.7
import com.auth0.jwt.*
import com.auth0.jwt.algorithms.*
import com.auth0.jwt.interfaces.RSAKeyProvider
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.testing.*
import io.ktor.config.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*

import kotlin.test.*

import kotlinx.serialization.json.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.decodeFromString

import net.catenax.core.managedidentitywallets.plugins.*
import net.catenax.core.managedidentitywallets.models.*
import net.catenax.core.managedidentitywallets.models.ssi.acapy.VerifyResponse

import net.catenax.core.managedidentitywallets.routes.*
import net.catenax.core.managedidentitywallets.services.*
import net.catenax.core.managedidentitywallets.persistence.entities.VerifiableCredentials
import net.catenax.core.managedidentitywallets.persistence.entities.Wallets
import net.catenax.core.managedidentitywallets.persistence.repositories.*
import net.catenax.core.managedidentitywallets.plugins.AuthConstants

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.security.KeyPair
import java.security.interfaces.*
import java.sql.DriverManager
import java.util.Base64
import java.util.Date

class ApplicationTest {

    private fun setupEnvironment(environment: ApplicationEnvironment) {
        val jdbcUrl = System.getenv("CX_DB_JDBC_URL") ?: "jdbc:sqlite:file:test?mode=memory&cache=shared"
        (environment.config as MapApplicationConfig).apply {
            put("app.version", System.getenv("APP_VERSION") ?: "0.0.7")
            // put("db.jdbcUrl", System.getenv("CX_DB_JDBC_URL") ?: "jdbc:h2:mem:miw;DB_CLOSE_DELAY=-1;")
            // put("db.jdbcDriver", System.getenv("CX_DB_JDBC_DRIVER") ?: "org.h2.Driver")
            put("db.jdbcUrl", jdbcUrl)
            put("db.jdbcDriver", System.getenv("CX_DB_JDBC_DRIVER") ?: "org.sqlite.JDBC")
            put("datapool.url", System.getenv("CX_DATAPOOL_URL") ?: "http://0.0.0.0:8080")
            put("acapy.apiAdminUrl", System.getenv("ACAPY_API_ADMIN_URL") ?: "http://localhost:11000")
            put("acapy.networkIdentifier", System.getenv("ACAPY_NETWORK_IDENTIFIER") ?: ":indy:test")
            put("acapy.adminApiKey", System.getenv("ACAPY_ADMIN_API_KEY") ?: "Hj23iQUsstG!dde")
            put("wallet.baseWalletBpn", System.getenv("CX_BPN") ?: DEFAULT_BPN)

            put("auth.jwksUrl", System.getenv("CX_AUTH_JWKS_URL") ?: "http://localhost:18080/jwks")
            put("auth.issuerUrl", System.getenv("CX_AUTH_ISSUER_URL") ?: JwtConfig.issuerUrl)
            put("auth.realm", System.getenv("CX_AUTH_REALM") ?: "catenax")
            put("auth.roleMappings", System.getenv("CX_AUTH_ROLE_MAPPINGS") ?: "create_wallets:create_wallets,view_wallets:view_wallets,update_wallets:update_wallets,delete_wallets:delete_wallets,view_wallet:view_wallet,update_wallet:update_wallet")
            put("auth.resourceId", System.getenv("CX_AUTH_RESOURCE_ID") ?: JwtConfig.resourceId)

            // unused yet, just for completeness
            put("auth.clientId", System.getenv("CX_AUTH_CLIENT_ID") ?: "clientId")
            put("auth.clientSecret", System.getenv("CX_AUTH_CLIENT_SECRET") ?: "clientSecret")
            put("auth.redirectUrl", System.getenv("CX_AUTH_REDIRECT_URL") ?: "http://localhost:8080/callback")
        }
        val keepAliveConnection = DriverManager.getConnection(jdbcUrl)
    }

    private fun setupEnvironmentWithMissingRoleMapping(environment: ApplicationEnvironment) {
        setupEnvironment(environment)
        (environment.config as MapApplicationConfig).apply {
            put("auth.roleMappings", System.getenv("CX_AUTH_ROLE_MAPPINGS") ?: "no_create_wallets:create_wallets,no_view_wallets:view_wallets,no_update_wallets:update_wallets,no_delete_wallets:delete_wallets,view_wallet:view_wallet,update_wallet:update_wallet")
        }
    }

    object JwtConfig {

        val issuerUrl = "http://localhost:8081/auth/realms/catenax"
        val resourceId = "ManagedIdentityWallets"

        private const val secret = "zAP5MBA4B4Ijz0MZaS48"
        private const val validityInMs = 36_000_00 * 10 // 10 hours
        val kp = KeyPairGenerator.getInstance("RSA").generateKeyPair()
        val provider = object : RSAKeyProvider {
            override fun getPublicKeyById(kid: String): RSAPublicKey {
                return kp.getPublic() as RSAPublicKey;
            }

            override fun getPrivateKey(): RSAPrivateKey {
                return kp.getPrivate() as RSAPrivateKey;
            }

            override fun getPrivateKeyId(): String {
                return "jEpf8fJRWA9Tc7muBqbCGgcqhhzFWIyDeL9GZAv8-zY";
            }

        }

        private val algorithm = Algorithm.RSA256(provider)

        fun makeBaseToken(role: String): JWTCreator.Builder = JWT.create()
                .withSubject("Authentication")
                .withIssuer(issuerUrl)
                .withAudience(resourceId)
                .withClaim("typ", "Bearer")
                .withClaim("resource_access", mapOf(JwtConfig.resourceId to mapOf(AuthConstants.ROLES to arrayOf(role))))
                .withExpiresAt(getExpiration())
        
        fun makeToken(role: String, bpn: String? = null): String {
            if (!bpn.isNullOrEmpty()) {
                return makeBaseToken(role).withClaim("BPN", bpn).sign(algorithm)
            } else {
                return makeBaseToken(role).sign(algorithm)
            }
        }

        fun jwks(): String {
            val pubKey = kp.getPublic() as RSAPublicKey
            val modulus = Base64.getUrlEncoder().encodeToString(pubKey.getModulus().toByteArray())
            val exponent = Base64.getUrlEncoder().encodeToString(pubKey.getPublicExponent().toByteArray())
            return """
{
  "keys": [
    {
      "kid": "jEpf8fJRWA9Tc7muBqbCGgcqhhzFWIyDeL9GZAv8-zY",
      "kty": "RSA",
      "alg": "RS256",
      "use": "sig",
      "n": "$modulus",
      "e": "$exponent",
      "x5c": [
        "MIICozCCAYsCBgGBI/qjTDANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDDApDWC1DZW50cmFsMB4XDTIyMDYwMjEwMzIxN1oXDTMyMDYwMjEwMzM1N1owFTETMBEGA1UEAwwKQ1gtQ2VudHJhbDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJW41obiIA9Qn5mil8LtlUSGaWJ+qs6S368pQhYDvtdyftS8QJCvdFZb3h2HDqJl92L4JJ33rysY/wlMxSH0MMHYZZORMryDKnjGzL5V7/5a4BRmm691zr0Nizx7gRE9A4c2PL7MTFeTn7z8qxR0FpPs2jTaWhZMYFMZurrlfie1WAPttg1Fohs3ao8/T6LMdQAnIj0ahwj+E5MpCYx++4brMzvfzOmF4fmPPDKf0MXMVdjGCu1jVND1SygwJhn6qe+OaObT1KPFwzW0DguijYlgzFvKcL8eJ4U/pC929uUMaSiEyr0Qlrof+85MP9Fgtmj4qGMUiongWn66x6O6CMUCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEARcnTn/oSSp+V0si1bcjz/JLX5ctSbJMuP3HVnMZMuQ+LBzITZYjVpmlz3/ZckEcbv2hzEFOpuI7+/JSvidWi3+xwuUpukaQqEDmIP+KkH5bFlGbEYYJGgLtYmdHVOez5cO9GOC1eoXeatD30N4arRTOqSo9d79OVbZk3fEG4FJ+74LT1x80yUbI3pbKnfUIDlTtm5GZq2WN8axN82v5dnI6jVzkMGyj9f2DQUher2+eytsr0kmkU7xepsPj+LlzUFJMyF5CDBRy+jy/51ph4RdrRvkGtcXRlJYqvclc316x9B66wcZZJYR5n4iR5Yf3cZZZUWQo4QpDLPu055RE56g=="
      ],
      "x5t": "MpbgCqkBr47cjCY6d7zZbWw7qew",
      "x5t#S256": "eaXcPQWMazr102rZ5DfzxzRlDcppaYOfwnEptVaZDCs"
    }
  ]
}
"""
        }

        /**
        * Calculate the expiration Date based on current time + the given validity
        */
        private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)

    }

    private val DEFAULT_BPN = "BPNL00000"
    private val walletRepository = WalletRepository()
    private val credentialRepository = CredentialRepository()
    private val acaPyMockedService = AcaPyMockedService(DEFAULT_BPN)
    private val walletService = AcaPyWalletServiceImpl(acaPyMockedService, walletRepository, credentialRepository)
    private val bpdService = BusinessPartnerDataMockedService()

    private val CREATE_TOKEN = JwtConfig.makeToken(AuthConstants.ROLE_CREATE_WALLETS)
    private val VIEW_TOKEN = JwtConfig.makeToken(AuthConstants.ROLE_VIEW_WALLETS)
    private val UPDATE_TOKEN = JwtConfig.makeToken(AuthConstants.ROLE_UPDATE_WALLETS)
    private val DELETE_TOKEN = JwtConfig.makeToken(AuthConstants.ROLE_DELETE_WALLETS)
    private val VIEW_TOKEN_SINGLE = JwtConfig.makeToken(AuthConstants.ROLE_VIEW_WALLET, DEFAULT_BPN)
    private val VIEW_TOKEN_SINGLE_WITHOUT_BPN = JwtConfig.makeToken(AuthConstants.ROLE_VIEW_WALLET)
    private val UPDATE_TOKEN_SINGLE = JwtConfig.makeToken(AuthConstants.ROLE_UPDATE_WALLET, DEFAULT_BPN)

    val server = embeddedServer(Netty, port = 18080) {
        routing {
            get("/jwks") {
                call.respondText(JwtConfig.jwks())
            }
        }
    }

    @BeforeTest
    fun setup() {
        server.start()
    }

    @AfterTest
    fun tearDown() {
        server.stop(1000, 10000)
    }

    @Test
    fun testMissingRoleMapping() {
        var exception = assertFailsWith<Exception> {
            withTestApplication({
                setupEnvironmentWithMissingRoleMapping(environment)
                configurePersistence()
                configureOpenAPI()
                configureSecurity()
                configureRouting(walletService)
                appRoutes(walletService, bpdService)
                configureSerialization()
            }) {
                assertTrue(false)
            }
        }
        assertTrue(exception.message!!.contains("role mapping not defined"))
    }

    @Test
    fun testRoot() {
        withTestApplication({
            setupEnvironment(environment)
            configurePersistence()
            configureOpenAPI()
            configureSecurity()
            configureRouting(walletService)
            appRoutes(walletService, bpdService)
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("Catena-X Core"))
            }
            handleRequest(HttpMethod.Post, "/").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
            handleRequest(HttpMethod.Get, "/docs").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            handleRequest(HttpMethod.Get, "/openapi.json").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testWalletCrud() {
        withTestApplication({
            setupEnvironment(environment)
            configurePersistence()
            configureOpenAPI()
            configureSecurity()
            configureRouting(walletService)
            appRoutes(walletService, bpdService)
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Get, "/api/wallets") {
                addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val wallets: List<WalletDto> = Json.decodeFromString(ListSerializer(WalletDto.serializer()), response.content!!)
                assertEquals(0, wallets.size)
            }
            // create wallet
            handleRequest(HttpMethod.Post, "/api/wallets") {
                addHeader(HttpHeaders.Authorization, "Bearer $CREATE_TOKEN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"bpn":"$DEFAULT_BPN", "name": "name1"}""")
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }
            handleRequest(HttpMethod.Get, "/api/wallets") {
                addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val wallets: List<WalletDto> = Json.decodeFromString(ListSerializer(WalletDto.serializer()), response.content!!)
                assertEquals(1, wallets.size)
            }

            // programmatically add a wallet
            runBlocking {
                walletService.createWallet(WalletCreateDto("did3", "name3"))
            }

            handleRequest(HttpMethod.Get, "/api/wallets") {
                addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val wallets: List<WalletDto> = Json.decodeFromString(ListSerializer(WalletDto.serializer()), response.content!!)
                assertEquals(2, wallets.size)
            }

            handleRequest(HttpMethod.Get, "/api/wallets/$DEFAULT_BPN") {
                addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val wallet: WalletDto = Json.decodeFromString(WalletDto.serializer(), response.content!!)
                assertEquals(DEFAULT_BPN, wallet.bpn)
            }

            // delete both from the store
            handleRequest(HttpMethod.Delete, "/api/wallets/$DEFAULT_BPN") {
                addHeader(HttpHeaders.Authorization, "Bearer $DELETE_TOKEN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            runBlocking {
                walletService.deleteWallet("did3")
            }

            // verify deletion
            handleRequest(HttpMethod.Get, "/api/wallets") {
                addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val wallets: List<WalletDto> = Json.decodeFromString(ListSerializer(WalletDto.serializer()), response.content!!)
                assertEquals(0, wallets.size)
            }

            assertEquals(0, walletService.getAll().size)
        }
    }

    @Test
    fun testWalletCrudWithWrongRoles() {
        withTestApplication({
            setupEnvironment(environment)
            configurePersistence()
            configureOpenAPI()
            configureSecurity()
            configureRouting(walletService)
            appRoutes(walletService, bpdService)
            configureSerialization()
        }) {
            // view wallets with wrong token should not work
            handleRequest(HttpMethod.Get, "/api/wallets") {
                addHeader(HttpHeaders.Authorization, "Bearer $UPDATE_TOKEN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            // create wallet with wrong token should not work
            handleRequest(HttpMethod.Post, "/api/wallets") {
                addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"bpn":"$DEFAULT_BPN", "name": "name1"}""")
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            // programmatically add base wallet
            runBlocking {
                walletService.createWallet(WalletCreateDto(DEFAULT_BPN, "name1"))
            }

            // delete should not work with wrong token
            handleRequest(HttpMethod.Delete, "/api/wallets/$DEFAULT_BPN").apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            // clean up
            runBlocking {
                walletService.deleteWallet(DEFAULT_BPN)
            }

            assertEquals(0, walletService.getAll().size)
        }
    }

    @Test
    fun testWalletCrudWithSingleRoles() {
        withTestApplication({
            setupEnvironment(environment)
            configurePersistence()
            configureOpenAPI()
            configureSecurity()
            configureRouting(walletService)
            appRoutes(walletService, bpdService)
            configureSerialization()
        }) {
            // view wallets with single view token should not work
            handleRequest(HttpMethod.Get, "/api/wallets") {
                addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN_SINGLE")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            // programmatically add base wallet
            runBlocking {
                walletService.createWallet(WalletCreateDto(DEFAULT_BPN, "default_name"))
            }

            // view single wallet should work
            handleRequest(HttpMethod.Get, "/api/wallets/$DEFAULT_BPN") {
                addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN_SINGLE")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            // view single wallet without BPN should not work
            handleRequest(HttpMethod.Get, "/api/wallets") {
                addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN_SINGLE_WITHOUT_BPN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            // request a credential by the Catena-X issuer
            var exception = assertFailsWith<BadRequestException> {
                handleRequest(HttpMethod.Post, "/api/credentials/issuer") {
                    addHeader(HttpHeaders.Authorization, "Bearer $UPDATE_TOKEN_SINGLE")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(
"""
{
    "@context": [
        "https://www.w3.org/2018/credentials/v1",
        "https://www.w3.org/2018/credentials/examples/v1"
    ],
    "id": "http://example.edu/credentials/3735",
    "type": [
        "University-Degree-Credential",
        "VerifiableCredential"
    ],
    "issuerIdentifier": "$DEFAULT_BPN",
    "issuanceDate": "2021-06-16T18:56:59Z",
    "expirationDate": "2026-06-17T18:56:59Z",
    "credentialSubject": {
        "givenName": "TestAfterQuestion",
        "familyName": "Student",
        "degree": {
            "type": "Master",
            "degreeType": "Undergraduate",
            "name": "Master of Test"
        },
        "college": "Test"
    },
    "holderIdentifier": "$DEFAULT_BPN"
}
""")
                }.apply {
                    // important is that it isn't Unauthorized
                    assertNotEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
            assertTrue(exception.message!!.contains("Error: no verification methods"))

            // request a presentation
            exception = assertFailsWith<BadRequestException> {
                handleRequest(HttpMethod.Post, "/api/presentations") {
                    addHeader(HttpHeaders.Authorization, "Bearer $UPDATE_TOKEN_SINGLE")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(
"""
{
    "holderIdentifier": "$DEFAULT_BPN",
    "verifiableCredentials": [
        {
            "id": "http://example.edu/credentials/3732",
            "@context": [
                "https://www.w3.org/2018/credentials/v1",
                "https://www.w3.org/2018/credentials/examples/v1"
            ],
            "type": [
                "University-Degree-Credential",
                "VerifiableCredential"
            ],
            "issuer": "did:indy:local:test:43Arq24V9uQFPKHuDb7TC5",
            "issuanceDate": "2019-06-16T18:56:59Z",
            "expirationDate": "2019-06-17T18:56:59Z",
            "credentialSubject": {
                "givenName": "Sally",
                "familyName": "Student",
                "degree": {
                    "type": "Master",
                    "degreeType": "Undergraduate",
                    "name": "Master of Science and Arts"
                },
                "college": "Stuttgart",
                "id": "did:indy:local:test:QZakhgHUUAowUbhgZ9PZLD"
            },
            "proof": {
                "type": "Ed25519Signature2018",
                "created": "2022-03-24T09:34:02Z",
                "proofPurpose": "assertionMethod",
                "verificationMethod": "did:indy:local:test:43Arq24V9uQFPKHuDb7TC5#key-1",
                "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..LvCQ4TWhFHOkwMzvrx-TxHovoaCLPlK2taHxQUtUOp0Uc_jYbjL3XgVR2u6jVMvGIdPt4gs-VZb49f7GuiXFDA"
            }
        }
    ]
}
""")
                }.apply {
                    // important is that it isn't Unauthorized
                    assertNotEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
            assertTrue(exception.message!!.contains("Error: no verification methods"))

            // clean up
            runBlocking {
                walletService.deleteWallet(DEFAULT_BPN)
            }

            assertEquals(0, walletService.getAll().size)
        }
    }

    @Test
    fun testWalletCrudExceptions() {
        withTestApplication({
            setupEnvironment(environment)
            configurePersistence()
            configureSecurity()
            configureOpenAPI()
            configureRouting(walletService)
            appRoutes(walletService, bpdService)
            configureSerialization()
        }) {

            // create base wallet
            runBlocking {
                walletService.createWallet(WalletCreateDto(DEFAULT_BPN, "name1"))
            }
            var exception = assertFailsWith<BadRequestException> {
                handleRequest(HttpMethod.Post, "/api/wallets") {
                    addHeader(HttpHeaders.Authorization, "Bearer $CREATE_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())       
                    setBody("wrong:json")
                }.apply {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
            assertTrue(exception.message!!.contains("wrong"))
            exception = assertFailsWith<BadRequestException> {
                handleRequest(HttpMethod.Post, "/api/wallets") {
                    addHeader(HttpHeaders.Authorization, "Bearer $CREATE_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"wrong":"json"}""")
                }.apply {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
            assertTrue(exception.message!!.contains("required"))
            exception = assertFailsWith<BadRequestException> {
                handleRequest(HttpMethod.Post, "/api/wallets") {
                    addHeader(HttpHeaders.Authorization, "Bearer $CREATE_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"bpn":null}""")
                }.apply {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
            assertTrue(exception.message!!.contains("null"))
            exception = assertFailsWith<BadRequestException> {
                handleRequest(HttpMethod.Post, "/api/wallets") {
                    addHeader(HttpHeaders.Authorization, "Bearer $CREATE_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"bpn":"bpn2", "name": null}""")
                }.apply {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
            assertTrue(exception.message!!.contains("null"))
            var iae = assertFailsWith<BadRequestException> {
                handleRequest(HttpMethod.Post, "/api/wallets") {
                    addHeader(HttpHeaders.Authorization, "Bearer $CREATE_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"bpn":"", "name": "name2"}""")
                }.apply {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
            assertEquals("Field 'bpn' is required not to be blank, but it was blank", iae.message)
            iae = assertFailsWith<BadRequestException> {
                handleRequest(HttpMethod.Post, "/api/wallets") {
                    addHeader(HttpHeaders.Authorization, "Bearer $CREATE_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"bpn":"bpn2", "name": ""}""")
                }.apply {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
            assertEquals("Field 'name' is required not to be blank, but it was blank", iae.message)

            // programmatically add a wallet
            runBlocking {
                walletService.createWallet(WalletCreateDto("bpn4", "name4"))
            }

            var ce = assertFailsWith<ConflictException> {
                handleRequest(HttpMethod.Post, "/api/wallets") {
                    addHeader(HttpHeaders.Authorization, "Bearer $CREATE_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("""{"bpn":"bpn4", "name": "name4"}""")
                }.apply {
                    assertEquals(HttpStatusCode.Created, response.status())
                }
            }
            assertEquals("Wallet with identifier bpn4 already exists!", ce.message)

            // clean up created wallets
            runBlocking {
                walletService.deleteWallet("bpn4")
                walletService.deleteWallet(DEFAULT_BPN) // Catena-X wallet
                assertEquals(0, walletService.getAll().size)
            }
        }
    }

    @Test
    fun testRegisterBaseWallet() {
        withTestApplication({
            setupEnvironment(environment)
            configurePersistence()
            configureOpenAPI()
            configureSecurity()
            configureRouting(walletService)
            appRoutes(walletService, bpdService)
            configureSerialization()
        }) {

            var verKey: String

            // programmatically add base wallet and an additional one
            runBlocking {
                val createdWallet = walletService.createWallet(WalletCreateDto(DEFAULT_BPN, "base"))
                verKey = createdWallet.verKey!!
                walletService.createWallet(WalletCreateDto("non_base_bpn", "non_base"))
            }

            var exception = assertFailsWith<NotFoundException> {
                handleRequest(HttpMethod.Post, "/api/wallets/non_existing_bpn/public") {
                    addHeader(HttpHeaders.Authorization, "Bearer $UPDATE_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("{\"verKey\":\"$verKey\"}")
                }.apply {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
            assertTrue(exception.message!!.contains("non_existing_bpn not found"))

            exception = assertFailsWith<NotFoundException> {
               handleRequest(HttpMethod.Post, "/api/wallets/non_base_bpn/public") {
                    addHeader(HttpHeaders.Authorization, "Bearer $UPDATE_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody("{\"verKey\":\"" + verKey + "\"}")
                }.apply {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
            assertTrue(exception.message!!.contains("wallet but the base wallet"))

            handleRequest(HttpMethod.Post, "/api/wallets/$DEFAULT_BPN/public") {
                addHeader(HttpHeaders.Authorization, "Bearer $UPDATE_TOKEN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("{\"verKey\":\"$verKey\"}")
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }

            // clean up created wallets
            runBlocking {
                walletService.deleteWallet(DEFAULT_BPN) // base wallet
                walletService.deleteWallet("non_base_bpn")
                assertEquals(0, walletService.getAll().size)
            }

        }
    }

    @Test
    fun testDataUpdate() {
        withTestApplication({
            setupEnvironment(environment)
            configurePersistence()
            configureOpenAPI()
            configureSecurity()
            configureRouting(walletService)
            appRoutes(walletService, bpdService)
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Post, "/api/businessPartnerData") {
                addHeader(HttpHeaders.Authorization, "Bearer $UPDATE_TOKEN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())
            }
        }
    }

    @Test
    fun testVerifyPresentation() {
        withTestApplication({
            setupEnvironment(environment)
            configurePersistence()
            configureOpenAPI()
            configureSecurity()
            configureRouting(walletService)
            appRoutes(walletService, bpdService)
            configureSerialization()
        }) {
            // programmatically add base wallet and an additional one
            runBlocking {
                walletService.createWallet(WalletCreateDto(DEFAULT_BPN, "base"))
            }

            var validVP = """
                {
                    "@context": [
                        "https://www.w3.org/2018/credentials/v1"
                    ],
                    "id": "73e9e2f1-c0f9-4453-9619-d26244c83f15",
                    "type": [
                        "VerifiablePresentation"
                    ],
                    "holder": "did:indy:local:test:AA5EEDcn8yTfMobaTcabj9",
                    "verifiableCredential": [
                        {
                            "id": "http://example.edu/credentials/3735",
                            "@context": [
                                "https://www.w3.org/2018/credentials/v1",
                                "https://www.w3.org/2018/credentials/examples/v1"
                            ],
                            "type": [
                                "University-Degree-Credential",
                                "VerifiableCredential"
                            ],
                            "issuer": "did:indy:local:test:LCNSw1JxSTDw7EpR1UMG7D",
                            "issuanceDate": "2021-06-16T18:56:59Z",
                            "expirationDate": "2026-06-17T18:56:59Z",
                            "credentialSubject": {
                                "givenName": "TestAfterQuestion",
                                "familyName": "Student",
                                "degree": {
                                    "type": "Master",
                                    "degreeType": "Undergraduate",
                                    "name": "Master of Test"
                                },
                                "college": "Test",
                                "id": "did:indy:local:test:AA5EEDcn8yTfMobaTcabj9"
                            },
                            "proof": {
                                "type": "Ed25519Signature2018",
                                "created": "2022-07-12T12:13:16Z",
                                "proofPurpose": "assertionMethod",
                                "verificationMethod": "did:indy:local:test:LCNSw1JxSTDw7EpR1UMG7D#key-1",
                                "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..0_1pSjyxk4MCPkaatFlv78rTiE6JkI4iXM9QEOPwIGwLiyORkkKPe6TwaHoVvuarouC7ozpGZxWEGmVRqfiWDg"
                            }
                        },
                        {
                            "id": "http://example.edu/credentials/3735",
                            "@context": [
                                "https://www.w3.org/2018/credentials/v1",
                                "https://www.w3.org/2018/credentials/examples/v1"
                            ],
                            "type": [
                                "University-Degree-Credential",
                                "VerifiableCredential"
                            ],
                            "issuer": "did:indy:local:test:LCNSw1JxSTDw7EpR1UMG7D",
                            "issuanceDate": "2021-06-16T18:56:59Z",
                            "expirationDate": "2027-06-17T18:56:59Z",
                            "credentialSubject": {
                                "givenName": "TestAfterQuestion",
                                "familyName": "Student",
                                "degree": {
                                    "type": "Master1",
                                    "degreeType": "Undergraduate2",
                                    "name": "Master of Test1"
                                },
                                "college": "Test2",
                                "id": "did:indy:local:test:AA5EEDcn8yTfMobaTcabj9"
                            },
                            "proof": {
                                "type": "Ed25519Signature2018",
                                "created": "2022-07-12T12:16:45Z",
                                "proofPurpose": "assertionMethod",
                                "verificationMethod": "did:indy:local:test:LCNSw1JxSTDw7EpR1UMG7D#key-1",
                                "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..6oIPVm3ealRVzpgiFKItyIzVWlNUT150fbh9OcBElj9FvaICAd-wc1yzrwka3ns1SmrPFsWIIe0wC1rJQLISBA"
                            }
                        }
                    ],
                    "proof": {
                        "type": "Ed25519Signature2018",
                        "created": "2022-07-12T12:28:44Z",
                        "proofPurpose": "assertionMethod",
                        "verificationMethod": "did:indy:local:test:AA5EEDcn8yTfMobaTcabj9#key-1",
                        "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..FYkZonVoXojBcwC3yWvhiyBh4uR0hNZR1qyu5cZS5_PXiB8BEyKUolWzqBAX_u7bbKD5QGqbTECs9qLyD63wAg"
                    }
                }
            """.trimIndent()
            handleRequest(HttpMethod.Post, "/api/presentations/validation") {
                addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN")
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(validVP)
            }.apply {
                var output = Json.decodeFromString<VerifyResponse>(response.content!!)
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue { output.valid }
            }

            var vpWithoutProof = """
                {
                    "@context": [
                        "https://www.w3.org/2018/credentials/v1"
                    ],
                    "id": "73e9e2f1-c0f9-4453-9619-d26244c83f15",
                    "type": [
                        "VerifiablePresentation"
                    ],
                    "holder": "did:indy:local:test:AA5EEDcn8yTfMobaTcabj9",
                    "verifiableCredential": [
                        {
                            "id": "http://example.edu/credentials/3735",
                            "@context": [
                                "https://www.w3.org/2018/credentials/v1",
                                "https://www.w3.org/2018/credentials/examples/v1"
                            ],
                            "type": [
                                "University-Degree-Credential",
                                "VerifiableCredential"
                            ],
                            "issuer": "did:indy:local:test:LCNSw1JxSTDw7EpR1UMG7D",
                            "issuanceDate": "2021-06-16T18:56:59Z",
                            "expirationDate": "2026-06-17T18:56:59Z",
                            "credentialSubject": {
                                "givenName": "TestAfterQuestion",
                                "familyName": "Student",
                                "degree": {
                                    "type": "Master",
                                    "degreeType": "Undergraduate",
                                    "name": "Master of Test"
                                },
                                "college": "Test",
                                "id": "did:indy:local:test:AA5EEDcn8yTfMobaTcabj9"
                            },
                            "proof": {
                                "type": "Ed25519Signature2018",
                                "created": "2022-07-12T12:13:16Z",
                                "proofPurpose": "assertionMethod",
                                "verificationMethod": "did:indy:local:test:LCNSw1JxSTDw7EpR1UMG7D#key-1",
                                "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..0_1pSjyxk4MCPkaatFlv78rTiE6JkI4iXM9QEOPwIGwLiyORkkKPe6TwaHoVvuarouC7ozpGZxWEGmVRqfiWDg"
                            }
                        }
                    ]
                }
            """.trimIndent()

            var exception = assertFailsWith<UnprocessableEntityException> {
                handleRequest(HttpMethod.Post, "/api/presentations/validation") {
                    addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(vpWithoutProof)
                }.apply { }
            }
            assertTrue(exception.message!!.contains("Cannot verify verifiable presentation due to missing proof"))

            var  vpWithOutdatedVC = """
                {
                    "@context": [
                        "https://www.w3.org/2018/credentials/v1"
                    ],
                    "id": "0c96720a-734d-41ea-89ca-92b4f8ba2fa8",
                    "type": [
                        "VerifiablePresentation"
                    ],
                    "holder": "did:indy:local:test:YHXZLLSLnKxz5D2HQaKXcP",
                    "verifiableCredential": [
                        {
                            "id": "http://example.edu/credentials/3735",
                            "@context": [
                                "https://www.w3.org/2018/credentials/v1",
                                "https://www.w3.org/2018/credentials/examples/v1"
                            ],
                            "type": [
                                "University-Degree-Credential",
                                "VerifiableCredential"
                            ],
                            "issuer": "did:indy:local:test:M6Mis1fZKuhEw71GNY3TAb",
                            "issuanceDate": "2021-06-16T18:56:59Z",
                            "expirationDate": "2021-06-17T18:56:59Z",
                            "credentialSubject": {
                                "givenName": "TestAfterQuestion",
                                "familyName": "Student",
                                "degree": {
                                    "type": "Master1",
                                    "degreeType": "Undergraduate2",
                                    "name": "Master of Test11"
                                },
                                "college": "Test2",
                                "id": "did:indy:local:test:YHXZLLSLnKxz5D2HQaKXcP"
                            },
                            "proof": {
                                "type": "Ed25519Signature2018",
                                "created": "2022-07-13T14:18:56Z",
                                "proofPurpose": "assertionMethod",
                                "verificationMethod": "did:indy:local:test:M6Mis1fZKuhEw71GNY3TAb#key-1",
                                "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..qFl7sQ9-PUQwz7KV0ONn89AEDpx3DkUO_1LDYBHvdbw2FlPi_XM51pvh_6tx4fLwyMlZEp3VdAbxyRR-AdZWDw"
                            }
                        }
                    ],
                    "proof": {
                        "type": "Ed25519Signature2018",
                        "created": "2022-07-13T14:19:32Z",
                        "proofPurpose": "assertionMethod",
                        "verificationMethod": "did:indy:local:test:YHXZLLSLnKxz5D2HQaKXcP#key-1",
                        "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..lAbqnkVHOzt5GGuTebAgqBdt0p5vZvn7Z4dIarKPW3_BCSv9ATDzegkjzqOM3B91WP7flp93fgqmq5T-bT9YBw"
                    }
                }
            """.trimIndent()

            var dateException = assertFailsWith<UnprocessableEntityException> {
                handleRequest(HttpMethod.Post, "/api/presentations/validation?withDateValidation=true") {
                    addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(vpWithOutdatedVC)
                }.apply { }
            }
            assertTrue(dateException.message!!.contains(
                "Verifiable credential http://example.edu/credentials/3735 expired 2021-06-17T18:56:59Z"))

            var vpWithFutureVC = """
                 {
                    "@context": [
                        "https://www.w3.org/2018/credentials/v1"
                    ],
                    "id": "7aed00f7-8e04-4093-b467-9bd084b42086",
                    "type": [
                        "VerifiablePresentation"
                    ],
                    "holder": "did:indy:local:test:YHXZLLSLnKxz5D2HQaKXcP",
                    "verifiableCredential": [
                        {
                            "id": "http://example.edu/credentials/3888",
                            "@context": [
                                "https://www.w3.org/2018/credentials/v1",
                                "https://www.w3.org/2018/credentials/examples/v1"
                            ],
                            "type": [
                                "University-Degree-Credential",
                                "VerifiableCredential"
                            ],
                            "issuer": "did:indy:local:test:M6Mis1fZKuhEw71GNY3TAb",
                            "issuanceDate": "2999-06-16T18:56:59Z",
                            "expirationDate": "2999-06-17T18:56:59Z",
                            "credentialSubject": {
                                "givenName": "TestAfterQuestion",
                                "familyName": "Student",
                                "degree": {
                                    "type": "Master1",
                                    "degreeType": "Undergraduate2",
                                    "name": "Master of Test11"
                                },
                                "college": "Test2",
                                "id": "did:indy:local:test:YHXZLLSLnKxz5D2HQaKXcP"
                            },
                            "proof": {
                                "type": "Ed25519Signature2018",
                                "created": "2022-07-21T13:17:21Z",
                                "proofPurpose": "assertionMethod",
                                "verificationMethod": "did:indy:local:test:M6Mis1fZKuhEw71GNY3TAb#key-1",
                                "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..CvGRIw0aqQrXsXy1n3ChGfN1xs0Y56eiwS3spTlf_Ph4l5OQSFKId7SKNxBpFfI4GaQMKi8ajDVXvaIdT-N0DA"
                            }
                        }
                    ],
                    "proof": {
                        "type": "Ed25519Signature2018",
                        "created": "2022-07-21T13:18:07Z",
                        "proofPurpose": "assertionMethod",
                        "verificationMethod": "did:indy:local:test:YHXZLLSLnKxz5D2HQaKXcP#key-1",
                        "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..pnipnhAJ34b9k8kBpRJfEAOdbiaSZK38TAJveSYyoBrKAMhF3DAJ_b0pChHvgghzy9QiAsal5ZFkl5fakIGwAg"
                    }
                }
            """.trimIndent()

            var issuanceDateException = assertFailsWith<UnprocessableEntityException> {
                handleRequest(HttpMethod.Post, "/api/presentations/validation?withDateValidation=true") {
                    addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(vpWithFutureVC)
                }.apply { }
            }
            assertTrue(issuanceDateException.message!!.contains(
                "Invalid issuance date 2999-06-16T18:56:59Z " +
                        "in verifiable credential http://example.edu/credentials/3888"))

            var vpWithVcWithoutProof = """
                {
                    "@context": [
                        "https://www.w3.org/2018/credentials/v1"
                    ],
                    "id": "d312945e-826e-49cc-9baa-3c78d090745b",
                    "type": [
                        "VerifiablePresentation"
                    ],
                    "holder": "did:indy:local:test:YHXZLLSLnKxz5D2HQaKXcP",
                    "verifiableCredential": [
                        {
                            "id": "http://example.edu/credentials/3735",
                            "@context": [
                                "https://www.w3.org/2018/credentials/v1",
                                "https://www.w3.org/2018/credentials/examples/v1"
                            ],
                            "type": [
                                "University-Degree-Credential",
                                "VerifiableCredential"
                            ],
                            "issuer": "did:indy:local:test:M6Mis1fZKuhEw71GNY3TAb",
                            "issuanceDate": "2025-06-16T18:56:59Z",
                            "expirationDate": "2026-06-17T18:56:59Z",
                            "credentialSubject": {
                                "givenName": "TestAfterQuestion",
                                "familyName": "Student",
                                "degree": {
                                    "type": "Master1",
                                    "degreeType": "Undergraduate2",
                                    "name": "Master of Test11"
                                },
                                "college": "Test2",
                                "id": "did:indy:local:test:YHXZLLSLnKxz5D2HQaKXcP"
                            }
                        }
                    ],
                    "proof": {
                        "type": "Ed25519Signature2018",
                        "created": "2022-07-13T14:47:36Z",
                        "proofPurpose": "assertionMethod",
                        "verificationMethod": "did:indy:local:test:YHXZLLSLnKxz5D2HQaKXcP#key-1",
                        "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..fGJqT596Y9696mw97DVFkNZsuXU5xO-VCZWkEysOaeljl6loRZkQAVGmyzfZK4ZImcLKMFwHfgLv1E-Xxze7Bw"
                    }
                }
            """.trimIndent()
            var exceptionVCMissingProof = assertFailsWith<UnprocessableEntityException> {
                handleRequest(HttpMethod.Post, "/api/presentations/validation") {
                    addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(vpWithVcWithoutProof)
                }.apply { }
            }
            assertTrue(exceptionVCMissingProof.message!!.contains("Cannot verify verifiable credential" +
                    " http://example.edu/credentials/3735 due to missing proof"))

            var vpWithoutHolder = """
            {
                "@context": [
                    "https://www.w3.org/2018/credentials/v1"
                ],
                "id": "73e9e2f1-c0f9-4453-9619-d26244c83f15",
                "type": [
                    "VerifiablePresentation"
                ],
                "verifiableCredential": [
                    {
                        "id": "http://example.edu/credentials/3735",
                        "@context": [
                            "https://www.w3.org/2018/credentials/v1",
                            "https://www.w3.org/2018/credentials/examples/v1"
                        ],
                        "type": [
                            "University-Degree-Credential",
                            "VerifiableCredential"
                        ],
                        "issuer": "did:indy:local:test:LCNSw1JxSTDw7EpR1UMG7D",
                        "issuanceDate": "2021-06-16T18:56:59Z",
                        "expirationDate": "2026-06-17T18:56:59Z",
                        "credentialSubject": {
                            "givenName": "TestAfterQuestion",
                            "familyName": "Student",
                            "degree": {
                                "type": "Master",
                                "degreeType": "Undergraduate",
                                "name": "Master of Test"
                            },
                            "college": "Test",
                            "id": "did:indy:local:test:AA5EEDcn8yTfMobaTcabj9"
                        },
                        "proof": {
                            "type": "Ed25519Signature2018",
                            "created": "2022-07-12T12:13:16Z",
                            "proofPurpose": "assertionMethod",
                            "verificationMethod": "did:indy:local:test:LCNSw1JxSTDw7EpR1UMG7D#key-1",
                            "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..0_1pSjyxk4MCPkaatFlv78rTiE6JkI4iXM9QEOPwIGwLiyORkkKPe6TwaHoVvuarouC7ozpGZxWEGmVRqfiWDg"
                        }
                    }
                ],
                "proof": {
                    "type": "Ed25519Signature2018",
                    "created": "2022-07-12T12:28:44Z",
                    "proofPurpose": "assertionMethod",
                    "verificationMethod": "did:indy:local:test:AA5EEDcn8yTfMobaTcabj9#key-1",
                    "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..FYkZonVoXojBcwC3yWvhiyBh4uR0hNZR1qyu5cZS5_PXiB8BEyKUolWzqBAX_u7bbKD5QGqbTECs9qLyD63wAg"
                }
            }
            """.trimIndent()
            handleRequest(HttpMethod.Post, "/api/presentations/validation") {
                    addHeader(HttpHeaders.Authorization, "Bearer $VIEW_TOKEN")
                    addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(vpWithoutHolder)
                }.apply {
                    var output = Json.decodeFromString<VerifyResponse>(response.content!!)
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertTrue { output.valid }
                }

            // clean up created wallets
            runBlocking {
                walletService.deleteWallet(DEFAULT_BPN) // base wallet
                assertEquals(0, walletService.getAll().size)
            }

        }
    }
}
