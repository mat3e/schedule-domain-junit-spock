package io.github.mat3e.schedule.domain


import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title
import spock.lang.Unroll

import java.time.ZonedDateTime

import static java.time.Duration.of
import static java.time.temporal.ChronoUnit.HOURS
import static java.time.temporal.ChronoUnit.MINUTES
import static java.time.temporal.ChronoUnit.SECONDS

@Title("Specification for schedule entries")
class ScheduleEntrySpec extends Specification {
    private static final ZonedDateTime start = ZonedDateTime.now()
    private static final ZonedDateTime end = start + of(2, HOURS)

    @Shared
    @Subject
    private static final ScheduleEntry entry = exampleEntry(start, end)

    def 'should interfere when the same start'() {
        expect:
        entry.interferesWith(entry)
    }

    def 'should NOT interfere when new start equal to old end'() {
        expect:
        !entry.interferesWith(exampleEntry(end, end + of(2, HOURS)))
    }

    def 'should NOT interfere when new end equal to old start'() {
        expect:
        !entry.interferesWith(exampleEntry(start - of(2, HOURS), start))
    }

    @Unroll
    def 'should interfere when start between an existing start and end'() {
        given:
        def lateEnd = startBetween + of(2, HOURS)

        expect:
        entry.interferesWith(exampleEntry(startBetween, lateEnd))

        where:
        startBetween << [
                start + of(1, SECONDS),
                start + of(30, MINUTES),
                end - of(1, SECONDS)
        ]
    }

    @Unroll
    def 'should interfere when end between an existing start and end (#description)'() {
        given:
        def newStart = endBetween - of(2, HOURS)

        expect:
        entry.interferesWith(exampleEntry(newStart, endBetween))

        where:
        endBetween             | description
        end - of(1, SECONDS)   | 'end 1s earlier'
        start + of(1, SECONDS) | 'end 1s after start'
        start + of(1, HOURS)   | 'end in between'
    }

    def 'should throw when creating with start less than end'() {
        when:
        exampleEntry(end, start)

        then:
        thrown IllegalArgumentException
    }

    private static ScheduleEntry exampleEntry(ZonedDateTime from, ZonedDateTime to) {
        new ScheduleEntry(exampleSurgeon(), from, to)
    }

    private static Doctor exampleSurgeon() {
        new Doctor(Specialization.SURGEON)
    }
}
