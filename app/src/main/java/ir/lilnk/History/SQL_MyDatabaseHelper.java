package ir.lilnk.History;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class SQL_MyDatabaseHelper extends SQLiteOpenHelper {

    private Context context;

    private static final String DATABASE_NAME = "lilnk.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "Shortenedlinks";
    private static final String COLUMN_ShortLink = "ShortLink";
    private static final String COLUMN_LongLink = "LongLink";


    public SQL_MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ShortLink + " TEXT, " + COLUMN_LongLink + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    public boolean IsItemExist(String ShortLink) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            @SuppressLint("Recycle")
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ShortLink + "=?", new String[]{ShortLink});
            if (cursor.moveToFirst()) {
                db.close();
                Log.d("Record  Already Exists", "Table is:" + TABLE_NAME + " ColumnName:" + ShortLink);
                return true;//record Exists

            }
            db.close();
        } catch (Exception errorException) {
            Log.d("Exception occured", "Exception occured " + errorException);
        }
        return false;
    }

    public void AddToDatabase(String MyShortLink, String MyLongLink) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ShortLink, MyShortLink);
        cv.put(COLUMN_LongLink, MyLongLink);

        long result = db.insert(TABLE_NAME, null, cv);
        if (result == -1) {
            Log.i("TAG_ToDatabase", "AddToDatabase: UnSuccessful");
        } else {
            Log.i("TAG_ToDatabase", "AddToDatabase: Successful");
        }

    }

    void resetFactory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME);
    }

    public Cursor ReadAllData() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;
        if (db != null)
            cursor = db.rawQuery(query, null);
        return cursor;
    }

}
