package com.triplicity.printitnow.ui.prints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.triplicity.printitnow.R
import com.triplicity.printitnow.entity.Print
import com.triplicity.printitnow.ui.profile.ProfileViewModel

class PrintsFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var adapter: PrintAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_prints, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)

        val printsRef = Firebase.firestore.collection("prints")
        val query = printsRef.whereEqualTo("user", viewModel.uid).orderBy("createdOn", Query.Direction.DESCENDING)
        val options: FirestoreRecyclerOptions<Print> = FirestoreRecyclerOptions.Builder<Print>()
            .setQuery(query, Print::class.java).build()

        adapter = PrintAdapter(options, requireContext())

        recyclerView = root.findViewById(R.id.rvPrints)
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
