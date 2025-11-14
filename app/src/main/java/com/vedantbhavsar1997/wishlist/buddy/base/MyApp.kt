package com.vedantbhavsar1997.wishlist.buddy.base

import android.app.Application

class MyApp : Application() {
    val appViewModel by lazy { AppViewModel() }
}