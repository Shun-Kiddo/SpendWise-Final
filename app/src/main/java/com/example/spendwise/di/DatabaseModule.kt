package com.example.spendwise.di
import android.content.Context
import androidx.room.Room
import com.example.spendwise.data.dao.MonthlyBalanceDao
import com.example.spendwise.data.dao.MonthlySummaryDao
import com.example.spendwise.data.roomdb.AppDatabase
import com.example.spendwise.data.dao.TransactionDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "spendwise_db"
        ).build()
    }
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    @Provides
    @Singleton
    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    fun provideMonthlyBalanceDao(database: AppDatabase): MonthlyBalanceDao {
        return database.monthlyBalanceDao()
    }
    @Provides
    fun provideMonthlySummaryDao(database: AppDatabase): MonthlySummaryDao {
        return database.monthlySummaryDao()
    }
    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }
}