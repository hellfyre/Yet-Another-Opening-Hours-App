package org.yaoha;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class OsmNodeDbHelper extends SQLiteOpenHelper implements NodeReceiverInterface<OsmNode>, NodesQueryInterface<Integer, OsmNode> {
    private static final String DATABASE_NAME = "osm_node.db";
    private static final int DATABASE_VERSION = 2;
    private static final String nodesTableName = "nodes";
    private static final String nodesTablePrimaryKey = "node_id";
    private static final String nodesTableLatitude = "lat";
    private static final String nodesTableLongitude = "lon";
    private static final String nodesAttributesTableName = "nodes_attr";
    private static final String nodesAttributesTableKey = "key";
    private static final String nodesAttributesTableValue = "value";
    SQLiteStatement insertNode;
    SQLiteStatement insertAttribute;
    
    private static class SingletonHolder {
        public static OsmNodeDbHelper instance;
    }
    
    public static OsmNodeDbHelper create(Context context) {
    	if (SingletonHolder.instance == null)
    		SingletonHolder.instance = new OsmNodeDbHelper(context);
    	return getInstance();
    }
    
    public static OsmNodeDbHelper getInstance() {
        return SingletonHolder.instance;
    }

    public OsmNodeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + nodesTableName + " (" + nodesTablePrimaryKey + " INTEGER PRIMARY KEY NOT NULL, "
                + nodesTableLatitude + " INTEGER NOT NULL, "
                + nodesTableLongitude + " INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + nodesAttributesTableName + " (" + nodesTablePrimaryKey + " INTEGER NOT NULL, "
                + nodesAttributesTableKey + " TEXT NOT NULL, "
                + nodesAttributesTableValue + " TEXT, "
                + "FOREIGN KEY (" + nodesTablePrimaryKey + ") REFERENCES nodes (" + nodesTablePrimaryKey + "), "
                + "PRIMARY KEY (" + nodesTablePrimaryKey + "," + nodesAttributesTableKey + "," + nodesAttributesTableValue + "));");
        insertNode = db.compileStatement("INSERT INTO " + nodesTableName + " (" + nodesTablePrimaryKey + "," + nodesTableLatitude + "," + nodesTableLongitude + ") VALUES (?,?,?);");
        insertAttribute = db.compileStatement("INSERT INTO " + nodesAttributesTableName + " (" + nodesTablePrimaryKey + "," + nodesAttributesTableKey + "," + nodesAttributesTableValue + ") VALUES (?,?,?);");
    }
    
    private Cursor queryNodes() {
    	// "SELECT * FROM " + nodesTableName + ";"
    	SQLiteDatabase db = getReadableDatabase();
    	return db.query(nodesTableName, null, null, null, null, null, null);
    }
    
    private Cursor queryAttributes(int key) {
    	// "SELECT * FROM " + nodesAttributesTableName + " WHERE " + nodesTablePrimaryKey + " = ?;"
    	SQLiteDatabase db = getReadableDatabase();
    	return db.query(nodesAttributesTableName, null, nodesTablePrimaryKey + " = ?", new String[] {"" + key}, null, null, null);
    }

    // TODO we should optimize the database for rangequeries containing latitude and longitude
    private Cursor queryNodesFromMapExtract(int left, int top, int right, int bottom) {
    	SQLiteDatabase db = getReadableDatabase();
    	return db.query(nodesTableName, null,
    			nodesTableLongitude + " >= " + left + "AND " + nodesTableLongitude + " <= " + right + " AND " + nodesTableLatitude + " >= " + bottom + " AND " + nodesTableLatitude + " <= " + top,
    			new String[] {"" + left, "" + right, "" + bottom, "" + top}, null, null, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("OsmNodeProvider", "Upgrading database from version "
                + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + nodesTableName + ";");
        db.execSQL("DROP TABLE IF EXISTS " + nodesAttributesTableName + ";");
        onCreate(db);
    }
    
    @Override
    public void put(OsmNode node) {
        insertNode.bindLong(0, node.getID());
        insertNode.bindLong(1, node.getLongitudeE6());
        insertNode.bindLong(2, node.getLatitudeE6());
        insertNode.executeInsert();
        insertNode.clearBindings();
        for (String key : node.getKeys()) {
            String value = node.getAttribute(key);
            insertAttribute.bindLong(0, node.getID());
            insertAttribute.bindString(1, key);
            insertAttribute.bindString(2, value);
            insertAttribute.executeInsert();
            insertAttribute.clearBindings();
        }
    }
    
    private OsmNode createNodeFromRow(Cursor c) {
    	int keyIndex = c.getColumnIndexOrThrow(nodesTablePrimaryKey);
    	int latIndex = c.getColumnIndexOrThrow(nodesTableLatitude);
        int lonIndex = c.getColumnIndexOrThrow(nodesTableLongitude);
        return new OsmNode(c.getInt(keyIndex), c.getInt(latIndex), c.getInt(lonIndex));
    }
    
    private void addAttributesToNode(OsmNode node) {
    	Cursor c = queryAttributes(node.getID());
        for (int i = 0; i < c.getCount(); i++, c.moveToNext()) {
        	int keyIndex = c.getColumnIndexOrThrow(nodesAttributesTableKey);
        	int valueIndex = c.getColumnIndexOrThrow(nodesAttributesTableValue);
        	node.putAttribute(c.getString(keyIndex), c.getString(valueIndex));
        }
        c.close();
    }
    
    @Override
    public HashMap<Integer, OsmNode> getAllNodes() {
    	return createNodesFromRows(queryNodes());
    }
    
    @Override
    public HashMap<Integer, OsmNode> getNodesFromMapExtract(int left, int top, int right, int bottom) {
    	return createNodesFromRows(queryNodesFromMapExtract(left, top, right, bottom));
    }
    
    private HashMap<Integer, OsmNode> createNodesFromRows(Cursor c) {
    	HashMap<Integer, OsmNode> nodesMap = new HashMap<Integer, OsmNode>();
    	for (int i = 0; i < c.getCount(); i++, c.moveToNext()) {
    		OsmNode node = createNodeFromRow(c);
    		addAttributesToNode(node);
    		nodesMap.put(node.getID(), node);
    	}
    	c.close();
        return nodesMap;
    }
}
