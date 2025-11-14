package com.vedantbhavsar1997.wishlist.buddy.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vedantbhavsar1997.wishlist.buddy.R
import com.vedantbhavsar1997.wishlist.buddy.model.Group

class AdapterHome(
    private val groupSelectionListener: GroupSelectionListener
) : RecyclerView.Adapter<AdapterHome.AdapterHomeViewHolder>() {
    private var groupList = listOf<Group>()

    interface GroupSelectionListener {
        fun onClick(group: Group)
    }

    inner class AdapterHomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupName: TextView = itemView.findViewById(R.id.tvGroupName)
        val createdBy: TextView = itemView.findViewById(R.id.tvCreatedBy)
        val createdByNumber: TextView = itemView.findViewById(R.id.tvCreatedByNumber)
        val llGroupTile: LinearLayout = itemView.findViewById(R.id.llGroupTile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterHomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return AdapterHomeViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AdapterHomeViewHolder, position: Int) {
        val group = groupList[position]
        holder.groupName.text = group.groupName
        holder.createdBy.text = "Created by: ${group.createdByName} "
        holder.createdByNumber.text = "(${group.createdByNumber})"
        holder.llGroupTile.setOnClickListener {
            groupSelectionListener.onClick(group)
        }
    }

    override fun getItemCount() = groupList.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<Group>) {
        groupList = newList
        notifyDataSetChanged()
    }
}