package com.example.xathor.repasoexamen;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import Class.Contact;
import Class.MyDbHelper;
import Class.PersonalizeAdapter;

public class MainActivity extends AppCompatActivity {

    private Button btnFirstContact;
    private ListView lvContacts;

    private ArrayList<Contact> contacts;

    private PersonalizeAdapter adapter;

    private Contact contactSelected;

    private MyDbHelper myDbHelper;
    private SQLiteDatabase myDb;

    private static final int NEWCONTACT = 1;
    private static final String NAMEDB = "Contacts";
    private static final int VERSIONDB = 1;

    private String dbDrop = "DROP TABLE IF EXISTS Contacts";
    private String delContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        loadList();

        registerForContextMenu(lvContacts);

        btnFirstContact.setOnClickListener(newContact);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == NEWCONTACT) {
            if(resultCode == RESULT_OK) {
                Contact contact = (Contact)data.getSerializableExtra("contact");
                ContentValues newValue = new ContentValues();
                newValue.put("tlf", contact.getTlf());
                newValue.put("name", contact.getName());
                newValue.put("email", contact.getMail());
                newValue.put("icon", contact.getIcon());

                if(data.getExtras().getInt("caseOp") == 1) {
                    myDb.delete("Contacts", "tlf = '"+data.getStringExtra("tlfOld")+"'", null);
                }

                myDb.insert("Contacts", null, newValue);
                loadList();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settingsmenu, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenu, menu);
        menu.setHeaderTitle(((Contact)lvContacts.getAdapter().getItem(info.position)).getName());
        delContact = ((Contact) lvContacts.getAdapter().getItem(info.position)).getTlf();
        contactSelected = (Contact)lvContacts.getAdapter().getItem(info.position);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.miNew:
                Intent iNew = new Intent(MainActivity.this, Main2Activity.class);
                iNew.putExtra("case", 0);
                iNew.putExtra("contactSelected", contactSelected);
                startActivityForResult(iNew, NEWCONTACT);
                return true;
            case R.id.miDel:
                AlertDialog.Builder dialogDelBD = new AlertDialog.Builder(MainActivity.this);
                dialogDelBD.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
                dialogDelBD.setTitle(getString(R.string.delBD));
                dialogDelBD.setMessage(getString(R.string.delBDMsg));
                dialogDelBD.setNegativeButton(getString(R.string.cancel), null);
                dialogDelBD.setPositiveButton(getString(R.string.ok), deleteBD);
                dialogDelBD.create().show();
                return true;
            case R.id.miSetting:
                Intent iSett = new Intent(MainActivity.this, Main3Activity.class);
                startActivity(iSett);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.miSeeContact:
                AlertDialog.Builder dialogSeeContact = new AlertDialog.Builder(MainActivity.this);
                dialogSeeContact.setIcon(getDrawable(contactSelected.getIcon()));
                dialogSeeContact.setTitle(contactSelected.getName());
                dialogSeeContact.setMessage("Nombre: "+contactSelected.getName()+"\n\nTeléfono: "+contactSelected.getTlf()+"\n\nEmail: "+contactSelected.getMail());
                dialogSeeContact.create().show();
                return true;
            case R.id.miEditContact:
                Intent iEdit = new Intent(MainActivity.this, Main2Activity.class);
                iEdit.putExtra("case", 1);
                iEdit.putExtra("contactSelected", contactSelected);
                startActivityForResult(iEdit, NEWCONTACT);
                return true;
            case R.id.miDelContact:
                AlertDialog.Builder dialogDelContact = new AlertDialog.Builder(MainActivity.this);
                dialogDelContact.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
                dialogDelContact.setTitle(getString(R.string.delContact));
                dialogDelContact.setMessage(getString(R.string.delContactMsg));
                dialogDelContact.setNegativeButton(getString(R.string.cancel), null);
                dialogDelContact.setPositiveButton(getString(R.string.ok), deleteContact);
                dialogDelContact.create().show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void initialize() {
        btnFirstContact = (Button)findViewById(R.id.btnFirstContact);
        lvContacts = (ListView)findViewById(R.id.lvContacts);

        contacts = new ArrayList();

        myDbHelper = new MyDbHelper(MainActivity.this, NAMEDB, null, VERSIONDB);
        myDb = myDbHelper.getWritableDatabase();

        adapter = new PersonalizeAdapter(MainActivity.this, contacts);
        lvContacts.setAdapter(adapter);
    }

    public void loadList() {
        adapter.clear();
        String name, tlf, email;
        int icon;

        String[] contactsLoad = {"tlf, name, email, icon"};
        Cursor cursor = myDb.query("Contacts", contactsLoad, null, null, null, null, null);

        if(!cursor.moveToFirst()) {
            btnFirstContact.setVisibility(View.VISIBLE);
            lvContacts.setVisibility(View.GONE);
        } else {
            btnFirstContact.setVisibility(View.GONE);
            lvContacts.setVisibility(View.VISIBLE);

            do {
                tlf = cursor.getString(0);
                name = cursor.getString(1);
                email = cursor.getString(2);
                icon = cursor.getInt(3);

                Contact contact = new Contact(name, tlf, email, icon);
                contacts.add(contact);
            } while(cursor.moveToNext());

            adapter.notifyDataSetChanged();
        }
    }

    public View.OnClickListener newContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent iNew = new Intent(MainActivity.this, Main2Activity.class);
            iNew.putExtra("case", 0);
            startActivityForResult(iNew, NEWCONTACT);
        }
    };

    public DialogInterface.OnClickListener deleteBD = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            myDb.execSQL(dbDrop);
            Toast.makeText(MainActivity.this, "Base de Datos Eliminada...", Toast.LENGTH_SHORT).show();
            adapter.clear();
            btnFirstContact.setVisibility(View.VISIBLE);
            lvContacts.setVisibility(View.GONE);
        }
    };

    public DialogInterface.OnClickListener deleteContact = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            myDb.delete("Contacts", "tlf = '"+delContact+"'", null);
            Toast.makeText(MainActivity.this, "Contacto "+delContact+" eliminado...", Toast.LENGTH_SHORT).show();
            loadList();
        }
    };

}