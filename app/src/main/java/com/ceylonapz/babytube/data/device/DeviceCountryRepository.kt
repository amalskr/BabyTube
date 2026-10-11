package com.ceylonapz.babytube.data.device

import android.content.Context
import android.telephony.TelephonyManager
import com.ceylonapz.babytube.domain.repository.CountryRepository

class DeviceCountryRepository(
    private val context: Context,
) : CountryRepository {

    // Network/SIM country is the most reliable signal, but most TVs have no telephony,
    // so fall back to the region the user picked in system settings.
    override fun getCountryCode(): String? {
        val telephony = context.getSystemService(TelephonyManager::class.java)
        return listOf(
            telephony?.networkCountryIso,
            telephony?.simCountryIso,
            context.resources.configuration.locales[0].country,
        ).firstOrNull { !it.isNullOrBlank() }?.uppercase()
    }
}
