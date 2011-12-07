package org.yaoha;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class OsmNodeProvider extends ContentProvider {
    private OsmNodeDbHelper dbHelper;

    private static class OsmNodeDbHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "osm_node.db";
        private static final int DATABASE_VERSION = 1;

        public OsmNodeDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE nodes (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "node_id INTEGER NOT NULL,"
                    + "lat INTEGER NOT NULL,"
                    + "lon INTEGER NOT NULL,"
                    + "name VARCHAR(255),"
                    + "amenity VARCHAR(255),"
                    + "opening_hours VARCHAR(255),"
                    + "last_updated INTEGER NOT NULL);");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("OsmNodeProvider", "Upgrading database from version " + oldVersion + " to " + newVersion);
            db.execSQL("DROP TABLE IF EXISTS nodes;");
            onCreate(db);
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new OsmNodeDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
