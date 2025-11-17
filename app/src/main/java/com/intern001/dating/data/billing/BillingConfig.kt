package com.intern001.dating.data.billing

object BillingConfig {
    object InAppProducts {
        const val NO_ADS = "test1"

        fun getNoAdsProductId(): String = NO_ADS
    }

    object Subscriptions {
        const val WEEKLY = "com.ledkeyboard.weekly"
        const val MONTHLY = "monthly"
        const val YEARLY = "com.led.yearlytest"

        fun getAllSubscriptionIds(): List<String> = listOf(WEEKLY, MONTHLY, YEARLY)
    }

    object RetryConfig {
        const val RECONNECT_DELAY_MS = 3000L
    }
}
