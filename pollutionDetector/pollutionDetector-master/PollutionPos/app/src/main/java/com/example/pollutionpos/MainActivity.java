package com.example.pollutionpos;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static Context context;
    private TextView latitude, longitude, twaqi, twpm10, twpm25;
    private String fournisseur;
    private EditText villeName;
    private String url;
    private String token;
    private String strLatitude, strLongitude;


    LocationManager locationManager = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        villeName = findViewById(R.id.villeName);
        twaqi = findViewById(R.id.aqi);
        twpm10 = findViewById(R.id.pm10);
        twpm25 = findViewById(R.id.pm25);

        token = "3b64dd9ba27f33d21f011add90f3376939d42490";
        Log.d("GPS", "onCreate");

        initialiserLocalisation();
    }
    LocationListener gpsListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getApplicationContext(), fournisseur + " localisation", Toast.LENGTH_SHORT).show();

            strLatitude = String.valueOf(location.getLatitude());
            strLongitude = String.valueOf(location.getLongitude());

            latitude.setText(strLatitude);
            longitude.setText(strLongitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            switch(status)
            {
                case LocationProvider.AVAILABLE:
                    Toast.makeText(getApplicationContext(), provider + " état disponible", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Toast.makeText(getApplicationContext(), provider + " état indisponible", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Toast.makeText(getApplicationContext(), provider + " état temporairement indisponible", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), provider + " état : " + status, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MainActivity.getContext(), provider + " activé !", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MainActivity.getContext(), provider + " désactivé !", Toast.LENGTH_SHORT).show();
        }
    };



    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        arreterLocalisation();
    }

    public static Context getContext() {
        return MainActivity.context;
    }

    private void initialiserLocalisation() {
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(true);
            criteria.setBearingRequired(true);
            criteria.setSpeedRequired(true);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_MEDIUM);

            fournisseur = locationManager.getBestProvider(criteria, true);
        }

        if (fournisseur != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            Location location = locationManager.getLastKnownLocation(fournisseur);

            if (location != null) {
                gpsListener.onLocationChanged(location);
            }

            locationManager.requestLocationUpdates(fournisseur, 10000, 0, gpsListener);
        }
    }

    private void arreterLocalisation() {
        if (locationManager != null) {
            locationManager.removeUpdates(gpsListener);
            gpsListener = null;
        }
    }

    public void PollutionSearch(View view) {
        twaqi.setText("");
        twpm25.setText("");
        twpm10.setText("");
        String name = villeName.getText().toString();
        url = "http://api.waqi.info/feed/"+name+"/?token="+token;
        httpclient client = new httpclient(url);
        client.start();
        try {
            client.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = client.getResponse();
        try {
            JSONObject json = new JSONObject(result);
            JSONObject data = json.getJSONObject("data");
            String valueAqi = data.getString("aqi");
            twaqi.setText(valueAqi);
        } catch (JSONException e) {
            e.printStackTrace();
            twaqi.setText("donnée indisponible");
        }
        try {
            JSONObject json = new JSONObject(result);
            JSONObject data = json.getJSONObject("data");
            JSONObject iaqi = data.getJSONObject("iaqi");
            JSONObject pm10 = iaqi.getJSONObject("pm10");
            String valuePm10 = pm10.getString("v");
            twpm10.setText(valuePm10);
        } catch (JSONException e) {
            e.printStackTrace();
            twpm10.setText("donnée indisponible");
        }
        try {
            JSONObject json = new JSONObject(result);
            JSONObject data = json.getJSONObject("data");
            JSONObject iaqi = data.getJSONObject("iaqi");
            JSONObject pm25 = iaqi.getJSONObject("pm25");
            String valuePm25 = pm25.getString("v");
            twpm25.setText(valuePm25);
        } catch (JSONException e) {
            e.printStackTrace();
            twpm25.setText("donnée indisponible");
        }

    }

    public void MyLocalisation(View view) {
        twaqi.setText("");
        twpm25.setText("");
        twpm10.setText("");
        url = "https://api.waqi.info/feed/geo:"+strLatitude+";"+strLongitude+"/?token="+token;
        httpclient client = new httpclient(url);
        client.start();
        try {
            client.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = client.getResponse();
        try {
            JSONObject json = new JSONObject(result);
            JSONObject data = json.getJSONObject("data");
            String valueAqi = data.getString("aqi");
            twaqi.setText(valueAqi);

        } catch (JSONException e) {
            e.printStackTrace();
            twaqi.setText("donnée indisponible");
        }
        try {
            JSONObject json = new JSONObject(result);
            JSONObject data = json.getJSONObject("data");
            JSONObject iaqi = data.getJSONObject("iaqi");
            JSONObject pm10 = iaqi.getJSONObject("pm10");
            String valuePm10 = pm10.getString("v");
            twpm10.setText(valuePm10);
        } catch (JSONException e) {
            e.printStackTrace();
            twpm10.setText("donnée indisponible");
        }
        try {
            JSONObject json = new JSONObject(result);
            JSONObject data = json.getJSONObject("data");
            JSONObject iaqi = data.getJSONObject("iaqi");
            JSONObject pm25 = iaqi.getJSONObject("pm25");
            String valuePm25 = pm25.getString("v");
            twpm25.setText(valuePm25);
        } catch (JSONException e) {
            e.printStackTrace();
            twpm25.setText("donnée indisponible");
        }

    }

}
