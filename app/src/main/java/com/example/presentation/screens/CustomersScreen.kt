package com.example.presentation.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.CustomerEntity
import com.example.presentation.components.SectionHeader
import com.example.presentation.components.StatusBadge
import com.example.presentation.viewmodel.CustomersViewModel
import com.example.ui.theme.AmberGlowPrimary
import com.example.ui.theme.ElectricCyanDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RubyAlert
import com.example.ui.theme.WarningOrange

@Composable
fun CustomersScreen(
    viewModel: CustomersViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()

    var selectedCustomerForCarePlan by remember { mutableStateOf<CustomerEntity?>(null) }
    var selectedCustomerForRepairDetails by remember { mutableStateOf<CustomerEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionHeader(
            title = "Installed Clients & Care Plans",
            subtitle = "Warranty tracking, technical repair specs, and annual maintenance plans"
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (customers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No installed client records yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("customers_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(customers, key = { it.id }) { customer ->
                    CustomerCard(
                        customer = customer,
                        onCarePlanClick = { selectedCustomerForCarePlan = customer },
                        onRepairSpecClick = { selectedCustomerForRepairDetails = customer },
                        onCall = {
                            if (customer.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                context.startActivity(intent)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Care Plan Dialog
    if (selectedCustomerForCarePlan != null) {
        val cust = selectedCustomerForCarePlan!!
        var chosenPlan by remember { mutableStateOf(cust.carePlan) }
        val fee = when (chosenPlan) {
            "Care Plan Basic" -> 2800.0
            "Care Plan Plus" -> 4500.0
            else -> 0.0
        }

        AlertDialog(
            onDismissRequest = { selectedCustomerForCarePlan = null },
            title = { Text("Signage Care Plan Subscription", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Client: ${cust.businessName}")
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = chosenPlan == "None",
                            onClick = { chosenPlan = "None" }
                        )
                        Text("No Care Plan (Standard Warranty Only)")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = chosenPlan == "Care Plan Basic",
                            onClick = { chosenPlan = "Care Plan Basic" }
                        )
                        Column {
                            Text("Care Plan Basic (2,800 ETB / year)", fontWeight = FontWeight.Bold)
                            Text("2x bi-annual face cleanings + electrical checkup", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = chosenPlan == "Care Plan Plus",
                            onClick = { chosenPlan = "Care Plan Plus" }
                        )
                        Column {
                            Text("Care Plan Plus (4,500 ETB / year)", fontWeight = FontWeight.Bold)
                            Text("Quarterly cleaning + free replacement LED modules + 24h response", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCarePlan(cust, chosenPlan, fee)
                        selectedCustomerForCarePlan = null
                        Toast.makeText(context, "Care plan updated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGlowPrimary, contentColor = Color.Black)
                ) {
                    Text("Save Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCustomerForCarePlan = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Repair Specifications Dialog
    if (selectedCustomerForRepairDetails != null) {
        val c = selectedCustomerForRepairDetails!!
        AlertDialog(
            onDismissRequest = { selectedCustomerForRepairDetails = null },
            title = { Text("Technical Repair Specifications", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Quick-Dispatch Hardware Reference for Workshop Technicians:")
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Sign Type: ${c.signType}", fontWeight = FontWeight.Bold)
                            Text("Dimensions: ${c.signDimensions}", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("LED Modules: ${c.ledBrand} (${c.ledCount} pcs)", style = MaterialTheme.typography.bodySmall)
                            Text("Power Supply (PSU): ${c.psuBrand} (${c.psuRating})", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Installed Date: ${c.installDate}", style = MaterialTheme.typography.bodySmall)
                            Text("Warranty Valid Until: ${c.warrantyExpiry}", style = MaterialTheme.typography.bodySmall, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCustomerForRepairDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CustomerCard(
    customer: CustomerEntity,
    onCarePlanClick: () -> Unit,
    onRepairSpecClick: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldSuccess.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = customer.warrantyId,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }

                StatusBadge(
                    label = if (customer.carePlan != "None") customer.carePlan else "Standard Warranty",
                    color = if (customer.carePlan != "None") AmberGlowPrimary else ElectricCyanDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = customer.businessName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${customer.location} • ${customer.signType} (${customer.signDimensions})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Warranty and Maintenance Dates
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
                        Text("Warranty Expiry", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(customer.warrantyExpiry, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                    }
                    Column {
                        Text("Next Care Inspection", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (customer.nextMaintenanceDate.isNotBlank()) customer.nextMaintenanceDate else "None Scheduled",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text("Referrals Won", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${customer.referralsGivenCount} shops", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AmberGlowPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onRepairSpecClick,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Repair Specs", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = onCarePlanClick,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGlowPrimary, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Care Plan", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (customer.phone.isNotBlank()) {
                    IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = EmeraldSuccess)
                    }
                }
            }
        }
    }
}
