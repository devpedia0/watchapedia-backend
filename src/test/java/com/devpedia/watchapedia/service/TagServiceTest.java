package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Tag;
import com.devpedia.watchapedia.dto.TagDto;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.repository.tag.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @InjectMocks
    private TagService tagService;

    @Mock
    private TagRepository tagRepository;

    @Test
    public void addTag_Correct_SaveTag() throws Exception {
        // given
        TagDto.TagInsertRequest request = TagDto.TagInsertRequest.builder()
                .description("tag1")
                .build();

        // when
        tagService.addTag(request);

        // then
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    public void delete_ExistTag_Delete() throws Exception {
        // given
        Tag tag = Tag.builder()
                .description("tag1")
                .build();

        given(tagRepository.findById(anyLong()))
                .willReturn(Optional.of(tag));

        // when
        tagService.delete(1L);

        // then
        verify(tagRepository, times(1)).delete(any(Tag.class));
    }

    @Test
    public void delete_NotExistTag_ThrowException() throws Exception {
        // given
        given(tagRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> tagService.delete(1L));

        // then
        assertThat(throwable).isInstanceOf(EntityNotExistException.class);
    }

    @Test
    public void searchWithPaging_ResultExist_ReturnList() throws Exception {
        // given
        Tag tag1 = Tag.builder()
                .description("tag1")
                .build();

        Tag tag2 = Tag.builder()
                .description("tag2")
                .build();

        Tag tag3 = Tag.builder()
                .description("tag3")
                .build();

        List<Tag> tags = Arrays.asList(tag1, tag2, tag3);

        given(tagRepository.findByDescriptionContaining(anyString(), any(Pageable.class)))
                .willReturn(tags);

        // when
        List<TagDto.TagInfo> tagInfos = tagService.searchWithPaging("tag", PageRequest.of(0, 10));

        // then
        assertThat(tagInfos).hasSize(3);
    }

    @Test
    public void searchWithPaging_ResultNotExist_ReturnEmptyList() throws Exception {
        // given
        given(tagRepository.findByDescriptionContaining(anyString(), any(Pageable.class)))
                .willReturn(new ArrayList<>());

        // when
        List<TagDto.TagInfo> tagInfos = tagService.searchWithPaging("tag", PageRequest.of(0, 10));

        // then
        assertThat(tagInfos).hasSize(0);
    }
}