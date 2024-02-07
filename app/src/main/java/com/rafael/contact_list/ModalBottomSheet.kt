package com.rafael.contact_list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rafael.contact_list.databinding.ModalBottomSheetContentBinding

class ModalBottomSheet : BottomSheetDialogFragment() {
    private var _binding: ModalBottomSheetContentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory(
            (activity?.application as ContactsApplication).database.contactDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ModalBottomSheetContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.imagePath.observe(this.viewLifecycleOwner) {
            binding.imgDelete.visibility = if (it != null) View.VISIBLE else View.GONE
        }

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    requireContext().contentResolver.takePersistableUriPermission(uri, flag)
                    viewModel.updateImagePath(uri.toString())
                    Log.d("PhotoPicker", "Selected URI: $uri")
                    dismiss()
                } else {
                    viewModel.updateImagePath(null)
                    Log.d("PhotoPicker", "No media selected")
                    dismiss()
                }
            }

        binding.shapeCamera.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.imgDelete.setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(resources.getString(R.string.dialog_title_remove))
            .setMessage(resources.getString(R.string.dialog_supporting_text_remove))
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                dismiss()
            }
            .setPositiveButton(resources.getString(R.string.dialog_action_remove)) { _, _ ->
                viewModel.updateImagePath(null)
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.toast_contact_photo_removed),
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
            .show()
    }
}
