package com.example.studentapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

import es.dmoral.toasty.Toasty;

public class MapsActivityS extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    DatabaseReference studentsRef;
    private FirebaseAuth mAuth;
    LatLng driverLocation;
    LatLng currentLocation;

    double currentLatitude;
    double currentLongitude;
    String busNumber;

    double driverLatitude;
    double driverLongitude;

    StudentLocation studentLocation;

    DatabaseReference databaseReference;
    DatabaseReference stdLocationRef;
    FirebaseUser user;
    String uid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapss);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        studentLocation = new StudentLocation();

//        user = mAuth.getInstance().getCurrentUser();
//        uid = user.getUid();

        Intent intent = getIntent();
        busNumber = intent.getStringExtra("busNumber");


    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
        }
        mMap.setMyLocationEnabled(true);

        double latitude = 34.2010552;
        double longitude = 73.1622682;
        LatLng latLng  = new LatLng(latitude,longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }
    public void DriverLocationBtn(View view){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));
        mMap.animateCamera(CameraUpdateFactory.scrollBy(10,0));
    }




    private void retrieveDriverLocation(){
        studentsRef = FirebaseDatabase.getInstance().getReference().child(busNumber);
        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 driverLatitude = Double.parseDouble(dataSnapshot.child("latitude").getValue().toString().trim());
                 driverLongitude = Double.parseDouble(dataSnapshot.child("longitude").getValue().toString().trim());


                driverLocation = new LatLng(driverLatitude,driverLongitude);
                if (driverLatitude==0 | driverLongitude==0)
                {
                    Toasty.info(MapsActivityS.this,"This bus doesn't operate now",Toast.LENGTH_LONG).show();
                }
                else {
                    // mMap.clear();
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(driverLocation);

                    markerOptions.title("Driver");

                     markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.driver));


                    float[] results = new float[10];
                    Location.distanceBetween(currentLatitude,currentLongitude,driverLatitude,driverLongitude,results);
                    if (results[0]>=1000.0)
                    {
                        double km = results[0]*0.001;
                        markerOptions.snippet("Distance= "+km + " Km");
                       // Toasty.info(MapsActivityS.this,"The distance of the driver from your location is " + km,Toast.LENGTH_LONG).show();
                    }else
                    {
                        markerOptions.snippet("Distance= "+results[0] + " m");
                       // Toasty.info(MapsActivityS.this,"The distance of the driver from your location is " + results[0],Toast.LENGTH_LONG).show();
                    }




                    mMap.addMarker(markerOptions).setFlat(true);

                    mMap.addPolyline(new PolylineOptions()
                            .add(currentLocation)
                            .add(driverLocation)
                            .width(8f)
                            .color(Color.RED)
                            .clickable(true)
                    );

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapsActivityS.this, "Error", Toast.LENGTH_SHORT).show();

            }
        });
    }
    private void retrieveCurrentUserData(){
        databaseReference = FirebaseDatabase.getInstance().getReference().child("StudentData");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child(uid).child("studentName").getValue(String.class);
               // String rollNo = dataSnapshot.child(uid).child("studentRollNo").getValue(String.class);
                //String department = dataSnapshot.child(uid).child("studentDepartment").getValue(String.class);
                //String email = dataSnapshot.child(uid).child("studentEmail").getValue(String.class);

                //emailInput.setText(""+email);
                //passwordInput.setText(""+name);

                Toast.makeText(MapsActivityS.this, ""+name, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapsActivityS.this, "database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

         currentLatitude = location.getLatitude();
         currentLongitude = location.getLongitude();

         currentLocation = new LatLng(currentLatitude,currentLongitude);
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));

        retrieveDriverLocation();


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLocation);
        mMap.clear();
        markerOptions.title("You");


       // markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.student));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));


        markerOptions.snippet("Your Current location");
        mMap.addMarker(markerOptions).setFlat(true);



    }
    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }
    //back press function start here

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("EXIT?")
                .setIcon(R.drawable.exit)
                .setMessage("Are u sure you want to exit")
                .setCancelable(false)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
                    }
                })
                .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }

    //back press ends.....

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {

            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;

            case R.id.bus_info:
                new AlertDialog.Builder(this)
                        .setTitle("Bus Timing")
                        .setMessage("Morning 08:45 " +" Evening 01:20")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
