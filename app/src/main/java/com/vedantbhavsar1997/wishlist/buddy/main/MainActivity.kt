package com.vedantbhavsar1997.wishlist.buddy.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.vedantbhavsar1997.wishlist.buddy.R
import com.vedantbhavsar1997.wishlist.buddy.auth.AuthActivity
import com.vedantbhavsar1997.wishlist.buddy.base.PageType
import com.vedantbhavsar1997.wishlist.buddy.databinding.ActivityMainBinding
import com.vedantbhavsar1997.wishlist.buddy.home.HomeActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var viewModel: MainViewModel? = null

    private val requestReadContactPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                viewModel?.init()
            } else {
                // Explain to the user that the feature is unavailable because
                // the feature requires a permission that the user denied.
                Toast.makeText(this, "Contact access denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel = binding.mainViewModel

        binding.mainViewModel?.pageType?.observe(this) { type ->
            goToScreen(type)
        }

        binding.btnContactAccessRequest.setOnClickListener {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestReadContactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        checkForContactPermission()
    }

    /***
     * Check for contacts access permission is granted or not.
     * If not request for access then load contact.
     * If request granted load contacts.
     */
    private fun checkForContactPermission() {
        // Check if the permission is already granted
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            viewModel?.init()
        }
    }

    private fun goToScreen(pageType: PageType) {
        if (pageType == PageType.Home) {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("Name", binding.mainViewModel?.contact?.value?.name)
            intent.putExtra("Number", binding.mainViewModel?.contact?.value?.number)
            startActivity(intent)
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
        }
        finish()
    }
}