package com.walmarttest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.walmarttest.models.Product;

public class ProductDetailsActivity extends AppCompatActivity {


    ImageView productImageView;
    TextView productNameTxtView, productDescTxtView, productPriceTxtView, productReviewCountTxtView, toolbarTitle;
    RatingBar productRatingsBar;
    Product product;
    Toolbar activityToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        String productJson = getIntent().getExtras().getString(getString(R.string.tag_product_json), "");
        if (productJson.isEmpty())
            finish();
        product = new Gson().fromJson(productJson, Product.class);
        init();
    }

    private void init() {
        setUpToolBar();
        productImageView = (ImageView) findViewById(R.id.product_image);
        productNameTxtView = (TextView) findViewById(R.id.product_name);
        productDescTxtView = (TextView) findViewById(R.id.product_desc);
        productPriceTxtView = (TextView) findViewById(R.id.product_price);
        productRatingsBar = (RatingBar) findViewById(R.id.product_rating);
        productReviewCountTxtView = (TextView) findViewById(R.id.product_review_count);
        productRatingsBar.setNumStars(5);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(String.format("https://mobile-tha-server.firebaseapp.com/%s", product.getProductImage()), productImageView);

        productNameTxtView.setText(product.getProductName());

        if (product.getShortDescription() != null && !product.getShortDescription().isEmpty())
            productDescTxtView.setText(Html.fromHtml(product.getShortDescription()));
        if (product.getLongDescription() != null && !product.getLongDescription().isEmpty())
            productDescTxtView.append(Html.fromHtml(product.getLongDescription()));

        productPriceTxtView.setText(String.format("Price : %s", product.getPrice()));
        productRatingsBar.setRating(Float.parseFloat(product.getReviewRating()));
        if (product.getReviewCount() != null)
            productReviewCountTxtView.setText(String.format("%s", product.getReviewCount()));
    }

    private void setUpToolBar() {
        activityToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(activityToolbar);
        getSupportActionBar().setTitle("");
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getString(R.string.product_details));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
