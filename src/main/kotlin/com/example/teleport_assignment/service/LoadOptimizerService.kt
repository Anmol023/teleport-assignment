package com.example.teleport_assignment.service

import com.example.teleport_assignment.config.LoadOptimizerConfig
import com.example.teleport_assignment.config.OptimizationStrategy
import com.example.teleport_assignment.exchange_model.LoadOptimizerRequest
import com.example.teleport_assignment.exchange_model.LoadOptimizerResponse
import com.example.teleport_assignment.model.Order
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.math.round

@Service
class LoadOptimizerService(
    @Autowired private val loadOptimizerConfig: LoadOptimizerConfig
) {
    fun optimizeLoad(request: LoadOptimizerRequest): LoadOptimizerResponse {

        val validOrders = request.orders.filter { isDateWindowValid(it) }
        if (validOrders.isEmpty()) return LoadOptimizerResponse.buildEmptyResponse(request.truck.id)

        val ordersByLocation = validOrders.groupBy { it.origin to it.destination }

        var globalBestScore = Double.NEGATIVE_INFINITY
        var globalBestPayout = 0L
        var globalBestMask = 0
        var globalBestOrders: List<Order> = emptyList()
        var globalBestWeight = 0
        var globalBestVolume = 0

        for ((_, orders) in ordersByLocation) {

            val n = orders.size
            if (n == 0) continue
            require(n <= 24) { "Too many orders ($n) in one direction" }

            val totalSubsets = 1 shl n
            val weightArr = IntArray(n) { orders[it].weightLbs }
            val volumeArr = IntArray(n) { orders[it].volumeCuft }
            val payoutArr = LongArray(n) { orders[it].payoutCents }
            val globalMaxPayout = validOrders.sumOf { it.payoutCents }.toDouble().coerceAtLeast(1.0)

            val dpWeight = IntArray(totalSubsets)
            val dpVolume = IntArray(totalSubsets)
            val dpPayout = LongArray(totalSubsets)
            val dpHasHazmat = BooleanArray(totalSubsets)
            val dpValid = BooleanArray(totalSubsets)

            dpValid[0] = true

            var bestScore = Double.NEGATIVE_INFINITY
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

                // hazmat must be loaded alone
                if (orders[i].isHazmat && prev != 0) continue
                if (dpHasHazmat[prev] && !orders[i].isHazmat) continue

                val tmpPayout = dpPayout[prev] + payoutArr[i]

                dpWeight[bitMask] = tmpWeight
                dpVolume[bitMask] = tmpVolume
                dpPayout[bitMask] = tmpPayout
                dpHasHazmat[bitMask] = dpHasHazmat[prev] || orders[i].isHazmat
                dpValid[bitMask] = true

                val score = computeScore(
                    currPayoutCents = tmpPayout,
                    currWeight = tmpWeight,
                    currVolume = tmpVolume,
                    maxPayout = globalMaxPayout,
                    maxWeight = request.truck.maxWieghtLbs,
                    maxVolume = request.truck.maxVolumeCuft
                )

                if (score > bestScore) {
                    bestScore = score
                    bestMask = bitMask
                    bestPayout = tmpPayout
                    bestWeight = tmpWeight
                    bestVolume = tmpVolume
                }
            }

            if (bestScore > globalBestScore) {
                globalBestScore = bestScore
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

        return if (selectedIds.isEmpty()) LoadOptimizerResponse.buildEmptyResponse(request.truck.id)
        else LoadOptimizerResponse(
            truckId = request.truck.id,
            selectedOrderIds = selectedIds,
            totalPayoutCents = globalBestPayout,
            totalWeightLbs = globalBestWeight,
            totalVolumeCuft = globalBestVolume,
            utilizationWeightPercent = percent(globalBestWeight, request.truck.maxWieghtLbs),
            utilizationVolumePercent = percent(globalBestVolume, request.truck.maxVolumeCuft)
        )
    }

    private fun computeScore(
        currPayoutCents: Long,
        currWeight: Int,
        currVolume: Int,
        maxPayout: Double,
        maxWeight: Int,
        maxVolume: Int
    ): Double {
        val payoutPercent = (currPayoutCents / maxPayout) * 100.0
        val weightPercent = if (maxWeight > 0) (currWeight.toDouble() / maxWeight) * 100.0 else 0.0
        val volumePercent = if (maxVolume > 0) (currVolume.toDouble() / maxVolume) * 100.0 else 0.0

        return when (loadOptimizerConfig.strategy) {
            OptimizationStrategy.MAX_PAYOUT -> currPayoutCents.toDouble()
            OptimizationStrategy.MAX_UTILIZATION -> (weightPercent + volumePercent) / 2.0
            OptimizationStrategy.BALANCED -> with(loadOptimizerConfig) {
                (this.weights.payout!! * payoutPercent + this.weights.weightUtilization!! * weightPercent + this.weights.volumeUtilization!! * volumePercent) / total!!
            }
        }
    }

    //date validity check: pickup date ≤ delivery
    private fun isDateWindowValid(order: Order): Boolean =
        !order.pickupDate.isAfter(order.deliveryDate)

    private fun percent(value: Int, max: Int): Number {
        if (max == 0) return 0
        val p = (value.toDouble() / max) * 100
        val rounded = round(p * 100) / 100.0
        return if (rounded % 1.0 == 0.0) rounded.toInt() else rounded
    }
}