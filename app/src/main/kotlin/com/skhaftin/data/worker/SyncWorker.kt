package com.skhaftin.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skhaftin.data.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val repository = DataRepository()
        try {
            // Sync local changes to Firebase
            repository.syncLocalDataToFirebase()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
