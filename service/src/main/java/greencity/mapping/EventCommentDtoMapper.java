package greencity.mapping;

import greencity.dto.eventcomment.EventCommentAuthorDto;
import greencity.dto.eventcomment.EventCommentDto;
import greencity.entity.event.EventComment;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class EventCommentDtoMapper extends AbstractConverter<EventComment, EventCommentDto> {
    @Override
    protected EventCommentDto convert(EventComment eventComment) {
        EventCommentDto dto = new EventCommentDto();
        dto.setId(eventComment.getId());
        dto.setCreatedDate(eventComment.getCreatedDate());
        dto.setText(eventComment.getText());
        dto.setAuthor(EventCommentAuthorDto.builder()
            .id(eventComment.getUser().getId())
            .name(eventComment.getUser().getName())
            .userProfilePicturePath(eventComment.getUser().getProfilePicturePath())
            .build());
        return dto;
    }
}
