package com.udacity.project4.util

import android.view.WindowManager
import androidx.test.espresso.Root
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

/**
 * @ToastMatcher class based in https://github.com/android/android-test/issues/803
 */
class ToastMatcher : TypeSafeMatcher<Root?>() {
    private var currentFailures: Int = 0
    private var maximumRetries: Int = 5

    override fun matchesSafely(root: Root?): Boolean {
        val type = root?.windowLayoutParams?.get()?.type
        if (type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) {
            val windowToken = root.decorView?.windowToken
            val appToken = root.decorView.applicationWindowToken
            if (windowToken === appToken) {
                return true
            }
        }
        return ++currentFailures >= maximumRetries
    }

    override fun describeTo(description: Description?) {
        description?.appendText("is toast")
    }
}