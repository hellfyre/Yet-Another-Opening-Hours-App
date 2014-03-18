/*
 *  This file is part of YAOHA.
 *
 *  YAOHA is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YAOHA is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YAOHA.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright 2012 Stefan Hobohm, Lutz Reinhardt, Matthias Uschok
 *
 */

package org.yaoha;

import java.util.ArrayList;
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
    private static final int DATABASE_VERSION = 5;
    private static final String nodesTableName = "nodes";
    private static final String nodesTablePrimaryKey = "node_id";
    private static final String nodesTableLatitude = "lat";
    private static final String nodesTableLongitude = "lon";
    private static final String nodesTableVersion = "version";
    private static final String nodesAttributesTableName = "nodes_attr";
    private static final String nodesAttributesTableKey = "key";
    private static final String nodesAttributesTableValue = "value";
    List<NodeReceiverInterface<OsmNode>> receiver;
    
    public static int number_of_nodes = -1;
    
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
                + nodesTableLongitude + " INTEGER NOT NULL, "
                + nodesTableVersion + " INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + nodesAttributesTableName + " (" + nodesTablePrimaryKey + " INTEGER NOT NULL, "
                + nodesAttributesTableKey + " TEXT NOT NULL, "
                + nodesAttributesTableValue + " TEXT, "
                + "FOREIGN KEY (" + nodesTablePrimaryKey + ") REFERENCES " + nodesTableName + " (" + nodesTablePrimaryKey + ") ON DELETE CASCADE, "
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
        removeNodesMissingTag("opening_hours");
    }
    
    void removeNodesMissingTag(String tag) {
        SQLiteDatabase db = getWritableDatabase();
        
        String query = "DELETE FROM " + nodesTableName + " WHERE " + nodesTablePrimaryKey
                + " NOT IN ( SELECT " + nodesTablePrimaryKey + " FROM " + nodesAttributesTableName + " WHERE " + nodesAttributesTableKey + " = '" + tag + "' )";
        db.execSQL(query);
        
        for (NodeReceiverInterface<OsmNode> rec : this.receiver)
            rec.requeryBoundingBox();
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
        // TODO only perform update if version is newer or has been edited
        SQLiteDatabase db = getWritableDatabase();
        
        Cursor c = db.query(nodesTableName, null, nodesTablePrimaryKey + " = ?", new String[] {"" + node.getID()}, null, null, null);
        boolean isUpdate = c.getCount() > 0;
        c.close();
        
        ContentValues cv = new ContentValues(3);
        cv.put(nodesTablePrimaryKey, node.getID());
        cv.put(nodesTableLatitude, node.getLatitudeE6());
        cv.put(nodesTableLongitude, node.getLongitudeE6());
        cv.put(nodesTableVersion, node.getVersion());
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
        number_of_nodes = queryNodes().getCount();
        Log.d(getClass().getSimpleName(), "There are " + number_of_nodes + " nodes in the database");
    }
    
    private OsmNode createNodeFromRow(Cursor c) {
        int keyIndex = c.getColumnIndexOrThrow(nodesTablePrimaryKey);
        int latIndex = c.getColumnIndexOrThrow(nodesTableLatitude);
        int lonIndex = c.getColumnIndexOrThrow(nodesTableLongitude);
        int verIndex = c.getColumnIndexOrThrow(nodesTableVersion);
        int id = c.getInt(keyIndex);
        int latitude = c.getInt(latIndex);
        int longitude = c.getInt(lonIndex);
        int version = c.getInt(verIndex);
        return new OsmNode(id, latitude, longitude, version);
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
        List<List<String>> must_can = splitSearchTermsIntoMustAndCan(search_terms);
        List<String> search_terms_must = must_can.get(0);
        List<String> search_terms_can = must_can.get(1);
        boolean search_matches_must = true;
        for (String must : search_terms_must) {
            search_matches_must &= checkNodeForSearchTerm(node, must);
        }
        boolean search_matches_can = search_terms_can.size() == 0;
        for (String can : search_terms_can) {
            search_matches_can |= checkNodeForSearchTerm(node, can);
        }
        
        return search_matches_can && search_matches_must;
    }
    
    static List<List<String>> splitSearchTermsIntoMustAndCan(String[] search_terms) {
        List<String> must = new LinkedList<String>();
        List<String> can = new LinkedList<String>();
        
        for (String item : search_terms) {
            if (item.length() == 0)
                continue;
            if (item.charAt(0) == '.' || item.charAt(0) == '&')
                must.add(item.substring(1).toLowerCase());
            else
                can.add(item.toLowerCase());
        }
        
        List<List<String>> items = new ArrayList<List<String>>();
        items.add(must);
        items.add(can);
        return items;
    }

    static boolean checkNodeForSearchTerm(OsmNode node, String search_term) {
        for (String key : node.getKeys()) {
            String value = node.getAttribute(key);
            if (key.toLowerCase().contains(search_term) || value.toLowerCase().contains(search_term))
                return true;
        }
        return false;
    }

    @Override
    public void requeryBoundingBox() {
    }
}
