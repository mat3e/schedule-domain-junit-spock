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
    private val range = from..to

    init {
        if (range.isEmpty()) {
            throw IllegalArgumentException("$from should be less than $to")
        }
    }

    fun interferesWith(other: ScheduleEntry): Boolean = other.from within this.range || this.from within other.range
}

private infix fun ZonedDateTime.within(range: ClosedRange<ZonedDateTime>): Boolean = this in range && this < range.endInclusive