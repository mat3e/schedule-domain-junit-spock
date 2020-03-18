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

@Title('Specification for schedule entries')
class ScheduleEntrySpec extends Specification {
    private static final ZonedDateTime start = ZonedDateTime.now()
    private static final ZonedDateTime end = start + of(2, HOURS)

    @Shared
    @Subject
    private static final ScheduleEntry entry = exampleEntry()

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

    def 'should NOT interfere when different room'() {
        expect:
        !entry.interferesWith(new ScheduleEntry(entry.doctor, start, end, new Room('different')))
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

    @Unroll
    def 'is identified by its values (#description)'() {
        expect:
        factory() == factory()

        where:
        factory << [
                () -> new ScheduleEntry(
                        new Doctor(Specialization.SURGEON),
                        start,
                        end,
                        new Room('example'),
                        new Patient('Frank')
                ),
                () -> new ScheduleEntry(
                        new Doctor(Specialization.SURGEON),
                        start,
                        end,
                        new Room('example')
                )
        ]
        description << ['with patient', 'without patient']
    }

    @Unroll
    def 'should #condition a visit'() {
        expect:
        new ScheduleEntry(
                exampleSurgeon(),
                start,
                end,
                new Room('foo'),
                potentialPatient
        ).visit == expected

        where:
        potentialPatient | expected | condition
        null             | false    | 'NOT be'
        new Patient('p') | true     | 'be'
    }

    private static ScheduleEntry exampleEntry(ZonedDateTime from = start, ZonedDateTime to = end) {
        new ScheduleEntry(
                exampleSurgeon(),
                from,
                to,
                new Room('foo')
        )
    }

    private static Doctor exampleSurgeon() {
        new Doctor(Specialization.SURGEON)
    }
}
