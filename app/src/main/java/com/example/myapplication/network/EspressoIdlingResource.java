package com.example.myapplication.network;

import androidx.test.espresso.idling.CountingIdlingResource;

/**
 * A simple CountingIdlingResource for Espresso synchronization.
 * Increment when starting an async task (like showing a loading dialog).
 * Decrement when finished.
 */
public class EspressoIdlingResource {
    private static final String RESOURCE = "GLOBAL";
    private static final CountingIdlingResource countingIdlingResource = new CountingIdlingResource(RESOURCE);

    public static void increment() {
        countingIdlingResource.increment();
    }

    public static void decrement() {
        if (!countingIdlingResource.isIdleNow()) {
            countingIdlingResource.decrement();
        }
    }

    public static CountingIdlingResource getIdlingResource() {
        return countingIdlingResource;
    }
}
