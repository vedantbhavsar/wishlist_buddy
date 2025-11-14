package com.vedantbhavsar1997.wishlist.buddy.wishes

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vedantbhavsar1997.wishlist.buddy.R
import com.vedantbhavsar1997.wishlist.buddy.model.Wish

class AdapterWish(
    private val wishCompletionListener: WishCompletionListener
) : RecyclerView.Adapter<AdapterWish.WishViewHolder>() {
    private var wishList = listOf<Wish>()

    interface WishCompletionListener {
        fun onCompletion(wish: Wish)
    }

    inner class WishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbComplete: CheckBox = itemView.findViewById(R.id.cbComplete)
        val tvWish: TextView = itemView.findViewById(R.id.tvWish)
        val tvCreatedBy: TextView = itemView.findViewById(R.id.tvCreatedBy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wish, parent, false)
        return WishViewHolder(view)
    }

    override fun getItemCount(): Int = wishList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: WishViewHolder, position: Int) {
        val wish = wishList[position]
        holder.tvWish.text = wish.wish
        if (wish.isCompleted) {
            holder.tvWish.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            holder.tvCreatedBy.text = "Completed by: ${wish.completedByName}"
            holder.cbComplete.isChecked = wish.isCompleted
        } else {
            holder.tvWish.apply {
                paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            holder.tvCreatedBy.text = "Added by: ${wish.addedByName}"
            holder.cbComplete.isChecked = wish.isCompleted
        }
        holder.cbComplete.setOnClickListener {
            wishCompletionListener.onCompletion(wish)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<Wish>) {
        wishList = newList
        notifyDataSetChanged()
    }
}