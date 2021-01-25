package com.tesseractmobile.pocketbot.activities;

import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.fragments.facefragments.FaceFragmentFactory;

import static android.support.test.espresso.Espresso.onView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by josh on 1/14/2016.
 */
@RunWith(AndroidJUnit4.class)
public class BaseFaceFragmentActivityTest {

    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<BaseFaceFragmentActivity> mActivityRule = new ActivityTestRule<>(BaseFaceFragmentActivity.class);

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testOnCreate() throws Exception {
        onView(withId(R.id.main_window)).check(ViewAssertions.matches(isDisplayed()));
        //Screengrab.screenshot("name_of_screenshot_here");
    }

    @Test
    public void testEfimface() throws Exception {
        PocketBotSettings.setSelectedFace(mActivityRule.getActivity(), FaceFragmentFactory.ID_FACE_EFIM);
        onView(withId(R.id.eyeViewLeft)).check(ViewAssertions.matches(isDisplayed()));
    }


}