package Class;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.xathor.repasoexamen.R;

import java.util.ArrayList;

public class PersonalizeAdapter extends ArrayAdapter<Contact> {

    private Activity context;
    private ArrayList<Contact> contacts;

    public PersonalizeAdapter(Activity context, ArrayList<Contact> contacts) {
        super(context, R.layout.layout_personalize, contacts);
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View fila = inflater.inflate(R.layout.layout_personalize, null);

        TextView tvName = (TextView)fila.findViewById(R.id.tvName);
        TextView tvTlf = (TextView)fila.findViewById(R.id.tvTlf);

        tvName.setTextColor(context.getResources().getColor(R.color.colorPrimary));

        tvName.setText(contacts.get(position).getName());
        tvTlf.setText(contacts.get(position).getTlf());

        return fila;
    }
}