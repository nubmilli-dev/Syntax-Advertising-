package com.example.data.repository

import com.example.data.dao.CustomerDao
import com.example.data.dao.ProjectDao
import com.example.data.dao.ProspectDao
import com.example.data.dao.ReadinessDao
import com.example.data.dao.SupplierDao
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProspectEntity
import com.example.data.entity.ReadinessEntity
import com.example.data.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

class SignageRepository(
    private val prospectDao: ProspectDao,
    private val projectDao: ProjectDao,
    private val customerDao: CustomerDao,
    private val supplierDao: SupplierDao,
    private val readinessDao: ReadinessDao
) {
    // Prospects
    val allProspects: Flow<List<ProspectEntity>> = prospectDao.getAllProspects()
    fun getProspectsByGrade(grade: String): Flow<List<ProspectEntity>> = prospectDao.getProspectsByGrade(grade)
    fun getProspectsByStatus(status: String): Flow<List<ProspectEntity>> = prospectDao.getProspectsByStatus(status)
    suspend fun getProspectById(id: Long): ProspectEntity? = prospectDao.getProspectById(id)
    suspend fun insertProspect(prospect: ProspectEntity): Long = prospectDao.insertProspect(prospect)
    suspend fun insertProspects(prospects: List<ProspectEntity>) = prospectDao.insertAll(prospects)
    suspend fun updateProspect(prospect: ProspectEntity) = prospectDao.updateProspect(prospect)
    suspend fun deleteProspect(prospect: ProspectEntity) = prospectDao.deleteProspect(prospect)
    suspend fun deleteProspectById(id: Long) = prospectDao.deleteById(id)

    // Projects
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val activeProjects: Flow<List<ProjectEntity>> = projectDao.getActiveProjects()
    suspend fun getProjectById(id: Long): ProjectEntity? = projectDao.getProjectById(id)
    suspend fun insertProject(project: ProjectEntity): Long = projectDao.insertProject(project)
    suspend fun updateProject(project: ProjectEntity) = projectDao.updateProject(project)
    suspend fun deleteProject(project: ProjectEntity) = projectDao.deleteProject(project)

    // Customers & Care Plans
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val activeCarePlanCustomers: Flow<List<CustomerEntity>> = customerDao.getActiveCarePlanCustomers()
    suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)
    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    // Suppliers & Partners
    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
    fun getSuppliersByCategory(category: String): Flow<List<SupplierEntity>> = supplierDao.getSuppliersByCategory(category)
    suspend fun insertSupplier(supplier: SupplierEntity): Long = supplierDao.insertSupplier(supplier)
    suspend fun updateSupplier(supplier: SupplierEntity) = supplierDao.updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: SupplierEntity) = supplierDao.deleteSupplier(supplier)

    // Readiness
    val allReadinessItems: Flow<List<ReadinessEntity>> = readinessDao.getAllItems()
    val verifiedCount: Flow<Int> = readinessDao.getVerifiedCount()
    val totalCount: Flow<Int> = readinessDao.getTotalCount()
    suspend fun updateReadinessItem(item: ReadinessEntity) = readinessDao.updateItem(item)
}
