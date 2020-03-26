package io.github.mat3e.schedule.domain

import org.jetbrains.annotations.NotNull
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

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

    def 'should schedule a new on call'() {
        when:
        toTest.scheduleOnCall(exampleOnCall())

        then:
        noExceptionThrown()

        when: 'adding same entry once again'
        toTest.scheduleOnCall(exampleOnCall())

        then:
        thrown BusinessScheduleException
    }

    def 'should throw when scheduling on call for the taken date (all rooms taken)'() {
        given:
        toTest.scheduleOnCall(exampleOnCall())

        when:
        def laterStart = start + Duration.of(1, HOURS)
        and:
        toTest.scheduleOnCall(exampleOnCall(laterStart, end))

        then:
        thrown DateAlreadyTakenException
    }

    def 'should throw when scheduling on call with the taken room'() {
        given:
        def room1 = new Room('1')
        and:
        toTest = new Schedule([room1, new Room('2')])
        and:
        toTest.scheduleOnCall(new ScheduleEntry(
                exampleSurgeon(),
                start,
                end,
                room1
        ))

        when:
        toTest.scheduleOnCall(new ScheduleEntry(
                exampleSurgeon(),
                start + Duration.of(1, HOURS),
                start + Duration.of(3, HOURS),
                room1
        ))

        then:
        def e = thrown RoomAlreadyTakenException
        e.message.contains(room1.name)
    }

    def 'should throw when scheduling on call with patient'() {
        when:
        toTest.scheduleOnCall(exampleVisit())

        then:
        thrown OnCallWithPatientException
    }

    def 'should throw when scheduling a visit with no patient'() {
        when:
        toTest.scheduleVisit(exampleOnCall())

        then:
        thrown NoPatientException
    }

    def 'should throw when scheduling a visit without a previous on call'() {
        when:
        toTest.scheduleVisit(exampleVisit())

        then:
        def e = thrown NoDoctorOnCallException
        e.message.contains('corresponding on call')
    }

    def 'should throw when scheduling a visit with a different room than in on call'() {
        given:
        def firstRoom = new Room('1')
        def secondRoom = new Room('2')
        and:
        toTest.scheduleOnCall(
                new ScheduleEntry(
                        exampleSurgeon(),
                        start,
                        end,
                        firstRoom
                )
        )

        when:
        toTest.scheduleVisit(
                new ScheduleEntry(
                        exampleSurgeon(),
                        start,
                        end,
                        secondRoom,
                        examplePatient()
                )
        )

        then:
        def e = thrown RoomMismatchException
        e.message.contains("in room $firstRoom.name")
    }

    @Unroll
    def 'should throw when scheduling a visit interfering with a different visit (#description)'() {
        given:
        toTest.scheduleOnCall(exampleOnCall(start, end))
        and:
        toTest.scheduleVisit(
                new ScheduleEntry(
                        exampleSurgeon(),
                        start,
                        end,
                        exampleRoom(),
                        new Patient(firstName)
                )
        )

        when:
        toTest.scheduleVisit(
                new ScheduleEntry(
                        exampleSurgeon(),
                        start,
                        end,
                        exampleRoom(),
                        new Patient(secondName)
                )
        )

        then:
        def e = thrown VisitAlreadyScheduledException
        e.message.contains('interfering visit')
        e.message.contains(firstName)

        where:
        firstName | secondName | description
        'Jack'    | 'Jack'     | 'same patient'
        'Jack'    | 'John'     | 'different patient'
    }

    @Unroll
    def 'should throw when scheduling a visit interfering with an empty slot (#description)'() {
        given:
        onCallStartDates.eachWithIndex { ZonedDateTime start, int i ->
            toTest.scheduleOnCall(exampleOnCall(start, onCallEndDates[i]))
        }

        when:
        toTest.scheduleVisit(exampleVisit())

        then:
        def e = thrown NoDoctorOnCallException
        e.message.contains('not fully aligned')

        where:
        onCallStartDates                               | onCallEndDates                                            | description
        [start.minusMinutes(30), end.minusMinutes(15)] | [end.minusMinutes(30), end.minusMinutes(15).plusHours(2)] | '15-min slot in the middle of a schedule'
        [start.plusMinutes(15)]                        | [end]                                                     | 'visit starts before the on call'
        [start]                                        | [end.minusMinutes(15)]                                    | 'visit ends after the on call'
    }

    def 'should schedule a new visit'() {
        given:
        toTest.scheduleOnCall(exampleOnCall(start.minusHours(1), end.plusHours(3)))

        when:
        toTest.scheduleVisit(exampleVisit())

        then:
        toTest.snapshot.entries == [
                exampleOnCall(start.minusHours(1), start),
                exampleVisit(),
                exampleOnCall(end, end.plusHours(3))
        ] as Set
    }

    def 'should throw when erasing with wrong dates'() {
        when:
        toTest.erase(end, start)

        then:
        thrown IllegalArgumentException
    }

    def 'should throw when no entries to be erased'() {
        given:
        toTest.scheduleOnCall(exampleOnCall(start, end))
        toTest.scheduleOnCall(exampleOnCall(end.plusHours(2), end.plusHours(4)))

        when:
        toTest.erase(end, end.plusHours(2))

        then:
        thrown NothingToEraseException
    }

    def 'should erase entries'() {
        given:
        toTest.scheduleOnCall(exampleOnCall())
        toTest.scheduleOnCall(exampleOnCall(end + Duration.of(1, HOURS), end + Duration.of(2, HOURS)))
        toTest.scheduleOnCall(exampleOnCall(end + Duration.of(3, HOURS), end + Duration.of(5, HOURS)))
        toTest.scheduleVisit(exampleVisit(end + Duration.of(3, HOURS), end + Duration.of(5, HOURS)))

        when:
        toTest.erase(end - Duration.of(1, HOURS), end + Duration.of(4, HOURS))
        and:
        List<ScheduleEntry> result = toTest.snapshot.entries.sort { it.from }

        then:
        result[0] == exampleOnCall(start, end - Duration.of(1, HOURS))
        result[1] == exampleVisit(end + Duration.of(4, HOURS), end + Duration.of(5, HOURS))
    }

    @NotNull
    private static ScheduleEntry exampleOnCall(ZonedDateTime from = start, ZonedDateTime to = end) {
        new ScheduleEntry(
                exampleSurgeon(),
                from,
                to,
                exampleRoom()
        )
    }

    @NotNull
    private static ScheduleEntry exampleVisit(ZonedDateTime from = start, ZonedDateTime to = end) {
        new ScheduleEntry(
                exampleSurgeon(),
                from,
                to,
                exampleRoom(),
                examplePatient()
        )
    }

    private static Doctor exampleSurgeon() {
        new Doctor(Specialization.SURGEON)
    }

    private static Room exampleRoom() {
        new Room('1')
    }

    private static Patient examplePatient() {
        new Patient('patient')
    }
}
