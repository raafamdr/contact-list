package com.rafael.contact_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rafael.contact_list.data.Contact
import com.rafael.contact_list.databinding.FragmentContactDetailBinding

class ContactDetailFragment : Fragment() {
    private var _binding: FragmentContactDetailBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: ContactDetailFragmentArgs by navArgs()
    private val viewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory(
            (activity?.application as ContactsApplication).database.contactDao()
        )
    }
    lateinit var contact: Contact

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.topAppBar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit -> {
                        editContact()
                        true
                    }

                    R.id.delete -> {
                        showConfirmationDialog()
                        true
                    }

                    else -> false
                }
            }
        }

        val id = navigationArgs.itemId
        viewModel.retrieveContact(id).observe(this.viewLifecycleOwner) { selectedContact ->
            contact = selectedContact
            bind(contact)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun bind(contact: Contact) {
        binding.apply {
            textContactName.text =
                resources.getString(R.string.contact_name, contact.firstName, contact.lastName)
            textPhone.text = contact.phone
            textAddress.text = contact.address
            textLocation.text =
                resources.getString(R.string.location, contact.city, contact.area, contact.zip)
        }
    }

    private fun editContact() {
        val action =
            ContactDetailFragmentDirections.actionContactDetailFragmentToAddContactFragment(
                contact.id
            )
        this.findNavController().navigate(action)
    }

    private fun deleteContact() {
        viewModel.deleteContact(contact)
        findNavController().navigateUp()
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(
            requireContext(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setIcon(R.drawable.ic_delete)
            .setTitle(resources.getString(R.string.delete_contact))
            .setMessage(resources.getString(R.string.delete_supporting_text))
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
            }
            .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                deleteContact()
            }
            .show()
    }

}