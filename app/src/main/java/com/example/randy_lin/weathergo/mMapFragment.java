package com.example.randy_lin.weathergo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static com.google.android.gms.internal.zzagr.runOnUiThread;

public class mMapFragment extends Fragment implements OnMapReadyCallback
        , GoogleMap.OnMapLongClickListener, GoogleMap.OnMyLocationButtonClickListener
        , GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener
        , SensorEventListener, GoogleMap.OnMapClickListener, View.OnClickListener {
    static View mapRootView;
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        }

        @SuppressLint("MissingPermission")
        public void onProviderDisabled(String provider) {
        }

        @SuppressLint("MissingPermission")
        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    float[] mGravity;
    float[] mGeomagnetic;
    Bitmap[] weather_icon;
    Bundle mapData;
    private Handler handler = new Handler();
    private Weather weather;
    private LocationManager locationManager;
    private Geolocation geo;
    private OnFragmentInteractionListener mListener;
    private MapView mMapView;
    private GoogleMap mMap;
    private PlaceAutocompleteFragment autocompleteFragment;
    private PolylineOptions path;
    private Polyline path_polyline;
    private LatLng clickLoc;
    private LatLng infoLoc;
    private Marker srcMarker;
    private Marker dstMarker;
    private Marker placeMarker;
    private Marker infoMarker;
    private Boolean MarkerState;
    private boolean srcMarkerExist;
    private boolean dstMarkerExist;
    private boolean placeMarkerExist;
    private boolean infoMarkerExist;
    private boolean pathExist;
    private chatView showChat;
    private Toast toastMsg;
    private final PlaceSelectionListener placeSelected = new PlaceSelectionListener() {
        @Override
        public void onPlaceSelected(Place place) {
            LatLng loc = place.getLatLng();
            if (placeMarkerExist) {
                placeMarker.remove();
                placeMarkerExist = false;
            }
            if (MarkerState == null) {
                if (srcMarkerExist) {
                    if (loc.equals(srcMarker.getPosition())) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));
                        return;
                    }
                }
                if (dstMarkerExist) {
                    if (loc.equals(dstMarker.getPosition())) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));
                        return;
                    }
                }
                placeMarker = mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .draggable(true));
                placeMarkerExist = true;
            } else if (MarkerState) { //src
                addSrcMarker(loc);
            } else {
                addDstMarker(loc);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));
        }

        @Override
        public void onError(Status status) {
            Toast.makeText(getActivity(), "Place selection failed: " + status.getStatusMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    };
    private int h;
    private int near;
    private int index;
    private String infoLocName;
    private Runnable updateTimer = new Runnable() {
        public void run() {
            Time t = new Time();
            t.setToNow();
            if (h != t.hour) {
                h = t.hour;
                if (!isNetworkConnected(getActivity())) return;
                requestPermissions(
                        new String[]{
                                ACCESS_COARSE_LOCATION,
                                ACCESS_FINE_LOCATION},
                        123);
                weather = new Weather();
                if (infoLoc != null && infoMarkerExist) {
                    infoLocName = getGeoloc(infoLoc);
                    setIndex();
                    addChatMarker();
                }
            }
            handler.postDelayed(this, 1000);
        }
    };

    public mMapFragment() {
    }

    public static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMinimumWidth(), view.getMinimumHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mapRootView == null) {
            mapRootView = inflater.inflate(R.layout.fragment_mmap, container, false);
        }
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        detectDevice();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        mMapView = mapRootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        (mapRootView.findViewById(R.id.srcBtn)).setOnClickListener(this);
        (mapRootView.findViewById(R.id.dstBtn)).setOnClickListener(this);
        showChat = new chatView(getActivity()).setmBounds(10);
        geo = new Geolocation(getContext());
        h = -1;
        Resources res = getResources();
        weather_icon = new Bitmap[]{
                null,
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_1),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_2),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_3),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_4),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_5),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_6),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_7),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_8),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_9),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_10),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_night_1),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_night_5),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_night_7),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_night_8),
                BitmapFactory.decodeResource(res, R.drawable.weather_icon_night_10)
        };
        return mapRootView;
    }

    private LatLng ArraytoLatLng(double[] index) {
        return new LatLng(index[0], index[1]);
    }

    private double[] LatLngtoArray(LatLng latLng) {
        return new double[]{latLng.latitude, latLng.longitude};
    }

    private void detectDevice() {
        boolean isNetworkConnected = isNetworkConnected(getActivity());
        boolean isWifiConnected = ((WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
        boolean isGPSConnected = isGPSConnected(getActivity());
        if (!isNetworkConnected && !isWifiConnected) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setMessage("偵測裝置到尚未開啟WiFi或行動網路，這可能會使大部分功能無法使用。");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
        if (!isGPSConnected) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setMessage("偵測到裝置尚未開啟GPS定位服務，這可能會使得無法取得當前位置。");
            dialog.setTitle("是否開啟GPS定位服務?");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int arg1) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 112);
                    dialog.dismiss();
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    private boolean isGPSConnected(Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        h = -1;
        handler.removeCallbacks(updateTimer);
        handler.post(updateTimer);
        mMapView.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 123: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //mProvider = getProvider();
                    mMapView.getMapAsync(this);
                } else {
                    makeToast("拒絕授予權限，將使得大部分功能無法使用。");
                    if (!shouldShowRequestPermissionRationale(permissions[0])) {
                        Snackbar snackbar =
                                Snackbar.make(getActivity().findViewById(R.id.snakcontainer_map), "前往取得GPS位置權限",
                                        Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("GOGO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getActivity().getPackageName(), null));
                                startActivityForResult(intent, 111);
                            }
                        });
                        snackbar.show();
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 111) {
            requestPermissions(
                    new String[]{
                            ACCESS_COARSE_LOCATION,
                            ACCESS_FINE_LOCATION},
                    123);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true); //抓取自己位置並標上小藍點
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                calNear(Math.round(mMap.getCameraPosition().zoom));
            }
        });
        Location location = getLastKnowLocation();
        if (location != null) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));
            autocompleteFragment = new PlaceAutocompleteFragment();
            autocompleteFragment.setOnPlaceSelectedListener(placeSelected);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.autocomplete_fragment, autocompleteFragment).commit();
            autocompleteFragment.setBoundsBias(new LatLngBounds(loc, loc));
        }
        reset();
        if (mapData != null) {
            double[] src = mapData.getDoubleArray("srcMarker");
            if (src != null) addSrcMarker(ArraytoLatLng(src));
            else srcMarkerExist = false;
            double[] dst = mapData.getDoubleArray("dstMarker");
            if (dst != null) addDstMarker(ArraytoLatLng(dst));
            else dstMarkerExist = false;
            double[] place = mapData.getDoubleArray("placeMarker");
            if (place != null) {
                placeMarker = mMap.addMarker(new MarkerOptions()
                        .position(ArraytoLatLng(place))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .draggable(true));
                placeMarkerExist = true;
            } else placeMarkerExist = false;
            double[] info = mapData.getDoubleArray("infoMarker");
            if (info != null) {
                infoLoc = ArraytoLatLng(info);
                addChatMarker();
            } else infoMarkerExist = false;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ArraytoLatLng(mapData.getDoubleArray("camera")),
                    mapData.getFloat("zoom")));
        }
    }

    private void reset() {
        MarkerState = null;
        srcMarkerExist = false;
        dstMarkerExist = false;
        placeMarkerExist = false;
        infoMarkerExist = false;
        pathExist = false;
    }

    @SuppressLint("MissingPermission")
    public Location getLastKnowLocation() {
        Location location = null;
        try {
            locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            1000,
                            0, locationListener);
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                1000,
                                0, locationListener);
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.srcBtn:
                MarkerState = true;
                break;
            case R.id.dstBtn:
                MarkerState = false;
                break;
        }
    }

    private void calNear(int zoom) {
        switch (zoom) {
            case 2: // under 5 couldn't show
            case 3:
            case 4:
            case 5:
                near = 0;
                break;
            case 6:
                near = 33000;
                break;
            case 7:
                near = 15000;
                break;
            case 8:
                near = 5000;
                break;
            case 9:
                near = 1640;
                break;
            case 10:
                near = 580;
                break;
            case 11:
                near = 200;
                break;
            case 12:
                near = 70;
                break;
            case 13:
                near = 40;
                break;
            case 14:
                near = 25;
                break;
            case 15:
                near = 17;
                break;
            case 16:
                near = 12;
                break;
            case 17:
                near = 10;
                break;
            case 18:
                near = 8;
                break;
            case 19:
                near = 4;
                break;
            case 20:
                near = 2;
                break;
            case 21:
                near = 0;
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                CameraPosition index = CameraPosition.builder(mMap.getCameraPosition())
                        .bearing((float) Math.toDegrees(orientation[0]))
                        .build();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(index));
            }
        }
    }

    private boolean isPointOnLine(LatLng linePointA, LatLng linePointB, LatLng point) {
        final float[] d = new float[1];
        Location.distanceBetween(Math.abs(linePointA.latitude - point.latitude) + Math.abs(linePointB.latitude - point.latitude),
                Math.abs(linePointA.longitude - point.longitude) + Math.abs(linePointB.longitude - point.longitude),
                Math.abs(linePointA.latitude - linePointB.latitude), Math.abs(linePointA.longitude - linePointB.longitude), d);
        return d[0] <= near;
    }

    private String getGeoloc(LatLng latLng) {
        String preGeoloc = null;
        String nowGeoloc = "";
        try {
            preGeoloc = geo.getGeolocation(latLng);
            wait_a_minute(preGeoloc, null, false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!Objects.equals(preGeoloc, nowGeoloc)) {
            nowGeoloc = preGeoloc;
            try {
                boolean successGet = weather.getWeather(nowGeoloc);
                if (!successGet) return preGeoloc;
                wait_a_minute(weather, null, false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return preGeoloc;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        clickLoc = latLng;
        wait_a_minute(clickLoc, latLng, true);

        Log.i("click", clickLoc.latitude + ", " + clickLoc.longitude);
        Log.i("click_", latLng.latitude + ", " + latLng.longitude);

        List<LatLng> polyCoords;
        if (infoMarkerExist) {
            infoMarker.remove();
            infoMarkerExist = false;
        }
        if (pathExist) {
            polyCoords = path.getPoints();
            for (int i = 1; i < polyCoords.size(); ++i) {
                if (isPointOnLine(polyCoords.get(i - 1), polyCoords.get(i), latLng)) {
                    Log.i("HERE", "path click");
                    infoLoc = latLng;
                    wait_a_minute(infoLoc, latLng, true);
                    infoLocName = getGeoloc(latLng);
                    if (infoLocName != null) {
                        setIndex();
                        addChatMarker();
                    }
                    break;
                }
            }
        }
        if (MarkerState != null) {
            if (MarkerState) { //src
                addSrcMarker(latLng);
            } else {
                addDstMarker(latLng);
            }
        }
    }

    private void setIndex() {
        String[] time = weather.getTime();
        for (index = 1; index < weather.size(); ++index) {
            if ((Integer.valueOf(time[index - 1].substring(11, 13)) == h) ||
                    (Integer.valueOf(time[index].substring(11, 13)) > h && Integer.valueOf(time[index - 1].substring(11, 13)) < h) ||
                    (Integer.valueOf(time[index].substring(11, 13)) == 0 && h > 21)) {
                index--;
                break;
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (placeMarkerExist)
            placeMarker.remove();

        placeMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .draggable(true));
        placeMarkerExist = true;

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        if (srcMarker != null && Objects.equals(marker.getId(), srcMarker.getId())) {
            srcMarker.remove();
            srcMarkerExist = false;
            MarkerState = null;
            if (dstMarkerExist && path_polyline != null) {
                if (infoMarkerExist) {
                    infoMarker.remove();
                    infoMarkerExist = false;
                }
                path_polyline.remove();
                pathExist = false;
            }
            makeToast("成功移除起點");
        } else if (dstMarker != null && Objects.equals(marker.getId(), dstMarker.getId())) {
            dstMarker.remove();
            dstMarkerExist = false;
            MarkerState = null;
            if (srcMarkerExist && path_polyline != null) {
                if (infoMarkerExist) {
                    infoMarker.remove();
                    infoMarkerExist = false;
                }
                path_polyline.remove();
                pathExist = false;
            }
            makeToast("成功移除終點");
        } else {
            placeMarker.remove();
            placeMarkerExist = false;
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
    }

    private void addChatMarker() {
        showChat
                .setLoc(infoLocName)
                .setWeather(weather.getWx()[index])
                .setTemperature(weather.getT()[index])
                .setPicture(weather_icon[weather_code(weather.getWeatherCode()[index])])
                .setRain_p(weather.getPoP6h()[index >> 1]);
        if (infoMarkerExist || infoMarker != null) infoMarker.remove();
        infoMarker = mMap.addMarker(new MarkerOptions().position(infoLoc).icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromView(showChat))));
        infoMarkerExist = true;
    }

    private void addSrcMarker(LatLng loc) {
        if (srcMarkerExist || srcMarker != null) srcMarker.remove();
        srcMarker = mMap.addMarker(new MarkerOptions()
                .position(loc)
                .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.ic_directions_run_mapuse_24dp))
                .draggable(true));
        srcMarker.showInfoWindow();
        srcMarkerExist = true;
        MarkerState = null;
        if (dstMarkerExist) setPath();
    }

    private void addDstMarker(LatLng loc) {
        if (dstMarkerExist || dstMarker != null) dstMarker.remove();
        dstMarkerExist = true;
        dstMarker = mMap.addMarker(new MarkerOptions()
                .position(loc)
                .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.ic_flag_mapuse_24dp))
                .draggable(true));
        dstMarker.showInfoWindow();
        MarkerState = null;
        if (srcMarkerExist) setPath();
    }

    public void setPath() {
        if (pathExist) path_polyline.remove();
        pathExist = true;
        pathAsyncTask asyDraw = new pathAsyncTask(getActivity(), srcMarker.getPosition(), dstMarker.getPosition());
        asyDraw.execute();
        makeToast("路徑設定成功");
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 40);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 40, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void makeToast(CharSequence msg) {
        if (toastMsg != null)
            toastMsg.cancel();
        toastMsg = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
        toastMsg.show();
    }

    private void wait_a_minute(Object a, Object b, boolean eq) {
        while (eq != Objects.equals(a, b)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = cm.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /*---------------------------------------------------------------------------------------------------------------------------*/
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            Log.e("", " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (!isNetworkConnected(getActivity())) return;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 7500, 0, locationListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMap != null) {
            pack();
            mMap.clear();
        }
        locationManager.removeUpdates(locationListener);
        handler.removeCallbacks(updateTimer);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    private void pack() {
        mapData = new Bundle();
        mapData.putDoubleArray("srcMarker", srcMarkerExist ? LatLngtoArray(srcMarker.getPosition()) : null);
        mapData.putDoubleArray("dstMarker", dstMarkerExist ? LatLngtoArray(dstMarker.getPosition()) : null);
        mapData.putDoubleArray("placeMarker", placeMarkerExist ? LatLngtoArray(placeMarker.getPosition()) : null);
        mapData.putDoubleArray("infoMarker", infoMarkerExist ? LatLngtoArray(infoMarker.getPosition()) : null);
        mapData.putDoubleArray("camera", mMap.getCameraPosition() == null ? null : LatLngtoArray(mMap.getCameraPosition().target));
        mapData.putFloat("zoom", mMap.getCameraPosition() == null ? null : mMap.getCameraPosition().zoom);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mMapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private int weather_code(String code) {
        String[][] code_table = {
                {}, {"01"}, {"05", "06"}, {"03"}, {"04", "12", "13", "26", "49", "57"}, {"02", "07", "08"},
                {"17", "18", "31", "36", "58", "59"}, {"24", "29", "34"}, {"43", "45", "46"}, {"44"}, {"60", "61"}
        };

        for (int i = 1; i < code_table.length; ++i)
            for (String num : code_table[i])
                if (num.equals(code)) {
                    if (h > 17 || h < 6) {
                        switch (i) {
                            case 1:
                                return 11;
                            case 5:
                                return 12;
                            case 7:
                                return 13;
                            case 8:
                                return 14;
                            case 10:
                                return 15;
                            default:
                                break;
                        }
                    }
                    return i;
                }
        return 0;
    }

    /*---------------------------------------------------------------------------------------------------------------------------*/

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private class pathAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private ProgressDialog progressDialog;
        private LatLng src, dst;

        pathAsyncTask(Context c, LatLng s, LatLng d) {
            mContext = c;
            src = s;
            dst = d;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String rst = new JSONParser().getJSONFromUrl(makeURL(src, dst));
            drawPath(rst);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            progressDialog.dismiss();
        }

        private String makeURL(LatLng src, LatLng dst) {
            return "https://maps.googleapis.com/maps/api/directions/json"
                    + "?origin="
                    + Double.toString(src.latitude)
                    + ","
                    + Double.toString(src.longitude)
                    + "&destination="
                    + Double.toString(dst.latitude)
                    + ","
                    + Double.toString(dst.longitude)
                    + "&sensor=false&mode=driving&alternatives=true"
                    + "&key=" + getResources().getString(R.string.google_maps_key);
        }

        private void drawPath(String result) {
            try {
                String encodedString = new JSONObject(result)
                        .getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points");
                List<LatLng> list = decodePoly(encodedString);
                path = new PolylineOptions()
                        .addAll(list)
                        .width(16)
                        .color(0xFF1E90FF)
                        .geodesic(true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        path_polyline = mMap.addPolyline(path);
                    }
                });
            } catch (JSONException e) {
                Log.e("DrawPath ERROR", e.toString());
            }
        }

        private List<LatLng> decodePoly(String encoded) {
            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
                lng += dlng;

                LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
                poly.add(p);
            }
            return poly;
        }
    }

}
