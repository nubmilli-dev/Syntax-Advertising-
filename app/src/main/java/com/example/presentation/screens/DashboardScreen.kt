package com.example.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.engine.MarketScenario
import com.example.presentation.components.PriorityGradeBadge
import com.example.presentation.components.SectionHeader
import com.example.presentation.components.StatCard
import com.example.presentation.components.StatusBadge
import com.example.presentation.viewmodel.DashboardViewModel
import com.example.ui.theme.AmberGlowDark
import com.example.ui.theme.AmberGlowPrimary
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RubyAlert
import com.example.ui.theme.WarningOrange

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToCalculator: () -> Unit,
    onNavigateToProspects: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToOperations: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Hero Card with Visual Corridor Artwork & Branding
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_dashboard_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.img_corridor_hero),
                    contentDescription = "Mersa Corridor at Dusk",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(top = 40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = AmberGlowPrimary.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberGlowPrimary)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(AmberGlowPrimary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MERSA CORRIDOR CONTROL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = AmberGlowPrimary
                                )
                            }
                        }

                        // Scenario Switch Button
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.clickable { viewModel.toggleScenario() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Toggle Scenario",
                                    tint = ElectricCyanDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.scenario == MarketScenario.SCENARIO_A) "Scenario A" else "Scenario B",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Lightbox Signage Execution System",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Asset-light • Deposit-funded • Samsung Galaxy A56 Optimized",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scenario Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.scenario == MarketScenario.SCENARIO_A) {
                    EmeraldSuccess.copy(alpha = 0.12f)
                } else {
                    WarningOrange.copy(alpha = 0.12f)
                }
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (uiState.scenario == MarketScenario.SCENARIO_A) EmeraldSuccess.copy(alpha = 0.4f) else WarningOrange.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (uiState.scenario == MarketScenario.SCENARIO_A) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (uiState.scenario == MarketScenario.SCENARIO_A) EmeraldSuccess else WarningOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.scenario.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = uiState.scenario.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Primary Business Health Stats Grid
        SectionHeader(
            title = "Financial & Production Overview",
            subtitle = "Direct real-time P&L indicators from active jobs"
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = "Invoiced Revenue",
                value = "${String.format("%,.0f", uiState.totalRevenue)} ETB",
                subtitle = "${uiState.completedInstallsCount} signs installed",
                icon = Icons.Default.MonetizationOn,
                accentColor = AmberGlowPrimary,
                modifier = Modifier.weight(1f),
                testTag = "stat_revenue"
            )
            Spacer(modifier = Modifier.width(10.dp))
            StatCard(
                title = "Average Gross Margin",
                value = "${String.format("%.1f", uiState.averageGrossMarginPct)}%",
                subtitle = "Target: ≥40% (Loaded)",
                icon = Icons.Default.Speed,
                accentColor = if (uiState.averageGrossMarginPct >= 40.0) EmeraldSuccess else RubyAlert,
                modifier = Modifier.weight(1f),
                testTag = "stat_margin"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = "Outstanding Balances",
                value = "${String.format("%,.0f", uiState.totalOutstandingBalances)} ETB",
                subtitle = "Collect before crew leaves",
                icon = Icons.Default.AttachMoney,
                accentColor = if (uiState.totalOutstandingBalances > 0) RubyAlert else EmeraldSuccess,
                modifier = Modifier.weight(1f),
                testTag = "stat_outstanding"
            )
            Spacer(modifier = Modifier.width(10.dp))
            StatCard(
                title = "Active Jobs",
                value = "${uiState.activeJobsCount} in pipeline",
                subtitle = "${uiState.totalProspectsCount} mapped prospects",
                icon = Icons.Default.Build,
                accentColor = ElectricCyanDark,
                modifier = Modifier.weight(1f),
                testTag = "stat_active_jobs"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Readiness Score Bar (Section 32 Audit)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mersa Plan Readiness Score",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.verifiedReadinessCount} of ${uiState.totalReadinessCount} verified (Target: ≥80%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (uiState.readinessPercentage >= 80) EmeraldSuccess.copy(alpha = 0.2f) else AmberGlowPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${uiState.readinessPercentage}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = if (uiState.readinessPercentage >= 80) EmeraldSuccess else AmberGlowPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (uiState.readinessPercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (uiState.readinessPercentage >= 80) EmeraldSuccess else AmberGlowPrimary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onNavigateToOperations,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("View 20-Point Checklist", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Launch Buttons
        SectionHeader(title = "Immediate Corridor Actions")
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onNavigateToCalculator,
                modifier = Modifier
                    .weight(1f)
                    .testTag("action_instant_quote"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberGlowPrimary, contentColor = Color.Black)
            ) {
                Icon(imageVector = Icons.Default.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Quote", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            FilledTonalButton(
                onClick = onNavigateToProspects,
                modifier = Modifier
                    .weight(1f)
                    .testTag("action_street_survey"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Assignment, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Street Survey", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Active Projects Spotlight
        SectionHeader(
            title = "Active Jobs Tracker",
            subtitle = "Jobs currently in material acquisition, fabrication or burn-in"
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.recentProjects.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = AmberGlowPrimary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No active production jobs yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onNavigateToCalculator) {
                        Text("Calculate & Generate First Quote")
                    }
                }
            }
        } else {
            uiState.recentProjects.forEach { project ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onNavigateToProjects() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = project.businessName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${project.productCode} • ${project.tier} • ${project.widthCm.toInt()}x${project.heightCm.toInt()} cm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Price: ${String.format("%,.0f", project.sellingPrice)} ETB | GP: ${String.format("%.1f", project.grossProfitPct)}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AmberGlowPrimary
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
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // High Priority Prospects Spotlight
        SectionHeader(
            title = "Top Grade-A Prospects",
            subtitle = "Immediate conversion targets based on urgency and influence"
        )
        Spacer(modifier = Modifier.height(10.dp))

        uiState.topProspects.forEach { prospect ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onNavigateToProspects() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PriorityGradeBadge(grade = prospect.priorityGrade, score = prospect.totalScore)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = prospect.businessName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${prospect.locationLandmark} • Current: ${prospect.currentSignage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Est. Value: ${String.format("%,.0f", prospect.estimatedValueEtb)} ETB (${prospect.tierGuess})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(
                        label = prospect.status,
                        color = when (prospect.status) {
                            "Quoted" -> WarningOrange
                            "Surveyed" -> ElectricCyanDark
                            "Won" -> EmeraldSuccess
                            else -> AmberGlowPrimary
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
