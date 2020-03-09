package io.github.mat3e.schedule.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

public class Schedule {
    private final UUID clinicId = UUID.randomUUID();
    private final Set<Room> availableRooms = new HashSet<>();
    private final Set<ScheduleEntry> entries = new HashSet<>();

    Schedule(Collection<Room> clinicRooms) {
        availableRooms.addAll(clinicRooms);
    }

    public void scheduleOnCall(final ScheduleEntry newEntry) {
        assertCorrectEntry(newEntry);
        entries.add(newEntry);
    }

    private void assertCorrectEntry(final ScheduleEntry newEntry) {
        Set<Room> interferingRooms = entries.stream()
                .filter(existingVisit -> existingVisit.interferesWith(newEntry))
                .map(ScheduleEntry::getRoom)
                .collect(toSet());
        if (interferingRooms.size() > 0) {
            availableRooms.stream()
                    .filter(room -> !interferingRooms.contains(room))
                    .findAny()
                    .ifPresentOrElse(
                            (potentialRoom) -> {
                                throw new RoomAlreadyTakenException(newEntry.getRoom());
                            },
                            () -> {
                                throw new DateAlreadyTakenException();
                            }
                    );
        }
    }
}
