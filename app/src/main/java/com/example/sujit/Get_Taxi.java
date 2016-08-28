package com.example.sujit.customerapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

public class Get_Taxi extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,GoogleMap.OnInfoWindowClickListener,GoogleMap.OnMarkerClickListener,LocationListener {

    GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    //Marker currLocationMarker;
    LocationRequest mLocationRequest;
 //   private Marker myMarker;



    TextView textViewName1, textViewLicenseNo1, textViewAddress1, textViewMobile1,textViewTaxiNo1,
            textusername1,textViewName2, textViewLicenseNo2, textViewAddress2, textViewMobile2,textViewTaxiNo2,
            textusername2,textView1,textView2,textView17;

    AppCompatButton buttonCall,buttonSelect,buttonAssign,buttonCall2;

     double currentLatitude;
     double currentLongitude;

    double destnLatitude;
    double destnLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get__taxi);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

               PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocompleteFragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destnLatitude = place.getLatLng().latitude;
                destnLongitude = place.getLatLng().longitude;

                mGoogleMap.clear();
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                mGoogleMap.addMarker(new MarkerOptions().position(latLng));
                mGoogleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .position(place.getLatLng())
                        .title(place.getName().toString()).snippet(place.getId()));

                getDirection();
            }

            @Override
            public void onError(Status status) {
                Log.d("Place Error", "An error occurred: " + status);
            }
        });
    }

    public String makeURL(double sourcelat, double sourcelog, double destlat, double destlog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=AIzaSyCdVpU-8Ccf8hvSnyHE2h2armbBRXyr6JM");

        return urlString.toString();
    }

    private void getDirection() {
        //Getting the URL
        String url = makeURL(currentLatitude, currentLongitude, destnLatitude, destnLongitude);

        //Showing a dialog till we get the route
        final ProgressDialog loading = ProgressDialog.show(this, "Getting Route", "Please wait...", false, false);

        //Creating a string request
        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        //Calling the method drawPath to draw the path
                        drawPath(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Log.d("Direction APi error", error.toString());
                    }
                });
            MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    //The parameter is the server response
    public void drawPath(String result) {
        //Getting both the coordinates
        LatLng from = new LatLng(currentLatitude, currentLongitude);
        LatLng to = new LatLng(destnLatitude, destnLongitude);

        //Calculating the distance in meters
        float distance = (float) SphericalUtil.computeDistanceBetween(from, to);
        //Displaying the distance
        Toast.makeText(this, String.valueOf((distance/1000) + "Kilometers"), Toast.LENGTH_LONG).show();
        showDialog(distance);

        try {
            //Parsing json
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Polyline line = mGoogleMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(8)
                    .color(Color.YELLOW)
                    .geodesic(true)
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }
        return poly;
    }
    private void showDialog(float distance) {

        float dis1=distance/1000;

        String dis = String.valueOf(dis1);

        String time= String.valueOf(dis1*3.03);

        String price = String.valueOf(dis1*35);

        LayoutInflater li = LayoutInflater.from(this);
        //Creating a view to get the dialog box
        View confirmDialog = li.inflate(R.layout.dialog_distance, null);

        //Initizliaing confirm button fo dialog box and edit text of dialog box
        buttonSelect = (AppCompatButton) confirmDialog.findViewById(R.id.buttonSelect);
        buttonAssign = (AppCompatButton) confirmDialog.findViewById(R.id.buttonAssign);

        textView1 = (TextView) confirmDialog.findViewById(R.id.textView1);
        textView2 = (TextView) confirmDialog.findViewById(R.id.textView2);
        textView17=(TextView)confirmDialog.findViewById(R.id.textView17);

        textView1.setText(dis);
        textView2.setText(time);
        textView17.setText(price);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        //Adding our dialog box to the view of alert dialog
        alert.setView(confirmDialog);

        //Creating an alert dialog
        final AlertDialog alertDialog = alert.create();

        //Displaying the alert dialog
        alertDialog.show();

        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                getDrivers();
            }
        });
        buttonAssign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                showProfile();
            }
        });
    }
    private void getDrivers() {
        final ProgressDialog loading = ProgressDialog.show(this, "Getting Drivers", "Please wait...", false, false);
        loading.show();
     //   RequestQueue requestQueue = Volley.newRequestQueue(Get_Taxi.this);
      //  String driversUrl="http://6e1e7877.ngrok.io/willselectataxi";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.willselectataxi, new Response.Listener<String>() {
            @Override
            public void onResponse(String  response) {
                mGoogleMap.clear();
                LatLng latLng1=new LatLng(currentLatitude,currentLongitude);
                mGoogleMap.addMarker(new MarkerOptions().position(latLng1).title("Your Location"));
                loading.dismiss();
                Log.d("Response", response);
                try {
                    JSONObject jsonObject=new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("markers");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = jsonArray.getJSONObject(i);

                        double lang = jsonObj.getDouble("longitude");
                        double lat = jsonObj.getDouble("latitude");

                        LatLng latLng = new LatLng(lat,lang);

                        if (i == 0) {
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(latLng).zoom(15).build();
                            mGoogleMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(cameraPosition));
                        }
                      
                        mGoogleMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_car))
                                .title(jsonObj.getString("username"))
                                .position(latLng));

                        mGoogleMap.setOnInfoWindowClickListener(Get_Taxi.this);
                        mGoogleMap.setOnMarkerClickListener(Get_Taxi.this);
                    }
                    loading.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d( "Error processing JSON", e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.dismiss();
                Toast.makeText(Get_Taxi.this, error.toString(), Toast.LENGTH_LONG).show();
                Log.d("abd", "Error: " + error
                        + ">>" + error.networkResponse.statusCode
                        + ">>" + error.getCause()
                        + ">>" + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {

                SharedPreferences sharedPreferences = getSharedPreferences("LoggedInUser",MODE_PRIVATE);
                String phnNo  = sharedPreferences.getString("phoneNo","");

                Map<String, String> params = new HashMap<>();

                params.put("latitude", String.valueOf(currentLatitude));
                params.put("longitude", String.valueOf(currentLongitude));
                params.put("mobile_number", phnNo);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }

    private void showProfile() {
        LayoutInflater li = LayoutInflater.from(this);
        //Creating a view to get the dialog box
        View confirmDialog = li.inflate(R.layout.dialog_profile, null);

        buttonCall = (AppCompatButton) confirmDialog.findViewById(R.id.buttonCall);
        textViewName1 = (TextView) confirmDialog.findViewById(R.id.textViewName1);
        textViewLicenseNo1 = (TextView) confirmDialog.findViewById(R.id.textViewLicenseNo1);
        textViewAddress1 = (TextView) confirmDialog.findViewById(R.id.textViewAddress1);
        textViewMobile1 = (TextView) confirmDialog.findViewById(R.id.textViewMobile1);
        textViewTaxiNo1 = (TextView) confirmDialog.findViewById(R.id.textViewTaxiNo1);
        textusername1=(TextView)confirmDialog.findViewById(R.id.textusername1);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        //Adding our dialog box to the view of alert dialog
        alert.setView(confirmDialog);

        //Creating an alert dialog
        final AlertDialog alertDialog = alert.create();

        //Displaying the alert dialog
        alertDialog.show();

      //  RequestQueue requestQueue = Volley.newRequestQueue(Get_Taxi.this);
        //Yesbata mobile no pani POST ma pathauna parcha
        final ProgressDialog loading = ProgressDialog.show(this, "Getting Nearest Driver", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.findmeataxi, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loading.dismiss();
                Log.d("FINDMEATAXI", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("driver");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String username = jsonObject1.getString("username");
                        String taxi_number = jsonObject1.getString("taxi_number");
                        String license_number = jsonObject1.getString("license_number");
                        String mobile_number = jsonObject1.getString("mobile_number");
                        String full_name = jsonObject1.getString("full_name");
                        String address = jsonObject1.getString("address");

                        textusername1.setText(username);
                        textViewTaxiNo1.setText(taxi_number);
                        textViewLicenseNo1.setText(license_number);
                        textViewMobile1.setText(mobile_number);
                        textViewName1.setText(full_name);
                        textViewAddress1.setText(address);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loading.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.dismiss();
                Log.d("Volley Error Response:", error.toString());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                SharedPreferences sharedPreferences = getSharedPreferences("LoggedInUser",MODE_PRIVATE);
                String phnNo  = sharedPreferences.getString("phoneNo","");

                Map<String, String> params = new HashMap<>();
                params.put("latitude", String.valueOf(currentLatitude));
                params.put("longitude", String.valueOf(currentLongitude));
                params.put("mobile_number", phnNo);

                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                SendInfo(textusername1.getText().toString());
                // callDriver();
            }
        });
    }
    private void SendInfo(final String s) {
      //  RequestQueue requestQueue = Volley.newRequestQueue(Get_Taxi.this);
        // String confirmUrl = "http://e4729191.ngrok.io/registeracustomer";
        //Creating an string request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.callnow,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //if the server response is success
                        if (response.contains("success")){
                            callDriver();
                      //    startActivity(new Intent(Get_Taxi.this, Information.class));
                        }else{
                            showProfile();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Get_Taxi.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding the parameters otp and username
                SharedPreferences sharedPreferences=getSharedPreferences("LoggedInUser", MODE_PRIVATE);
                String mobile_number=sharedPreferences.getString("phoneNo","");

                params.put("mobile_number",mobile_number);

                params.put("username", s);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void callDriver() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + textViewMobile1.getText()));
        if (ActivityCompat.checkSelfPermission(Get_Taxi.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(Get_Taxi.this, "Map Ready", Toast.LENGTH_SHORT).show();
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            //Getting longitude and latitude
            currentLongitude = location.getLongitude();
            currentLatitude = location.getLatitude();
            //moving the map to location

            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            //Adding marker to map
            mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .position(latLng)
                    .title("Current Location"));

            Toast.makeText(this, "Your Location : " + latLng.toString(), Toast.LENGTH_LONG).show();

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Connection failed", String.valueOf(connectionResult.getErrorCode() + connectionResult.getErrorCode()));
    }

      @Override
    public void onMapReady(GoogleMap gMap) {
        mGoogleMap = gMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
      //  getCurrentLocation();
    }
    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng1 = new LatLng(location.getLatitude(), location.getLongitude());

        mGoogleMap.addMarker(new MarkerOptions().position(latLng1).title("New Position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        //  MarkerOptions markerOptions = new MarkerOptions();
        //markerOptions.position(latLng1);
        //markerOptions.title("Current Position");
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        //mCurrLocation = mGoogleMap.addMarker(markerOptions);

        Toast.makeText(this,"Location Changed",Toast.LENGTH_SHORT).show();

        LatLng from = new LatLng(currentLatitude, currentLongitude);

        Double distance = (SphericalUtil.computeDistanceBetween(from, latLng1));

        Toast.makeText(this, "The Distance Travelled is : " + distance, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
       final String username=marker.getTitle();
        LayoutInflater li = LayoutInflater.from(this);
        //Creating a view to get the dialog box
        View confirmDialog = li.inflate(R.layout.dialog_drivercall, null);

        buttonCall2 = (AppCompatButton) confirmDialog.findViewById(R.id.buttonCall2);
        textViewName2 = (TextView) confirmDialog.findViewById(R.id.textViewName2);
        textViewLicenseNo2 = (TextView) confirmDialog.findViewById(R.id.textViewLicenseNo2);
        textViewAddress2 = (TextView) confirmDialog.findViewById(R.id.textViewAddress2);
        textViewMobile2 = (TextView) confirmDialog.findViewById(R.id.textViewMobile2);
        textViewTaxiNo2 = (TextView) confirmDialog.findViewById(R.id.textViewTaxiNo2);
        textusername2=(TextView)confirmDialog.findViewById(R.id.textusername2);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        //Adding our dialog box to the view of alert dialog
        alert.setView(confirmDialog);

        final AlertDialog alertDialog = alert.create();

        alertDialog.show();
    //    showDriver(username);
    //}
    //private void showDriver(final String user) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.showprofile,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       // showCallProfile(response);
                        Log.d("Selected Driver Info",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("driver");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                String username = jsonObject1.getString("username");
                                String license_number = jsonObject1.getString("license_number");
                                String mobile_number = jsonObject1.getString("mobile_number");
                                String full_name = jsonObject1.getString("full_name");
                                String taxi_number = jsonObject1.getString("taxi_number");
                                String address = jsonObject1.getString("address");
                               // insert(username, taxi_number, license_number,mobile_number,full_name,address);

                                textusername2.setText(username);
                                textViewTaxiNo2.setText(taxi_number);
                                textViewLicenseNo2.setText(license_number);
                                textViewMobile2.setText(mobile_number);
                                textViewName2.setText(full_name);
                                textViewAddress2.setText(address);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Get_Taxi.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding the parameters otp and username
                params.put("username", username);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        buttonCall2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
               // Toast.makeText(Get_Taxi.this,username, Toast.LENGTH_LONG).show();
                sendDriverInfo(username);
            }
        });
    }
    private void sendDriverInfo(final String ss) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.callnow,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //if the server response is success
                        if (response.contains("success")){
                            callDriver2();
                           // startActivity(new Intent(Get_Taxi.this, Information.class));
                        }else{
                           // showProfile();
                            Toast.makeText(Get_Taxi.this, "Call Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Get_Taxi.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                SharedPreferences sharedPreferences=getSharedPreferences("LoggedInUser", MODE_PRIVATE);
                String mobile_number=sharedPreferences.getString("phoneNo","");

                params.put("mobile_number",mobile_number);
                params.put("username", ss);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }

    private void callDriver2() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + textViewMobile2.getText()));
        if (ActivityCompat.checkSelfPermission(Get_Taxi.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);
    }

    /*
        private void showCallProfile(String response) {

        }
        */
    /*
    private void insert(String username, String taxi_number, String license_number, String mobile_number, String full_name, String address) {


        buttonCall2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Get_Taxi.this, (CharSequence) textViewMobile1, Toast.LENGTH_LONG).show();
            }
        });
    }
*/
    @Override
    public boolean onMarkerClick(Marker marker) {
        String name= marker.getTitle();
        Toast.makeText(Get_Taxi.this, name, Toast.LENGTH_LONG).show();

        return false;
    }
}