package com.app.tracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class TaskCreationUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun appLaunches_clicksFAB_typesTitle_saves_verifiesItemInList() {
        // 1. Click FAB to add task
        onView(withId(R.id.fab_add_task))
            .perform(click())

        // 2. Type task title
        onView(withId(R.id.edit_task_title))
            .perform(typeText("Espresso Task Title"), closeSoftKeyboard())

        // 3. Type task description
        onView(withId(R.id.edit_task_desc))
            .perform(typeText("Created by Espresso UI Test"), closeSoftKeyboard())

        // 4. Click Save Task button
        onView(withId(R.id.btn_save_task))
            .perform(click())

        // 5. Verify the item appears in the list view
        onView(withText("Espresso Task Title"))
            .check(matches(isDisplayed()))
    }
}
