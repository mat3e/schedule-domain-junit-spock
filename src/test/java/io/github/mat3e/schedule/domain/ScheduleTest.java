package io.github.mat3e.schedule.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScheduleTest {
    @Test
    @DisplayName("should throw when scheduling for the taken date")
    void scheduleOnCall_throwsWhenDateAlreadyTaken() {
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
}