package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.myapplication.activities.LoginActivity;
import com.example.myapplication.network.EspressoIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Senior QA implementation of Login/Register tests.
 * Uses IdlingResource for production-level synchronization instead of flaky Thread.sleep().
 */
@RunWith(AndroidJUnit4.class)
public class LoginRegisterTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void registerIdlingResource() {
        // Register IdlingResource to synchronize Espresso with background tasks (Firebase/Loading Dialog)
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource());
    }

    @After
    public void unregisterIdlingResource() {
        // Unregister to avoid memory leaks and side effects in other test classes
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource());
    }

    // =========================
    // Helpers
    // =========================

    private void performLogin(String email, String password) {
        onView(withId(R.id.etEmail)).perform(replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        // No Thread.sleep here! IdlingResource handles the wait automatically.
    }

    // =========================
    // TESTS
    // =========================

    @Test
    public void testWrongCredentials() {
        performLogin("wrong@test.com", "wrongpass");

        // Espresso waits here automatically until the Loading Dialog is dismissed.
        // Once dismissed, focus returns to LoginActivity where btnLogin exists.
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
    }

    @Test
    public void testValidLoginSuccess() {
        // Use your test account credentials
        performLogin("testuser@gmail.com", "correctPassword");

        // Espresso waits until the login + checkUserExists + navigation logic finishes
        // and the homeContainer view appears in the next activity.
        onView(withId(R.id.homeContainer)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToRegister() {
        onView(withId(R.id.tvRegister)).perform(click());
        
        // Navigation is usually fast, but Espresso handles it gracefully
        onView(withId(R.id.etFullName)).check(matches(isDisplayed()));
    }

    @Test
    public void testSuccessfulRegistration() {
        onView(withId(R.id.tvRegister)).perform(click());

        // Generate a unique email to avoid "User already exists" error
        String uniqueEmail = "test" + System.currentTimeMillis() + "@gmail.com";

        onView(withId(R.id.etFullName)).perform(replaceText("QA Tester"), closeSoftKeyboard());
        onView(withId(R.id.etUsername)).perform(replaceText("tester" + System.currentTimeMillis()), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(replaceText(uniqueEmail), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText("Password@123"), closeSoftKeyboard());
        
        onView(withId(R.id.btnRegister)).perform(click());

        // Automatically waits for Firebase registration and navigation
        onView(withId(R.id.homeContainer)).check(matches(isDisplayed()));
    }

    @Test
    public void testBackNavigation() {
        onView(withId(R.id.tvRegister)).perform(click());
        pressBack();
        
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
    }
}
