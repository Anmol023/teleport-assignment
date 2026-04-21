package com.example.teleport_assignment.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Truck(
    val id: String,
    @JsonProperty("max_weight_lbs")
    val maxWieghtLbs: Int,
    @JsonProperty("max_volume_cuft")
    val maxVolumeCuft: Int
)