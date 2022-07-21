package net.catenax.core.managedidentitywallets.services

import io.ktor.client.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.observer.*
import kotlinx.coroutines.Deferred
import net.catenax.core.managedidentitywallets.models.BPMDConfig

interface BusinessPartnerDataService {

    suspend fun pullDataAndUpdateCatenaXCredentialsAsync()

    suspend fun<T> issueAndStoreCatenaXCredentialsAsync(
        bpn: String,
        type: String,
        data: T? = null
    ): Deferred<Boolean>

    companion object {
        fun createBusinessPartnerDataService(walletService: WalletService,
                                             bpmdConfig: BPMDConfig
        ): BusinessPartnerDataService {
            return BusinessPartnerDataServiceImpl(
                walletService,
                bpmdConfig,
                HttpClient() {
                    expectSuccess = true
                    install(ResponseObserver) {
                        onResponse { response ->
                            println("HTTP status: ${response.status.value}")
                            println("HTTP description: ${response.status.description}")
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
