package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prospects")
data class ProspectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String, // e.g. "S2-07"
    val segment: String, // "S1", "S2", "S3", etc.
    val businessName: String,
    val locationLandmark: String,
    val category: String, // "Pharmacy", "Hotel", "Supermarket", "Clinic", "Retail", "Café", "Salon", "Bank", "Other"
    val currentSignage: String = "Faded Banner", // "Faded Banner", "Painted Wall", "Old Flex", "Dead Lightbox", "None"
    val approxWidthCm: Double = 240.0,
    val approxHeightCm: Double = 80.0,
    val isIlluminated: Boolean = false,
    val condition: String = "Poor", // "Good", "Fair", "Poor", "Dead-lit"
    val complianceConcern: String = "Banner on designated corridor street",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val landlordName: String = "",
    val visibilityScore: Int = 2, // 1-3
    val abilityToPayScore: Int = 2, // 1-3
    val urgencyScore: Int = 3, // 1-3
    val influenceScore: Int = 2, // 1-3
    val totalScore: Int = 9, // 4-12
    val priorityGrade: String = "B", // A, B, C
    val estimatedValueEtb: Double = 44000.0,
    val tierGuess: String = "Standard",
    val status: String = "Identified", // "Identified", "Visited", "Surveyed", "Quoted", "Negotiating", "Won", "Lost"
    val followUpDate: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobCode: String, // e.g. "JOB-2026-001"
    val customerName: String,
    val businessName: String,
    val customerPhone: String,
    val tier: String, // "Economy", "Standard", "Premium", "Institutional"
    val productCode: String, // "P1", "P2", "P3", "P4", "P5", "P6"
    val widthCm: Double,
    val heightCm: Double,
    val faceAreaM2: Double,
    val sellingPrice: Double,
    val materialsCost: Double,
    val outsourcingCost: Double,
    val labourCost: Double,
    val transportCost: Double,
    val installationCost: Double,
    val overheadAllocated: Double,
    val contingencyUsed: Double,
    val grossProfit: Double,
    val grossProfitPct: Double,
    val netContribution: Double,
    val isBelowTargetMargin: Boolean,
    val targetMarginPct: Double,
    val depositAmount: Double,
    val depositDate: String,
    val depositReceived: Boolean = false,
    val balanceDue: Double,
    val balanceReceived: Boolean = false,
    val balanceReceivedDate: String = "",
    val daysOutstanding: Int = 0,
    val status: String = "Deposit Pending", // "Deposit Pending", "Material Purchased", "In Fabrication", "Burn-in / QC Passed", "Installed & Completed"
    val warrantyMonths: Int = 12,
    val warrantyStartDate: String = "",
    val warrantyExpiryDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val warrantyId: String, // "WAR-MS-001"
    val businessName: String,
    val ownerName: String,
    val phone: String,
    val location: String,
    val signType: String,
    val signDimensions: String,
    val tier: String,
    val ledBrand: String,
    val ledCount: Int,
    val psuBrand: String,
    val psuRating: String,
    val installDate: String,
    val warrantyExpiry: String,
    val carePlan: String = "None", // "None", "Care Plan Basic", "Care Plan Plus"
    val carePlanAnnualFee: Double = 0.0,
    val nextMaintenanceDate: String = "",
    val totalPaid: Double = 0.0,
    val balanceOutstanding: Double = 0.0,
    val referralsGivenCount: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "Acrylic", "ACP", "LEDs", "PSU", "Welding/Frames", "Electrician", "CNC/Laser", "Flex/Print"
    val location: String, // "Mersa", "Dessie", "Kombolcha", "Woldia", "Addis Ababa"
    val phone: String,
    val qualityScore: Double = 4.0,
    val reliabilityScore: Double = 4.0,
    val speedScore: Double = 4.0,
    val priceScore: Double = 4.0,
    val materialScore: Double = 4.0,
    val capacityScore: Double = 4.0,
    val termsScore: Double = 4.0,
    val emergencyScore: Double = 4.0,
    val weightedScore: Double = 4.0,
    val ratingStatus: String = "Preferred (>=4.0)",
    val notes: String = ""
)

@Entity(tableName = "readiness_items")
data class ReadinessEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemNumber: Int,
    val category: String, // "Regulatory", "Commercial", "Technical", "Operational"
    val title: String,
    val description: String,
    val isVerified: Boolean = false,
    val verifiedDate: String = "",
    val verifiedByNote: String = ""
)
