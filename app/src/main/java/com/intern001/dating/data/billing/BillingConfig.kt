package com.intern001.dating.data.billing

object BillingConfig {

    /**
     * In-App Products (One-time purchases)
     */
    object InAppProducts {
        // Test product ID for development/testing
        const val NO_ADS_TEST = "android.test.purchased"

        // Production product ID
        const val NO_ADS = "no_ads"

        /**
         * Returns the active product ID based on build configuration
         * Currently using test product ID
         * TODO: Switch to NO_ADS for production release
         */
        fun getNoAdsProductId(): String = NO_ADS_TEST
    }

    /**
     * Subscription Products (Recurring purchases)
     * Add subscription product IDs here when needed
     */
    object Subscriptions {
        // Example: const val PREMIUM_MONTHLY = "premium_monthly"
        // Example: const val PREMIUM_YEARLY = "premium_yearly"
    }

    /**
     * Billing retry configuration
     */
    object RetryConfig {
        const val RECONNECT_DELAY_MS = 3000L
        const val MAX_RETRY_ATTEMPTS = 3
    }

    /**
     * Feature flags for billing
     */
    object Features {
        const val ENABLE_PENDING_PURCHASES = true
        const val ENABLE_ONE_TIME_PRODUCTS = true
    }
}
