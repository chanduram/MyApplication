package org.pennywise.android.myapplication.servicelayer;

import com.google.gson.JsonElement;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by chandart on 9/24/2014.
 */
public interface ServiceClient {

    String SERVICE_URL = "http://stag.snapshop.info";
    @GET("/ws-snapshop.php?format=json&method=pwg.snapshop.venue")
    void getIndustries(Callback<JsonElement> industriesCallback);


}
