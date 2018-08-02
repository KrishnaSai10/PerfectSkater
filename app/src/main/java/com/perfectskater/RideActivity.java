package com.perfectskater;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RideActivity extends AppCompatActivity {


    ProgressBar progressBar;
    View rootView;
    TextView placeName, distanceEd, price;
    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        getSupportActionBar().setTitle("Ride Details");
        progressBar = findViewById(R.id.progressBar);


        placeName = findViewById(R.id.placeName);
        distanceEd = findViewById(R.id.distance);
        price = findViewById(R.id.price);
        rootView = findViewById(R.id.root);
        Button map = findViewById(R.id.map);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RideActivity.this, MapsActivity.class));
            }
        });

        preferences = getSharedPreferences(ProfileActivity.PROFILE_PREFF, MODE_PRIVATE);


        String location = preferences.getString("location", "scarborough");

        placesRequest(location);

    }

    public void placesRequest(String location) {


        progressBar.setVisibility(View.VISIBLE);
        rootView.setVisibility(View.GONE);

        final String loc_places_url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + location + "&key=AIzaSyCczlB0pFZ1tEnWotdfmH0qokALcooSgO0";
        Log.d("Request Url", loc_places_url);


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                loc_places_url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("response", response.toString());


                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject obj = results.getJSONObject(i);
                                String formatted_address = obj.getString("formatted_address");

                                placeName.setText(formatted_address);
                                String place = formatted_address.split(",")[0];
                                String lat = obj.getJSONObject("geometry").getJSONObject("location").getString("lat");
                                String lng = obj.getJSONObject("geometry").getJSONObject("location").getString("lng");

//                                distanceRequest(place);
                                distanceRequest(lat.concat(",").concat(lng));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("error", "Error: " + error.getMessage());

                Log.d("error", error.toString());
            }
        }) {


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("charset", "utf-8");

                return headers;
            }

        };

        // Adding request to request queue
        App.getInstance().addToRequestQueue(jsonObjReq);

        // Cancelling request
        // ApplicationController.getInstance().getRequestQueue().cancelAll(tag_json_obj);
    }


    public void distanceRequest(String destination) {

        progressBar.setVisibility(View.VISIBLE);
        rootView.setVisibility(View.GONE);
        String lat = preferences.getString("lat", "");
        String longi = preferences.getString("longi", "");


        final String loc_places_url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + lat + "," + longi + "&destination=" + destination + "&key=AIzaSyCczlB0pFZ1tEnWotdfmH0qokALcooSgO0\n";
//        final String loc_places_url = "https://maps.googleapis.com/maps/api/directions/json?origin=m1s4c3&destination="+destination+"&key=AIzaSyCczlB0pFZ1tEnWotdfmH0qokALcooSgO0\n";

        Log.d("loc_places_url", loc_places_url);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                loc_places_url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("response", response.toString());

                        progressBar.setVisibility(View.GONE);
                        rootView.setVisibility(View.VISIBLE);
                        try {
                            JSONArray results = response.getJSONArray("routes");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject obj = results.getJSONObject(i);
                                JSONArray legs = obj.getJSONArray("legs");
                                for (int j = 0; j < legs.length(); j++) {
                                    JSONObject legsObj = legs.getJSONObject(i);
                                    JSONObject distanceObj = legsObj.getJSONObject("distance");
                                    int meters = distanceObj.getInt("value");
                                    int kms = meters / 1000;
                                    distanceEd.setText(kms + " Km");
                                    price.setText("$" + kms);
                                    JSONObject end_locationObj = legsObj.getJSONObject("end_location");
                                    String end_location_lat = String.valueOf(end_locationObj.getDouble("lat"));
                                    String end_location_long = String.valueOf(end_locationObj.getDouble("lng"));
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("end_location_lat", end_location_lat);
                                    editor.putString("end_location_long", end_location_long);
                                    editor.commit();
                                }

                                JSONObject overview_polyline = obj.getJSONObject("overview_polyline");
                                String polyline = overview_polyline.getString("points");
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("polyline", polyline);
                                editor.commit();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("error", "Error: " + error.getMessage());

                Log.d("error", error.toString());
            }
        }) {


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("charset", "utf-8");

                return headers;
            }

        };

        // Adding request to request queue
        App.getInstance().addToRequestQueue(jsonObjReq);

        // Cancelling request
        // ApplicationController.getInstance().getRequestQueue().cancelAll(tag_json_obj);
    }
}
