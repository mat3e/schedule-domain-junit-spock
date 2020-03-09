package io.github.mat3e.schedule.domain;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
class ScheduleTest {
    private static final ZonedDateTime start = ZonedDateTime.now();
    private static final ZonedDateTime end = start.plusHours(2);

    private Schedule toTest;

    @BeforeEach
    void init() {
        toTest = new Schedule(emptySet());
    }

    @Test
    @DisplayName("should schedule a new on call date")
    void scheduleOnCall_worksAsExpected() {
        assertAll(
                // first schedule = works as a charm!
                () -> assertDoesNotThrow(() -> toTest.scheduleOnCall(exampleEntry(start, end))),
                // second time? NO WAY, already scheduled
                () -> assertThrows(
                        BusinessScheduleException.class,
                        () -> toTest.scheduleOnCall(exampleEntry(start, end))
                )
        );
    }

    @Test
    @DisplayName("should throw when scheduling for the taken date (all rooms taken)")
    void scheduleOnCall_throwsWhenDateAlreadyTaken() {
        // given
        toTest.scheduleOnCall(exampleEntry(start, end));

        // when
        var laterStart = start.plusHours(1);

        // then
        assertThrows(
                DateAlreadyTakenException.class,
                () -> toTest.scheduleOnCall(exampleEntry(laterStart, end))
        );
    }

    @Test
    @DisplayName("should throw when scheduling with the taken room")
    void scheduleOnCall_throwsWhenRoomAlreadyTaken() {
        // given
        var roomFoo = new Room("foo");
        // and
        toTest = new Schedule(Set.of(roomFoo, new Room("bar")));
        // and
        toTest.scheduleOnCall(new ScheduleEntry(
                exampleSurgeon(),
                start,
                end,
                roomFoo
        ));

        // when + then
        var e = assertThrows(
                RoomAlreadyTakenException.class,
                () -> toTest.scheduleOnCall(new ScheduleEntry(
                        exampleSurgeon(),
                        start.plusHours(1),
                        start.plusHours(3),
                        roomFoo
                ))
        );
        assertEquals("Cannot schedule for a room \"foo\"", e.getMessage());
    }

    @Test
    @DisplayName("should throw when scheduling a visit with no patient")
    void scheduleNewVisit_throwsWhenNoPatient() {
    }

    @Test
    @DisplayName("should schedule a new visit")
    void scheduleNewVisit_worksAsExpected() {
    }

    @Test
    @DisplayName("should throw when overriding on call with a doctor with a different specialization")
    void overrideOnCall_throwsWhenDifferentSpecialization() {
    }

    @Test
    @DisplayName("should override on call")
    void overrideOnCall_worksAsExpected() {
    }

    @NotNull
    private static ScheduleEntry exampleEntry(ZonedDateTime from, ZonedDateTime to) {
        return new ScheduleEntry(
                exampleSurgeon(),
                from,
                to,
                new Room("foo"),
                new Patient("bar")
        );
    }

    @NotNull
    private static Doctor exampleSurgeon() {
        return new Doctor(Specialization.SURGEON);
    }
}