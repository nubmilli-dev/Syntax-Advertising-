package com.example

import com.example.domain.engine.CostingEngine
import com.example.domain.engine.EquipmentRoiInput
import com.example.domain.engine.OperationsEngines
import com.example.domain.model.AddOnSelection
import com.example.domain.model.BandStatus
import com.example.domain.model.ProductType
import com.example.domain.model.SignTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test Standard S3 quote calculation worked example parity`() {
        val result = CostingEngine.calculateQuote(
            product = ProductType.P2_STD_ACRYLIC,
            tier = SignTier.STANDARD,
            widthCm = 240.0,
            heightCm = 80.0,
            addOns = AddOnSelection(),
            isCustomSize = false
        )

        // Face area = 240x80 = 1.92 -> rounded up to 2.0 m²
        assertEquals(2.0, result.faceAreaM2, 0.01)

        // Illumination: 50 modules/m2 * 2.0 = 100 modules
        assertEquals(100, result.ledModuleCount)

        // PSU continuous safety load cap: 100 * 1.2W = 120W draw / 0.8 = 150W PSU rating
        assertEquals(120.0, result.psuWattsRequired, 0.01)
        assertEquals(150.0, result.psuWattsSafetyCap, 0.01)

        // Final selling price: calculated 50,500 ETB for Standard 240x80cm acrylic lightbox
        assertEquals(50500.0, result.finalSellingPrice, 0.01)

        // Deposit: 60% of selling price
        assertEquals(result.finalSellingPrice * 0.60, result.depositRequired, 100.0)

        // Balance: 40% of selling price
        assertEquals(result.finalSellingPrice - result.depositRequired, result.balanceOnInstall, 0.01)

        // Band status must be OK
        assertEquals(BandStatus.OK, result.bandStatus)
    }

    @Test
    fun `test minimum job price protection for small kiosk signs`() {
        val result = CostingEngine.calculateQuote(
            product = ProductType.P1_ECO_FLEX,
            tier = SignTier.ECONOMY,
            widthCm = 100.0,
            heightCm = 40.0,
            addOns = AddOnSelection()
        )

        // Economy tier has a strict minimum job floor of 15,000 ETB
        assertTrue(result.finalSellingPrice >= 15000.0)
    }

    @Test
    fun `test corridor priority score calculation`() {
        // High priority lead: Selam Hiwot Pharmacy (Visibility: 3, Ability: 3, Urgency: 3, Influence: 2) -> 11 points -> Grade A
        val (scoreA, gradeA) = OperationsEngines.computePriorityScore(3, 3, 3, 2)
        assertEquals(11, scoreA)
        assertEquals("A", gradeA)

        // Mid priority lead: (Visibility: 2, Ability: 2, Urgency: 2, Influence: 2) -> 8 points -> Grade B
        val (scoreB, gradeB) = OperationsEngines.computePriorityScore(2, 2, 2, 2)
        assertEquals(8, scoreB)
        assertEquals("B", gradeB)

        // Low priority lead: (Visibility: 1, Ability: 1, Urgency: 2, Influence: 1) -> 5 points -> Grade C
        val (scoreC, gradeC) = OperationsEngines.computePriorityScore(1, 1, 2, 1)
        assertEquals(5, scoreC)
        assertEquals("C", gradeC)
    }

    @Test
    fun `test supplier weighted scorecard formula`() {
        // Perfect supplier
        val score = OperationsEngines.calculateSupplierScore(
            quality = 5.0,
            reliability = 5.0,
            speed = 5.0,
            price = 5.0,
            material = 5.0,
            capacity = 5.0,
            terms = 5.0,
            emergency = 5.0
        )
        assertEquals(5.0, score, 0.01)
        assertEquals("Preferred Partner (>=4.0)", OperationsEngines.classifySupplierRating(score))
    }

    @Test
    fun `test equipment ROI 4-criteria decision rule`() {
        // Valid investment scenario: high benefit, rapid payback, adequate working capital
        val approvedInput = EquipmentRoiInput(
            machineName = "CO2 Laser",
            landedPriceEtb = 320000.0,
            monthlyOutsourcingSavedEtb = 25000.0,
            monthlyCapturedJobMarginEtb = 15000.0,
            monthlyNewProductMarginEtb = 10000.0,
            monthlyOperatorWageEtb = 7000.0,
            monthlyPowerCostEtb = 2000.0,
            monthlyMaintenanceCostEtb = 2500.0,
            monthlyConsumablesEtb = 3500.0,
            hasTrainedOperator = true,
            hasAdequatePower = true,
            monthsWorkingCapitalOnHand = 4
        )

        val approvedResult = OperationsEngines.calculateEquipmentRoi(approvedInput)
        assertTrue(approvedResult.passesRatioRule)
        assertTrue(approvedResult.passesPaybackRule)
        assertTrue(approvedResult.passesCapitalRule)
        assertTrue(approvedResult.canBuyNow)

        // Negative scenario: low outsourcing saved -> Benefit ratio fails
        val rejectedInput = approvedInput.copy(
            monthlyOutsourcingSavedEtb = 5000.0,
            monthlyCapturedJobMarginEtb = 2000.0,
            monthlyNewProductMarginEtb = 0.0
        )
        val rejectedResult = OperationsEngines.calculateEquipmentRoi(rejectedInput)
        assertFalse(rejectedResult.passesRatioRule)
        assertFalse(rejectedResult.canBuyNow)
    }

    @Test
    fun `test CSV import validation and RFC 4180 parsing`() {
        val sampleCsv = """
            BusinessName,Landmark,Category,Phone,Width,Height,EstValue
            "Selam Pharmacy, Central",Mexico Square,Pharmacy,+251911223344,240,80,44000
            ,,Retail,+251922334455,300,100,68000
            Awash Supermarket,Stadium,Retail,+251933445566,300,100,68000
        """.trimIndent()

        val result = com.example.domain.engine.DataExchangeEngine.parseAndValidateProspectsCsv(sampleCsv)

        // Line 2 has an empty BusinessName, so should report 1 validation error
        assertEquals(1, result.errors.size)
        assertEquals(2, result.validItems.size)

        // Verify escaped quoted field with comma
        assertEquals("Selam Pharmacy, Central", result.validItems[0].businessName)
        assertEquals("Mexico Square", result.validItems[0].locationLandmark)
        assertEquals(240.0, result.validItems[0].approxWidthCm, 0.01)
    }

    @Test
    fun `test Financial Summary P&L computation`() {
        val projects = listOf(
            com.example.data.entity.ProjectEntity(
                id = 1,
                jobCode = "JOB-2026-001",
                customerName = "Selam Shop",
                businessName = "Selam Shop",
                customerPhone = "+251911000000",
                tier = "Standard",
                productCode = "P2",
                widthCm = 240.0,
                heightCm = 80.0,
                faceAreaM2 = 2.0,
                sellingPrice = 39500.0,
                materialsCost = 15000.0,
                outsourcingCost = 3000.0,
                labourCost = 3500.0,
                transportCost = 1000.0,
                installationCost = 1500.0,
                overheadAllocated = 3000.0,
                contingencyUsed = 0.0,
                grossProfit = 15500.0,
                grossProfitPct = 39.2,
                netContribution = 12500.0,
                isBelowTargetMargin = false,
                targetMarginPct = 35.0,
                depositAmount = 23700.0,
                depositDate = "2026-09-15",
                depositReceived = true,
                balanceDue = 15800.0,
                balanceReceived = false,
                daysOutstanding = 12,
                status = "In Fabrication"
            )
        )

        val summary = com.example.domain.engine.DataExchangeEngine.computeFinancialSummary(projects)

        assertEquals(39500.0, summary.totalRevenueEtb, 0.01)
        assertEquals(24000.0, summary.totalDirectCostsEtb, 0.01)
        assertEquals(15500.0, summary.totalGrossProfitEtb, 0.01)
        assertEquals(12500.0, summary.totalNetContributionEtb, 0.01)
        assertEquals(23700.0, summary.totalCashCollectedEtb, 0.01)
        assertEquals(15800.0, summary.totalReceivablesOutstandingEtb, 0.01)
    }
}
