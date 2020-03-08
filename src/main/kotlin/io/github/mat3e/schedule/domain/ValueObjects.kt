package io.github.mat3e.schedule.domain

import java.time.ZonedDateTime

enum class Specialization {
    SURGEON
}

data class Doctor(val specialization: Specialization)

data class Patient(val name: String)

data class Room(val name: String)

data class ScheduleEntry @JvmOverloads constructor(
        val doctor: Doctor,
        val from: ZonedDateTime,
        val to: ZonedDateTime,
        val room: Room,
        val patient: Patient? = null
) {
    private val range = from..to

    init {
        if (range.isEmpty()) {
            throw IllegalArgumentException("$from should be less than $to")
        }
    }

    fun interferesWith(other: ScheduleEntry): Boolean =
            this.room == other.room && this datesInterfereWith other

    fun withPatient(patient: Patient): ScheduleEntry {
        if (this.patient != null) {
            throw DateAlreadyTakenException()
        }
        return this.copy(patient = patient)
    }

    private infix fun datesInterfereWith(other: ScheduleEntry): Boolean =
            other.from within this.range || this.from within other.range
}

private infix fun ZonedDateTime.within(range: ClosedRange<ZonedDateTime>): Boolean = this in range && this < range.endInclusive