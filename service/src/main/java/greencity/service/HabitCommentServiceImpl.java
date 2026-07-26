package greencity.service;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.habitcomment.AddHabitCommentDtoRequest;
import greencity.dto.habitcomment.AddHabitCommentDtoResponse;
import greencity.dto.habitcomment.HabitCommentAuthorDto;
import greencity.dto.habitcomment.HabitCommentDto;
import greencity.dto.user.UserVO;
import greencity.entity.Habit;
import greencity.entity.HabitComment;
import greencity.entity.HabitTranslation;
import greencity.entity.User;
import greencity.enums.Role;
import greencity.event.HabitCommentNotificationEvent;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.HabitCommentRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitTranslationRepo;
import greencity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitCommentServiceImpl implements HabitCommentService {
    private final HabitCommentRepo habitCommentRepo;
    private final HabitRepo habitRepo;
    private final HabitTranslationRepo habitTranslationRepo;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AddHabitCommentDtoResponse save(Long habitId, AddHabitCommentDtoRequest addHabitCommentDtoRequest,
        UserVO userVO) {
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
        User user = userRepo.findById(userVO.getId())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userVO.getId()));

        HabitComment comment = HabitComment.builder()
            .text(addHabitCommentDtoRequest.getText())
            .user(user)
            .habit(habit)
            .build();
        HabitComment savedComment = habitCommentRepo.save(comment);

        Long ownerId = habit.getUserId();
        if (ownerId != null && !userVO.getId().equals(ownerId)) {
            User owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + ownerId));
            eventPublisher.publishEvent(HabitCommentNotificationEvent.builder()
                .owner(modelMapper.map(owner, UserVO.class))
                .commenter(userVO)
                .habitId(habitId)
                .habitName(findHabitName(habit))
                .build());
        }

        return AddHabitCommentDtoResponse.builder()
            .id(savedComment.getId())
            .text(savedComment.getText())
            .createdDate(savedComment.getCreatedDate())
            .author(HabitCommentAuthorDto.builder()
                .id(user.getId())
                .name(user.getName())
                .userProfilePicturePath(user.getProfilePicturePath())
                .build())
            .build();
    }

    @Override
    public PageableDto<HabitCommentDto> findAllComments(Pageable pageable, Long habitId) {
        if (!habitRepo.existsById(habitId)) {
            throw new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId);
        }
        Page<HabitComment> comments = habitCommentRepo.findAllByHabitIdOrderByCreatedDateDesc(habitId, pageable);
        List<HabitCommentDto> dtoList = comments.stream()
            .map(comment -> modelMapper.map(comment, HabitCommentDto.class))
            .toList();
        return new PageableDto<>(
            dtoList,
            comments.getTotalElements(),
            comments.getPageable().getPageNumber(),
            comments.getTotalPages());
    }

    @Override
    @Transactional
    public void deleteById(Long id, UserVO userVO) {
        HabitComment comment = habitCommentRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COMMENT_NOT_FOUND_EXCEPTION));

        if (userVO.getRole() != Role.ROLE_ADMIN && !userVO.getId().equals(comment.getUser().getId())) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
        habitCommentRepo.delete(comment);
    }

    private String findHabitName(Habit habit) {
        return habitTranslationRepo.findByHabitAndLanguageCode(habit, AppConstant.DEFAULT_LANGUAGE_CODE)
            .map(HabitTranslation::getName)
            .orElse(null);
    }
}
