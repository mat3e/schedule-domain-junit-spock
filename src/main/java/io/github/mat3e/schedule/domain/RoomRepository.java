package io.github.mat3e.schedule.domain;

import java.util.List;

public interface RoomRepository {
    List<Room> findAll();
}
