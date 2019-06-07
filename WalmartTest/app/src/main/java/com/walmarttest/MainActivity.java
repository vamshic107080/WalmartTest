package com.walmarttest;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.walmarttest.models.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public int pageNumber = 1;
    public static final int productsPerPage = 10;
    public static final int STATUS_CODE_SUCCESS = 200;
    public int totalProducts;

    Snackbar notificationBar;
    RelativeLayout parentLayout;
    RecyclerView productsList;
    ProgressBar loadingProgress;
    Toolbar activityToolbar;
    TextView toolbarTitle;

    ProductsListAdapter productsListAdapter;
    List<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        setUpToolBar();
        productsList = (RecyclerView) findViewById(R.id.products_list);
        parentLayout = (RelativeLayout) findViewById(R.id.parent_layout);
        loadingProgress = (ProgressBar) findViewById(R.id.waiting_progress);
        products = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        productsList.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(productsList.getContext(),
                linearLayoutManager.getOrientation());
        productsList.addItemDecoration(dividerItemDecoration);
        productsListAdapter = new ProductsListAdapter(productsList, products, this, this::loadMoreProducts, this::doShowProductDetails);
        productsList.setAdapter(productsListAdapter);

        new GetProducts(pageNumber, productsPerPage).execute();
    }

    private void setUpToolBar() {
        activityToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(activityToolbar);
        getSupportActionBar().setTitle("");
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getString(R.string.products_list));
    }

    @Override
    public void onBackPressed() {
        if (loadingProgress.getVisibility() == View.VISIBLE)
            loadingProgress.setVisibility(View.GONE);
        else
            super.onBackPressed();
    }


    public void loadMoreProducts() {
        if (products.size() < totalProducts) {
            products.add(null);
            productsListAdapter.notifyItemInserted(products.size() - 1);
            new GetProducts(pageNumber, productsPerPage).execute();
        }
    }

    public void doShowProductDetails(int position) {
        Product selectedProduct = products.get(position);
        String productJson = new Gson().toJson(selectedProduct);
        Intent productDetailsIntent = new Intent(this, ProductDetailsActivity.class);
        productDetailsIntent.putExtra(getString(R.string.tag_product_json), productJson);
        startActivity(productDetailsIntent);
    }


    public class GetProducts extends AsyncTask<Void, String, String> {

        int pageNumber;
        int pageSize;

        protected GetProducts(int pageNumber, int pageSize) {
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(String.format("https://mobile-tha-server.firebaseapp.com/walmartproducts/%d/%d", this.pageNumber, this.pageSize))
                        .get()
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                return new String(response.body().bytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (loadingProgress.getVisibility() == View.VISIBLE)
                loadingProgress.setVisibility(View.GONE);
            if (response != null) {
                try {
                    JSONObject resJsonObj = new JSONObject(response);
                    int statusCode = resJsonObj.getInt(getString(R.string.tag_statuscode));
                    if (statusCode == STATUS_CODE_SUCCESS) {
                        doProcessSuccessResponse(resJsonObj);
                    } else {
                        doProcessFaiureResponse();
                    }
                } catch (JSONException jsonExp) {
                    jsonExp.printStackTrace();
                    doProcessFaiureResponse();
                }
            } else doProcessFaiureResponse();
        }
    }

    private void doProcessFaiureResponse() {
        notifyUser(getString(R.string.msg_fail));
        if (products.size() > 0 && products.get(products.size() - 1) == null) {
            products.remove(products.size() - 1);
            productsListAdapter.notifyItemRemoved(products.size());
        }
        productsListAdapter.notifyDataSetChanged();
        productsListAdapter.setLoaded();
    }

    private void notifyUser(String message) {
        if (notificationBar != null)
            notificationBar.dismiss();
        notificationBar = Snackbar
                .make(parentLayout, message, Snackbar.LENGTH_LONG);
        notificationBar.show();
    }

    private void doProcessSuccessResponse(JSONObject resJsonObj) throws JSONException {
        if (products.size() > 0 && products.get(products.size() - 1) == null) {
            products.remove(products.size() - 1);
            productsListAdapter.notifyItemRemoved(products.size());
        }
        ArrayList<Product> temp = new ArrayList<>();
        pageNumber += 1;
        if (totalProducts == 0)
            totalProducts = resJsonObj.getInt(getString(R.string.tag_totalproducts));
        JSONArray productsJsonArray = resJsonObj.getJSONArray(getString(R.string.tag_products));
        Gson gson = new Gson();
        for (int index = 0; index < productsJsonArray.length(); index++)
            temp.add(gson.fromJson(productsJsonArray.getJSONObject(index).toString(), Product.class));

        products.addAll(temp);
        temp.clear();
        productsListAdapter.notifyDataSetChanged();
        productsListAdapter.setLoaded();
    }
}
