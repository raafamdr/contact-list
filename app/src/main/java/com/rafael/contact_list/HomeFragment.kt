package com.rafael.contact_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rafael.contact_list.data.Contact
import com.rafael.contact_list.databinding.FragmentHomeBinding

class HomeFragment : Fragment(), SearchView.OnQueryTextListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ContactListAdapter
    private var contacts: List<Contact> = emptyList()

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ContactListAdapter {
            val action = HomeFragmentDirections.actionHomeFragmentToContactDetailFragment(it.id)
            this.findNavController().navigate(action)
        }
        binding.recyclerView.adapter = adapter
        viewModel.allContacts.observe(this.viewLifecycleOwner) { contacts ->
            contacts.let {
                adapter.submitList(it)
            }
            if (contacts.isEmpty()) {
                binding.imgEmpty.visibility = View.VISIBLE
                binding.textEmpty.visibility = View.VISIBLE
            } else {
                binding.imgEmpty.visibility = View.GONE
                binding.textEmpty.visibility = View.GONE
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)

        binding.floatingActionButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToAddContactFragment()
            findNavController().navigate(action)
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    val searchView = menuItem.actionView as SearchView
                    searchView.setOnQueryTextListener(this@HomeFragment)
                    true
                }

                R.id.az -> {
                    contacts = viewModel.allContacts.value ?: emptyList()
                    contacts = contacts.sortedBy { it.firstName }
                    adapter.submitList(contacts)
                    true
                }

                R.id.za -> {
                    contacts = viewModel.allContacts.value ?: emptyList()
                    contacts = contacts.sortedByDescending { it.firstName }
                    adapter.submitList(contacts)
                    true
                }

                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {
        filterList(query)
        return true
    }

    private fun filterList(query: String?) {
        contacts = if (query.isNullOrEmpty()) {
            viewModel.allContacts.value ?: emptyList()
        } else {
            viewModel.searchContacts(query)
        }
        adapter.submitList(contacts)
    }
}