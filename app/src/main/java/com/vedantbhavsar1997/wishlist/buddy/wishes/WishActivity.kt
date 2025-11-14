package com.vedantbhavsar1997.wishlist.buddy.wishes

import android.app.AlertDialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.vedantbhavsar1997.wishlist.buddy.R
import com.vedantbhavsar1997.wishlist.buddy.databinding.ActivityWishBinding
import com.vedantbhavsar1997.wishlist.buddy.home.AdapterHome
import com.vedantbhavsar1997.wishlist.buddy.model.Group
import com.vedantbhavsar1997.wishlist.buddy.model.Wish

class WishActivity : AppCompatActivity(), AdapterWish.WishCompletionListener {
    private lateinit var binding: ActivityWishBinding
    private var viewModel: WishViewModel? = null
    private lateinit var adapterWish: AdapterWish

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_wish)
        binding.wishViewModel = ViewModelProvider(this) [WishViewModel::class.java]
        viewModel = binding.wishViewModel

        viewModel?.createContact(
            intent.getStringExtra("Name") ?: "",
            intent.getStringExtra("Number") ?: ""
        )
        viewModel?.createGroup(
            intent.getStringExtra("GroupName") ?: "",
            intent.getStringExtra("CreatedByNumber") ?: "",
            intent.getStringExtra("CreatedByName") ?: ""
        )

        binding.tvTitle.text = viewModel?.group?.value?.groupName
        adapterWish = AdapterWish(this)
        binding.rvWishes.layoutManager = LinearLayoutManager(this)
        binding.rvWishes.adapter = adapterWish

        binding.wishViewModel?.wishes?.observe(this) {wishes ->
            adapterWish.submitList(wishes)
        }
        binding.fabAddWish.setOnClickListener {
            val viewGroup: ViewGroup? = null
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_create_wish, viewGroup)
            builder.setView(view)
            val dialog = builder.create()
            val btnAdd: Button = view.findViewById(R.id.btnAdd)
            val etWishName: TextInputEditText = view.findViewById(R.id.etWishName)
            btnAdd.setOnClickListener {
                val groupName: String = etWishName.text.toString().trim()
                if (groupName.isNotBlank()) {
                    viewModel?.addWishToList(
                        Wish(
                            groupName,
                            viewModel?.contact?.value?.name ?: "",
                            viewModel?.contact?.value?.number ?: "",
                            false,
                            "", ""
                        )
                    )
                    dialog.dismiss()
                }
            }
            dialog.show()
        }
    }

    override fun onCompletion(wish: Wish) {
        val updateWish = Wish(
            wish.wish, wish.addedByName, wish.addedByNumber,
            !wish.isCompleted,
            viewModel?.contact?.value?.name,
            viewModel?.contact?.value?.number
        )
        viewModel?.updateWishList(updateWish)
    }

    override fun onResume() {
        super.onResume()
        viewModel?.init()
    }

    override fun onPause() {
        super.onPause()
        viewModel?.removeListener()
    }
}