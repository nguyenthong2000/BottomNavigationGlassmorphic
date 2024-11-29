package xyz.teamgravity.bottomnavigationglassmorphic.haze

import android.util.Log
import androidx.compose.runtime.snapshots.Snapshot


internal const val LOG_ENABLED = false

internal fun log(tag: String, message: () -> String) {
    if (LOG_ENABLED) {
        Snapshot.withoutReadObservation {
            Log.d(tag, message())
        }
    }
}