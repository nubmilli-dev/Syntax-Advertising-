package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProspectEntity
import com.example.data.entity.ReadinessEntity
import com.example.data.entity.SupplierEntity
import com.example.data.repository.SignageRepository
import com.example.domain.engine.CostingEngine
import com.example.domain.engine.EquipmentRoiInput
import com.example.domain.engine.EquipmentRoiResult
import com.example.domain.engine.MarketScenario
import com.example.domain.engine.OperationsEngines
import com.example.domain.model.AddOnSelection
import com.example.domain.model.ProductType
import com.example.domain.model.QuoteResult
import com.example.domain.model.SignTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Dashboard State
data class DashboardUiState(
    val scenario: MarketScenario = MarketScenario.SCENARIO_A,
    val totalRevenue: Double = 0.0,
    val totalGrossProfit: Double = 0.0,
    val averageGrossMarginPct: Double = 0.0,
    val totalOutstandingBalances: Double = 0.0,
    val activeJobsCount: Int = 0,
    val completedInstallsCount: Int = 0,
    val totalProspectsCount: Int = 0,
    val highPriorityCount: Int = 0,
    val verifiedReadinessCount: Int = 0,
    val totalReadinessCount: Int = 20,
    val readinessPercentage: Int = 0,
    val recentProjects: List<ProjectEntity> = emptyList(),
    val topProspects: List<ProspectEntity> = emptyList()
)

class DashboardViewModel(private val repository: SignageRepository) : ViewModel() {

    private val _scenario = MutableStateFlow(MarketScenario.SCENARIO_A)
    val scenario = _scenario.asStateFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        _scenario,
        repository.allProjects,
        repository.allProspects,
        repository.allReadinessItems
    ) { currentScenario, projects, prospects, readiness ->
        val totalRev = projects.sumOf { it.sellingPrice }
        val totalGp = projects.sumOf { it.grossProfit }
        val avgMargin = if (totalRev > 0) (totalGp / totalRev) * 100.0 else 0.0
        val totalOutstanding = projects.filter { !it.balanceReceived }.sumOf { it.balanceDue }
        val activeCount = projects.count { it.status != "Installed & Completed" }
        val completedCount = projects.count { it.status == "Installed & Completed" }
        val gradeACount = prospects.count { it.priorityGrade == "A" }
        val verifiedCount = readiness.count { it.isVerified }
        val totalReadiness = if (readiness.isNotEmpty()) readiness.size else 20
        val readinessPct = if (totalReadiness > 0) ((verifiedCount.toDouble() / totalReadiness) * 100).toInt() else 0

        DashboardUiState(
            scenario = currentScenario,
            totalRevenue = totalRev,
            totalGrossProfit = totalGp,
            averageGrossMarginPct = avgMargin,
            totalOutstandingBalances = totalOutstanding,
            activeJobsCount = activeCount,
            completedInstallsCount = completedCount,
            totalProspectsCount = prospects.size,
            highPriorityCount = gradeACount,
            verifiedReadinessCount = verifiedCount,
            totalReadinessCount = totalReadiness,
            readinessPercentage = readinessPct,
            recentProjects = projects.take(3),
            topProspects = prospects.filter { it.priorityGrade == "A" }.take(3)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun toggleScenario() {
        _scenario.value = if (_scenario.value == MarketScenario.SCENARIO_A) {
            MarketScenario.SCENARIO_B
        } else {
            MarketScenario.SCENARIO_A
        }
    }
}

// Calculator ViewModel
class CalculatorViewModel(private val repository: SignageRepository) : ViewModel() {

    var selectedProduct = MutableStateFlow(ProductType.P2_STD_ACRYLIC)
    var selectedTier = MutableStateFlow(SignTier.STANDARD)
    var widthCm = MutableStateFlow(240.0)
    var heightCm = MutableStateFlow(80.0)
    var addOns = MutableStateFlow(AddOnSelection())
    var isCustomSize = MutableStateFlow(false)
    var clientName = MutableStateFlow("")
    var businessName = MutableStateFlow("")

    val quoteResult: StateFlow<QuoteResult> = combine(
        combine(selectedProduct, selectedTier, ::Pair),
        combine(widthCm, heightCm, ::Pair),
        combine(addOns, isCustomSize, ::Pair)
    ) { (product, tier), (w, h), (addons, custom) ->
        CostingEngine.calculateQuote(
            product = product,
            tier = tier,
            widthCm = w,
            heightCm = h,
            addOns = addons,
            isCustomSize = custom
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CostingEngine.calculateQuote(
            product = ProductType.P2_STD_ACRYLIC,
            tier = SignTier.STANDARD,
            widthCm = 240.0,
            heightCm = 80.0
        )
    )

    fun setStandardSize(w: Double, h: Double, custom: Boolean = false) {
        widthCm.value = w
        heightCm.value = h
        isCustomSize.value = custom
    }

    fun convertQuoteToProject(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val q = quoteResult.value
            val project = ProjectEntity(
                jobCode = "JOB-${System.currentTimeMillis().toString().takeLast(4)}",
                customerName = if (clientName.value.isNotBlank()) clientName.value else "Client",
                businessName = if (businessName.value.isNotBlank()) businessName.value else "Corridor Frontage",
                customerPhone = "",
                tier = q.tier.displayName,
                productCode = q.product.code,
                widthCm = q.widthCm,
                heightCm = q.heightCm,
                faceAreaM2 = q.faceAreaM2,
                sellingPrice = q.finalSellingPrice,
                materialsCost = q.materialsCost,
                outsourcingCost = if (q.tier == SignTier.PREMIUM) q.faceAreaM2 * 2200.0 else 0.0,
                labourCost = q.labourCost,
                transportCost = q.transportCost,
                installationCost = q.installationCost,
                overheadAllocated = q.overheadCost,
                contingencyUsed = 0.0,
                grossProfit = q.finalSellingPrice - q.directCost,
                grossProfitPct = if (q.finalSellingPrice > 0) ((q.finalSellingPrice - q.directCost) / q.finalSellingPrice) * 100 else 0.0,
                netContribution = q.finalSellingPrice - q.loadedCost,
                isBelowTargetMargin = false,
                targetMarginPct = q.tier.targetMargin * 100,
                depositAmount = q.depositRequired,
                depositDate = "",
                depositReceived = false,
                balanceDue = q.balanceOnInstall,
                balanceReceived = false,
                status = "Deposit Pending",
                warrantyMonths = q.tier.warrantyMonths
            )
            val newId = repository.insertProject(project)
            onSuccess(newId)
        }
    }
}

// Prospects ViewModel
class ProspectsViewModel(private val repository: SignageRepository) : ViewModel() {
    val allProspects: StateFlow<List<ProspectEntity>> = repository.allProspects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveProspect(prospect: ProspectEntity) {
        viewModelScope.launch {
            val (total, grade) = OperationsEngines.computePriorityScore(
                prospect.visibilityScore,
                prospect.abilityToPayScore,
                prospect.urgencyScore,
                prospect.influenceScore
            )
            val updated = prospect.copy(totalScore = total, priorityGrade = grade)
            if (updated.id == 0L) {
                repository.insertProspect(updated)
            } else {
                repository.updateProspect(updated)
            }
        }
    }

    fun updateStatus(prospect: ProspectEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateProspect(prospect.copy(status = newStatus))
        }
    }

    fun deleteProspect(prospect: ProspectEntity) {
        viewModelScope.launch {
            repository.deleteProspect(prospect)
        }
    }

    fun bulkImportProspects(prospects: List<ProspectEntity>, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            repository.insertProspects(prospects)
            onComplete(prospects.size)
        }
    }
}

// Projects ViewModel
class ProjectsViewModel(private val repository: SignageRepository) : ViewModel() {
    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveProject(project: ProjectEntity) {
        viewModelScope.launch {
            if (project.id == 0L) {
                repository.insertProject(project)
            } else {
                repository.updateProject(project)
            }
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    fun updateProjectStatus(project: ProjectEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateProject(project.copy(status = newStatus))
        }
    }

    fun markDepositReceived(project: ProjectEntity) {
        viewModelScope.launch {
            repository.updateProject(
                project.copy(
                    depositReceived = true,
                    depositDate = "2026-09-15",
                    status = "Material Purchased"
                )
            )
        }
    }

    fun markBalanceReceived(project: ProjectEntity) {
        viewModelScope.launch {
            repository.updateProject(
                project.copy(
                    balanceReceived = true,
                    balanceReceivedDate = "2026-09-15",
                    daysOutstanding = 0,
                    status = "Installed & Completed"
                )
            )
            // Also create or link into Customers table
            val customer = CustomerEntity(
                warrantyId = "WAR-MS-${project.id.toString().padStart(3, '0')}",
                businessName = project.businessName,
                ownerName = project.customerName,
                phone = project.customerPhone,
                location = "Mersa Corridor",
                signType = "${project.productCode} ${project.tier}",
                signDimensions = "${project.widthCm.toInt()} x ${project.heightCm.toInt()} cm",
                tier = project.tier,
                ledBrand = "Injection Modules 12V",
                ledCount = (project.faceAreaM2 * 50).toInt(),
                psuBrand = "Genuine Weatherproof PSU",
                psuRating = "${(project.faceAreaM2 * 60 / 0.8).toInt()}W",
                installDate = "2026-09-15",
                warrantyExpiry = "2027-09-15",
                carePlan = "None",
                totalPaid = project.sellingPrice,
                balanceOutstanding = 0.0
            )
            repository.insertCustomer(customer)
        }
    }
}

// Customers ViewModel
class CustomersViewModel(private val repository: SignageRepository) : ViewModel() {
    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateCarePlan(customer: CustomerEntity, newPlan: String, fee: Double) {
        viewModelScope.launch {
            repository.updateCustomer(
                customer.copy(
                    carePlan = newPlan,
                    carePlanAnnualFee = fee,
                    nextMaintenanceDate = "2027-03-15"
                )
            )
        }
    }

    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                repository.insertCustomer(customer)
            } else {
                repository.updateCustomer(customer)
            }
        }
    }
}

// Operations ViewModel
class OperationsViewModel(private val repository: SignageRepository) : ViewModel() {
    val allSuppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readinessItems: StateFlow<List<ReadinessEntity>> = repository.allReadinessItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Equipment ROI state
    val equipmentRoiInput = MutableStateFlow(
        EquipmentRoiInput(
            machineName = "CO2 Laser Cutter (1300x900)",
            landedPriceEtb = 320000.0,
            monthlyOutsourcingSavedEtb = 22000.0,
            monthlyCapturedJobMarginEtb = 12000.0,
            monthlyNewProductMarginEtb = 8000.0,
            monthlyOperatorWageEtb = 7000.0,
            monthlyPowerCostEtb = 2000.0,
            monthlyMaintenanceCostEtb = 2500.0,
            monthlyConsumablesEtb = 3500.0,
            hasTrainedOperator = true,
            hasAdequatePower = true,
            monthsWorkingCapitalOnHand = 4
        )
    )

    val equipmentRoiResult: StateFlow<EquipmentRoiResult> = equipmentRoiInput
        .map { OperationsEngines.calculateEquipmentRoi(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OperationsEngines.calculateEquipmentRoi(equipmentRoiInput.value)
        )

    fun updateRoiInput(transform: (EquipmentRoiInput) -> EquipmentRoiInput) {
        equipmentRoiInput.value = transform(equipmentRoiInput.value)
    }

    fun toggleReadinessItem(item: ReadinessEntity) {
        viewModelScope.launch {
            val newStatus = !item.isVerified
            val updated = item.copy(
                isVerified = newStatus,
                verifiedDate = if (newStatus) "2026-09-15" else "",
                verifiedByNote = if (newStatus) "Verified by user" else ""
            )
            repository.updateReadinessItem(updated)
        }
    }

    fun saveSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            val score = OperationsEngines.calculateSupplierScore(
                supplier.qualityScore,
                supplier.reliabilityScore,
                supplier.speedScore,
                supplier.priceScore,
                supplier.materialScore,
                supplier.capacityScore,
                supplier.termsScore,
                supplier.emergencyScore
            )
            val rating = OperationsEngines.classifySupplierRating(score)
            val updated = supplier.copy(weightedScore = score, ratingStatus = rating)
            if (updated.id == 0L) {
                repository.insertSupplier(updated)
            } else {
                repository.updateSupplier(updated)
            }
        }
    }

    fun selectPresetMachine(machineName: String) {
        when (machineName) {
            "CO2 Laser Cutter (1300x900)" -> {
                equipmentRoiInput.value = equipmentRoiInput.value.copy(
                    machineName = machineName,
                    landedPriceEtb = 320000.0,
                    monthlyOutsourcingSavedEtb = 22000.0,
                    monthlyCapturedJobMarginEtb = 12000.0,
                    monthlyNewProductMarginEtb = 8000.0
                )
            }
            "CNC Router (1325 Heavy Duty)" -> {
                equipmentRoiInput.value = equipmentRoiInput.value.copy(
                    machineName = machineName,
                    landedPriceEtb = 650000.0,
                    monthlyOutsourcingSavedEtb = 35000.0,
                    monthlyCapturedJobMarginEtb = 20000.0,
                    monthlyNewProductMarginEtb = 15000.0
                )
            }
            "Aluminium Profile Miter Saw" -> {
                equipmentRoiInput.value = equipmentRoiInput.value.copy(
                    machineName = machineName,
                    landedPriceEtb = 55000.0,
                    monthlyOutsourcingSavedEtb = 6000.0,
                    monthlyCapturedJobMarginEtb = 4000.0,
                    monthlyNewProductMarginEtb = 0.0
                )
            }
        }
    }
}

// ViewModel Factory
class ViewModelFactory(private val repository: SignageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(CalculatorViewModel::class.java) -> CalculatorViewModel(repository) as T
            modelClass.isAssignableFrom(ProspectsViewModel::class.java) -> ProspectsViewModel(repository) as T
            modelClass.isAssignableFrom(ProjectsViewModel::class.java) -> ProjectsViewModel(repository) as T
            modelClass.isAssignableFrom(CustomersViewModel::class.java) -> CustomersViewModel(repository) as T
            modelClass.isAssignableFrom(OperationsViewModel::class.java) -> OperationsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
