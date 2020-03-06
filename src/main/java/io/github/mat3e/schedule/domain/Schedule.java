package io.github.mat3e.schedule.domain;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static io.github.mat3e.schedule.domain.ScheduleEntry.of;

public class Schedule {
    private Set<ScheduleEntry> entries = new HashSet<>();

    public void scheduleOnCall(final Doctor doctor, final ZonedDateTime from, final ZonedDateTime to) {
        if (entries.stream().anyMatch(existingVisit -> existingVisit.interferesWith(from))) {
            throw new DateAlreadyTakenException();
        }
        entries.add(of(doctor, from, to));
    }
}
