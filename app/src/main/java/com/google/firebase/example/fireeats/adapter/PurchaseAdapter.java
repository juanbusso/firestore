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
import com.google.firebase.example.fireeats.model.Purchase;
import com.google.firebase.example.fireeats.util.PurchaseUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

/**
 * RecyclerView adapter for a list of Purchases.
 */
public class PurchaseAdapter extends FirestoreAdapter<PurchaseAdapter.ViewHolder> {

    public interface OnPurchaseSelectedListener {

        void onPurchaseSelected(DocumentSnapshot purchase);
    }

    private OnPurchaseSelectedListener mListener;

    public PurchaseAdapter(Query query, OnPurchaseSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_purchase, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.purchase_item_image)
        ImageView imageView;

        @BindView(R.id.purchase_item_name)
        TextView nameView;

        @BindView(R.id.purchase_item_rating)
        MaterialRatingBar ratingBar;

        @BindView(R.id.purchase_item_num_ratings)
        TextView numRatingsView;

        @BindView(R.id.purchase_item_price)
        TextView priceView;

        @BindView(R.id.purchase_item_category)
        TextView categoryView;

        @BindView(R.id.purchase_item_city)
        TextView cityView;

        @BindView(R.id.itemAddButton)
        FloatingActionButton Button;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnPurchaseSelectedListener listener) {

            final Purchase purchase = snapshot.toObject(Purchase.class);
            Resources resources = itemView.getResources();

            // Load image
            Glide.with(imageView.getContext())
                    .load(purchase.getPhoto())
                    .into(imageView);

            nameView.setText(purchase.getName());
            ratingBar.setRating((float) purchase.getAvgRating());
            cityView.setText(purchase.getCity());
            categoryView.setText(purchase.getCategory());
            numRatingsView.setText(resources.getString(R.string.fmt_num_ratings,
                    purchase.getNumRatings()));
            priceView.setText(PurchaseUtil.getPriceString(purchase));

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onPurchaseSelected(snapshot);
                    }
                }
            });
        }

    }
}
