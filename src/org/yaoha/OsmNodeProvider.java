package org.yaoha;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

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
