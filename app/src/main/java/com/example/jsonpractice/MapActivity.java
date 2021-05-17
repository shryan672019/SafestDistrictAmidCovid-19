package com.example.jsonpractice;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String stateData;
    ArrayList<Marker> mark = new ArrayList<Marker>();
   ArrayList<Map<String, String>> confirmedCasesDistrict = new ArrayList<Map<String, String>>();
   ArrayList<String> districtNames = new ArrayList<String>();
   Marker safestCityMarker;
class GeoLocation {
    public  Double [] getAddress(String locationName, Context context, Handler handle) {
        Double dimension[] = new Double[2];
        Geocoder geoArea = new Geocoder(context, Locale.getDefault());
        String result = "";
        try {
            List<Address> list = geoArea.getFromLocationName(locationName, 10);
            if (list != null && list.size() != 0) {
                Address address = list.get(0);
                dimension[0] = address.getLatitude();

                dimension[1] = address.getLongitude();

                return dimension;
            } else {
                Toast.makeText(context, "Data Not Available for district " + locationName, Toast.LENGTH_SHORT).show();
                return dimension;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

       return  dimension;
    }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent i = getIntent();
        stateData = i.getStringExtra("StateData");

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        GeoLocation location = new GeoLocation();

        try {
            JSONArray array = new JSONArray(stateData);
            JSONObject obj = new JSONObject(array.getString(1));
            JSONObject districtObject = new JSONObject(obj.getJSONObject("districtData").toString());
            mark = new ArrayList<Marker>();
             Iterator it = districtObject.keys();
             while(it.hasNext()){
                 String s = (String)it.next();
                 if(s.equals("Unknown")){

                 }else{
                     Double dimension[] =  location.getAddress(s,getApplicationContext(),new GeoHandler());
                     if(dimension.length>0) {
                         showLatLong(dimension, s,stateData);
                     }
                 }

             }


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Toast.makeText(MapActivity.this, "Click on any District Tag to see safest District", Toast.LENGTH_LONG).show();
                }
            }, 5000);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
              if(mark.contains(marker)){
                int index =  mark.indexOf(marker);
                Marker tempMarker = mark.get(index);

                  Toast.makeText(MapActivity.this,"Recovered Cases: "+(String) tempMarker.getTag(), Toast.LENGTH_SHORT).show();
              }

              return false;
            }
        });
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                computeSafestDistrict(confirmedCasesDistrict,districtNames);
            }
        });
        // Add a marker in Sydney and move the camera

    }

   void showLatLong(Double [] recordLatLang,String s,String stateData){

        if(recordLatLang[0]!=null && recordLatLang[1]!=null) {
            String confirmedCases="";String recoveredCases=""; Map<String,String> map = new HashMap<String,String>();
            try {
                JSONArray   array = new JSONArray(stateData);
                JSONObject obj = new JSONObject(array.getString(1));
                JSONObject districtObject = new JSONObject(obj.getJSONObject("districtData").toString());
                JSONObject district = districtObject.getJSONObject(s);
                confirmedCases = district.getString("confirmed");
                recoveredCases = district.getString("recovered");
                map.put(s,confirmedCases);
                confirmedCasesDistrict.add(map);
                districtNames.add(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            LatLng sydney = new LatLng(recordLatLang[0], recordLatLang[1]);
          Marker m =   mMap.addMarker(new MarkerOptions().position(sydney).title(s));

          m.setSnippet("Confirmed Covid Cases: "+ confirmedCases);
          m.setTag(recoveredCases);
          mark.add(m);
          m.showInfoWindow();
           mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        }
  }
    void computeSafestDistrict(ArrayList<Map<String,String>>confirmedData,ArrayList<String> districtNames){
        ArrayList<Integer> sortedCases = new ArrayList<Integer>();
        int i=0;String safestDistrict="";
        for(Map<String,String> m1:confirmedData){
            sortedCases.add(Integer.parseInt(m1.get(districtNames.get(i))));
            i++;
        }
        Collections.sort(sortedCases);
        Log.i("sortedCase",sortedCases.toString());

        int j=0;
        for(Map<String,String> m1:confirmedData){
            if(m1.get(districtNames.get(j)).equals((String.valueOf(sortedCases.get(0))))){
                safestDistrict= districtNames.get(j);
                j++;
            }else{
                j++;
            }
        }
        showsafestCity(safestDistrict);
    }


    void showsafestCity(String s){
        Toast.makeText(this, "SafestDistrict: "+s, Toast.LENGTH_LONG).show();
        GeoLocation location = new GeoLocation();
        Double [] recordLatLon=  location.getAddress(s,getApplicationContext(),new GeoHandler());
        LatLng sydney = new LatLng(recordLatLon[0], recordLatLon[1]);

        safestCityMarker =   mMap.addMarker(new MarkerOptions().position(sydney).title(s));
        if(mark.contains(safestCityMarker)==true){
            int i =mark.indexOf(safestCityMarker);
            Marker m1 = mark.get(i);
            m1.setVisible(false);
        }
        for(Marker m2:mark){
            m2.setVisible(false);
        }
        safestCityMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        safestCityMarker.setSnippet("District with least covid cases in the state");
        safestCityMarker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,10));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(5), 5000, null);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(MapActivity.this, "To see the route click on district Tag", Toast.LENGTH_LONG).show();
            }
        }, 3000);
    }




    private class GeoHandler extends Handler {

    }
}