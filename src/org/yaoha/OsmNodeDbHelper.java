package org.yaoha;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OsmNodeDbHelper extends SQLiteOpenHelper implements NodeReceiverInterface<OsmNode>, NodesQueryInterface<Integer, OsmNode> {
    private static final String DATABASE_NAME = "osm_node.db";
    private static final int DATABASE_VERSION = 3;
    private static final String nodesTableName = "nodes";
    private static final String nodesTablePrimaryKey = "node_id";
    private static final String nodesTableLatitude = "lat";
    private static final String nodesTableLongitude = "lon";
    private static final String nodesAttributesTableName = "nodes_attr";
    private static final String nodesAttributesTableKey = "key";
    private static final String nodesAttributesTableValue = "value";
    List<NodeReceiverInterface<OsmNode>> receiver;
    
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
        receiver = new LinkedList<NodeReceiverInterface<OsmNode>>();
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
                + "PRIMARY KEY (" + nodesTablePrimaryKey + "," + nodesAttributesTableKey + "));");
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
    
    void removeNodesWithoutOpeningHoursSet() {
        // TODO
        // DELETE * FROM nodesAttributesTableName WHERE nodesAttributesTableKey != 'opening_hours'
        // DELETE * FROM nodesTableName WHERE !(nodesAttributesTableName.nodesTablePrimaryKey = nodesTablePrimaryKey)
        SQLiteDatabase db = getWritableDatabase();
//        db.delete(nodesAttributesTableName, nodesAttributesTableKey + " != 'opening_hours'", null);
        
        String query = "DELETE FROM " + nodesAttributesTableName + " WHERE " + nodesTablePrimaryKey
                + " NOT IN ( SELECT " + nodesTablePrimaryKey + " FROM " + nodesAttributesTableName + " WHERE " + nodesAttributesTableKey + " = opening_hours " + " )";
        // TODO continue
        
//        int size3 = db.delete(nodesTableName, "not ( " + nodesTablePrimaryKey + " = " + nodesAttributesTableName + "." + nodesTablePrimaryKey + " )", null);
//        String query = "DELETE FROM " + nodesTableName + " WHERE " + nodesTablePrimaryKey 
//                + " IN ( SELECT ntn." + nodesTablePrimaryKey + " FROM " + nodesTableName + " ntn, " + nodesAttributesTableName + " natn WHERE NOT ( ntn." + nodesTablePrimaryKey + " = natn." + nodesTablePrimaryKey + ") )";
//        db.execSQL(query);
        
//        int size = db.query(nodesTableName, null, null, null, null, null, null).getCount();
        Cursor c = db.query(nodesAttributesTableName, null, null, null, null, null, null);
        while (c.moveToNext()) {
            int key_index = c.getColumnIndex(nodesAttributesTableKey);
            int value_index = c.getColumnIndex(nodesAttributesTableValue);
            String content = c.getString(key_index) + ", " + c.getString(value_index);
            content.toString();
        }
    }

// old query: 
// select * from table NODESTABLENAME
//   where LONGITUDE >= left and LONGITUDE <= right and LATITUDE >= bottom and LATITUDE <= top;
// new query:
// select n.PRIMARYKEY, n.LONGITUDE, n.LATITUDE from table NODES n INNER JOIN NODESATTRIBUTS na ON n.PRIMARYKEY = na.PRIMARYKEY
//   where n.LONGITUDE >= left and n.LONGITUDE <= right and n.LATITUDE >= bottom and n.LATITUDE <= top
//     and (na.KEY = st1 or na.VALUE = st1 or na.key = st2 or na.value = st2 or ...);
// 
// it seems as db.query() cannot do JOIN
// select n.PRIMARYKEY, n.LONGITUDE, n.LATITUDE from table NODES n, NODESATTRIBUTS na
//  where n.LONGITUDE >= left and n.LONGITUDE <= right and n.LATITUDE >= bottom and n.LATITUDE <= top
//   and n.PRIMARYKEY = na.PRIMARYKEY
//   and (na.KEY = st1 or na.VALUE = st1 or na.key = st2 or na.value = st2 or ...);
    private Cursor queryNodesFromMapExtract(int left, int top, int right, int bottom, String[] search_terms) {
        SQLiteDatabase db = getReadableDatabase();
        /*
        StringBuilder sb = new StringBuilder();
        sb.append("select n." + nodesTablePrimaryKey + ", n." + nodesTableLatitude + ", n." + nodesTableLongitude);
        sb.append(" from " + nodesTableName + " n inner join " + nodesAttributesTableName + " na on n." + nodesTablePrimaryKey + " = na." + nodesTablePrimaryKey);
        sb.append(" where n." + nodesTableLongitude + " >= ? AND n." + nodesTableLongitude + " <= ? AND n." + nodesTableLatitude + " >= ? AND n." + nodesTableLatitude + " <= ?");
        if (search_terms != null && search_terms.length > 0) {
            sb.append(" and ( ");
            for (int i = 0; i < search_terms.length-1; i++) {
                // SELECT * FROM suppliers
                //  WHERE supplier_name like '%bob%';
                sb.append("na." + nodesAttributesTableKey + " like '%?%' or ");
                sb.append("na." + nodesAttributesTableValue + " like '%?%' or ");
            }
            sb.append("na." + nodesAttributesTableKey + " like '%?%' or ");
            sb.append("na." + nodesAttributesTableValue + " like '%?%'");
            sb.append(")");
        }
        
        int size = 4;
        if (search_terms != null)
            size += 2*search_terms.length;
        List<String> selectionArgsList = new ArrayList<String>(size);
        selectionArgsList.add("" + left);
        selectionArgsList.add("" + right);
        selectionArgsList.add("" + bottom);
        selectionArgsList.add("" + top);
        if (search_terms != null)
            for (String search_term : search_terms) {
                selectionArgsList.add(search_term);
                selectionArgsList.add(search_term);
            }
        return db.rawQuery(sb.toString(), selectionArgsList.toArray(new String[] {}));
        */
        
        return db.query(nodesTableName, null,
                nodesTableLongitude + " >= ? AND " + nodesTableLongitude + " <= ? AND " + nodesTableLatitude + " >= ? AND " + nodesTableLatitude + " <= ?",
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
        put(node, false);
    }
    
    public void put(OsmNode node, boolean hasBeenEdited) {
        SQLiteDatabase db = getWritableDatabase();
        
        Cursor c = db.query(nodesTableName, null, nodesTablePrimaryKey + " = ?", new String[] {"" + node.getID()}, null, null, null);
        boolean isUpdate = c.getCount() > 0;
        c.close();
        
        ContentValues cv = new ContentValues(3);
        cv.put(nodesTablePrimaryKey, node.getID());
        cv.put(nodesTableLatitude, node.getLatitudeE6());
        cv.put(nodesTableLongitude, node.getLongitudeE6());
        if (!isUpdate)
            db.insert(nodesTableName, null, cv);
        else
            db.update(nodesTableName, cv, nodesTablePrimaryKey + " = ?", new String[] {"" + node.getID()});
        
        for (String key : node.getKeys()) {
            c = db.query(nodesAttributesTableName, null, nodesTablePrimaryKey + " = ? AND " + nodesAttributesTableKey + " = ?", new String[] {"" + node.getID(), key}, null, null, null);
            boolean isAttributeUpdate = c.getCount() > 0;
            c.close();
            
            String value = node.getAttribute(key);
            cv = new ContentValues(3);
            cv.put(nodesTablePrimaryKey, node.getID());
            cv.put(nodesAttributesTableKey, key);
            cv.put(nodesAttributesTableValue, value);
            if (!isAttributeUpdate)
                db.insert(nodesAttributesTableName, null, cv);
            else
                db.update(nodesAttributesTableName, cv, nodesTablePrimaryKey + " = ? AND " + nodesAttributesTableKey + " = ?", new String[] {"" + node.getID(), key});
        }
        
        // check if the node contains updated values and is displayed
        if (!isUpdate || hasBeenEdited)
            for (NodeReceiverInterface<OsmNode> irec : receiver)
                irec.put(node);
        
        Log.d(getClass().getSimpleName(), "There are " + queryNodes().getCount() + " nodes in the database");
    }
    
    private OsmNode createNodeFromRow(Cursor c) {
        int keyIndex = c.getColumnIndexOrThrow(nodesTablePrimaryKey);
        int latIndex = c.getColumnIndexOrThrow(nodesTableLatitude);
        int lonIndex = c.getColumnIndexOrThrow(nodesTableLongitude);
        int id = c.getInt(keyIndex);
        int latitude = c.getInt(latIndex);
        int longitude = c.getInt(lonIndex);
        return new OsmNode(id, latitude, longitude);
    }
    
    private void addAttributesToNode(OsmNode node) {
        Cursor c = queryAttributes(node.getID());
        while (c.moveToNext()) {
            int keyIndex = c.getColumnIndexOrThrow(nodesAttributesTableKey);
            int valueIndex = c.getColumnIndexOrThrow(nodesAttributesTableValue);
            node.putAttribute(c.getString(keyIndex), c.getString(valueIndex));
        }
        c.close();
    }
    
    @Override
    public HashMap<Integer, OsmNode> getNodesFromMapExtract(int left, int top, int right, int bottom, String[] search_terms) {
        return createNodesFromRows(queryNodesFromMapExtract(left, top, right, bottom, search_terms), search_terms);
    }
    
    private HashMap<Integer, OsmNode> createNodesFromRows(Cursor c, String[] search_terms) {
        HashMap<Integer, OsmNode> nodesMap = new HashMap<Integer, OsmNode>();
        while (c.moveToNext()) {
            OsmNode node = createNodeFromRow(c);
            addAttributesToNode(node);
            if (nodeMatchesSearchTerms(node, search_terms)) {
                nodesMap.put(node.getID(), node);
            }
        }
        c.close();
        return nodesMap;
    }
    
    public void addListener(NodeReceiverInterface<OsmNode> irec) {
        receiver.add(irec);
    }
    
    void removeListener(NodeReceiverInterface<OsmNode> irec) {
        receiver.remove(irec);
    }
    
    public boolean nodeMatchesSearchTerms(OsmNode node, String[] search_terms) {
        // check if there was no search or a search term matches
        if (search_terms == null || search_terms.length == 0)
            return true;
        boolean search_matches = false;
        for (String key : node.getKeys()) {
            String tag_value = node.getAttribute(key);
            search_matches |= checkSearchStringForTagAndValue(key.toLowerCase(), tag_value.toLowerCase(), search_terms);
        }
        return search_matches;
    }

    static boolean checkSearchStringForTagAndValue(String tag, String value, String[] search_terms) {
        if (search_terms == null)
            return true;
        for (String seach_term : search_terms) {
            if (tag.contains(seach_term) || value.contains(seach_term))
                return true;
        }
        return false;
    }
}
