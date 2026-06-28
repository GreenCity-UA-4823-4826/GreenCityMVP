package greencity.dto.event;

import greencity.annotations.ValidEventDate;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@ValidEventDate
@Getter
@Setter
public class EventDateDto {

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date cannot be in the past")
    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private boolean allDay;
}