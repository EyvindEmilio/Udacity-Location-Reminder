package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - insert
        val reminderDTO = ReminderDTO(
            "MyTitle",
            "MyDescription",
            "Bolivia",
            -16.497432,
            -68.144733
        )
        database.reminderDao().saveReminder(reminderDTO)

        // WHEN
        val loaded = database.reminderDao().getReminderById(reminderDTO.id)

        // THEN
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminderDTO.id))
        assertThat(loaded.title, `is`(reminderDTO.title))
        assertThat(loaded.description, `is`(reminderDTO.description))
        assertThat(loaded.location, `is`(reminderDTO.location))
        assertThat(loaded.latitude, `is`(reminderDTO.latitude))
        assertThat(loaded.longitude, `is`(reminderDTO.longitude))
    }

    @Test
    fun updateReminderAndGetById() = runBlockingTest {
        // When inserting
        val reminderDTO = ReminderDTO(
            "MyTitle",
            "MyDescription",
            "Bolivia",
            -16.497432,
            -68.144733
        )
        database.reminderDao().saveReminder(reminderDTO)

        // When
        val updatedReminderDTO = ReminderDTO(
            "MyTitleUpdated",
            "MyDescriptionUpdated",
            "Bolivia - La Paz",
            -16.497433,
            -68.144734,
            reminderDTO.id
        )
        database.reminderDao().saveReminder(updatedReminderDTO)
        val loaded = database.reminderDao().getReminderById(reminderDTO.id)

        // THEN
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminderDTO.id))
        assertThat(loaded.title, `is`("MyTitleUpdated"))
        assertThat(loaded.description, `is`("MyDescriptionUpdated"))
        assertThat(loaded.location, `is`("Bolivia - La Paz"))
        assertThat(loaded.latitude, `is`(-16.497433))
        assertThat(loaded.longitude, `is`(-68.144734))
    }
}