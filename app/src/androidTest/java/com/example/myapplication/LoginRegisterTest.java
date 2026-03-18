package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.myapplication.activities.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI Test Bot for Login and Registration functionality.
 * Optimized with Root Matchers and Delays to handle Loading Dialogs.
 */
@RunWith(AndroidJUnit4.class)
public class LoginRegisterTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    private void waitFor(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    @Test
    public void testFullNavigationFlow() {
        onView(withId(R.id.btnPhoneSignIn)).perform(click());
        waitFor(1000);
        onView(withId(R.id.btnSendOtp)).check(matches(isDisplayed()));
        
        androidx.test.espresso.Espresso.pressBack();
        waitFor(500);

        onView(withId(R.id.tvRegister)).perform(click());
        waitFor(1000);
        onView(withId(R.id.etFullName)).check(matches(isDisplayed()));
    }

    @Test
    public void testEmailLoginValidation() {
        // Test empty fields
        onView(withId(R.id.btnLogin)).perform(click());
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));

        // Test invalid credentials
        onView(withId(R.id.etEmail)).perform(typeText("wrong@test.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        
        // IMPORTANT: The app shows a Loading Dialog ("Logging in...")
        // We must wait for the dialog to disappear before checking for the button again.
        waitFor(3000); 
        
        // Check if we are back on the Login screen (button is visible)
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
    }

    @Test
    public void testRegistrationValidation() {
        onView(withId(R.id.tvRegister)).perform(click());
        waitFor(1000);

        onView(withId(R.id.btnRegister)).perform(click());
        onView(withId(R.id.etFullName)).check(matches(isDisplayed()));

        onView(withId(R.id.etFullName)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.etUsername)).perform(typeText("tu"), closeSoftKeyboard());
        onView(withId(R.id.btnRegister)).perform(click());
        
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
    }

    @Test
    public void testSocialNavigationFromRegister() {
        onView(withId(R.id.tvRegister)).perform(click());
        waitFor(1000);

        onView(withId(R.id.btnPhoneSignUp)).perform(click());
        waitFor(1500); // Transition to PhoneLoginActivity

        onView(withId(R.id.btnSendOtp)).check(matches(isDisplayed()));
    }
}
