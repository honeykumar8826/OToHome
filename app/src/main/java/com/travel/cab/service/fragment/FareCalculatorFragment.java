package com.travel.cab.service.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.travel.cab.service.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class FareCalculatorFragment extends Fragment implements OnMapReadyCallback,View.OnClickListener {
    private static final String TAG = "PlaceActivity";
    private FusedLocationProviderClient mClient;
    private Location mLocation;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private TextView value;
    private Marker marker = null;
    private AutocompleteSupportFragment autocompleteFragment, autocompleteFragmentDrop;
    private String placeKey = "AIzaSyBBYFaI01s055CRQvOdnrZeapV_0duHFsI";
    private LatLng destination, source;
    private Context context;
    private LinearLayout pickLoc,dropLoc;
    private int AUTOCOMPLETE_REQUEST_CODE=101;
    private int AUTOCOMPLETE_REQUEST_CODE_DROP=102;


    public FareCalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle("Fare Calculator");
        return inflater.inflate(R.layout.fragment_fare_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        value = view.findViewById(R.id.textView);
        pickLoc = view.findViewById(R.id.ll_pickup);
        dropLoc = view.findViewById(R.id.ll_drop);
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), placeKey);
        }
        pickLoc.setOnClickListener(this);
        dropLoc.setOnClickListener(this);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        else {
            mClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    mLocation = location;
                     BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.icon);
                    LatLng currentLatLong = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                   // mMap.addMarker(new MarkerOptions().position(currentLatLong).title("Marker in India").icon(icon));
                    //Move the camera to the user's location and zoom in!
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14.0f));
                    Log.i(TAG, "onSuccess: "+location.getLatitude()+"-"+location.getLongitude());

                    CircleOptions circleOptions = new CircleOptions()
                            .center(currentLatLong).radius(1000)
                            .fillColor(Color.TRANSPARENT).strokeColor(Color.GREEN).strokeWidth(8);
                    Circle mCircle = mMap.addCircle(circleOptions);
                    Log.i(TAG, "onSuccess: " + location.getLongitude());
                }
            });
            mapFragment.getMapAsync(this);
        }
        configureCameraIdle();

    }

    private void configureCameraIdle() {
        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLng latLng = mMap.getCameraPosition().target;
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.pin);
                Geocoder geocoder = new Geocoder(context);
                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        String locality = addressList.get(0).getAddressLine(0);
                        String country = addressList.get(0).getCountryName();
                        if (!locality.isEmpty() && !country.isEmpty())
                            value.setText(locality +
                                    "  " + country);
                        MarkerOptions options = new MarkerOptions().position(latLng).icon(icon);
                /*    if(marker==null)
                    {
                        marker = mMap.addMarker(options);
                    }
                     // marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in India").icon(icon));
                    else
                    {
                        marker.setPosition(new LatLng(latLng.latitude,latLng.longitude));

                    }*/


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setOnCameraIdleListener(onCameraIdleListener);
        mMap.setMyLocationEnabled(true);
    }

    private void setId() {

//        // Initialize the AutocompleteSupportFragment.
//        autocompleteFragment = (AutocompleteSupportFragment)
//                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
//        autocompleteFragmentDrop = (AutocompleteSupportFragment)
//                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_drop);
//        Places.initialize(getApplicationContext(), placeKey);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.ll_pickup:
                openPickupIntent();
                break;
                case R.id.ll_drop:
                openDropIntent();
                break;
                default:
                    break;

        }
    }

    private void openDropIntent() {

        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(context);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void openPickupIntent() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(context);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_DROP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_DROP) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
