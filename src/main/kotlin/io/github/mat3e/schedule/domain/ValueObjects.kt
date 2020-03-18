package io.github.mat3e.schedule.domain

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Optional
import java.util.UUID
import java.util.stream.Collectors.toSet
import java.util.stream.Stream

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
    private val range: ClosedRange<ZonedDateTime> = from..to
    val isVisit: Boolean = patient != null

    init {
        if (range.isEmpty()) {
            throw IllegalArgumentException("$from should be less than $to")
        }
    }

    fun interferesWith(other: ScheduleEntry): Boolean =
            this.room == other.room && this datesInterfereWith other

    /**
     * Returns a new set containing this visit mixed with the existing "on call" entries.
     *
     * E.g. when the entry is a visit 11:00-12:30 and entries are [on call 10:00-12:00, on call 12:00-14:00],
     * the result will be [on call 10:00-11:00, visit 11:00-12:30, on call 12:30-14:00].
     *
     * All the entries must interfere with this entry and must have the same doctor. None of the entries can be a visit.
     * Moreover, there can be NO empty slots between them.
     * Otherwise, set is returned without any modifications.
     *
     * @param entries "on call" entries which should interfere with this, have the same doctor and should not have any empty slots in between them
     * @return set containing a visit inside the "on call" entries if all the conditions fulfilled;
     * unmodified set otherwise
     */
    internal fun immerseInto(entries: Set<ScheduleEntry>): Set<ScheduleEntry> {
        if (this cannotBeImmersedInto entries) {
            return entries
        }
        val totalRange: ClosedRange<ZonedDateTime> = entries
                .sortedBy { it.from }
                .map { it.range }
                .reduce { totalRange, currentRange ->
                    when {
                        totalRange.isEmpty() -> totalRange
                        totalRange canBeMergedWith currentRange -> totalRange.start..currentRange.endInclusive
                        else -> emptyRange()
                    }
                }
        if (this notFullyIncludedIn totalRange) {
            return entries
        }
        return Stream.concat(
                Stream.of(this),
                Stream.concat(
                        convertToOnCallWithDates(totalRange.start, this.from).stream(),
                        convertToOnCallWithDates(this.to, totalRange.endInclusive).stream()
                )
        ).collect(toSet())
    }

    internal infix fun datesInterfereWith(other: ScheduleEntry): Boolean =
            other.from within this.range || this.from within other.range

    private infix fun cannotBeImmersedInto(entries: Set<ScheduleEntry>): Boolean =
            !this.isVisit || this notRelatedWith entries || entries.any { it.isVisit }

    private infix fun notRelatedWith(entries: Set<ScheduleEntry>): Boolean =
            entries.any { !it.interferesWith(this) || it.doctor != this.doctor }

    private infix fun notFullyIncludedIn(totalRange: ClosedRange<ZonedDateTime>): Boolean =
            totalRange.isEmpty() || this.from !in totalRange || this.to !in totalRange

    private fun convertToOnCallWithDates(start: ZonedDateTime, end: ZonedDateTime): Optional<ScheduleEntry> =
            Optional.of(start)
                    .filter { start < end }
                    .map { copy(from = start, to = end, patient = null) }
}

data class ScheduleSnapshot(val clinicId: UUID, val entries: Set<ScheduleEntry>)

// exclusive end in range
private infix fun ZonedDateTime.within(range: ClosedRange<ZonedDateTime>): Boolean =
        this in range && this < range.endInclusive

// range right after another range
private infix fun ClosedRange<ZonedDateTime>.canBeMergedWith(other: ClosedRange<ZonedDateTime>): Boolean =
        this.endInclusive == other.start

// big dates in the reversed order = empty range
private fun emptyRange(): ClosedRange<ZonedDateTime> =
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.MAX_VALUE), ZoneOffset.UTC)..ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.MIN_VALUE), ZoneOffset.UTC)
