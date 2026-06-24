package com.windfreak.synthhd.persistence

import android.content.Context
import com.windfreak.synthhd.domain.SynthDeviceState
import com.windfreak.synthhd.domain.synthDeviceStateFromJson
import com.windfreak.synthhd.domain.toJson
import org.json.JSONObject

class SynthStateStore(context: Context) {
    private val preferences = context.getSharedPreferences("synthhd-state", Context.MODE_PRIVATE)

    fun load(): SynthDeviceState {
        val raw = preferences.getString(KEY_STATE, null) ?: return SynthDeviceState()
        return runCatching { synthDeviceStateFromJson(JSONObject(raw)) }.getOrDefault(SynthDeviceState())
    }

    fun save(state: SynthDeviceState) {
        preferences.edit().putString(KEY_STATE, state.toJson().toString()).apply()
    }

    companion object {
        private const val KEY_STATE = "state"
    }
}
