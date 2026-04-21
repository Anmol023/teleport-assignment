package com.example.teleport_assignment.service

import com.example.teleport_assignment.exchange_model.LoadOptimizerRequest
import com.example.teleport_assignment.model.Order
import com.example.teleport_assignment.model.Truck
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class LoadOptimizerServiceTest {

    private val service = LoadOptimizerService()

    @Test
    fun `should select best combination`() {
        val request = LoadOptimizerRequest(
            truck = Truck("truck-1", 44_000, 3_000),
            orders = listOf(
                Order(
                    id = "ord-001",
                    payoutCents = 2_50_000,
                    weightLbs = 18_000,
                    volumeCuft = 1_200,
                    origin = "Los Angeles, CA",
                    destination = "Dallas, TX",
                    pickupDate = LocalDate.parse("2025-12-05"),
                    deliveryDate = LocalDate.parse("2025-12-09"),
                    isHazmat = false
                ),
                Order(
                    id = "ord-002",
                    payoutCents = 1_80_000,
                    weightLbs = 12_000,
                    volumeCuft = 900,
                    origin = "Los Angeles, CA",
                    destination = "Dallas, TX",
                    pickupDate = LocalDate.parse("2025-12-04"),
                    deliveryDate = LocalDate.parse("2025-12-10"),
                    isHazmat = false
                ),
                Order(
                    id = "ord-003",
                    payoutCents = 3_20_000,
                    weightLbs = 30_000,
                    volumeCuft = 1_800,
                    origin = "Los Angeles, CA",
                    destination = "Dallas, TX",
                    pickupDate = LocalDate.parse("2025-12-06"),
                    deliveryDate = LocalDate.parse("2025-12-08"),
                    isHazmat = true
                )
            )
        )

        val result = service.optimizeLoad(request)

        listOf("ord-002", "ord-001") shouldContainExactlyInAnyOrder result.selectedOrderIds
        assertEquals(4_30_000L, result.totalPayoutCents)
        assertEquals(30_000, result.totalWeightLbs)
        assertEquals(2_100, result.totalVolumeCuft)
        assertEquals(68.18, result.utilizationWeightPercent)
        assertEquals(70, result.utilizationVolumePercent)
    }

    @Test
    fun `should select best option hazmat`() {
        val request = LoadOptimizerRequest(
            truck = Truck("truck-1", 44_000, 3_000),
            orders = listOf(
                Order(
                    id = "ord-001",
                    payoutCents = 2_50_000,
                    weightLbs = 18_000,
                    volumeCuft = 1_200,
                    origin = "Los Angeles, CA",
                    destination = "Dallas, TX",
                    pickupDate = LocalDate.parse("2025-12-05"),
                    deliveryDate = LocalDate.parse("2025-12-09"),
                    isHazmat = false
                ),
                Order(
                    id = "ord-002",
                    payoutCents = 1_80_000,
                    weightLbs = 12_000,
                    volumeCuft = 900,
                    origin = "Los Angeles, CA",
                    destination = "Dallas, TX",
                    pickupDate = LocalDate.parse("2025-12-04"),
                    deliveryDate = LocalDate.parse("2025-12-10"),
                    isHazmat = false
                ),
                Order(
                    id = "ord-003",
                    payoutCents = 3_20_0000,
                    weightLbs = 30_000,
                    volumeCuft = 1_800,
                    origin = "Los Angeles, CA",
                    destination = "Dallas, TX",
                    pickupDate = LocalDate.parse("2025-12-06"),
                    deliveryDate = LocalDate.parse("2025-12-08"),
                    isHazmat = true
                )
            )
        )

        val result = service.optimizeLoad(request)

        listOf("ord-003") shouldContainExactlyInAnyOrder result.selectedOrderIds
        assertEquals(3_20_0000, result.totalPayoutCents)
        assertEquals(30_000, result.totalWeightLbs)
        assertEquals(1_800, result.totalVolumeCuft)
        assertEquals(68.18, result.utilizationWeightPercent)
        assertEquals(60, result.utilizationVolumePercent)
    }

    @Test
    fun `should skips overweight combinations and pick best available option`() {
        val request = LoadOptimizerRequest(
            truck = Truck("truck-1", 10_000, 1_000),
            orders = listOf(
                Order(
                    id = "order-1",
                    payoutCents = 70_000,
                    weightLbs = 6_000,
                    volumeCuft = 400,
                    origin = "A",
                    destination = "B",
                    pickupDate = LocalDate.parse("2025-12-01"),
                    deliveryDate = LocalDate.parse("2025-12-02"),
                    isHazmat = false
                ),
                Order(
                    id = "order-2",
                    payoutCents = 60_000,
                    weightLbs = 5_000,
                    volumeCuft = 400,
                    origin = "A",
                    destination = "B",
                    pickupDate = LocalDate.parse("2025-12-03"),
                    deliveryDate = LocalDate.parse("2025-12-04"),
                    isHazmat = false
                ),
                Order(
                    id = "order-3",
                    payoutCents = 55_000,
                    weightLbs = 4_000,
                    volumeCuft = 300,
                    origin = "A",
                    destination = "B",
                    pickupDate = LocalDate.parse("2025-12-05"),
                    deliveryDate = LocalDate.parse("2025-12-06"),
                    isHazmat = false
                )
            )
        )

        val result = service.optimizeLoad(request)
        listOf("order-1", "order-3") shouldContainExactlyInAnyOrder result.selectedOrderIds
        assertEquals(1_25_000L, result.totalPayoutCents)
        assertEquals(10_000, result.totalWeightLbs)
        assertEquals(700, result.totalVolumeCuft)
    }
}