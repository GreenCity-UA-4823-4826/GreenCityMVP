package greencity.validator;

import greencity.annotations.ValidEventLocation;
import greencity.dto.event.EventCreateRequestDto;
import greencity.enums.event.EventType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EventLocationValidator implements ConstraintValidator<ValidEventLocation, EventCreateRequestDto> {
    @Override
    public boolean isValid(EventCreateRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getEventTypes() == null) {
            return true;
        }

        boolean isPlace = dto.getEventTypes().contains(EventType.PLACE);
        boolean isOnline = dto.getEventTypes().contains(EventType.ONLINE);

        boolean isValid = true;

        if (isPlace && (dto.getLocation() == null || dto.getLocation().isBlank())) {
            context.buildConstraintViolationWithTemplate("Location is required when PLACE is selected")
                .addPropertyNode("location")
                .addConstraintViolation();
            isValid = false;
        }

        if (isOnline && (dto.getOnlineLink() == null || dto.getOnlineLink().isBlank())) {
            context.buildConstraintViolationWithTemplate("Online link is required when ONLINE is selected")
                .addPropertyNode("onlineLink")
                .addConstraintViolation();
            isValid = false;
        }

        boolean hasLat = dto.getLatitude() != null;
        boolean hasLng = dto.getLongitude() != null;
        if (hasLat != hasLng) {
            context.buildConstraintViolationWithTemplate("Both latitude and longitude must be provided together")
                .addPropertyNode("latitude")
                .addConstraintViolation();
            isValid = false;
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
        }

        return isValid;
    }
}