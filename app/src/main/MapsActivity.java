package com.example.swapnil.cleanbangalore;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import projectssy.com.cleanbangalore.R;
import projectssy.com.cleanbangalore.UploadToServer;


public class MapsActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    static String name;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String TAG ="mapErr" ;
    GoogleMap mMap;
    EditText text;
    static double lat,lng;

    static String locality;
    private GoogleApiClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.tag);
        builder.setTitle("Mark location ");
        builder.setMessage("Tap the button, So we can detect your location automatically.");
        builder.show();
        name = getIntent().getStringExtra("name");
        if (servicesOK()) {
            setContentView(R.layout.activity_maps);

            if (initMap()) {

                mLocationClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();

                mLocationClient.connect();

            } else {
            }

        } else {
            setContentView(R.layout.activity_main);
        }
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Geocoder gc = new Geocoder(MapsActivity.this);
                List<Address> list = null;
                LatLng latLng = marker.getPosition();
                try {
                    list  = gc.getFromLocation(latLng.latitude,latLng.longitude,1);

                }catch (IOException e ) {
                    e.printStackTrace();
                    return;
                }
                Address add = list.get(0);
                locality = add.getSubLocality();
                lat = add.getLatitude();
                lng = add.getLongitude();

            }
        });
    }


    public boolean servicesOK() {

        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog =
                    GooglePlayServicesUtil.getErrorDialog(isAvailable, this, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {

        }

        return false;
    }

    private boolean initMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
        }
        return (mMap != null);
    }

    public void UploadNow(View view) throws IOException {


        text = (EditText) findViewById(R.id.landmarkText);
        locality = text.getText().toString();
        Intent openUploadActivity = new Intent(this,UploadToServer.class);
        String filePath = getIntent().getStringExtra("location");
        openUploadActivity.putExtra("location",filePath);
        openUploadActivity.putExtra("locality",locality);
        openUploadActivity.putExtra("lat",lat);
        openUploadActivity.putExtra("lng",lng);
        openUploadActivity.putExtra("name",name);
        startActivity(openUploadActivity);
        finish();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void showCurrentLocation(View view)   {
        Toast toast = Toast.makeText(this,"Hold and drag marker to get accurate location.",Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
        toast.show();

        Location currentLocation = LocationServices.FusedLocationApi
                .getLastLocation(mLocationClient);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = mapFragment.getMap();
        mMap.clear();
        mMap.setMyLocationEnabled(true);
        Button btn = (Button) findViewById(R.id.button3);
        btn.setEnabled(true);
        EditText tv = (EditText) findViewById(R.id.landmarkText);
        if (currentLocation == null) {
        } else {
            LatLng latLng = new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );
            lat = currentLocation.getLatitude();
            lng = currentLocation.getLongitude();
            Geocoder gc = new Geocoder(this);
            List<Address> list = null;
            try {
                list = gc.getFromLocation(currentLocation.getLatitude(),currentLocation.getLongitude(),1);

                if(list.size()> 0 ) {
                    Address  add = list.get(0);
                    String locality = add.getSubLocality();
                    tv.setText(locality);
                } else {
                    tv.setText("Not found, Enter manually");
                }

            } catch (IOException e) {
                Log.d(TAG,"SomeShit "+e.getMessage());
                e.printStackTrace();
            }



            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(
                    latLng, 18
            );
            mMap.animateCamera(update);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Current Location")
                    .draggable(true)
                    .snippet("Proceed further by adding a landmark."));
        }
    }



}