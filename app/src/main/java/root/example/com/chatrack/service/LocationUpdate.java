package root.example.com.chatrack.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationUpdate extends Service {

    private static final String TAG = "LocationService";

    private FusedLocationProviderClient mFusedLocationClient;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID,lat,lng;
    private String Lokasi;
    private final static long UPDATE_INTERVAL = 4 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */
    private final static long DISTANCE = 10; /* 2 sec */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("ChaTrack")
                    .setPriority(NotificationManager.IMPORTANCE_MAX)
                    .setContentText("ChaTrack Apps is running in background process").build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;
    }

    private void getLocation() {

        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequestHighAccuracy.setSmallestDisplacement(DISTANCE);


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d(TAG, "getLocation: getting location information.");
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Log.d(TAG, "onLocationResult: got location result.");

                        Location location = locationResult.getLastLocation();

                        if (location != null) {
                            lat = String.valueOf(location.getLatitude());
                            lng = String.valueOf(location.getLongitude());
                            saveUserLocation();
                        }
                    }
                },
                Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

    //Upload Lokasi
    private void saveUserLocation(){

        try{
            //Buat Update Lokasi
            mAuth = FirebaseAuth.getInstance();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            myRef = mFirebaseDatabase.getReference();
            FirebaseUser user = mAuth.getCurrentUser();
            userID = user.getUid();

            myRef.child("CHATRACK").child("USER").child(userID).child("Latitude").setValue(lat);
            myRef.child("CHATRACK").child("USER").child(userID).child("Longitude").setValue(lng);
        }catch (NullPointerException e){
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.");
            Log.e(TAG, "saveUserLocation: NullPointerException: "  + e.getMessage() );
            stopSelf();
        }

    }


}

