package com.vedantbhavsar1997.wishlist.buddy.add_contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.vedantbhavsar1997.wishlist.buddy.base.AppViewModel
import com.vedantbhavsar1997.wishlist.buddy.model.Contact
import com.vedantbhavsar1997.wishlist.buddy.model.Group

class AddContactViewModel : AppViewModel() {

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts
    private val _selectedContacts = MutableLiveData<List<Contact>>()
    val selectedContacts: LiveData<List<Contact>> = _selectedContacts
    private var _allContacts = listOf<Contact>()

    override fun init() {
        _selectedContacts.value = mutableListOf()
    }

    fun storeContacts(contacts: List<Contact>) {
        _contacts.postValue(contacts)
        _allContacts = contacts
    }

    fun filterContacts(filterString: String) {
        if (filterString.trim().isEmpty()) {
            _contacts.postValue(_allContacts)
        }
        val filteredContacts = _allContacts.filter {
            it.name.lowercase().contains(filterString.lowercase())
        }
        _contacts.postValue(filteredContacts)
    }

    fun updateContactSelection(contact: Contact) {
        val currentList = _selectedContacts.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst {
            it.number == contact.number || it.name == contact.name
        }
        if (index == -1) {
            currentList.add(contact)
            _selectedContacts.postValue(currentList)
        }
    }

    fun createGroupAndAddToDbs(group: Group) {
        FirebaseFirestore.getInstance().collection("User Contact")
            .document(contact.value?.number ?: "")
            .collection("Group")
            .document(group.groupName)
            .set(group)
        _selectedContacts.value?.forEach {
            FirebaseDatabase.getInstance().getReference(group.groupName)
                .child(it.number).setValue(it)
            val groupDoc = FirebaseFirestore.getInstance().collection("User Contact")
                .document(it.number)
            groupDoc.set(it)
            groupDoc.collection("Group")
                .document(group.groupName)
                .set(group)
        }
    }
}
