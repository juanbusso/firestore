package com.google.firebase.example.fireeats.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.model.Product;
import com.google.firebase.example.fireeats.util.ProductUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

/**
 * RecyclerView adapter for a list of Products.
 */
public class ProductAdapter extends FirestoreAdapter<ProductAdapter.ViewHolder> {

    public interface OnProductSelectedListener {

        void onProductSelected(DocumentSnapshot product);
        void onButton(DocumentSnapshot product);
    }

    private OnProductSelectedListener mListener;

    public ProductAdapter(Query query, OnProductSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_product, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.product_item_image)
        ImageView imageView;

        @BindView(R.id.product_item_name)
        TextView nameView;

        @BindView(R.id.product_item_rating)
        MaterialRatingBar ratingBar;

        @BindView(R.id.product_item_num_ratings)
        TextView numRatingsView;

        @BindView(R.id.product_item_price)
        TextView priceView;

        @BindView(R.id.product_item_category)
        TextView categoryView;

        @BindView(R.id.product_item_city)
        TextView cityView;

        @BindView(R.id.itemAddButton)
        FloatingActionButton Button;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnProductSelectedListener listener) {

            final Product product = snapshot.toObject(Product.class);
            Resources resources = itemView.getResources();

            // Load image
            Glide.with(imageView.getContext())
                    .load(product.getPhoto())
                    .into(imageView);

            nameView.setText(product.getName());
            ratingBar.setRating((float) product.getAvgRating());
            cityView.setText(product.getCity());
            categoryView.setText(product.getCategory());
            numRatingsView.setText(resources.getString(R.string.fmt_num_ratings,
                    product.getNumRatings()));
            priceView.setText(ProductUtil.getPriceString(product));

            Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                        if (listener != null) {
                            listener.onButton(snapshot);
                        }
                }
            });

                // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onProductSelected(snapshot);
                    }
                }
            });
        }

    }
}
