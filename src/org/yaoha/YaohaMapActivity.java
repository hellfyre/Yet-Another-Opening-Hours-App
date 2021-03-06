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

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class YaohaMapActivity extends Activity implements LocationListener {
    MapView mapview;
    MapController mapController;
    LocationManager locationManager;
    MyLocationNewOverlay mOverlay;
    NodesOverlay no;
    static final GeoPoint braunschweig = new GeoPoint(52265000, 10525000);
    
    String search_term = "";
    
    SharedPreferences mprefs = null;
    SharedPreferences default_shared_prefs = null;
    
    boolean editMode = false;
    YaohaMapListener mapListener;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);
        mapview = (MapView) findViewById(R.id.mapview);
        mapview.setBuiltInZoomControls(true);
        mapview.setMultiTouchControls(true);
        mapController = (MapController) this.mapview.getController();
        
        // just save the search term
        // we may later add a method to actually look in maps after it
        Intent intent = getIntent();
        CharSequence text = intent.getCharSequenceExtra("org.yaoha.YaohaMapActivity.SearchText");
        search_term = text.toString();
        Log.i(YaohaMapActivity.class.getSimpleName(), "Search field input was: " + text);
        
        no = new NodesOverlay(getResources().getDrawable(R.drawable.dontknow), new org.osmdroid.DefaultResourceProxyImpl(mapview.getContext()), this, mapview, OsmNodeDbHelper.getInstance(), search_term);
        
        mapview.getOverlays().add(no);
        mapview.setMapListener(mapListener = new YaohaMapListener(this, no));

        mprefs = getPreferences(MODE_PRIVATE);
        default_shared_prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        assert(mprefs != default_shared_prefs);
        int zoom = -1;//Integer.parseInt(default_shared_prefs.getString("zoomlevel", "-1"));
        zoom = mprefs.getInt("zoomlevel", zoom);
        //Toast.makeText(this, "Set zoom to " + zoom +"m!", Toast.LENGTH_LONG).show();
        //mapController.setZoom(prefs.getInt("zoomlevel", 15));

        // hardcoded default is braunschweig
        int longitude = mprefs.getInt("longitude", braunschweig.getLongitudeE6());
        int latitude = mprefs.getInt("latitude", braunschweig.getLatitudeE6());
        GeoPoint myPosition = new GeoPoint(latitude, longitude);
        
        // get saved values
        if (savedInstanceState != null) {
            savedInstanceState.getInt("zoomlevel", zoom);
            myPosition.setLatitudeE6(savedInstanceState.getInt("latitude", myPosition.getLatitudeE6()));
            myPosition.setLongitudeE6(savedInstanceState.getInt("longitude", myPosition.getLongitudeE6()));
        }
        
     // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        
        if (loc != null) {
            myPosition = new GeoPoint(loc);
            Log.i(YaohaMapActivity.class.getSimpleName(), "last known location is " + myPosition);
        }
        else {
            Log.i(YaohaMapActivity.class.getSimpleName(), "no last known location " + myPosition);
        }

        mapController.setZoom(zoom);
        mapController.setCenter(myPosition);
        setNodes();
     // Register the listener with the Location Manager to receive location updates
//        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 300, 200, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000, 200, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 200, this);
        

        mOverlay = new MyLocationNewOverlay(this, mapview); //TODO
        mapview.getOverlays().add(mOverlay);
        mapview.postInvalidate();
        mOverlay.enableMyLocation();
        
        BoundingBoxE6 bb = mapview.getBoundingBox();
        int west = mprefs.getInt("west", bb.getLonWestE6()-1000);
        int east = mprefs.getInt("east", bb.getLonEastE6()+1000);
        int north = mprefs.getInt("north", bb.getLatNorthE6()+1000);
        int south = mprefs.getInt("south", bb.getLatSouthE6()-1000);
        mapListener.update(new BoundingBoxE6(north, east, south, west));
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        //GeoPoint myPosition = new GeoPoint(location);
        //mapController.setCenter(myPosition); //TODO  causes random jumps to mylocation
        // Remove the listener you previously added
//        locationManager.removeUpdates(this);
        setNodes();
        Log.i(this.getClass().getSimpleName(), "got location update from " + location.getProvider());
        Log.i(this.getClass().getSimpleName(), "new location is " + location.getLatitude() + "," + location.getLongitude());
    }
    

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
        
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);
        return true;
    }
    
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.edit_mode).setChecked(editMode);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.track_me:
            Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc == null) {
                Toast.makeText(this, "No location known *crash*", Toast.LENGTH_LONG).show();
            } else {
                IGeoPoint myPosition = new GeoPoint(loc);
                //mapController.setCenter(myPosition);
                mapController.setZoom(17);
                mapController.animateTo(myPosition);
                Toast.makeText(this, "Tracking me!", Toast.LENGTH_LONG).show();
            }
            return true;
        case R.id.debug_track_me:
            mapController.setZoom(17);
            mapController.setCenter(braunschweig);
            Toast.makeText(this, "DEBUG Tracking me!", Toast.LENGTH_LONG).show();
            return true;
        case R.id.edit_mode:
            editMode = true ^ editMode;
            String message = new String("Editmode ");
            if (editMode)
                message += "enabled";
            else
                message += "disabled";
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            if (editMode)
                mapListener.requeryBoundingBox();
            else
                OsmNodeDbHelper.getInstance().removeNodesWithoutOpeningHoursSet();
            return true;
        case R.id.nodes_Update:
            setNodes();
        }
        return false;
    }
    @Override
    protected void onResume() {
        super.onResume();
        mOverlay.setEnabled(true);//  enableMyLocation();
    }
    
    private void setNodes(){
        TextView t=new TextView(this); 
        t=(TextView)findViewById(R.id.numberOfNodesText); 
        String nodes = ""+OsmNodeDbHelper.number_of_nodes;
        try {
            t.setText("Nodes in your Database: " + nodes);
        } catch (Exception e) {
            t.setText(e.getMessage());
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mOverlay.setEnabled(false);
        

        IGeoPoint center = mapview.getMapCenter();
        Editor editor = mprefs.edit();
        editor.putInt("zoomlevel", mapview.getZoomLevel());
        editor.putInt("longitude", center.getLongitudeE6());
        editor.putInt("latitude", center.getLatitudeE6());
        
        BoundingBoxE6 bb = mapview.getBoundingBox();
        editor.putInt("west", bb.getLonWestE6());
        editor.putInt("east", bb.getLonEastE6());
        editor.putInt("north", bb.getLatNorthE6());
        editor.putInt("south", bb.getLatSouthE6());
        editor.commit();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        IGeoPoint center = mapview.getMapCenter();
        outState.putInt("zoomlevel", mapview.getZoomLevel());
        outState.putInt("longitude", center.getLongitudeE6());
        outState.putInt("latitude", center.getLatitudeE6());
        
        BoundingBoxE6 bb = mapview.getBoundingBox();
        outState.putInt("west", bb.getLonWestE6());
        outState.putInt("east", bb.getLonEastE6());
        outState.putInt("north", bb.getLatNorthE6());
        outState.putInt("south", bb.getLatSouthE6());
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int zoom_level = savedInstanceState.getInt("zoomlevel");
        int longitude = savedInstanceState.getInt("longitude");
        int latitude = savedInstanceState.getInt("latitude");
        
        mapController.setZoom(zoom_level);
        mapController.setCenter(new GeoPoint(latitude, longitude));
    }
}
