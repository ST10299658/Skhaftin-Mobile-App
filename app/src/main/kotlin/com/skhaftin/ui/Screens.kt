package com.skhaftin.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.skhaftin.R
import com.skhaftin.model.Item
import java.io.File

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat



/**
 * Composable function that displays the donation screen with tabs for donating and receiving items.
 * Includes overview cards for performance metrics and a list of available items to receive.
 * @param onBackClick Callback invoked when the back button is clicked.
 * @param onCreateListingClick Callback invoked when the create listing button is clicked.
 * @param availableItems List of available items to display in the Receive tab.
 * @param selectedItem The currently selected item.
 * @param onItemClick Callback invoked when an item is clicked.
 * @param onContactClick Callback invoked when the contact icon is clicked.
 */
@Composable
fun DonationScreen(
    onBackClick: () -> Unit,
    onCreateListingClick: () -> Unit,
    availableItems: List<Item> = emptyList(),
    selectedItem: Item? = null,
    onItemClick: (Item) -> Unit = {},
    onContactClick: (Item) -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Donate", "Receive")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFE066)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar with back button and tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFE066))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF333333)
                    )
                }
                TabRow(selectedTabIndex = selectedTab, containerColor = Color(0xFFFFE066)) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = if (index == 0) Icons.Filled.Restaurant else Icons.Filled.ShoppingCart,
                                    contentDescription = title,
                                    tint = if (selectedTab == index) Color.Black else Color.Gray
                                )
                            },
                            text = { Text(title, color = if (selectedTab == index) Color.Black else Color.Gray) }
                        )
                    }
                }
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    // Donate tab content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Button(
                            onClick = onCreateListingClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF3B0))
                        ) {
                            Text("+ Create Listing", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(32.dp))

                        // Overview cards - hardcoded for now
                        OverviewCard(title = "Chats to answer", value = "0")
                        OverviewCard(title = "Active listing", value = "1")
                        OverviewCard(title = "Listing to renew", value = "1")
                        OverviewCard(title = "Listings to delete and relist", value = "0")

                        Spacer(modifier = Modifier.height(48.dp))

                        Text("Performance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        OverviewCard(title = "Clicks on listings", value = "0\nLast 7 days")
                        OverviewCard(title = "Seller rating", value = "0\n0 ratings")
                        OverviewCard(title = "New followers", value = "0\nLast 7 days")
                    }
                }
                1 -> {
                    // Receive tab content
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(availableItems) { item ->
                            FoodItemCard(
                                item = item,
                                isSelected = selectedItem == item,
                                onItemClick = onItemClick,
                                onContactClick = onContactClick
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable function that displays an overview card with a title and value.
 * Used for showing metrics like chats to answer, active listings, etc.
 * @param title The title text to display.
 * @param value The value text to display.
 */
@Composable
fun OverviewCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        // A Row arranges its children horizontally
        Row(
            modifier = Modifier
                .fillMaxWidth() // Ensure the row takes the full width
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically // Align children vertically
        ) {
            // First child: The title text
            Text(
                text = title,
                color = Color.Black,
                fontSize = 14.sp
            )

            // Second child: The value text
            Text(
                text = value,
                modifier = Modifier.weight(1f), // Takes remaining space
                textAlign = TextAlign.End, // Aligns the text to the end (right)
                color = Color.DarkGray,
                fontSize = 14.sp,
                lineHeight = 16.sp // Improves readability for multi-line text
            )
        }
    }
}

/**
 * Composable function that displays a card for a food item.
 * Shows the item's image, name, quantity, and location.
 * @param item The Item object containing the details to display.
 * @param isSelected Whether the item is selected.
 * @param onItemClick Callback invoked when the item is clicked.
 * @param onContactClick Callback invoked when the contact icon is clicked.
 */
@Composable
fun FoodItemCard(
    item: Item,
    isSelected: Boolean = false,
    onItemClick: (Item) -> Unit = {},
    onContactClick: (Item) -> Unit = {}
) {
    // Debug log
    android.util.Log.d("FoodItemCard", "Displaying item: ${item.name}, imageUrl: ${item.imageUrl}")
    val donatorAvailable = item.ownerId.isNotBlank()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onItemClick(item) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFFFF3B0) else Color.White)
    ) {
        Column {
            Row(modifier = Modifier.padding(16.dp)) {
                if (item.imageUrl != null) {
                    android.util.Log.d("FoodItemCard", "Loading image from URL: ${item.imageUrl}")
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier.size(64.dp),
                        error = painterResource(id = R.drawable.ic_launcher) // Fallback image on error
                    )
                } else if (item.localImagePath != null) {
                    android.util.Log.d("FoodItemCard", "Loading image from local path: ${item.localImagePath}")
                    AsyncImage(
                        model = File(item.localImagePath),
                        contentDescription = item.name,
                        modifier = Modifier.size(64.dp),
                        error = painterResource(id = R.drawable.ic_launcher) // Fallback image on error
                    )
                } else {
                    // Fallback to local drawable if no URL or local path
                    val imageRes = when (item.name) {
                        "Fresh Apples" -> R.drawable.fresh_apples
                        "Bread Loaves" -> R.drawable.bread_loaves
                        "Pizza" -> R.drawable.pizza
                        "Milk" -> R.drawable.milk
                        "Rice" -> R.drawable.rice
                        else -> R.drawable.ic_launcher
                    }
                    android.util.Log.d("FoodItemCard", "Using fallback drawable for ${item.name}: $imageRes")
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = item.name,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(item.name, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Quantity: ${item.quantity}", color = Color.DarkGray)
                    Text("Location: ${item.location}", color = Color.DarkGray)
                }
            }
            if (isSelected && donatorAvailable) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { onContactClick(item) }) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = "Contact Donator",
                            tint = Color(0xFF333333)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable function that displays a card for an image from the Images folder.
 * Shows the image and name.
 * @param name The name of the image.
 * @param imageRes The drawable resource ID for the image.
 */
@Composable
fun ImageCard(name: String, imageRes: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

/**
 * Composable function that displays the screen for creating a new food listing.
 * Allows users to input food name, quantity, description, and location.
 * @param onBackClick Callback invoked when the back button is clicked.
 * @param onSubmit Callback invoked when the submit button is clicked with the entered details.
 * @param isLoading Boolean indicating if the submission is in progress.
 */
@Composable
fun CreateListingScreen(
    onBackClick: () -> Unit,
    onSubmit: (name: String, quantity: String, description: String, location: String, imageUri: Uri?) -> Unit,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        imageUri = uri
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            Toast.makeText(context, "Permission required to select image", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFE066)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF333333)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Create Listing",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } else {
                            permissionLauncher.launch(permission)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Select Image", color = Color.Black)
                }
                Spacer(modifier = Modifier.height(8.dp))

                imageUri?.let {
                    Text(
                        text = "Selected Image: $it",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && quantity.isNotBlank() && location.isNotBlank()) {
                            onSubmit(name, quantity, description, location, imageUri)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF3B0)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                    } else {
                        Text("Submit", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Composable function that displays the login screen.
 * Allows users to enter email and password, with options for Google sign-in and registration.
 * @param onLogin Callback invoked when the login button is clicked with email and password.
 * @param onRegisterClick Callback invoked when the register link is clicked.
 * @param onBackClick Callback invoked when the back button is clicked.
 * @param onGoogleSignInClick Callback invoked when the Google sign-in button is clicked.
 * @param isLoading Boolean indicating if the login is in progress.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    isLoading: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFFE066)) {
        Column(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF333333)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Skhaftin",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "Welcome back",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(24.dp))
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(24.dp))
                ) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                modifier = Modifier
                                    .clickable { isPasswordVisible = !isPasswordVisible }
                                    .padding(8.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            onLogin(email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF3B0)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                    } else {
                        Text("Login", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Sign in with Google", color = Color.Black)
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onRegisterClick) {
                    Text("Don't have an account? Register", color = Color(0xFF333333))
                }
            }
        }
    }
}

/**
 * Composable function that displays the registration screen.
 * Allows users to enter full name, email, password, phone number, location, and preferred language.
 * @param onRegister Callback invoked when the register button is clicked with the entered details.
 * @param onLoginClick Callback invoked when the login link is clicked.
 * @param isLoading Boolean indicating if the registration is in progress.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegister: (String, String, String, String?, String?, String?) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var preferredLanguage by remember { mutableStateOf("English") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFFE066)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_skhaftin),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
            )
            Text(
                "Skhaftin",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Create your account",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            modifier = Modifier
                                .clickable { isPasswordVisible = !isPasswordVisible }
                                .padding(8.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
            ) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
            ) {
                OutlinedTextField(
                    value = preferredLanguage,
                    onValueChange = { preferredLanguage = it },
                    label = { Text("Preferred Language") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                        onRegister(name, email, password, phoneNumber.takeIf { it.isNotBlank() }, location.takeIf { it.isNotBlank() }, preferredLanguage.takeIf { it.isNotBlank() })
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF3B0)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                } else {
                    Text("Register", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onLoginClick) {
                Text("Already have an account? Login", color = Color(0xFF333333))
            }
        }
    }
}

/**
 * Composable function that displays the home screen after login.
 * Shows a welcome message with the user's name and a logout button.
 * @param userName The name of the logged-in user.
 * @param onLogout Callback invoked when the logout button is clicked.
 */
@Composable
fun HomeScreen(
    userName: String,
    onLogout: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFFE066)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome, $userName!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "You are now logged in to Skhaftin",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF3B0))
            ) {
                Text("Logout", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Composable function that displays the splash screen.
 * Shows the app name centered on the screen.
 */
@Composable
fun SplashScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFE066)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Skhaftin",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.align(Alignment.Center)
            )
            // Adding logo image above the text
            Image(
                painter = painterResource(id = R.drawable.logo_skhaftin),
                contentDescription = "App Logo",
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 200.dp),
            )

        }

    }
}

/**
 * Composable function that displays a loading screen.
 * Shows a circular progress indicator and "Loading..." text.
 */
@Composable
fun LoadingScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFE066)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF333333),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }
            // Adding logo image above the text
            Image(
                painter = painterResource(id = R.drawable.logo_skhaftin),
                contentDescription = "App Logo",
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 200.dp),
            )
        }
    }
}

/**
 * Composable function that displays the main screen.
 * Provides buttons to navigate to login or register screens.
 * @param onLoginClick Callback invoked when the login button is clicked.
 * @param onRegisterClick Callback invoked when the register button is clicked.
 */
@Composable
fun MainScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFE066)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to Skhaftin",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF3B0))
                ) {
                    Text("Login", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF3B0))
                ) {
                    Text("Register", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
