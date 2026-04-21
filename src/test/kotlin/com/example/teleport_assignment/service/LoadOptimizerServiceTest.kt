package com.example.teleport_assignment.service

import com.example.teleport_assignment.config.LoadOptimizerConfig
import com.example.teleport_assignment.config.OptimizationStrategy
import com.example.teleport_assignment.config.StrategyWeights
import com.example.teleport_assignment.exchange_model.LoadOptimizerRequest
import com.example.teleport_assignment.model.Order
import com.example.teleport_assignment.model.Truck
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoadOptimizerServiceTest {

    private fun service(
        strategy: OptimizationStrategy = OptimizationStrategy.MAX_PAYOUT,
        payout: Int = 100,
        weightUtilization: Int = 0,
        volumeUtilization: Int = 0
    ) = LoadOptimizerService(
        LoadOptimizerConfig(strategy, StrategyWeights(payout, weightUtilization, volumeUtilization))
    )

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

        val result = service().optimizeLoad(request)

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

        val result = service().optimizeLoad(request)

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
                order(
                    id = "order-1",
                    payoutCents = 70_000,
                    weightLbs = 6_000,
                    volumeCuft = 400,
                    pickupDate = LocalDate.parse("2025-12-01"),
                    deliveryDate = LocalDate.parse("2025-12-02"),
                ),
                order(
                    id = "order-2",
                    payoutCents = 60_000,
                    weightLbs = 5_000,
                    volumeCuft = 400,
                    pickupDate = LocalDate.parse("2025-12-03"),
                    deliveryDate = LocalDate.parse("2025-12-04"),
                ),
                order(
                    id = "order-3",
                    payoutCents = 55_000,
                    weightLbs = 4_000,
                    volumeCuft = 300,
                    pickupDate = LocalDate.parse("2025-12-05"),
                    deliveryDate = LocalDate.parse("2025-12-06")
                )
            )
        )

        val result = service().optimizeLoad(request)
        result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("order-1", "order-3")
        assertEquals(1_25_000L, result.totalPayoutCents)
        assertEquals(10_000, result.totalWeightLbs)
        assertEquals(700, result.totalVolumeCuft)
    }

    @Nested
    inner class EmptyAndInvalidInput {

        @Test
        fun `empty order list returns empty response`() {
            val result = service().optimizeLoad(LoadOptimizerRequest(truck, emptyList()))
            assertTrue(result.selectedOrderIds.isEmpty())
        }

        @Test
        fun `order where pickupDate is after deliveryDate is filtered`() {
            val bad = order(
                "o1", 9_999,
                pickupDate = LocalDate.of(2025, 12, 10),
                deliveryDate = LocalDate.of(2025, 12, 1)
            )
            val result = service().optimizeLoad(LoadOptimizerRequest(truck, listOf(bad)))
            assertTrue(result.selectedOrderIds.isEmpty())
        }

        @Test
        fun `order where pickupDate equals deliveryDate is valid`() {
            val sameDay = order(
                "o1", 1_000,
                pickupDate = LocalDate.of(2025, 12, 1),
                deliveryDate = LocalDate.of(2025, 12, 1)
            )
            val result = service().optimizeLoad(LoadOptimizerRequest(truck, listOf(sameDay)))
            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("o1")
        }
    }

    @Nested
    inner class WeightConstraint {

        @Test
        fun `single order exceeding truck weight is excluded`() {
            val heavy = order("heavy", 9_999, weightLbs = truck.maxWieghtLbs + 1)
            val light = order("light", 1_000, weightLbs = 100)
            val result = service().optimizeLoad(LoadOptimizerRequest(truck, listOf(heavy, light)))
            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("light")
        }

        @Test
        fun `skips overweight combinations and picks best valid`() {
            // ord-1 (6k) + ord-3 (4k) = 10k payout 125,000
            val o1 = order("ord-1", 70_000, weightLbs = 6_000, volumeCuft = 400)
            val o2 = order("ord-2", 60_000, weightLbs = 5_000, volumeCuft = 400)
            val o3 = order("ord-3", 55_000, weightLbs = 4_000, volumeCuft = 300)

            val result = service().optimizeLoad(LoadOptimizerRequest(truck, listOf(o1, o2, o3)))

            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("ord-1", "ord-3")
            assertEquals(125_000L, result.totalPayoutCents)
            assertEquals(10_000, result.totalWeightLbs)
            assertEquals(700, result.totalVolumeCuft)
        }
    }

    @Nested
    inner class VolumeConstraint {

        @Test
        fun `single order exceeding truck volume is excluded`() {
            val bulky = order("bulky", 9_999, volumeCuft = truck.maxVolumeCuft + 1)
            val small = order("small", 1_000, volumeCuft = 100)
            val result = service().optimizeLoad(LoadOptimizerRequest(truck, listOf(bulky, small)))
            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("small")
        }

        @Test
        fun `combination exceeding volume limit is rejected even if weight fits`() {
            val o1 = order("order-1", 500, weightLbs = 200, volumeCuft = 600)
            val o2 = order("order-2", 500, weightLbs = 200, volumeCuft = 600)
            val result = service().optimizeLoad(LoadOptimizerRequest(truck, listOf(o1, o2)))
            assertEquals(1, result.selectedOrderIds.size)
        }
    }

    @Nested
    inner class HazmatConstraint {

        @Test
        fun `hazmat order loads alone even if combined payout would be higher`() {
            val hazmat = order("haz", 9_999, isHazmat = true)
            val nonHazmat = order("reg", 9_999, isHazmat = false)
            val result = service().optimizeLoad(LoadOptimizerRequest(truck, listOf(hazmat, nonHazmat)))
            assertEquals(1, result.selectedOrderIds.size)
        }

        @Test
        fun `single hazmat order is selected when it has the best score`() {
            val hazmat = order("haz", 5_000, isHazmat = true)
            val result = service().optimizeLoad(LoadOptimizerRequest(truck, listOf(hazmat)))
            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("haz")
        }

        @Test
        fun `non-hazmat combination beats single hazmat when payout is higher`() {
            val haz = order("haz", 5_000, isHazmat = true)
            val r1 = order("r1", 3_000, isHazmat = false)
            val r2 = order("r2", 3_000, isHazmat = false)
            val result = service().optimizeLoad(LoadOptimizerRequest(truck, listOf(haz, r1, r2)))
            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("r1", "r2")
        }
    }

    @Nested
    inner class DateConstraint {

        @Test
        fun `overlapping date windows on same lane can be combined`() {
            val o1 = order(
                "ord-001", 250_000, weightLbs = 18_000, volumeCuft = 1_200,
                pickupDate = LocalDate.of(2025, 12, 5),
                deliveryDate = LocalDate.of(2025, 12, 9)
            )
            val o2 = order(
                "ord-002", 180_000, weightLbs = 12_000, volumeCuft = 900,
                pickupDate = LocalDate.of(2025, 12, 4),
                deliveryDate = LocalDate.of(2025, 12, 10)
            )
            val bigTruck = Truck("t", maxWieghtLbs = 44_000, maxVolumeCuft = 3_000)

            val result = service().optimizeLoad(LoadOptimizerRequest(bigTruck, listOf(o1, o2)))

            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("ord-001", "ord-002")
            assertEquals(430_000L, result.totalPayoutCents)
        }

        @Test
        fun `should combine orders with identical pickup and delivery dates`() {
            val o1 = order(
                "o1", 3_000, weightLbs = 300,
                pickupDate = LocalDate.of(2025, 12, 1), deliveryDate = LocalDate.of(2025, 12, 1)
            )
            val o2 = order(
                "o2", 3_000, weightLbs = 300,
                pickupDate = LocalDate.of(2025, 12, 1), deliveryDate = LocalDate.of(2025, 12, 1)
            )

            val result = service().optimizeLoad(LoadOptimizerRequest(truck, listOf(o1, o2)))

            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("o1", "o2")
        }
    }

    @Nested
    inner class LocationGrouping {

        @Test
        fun `should not combine orders on different lanes`() {
            val ab = order("ab", 5_000, origin = "A", destination = "B")
            val ac = order("ac", 3_000, origin = "A", destination = "C")
            val result = service().optimizeLoad(LoadOptimizerRequest(truck, listOf(ab, ac)))
            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("ab")
        }

        @Test
        fun `best lane wins when two lanes have different payouts`() {
            val ab1 = order("ab1", 4_000, origin = "A", destination = "B")
            val ab2 = order("ab2", 4_000, origin = "A", destination = "B")
            val ac = order("ac", 5_000, origin = "A", destination = "C")
            val result = service().optimizeLoad(LoadOptimizerRequest(truck, listOf(ab1, ab2, ac)))
            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("ab1", "ab2")
            assertEquals(8_000L, result.totalPayoutCents)
        }
    }

    @Nested
    inner class OptimisationStrategies {

        @Test
        fun `MAX_PAYOUT selects highest combined payout within limits`() {
            val o1 = order("o1", 3_000, weightLbs = 4_000)
            val o2 = order("o2", 3_000, weightLbs = 4_000)
            val o3 = order("o3", 5_000, weightLbs = 7_000)

            val result = service(OptimizationStrategy.MAX_PAYOUT)
                .optimizeLoad(LoadOptimizerRequest(truck, listOf(o1, o2, o3)))

            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("o1", "o2")
            assertEquals(6_000L, result.totalPayoutCents)
        }

        @Test
        fun `MAX_UTILIZATION prefers high truck fill over higher payout`() {
            val highUtil = order(
                "util", 1_000, weightLbs = 9_000, volumeCuft = 900,
                origin = "A", destination = "B"
            )
            val highPay = order(
                "pay", 9_000, weightLbs = 100, volumeCuft = 100,
                origin = "C", destination = "D"
            )

            val result = service(OptimizationStrategy.MAX_UTILIZATION)
                .optimizeLoad(LoadOptimizerRequest(truck, listOf(highUtil, highPay)))

            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("util")
        }

        @Test
        fun `BALANCED with payout-heavy weights behaves like MAX_PAYOUT`() {

            val highPay = order(
                "pay", 9_000, weightLbs = 1_000, volumeCuft = 100,
                origin = "A", destination = "B"
            )
            val highUtil = order(
                "util", 1_000, weightLbs = 9_000, volumeCuft = 900,
                origin = "C", destination = "D"
            )

            val result = service(
                strategy = OptimizationStrategy.BALANCED,
                payout = 98,
                weightUtilization = 1,
                volumeUtilization = 1
            ).optimizeLoad(LoadOptimizerRequest(truck, listOf(highPay, highUtil)))

            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("pay")
        }

        @Test
        fun `BALANCED with utilization-heavy weights behaves like MAX_UTILIZATION`() {
            val highPay = order(
                "pay", 9_000, weightLbs = 100, volumeCuft = 100,
                origin = "A", destination = "B"
            )
            val highUtil = order(
                "util", 1_000, weightLbs = 9_000, volumeCuft = 900,
                origin = "C", destination = "D"
            )

            val result = service(
                strategy = OptimizationStrategy.BALANCED,
                payout = 10,
                weightUtilization = 45,
                volumeUtilization = 45
            ).optimizeLoad(LoadOptimizerRequest(truck, listOf(highPay, highUtil)))

            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("util")
        }

        @Test
        fun `BALANCED score uses weight coefficient not payout cents`() {
            val o1 = order(
                "o1", payoutCents = 1_000, weightLbs = 9_000, volumeCuft = 900,
                origin = "A", destination = "B"
            )
            val o2 = order(
                "o2", payoutCents = 99_000, weightLbs = 500, volumeCuft = 500,
                origin = "C", destination = "D"
            )

            val result = service(
                strategy = OptimizationStrategy.BALANCED,
                payout = 10,
                weightUtilization = 45,
                volumeUtilization = 45
            ).optimizeLoad(LoadOptimizerRequest(truck, listOf(o1, o2)))

            result.selectedOrderIds shouldContainExactlyInAnyOrder listOf("o1")
        }
    }

    private val truck = Truck(id = "truck-1", maxWieghtLbs = 10_000, maxVolumeCuft = 1_000)

    private fun order(
        id: String,
        payoutCents: Long,
        weightLbs: Int = 100,
        volumeCuft: Int = 100,
        origin: String = "A",
        destination: String = "B",
        isHazmat: Boolean = false,
        pickupDate: LocalDate = LocalDate.of(2025, 12, 1),
        deliveryDate: LocalDate = LocalDate.of(2025, 12, 2)
    ) = Order(
        id = id,
        payoutCents = payoutCents,
        weightLbs = weightLbs,
        volumeCuft = volumeCuft,
        origin = origin,
        destination = destination,
        pickupDate = pickupDate,
        deliveryDate = deliveryDate,
        isHazmat = isHazmat
    )
}