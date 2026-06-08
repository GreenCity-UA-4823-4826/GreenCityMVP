package greencity.mapping;

import greencity.dto.tag.TagDto;
import greencity.entity.Tag;
import greencity.enums.TagType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsMapperTest {

    @Test
    void mapTest() {
        Tag tag = Tag.builder().id(1L).type(TagType.ECO_NEWS).build();

        TagDto result = UtilsMapper.map(tag, TagDto.class);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void mapAllToListTest() {
        Tag tag1 = Tag.builder().id(1L).type(TagType.ECO_NEWS).build();
        Tag tag2 = Tag.builder().id(2L).type(TagType.HABIT).build();

        List<TagDto> result = UtilsMapper.mapAllToList(Arrays.asList(tag1, tag2), TagDto.class);

        assertEquals(2, result.size());
    }

    @Test
    void mapAllToListWithEmptyListTest() {
        List<TagDto> result = UtilsMapper.mapAllToList(Collections.emptyList(), TagDto.class);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapAllToSetTest() {
        Tag tag1 = Tag.builder().id(1L).type(TagType.ECO_NEWS).build();
        Tag tag2 = Tag.builder().id(2L).type(TagType.HABIT).build();

        Set<TagDto> result = UtilsMapper.mapAllToSet(Arrays.asList(tag1, tag2), TagDto.class);

        assertEquals(2, result.size());
    }

    @Test
    void mapAllToSetWithEmptyListTest() {
        Set<TagDto> result = UtilsMapper.mapAllToSet(Collections.emptyList(), TagDto.class);
        assertTrue(result.isEmpty());
    }
}
