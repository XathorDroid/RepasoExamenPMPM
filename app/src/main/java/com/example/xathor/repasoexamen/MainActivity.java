package com.example.xathor.repasoexamen;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import Class.Contact;
import Class.MyDbHelper;
import Class.PersonalizeAdapter;

public class MainActivity extends AppCompatActivity {

    private Button btnFirstContact;
    private ListView lvContacts;
    private EditText etNewUser;

    private ArrayList<Contact> contacts;

    private PersonalizeAdapter adapter;

    private Contact contactSelected;

    private MyDbHelper myDbHelper;
    private SQLiteDatabase myDb;

    private SharedPreferences myPreferences;

    private static final int NEWCONTACT = 1;
    private static final String NAMEDB = "Contacts";
    private static final int VERSIONDB = 1;

    private String dbCreate = "CREATE TABLE Contacts (tlf CHAR(15) PRIMARY KEY, name VARCHAR(30) NOT NULL, email VARCHAR(50) NOT NULL, icon INTEGER NOT NULL)";
    private String dbDrop = "DROP TABLE IF EXISTS Contacts";
    private String delContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Método propio donde inicializo las variables y las vistas
        initialize();
        // Método propio donde recupero las Preferencias Compartidas
        // En este caso recupero un nombre de usuario que pongo como título en la ActionBar
        loadPreferences();
        // Método propio que carga de la Base de Datos, si no está vacía, la lista de contactos
        // a una ListView
        loadList();

        // Método necesario para establecer un Menú Contextual a una vista
        // registerForContextMenu(nombre_de_la_vista);
        registerForContextMenu(lvContacts);

        // Método asignado al botón principal que aparece cuando la lista de contactos está vacía
        btnFirstContact.setOnClickListener(newContact);
    }

    // En este método Sobreescrito recuperas el Intent con los datos que le añades
    // En la segunda actividad
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // NEWCONTACT es el atributo FINAL que uso como bandera para recibir los datos
        if(requestCode == NEWCONTACT) { 
            // RESULT_OK valor predefinido de Android que indica que se devolvieron los datos sin problema
            if(resultCode == RESULT_OK) { 
                // Recupero del Intent un objeto de la clase Contact
                Contact contact = (Contact)data.getSerializableExtra("contact");
                // Creo un ContentValues para darle los valores del Contacto nuevo que guardaré en la BD
                ContentValues newValue = new ContentValues();
                newValue.put("tlf", contact.getTlf());
                newValue.put("name", contact.getName());
                newValue.put("email", contact.getMail());
                newValue.put("icon", contact.getIcon());

                // Si caseOp (una bandera enviada a la Actividad2) vale 1, indica que estoy modificando, y antes
                // de guardar el contacto nuevo, lo elimino de la BD
                if(data.getExtras().getInt("caseOp") == 1) {
                    myDb.delete("Contacts", "tlf = '"+data.getStringExtra("tlfOld")+"'", null);
                }
                // Me acabo de dar cuenta que en vez de eliminar e insertar podía hacer un update xD
                myDb.insert("Contacts", null, newValue); 
                // Vuelvo a cargar la lista en la ListView
                loadList();
            }
        }
    }

    // Creo el Menú que aparece en la ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settingsmenu, menu);
        return true;
    }

    // Creo el Menú Contextual que usaré con cada elemento de la ListView
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenu, menu);
        // Establezco como Cabecera del menú el nombre del Contacto seleccionado
        menu.setHeaderTitle(((Contact)lvContacts.getAdapter().getItem(info.position)).getName());
        // Guardo en variables datos del Contacto seleccionado para usar más adelante
        delContact = ((Contact) lvContacts.getAdapter().getItem(info.position)).getTlf();
        contactSelected = (Contact)lvContacts.getAdapter().getItem(info.position);
    }

    // Establezco las funciones a realizar en cada opción del Menú de la ActionBar según la que se escoja
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.miNew:
                // Se crea el Intent para enviar con los parámentros Contexto desde dónde se envía y Clase a la que se envía
                Intent iNew = new Intent(MainActivity.this, Main2Activity.class);
                iNew.putExtra("case", 0);
                iNew.putExtra("contactSelected", contactSelected);
                // Después de insertarle los datos con putExtra() o los específicos putString(), putInt(), etc...
                // Mandamos el Intent, junto a la bandera para indicar el retorno de datos, mediante startActivityForResult()
                startActivityForResult(iNew, NEWCONTACT);
                return true;
            case R.id.miDel:
                // Creamos la Ventana de Diálogo que mostraremos, dándole como parametro el Contexto de la Actividad
                AlertDialog.Builder dialogDelBD = new AlertDialog.Builder(MainActivity.this);
                dialogDelBD.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
                dialogDelBD.setTitle(getString(R.string.delBD));
                dialogDelBD.setMessage(getString(R.string.delBDMsg));
                dialogDelBD.setNegativeButton(getString(R.string.cancel), null);
                dialogDelBD.setPositiveButton(getString(R.string.ok), deleteBD);
                dialogDelBD.create().show();
                // A esa ventana le asignamos icono, título, mensaje, y los botones que queramos mostrar
                // Dentro de cada botón indicamos el texto que mostrará y la función que se realizará
                // al pulsar sobre ellos
                return true;
            case R.id.miSetting:
                // Creo un nuevo intent y como no retornará datos uso el método startActivity para enviarlo
                Intent iSett = new Intent(MainActivity.this, Main3Activity.class);
                startActivity(iSett);
                return true;
            case R.id.miChangeUser:
                AlertDialog.Builder dialogChangeUser = new AlertDialog.Builder(MainActivity.this);
                dialogChangeUser.setIcon(getDrawable(R.drawable.account));
                dialogChangeUser.setTitle(getString(R.string.miChangeUser));
                
                // Estas líneas asignan a la nueva ventana de diálogo un layout que incorporará un EditText
                // Esto específicamente no lo hemos dado
                etNewUser = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                etNewUser.setLayoutParams(lp);
                dialogChangeUser.setView(etNewUser);
                
                dialogChangeUser.setPositiveButton(getString(R.string.ok), saveUser);
                dialogChangeUser.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Especifico las funciones a realizar según la opción pulsada en cada Opción del Menú Contextual
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.miSeeContact:
                // Ventana de Diálogo que únicamente muestra información en el mensaje
                AlertDialog.Builder dialogSeeContact = new AlertDialog.Builder(MainActivity.this);
                dialogSeeContact.setIcon(getDrawable(contactSelected.getIcon()));
                dialogSeeContact.setTitle(contactSelected.getName());
                dialogSeeContact.setMessage("Nombre: "+contactSelected.getName()+"\n\nTeléfono: "+contactSelected.getTlf()+"\n\nEmail: "+contactSelected.getMail());
                dialogSeeContact.create().show();
                return true;
            case R.id.miEditContact:
                // Creo un nuevo Intent que retornará datos. Le pasamos como bandera el case = 1 para indicar
                // a la otra actividad que estamos modificando
                Intent iEdit = new Intent(MainActivity.this, Main2Activity.class);
                iEdit.putExtra("case", 1);
                iEdit.putExtra("contactSelected", contactSelected);
                startActivityForResult(iEdit, NEWCONTACT);
                return true;
            case R.id.miDelContact:
                // Creamos una ventana de diálogo para pedir confirmación antes de eliminar un registro de la BD
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

        // Creamos un objeto del Auxiliar de BD que creamos en otra clase
        // Le pasamos como parámetros el contexto de la actividad, el nombre de la BD, un valor null como cursor, y la versión de la BD
        myDbHelper = new MyDbHelper(MainActivity.this, NAMEDB, null, VERSIONDB);
        // Creamos una instancia escribible de la Base de Datos con getWritableDatabase()
        // Nos retorna SQLiteDatabase
        myDb = myDbHelper.getWritableDatabase();

        // Creamos el adaptador personalizado para la ListView y se lo asignamos con setAdapter()
        adapter = new PersonalizeAdapter(MainActivity.this, contacts);
        lvContacts.setAdapter(adapter);
    }

    public void loadPreferences() {
        // Creamos un objeto de SharedPreferences a partir de las Preferencias Compartidas por Defecto
        myPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        // Recuperamos en un String un valor almacenado en las SharedPreferences con el indicador "user"
        // Si no hay ningún valor, guardamos en el String, como valor por defecto, R.string.userNo
        String user = myPreferences.getString("user", getString(R.string.userNo));
        // Asignamos el String como título del Layout (aparece en la ActionBar)
        this.setTitle(user);
    }

    public void loadList() {
        // Antes de llenar la lista de datos la vaciamos
        adapter.clear();
        String name, tlf, email;
        int icon;

        // Creo un Array con los datos que recibiré de la Base de Datos
        String[] contactsLoad = {"tlf, name, email, icon"};
        // Con el método query recupero los datos como si de una consulta SELECT se tratara
        Cursor cursor = myDb.query("Contacts", contactsLoad, null, null, null, null, null);

        if(!cursor.moveToFirst()) {
        // Si cursor.moveToFirst() es false significa que no hay datos que respondan a la consulta
            btnFirstContact.setVisibility(View.VISIBLE);
            lvContacts.setVisibility(View.GONE);
        } else {
        // En caso contrario recuperaré los datos y los grabaré en la ListView
            btnFirstContact.setVisibility(View.GONE);
            lvContacts.setVisibility(View.VISIBLE);

            // Mientras el cursos se pueda mover a un siguiente puesto, recupero los datos de la DB y creo
            // un nuevo objeto Contact que añado a un ArrayList y usaré para grabar los datos en la ListView
            do {
                tlf = cursor.getString(0);
                name = cursor.getString(1);
                email = cursor.getString(2);
                icon = cursor.getInt(3);

                Contact contact = new Contact(name, tlf, email, icon);
                contacts.add(contact);
            } while(cursor.moveToNext());

            // Notifico al adaptador que se han realizado cambios para que me los muestre
            adapter.notifyDataSetChanged();
        }
    }

    
    // Clases Anónimas
    // Responde al click sobre el botón newContact
    public View.OnClickListener newContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Creo un nuevo intent que enviará una bandera a la segunda actividad y espera recuperar datos
            // de ella
            Intent iNew = new Intent(MainActivity.this, Main2Activity.class);
            iNew.putExtra("case", 0);
            startActivityForResult(iNew, NEWCONTACT);
        }
    };

    // Responde al click del botón Si dentro de la Ventana de Diálogo que se abre al querer eliminar la BD
    public DialogInterface.OnClickListener deleteBD = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Ejecuto las sentencias SQL indicadas en variables
            myDb.execSQL(dbDrop);
            myDb.execSQL(dbCreate);
            // Indico en un Toast que se ha eliminado la BD y vacío la ListView
            Toast.makeText(MainActivity.this, getString(R.string.delBDOk), Toast.LENGTH_SHORT).show();
            adapter.clear();
            btnFirstContact.setVisibility(View.VISIBLE);
            lvContacts.setVisibility(View.GONE);
        }
    };

    // Responde al click del botón Si dentro de la Ventana de Diálogo que se abre al querer eliminar un Contact
    public DialogInterface.OnClickListener deleteContact = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Elimina el registro mediante el método reservado delete(nombre_tabla, condiciçon_where, null)
            myDb.delete("Contacts", "tlf = '"+delContact+"'", null);
            // Muestro un Toast como notificación y recargo la ListView
            Toast.makeText(MainActivity.this, "Contacto "+delContact+" eliminado...", Toast.LENGTH_SHORT).show();
            loadList();
        }
    };

    // Responde al click del botón Si dentro de la Ventana de Diálogo que se abre al querer cambiar el Usuario de la APP
    public DialogInterface.OnClickListener saveUser = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Guardo en un String el dato introducido en el EditText
            String newUser = etNewUser.getText().toString();

            // Creo un objeto Editor que servirá para guardar datos dentro de las SharedPreferences
            SharedPreferences.Editor editor = myPreferences.edit();
            editor.putString("user", newUser);
            // Para guardar los datos no olvidar ejecutar el apply() o el commit()
            editor.apply();

            loadPreferences();
        }
    };

}
