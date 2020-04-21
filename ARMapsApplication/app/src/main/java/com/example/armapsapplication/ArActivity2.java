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
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.GeoApiContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ArActivity2 extends AppCompatActivity implements SensorEventListener {

    boolean debug = true;
    TextView debugRotationText;

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
    private FusedLocationProviderClient mFusedLocationClient;
    private GeoApiContext mGeoApiContext = null;

    int currIdx;
    ArrayList<LatLng> checkpoints = new ArrayList<LatLng>();
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

        TextView debugText = (TextView) findViewById(R.id.debugLatLng);
        debugRotationText = (TextView) findViewById(R.id.debugRotation);

        fillCheckpoints();

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
                    String strLat = String.valueOf(location.getLatitude());
                    String strLng = String.valueOf(location.getLongitude());

                    debugText.setText(strLat + ", " + strLng);
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

                    String strLat = String.valueOf(location.getLatitude());
                    String strLng = String.valueOf(location.getLongitude());

                    debugText.setText(strLat + ", " + strLng);
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
//                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];
//                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];
                mGravity = sensorEvent.values;
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
//                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * sensorEvent.values[1];
//                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * sensorEvent.values[2];
                mGeomagnetic = sensorEvent.values;
            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            if (success) {
//                float orientation[] = new float[3];
//                SensorManager.getOrientation(R, orientation);
//                azimuth = (float) Math.toDegrees(orientation[0]);
//                azimuth = (azimuth * 360) % 360;
//
//
//                Animation anim = new RotateAnimation(-currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f);
//                currentAzimuth = azimuth;
//
//                anim.setDuration(500);
//                anim.setRepeatCount(0);
//                anim.setFillAfter(true);

//                imageView.startAnimation(anim);
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0];

                float newRotation = 0.0f;

                if (arrowDirections != null)
                    newRotation = arrowDirections.get(currIdx) - (-azimuth*360/(2*3.14159f));

                newRotation = (-azimuth*360/(2*3.14159f));

                debugRotationText.setText( String.valueOf(newRotation));
                imageView.setRotation(newRotation);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // ------------------------------------------------------------------------------------------ //
    // ------------------------------- CAMERA METHODS ------------------------------------------- //
    // ------------------------------------------------------------------------------------------ //

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

    // ------------------------------------------------------------------------------------------ //
    // ------------------------------- LOCATION METHODS ----------------------------------------- //
    // ------------------------------------------------------------------------------------------ //

    private void fillCheckpoints() {
        // Randomly pick a location
        Random r = new Random();
        int destNum = r.nextInt(3);

        if (debug)
            destNum = 0;

        if (destNum == 0)
            fillNOAcheckpoints();
//        else if (destNum == 1)
//            fillLawCheckpoints();
//        else
//            fillHarryRansomCheckpoints();

        currIdx = 0;
    }

    private void fillNOAcheckpoints() {
        checkpoints.add(new LatLng(30.286285, -97.737116)); //0
        arrowDirections.add(270.0f);

        checkpoints.add(new LatLng( 30.284742, -97.737287)); //1
        arrowDirections.add(180.0f);

        checkpoints.add(new LatLng(30.284971, -97.739283));  //2
        arrowDirections.add(270.0f);

        checkpoints.add(new LatLng(30.284093, -97.739358));  //3
        arrowDirections.add(180.0f);

        checkpoints.add(new LatLng(30.284128, -97.739790));  //4
        arrowDirections.add(270.0f);

        checkpoints.add(new LatLng(30.283906, -97.739857));  //5
        arrowDirections.add(180.0f);

        checkpoints.add(new LatLng(30.283992, -97.740941));  //6
        arrowDirections.add(270.0f);

        checkpoints.add(new LatLng(30.284295, -97.740922)); //7
        arrowDirections.add(0.0f);
    }

    private void fillLawCheckpoints() {

    }

    private void fillHarryRansomCheckpoints() {

    }


    private void checkCheckpointReached(LatLng userLatLng) {

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
}
