package org.thamba.guard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract

object Contacts {

    /**
     * True only when the number is saved in the phone's contacts.
     * If we cannot check, we answer false: an unverified caller is treated as unknown.
     */
    fun isKnown(context: Context, number: String?): Boolean {
        if (number.isNullOrBlank()) return false
        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return false
        return try {
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
            context.contentResolver
                .query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
                ?.use { it.moveToFirst() } ?: false
        } catch (e: Exception) {
            false
        }
    }
}
