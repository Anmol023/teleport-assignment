package com.example.teleport_assignment.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class Truck(
    @field:NotBlank(message = "truck.id is required")
    val id: String,
    @field:Positive(message = "truck.max_weight_lbs must be greater than 0")
    @JsonProperty("max_weight_lbs")
    val maxWieghtLbs: Int,
    @field:Positive(message = "truck.max_volume_cuft must be greater than 0")
    @JsonProperty("max_volume_cuft")
    val maxVolumeCuft: Int
)