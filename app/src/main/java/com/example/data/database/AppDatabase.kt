package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CustomerDao
import com.example.data.dao.ProjectDao
import com.example.data.dao.ProspectDao
import com.example.data.dao.ReadinessDao
import com.example.data.dao.SupplierDao
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProspectEntity
import com.example.data.entity.ReadinessEntity
import com.example.data.entity.SupplierEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProspectEntity::class,
        ProjectEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        ReadinessEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prospectDao(): ProspectDao
    abstract fun projectDao(): ProjectDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun readinessDao(): ReadinessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mersa_signage_control.db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                seedDatabase(database)
            }
        }
    }
}

suspend fun seedDatabase(database: AppDatabase) {
    // 1. Seed Corridor Prospects
    val initialProspects = listOf(
        ProspectEntity(
            code = "S2-07",
            segment = "S2",
            businessName = "Selam Hiwot Pharmacy (ሰላም ህይወት መድሀኒት ቤት)",
            locationLandmark = "Opposite Main Bus Station",
            category = "Pharmacy",
            currentSignage = "Faded Banner",
            approxWidthCm = 240.0,
            approxHeightCm = 80.0,
            isIlluminated = false,
            condition = "Poor",
            complianceConcern = "Faded fabric banner violates corridor standard",
            ownerName = "Dr. Dawit Tadesse",
            ownerPhone = "0911234567",
            landlordName = "Wollo Building Trust",
            visibilityScore = 3,
            abilityToPayScore = 3,
            urgencyScore = 3,
            influenceScore = 2,
            totalScore = 11,
            priorityGrade = "A",
            estimatedValueEtb = 44000.0,
            tierGuess = "Standard",
            status = "Surveyed",
            followUpDate = "Tomorrow 10:00 AM",
            notes = "High night traffic. Needs green cross graphic + Amharic lettering. Ready for mockup pitch."
        ),
        ProspectEntity(
            code = "S1-03",
            segment = "S1",
            businessName = "Tana Café & Pastry (ጣና ካፌ)",
            locationLandmark = "Next to Commercial Bank of Ethiopia",
            category = "Café",
            currentSignage = "Painted Shutter / No Box",
            approxWidthCm = 300.0,
            approxHeightCm = 90.0,
            isIlluminated = false,
            condition = "Poor",
            complianceConcern = "Corridor frontage banner removal notice received",
            ownerName = "Wro. Almaz Mengistu",
            ownerPhone = "0923456789",
            landlordName = "Ato Getachew",
            visibilityScore = 3,
            abilityToPayScore = 2,
            urgencyScore = 3,
            influenceScore = 3,
            totalScore = 11,
            priorityGrade = "A",
            estimatedValueEtb = 32000.0,
            tierGuess = "Standard",
            status = "Visited",
            followUpDate = "Day 5 afternoon",
            notes = "Interested in circular blade logo sign + main fascia. Requested electricity monthly cost estimate."
        ),
        ProspectEntity(
            code = "S3-12",
            segment = "S3",
            businessName = "Baraka Supermarket (በረካ ሱፐርማርኬት)",
            locationLandmark = "Central Corridor Junction",
            category = "Supermarket",
            currentSignage = "Dead-lit Old Box (Cheap Driver Failed)",
            approxWidthCm = 240.0,
            approxHeightCm = 120.0,
            isIlluminated = false,
            condition = "Dead-lit",
            complianceConcern = "Dark box looks abandoned at night",
            ownerName = "Ato Mohammed Nur",
            ownerPhone = "0934567890",
            landlordName = "Owner Occupied",
            visibilityScore = 3,
            abilityToPayScore = 3,
            urgencyScore = 2,
            influenceScore = 3,
            totalScore = 11,
            priorityGrade = "A",
            estimatedValueEtb = 82000.0,
            tierGuess = "Premium",
            status = "Quoted",
            followUpDate = "Friday morning",
            notes = "Previous sign failed after 4 months due to cheap unbranded PSU. Wants Premium Folded ACP with surge protector."
        ),
        ProspectEntity(
            code = "S2-15",
            segment = "S2",
            businessName = "Abyssinia Modern Electronics (አቢሲኒያ ኤሌክትሮኒክስ)",
            locationLandmark = "Mersa High Street Mid-block",
            category = "Retail",
            currentSignage = "Old Printed Flex Box",
            approxWidthCm = 240.0,
            approxHeightCm = 60.0,
            isIlluminated = true,
            condition = "Fair",
            complianceConcern = "Non-uniform height with adjacent shops",
            ownerName = "Ato Daniel Assefa",
            ownerPhone = "0945678901",
            landlordName = "Mersa Trade Complex",
            visibilityScore = 2,
            abilityToPayScore = 2,
            urgencyScore = 2,
            influenceScore = 2,
            totalScore = 8,
            priorityGrade = "B",
            estimatedValueEtb = 39500.0,
            tierGuess = "Standard",
            status = "Identified",
            followUpDate = "Day 6",
            notes = "Can be bundled with 3 adjacent shops for 10% building package discount."
        ),
        ProspectEntity(
            code = "S1-09",
            segment = "S1",
            businessName = "Bole Fashion Tailoring & Salon (ቦሌ ፋሽን)",
            locationLandmark = "Near Kebele 01 Administration",
            category = "Salon",
            currentSignage = "Vinyl Banner",
            approxWidthCm = 120.0,
            approxHeightCm = 60.0,
            isIlluminated = false,
            condition = "Poor",
            complianceConcern = "Banner removal mandated",
            ownerName = "Wro. Tigist Haile",
            ownerPhone = "0956789012",
            landlordName = "Kebele Rental",
            visibilityScore = 1,
            abilityToPayScore = 1,
            urgencyScore = 3,
            influenceScore = 1,
            totalScore = 6,
            priorityGrade = "C",
            estimatedValueEtb = 15000.0,
            tierGuess = "Economy",
            status = "Identified",
            followUpDate = "Day 7",
            notes = "Budget constrained. Pitch S1 Economy Compliance Basic at 15,000 ETB."
        )
    )
    database.prospectDao().insertAll(initialProspects)

    // 2. Seed Active Projects
    val initialProjects = listOf(
        ProjectEntity(
            jobCode = "JOB-2026-001",
            customerName = "Havana Hotel GM",
            businessName = "Havana Hotel Mersa",
            customerPhone = "0911998877",
            tier = "Premium",
            productCode = "P4",
            widthCm = 240.0,
            heightCm = 120.0,
            faceAreaM2 = 3.0,
            sellingPrice = 116000.0,
            materialsCost = 42000.0,
            outsourcingCost = 14000.0,
            labourCost = 6500.0,
            transportCost = 2500.0,
            installationCost = 7500.0,
            overheadAllocated = 8700.0,
            contingencyUsed = 2000.0,
            grossProfit = 43500.0,
            grossProfitPct = 37.5,
            netContribution = 32800.0,
            isBelowTargetMargin = false,
            targetMarginPct = 52.0,
            depositAmount = 81200.0,
            depositDate = "2026-09-08",
            depositReceived = true,
            balanceDue = 34800.0,
            balanceReceived = false,
            daysOutstanding = 2,
            status = "Burn-in / QC Passed",
            warrantyMonths = 24,
            warrantyStartDate = "2026-09-14",
            warrantyExpiryDate = "2028-09-14"
        ),
        ProjectEntity(
            jobCode = "JOB-2026-002",
            customerName = "Ato Belayneh Kassa",
            businessName = "Mersa Dairy & Fresh Milk",
            customerPhone = "0912334455",
            tier = "Standard",
            productCode = "P2",
            widthCm = 240.0,
            heightCm = 80.0,
            faceAreaM2 = 2.0,
            sellingPrice = 44000.0,
            materialsCost = 15200.0,
            outsourcingCost = 0.0,
            labourCost = 2200.0,
            transportCost = 700.0,
            installationCost = 4000.0,
            overheadAllocated = 2650.0,
            contingencyUsed = 500.0,
            grossProfit = 21900.0,
            grossProfitPct = 49.7,
            netContribution = 18750.0,
            isBelowTargetMargin = false,
            targetMarginPct = 45.0,
            depositAmount = 26400.0,
            depositDate = "2026-09-11",
            depositReceived = true,
            balanceDue = 17600.0,
            balanceReceived = true,
            balanceReceivedDate = "2026-09-14",
            daysOutstanding = 0,
            status = "Installed & Completed",
            warrantyMonths = 12,
            warrantyStartDate = "2026-09-14",
            warrantyExpiryDate = "2027-09-14"
        )
    )
    database.projectDao().insertAll(initialProjects)

    // 3. Seed Installed Customers & Care Plans
    val initialCustomers = listOf(
        CustomerEntity(
            warrantyId = "WAR-MS-001",
            businessName = "Mersa City Administration Office (የመርሳ ከተማ አስተዳደር)",
            ownerName = "Corridor Project Coordinator",
            phone = "0333310022",
            location = "Main Administration Compound",
            signType = "P2 Standard Illuminated Architectural Fascia",
            signDimensions = "360 x 90 cm",
            tier = "Standard",
            ledBrand = "High-Lumen Injection 12V",
            ledCount = 180,
            psuBrand = "MeanWell IP67 300W",
            psuRating = "300W Weatherproof",
            installDate = "2026-08-20",
            warrantyExpiry = "2027-08-20",
            carePlan = "Care Plan Basic",
            carePlanAnnualFee = 4500.0,
            nextMaintenanceDate = "2027-02-20",
            totalPaid = 68000.0,
            balanceOutstanding = 0.0,
            referralsGivenCount = 4,
            notes = "Primary governmental anchor reference. Pristine installation, flawless night photos."
        ),
        CustomerEntity(
            warrantyId = "WAR-MS-002",
            businessName = "Mersa Dairy & Fresh Milk",
            ownerName = "Ato Belayneh Kassa",
            phone = "0912334455",
            location = "Market Corridor Block 3",
            signType = "P2 Standard Opal Acrylic + Vinyl",
            signDimensions = "240 x 80 cm",
            tier = "Standard",
            ledBrand = "Injection Module 12V 1.2W",
            ledCount = 100,
            psuBrand = "Branded Weatherproof 150W",
            psuRating = "150W (Capped at 80%)",
            installDate = "2026-09-14",
            warrantyExpiry = "2027-09-14",
            carePlan = "Care Plan Basic",
            carePlanAnnualFee = 2800.0,
            nextMaintenanceDate = "2027-03-14",
            totalPaid = 44000.0,
            balanceOutstanding = 0.0,
            referralsGivenCount = 1,
            notes = "Customer thrilled with dusk photocell operation. Replaced faded vinyl banner."
        )
    )
    database.customerDao().insertAll(initialCustomers)

    // 4. Seed Suppliers & Partner Network
    val initialSuppliers = listOf(
        SupplierEntity(
            name = "Wollo Precision Metal Fabrication",
            category = "Welding/Frames",
            location = "Mersa",
            phone = "0911445566",
            qualityScore = 4.5,
            reliabilityScore = 4.5,
            speedScore = 4.5,
            priceScore = 4.0,
            materialScore = 4.0,
            capacityScore = 4.0,
            termsScore = 4.0,
            emergencyScore = 4.5,
            weightedScore = 4.3,
            ratingStatus = "Preferred (>=4.0)",
            notes = "Signed 1-page agreement. Delivers welded frames primed within 48h. Drain holes standard."
        ),
        SupplierEntity(
            name = "Dessie Laser & CNC Crafts",
            category = "CNC/Laser",
            location = "Dessie",
            phone = "0922556677",
            qualityScore = 4.5,
            reliabilityScore = 4.0,
            speedScore = 3.8,
            priceScore = 3.5,
            materialScore = 4.5,
            capacityScore = 4.0,
            termsScore = 3.5,
            emergencyScore = 3.5,
            weightedScore = 4.0,
            ratingStatus = "Preferred (>=4.0)",
            notes = "High-precision routing for folded ACP and push-through acrylic letters. 3-day turnaround."
        ),
        SupplierEntity(
            name = "Addis Signage Wholesalers (Merkato)",
            category = "LEDs & PSUs",
            location = "Addis Ababa",
            phone = "0933667788",
            qualityScore = 4.2,
            reliabilityScore = 4.2,
            speedScore = 3.5,
            priceScore = 4.5,
            materialScore = 4.2,
            capacityScore = 5.0,
            termsScore = 4.0,
            emergencyScore = 3.0,
            weightedScore = 4.1,
            ratingStatus = "Preferred (>=4.0)",
            notes = "Primary bulk importer of genuine MeanWell power supplies and Samsung bin LED modules."
        ),
        SupplierEntity(
            name = "Kombolcha Industrial Supplies",
            category = "Acrylic & ACP",
            location = "Kombolcha",
            phone = "0944778899",
            qualityScore = 4.0,
            reliabilityScore = 4.0,
            speedScore = 4.0,
            priceScore = 4.0,
            materialScore = 4.0,
            capacityScore = 4.0,
            termsScore = 4.0,
            emergencyScore = 3.5,
            weightedScore = 4.0,
            ratingStatus = "Preferred (>=4.0)",
            notes = "Opal cast acrylic 3mm/4mm and architectural 4mm ACP sheets (PVDF coated)."
        )
    )
    database.supplierDao().insertAll(initialSuppliers)

    // 5. Seed 20-Point Readiness Verification Checklist
    val initialReadiness = listOf(
        ReadinessEntity(itemNumber = 1, category = "Regulatory", title = "City Standard Documentation", description = "Obtain written or photographed corridor signage standard dimensions and height rules from City Administration.", isVerified = true, verifiedDate = "2026-09-12", verifiedByNote = "Verified at Corridor Infrastructure Office"),
        ReadinessEntity(itemNumber = 2, category = "Regulatory", title = "Enforcement Deadline Confirmation", description = "Confirm official grace period and enforcement schedule for banner removals on primary corridor.", isVerified = true, verifiedDate = "2026-09-12", verifiedByNote = "Phase 1 active, 30 days notice posted"),
        ReadinessEntity(itemNumber = 3, category = "Regulatory", title = "Building Uniformity Guidelines", description = "Verify whether uniform fascia height and color scheme is mandated per multi-tenant building.", isVerified = true, verifiedDate = "2026-09-12", verifiedByNote = "Horizontal alignment mandated above shop shutters"),
        ReadinessEntity(itemNumber = 4, category = "Regulatory", title = "Municipal Advertising Fee Schedule", description = "Inspect revenue office outdoor sign tax schedule to include permit exclusions in quotation contract.", isVerified = true, verifiedDate = "2026-09-13", verifiedByNote = "Annual advertising tax schedule recorded; excluded in proforma"),
        ReadinessEntity(itemNumber = 5, category = "Commercial", title = "Corridor Frontage Census", description = "Street survey minimum 60 commercial frontages mapped with geotags and current sign conditions.", isVerified = true, verifiedDate = "2026-09-13", verifiedByNote = "85 frontages mapped across segments S1, S2, S3"),
        ReadinessEntity(itemNumber = 6, category = "Commercial", title = "3-Quote Material Verification", description = "Obtain 3 competitive quotes each for acrylic, ACP, LED modules, and PSUs to anchor cost model.", isVerified = true, verifiedDate = "2026-09-14", verifiedByNote = "Quotes collected from Mersa, Kombolcha, and Addis Merkato"),
        ReadinessEntity(itemNumber = 7, category = "Commercial", title = "Standardized Price List Published", description = "Lock 3-tier prices (Economy, Standard, Premium) with strict minimum job floors and band checks.", isVerified = true, verifiedDate = "2026-09-14", verifiedByNote = "Costing engine validated with 45% target gross margin"),
        ReadinessEntity(itemNumber = 8, category = "Commercial", title = "One-Street 10-Shop Field Test", description = "Test 3-tier quotes on 10 prospective shop owners in segment S2; confirm tier distribution and objections.", isVerified = true, verifiedDate = "2026-09-14", verifiedByNote = "7/10 receptive; 2 ordered S3 Standard, 1 S1 Economy"),
        ReadinessEntity(itemNumber = 9, category = "Technical", title = "Battery-Powered Lit Demo Box", description = "Build portable 60x60cm demo box with battery switch to demonstrate daytime and night illumination in shops.", isVerified = true, verifiedDate = "2026-09-11", verifiedByNote = "Operational with 12V 7Ah rechargeable pack"),
        ReadinessEntity(itemNumber = 10, category = "Technical", title = "Pre-Stocked Installation Kit", description = "Essential installation gear: laser level, hammer drill, masonry expansion bolts, harness, multimeter, crimp tool.", isVerified = true, verifiedDate = "2026-09-10", verifiedByNote = "Full field kit inspected and tested"),
        ReadinessEntity(itemNumber = 11, category = "Technical", title = "Power Supply 80% Safety Load Rule", description = "Ensure all PSU calculations automatically cap continuous wattage draw at 80% to eliminate driver burnout.", isVerified = true, verifiedDate = "2026-09-10", verifiedByNote = "Enforced in CostingEngine logic"),
        ReadinessEntity(itemNumber = 12, category = "Technical", title = "12–24h Workshop Burn-In Protocol", description = "Enforce continuous 12-hour burn-in run before any sign leaves workshop to catch infant LED failures.", isVerified = true, verifiedDate = "2026-09-11", verifiedByNote = "Documented in Production SOP"),
        ReadinessEntity(itemNumber = 13, category = "Operational", title = "Local Metal Fabricator Agreement", description = "Sign 1-page non-solicitation agreement with local metal workshop for 48h frame fabrication.", isVerified = true, verifiedDate = "2026-09-12", verifiedByNote = "Wollo Precision Metal under active agreement"),
        ReadinessEntity(itemNumber = 14, category = "Operational", title = "Licensed Electrician Subcontract", description = "Partner with certified electrician for building main connections and code compliance.", isVerified = true, verifiedDate = "2026-09-12", verifiedByNote = "Local licensed electrician on retainer per install"),
        ReadinessEntity(itemNumber = 15, category = "Operational", title = "Dessie/Addis CNC Cutting Channel", description = "Establish file transfer and 3-day turnaround with CNC router/laser cutting service for Premium jobs.", isVerified = true, verifiedDate = "2026-09-13", verifiedByNote = "Telegram digital file workflow established with Dessie shop"),
        ReadinessEntity(itemNumber = 16, category = "Operational", title = "Bilingual Quotation & Approval Forms", description = "Prepare Amharic and English printed survey, proforma, customer sign-off (including spelling) forms.", isVerified = true, verifiedDate = "2026-09-11", verifiedByNote = "Approval forms require signature for both English & Amharic spelling"),
        ReadinessEntity(itemNumber = 17, category = "Operational", title = "Safety & Work at Height Protocol", description = "2-person installation rule, pavement caution barrier, safety harness above 3m, power disconnect verification.", isVerified = true, verifiedDate = "2026-09-10", verifiedByNote = "Strict stop-work rule enforced for windy or wet weather"),
        ReadinessEntity(itemNumber = 18, category = "Operational", title = "Deposit-Funded Cash Flow Rule", description = "Strict cash discipline: 0% material purchase without 50-70% client deposit. Balance collected at install.", isVerified = true, verifiedDate = "2026-09-12", verifiedByNote = "Bank & telebirr separate sub-account established"),
        ReadinessEntity(itemNumber = 19, category = "Commercial", title = "Institutional Pipeline Outreach", description = "Submit introductory letters and portfolio to 5 institutional targets (Hotels, MFIs, Health Centers).", isVerified = false, verifiedDate = "", verifiedByNote = "Letters prepared; delivery scheduled Week 2"),
        ReadinessEntity(itemNumber = 20, category = "Technical", title = "Equipment ROI Verification Rule", description = "Require 3 consecutive months of data showing Benefit >= 1.5x Ownership Cost before purchasing CNC/Laser.", isVerified = true, verifiedDate = "2026-09-14", verifiedByNote = "Automated in ROI calculation module")
    )
    database.readinessDao().insertAll(initialReadiness)
}
