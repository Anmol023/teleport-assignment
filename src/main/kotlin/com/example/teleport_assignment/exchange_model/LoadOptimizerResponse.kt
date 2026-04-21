package com.example.teleport_assignment.exchange_model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class LoadOptimizerResponse(
    @JsonProperty("truck_id")
    val truckId: String,
    @JsonProperty("selected_order_ids")
    val selectedOrderIds: List<String>,
    @JsonProperty("total_payout_cents")
    val totalPayoutCents: Long,
    @JsonProperty("total_weight_lbs")
    val totalWeightLbs: Int,
    @JsonProperty("total_volume_cuft")
    val totalVolumeCuft: Int,
    @JsonProperty("utilization_weight_percent")
    val utilizationWeightPercent: Number,
    @JsonProperty("utilization_volume_percent")
    val utilizationVolumePercent: Number
){
    companion object{
        fun buildEmptyResponse(truckId: String) = LoadOptimizerResponse(
            truckId = truckId,
            selectedOrderIds = emptyList(),
            totalPayoutCents = 0,
            totalWeightLbs = 0,
            totalVolumeCuft = 0,
            utilizationWeightPercent = 0.0,
            utilizationVolumePercent = 0.0
        )
    }
}