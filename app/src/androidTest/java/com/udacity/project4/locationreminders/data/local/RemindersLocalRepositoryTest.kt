package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    private val reminder1 = ReminderDTO("Title1", "Des1", "Loc1", 0.01, 0.01)
    private val reminder2 = ReminderDTO("Title2", "Des2", "Loc2", 0.02, 0.02)
    private val reminder3 = ReminderDTO("Title3", "Des3", "Loc3", 0.03, 0.03)

    private val reminders = mutableListOf(reminder1, reminder2, reminder3)
    lateinit var database: RemindersDatabase

    private lateinit var reminderRepository: RemindersLocalRepository

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        reminderRepository = RemindersLocalRepository(
            database.reminderDao(), TestCoroutineDispatcher()
        )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun getRemindersTest() = runBlockingTest {
        reminders.forEach { reminderRepository.saveReminder(it) }
        val remindersSaved = reminderRepository.getReminders()
        assert(remindersSaved is Result.Success)
        assertThat(
            (remindersSaved as Result.Success<List<ReminderDTO>>).data.size,
            `is`(reminders.size)
        )
    }

    @Test
    fun saveReminderTest() = runBlockingTest {
        // When
        reminderRepository.saveReminder(reminder1)
        val savedData = reminderRepository.getReminder(reminder1.id)

        assert(savedData is Result.Success)
        assertThat((savedData as Result.Success).data.id, `is`(reminder1.id))
    }

    @Test
    fun getReminderTest() = runBlockingTest {
        reminders.forEach { reminderRepository.saveReminder(it) }
        val reminderSaved = reminderRepository.getReminder(reminder3.id)
        assert(reminderSaved is Result.Success)
        assertThat(
            (reminderSaved as Result.Success<ReminderDTO>).data.id,
            `is`(reminder3.id)
        )
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {
        reminders.forEach { reminderRepository.saveReminder(it) }
        var remindersSaved = reminderRepository.getReminders()
        assert(remindersSaved is Result.Success)
        assertThat(
            (remindersSaved as Result.Success<List<ReminderDTO>>).data.size,
            `is`(reminders.size)
        )

        reminderRepository.deleteAllReminders()
        remindersSaved = reminderRepository.getReminders()
        assert(remindersSaved is Result.Success)
        assertThat(
            (remindersSaved as Result.Success<List<ReminderDTO>>).data.size,
            `is`(0)
        )
    }
}