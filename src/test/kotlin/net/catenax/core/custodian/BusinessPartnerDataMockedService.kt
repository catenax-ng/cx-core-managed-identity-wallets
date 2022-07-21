package net.catenax.core.managedidentitywallets

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.catenax.core.managedidentitywallets.models.*
import net.catenax.core.managedidentitywallets.services.BusinessPartnerDataService

class BusinessPartnerDataMockedService: BusinessPartnerDataService {

    override suspend fun pullDataAndUpdateCatenaXCredentialsAsync() {}

    override suspend fun <T> issueAndStoreCatenaXCredentialsAsync(
        bpn: String,
        type: String,
        data: T?
    ): Deferred<Boolean> {
        return CompletableDeferred(true)
    }

}
