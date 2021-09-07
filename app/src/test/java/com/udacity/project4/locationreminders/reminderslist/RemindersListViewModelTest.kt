package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun init() {
        stopKoin()

        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun loadReminders_withNoResult() = runBlockingTest {
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isEmpty(), `is`(true))
    }

    @Test
    fun loadReminders_withResult() = runBlockingTest {
        val reminder = ReminderDTO("Title1", "Des1", "Loc1", 0.01, 0.01)
        fakeDataSource.saveReminder(reminder)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isEmpty(), `is`(false))
    }

    @Test
    fun loadReminders_showLoader() = runBlockingTest {
        val reminder = ReminderDTO("Title1", "Des1", "Loc1", 0.01, 0.01)
        fakeDataSource.saveReminder(reminder)

        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_errorLoading() = runBlockingTest {
        val reminder = ReminderDTO("Title1", "Des1", "Loc1", 0.01, 0.01)
        fakeDataSource.saveReminder(reminder)
        fakeDataSource.setHasError(true)
        remindersListViewModel.loadReminders()
        assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            `is`("Can't load reminders")
        )
    }

}