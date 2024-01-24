package com.rafael.contact_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rafael.contact_list.data.Contact
import com.rafael.contact_list.data.ContactDao
import kotlinx.coroutines.launch

class ContactViewModel(private val contactDao: ContactDao) : ViewModel() {
    private fun insertContact(contact: Contact) {
        viewModelScope.launch {
            contactDao.insert(contact)
        }
    }

    private fun getNewContactEntry(
        firstName: String,
        lastName: String,
        phone: String,
        address: String,
        city: String,
        area: String,
        zip: String
    ): Contact {
        return Contact(
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            address = address,
            city = city,
            area = area,
            zip = zip,
        )
    }

    fun addNewItem(
        firstName: String,
        lastName: String,
        phone: String,
        address: String,
        city: String,
        area: String,
        zip: String
    ) {
        val newContact = getNewContactEntry(firstName, lastName, phone, address, city, area, zip)
        insertContact(newContact)
    }
}

class ContactsViewModelFactory(private val contactDao: ContactDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(contactDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
