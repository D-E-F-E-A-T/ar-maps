package com.example.armapsapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.GeoApiContext;

import java.util.ArrayList;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // variables
    private GoogleMap mMap;
    //    private Location userLocation;
    private LocationManager locationManager;

    private LatLng[] destinations;
    private LatLng gdcBuilding = new LatLng(30.286268, -97.736844);

    private FusedLocationProviderClient mFusedLocationClient;

    private GeoApiContext mGeoApiContext = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fillDestinations();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    checkDestinationReached(latLng);
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("User")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) { }

                @Override
                public void onProviderEnabled(String provider) { }

                @Override
                public void onProviderDisabled(String provider) { }
            });
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    checkDestinationReached(latLng);
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("User")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) { }

                @Override
                public void onProviderEnabled(String provider) { }

                @Override
                public void onProviderDisabled(String provider) { }
            });
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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

        if (destNum == 0)
            createNOApolylines();
        else if (destNum == 1)
            createLawSchoolPolylines();
        else
            createHarryRansomPolylines();

//        getLastKnownLocation();

        mMap.addMarker(new MarkerOptions().position(gdcBuilding).title("Start"));
        mMap.addMarker(new MarkerOptions().position(finalDest).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(finalDest, 15));
    }

    private void fillDestinations() {
        Log.d("MapsActivity", "fillDestinations: called");
        LatLng northOfficeBuilding = new LatLng(30.291263, -97.737619);
        LatLng lawSchoolBuilding = new LatLng(30.288391, -97.730813);
        LatLng harryRansomCenter = new LatLng(30.284295, -97.740922);

        destinations = new LatLng[3];
        destinations[0] = northOfficeBuilding;
        destinations[1] = lawSchoolBuilding;
        destinations[2] = harryRansomCenter;
    }

    private void createNOApolylines() {
        ArrayList<LatLng> vectors = new ArrayList<LatLng>();
        vectors.add(new LatLng(30.286285, -97.737116)); //0
        vectors.add(new LatLng(30.287727, -97.736964)); //1
        vectors.add(new LatLng(30.287813, -97.737610)); //2
        vectors.add(new LatLng(30.288021, -97.737707)); //3
        vectors.add(new LatLng( 30.288029, -97.738094)); //4
        vectors.add(new LatLng(30.288270, -97.738065)); //5
        vectors.add(new LatLng(30.288351, -97.738113)); //6
        vectors.add(new LatLng(30.288966, -97.738051)); //7
        vectors.add(new LatLng(30.289107,-97.738140)); //8
        vectors.add(new LatLng (30.289510, -97.738110)); //9

        Polyline line = mMap.addPolyline(new PolylineOptions()
            .add( gdcBuilding, vectors.get(0))
            .add(vectors.get(0), vectors.get(1))
            .add(vectors.get(1), vectors.get(2))
            .add(vectors.get(2), vectors.get(3))
            .add(vectors.get(3), vectors.get(4))
            .add(vectors.get(4), vectors.get(5))
            .add(vectors.get(5), vectors.get(6))
            .add(vectors.get(6), vectors.get(7))
            .add(vectors.get(7), vectors.get(8))
            .add(vectors.get(8), vectors.get(9))
            .width(7)
            .color(Color.RED)
            );
    }

    private void createHarryRansomPolylines() {
        ArrayList<LatLng> vectors = new ArrayList<LatLng>();
        vectors.add(new LatLng(30.286285, -97.737116)); //0
        vectors.add(new LatLng(30.284742, -97.737287)); //1
        vectors.add(new LatLng(30.284800, -97.738043)); //2
        vectors.add(new LatLng(30.284904,-97.738419)); //3
        vectors.add(new LatLng(30.284971, -97.739283)); //4
        vectors.add(new LatLng(30.284093, -97.739358)); //5
        vectors.add(new LatLng(30.284128, -97.739790)); //6
        vectors.add(new LatLng(30.283906, -97.739857)); //7
        vectors.add(new LatLng(30.283992, -97.740941)); //8
        vectors.add(new LatLng(30.284295, -97.740922)); //9

        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add( gdcBuilding, vectors.get(0))
                .add(vectors.get(0), vectors.get(1))
                .add(vectors.get(1), vectors.get(2))
                .add(vectors.get(2), vectors.get(3))
                .add(vectors.get(3), vectors.get(4))
                .add(vectors.get(4), vectors.get(5))
                .add(vectors.get(5), vectors.get(6))
                .add(vectors.get(6), vectors.get(7))
                .add(vectors.get(7), vectors.get(8))
                .add(vectors.get(8), vectors.get(9))
                .width(7)
                .color(Color.RED)
        );
    }

    private void createLawSchoolPolylines() {
        ArrayList<LatLng> vectors = new ArrayList<LatLng>();
        vectors.add(new LatLng(30.286285, -97.737116)); //0
        vectors.add(new LatLng(30.285490, -97.737206)); //1
        vectors.add(new LatLng(30.285427, -97.735863 )); //2
        vectors.add(new LatLng(30.285483, -97.735383)); //3
        vectors.add(new LatLng(30.285526, -97.735294)); //4
        vectors.add(new LatLng(30.285564, -97.735201)); //5
        vectors.add(new LatLng(30.285523, -97.735027)); //6
        vectors.add(new LatLng(30.285481, -97.734973)); //7
        vectors.add(new LatLng(30.285416, -97.734933)); //8
        vectors.add(new LatLng(30.285394,  -97.734790)); //9 start of the roundabout
        vectors.add(new LatLng(30.285444, -97.734766)); //10
        vectors.add(new LatLng(30.285484,-97.734730)); //11
        vectors.add(new LatLng(30.285529, -97.734675)); //12
        vectors.add(new LatLng(30.285558, -97.734599)); //13
        vectors.add(new LatLng(30.285555, -97.734527)); //14
        vectors.add(new LatLng(30.285546, -97.734473)); //15
        vectors.add(new LatLng(30.285531, -97.734410)); //16
        vectors.add(new LatLng(30.285488, -97.734364)); //17
        vectors.add(new LatLng(30.285457, -97.734331)); //18
        vectors.add(new LatLng(30.285430, -97.734309)); //19
        vectors.add(new LatLng(30.285411, -97.734296)); //20
        vectors.add(new LatLng(30.285384, -97.734287)); //21 end of the roundabout

        vectors.add(new LatLng(30.285277, -97.732737)); //22
        vectors.add(new LatLng(30.285780, -97.732626)); //23
        vectors.add(new LatLng(30.286053, -97.732535)); //24
        vectors.add(new LatLng(30.286162, -97.732489)); //25
        vectors.add(new LatLng(30.286324, -97.732356)); //26
        vectors.add(new LatLng(30.286514, -97.732078)); //27
        vectors.add(new LatLng(30.286643, -97.731920)); //28
        vectors.add(new LatLng(30.287627, -97.731837)); //29
        vectors.add(new LatLng(30.287741, -97.731776)); //30
        vectors.add(new LatLng(30.287824, -97.731685)); //31
        vectors.add(new LatLng(30.287877, -97.731511)); //32
        vectors.add(new LatLng(30.287884, -97.731366)); //33
        vectors.add(new LatLng(30.287886, -97.730690)); //34
        vectors.add(new LatLng(30.288114, -97.730658)); //35
        vectors.add(new LatLng(30.288140, -97.730671)); //36
        vectors.add(new LatLng(30.288225, -97.730841)); //37

        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add( gdcBuilding, vectors.get(0))
                .add(vectors.get(0), vectors.get(1))
                .add(vectors.get(1), vectors.get(2))
                .add(vectors.get(2), vectors.get(3))
                .add(vectors.get(3), vectors.get(4))
                .add(vectors.get(4), vectors.get(5))
                .add(vectors.get(5), vectors.get(6))
                .add(vectors.get(6), vectors.get(7))
                .add(vectors.get(7), vectors.get(8))
                .add(vectors.get(8), vectors.get(9))
                .add(vectors.get(9), vectors.get(10))
                .add(vectors.get(10), vectors.get(11))
                .add(vectors.get(11), vectors.get(12))
                .add(vectors.get(12), vectors.get(13))
                .add(vectors.get(13), vectors.get(14))
                .add(vectors.get(14), vectors.get(15))
                .add(vectors.get(15), vectors.get(16))
                .add(vectors.get(16), vectors.get(17))
                .add(vectors.get(17), vectors.get(18))
                .add(vectors.get(18), vectors.get(19))
                .add(vectors.get(19), vectors.get(20))
                .add(vectors.get(20), vectors.get(21))
                .add(vectors.get(21), vectors.get(22))
                .add(vectors.get(22), vectors.get(23))
                .add(vectors.get(23), vectors.get(24))
                .add(vectors.get(24), vectors.get(25))
                .add(vectors.get(25), vectors.get(26))
                .add(vectors.get(26), vectors.get(27))
                .add(vectors.get(27), vectors.get(28))
                .add(vectors.get(28), vectors.get(29))
                .add(vectors.get(29), vectors.get(30))
                .add(vectors.get(30), vectors.get(31))
                .add(vectors.get(31), vectors.get(32))
                .add(vectors.get(32), vectors.get(33))
                .add(vectors.get(33), vectors.get(34))
                .add(vectors.get(35), vectors.get(36))
                .add(vectors.get(36), vectors.get(37))
                .width(7)
                .color(Color.RED)
        );
    }

    private void checkDestinationReached(LatLng userLatLng) {

        // reached the NOA Building A
        if ((userLatLng.latitude >= 30.291 && userLatLng.latitude <= 30.291) &&
            (userLatLng.longitude >= -97.737 && userLatLng.longitude <= -97.737)) {

            Intent startIntent = new Intent(getApplicationContext(), DestinationActivity.class);
            startActivity(startIntent);

        } else if ((userLatLng.latitude >= 30.288 && userLatLng.latitude <= 30.288) &&
                (userLatLng.longitude >= -97.73 && userLatLng.longitude <= -97.73)) {

            Intent startIntent = new Intent(getApplicationContext(), DestinationActivity.class);
            startActivity(startIntent);

        } else if ((userLatLng.latitude >= 30.284 && userLatLng.latitude <= 30.284) &&
                ( userLatLng.longitude >= -97.740 && userLatLng.longitude <= -97.740)) {

            Intent startIntent = new Intent(getApplicationContext(), DestinationActivity.class);
            startActivity(startIntent);
        }
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
//                    calculateDirections(userLocation);
                } else {
                    Log.d("MapsActivity", "getLastKnownLocation: FAILED");
                }
            }
        });
    }

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
