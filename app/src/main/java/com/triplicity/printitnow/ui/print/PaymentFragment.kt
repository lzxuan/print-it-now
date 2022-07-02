package com.triplicity.printitnow.ui.print

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.triplicity.printitnow.R
import com.triplicity.printitnow.databinding.FragmentPaymentBinding

class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentBinding
    private lateinit var viewModel: PrintViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_payment, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(PrintViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        val editCard = binding.editCard.editText!!
        val editCardListener = MaskedTextChangedListener("[0000]{ }[0000]{ }[0000]{ }[0000]", editCard)
        editCard.addTextChangedListener(editCardListener)
        editCard.onFocusChangeListener = editCardListener

        val editExp = binding.editExp.editText!!
        val editExpListener = MaskedTextChangedListener("[00]{/}[00]", editExp)
        editExp.addTextChangedListener(editExpListener)
        editExp.onFocusChangeListener = editExpListener

        val editCVV = binding.editCVV.editText!!
        val editCVVListener = MaskedTextChangedListener("[000]", editCVV)
        editCVV.addTextChangedListener(editCVVListener)
        editCVV.onFocusChangeListener = editCVVListener

        binding.buttonConfirmPay.setOnClickListener { pay() }

        viewModel.loaded.observe(viewLifecycleOwner, Observer {
            if (it) loaded() else loading()
        })

        viewModel.paid.observe(viewLifecycleOwner, Observer {
            if (it) {
                Snackbar.make(requireView(), R.string.printing_sent, Snackbar.LENGTH_SHORT).show()
                val action = PaymentFragmentDirections.actionPrintPaymentToNavPrints()
                NavHostFragment.findNavController(this).navigate(action)
            }
        })

        return binding.root
    }

    private fun loading() {
        binding.groupPay.isEnabled = false
        binding.loading.visibility = View.VISIBLE
        binding.buttonConfirmPay.isEnabled = false
        if (!viewModel.priced) {
            binding.textPrice.setText(R.string.getting_price)
        }
    }

    private fun loaded() {
        binding.groupPay.isEnabled = true
        binding.loading.visibility = View.GONE
        binding.buttonConfirmPay.isEnabled = true
        if (!viewModel.priced) {
            binding.textPrice.text = getString(R.string.price, viewModel.price)
        }
    }

    private fun isValid(): Boolean {
        val nameSet = !binding.editName.editText?.text.isNullOrBlank()
        val cardSet = !binding.editCard.editText?.text.isNullOrBlank()
        val expSet = !binding.editExp.editText?.text.isNullOrBlank()
        val cvvSet = !binding.editCVV.editText?.text.isNullOrBlank()

        val imm =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        if (!nameSet) {
            binding.editName.editText?.requestFocus()
            imm.showSoftInput(binding.editName.editText, 0)
        } else if (!cardSet) {
            binding.editCard.editText?.requestFocus()
            imm.showSoftInput(binding.editCard.editText, 0)
        } else if (!expSet) {
            binding.editExp.editText?.requestFocus()
            imm.showSoftInput(binding.editExp.editText, 0)
        } else if (!cvvSet) {
            binding.editCVV.editText?.requestFocus()
            imm.showSoftInput(binding.editCVV.editText, 0)
        } else {
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            return true
        }

        return false
    }

    private fun pay() {
        if (isValid()) {
            viewModel.pay(requireContext())
        } else {
            Snackbar.make(requireView(), R.string.validation_error, Snackbar.LENGTH_SHORT).show()
        }
    }
}