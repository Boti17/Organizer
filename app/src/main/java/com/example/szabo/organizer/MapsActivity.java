package com.example.szabo.organizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Context mContext;
    private static final int REQUEST = 112;
    private LatLng resultLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext = this;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        searchLocation();
        changeType();
        sendLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                boolean gps_enabled = false;

                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ex) {
                }

                if (!gps_enabled) {
                    Toast.makeText(mContext, getResources().getString(R.string.locationOff), Toast.LENGTH_LONG).show();
                    return false;
                }
                Location myLocation = mMap.getMyLocation();
                mMap.clear();
                if (myLocation == null)
                {
                    Toast.makeText(mContext, getResources().getString(R.string.myLocationNotFound), Toast.LENGTH_LONG).show();
                }else {
                    resultLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(resultLatLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(resultLatLng,12.0f));
                }
                return true;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                mMap.clear();
                resultLatLng = point;
                mMap.addMarker(new MarkerOptions().position(resultLatLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(resultLatLng,12.0f));
            }
        });
    }

    private void searchLocation()
    {
        final EditText searchedLocationField = (EditText) findViewById(R.id.searchField);
        Button mSearchButton = (Button) findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchedLocation = searchedLocationField.getText().toString();
                List<Address> addresses = null;
                if (!searchedLocation.equals("")) {
                    Geocoder geocoder = new Geocoder(mContext);
                    try {
                        addresses = geocoder.getFromLocationName(searchedLocation, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addresses.size()==0) {
                        Toast.makeText(mContext, getResources().getString(R.string.locationNotExists), Toast.LENGTH_LONG).show();
                    } else {
                        Address address = addresses.get(0);
                        resultLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(resultLatLng));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(resultLatLng,12.0f));
                    }
                }
            }
        });
    }

    private void changeType()
    {
        Button mChangeTypeButton = (Button) findViewById(R.id.changeTypeButton);
        mChangeTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mMap.getMapType()) {
                    case GoogleMap.MAP_TYPE_NORMAL:
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case GoogleMap.MAP_TYPE_SATELLITE:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case GoogleMap.MAP_TYPE_TERRAIN:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case GoogleMap.MAP_TYPE_HYBRID:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void sendLocation()
    {
        Button mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resultLatLng != null)
                {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result",resultLatLng);
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }
                else
                {
                    Toast.makeText(MapsActivity.this, getResources().getString(R.string.selectALocation), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
