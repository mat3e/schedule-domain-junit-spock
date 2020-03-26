package io.github.mat3e.schedule.domain;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

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
    @DisplayName("should schedule a new on call")
    void scheduleOnCall_worksAsExpected() {
        assertAll(
                // first schedule = works as a charm!
                () -> assertDoesNotThrow(() -> toTest.scheduleOnCall(exampleOnCall(start, end))),
                // second time? NO WAY, already scheduled
                () -> assertThrows(
                        BusinessScheduleException.class,
                        () -> toTest.scheduleOnCall(exampleOnCall(start, end))
                )
        );
    }

    @Test
    @DisplayName("should throw when scheduling on call for the taken date (all rooms taken)")
    void scheduleOnCall_throwsWhenDateAlreadyTaken() {
        // given
        toTest.scheduleOnCall(exampleOnCall(start, end));

        // when
        var laterStart = start.plusHours(1);

        // then
        assertThrows(
                DateAlreadyTakenException.class,
                () -> toTest.scheduleOnCall(exampleOnCall(laterStart, end))
        );
    }

    @Test
    @DisplayName("should throw when scheduling on call with the taken room")
    void scheduleOnCall_throwsWhenRoomAlreadyTaken() {
        // given
        var room1 = new Room("1");
        // and
        toTest = new Schedule(Set.of(room1, new Room("2")));
        // and
        toTest.scheduleOnCall(new ScheduleEntry(
                exampleSurgeon(),
                start,
                end,
                room1
        ));

        // when
        var e = assertThrows(
                RoomAlreadyTakenException.class,
                () -> toTest.scheduleOnCall(new ScheduleEntry(
                        exampleSurgeon(),
                        start.plusHours(1),
                        start.plusHours(3),
                        room1
                ))
        );

        // then
        assertEquals("Cannot schedule for a room \"1\"", e.getMessage());
    }

    @Test
    @DisplayName("should throw when scheduling on call with patient")
    void scheduleOnCall_throwsWhenPatient() {
        // expect
        assertThrows(
                OnCallWithPatientException.class,
                () -> toTest.scheduleOnCall(exampleVisit(start, end))
        );
    }

    @Test
    @DisplayName("should throw when scheduling a visit with no patient")
    void scheduleNewVisit_throwsWhenNoPatient() {
        // expect
        assertThrows(
                NoPatientException.class,
                () -> toTest.scheduleVisit(exampleOnCall(start, end))
        );
    }

    @Test
    @DisplayName("should throw when scheduling a visit without a previous on call")
    void scheduleNewVisit_throwsWhenNoOnCallSetBefore() {
        // expect
        var e = assertThrows(
                NoDoctorOnCallException.class,
                () -> toTest.scheduleVisit(exampleVisit(start, end))
        );
        assertEquals("No corresponding on call for this doctor", e.getMessage());
    }

    @Test
    @DisplayName("should throw when scheduling a visit with a different room than in on call")
    void scheduleNewVisit_throwsWhenOnCallInDifferentRoom() {
        // given
        var firstRoom = new Room("1");
        var secondRoom = new Room("2");
        // and
        toTest.scheduleOnCall(
                new ScheduleEntry(
                        exampleSurgeon(),
                        start,
                        end,
                        firstRoom
                )
        );

        // when
        var e = assertThrows(
                RoomMismatchException.class,
                () -> toTest.scheduleVisit(
                        new ScheduleEntry(
                                exampleSurgeon(),
                                start,
                                end,
                                secondRoom,
                                examplePatient()
                        )
                )
        );

        // then
        assertEquals("Doctor should be in room " + firstRoom.getName(), e.getMessage());
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("should throw when scheduling a visit interfering with a different visit")
    void scheduleNewVisit_throwsWhenAnotherVisit(String firstName, String secondName) {
        // given
        toTest.scheduleOnCall(exampleOnCall(start, end));
        toTest.scheduleVisit(
                new ScheduleEntry(
                        exampleSurgeon(),
                        start,
                        end,
                        exampleRoom(),
                        new Patient(firstName)
                )
        );

        // when
        var e = assertThrows(
                VisitAlreadyScheduledException.class,
                () -> toTest.scheduleVisit(
                        new ScheduleEntry(
                                exampleSurgeon(),
                                start,
                                end,
                                exampleRoom(),
                                new Patient(secondName)
                        )
                )
        );

        // then
        assertEquals("There are already interfering visits, e.g. for patient " + firstName, e.getMessage());
    }

    static Stream<Arguments> scheduleNewVisit_throwsWhenAnotherVisit() {
        return Stream.of(
                Arguments.of("Jack", "John"),
                Arguments.of("Jack", "Jack")
        );
    }

    @TestFactory
    @DisplayName("should throw when scheduling a visit interfering with an empty slot")
    Stream<DynamicNode> generateIsVisitTests() {
        return Stream.of(
                new EmptySlotTestCase(
                        "15-min slot in the middle of schedule",
                        Set.of(
                                new Dates(start.minusMinutes(30), end.minusMinutes(30)),
                                new Dates(end.minusMinutes(15), end.minusMinutes(15).plusHours(2))
                        )
                ),
                new EmptySlotTestCase(
                        "visit starts before the on call",
                        Set.of(new Dates(start.plusMinutes(15), end))
                ),
                new EmptySlotTestCase(
                        "visit ends after the on call",
                        Set.of(new Dates(start, end.minusMinutes(15)))
                )
        ).map(testCase -> dynamicTest(
                testCase.description,
                () -> {
                    // given
                    init(); // otherwise executed just once
                    testCase.dates.forEach(entry -> toTest.scheduleOnCall(exampleOnCall(entry.start, entry.end)));

                    // expect
                    var e = assertThrows(
                            NoDoctorOnCallException.class,
                            () -> toTest.scheduleVisit(exampleVisit(start, end))
                    );
                    assertEquals("Doctor's on calls are not fully aligned with the visit", e.getMessage());
                })
        );
    }

    @Test
    @DisplayName("should schedule a new visit")
    void scheduleNewVisit_worksAsExpected() {
        // given
        toTest.scheduleOnCall(exampleOnCall(start.minusHours(1), end.plusHours(3)));

        // when
        toTest.scheduleVisit(exampleVisit(start, end));

        // then
        assertArrayEquals(
                Set.of(
                        exampleOnCall(start.minusHours(1), start),
                        exampleVisit(start, end),
                        exampleOnCall(end, end.plusHours(3))
                ).stream()
                        .sorted(comparing(ScheduleEntry::getFrom))
                        .toArray(),
                toTest.getSnapshot().getEntries().stream()
                        .sorted(comparing(ScheduleEntry::getFrom))
                        .toArray()
        );
    }

    @Test
    @DisplayName("should throw when erasing with wrong dates")
    void erase_throwsWhenWrongDates() {
        assertThrows(
                IllegalArgumentException.class,
                () -> toTest.erase(end, start)
        );
    }

    @Test
    @DisplayName("should throw when no entries to be erased")
    void erase_throwsWhenNoEntryCollides() {
        // given
        toTest.scheduleOnCall(exampleOnCall(start, end));
        toTest.scheduleOnCall(exampleOnCall(end.plusHours(2), end.plusHours(4)));

        // expect
        assertThrows(
                NothingToEraseException.class,
                () -> toTest.erase(end, end.plusHours(2))
        );
    }

    @Test
    @DisplayName("should erase entries")
    void erase_worksAsExpected() {
        // given
        toTest.scheduleOnCall(exampleOnCall(start, end));
        toTest.scheduleOnCall(exampleOnCall(end.plusHours(1), end.plusHours(2)));
        toTest.scheduleOnCall(exampleOnCall(end.plusHours(3), end.plusHours(5)));
        toTest.scheduleVisit(exampleVisit(end.plusHours(3), end.plusHours(5)));

        // when
        toTest.erase(end.minusHours(1), end.plusHours(4));
        // and
        List<ScheduleEntry> result = toTest.getSnapshot().getEntries().stream()
                .sorted(comparing(ScheduleEntry::getFrom))
                .collect(toUnmodifiableList());

        // then
        assertEquals(result.get(0), exampleOnCall(start, end.minusHours(1)));
        assertEquals(result.get(1), exampleVisit(end.plusHours(4), end.plusHours(5)));
    }

    @NotNull
    private static ScheduleEntry exampleOnCall(ZonedDateTime from, ZonedDateTime to) {
        return new ScheduleEntry(
                exampleSurgeon(),
                from,
                to,
                exampleRoom()
        );
    }

    @NotNull
    private static ScheduleEntry exampleVisit(ZonedDateTime from, ZonedDateTime to) {
        return new ScheduleEntry(
                exampleSurgeon(),
                from,
                to,
                exampleRoom(),
                examplePatient()
        );
    }

    @NotNull
    private static Doctor exampleSurgeon() {
        return new Doctor(Specialization.SURGEON);
    }

    @NotNull
    private static Room exampleRoom() {
        return new Room("1");
    }

    @NotNull
    private static Patient examplePatient() {
        return new Patient("patient");
    }

    private static class Dates {
        ZonedDateTime start;
        ZonedDateTime end;

        Dates(final ZonedDateTime start, final ZonedDateTime end) {
            this.start = start;
            this.end = end;
        }
    }

    private static class EmptySlotTestCase {
        String description;
        Set<Dates> dates;

        EmptySlotTestCase(final String description, final Set<Dates> dates) {
            this.description = description;
            this.dates = dates;
        }
    }
}