package com.example.armapsapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // variables
    private GoogleMap mMap;
//    private Location userLocation;
    private LocationManager locationManager;

    private LatLng[] destinations;

    private FusedLocationProviderClient mFusedLocationClient;

    private GeoApiContext mGeoApiContext = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fillDestinations();

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Randomly pick a location
        Random r = new Random();
        int destNum = r.nextInt(3);
        LatLng finalDest = destinations[destNum];

        getLastKnownLocation();


        mMap.addMarker(new MarkerOptions().position(finalDest).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(finalDest, 15));
    }

    private void fillDestinations() {
        Log.d("MapsActivity", "fillDestinations: called");
        LatLng northOfficeBuilding = new LatLng(30.293973, -97.730594);
        LatLng lawSchoolBuilding = new LatLng(30.288704, -97.730594);
        LatLng harryRansomCenter = new LatLng(30.284498, -97.740496);

        destinations = new LatLng[3];
        destinations[0] = northOfficeBuilding;
        destinations[1] = lawSchoolBuilding;
        destinations[2] = harryRansomCenter;
    }

    private void getLastKnownLocation() {
        Log.d("MapsActivity", "getLastKnownLocation: called");
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location userLocation = task.getResult();
                    Log.d("MapsActivity", "getLastKnownLocation: WORKED" + userLocation.getLongitude());
                    LatLng userLoc = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLoc).title("User"));
                    calculateDirections(userLocation);
                } else {
                    Log.d("MapsActivity", "getLastKnownLocation: FAILED");
                }
            }
        });
    }

    private void calculateDirections(Location userLocation) {
        Log.d("MapsActivity", "calculateDirections: entered the method");

//        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(30.293973,-97.730594 );
//
//        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
//
//        directions.alternatives(true);
//        directions.origin(
//                new com.google.maps.model.LatLng(userLocation.getLatitude(), userLocation.getLongitude())
//        );
//
//        Log.d("MapsActivity", "calculateDirections: desintation: " + destination.toString());
//
//        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
//            @Override
//            public void onResult(DirectionsResult result) {
//                Log.d("MapsActivity", "calculateDirections: routes: " +  result.routes[0].toString() );
//            }
//
//            @Override
//            public void onFailure(Throwable e) {
//                Log.e("MapsActivity", "calculateDirections: Failed to get directions " + e.getMessage());
//            }
//        });


        String origin_str = "origin=" + userLocation.getLatitude() + "," + userLocation.getLongitude();
        String dest_str = "destination=" + String.valueOf(30.293973) + "," + String.valueOf(-97.730594);
        String mode_str = "mode=" + "walking";

        String parameters_str = origin_str + "&" + dest_str + "&" + mode_str;

        String output_str = "json";

        String url_str = "https://maps.googleapis.com/maps/api/directions/" + output_str + "?" + parameters_str + "&key" + getString(R.string.google_maps_key);

        Log.d("MapsActivity", "calculateDirections: I made a url " + url_str);

        new FetchURL(MapsActivity.this).execute(url_str);

//        Log.d("MapsActivity", "calculateDirections: i called the URL");

    }
}
