package io.github.mat3e.schedule.domain;

import java.util.HashSet;
import java.util.Set;

public class Schedule {
    private Set<ScheduleEntry> entries = new HashSet<>();

    public void scheduleOnCall(final ScheduleEntry newEntry) {
        if (entries.stream().anyMatch(existingVisit -> existingVisit.interferesWith(newEntry))) {
            throw new DateAlreadyTakenException();
        }
        entries.add(newEntry);
    }
}
