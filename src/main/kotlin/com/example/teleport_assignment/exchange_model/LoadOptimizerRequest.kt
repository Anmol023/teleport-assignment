package com.example.teleport_assignment.exchange_model

import com.example.teleport_assignment.model.Order
import com.example.teleport_assignment.model.Truck
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class LoadOptimizerRequest(
    @field:Valid
    val truck: Truck,
    @field:NotEmpty(message = "orders must not be empty")
    @field:Size(max = 25, message = "orders must be <= 25") //as mentioned n will be till 22
    @field:Valid
    val orders: List<Order>
)
