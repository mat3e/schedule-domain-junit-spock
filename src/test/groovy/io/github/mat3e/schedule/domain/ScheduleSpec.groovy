package io.github.mat3e.schedule.domain

import spock.lang.Specification
import spock.lang.Subject

import java.time.ZonedDateTime

class ScheduleSpec extends Specification {
    @Subject
    private Schedule toTest

    def setup() {
        toTest = new Schedule()
    }

    def 'should throw when scheduling for the taken date'() {
        given:
        Doctor first = exampleSurgeon()
        def start = ZonedDateTime.now()
        def end = start.plusHours(2)
        and:
        toTest.scheduleOnCall(first, start, end)

        when:
        Doctor second = exampleSurgeon()
        def start2 = start.plusHours(1)
        and:
        toTest.scheduleOnCall(second, start2, end)

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

    private static Doctor exampleSurgeon() {
        new Doctor(Specialization.SURGEON)
    }
}
