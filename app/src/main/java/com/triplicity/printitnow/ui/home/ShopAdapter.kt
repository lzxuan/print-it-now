package com.triplicity.printitnow.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.triplicity.printitnow.R
import com.triplicity.printitnow.entity.Shop
import com.triplicity.printitnow.ui.home.ShopAdapter.ShopHolder

class ShopAdapter(
    options: FirestoreRecyclerOptions<Shop>,
    private val home: HomeFragment,
    private val uid: String) :
    FirestoreRecyclerAdapter<Shop, ShopHolder>(options) {

    override fun onBindViewHolder(holder: ShopHolder, position: Int, model: Shop) {
        val shopId = this.snapshots.getSnapshot(position).id
        val storage = Firebase.storage.reference
        val imgRef = storage.child("shops/$shopId.png")

        Glide.with(home).load(imgRef).into(holder.shopImage)
        holder.shopName.text = model.name
        holder.shopAddress.text = model.address
        holder.shopDescription.text = model.description

        holder.itemView.setOnClickListener {
            nextFragment(shopId)
        }
        holder.shopButton.setOnClickListener {
            nextFragment(shopId)
        }
    }

    private fun nextFragment(shopId: String) {
        val action = HomeFragmentDirections.actionNavHomeToPrintPrint(uid, shopId)
        NavHostFragment.findNavController(home).navigate(action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.card_shop,
            parent,
            false
        )
        return ShopHolder(view)
    }

    inner class ShopHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var shopImage: ImageView = itemView.findViewById(R.id.shopImage)
        var shopName: TextView = itemView.findViewById(R.id.shopName)
        var shopAddress: TextView = itemView.findViewById(R.id.shopAddress)
        var shopDescription: TextView = itemView.findViewById(R.id.shopDescription)
        var shopButton: Button = itemView.findViewById(R.id.shopButton)
    }
}