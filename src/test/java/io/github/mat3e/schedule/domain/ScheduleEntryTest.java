package io.github.mat3e.schedule.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.ZonedDateTime;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Tag("unit")
class ScheduleEntryTest {
    private static final ZonedDateTime start = ZonedDateTime.now();
    private static final ZonedDateTime end = start.plusHours(2);

    private ScheduleEntry entry = exampleEntry(start, end);

    @Test
    @DisplayName("should interfere when the same start")
    void interferesWith_itself() {
        assertTrue(entry.interferesWith(entry));
    }

    @Test
    @DisplayName("should NOT interfere when new start equal to old end")
    void not_interferesWith_otherWhichStartsOnThisEnds() {
        assertFalse(entry.interferesWith(exampleEntry(end, end.plusHours(2))));
    }

    @Test
    @DisplayName("should NOT interfere when new end equal to old start")
    void not_interferesWith_otherWhichEndsOnThisStart() {
        assertFalse(entry.interferesWith(exampleEntry(start.minusHours(2), start)));
    }

    @Test
    @DisplayName("should NOT interfere when different room")
    void not_interferesWith_otherInDifferentRoom() {
        assertFalse(entry.interferesWith(new ScheduleEntry(entry.getDoctor(), start, end, new Room("different"))));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource
    @DisplayName("should interfere when start between an existing start and end")
    void interferesWith_otherWhichStartsBeforeThisEnds(ZonedDateTime startBetween) {
        // given
        ZonedDateTime lateEnd = startBetween.plusHours(2);

        // expect
        assertTrue(entry.interferesWith(exampleEntry(startBetween, lateEnd)));
    }

    static Stream<String> interferesWith_otherWhichStartsBeforeThisEnds() {
        return Stream.of(
                start.plusSeconds(1).toString(),
                start.plusHours(1).toString(),
                end.minusSeconds(1).toString()
        );
    }

    @ParameterizedTest
    @MethodSource("endsBetween")
    @DisplayName("should interfere when end between an existing start and end")
    void interferesWith_otherWhichEndsBeforeThisEnds(ZonedDateTime endBetween) {
        // given
        ZonedDateTime newStart = endBetween.minusHours(2);

        // expect
        assertTrue(entry.interferesWith(exampleEntry(newStart, endBetween)));
    }

    static Stream<Arguments> endsBetween() {
        return Stream.of(
                end.minusSeconds(1),
                start.plusSeconds(1),
                start.plusHours(1)
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3600})
    @DisplayName("should throw when creating with start less than end")
    void constructor_disallowStartAfterEnd(int seconds) {
        assertThrows(
                IllegalArgumentException.class,
                () -> exampleEntry(end.plusSeconds(seconds), end)
        );
    }

    @TestFactory
    @DisplayName("is identified by its values")
    Stream<DynamicNode> generateEqualityTests() {
        return Stream.<Supplier<ScheduleEntry>>of(
                () -> new ScheduleEntry(
                        new Doctor(Specialization.SURGEON),
                        start,
                        end,
                        new Room("example"),
                        new Patient("Frank")
                ), () -> new ScheduleEntry(
                        new Doctor(Specialization.SURGEON),
                        start,
                        end,
                        new Room("example")
                )
        ).map(factory ->
                dynamicTest(
                        factory.get().getPatient() != null ? "with patient" : "without patient",
                        () -> assertEquals(factory.get(), factory.get())
                )
        );
    }

    @TestFactory
    @DisplayName("can be a visit")
    Stream<DynamicNode> generateIsVisitTests() {
        return Stream.of(
                new Patient("p"),
                null
        ).map(potentialPatient -> {
                    boolean expected = potentialPatient != null;
                    return dynamicTest(
                            "should" + (expected ? " " : " NOT ") + "be a visit",
                            () -> {
                                // given
                                var entry = new ScheduleEntry(
                                        exampleSurgeon(),
                                        start,
                                        end,
                                        new Room("123"),
                                        potentialPatient
                                );

                                // when
                                boolean result = entry.isVisit();

                                // then
                                assertEquals(expected, result);
                            }
                    );
                }
        );
    }

    private static ScheduleEntry exampleEntry(ZonedDateTime from, ZonedDateTime to) {
        return new ScheduleEntry(
                exampleSurgeon(),
                from,
                to,
                new Room("foo")
        );
    }

    private static Doctor exampleSurgeon() {
        return new Doctor(Specialization.SURGEON);
    }
}
