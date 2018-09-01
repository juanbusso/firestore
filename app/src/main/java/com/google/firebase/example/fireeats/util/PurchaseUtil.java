package com.google.firebase.example.fireeats.util;

import android.content.Context;

import com.google.firebase.example.fireeats.R;
import com.google.firebase.example.fireeats.model.Purchase;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/**
 * Utilities for Purchases.
 */
public class PurchaseUtil {

    private static final String TAG = "PurchaseUtil";

    private static final String RESTAURANT_URL_FMT = "https://storage.googleapis.com/firestorequickstarts.appspot.com/food_%d.png";
    private static final int MAX_IMAGE_NUM = 22;

    private static final String[] NAME_FIRST_WORDS = {
            "Foo",
            "Bar",
            "Baz",
            "Qux",
            "Fire",
            "Sam's",
            "World Famous",
            "Google",
            "The Best",
    };

    private static final String[] NAME_SECOND_WORDS = {
            "Purchase",
            "Cafe",
            "Spot",
            "Eatin' Place",
            "Eatery",
            "Drive Thru",
            "Diner",
    };

    /**
     * Create a random Purchase POJO.
     */
    public static Purchase getRandom(Context context) {
        Purchase purchase = new Purchase();
        Random random = new Random();

        // Cities (first elemnt is 'Any')
        String[] cities = context.getResources().getStringArray(R.array.cities);
        cities = Arrays.copyOfRange(cities, 1, cities.length);

        // Categories (first element is 'Any')
        String[] categories = context.getResources().getStringArray(R.array.categories);
        categories = Arrays.copyOfRange(categories, 1, categories.length);

        int[] prices = new int[]{1, 2, 3};

        purchase.setName(getRandomName(random));
        purchase.setCity(getRandomString(cities, random));
        purchase.setCategory(getRandomString(categories, random));
        purchase.setPhoto(getRandomImageUrl(random));
        purchase.setPrice(getRandomInt(prices, random));
        purchase.setNumRatings(random.nextInt(20));

        // Note: average rating intentionally not set

        return purchase;
    }


    /**
     * Get a random image.
     */
    private static String getRandomImageUrl(Random random) {
        // Integer between 1 and MAX_IMAGE_NUM (inclusive)
        int id = random.nextInt(MAX_IMAGE_NUM) + 1;

        return String.format(Locale.getDefault(), RESTAURANT_URL_FMT, id);
    }

    /**
     * Get price represented as dollar signs.
     */
    public static String getPriceString(Purchase purchase) {
        return getPriceString(purchase.getPrice());
    }

    /**
     * Get price represented as dollar signs.
     */
    public static String getPriceString(int priceInt) {
        switch (priceInt) {
            case 1:
                return "$";
            case 2:
                return "$$";
            case 3:
            default:
                return "$$$";
        }
    }

    private static String getRandomName(Random random) {
        return getRandomString(NAME_FIRST_WORDS, random) + " "
                + getRandomString(NAME_SECOND_WORDS, random);
    }

    private static String getRandomString(String[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

    private static int getRandomInt(int[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

}
