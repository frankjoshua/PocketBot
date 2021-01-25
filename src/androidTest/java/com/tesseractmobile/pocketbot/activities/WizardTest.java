package com.tesseractmobile.pocketbot.activities;

import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.wizard.WizardActivity;
import com.tesseractmobile.pocketbot.activities.wizard.WizardStepOne;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by josh on 6/24/17.
 */
@RunWith(AndroidJUnit4.class)
public class WizardTest {

    @Rule
    public ActivityTestRule<WizardActivity> mActivityRule = new ActivityTestRule<>(WizardActivity.class);

    @Test
    public void testOnCreate() throws Exception {
        onView(withId(R.id.btnSkip)).check(ViewAssertions.matches(isDisplayed()));
    }

    @Test
    public void testMountedOnRobot() throws Exception {
        onView(withId(R.id.btnOnRobot)).perform(ViewActions.click());
        onView(withId(R.id.btnWifi)).check(ViewAssertions.matches(isDisplayed()));
    }

}
