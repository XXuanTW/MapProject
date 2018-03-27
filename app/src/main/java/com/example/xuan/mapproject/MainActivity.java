package com.example.xuan.mapproject;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int READ_CONTACTS_REQUEST = 1;
    private GoogleMap mMap;

    String Url = "http://10.0.2.2/index.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //權限
        getPermissionToReadUserContacts();
        //google mapFragment 設定
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    class TransTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(params[0]);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                String line = in.readLine();
                while (line != null) {
                    Log.d("HTTP", line);
                    sb.append(line);
                    line = in.readLine();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("JSON", s);
            parseJSON(s);
        }
    }

    private void parseJSON(String s) {
        ArrayList<Transaction> trans = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(s);
            for (int i=0; i<array.length(); i++){
                JSONObject obj = array.getJSONObject(i);
                int id = obj.getInt("id");
                int x = obj.getInt("x");
                int y = obj.getInt("y");
                String name = obj.getString("name");
                Transaction t = new Transaction(id, x, y, name);

                //標籤
                mMap.addMarker(new MarkerOptions().position(new LatLng(x,y)).title(name));

                trans.add(t);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//google 功能設定
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //GPS定位
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        //放大縮小
        mMap.getUiSettings().setZoomControlsEnabled(true);
//        //標籤
//        mMap.addMarker(new MarkerOptions().position(new LatLng(10, 10)).title("100"));
        new TransTask()
                .execute(Url);
    }

    //權限
    public void getPermissionToReadUserContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED){
            {
            //解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
                Toast.makeText(this, "需要定位位置權限以及儲存權限才可以正常工作", Toast.LENGTH_SHORT).show();
            }
            //发起请求获得用户许可,可以在此请求多个权限
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET},
                    READ_CONTACTS_REQUEST);
            }
        }
    }


}
