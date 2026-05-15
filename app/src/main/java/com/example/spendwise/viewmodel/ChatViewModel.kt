package com.example.spendwise.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwise.screen.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}
class ChatViewModel : ViewModel() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyAtZmcT2qOlFMoNCcirDWTtjDGU9BilijI"
    )
    val messages = mutableStateListOf(
        ChatMessage("Hello Gastador! I am Spendy, your SpendWise assistant. How can I help you today?", false)
    )

    var isAiTyping by mutableStateOf(false)
        private set

    fun sendMessage(userText: String, context: Context) {
        if (userText.isBlank()) return

        if (!isInternetAvailable(context)) {
            android.widget.Toast.makeText(
                context,
                "Please turn on your internet to talk to Asi.",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }

        messages.add(ChatMessage(userText, true))

        viewModelScope.launch {
            try {

                isAiTyping = true

                delay(1000)

                val systemInstruction = """
                    You are Spendy, a helpful financial assistant for 'SpendWise'.
                    SpendWise is an offline-first Android app for students/individuals.
                    Capabilities:
                    - Record expenses and income instantly.
                    - Categorize spending (Food, Transport, etc.).
                    - View summaries offline.
                    - Monitor savings goals.
                    
                    Tone: Friendly, professional, and encouraging. Use concise answers.
                """.trimIndent()
                val response = generativeModel.generateContent(
                    content {
                        text(systemInstruction)
                        text("User Inquiry: $userText")
                    }
                )

                isAiTyping = false
                response.text?.let {
                    messages.add(ChatMessage(it, false))
                }
            } catch (e: Exception) {
                isAiTyping = false

                val errorMessage = e.localizedMessage ?: ""

                val userFriendlyError = when {
                    errorMessage.contains("quota", ignoreCase = true) ->
                        "I'm a bit overwhelmed right now! Please wait a few seconds before asking another question. 😅"

                    errorMessage.contains("limit", ignoreCase = true) ->
                        "We've reached the speed limit! Let's take a 10-second breather."

                    else -> "Oops! I'm having trouble connecting. Please check your internet and try again."
                }

                messages.add(ChatMessage(userFriendlyError, false))
            }
        }
    }
}