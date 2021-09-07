package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun init() {
        stopKoin()

        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun saveReminder_withCorrectValues() {
        val reminder = ReminderDataItem("Title1", "Des1", "Loc1", 0.01, 0.01)
        saveReminderViewModel.saveReminder(reminder)

        assertThat(
            saveReminderViewModel.showToast.getOrAwaitValue(),
            `is`("Reminder Saved !")
        )
    }

    @Test
    fun saveReminder_withIncorrectTitle() {
        val reminder = ReminderDataItem(null, "Desc 1", "Loc1", 0.01, 0.01)
        saveReminderViewModel.validateAndSaveReminder(reminder)

        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )
    }

    @Test
    fun saveReminder_withIncorrectLocation() {
        val reminder = ReminderDataItem("Title1", "Desc 1", null, 0.01, 0.01)
        saveReminderViewModel.validateAndSaveReminder(reminder)

        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
    }

    @Test
    fun saveReminder_checkLoader() {
        val reminder = ReminderDataItem("Title1", "Des1", "Loc1", 0.01, 0.01)
        mainCoroutineRule.pauseDispatcher()

        saveReminderViewModel.saveReminder(reminder)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
    }


}