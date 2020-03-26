package io.github.mat3e.schedule.domain

sealed class ScheduleException(message: String) : RuntimeException(message)

internal sealed class BusinessScheduleException(description: String) : ScheduleException(description)

internal class DateAlreadyTakenException : BusinessScheduleException("Cannot schedule for a given date. All the rooms taken")

internal class RoomAlreadyTakenException(takenRoom: Room) : BusinessScheduleException("Cannot schedule for a room \"${takenRoom.name}\"")

internal class OnCallWithPatientException : BusinessScheduleException("Cannot schedule on call with patient")

internal class NoPatientException : BusinessScheduleException("Cannot schedule a visit without a patient")

internal class NoDoctorOnCallException(message: String) : BusinessScheduleException(message)

internal class RoomMismatchException(onCallRoom: Room) : BusinessScheduleException("Doctor should be in room ${onCallRoom.name}")

internal class VisitAlreadyScheduledException(existingPatient: Patient) : BusinessScheduleException("There are already interfering visits, e.g. for patient ${existingPatient.name}")

internal class NothingToEraseException : BusinessScheduleException("There are no entries to erase")
