package com.vedantbhavsar1997.wishlist.buddy.wishes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.vedantbhavsar1997.wishlist.buddy.base.AppViewModel
import com.vedantbhavsar1997.wishlist.buddy.model.Group
import com.vedantbhavsar1997.wishlist.buddy.model.Wish

class WishViewModel : AppViewModel() {
    private val _group = MutableLiveData<Group>()
    val group: LiveData<Group> = _group
    private val _wishes = MutableLiveData<List<Wish>>()
    val wishes: LiveData<List<Wish>> = _wishes

    private val wishValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val wishlist = arrayListOf<Wish>()
            snapshot.children.forEach {
                val wish = it.getValue<Wish>()
                if (wish != null) {
                    wishlist.add(wish)
                }
                Log.d("WishTag", wish.toString())
            }
            _wishes.value = wishlist
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("WishTag", "Error while getting wish: $error")
        }
    }

    override fun init() {
        FirebaseDatabase.getInstance().getReference(group.value?.groupName!!)
            .child("Wishlist").addValueEventListener(wishValueListener)
    }

    fun createGroup(groupName: String, createdByName: String, createdByNumber: String) {
        _group.value = Group(
            groupName, createdByName, createdByNumber
        )
    }

    fun addWishToList(wish: Wish) {
        val wishes = if (_wishes.value.isNullOrEmpty())
            arrayListOf()
        else
            _wishes.value?.toMutableList()
        wishes?.add(wish)
        FirebaseDatabase.getInstance().getReference(group.value?.groupName!!)
            .child("Wishlist").setValue(wishes)
    }

    fun updateWishList(wish: Wish) {
        val wishes = if (_wishes.value.isNullOrEmpty())
            arrayListOf()
        else
            _wishes.value?.toMutableList()
        val index = wishes?.indexOfFirst {
            it.wish == wish.wish
        }
        index?.let {
            if (index != -1) {
                wishes.removeAt(index)
                wishes.add(index, wish)
            }
        }
        FirebaseDatabase.getInstance().getReference(group.value?.groupName!!)
            .child("Wishlist").setValue(wishes)
    }

    fun removeListener() {
        FirebaseDatabase.getInstance().getReference(group.value?.groupName!!)
            .child("Wishlist").removeEventListener(wishValueListener)
    }
}