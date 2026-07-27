package greencity.mapping;

import greencity.dto.habitcomment.HabitCommentAuthorDto;
import greencity.dto.habitcomment.HabitCommentDto;
import greencity.entity.HabitComment;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class HabitCommentDtoMapper extends AbstractConverter<HabitComment, HabitCommentDto> {
    @Override
    protected HabitCommentDto convert(HabitComment habitComment) {
        HabitCommentDto dto = new HabitCommentDto();
        dto.setId(habitComment.getId());
        dto.setCreatedDate(habitComment.getCreatedDate());
        dto.setText(habitComment.getText());
        dto.setAuthor(HabitCommentAuthorDto.builder()
            .id(habitComment.getUser().getId())
            .name(habitComment.getUser().getName())
            .userProfilePicturePath(habitComment.getUser().getProfilePicturePath())
            .build());
        return dto;
    }
}
