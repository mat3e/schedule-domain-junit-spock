package io.github.mat3e.schedule.domain;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

public class Schedule {
    private final UUID clinicId;
    private final Set<Room> availableRooms = new HashSet<>();
    private final Set<ScheduleEntry> entries = new HashSet<>();

    Schedule(Collection<Room> clinicRooms) {
        this(UUID.randomUUID(), clinicRooms);
    }

    Schedule(UUID clinicId, Collection<Room> clinicRooms) {
        this.clinicId = clinicId;
        availableRooms.addAll(clinicRooms);
    }

    public void scheduleOnCall(final ScheduleEntry newEntry) {
        assertOnCall(newEntry);
        assertAvailableDate(newEntry);
        entries.add(newEntry);
    }

    void scheduleVisit(final ScheduleEntry entry) {
        assertPatientDefined(entry);
        Set<ScheduleEntry> interferingEntries = findInterferingDoctorEntries(entry);
        assertDoctorOnCall(interferingEntries);
        assertInSameRoom(interferingEntries, entry.getRoom());
        assertNoVisits(interferingEntries);
        Set<ScheduleEntry> newEntries = entry.immerseInto$schedule(interferingEntries);
        if (newEntries.equals(interferingEntries)) {
            throw new NoDoctorOnCallException("Doctor's on calls are not fully aligned with the visit");
        }
        entries.removeAll(interferingEntries);
        entries.addAll(newEntries);
    }

    ScheduleSnapshot getSnapshot() {
        return new ScheduleSnapshot(
                clinicId,
                unmodifiableSet(entries)
        );
    }

    private void assertOnCall(final ScheduleEntry newEntry) {
        if (newEntry.isVisit()) {
            throw new OnCallWithPatientException();
        }
    }

    private void assertAvailableDate(final ScheduleEntry newEntry) {
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

    private void assertPatientDefined(final ScheduleEntry entry) {
        if (entry.getPatient() == null) {
            throw new NoPatientException();
        }
    }

    @NotNull
    private Set<ScheduleEntry> findInterferingDoctorEntries(final ScheduleEntry entry) {
        return entries.stream()
                .filter(onCall -> onCall.getDoctor().equals(entry.getDoctor()))
                .filter(onCall -> onCall.datesInterfereWith$schedule(entry))
                .collect(toSet());
    }

    private void assertDoctorOnCall(final Set<ScheduleEntry> interferingEntries) {
        if (interferingEntries.isEmpty()) {
            throw new NoDoctorOnCallException("No corresponding on call for this doctor");
        }
    }

    private void assertInSameRoom(final Set<ScheduleEntry> interferingEntries, final Room room) {
        Optional<Room> differentRoom = interferingEntries.stream()
                .map(ScheduleEntry::getRoom)
                .filter(interferingRoom -> !interferingRoom.equals(room))
                .findAny();
        if (differentRoom.isPresent()) {
            throw new RoomMismatchException(differentRoom.get());
        }
    }

    private void assertNoVisits(final Set<ScheduleEntry> interferingEntries) {
        Optional<Patient> otherPatient = interferingEntries.stream()
                .filter(ScheduleEntry::isVisit)
                .map(ScheduleEntry::getPatient)
                .findAny();
        if (otherPatient.isPresent()) {
            throw new VisitAlreadyScheduledException(otherPatient.get());
        }
    }
}
