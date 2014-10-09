package org.pennywise.android.myapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.pennywise.android.myapplication.model.Venue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chandart on 10/6/2014.
 */
public class JsonListAdapter1 {




    List<Map<String, String>> mdata;
    SimpleAdapter mSimpleAdapter;
    Context mContext;

    public JsonListAdapter1(Context context, String searchKey, JsonObject jsonObject, int resource, String[] from, int[] to) {

        mdata = new ArrayList<Map<String, String>>();
        mContext = context.getApplicationContext();
        mSimpleAdapter = new SimpleAdapter(mContext.getApplicationContext(), mdata, resource, from, to);
        initListFromJsonObject(searchKey, jsonObject);

    }



    private void initListFromJsonObject(final String searchKey, final JsonObject jsonObject) {

        if (mContext != null) {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    try {

                        mdata = convertJsonArrayToListMap(searchKey, jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    mSimpleAdapter.notifyDataSetChanged();

                    super.onPostExecute(aVoid);
                }
            }.execute();


        }

    }

    public SimpleAdapter getmSimpleAdapter() {
        return mSimpleAdapter;
    }

    public void notifyDataSetChanged() {

        mSimpleAdapter.notifyDataSetChanged();
    }
    private List<Map<String, String>> convertJsonArrayToListMap(String s, JsonObject jsonObject) throws JSONException {

        JsonArray jsonArrayWithSearchKey = getJsonArrayWithSearchKey("venues", jsonObject);

        List<Map<String, String>> listOfElements = new ArrayList<Map<String, String>>();
        if (jsonArrayWithSearchKey != null && jsonArrayWithSearchKey.size() > 0) {

            for (JsonElement jsonElement : jsonArrayWithSearchKey) {

                if (jsonElement != null && jsonElement.isJsonObject()) {

                    Map<String, String> tempMap = new HashMap<String, String>();
                    Set<Map.Entry<String, JsonElement>> entrySet = ((JsonObject) jsonElement).entrySet();
                    for (Iterator<Map.Entry<String, JsonElement>> itr = entrySet.iterator(); itr.hasNext(); ) {

                        Map.Entry<String, JsonElement> entry = itr.next();
                        String key = entry.getKey();
                        JsonElement tempElement = entry.getValue();
                        if (tempElement != null && !tempElement.isJsonNull() && tempElement.isJsonPrimitive()) {
                            tempMap.put(key, tempElement.getAsString());
                        }

                    }
                    listOfElements.add(tempMap);

                }
            }

        }

        return listOfElements;

    }

    public JsonArray getJsonArrayWithSearchKey(String searchKey, JsonObject inputObject) throws JSONException {

        Set<Map.Entry<String, JsonElement>> entries = inputObject.entrySet();

        for (Map.Entry<String, JsonElement> entryElement : entries) {

            String key = entryElement.getKey();
            Object object = inputObject.get(key);

            Log.d("MyActivity", "Key : " + key + " object ");
            if (key.equals(searchKey) && object instanceof JsonArray) {

                return (JsonArray) object;  // return the JsonArray with the search key item
            }
            if (object instanceof JsonArray) {

                return getJsonArrayWithSearchKey(searchKey, (JsonArray) object);

            } else if (object instanceof JsonObject) {

                traceD(" key : " + key + " JsonObject");
                return getJsonArrayWithSearchKey(searchKey, (JsonObject) object);
            }
        }


        return null;

    }

    private void traceD(String s) {
        Log.d("MyActivity : ", s);
    }

    private JsonArray getJsonArrayWithSearchKey(String searchKey, JsonArray jsonArray) throws JSONException {

        for (int i = 0; i < jsonArray.size(); i++) {

            Object o = jsonArray.get(i);

            if (o instanceof JsonObject) {

                return getJsonArrayWithSearchKey(searchKey, (JsonObject) o);

            } else if (jsonArray instanceof JsonArray) {

                return getJsonArrayWithSearchKey(searchKey, (JsonArray) jsonArray);

            }
        }

        return null;
    }


    private void parseJsonArray(JsonObject JsonObject, String key) {

        String[] keys = key.split(":;");
        JsonObject JsonObject1 = JsonObject.get(keys[0]).getAsJsonObject();
        JsonArray JsonArray = JsonObject1.get(keys[1]).getAsJsonArray();
        Iterator<JsonElement> JsonElementIterator = JsonArray.iterator();
        Gson gson = new Gson();
        Type type = new TypeToken<List<Venue>>() {
        }.getType();
        Venue venue = gson.fromJson(JsonArray.get(0), Venue.class);
        Log.d(" MyAct : ", "list of elements " + venue.getName());


    }

}
