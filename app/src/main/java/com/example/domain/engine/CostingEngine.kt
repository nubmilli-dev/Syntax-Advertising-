package com.example.domain.engine

import com.example.domain.model.AddOnSelection
import com.example.domain.model.BandStatus
import com.example.domain.model.BomItem
import com.example.domain.model.ProductType
import com.example.domain.model.QuoteResult
import com.example.domain.model.SignTier
import com.example.domain.model.StandardSize
import kotlin.math.ceil
import kotlin.math.max

object CostingEngine {

    val STANDARD_SIZES = listOf(
        StandardSize("S1", "S1 Compact Shop/Kiosk", 120.0, 60.0, "1/4 sheet", "Kiosk, small shop, side entrance", 15000.0, 28000.0, 57500.0),
        StandardSize("S2", "S2 Narrow Fascia", 240.0, 60.0, "1/2 sheet", "Narrow corridor shop front", 20000.0, 39500.0, 75000.0),
        StandardSize("S3", "S3 Default Corridor Front", 240.0, 80.0, "2/3 sheet", "Standard corridor business frontage", 23000.0, 44000.0, 82000.0),
        StandardSize("S4", "S4 Large Frontage", 240.0, 120.0, "1 full sheet", "Hotels, supermarkets, large retailers", 29500.0, 61000.0, 116000.0),
        StandardSize("S5", "S5 Extra Wide Flex", 300.0, 90.0, "Continuous flex", "Wide retail fronts (Economy flex only)", 28500.0, null, null),
        StandardSize("B1", "B1 Projecting Blade (Double-Face)", 60.0, 60.0, "Offcuts", "Perpendicular pedestrian walkway visibility", 18500.0, 32000.0, null),
        StandardSize("R1", "R1 Circular Logo Lightbox", 60.0, 60.0, "Offcuts", "Café / Pharmacy Cross / Brand emblem", null, 25000.0, null)
    )

    fun calculateQuote(
        product: ProductType,
        tier: SignTier = product.defaultTier,
        widthCm: Double,
        heightCm: Double,
        addOns: AddOnSelection = AddOnSelection(),
        isCustomSize: Boolean = false
    ): QuoteResult {
        // Raw face area in square meters
        val rawArea = (widthCm / 100.0) * (heightCm / 100.0)
        // Rule: Round face area up to next 0.1 m2
        val faceArea = max(0.4, ceil(rawArea * 10.0) / 10.0)
        val perimeterM = 2.0 * ((widthCm / 100.0) + (heightCm / 100.0))

        // Multiplier for double-face blade signs
        val faceMultiplier = if (product == ProductType.P3_BLADE) 2.0 else 1.0

        // 1. Structural Steel Frame
        // Tube length = (perimeter * 2 front & back rings + internal braces every ~60cm)
        val bracesCount = max(1, (widthCm / 60.0).toInt())
        val totalTubeMeters = (perimeterM * 2.0) + (bracesCount * (heightCm / 100.0))
        val tubeLengthsNeeded = ceil(totalTubeMeters / 6.0).toInt()
        val tubeCostPerLength = 800.0
        val frameCost = tubeLengthsNeeded * tubeCostPerLength

        // 2. Returns & Enclosure (Box depth = 15cm)
        val boxDepthM = 0.15
        val returnAreaM2 = perimeterM * boxDepthM * 1.25 // with 25% folding & waste
        val returnCost = when (tier) {
            SignTier.ECONOMY -> returnAreaM2 * 950.0 // Sheet metal returns
            SignTier.STANDARD -> returnAreaM2 * 1480.0 // ACP returns
            SignTier.PREMIUM -> returnAreaM2 * 2200.0 // Folded architectural ACP
            SignTier.INSTITUTIONAL -> returnAreaM2 * 1800.0
        }

        // 3. Face Materials & Graphics
        val faceMaterialCost = when (tier) {
            SignTier.ECONOMY -> {
                // Backlit printed flex (with 15% tensioning waste)
                val flexArea = faceArea * 1.15 * faceMultiplier
                flexArea * 900.0
            }
            SignTier.STANDARD -> {
                // Opal acrylic (3mm-4mm) + computer cut translucent vinyl
                val sheetFractionCost = faceArea * 2100.0 * faceMultiplier
                val vinylCost = (faceArea * 0.6) * 1100.0 * faceMultiplier
                sheetFractionCost + vinylCost
            }
            SignTier.PREMIUM -> {
                // Architectural ACP face with routed push-through or backed 3D acrylic letters
                val acpFace = faceArea * 1800.0 * faceMultiplier
                val acrylicBacking = faceArea * 2800.0 * faceMultiplier
                val outsourcedCncRouting = faceArea * 2200.0 * faceMultiplier
                acpFace + acrylicBacking + outsourcedCncRouting
            }
            SignTier.INSTITUTIONAL -> {
                val sheetCost = faceArea * 2200.0 * faceMultiplier
                val vinylCost = faceArea * 1200.0 * faceMultiplier
                sheetCost + vinylCost
            }
        }

        // 4. Illumination (LEDs & Power Supplies)
        // Spacing rule: 50 modules/m2 for 15cm deep box for even non-spotting illumination
        val modulesPerM2 = when (tier) {
            SignTier.ECONOMY -> 45
            SignTier.STANDARD -> 50
            SignTier.PREMIUM -> 60
            SignTier.INSTITUTIONAL -> 55
        }
        val rawLedCount = ceil(faceArea * modulesPerM2 * faceMultiplier).toInt()
        val ledModuleCount = max(20, rawLedCount)
        val moduleCost = when (tier) {
            SignTier.ECONOMY -> 18.0
            SignTier.STANDARD -> 25.0 // Branded high-lumen injection modules
            SignTier.PREMIUM -> 32.0 // Premium Nichia/Samsung bin modules
            SignTier.INSTITUTIONAL -> 28.0
        }
        val totalLedCost = ledModuleCount * moduleCost

        // Power Supply Calculation: 1.2W per module typical
        val totalLedWatts = ledModuleCount * 1.2
        // SAFETY MANDATE: Never load PSU beyond 80% (divide by 0.8)
        val psuWattsRequired = totalLedWatts
        val psuWattsSafetyCap = totalLedWatts / 0.80
        val psuRatePerWatt = when (tier) {
            SignTier.ECONOMY -> 9.0 // Standard commercial driver
            SignTier.STANDARD -> 12.0 // Genuine weatherproof IP67 driver
            SignTier.PREMIUM -> 16.0 // High-spec MeanWell/heavy duty driver
            SignTier.INSTITUTIONAL -> 14.0
        }
        val psuCost = psuWattsSafetyCap * psuRatePerWatt

        // 5. Electrical Accessories & Sensors
        var electricalAccessories = when (tier) {
            SignTier.ECONOMY -> 600.0 // Cable, gland, internal fuse
            SignTier.STANDARD -> 1500.0 // Includes photocell sensor, glands, terminal blocks
            SignTier.PREMIUM -> 2800.0 // Includes photocell, timer, surge protector, glands
            SignTier.INSTITUTIONAL -> 2000.0
        }
        if (addOns.includeTimer && tier != SignTier.PREMIUM) electricalAccessories += 1500.0
        if (addOns.includeSurgeProtector && tier != SignTier.PREMIUM) electricalAccessories += 1200.0

        // 6. Fasteners, Consumables, Sealing
        val consumablesCost = faceArea * 300.0 * faceMultiplier

        // Total Materials
        val materialsCost = frameCost + returnCost + faceMaterialCost + totalLedCost + psuCost + electricalAccessories + consumablesCost

        // 7. Labour (Owner Assembly + Metal Fabricator Partner)
        val metalFabricatorDays = if (faceArea <= 1.5) 0.75 else 1.0
        val metalFabricatorRate = 1200.0
        val assemblyDays = if (faceArea <= 1.5) 0.75 else 1.0
        val assemblyRate = 1000.0
        val labourCost = (metalFabricatorDays * metalFabricatorRate) + (assemblyDays * assemblyRate)

        // 8. Transport & Logistics
        val transportCost = 700.0 // Local transit & material fetch allocation

        // 9. Installation & Mounting Hardware
        var installationCost = tier.fixedInstallFee
        if (addOns.isHeightAbove4m) installationCost += 2500.0 // Scaffolding rental & safety crew
        if (addOns.includeOldSignRemoval) installationCost += 1200.0

        // Direct Cost D
        val directCost = materialsCost + labourCost + transportCost + installationCost

        // Overhead & Contingency
        val overheadPct = 0.12 // 12%
        val contingencyPct = if (isCustomSize) 0.15 else 0.08 // 8% standard, 15% custom
        val overheadCost = directCost * overheadPct
        val contingencyCost = directCost * contingencyPct
        val loadedCost = directCost + overheadCost + contingencyCost

        // Base Price before add-on adjustments
        var basePrice = loadedCost / (1.0 - tier.targetMargin)

        // Minimum job protection rule: Small signs must never fall below minimum threshold
        val priceFloorProtected = max(tier.minJobPrice, basePrice)

        // Add-ons & discounts
        var finalSellingPrice = priceFloorProtected
        if (addOns.isRushOrder) finalSellingPrice *= 1.20 // +20% rush surcharge
        if (addOns.blockDiscountApplied) finalSellingPrice *= 0.90 // -10% block deal discount
        if (addOns.customDesignFromScratch) finalSellingPrice += 2000.0

        // Round selling price up to nearest 500 ETB for clean client presentation
        finalSellingPrice = ceil(finalSellingPrice / 500.0) * 500.0

        // Payment Split
        val depositRequired = ceil((finalSellingPrice * tier.depositPct) / 100.0) * 100.0
        val balanceOnInstall = finalSellingPrice - depositRequired

        // Effective rate per m2 for band check
        val priceExcludingInstall = max(0.0, finalSellingPrice - installationCost)
        val effectiveRatePerM2 = priceExcludingInstall / faceArea

        val bandFloor = (faceArea * tier.bandRateMin) + installationCost
        val bandCeiling = (faceArea * tier.bandRateMax) + installationCost

        val bandStatus = when {
            finalSellingPrice < bandFloor && finalSellingPrice > tier.minJobPrice -> BandStatus.BELOW
            finalSellingPrice > bandCeiling * 1.25 -> BandStatus.ABOVE
            else -> BandStatus.OK
        }

        // BOM Breakdown
        val bomList = listOf(
            BomItem("Structural", "Steel Tube 25x25mm", "$tubeLengthsNeeded lengths ($totalTubeMeters m)", tubeCostPerLength, frameCost),
            BomItem("Enclosure", "Returns & Frame Cladding", String.format("%.2f m²", returnAreaM2), returnCost / max(0.1, returnAreaM2), returnCost),
            BomItem("Face & Graphic", "Face Substrate + Translucent Graphics", String.format("%.2f m²", faceArea * faceMultiplier), faceMaterialCost / max(0.1, faceArea), faceMaterialCost),
            BomItem("Illumination", "LED Injection Modules (12V)", "$ledModuleCount modules", moduleCost, totalLedCost),
            BomItem("Electrical", "Genuine Waterproof PSU (80% load cap)", String.format("%.0fW (capped at %.0fW)", psuWattsRequired, psuWattsSafetyCap), psuRatePerWatt, psuCost),
            BomItem("Controls", "Photocell / Timer / Wiring Kit", "1 assembly", electricalAccessories, electricalAccessories),
            BomItem("Consumables", "Rivets, Sealant, Structural Screws", "Kit", consumablesCost, consumablesCost),
            BomItem("Labour", "Metal Fabrication + In-House Wiring/Assembly", "2 technicians", labourCost, labourCost),
            BomItem("Logistics", "Material Fetch + In-Town Delivery", "Local allocation", transportCost, transportCost),
            BomItem("Installation", "Site Mounting, Heavy Anchors & Connection", "2-person crew", installationCost, installationCost)
        )

        return QuoteResult(
            product = product,
            tier = tier,
            widthCm = widthCm,
            heightCm = heightCm,
            faceAreaM2 = faceArea,
            perimeterM = perimeterM,
            ledModuleCount = ledModuleCount,
            psuWattsRequired = psuWattsRequired,
            psuWattsSafetyCap = psuWattsSafetyCap,
            materialsCost = materialsCost,
            labourCost = labourCost,
            transportCost = transportCost,
            installationCost = installationCost,
            directCost = directCost,
            overheadCost = overheadCost,
            contingencyCost = contingencyCost,
            loadedCost = loadedCost,
            basePrice = basePrice,
            finalSellingPrice = finalSellingPrice,
            depositRequired = depositRequired,
            balanceOnInstall = balanceOnInstall,
            effectiveRatePerM2 = effectiveRatePerM2,
            bandStatus = bandStatus,
            bomItems = bomList
        )
    }

    fun generateEnglishQuoteText(result: QuoteResult, customerName: String, businessName: String): String {
        return """
            *QUOTATION — MERSA LIGHTBOX SIGNAGE*
            ------------------------------------------------
            Client: $customerName ($businessName)
            Product: ${result.product.title}
            Tier: ${result.tier.displayName}
            Dimensions: ${result.widthCm.toInt()} x ${result.heightCm.toInt()} cm (${String.format("%.2f", result.faceAreaM2)} m²)
            Illumination: ${result.ledModuleCount} high-lumen LED modules
            Power Supply: Genuine Weatherproof PSU (safety capped at 80% load)
            Sensors: Auto dusk-to-dawn photocell included
            Warranty: ${result.tier.warrantyMonths} Months (Written local guarantee)
            ------------------------------------------------
            TOTAL INVESTMENT: ${String.format("%,.0f", result.finalSellingPrice)} ETB
            Deposit (to start production): ${String.format("%,.0f", result.depositRequired)} ETB (${(result.tier.depositPct * 100).toInt()}%)
            Balance (payable on install & light test): ${String.format("%,.0f", result.balanceOnInstall)} ETB
            Production Lead Time: 3–6 working days from signed approval
            Validity: 7 days
            ------------------------------------------------
            Mersa Corridor Signage Operations
            📞 Phone / WhatsApp: +251 9XX XXX XXX
        """.trimIndent()
    }

    fun generateAmharicQuoteText(result: QuoteResult, customerName: String, businessName: String): String {
        return """
            *የዋጋ ማቅረቢያ (ፕሮፎርማ) — መርሳ ላይት ቦክስ ማስታወቂያ*
            ------------------------------------------------
            ደንበኛ፦ $customerName ($businessName)
            የስራ አይነት፦ ${result.product.title}
            ደረጃ፦ ${result.tier.displayName}
            መጠን፦ ${result.widthCm.toInt()} x ${result.heightCm.toInt()} ሳ.ሜ (${String.format("%.2f", result.faceAreaM2)} ካሬ)
            መብራት፦ ${result.ledModuleCount} ጥራት ያላቸው የLED ሞጁሎች
            ትራንስፎርመር፦ ኦሪጅናል ውሀ የማያሳልፍ (በ80% አቅም የሚሰራ ለረጅም እድሜ)
            ሴንሰር፦ ማታ ራሱ በርቶ ጠዋት የሚጠፋ ፎቶሴል
            ዋስትና፦ ${result.tier.warrantyMonths} ወራት የፅሁፍ ዋስትና ከአካባቢው ፈጣን ጥገና ጋር
            ------------------------------------------------
            ጠቅላላ ዋጋ፦ ${String.format("%,.0f", result.finalSellingPrice)} የኢትዮጵያ ብር
            ቅድመ ክፍያ፦ ${String.format("%,.0f", result.depositRequired)} ብር (${(result.tier.depositPct * 100).toInt()}%)
            ቀሪ ክፍያ፦ ${String.format("%,.0f", result.balanceOnInstall)} ብር (ከተተከለና ከበራ በኋላ የሚከፈል)
            የማጠናቀቂያ ጊዜ፦ ከ3 እስከ 6 የስራ ቀናት
            የዋጋው ፀንቶ መቆያ፦ 7 ቀናት
            ------------------------------------------------
            መርሳ ዘመናዊ የኮሪደር ማስታወቂያ ስራዎች
            📞 ስልክ / ቴሌግራም፦ +251 9XX XXX XXX
        """.trimIndent()
    }
}
