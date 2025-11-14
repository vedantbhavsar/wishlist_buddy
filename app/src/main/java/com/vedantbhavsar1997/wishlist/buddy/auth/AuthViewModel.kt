package com.vedantbhavsar1997.wishlist.buddy.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.vedantbhavsar1997.wishlist.buddy.base.AppViewModel
import com.vedantbhavsar1997.wishlist.buddy.base.PageType

class AuthViewModel : AppViewModel() {
    private val _authType = MutableLiveData<AuthType>()
    val authType: LiveData<AuthType> = _authType

    override fun init() {
        _authType.value = AuthType.SignIn
    }

    fun updateAuthType(authType: AuthType) {
        _authType.postValue(authType)
    }

    fun updatePageType(pageType: PageType) {
        _pageType.postValue(pageType    )
    }

    fun userSignInUp(
        name: String, email: String,
        password: String
    ) {
        if (_authType.value == AuthType.SignIn) {
            userSignIn(email, password)
        } else if (_authType.value == AuthType.SignUp) {
            userSignUp(name, email, password)
        }
    }

    private fun userSignIn(
        email: String, password: String
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.exception != null) {
                    _authType.value = AuthType.SignIn
                    _error.value = it.exception
                    return@addOnCompleteListener
                }
                Log.d("Auth", "Sign in status: ${it.result}")
                it.result.user?.let { user ->
                    createContact(
                        user.displayName ?: "", user.phoneNumber ?: "",
                    )
                    if (user.phoneNumber.isNullOrEmpty()) {
                        _pageType.value = PageType.Mobile
                    } else {
                        _pageType.value = PageType.Home
                    }
                }
        }
    }

    private fun userSignUp(
        name: String, email: String,
        password: String
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.exception != null) {
                _authType.value = AuthType.SignUp
                _error.value = it.exception
                return@addOnCompleteListener
            }
            val currentUser = it.result.user
            if (currentUser != null) {
                if (currentUser.displayName.isNullOrEmpty()) {
                    it.result.user?.let { user ->
                        user.updateProfile(
                            userProfileChangeRequest {
                                displayName = name
                            }
                        )
                    }
                    it.result.user?.reload()
                }
                createContact(
                    name, "",
                )
                _pageType.value = PageType.Mobile
            }
        }
    }

    fun verifyCode(code: String, verificationId: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        updatePhoneNumber(credential)
    }

    fun updatePhoneNumber(credential: PhoneAuthCredential) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.updatePhoneNumber(credential)
            ?.addOnCompleteListener { task ->
                if (task.exception != null) {
                    _authType.value = AuthType.SignIn
                    _error.value = task.exception
                    return@addOnCompleteListener
                }
                if (task.isSuccessful) {
                    Log.d("Auth", "Phone number updated successfully")
                    _pageType.value = PageType.Home
                } else {
                    Log.e("Auth", "Update failed: ${task.exception?.message}")
                    _pageType.value = PageType.Auth
                    _authType.value = AuthType.SignIn
                    _error.value = Exception("Update phone number failed")
                }
            }
    }
}

enum class AuthType {
    SignIn,
    SignUp
}