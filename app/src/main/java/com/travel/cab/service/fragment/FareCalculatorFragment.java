package com.travel.cab.service.fragment;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.travel.cab.service.BuildConfig;
import com.travel.cab.service.R;
import com.travel.cab.service.activity.PackageDetailActivity;
import com.travel.cab.service.broadcast.InternetBroadcastReceiver;
import com.travel.cab.service.ui.IntentFilterCondition;

import org.joda.time.DateTime;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    Boolean isButtonClickedTo = false;
    Boolean isButtonClickedFrom = false;
    private FusedLocationProviderClient mClient;
    private Location mLocation;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private TextView value;
    private Marker marker = null;
    private AutocompleteSupportFragment autocompleteFragment, autocompleteFragmentDrop;
    private String placeKey = BuildConfig.AllApiKey;
    private String directionKey = BuildConfig.AllApiKey;
    private LatLng destinationLatlong, sourceLatlong;
    private Context context;
    private LinearLayout pickLoc, dropLoc;
    private int AUTOCOMPLETE_REQUEST_CODE = 101;
    private int AUTOCOMPLETE_REQUEST_CODE_DROP = 102;
    private IntentFilter intentFilter;
    private InternetBroadcastReceiver internetBroadcastReceiver;
    private Button buyPackageFare, showFare;
    private TextView sourcePoint, destinationPoint;
    private TextView fromLoc, toLoc, packageDistance, fare;
    private String[] days = {"select service days", "1day", "2days", "3days", "4days", "5days"};
    private String[] serviceType = {"select service type", "one sided", "both sided"};
    private String[] vehicleType = {"select vehicle type", "Bike", "Car"};
    private String toAddress, fromAddress;
    private String[] shortToAddress, shortFromAddress;
    private Spinner daysDropDown,serviceTypeDropDown,vehicleDropDown;
    private ImageView showMoreContentForTo, showMoreContentForFrom, closeBuyPackageDialog;
    private DirectionsResult result;
    private ProgressBar progressBar;
    private boolean isDirectionGet = false;
    private RectangularBounds bounds;
    private AlertDialog show;
    private int numOfDays,typeOfServiceAtPosition,vehicleTypePosition;
    private RelativeLayout relativeLayout;
    private PackageInfo mPackageInfo;
    private  ProgressDialog dialog;

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
//        value = view.findViewById(R.id.textView);
        pickLoc = view.findViewById(R.id.ll_pickup);
        dropLoc = view.findViewById(R.id.ll_drop);
        sourcePoint = view.findViewById(R.id.tv_pickup_location);
        destinationPoint = view.findViewById(R.id.tv_drop_location);
        relativeLayout = view.findViewById(R.id.rl_fare_calculator);
        buyPackageFare = view.findViewById(R.id.btn_buy_package_fare);
        progressBar = view.findViewById(R.id.show_progress);
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), placeKey);
        }
        pickLoc.setOnClickListener(this);
        dropLoc.setOnClickListener(this);
        buyPackageFare.setOnClickListener(this);

        /*( ----regiion inwhich user can search the places)*/
        bounds = RectangularBounds.newInstance(
                new LatLng(28.567865, 77.326182),
                new LatLng(28.752388, 77.498774));


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
      /*  BaseActivity baseActivity = new BaseActivity();
        baseActivity.checkPermission(getActivity());*/
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            buildAlertMessageNoLocationPermission();
            Toast.makeText(context, getString(R.string.permission_not_grant), Toast.LENGTH_SHORT).show();
            return;
        } else {
            checkGpsState();
            mClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    mLocation = location;
                    if (mLocation != null) {
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.icon);
                        LatLng currentLatLong = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                        // mMap.addMarker(new MarkerOptions().position(currentLatLong).title("Marker in India").icon(icon));
                        //Move the camera to the user's location and zoom in!
                        if (mMap != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14.0f));
                        } else {
                            return;
                        }
                        Log.i(TAG, "onSuccess: " + location.getLatitude() + "-" + location.getLongitude());

                        CircleOptions circleOptions = new CircleOptions()
                                .center(currentLatLong).radius(1000)
                                .fillColor(Color.TRANSPARENT).strokeColor(Color.GREEN).strokeWidth(8);
                        Circle mCircle = mMap.addCircle(circleOptions);
                        Log.i(TAG, "onSuccess: " + location.getLongitude());
                    } else {
                        Toast.makeText(context, "Problem occured", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            mapFragment.getMapAsync(this);
        }
        configureCameraIdle();

    }

    private void checkGpsState() {
        final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }


    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    private void buildAlertMessageNoLocationPermission() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Enable Location permission for map, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                       Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",getActivity().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void configureCameraIdle() {

        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLng latLng = mMap.getCameraPosition().target;
                sourceLatlong = latLng;
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.pin);
                Geocoder geocoder = new Geocoder(context);
                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        String locality = addressList.get(0).getAddressLine(0);
                        String country = addressList.get(0).getCountryName();
                        if (!locality.isEmpty() && !country.isEmpty()) {
                            // value.setText(locality + "  " + country);
                            fromAddress = locality;
                            shortFromAddress = getSplitAddress(locality);
                            setSelectedLocationByUserEitherSourceOrDestinationPoint(shortFromAddress, sourcePoint);
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
      /*  mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000); // two minute interval
        mLocationRequest.setFastestInterval(120000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mClient.requestLocationUpdates(mLocationRequest,this, Looper.myLooper());*/

        mMap.setOnCameraIdleListener(onCameraIdleListener);
        mMap.setMyLocationEnabled(true);
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
          dialog = new ProgressDialog(context);
        if (sourcePoint.getText().equals("")) {
            Toast.makeText(context, getString(R.string.select_pickup_location), Toast.LENGTH_SHORT).show();
            openPickupIntent();
        } else if (destinationPoint.getText().equals("")) {
            Toast.makeText(context, getString(R.string.select_drop_location), Toast.LENGTH_SHORT).show();

            openDropIntent();
        } else {
            setUpDialogForShowingPackage();

        }


    }

    private void setUpDialogForShowingPackage() {
        dialog.show();
        progressBar.setVisibility(View.VISIBLE);
        String distance = getDistanceBetweenTwoLocation();
        View alertLayout = LayoutInflater.from(context).inflate(R.layout.custom_dialog_for_package, null);
        final AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(context);
        fromLoc = alertLayout.findViewById(R.id.tv_book_package_for_data_from);
        toLoc = alertLayout.findViewById(R.id.tv_book_package_for_data_to);
        packageDistance = alertLayout.findViewById(R.id.tv_distance);
        fare = alertLayout.findViewById(R.id.tv_fare);
        showFare = alertLayout.findViewById(R.id.tv_buy_select_package);
        daysDropDown = alertLayout.findViewById(R.id.spinner);
        serviceTypeDropDown = alertLayout.findViewById(R.id.spinner_select_service_type);
        vehicleDropDown = alertLayout.findViewById(R.id.spinner_select_Vehicle_type);
        showMoreContentForTo = alertLayout.findViewById(R.id.img_book_package_more_content_to);
        showMoreContentForFrom = alertLayout.findViewById(R.id.img_book_package_more_content_from);
        closeBuyPackageDialog = alertLayout.findViewById(R.id.img_close);
        daysDropDown.setOnItemSelectedListener(this);
        serviceTypeDropDown.setOnItemSelectedListener(this);
        vehicleDropDown.setOnItemSelectedListener(this);
        String[] shortAddressFrom = getSplitAddressForShort(fromAddress);
        setShortSelectedLocationByUser(shortAddressFrom, fromLoc);
        String[] shortAddressTo = getSplitAddressForShort(toAddress);
        setShortSelectedLocationByUser(shortAddressTo, toLoc);


        showFare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!fromLoc.getText().toString().isEmpty()
                        && !toLoc.getText().toString().isEmpty()
                        && !packageDistance.getText().toString().isEmpty()) {
                    if(typeOfServiceAtPosition>0)
                    {
                        if (numOfDays > 0) {
                            if(vehicleTypePosition>0)
                            {
                                float rideFare = calculateFare(packageDistance.getText().toString(), numOfDays,typeOfServiceAtPosition,typeOfServiceAtPosition);
                                fare.setText(String.valueOf(rideFare));
                                sendPackageDetailViaIntent();
                            }
                            else
                            {
                                Toast.makeText(context, getString(R.string.select_vehicle_type), Toast.LENGTH_SHORT).show();
                            }



                        } else {
                            Toast.makeText(context, getString(R.string.select_days), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(context, getString(R.string.select_service_type), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(context, getString(R.string.click_on_buy_package), Toast.LENGTH_SHORT).show();
                }
            }
        });
        // spinner for service required days
        ArrayAdapter aa = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, days);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        daysDropDown.setAdapter(aa);
        // spinner for service type it may be of one sided or both sided
        ArrayAdapter arayAdapterForServiceType = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, serviceType);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        serviceTypeDropDown.setAdapter(arayAdapterForServiceType);
        // spinner for vehicle type
        ArrayAdapter arayAdapterForVehicleType = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, vehicleType);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        vehicleDropDown.setAdapter(arayAdapterForVehicleType);
        showMoreContentForFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getherDataFrom = fromAddress;
                String[] shortAddress = getSplitAddressForShort(getherDataFrom);
                if (!isButtonClickedFrom) {
                    fromLoc.setText(fromAddress);
                    isButtonClickedFrom = true;
                } else {
                    setShortSelectedLocationByUser(shortAddress, fromLoc);
                    //fromLoc.setText(R.string.content_from_show_package);
                    isButtonClickedFrom = false;
                }
            }
        });
        showMoreContentForTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getherDataTo = toAddress;
                String[] shortAddress = getSplitAddressForShort(getherDataTo);
                if (!isButtonClickedTo) {
                    toLoc.setText(toAddress);
                    isButtonClickedTo = true;
                } else {
                    setShortSelectedLocationByUser(shortAddress, toLoc);
                    isButtonClickedTo = false;
                }
            }
        });
         /*   if (isDirectionGet) {
                mAlertBuilder.setView(alertLayout);
                mAlertBuilder.show();
            } else {
                Toast.makeText(context, "Service not available", Toast.LENGTH_SHORT).show();
            }*/
        if (distance != null) {
            dialog.dismiss();
            progressBar.setVisibility(View.GONE);
            packageDistance.setText(distance);
            List<LatLng> decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.getEncodedPath());
            mMap.addPolyline(new PolylineOptions().addAll(decodedPath).width(15).color(Color.GREEN));
            mAlertBuilder.setView(alertLayout);
            show = mAlertBuilder.show();
            show.setCancelable(false);

        }
        closeBuyPackageDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                packageDistance.setText("");
                show.dismiss();
            }
        });
    }

    private void sendPackageDetailViaIntent() {
        Map<String, String> packageMap = new HashMap<>();
        packageMap.put("fromLocation", fromAddress);
        packageMap.put("toLocation", toAddress);
        packageMap.put("fare", fare.getText().toString());
        packageMap.put("distanceDiff", packageDistance.getText().toString());
        packageMap.put("serviceDays", String.valueOf(numOfDays));
        packageMap.put("serviceType", serviceType[typeOfServiceAtPosition]);
        packageMap.put("vehicleType", vehicleType[vehicleTypePosition]);
        Log.i(TAG, "sendPackageDetailViaIntent: " + packageMap.size()
        );
//        mPackageInfo.getPackageDetail(packageMap);
        Intent intent = new Intent(context, PackageDetailActivity.class);
        intent.putExtra("packageInfoMap", (Serializable) packageMap);
        startActivity(intent);

    }
    // for calculating the fare for both bike and car
    private float calculateFare(String packageDistance, int numOfDays,int serviceType,int vechileType) {
        String seperateUnit[] = packageDistance.split(" ");
        if (packageDistance.contains("km")) {
            /*----------when this scenario will come like (1,900)-----------not work------------*/
            if (seperateUnit.length <= 3) {
                float distance = Float.parseFloat(seperateUnit[0]);
                if(vechileType ==1)
                {
                    // condition for one sided or both sided
                    if(serviceType==1)
                    {
                        return (distance * numOfDays )* 4;
                    }
                    else
                    {
                        return (distance * numOfDays )* 4*2;
                    }
                }
                else
                {
                    // condition for one sided or both sided
                    if(serviceType==1)
                    {
                        return (distance * numOfDays) * 7;
                    }
                    else
                    {
                        return (distance * numOfDays) * 7*2;
                    }
                }


            } else {

                Snackbar snackbar = Snackbar
                        .make(relativeLayout, "Service not available", Snackbar.LENGTH_LONG);
                snackbar.show();

                return 0;
            }

        } else if (packageDistance.equals("m")) {
            return 10;
        } else {
            return 0;
        }

    }

    private String getDistanceBetweenTwoLocation() {
        progressBar.setVisibility(View.VISIBLE);
        dialog.show();

        DateTime now = new DateTime();
        try {
            result = DirectionsApi.newRequest(getGeoContext())
                    .mode(TravelMode.DRIVING).origin(fromAddress)
                    .destination(toAddress).departureTime(now).await();
        } catch (ApiException e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            dialog.dismiss();
            Toast.makeText(context, getString(R.string.some_problem_occured) + e, Toast.LENGTH_LONG).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            dialog.dismiss();
            Toast.makeText(context, getString(R.string.some_problem_occured) + e, Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            dialog.dismiss();
            Toast.makeText(context, getString(R.string.some_problem_occured) + e, Toast.LENGTH_LONG).show();
        }
        if (result != null) {
            return result.routes[0].legs[0].distance.humanReadable;
        } else {
            Toast.makeText(context, "Service not available", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            dialog.dismiss();
        }
        //new DirectionResult().execute();
        return null;
    }

    /*geocontext is passed in a request api*/
    private GeoApiContext getGeoContext() {
        dialog.show();
        progressBar.setVisibility(View.VISIBLE);
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(directionKey).
                        setConnectTimeout(0, TimeUnit.SECONDS)
                .setReadTimeout(0, TimeUnit.SECONDS)
                .setWriteTimeout(0, TimeUnit.SECONDS);
    }

    private void openDropIntent() {

        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID,
                Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields).setCountry("In")
                .setLocationBias(bounds)
                .build(context);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_DROP);
    }

    private void openPickupIntent() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID,
                Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);


        // Start the autocomplete intent.
        // noida 28.567865, 77.326182 gip
        // Create a RectangularBounds object.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields).setCountry("In")
                .setLocationBias(bounds)
                .build(context);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                sourceLatlong = place.getLatLng();
                fromAddress = place.getAddress();
                shortFromAddress = getSplitAddress(place.getAddress());
                setSelectedLocationByUserEitherSourceOrDestinationPoint(shortFromAddress, sourcePoint);
                //sourcePoint.setText(address[0]+address[1]+address[2]+address[3]);
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
            if (resultCode == Activity.RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                destinationLatlong = place.getLatLng();
                toAddress = place.getAddress();
                shortToAddress = getSplitAddress(place.getAddress());
                setSelectedLocationByUserEitherSourceOrDestinationPoint(shortToAddress, destinationPoint);
                // destinationPoint.setText(address[0]+address[1]);
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

    private void setSelectedLocationByUserEitherSourceOrDestinationPoint(String[] address, TextView eitherSourceOrDesinationPoint) {
        switch (address.length) {
            case 1:
                eitherSourceOrDesinationPoint.setText(address[0]);
                break;
            case 2:
                eitherSourceOrDesinationPoint.setText(address[0] + address[1]);
                break;
            case 3:
                eitherSourceOrDesinationPoint.setText(address[0] + address[1] + address[2]);
                break;
            case 4:
                eitherSourceOrDesinationPoint.setText(address[0] + address[1] + address[2] + address[3]);
                break;
            case 5:
                eitherSourceOrDesinationPoint.setText(address[0] + address[1]
                        + address[2] + address[3] + address[4]);
                break;
            case 6:
                eitherSourceOrDesinationPoint.setText(address[0] + address[1]
                        + address[2] + address[3] + address[4] + address[5]);
                break;
            default:
                break;
        }
    }

    private void setShortSelectedLocationByUser(String[] address, TextView eitherSourceOrDesinationPoint) {
        switch (address.length) {
            case 1:
                eitherSourceOrDesinationPoint.setText(address[0]);
                break;
            case 2:
                eitherSourceOrDesinationPoint.setText(address[0] + address[1]);
                break;
            case 3:
                eitherSourceOrDesinationPoint.setText(address[0] + address[1]);
                break;
            default:
                break;
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

    @Override
    public void onStop() {
        super.onStop();
        if (show != null)
            show.dismiss();
    }

    private String[] getSplitAddress(String address) {
        String[] addressArray = address.split(",", 5);

        return addressArray;
    }

    private String[] getSplitAddressForShort(String address) {
        String[] addressArray = address.split(",", 3);

        return addressArray;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if(adapterView.getAdapter().getItem(0).toString().equals("select service type"))
        {
         typeOfServiceAtPosition = position;
        }
        else if(adapterView.getAdapter().getItem(0).toString().equals("select vehicle type"))
        {
            vehicleTypePosition = position;
        }
        else
        {
            numOfDays = position;
           // Toast.makeText(getActivity(), days[position], Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public interface PackageInfo {
        void getPackageDetail(Map<String, String> map);
    }

    /* ------------------inner class to find the distance between two location ---------------------------*/
    /*class DirectionResult extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            Log.i(TAG, "onPreExecute: ");
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.i(TAG, "doInBackground: ");
            DateTime now = new DateTime();

            try {
                result = DirectionsApi.newRequest(getGeoContext())
                        .mode(TravelMode.DRIVING).origin(fromAddress)
                        .destination(toAddress).departureTime(now).await();
                Thread.sleep(3000);
                if (result != null) {
                    return result.routes[0].legs[0].distance.humanReadable;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (com.google.maps.errors.ApiException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String distanceFToHome) {
            super.onPostExecute(distanceFToHome);
            Log.i(TAG, "onPostExecute: ");
            if (distanceFToHome != null) {
                isDirectionGet = true;
                packageDistance.setText(distanceFToHome);
                List<LatLng> decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.getEncodedPath());
                mMap.addPolyline(new PolylineOptions().addAll(decodedPath).width(25).color(Color.RED));
                progressBar.setVisibility(View.GONE);
            } else {
                packageDistance.setText("");
                Toast.makeText(context, "Service not Available", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }


        }

        *//*geocontext is passed in a request api*//*
        private GeoApiContext getGeoContext() {
            GeoApiContext geoApiContext = new GeoApiContext();
            return geoApiContext.setQueryRateLimit(3)
                    .setApiKey(directionKey).
                            setConnectTimeout(1, TimeUnit.SECONDS)
                    .setReadTimeout(1, TimeUnit.SECONDS)
                    .setWriteTimeout(1, TimeUnit.SECONDS);
        }
    }*/
}
