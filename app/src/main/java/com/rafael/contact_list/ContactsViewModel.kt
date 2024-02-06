package com.rafael.contact_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rafael.contact_list.data.Contact
import com.rafael.contact_list.data.ContactDao
import kotlinx.coroutines.launch

class ContactsViewModel(private val contactDao: ContactDao) : ViewModel() {

    val allContacts: LiveData<List<Contact>> = contactDao.getContacts().asLiveData()
    private val _imagePath = MutableLiveData<String?>()
    val imagePath: LiveData<String?> = _imagePath

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
        zip: String,
        imagePath: String?
    ): Contact {
        return Contact(
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            address = address,
            city = city,
            area = area,
            zip = zip,
            imagePath = imagePath
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
        zip: String,
        imagePath: String?
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
            imagePath = imagePath
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
        zip: String,
        imagePath: String?
    ) {
        val updatedContact =
            getUpdatedContactEntry(
                contactId,
                firstName,
                lastName,
                phone,
                address,
                city,
                area,
                zip,
                imagePath
            )
        updateContact(updatedContact)
    }

    fun addNewContact(
        firstName: String,
        lastName: String,
        phone: String,
        address: String,
        city: String,
        area: String,
        zip: String,
        imagePath: String?
    ) {
        val newContact =
            getNewContactEntry(firstName, lastName, phone, address, city, area, zip, imagePath)
        insertContact(newContact)
    }

    fun areFieldsNotBlank(vararg fields: String): Boolean {
        return fields.all { it.isNotBlank() }
    }

    fun searchContacts(query: String): List<Contact> {
        val allContacts = allContacts
        val filteredContacts = allContacts.value!!.filter { contact ->
            contact.firstName.contains(query, ignoreCase = true)
        }

        return filteredContacts
    }

    fun updateImagePath(path: String?) {
        _imagePath.value = path
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
