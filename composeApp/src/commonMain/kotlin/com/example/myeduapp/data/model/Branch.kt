package com.example.myeduapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BranchOption(
    @Serializable(with = JsonFlexibleIdSerializer::class)
    val id: String,
    val name: String,
    val code: String? = null,
    val is_active: Boolean? = true
)

@Serializable
data class AccessibleBranchesResponse(
    val success: Boolean,
    val data: List<BranchOption> = emptyList(),
    val user_branch_id: Int? = null,
    val can_select_branch: Boolean? = null
)
