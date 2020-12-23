package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.builder.CollectionMother;
import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.domain.Movie;
import com.devpedia.watchapedia.domain.Tag;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.MovieDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.repository.collection.CollectionRepository;
import com.devpedia.watchapedia.repository.tag.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @InjectMocks
    private MovieService movieService;

    @Mock
    private ContentService contentService;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private CollectionRepository collectionRepository;

    @Test
    public void saveWithImage_CorrectInput_Save() throws Exception {
        // given
        MovieDto.MovieInsertRequest request = MovieDto.MovieInsertRequest.builder()
                .bookRate(0.0)
                .category("cate")
                .countryCode("KR")
                .description("desc")
                .isNetflixContent(true)
                .isWatchaContent(false)
                .mainTitle("main")
                .originTitle("origin")
                .productionDate(LocalDate.of(2020, 1, 1))
                .runningTimeInMinutes(100)
                .totalAudience(1000L)
                .roles(new ArrayList<>())
                .tags(new ArrayList<>())
                .build();

        MockMultipartFile file = new MockMultipartFile("poster", new byte[100]);

        // when
        movieService.saveWithImage(request, file, List.of(file));

        // then
        verify(contentService, times(1))
                .createContent(any(Movie.class), any(MultipartFile.class), any(ContentDto.ContentChildren.class));
    }

    @Test
    public void getTagList_CorrectInput_Save() throws Exception {
        // given
        Tag tag = Tag.builder().description("tag").build();

        ContentDto.MainListItem item = ContentDto.MainListItem.builder()
                .id(1L)
                .isNetflixContent(true)
                .isWatchaContent(false)
                .mainTitle("main")
                .posterImagePath("/path")
                .score(3.0)
                .build();

        ContentDto.MainList list = ContentDto.MainList.builder()
                .title("title")
                .type("tag")
                .list(List.of(item))
                .build();

        given(tagRepository.findByRandom(any()))
                .willReturn(List.of(tag));

        given(contentService.getTagList(eq(ContentTypeParameter.MOVIES), any(Tag.class), anyInt()))
                .willReturn(list);

        // when
        List<ContentDto.MainList> tagList = movieService.getTagList();

        // then
        assertThat(tagList).hasSize(1);
        assertThat(tagList.get(0)).isEqualTo(list);
    }

    @Test
    public void getCollectionList_CorrectInput_Save() throws Exception {
        // given
        Collection collection = CollectionMother.defaultCollection(null).build();

        ContentDto.MainListItem item = ContentDto.MainListItem.builder()
                .id(1L)
                .isNetflixContent(true)
                .isWatchaContent(false)
                .mainTitle("main")
                .posterImagePath("/path")
                .score(3.0)
                .build();

        ContentDto.MainListForCollection list = ContentDto.MainListForCollection.builder()
                .title("title")
                .type("tag")
                .userId(1L)
                .collectionId(1L)
                .subtitle("sub")
                .list(List.of(item))
                .build();

        given(collectionRepository.getRandom(eq(ContentTypeParameter.MOVIES), anyInt()))
                .willReturn(List.of(collection));

        given(contentService.getCollectionList(any(Collection.class), anyInt()))
                .willReturn(list);

        // when
        List<ContentDto.MainListForCollection> tagList = movieService.getCollectionList();

        // then
        assertThat(tagList).hasSize(1);
        assertThat(tagList.get(0)).isEqualTo(list);
    }
}