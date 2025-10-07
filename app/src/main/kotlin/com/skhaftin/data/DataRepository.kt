package com.skhaftin.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.room.Room
import com.skhaftin.data.local.AppDatabase
import com.skhaftin.model.User
import com.skhaftin.model.FoodListing
import com.skhaftin.model.Chat
import com.skhaftin.model.Message
import com.skhaftin.model.Impact
import com.skhaftin.model.ScheduledDonation
import com.skhaftin.model.PickupLocation
import com.skhaftin.model.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.messaging.FirebaseMessaging
import android.net.Uri
import kotlin.math.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class DataRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    /**
     * Returns the currently authenticated Firebase user.
     * @return FirebaseUser if authenticated, null otherwise
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    private val database: DatabaseReference = FirebaseDatabase.getInstance("https://skhaftin-2131e-default-rtdb.europe-west1.firebasedatabase.app").reference
    private val storage: StorageReference = FirebaseStorage.getInstance().reference
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()
    private var authToken: String? = null
    private var context: Context? = null
    private var appDatabase: AppDatabase? = null

    init {
        // Persistence enabled in MainActivity
    }

    fun setContext(context: Context) {
        this.context = context
        appDatabase = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "skhaftin_database"
        ).build()
    }

    private fun isOnline(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun setAuthToken(token: String) {
        this.authToken = token
    }

    private fun firebaseUserToUser(firebaseUser: FirebaseUser?): User? {
        return firebaseUser?.let { user ->
            User(
                id = user.uid,
                name = user.displayName ?: "",
                email = user.email ?: "N/A",
                phoneNumber = user.phoneNumber ?: "N/A",
                location = "N/A",
                language = "N/A",
                preferences = emptyMap(),
                verified = false,
                impact = emptyMap(),
                token = "N/A"
            )
        }
    }

    fun loginUser(email: String, password: String, callback: (Result<User>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        getUserFromDatabase(firebaseUser.uid) { result ->
                            result.onSuccess { user ->
                                // Update login count and times
                                val updatedUser = user.copy(
                                    loginCount = user.loginCount + 1,
                                    loginTimes = user.loginTimes + System.currentTimeMillis()
                                )
                                saveUserToDatabase(updatedUser) { saveResult ->
                                    saveResult.onSuccess {
                                        callback(Result.success(updatedUser))
                                    }.onFailure { e ->
                                        callback(Result.failure(e))
                                    }
                                }
                            }.onFailure {
                                // User not in database, create from Firebase Auth
                                val newUser = firebaseUserToUser(firebaseUser)?.copy(
                                    id = firebaseUser.uid,
                                    name = firebaseUser.displayName ?: "",
                                    email = firebaseUser.email ?: "",
                                    verified = firebaseUser.isEmailVerified,
                                    loginCount = 1,
                                    loginTimes = listOf(System.currentTimeMillis())
                                )
                                if (newUser != null) {
                                    saveUserToDatabase(newUser) { saveResult ->
                                        saveResult.onSuccess {
                                            callback(Result.success(newUser))
                                        }.onFailure { e ->
                                            callback(Result.failure(e))
                                        }
                                    }
                                } else {
                                    callback(Result.failure(Exception("Failed to create user from Auth")))
                                }
                            }
                        }
                    } else {
                        callback(Result.failure(Exception("User data is null after login")))
                    }
                } else {
                    callback(Result.failure(task.exception ?: Exception("Login failed")))
                }
            }
    }

    fun registerUser(
        name: String,
        email: String,
        password: String,
        phoneNumber: String?,
        location: String?,
        language: String?,
        preferences: Map<String, Boolean>?,
        callback: (Result<User>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    // Update displayName
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name ?: "")
                        .build()
                    firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            val user = firebaseUserToUser(firebaseUser)?.copy(
                                phoneNumber = phoneNumber ?: "",
                                location = location ?: "",
                                language = language ?: "",
                                preferences = preferences ?: emptyMap()
                            )
                            if (user != null) {
                                // Save user to database
                                saveUserToDatabase(user) { saveResult ->
                                    saveResult.onSuccess {
                                        callback(Result.success(user))
                                    }.onFailure { e ->
                                        callback(Result.failure(e))
                                    }
                                }
                            } else {
                                callback(Result.failure(Exception("User data is null after registration")))
                            }
                        } else {
                            callback(Result.failure(updateTask.exception ?: Exception("Failed to update user profile")))
                        }
                    }
                } else {
                    callback(Result.failure(task.exception ?: Exception("Registration failed")))
                }
            }
    }

    fun saveUserToDatabase(user: User, callback: (Result<Unit>) -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            // Ensure user.id is set to current UID
            val userToSave = if (user.id == firebaseUser.uid) user else user.copy(id = firebaseUser.uid)
            database.child("users").child(firebaseUser.uid).setValue(userToSave)
                .addOnSuccessListener { callback(Result.success(Unit)) }
                .addOnFailureListener { e -> callback(Result.failure(e)) }
        } else {
            callback(Result.failure(Exception("No authenticated user found")))
        }
    }

    fun getUserFromDatabase(userId: String, callback: (Result<User>) -> Unit) {
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    callback(Result.success(user))
                } else {
                    callback(Result.failure(Exception("User not found")))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(Result.failure(error.toException()))
            }
        })
    }

    fun addUserListener(userId: String, callback: (User?) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                callback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        }
        database.child("users").child(userId).addValueEventListener(listener)
        return listener
    }

    fun removeUserListener(userId: String, listener: ValueEventListener) {
        database.child("users").child(userId).removeEventListener(listener)
    }

    // Items methods
    private fun saveItemToFirebaseWithPush(
        name: String,
        quantity: String,
        description: String,
        location: String,
        imageUrl: String?,
        localImagePath: String?,
        ownerId: String,
        uniqueCode: String,
        callback: (Result<Item>) -> Unit
    ) {
        val newItemRef = database.child("items").push()
        val itemData = mapOf(
            "id" to newItemRef.key,
            "name" to name,
            "quantity" to quantity,
            "description" to description,
            "location" to location,
            "imageUrl" to imageUrl,
            "localImagePath" to localImagePath,
            "ownerId" to ownerId,
            "uniqueCode" to uniqueCode,
            "timestamp" to System.currentTimeMillis()
        )

        newItemRef.setValue(itemData)
            .addOnSuccessListener {
                val item = Item(
                    id = newItemRef.key ?: "",
                    name = name,
                    quantity = quantity,
                    description = description,
                    location = location,
                    imageUrl = imageUrl,
                    localImagePath = localImagePath,
                    ownerId = ownerId,
                    uniqueCode = uniqueCode
                )
                // Save to local DB
                context?.let { _ ->
                    @OptIn(DelicateCoroutinesApi::class)
                    GlobalScope.launch {
                        appDatabase?.itemDao()?.insertItem(item)
                    }
                }
                callback(Result.success(item))
            }
            .addOnFailureListener { e ->
                callback(Result.failure(e))
            }
    }

    fun getItems(callback: (Result<List<Item>>) -> Unit) {
        android.util.Log.d("DataRepository", "getItems called, isOnline: ${isOnline()}")
        if (isOnline()) {
            database.child("items").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = mutableListOf<Item>()
                    for (childSnapshot in snapshot.children) {
                        val item = childSnapshot.getValue(Item::class.java)
                        if (item != null) {
                            val id = childSnapshot.key ?: ""
                            items.add(item.copy(id = id))
                            android.util.Log.d("DataRepository", "Fetched item: ${item.name}, imageUrl: ${item.imageUrl}")
                        }
                    }
                    android.util.Log.d("DataRepository", "Total items fetched: ${items.size}")
                    // Save to local DB
                    items.let {
                        // Insert items asynchronously using coroutine
                        context?.let { _ ->
                            @OptIn(DelicateCoroutinesApi::class)
                            GlobalScope.launch {
                                appDatabase?.itemDao()?.insertAll(it)
                            }
                        }
                    }
                    callback(Result.success(items))
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.d("DataRepository", "Firebase fetch cancelled, falling back to local DB")
                    // Fallback to local DB
                    context?.let { _ ->
                        @OptIn(DelicateCoroutinesApi::class)
                        GlobalScope.launch {
                            val localItems = appDatabase?.itemDao()?.getAllItems()?.first() ?: emptyList()
                            android.util.Log.d("DataRepository", "Local items count: ${localItems.size}")
                            callback(Result.success(localItems))
                        }
                    } ?: run {
                        callback(Result.success(emptyList()))
                    }
                }
            })
        } else {
            android.util.Log.d("DataRepository", "Offline, fetching from local DB")
            // Offline: get from local DB
            context?.let { _ ->
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch {
                    val localItems = appDatabase?.itemDao()?.getAllItems()?.first() ?: emptyList()
                    android.util.Log.d("DataRepository", "Local items count: ${localItems.size}")
                    callback(Result.success(localItems))
                }
            } ?: run {
                callback(Result.success(emptyList()))
            }
        }
    }

    private fun generateUniqueCode(name: String): String {
        val prefix = name.take(3).uppercase()
        val random = (100..999).random()
        return "$prefix-$random"
    }

    fun createItem(
        name: String,
        quantity: String,
        description: String,
        location: String,
        imageUri: Uri?, // <-- allow optional image
        callback: (Result<Item>) -> Unit
    ) {
        val uniqueCode = generateUniqueCode(name)
        if (!isOnline()) {
            // Offline: save image locally and to local DB
            val localPath = imageUri?.let { saveImageLocally(it) }
            val item = Item(
                id = System.currentTimeMillis().toString(),
                name = name,
                quantity = quantity,
                description = description,
                location = location,
                imageUrl = null,
                localImagePath = localPath,
                ownerId = auth.currentUser?.uid ?: "",
                uniqueCode = uniqueCode
            )
            context?.let { _ ->
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch {
                    appDatabase?.itemDao()?.insertItem(item)
                    callback(Result.success(item))
                }
            } ?: run {
                callback(Result.success(item))
            }
            return
        }

        // Online: upload image if any, then save to Firebase, then save to local
        if (imageUri != null) {
            uploadImage(imageUri) { url ->
                if (url != null) {
                    saveItemToFirebaseWithPush(name, quantity, description, location, url, null, auth.currentUser?.uid ?: "", uniqueCode, callback)
                } else {
                    callback(Result.failure(Exception("Image upload failed")))
                }
            }
        } else {
            saveItemToFirebaseWithPush(name, quantity, description, location, null, null, auth.currentUser?.uid ?: "", uniqueCode, callback)
        }
    }



    fun updateItem(id: String, name: String?, quantity: String?, description: String?, location: String?, callback: (Result<Unit>) -> Unit) {
        val updates = mutableMapOf<String, Any?>()
        name?.let { updates["name"] = it }
        quantity?.let { updates["quantity"] = it }
        description?.let { updates["description"] = it }
        location?.let { updates["location"] = it }
        database.child("items").child(id).updateChildren(updates).addOnSuccessListener {
            callback(Result.success(Unit))
        }.addOnFailureListener {
            callback(Result.failure(it))
        }
    }

    fun deleteItem(id: String, callback: (Result<Unit>) -> Unit) {
        database.child("items").child(id).removeValue().addOnSuccessListener {
            callback(Result.success(Unit))
        }.addOnFailureListener {
            callback(Result.failure(it))
        }
    }

    fun getAvailableItems(callback: (Result<List<Item>>) -> Unit) {
        // Assuming all items are available, or add a field if needed
        getItems(callback)
    }

    // FoodListings methods
    fun getFoodListings(callback: (Result<List<FoodListing>>) -> Unit) {
        database.child("foodlistings").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listings = mutableListOf<FoodListing>()
                for (childSnapshot in snapshot.children) {
                    val listing = childSnapshot.getValue(FoodListing::class.java)
                    if (listing != null) {
                        val id = childSnapshot.key ?: ""
                        listings.add(listing.copy(id = id))
                    }
                }
                callback(Result.success(listings))
            }

            override fun onCancelled(error: DatabaseError) {
                callback(Result.failure(error.toException()))
            }
        })
    }

    fun createFoodListing(
        donorId: String,
        foodName: String,
        description: String,
        category: String,
        expiryDate: String,
        urgency: String,
        quantity: String,
        pickupLocation: PickupLocation?,
        pickupTime: String,
        imageUri: Uri? = null,
        callback: (Result<FoodListing>) -> Unit
    ) {
        if (imageUri != null) {
            uploadImage(imageUri) { url ->
                if (url != null) {
                    saveFoodListing(donorId, foodName, description, category, expiryDate, urgency, quantity, pickupLocation, pickupTime, url, callback)
                } else {
                    callback(Result.failure(Exception("Image upload failed")))
                }
            }
        } else {
            saveFoodListing(donorId, foodName, description, category, expiryDate, urgency, quantity, pickupLocation, pickupTime, null, callback)
        }
    }

    private fun saveFoodListing(
        donorId: String,
        foodName: String,
        description: String,
        category: String,
        expiryDate: String,
        urgency: String,
        quantity: String,
        pickupLocation: PickupLocation?,
        pickupTime: String,
        imageUrl: String?,
        callback: (Result<FoodListing>) -> Unit
    ) {
        val listingData = mapOf(
            "donorId" to donorId,
            "foodName" to foodName,
            "description" to description,
            "category" to category,
            "expiryDate" to expiryDate,
            "urgency" to urgency,
            "quantity" to quantity,
            "pickupLocation" to pickupLocation,
            "pickupTime" to pickupTime,
            "imageUrl" to imageUrl,
            "available" to true,
            "timestamp" to System.currentTimeMillis()
        )
        val newListingRef = database.child("foodlistings").push()
        newListingRef.setValue(listingData).addOnSuccessListener {
            val listing = FoodListing(
                id = newListingRef.key ?: "",
                donorId = donorId,
                foodName = foodName,
                description = description,
                category = category,
                expiryDate = expiryDate,
                urgency = urgency,
                quantity = quantity,
                pickupLocation = pickupLocation,
                pickupTime = pickupTime,
                imageUrl = imageUrl,
                available = true,
                timestamp = System.currentTimeMillis()
            )
            callback(Result.success(listing))
        }.addOnFailureListener {
            callback(Result.failure(it))
        }
    }

    fun updateFoodListing(id: String, updates: Map<String, Any?>, callback: (Result<Unit>) -> Unit) {
        database.child("foodlistings").child(id).updateChildren(updates).addOnSuccessListener {
            callback(Result.success(Unit))
        }.addOnFailureListener {
            callback(Result.failure(it))
        }
    }

    fun deleteFoodListing(id: String, callback: (Result<Unit>) -> Unit) {
        database.child("foodlistings").child(id).removeValue().addOnSuccessListener {
            callback(Result.success(Unit))
        }.addOnFailureListener {
            callback(Result.failure(it))
        }
    }

    fun getAvailableFoodListings(callback: (Result<List<FoodListing>>) -> Unit) {
        database.child("foodlistings").orderByChild("available").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listings = mutableListOf<FoodListing>()
                    for (childSnapshot in snapshot.children) {
                        val listing = childSnapshot.getValue(FoodListing::class.java)
                        if (listing != null) {
                            val id = childSnapshot.key ?: ""
                            listings.add(listing.copy(id = id))
                        }
                    }
                    callback(Result.success(listings))
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(Result.failure(error.toException()))
                }
            })
    }

    fun getNearbyFoodListings(userLat: Double, userLng: Double, radiusKm: Double, callback: (Result<List<FoodListing>>) -> Unit) {
        getAvailableFoodListings { result ->
            result.onSuccess { listings ->
                val nearby = listings.filter { listing ->
                    listing.pickupLocation?.let { loc ->
                        calculateDistance(userLat, userLng, loc.lat, loc.lng) <= radiusKm
                    } ?: false
                }
                callback(Result.success(nearby))
            }.onFailure {
                callback(Result.failure(it))
            }
        }
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun saveImageLocally(uri: Uri): String? {
        val context = this.context ?: return null
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = java.io.File(context.filesDir, "images/${System.currentTimeMillis()}.jpg")
        file.parentFile?.mkdirs()
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return file.absolutePath
    }

    private fun uploadImage(uri: Uri, callback: (String?) -> Unit) {
        val ref = storage.child("images/${System.currentTimeMillis()}.jpg")
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { url ->
                callback(url.toString())
            }.addOnFailureListener {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun uploadImageFromResource(resourceId: Int, callback: (String?) -> Unit) {
        val uri = Uri.parse("android.resource://${context?.packageName}/$resourceId")
        uploadImage(uri, callback)
    }

    // Google Sign-In
    fun signInWithGoogle(idToken: String, callback: (Result<User>) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        getUserFromDatabase(firebaseUser.uid) { result ->
                            result.onSuccess { user ->
                                // Update login count and times
                                val updatedUser = user.copy(
                                    loginCount = user.loginCount + 1,
                                    loginTimes = user.loginTimes + System.currentTimeMillis()
                                )
                                saveUserToDatabase(updatedUser) { saveResult ->
                                    saveResult.onSuccess {
                                        callback(Result.success(updatedUser))
                                    }.onFailure { e ->
                                        callback(Result.failure(e))
                                    }
                                }
                            }.onFailure {
                                // If user not in database, create one
                                val newUser = firebaseUserToUser(firebaseUser)?.copy(
                                    id = firebaseUser.uid,
                                    name = firebaseUser.displayName ?: "",
                                    email = firebaseUser.email ?: "",
                                    verified = firebaseUser.isEmailVerified,
                                    loginCount = 1,
                                    loginTimes = listOf(System.currentTimeMillis())
                                )
                                if (newUser != null) {
                                    saveUserToDatabase(newUser) { saveResult ->
                                        saveResult.onSuccess {
                                            callback(Result.success(newUser))
                                        }.onFailure { e ->
                                            callback(Result.failure(e))
                                        }
                                    }
                                } else {
                                    callback(Result.failure(Exception("Failed to create user")))
                                }
                            }
                        }
                    } else {
                        callback(Result.failure(Exception("Firebase user is null")))
                    }
                } else {
                    callback(Result.failure(task.exception ?: Exception("Google sign-in failed")))
                }
            }
    }

    // Chats methods
    fun getChatsForUser(userId: String, callback: (Result<List<Chat>>) -> Unit) {
        database.child("chats").orderByChild("participants/$userId").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chats = mutableListOf<Chat>()
                    for (childSnapshot in snapshot.children) {
                        val chat = childSnapshot.getValue(Chat::class.java)
                        if (chat != null) {
                            val id = childSnapshot.key ?: ""
                            chats.add(chat.copy(chatId = id))
                        }
                    }
                    callback(Result.success(chats))
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(Result.failure(error.toException()))
                }
            })
    }

    fun createChat(participants: List<String>, callback: (Result<Chat>) -> Unit) {
        val participantsMap = participants.associateWith { true }
        val chatData = mapOf(
            "participants" to participantsMap
        )
        val newChatRef = database.child("chats").push()
        newChatRef.setValue(chatData).addOnSuccessListener {
            val chat = Chat(
                chatId = newChatRef.key ?: "",
                participants = participantsMap,
                messages = emptyMap()
            )
            callback(Result.success(chat))
        }.addOnFailureListener {
            callback(Result.failure(it))
        }
    }

    fun sendMessage(chatId: String, message: Message, callback: (Result<Unit>) -> Unit) {
        val messageData = mapOf(
            "messageId" to message.messageId,
            "senderId" to message.senderId,
            "text" to message.text,
            "timestamp" to message.timestamp,
            "seen" to message.seen
        )
        database.child("chats").child(chatId).child("messages").child(message.messageId).setValue(messageData)
            .addOnSuccessListener {
                callback(Result.success(Unit))
            }.addOnFailureListener {
                callback(Result.failure(it))
            }
    }

    fun getMessagesForChat(chatId: String, callback: (Result<List<Message>>) -> Unit) {
        database.child("chats").child(chatId).child("messages").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (childSnapshot in snapshot.children) {
                    val message = childSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }
                callback(Result.success(messages.sortedBy { it.timestamp }))
            }

            override fun onCancelled(error: DatabaseError) {
                callback(Result.failure(error.toException()))
            }
        })
    }

    fun getMessagesFlow(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (childSnapshot in snapshot.children) {
                    val message = childSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }
                trySend(messages.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.child("chats").child(chatId).child("messages").addValueEventListener(listener)
        // awaitClose { database.child("Chats").child(chatId).child("messages").removeEventListener(listener) }
    }

    // Impact methods
    fun getUserImpact(userId: String, callback: (Result<Impact>) -> Unit) {
        database.child("impact").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val impact = snapshot.getValue(Impact::class.java) ?: Impact()
                callback(Result.success(impact))
            }

            override fun onCancelled(error: DatabaseError) {
                callback(Result.failure(error.toException()))
            }
        })
    }

    fun updateUserImpact(userId: String, impact: Impact, callback: (Result<Unit>) -> Unit) {
        database.child("impact").child(userId).setValue(impact).addOnSuccessListener {
            callback(Result.success(Unit))
        }.addOnFailureListener {
            callback(Result.failure(it))
        }
    }

    // Scheduled Donations methods
    fun getScheduledDonationsForUser(userId: String, callback: (Result<List<ScheduledDonation>>) -> Unit) {
        database.child("scheduleddonations").orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val donations = mutableListOf<ScheduledDonation>()
                    for (childSnapshot in snapshot.children) {
                        val donation = childSnapshot.getValue(ScheduledDonation::class.java)
                        if (donation != null) {
                            donations.add(donation)
                        }
                    }
                    callback(Result.success(donations))
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(Result.failure(error.toException()))
                }
            })
    }

    fun createScheduledDonation(donation: ScheduledDonation, callback: (Result<ScheduledDonation>) -> Unit) {
        val donationData = mapOf(
            "scheduleId" to donation.scheduleId,
            "day" to donation.day,
            "time" to donation.time,
            "foodItem" to donation.foodItem,
            "repeat" to donation.repeat
        )
        val newDonationRef = database.child("scheduleddonations").push()
        newDonationRef.setValue(donationData).addOnSuccessListener {
            val newDonation = donation.copy(scheduleId = newDonationRef.key ?: "")
            callback(Result.success(newDonation))
        }.addOnFailureListener {
            callback(Result.failure(it))
        }
    }

    fun deleteScheduledDonation(scheduleId: String, callback: (Result<Unit>) -> Unit) {
        database.child("scheduleddonations").child(scheduleId).removeValue().addOnSuccessListener {
            callback(Result.success(Unit))
        }.addOnFailureListener {
            callback(Result.failure(it))
        }
    }

    // Messaging
    fun getFCMToken(callback: (String?) -> Unit) {
        messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(task.result)
            } else {
                callback(null)
            }
        }
    }

    fun sendNotificationToUser(userId: String, title: String, body: String) {
        // This would typically be done via a server function or Firebase Cloud Functions
        // For now, just log it
        println("Notification to $userId: $title - $body")
    }



    private fun checkItemExists(name: String, callback: (Boolean) -> Unit) {
        val query = database.child("items").orderByChild("name").equalTo(name)
        query.get().addOnSuccessListener { snapshot ->
            callback(snapshot.exists())
        }.addOnFailureListener {
            callback(false) // Assume not exists on error
        }
    }

    fun syncLocalDataToFirebase() {
        // TODO: Implement syncing local data to Firebase
        // For now, this is a placeholder
    }

    fun saveItemsLocally(items: List<Item>) {
        context?.let { _ ->
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                appDatabase?.itemDao()?.insertAll(items)
            }
        }
    }

    suspend fun clearLocalItems() {
        appDatabase?.itemDao()?.deleteAll()
    }
}
