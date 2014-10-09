package org.pennywise.android.myapplication;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
public class JsonListAdapter extends BaseAdapter implements Filterable {
    private int[] mTo;
    private String[] mFrom;
    private ViewBinder mViewBinder;

    private List<? extends Map<String, ?>> mData;

    private int mResource;
    private int mDropDownResource;
    private LayoutInflater mInflater;

    private SimpleFilter mFilter;
    private ArrayList<Map<String, ?>> mUnfilteredData;



    JsonObject mJsonObject;
    Context mContext;
    String mSearchKey;


    public JsonListAdapter(Context context,String searchKey,JsonObject jsonObject,
                         int resource, String[] from, int[] to) {
        mData = new ArrayList<Map<String, String>>();
        mContext = context.getApplicationContext();
        mResource = mDropDownResource = resource;
        mFrom = from;
        mTo = to;
        mJsonObject = jsonObject;
        mSearchKey = searchKey;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initListFromJsonObject(mSearchKey, mJsonObject);

    }


    /**
     * @see android.widget.Adapter#getCount()
     */
    public int getCount() {
        return mData.size();
    }

    /**
     * @see android.widget.Adapter#getItem(int)
     */
    public Object getItem(int position) {
        return mData.get(position);
    }

    /**
     * @see android.widget.Adapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }

    private View createViewFromResource(int position, View convertView,
                                        ViewGroup parent, int resource) {
        View v;
        if (convertView == null) {
            v = mInflater.inflate(resource, parent, false);
        } else {
            v = convertView;
        }

        bindView(position, v);

        return v;
    }

    /**
     * <p>Sets the layout resource to create the drop down views.</p>
     *
     * @param resource the layout resource defining the drop down views
     * @see #getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    public void setDropDownViewResource(int resource) {
        this.mDropDownResource = resource;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mDropDownResource);
    }

    private void bindView(int position, View view) {
        final Map dataSet = mData.get(position);
        if (dataSet == null) {
            return;
        }

        final ViewBinder binder = mViewBinder;
        final String[] from = mFrom;
        final int[] to = mTo;
        final int count = to.length;

        for (int i = 0; i < count; i++) {
            final View v = view.findViewById(to[i]);
            if (v != null) {
                final Object data = dataSet.get(from[i]);
                String text = data == null ? "" : data.toString();
                if (text == null) {
                    text = "";
                }

                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, data, text);
                }

                if (!bound) {
                    if (v instanceof Checkable) {
                        if (data instanceof Boolean) {
                            ((Checkable) v).setChecked((Boolean) data);
                        } else if (v instanceof TextView) {
                            // Note: keep the instanceof TextView check at the bottom of these
                            // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                            setViewText((TextView) v, text);
                        } else {
                            throw new IllegalStateException(v.getClass().getName() +
                                    " should be bound to a Boolean, not a " +
                                    (data == null ? "<unknown type>" : data.getClass()));
                        }
                    } else if (v instanceof TextView) {
                        // Note: keep the instanceof TextView check at the bottom of these
                        // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                        setViewText((TextView) v, text);
                    } else if (v instanceof ImageView) {
                        if (data instanceof Integer) {
                            setViewImage((ImageView) v, (Integer) data);
                        } else {
                            setViewImage((ImageView) v, text);
                        }
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                " view that can be bounds by this SimpleAdapter");
                    }
                }
            }
        }
    }

    /**
     * Returns the {@link ViewBinder} used to bind data to views.
     *
     * @return a ViewBinder or null if the binder does not exist
     *
     * @see #setViewBinder(android.widget.SimpleAdapter.ViewBinder)
     */
    public ViewBinder getViewBinder() {
        return mViewBinder;
    }

    /**
     * Sets the binder used to bind data to views.
     *
     * @param viewBinder the binder used to bind data to views, can be null to
     *        remove the existing binder
     *
     * @see #getViewBinder()
     */
    public void setViewBinder(ViewBinder viewBinder) {
        mViewBinder = viewBinder;
    }

    @Override
    public void notifyDataSetChanged() {


        super.notifyDataSetChanged();
    }

    /**
     * Called by bindView() to set the image for an ImageView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to an ImageView.
     *
     * This method is called instead of {@link #setViewImage(ImageView, String)}
     * if the supplied data is an int or Integer.
     *
     * @param v ImageView to receive an image
     * @param value the value retrieved from the data set
     *
     * @see #setViewImage(ImageView, String)
     */
    public void setViewImage(ImageView v, int value) {
        v.setImageResource(value);
    }

    /**
     * Called by bindView() to set the image for an ImageView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to an ImageView.
     *
     * By default, the value will be treated as an image resource. If the
     * value cannot be used as an image resource, the value is used as an
     * image Uri.
     *
     * This method is called instead of {@link #setViewImage(ImageView, int)}
     * if the supplied data is not an int or Integer.
     *
     * @param v ImageView to receive an image
     * @param value the value retrieved from the data set
     *
     * @see #setViewImage(ImageView, int)
     */
    public void setViewImage(ImageView v, String value) {
        try {
            v.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException nfe) {
            v.setImageURI(Uri.parse(value));
        }
    }

    /**
     * Called by bindView() to set the text for a TextView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to a TextView.
     *
     * @param v TextView to receive text
     * @param text the text to be set for the TextView
     */
    public void setViewText(TextView v, String text) {
        v.setText(text);
    }

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SimpleFilter();
        }
        return mFilter;
    }

    /**
     * This class can be used by external clients of SimpleAdapter to bind
     * values to views.
     *
     * You should use this class to bind values to views that are not
     * directly supported by SimpleAdapter or to change the way binding
     * occurs for views supported by SimpleAdapter.
     *
     * @see SimpleAdapter#setViewImage(ImageView, int)
     * @see SimpleAdapter#setViewImage(ImageView, String)
     * @see SimpleAdapter#setViewText(TextView, String)
     */
    public static interface ViewBinder {
        /**
         * Binds the specified data to the specified view.
         *
         * When binding is handled by this ViewBinder, this method must return true.
         * If this method returns false, SimpleAdapter will attempts to handle
         * the binding on its own.
         *
         * @param view the view to bind the data to
         * @param data the data to bind to the view
         * @param textRepresentation a safe String representation of the supplied data:
         *        it is either the result of data.toString() or an empty String but it
         *        is never null
         *
         * @return true if the data was bound to the view, false otherwise
         */
        boolean setViewValue(View view, Object data, String textRepresentation);
    }

    /**
     * <p>An array filters constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class SimpleFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = new ArrayList<Map<String, ?>>(mData);
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<Map<String, ?>> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<Map<String, ?>> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                ArrayList<Map<String, ?>> newValues = new ArrayList<Map<String, ?>>(count);

                for (int i = 0; i < count; i++) {
                    Map<String, ?> h = unfilteredValues.get(i);
                    if (h != null) {

                        int len = mTo.length;

                        for (int j=0; j<len; j++) {
                            String str =  (String)h.get(mFrom[j]);

                            String[] words = str.split(" ");
                            int wordCount = words.length;

                            for (int k = 0; k < wordCount; k++) {
                                String word = words[k];

                                if (word.toLowerCase().startsWith(prefixString)) {
                                    newValues.add(h);
                                    break;
                                }
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mData = (List<Map<String, ?>>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }


    private void initListFromJsonObject(final String searchKey, final JsonObject jsonObject) {

        traceD("initlist called");
        if (mContext != null) {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    try {

                        mData = convertJsonArrayToListMap(searchKey, jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {


                    super.onPostExecute(aVoid);
                }
            }.execute();


        }

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

