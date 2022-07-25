package net.catenax.core.managedidentitywallets.services

import io.ktor.client.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.observer.*
import kotlinx.coroutines.Deferred
import net.catenax.core.managedidentitywallets.models.BPDMConfig
import org.slf4j.LoggerFactory

interface BusinessPartnerDataService {

    suspend fun pullDataAndUpdateCatenaXCredentialsAsync()

    suspend fun<T> issueAndStoreCatenaXCredentialsAsync(
        bpn: String,
        type: String,
        data: T? = null
    ): Deferred<Boolean>

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)

        fun createBusinessPartnerDataService(walletService: WalletService,
                                             bpdmConfig: BPDMConfig
        ): BusinessPartnerDataService {
            return BusinessPartnerDataServiceImpl(
                walletService,
                bpdmConfig,
                HttpClient() {
                    expectSuccess = true
                    install(ResponseObserver) {
                        onResponse { response ->
                            log.debug("HTTP status: ${response.status.value}")
                            log.debug("HTTP description: ${response.status.description}")
                        }
                    }
                    install(Logging) {
                        logger = Logger.DEFAULT
                        level = LogLevel.BODY
                    }
                })
            }
    }
}
