package com.rafael.contact_list

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.Coil
import coil.request.ImageRequest
import coil.transition.TransitionTarget
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
            textAddress.text = formatAddress(contact.address)
            textLocation.text = formatLocation(contact.city, contact.area, contact.zip)
            contact.imagePath?.let {
                if (it.isNotEmpty()) {
                    val request = ImageRequest.Builder(requireContext())
                        .data(it)
                        .error(ContextCompat.getDrawable(requireContext(), R.drawable.ic_account))
                        .crossfade(600)
                        .target(createImageTarget())
                        .build()
                    Coil.imageLoader(requireContext()).enqueue(request)
                }
            }
        }
    }

    private fun createImageTarget() = object : TransitionTarget {
        override val drawable get() = binding.imgUserPhoto.drawable
        override val view get() = binding.imgUserPhoto

        override fun onSuccess(result: Drawable) {
            binding.imgUserPhoto.setImageDrawable(result)
            (result as? Animatable)?.start()
        }

        override fun onError(error: Drawable?) {
            binding.imgUserPhoto.setImageDrawable(error)
            Toast.makeText(requireContext(), R.string.toast_image_not_available, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun formatAddress(address: String): String =
        address.ifEmpty { resources.getString(R.string.not_available) }

    private fun formatLocation(city: String, area: String, zip: String): String {
        return if (city.isNotEmpty() && area.isNotEmpty() && zip.isNotEmpty()) {
            resources.getString(R.string.location, city, area, zip)
        } else if (city.isNotEmpty() && area.isNotEmpty()) {
            resources.getString(R.string.no_zip_location, city, area)
        } else {
            resources.getString(R.string.not_available)
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
        Toast.makeText(requireContext(), R.string.toast_contact_deleted, Toast.LENGTH_SHORT).show()
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(
            requireContext(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setIcon(R.drawable.ic_delete)
            .setTitle(resources.getString(R.string.dialog_title_delete))
            .setMessage(resources.getString(R.string.dialog_supporting_text_delete))
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
            }
            .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                deleteContact()
            }
            .show()
    }

}