package com.triplicity.printitnow.ui.profile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.triplicity.printitnow.R
import com.triplicity.printitnow.databinding.FragmentProfileBinding
import com.triplicity.printitnow.entity.User


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var currentUser: FirebaseUser
    private lateinit var userRef: DocumentReference

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_profile, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        currentUser = FirebaseAuth.getInstance().currentUser!!
        userRef = Firebase.firestore.collection("users").document(viewModel.uid)

        val editPhone = binding.editPhone.editText!!
        val editPhoneListener = MaskedTextChangedListener("[000]{-}[00000009]", editPhone)
        editPhone.addTextChangedListener(editPhoneListener)
        editPhone.onFocusChangeListener = editPhoneListener

        binding.buttonUpdate.setOnClickListener { updateProfile() }

        return binding.root
    }

    private fun updateProfile() {
        val name = binding.editName.editText?.text.toString()
        val phone = binding.editPhone.editText?.text.toString()

        val oldUser = viewModel.user.value
        val user = User(name, oldUser!!.email, phone)

        val imm =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)

        val update = UserProfileChangeRequest.Builder()
            .setDisplayName(user.name).build()
        currentUser.updateProfile(update).addOnSuccessListener {
            userRef.update(mapOf(
                "name" to user.name,
                "phoneNumber" to user.phone
            )).addOnSuccessListener {
                viewModel.setUser(user)
                Snackbar.make(requireView(), R.string.profile_updated, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
