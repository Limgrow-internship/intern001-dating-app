package com.intern001.dating.data.billing

object BillingConfig {
    object InAppProducts {
        const val NO_ADS = "test1"

        fun getNoAdsProductId(): String = NO_ADS
    }

    object Subscriptions {
        const val BASIC = "basic.test"
        const val GOLD = "gold.test"
        const val ELITE = "elite.test"

        fun getAllSubscriptionIds(): List<String> = listOf(BASIC, GOLD, ELITE)
    }

    object RetryConfig {
        const val RECONNECT_DELAY_MS = 3000L
    }
}
