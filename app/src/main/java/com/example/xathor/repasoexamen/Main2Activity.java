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

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvError.setText(null);
                finish();
            }
        });

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

        btnSave.setOnClickListener(saveContact);

        Bundle bGet = getIntent().getExtras();
        caseOp = bGet.getInt("case");
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

        adapter = new ArrayAdapter<>(Main2Activity.this, android.R.layout.simple_spinner_dropdown_item, icons);

        spIcons.setAdapter(adapter);
    }

    public View.OnClickListener saveContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            tvError.setText(null);

            boolean check = false;
            String name, email, tlf;
            name = etName.getText().toString();
            email = etEmail.getText().toString();
            tlf = etTlf.getText().toString();

            if(caseOp != 1) {
                MyDbHelper myDbHelper = new MyDbHelper(Main2Activity.this, "Contacts", null, 1);
                SQLiteDatabase myDB = myDbHelper.getReadableDatabase();
                String[] contacts = {"tlf"};
                Cursor cursor = myDB.query("Contacts", contacts, "tlf = '" + tlf + "'", null, null, null, null, null);
                if(cursor.moveToFirst()) {
                    tvError.setText(getString(R.string.duplicateTlf));
                    check = true;
                } else {
                    check = false;
                }
            }

            if(!check) {
                tvError.setText(null);
                if (name.isEmpty()) {
                    tvError.append(getString(R.string.error1) + "\n");
                } else if (tlf.isEmpty()) {
                    tvError.append(getString(R.string.error2) + "\n");
                } else if (email.isEmpty()) {
                    tvError.append(getString(R.string.error3) + "\n");
                } else {
                    Contact contact = new Contact(name, tlf, email, iconId);

                    Intent i = new Intent();
                    i.putExtra("contact", contact);
                    i.putExtra("caseOp", caseOp);
                    i.putExtra("tlfOld", tlfOld);
                    setResult(RESULT_OK, i);
                    finish();
                }
            }
        }
    };

}