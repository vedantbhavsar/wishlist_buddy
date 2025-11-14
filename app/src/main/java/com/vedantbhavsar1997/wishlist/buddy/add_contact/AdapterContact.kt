package com.vedantbhavsar1997.wishlist.buddy.add_contact

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vedantbhavsar1997.wishlist.buddy.R
import com.vedantbhavsar1997.wishlist.buddy.model.Contact

class AdapterContact(
    private val contactSelectionListener: ContactSelectionListener
) : RecyclerView.Adapter<AdapterContact.AdapterContactViewHolder>() {
    private var contactList = listOf<Contact>()

    interface ContactSelectionListener {
        fun onSelect(contact: Contact)
    }

    inner class AdapterContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvName)
        val number: TextView = itemView.findViewById(R.id.tvNumber)
        val llContactTile: LinearLayout = itemView.findViewById(R.id.llContactTile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return AdapterContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.name.text = contact.name
        holder.number.text = contact.number
        holder.llContactTile.setOnClickListener {
            contactSelectionListener.onSelect(contact)
        }
    }

    override fun getItemCount() = contactList.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<Contact>) {
        contactList = newList
        notifyDataSetChanged()
    }
}