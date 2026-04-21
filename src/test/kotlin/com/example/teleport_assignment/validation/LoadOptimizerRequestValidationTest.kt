package com.example.teleport_assignment.validation

import com.example.teleport_assignment.exchange_model.LoadOptimizerRequest
import com.example.teleport_assignment.model.Order
import com.example.teleport_assignment.model.Truck
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class LoadOptimizerRequestValidationTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `valid request should have no violations`() {
        val request = LoadOptimizerRequest(
            truck = Truck("truck-1", 44_000, 3_000),
            orders = listOf(validOrder("ord-001"))
        )

        val violations = validator.validate(request)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `orders must not be empty`() {
        val request = LoadOptimizerRequest(
            truck = Truck("truck-1", 44_000, 3_000),
            orders = emptyList()
        )

        val violations = validator.validate(request)

        assertTrue(violations.any { it.propertyPath.toString() == "orders" })
        assertTrue(violations.any { it.message == "orders must not be empty" })
    }

    @Test
    fun `orders size must be less than or equal to 25`() {
        val orders = (1..26).map { validOrder("ord-$it") }
        val request = LoadOptimizerRequest(
            truck = Truck("truck-1", 44_000, 3_000),
            orders = orders
        )

        val violations = validator.validate(request)

        assertTrue(violations.any { it.propertyPath.toString() == "orders" })
        assertTrue(violations.any { it.message == "orders must be <= 25" })
    }

    @Test
    fun `nested truck validation should fail for blank id`() {
        val request = LoadOptimizerRequest(
            truck = Truck("   ", 44_000, 3_000),
            orders = listOf(validOrder("ord-001"))
        )

        val violations = validator.validate(request)

        assertTrue(violations.any { it.propertyPath.toString() == "truck.id" })
        assertTrue(violations.any { it.message == "truck.id is required" })
    }

    @Test
    fun `nested order validation should fail for non-positive payout`() {
        val badOrder = validOrder("ord-001").copy(payoutCents = 0)
        val request = LoadOptimizerRequest(
            truck = Truck("truck-1", 44_000, 3_000),
            orders = listOf(badOrder)
        )

        val violations = validator.validate(request)
        print(violations)
        assertTrue(violations.any {
            it.propertyPath.toString().contains("orders[0].payoutCents")
        })
        assertTrue(violations.any { it.message == "order.payout_cents must be greater than 0" })
    }

    private fun validOrder(id: String): Order =
        Order(
            id = id,
            payoutCents = 100_000,
            weightLbs = 1_000,
            volumeCuft = 100,
            origin = "A",
            destination = "B",
            pickupDate = LocalDate.parse("2026-04-21"),
            deliveryDate = LocalDate.parse("2026-04-22"),
            isHazmat = false
        )
}
