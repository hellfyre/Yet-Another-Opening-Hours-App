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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

public class YaohaActivity extends Activity implements OnClickListener {
    private static Context staticApplicationContext;
    Button startButton;
    ImageButton button_favorite_1, button_favorite_2, button_favorite_3, button_favorite_4, button_favorite_5, button_favorite_6;
    ImageButton actualButton;
    final static String EDIT_FAV_STRING = "edit favorite";
    final static String EDIT_FAV_PIC = "edit picture";
    final static String REMOVE_FAV = "remove favorite";
    TextView text_fav_1, text_fav_2, text_fav_3, text_fav_4, text_fav_5, text_fav_6;
    final static int SELECT_PICTURE = 1;
    Uri selectedImageUri;

    private SharedPreferences prefs;
    private static final String[] SHOP_TYPES = new String[] {
        "groceries", "computer", "sport", "clothes", "gas station"
    };
    
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (staticApplicationContext == null) staticApplicationContext = getApplicationContext();
        
        this.prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        text_fav_1 = new TextView(this);
        text_fav_1.setText(R.string.add_favorite);
        text_fav_2 = new TextView(this);
        text_fav_2.setText(R.string.add_favorite);
        text_fav_3 = new TextView(this);
        text_fav_3.setText(R.string.add_favorite);
        text_fav_4 = new TextView(this);
        text_fav_4.setText(R.string.add_favorite);
        text_fav_5 = new TextView(this);
        text_fav_5.setText(R.string.add_favorite);
        text_fav_6 = new TextView(this);
        text_fav_6.setText(R.string.add_favorite);


        startButton = (Button) findViewById(R.id.button_start);
        startButton.setOnClickListener(this);
        button_favorite_1 = (ImageButton) findViewById(R.id.button_fav_1);
        button_favorite_1.setOnClickListener(this);
//        button_favorite_1.setScaleType(F)
        button_favorite_2 = (ImageButton) findViewById(R.id.button_fav_2);
        button_favorite_2.setOnClickListener(this);
        button_favorite_3 = (ImageButton) findViewById(R.id.button_fav_3);
        button_favorite_3.setOnClickListener(this);
        button_favorite_4 = (ImageButton) findViewById(R.id.button_fav_4);
        button_favorite_4.setOnClickListener(this);
        button_favorite_5 = (ImageButton) findViewById(R.id.button_fav_5);
        button_favorite_5.setOnClickListener(this);
        button_favorite_6 = (ImageButton) findViewById(R.id.button_fav_6);
        button_favorite_6.setOnClickListener(this);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, SHOP_TYPES);
        MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.searchTextfield);
        textView.setAdapter(adapter);
        textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        
        registerForContextMenu(button_favorite_1);
        registerForContextMenu(button_favorite_2);
        registerForContextMenu(button_favorite_3);
        registerForContextMenu(button_favorite_4);
        registerForContextMenu(button_favorite_5);
        registerForContextMenu(button_favorite_6);
        
//        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(getBaseContext()); 
        if (prefs.getBoolean("start_with_map",false) == true) {
            startButton.performClick();
        }
        OsmNodeDbHelper.create(getApplicationContext());
        
        getFavSettings();
    }
    
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.buy_pro:
                Toast.makeText(this, "You just payed 49,99€. Enjoy this Pro-Version!", Toast.LENGTH_LONG).show();
                return true;
            case R.id.setOSM:
                Intent intent = new Intent(this, OSMSettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.connectToOSM:
                connectToOSM();
                return true;
            default:
                return false;
        }
    }
    
//    private void checkExMediaState(){
//        String state = Environment.getExternalStorageState();
//        if (Environment.MEDIA_MOUNTED.equals(state)) {
//            // We can read and write the media
//            mExternalStorageAvailable = mExternalStorageWriteable = true;
//        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
//            // We can only read the media
//            mExternalStorageAvailable = true;
//            mExternalStorageWriteable = false;
//        } else {
//            // Something else is wrong. It may be one of many other states, but all we need
//            //  to know is we can neither read nor write
//            mExternalStorageAvailable = mExternalStorageWriteable = false;
//        }
//    }
    
    
    private void getFavSettings(){
        //Toast.makeText(this, prefs.getString("saved_fav_1_text", "-1"), Toast.LENGTH_LONG).show();
        if (prefs.getString("saved_fav_1_text", "-1")!= "-1") {
            text_fav_1.setText(prefs.getString("saved_fav_1_text", "-1"));
            button_favorite_1.setImageDrawable(readDrawableFromSD("pic_fav_1.jpg"));
        }
        if (prefs.getString("saved_fav_2_text", "-1")!= "-1") {
            text_fav_2.setText(prefs.getString("saved_fav_2_text", "-1"));
            button_favorite_2.setImageDrawable(readDrawableFromSD("pic_fav_2.jpg"));
        }
        if (prefs.getString("saved_fav_3_text", "-1")!= "-1") {
            text_fav_3.setText(prefs.getString("saved_fav_3_text", "-1"));
            button_favorite_3.setImageDrawable(readDrawableFromSD("pic_fav_3.jpg"));
        }
        if (prefs.getString("saved_fav_4_text", "-1")!= "-1") {
            text_fav_4.setText(prefs.getString("saved_fav_4_text", "-1"));
            button_favorite_4.setImageDrawable(readDrawableFromSD("pic_fav_4.jpg"));
        }
        if (prefs.getString("saved_fav_5_text", "-1")!= "-1") {
            text_fav_5.setText(prefs.getString("saved_fav_5_text", "-1"));
            button_favorite_5.setImageDrawable(readDrawableFromSD("pic_fav_5.jpg"));
        }
        if (prefs.getString("saved_fav_6_text", "-1")!= "-1") {
            text_fav_6.setText(prefs.getString("saved_fav_6_text", "-1"));
            button_favorite_6.setImageDrawable(readDrawableFromSD("pic_fav_6.jpg"));
        }
    }
    
    private void connectToOSM(){
        String url = "http://openstreetmap.org";
        //HttpURLConnection request = null;
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        String token = prefs.getString(OAuth.OAUTH_TOKEN, null);
        String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, null);
        
        OAuthConsumer OSMconsumer = new CommonsHttpOAuthConsumer(token, secret);
        OSMconsumer.setTokenWithSecret(token, secret);
        try {
            Toast.makeText(this, makeSecuredReq(url, OSMconsumer), Toast.LENGTH_LONG).show();
            //Toast.makeText(this, "Response: " + response.getStatusLine(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            String err = e.getMessage();
            Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        }
    }
 
    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap); 
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    
    public void writeDrawableToSD(Drawable pic, String fileName){
        File file = new File(getExternalFilesDir(null), fileName);
        //Drawable pic = button_favorite_1.getDrawable();
        Bitmap bitmap = drawableToBitmap(pic);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Drawable readDrawableFromSD(String fileName){
        File file = new File(getExternalFilesDir(null), fileName);
        Drawable d;
        if (file.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            d = new BitmapDrawable(getResources(),myBitmap);
        } else {
            Toast.makeText(this, "Could not find File, loading default picture.",Toast.LENGTH_LONG).show();
            d = getResources().getDrawable(R.drawable.placeholder_logo);
        }
        return d;
    }
    
    private String determineStoreIcon(String storeName) { //TODO
        if (storeName == "") {
            return "something else";
        } else {
            return "standard";
        }
    }
    
    
	private String makeSecuredReq(String url,OAuthConsumer consumer) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
    	HttpGet request = new HttpGet(url);
    	Log.d(C.TAG,"Requesting URL : " + url);
    	consumer.sign(request);
    	HttpResponse response = httpclient.execute(request);
    	Log.d(C.TAG,"Statusline : " + response.getStatusLine());
    	InputStream data = response.getEntity().getContent();
    	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(data));
        String responeLine;
        StringBuilder responseBuilder = new StringBuilder();
        while ((responeLine = bufferedReader.readLine()) != null) {
        	responseBuilder.append(responeLine);
        }
        Log.d(C.TAG,"Response : " + responseBuilder.toString());
        return responseBuilder.toString();
	}	
    
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_start) {
            MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.searchTextfield);
            searchMapWithKey(textView.getText());
        }
        if(v.getId() == R.id.button_fav_1) {
            if (text_fav_1.getText().equals(getText(R.string.add_favorite))) {
                openFavMenu(button_favorite_1, text_fav_1);
            } else {
                Toast.makeText(this, "Looking for: "+ text_fav_1.getText(), Toast.LENGTH_LONG).show();
                searchMapWithKey(text_fav_1.getText());
            }
        }
        if(v.getId() == R.id.button_fav_2) {
            if (text_fav_2.getText().equals(getText(R.string.add_favorite))) {
                openFavMenu(button_favorite_2, text_fav_2);
            } else {
                Toast.makeText(this, "Looking for: "+ text_fav_2.getText(), Toast.LENGTH_LONG).show();
                searchMapWithKey(text_fav_2.getText());
            }
        }
        if(v.getId() == R.id.button_fav_3) {
            if (text_fav_3.getText().equals(getText(R.string.add_favorite))) {
                openFavMenu(button_favorite_3, text_fav_3);
            } else {
                Toast.makeText(this, "Looking for: "+ text_fav_3.getText(), Toast.LENGTH_LONG).show();
                searchMapWithKey(text_fav_3.getText());
            }
        }
        if(v.getId() == R.id.button_fav_4) {
            if (text_fav_4.getText().equals(getText(R.string.add_favorite))) {
                openFavMenu(button_favorite_4, text_fav_4);
            } else {
                Toast.makeText(this, "Looking for: "+ text_fav_4.getText(), Toast.LENGTH_LONG).show();
                searchMapWithKey(text_fav_4.getText());
            }
        }
        if(v.getId() == R.id.button_fav_5) {
            if (text_fav_5.getText().equals(getText(R.string.add_favorite))) {
                openFavMenu(button_favorite_5, text_fav_5);
            } else {
                Toast.makeText(this, "Looking for: "+ text_fav_5.getText(), Toast.LENGTH_LONG).show();
                searchMapWithKey(text_fav_5.getText());
            }
        }
        if(v.getId() == R.id.button_fav_6) {
            if (text_fav_6.getText().equals(getText(R.string.add_favorite))) {
                openFavMenu(button_favorite_6, text_fav_6);
            } else {
                Toast.makeText(this, "Looking for: "+ text_fav_6.getText(), Toast.LENGTH_LONG).show();
                searchMapWithKey(text_fav_6.getText());
            }
        }
    }
    
    public void searchMapWithKey(CharSequence keyword){
        Intent intent = new Intent(this, YaohaMapActivity.class);
        intent.putExtra("org.yaoha.YaohaMapActivity.SearchText", keyword);
        startActivity(intent); 
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Action");  
        menu.add(0, v.getId(), 0, EDIT_FAV_STRING);
        menu.add(0, v.getId(), 0, EDIT_FAV_PIC);
        menu.add(0, v.getId(), 0, REMOVE_FAV);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == button_favorite_1.getId()){
            editFavs(item, button_favorite_1, text_fav_1);
        }else if (item.getItemId() == button_favorite_2.getId()){
            editFavs(item, button_favorite_2, text_fav_2);
        }else if (item.getItemId() == button_favorite_3.getId()){
            editFavs(item, button_favorite_3, text_fav_3);
        }else if (item.getItemId() == button_favorite_4.getId()){
            editFavs(item, button_favorite_4, text_fav_4);
        }else if (item.getItemId() == button_favorite_5.getId()){
            editFavs(item, button_favorite_5, text_fav_5);
        }else if (item.getItemId() == button_favorite_6.getId()){
            editFavs(item, button_favorite_6, text_fav_6);
        }
    return super.onContextItemSelected(item);
    }
    
    public boolean editFavs(MenuItem item, final ImageButton btn, final TextView tv){
        if(item.getTitle()==EDIT_FAV_STRING){
            openFavMenu(btn, tv);
        } else if (item.getTitle()==EDIT_FAV_PIC){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            actualButton = btn;  //workaround, there must be a better way
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
        } else if (item.getTitle()==REMOVE_FAV){
           tv.setText(getText(R.string.add_favorite));
           btn.setImageResource(R.drawable.plus_sign_small);
           String tmp="";
           final Editor edit = prefs.edit();
           if (btn.getId() == button_favorite_1.getId()){
               tmp = "saved_fav_1_text";
           } else if (btn.getId() == button_favorite_2.getId()){
               tmp = "saved_fav_2_text";
           } else if (btn.getId() == button_favorite_3.getId()){
               tmp = "saved_fav_3_text";
           } else if (btn.getId() == button_favorite_4.getId()){
               tmp = "saved_fav_4_text";
           } else if (btn.getId() == button_favorite_5.getId()){
               tmp = "saved_fav_5_text";
           } else if (btn.getId() == button_favorite_6.getId()){
               tmp = "saved_fav_6_text";
           }
           edit.remove(tmp);
           edit.commit();
        } else {
            Toast.makeText(this, "Placeholder - You schould never see this.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return super.onContextItemSelected(item);
    }

    private void openFavMenu(final ImageButton btn, final TextView tv) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this); 
        final EditText input = new EditText(this);
        alert.setTitle("Adding favorite");
        alert.setMessage("Enter your favorite search"); 
        alert.setView(input);

        alert.setPositiveButton("Set", new DialogInterface.OnClickListener() { 
            public void onClick(DialogInterface dialog, int whichButton) {
                final Editor edit = prefs.edit();
                String tmp="";
                tv.setText(input.getText());
                btn.setImageResource(R.drawable.placeholder_logo);
                if (btn.getId() == button_favorite_1.getId()){
                    tmp = "saved_fav_1_text";
                } else if (btn.getId() == button_favorite_2.getId()){
                    tmp = "saved_fav_2_text";
                } else if (btn.getId() == button_favorite_3.getId()){
                    tmp = "saved_fav_3_text";
                } else if (btn.getId() == button_favorite_4.getId()){
                    tmp = "saved_fav_4_text";
                } else if (btn.getId() == button_favorite_5.getId()){
                    tmp = "saved_fav_5_text";
                } else if (btn.getId() == button_favorite_6.getId()){
                    tmp = "saved_fav_6_text";
                }
                edit.putString(tmp, input.getText().toString());
                edit.commit();
                //TODO add method to catch pre-defined store-icons
            } 
            }); 

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { 
              public void onClick(DialogInterface dialog, int whichButton) { 
                // Canceled. 
              } 
            }); 
        alert.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == RESULT_OK) {
                if (requestCode == SELECT_PICTURE) {
                    selectedImageUri = data.getData();
                    Bitmap tmpbitmap = null;
                    try {
                        tmpbitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    int tmp_width = tmpbitmap.getWidth();
                    int tmp_height = tmpbitmap.getHeight();
                    Bitmap fav_bitmap = null;
                    if (tmp_width < tmp_height) {
                        fav_bitmap = Bitmap.createBitmap(tmpbitmap, 0, (tmp_height-tmp_width)/2, tmp_width, tmp_width);
                    } else if (tmp_height < tmp_width) {
                        fav_bitmap = Bitmap.createBitmap(tmpbitmap, (tmp_width-tmp_height)/2, 0, tmp_height, tmp_height);
                    } else {
                        fav_bitmap = tmpbitmap;
                    }
                    
                    actualButton.setImageBitmap(Bitmap.createScaledBitmap(fav_bitmap, actualButton.getWidth()-20, actualButton.getHeight()-20, false));
                    if (actualButton.getId() == button_favorite_1.getId()){
                        writeDrawableToSD(actualButton.getDrawable(), "pic_fav_1.jpg");
                    }
                }
            }
    }
    
    public void onBackPressed(){
        finish();
    }
    
    public static final Context getStaticApplicationContext() {
        return staticApplicationContext;
    }
}
