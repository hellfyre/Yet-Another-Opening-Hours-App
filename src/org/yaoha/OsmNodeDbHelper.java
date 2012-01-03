package org.yaoha;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OsmNodeDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "osm_node.db";
    private static final int DATABASE_VERSION = 1;

    public OsmNodeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE nodes (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "node_id INTEGER NOT NULL, "
                + "lat INTEGER NOT NULL, "
                + "lon INTEGER NOT NULL; ");
        db.execSQL("CREATE TABLE node_attr (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "key_id INTEGER NOT NULL, "
                + "key TEXT NOT NULL, "
                + "value TEXT, "
                + "FOREIGN KEY (key_id) REFERENCES nodes (node_id ));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("OsmNodeProvider", "Upgrading database from version "
                + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS nodes;");
        onCreate(db);
    }

}
