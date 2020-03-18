package io.github.mat3e.schedule.domain;

class ScheduleFactory {
    private final RoomRepository roomRepository;

    ScheduleFactory(final RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    Schedule create() {
        return new Schedule(roomRepository.findAll());
    }

    Schedule restore(ScheduleSnapshot snapshot) {
        var result = new Schedule(snapshot.getClinicId(), roomRepository.findAll());
        snapshot.getEntries().forEach(result::scheduleOnCall);
        return result;
    }
}
