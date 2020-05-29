package com.example.armapsapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.GeoApiContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // variables
    private int destNum = 0;
    private boolean reached = false;

    private GoogleMap mMap;
    //    private Location userLocation;
    private LocationManager locationManager;
    private Marker userMarker;
    private Marker destinationMarker;
    private String destinationName;

    private LatLng[] destinations;

    private LatLng startLocation = new LatLng(30.286285, -97.737116);

    private LatLng northOfficeBuilding = new LatLng(30.291263, -97.737619);
    private LatLng texasExes = new LatLng(30.284176, -97.734424);
    private LatLng parlinHall = new LatLng(30.284915, -97.740109);

    private FusedLocationProviderClient mFusedLocationClient;
    LocationRequest locationRequest;

    private GeoApiContext mGeoApiContext = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fillDestinations();

//        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//
//        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0.01f, new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                    checkDestinationReached(latLng);
//
//                    if (userMarker != null) {
//                        userMarker.remove();
//                    }
//
//                    userMarker = mMap.addMarker(new MarkerOptions()
//                            .position(latLng)
//                            .title("User")
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//                }
//
//                @Override
//                public void onStatusChanged(String provider, int status, Bundle extras) { }
//
//                @Override
//                public void onProviderEnabled(String provider) { }
//
//                @Override
//                public void onProviderDisabled(String provider) { }
//            });
//        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0.01f, new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                    checkDestinationReached(latLng);
//
//                    if (userMarker != null) {
//                        userMarker.remove();
//                    }
//
//                    userMarker = mMap.addMarker(new MarkerOptions()
//                            .position(latLng)
//                            .title("User")
//                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//                }
//
//                @Override
//                public void onStatusChanged(String provider, int status, Bundle extras) { }
//
//                @Override
//                public void onProviderEnabled(String provider) { }
//
//                @Override
//                public void onProviderDisabled(String provider) { }
//            });
//        }

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            if (!reached)
                                checkDestinationReached(latLng);

                            if (userMarker != null) {
                                userMarker.remove();
                            }

                            userMarker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("User")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        }
                    }
                });

        createLocationRequest();
        mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (!reached)
                    checkDestinationReached(latLng);

                if (userMarker != null) {
                    userMarker.remove();
                }

                userMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("User")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
        }, getMainLooper());

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }

    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Style the Google Maps
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.maps_style));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }


        // Randomly pick a location
//        Random r = new Random();
//        int destNum = r.nextInt(3);
        LatLng finalDest = destinations[destNum];

        if (destNum == 0) {
            createNOApolylines();
            destinationName = "Destination A";
        } else if (destNum == 1) {
            createTexasExesPolylines();
            destinationName = "Destination B";
        } else {
            createParlinPolylines();
            destinationName = "Destination C";
        }

        userMarker = mMap.addMarker(new MarkerOptions().position(startLocation).title("Start"));
        destinationMarker = mMap.addMarker(new MarkerOptions().position(finalDest).title(destinationName));
        destinationMarker.showInfoWindow();


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(finalDest, 15));
    }

    private void fillDestinations() {
        Log.d("MapsActivity", "fillDestinations: called");

        destinations = new LatLng[3];
        destinations[0] = northOfficeBuilding;
        destinations[1] = texasExes;
        destinations[2] = parlinHall;
    }

    private void createNOApolylines() {
        ArrayList<LatLng> vectors = new ArrayList<LatLng>();
        vectors.add(new LatLng(30.28953, -97.73673)); //1
        vectors.add(new LatLng(30.289646, -97.738039)); //2
        vectors.add(new LatLng(30.2906, -97.73795)); //3
        vectors.add(new LatLng(30.2906, -97.73783)); //4
        vectors.add(new LatLng(30.2914, -97.73775)); //5
        vectors.add(new LatLng(30.2914, -97.73775)); //6

        Polyline line = mMap.addPolyline(new PolylineOptions()
            .add( startLocation, vectors.get(0))
            .add(vectors.get(0), vectors.get(1))
            .add(vectors.get(1), vectors.get(2))
            .add(vectors.get(2), vectors.get(3))
            .add(vectors.get(3), vectors.get(4))
            .add(vectors.get(4), vectors.get(5))
            .width(15)
            .color(Color.RED)
            );
    }

    private void createTexasExesPolylines() {
        ArrayList<LatLng> vectors = new ArrayList<LatLng>();
        vectors.add(new LatLng(30.285490, -97.737206)); //0
        vectors.add(new LatLng(30.285204, -97.733750)); //1
        vectors.add(new LatLng(30.284104, -97.733995)); //2

        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add( startLocation, vectors.get(0))
                .add(vectors.get(0), vectors.get(1))
                .add(vectors.get(1), vectors.get(2))
                .add(vectors.get(2), texasExes)
                .width(15)
                .color(Color.RED)
        );
    }

    private void createParlinPolylines() {
        ArrayList<LatLng> vectors = new ArrayList<LatLng>();
        vectors.add(new LatLng(30.283484, -97.737428)); //0
        vectors.add(new LatLng(30.283680, -97.739837)); //1
        vectors.add(new LatLng(30.284875, -97.739721)); //2

        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add( startLocation, vectors.get(0))
                .add(vectors.get(0), vectors.get(1))
                .add(vectors.get(1), vectors.get(2))
                .add(vectors.get(2), parlinHall)
                .width(15)
                .color(Color.RED)
        );

    }

    private void checkDestinationReached(LatLng userLatLng) {

        double userLat = userLatLng.latitude;
        double userLong = userLatLng.longitude;

        // reached the NOA Building A
        if ((userLat >= 30.2911 && userLat <= 30.292) && (userLong <= -97.737 && userLong >= -97.738)) {
            reached = true;
            Intent startIntent = new Intent(getApplicationContext(), DestinationActivity.class);
            startActivity(startIntent);

            //reached Parlin Hall
        } else if ((userLat >= 30.28475 && userLat <= 30.285) && (userLong <= -97.73965 && userLong >= -97.7404)) {
            reached = true;
            Intent startIntent = new Intent(getApplicationContext(), DestinationActivity.class);
            startActivity(startIntent);

            //reached Texas Exes
        } else if ( (userLat <= 30.2843 && userLat >= 30.2838) && (userLong <= -97.7339 && userLong >= -97.7345)) {
            reached = true;
            Intent startIntent = new Intent(getApplicationContext(), DestinationActivity.class);
            startActivity(startIntent);
        }
    }

//    private void getLastKnownLocation() {
//        Log.d("MapsActivity", "getLastKnownLocation: called");
//        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
//            @Override
//            public void onComplete(@NonNull Task<Location> task) {
//                if (task.isSuccessful()) {
//                    Location userLocation = task.getResult();
//                    Log.d("MapsActivity", "getLastKnownLocation: WORKED" + userLocation.getLongitude());
//                    LatLng userLoc = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
//                    mMap.addMarker(new MarkerOptions().position(userLoc).title("User"));
////                    calculateDirections(userLocation);
//                } else {
//                    Log.d("MapsActivity", "getLastKnownLocation: FAILED");
//                }
//            }
//        });
//    }

//    private void calculateDirections(Location userLocation) {
//        Log.d("MapsActivity", "calculateDirections: entered the method");
//
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
//
//
//        String origin_str = "origin=" + userLocation.getLatitude() + "," + userLocation.getLongitude();
//        String dest_str = "destination=" + String.valueOf(30.293973) + "," + String.valueOf(-97.730594);
//        String mode_str = "mode=" + "walking";
//
//        String parameters_str = origin_str + "&" + dest_str + "&" + mode_str;
//
//        String output_str = "json";
//
//        String url_str = "https://maps.googleapis.com/maps/api/directions/" + output_str + "?" + parameters_str + "&key" + "AIzaSyC3KqBgZAkVanUH0Ed8NQ8i8dV5uWopmRU";
//
//        new CallDirectionsApi(MapsActivity.this).execute(url_str);
//        Log.d("MapsActivity", "calculateDirections: i called the URL");
//
//    }
}
