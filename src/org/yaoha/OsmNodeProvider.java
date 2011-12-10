package org.yaoha;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class OsmNodeProvider extends ContentProvider {
    private OsmNodeDbHelper dbHelper;
    private static final String nodesAuthority = "org.yaoha.nodes";
    private static final UriMatcher sUriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    private static final Map<String, String> nodeProjectionMap = new HashMap<String, String>();
    private static final Map<String, String> keyProjectionMap = new HashMap<String, String>();

    private static final int NODES = 1;

    static {
        sUriMatcher.addURI(nodesAuthority, "nodes", NODES);

        nodeProjectionMap.put("_id", "_id");
        nodeProjectionMap.put("node_id", "node_id");
        nodeProjectionMap.put("lat", "lat");
        nodeProjectionMap.put("lon", "lon");
        
        keyProjectionMap.put("_id", "_id");
        keyProjectionMap.put("key_id", "key_id");
        keyProjectionMap.put("key", "key");
        keyProjectionMap.put("value", "value");
    }

    private static class OsmNodeDbHelper extends SQLiteOpenHelper {
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