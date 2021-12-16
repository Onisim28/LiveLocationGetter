package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.location.Address;
import android.widget.TextView;

import com.example.myapplication.data.model.LoggedInUser;
import com.example.myapplication.data.model.Person;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomePage extends AppCompatActivity implements LocationListener {

    private Executor executor = Executors.newFixedThreadPool(1);
    TextView tx1V, tx2V, tx3V, tx4V;
    double latitude, longitude;
    private LoggedInUser loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            loggedInUser = (LoggedInUser) bundle.getSerializable(login.LOGGED_IN_USER_KEY);
        }

        TextView tx2V = (TextView) findViewById(R.id.tx2);
        TextView tx3V = (TextView) findViewById(R.id.tx3);
        TextView tx4V = (TextView) findViewById(R.id.tx4);

        Button getLocationB = (Button) findViewById(R.id.getLocation);

        getLocationB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 0, HomePage.this);

            }
        });

    }


    private boolean addPosition(double latitude, double longitude) throws IOException {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request request = new Request.Builder()
                .url("http://192.168.1.9:8080/positions/addPosition/"+(int)(loggedInUser.getId()))
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            System.out.println("Error: " + response);
            return false;
        }

        System.out.println(response.body().string());
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {

        TextView tx1V = (TextView) findViewById(R.id.tx1);
        tx1V.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
        System.out.println(location.getLatitude());
        System.out.println(location.getLongitude());
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        executor.execute(new Runnable() {
            public void run() {
                /* Message msg = msgHandler.obtainMessage();*/
                try {
                            /*String username = usernameEt.getText().toString();
                            String emailAddress = emailAddressEt.getText().toString();
                            String password = passwordEt.getText().toString();
                            String confirmPassword = confirmPasswordEt.getText().toString();*/
                    addPosition(latitude, longitude);
                    /*msgHandler.sendMessage(msg);*/
                } catch (
                        IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

}

