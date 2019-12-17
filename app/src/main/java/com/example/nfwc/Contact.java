package com.example.nfwc;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;
import android.util.Log;

public class Contact {
    private Context mContext;

    public Contact(Context context){
        mContext=context;
    }

    public void addContact(String name,String phoneNumber){
        ContentValues values=new ContentValues();

        Uri rawContactUri=mContext.getContentResolver().insert(RawContacts.CONTENT_URI,values);
        long rawContactId=ContentUris.parseId(rawContactUri);
        values.clear();
        values.put(Data.RAW_CONTACT_ID,rawContactId);
        values.put(Data.MIMETYPE,StructuredName.CONTENT_ITEM_TYPE);
        values.put(StructuredName.GIVEN_NAME,name);
        mContext.getContentResolver().insert(Data.CONTENT_URI,values);

        values.clear();
        values.put(Data.RAW_CONTACT_ID,rawContactId);
        values.put(Data.MIMETYPE,Phone.CONTENT_ITEM_TYPE);
        values.put(Phone.NUMBER,phoneNumber);
        values.put(Phone.TYPE,Phone.TYPE_MOBILE);
        mContext.getContentResolver().insert(Data.CONTENT_URI,values);

//        values.clear();
//        values.put(Data.RAW_CONTACT_ID, rawContactId);
//        values.put(Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
//        values.put(ContactsContract.CommonDataKinds.Email.DATA, "kesenhoo@gmail.com");
//        values.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
//        mContext.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
    }
}
