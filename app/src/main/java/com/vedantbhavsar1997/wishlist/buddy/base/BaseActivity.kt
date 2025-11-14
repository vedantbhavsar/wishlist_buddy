package com.vedantbhavsar1997.wishlist.buddy.base

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {
    open fun showSnackbar(view: View, message: String, longDuration: Boolean) {
        Snackbar.make(
            view, message,
            if (longDuration) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
        ).show()
    }

    open fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}