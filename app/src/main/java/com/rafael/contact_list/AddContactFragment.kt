package com.rafael.contact_list

import android.content.Intent
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.Coil
import coil.load
import coil.request.ImageRequest
import coil.transition.TransitionTarget
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.rafael.contact_list.data.Contact
import com.rafael.contact_list.databinding.FragmentAddContactBinding

class AddContactFragment : Fragment() {
    private var _binding: FragmentAddContactBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: AddContactFragmentArgs by navArgs()
    private val viewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory(
            (activity?.application as ContactsApplication).database.contactDao()
        )
    }
    lateinit var contact: Contact
    private var imagePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = navigationArgs.itemId
        if (id > 0) {
            binding.topAppBar.title = getString(R.string.edit_contact)
            viewModel.retrieveContact(id).observe(this.viewLifecycleOwner) { selectedContact ->
                contact = selectedContact
                bind(contact)
            }
        } else {
            binding.btnSave.setOnClickListener {
                addNewContact()
            }
        }

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    requireContext().contentResolver.takePersistableUriPermission(uri, flag)

                    binding.imgAccount.load(uri)
                    this.imagePath = uri.toString()

                    Log.d("PhotoPicker", "Selected URI: $uri")
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }

        binding.shapeCamera.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addNewContact() {
        if (isEntryValid()) {
            viewModel.addNewContact(
                binding.textFirstName.text.toString(),
                binding.textLastName.text.toString(),
                binding.textPhone.text.toString(),
                binding.textAddress.text.toString(),
                binding.textCity.text.toString(),
                binding.textArea.text.toString(),
                binding.textZip.text.toString(),
                imagePath
            )
            val action = AddContactFragmentDirections.actionAddContactFragmentToHomeFragment()
            findNavController().navigate(action)
            Toast.makeText(requireContext(), R.string.contact_added, Toast.LENGTH_SHORT).show()
        } else {
            validateField(binding.labelFirstName, binding.textFirstName)
            validateField(binding.labelLastName, binding.textLastName)
            validateField(binding.labelPhone, binding.textPhone)

            Toast.makeText(requireContext(), R.string.fill_all_required_fields, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updateContact() {
        if (isEntryValid()) {
            viewModel.updateContact(
                this.navigationArgs.itemId,
                this.binding.textFirstName.text.toString(),
                this.binding.textLastName.text.toString(),
                this.binding.textPhone.text.toString(),
                this.binding.textAddress.text.toString(),
                this.binding.textCity.text.toString(),
                this.binding.textArea.text.toString(),
                this.binding.textZip.text.toString(),
                imagePath
            )
            val action = AddContactFragmentDirections.actionAddContactFragmentToHomeFragment()
            findNavController().navigate(action)
            Toast.makeText(requireContext(), R.string.contact_updated, Toast.LENGTH_SHORT).show()
        } else {
            validateField(binding.labelFirstName, binding.textFirstName)
            validateField(binding.labelLastName, binding.textLastName)
            validateField(binding.labelPhone, binding.textPhone)

            Toast.makeText(requireContext(), R.string.fill_all_required_fields, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun isEntryValid(): Boolean {
        return viewModel.areFieldsNotBlank(
            binding.textFirstName.text.toString(),
            binding.textLastName.text.toString(),
            binding.textPhone.text.toString(),
        )
    }

    private fun validateField(textInputLayout: TextInputLayout, editText: TextInputEditText) {
        if (editText.text!!.isEmpty()) {
            textInputLayout.error = getString(R.string.required)
        } else {
            textInputLayout.error = null
        }
    }

    private fun bind(contact: Contact) {

        binding.apply {
            textFirstName.setText(contact.firstName, TextView.BufferType.SPANNABLE)
            textLastName.setText(contact.lastName, TextView.BufferType.SPANNABLE)
            textPhone.setText(contact.phone, TextView.BufferType.SPANNABLE)
            textAddress.setText(contact.address, TextView.BufferType.SPANNABLE)
            textCity.setText(contact.city, TextView.BufferType.SPANNABLE)
            textArea.setText(contact.area, TextView.BufferType.SPANNABLE)
            textZip.setText(contact.zip, TextView.BufferType.SPANNABLE)
            contact.imagePath?.let {
                if (it.isNotEmpty()) {
                    val request = ImageRequest.Builder(requireContext())
                        .data(it)
                        .error(ContextCompat.getDrawable(requireContext(), R.drawable.ic_account))
                        .crossfade(600)
                        .target(createImageTarget())
                        .build()
                    Coil.imageLoader(requireContext()).enqueue(request)
                    imagePath = contact.imagePath
                }
            }
            btnSave.setOnClickListener {
                updateContact()
            }
        }
    }

    private fun createImageTarget() = object : TransitionTarget {
        override val drawable get() = binding.imgAccount.drawable
        override val view get() = binding.imgAccount

        override fun onSuccess(result: Drawable) {
            binding.imgAccount.setImageDrawable(result)
            (result as? Animatable)?.start()
        }

        override fun onError(error: Drawable?) {
            binding.imgAccount.setImageDrawable(error)
            Toast.makeText(requireContext(), R.string.image_not_available, Toast.LENGTH_SHORT)
                .show()
        }
    }
}