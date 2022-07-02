package com.triplicity.printitnow.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.triplicity.printitnow.R
import com.triplicity.printitnow.entity.Shop

class HomeFragment : Fragment() {

    private lateinit var adapter: ShopAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val shopsRef = Firebase.firestore.collection("shops")
        val query = shopsRef.whereEqualTo("enabled", true)
        val options: FirestoreRecyclerOptions<Shop> = FirestoreRecyclerOptions.Builder<Shop>()
            .setQuery(query, Shop::class.java).build()

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        adapter = ShopAdapter(options, this, uid)

        recyclerView = root.findViewById(R.id.rvShop)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this.context)

        return root
    }

    override fun onStart() {
        super.onStart()
        recyclerView.adapter = adapter
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
