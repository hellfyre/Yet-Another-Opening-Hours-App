package uber.lutz;

import android.app.Activity;
import org.osmdroid.views.MapView;

import android.os.Bundle;

public class OsmdroidtestActivity extends Activity {
    
    MapView mapview;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mapview = (MapView)findViewById(R.id.mapview);
        mapview.setBuiltInZoomControls(true);
    }
}