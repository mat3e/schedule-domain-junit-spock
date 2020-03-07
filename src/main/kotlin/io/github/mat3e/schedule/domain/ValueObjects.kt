package io.github.mat3e.schedule.domain

import java.time.ZonedDateTime

enum class Specialization {
    SURGEON
}

data class Doctor(val specialization: Specialization)

data class ScheduleEntry(
        val doctor: Doctor,
        val from: ZonedDateTime,
        val to: ZonedDateTime
) {
    companion object {
        @JvmStatic
        fun of(doctor: Doctor, from: ZonedDateTime, to: ZonedDateTime) = ScheduleEntry(doctor, from, to)
    }

    fun interferesWith(other: ScheduleEntry): Boolean = interferesWith(other.from)

    private fun interferesWith(date: ZonedDateTime): Boolean = date.isAfter(from) && date.isBefore(to)
}