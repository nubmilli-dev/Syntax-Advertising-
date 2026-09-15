package com.example.presentation.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ProjectEntity
import com.example.domain.engine.DataExchangeEngine
import com.example.presentation.components.SectionHeader
import com.example.presentation.components.StatusBadge
import com.example.presentation.viewmodel.ProjectsViewModel
import com.example.ui.theme.AmberGlowPrimary
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RubyAlert
import com.example.ui.theme.WarningOrange

@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()

    var filterStatus by remember { mutableStateOf("ALL") } // "ALL", "Active", "Completed"
    var selectedProjectForPnl by remember { mutableStateOf<ProjectEntity?>(null) }
    var showFinancialSummaryDialog by remember { mutableStateOf(false) }

    val filteredProjects = projects.filter { p ->
        when (filterStatus) {
            "Active" -> p.status != "Installed & Completed"
            "Completed" -> p.status == "Installed & Completed"
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Projects & Job-Level P&L Tracker",
            subtitle = "Track real material costs, deposit cashflow, and target margins"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips and Financial Report button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ALL", "Active", "Completed").forEach { status ->
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        label = { Text(status) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberGlowPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            OutlinedButton(
                onClick = { showFinancialSummaryDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_export_financials")
            ) {
                Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("P&L Report", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredProjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No production jobs found for this filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("projects_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProjects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onViewPnl = { selectedProjectForPnl = project },
                        onMarkDeposit = {
                            viewModel.markDepositReceived(project)
                            Toast.makeText(context, "Deposit recorded! Materials unlocked.", Toast.LENGTH_SHORT).show()
                        },
                        onMarkBalance = {
                            viewModel.markBalanceReceived(project)
                            Toast.makeText(context, "Balance received! Sign installed & warranty active.", Toast.LENGTH_SHORT).show()
                        },
                        onStatusChange = { nextStatus ->
                            viewModel.updateProjectStatus(project, nextStatus)
                        },
                        onDelete = {
                            viewModel.deleteProject(project)
                            Toast.makeText(context, "Job deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // P&L Breakdown Dialog
    if (selectedProjectForPnl != null) {
        val proj = selectedProjectForPnl!!
        AlertDialog(
            onDismissRequest = { selectedProjectForPnl = null },
            title = {
                Text("${proj.jobCode} — Job P&L Analysis", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "${proj.businessName} (${proj.productCode} ${proj.tier})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Selling Price:", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%,.0f", proj.sellingPrice)} ETB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Materials Cost:", style = MaterialTheme.typography.bodySmall)
                                Text("-${String.format("%,.0f", proj.materialsCost)} ETB", style = MaterialTheme.typography.bodySmall)
                            }
                            if (proj.outsourcingCost > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Outsourced CNC/Routing:", style = MaterialTheme.typography.bodySmall)
                                    Text("-${String.format("%,.0f", proj.outsourcingCost)} ETB", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Direct Labour Cost:", style = MaterialTheme.typography.bodySmall)
                                Text("-${String.format("%,.0f", proj.labourCost)} ETB", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Transport & Delivery:", style = MaterialTheme.typography.bodySmall)
                                Text("-${String.format("%,.0f", proj.transportCost)} ETB", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Installation & Anchors:", style = MaterialTheme.typography.bodySmall)
                                Text("-${String.format("%,.0f", proj.installationCost)} ETB", style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gross Profit:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "${String.format("%,.0f", proj.grossProfit)} ETB (${String.format("%.1f", proj.grossProfitPct)}%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (proj.grossProfitPct >= proj.targetMarginPct) EmeraldSuccess else RubyAlert
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Target Margin (${proj.tier}):", style = MaterialTheme.typography.bodySmall)
                                Text("${proj.targetMarginPct.toInt()}%", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Allocated Workshop Overhead:", style = MaterialTheme.typography.bodySmall)
                                Text("-${String.format("%,.0f", proj.overheadAllocated)} ETB", style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Contribution:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("${String.format("%,.0f", proj.netContribution)} ETB", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = AmberGlowPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedProjectForPnl = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Financial Summary & P&L Statement Dialog (GATE 7 & Step 13)
    if (showFinancialSummaryDialog) {
        val summary = DataExchangeEngine.computeFinancialSummary(projects)
        val pnlScrollState = rememberScrollState()

        AlertDialog(
            onDismissRequest = { showFinancialSummaryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = AmberGlowPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Workshop P&L & Cashflow", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(pnlScrollState)
                        .fillMaxWidth()
                ) {
                    // Revenue & Profit Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("REVENUE & PROFITABILITY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AmberGlowPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Invoiced Revenue:", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%,.0f", summary.totalRevenueEtb)} ETB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Direct Materials & Subcontracts:", style = MaterialTheme.typography.bodySmall)
                                Text("-${String.format("%,.0f", summary.totalDirectCostsEtb)} ETB", style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Overall Gross Margin:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "${String.format("%,.0f", summary.totalGrossProfitEtb)} ETB (${String.format("%.1f", summary.averageGrossProfitPct)}%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Workshop Contribution:", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%,.0f", summary.totalNetContributionEtb)} ETB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AmberGlowPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Cashflow & Receivables Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("LIQUIDITY & RECEIVABLES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ElectricCyanDark)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Cash Collected (Bank/Cash):", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%,.0f", summary.totalCashCollectedEtb)} ETB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("  • Deposits in Hand:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format("%,.0f", summary.totalDepositCollectedEtb)} ETB", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("  • Final Balances Paid:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format("%,.0f", summary.totalBalanceCollectedEtb)} ETB", style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Uncollected Receivables:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "${String.format("%,.0f", summary.totalReceivablesOutstandingEtb)} ETB",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.totalReceivablesOutstandingEtb > 0) RubyAlert else EmeraldSuccess
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val report = DataExchangeEngine.generateFinancialReportText(projects)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Mersa_Signage_Workshop_Financial_Report.txt")
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share P&L Statement"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGlowPrimary, contentColor = Color.Black)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Financial Statement", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFinancialSummaryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    onViewPnl: () -> Unit,
    onMarkDeposit: () -> Unit,
    onMarkBalance: () -> Unit,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewPnl() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AmberGlowPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = project.jobCode,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmberGlowPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = project.businessName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusBadge(
                    label = project.status,
                    color = when (project.status) {
                        "Installed & Completed" -> EmeraldSuccess
                        "Burn-in / QC Passed" -> ElectricCyanDark
                        "Material Purchased" -> WarningOrange
                        else -> AmberGlowPrimary
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${project.productCode} • ${project.tier} • ${project.widthCm.toInt()}x${project.heightCm.toInt()} cm (${String.format("%.2f", project.faceAreaM2)} m²)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Financial Summary Bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Contract Price", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format("%,.0f", project.sellingPrice)} ETB", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Gross Margin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${String.format("%.1f", project.grossProfitPct)}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (project.grossProfitPct >= project.targetMarginPct) EmeraldSuccess else RubyAlert
                        )
                    }
                    Column {
                        Text("Net Profit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format("%,.0f", project.netContribution)} ETB", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AmberGlowPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cash Collection Status
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (project.depositReceived) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (project.depositReceived) EmeraldSuccess else WarningOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (project.depositReceived) "Deposit Paid (${String.format("%,.0f", project.depositAmount)} ETB)" else "Deposit Pending (${String.format("%,.0f", project.depositAmount)} ETB)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (project.depositReceived) FontWeight.Normal else FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (project.balanceReceived) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (project.balanceReceived) EmeraldSuccess else RubyAlert,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (project.balanceReceived) "Balance Paid" else "Due: ${String.format("%,.0f", project.balanceDue)} ETB",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Steppers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewPnl,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("P&L Detail", style = MaterialTheme.typography.labelSmall)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!project.depositReceived) {
                        Button(
                            onClick = onMarkDeposit,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGlowPrimary, contentColor = Color.Black)
                        ) {
                            Text("Collect Deposit", style = MaterialTheme.typography.labelSmall)
                        }
                    } else if (project.status == "Material Purchased") {
                        Button(
                            onClick = { onStatusChange("In Fabrication") },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Start Fabrication", style = MaterialTheme.typography.labelSmall)
                        }
                    } else if (project.status == "In Fabrication") {
                        Button(
                            onClick = { onStatusChange("Burn-in / QC Passed") },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyanDark)
                        ) {
                            Text("Burn-in Pass", style = MaterialTheme.typography.labelSmall)
                        }
                    } else if (project.status == "Burn-in / QC Passed") {
                        Button(
                            onClick = onMarkBalance,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            Text("Install & Collect Balance", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
