package com.example.spendwise.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.spendwise.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data class defined locally
data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    navController: NavController,
    viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Observe messages from ViewModel
    val messages = viewModel.messages

    // EFFECT: Safe Auto-scroll to bottom when messages list updates
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            // Safety: Ensure index is never negative
            val lastIndex = (messages.size - 1).coerceAtLeast(0)
            listState.animateScrollToItem(lastIndex)
        }
    }

    Scaffold(
        topBar = {
            MessengerHeader(onBackClick = { navController.popBackStack() })
        },
        containerColor = Color(0xFFF8FAFB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- Chat Messages Area ---
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ADDED: Always show a welcome message if the list is empty
                if (messages.isEmpty()) {
                    item {
                        ChatBubble(ChatMessage("Hello! I am Spendy, your personal finance assistant. How can I help you today?", false))
                    }
                }

                items(messages) { message ->
                    ChatBubble(message)
                }

                if (viewModel.isAiTyping) {
                    item {
                        TypingIndicatorBubble()
                    }
                }
            }

            // --- Input Area ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask Spendy anything...", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF29CFAE),
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color(0xFFF1F3F4),
                            unfocusedContainerColor = Color(0xFFF1F3F4)
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val textToSend = inputText
                                inputText = ""

                                viewModel.sendMessage(textToSend, context)

                                coroutineScope.launch {
                                    delay(200)
                                    // CRASH FIX: Verify list is not empty before scrolling
                                    if (messages.isNotEmpty()) {
                                        val lastIndex = (messages.size - 1).coerceAtLeast(0)
                                        listState.animateScrollToItem(lastIndex)
                                    }
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF29CFAE),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble() {
    var dots by remember { mutableStateOf("") }

    // Animate the dots every 400ms
    LaunchedEffect(Unit) {
        while (true) {
            dots = when (dots) {
                "" -> "."
                "." -> ".."
                ".." -> "..."
                else -> ""
            }
            delay(400)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            color = Color(0xFFE9E9EB),
            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 2.dp)
        ) {
            Text(
                text = "Spendy is typing$dots",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerHeader(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Spendy",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.Black
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF4CAF50), shape = RoundedCornerShape(50))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Active Now",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                    contentDescription = "Back",
                    tint = Color(0xFF29CFAE),
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
        modifier = Modifier.shadow(elevation = 2.dp)
    )
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (message.isUser) Color(0xFF29CFAE) else Color(0xFFE9E9EB)
    val textColor = if (message.isUser) Color.White else Color.Black
    val shape = if (message.isUser) {
        RoundedCornerShape(18.dp, 18.dp, 2.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 2.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(color = bubbleColor, shape = shape) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                color = textColor,
                fontSize = 15.sp
            )
        }
    }
}