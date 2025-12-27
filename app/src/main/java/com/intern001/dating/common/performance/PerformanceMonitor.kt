package com.intern001.dating.common.performance

import android.util.Log
import com.intern001.dating.BuildConfig
import kotlin.system.measureTimeMillis

/**
 * Performance monitoring utility to measure execution time of operations
 * Only active in DEBUG builds to avoid overhead in production
 */
object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    private val enabled = BuildConfig.DEBUG
    private val measurements = mutableMapOf<String, MutableList<Long>>()

    /**
     * Measure execution time of a block of code
     * @param operationName Name of the operation being measured
     * @param block Code block to measure
     * @return Result of the block execution
     */
    inline fun <T> measure(operationName: String, block: () -> T): T {
        if (!enabled) return block()

        val time = measureTimeMillis {
            // Block will be executed inline
        }
        val result: T
        val actualTime = measureTimeMillis {
            result = block()
        }

        recordMeasurement(operationName, actualTime)
        logMeasurement(operationName, actualTime)

        return result
    }

    /**
     * Measure execution time and return both result and time
     */
    inline fun <T> measureWithTime(operationName: String, block: () -> T): Pair<T, Long> {
        if (!enabled) {
            val result = block()
            return Pair(result, 0L)
        }

        val result: T
        val time = measureTimeMillis {
            result = block()
        }

        recordMeasurement(operationName, time)
        logMeasurement(operationName, time)

        return Pair(result, time)
    }

    /**
     * Get average time for an operation
     */
    fun getAverageTime(operationName: String): Double {
        val times = measurements[operationName] ?: return 0.0
        return if (times.isEmpty()) 0.0 else times.average()
    }

    /**
     * Get all measurements for an operation
     */
    fun getMeasurements(operationName: String): List<Long> {
        return measurements[operationName]?.toList() ?: emptyList()
    }

    /**
     * Clear all measurements
     */
    fun clearMeasurements() {
        measurements.clear()
    }

    /**
     * Print summary of all measurements
     */
    fun printSummary() {
        if (!enabled) return

        Log.d(TAG, "=== Performance Summary ===")
        measurements.forEach { (operation, times) ->
            val avg = times.average()
            val min = times.minOrNull() ?: 0L
            val max = times.maxOrNull() ?: 0L
            Log.d(TAG, "$operation: avg=${avg}ms, min=${min}ms, max=${max}ms, count=${times.size}")
        }
        Log.d(TAG, "========================")
    }

    private fun recordMeasurement(operationName: String, time: Long) {
        measurements.getOrPut(operationName) { mutableListOf() }.add(time)
        // Keep only last 100 measurements per operation
        measurements[operationName]?.let {
            if (it.size > 100) {
                it.removeAt(0)
            }
        }
    }

    private fun logMeasurement(operationName: String, time: Long) {
        if (time > 100) { // Only log slow operations (>100ms)
            Log.w(TAG, "⚠️ Slow operation: $operationName took ${time}ms")
        } else if (time > 50) { // Warn for moderately slow operations
            Log.d(TAG, "⏱️ $operationName took ${time}ms")
        }
    }
}
