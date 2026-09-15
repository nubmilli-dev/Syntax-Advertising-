package com.example.domain.engine

import kotlin.math.roundToInt

// Funnel & Market Conversion Engine
enum class MarketScenario(val title: String, val description: String) {
    SCENARIO_A(
        "Scenario A (Visible Enforcement)",
        "City enforcement is active; deadline set; conversion ~35% of quotes; corridor street sales prioritized."
    ),
    SCENARIO_B(
        "Scenario B (Weak Enforcement)",
        "Enforcement delayed/lax; conversion ~20%; shift 40% focus to institutions, dead-sign repairs, and nearby towns."
    )
}

data class FunnelStage(
    val stageName: String,
    val targetCountA: Int,
    val targetCountB: Int,
    val currentCount: Int,
    val description: String
)

data class EquipmentRoiInput(
    val machineName: String,
    val landedPriceEtb: Double,
    val monthlyOutsourcingSavedEtb: Double,
    val monthlyCapturedJobMarginEtb: Double,
    val monthlyNewProductMarginEtb: Double,
    val monthlyOperatorWageEtb: Double,
    val monthlyPowerCostEtb: Double,
    val monthlyMaintenanceCostEtb: Double,
    val monthlyConsumablesEtb: Double,
    val hasTrainedOperator: Boolean,
    val hasAdequatePower: Boolean,
    val monthsWorkingCapitalOnHand: Int
)

data class EquipmentRoiResult(
    val machineName: String,
    val monthlyBenefitB: Double,
    val monthlyOwnershipCostO: Double,
    val ratioBOverO: Double,
    val paybackMonths: Double,
    val passesRatioRule: Boolean, // B >= 1.5 * O
    val passesPaybackRule: Boolean, // Payback <= 18 months
    val passesCapitalRule: Boolean, // Working capital >= 3 months
    val passesInfrastructureRule: Boolean, // Operator + power
    val finalRecommendation: String,
    val canBuyNow: Boolean
)

object OperationsEngines {

    fun calculateEquipmentRoi(input: EquipmentRoiInput): EquipmentRoiResult {
        val totalMonthlyOperatingCosts = input.monthlyOperatorWageEtb +
                input.monthlyPowerCostEtb +
                input.monthlyMaintenanceCostEtb +
                input.monthlyConsumablesEtb

        val monthlyGrossBenefit = input.monthlyOutsourcingSavedEtb +
                input.monthlyCapturedJobMarginEtb +
                input.monthlyNewProductMarginEtb

        val monthlyBenefitB = monthlyGrossBenefit - totalMonthlyOperatingCosts
        val monthlyOwnershipCostO = input.landedPriceEtb / 36.0 // 3-year straight line amortization

        val ratio = if (monthlyOwnershipCostO > 0) monthlyBenefitB / monthlyOwnershipCostO else 0.0
        val paybackMonths = if (monthlyBenefitB > 0) input.landedPriceEtb / monthlyBenefitB else 999.0

        val passesRatio = ratio >= 1.5
        val passesPayback = paybackMonths <= 18.0
        val passesCapital = input.monthsWorkingCapitalOnHand >= 3
        val passesInfra = input.hasTrainedOperator && input.hasAdequatePower

        val canBuyNow = passesRatio && passesPayback && passesCapital && passesInfra

        val recommendation = when {
            canBuyNow -> "BUY APPROVED: Metrics satisfy all 4 investment criteria (Benefit Ratio ${(ratio * 10).roundToInt() / 10.0}x, Payback ${(paybackMonths * 10).roundToInt() / 10.0} mos)."
            !passesRatio -> "DO NOT BUY: Monthly net benefit (ETB ${monthlyBenefitB.toInt()}) is below 1.5x ownership cost (ETB ${(1.5 * monthlyOwnershipCostO).toInt()}). Keep outsourcing."
            !passesPayback -> "DO NOT BUY: Payback period (${(paybackMonths * 10).roundToInt() / 10.0} months) exceeds the 18-month ceiling."
            !passesCapital -> "HOLD PURCHASE: Preserve cash buffer. You have ${input.monthsWorkingCapitalOnHand} months of working capital reserve (minimum 3 required)."
            else -> "HOLD PURCHASE: Ensure trained operator and stable industrial power before acquiring."
        }

        return EquipmentRoiResult(
            machineName = input.machineName,
            monthlyBenefitB = monthlyBenefitB,
            monthlyOwnershipCostO = monthlyOwnershipCostO,
            ratioBOverO = ratio,
            paybackMonths = paybackMonths,
            passesRatioRule = passesRatio,
            passesPaybackRule = passesPayback,
            passesCapitalRule = passesCapital,
            passesInfrastructureRule = passesInfra,
            finalRecommendation = recommendation,
            canBuyNow = canBuyNow
        )
    }

    fun calculateSupplierScore(
        quality: Double,
        reliability: Double,
        speed: Double,
        price: Double,
        material: Double,
        capacity: Double,
        terms: Double,
        emergency: Double
    ): Double {
        // Exact weights from Section 11.3:
        // Quality 25%, Reliability 20%, Speed 15%, Price 15%, Material 10%, Capacity 5%, Terms 5%, Emergency 5%
        return (quality * 0.25) +
                (reliability * 0.20) +
                (speed * 0.15) +
                (price * 0.15) +
                (material * 0.10) +
                (capacity * 0.05) +
                (terms * 0.05) +
                (emergency * 0.05)
    }

    fun classifySupplierRating(score: Double): String {
        return when {
            score >= 4.0 -> "Preferred Partner (>=4.0)"
            score >= 3.0 -> "Backup Partner (3.0–3.9)"
            else -> "Drop / Unqualified (<3.0)"
        }
    }

    fun computePriorityScore(visibility: Int, abilityToPay: Int, urgency: Int, influence: Int): Pair<Int, String> {
        val total = (visibility.coerceIn(1, 3)) +
                (abilityToPay.coerceIn(1, 3)) +
                (urgency.coerceIn(1, 3)) +
                (influence.coerceIn(1, 3))

        val grade = when {
            total >= 10 -> "A"
            total >= 7 -> "B"
            else -> "C"
        }
        return Pair(total, grade)
    }
}
