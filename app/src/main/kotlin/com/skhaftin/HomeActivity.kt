package com.skhaftin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skhaftin.data.DataRepository
import com.skhaftin.ui.FoodItemCard
import com.skhaftin.viewmodel.ItemViewModel
import com.skhaftin.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
class HomeActivity : ComponentActivity() {

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            val user by userViewModel.user.collectAsState()
            val itemViewModel = androidx.lifecycle.viewmodel.compose.viewModel<ItemViewModel>()
            LaunchedEffect(Unit) {
                itemViewModel.loadAvailableItems()
            }
            val items by itemViewModel.items.collectAsState()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Welcome, ${user?.name ?: "User"}") },
                        actions = {
                            IconButton(onClick = {
                                startActivity(Intent(this@HomeActivity, ChatActivity::class.java))
                            }) {
                                Icon(Icons.Filled.Chat, contentDescription = "Chat")
                            }
                            IconButton(onClick = {
                                userViewModel.logout()
                                itemViewModel.clearItems()
                                Toast.makeText(this@HomeActivity, "Logged out", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                                finish()
                            }) {
                                Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFFFFE066),
                            titleContentColor = Color(0xFF333333)
                        )
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            startActivity(Intent(this@HomeActivity, DonationActivity::class.java))
                        },
                        containerColor = Color(0xFFFFE066),
                        contentColor = Color(0xFF333333)
                    ) {
                        Icon(Icons.Filled.Restaurant, contentDescription = "Donations")
                    }
                },
                content = { padding ->
                    if (items.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                            Text("No available items", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                            items(items) { item ->
                                FoodItemCard(item)
                            }
                        }
                    }
                }
            )
        }
    }
}
