package com.example.teleport_assignment.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties("load-optimizer")
data class LoadOptimizerConfig(
    @DefaultValue("MAX_PAYOUT")
    val strategy: OptimizationStrategy = OptimizationStrategy.MAX_PAYOUT,
    val weights: StrategyWeights = StrategyWeights()
)

data class StrategyWeights(
    @DefaultValue("60") val payout: Int = 60,
    @DefaultValue("20") val weightUtilization: Int = 20,
    @DefaultValue("20") val volumeUtilization: Int = 20
) {
    val total = payout + weightUtilization + volumeUtilization
    init {
        require(payout + weightUtilization + volumeUtilization == 100) {
            "Strategy weights must sum to 100, got: ${payout + weightUtilization + volumeUtilization}"
        }
    }
}

enum class OptimizationStrategy {
    MAX_PAYOUT,
    MAX_UTILIZATION,
    BALANCED
}