package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProspectEntity
import com.example.data.entity.ReadinessEntity
import com.example.data.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProspectDao {
    @Query("SELECT * FROM prospects ORDER BY totalScore DESC, id DESC")
    fun getAllProspects(): Flow<List<ProspectEntity>>

    @Query("SELECT * FROM prospects WHERE priorityGrade = :grade ORDER BY totalScore DESC")
    fun getProspectsByGrade(grade: String): Flow<List<ProspectEntity>>

    @Query("SELECT * FROM prospects WHERE status = :status ORDER BY totalScore DESC")
    fun getProspectsByStatus(status: String): Flow<List<ProspectEntity>>

    @Query("SELECT * FROM prospects WHERE id = :id")
    suspend fun getProspectById(id: Long): ProspectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProspect(prospect: ProspectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prospects: List<ProspectEntity>)

    @Update
    suspend fun updateProspect(prospect: ProspectEntity)

    @Delete
    suspend fun deleteProspect(prospect: ProspectEntity)

    @Query("DELETE FROM prospects WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE status != 'Installed & Completed' ORDER BY id DESC")
    fun getActiveProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY id DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE carePlan != 'None' ORDER BY nextMaintenanceDate ASC")
    fun getActiveCarePlanCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY weightedScore DESC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE category = :category ORDER BY weightedScore DESC")
    fun getSuppliersByCategory(category: String): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suppliers: List<SupplierEntity>)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)
}

@Dao
interface ReadinessDao {
    @Query("SELECT * FROM readiness_items ORDER BY itemNumber ASC")
    fun getAllItems(): Flow<List<ReadinessEntity>>

    @Query("SELECT COUNT(*) FROM readiness_items WHERE isVerified = 1")
    fun getVerifiedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM readiness_items")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ReadinessEntity>)

    @Update
    suspend fun updateItem(item: ReadinessEntity)
}
