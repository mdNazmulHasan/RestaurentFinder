package com.nerdcastle.nazmul.restaurentfinder;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,GoogleMap.OnInfoWindowClickListener,
        LocationListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    Place place;
   // ArrayList<Place> placeArrayList;
    String placesAPI_KEY;
    String urlToGetNearestPlace;
    double lat;
    double lng;
    String name;
    String nearestPlaceId;
    String vicinity;
    HashMap<String, HashMap> extraMarkerInfo = new HashMap<String, HashMap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);
    }

    private void getNearestPlace(Location location) {

       // placeArrayList = new ArrayList<>();
        placesAPI_KEY = getString(R.string.placesAPI_KEY);
        urlToGetNearestPlace = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + location.getLatitude() + "," + location.getLongitude() + "&radius=2000&types=cafe|restaurant&key=" + placesAPI_KEY;
        JsonObjectRequest requestToGetPlace = new JsonObjectRequest(Request.Method.GET, urlToGetNearestPlace, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray result = response.getJSONArray("results");
                    for (int i = 0; i < result.length(); i++) {
                        String latitude = result.getJSONObject(i).getJSONObject("geometry")
                                .getJSONObject("location").getString("lat");
                        lat = Double.parseDouble(latitude);
                        String longitude = result.getJSONObject(i).getJSONObject("geometry")
                                .getJSONObject("location").getString("lng");
                        lng = Double.parseDouble(longitude);
                        name = result.getJSONObject(i).getString("name");
                        nearestPlaceId = result.getJSONObject(i).getString("place_id");
                        vicinity = result.getJSONObject(i).getString("vicinity");

                        place = new Place(name,nearestPlaceId,vicinity,lat,lng);
                        getPlaceDetails(place);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        AppController.getInstance().addToRequestQueue(requestToGetPlace);
    }

    private void getPlaceDetails(final Place place) {
        nearestPlaceId=place.getNearestPlaceId();
        placesAPI_KEY = getString(R.string.placesAPI_KEY);
        String urlToGetDetails = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + nearestPlaceId + "&key=" + placesAPI_KEY;
        JsonObjectRequest requestToGetDetails = new JsonObjectRequest(Request.Method.GET, urlToGetDetails, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject result = response.getJSONObject("result");
                    String contact = result.getString("formatted_phone_number");
                    String rating = result.getString("rating");
                    place.setContact(contact);
                    place.setRating(rating);
                    //Toast.makeText(getApplicationContext(),place.getName() +" "+place.getContact(),Toast.LENGTH_LONG).show();
                    handleNewLocation(place);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        AppController.getInstance().addToRequestQueue(requestToGetDetails);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {

            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getApplicationContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setGravity(Gravity.CENTER);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }

    private void handleNewLocation( final Place place) {

        double lat = place.getLatitude();
        double lon = place.getLongitude();
        LatLng placeLocation = new LatLng(lat, lon);

        MarkerOptions options = new MarkerOptions()
                .position(placeLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(place.getName()).snippet(place.getVicinity() + "\n" + place.getContact() + "\n" + place.getRating() + "*");
        Marker marker = mMap.addMarker(options);
        marker.showInfoWindow();
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("contact", place.getContact());
        extraMarkerInfo.put(marker.getId(), data);
        mMap.setOnInfoWindowClickListener(this);
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            getNearestPlace(location);
            mMap.clear();
            setMyLocationMarker(location);

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("location failed", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mMap.clear();
        getNearestPlace(location);
        setMyLocationMarker(location);
    }

    private void setMyLocationMarker(Location location) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                location.getLongitude())).title("I am here"));
        LatLng myLocationLatLng=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocationLatLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocationLatLng, 15.0f));
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        HashMap<String, String> marker_data = extraMarkerInfo.get(marker.getId());
        String phoneNumber=marker_data.get("contact");
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
        //Toast.makeText(getApplicationContext(),phoneNumber,Toast.LENGTH_LONG).show();

    }
}
