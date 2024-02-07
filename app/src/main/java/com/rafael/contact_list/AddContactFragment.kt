package com.rafael.contact_list

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.Coil
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
            binding.topAppBar.title = getString(R.string.title_edit_contact)
            viewModel.retrieveContact(id).observe(this.viewLifecycleOwner) { selectedContact ->
                contact = selectedContact
                bind(contact)
            }
        } else {
            binding.btnSave.setOnClickListener {
                addNewContact()
            }
        }

        viewModel.imagePath.observe(this.viewLifecycleOwner) { imagePath ->
            if (imagePath != null) {
                val request = ImageRequest.Builder(requireContext())
                    .data(imagePath)
                    .error(ContextCompat.getDrawable(requireContext(), R.drawable.ic_account))
                    .crossfade(600)
                    .target(createImageTarget())
                    .build()
                Coil.imageLoader(requireContext()).enqueue(request)
            } else {
                binding.imgAccount.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_account
                    )
                )
            }
        }

        binding.apply {
            shapeCamera.setOnClickListener {
                findNavController().navigate(R.id.action_addContactFragment_to_modalBottomSheet)
            }
            imgAccount.setOnClickListener {
                findNavController().navigate(R.id.action_addContactFragment_to_modalBottomSheet)
            }
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
        viewModel.updateImagePath(null)
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
                viewModel.imagePath.value,
            )
            val action = AddContactFragmentDirections.actionAddContactFragmentToHomeFragment()
            findNavController().navigate(action)
            Toast.makeText(requireContext(), R.string.toast_contact_added, Toast.LENGTH_SHORT).show()
        } else {
            validateField(binding.labelFirstName, binding.textFirstName)
            validateField(binding.labelLastName, binding.textLastName)
            validateField(binding.labelPhone, binding.textPhone)

            Toast.makeText(requireContext(), R.string.toast_fill_all_required_fields, Toast.LENGTH_SHORT)
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
                viewModel.imagePath.value
            )
            val action = AddContactFragmentDirections.actionAddContactFragmentToHomeFragment()
            findNavController().navigate(action)
            Toast.makeText(requireContext(), R.string.toast_contact_updated, Toast.LENGTH_SHORT).show()
        } else {
            validateField(binding.labelFirstName, binding.textFirstName)
            validateField(binding.labelLastName, binding.textLastName)
            validateField(binding.labelPhone, binding.textPhone)

            Toast.makeText(requireContext(), R.string.toast_fill_all_required_fields, Toast.LENGTH_SHORT)
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
            viewModel.updateImagePath(contact.imagePath)
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
            Toast.makeText(requireContext(), R.string.toast_image_not_available, Toast.LENGTH_SHORT)
                .show()
        }
    }
}