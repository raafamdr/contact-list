package com.rafael.contact_list.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact)

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("SELECT * FROM contact WHERE id = :id")
    fun getContact(id: Int): Flow<Contact>

    @Query("SELECT * FROM contact ORDER BY first_name ASC")
    fun getContacts(): Flow<List<Contact>>
}