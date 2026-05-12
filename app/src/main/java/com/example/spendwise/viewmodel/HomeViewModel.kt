package com.example.spendwise.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwise.data.repository.TransactionRepository
import com.example.spendwise.data.toTransaction
import com.example.spendwise.data.toEntity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> =
        repository.getTransactions()
            .map { entities ->
                entities.map { it.toTransaction() }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val hiddenCategories = repository.getAllHiddenCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun hideCategoryFromSuggestions(category: String) {
        viewModelScope.launch {
            repository.hideCategory(category)
        }
    }

    init {
        refreshDataFromCloud()
    }
    fun refreshDataFromCloud() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    repository.fetchFromFirestore(currentUser.uid)
                    Log.d("HomeVM", "Syncing data for dashboard...")
                } catch (e: Exception) {
                    Log.e("HomeVM", "Cloud sync failed: ${e.message}")
                }
            }
        }
    }
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction.toEntity())

            repository.unhideCategory(transaction.title)
        }
    }

}

