package com.example.randy_lin.weathergo;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Geolocation {
    private String result;
    private Context context;
    private boolean chunk = true;

    public Geolocation(Context c){
        this.context = c;
    }

    public String getGeolocation(LatLng latlng) throws JSONException {
        JSONObject jObj = new JSONObject(getJSON(latlng));
        chunk = true;
        result = null;
        return (jObj.getJSONArray("results")
                .getJSONObject(0)
                .getJSONArray("address_components")
                .getJSONObject(1)
                .getString("long_name") + "," +
                jObj.getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONArray("address_components")
                        .getJSONObject(0)
                        .getString("long_name"));
    }

    private String getJSON(LatLng latlng) {
        final HttpClient httpClient = new DefaultHttpClient();
        final HttpGet httpRequest = new HttpGet(
                "https://maps.google.com/maps/api/geocode/json?"
                        + "latlng=" + latlng.latitude + "," + latlng.longitude
                        + "&result_type=administrative_area_level_3"
                        + "&language=zh-TW&sensor=true"
                        + "&key=" + context.getString(R.string.google_maps_key)
        );
        Log.e("@@@@@@google api",httpRequest.getURI()+"");
        new Thread(new Runnable() {
            public void run() {
                do {
                    try {
                        HttpResponse httpResponse = httpClient.execute(httpRequest);
                        HttpEntity httpEntity = httpResponse.getEntity();
                        result = EntityUtils.toString(httpEntity);
                        chunk = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } while (chunk);
            }
        }).start();
        while (result == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
