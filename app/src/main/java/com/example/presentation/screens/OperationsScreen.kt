package com.example.presentation.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ReadinessEntity
import com.example.data.entity.SupplierEntity
import com.example.domain.engine.EquipmentRoiResult
import com.example.presentation.components.SectionHeader
import com.example.presentation.components.StatusBadge
import com.example.presentation.viewmodel.OperationsViewModel
import com.example.ui.theme.AmberGlowPrimary
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RubyAlert
import com.example.ui.theme.WarningOrange

@Composable
fun OperationsScreen(
    viewModel: OperationsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }

    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    val readinessItems by viewModel.readinessItems.collectAsStateWithLifecycle()
    val roiResult by viewModel.equipmentRoiResult.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            edgePadding = 16.dp
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Readiness (20-Pt)", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Equipment ROI", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Supplier Network", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 3,
                onClick = { selectedSubTab = 3 },
                text = { Text("Field Kit & Scripts", fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedSubTab) {
            0 -> ReadinessTab(
                items = readinessItems,
                onToggle = { viewModel.toggleReadinessItem(it) }
            )
            1 -> EquipmentRoiTab(
                viewModel = viewModel,
                result = roiResult
            )
            2 -> SupplierScorecardTab(
                suppliers = suppliers,
                onSaveSupplier = { viewModel.saveSupplier(it) }
            )
            3 -> FieldKitReferenceTab()
        }
    }
}

@Composable
fun ReadinessTab(
    items: List<ReadinessEntity>,
    onToggle: (ReadinessEntity) -> Unit
) {
    val verifiedCount = items.count { it.isVerified }
    val total = if (items.isNotEmpty()) items.size else 20
    val pct = if (total > 0) ((verifiedCount.toDouble() / total) * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("20-Point Operational Readiness Audit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("$verifiedCount of $total points verified (Target: ≥80%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (pct >= 80) EmeraldSuccess.copy(alpha = 0.2f) else AmberGlowPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "$pct%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = if (pct >= 80) EmeraldSuccess else AmberGlowPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (pct >= 80) EmeraldSuccess else AmberGlowPrimary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("readiness_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(item) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isVerified) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (item.isVerified) EmeraldSuccess.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isVerified,
                            onCheckedChange = { onToggle(item) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "#${item.itemNumber} • ${item.category}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (item.verifiedDate.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Verified on ${item.verifiedDate} (${item.verifiedByNote})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldSuccess
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun EquipmentRoiTab(
    viewModel: OperationsViewModel,
    result: EquipmentRoiResult
) {
    val scrollState = rememberScrollState()
    val roiInput by viewModel.equipmentRoiInput.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Equipment Acquisition ROI Engine",
            subtitle = "Strict financial test: Laser vs CNC vs Miter Saw purchase decision"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preset Machines Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "CO2 Laser Cutter (1300x900)",
                "CNC Router (1325 Heavy Duty)",
                "Aluminium Profile Miter Saw"
            ).forEach { machine ->
                FilterChip(
                    selected = roiInput.machineName == machine,
                    onClick = { viewModel.selectPresetMachine(machine) },
                    label = { Text(machine.take(15) + "...") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // RESULT VERDICT CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (result.canBuyNow) EmeraldSuccess.copy(alpha = 0.15f) else WarningOrange.copy(alpha = 0.15f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                if (result.canBuyNow) EmeraldSuccess else WarningOrange
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (result.canBuyNow) "INVESTMENT APPROVED" else "HOLD / KEEP OUTSOURCING",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (result.canBuyNow) EmeraldSuccess else WarningOrange
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Payback: ${String.format("%.1f", result.paybackMonths)} mos",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = result.finalRecommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                // 4 Decision Rules Checklist
                RuleRow("Monthly Benefit Ratio B ≥ 1.5× Ownership O", result.passesRatioRule, "Ratio: ${String.format("%.1f", result.ratioBOverO)}x (Net Benefit: ${String.format("%,.0f", result.monthlyBenefitB)} ETB vs O: ${String.format("%,.0f", result.monthlyOwnershipCostO)} ETB)")
                RuleRow("Payback Period ≤ 18 Months", result.passesPaybackRule, "${String.format("%.1f", result.paybackMonths)} months amortized")
                RuleRow("Working Capital Reserve ≥ 3 Months", result.passesCapitalRule, "${roiInput.monthsWorkingCapitalOnHand} months reserve currently on hand")
                RuleRow("Infrastructure (Trained Operator + Power)", result.passesInfrastructureRule, "Trained operator: ${roiInput.hasTrainedOperator} | Stable power: ${roiInput.hasAdequatePower}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Modifiers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Operating Benefit & Cost Parameters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Machine Landed Price: ${String.format("%,.0f", roiInput.landedPriceEtb)} ETB", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = roiInput.landedPriceEtb.toFloat(),
                    onValueChange = { p -> viewModel.updateRoiInput { it.copy(landedPriceEtb = p.toDouble()) } },
                    valueRange = 50000f..1000000f,
                    steps = 19
                )

                Text("Monthly Outsourcing Saved: ${String.format("%,.0f", roiInput.monthlyOutsourcingSavedEtb)} ETB", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = roiInput.monthlyOutsourcingSavedEtb.toFloat(),
                    onValueChange = { s -> viewModel.updateRoiInput { it.copy(monthlyOutsourcingSavedEtb = s.toDouble()) } },
                    valueRange = 5000f..80000f,
                    steps = 15
                )

                Text("Captured Job Margin: ${String.format("%,.0f", roiInput.monthlyCapturedJobMarginEtb)} ETB", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = roiInput.monthlyCapturedJobMarginEtb.toFloat(),
                    onValueChange = { m -> viewModel.updateRoiInput { it.copy(monthlyCapturedJobMarginEtb = m.toDouble()) } },
                    valueRange = 0f..50000f,
                    steps = 10
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Trained Operator Available")
                    Switch(
                        checked = roiInput.hasTrainedOperator,
                        onCheckedChange = { o -> viewModel.updateRoiInput { it.copy(hasTrainedOperator = o) } }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Adequate Industrial Power / Generator")
                    Switch(
                        checked = roiInput.hasAdequatePower,
                        onCheckedChange = { p -> viewModel.updateRoiInput { it.copy(hasAdequatePower = p) } }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RuleRow(ruleName: String, passed: Boolean, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (passed) EmeraldSuccess else RubyAlert,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(ruleName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(detail, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SupplierScorecardTab(
    suppliers: List<SupplierEntity>,
    onSaveSupplier: (SupplierEntity) -> Unit
) {
    val context = LocalContext.current
    var isAdding by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(
                title = "Supplier Scorecard & Partner Network",
                subtitle = "Weighted evaluation across 8 critical supply chain metrics",
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { isAdding = true },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGlowPrimary, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(suppliers, key = { it.id }) { supplier ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(supplier.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${supplier.category} • ${supplier.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (supplier.weightedScore >= 4.0) EmeraldSuccess.copy(alpha = 0.2f) else AmberGlowPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = String.format("%.1f ★", supplier.weightedScore),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (supplier.weightedScore >= 4.0) EmeraldSuccess else AmberGlowPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Status: ${supplier.ratingStatus}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        if (supplier.notes.isNotBlank()) {
                            Text("Agreement: ${supplier.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun FieldKitReferenceTab() {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Field Kit & Sales Pitch Reference",
            subtitle = "Corridor sales scripts in Amharic, objection handlers, and QC gates"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Script 1: Cold Approach
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("1. የኮሪደር ንግድ ባለቤቶች ቀጥታ አቀራረብ (Cold Approach)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AmberGlowPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = """
                        "ጤና ይስጥልኝ! መርሳ ከተማ ዋናውን የኮሪደር መንገድ እያስተካከለች እንደሆነ ያውቃሉ። የጨርቅ ባነር እና ያረጁ ማስታወቂያዎችን የማስነሳት ስራ ተጀምሯል።
                        
                        እኛ ደረጃውን የጠበቀ፣ በምሽት በደመቀ ሁኔታ የሚያበራ እና ከማዘጋጃ ቤቱ መስፈርት ጋር የሚስማማ የላይት ቦክስ (Lightbox Signage) ስራ እንሰራለን።
                        
                        ዋናው ጥቅሙ፦ ማታ ሱቅዎን ከሩቅ ያደምቃል፣ ደንበኛ ይስባል፣ እንዲሁም ለ1 አመት ሙሉ የአካባቢ ዋስትና ያለው ኦሪጅናል እቃ ነው። ሱቅዎን ለክተን ነፃ የዋጋ ማቅረቢያ እንስጥዎ?"
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Script 2: Objection - Price
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("2. 'ዋጋችሁ ተወደደ / ባነር ይበቃኛል' ለሚል ተቃውሞ", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AmberGlowPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = """
                        "ይገባኛል! ነገር ግን የጨርቅ ባነር በየ6 ወሩ በፀሀይ እና ዝናብ ይሰበራል ወይም ቀለም ያጣል፤ ማዘጋጃ ቤቱም ያስነሳዋል።
                        
                        የእኛ ላይት ቦክስ ቢያንስ ለ5 ዓመታት ያገለግላል። በቀን ሲያሰሉት ከ1 ሻይ ዋጋ ያነሰ ነው የሚወጣው። በተጨማሪም ፎቶሴል ስላለው ማታ ራሱ በርቶ ጠዋት ይጠፋል፤ የኤሌክትሪክ ፍጆታውም በወር ከ100-150 ብር አይበልጥም።
                        
                        ቅድመ ክፍያ 60% ብቻ ከፍለው፣ ቀሪውን 40% ተተክሎና በምሽት ሲበራ ያዩትና ይከፍላሉ።"
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4 QC Gates
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("3. አራቱ የምርት ጥራት ቁጥጥር ደረጃዎች (4 QC Gates)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ElectricCyanDark)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = """
                        • ደረጃ 1 (ዕቃ መቀበል)፦ የአክሪሊክ ውፍረት በካሊፐር መለካት፣ የLED ሞጁል በ12V ባትሪ መሞከር።
                        • ደረጃ 2 (ፍሬም እና ኤሌክትሪክ)፦ የታችኛው ማፍሰሻ ቀዳዳዎች (drain holes) መኖራቸውን ማረጋገጥ፣ ሽቦዎች በኬብል ታይ መታሰራቸው።
                        • ደረጃ 3 (የ12-24 ሰዓት የበርን-ኢን ፍተሻ)፦ ሰሌዳው ሳይገጠም በዎርክሾፕ ውስጥ ለ12 ሰዓት ያለማቋረጥ እንዲበራ አድርጎ ትራንስፎርመሩ እንዳይሞቅ ማረጋገጥ።
                        • ደረጃ 4 (ሳይት ላይ ተከላ)፦ የከፍታ ሚዛን (Laser level)፣ የዝናብ መከላከያ ሲሊኮን፣ እና ከዋናው መስመር ጋር በትክክል ማገናኘት።
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
