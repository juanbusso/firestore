package com.google.firebase.example.fireeats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.app.ActionBar;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.adapter.PurchaseAdapter;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class CartDetailActivity extends AppCompatActivity
        implements PurchaseAdapter.OnPurchaseSelectedListener  {

    private PurchaseAdapter mAdapter;
    private FirebaseFirestore mFirestore;
    private Query mQuery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_cart);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();


        DocumentReference mProductRef = mFirestore.collection("cart").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Get ${LIMIT} products
        mQuery = mProductRef.collection("purchases")
                .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mAdapter = new PurchaseAdapter(mQuery, this) {

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        RecyclerView mPurchaseRecycler = findViewById(R.id.recycler_purchases);
        mPurchaseRecycler.setLayoutManager(new LinearLayoutManager(this));
        mPurchaseRecycler.setAdapter(mAdapter);


    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    public void onStart() {
        super.onStart();


        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onPurchaseSelected(DocumentSnapshot product) {
        Context context = getApplicationContext();
        CharSequence text = "hello";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();
    }


}
