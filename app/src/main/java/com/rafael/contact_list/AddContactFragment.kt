package com.rafael.contact_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
                binding.textZip.text.toString()
            )
            val action = AddContactFragmentDirections.actionAddContactFragmentToHomeFragment()
            findNavController().navigate(action)
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
                this.binding.textZip.text.toString()
            )
            val action = AddContactFragmentDirections.actionAddContactFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }

    private fun isEntryValid(): Boolean {
        return viewModel.areFieldsNotBlank(
            binding.textFirstName.text.toString(),
            binding.textLastName.text.toString(),
            binding.textPhone.text.toString(),
        )
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
            btnSave.setOnClickListener {
                updateContact()
            }
        }
    }
}