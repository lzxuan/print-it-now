package com.triplicity.printitnow.ui.print

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.redmadrobot.inputmask.MaskedTextChangedListener.Companion.installOn
import com.redmadrobot.inputmask.MaskedTextChangedListener.ValueListener
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader
import com.triplicity.printitnow.R
import com.triplicity.printitnow.databinding.FragmentPrintBinding
import com.triplicity.printitnow.entity.FileInfo.FILE.FILE_TYPES_ALLOWED

class PrintFragment : Fragment() {

    private lateinit var binding: FragmentPrintBinding
    private lateinit var viewModelFactory: PrintViewModelFactory
    private lateinit var viewModel: PrintViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_print, container, false)

        viewModelFactory = PrintViewModelFactory(
            PrintFragmentArgs.fromBundle(requireArguments()).uid,
            PrintFragmentArgs.fromBundle(requireArguments()).shopId
        )

        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(PrintViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner

        val paperSizeOption = resources.getStringArray(R.array.paper_size_array)
        binding.spinnerPaperSize.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, paperSizeOption)
        binding.spinnerPaperSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.printConfig.paperSize = paperSizeOption[position]
            }
        }
        val paperSizePos = paperSizeOption.indexOf(viewModel.printConfig.paperSize)
        if (paperSizePos >= 0) binding.spinnerPaperSize.setSelection(paperSizePos)

        val colorOption = resources.getStringArray(R.array.color_array)
        binding.spinnerColor.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, colorOption)
        binding.spinnerColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.printConfig.color = position == 0
            }
        }
        if (!viewModel.printConfig.color) binding.spinnerColor.setSelection(1)

        val sideOption = resources.getStringArray(R.array.side_array)
        binding.spinnerTwoSided.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, sideOption)
        binding.spinnerTwoSided.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.printConfig.twoSided = position == 1
            }
        }
        if (viewModel.printConfig.twoSided) binding.spinnerTwoSided.setSelection(1)

        val editCopies = binding.editCopies.editText!!
        installOn(
            editCopies,
            "[099]",
            object : ValueListener {
                override fun onTextChanged(
                    maskFilled: Boolean,
                    extractedValue: String,
                    formattedValue: String
                ) {
                    val copies = formattedValue.toIntOrNull()
                    if (copies != null) viewModel.printConfig.copies = copies
                }
            }
        )
        binding.editCopies.editText!!.setText(viewModel.printConfig.copies.toString())

        PDFBoxResourceLoader.init(context)

        binding.buttonSelect.setOnClickListener { selectFiles() }
        binding.buttonCheckOut.setOnClickListener { checkOut() }

        viewModel.fileCount.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val chipCount = binding.chipGroupFileName.childCount

            if (chipCount < it) {
                for (i in chipCount until it) {
                    addFileNameChip(viewModel.files[i].name)
                }
                val pages = viewModel.pages
                binding.textPages.text = if (pages == 1) "$pages page" else "$pages pages"
            } else if (it == 0) {
                binding.chipGroupFileName.removeAllViews()
                binding.textPages.setText(R.string.no_file_chosen)
            }
        })

        viewModel.loaded.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) loaded() else loading()
        })

        return binding.root
    }

    private fun selectFiles() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .setType("*/*")
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            .putExtra(Intent.EXTRA_MIME_TYPES, FILE_TYPES_ALLOWED)

        try {
            startActivityForResult(
                Intent.createChooser(intent, resources.getString(R.string.select_files)),
                RC_SELECT_FILES
            )
        } catch (e: ActivityNotFoundException) {
            val string = resources.getString(R.string.install_file_manager)
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SELECT_FILES && resultCode == Activity.RESULT_OK) {
            viewModel.clearFiles()

            if (data != null) {
                if (data.clipData != null) {
                    viewModel.addFiles(requireContext(), data.clipData!!)
                } else {
                    viewModel.addFile(requireContext(), data.data!!)
                }
            }
        }
    }

    private fun addFileNameChip(fileName: String) {
        val chip = Chip(context)
        chip.text = fileName
        binding.chipGroupFileName.addView(chip)
    }

    private fun loading() {
        binding.buttonSelect.visibility = View.GONE
        binding.loading.visibility = View.VISIBLE
        binding.buttonCheckOut.isEnabled = false
    }

    private fun loaded() {
        binding.buttonSelect.visibility = View.VISIBLE
        binding.loading.visibility = View.GONE
        binding.buttonCheckOut.isEnabled = true
    }

    private fun checkOut() {
        if (viewModel.files.size > 0) {
            viewModel.requestPrice()
            val imm =
                requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            val action = PrintFragmentDirections.actionPrintPrintToPrintPayment()
            NavHostFragment.findNavController(this).navigate(action)
        } else {
            Snackbar.make(requireView(), R.string.select_at_least_1_file, Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val RC_SELECT_FILES = 2
    }
}