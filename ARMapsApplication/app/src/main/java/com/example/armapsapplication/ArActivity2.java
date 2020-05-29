package com.example.armapsapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.GeoApiContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ArActivity2 extends AppCompatActivity implements SensorEventListener {

    TextView debugText;
    TextView destinationText;

    // Camera variables
    TextureView textureView;

    CameraDevice cameraDevice;
    String cameraId;
    Size imageDimensions;

    Handler backgroundHandler;
    HandlerThread handlerThread;

    CaptureRequest.Builder captureRequestBuilder;
    CameraCaptureSession cameraSession;


    // Compass Sensor variables
    private ImageView imageView;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float currentAzimuth = 0f;
    private SensorManager mSensorManager;


    // Location variables
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private GeoApiContext mGeoApiContext = null;

    int destNum = 2;
    int currIdx;
    private boolean reached = false;
    ArrayList<Float> arrowDirections = new ArrayList<Float>();


    // ------------------------------------------------------------------------------------------ //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ar2);

        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(surfaceTextureListener);

        imageView = (ImageView) findViewById(R.id.arrow);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        debugText = (TextView) findViewById(R.id.debugLatLng);
        destinationText = (TextView) findViewById(R.id.destinationName);


        fillCheckpoints();

        /*
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            return;
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0.01f, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    String strLat = String.valueOf(location.getLatitude());
                    String strLng = String.valueOf(location.getLongitude());

                    debugText.setText(strLat + ", " + strLng);

                    checkDestinationReached(latLng);
                    checkpointReached(latLng);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) { }

                @Override
                public void onProviderEnabled(String provider) { }

                @Override
                public void onProviderDisabled(String provider) { }
            });
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0.01f, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


                    String strLat = String.valueOf(location.getLatitude());
                    String strLng = String.valueOf(location.getLongitude());

                    debugText.setText(strLat + ", " + strLng);

                    checkDestinationReached(latLng);
                    checkpointReached(latLng);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) { }

                @Override
                public void onProviderEnabled(String provider) { }

                @Override
                public void onProviderDisabled(String provider) { }
            });
        }
        */

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (!reached) {
                    checkpointReached(latLng);
                    checkDestinationReached(latLng);
                }

            }
        }, getMainLooper());

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);

        startBackgroundThread();

        if (textureView.isAvailable()) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }

    }

    @Override
    protected void onPause() {
        try  {
            stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = 0.97f;
        synchronized (this) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];
                mGravity = sensorEvent.values;
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
                mGeomagnetic = sensorEvent.values;
            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0];

                float newRotation = 0.0f;

                if (arrowDirections != null) {
                    newRotation = 90f *  Math.round( (arrowDirections.get(currIdx) - (azimuth*360/(2*3.14159f))) / 90);
                }


                imageView.setRotation(newRotation);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    // ------------------------------- CAMERA METHODS ------------------------------------------- //

    private void openCamera() throws CameraAccessException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = cameraManager.getCameraIdList()[0];

            CameraCharacteristics cameraCharacter = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = cameraCharacter.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            imageDimensions = map.getOutputSizes(SurfaceTexture.class)[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            cameraManager.openCamera(cameraId, stateCallback, null);

        }

    }

    private void startCameraPreview() throws CameraAccessException {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimensions.getWidth(), imageDimensions.getHeight());

        Surface surface = new Surface(texture);

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

        captureRequestBuilder.addTarget(surface);

        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if (cameraDevice == null)
                    return;

                cameraSession = session;

                try {
                    updatePreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, null);
    }

    private void updatePreview() throws CameraAccessException {
        if (cameraDevice == null)
            return;

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        cameraSession.setRepeatingRequest(captureRequestBuilder.build(),null,backgroundHandler);
    }

    private void startBackgroundThread() {
        handlerThread = new HandlerThread("Camera Background");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
    }

    private void stopBackgroundThread() throws InterruptedException {
        handlerThread.quitSafely();
        handlerThread.join();
        backgroundHandler = null;
        handlerThread = null;
    }

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;

            try  {
                startCameraPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    // ------------------------------- LOCATION METHODS ----------------------------------------- //

    private void fillCheckpoints() {
        // Randomly pick a location
//        Random r = new Random();
//        destNum = r.nextInt(3);

        if (destNum == 0) {
            fillNOAcheckpoints();
            destinationText.setText("Destination A");
        }
        else if (destNum == 1) {
            fillTexasExesCheckpoints();
            destinationText.setText("Destination B");
        } else {
            fillParlinCheckpoints();
            destinationText.setText("Destination C");
        }

        currIdx = 0;

    }

    private void fillNOAcheckpoints() {
        arrowDirections.add(0.0f);
        arrowDirections.add(270.0f);
        arrowDirections.add(0.0f);
    }


    private void fillParlinCheckpoints() {
        arrowDirections.add(180.0f);
        arrowDirections.add(270.0f);
        arrowDirections.add(0.0f);
    }

    private void fillTexasExesCheckpoints() {
        arrowDirections.add(180.0f);
        arrowDirections.add(90.0f);
        arrowDirections.add(180.0f);
    }

    private void checkpointReached(LatLng userLatLng) {
        double userLat = userLatLng.latitude;
        double userLong = userLatLng.longitude;

        if (destNum == 0) {
            checkNOAcheckpoints(userLat, userLong);
        } else if (destNum == 1) {
            checkTexasExesCheckpoints(userLat, userLong);
        } else if (destNum == 2) {
            checkParlinCheckpoints(userLat, userLong);
        }
    }

    private void checkNOAcheckpoints(double userLat, double userLong) {
        if (currIdx == 0) {
            if ((userLat >= 30.28945 && userLat <= 30.2897) &&
                (userLong <= -97.7366 && userLong >= -97.737))
                currIdx++;
        } else if (currIdx == 1) {
            if ((userLat <= 30.2898 && userLat >= 30.2894) &&
                (userLong <= -97.7379 && userLong >= -97.7382))
                currIdx++;
        }
    }

    private void checkParlinCheckpoints(double userLat, double userLong) {
        if (currIdx == 0) {
            if ((userLat >= 30.2831 && userLat <= 30.2837) &&
                (userLong >= -97.7378 && userLong <= -97.737)) {
                currIdx++;
            }
        } else if (currIdx == 1) {
            if ((userLat <= 30.2841 && userLat >= 30.2835) &&
                (userLong <= -97.7396 && userLong >= -97.74))
                currIdx++;
        }
    }

    private void checkTexasExesCheckpoints(double userLat, double userLong) {
        if (currIdx == 0) {
            if ((userLat >= 30.28535 && userLat <= 30.2857) &&
                (userLong >= -97.7374 && userLong <= -97.7369))
                currIdx++;
        } else if (currIdx == 1) {
            if ((userLat <= 30.2854 && userLat >= 30.2850) &&
                (userLong <= -97.73365 && userLong >=  -97.734))
                currIdx++;
        }

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
        } else if ( (userLat <= 30.2843 && userLat >= 30.2836) && (userLong <= -97.7339 && userLong >= -97.7345)) {
            reached = true;
            Intent startIntent = new Intent(getApplicationContext(), DestinationActivity.class);
            startActivity(startIntent);
        }
    }
}
