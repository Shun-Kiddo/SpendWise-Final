package com.example.spendwise.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
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

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
        apiKey = "AIzaSyCdSPZJOtgSY1RTWc_xMQAnoJEhZqyk-9A"
    )

    val messages = mutableStateListOf(
        ChatMessage(
            "Hey Gastador! 👋 I'm Spendy, your SpendWise assistant!\n\nJust a heads-up before we start:\n\n⚠️ I don't have access to your balance or transaction history — for that, check the app's dashboard directly.\n\n💡 What I can do is help you with tips on budgeting, managing expenses, and making the most of SpendWise!\n\nAlso, this chat is session-only — messages won't be saved once you leave. 🗂️\n\nSo, what can I help you with today?",
            false
        )
    )

    var isAiTyping by mutableStateOf(false)
        private set

    private val systemInstruction = """
        You are Spendy, a helpful financial assistant for 'SpendWise'.
        SpendWise is an offline-first Android app for students/individuals.
        Capabilities:
        - Record expenses and income instantly.
        - Categorize spending (Food, Transport, etc.).
        - View summaries offline.
        - Monitor savings goals.
        
        Tone: Friendly, professional, and encouraging. Use concise answers.
    """.trimIndent()

    fun sendMessage(userText: String, context: Context) {
        if (userText.isBlank()) return

        if (!isInternetAvailable(context)) {
            android.widget.Toast.makeText(
                context,
                "Please turn on your internet to talk to Spendy.",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }

        messages.add(ChatMessage(userText, true))

        viewModelScope.launch {
            try {
                isAiTyping = true
                delay(1000)

                val chat = generativeModel.startChat(
                    history = listOf(
                        content("user") { text(systemInstruction) },
                        content("model") { text("Understood! I'm Spendy, your SpendWise assistant. Ready to help!") }
                    )
                )

                val response = chat.sendMessage(userText)

                isAiTyping = false
                response.text?.let {
                    messages.add(ChatMessage(it, false))
                }

            } catch (e: Exception) {
                isAiTyping = false

                Log.e("ChatViewModel", "Gemini API Error: ${e::class.simpleName}: ${e.localizedMessage}", e)

                val errorMessage = e.localizedMessage ?: ""

                val userFriendlyError = when {
                    errorMessage.contains("quota", ignoreCase = true) ->
                        "I'm a bit overwhelmed right now! Please wait a few seconds before asking another question. 😅"
                    errorMessage.contains("limit", ignoreCase = true) ->
                        "We've reached the speed limit! Let's take a 10-second breather. ⏱️"
                    errorMessage.contains("API_KEY", ignoreCase = true) ->
                        "There's a configuration issue. Please contact support. 🔧"
                    else ->
                        "Oops! I'm having trouble connecting. Please check your internet and try again. 🌐"
                }

                messages.add(ChatMessage(userFriendlyError, false))
            }
        }
    }
}