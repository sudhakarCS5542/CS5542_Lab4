package com.example.sudhakareddy.imagesearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;


import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {



    private final String SEARCH_POINT = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=" + PAGE_SIZES;
    public final int MAX_RESULTS = 40;
    private final int MAX_CONCURRENT_REQUEST = 4;
    private final int PAGE_SIZES = 8;
    private ImageAdapter resultsIA;
    private GridView resultsGrid;
    public LruCache<String, Bitmap> imageCache;

    protected void asyncJson(String url, final int start, final ImageAdapter searchResultsAdapter){

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        imageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        final AQuery aq = new AQuery(findViewById(android.R.id.content));
        aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {

                if(!isAttached(searchResultsAdapter)) return;

                if(json != null){
                    try{
                        JSONObject responseData = json.getJSONObject("responseData");
                        JSONArray results = responseData.getJSONArray("results");
                        for(int i=0; i<results.length(); i++){
                            final ImageResult imageResult = new ImageResult();
                            imageResult.imageUrl = results.getJSONObject(i).getString("tbUrl");
                            imageResult.result = start + i;

                            if(imageResult.result >= MAX_RESULTS){
                                continue;
                            }

                            //Load the image for this result in background before adding to adaptor
                            aq.ajax(imageResult.imageUrl, Bitmap.class, new AjaxCallback<Bitmap>() {
                                @Override
                                public void callback(String url, Bitmap bitmap, AjaxStatus status) {

                                    if(!isAttached(searchResultsAdapter)) return;

                                    imageCache.put(url, bitmap);
                                    resultsIA.addResult(imageResult);
                                    resultsGrid.invalidateViews();
                                }
                            });
                        }
                    }catch (JSONException e){
                        //TODO: handle errors
                    }
                }else{
                    //TODO: handle errors
                }
            }
        });
    }

    protected boolean isAttached(ImageAdapter adapter){
        return resultsGrid.getAdapter()==adapter;
    }

    protected void runSearch(String query){

        //Perform the search
        for(int i=0;i<MAX_RESULTS;i+=PAGE_SIZES){
            try {
                asyncJson(SEARCH_POINT + "&q=" + URLEncoder.encode(query, "UTF-8") + "&start=" + i, i, resultsImageAdapter);
            } catch (UnsupportedEncodingException e){
            }
        }
    }

    public void searchButtonClick(View view){
        final EditText searchInput = (EditText) findViewById(R.id.search_input);

        //Trash the last query results if there was one
        resultsIA = new ImageAdapter(this);
        resultsGrid.setAdapter(resultsIA);
        resultsGrid.invalidateViews();

        new SearchTask(this).execute(searchInput.getText().toString());

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AjaxCallback.setNetworkLimit(MAX_CONCURRENT_REQUEST);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultsGrid = (GridView) findViewById(R.id.gridview);
    }
}