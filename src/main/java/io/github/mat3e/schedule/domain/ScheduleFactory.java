package io.github.mat3e.schedule.domain;

import java.util.Collection;

class ScheduleFactory {
    private final RoomRepository roomRepository;

    ScheduleFactory(final RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    Schedule create() {
        return new Schedule(roomRepository.findAll());
    }

    Schedule restore(Collection<ScheduleEntry> entries) {
        var result = new Schedule(roomRepository.findAll());
        entries.forEach(result::scheduleOnCall);
        return result;
    }
}
