package com.vedantbhavsar1997.wishlist.buddy.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.vedantbhavsar1997.wishlist.buddy.base.AppViewModel
import com.vedantbhavsar1997.wishlist.buddy.model.Group

class HomeViewModel : AppViewModel() {
    private val _groups = MutableLiveData<List<Group>>()
    val groups: LiveData<List<Group>> = _groups

    override fun init() {
        FirebaseFirestore.getInstance().collection("User Contact")
            .document(contact.value?.number ?: "")
            .collection("Group")
            .addSnapshotListener { value, error ->
                if (error == null) {
                    val groups = arrayListOf<Group>()
                    value.let {
                        it?.documents?.forEach { e ->
                            val data = e.data
                            val doc = Group(
                                data?.get("groupName").toString(),
                                data?.get("createdByName").toString(),
                                data?.get("createdByNumber").toString(),
                            )
                            groups.add(doc)
                        }
                    }
                    _groups.value = groups
                } else {
                    Log.e("Home", "Error while fetching groups: $error")
                }
            }
    }

    fun getUserContact() {
        Log.d("Home", "User Contact: ${contact.value}")
        val doc = FirebaseFirestore.getInstance().collection("User Contact")
            .document(contact.value?.number ?: "")
        doc.get().addOnCompleteListener { result ->
            if (!result.result.exists()) {
                contact.value?.let { doc.set(it) }
            }
        }
    }
}