package com.rafael.contact_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rafael.contact_list.data.Contact
import com.rafael.contact_list.data.ContactDao
import kotlinx.coroutines.launch

class ContactsViewModel(private val contactDao: ContactDao) : ViewModel() {

    val allContacts: LiveData<List<Contact>> = contactDao.getContacts().asLiveData()
    private fun insertContact(contact: Contact) {
        viewModelScope.launch {
            contactDao.insert(contact)
        }
    }

    private fun updateContact(contact: Contact) {
        viewModelScope.launch {
            contactDao.update(contact)
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            contactDao.delete(contact)
        }
    }

    fun retrieveContact(id: Int): LiveData<Contact> {
        return contactDao.getContact(id).asLiveData()
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

    private fun getUpdatedContactEntry(
        contactId: Int,
        firstName: String,
        lastName: String,
        phone: String,
        address: String,
        city: String,
        area: String,
        zip: String
    ): Contact {
        return Contact(
            id = contactId,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            address = address,
            city = city,
            area = area,
            zip = zip,
        )
    }

    fun updateContact(
        contactId: Int,
        firstName: String,
        lastName: String,
        phone: String,
        address: String,
        city: String,
        area: String,
        zip: String
    ) {
        val updatedContact =
            getUpdatedContactEntry(contactId, firstName, lastName, phone, address, city, area, zip)
        updateContact(updatedContact)
    }

    fun addNewContact(
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

    fun areFieldsNotBlank(vararg fields: String): Boolean {
        return fields.all { it.isNotBlank() }
    }
}

class ContactsViewModelFactory(private val contactDao: ContactDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(contactDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
