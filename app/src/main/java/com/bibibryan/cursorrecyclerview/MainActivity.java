package com.bibibryan.cursorrecyclerview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1111;
    private static final String CONTACTS_KEY = "CONTACTS_KEY";
    private final static String[] PROJECTION = {
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID

    };
    private static final String SELECTION = ContactsContract.CommonDataKinds.Phone.NUMBER
            + " LIKE ? " + "AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'";
    private String searchString = "";
    private String[] selectionArgs = {searchString};
    private String sortKey = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY +  " COLLATE NOCASE ";
    private ArrayList<HashMap<String, String>> contactsList = new ArrayList<>();

    private CursorRecyclerViewAdapter cursorRecyclerViewAdapter;



    //
//    private final static String[] PROJECTION = {
//            ContactsContract.Data._ID,
//            ContactsContract.Data.DISPLAY_NAME_PRIMARY,
//            ContactsContract.Data.CONTACT_ID,
//            ContactsContract.Data.LOOKUP_KEY,
//    };


//    private static final String SELECTION = ContactsContract.Data.LOOKUP_KEY
//            + " = ? " + "AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestAllPermissions();


        cursorRecyclerViewAdapter = new CursorRecyclerViewAdapter(contactsList);

        RecyclerView recyclerView = findViewById(R.id.contacts_list_view);
        recyclerView.setAdapter(cursorRecyclerViewAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        LoaderManager.getInstance(this).initLoader(0, null, this);

    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CONTACTS_KEY, contactsList);
    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        contactsList = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable(CONTACTS_KEY);
        if(contactsList == null){
            Log.d("MAIN_ACTIVITY", "CONTACT LIST IS NULL");
        }
        cursorRecyclerViewAdapter.update(contactsList);
    }



    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        selectionArgs[0] = "%" + searchString + "%";

        return new CursorLoader(this,
                ContactsContract.Data.CONTENT_URI,
                PROJECTION,
                SELECTION,
                selectionArgs,
                sortKey);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.d("MAIN_ACTIVITY", "data count" + data.getCount());
        contactsList = cursorToArrayList(data);
        cursorRecyclerViewAdapter.update(contactsList);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        cursorRecyclerViewAdapter.update(null);
    }





    private ArrayList<HashMap<String, String>> cursorToArrayList(Cursor data) {
        HashMap<String, String> contactsMap = new HashMap<>();

        for(int i = 0; i < data.getCount(); i++){
            data.moveToPosition(i);
            int contactNameIndex = data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
            int contactPhonenumberIndex = data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            String phonenumber = data.getString(contactPhonenumberIndex).replaceAll(" ", "");
            String name = data.getString(contactNameIndex);

            if(!contactsMap.containsKey(phonenumber)){
                contactsMap.put(phonenumber, name);

                HashMap<String, String> contactDetails = new HashMap<>();
                contactDetails.put(phonenumber, name);

                contactsList.add(contactDetails);
            }
        }

        contactsMap.clear();
        return contactsList;
    }


    private void requestAllPermissions() {
        //TODO: Request permission when the user is about to use the meet functionality.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                //TODO:HOW DO WE SHOW THE EXPLANATION TO THE USER?
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
    }
}


//No need to use the cursor directly.
class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.CursorRecyclerViewHolder>{

//    private Cursor mCursor;
    private ArrayList<HashMap<String, String>> contacts;

    public CursorRecyclerViewAdapter(ArrayList<HashMap<String, String>> contacts){
        this.contacts = contacts;
    }


    @NonNull
    @Override
    public CursorRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false);
        return new CursorRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CursorRecyclerViewHolder holder, int position) {
        Log.d("MAIN_ACTIVITY", "ITEM POSITION " + position);

//        boolean moved = mCursor.moveToPosition(position);
//        int contactNameIndex = mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
//        int contactPhonenumberIndex = mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//        if(moved){
//            holder.contactListName.setText(mCursor.getString(contactNameIndex));
//            holder.contactListNumber.setText(mCursor.getString(contactPhonenumberIndex));
//        }

        String phoneNumberAsKey = contacts.get(position).keySet().toArray()[0].toString();
        holder.contactListName.setText(contacts.get(position).get(phoneNumberAsKey));
        holder.contactListNumber.setText(phoneNumberAsKey);
    }

    @Override
    public int getItemCount() {
        if(contacts != null){
            return this.contacts.size();
        }else {
            return 0;
        }
    }

    public void update(ArrayList<HashMap<String, String>> data) {
        this.contacts = data;
        Log.d("MAIN_ACTIVITY", "CONTACT LIST SIZE IN ADAPTER " + contacts.size());

        notifyDataSetChanged();
    }


    public class CursorRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView contactListName, contactListNumber;

        public CursorRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            contactListName = itemView.findViewById(R.id.contact_list_item_name);
            contactListNumber = itemView.findViewById(R.id.contact_list_item_number);
        }
    }
}