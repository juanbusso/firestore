package com.google.firebase.example.fireeats;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
//import android.view.Menu;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.fireeats.adapter.ProductAdapter;
import com.google.firebase.example.fireeats.model.Rating;
import com.google.firebase.example.fireeats.model.Product;
import com.google.firebase.example.fireeats.util.RatingUtil;
import com.google.firebase.example.fireeats.util.ProductUtil;
import com.google.firebase.example.fireeats.viewmodel.MainActivityViewModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;


public class MainActivity extends AppCompatActivity implements
        FilterDialogFragment.FilterListener,
        ProductAdapter.OnProductSelectedListener {

    private static final String TAG = "MainActivity";

    private static final int RC_SIGN_IN = 9001;

    private static final int LIMIT = 50;


    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.text_current_search)
    TextView mCurrentSearchView;

    @BindView(R.id.text_current_sort_by)
    TextView mCurrentSortByView;

    @BindView(R.id.recycler_products)
    RecyclerView mProductsRecycler;

    @BindView(R.id.view_empty)
    ViewGroup mEmptyView;

    @BindView(R.id.filter_bar_container)
    ViewGroup mfilter_bar_container;


   // Resources res = getResources();
   // Drawable icon_store = res.getDrawable(R.drawable.ic_audiotrack_white_24dp);

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private FilterDialogFragment mFilterDialog;
    private ProductAdapter mAdapter;

    private MainActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get ${LIMIT} products
        mQuery = mFirestore.collection("products")
                .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new ProductAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mProductsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mProductsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mProductsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mProductsRecycler.setAdapter(mAdapter);

        // Filter Dialog
        mFilterDialog = new FilterDialogFragment();



        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            final View iconView = menuView.getChildAt(i).findViewById(android.support.design.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, displayMetrics);
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, displayMetrics);
            iconView.setLayoutParams(layoutParams);
        }

        final Menu menu = bottomNavigationView.getMenu();

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.action_favorites:
                                mProductsRecycler.setVisibility(View.VISIBLE);
                                mEmptyView.setVisibility(View.GONE);
                                mfilter_bar_container.setVisibility(View.VISIBLE);
                                item.setIcon(R.drawable.ic_audiotrack_white_24dp);
                                menu.findItem(R.id.action_schedules).setIcon(R.drawable.ic_heart_empty);
                                break;
                            case R.id.action_schedules:
                                mProductsRecycler.setVisibility(View.GONE);
                                mEmptyView.setVisibility(View.VISIBLE);
                                mfilter_bar_container.setVisibility(View.VISIBLE);
                                item.setIcon(R.drawable.ic_heart_full);
                                menu.findItem(R.id.action_favorites).setIcon(R.drawable.ic_heart_empty);
                                break;
                            case R.id.action_music:
                                mProductsRecycler.setVisibility(View.VISIBLE);
                                mEmptyView.setVisibility(View.GONE);
                                mfilter_bar_container.setVisibility(View.GONE);
                                break;
                        }
                        return false;
                    }
                });

    }

    @OnClick(R.id.CartButton)
    public void onCartButtonClicked() {
       //  AuthUI.getInstance().signOut(this);
       // startSignIn();

        Context context = getApplicationContext();
        CharSequence text = "Helokk !";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();

        Intent intent2 = new Intent(this, CartDetailActivity.class);

        MainActivity.this.startActivity(intent2);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }


    @Override
    public void onStart() {
        super.onStart();

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }

        // Apply filters
        onFilter(mViewModel.getFilters());

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            mViewModel.setIsSigningIn(false);

            if (resultCode != RESULT_OK) {
                if (response == null) {
                    // User pressed the back button.
                    finish();
                } else if (response.getError() != null
                        && response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSignInErrorDialog(R.string.message_no_network);
                } else {
                    showSignInErrorDialog(R.string.message_unknown);
                }
            }
        }
    }

    @OnClick(R.id.filter_bar)
    public void onFilterClicked() {
        // Show the dialog containing filter options
        mFilterDialog.show(getSupportFragmentManager(), FilterDialogFragment.TAG);
    }

    @OnClick(R.id.button_clear_filter)
    public void onClearFilterClicked() {
        mFilterDialog.resetFilters();

        onFilter(Filters.getDefault());
    }

    @Override
    public void onButton(DocumentSnapshot product) {

        DocumentReference resto = mFirestore.collection("products").document(product.getId());
        resto.update("numRatings", product.get("numRatings").hashCode()+1);
        resto.update("User",FirebaseAuth.getInstance().getCurrentUser().getUid());
        Context context = getApplicationContext();
        CharSequence text = product.getId();
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();

    }

    @Override
    public void onProductSelected(DocumentSnapshot product) {
        // Go to the details page for the selected product
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.KEY_RESTAURANT_ID, product.getId());

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    @Override
    public void onFilter(Filters filters) {
        // Construct query basic query
        Query query = mFirestore.collection("products");

        // Category (equality filter)
        if (filters.hasCategory()) {
            query = query.whereEqualTo(Product.FIELD_CATEGORY, filters.getCategory());
        }

        // City (equality filter)
        if (filters.hasCity()) {
            query = query.whereEqualTo(Product.FIELD_CITY, filters.getCity());
        }

        // Price (equality filter)
        if (filters.hasPrice()) {
            query = query.whereEqualTo(Product.FIELD_PRICE, filters.getPrice());
        }

        // Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
        }

        // Limit items
        query = query.limit(LIMIT);

        // Update the query
        mAdapter.setQuery(query);

        // Set header
        mCurrentSearchView.setText(Html.fromHtml(filters.getSearchDescription(this)));
        mCurrentSortByView.setText(filters.getOrderDescription(this));

        // Save filters
        mViewModel.setFilters(filters);
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    private void startSignIn() {
        // Sign in with FirebaseUI
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .setIsSmartLockEnabled(false)
                .build();

        startActivityForResult(intent, RC_SIGN_IN);
        mViewModel.setIsSigningIn(true);
    }

    private void onAddItemsClicked() {
        // Add a bunch of random products
        WriteBatch batch = mFirestore.batch();
        for (int i = 0; i < 10; i++) {
            DocumentReference restRef = mFirestore.collection("products").document();

            // Create random product / ratings
            Product randomProduct = ProductUtil.getRandom(this);
            List<Rating> randomRatings = RatingUtil.getRandomList(randomProduct.getNumRatings());
            randomProduct.setAvgRating(RatingUtil.getAverageRating(randomRatings));

            // Add product
            batch.set(restRef, randomProduct);

            // Add ratings to subcollection
            for (Rating rating : randomRatings) {
                batch.set(restRef.collection("ratings").document(), rating);
            }
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Write batch succeeded.");
                } else {
                    Log.w(TAG, "write batch failed.", task.getException());
                }
            }
        });
    }

    private void showSignInErrorDialog(@StringRes int message) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_sign_in_error)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.option_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                      startSignIn();
                    }
                })
                .setNegativeButton(R.string.option_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).create();

        dialog.show();
    }
}
