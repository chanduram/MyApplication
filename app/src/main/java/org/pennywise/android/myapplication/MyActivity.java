package org.pennywise.android.myapplication;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ListView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.pennywise.android.myapplication.servicelayer.ServiceClient;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Response;


public class MyActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        Log.d("MyAct", "oncreate");

        ServiceClient serviceClient = getServiceClient();
        serviceClient.getIndustries(callback);

    }

    Callback callback = new Callback<JsonElement>() {
        @Override
        public void success(JsonElement o, Response response) {


            JsonObject jsonObject = o.getAsJsonObject();
            JsonListAdapter adapter=new JsonListAdapter(MyActivity.this,"venues",jsonObject,R.layout.feed_item_my_list,new String[]{"id","name"},new int[]{R.id.textView,R.id.textView2});
            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        }

        @Override
        public void failure(RetrofitError error) {

            Log.d("MyAct", "call back failed" + error.getMessage());

        }
    };



    private void traceD(String s) {
        Log.d("MyActivity : ",s);
    }


    public static ServiceClient getServiceClient() {

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ServiceClient.SERVICE_URL).setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog("MyActivity"))
                .build();
        return restAdapter.create(ServiceClient.class);
    }
}
