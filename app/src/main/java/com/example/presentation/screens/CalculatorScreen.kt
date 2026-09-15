package com.example.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.domain.engine.CostingEngine
import com.example.domain.model.AddOnSelection
import com.example.domain.model.BandStatus
import com.example.domain.model.ProductType
import com.example.domain.model.SignTier
import com.example.presentation.components.SectionHeader
import com.example.presentation.components.StatusBadge
import com.example.presentation.viewmodel.CalculatorViewModel
import com.example.ui.theme.AmberGlowDark
import com.example.ui.theme.AmberGlowPrimary
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RubyAlert
import com.example.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    onNavigateToProjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Calculator, 1: Standard Price List

    val product by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val tier by viewModel.selectedTier.collectAsStateWithLifecycle()
    val width by viewModel.widthCm.collectAsStateWithLifecycle()
    val height by viewModel.heightCm.collectAsStateWithLifecycle()
    val addOns by viewModel.addOns.collectAsStateWithLifecycle()
    val isCustom by viewModel.isCustomSize.collectAsStateWithLifecycle()
    val quoteResult by viewModel.quoteResult.collectAsStateWithLifecycle()

    val clientName by viewModel.clientName.collectAsStateWithLifecycle()
    val businessName by viewModel.businessName.collectAsStateWithLifecycle()

    var showBomDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var quoteLanguage by remember { mutableStateOf("English") } // "English" or "Amharic"
    var showSaveProjectDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Live Quote Generator", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Standard Price List (S1-S5)", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // Live Interactive Calculator
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Tier Selector
                SectionHeader(title = "Signage Quality Tier")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SignTier.values().forEach { t ->
                        FilterChip(
                            selected = tier == t,
                            onClick = {
                                viewModel.selectedTier.value = t
                            },
                            label = {
                                Text("${t.displayName} (${(t.targetMargin * 100).toInt()}% GP)")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberGlowPrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Product Type
                SectionHeader(title = "Product Category")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProductType.values().forEach { p ->
                        FilterChip(
                            selected = product == p,
                            onClick = {
                                viewModel.selectedProduct.value = p
                                viewModel.selectedTier.value = p.defaultTier
                            },
                            label = { Text(p.code) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyanDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Text(
                    text = "${product.title} — ${product.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Standard Sizes Shortcut Chips
                SectionHeader(title = "Standard Corridor Dimensions")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CostingEngine.STANDARD_SIZES.forEach { std ->
                        val isSelected = !isCustom && width == std.widthCm && height == std.heightCm
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setStandardSize(std.widthCm, std.heightCm, custom = false)
                            },
                            label = { Text("${std.code} (${std.widthCm.toInt()}x${std.heightCm.toInt()})") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Dimension Controls
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
                            Text(
                                text = "Dimensions: ${width.toInt()} x ${height.toInt()} cm",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${String.format("%.2f", quoteResult.faceAreaM2)} m² face",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Width: ${width.toInt()} cm", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = width.toFloat(),
                            onValueChange = {
                                viewModel.setStandardSize(it.toDouble(), height, custom = true)
                            },
                            valueRange = 60f..400f,
                            steps = 33
                        )

                        Text("Height: ${height.toInt()} cm", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = height.toFloat(),
                            onValueChange = {
                                viewModel.setStandardSize(width, it.toDouble(), custom = true)
                            },
                            valueRange = 40f..200f,
                            steps = 15
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Engineering & Add-ons
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Add-ons & Installation Conditions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Automatic Dusk Photocell Sensor", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = addOns.includePhotocell,
                                onCheckedChange = {
                                    viewModel.addOns.value = addOns.copy(includePhotocell = it)
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Programmable 24h Digital Timer", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = addOns.includeTimer,
                                onCheckedChange = {
                                    viewModel.addOns.value = addOns.copy(includeTimer = it)
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Voltage Surge / Fluctuation Protector", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = addOns.includeSurgeProtector,
                                onCheckedChange = {
                                    viewModel.addOns.value = addOns.copy(includeSurgeProtector = it)
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mounting Height Above 4m (Scaffolding)", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = addOns.isHeightAbove4m,
                                onCheckedChange = {
                                    viewModel.addOns.value = addOns.copy(isHeightAbove4m = it)
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Removal & Disposal of Old Sign", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = addOns.includeOldSignRemoval,
                                onCheckedChange = {
                                    viewModel.addOns.value = addOns.copy(includeOldSignRemoval = it)
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rush Fabrication Order (+20%)", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = addOns.isRushOrder,
                                onCheckedChange = {
                                    viewModel.addOns.value = addOns.copy(isRushOrder = it)
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Block Deal / Neighbor Shop Discount (-10%)", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = addOns.blockDiscountApplied,
                                onCheckedChange = {
                                    viewModel.addOns.value = addOns.copy(blockDiscountApplied = it)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // QUOTATION OUTPUT CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quote_output_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(2.dp, AmberGlowPrimary)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL PROFORMA INVESTMENT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = AmberGlowPrimary
                                )
                                Text(
                                    text = "${String.format("%,.0f", quoteResult.finalSellingPrice)} ETB",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            StatusBadge(
                                label = quoteResult.bandStatus.label,
                                color = when (quoteResult.bandStatus) {
                                    BandStatus.OK -> EmeraldSuccess
                                    BandStatus.BELOW -> WarningOrange
                                    BandStatus.ABOVE -> ElectricCyanDark
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Cash Discipline Payment Terms
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Deposit (${(tier.depositPct * 100).toInt()}%):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format("%,.0f", quoteResult.depositRequired)} ETB",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                                Text("Required to buy materials", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Balance on Install:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format("%,.0f", quoteResult.balanceOnInstall)} ETB",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text("Payable upon night illumination", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Engineering & Safety Metrics
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Illumination Modules:", style = MaterialTheme.typography.bodySmall)
                                    Text("${quoteResult.ledModuleCount} high-lumen LEDs", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Power Supply (80% Safety Cap):", style = MaterialTheme.typography.bodySmall)
                                    Text("${String.format("%.0f", quoteResult.psuWattsRequired)}W draw → ${String.format("%.0f", quoteResult.psuWattsSafetyCap)}W rated PSU", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Local Written Warranty:", style = MaterialTheme.typography.bodySmall)
                                    Text("${tier.warrantyMonths} Months Guarantee", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Effective Rate / m²:", style = MaterialTheme.typography.bodySmall)
                                    Text("${String.format("%,.0f", quoteResult.effectiveRatePerM2)} ETB/m²", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: BOM breakdown, Share Quote, Convert to Project
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showBomDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("View Full BOM")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { showShareDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberGlowPrimary, contentColor = Color.Black)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export Quote", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showSaveProjectDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_convert_project"),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyanDark, contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Default.PostAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Convert to Active Production Job", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        } else {
            // Standard Price List Tab
            StandardPriceListScreen(onSelectSize = { std, t ->
                viewModel.selectedTier.value = t
                viewModel.setStandardSize(std.widthCm, std.heightCm, custom = false)
                selectedTab = 0
            })
        }
    }

    // Full BOM Dialog
    if (showBomDialog) {
        AlertDialog(
            onDismissRequest = { showBomDialog = false },
            title = {
                Text("Bill of Materials (BOM) & Direct Costing", fontWeight = FontWeight.Bold)
            },
            text = {
                val bomScrollState = rememberScrollState()
                Column(modifier = Modifier.verticalScroll(bomScrollState)) {
                    Text(
                        text = "Direct Cost: ${String.format("%,.0f", quoteResult.directCost)} ETB | Loaded Cost: ${String.format("%,.0f", quoteResult.loadedCost)} ETB",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberGlowPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    quoteResult.bomItems.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text(item.quantitySpec, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    "${String.format("%,.0f", item.subtotal)} ETB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBomDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Share / Proforma Text Dialog (English & Amharic)
    if (showShareDialog) {
        val clientLabel = if (clientName.isNotBlank()) clientName else "Valued Client"
        val bizLabel = if (businessName.isNotBlank()) businessName else "Corridor Store"
        val quoteText = if (quoteLanguage == "English") {
            CostingEngine.generateEnglishQuoteText(quoteResult, clientLabel, bizLabel)
        } else {
            CostingEngine.generateAmharicQuoteText(quoteResult, clientLabel, bizLabel)
        }

        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Formal Proforma Text", fontWeight = FontWeight.Bold)
                    Row {
                        FilterChip(
                            selected = quoteLanguage == "English",
                            onClick = { quoteLanguage = "English" },
                            label = { Text("EN") }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = quoteLanguage == "Amharic",
                            onClick = { quoteLanguage = "Amharic" },
                            label = { Text("አማ") }
                        )
                    }
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { viewModel.clientName.value = it },
                        label = { Text("Client Contact Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { viewModel.businessName.value = it },
                        label = { Text("Business / Store Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        val quoteScroll = rememberScrollState()
                        Text(
                            text = quoteText,
                            modifier = Modifier
                                .padding(10.dp)
                                .verticalScroll(quoteScroll),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Signage Quotation", quoteText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Quotation copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Text")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, quoteText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Proforma via"))
                    }
                ) {
                    Text("Share Directly")
                }
            }
        )
    }

    // Convert to Project Dialog
    if (showSaveProjectDialog) {
        AlertDialog(
            onDismissRequest = { showSaveProjectDialog = false },
            title = { Text("Create Production Job", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Start tracking this job in the active P&L production workflow:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { viewModel.businessName.value = it },
                        label = { Text("Business / Frontage Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { viewModel.clientName.value = it },
                        label = { Text("Contact Person") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Total Price: ${String.format("%,.0f", quoteResult.finalSellingPrice)} ETB (Deposit: ${String.format("%,.0f", quoteResult.depositRequired)} ETB)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberGlowPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.convertQuoteToProject {
                            showSaveProjectDialog = false
                            Toast.makeText(context, "Job created successfully!", Toast.LENGTH_SHORT).show()
                            onNavigateToProjects()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyanDark)
                ) {
                    Text("Create Job")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StandardPriceListScreen(
    onSelectSize: (com.example.domain.model.StandardSize, SignTier) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Standard Sizes & Locked Unit Prices",
            subtitle = "Reference price schedule derived from exact sheet yield and direct costing"
        )
        Spacer(modifier = Modifier.height(12.dp))

        CostingEngine.STANDARD_SIZES.forEach { size ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = size.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${size.widthCm.toInt()} x ${size.heightCm.toInt()} cm",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = "Sheet yield: ${size.sheetFraction} • Use: ${size.typicalUse}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (size.economyPrice != null) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectSize(size, SignTier.ECONOMY) }
                            ) {
                                Text("Economy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format("%,.0f", size.economyPrice)} ETB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (size.standardPrice != null) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectSize(size, SignTier.STANDARD) }
                            ) {
                                Text("Standard", style = MaterialTheme.typography.labelSmall, color = AmberGlowPrimary, fontWeight = FontWeight.Bold)
                                Text("${String.format("%,.0f", size.standardPrice)} ETB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AmberGlowPrimary)
                            }
                        }
                        if (size.premiumPrice != null) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectSize(size, SignTier.PREMIUM) }
                            ) {
                                Text("Premium", style = MaterialTheme.typography.labelSmall, color = ElectricCyanDark)
                                Text("${String.format("%,.0f", size.premiumPrice)} ETB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
