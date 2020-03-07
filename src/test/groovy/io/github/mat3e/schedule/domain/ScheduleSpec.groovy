package io.github.mat3e.schedule.domain

import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration
import java.time.ZonedDateTime

import static java.time.temporal.ChronoUnit.HOURS

class ScheduleSpec extends Specification {
    @Subject
    private Schedule toTest

    def setup() {
        toTest = new Schedule()
    }

    def 'should throw when scheduling for the taken date'() {
        given:
        def start = ZonedDateTime.now()
        def end = start + Duration.of(2, HOURS)
        and:
        toTest.scheduleOnCall(exampleEntry(start, end))

        when:
        def laterStart = start + Duration.of(1, HOURS)
        and:
        toTest.scheduleOnCall(exampleEntry(laterStart, end))

        then:
        thrown DateAlreadyTakenException
    }

    def 'should throw when scheduling with the taken room'() {
        expect:
        true
    }

    def 'should schedule a new on call date'() {
        expect:
        true
    }

    def 'should throw when scheduling a visit to an absent doctor'() {
        expect:
        true
    }

    def 'should throw when scheduling a visit for the taken date'() {
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
        new ScheduleEntry(exampleSurgeon(), from, to)
    }

    private static Doctor exampleSurgeon() {
        new Doctor(Specialization.SURGEON)
    }
}
