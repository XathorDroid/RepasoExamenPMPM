package Class;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDbHelper extends SQLiteOpenHelper {

    String dbCreate = "CREATE TABLE Contacts (tlf CHAR(15) PRIMARY KEY, name VARCHAR(30) NOT NULL, email VARCHAR(50) NOT NULL, icon INTEGER NOT NULL)";
    String dbDrop = "DROP TABLE IF EXISTS Contacts";

    public MyDbHelper(Context context, String name, SQLiteDatabase.CursorFactory cursor, int version) {
        super(context, name, cursor, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(dbCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(dbDrop);
        db.execSQL(dbCreate);
    }
}