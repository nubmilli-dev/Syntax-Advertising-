package com.example.domain.engine

import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProspectEntity

data class CsvParseError(
    val rowNumber: Int,
    val rawLine: String,
    val message: String
)

data class CsvImportResult<T>(
    val validItems: List<T>,
    val errors: List<CsvParseError>,
    val totalRowsProcessed: Int
)

data class FinancialSummary(
    val totalProjectsCount: Int,
    val activeJobsCount: Int,
    val completedJobsCount: Int,
    val totalRevenueEtb: Double,
    val totalDirectCostsEtb: Double,
    val totalGrossProfitEtb: Double,
    val averageGrossProfitPct: Double,
    val totalNetContributionEtb: Double,
    val totalDepositCollectedEtb: Double,
    val totalBalanceCollectedEtb: Double,
    val totalCashCollectedEtb: Double,
    val totalReceivablesOutstandingEtb: Double
)

object DataExchangeEngine {

    /**
     * Exports a list of corridor survey prospects to RFC 4180 compliant CSV.
     */
    fun exportProspectsToCsv(prospects: List<ProspectEntity>): String {
        val sb = StringBuilder()
        sb.append("Code,Segment,BusinessName,LocationLandmark,Category,CurrentSignage,WidthCm,HeightCm,Condition,OwnerPhone,TotalScore,PriorityGrade,EstimatedValueETB,Status\n")
        prospects.forEach { p ->
            sb.append(escapeCsv(p.code)).append(",")
            sb.append(escapeCsv(p.segment)).append(",")
            sb.append(escapeCsv(p.businessName)).append(",")
            sb.append(escapeCsv(p.locationLandmark)).append(",")
            sb.append(escapeCsv(p.category)).append(",")
            sb.append(escapeCsv(p.currentSignage)).append(",")
            sb.append(p.approxWidthCm).append(",")
            sb.append(p.approxHeightCm).append(",")
            sb.append(escapeCsv(p.condition)).append(",")
            sb.append(escapeCsv(p.ownerPhone)).append(",")
            sb.append(p.totalScore).append(",")
            sb.append(escapeCsv(p.priorityGrade)).append(",")
            sb.append(p.estimatedValueEtb).append(",")
            sb.append(escapeCsv(p.status)).append("\n")
        }
        return sb.toString()
    }

    /**
     * Parses and validates a CSV text string of corridor prospects.
     * Supports flexible column mapping and graceful fallback.
     */
    fun parseAndValidateProspectsCsv(csvContent: String): CsvImportResult<ProspectEntity> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return CsvImportResult(emptyList(), emptyList(), 0)
        }

        val validList = mutableListOf<ProspectEntity>()
        val errorList = mutableListOf<CsvParseError>()

        // Check if first row is header
        val hasHeader = lines.first().contains("Business", ignoreCase = true) ||
                lines.first().contains("Code", ignoreCase = true) ||
                lines.first().contains("Name", ignoreCase = true)

        val dataLines = if (hasHeader) lines.drop(1) else lines

        dataLines.forEachIndexed { index, line ->
            val rowNumber = if (hasHeader) index + 2 else index + 1
            val tokens = parseCsvLine(line)

            if (tokens.size < 3) {
                errorList.add(
                    CsvParseError(
                        rowNumber = rowNumber,
                        rawLine = line,
                        message = "Row has only ${tokens.size} columns. At least Business Name, Location, and Category are required."
                    )
                )
                return@forEachIndexed
            }

            try {
                // Expected order or fallback: Code, Segment, BusinessName, Location, Category...
                // Or simplified: BusinessName, Location, Category, Phone, EstValue
                var code = "IMP-${rowNumber}"
                var segment = "Corridor"
                var businessName = ""
                var location = ""
                var category = "Retail"
                var phone = ""
                var width = 240.0
                var height = 80.0
                var estimatedValue = 44000.0
                var status = "Identified"

                if (tokens.size >= 14) {
                    // Full format
                    code = tokens[0].trim().ifBlank { "IMP-$rowNumber" }
                    segment = tokens[1].trim().ifBlank { "Corridor" }
                    businessName = tokens[2].trim()
                    location = tokens[3].trim()
                    category = tokens[4].trim().ifBlank { "Retail" }
                    width = tokens[6].trim().toDoubleOrNull() ?: 240.0
                    height = tokens[7].trim().toDoubleOrNull() ?: 80.0
                    phone = tokens[9].trim()
                    estimatedValue = tokens[12].trim().toDoubleOrNull() ?: 44000.0
                    status = tokens[13].trim().ifBlank { "Identified" }
                } else {
                    // Flexible format: Name, Location, Category, [Phone], [Width], [Height], [Value]
                    businessName = tokens[0].trim()
                    location = tokens[1].trim()
                    category = tokens[2].trim().ifBlank { "Retail" }
                    if (tokens.size > 3) phone = tokens[3].trim()
                    if (tokens.size > 4) width = tokens[4].trim().toDoubleOrNull() ?: 240.0
                    if (tokens.size > 5) height = tokens[5].trim().toDoubleOrNull() ?: 80.0
                    if (tokens.size > 6) estimatedValue = tokens[6].trim().toDoubleOrNull() ?: 44000.0
                }

                if (businessName.isBlank()) {
                    errorList.add(
                        CsvParseError(
                            rowNumber = rowNumber,
                            rawLine = line,
                            message = "Business Name cannot be empty."
                        )
                    )
                    return@forEachIndexed
                }

                // Calculate default scores
                val (priorityScore, grade) = OperationsEngines.computePriorityScore(2, 2, 3, 2)

                validList.add(
                    ProspectEntity(
                        code = code,
                        segment = segment,
                        businessName = businessName,
                        locationLandmark = location,
                        category = category,
                        currentSignage = "Identified for upgrade",
                        approxWidthCm = width,
                        approxHeightCm = height,
                        condition = "Fair",
                        complianceConcern = "Standard corridor specification check",
                        ownerPhone = phone,
                        visibilityScore = 2,
                        abilityToPayScore = 2,
                        urgencyScore = 3,
                        influenceScore = 2,
                        totalScore = priorityScore,
                        priorityGrade = grade,
                        estimatedValueEtb = estimatedValue,
                        tierGuess = "Standard",
                        status = status
                    )
                )
            } catch (e: Exception) {
                errorList.add(
                    CsvParseError(
                        rowNumber = rowNumber,
                        rawLine = line,
                        message = "Parse error: ${e.localizedMessage ?: "Invalid numeric or date format"}"
                    )
                )
            }
        }

        return CsvImportResult(
            validItems = validList,
            errors = errorList,
            totalRowsProcessed = dataLines.size
        )
    }

    /**
     * Calculates high-level aggregate financial metrics across all active and completed jobs.
     */
    fun computeFinancialSummary(projects: List<ProjectEntity>): FinancialSummary {
        val totalProjects = projects.size
        val activeCount = projects.count { it.status != "Installed & Completed" }
        val completedCount = projects.count { it.status == "Installed & Completed" }

        val totalRev = projects.sumOf { it.sellingPrice }
        val totalDirect = projects.sumOf {
            it.materialsCost + it.outsourcingCost + it.labourCost + it.transportCost + it.installationCost
        }
        val totalGross = projects.sumOf { it.grossProfit }
        val avgGrossPct = if (totalRev > 0) (totalGross / totalRev) * 100.0 else 0.0
        val totalNet = projects.sumOf { it.netContribution }

        val totalDepositCollected = projects.filter { it.depositReceived }.sumOf { it.depositAmount }
        val totalBalanceCollected = projects.filter { it.balanceReceived }.sumOf { it.balanceDue }
        val totalCashCollected = totalDepositCollected + totalBalanceCollected

        val totalReceivables = projects.sumOf { p ->
            var uncollected = 0.0
            if (!p.depositReceived) uncollected += p.depositAmount
            if (!p.balanceReceived) uncollected += p.balanceDue
            uncollected
        }

        return FinancialSummary(
            totalProjectsCount = totalProjects,
            activeJobsCount = activeCount,
            completedJobsCount = completedCount,
            totalRevenueEtb = totalRev,
            totalDirectCostsEtb = totalDirect,
            totalGrossProfitEtb = totalGross,
            averageGrossProfitPct = avgGrossPct,
            totalNetContributionEtb = totalNet,
            totalDepositCollectedEtb = totalDepositCollected,
            totalBalanceCollectedEtb = totalBalanceCollected,
            totalCashCollectedEtb = totalCashCollected,
            totalReceivablesOutstandingEtb = totalReceivables
        )
    }

    /**
     * Generates a formal, printable text report summarizing financial performance and job health.
     */
    fun generateFinancialReportText(projects: List<ProjectEntity>): String {
        val summary = computeFinancialSummary(projects)
        val sb = StringBuilder()

        sb.append("===================================================\n")
        sb.append("       MERSA SIGNAGE • WORKSHOP P&L STATEMENT\n")
        sb.append("===================================================\n\n")

        sb.append("1. EXECUTIVE REVENUE & MARGIN SUMMARY\n")
        sb.append("---------------------------------------------------\n")
        sb.append("• Total Signage Projects: ${summary.totalProjectsCount} (${summary.activeJobsCount} Active, ${summary.completedJobsCount} Delivered)\n")
        sb.append("• Total Invoiced Revenue: ${String.format("%,.0f", summary.totalRevenueEtb)} ETB\n")
        sb.append("• Direct Materials & Subs: ${String.format("%,.0f", summary.totalDirectCostsEtb)} ETB\n")
        sb.append("• Overall Gross Profit:    ${String.format("%,.0f", summary.totalGrossProfitEtb)} ETB (${String.format("%.1f", summary.averageGrossProfitPct)}%)\n")
        sb.append("• Net Workshop Contribution:${String.format("%,.0f", summary.totalNetContributionEtb)} ETB\n\n")

        sb.append("2. CASHFLOW DISCIPLINE & LIQUIDITY\n")
        sb.append("---------------------------------------------------\n")
        sb.append("• Total Cash Collected:    ${String.format("%,.0f", summary.totalCashCollectedEtb)} ETB\n")
        sb.append("  - Deposits In Hand:      ${String.format("%,.0f", summary.totalDepositCollectedEtb)} ETB\n")
        sb.append("  - Final Balances In Hand:${String.format("%,.0f", summary.totalBalanceCollectedEtb)} ETB\n")
        sb.append("• Receivables Outstanding: ${String.format("%,.0f", summary.totalReceivablesOutstandingEtb)} ETB\n\n")

        sb.append("3. INDIVIDUAL JOB AUDIT TRAIL\n")
        sb.append("---------------------------------------------------\n")
        projects.forEach { p ->
            val marginTag = if (p.grossProfitPct >= p.targetMarginPct) "[HEALTHY]" else "[ALERT]"
            sb.append("• ${p.jobCode} - ${p.businessName} (${p.tier})\n")
            sb.append("  Price: ${String.format("%,.0f", p.sellingPrice)} ETB | GP: ${String.format("%,.0f", p.grossProfit)} ETB (${String.format("%.1f", p.grossProfitPct)}%) $marginTag\n")
            sb.append("  Status: ${p.status} | Deposit: ${if (p.depositReceived) "PAID" else "PENDING"} | Balance: ${if (p.balanceReceived) "PAID" else "DUE"}\n\n")
        }

        sb.append("===================================================\n")
        sb.append("Report generated by Mersa Signage Android Mobile OS\n")
        sb.append("Corridor Infrastructure & Signage Workshop System\n")

        return sb.toString()
    }

    /**
     * Generates a corridor survey executive pipeline summary report.
     */
    fun generateProspectsReportText(prospects: List<ProspectEntity>): String {
        val total = prospects.size
        val countA = prospects.count { it.priorityGrade == "A" }
        val countB = prospects.count { it.priorityGrade == "B" }
        val countC = prospects.count { it.priorityGrade == "C" }
        val totalPipelineValue = prospects.sumOf { it.estimatedValueEtb }
        val wonCount = prospects.count { it.status == "Won" }
        val conversionRate = if (total > 0) (wonCount.toDouble() / total) * 100.0 else 0.0

        val sb = StringBuilder()
        sb.append("===================================================\n")
        sb.append("    MERSA CORRIDOR STREET SURVEY PIPELINE AUDIT\n")
        sb.append("===================================================\n\n")
        sb.append("• Total Surveyed Storefronts: $total\n")
        sb.append("• Grade A (Immediate Priority): $countA (${if (total > 0) countA * 100 / total else 0}%)\n")
        sb.append("• Grade B (Warm Upgrade Need):  $countB (${if (total > 0) countB * 100 / total else 0}%)\n")
        sb.append("• Grade C (Low Urgency):       $countC\n")
        sb.append("• Total Est. Pipeline Value:   ${String.format("%,.0f", totalPipelineValue)} ETB\n")
        sb.append("• Closed / Won Conversion:     ${String.format("%.1f", conversionRate)}% ($wonCount won)\n\n")

        sb.append("TOP IMMEDIATE CORRIDOR PROSPECTS (GRADE A):\n")
        sb.append("---------------------------------------------------\n")
        prospects.filter { it.priorityGrade == "A" }.forEach { p ->
            sb.append("• [Score ${p.totalScore}/12] ${p.businessName} - ${p.locationLandmark}\n")
            sb.append("  Current: ${p.currentSignage} (${p.condition}) | Est. Value: ${String.format("%,.0f", p.estimatedValueEtb)} ETB\n")
            if (p.ownerPhone.isNotBlank()) sb.append("  Phone: ${p.ownerPhone} | Status: ${p.status}\n")
            sb.append("\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    current.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString())
                current.setLength(0)
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
