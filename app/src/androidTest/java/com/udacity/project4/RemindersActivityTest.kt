package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.ui.authentication.AuthVM
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            viewModel {
                AuthVM()
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun addReminder_withCorrectValuesWithoutLocation() {
        runBlocking { repository.deleteAllReminders() }
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed())).perform(typeText("Title1"))
        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
            .perform(typeText("Desc1"))
        closeSoftKeyboard()

        onView(withId(R.id.saveReminder)).check(matches(isDisplayed())).perform(click())

        onView(withText("Title1")).check(matches(isDisplayed()))
        onView(withText("Desc1")).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.select_location)))
    }

    @Test
    fun addReminder_withBadValues() {
        runBlocking { repository.deleteAllReminders() }
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed())).perform(click())

        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
            .perform(typeText("Desc1"))

        closeSoftKeyboard()
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed())).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))
    }
}
