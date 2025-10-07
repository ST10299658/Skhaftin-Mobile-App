package com.skhaftin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skhaftin.ui.CreateListingScreen
import com.skhaftin.ui.DonationScreen
import com.skhaftin.viewmodel.ItemViewModel

class DonationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var currentScreen by remember { mutableStateOf("donation") }
            var selectedItem by remember { mutableStateOf<com.skhaftin.model.Item?>(null) }
            val itemViewModel: ItemViewModel = viewModel()
            itemViewModel.setContext(this@DonationActivity)
            LaunchedEffect(Unit) {
                itemViewModel.loadAvailableItems()
            }
            val items by itemViewModel.items.collectAsState()
            val isLoading by itemViewModel.isLoading.collectAsState()
            // Debug log
            android.util.Log.d("DonationActivity", "Items count: ${items.size}")
            items.forEach { item ->
                android.util.Log.d("DonationActivity", "Item: ${item.name}, imageUrl: ${item.imageUrl}")
            }

            when (currentScreen) {
                "donation" -> DonationScreen(
                    onBackClick = { finish() },
                    onCreateListingClick = { currentScreen = "create" },
                    availableItems = items,
                    selectedItem = selectedItem,
                    onItemClick = { item -> selectedItem = if (selectedItem == item) null else item },
                    onContactClick = { item ->
                        val intent = android.content.Intent(this, ChatActivity::class.java).apply {
                            putExtra("recipientId", item.ownerId)
                        }
                        startActivity(intent)
                    }
                )
                "create" -> CreateListingScreen(
                    onBackClick = { currentScreen = "donation" },
                    onSubmit = { name, quantity, description, location, imageUri ->
                        itemViewModel.createItem(name, quantity, description, location, imageUri)
                        Toast.makeText(this, "Donation submitted", Toast.LENGTH_SHORT).show()
                        currentScreen = "donation"
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}
