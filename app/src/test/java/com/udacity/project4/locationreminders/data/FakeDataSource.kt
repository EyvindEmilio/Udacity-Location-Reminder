package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
public class FakeDataSource(
    private val reminders: MutableList<ReminderDTO> = mutableListOf()
) : ReminderDataSource {

    private var hasError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (hasError) Result.Error("Can't load reminders") else Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        if (hasError) Result.Error("Can't save reminder") else reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (hasError) {
            Result.Error("Can't get reminder")
        } else {
            val reminder = reminders.find { it.id == id }
            if (reminder != null) {
                Result.Success(reminder)
            } else {
                Result.Error("Reminder not found")
            }
        }
    }

    override suspend fun deleteAllReminders() {
        if (hasError) Result.Error("Can't delete reminders") else reminders.clear()
    }

    fun setHasError(hasError: Boolean) {
        this.hasError = hasError
    }

}