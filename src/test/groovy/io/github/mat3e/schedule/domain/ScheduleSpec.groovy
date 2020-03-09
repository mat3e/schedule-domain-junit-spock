package io.github.mat3e.schedule.domain

import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration
import java.time.ZonedDateTime

import static java.time.temporal.ChronoUnit.HOURS

class ScheduleSpec extends Specification {
    private static final ZonedDateTime start = ZonedDateTime.now()
    private static final ZonedDateTime end = start + Duration.of(2, HOURS)

    @Subject
    private Schedule toTest

    def setup() {
        toTest = new Schedule([])
    }

    def 'should schedule a new on call date'() {
        when:
        toTest.scheduleOnCall(exampleEntry(start, end))

        then:
        noExceptionThrown()

        when: 'adding same entry once again'
        toTest.scheduleOnCall(exampleEntry(start, end))

        then:
        thrown BusinessScheduleException
    }

    def 'should throw when scheduling for the taken date (all rooms taken)'() {
        given:
        toTest.scheduleOnCall(exampleEntry(start, end))

        when:
        def laterStart = start + Duration.of(1, HOURS)
        and:
        toTest.scheduleOnCall(exampleEntry(laterStart, end))

        then:
        thrown DateAlreadyTakenException
    }

    def 'should throw when scheduling with the taken room'() {
        given:
        def roomFoo = new Room('foo')
        and:
        toTest = new Schedule([roomFoo, new Room('bar')])
        and:
        toTest.scheduleOnCall(new ScheduleEntry(
                exampleSurgeon(),
                start,
                end,
                roomFoo
        ))

        when:
        toTest.scheduleOnCall(new ScheduleEntry(
                exampleSurgeon(),
                start + Duration.of(1, HOURS),
                start + Duration.of(3, HOURS),
                roomFoo
        ))

        then:
        def e = thrown RoomAlreadyTakenException
        e.message.contains(roomFoo.name)
    }

    def 'should throw when scheduling a visit with no patient'() {
        expect:
        true
    }

    def 'should schedule a new visit'() {
        expect:
        true
    }

    def 'should throw when overriding on call with a doctor with a different specialization'() {
        expect:
        true
    }

    def 'should override on call'() {
        expect:
        true
    }

    private static ScheduleEntry exampleEntry(ZonedDateTime from, ZonedDateTime to) {
        new ScheduleEntry(
                exampleSurgeon(),
                from,
                to,
                new Room('foo'),
                new Patient('bar')
        )
    }

    private static Doctor exampleSurgeon() {
        new Doctor(Specialization.SURGEON)
    }
}
