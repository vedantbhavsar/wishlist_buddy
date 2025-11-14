package com.vedantbhavsar1997.wishlist.buddy.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.vedantbhavsar1997.wishlist.buddy.R
import com.vedantbhavsar1997.wishlist.buddy.base.BaseActivity
import com.vedantbhavsar1997.wishlist.buddy.base.PageType
import com.vedantbhavsar1997.wishlist.buddy.databinding.ActivityAuthBinding
import com.vedantbhavsar1997.wishlist.buddy.home.HomeActivity
import java.util.concurrent.TimeUnit

class AuthActivity : BaseActivity() {
    private lateinit var binding: ActivityAuthBinding
    private var viewModel: AuthViewModel? = null
    private var authType: AuthType = AuthType.SignIn
    private var pageType: PageType = PageType.Auth
    private var verificationId: String = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private var otp = ""
    private var countDownTimer: CountDownTimer? = null
    private val otpTimeout: Long = 60000 // 60 seconds
    private var counter: Int = 0 // 60 seconds

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-retrieval or instant verification
            viewModel?.updatePhoneNumber(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.e("Auth", "Verification failed: ${e.message}")
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            this@AuthActivity.verificationId = verificationId
            resendToken = token
            Log.d("Auth", "Verification Token: $verificationId")
            val number = binding.authViewModel?.contact?.value?.number!!
            startOTPTimer(number)
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String, activity: AuthActivity) {
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout
            .setActivity(activity)             // Activity (for callback binding)
            .setCallbacks(callbacks)           // Callbacks
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun startOTPTimer(phoneNumber: String) {
        if (counter > 2) {
            binding.tvMember.text = getString(R.string.retry_after_time)
            return
        }

        counter += 1
        binding.tvMember.visibility = View.VISIBLE

        countDownTimer?.cancel() // Cancel if already running

        countDownTimer = object : CountDownTimer(otpTimeout, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.tvMember.text = getString(R.string.resend_code_message, "$secondsLeft")
            }

            override fun onFinish() {
                binding.tvMember.text = getString((R.string.resending_code))
                resendVerificationCode(phoneNumber)
            }
        }.start()
    }

    private fun resendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken) // Reuse the token
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_auth)
        binding.authViewModel = ViewModelProvider(this) [AuthViewModel::class.java]

        viewModel = binding.authViewModel
        binding.authViewModel?.init()

        binding.authViewModel?.authType?.observe(this) {type ->
            Log.d("Auth", "Auth type set: $type")
            if (type == AuthType.SignIn) {
                showSignInView()
            } else if (type == AuthType.SignUp) {
                showSignUpView()
            }
        }
        binding.authViewModel?.pageType?.observe(this) { type ->
            Log.d("Auth", "Updated page type: $type")
            enableDisableButtons(false)
            pageType = type
            if (type != PageType.Mobile) {
                goToScreen(type)
            } else {
                if (!binding.authViewModel?.contact?.value?.number.isNullOrEmpty()) {
                    showMobileOtpView()
                    val number = binding.authViewModel?.contact?.value?.number!!
                    startPhoneNumberVerification(
                        number, this
                    )
                } else {
                    showConfirmDetailsView()
                }
            }
        }
        binding.authViewModel?.contact?.observe(this) {contact ->
            Log.d("Auth", "Contact updated: $contact")
        }
        binding.authViewModel?.error?.observe(this) { exception ->
            showSnackbar(
                binding.main,
                exception.message ?: exception.localizedMessage ?: exception.toString(),
                false
            )
        }

        showSignInView()
        binding.tvSignUpIn.setOnClickListener {
            authType = if (authType == AuthType.SignUp) {
                AuthType.SignIn
            } else {
                AuthType.SignUp
            }
            binding.authViewModel?.updateAuthType(authType)
        }
        binding.btnSignUpIn.setOnClickListener {
            hideKeyboard(binding.main)
            val name = binding.etName.text.toString().trim()
            val number = binding.etNumber.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val otp = binding.etOtp.text.toString().trim()
            var isValid = true

            if ((authType == AuthType.SignUp || pageType == PageType.Mobile) && name.isEmpty()) {
                binding.etName.error = "Invalid Name"
                binding.etlName.error = "Invalid Name"
                isValid = false
            }
            if ((authType == AuthType.SignUp || pageType == PageType.Mobile) && number.isEmpty()) {
                binding.etNumber.error = "Invalid Number"
                binding.etlNumber.error = "Invalid Number"
                isValid = false
            }
            if (email.isEmpty()) {
                binding.etEmail.error = "Invalid Email"
                binding.etlEmail.error = "Invalid Email"
                isValid = false
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Invalid Password"
                binding.etlPassword.error = "Invalid Password"
                isValid = false
            }
            if (authType == AuthType.SignUp && password != confirmPassword) {
                Log.d("Auth", "Password: $password | $confirmPassword")
                binding.etConfirmPassword.error = "Password does not match"
                binding.etlConfirmPassword.error = "Password does not match"
                isValid = false
            }
            if (pageType == PageType.Mobile && otp.isEmpty() && otp.length != 6) {
                if (!binding.authViewModel?.contact?.value?.number.isNullOrEmpty()) {
                    binding.etOtp.error = "Invalid OTP"
                    binding.etlOtp.error = "Invalid OTP"
                    isValid = false
                }
            }

            Log.d("Auth", "Validation: $isValid || $authType || $pageType")
            if (isValid && pageType != PageType.Mobile) {
                enableDisableButtons(true)
                binding.authViewModel?.userSignInUp(name, email, password)
            } else if (isValid && pageType == PageType.Mobile && verificationId.isEmpty()) {
                enableDisableButtons(true)
                this@AuthActivity.otp = otp
                Log.d("Auth", "Mobile Verification triggered..")
                binding.authViewModel?.createContact(name, number)
                binding.authViewModel?.updatePageType(PageType.Mobile)
            } else if (isValid && pageType == PageType.Mobile && verificationId.isNotEmpty()) {
                enableDisableButtons(true)
                Log.d("Auth", "Mobile Verification Code triggered..")
                viewModel?.verifyCode(otp, verificationId)
            }
        }
    }

    private fun enableDisableButtons(hide: Boolean) {
        binding.btnSignUpIn.visibility = if (hide) View.GONE else View.VISIBLE
        binding.tvSignUpIn.visibility = if (hide) View.GONE else View.VISIBLE
        binding.pbLoading.visibility = if (hide) View.VISIBLE else View.GONE
        binding.tvMember.visibility = if (hide) View.GONE else View.VISIBLE
    }

    private fun showSignInView() {
        binding.etName.visibility = View.GONE
        binding.etlName.visibility = View.GONE
        binding.etNumber.visibility = View.GONE
        binding.etlNumber.visibility = View.GONE
        binding.etConfirmPassword.visibility = View.GONE
        binding.etlConfirmPassword.visibility = View.GONE
        binding.etOtp.visibility = View.GONE
        binding.pbLoading.visibility = View.GONE
        binding.btnSignUpIn.visibility = View.VISIBLE
        binding.tvMember.visibility = View.VISIBLE
        binding.tvSignUpIn.visibility = View.VISIBLE
        binding.btnSignUpIn.text = getString(R.string.sign_in)
        binding.tvSignUpIn.text = getString(R.string.sign_up)
        binding.tvMember.text = getString(R.string.new_member)
    }

    private fun showSignUpView() {
        binding.etName.visibility = View.VISIBLE
        binding.etlName.visibility = View.VISIBLE
        binding.etNumber.visibility = View.VISIBLE
        binding.etlNumber.visibility = View.VISIBLE
        binding.etConfirmPassword.visibility = View.VISIBLE
        binding.etlConfirmPassword.visibility = View.VISIBLE
        binding.etOtp.visibility = View.GONE
        binding.pbLoading.visibility = View.GONE
        binding.btnSignUpIn.visibility = View.VISIBLE
        binding.tvMember.visibility = View.VISIBLE
        binding.tvSignUpIn.visibility = View.VISIBLE
        binding.btnSignUpIn.text = getString(R.string.sign_up)
        binding.tvSignUpIn.text = getString(R.string.sign_in)
        binding.tvMember.text = getString(R.string.already_a_member)
    }

    private fun showMobileOtpView() {
        binding.etName.visibility = View.GONE
        binding.etNumber.visibility = View.GONE
        binding.etPassword.visibility = View.GONE
        binding.etEmail.visibility = View.GONE
        binding.etConfirmPassword.visibility = View.GONE
        binding.etlName.visibility = View.GONE
        binding.etlNumber.visibility = View.GONE
        binding.etlPassword.visibility = View.GONE
        binding.etlEmail.visibility = View.GONE
        binding.etlConfirmPassword.visibility = View.GONE
        binding.tvSignUpIn.visibility = View.GONE
        binding.tvMember.visibility = View.GONE
        binding.etlOtp.visibility = View.VISIBLE
        binding.etOtp.visibility = View.VISIBLE
        binding.btnSignUpIn.text = getString(R.string.verify)
        binding.tvConfirmDetails.visibility = View.VISIBLE
        binding.tvConfirmDetails.text = getString(R.string.mobile_verification)
    }

    private fun showConfirmDetailsView() {
        binding.etName.visibility = View.VISIBLE
        binding.etlName.visibility = View.VISIBLE
        binding.etNumber.visibility = View.VISIBLE
        binding.etlNumber.visibility = View.VISIBLE
        binding.etlEmail.visibility = View.GONE
        binding.etlPassword.visibility = View.GONE
        binding.etlEmail.visibility = View.GONE
        binding.etlConfirmPassword.visibility = View.GONE
        binding.etlOtp.visibility = View.GONE
        binding.btnSignUpIn.text = getString(R.string.save)
        binding.tvConfirmDetails.visibility = View.VISIBLE
        binding.tvConfirmDetails.text = getString(R.string.confirm_details)
    }

    private fun goToScreen(pageType: PageType) {
        Log.d("Auth", "Navigating to page: $pageType")
        if (pageType == PageType.Home) {
            Log.d("Auth", "Navigating to page: $pageType")
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("Name", binding.authViewModel?.contact?.value?.name)
            intent.putExtra("Number", binding.authViewModel?.contact?.value?.number)
            startActivity(intent)
        } else {
            Log.d("Auth", "Navigating to page: $pageType")
            startActivity(Intent(this, AuthActivity::class.java))
        }
        finish()
    }
}