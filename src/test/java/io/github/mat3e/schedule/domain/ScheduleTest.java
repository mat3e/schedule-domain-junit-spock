package io.github.mat3e.schedule.domain;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
class ScheduleTest {
    private Schedule toTest;

    @BeforeEach
    void init() {
        toTest = new Schedule();
    }

    @Test
    @DisplayName("should throw when scheduling for the taken date")
    void scheduleOnCall_throwsWhenDateAlreadyTaken() {
        // given
        var start = ZonedDateTime.now();
        var end = start.plusHours(2);
        // and
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
    }

    @Test
    @DisplayName("should schedule a new on call date")
    void scheduleOnCall_worksAsExpected() {
    }

    @Test
    @DisplayName("should throw when scheduling a visit to an absent doctor")
    void scheduleNewVisit_throwsWhenDoctorUnavailable() {
    }

    @Test
    @DisplayName("should throw when scheduling a visit for the taken date")
    void scheduleNewVisit_throwsWhenDateAlreadyTaken() {
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