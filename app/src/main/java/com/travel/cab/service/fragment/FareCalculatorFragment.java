package com.travel.cab.service.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.travel.cab.service.broadcast.InternetBroadcastReceiver;
import com.travel.cab.service.ui.IntentFilterCondition;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 *///
public class FareCalculatorFragment extends Fragment implements OnMapReadyCallback,
        View.OnClickListener, AdapterView.OnItemSelectedListener {
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
    private LatLng destinationLatlong, sourceLatlong;
    private Context context;
    private LinearLayout pickLoc, dropLoc;
    private int AUTOCOMPLETE_REQUEST_CODE = 101;
    private int AUTOCOMPLETE_REQUEST_CODE_DROP = 102;
    private IntentFilter intentFilter;
    private InternetBroadcastReceiver internetBroadcastReceiver;
    private Button buyPackageFare,showFare;
    private TextView sourcePoint, destinationPoint;
    private TextView fromLoc,toLoc,packageDistance,fare;
    String[] country = { "India", "USA", "China", "Japan", "Other"};
    private Spinner daysDropDown;


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
        internetBroadcastReceiver = new InternetBroadcastReceiver();
        mClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        value = view.findViewById(R.id.textView);
        pickLoc = view.findViewById(R.id.ll_pickup);
        dropLoc = view.findViewById(R.id.ll_drop);
        sourcePoint = view.findViewById(R.id.tv_pickup_location);
        destinationPoint = view.findViewById(R.id.tv_drop_location);
        

        buyPackageFare = view.findViewById(R.id.btn_buy_package_fare);
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), placeKey);
        }
        pickLoc.setOnClickListener(this);
        dropLoc.setOnClickListener(this);
        buyPackageFare.setOnClickListener(this);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            mClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    mLocation = location;
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.icon);
                    LatLng currentLatLong = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                    // mMap.addMarker(new MarkerOptions().position(currentLatLong).title("Marker in India").icon(icon));
                    //Move the camera to the user's location and zoom in!
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14.0f));
                    Log.i(TAG, "onSuccess: " + location.getLatitude() + "-" + location.getLongitude());

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
                sourceLatlong=latLng;
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.pin);
                Geocoder geocoder = new Geocoder(context);
                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        String locality = addressList.get(0).getAddressLine(0);
                        String country = addressList.get(0).getCountryName();
                        if (!locality.isEmpty() && !country.isEmpty())
                        {
                           // value.setText(locality + "  " + country);
                            String address[] = getSplitAddress(locality);
                            sourcePoint.setText(address[0]+address[1]+address[3]);
                        }
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
        switch (view.getId()) {
            case R.id.ll_pickup:
                openPickupIntent();
                break;
            case R.id.ll_drop:
                openDropIntent();
                break;
            case R.id.btn_buy_package_fare:
                checkForBuyPackage();
                break;
            default:
                break;

        }
    }

    private void checkForBuyPackage() {
        if (sourcePoint.getText().equals("")) {
            openPickupIntent();
            //Toast.makeText(context, "s", Toast.LENGTH_SHORT).show();
        } else if (destinationPoint.getText().equals("")) {
            openDropIntent();
            //Toast.makeText(context, "d", Toast.LENGTH_SHORT).show();

        } else {
            View alertLayout = LayoutInflater.from(context).inflate(R.layout.custom_dialog_for_package, null);
            final AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(context);
           fromLoc = alertLayout.findViewById(R.id.tv_from);
           toLoc = alertLayout.findViewById(R.id.tv_to);
           packageDistance = alertLayout.findViewById(R.id.tv_distance);
           fare = alertLayout.findViewById(R.id.tv_fare);
           showFare = alertLayout.findViewById(R.id.tv_buy_select_package);
           daysDropDown = alertLayout.findViewById(R.id.spinner);
            daysDropDown.setOnItemSelectedListener(this);
            fromLoc.setText(sourcePoint.getText());
            toLoc.setText(destinationPoint.getText());
            packageDistance.setText(sourcePoint.getText());
            fare.setText(sourcePoint.getText());
            if(!sourcePoint.getText().toString().isEmpty()
                    &&!destinationPoint.getText().toString().isEmpty())
            {
                Double distanceFToHome =    calculationByDistance(sourceLatlong,destinationLatlong);
                fare.setText(distanceFToHome.toString());

            }
            showFare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(context, "Booked Package", Toast.LENGTH_SHORT).show();
                }
            });
            ArrayAdapter aa = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,country);
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //Setting the ArrayAdapter data on the Spinner
            daysDropDown.setAdapter(aa);
            mAlertBuilder.setView(alertLayout);
            mAlertBuilder.show();
        }
    }

    private void openDropIntent() {

        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID,
                Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(context);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_DROP);
    }

    private void openPickupIntent() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID,
                Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG);


        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(context);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                sourceLatlong = place.getLatLng();
                String address[] =getSplitAddress(place.getAddress());
                sourcePoint.setText(address[0]+address[1]+address[2]+address[3]);
                Log.i(TAG, "Place: " + place.getAddress() + ", " + place.getId());
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
                destinationLatlong = place.getLatLng();
                String address[] = getSplitAddress(place.getAddress());
                destinationPoint.setText(address[0]+address[1]);
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

    @Override
    public void onResume() {
        super.onResume();
        intentFilter = IntentFilterCondition.getInstance().callIntentFilter();
        getActivity().registerReceiver(internetBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(internetBroadcastReceiver);
    }
    private String[] getSplitAddress(String address)
    {
        String[] addressArray = address.split(",", 5);

        return addressArray;
    }
    public double calculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(getActivity(),country[i] , Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
