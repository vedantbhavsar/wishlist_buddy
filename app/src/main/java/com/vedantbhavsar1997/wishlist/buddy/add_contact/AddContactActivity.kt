package com.vedantbhavsar1997.wishlist.buddy.add_contact

import android.app.AlertDialog
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.vedantbhavsar1997.wishlist.buddy.R
import com.vedantbhavsar1997.wishlist.buddy.databinding.ActivityAddContactBinding
import com.vedantbhavsar1997.wishlist.buddy.model.Contact
import com.vedantbhavsar1997.wishlist.buddy.model.Group

class AddContactActivity : AppCompatActivity(), AdapterContact.ContactSelectionListener {
    private lateinit var binding: ActivityAddContactBinding
    private var viewModel: AddContactViewModel? = null
    private lateinit var adapterContact: AdapterContact

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_contact)
        binding.addContactViewModel = ViewModelProvider(this)[AddContactViewModel::class.java]
        viewModel = binding.addContactViewModel
        viewModel?.init()
        viewModel?.createContact(
            intent.getStringExtra("Name") ?: "",
            intent.getStringExtra("Number") ?: ""
        )

        loadContacts()

        adapterContact = AdapterContact(this)
        binding.rvContacts.layoutManager = LinearLayoutManager(this)
        binding.rvContacts.adapter = adapterContact

        viewModel?.contacts?.observe(this) { contactList ->
            if (contactList.isNullOrEmpty()) return@observe
            adapterContact.submitList(contactList)
        }
        viewModel?.selectedContacts?.observe(this) { contactList ->
            if (contactList.isNullOrEmpty()) {
                binding.tvSelectedContacts.visibility = View.GONE
                binding.btnCreate.visibility = View.GONE
            } else if (contactList.size > 1) {
                binding.tvSelectedContacts.text = contactList.toString()
                    .replace("[", "").replace("]", "")
                binding.tvSelectedContacts.visibility = View.VISIBLE
                binding.btnCreate.visibility = View.VISIBLE
            }
        }

        binding.etSearch.addTextChangedListener {
            viewModel?.filterContacts(it.toString())
        }
        binding.btnCreate.setOnClickListener {
            val viewGroup: ViewGroup? = null
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_create_group, viewGroup)
            builder.setView(view)
            val dialog = builder.create()
            val btnCreate: Button = view.findViewById(R.id.btnCreate)
            val etGroupName: TextInputEditText = view.findViewById(R.id.etGroupName)
            btnCreate.setOnClickListener {
                val groupName: String = etGroupName.text.toString().trim()
                if (groupName.isNotBlank()) {
                    viewModel?.createGroupAndAddToDbs(
                        Group(
                            groupName,
                            binding.addContactViewModel?.contact?.value?.name!!,
                            binding.addContactViewModel?.contact?.value?.number!!
                        )
                    )
                    dialog.dismiss()
                    finish()
                }
            }
            dialog.show()
        }
    }

    override fun onSelect(contact: Contact) {
        viewModel?.updateContactSelection(contact)
    }

    private fun loadContacts() {
        val contactsList = mutableListOf<Contact>()

        // Access the Contacts content provider
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )

        cursor?.use {
            // Check if cursor contains data
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex).trim().replace(" ", "")
                val index = contactsList.indexOfFirst { e ->
                    e.name == name || e.number == number
                }
                if (index == -1) {
                    contactsList.add(
                        Contact(
                            name.trim(),
                            number,
                        )
                    )
                }
            }
        }

        // Now you have the contacts list, you can use it as needed
        viewModel?.storeContacts(contactsList)
    }
}