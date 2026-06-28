package greencity.validator;

import greencity.annotations.ValidEventDate;
import greencity.dto.event.EventDateDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventDateValidator implements ConstraintValidator<ValidEventDate, EventDateDto> {

    @Override
    public boolean isValid(EventDateDto dto, ConstraintValidatorContext context) {
        boolean isValid = true;

        if (!dto.isAllDay()) {
            if (dto.getStartTime() == null) {
                context.buildConstraintViolationWithTemplate("Start time is required when not all-day")
                        .addPropertyNode("startTime")
                        .addConstraintViolation();
                isValid = false;
            }

            if (dto.getEndTime() == null) {
                context.buildConstraintViolationWithTemplate("End time is required when not all-day")
                        .addPropertyNode("endTime")
                        .addConstraintViolation();
                isValid = false;
            }

            if (dto.getStartTime() != null && dto.getEndTime() != null
                    && !dto.getStartTime().isBefore(dto.getEndTime())) {
                context.buildConstraintViolationWithTemplate("Start time must be before end time")
                        .addPropertyNode("startTime")
                        .addConstraintViolation();
                isValid = false;
            }

            if (dto.getDate() != null && dto.getDate().isEqual(LocalDate.now())
                    && dto.getStartTime() != null && dto.getStartTime().isBefore(LocalTime.now())) {
                context.buildConstraintViolationWithTemplate("Start time cannot be in the past for today's date")
                        .addPropertyNode("startTime")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
        }

        return isValid;
    }
}