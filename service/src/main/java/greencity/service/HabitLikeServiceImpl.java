package greencity.service;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.habitlike.HabitLikeDtoResponse;
import greencity.dto.user.UserVO;
import greencity.entity.Habit;
import greencity.entity.HabitLike;
import greencity.entity.HabitTranslation;
import greencity.entity.User;
import greencity.event.HabitLikeNotificationEvent;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.HabitLikeRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitTranslationRepo;
import greencity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HabitLikeServiceImpl implements HabitLikeService {
    private final HabitLikeRepo habitLikeRepo;
    private final HabitRepo habitRepo;
    private final HabitTranslationRepo habitTranslationRepo;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public HabitLikeDtoResponse like(Long habitId, UserVO userVO) {
        Habit habit = findHabit(habitId);
        if (!habitLikeRepo.existsByHabitIdAndUserId(habitId, userVO.getId())) {
            User user = userRepo.findById(userVO.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userVO.getId()));
            habitLikeRepo.save(HabitLike.builder()
                .habit(habit)
                .user(user)
                .build());
            publishNotification(habit, habitId, userVO, true);
        }
        return buildResponse(habitId, userVO.getId());
    }

    @Override
    @Transactional
    public HabitLikeDtoResponse unlike(Long habitId, UserVO userVO) {
        Habit habit = findHabit(habitId);
        habitLikeRepo.findByHabitIdAndUserId(habitId, userVO.getId()).ifPresent(like -> {
            habitLikeRepo.delete(like);
            publishNotification(habit, habitId, userVO, false);
        });
        return buildResponse(habitId, userVO.getId());
    }

    private Habit findHabit(Long habitId) {
        return habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
    }

    private void publishNotification(Habit habit, Long habitId, UserVO userVO, boolean liked) {
        Long ownerId = habit.getUserId();
        if (ownerId == null || userVO.getId().equals(ownerId)) {
            return;
        }
        User owner = userRepo.findById(ownerId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + ownerId));
        eventPublisher.publishEvent(HabitLikeNotificationEvent.builder()
            .owner(modelMapper.map(owner, UserVO.class))
            .liker(userVO)
            .habitId(habitId)
            .habitName(findHabitName(habit))
            .liked(liked)
            .build());
    }

    private String findHabitName(Habit habit) {
        return habitTranslationRepo.findByHabitAndLanguageCode(habit, AppConstant.DEFAULT_LANGUAGE_CODE)
            .map(HabitTranslation::getName)
            .orElse(null);
    }

    private HabitLikeDtoResponse buildResponse(Long habitId, Long userId) {
        return HabitLikeDtoResponse.builder()
            .habitId(habitId)
            .likesCount(habitLikeRepo.countByHabitId(habitId))
            .liked(habitLikeRepo.existsByHabitIdAndUserId(habitId, userId))
            .build();
    }
}
