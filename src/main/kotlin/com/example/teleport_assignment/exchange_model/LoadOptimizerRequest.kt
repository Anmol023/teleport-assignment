package com.example.teleport_assignment.exchange_model

import com.example.teleport_assignment.model.Order
import com.example.teleport_assignment.model.Truck
import jakarta.validation.Valid

data class LoadOptimizerRequest(
    @field:Valid
    val truck: Truck,
    val orders: List<Order>
)
