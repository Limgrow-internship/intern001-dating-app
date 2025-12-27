package com.intern001.dating.common.performance

import android.util.Log
import com.intern001.dating.BuildConfig

/**
 * Utility to compare performance before and after refactoring
 * Usage: Call markBeforeRefactor() before changes, markAfterRefactor() after changes
 */
object PerformanceComparison {
    private const val TAG = "PerformanceComparison"
    private val enabled = BuildConfig.DEBUG

    private val beforeMeasurements = mutableMapOf<String, MutableList<Long>>()
    private val afterMeasurements = mutableMapOf<String, MutableList<Long>>()

    /**
     * Record a measurement before refactoring
     */
    fun recordBefore(operationName: String, timeMs: Long) {
        if (!enabled) return
        beforeMeasurements.getOrPut(operationName) { mutableListOf() }.add(timeMs)
    }

    /**
     * Record a measurement after refactoring
     */
    fun recordAfter(operationName: String, timeMs: Long) {
        if (!enabled) return
        afterMeasurements.getOrPut(operationName) { mutableListOf() }.add(timeMs)
    }

    /**
     * Print comparison report
     */
    fun printComparisonReport() {
        if (!enabled) return

        Log.d(TAG, "═══════════════════════════════════════════════════════")
        Log.d(TAG, "📊 PERFORMANCE COMPARISON REPORT")
        Log.d(TAG, "═══════════════════════════════════════════════════════")

        val allOperations = (beforeMeasurements.keys + afterMeasurements.keys).distinct()

        if (allOperations.isEmpty()) {
            Log.d(TAG, "No measurements recorded yet")
            return
        }

        allOperations.forEach { operation ->
            val beforeTimes = beforeMeasurements[operation] ?: emptyList()
            val afterTimes = afterMeasurements[operation] ?: emptyList()

            if (beforeTimes.isEmpty() && afterTimes.isEmpty()) {
                return@forEach
            }

            val beforeAvg = if (beforeTimes.isNotEmpty()) beforeTimes.average() else 0.0
            val afterAvg = if (afterTimes.isNotEmpty()) afterTimes.average() else 0.0

            val improvement = if (beforeAvg > 0) {
                ((beforeAvg - afterAvg) / beforeAvg * 100)
            } else {
                0.0
            }

            val improvementText = when {
                improvement > 0 -> "✅ Improved by ${String.format("%.1f", improvement)}%"
                improvement < 0 -> "❌ Slower by ${String.format("%.1f", -improvement)}%"
                else -> "➡️ No change"
            }

            Log.d(TAG, "")
            Log.d(TAG, "Operation: $operation")
            if (beforeTimes.isNotEmpty()) {
                Log.d(
                    TAG,
                    "  Before: avg=${String.format("%.2f", beforeAvg)}ms, " +
                        "min=${beforeTimes.minOrNull()}ms, " +
                        "max=${beforeTimes.maxOrNull()}ms, " +
                        "count=${beforeTimes.size}",
                )
            }
            if (afterTimes.isNotEmpty()) {
                Log.d(
                    TAG,
                    "  After:  avg=${String.format("%.2f", afterAvg)}ms, " +
                        "min=${afterTimes.minOrNull()}ms, " +
                        "max=${afterTimes.maxOrNull()}ms, " +
                        "count=${afterTimes.size}",
                )
            }
            if (beforeTimes.isNotEmpty() && afterTimes.isNotEmpty()) {
                Log.d(TAG, "  $improvementText")
                Log.d(TAG, "  Time saved: ${String.format("%.2f", beforeAvg - afterAvg)}ms per operation")
            }
        }

        Log.d(TAG, "")
        Log.d(TAG, "═══════════════════════════════════════════════════════")
    }

    /**
     * Clear all measurements
     */
    fun clear() {
        beforeMeasurements.clear()
        afterMeasurements.clear()
    }

    /**
     * Get improvement percentage for an operation
     */
    fun getImprovementPercentage(operationName: String): Double {
        val beforeTimes = beforeMeasurements[operationName] ?: return 0.0
        val afterTimes = afterMeasurements[operationName] ?: return 0.0

        if (beforeTimes.isEmpty() || afterTimes.isEmpty()) return 0.0

        val beforeAvg = beforeTimes.average()
        val afterAvg = afterTimes.average()

        return if (beforeAvg > 0) {
            ((beforeAvg - afterAvg) / beforeAvg * 100)
        } else {
            0.0
        }
    }
}
