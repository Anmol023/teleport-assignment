package com.example.teleport_assignment.service

import com.example.teleport_assignment.exchange_model.LoadOptimizerRequest
import com.example.teleport_assignment.exchange_model.LoadOptimizerResponse
import com.example.teleport_assignment.model.Order
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class LoadOptimizerService {

    fun optimizeLoad(request: LoadOptimizerRequest): LoadOptimizerResponse {

        val validOrders = request.orders.filter { isDateWindowValid(it) }
        val ordersByLocation = validOrders.groupBy { it.origin to it.destination }

        var globalBestPayout = 0L
        var globalBestMask = 0
        var globalBestOrders: List<Order> = emptyList()
        var globalBestWeight = 0
        var globalBestVolume = 0

        for ((_, orders) in ordersByLocation) {

            val n = orders.size
            if (n == 0) continue

            require(n <= 24) { "Error too many orders $n in one direction " }

            val totalSubsets = 1 shl n

            val weightArr = IntArray(n) { orders[it].weightLbs }
            val volumeArr = IntArray(n) { orders[it].volumeCuft }
            val payoutArr = LongArray(n) { orders[it].payoutCents }

            val allDates = orders.flatMap { listOf(it.pickupDate, it.deliveryDate) }
                .distinct()

            val dateIndex = allDates.withIndex().associate { it.value to it.index }
            val pickupBit = LongArray(n)
            val deliveryBit = LongArray(n)
            for (i in 0 until n) {
                pickupBit[i] = 1L shl dateIndex[orders[i].pickupDate]!!
                deliveryBit[i] = 1L shl dateIndex[orders[i].deliveryDate]!!
            }

            val dpWeight = IntArray(totalSubsets)
            val dpVolume = IntArray(totalSubsets)
            val dpPayout = LongArray(totalSubsets)
            val dpHasHazmat = BooleanArray(totalSubsets)
            val dpValid = BooleanArray(totalSubsets)

            val dpPickupBits = LongArray(totalSubsets)
            val dpDeliveryBits = LongArray(totalSubsets)

            dpValid[0] = true

            var bestMask = 0
            var bestPayout = 0L
            var bestWeight = 0
            var bestVolume = 0

            for (bitMask in 1 until totalSubsets) {

                val lsb = bitMask and -bitMask
                val i = Integer.numberOfTrailingZeros(lsb)
                val prev = bitMask xor lsb

                if (!dpValid[prev]) continue

                val tmpWeight = dpWeight[prev] + weightArr[i]
                if (tmpWeight > request.truck.maxWieghtLbs) continue

                val tmpVolume = dpVolume[prev] + volumeArr[i]
                if (tmpVolume > request.truck.maxVolumeCuft) continue

                // hazmat rule
                if (orders[i].isHazmat && prev != 0) continue
                if (dpHasHazmat[prev] && !orders[i].isHazmat) continue

                val prevPickup = dpPickupBits[prev]
                val prevDelivery = dpDeliveryBits[prev]

                val newPickup = pickupBit[i]
                val newDelivery = deliveryBit[i]

                // Same-date same-location stops are fine
                if ((prevDelivery and newPickup) != 0L) continue
                if ((prevPickup and newDelivery) != 0L) continue

                dpPickupBits[bitMask] = prevPickup or newPickup
                dpDeliveryBits[bitMask] = prevDelivery or newDelivery

                val tmpPayout = dpPayout[prev] + payoutArr[i]

                dpWeight[bitMask] = tmpWeight
                dpVolume[bitMask] = tmpVolume
                dpPayout[bitMask] = tmpPayout
                dpHasHazmat[bitMask] = dpHasHazmat[prev] || orders[i].isHazmat
                dpValid[bitMask] = true

                if (tmpPayout > bestPayout) {
                    bestPayout = tmpPayout
                    bestMask = bitMask
                    bestWeight = tmpWeight
                    bestVolume = tmpVolume
                }
            }

            if (bestPayout > globalBestPayout) {
                globalBestPayout = bestPayout
                globalBestMask = bestMask
                globalBestOrders = orders
                globalBestWeight = bestWeight
                globalBestVolume = bestVolume
            }
        }

        val selectedIds = globalBestOrders.mapIndexedNotNull { idx, order ->
            if ((globalBestMask shr idx) and 1 == 1) order.id else null
        }

        return LoadOptimizerResponse(
            truckId = request.truck.id,
            selectedOrderIds = selectedIds,
            totalPayoutCents = globalBestPayout,
            totalWeightLbs = globalBestWeight,
            totalVolumeCuft = globalBestVolume,
            utilizationWeightPercent = percent(globalBestWeight, request.truck.maxWieghtLbs),
            utilizationVolumePercent = percent(globalBestVolume, request.truck.maxVolumeCuft)
        )
    }

    private fun isDateWindowValid(order: Order): Boolean =
        !order.pickupDate.isAfter(order.deliveryDate)

    private fun percent(value: Int, max: Int): Number {
        if (max == 0) return 0
        val p = (value.toDouble() / max) * 100
        val rounded = kotlin.math.round(p * 100) / 100
        return if (rounded % 1.0 == 0.0) rounded.toInt() else rounded
    }
}