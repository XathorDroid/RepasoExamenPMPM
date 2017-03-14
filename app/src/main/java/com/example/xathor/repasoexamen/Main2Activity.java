package com.example.xathor.repasoexamen;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import Class.Contact;
import Class.MyDbHelper;

public class Main2Activity extends AppCompatActivity {

    private EditText etName, etTlf, etEmail;
    private Spinner spIcons;
    private Button btnSave, btnCancel;
    private ImageView ivIcon;
    private TextView tvError;

    private ArrayAdapter<String> adapter;

    private String[] icons = {"Flecha Abajo", "Flecha Arriba", "Micrófono", "Eliminar", "Añadir"};
    private Integer[] iconsResource = {android.R.drawable.arrow_down_float, android.R.drawable.arrow_up_float, android.R.drawable.ic_btn_speak_now, android.R.drawable.ic_delete, android.R.drawable.ic_input_add};

    private String tlfOld;
    private int iconId = iconsResource[0];
    int caseOp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initialize();

        // Este botón cierra la actividad 2 y vuelve a la 1
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvError.setText(null);
                finish();
            }
        });

        // Método para recoger la selección de un Spinner
        // No confundir con setOnItemClickListener, utilizado para las ListView
        spIcons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ivIcon.setImageResource(iconsResource[position]);
                iconId = iconsResource[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Botón que guarda los datos
        btnSave.setOnClickListener(saveContact);

        // Recojo los datos enviados desde la Actividad 1 a través de un Intent
        // En este caso utilizo un Bundle, pero podría hacer un: Intent i = getIntent() que funcionaría igual
        Bundle bGet = getIntent().getExtras();
        caseOp = bGet.getInt("case");
        // Si el valor de caseOP es 1, recojo el Contacto enviado para rellenar los campos de la vista y poder editar sus campos
        if(caseOp == 1) {
            Contact contactSelected = (Contact)bGet.getSerializable("contactSelected");
            tlfOld = contactSelected.getTlf();
            etName.setText(contactSelected.getName());
            etTlf.setText(contactSelected.getTlf());
            etEmail.setText(contactSelected.getMail());
            for (int i = 0; i < iconsResource.length; i++) {
                if(iconsResource[i] == contactSelected.getIcon()) {
                    spIcons.setSelection(i);
                }
            }
        }
    }

    public void initialize() {
        etName = (EditText)findViewById(R.id.etName);
        etTlf = (EditText)findViewById(R.id.etTlf);
        etEmail = (EditText)findViewById(R.id.etEmail);
        ivIcon = (ImageView)findViewById(R.id.ivIcon);
        spIcons = (Spinner)findViewById(R.id.spIcon);
        btnSave = (Button)findViewById(R.id.btnSave);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        tvError = (TextView)findViewById(R.id.tvError);

        // Creo un adaptador para el Spinner y se lo asigno
        adapter = new ArrayAdapter<>(Main2Activity.this, android.R.layout.simple_spinner_dropdown_item, icons);

        spIcons.setAdapter(adapter);
    }

    // Clase anónima para el botón que guardará el contacto
    public View.OnClickListener saveContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            tvError.setText(null);

            boolean check = false;
            String name, email, tlf;
            name = etName.getText().toString();
            email = etEmail.getText().toString();
            tlf = etTlf.getText().toString();

            // Si la opción no es 1, estamos creando un nuevo contacto, por lo que comprobaremos siempre
            // que el teléfono que le introduzcamos no existe ya
            if(caseOp != 1) {
                // Creo un nuevo objeto myDbHelper y una nueva referencia a la Base de Datos con getReadableDatabase
                // En este caso es sólo en forma de lectura porque no necesitamos modificar sus datos
                MyDbHelper myDbHelper = new MyDbHelper(Main2Activity.this, "Contacts", null, 1);
                SQLiteDatabase myDB = myDbHelper.getReadableDatabase();
                // Creamos array donde indicamos los datos que recuperaremos
                String[] contacts = {"tlf"};
                // Y usamos el método query para hacer la consulta a la tabla Contacts
                Cursor cursor = myDB.query("Contacts", contacts, "tlf = '" + tlf + "'", null, null, null, null, null);
                if(cursor.moveToFirst()) {
                // Si el cursor accede a la primera posición es que existe algún registro ya
                // por lo que emitiremos un mensaje de error y no permitiremos guardar el Contacto
                    tvError.setText(getString(R.string.duplicateTlf));
                    check = true;
                } else {
                // En caso contrario permitiremos guardar el Contacto
                    check = false;
                }
            }

            if(!check) {
                tvError.setText(null);
                // Comprobamos que los campos no están vacíos. Si lo están damos un mensaje de error
                if (name.isEmpty()) {
                    tvError.append(getString(R.string.error1) + "\n");
                } else if (tlf.isEmpty()) {
                    tvError.append(getString(R.string.error2) + "\n");
                } else if (email.isEmpty()) {
                    tvError.append(getString(R.string.error3) + "\n");
                } else {
                // En caso de no estar vacíos creamos un nuevo objeto Contact con los datos de las Vistas
                    Contact contact = new Contact(name, tlf, email, iconId);

                    // Creamos un nuevo intent que enviaremos de regreso a la Actividad 1
                    // Para añadir al Intent un objeto, debemos implementarle al objeto Serializable
                    Intent i = new Intent();
                    i.putExtra("contact", contact);
                    i.putExtra("caseOp", caseOp);
                    i.putExtra("tlfOld", tlfOld);
                    // Enviamos el intent con una bandera que indica que se retorna sin problemas (RESULT_OK)
                    setResult(RESULT_OK, i);
                    // Cerramos la actividad
                    finish();
                }
            }
        }
    };

}
