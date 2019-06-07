package com.walmarttest;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.walmarttest.models.Product;

import java.util.List;

public class ProductsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_PRODUCT = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private LoadMoreProductsListener loadMoreProductsListener;
    private boolean isLoading;
    private Activity activity;
    private List<Product> products;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;
    private ImageLoader imageLoader;
    private ListItemClickListener listItemClickListener;

    public ProductsListAdapter(RecyclerView recyclerView, List<Product> products, Activity activity,
                               LoadMoreProductsListener loadMoreProductsListener, ListItemClickListener listItemClickListener) {
        this.products = products;
        this.activity = activity;
        this.listItemClickListener = listItemClickListener;
        this.loadMoreProductsListener = loadMoreProductsListener;
        init(recyclerView);
    }

    private void init(RecyclerView recyclerView) {
        imageLoader = ImageLoader.getInstance(); // Get singleton instance
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (loadMoreProductsListener != null) {
                        loadMoreProductsListener.loadMoreProducts();
                    }
                    isLoading = true;
                }
            }
        });
    }


    @Override
    public int getItemViewType(int position) {
        return products.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_PRODUCT;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_PRODUCT) {
            View view = LayoutInflater.from(activity).inflate(R.layout.listitem_product, parent, false);
            return new ProductInfoViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.listitem_loading, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProductInfoViewHolder) {
            Product product = products.get(position);
            ((ProductInfoViewHolder) holder).productNameTxtView.setText(product.getProductName());
            ((ProductInfoViewHolder) holder).productPriceTxtView.setText(product.getPrice());
            ((ProductInfoViewHolder) holder).productRatingBar.setRating(Float.parseFloat(product.getReviewRating()));
            if (product.getReviewCount() != null)
                ((ProductInfoViewHolder) holder).productReviewCountTxtView.setText(String.format("%s", product.getReviewCount()));

            imageLoader.displayImage(String.format("https://mobile-tha-server.firebaseapp.com/%s", product.getProductImage()), ((ProductInfoViewHolder) holder).productImageView);
            ((ProductInfoViewHolder) holder).parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listItemClickListener != null)
                        listItemClickListener.onListItemClick(position);
                }
            });
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return products == null ? 0 : products.size();
    }

    public void setLoaded() {
        isLoading = false;
    }


    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.loading_progress);
        }
    }

    private class ProductInfoViewHolder extends RecyclerView.ViewHolder {
        public ImageView productImageView;
        public TextView productNameTxtView, productPriceTxtView, productReviewCountTxtView;
        public RatingBar productRatingBar;
        public View parentLayout;

        public ProductInfoViewHolder(View view) {
            super(view);
            parentLayout = view;
            productImageView = (ImageView) view.findViewById(R.id.product_image);
            productNameTxtView = (TextView) view.findViewById(R.id.product_name);
            productPriceTxtView = (TextView) view.findViewById(R.id.product_price);
            productReviewCountTxtView = (TextView) view.findViewById(R.id.product_review_count);
            productRatingBar = (RatingBar) view.findViewById(R.id.product_rating);
            productRatingBar.setNumStars(5);
            productRatingBar.setEnabled(false);
        }
    }


}
