package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.isBackgroundPermissionGranted
import com.udacity.project4.utils.isLocationPermissionGranted
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    companion object {
        const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT.LOCATION_REMINDER"
        const val REQUEST_CODE_LOCATION_PERMISSION = 43512
        const val REQUEST_CODE_LOCATION_AND_BACKGROUND_PERMISSION = 4354
        const val REQUEST_TURN_DEVICE_LOCATION_ON = 765
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            val reminder = ReminderDataItem(
                title, description, location, latitude, longitude
            )

            if (_viewModel.validateEnteredData(reminder)) {
                requestLocationPermissionsAndStartGeofencing()
            }
        }
    }

    private fun checkDeviceLocationSettingsAndStartGeofence() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.d("Error getting location settings resolution: ${sendEx.message}")
                }
            } else {
                Snackbar.make(
                    binding.root, R.string.location_required_error, Snackbar.LENGTH_LONG
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener { task ->
            Timber.d("locationSettingsResponseTask.addOnCompleteListener ${task.isSuccessful}")
            if (task.isSuccessful) {
                _viewModel.reminderDataItemValidated?.let {
                    _viewModel.saveReminder(it)
                    buildGeofence(it)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("onActivityResult() requestCode=$requestCode, resultCode=$resultCode")
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            if (resultCode == Activity.RESULT_OK) {
                _viewModel.reminderDataItemValidated?.let {
                    _viewModel.saveReminder(it)
                    buildGeofence(it)
                }
            } else {
                Snackbar.make(
                    binding.root, R.string.location_required_error, Snackbar.LENGTH_LONG
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
    }

    private fun requestLocationPermissionsAndStartGeofencing() {
        Timber.d("requestLocationPermissionsAndStartGeofencing()")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Timber.d("isLocationPermissionGranted = ${requireContext().isLocationPermissionGranted()}")
            Timber.d("isBackgroundPermissionGranted = ${requireContext().isBackgroundPermissionGranted()}")
            if (requireContext().isLocationPermissionGranted() && requireContext().isBackgroundPermissionGranted()) {
                checkDeviceLocationSettingsAndStartGeofence()
            } else {
                Timber.d("Request location for android >= Q")
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.location)
                        .setMessage(R.string.location_message)
                        .setPositiveButton(R.string.allow) { _, _ ->
                            requestPermissions(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                ),
                                REQUEST_CODE_LOCATION_AND_BACKGROUND_PERMISSION
                            )
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                } else {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        REQUEST_CODE_LOCATION_AND_BACKGROUND_PERMISSION
                    )
                }
            }
        } else {
            if (requireContext().isLocationPermissionGranted()) {
                checkDeviceLocationSettingsAndStartGeofence()
            } else {
                Timber.d("Request location for android < Q")
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_LOCATION_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.d("onRequestPermissionsResult()")
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (requireContext().isLocationPermissionGranted()) {
                Timber.d("Permission granted")
                checkDeviceLocationSettingsAndStartGeofence()
            } else {
                Timber.d("Permission denied")
                showSnackBarToOpenAppSettings()
            }
        } else if (requestCode == REQUEST_CODE_LOCATION_AND_BACKGROUND_PERMISSION) {
            Timber.d("isLocationPermissionGranted = ${requireContext().isLocationPermissionGranted()}")
            Timber.d("isBackgroundPermissionGranted = ${requireContext().isBackgroundPermissionGranted()}")

            if (requireContext().isLocationPermissionGranted() &&
                requireContext().isBackgroundPermissionGranted()
            ) {
                Timber.d("Permission granted for android Q+")
                checkDeviceLocationSettingsAndStartGeofence()
            } else {
                Timber.d("Permission denied for android Q+")
                showSnackBarToOpenAppSettings()
            }
        }
    }

    private fun showSnackBarToOpenAppSettings() {
        Snackbar.make(
            binding.root, R.string.permission_denied_explanation, Snackbar.LENGTH_LONG
        ).setAction(android.R.string.ok) {
            startActivity(Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireActivity().packageName, null)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }.show()
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @SuppressLint("MissingPermission")
    fun buildGeofence(reminder: ReminderDataItem) {
        // Build the Geofence Object
        val geofence = Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(
                reminder.latitude ?: 0.0,
                reminder.longitude ?: 0.0,
                100f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Timber.d("Geofence Added")
            }
            addOnFailureListener {
                Timber.d("Failed adding geofence Geofence")
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
