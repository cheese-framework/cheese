package net.codeocean.cheese.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference


import java.util.concurrent.CopyOnWriteArrayList

class ActivityTracker : Application.ActivityLifecycleCallbacks {

    private val activities = CopyOnWriteArrayList<WeakReference<Activity>>()

    fun getCurrentActivity(): Activity? {
        return activities.mapNotNull { it.get() }
            .lastOrNull { !it.isFinishing && !it.isDestroyed }
    }

    fun <T : Activity> getActivity(clazz: Class<T>): T? {
        return activities.mapNotNull { it.get() }
            .firstOrNull { clazz.isInstance(it) && !it.isFinishing && !it.isDestroyed } as? T
    }

    fun getAllActivities(): List<Activity> {
        return activities.mapNotNull { it.get() }
            .filter { !it.isFinishing && !it.isDestroyed }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        cleanUpDeadActivities()
        activities.add(WeakReference(activity))
    }

    override fun onActivityDestroyed(activity: Activity) {
        cleanUpDeadActivities()
        activities.removeAll { it.get() == null || it.get() == activity }
    }

    private fun cleanUpDeadActivities() {
        activities.removeAll {
            val act = it.get()
            act == null || act.isFinishing || act.isDestroyed
        }
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}


object GlobalActivity {
    private var tracker: ActivityTracker? = null

    fun init(application: Application) {
        tracker = ActivityTracker().also {
            application.registerActivityLifecycleCallbacks(it)
        }
    }

    fun getCurrentActivity(): Activity? {
        return tracker?.getCurrentActivity()
    }

    fun <T : Activity> getActivity(clazz: Class<T>): T? {
        return tracker?.getActivity(clazz)
    }

    fun getAllActivities(): List<Activity> {
        return tracker?.getAllActivities().orEmpty()
    }
}