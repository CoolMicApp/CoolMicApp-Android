package cc.echonet.coolmicapp.test;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.filters.LargeTest;

import cc.echonet.coolmicapp.MainActivity;
import cc.echonet.coolmicapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityBasicTest {

    private static final String STRING_LISTENER = "N/A(N/A)";
    private static final String STRING_TIMER = "00:00:00";

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void basicFields_Test() {
        onView(withId(R.id.txtListeners)).check(matches(withText(STRING_LISTENER)));
        onView(withId(R.id.timerValue)).check(matches(withText(STRING_TIMER)));
        onView(withId(R.id.start_recording_button)).check(matches(withText("START LIVESTREAM")));
        onView(withId(R.id.start_recording_button)).perform(click());
        onView(withId(R.id.start_recording_button)).check(matches(withText("START LIVESTREAM")));
    }
}
