package root.example.com.chatrack;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import root.example.com.chatrack.service.LocationUpdate;
import root.example.com.chatrack.tabFragment.TabChat;
import root.example.com.chatrack.tabFragment.TabFriends;
import root.example.com.chatrack.tabFragment.TabProfile;
import root.example.com.chatrack.tabFragment.TabStreaming;
import root.example.com.chatrack.tabFragment.ViewPagerAdapter;

import static root.example.com.chatrack.MainActivity.ERROR_DIALOG_REQUEST;
import static root.example.com.chatrack.MainActivity.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "MainMenu";
    //fragment
    private TabLayout tabLayout;

    //This is our viewPagerAdapter
    private ViewPager viewPager;

    ViewPagerAdapter adapter;
    //Fragments
    TabFriends friendsFragments;
    TabStreaming streamingFragments;
    TabChat chatFragments;
    TabProfile profileFragments;
    DrawerLayout drawer;
    public boolean profileBuka = false;

    //dataModel
    private String personName,
            personGivenName,
            personFamilyName,
            personEmail,
            personId;
    private Uri personPhoto;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView NamaHeader, EmailHeader;
    private ImageView FotoProfil;
    private long backPressedTime;
    private Toast backToast;
    private TextView toolbar_text;

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private FirebaseAuth firebaseAuth;
    String UserId;
    private String lat, lng;

    //Lokasi Update
    private FusedLocationProviderClient mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //Database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UserId = user.getUid();
        mLastLocation = LocationServices.getFusedLocationProviderClient(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar_text = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbar_text.setText("Friends");
        setSupportActionBar(toolbar);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        profileFragments = new TabProfile().newInstance();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        toolbar_text.setText("Friends");
                        System.out.println("Friends");
                        break;
                    case 1:
                        toolbar_text.setText("Chats");

                        break;
                    case 2:
                        toolbar_text.setText("Streaming");
                        break;
                }
                tabLayout.getTabAt(position).select();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            personName = acct.getDisplayName();
            personGivenName = acct.getGivenName();
            personFamilyName = acct.getFamilyName();
            personEmail = acct.getEmail();
            personId = acct.getId();
            personPhoto = acct.getPhotoUrl();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        NamaHeader = (TextView) header.findViewById(R.id.TvNama);
        EmailHeader = (TextView) header.findViewById(R.id.TvEmail);
        FotoProfil = (ImageView) header.findViewById(R.id.IvProvile);
        NamaHeader.setText(personName);
        EmailHeader.setText(personEmail);
        Glide.with(getApplicationContext()).load(personPhoto).into(FotoProfil);
        Log.d(TAG, "onCreate() returned: " + personPhoto);

        FotoProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lihatProfile();
            }
        });

    }

    private void lihatProfile() {
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        Bundle mBundle = new Bundle();
        Log.d(TAG, "lihatProfile() returned: " + profileBuka);
        if (!profileBuka) {
            mBundle.clear();
            mBundle.putString("Uri",personPhoto.toString());
            mBundle.putString("Nama",personName);
            profileFragments.setArguments(mBundle);
            mFragmentTransaction.replace(R.id.ProfileContainer, profileFragments, "Profile");
            mFragmentTransaction.attach(profileFragments);
            mFragmentTransaction.addToBackStack(null);
            mFragmentTransaction.commit();
            toolbar_text.setText("Profile");
            Log.d(TAG, "lihatProfile() returned: Mashok");
            drawer.closeDrawer(GravityCompat.START);
            profileBuka = true;
            Log.d(TAG, "lihatProfile() returned: " + profileBuka);
        } else {
            profileBuka = true;
        }
    }

    public void removeFragments(Fragment mFragment) {
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.detach(mFragment).commit();
        profileBuka = false;
        Log.d(TAG, "removeFragments() returned: " + profileBuka);
    }

    private void setupViewPager(ViewPager viewPagerAdapter) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        friendsFragments = new TabFriends();
        streamingFragments = new TabStreaming();
        chatFragments = new TabChat();
        adapter.addFragment(friendsFragments, "Friends");
        adapter.addFragment(chatFragments, "Chats");
        adapter.addFragment(streamingFragments, "Streaming");
        viewPager.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.Logout) {
            // Handle the camera action
            signOut();
            mAuth.signOut();
        }else if (id == R.id.DashboardMenu){
            removeFragments(profileFragments);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        revokeAccess();
                        startActivity(new Intent(MainMenu.this, MainActivity.class));
                    }
                });
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete() returned: Beres");
                    }
                });
    }

    private void updateLokasi() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    try {
                        //Update to firebase
                        Location location = task.getResult();
                        lat = String.valueOf(location.getLatitude());
                        lng = String.valueOf(location.getLongitude());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    myRef.child("USER").child(UserId)
                            .child("Latitude").setValue(lat);
                    myRef.child("USER").child(UserId)
                            .child("Longitude").setValue(lng);
                    startLocationService();
                } else {
                    //Toast.makeText(this, "Couldn't get the location",Toast.LENGTH_SHORT).show();
                    Log.d("TEST", "Couldn't load location");
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLokasi();
        checkMapServices();
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, LocationUpdate.class);
            //this.startService(serviceIntent);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                MainMenu.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("root.example.com.tar_q.services.LocationUpdate".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Aplikasi Ini Membutuhkan GPS Untuk Bekerja Dengan Baik, Apakah Anda Ingin Menyalakannya ?")
                .setCancelable(false)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainMenu.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainMenu.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (profileBuka) {
            removeFragments(profileFragments);
        } else if (backPressedTime + 2000 > System.currentTimeMillis()) {
            Intent intent = new Intent(MainMenu.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
            backToast.cancel();
            super.onBackPressed();
        } else {
            backToast = Toast.makeText(getBaseContext(), "Tekan Lagi Untuk Keluar", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
