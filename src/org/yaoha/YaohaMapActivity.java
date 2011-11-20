package org.yaoha;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class YaohaMapActivity extends Activity {
	MapView mapview;
	MapController mapController;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mapview);
	    mapview = (MapView) findViewById(R.id.mapview);
	    mapview.setBuiltInZoomControls(true);
	    mapview.setMultiTouchControls(true);
	    
	    mapController = this.mapview.getController();
	    SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    int zoom = Integer.parseInt(prefs.getString("zoomlevel", "-1"));
	    //Toast.makeText(this, "Set zoom to " + zoom +"m!", Toast.LENGTH_LONG).show();
        //mapController.setZoom(prefs.getInt("zoomlevel", 15));
	    mapController.setZoom(zoom);
        GeoPoint myPosition = new GeoPoint(53554070, -2959520);  // versteh mal jemand diese Angaben >.> (http://code.google.com/p/osmdroid/source/browse/trunk/osmdroid-android/src/org/osmdroid/util/GeoPoint.java?r=955)
        //GeoPoint myPosition = new GeoPoint(522667, 105333); // BS?
        mapController.setCenter(myPosition);
	}

}
