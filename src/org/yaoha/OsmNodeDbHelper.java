package org.yaoha;

import java.util.HashMap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class OsmNodeDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "osm_node.db";
    private static final int DATABASE_VERSION = 1;
    private static final String nodesTableName = "nodes";
    private static final String nodesTablePrimaryKey = "node_id";
    private static final String nodesTableLatitude = "lat";
    private static final String nodesTableLongitude = "lon";
    private static final String nodesAttributesTableName = "nodes_attr";
    private static final String nodesAttributesTableKey = "key";
    private static final String nodesAttributesTableValue = "value";
    SQLiteDatabase db;
    SQLiteStatement queryNode;
    SQLiteStatement queryNodes;
    SQLiteStatement queryAttributes;
    SQLiteStatement queryNodesFromMapExtract;
    SQLiteStatement insertNode;
    SQLiteStatement insertAttribute;

    public OsmNodeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        db.execSQL("CREATE TABLE " + nodesTableName + " (" + nodesTablePrimaryKey + " INTEGER PRIMARY KEY NOT NULL, "
                + nodesTableLatitude + " INTEGER NOT NULL, "
                + nodesTableLongitude + " INTEGER NOT NULL; ");
        db.execSQL("CREATE TABLE " + nodesAttributesTableName + " (" + nodesTablePrimaryKey + " INTEGER NOT NULL, "
                + nodesAttributesTableKey + " TEXT NOT NULL, "
                + nodesAttributesTableValue + " TEXT, "
                + "FOREIGN KEY (" + nodesTablePrimaryKey + ") REFERENCES nodes (" + nodesTablePrimaryKey + "), "
                + "PRIMARY KEY (" + nodesTablePrimaryKey + "," + nodesAttributesTableKey + "," + nodesAttributesTableValue + "));");
        queryNode = db.compileStatement("SELECT * FROM " + nodesTableName + " WHERE " + nodesTablePrimaryKey + " = ?;");
        queryNodes = db.compileStatement("SELECT * FROM " + nodesTableName + ";");
        queryAttributes = db.compileStatement("SELECT * FROM " + nodesAttributesTableName + " WHERE " + nodesTablePrimaryKey + " = ?;");
        queryNodesFromMapExtract = null; // TODO
        insertNode = db.compileStatement("INSERT INTO " + nodesTableName + " (" + nodesTablePrimaryKey + "," + nodesTableLatitude + "," + nodesTableLongitude + ") VALUES (?,?,?);");
        insertAttribute = db.compileStatement("INSERT INTO " + nodesAttributesTableName + " (" + nodesTablePrimaryKey + "," + nodesAttributesTableKey + "," + nodesAttributesTableValue + ") VALUES (?,?,?);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("OsmNodeProvider", "Upgrading database from version "
                + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS nodes;");
        db.execSQL("DROP TABLE IF EXISTS nodes_attr;");
        onCreate(db);
    }
    
    public void storeNode(OsmNode node) {
        
    }
    
    public OsmNode getNode(int key) {
        
        return null;
    }
    
    public HashMap<Integer, OsmNode> createNodesFromDatabase() {
        return null;
    }

}
