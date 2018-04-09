package com.example.randy_lin.weathergo;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Weather {
    private final String GET_CWB_OPENDATA_REST_URL = "http://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-093?";
    private final String AUTHORIZATION_KEY = "Authorization";
    private final String AUTHORIZATION_VALUE = "CWB-5E972971-9EE4-49BC-8FAE-D9B516D0B672";
    private final String ELEMENTNAME = "AT,Wx,PoP6h,Wind,RH,T";
    private boolean chunk = true;

    private String[] Wx;
    private String[] weatherCode;
    private String[] AT;
    private String[] T;
    private String[] RH;
    private String[] PoP6h;
    private String[] Wind;
    private String[] WindInfo;
    private String[] time;
    private int size;

    public boolean getWeather(String geoloction) throws JSONException {
        if (geoloction == null) return false;
        JSONObject jObj = new JSONObject(getResult(geoloction));
        JSONArray[] jAry = new JSONArray[6];
        for (int i = 0; i < jAry.length; i++) {
            jAry[i] = jObj.getJSONObject("records")
                    .getJSONArray("locations")
                    .getJSONObject(0)
                    .getJSONArray("location")
                    .getJSONObject(0)
                    .getJSONArray("weatherElement")
                    .getJSONObject(i)
                    .getJSONArray("time");
        }
        size = jAry[0].length();
        int halfSize = size >> 1;
        Wx = new String[size];
        weatherCode = new String[size];
        AT = new String[size];
        T = new String[size];
        RH = new String[size];
        PoP6h = new String[halfSize];
        Wind = new String[size];
        WindInfo = new String[size];
        time = new String[size];
        for (int i = 0, halfi = 0; i < size; i++, halfi = i >> 1) {
            Wx[i] = jAry[0].getJSONObject(i).getString("elementValue");
            weatherCode[i] = jAry[0].getJSONObject(i)
                    .getJSONArray("parameter")
                    .getJSONObject(0)
                    .getString("parameterValue");
            AT[i] = jAry[1].getJSONObject(i).getString("elementValue");
            T[i] = jAry[2].getJSONObject(i).getString("elementValue");
            RH[i] = jAry[3].getJSONObject(i).getString("elementValue");
            PoP6h[halfi] = jAry[4].getJSONObject(halfi).getString("elementValue");
            Wind[i] = jAry[5].getJSONObject(i)
                    .getJSONArray("parameter")
                    .getJSONObject(2)
                    .getString("parameterValue");
            WindInfo[i] = jAry[5].getJSONObject(i)
                    .getJSONArray("parameter")
                    .getJSONObject(1)
                    .getString("parameterValue");
            time[i] = (String) jAry[1].getJSONObject(i).get("dataTime");
            if (halfi > halfSize - 3) PoP6h[halfi] = PoP6h[halfSize - 3];
        }
        return true;
    }

    private String getResult(String geoloction) {
        final String[] result = {null};
        String[] Location = geoloction.split(",");
        final HttpClient httpClient = new DefaultHttpClient();
        final HttpGet httpRequest = new HttpGet(GET_CWB_OPENDATA_REST_URL
                + "&locationId=F-D0047-0" + getlocaiotnID(Location[0])
                + "&locationName=" + Location[1]
                + "&elementName=" + ELEMENTNAME + "&sort=time");
        httpRequest.addHeader(AUTHORIZATION_KEY, AUTHORIZATION_VALUE);

        new Thread(new Runnable() {
            public void run() {
                do {
                    try {
                        result[0] = EntityUtils.toString(httpClient.execute(httpRequest).getEntity());
                        chunk = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } while (chunk);
            }
        }).start();
        while (result[0] == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result[0];
    }

    private String getlocaiotnID(String location) {
        switch (location) {
            case "宜蘭縣":
                return "01";
            case "桃園市":
                return "05";
            case "新竹縣":
                return "09";
            case "苗栗縣":
                return "13";
            case "彰化縣":
                return "17";
            case "南投縣":
                return "21";
            case "雲林縣":
                return "25";
            case "嘉義縣":
                return "29";
            case "屏東縣":
                return "33";
            case "台東縣":
                return "37";
            case "花蓮縣":
                return "41";
            case "澎湖縣":
                return "45";
            case "基隆市":
                return "49";
            case "新竹市":
                return "53";
            case "嘉義市":
                return "57";
            case "台北市":
                return "61";
            case "高雄市":
                return "65";
            case "新北市":
                return "69";
            case "台中市":
                return "73";
            case "台南市":
                return "77";
            case "連江縣":
                return "81";
            case "金門縣":
                return "85";
        }
        return null;
    }

    public boolean isEmpty() {
        return Wx == null || weatherCode == null || AT == null ||
                T == null || RH == null || PoP6h == null ||
                Wind == null || WindInfo == null || time == null;
    }

    public String[] getWx() {
        return Wx;
    }

    public String[] getWeatherCode() {
        return weatherCode;
    }

    public String[] getAT() {
        return AT;
    }

    public String[] getT() {
        return T;
    }

    public String[] getRH() {
        return RH;
    }

    public String[] getPoP6h() {
        return PoP6h;
    }

    public String[] getWind() {
        return Wind;
    }

    public String[] getWindInfo() {
        return WindInfo;
    }

    public String[] getTime() {
        return time;
    }

    public int size() {
        return size;
    }

}
