package com.example.models.supplier

import com.example.models.util.ApprovalStatus
import kotlinx.serialization.Serializable

@Serializable
data class SupplierStatusUpdate(
    val status: ApprovalStatus
)
