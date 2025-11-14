package com.vedantbhavsar1997.wishlist.buddy.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.vedantbhavsar1997.wishlist.buddy.model.Contact

open class AppViewModel : ViewModel() {
    private val _contact = MutableLiveData<Contact>()
    val contact: LiveData<Contact> = _contact
    val _pageType = MutableLiveData<PageType>()
    val pageType: LiveData<PageType> = _pageType
    val _error = MutableLiveData<Exception>()
    val error: LiveData<Exception> = _error

    open fun init() {
        FirebaseAuth.getInstance().addAuthStateListener {
            val currentUser = it.currentUser
            Log.d("Auth", "${currentUser?.email} | ${currentUser?.displayName} | ${currentUser?.phoneNumber}")
            if (currentUser != null && !currentUser.phoneNumber.isNullOrEmpty()) {
                Log.d("Auth", "Updating page type: Home")
                createContact(currentUser.displayName ?: "", currentUser.phoneNumber ?: "")
                _pageType.value = PageType.Home
            } else if (currentUser != null && currentUser.phoneNumber.isNullOrEmpty()) {
                Log.d("Auth", "Updating page type: Mobile")
                createContact(currentUser.displayName ?: "", currentUser.phoneNumber ?: "")
                _pageType.value = PageType.Mobile
            } else {
                Log.d("Auth", "Updating page type: Auth")
                _pageType.value = PageType.Auth
            }
        }
    }

    fun createContact(name: String, number: String) {
        val newContact = Contact(name, number)
        _contact.value = newContact
    }
}

enum class PageType {
    Home,
    Auth,
    Mobile
}