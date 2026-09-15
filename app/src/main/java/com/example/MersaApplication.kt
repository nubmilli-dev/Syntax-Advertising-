package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.SignageRepository

class MersaApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy {
        SignageRepository(
            prospectDao = database.prospectDao(),
            projectDao = database.projectDao(),
            customerDao = database.customerDao(),
            supplierDao = database.supplierDao(),
            readinessDao = database.readinessDao()
        )
    }
}
