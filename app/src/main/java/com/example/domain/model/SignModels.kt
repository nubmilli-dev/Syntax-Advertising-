package com.example.domain.model

enum class SignTier(
    val displayName: String,
    val targetMargin: Double,
    val depositPct: Double,
    val warrantyMonths: Int,
    val minJobPrice: Double,
    val bandRateMin: Double,
    val bandRateMax: Double,
    val fixedInstallFee: Double
) {
    ECONOMY(
        displayName = "Economy",
        targetMargin = 0.38,
        depositPct = 0.50,
        warrantyMonths = 6,
        minJobPrice = 15000.0,
        bandRateMin = 8000.0,
        bandRateMax = 12000.0,
        fixedInstallFee = 2500.0
    ),
    STANDARD(
        displayName = "Standard",
        targetMargin = 0.45,
        depositPct = 0.60,
        warrantyMonths = 12,
        minJobPrice = 25000.0,
        bandRateMin = 16000.0,
        bandRateMax = 23000.0,
        fixedInstallFee = 4000.0
    ),
    PREMIUM(
        displayName = "Premium",
        targetMargin = 0.52,
        depositPct = 0.70,
        warrantyMonths = 24,
        minJobPrice = 50000.0,
        bandRateMin = 28000.0,
        bandRateMax = 45000.0,
        fixedInstallFee = 7500.0
    ),
    INSTITUTIONAL(
        displayName = "Institutional",
        targetMargin = 0.45,
        depositPct = 0.50,
        warrantyMonths = 18,
        minJobPrice = 30000.0,
        bandRateMin = 20000.0,
        bandRateMax = 32000.0,
        fixedInstallFee = 5000.0
    )
}

enum class ProductType(
    val code: String,
    val title: String,
    val defaultTier: SignTier,
    val description: String,
    val typicalCustomer: String
) {
    P1_ECO_FLEX("P1", "P1 Eco-Flex Lightbox", SignTier.ECONOMY, "Painted steel tube frame, sheet metal returns, backlit printed flex face", "Small shops, kiosks, salons, cafés"),
    P2_STD_ACRYLIC("P2", "P2 Standard Acrylic", SignTier.STANDARD, "Steel/Alu frame, ACP returns, 3-5mm opal acrylic + translucent vinyl/film, branded LEDs & genuine PSU", "Pharmacies, clinics, restaurants, retail"),
    P3_BLADE("P3", "P3 Double-Face Blade", SignTier.STANDARD, "Double-sided projecting sign, heavy-duty wall bracket, modules both sides", "Busy pedestrian walkways, cross views"),
    P4_PREM_ACP("P4", "P4 Premium Folded ACP", SignTier.PREMIUM, "Folded ACP box, routed push-through or backed acrylic letters, timer, surge protector", "Hotels, supermarkets, banks, flagship stores"),
    P5_LOGO_SHAPE("P5", "P5 Shaped/Round Lightbox", SignTier.STANDARD, "Round (Ø60cm) or custom silhouette, laser cut acrylic face, edge or back-lit", "Cafés, pharmacies (cross), modern brands"),
    P6_CORP_PACKAGE("P6", "P6 Corporate Multi-Sign", SignTier.INSTITUTIONAL, "Integrated bundle: main fascia + projecting blade + directional plates", "Institutions, schools, NGO offices, banks")
}

data class StandardSize(
    val code: String,
    val name: String,
    val widthCm: Double,
    val heightCm: Double,
    val sheetFraction: String,
    val typicalUse: String,
    val economyPrice: Double?,
    val standardPrice: Double?,
    val premiumPrice: Double?
) {
    val faceAreaM2: Double
        get() = (widthCm / 100.0) * (heightCm / 100.0)
}

data class BomItem(
    val category: String,
    val name: String,
    val quantitySpec: String,
    val unitCost: Double,
    val subtotal: Double
)

data class QuoteResult(
    val product: ProductType,
    val tier: SignTier,
    val widthCm: Double,
    val heightCm: Double,
    val faceAreaM2: Double,
    val perimeterM: Double,
    val ledModuleCount: Int,
    val psuWattsRequired: Double,
    val psuWattsSafetyCap: Double, // Loaded to 80% maximum
    val materialsCost: Double,
    val labourCost: Double,
    val transportCost: Double,
    val installationCost: Double,
    val directCost: Double,
    val overheadCost: Double,
    val contingencyCost: Double,
    val loadedCost: Double,
    val basePrice: Double,
    val finalSellingPrice: Double,
    val depositRequired: Double,
    val balanceOnInstall: Double,
    val effectiveRatePerM2: Double,
    val bandStatus: BandStatus,
    val bomItems: List<BomItem>
)

enum class BandStatus(val label: String) {
    OK("Within Target Band"),
    BELOW("BELOW Band Floor (Check Missing Costs)"),
    ABOVE("ABOVE Band Ceiling (Check Supplier Pricing)")
}

data class AddOnSelection(
    val includePhotocell: Boolean = true,
    val includeTimer: Boolean = false,
    val includeSurgeProtector: Boolean = false,
    val isRushOrder: Boolean = false,
    val isHeightAbove4m: Boolean = false,
    val includeOldSignRemoval: Boolean = false,
    val customDesignFromScratch: Boolean = false,
    val blockDiscountApplied: Boolean = false
)
