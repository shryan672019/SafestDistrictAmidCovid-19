package com.example.jsonpractice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.AsyncTaskLoader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.util.Arrays;
import java.util.Map;

public class ShowDistrictActivity extends AppCompatActivity {

    private Button b1 ;
    private Spinner stateName;

    ProgressDialog dialog;
    public static TextView data;




    public class DownloadTask extends AsyncTask<String,String,String> {
        String result= "";
        @Override
        protected String doInBackground(String...voids ){

            URL url;

            try{
                url = new URL("https://api.covid19india.org/state_district_wise.json");
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();

                BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                String data =buffer.readLine();

                while(data.equals(null)==false){
                    if(data.contains(stateName.getSelectedItem().toString())==true){
                        data = data.replaceAll(":",",");
                        result= "[ "+data;
                        while(data.contains("statecode")==false){
                            data = buffer.readLine();
                            result+=data;

                        }
                        data = buffer.readLine();
                        result= result +data+" ]";
                        break;
                    }
                    data = buffer.readLine();
                }

            }catch(Exception e){
                e.printStackTrace();

            }
            return result;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            Intent i = new Intent(ShowDistrictActivity.this, MapActivity.class);
            i.putExtra("StateData",s);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    dialog.dismiss();
                }
            }, 3000);
            startActivity(i);







        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_district);
        b1 = findViewById(R.id.button);
        stateName =(Spinner) findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.states_name, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateName.setAdapter(adapter);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadTask task = new DownloadTask();
                task.execute();
             dialog = ProgressDialog.show(ShowDistrictActivity.this, "",
                        "Loading. Please wait...", true);
            }
        });

    }
}