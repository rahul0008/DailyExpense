package com.example.dailyexpense.diModules // Or your preferred package

import android.content.Context
import com.example.dailyexpense.db.dao.ExpenseDao
import com.example.dailyexpense.db.database.ExpenseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): ExpenseDatabase {
       return ExpenseDatabase.getDatabase(appContext)
    }

    @Provides
    @Singleton // Or @ViewModelScoped, @ActivityScoped depending on your DAO's lifecycle needs
    fun provideExpenseDao(appDatabase: ExpenseDatabase): ExpenseDao {
        return appDatabase.expenseDao()
    }
}
