package io.github.mat3e.schedule.domain

sealed class ScheduleException(message: String) : RuntimeException(message)

internal sealed class BusinessScheduleException(description: String) : ScheduleException(description)

internal class DateAlreadyTakenException : BusinessScheduleException("Cannot schedule for a given date. It's already taken")