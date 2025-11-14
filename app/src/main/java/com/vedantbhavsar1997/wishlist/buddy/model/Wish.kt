package com.vedantbhavsar1997.wishlist.buddy.model

data class Wish(
    val wish: String? = null,
    val addedByName: String? = null,
    val addedByNumber: String? = null,
    var isCompleted: Boolean = false,
    var completedByName: String? = null,
    var completedByNumber: String? = null
)
