package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.dto.shoppinglistitem.BulkSaveCustomShoppingListItemDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemSaveRequestDto;
import greencity.enums.ShoppingListItemStatus;
import greencity.service.CustomShoppingListItemService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomShoppingListItemControllerTest {
    private static final String BASE_URL = "/custom/shopping-list-items";

    private MockMvc mockMvc;

    @Mock
    private CustomShoppingListItemService customShoppingListItemService;

    @InjectMocks
    private CustomShoppingListItemController customShoppingListItemController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(customShoppingListItemController)
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getAllAvailableCustomShoppingListItems_ValidUserIdAndHabitId_ReturnsOk() throws Exception {
        Long userId = 1L;
        Long habitId = 2L;
        CustomShoppingListItemResponseDto expectedItem = ModelUtils.getCustomShoppingListItemResponseDto();

        when(customShoppingListItemService.findAllAvailableCustomShoppingListItems(userId, habitId))
                .thenReturn(List.of(expectedItem));

        mockMvc.perform(get(BASE_URL + "/{userId}/{habitId}", userId, habitId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(expectedItem.getId()))
                .andExpect(jsonPath("$[0].text").value(expectedItem.getText()))
                .andExpect(jsonPath("$[0].status").value(expectedItem.getStatus().name()));

        verify(customShoppingListItemService, times(1))
                .findAllAvailableCustomShoppingListItems(userId, habitId);
        verifyNoMoreInteractions(customShoppingListItemService);
    }

    @Test
    void getAllAvailableCustomShoppingListItems_InvalidUserId_ReturnsBadRequest() throws Exception {
        Long habitId = 2L;

        mockMvc.perform(get(BASE_URL + "/{userId}/{habitId}", "abc", habitId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    void getAllAvailableCustomShoppingListItems_InvalidHabitId_ReturnsBadRequest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get(BASE_URL + "/{userId}/{habitId}", userId, "abc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    void saveUserCustomShoppingListItems_ValidRequestBody_ReturnsCreated() throws Exception {
        Long userId = 1L;
        Long habitAssignId = 10L;
        String itemText = "Buy vegetables";

        CustomShoppingListItemSaveRequestDto itemDto =
                CustomShoppingListItemSaveRequestDto.builder()
                        .text(itemText)
                        .build();

        BulkSaveCustomShoppingListItemDto requestDto =
                new BulkSaveCustomShoppingListItemDto(List.of(itemDto));

        CustomShoppingListItemResponseDto expectedItem =
                ModelUtils.getCustomShoppingListItemResponseDto();

        when(customShoppingListItemService.save(
                any(BulkSaveCustomShoppingListItemDto.class), eq(userId), eq(habitAssignId)))
                .thenReturn(List.of(expectedItem));

        mockMvc.perform(post(BASE_URL + "/{userId}/{habitAssignId}/custom-shopping-list-items", userId, habitAssignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(expectedItem.getId()))
                .andExpect(jsonPath("$[0].text").value(expectedItem.getText()))
                .andExpect(jsonPath("$[0].status").value(expectedItem.getStatus().name()));

        ArgumentCaptor<BulkSaveCustomShoppingListItemDto> requestCaptor =
                ArgumentCaptor.forClass(BulkSaveCustomShoppingListItemDto.class);

        verify(customShoppingListItemService, times(1))
                .save(requestCaptor.capture(), eq(userId), eq(habitAssignId));

        BulkSaveCustomShoppingListItemDto capturedRequestDto = requestCaptor.getValue();

        assertEquals(1, capturedRequestDto.getCustomShoppingListItemSaveRequestDtoList().size());
        assertEquals(itemText, capturedRequestDto.getCustomShoppingListItemSaveRequestDtoList().get(0).getText());

        verifyNoMoreInteractions(customShoppingListItemService);
    }

    @Test
    void saveUserCustomShoppingListItems_MissingRequestBody_ReturnsBadRequest() throws Exception {
        Long userId = 1L;
        Long habitAssignId = 10L;

        mockMvc.perform(post(BASE_URL + "/{userId}/{habitAssignId}/custom-shopping-list-items", userId, habitAssignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    void saveUserCustomShoppingListItems_BlankText_ReturnsBadRequest() throws Exception {
        Long userId = 1L;
        Long habitAssignId = 10L;

        CustomShoppingListItemSaveRequestDto itemDto =
                CustomShoppingListItemSaveRequestDto.builder()
                        .text("")
                        .build();

        BulkSaveCustomShoppingListItemDto requestDto =
                new BulkSaveCustomShoppingListItemDto(List.of(itemDto));

        mockMvc.perform(post(BASE_URL + "/{userId}/{habitAssignId}/custom-shopping-list-items", userId, habitAssignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    void saveUserCustomShoppingListItems_InvalidUserId_ReturnsBadRequest() throws Exception {
        Long habitAssignId = 10L;

        CustomShoppingListItemSaveRequestDto itemDto =
                CustomShoppingListItemSaveRequestDto.builder()
                        .text("Buy vegetables")
                        .build();

        BulkSaveCustomShoppingListItemDto requestDto =
                new BulkSaveCustomShoppingListItemDto(List.of(itemDto));

        mockMvc.perform(post(BASE_URL + "/{userId}/{habitAssignId}/custom-shopping-list-items", "abc", habitAssignId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    void saveUserCustomShoppingListItems_InvalidHabitAssignId_ReturnsBadRequest() throws Exception {
        Long userId = 1L;

        CustomShoppingListItemSaveRequestDto itemDto =
                CustomShoppingListItemSaveRequestDto.builder()
                        .text("Buy vegetables")
                        .build();

        BulkSaveCustomShoppingListItemDto requestDto =
                new BulkSaveCustomShoppingListItemDto(List.of(itemDto));

        mockMvc.perform(post(BASE_URL + "/{userId}/{habitAssignId}/custom-shopping-list-items", userId, "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @ParameterizedTest
    @EnumSource(value = ShoppingListItemStatus.class, names = {"ACTIVE", "DONE", "INPROGRESS", "DISABLED"})
    void updateItemStatus_ValidItemIdAndStatus_ReturnsOk(ShoppingListItemStatus itemStatus) throws Exception {
        Long userId = 1L;
        Long itemId = 5L;

        CustomShoppingListItemResponseDto expectedItem = CustomShoppingListItemResponseDto.builder()
                .id(itemId)
                .text("Buy vegetables")
                .status(itemStatus)
                .build();

        when(customShoppingListItemService.updateItemStatus(userId, itemId, itemStatus.name()))
                .thenReturn(expectedItem);

        mockMvc.perform(patch(BASE_URL + "/{userId}/custom-shopping-list-items", userId)
                        .param("itemId", itemId.toString())
                        .param("status", itemStatus.name())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedItem.getId()))
                .andExpect(jsonPath("$.text").value(expectedItem.getText()))
                .andExpect(jsonPath("$.status").value(itemStatus.name()));

        verify(customShoppingListItemService, times(1))
                .updateItemStatus(userId, itemId, itemStatus.name());
        verifyNoMoreInteractions(customShoppingListItemService);
    }

    @Test
    void updateItemStatus_MissingItemId_ReturnsBadRequest() throws Exception {
        Long userId = 1L;
        String itemStatus = ShoppingListItemStatus.DONE.name();

        mockMvc.perform(patch(BASE_URL + "/{userId}/custom-shopping-list-items", userId)
                        .param("status", itemStatus)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    void updateItemStatus_MissingStatus_ReturnsBadRequest() throws Exception {
        Long userId = 1L;
        Long itemId = 5L;

        mockMvc.perform(patch(BASE_URL + "/{userId}/custom-shopping-list-items", userId)
                        .param("itemId", itemId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    void updateItemStatus_InvalidItemId_ReturnsBadRequest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(patch(BASE_URL + "/{userId}/custom-shopping-list-items", userId)
                        .param("itemId", "abc")
                        .param("status", ShoppingListItemStatus.DONE.name())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    void updateItemStatusToDone_ValidItemId_ReturnsOk() throws Exception {
        Long userId = 1L;
        Long itemId = 5L;

        mockMvc.perform(patch(BASE_URL + "/{userId}/done", userId)
                        .param("itemId", itemId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(customShoppingListItemService, times(1))
                .updateItemStatusToDone(userId, itemId);
        verifyNoMoreInteractions(customShoppingListItemService);
    }

    @Test
    void updateItemStatusToDone_InvalidItemId_ReturnsBadRequest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(patch(BASE_URL + "/{userId}/done", userId)
                        .param("itemId", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    void updateItemStatusToDone_MissingItemId_ReturnsBadRequest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(patch(BASE_URL + "/{userId}/done", userId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    void bulkDeleteCustomShoppingListItems_ValidIds_ReturnsOk() throws Exception {
        Long userId = 1L;
        String ids = "1,2,3";
        List<Long> deletedIds = List.of(1L, 2L, 3L);

        when(customShoppingListItemService.bulkDelete(ids))
                .thenReturn(deletedIds);

        mockMvc.perform(delete(BASE_URL + "/{userId}/custom-shopping-list-items", userId)
                        .param("ids", ids)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]").value(deletedIds.get(0)))
                .andExpect(jsonPath("$[1]").value(deletedIds.get(1)))
                .andExpect(jsonPath("$[2]").value(deletedIds.get(2)));

        verify(customShoppingListItemService, times(1)).bulkDelete(ids);
        verifyNoMoreInteractions(customShoppingListItemService);
    }

    @Test
    void bulkDeleteCustomShoppingListItems_MissingIds_ReturnsBadRequest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete(BASE_URL + "/{userId}/custom-shopping-list-items", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @ParameterizedTest
    @EnumSource(value = ShoppingListItemStatus.class, names = {"ACTIVE", "DONE", "INPROGRESS", "DISABLED"})
    void getAllCustomShoppingItemsByStatus_WithStatus_ReturnsOk(ShoppingListItemStatus itemStatus) throws Exception {
        Long userId = 1L;

        CustomShoppingListItemResponseDto expectedItem = CustomShoppingListItemResponseDto.builder()
                .id(1L)
                .text("Buy vegetables")
                .status(itemStatus)
                .build();

        when(customShoppingListItemService
                .findAllUsersCustomShoppingListItemsByStatus(userId, itemStatus.name()))
                .thenReturn(List.of(expectedItem));

        mockMvc.perform(get(BASE_URL + "/{userId}/custom-shopping-list-items", userId)
                        .param("status", itemStatus.name())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(expectedItem.getId()))
                .andExpect(jsonPath("$[0].text").value(expectedItem.getText()))
                .andExpect(jsonPath("$[0].status").value(itemStatus.name()));

        verify(customShoppingListItemService, times(1))
                .findAllUsersCustomShoppingListItemsByStatus(userId, itemStatus.name());
        verifyNoMoreInteractions(customShoppingListItemService);
    }

    @Test
    void getAllCustomShoppingItemsByStatus_WithoutStatus_ReturnsOk() throws Exception {
        Long userId = 1L;

        CustomShoppingListItemResponseDto expectedItem =
                ModelUtils.getCustomShoppingListItemResponseDto();

        when(customShoppingListItemService
                .findAllUsersCustomShoppingListItemsByStatus(eq(userId), isNull()))
                .thenReturn(List.of(expectedItem));

        mockMvc.perform(get(BASE_URL + "/{userId}/custom-shopping-list-items", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(expectedItem.getId()))
                .andExpect(jsonPath("$[0].text").value(expectedItem.getText()))
                .andExpect(jsonPath("$[0].status").value(expectedItem.getStatus().name()));

        verify(customShoppingListItemService, times(1))
                .findAllUsersCustomShoppingListItemsByStatus(eq(userId), isNull());
        verifyNoMoreInteractions(customShoppingListItemService);
    }

    @Test
    void getAllCustomShoppingItemsByStatus_InvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{userId}/custom-shopping-list-items", "abc")
                        .param("status", ShoppingListItemStatus.ACTIVE.name())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }
}
