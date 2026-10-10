package com.baharhooni.morkhesi.data

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Storage عمداً lazy است: اگر Storage فعال نشده باشد، اپ هنگام باز شدن کرش نمی‌کند
    // و فقط آپلود گواهی پزشکی خطا می‌دهد.
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
    }

    fun signOut() = auth.signOut()
    fun currentUserId(): String? = auth.currentUser?.uid

    suspend fun getUserProfile(): UserProfile? {
        val uid = currentUserId() ?: return null
        val snap = db.collection("users").document(uid).get().await()
        return snap.toObject(UserProfile::class.java)
    }

    suspend fun getBalance(): LeaveBalance {
        val uid = currentUserId() ?: return LeaveBalance()
        val snap = db.collection("leaveBalances").document(uid).get().await()
        return snap.toObject(LeaveBalance::class.java) ?: LeaveBalance()
    }

    // مرتب‌سازی در خود اپ انجام می‌شود تا نیازی به ساخت Index در Firestore نباشد.
    suspend fun getRecentRequests(limit: Int = 5): List<LeaveRequest> {
        val uid = currentUserId() ?: return emptyList()
        return db.collection("leaveRequests")
            .whereEqualTo("userId", uid)
            .get().await()
            .documents
            .mapNotNull { doc -> doc.toObject(LeaveRequest::class.java)?.copy(id = doc.id) }
            .sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE }
            .take(limit)
    }

    suspend fun uploadMedicalCertificate(uri: Uri): String {
        val uid = currentUserId() ?: error("کاربر وارد نشده است")
        val ref = storage.reference.child("medicalCertificates/$uid/${System.currentTimeMillis()}.pdf")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun createLeaveRequest(request: LeaveRequest, certificateUri: Uri? = null) {
        val uid = currentUserId() ?: error("کاربر وارد نشده است")
        val certificateUrl = certificateUri?.let { uploadMedicalCertificate(it) }
        val data = hashMapOf<String, Any?>(
            "userId" to uid,
            "leaveType" to request.leaveType,
            "startDate" to request.startDate,
            "endDate" to request.endDate,
            "hours" to request.hours,
            "reason" to request.reason,
            "medicalCertificateUrl" to certificateUrl,
            "status" to "PENDING",
            "managerNote" to "",
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("leaveRequests").add(data).await()
    }

    suspend fun ensureUserProfile(email: String) {
        val uid = currentUserId() ?: return
        val ref = db.collection("users").document(uid)
        ref.set(
            mapOf("email" to email, "updatedAt" to FieldValue.serverTimestamp()),
            SetOptions.merge()
        ).await()
    }
}

data class UserProfile(
    val name: String = "",
    val employeeCode: String = "",
    val email: String = ""
)

data class LeaveBalance(
    val entitlementDays: Double = 0.0,
    val usedDays: Double = 0.0,
    val incentiveDays: Double = 0.0,
    val carriedDays: Double = 0.0
) {
    val remainingDays: Double get() = (entitlementDays - usedDays + carriedDays).coerceAtLeast(0.0)
}

data class LeaveRequest(
    val id: String = "",
    val userId: String = "",
    val leaveType: String = "استحقاقی",
    val startDate: String = "",
    val endDate: String = "",
    val hours: Double = 8.0,
    val reason: String = "",
    val medicalCertificateUrl: String? = null,
    val status: String = "PENDING",
    val managerNote: String = "",
    val createdAt: Timestamp? = null
)
