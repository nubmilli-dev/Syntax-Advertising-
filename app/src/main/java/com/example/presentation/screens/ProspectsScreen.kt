package com.example.presentation.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import com.example.data.entity.ProspectEntity
import com.example.domain.engine.CsvImportResult
import com.example.domain.engine.DataExchangeEngine
import com.example.domain.engine.OperationsEngines
import com.example.presentation.components.PriorityGradeBadge
import com.example.presentation.components.StatusBadge
import com.example.presentation.viewmodel.ProspectsViewModel
import com.example.ui.theme.AmberGlowPrimary
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RubyAlert
import com.example.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProspectsScreen(
    viewModel: ProspectsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prospects by viewModel.allProspects.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGradeFilter by remember { mutableStateOf("ALL") } // "ALL", "A", "B", "C"
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    var editingProspect by remember { mutableStateOf<ProspectEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val filteredList = prospects.filter { p ->
        val matchesSearch = p.businessName.contains(searchQuery, ignoreCase = true) ||
                p.locationLandmark.contains(searchQuery, ignoreCase = true) ||
                p.code.contains(searchQuery, ignoreCase = true) ||
                p.category.contains(searchQuery, ignoreCase = true)
        val matchesGrade = if (selectedGradeFilter == "ALL") true else p.priorityGrade == selectedGradeFilter
        val matchesStatus = if (selectedStatusFilter == "ALL") true else p.status == selectedStatusFilter
        matchesSearch && matchesGrade && matchesStatus
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Box & Action Buttons Row
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search shop, landmark, segment (e.g. S2-07)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_prospects"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Import & Export Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showExportDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_export_leads"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Leads", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = { showImportDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_import_leads"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import CSV", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grade Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "A", "B", "C").forEach { g ->
                    FilterChip(
                        selected = selectedGradeFilter == g,
                        onClick = { selectedGradeFilter = g },
                        label = { Text(if (g == "ALL") "All Grades" else "Grade $g") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberGlowPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }

                listOf("ALL", "Identified", "Visited", "Surveyed", "Quoted", "Won").forEach { s ->
                    if (s != "ALL") {
                        FilterChip(
                            selected = selectedStatusFilter == s,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == s) "ALL" else s
                            },
                            label = { Text(s) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${filteredList.size} corridor prospects listed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("prospects_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { prospect ->
                    ProspectCard(
                        prospect = prospect,
                        onEdit = { editingProspect = prospect },
                        onCall = {
                            if (prospect.ownerPhone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${prospect.ownerPhone}"))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "No phone recorded for this prospect", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onWhatsApp = {
                            if (prospect.ownerPhone.isNotBlank()) {
                                val message = "ጤና ይስጥልኝ ${prospect.ownerName}፤ ለመርሳ ከተማ ኮሪደር ደረጃውን የጠበቀ የላይት ቦክስ ማስታወቂያ (Lightbox Signage) ዋጋ ማቅረቢያ ልከንሎት ነበር። ተጨማሪ መረጃ ይፈልጋሉ?"
                                val uri = Uri.parse("https://api.whatsapp.com/send?phone=251${prospect.ownerPhone.removePrefix("0")}&text=${Uri.encode(message)}")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp not installed. Sending SMS...", Toast.LENGTH_SHORT).show()
                                    val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${prospect.ownerPhone}")).apply {
                                        putExtra("sms_body", message)
                                    }
                                    context.startActivity(smsIntent)
                                }
                            } else {
                                Toast.makeText(context, "No phone recorded", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = {
                            viewModel.deleteProspect(prospect)
                            Toast.makeText(context, "Prospect removed", Toast.LENGTH_SHORT).show()
                        },
                        onStatusChange = { newStatus ->
                            viewModel.updateStatus(prospect, newStatus)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Floating Action Button to Add New Prospect
        FloatingActionButton(
            onClick = { isAddingNew = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_prospect"),
            containerColor = AmberGlowPrimary,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Prospect")
        }
    }

    // Add or Edit Dialog
    if (isAddingNew || editingProspect != null) {
        val target = editingProspect ?: ProspectEntity(
            code = "S2-${(prospects.size + 1).toString().padStart(2, '0')}",
            segment = "S2",
            businessName = "",
            locationLandmark = "",
            category = "Retail"
        )

        ProspectEditDialog(
            prospect = target,
            onDismiss = {
                isAddingNew = false
                editingProspect = null
            },
            onSave = { updated ->
                viewModel.saveProspect(updated)
                isAddingNew = false
                editingProspect = null
                Toast.makeText(context, "Prospect saved!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportProspectsDialog(
            prospects = filteredList.ifEmpty { prospects },
            onDismiss = { showExportDialog = false },
            onShare = { text, title ->
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(sendIntent, title))
                showExportDialog = false
            }
        )
    }

    // Import Dialog (Step 12: Select -> Parse -> Validate -> Detect errors -> Preview -> Import -> Verify)
    if (showImportDialog) {
        ImportProspectsDialog(
            onDismiss = { showImportDialog = false },
            onImport = { items ->
                viewModel.bulkImportProspects(items) { count ->
                    Toast.makeText(context, "Successfully imported $count corridor prospects!", Toast.LENGTH_LONG).show()
                    showImportDialog = false
                }
            }
        )
    }
}

@Composable
fun ProspectCard(
    prospect: ProspectEntity,
    onEdit: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
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
                    PriorityGradeBadge(grade = prospect.priorityGrade, score = prospect.totalScore)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = prospect.code,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                StatusBadge(
                    label = prospect.status,
                    color = when (prospect.status) {
                        "Won" -> EmeraldSuccess
                        "Quoted" -> WarningOrange
                        "Surveyed" -> ElectricCyanDark
                        else -> AmberGlowPrimary
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = prospect.businessName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${prospect.locationLandmark} • Category: ${prospect.category}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Current: ${prospect.currentSignage} (${prospect.condition})",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarningOrange,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Est. Value: ${String.format("%,.0f", prospect.estimatedValueEtb)} ETB",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AmberGlowPrimary
                )
            }

            if (prospect.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${prospect.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (prospect.ownerPhone.isNotBlank()) {
                        IconButton(
                            onClick = onCall,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = EmeraldSuccess)
                        }
                        IconButton(
                            onClick = onWhatsApp,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp/SMS", tint = ElectricCyanDark)
                        }
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AmberGlowPrimary)
                    }
                }

                // Quick Status progression chip
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (prospect.status != "Surveyed" && prospect.status != "Quoted" && prospect.status != "Won") {
                        OutlinedButton(
                            onClick = { onStatusChange("Surveyed") },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Survey", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (prospect.status == "Surveyed") {
                        Button(
                            onClick = { onStatusChange("Quoted") },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGlowPrimary, contentColor = Color.Black)
                        ) {
                            Text("Quoted", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (prospect.status == "Quoted") {
                        Button(
                            onClick = { onStatusChange("Won") },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            Text("Won", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProspectEditDialog(
    prospect: ProspectEntity,
    onDismiss: () -> Unit,
    onSave: (ProspectEntity) -> Unit
) {
    var code by remember { mutableStateOf(prospect.code) }
    var segment by remember { mutableStateOf(prospect.segment) }
    var name by remember { mutableStateOf(prospect.businessName) }
    var landmark by remember { mutableStateOf(prospect.locationLandmark) }
    var category by remember { mutableStateOf(prospect.category) }
    var currentSign by remember { mutableStateOf(prospect.currentSignage) }
    var ownerName by remember { mutableStateOf(prospect.ownerName) }
    var ownerPhone by remember { mutableStateOf(prospect.ownerPhone) }

    var visibility by remember { mutableStateOf(prospect.visibilityScore) }
    var ability by remember { mutableStateOf(prospect.abilityToPayScore) }
    var urgency by remember { mutableStateOf(prospect.urgencyScore) }
    var influence by remember { mutableStateOf(prospect.influenceScore) }

    var tierGuess by remember { mutableStateOf(prospect.tierGuess) }
    var estValue by remember { mutableStateOf(prospect.estimatedValueEtb.toString()) }
    var notes by remember { mutableStateOf(prospect.notes) }

    val (currentTotal, currentGrade) = OperationsEngines.computePriorityScore(visibility, ability, urgency, influence)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (prospect.id == 0L) "New Corridor Prospect" else "Edit Prospect", fontWeight = FontWeight.Bold) },
        text = {
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .verticalScroll(scroll)
                    .fillMaxWidth()
            ) {
                // Priority Score Preview Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("PRIORITY CALCULATION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("Total: $currentTotal / 12 points", style = MaterialTheme.typography.bodySmall)
                        }
                        PriorityGradeBadge(grade = currentGrade, score = currentTotal)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4 Scoring Sliders
                Text("Visibility / Foot Traffic: $visibility/3", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Slider(
                    value = visibility.toFloat(),
                    onValueChange = { visibility = it.toInt() },
                    valueRange = 1f..3f,
                    steps = 1
                )

                Text("Ability to Pay: $ability/3", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Slider(
                    value = ability.toFloat(),
                    onValueChange = { ability = it.toInt() },
                    valueRange = 1f..3f,
                    steps = 1
                )

                Text("Urgency (Notice/Dead Sign): $urgency/3", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Slider(
                    value = urgency.toFloat(),
                    onValueChange = { urgency = it.toInt() },
                    valueRange = 1f..3f,
                    steps = 1
                )

                Text("Influence on Neighbors: $influence/3", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Slider(
                    value = influence.toFloat(),
                    onValueChange = { influence = it.toInt() },
                    valueRange = 1f..3f,
                    steps = 1
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code (e.g. S2-07)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Business Name (English & Amharic)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Street Location & Landmark") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner / Manager Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = ownerPhone,
                    onValueChange = { ownerPhone = it },
                    label = { Text("Phone (e.g. 0911234567)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = currentSign,
                    onValueChange = { currentSign = it },
                    label = { Text("Current Signage (e.g. Faded Banner)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Field Notes & Objections") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = prospect.copy(
                        code = code,
                        segment = segment,
                        businessName = name,
                        locationLandmark = landmark,
                        currentSignage = currentSign,
                        ownerName = ownerName,
                        ownerPhone = ownerPhone,
                        visibilityScore = visibility,
                        abilityToPayScore = ability,
                        urgencyScore = urgency,
                        influenceScore = influence,
                        totalScore = currentTotal,
                        priorityGrade = currentGrade,
                        estimatedValueEtb = estValue.toDoubleOrNull() ?: prospect.estimatedValueEtb,
                        tierGuess = tierGuess,
                        notes = notes
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGlowPrimary, contentColor = Color.Black)
            ) {
                Text("Save Prospect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExportProspectsDialog(
    prospects: List<ProspectEntity>,
    onDismiss: () -> Unit,
    onShare: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Export Corridor Pipeline", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Select export format for ${prospects.size} selected prospects:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val csv = DataExchangeEngine.exportProspectsToCsv(prospects)
                            onShare(csv, "Mersa_Corridor_Prospects.csv")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = AmberGlowPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Standard CSV Spreadsheet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("RFC 4180 format for Excel, Google Sheets, or CRM import", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val report = DataExchangeEngine.generateProspectsReportText(prospects)
                            onShare(report, "Mersa_Corridor_Pipeline_Executive_Summary.txt")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = ElectricCyanDark)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Executive Pipeline Audit Report", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Formatted corridor summary with scores, priorities, and pipeline totals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ImportProspectsDialog(
    onDismiss: () -> Unit,
    onImport: (List<ProspectEntity>) -> Unit
) {
    var rawCsvText by remember { mutableStateOf("") }
    var parseResult by remember { mutableStateOf<CsvImportResult<ProspectEntity>?>(null) }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Import Corridor Data (Step 12)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Paste comma-separated survey data (or sample text) below:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Expected: BusinessName, Landmark, Category, Phone, Width, Height, EstValue",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = AmberGlowPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rawCsvText,
                    onValueChange = {
                        rawCsvText = it
                        parseResult = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("input_import_csv"),
                    placeholder = {
                        Text(
                            "Selam Pharmacy, Mexico Square, Pharmacy, +251911223344, 240, 80, 44000\n" +
                            "Awash Supermarket, Stadium, Retail, +251922334455, 300, 100, 68000"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = {
                        rawCsvText = "Ethio Telecom Agency, Bole Medhanialem, Retail, +251911001122, 280, 90, 56000\n" +
                                "Ras Amba Clinic, Piazza, Clinic, +251911445566, 200, 70, 36000\n" +
                                "Bole Cafe & Bakery, Bole Road, Café, +251911778899, 240, 80, 48000"
                        parseResult = null
                    }) {
                        Text("Paste Sample Rows", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = {
                            parseResult = DataExchangeEngine.parseAndValidateProspectsCsv(rawCsvText)
                        },
                        enabled = rawCsvText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyanDark, contentColor = Color.White)
                    ) {
                        Text("Validate & Preview")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                parseResult?.let { result ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Validation Results (${result.validItems.size} valid / ${result.errors.size} errors)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (result.errors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RubyAlert.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Validation Warnings / Errors:", fontWeight = FontWeight.Bold, color = RubyAlert, fontSize = 12.sp)
                                result.errors.forEach { err ->
                                    Text("• Row ${err.rowNumber}: ${err.message}", fontSize = 11.sp, color = RubyAlert)
                                }
                            }
                        }
                    }

                    if (result.validItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Ready to Import (${result.validItems.size} records):", fontWeight = FontWeight.Bold, color = EmeraldSuccess, fontSize = 12.sp)
                                result.validItems.take(5).forEach { p ->
                                    Text("✓ ${p.businessName} (${p.category}) - ${p.locationLandmark}", fontSize = 11.sp)
                                }
                                if (result.validItems.size > 5) {
                                    Text("...and ${result.validItems.size - 5} more records", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val validItems = parseResult?.validItems
            Button(
                onClick = {
                    if (validItems != null && validItems.isNotEmpty()) {
                        onImport(validItems)
                    }
                },
                enabled = validItems != null && validItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = AmberGlowPrimary, contentColor = Color.Black),
                modifier = Modifier.testTag("btn_confirm_import")
            ) {
                Text("Confirm Import (${validItems?.size ?: 0})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

