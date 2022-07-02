package com.triplicity.printitnow.ui.about

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.triplicity.printitnow.R

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_about, container, false)

        val policy: TextView = root.findViewById(R.id.textPolicy)
        policy.movementMethod = LinkMovementMethod.getInstance()

        val terms: TextView = root.findViewById(R.id.textTerms)
        terms.movementMethod = LinkMovementMethod.getInstance()

        return root
    }
}