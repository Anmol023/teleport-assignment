package com.example.teleport_assignment.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties("load-optimizer")
data class LoadOptimizerConfig(
    val strategy: OptimizationStrategy = OptimizationStrategy.MAX_PAYOUT,
    val weights: StrategyWeights = StrategyWeights()
){
    val total = weights.volumeUtilization?.let { weights.weightUtilization?.let { weights.payout?.plus(it) }?.plus(it) }
    init {
        if(strategy.name == OptimizationStrategy.BALANCED.name){
            require(total == 100 && weights.payout != null && weights.weightUtilization != null && weights.volumeUtilization != null) {
                "Strategy weights must sum to 100, got: $total"
            }
        }
    }
}

data class StrategyWeights(
    val payout: Int? = 60,
    val weightUtilization: Int? = 20,
    val volumeUtilization: Int? = 20
)

enum class OptimizationStrategy {
    MAX_PAYOUT,
    MAX_UTILIZATION,
    BALANCED
}