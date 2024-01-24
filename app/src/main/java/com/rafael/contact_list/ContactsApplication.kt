package com.rafael.contact_list

import android.app.Application
import com.rafael.contact_list.data.ContactRoomDatabase

class ContactsApplication : Application() {
    val database: ContactRoomDatabase by lazy { ContactRoomDatabase.getDatabase(this) }
}