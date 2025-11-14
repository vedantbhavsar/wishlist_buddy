package com.vedantbhavsar1997.wishlist.buddy.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vedantbhavsar1997.wishlist.buddy.R
import com.vedantbhavsar1997.wishlist.buddy.add_contact.AddContactActivity
import com.vedantbhavsar1997.wishlist.buddy.databinding.ActivityHomeBinding
import com.vedantbhavsar1997.wishlist.buddy.model.Group
import com.vedantbhavsar1997.wishlist.buddy.wishes.WishActivity

class HomeActivity : AppCompatActivity(), AdapterHome.GroupSelectionListener {
    private lateinit var binding: ActivityHomeBinding
    private var viewModel: HomeViewModel? = null
    private lateinit var adapterHome: AdapterHome

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.homeViewModel = ViewModelProvider(this) [HomeViewModel::class.java]
        viewModel = binding.homeViewModel

        Log.d("Home", applicationContext.packageName)

        viewModel?.createContact(
            intent.getStringExtra("Name") ?: "",
            intent.getStringExtra("Number") ?: ""
        )
        viewModel?.getUserContact()
        binding.homeViewModel?.init()

        adapterHome = AdapterHome(this)
        binding.rvGroups.layoutManager = LinearLayoutManager(this)
        binding.rvGroups.adapter = adapterHome

        binding.homeViewModel?.groups?.observe(this) {groups ->
            adapterHome.submitList(groups)
        }
        binding.fabCreateGroup.setOnClickListener {
            goToAddContact()
        }
    }

    override fun onClick(group: Group) {
        val intent = Intent(this, WishActivity::class.java)
        intent.putExtra("Name", binding.homeViewModel?.contact?.value?.name)
        intent.putExtra("Number", binding.homeViewModel?.contact?.value?.number)
        intent.putExtra("GroupName", group.groupName)
        intent.putExtra("CreatedByNumber", group.createdByNumber)
        intent.putExtra("CreatedByName", group.createdByName)
        startActivity(intent)
    }

    private fun goToAddContact() {
        val intent = Intent(this, AddContactActivity::class.java)
        intent.putExtra("Name", binding.homeViewModel?.contact?.value?.name)
        intent.putExtra("Number", binding.homeViewModel?.contact?.value?.number)
        startActivity(intent)
    }
}