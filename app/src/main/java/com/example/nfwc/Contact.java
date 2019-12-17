package com.example.nfwc;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;

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
        values.put(Data.CONTACT_ID,rawContactId);
        values.put(Data.MIMETYPE,Phone.CONTENT_ITEM_TYPE);
        values.put(Phone.NUMBER,phoneNumber);
        values.put(Phone.TYPE,Phone.TYPE_MOBILE);
        mContext.getContentResolver().insert(Data.CONTENT_URI,values);

    }
}
