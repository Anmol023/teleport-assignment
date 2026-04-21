package com.example.teleport_assignment.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class Order(
    val id: String,
    @JsonProperty("payout_cents")
    val payoutCents: Long,
    @JsonProperty("weight_lbs")
    val weightLbs: Int,
    @JsonProperty("volume_cuft")
    val volumeCuft: Int,
    val origin: String,
    val destination: String,
    @JsonProperty("pickup_date")
    val pickupDate: LocalDate,
    @JsonProperty("delivery_date")
    val deliveryDate: LocalDate,
    @JsonProperty("is_hazmat")
    val isHazmat: Boolean
)