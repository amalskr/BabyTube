package com.ceylonapz.babytube.domain.repository

interface CountryRepository {
    /** ISO 3166-1 alpha-2 country code in upper case (e.g. "LK"), or null if unknown. */
    fun getCountryCode(): String?
}
