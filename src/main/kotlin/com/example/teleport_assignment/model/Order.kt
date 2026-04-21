package com.example.teleport_assignment.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDate

data class Order(
    @field:NotBlank(message = "order.id is required")
    val id: String,
    @field:Positive(message = "order.payout_cents must be greater than 0")
    @JsonProperty("payout_cents")
    val payoutCents: Long,
    @field:Positive(message = "order.weight_lbs must be greater than 0")
    @JsonProperty("weight_lbs")
    val weightLbs: Int,
    @field:Positive(message = "order.volume_cuft must be greater than 0")
    @JsonProperty("volume_cuft")
    val volumeCuft: Int,
    @field:NotBlank(message = "order.origin is required")
    val origin: String,
    @field:NotBlank(message = "order.destination is required")
    val destination: String,
    @JsonProperty("pickup_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val pickupDate: LocalDate,
    @JsonProperty("delivery_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val deliveryDate: LocalDate,
    @JsonProperty("is_hazmat")
    val isHazmat: Boolean
)