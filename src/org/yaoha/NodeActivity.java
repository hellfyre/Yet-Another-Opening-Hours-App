package org.yaoha;

import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class NodeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        Bundle b = i.getExtras();
        String addr_street = "", addr_postal_code = "", addr_city = "", addr_number = "", shop_name = "", opening_hours = "";

        ScrollView sv = new ScrollView(this);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        sv.addView(ll);
      //List<String> nodeElements = new LinkedList<String>();
        Set<String> keySet = b.keySet();
      for (String key : keySet) {
          //nodeElements.add(key);
          if (key.equals("addr:street")){
              addr_street = b.getString(key);
          }
          else if (key.equals("addr:city")){
              addr_city = b.getString(key);
          }
          else if (key.equals("addr:housenumber")){
              addr_number = b.getString(key);
          }
          else if (key.equals("addr:postcode")){
              addr_postal_code = b.getString(key);
          }
          else if (key.equals("shop") || key.equals("name")){
              shop_name = b.getString(key);
          }
          else if (key.equals("opening_hours")) {
              opening_hours = b.getString(key);
          }
      }
      String addr_complete = addr_street + " " + addr_number + "\n" + addr_postal_code + " " + addr_city;
      TextView name = new TextView(getApplicationContext());
      EditText value = new EditText(getApplicationContext());
      TextView name2 = new TextView(getApplicationContext());
      EditText value2 = new EditText(getApplicationContext());
      TextView name3 = new TextView(getApplicationContext());
      EditText value3 = new EditText(getApplicationContext());      
      name.setText("Name");
      ll.addView(name);
      value.setText(shop_name);
      value.setKeyListener(null);
      ll.addView(value);
      
      name2.setText("Location");
      ll.addView(name2);
      value2.setText(addr_complete);
      value2.setKeyListener(null);
      ll.addView(value2);
      
      name3.setText("Opening hours");
      ll.addView(name3);
      value3.setText(opening_hours);
      value3.setKeyListener(null);
      ll.addView(value3);
//      for (String key : keySet) {
//          TextView name = new TextView(getApplicationContext());
//          String tag = tagToReadable(key);
//          name.setText(tag);
//          ll.addView(name);
//          EditText value = new EditText(getApplicationContext());
//          value.setText(b.getString(key));
//          value.setKeyListener(null);
//          ll.addView(value);
//      }
      this.setContentView(sv);
    }
    
//    public String tagToReadable(String tag){
//        if (tag.equals("shop")) {
//            tag = "Type";
//        }
//        else if (tag.equals("addr:postcode")) {
//            tag = "Postal code";
//        }
//        else if (tag.equals("source")) {
//            tag = "Source";
//        }
//        else if (tag.equals("wheelchair")) {
//            tag = "Wheelchair?";
//        }
//        else if (tag.equals("shop") || tag.equals("name")) {
//            tag = "Name of shop";
//        }
//        else if (tag.equals("addr:city")) {
//            tag = "City";
//        }
//        else if (tag.equals("opening_hours")) {
//            tag = "Opening hours";
//        }
//        else if (tag.equals("addr:housenumber")) {
//            tag = "Housenumber";
//        }
//        else if (tag.equals("website")) {
//            tag = "Website";
//        }
//        else if (tag.equals("fax")) {
//            tag = "Fax";
//        }
//        else if (tag.equals("phone")) {
//            tag = "Phone";
//        }
//        else if (tag.equals("cuisine")) {
//            tag = "Cuisine";
//        }
//        else if (tag.equals("description")) {
//            tag = "Description";
//        }
//        else if (tag.equals("addr:country")) {
//            tag = "Country";
//        }
//        else if (tag.equals("amenity")) {
//            tag = "Amenity";
//        }
//        else {
//        }
//        return tag;
//    }

}
