package io.github.mat3e.schedule.domain;

import java.util.UUID;

public interface ScheduleRepository {
    void findBy(UUID clinicId);

    void save(Schedule schedule);
}
