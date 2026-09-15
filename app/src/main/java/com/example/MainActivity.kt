package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.presentation.screens.CalculatorScreen
import com.example.presentation.screens.CustomersScreen
import com.example.presentation.screens.DashboardScreen
import com.example.presentation.screens.OperationsScreen
import com.example.presentation.screens.ProjectsScreen
import com.example.presentation.screens.ProspectsScreen
import com.example.presentation.viewmodel.CalculatorViewModel
import com.example.presentation.viewmodel.CustomersViewModel
import com.example.presentation.viewmodel.DashboardViewModel
import com.example.presentation.viewmodel.OperationsViewModel
import com.example.presentation.viewmodel.ProjectsViewModel
import com.example.presentation.viewmodel.ProspectsViewModel
import com.example.presentation.viewmodel.ViewModelFactory
import com.example.ui.theme.AmberGlowPrimary
import com.example.ui.theme.MersaSignageTheme

enum class Screen(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Control Center", Icons.Default.Dashboard, "nav_dashboard"),
    CALCULATOR("Quote Engine", Icons.Default.Calculate, "nav_calculator"),
    PROSPECTS("Corridor CRM", Icons.AutoMirrored.Filled.Assignment, "nav_prospects"),
    PROJECTS("Jobs & P&L", Icons.Default.Build, "nav_projects"),
    CUSTOMERS("Warranties", Icons.Default.Shield, "nav_customers"),
    OPERATIONS("Operations", Icons.Default.Handshake, "nav_operations")
}

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        (application as MersaApplication).repository
    }

    private val factory by lazy {
        ViewModelFactory(repository)
    }

    private val dashboardViewModel: DashboardViewModel by viewModels { factory }
    private val calculatorViewModel: CalculatorViewModel by viewModels { factory }
    private val prospectsViewModel: ProspectsViewModel by viewModels { factory }
    private val projectsViewModel: ProjectsViewModel by viewModels { factory }
    private val customersViewModel: CustomersViewModel by viewModels { factory }
    private val operationsViewModel: OperationsViewModel by viewModels { factory }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MersaSignageTheme {
                var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Mersa Signage • ${currentScreen.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            Screen.values().forEach { screen ->
                                NavigationBarItem(
                                    selected = currentScreen == screen,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.title.split(" ").first(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.testTag(screen.tag),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = AmberGlowPrimary,
                                        indicatorColor = AmberGlowPrimary
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    when (currentScreen) {
                        Screen.DASHBOARD -> DashboardScreen(
                            viewModel = dashboardViewModel,
                            onNavigateToCalculator = { currentScreen = Screen.CALCULATOR },
                            onNavigateToProspects = { currentScreen = Screen.PROSPECTS },
                            onNavigateToProjects = { currentScreen = Screen.PROJECTS },
                            onNavigateToOperations = { currentScreen = Screen.OPERATIONS },
                            modifier = Modifier.padding(innerPadding)
                        )
                        Screen.CALCULATOR -> CalculatorScreen(
                            viewModel = calculatorViewModel,
                            onNavigateToProjects = { currentScreen = Screen.PROJECTS },
                            modifier = Modifier.padding(innerPadding)
                        )
                        Screen.PROSPECTS -> ProspectsScreen(
                            viewModel = prospectsViewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        Screen.PROJECTS -> ProjectsScreen(
                            viewModel = projectsViewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        Screen.CUSTOMERS -> CustomersScreen(
                            viewModel = customersViewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        Screen.OPERATIONS -> OperationsScreen(
                            viewModel = operationsViewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
