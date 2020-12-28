package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.builder.CollectionMother;
import com.devpedia.watchapedia.builder.ContentMother;
import com.devpedia.watchapedia.builder.ParticipantMother;
import com.devpedia.watchapedia.builder.UserMother;
import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.ParticipantDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.InvalidFileException;
import com.devpedia.watchapedia.repository.ElasticSearchRepository;
import com.devpedia.watchapedia.repository.collection.CollectionRepository;
import com.devpedia.watchapedia.repository.content.ContentRepository;
import com.devpedia.watchapedia.repository.participant.ParticipantRepository;
import com.devpedia.watchapedia.repository.tag.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @InjectMocks
    private ContentService contentService;

    @Mock
    private S3Service s3Service;
    @Mock
    private UserService userService;
    @Mock
    private ContentRepository contentRepository;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private ElasticSearchRepository searchRepository;

    @Test
    public void createContent_WithoutChildren_Save() throws Exception {
        // given
        Content content = mock(Content.class);
        MockMultipartFile poster = new MockMultipartFile("poster", "originName.jpg", "image/jpg", new byte[100]);

        ContentDto.ContentChildren children = ContentDto.ContentChildren.builder()
                .tags(new ArrayList<>())
                .roles(new ArrayList<>())
                .gallery(new ArrayList<>())
                .build();

        // when
        contentService.createContent(content, poster, children);

        // then
        verify(content, times(1)).setPosterImage(any(Image.class));
        verify(contentRepository, times(1)).save(content);
    }

    @Test
    public void createContent_InvalidPoster_ThrowException() throws Exception {
        // given
        Content content = mock(Content.class);
        MockMultipartFile poster = new MockMultipartFile("poster", "originName.jpg", "", new byte[100]);

        ContentDto.ContentChildren children = ContentDto.ContentChildren.builder()
                .tags(new ArrayList<>())
                .roles(new ArrayList<>())
                .gallery(new ArrayList<>())
                .build();

        // when
        Throwable throwable = catchThrowable(() -> contentService.createContent(content, poster, children));

        // then
        assertThat(throwable).isInstanceOf(InvalidFileException.class);
    }

    @Test
    public void createContent_WithTag_Save() throws Exception {
        // given
        Content content = mock(Content.class);
        Tag tag = mock(Tag.class);

        MockMultipartFile poster = new MockMultipartFile("poster", "originName.jpg", "image/jpg", new byte[100]);

        ContentDto.ContentChildren children = ContentDto.ContentChildren.builder()
                .tags(List.of(1L))
                .roles(new ArrayList<>())
                .gallery(new ArrayList<>())
                .build();

        given(tagRepository.findAllById(anyIterable()))
                .willReturn(List.of(tag));

        // when
        contentService.createContent(content, poster, children);

        // then
        verify(contentRepository, times(1)).save(content);
        verify(content, times(1)).setPosterImage(any(Image.class));
        verify(content, times(1)).addTag(any(Tag.class));
    }

    @Test
    public void createContent_WithParticipant_Save() throws Exception {
        // given
        Content content = mock(Content.class);
        Participant participant = mock(Participant.class);

        MockMultipartFile poster = new MockMultipartFile("poster", "originName.jpg", "image/jpg", new byte[100]);

        ContentDto.ContentChildren children = ContentDto.ContentChildren.builder()
                .tags(new ArrayList<>())
                .roles(List.of(ParticipantDto.ParticipantRole.builder().participantId(1L).build()))
                .gallery(new ArrayList<>())
                .build();

        given(participant.getId()).willReturn(1L);
        given(participantRepository.findAllById(anyIterable()))
                .willReturn(List.of(participant));

        // when
        contentService.createContent(content, poster, children);

        // then
        verify(contentRepository, times(1)).save(content);
        verify(content, times(1)).setPosterImage(any(Image.class));
        verify(content, times(1)).addParticipant(any(Participant.class), any(), any());
    }

    @Test
    public void createContent_WithGallery_Save() throws Exception {
        // given
        Content content = mock(Content.class);

        MockMultipartFile poster = new MockMultipartFile("poster", "originName.jpg", "image/jpg", new byte[100]);
        MockMultipartFile gallery = new MockMultipartFile("gallery", "galleryName.jpg", "image/jpg", new byte[100]);

        ContentDto.ContentChildren children = ContentDto.ContentChildren.builder()
                .tags(new ArrayList<>())
                .roles(new ArrayList<>())
                .gallery(List.of(gallery))
                .build();

        // when
        contentService.createContent(content, poster, children);

        // then
        verify(contentRepository, times(1)).save(content);
        verify(content, times(1)).setPosterImage(any(Image.class));
        verify(content, times(1)).addImage(any(Image.class));
    }

    @Test
    public void createContent_WithInvalidGallery_ThrowException() throws Exception {
        // given
        Content content = mock(Content.class);

        MockMultipartFile poster = new MockMultipartFile("poster", "originName.jpg", "image/jpg", new byte[100]);
        MockMultipartFile gallery = new MockMultipartFile("gallery", "galleryName.jpg", "", new byte[100]);

        ContentDto.ContentChildren children = ContentDto.ContentChildren.builder()
                .tags(new ArrayList<>())
                .roles(new ArrayList<>())
                .gallery(List.of(gallery))
                .build();

        // when
        Throwable throwable = catchThrowable(() -> contentService.createContent(content, poster, children));

        // then
        assertThat(throwable).isInstanceOf(InvalidFileException.class);
    }

    @Test
    public void getHighScoreList_GreaterThanScore_ReturnMainList() throws Exception {
        // given
        Movie movie = spy(ContentMother.movie().build());

        given(movie.getId()).willReturn(1L);
        given(contentRepository.getContentsScoreIsGreaterThan(eq(ContentTypeParameter.MOVIES), anyDouble(), anyInt()))
                .willReturn(List.of(movie));
        given(contentRepository.getContentScore(anySet())).willReturn(Map.of(1L, 3.0));

        // when
        ContentDto.MainList list = contentService.getHighScoreList(ContentTypeParameter.MOVIES, 3.0, 10);

        // then
        assertThat(list).isNotNull();
        assertThat(list.getList()).hasSize(1);
        assertThat(list.getList().get(0).getScore()).isEqualTo(3.0);
    }

    @Test
    public void getHighScoreList_NoContentGreaterThanScore_ReturnEmpty() throws Exception {
        // given
        given(contentRepository.getContentsScoreIsGreaterThan(eq(ContentTypeParameter.MOVIES), anyDouble(), anyInt()))
                .willReturn(Collections.emptyList());
        given(contentRepository.getContentScore(anySet())).willReturn(Collections.emptyMap());

        // when
        ContentDto.MainList list = contentService.getHighScoreList(ContentTypeParameter.MOVIES, 3.0, 10);

        // then
        assertThat(list).isNotNull();
        assertThat(list.getList()).hasSize(0);
    }

    @Test
    public void getPeopleList_ContentExist_ReturnMainList() throws Exception {
        // given
        Participant participant = spy(ParticipantMother.defaultParticipant("배우").build());
        Movie movie = spy(ContentMother.movie().build());

        given(participant.getId()).willReturn(1L);
        given(movie.getId()).willReturn(1L);
        given(participantRepository.findMostFamous(eq(ContentTypeParameter.MOVIES), anyString()))
                .willReturn(participant);
        given(contentRepository.getContentsHasParticipant(eq(ContentTypeParameter.MOVIES), anyLong(), anyInt()))
                .willReturn(List.of(movie));
        given(contentRepository.getContentScore(anySet())).willReturn(Map.of(1L, 3.0));

        // when
        ContentDto.MainList list = contentService.getPeopleList(ContentTypeParameter.MOVIES, "배우", 10);

        // then
        assertThat(list).isNotNull();
        assertThat(list.getList()).hasSize(1);
        assertThat(list.getList().get(0).getScore()).isEqualTo(3.0);
    }

    @Test
    public void getPeopleList_ContentNotExist_ReturnEmpty() throws Exception {
        // given
        Participant participant = spy(ParticipantMother.defaultParticipant("배우").build());

        given(participant.getId()).willReturn(1L);
        given(participantRepository.findMostFamous(eq(ContentTypeParameter.MOVIES), anyString()))
                .willReturn(participant);
        given(contentRepository.getContentsHasParticipant(eq(ContentTypeParameter.MOVIES), anyLong(), anyInt()))
                .willReturn(Collections.emptyList());
        given(contentRepository.getContentScore(anySet())).willReturn(Collections.emptyMap());

        // when
        ContentDto.MainList list = contentService.getPeopleList(ContentTypeParameter.MOVIES, "배우", 10);

        // then
        assertThat(list).isNotNull();
        assertThat(list.getList()).hasSize(0);
    }

    @Test
    public void getTagList_ContentExist_ReturnMainList() throws Exception {
        // given
        Tag tag = spy(Tag.builder().description("tag").build());
        Movie movie = spy(ContentMother.movie().build());

        given(tag.getId()).willReturn(1L);
        given(movie.getId()).willReturn(1L);
        given(contentRepository.getContentsTagged(eq(ContentTypeParameter.MOVIES), anyLong(), anyInt()))
                .willReturn(List.of(movie));
        given(contentRepository.getContentScore(anySet())).willReturn(Map.of(1L, 3.0));

        // when
        ContentDto.MainList list = contentService.getTagList(ContentTypeParameter.MOVIES, tag, 10);

        // then
        assertThat(list).isNotNull();
        assertThat(list.getList()).hasSize(1);
        assertThat(list.getList().get(0).getScore()).isEqualTo(3.0);
    }

    @Test
    public void getTagList_ContentNotExist_ReturnEmpty() throws Exception {
        // given
        Tag tag = spy(Tag.builder().description("tag").build());

        given(tag.getId()).willReturn(1L);
        given(contentRepository.getContentsTagged(eq(ContentTypeParameter.MOVIES), anyLong(), anyInt()))
                .willReturn(Collections.emptyList());
        given(contentRepository.getContentScore(anySet())).willReturn(Collections.emptyMap());

        // when
        ContentDto.MainList list = contentService.getTagList(ContentTypeParameter.MOVIES, tag, 10);

        // then
        assertThat(list).isNotNull();
        assertThat(list.getList()).hasSize(0);
    }

    @Test
    public void getCollectionList_ContentExist_ReturnMainList() throws Exception {
        // given
        User user = spy(UserMother.defaultUser().build());
        Collection collection = spy(CollectionMother.defaultCollection(user).build());
        Movie movie = spy(ContentMother.movie().build());

        given(user.getId()).willReturn(1L);
        given(collection.getId()).willReturn(1L);
        given(movie.getId()).willReturn(1L);
        given(contentRepository.getContentsInCollection(anyLong(), any(Pageable.class)))
                .willReturn(List.of(movie));
        given(contentRepository.getContentScore(anySet())).willReturn(Map.of(1L, 3.0));

        // when
        ContentDto.MainListForCollection list = contentService.getCollectionList(collection, 10);

        // then
        assertThat(list).isNotNull();
        assertThat(list.getUserId()).isEqualTo(1L);
        assertThat(list.getCollectionId()).isEqualTo(1L);
        assertThat(list.getList()).hasSize(1);
        assertThat(list.getList().get(0).getScore()).isEqualTo(3.0);
    }

    @Test
    public void getCollectionList_ContentNotExist_ReturnEmpty() throws Exception {
        // given
        User user = spy(UserMother.defaultUser().build());
        Collection collection = spy(CollectionMother.defaultCollection(user).build());

        given(user.getId()).willReturn(1L);
        given(collection.getId()).willReturn(1L);
        given(contentRepository.getContentsInCollection(anyLong(), any(Pageable.class)))
                .willReturn(Collections.emptyList());
        given(contentRepository.getContentScore(anySet())).willReturn(Collections.emptyMap());

        // when
        ContentDto.MainListForCollection list = contentService.getCollectionList(collection, 10);

        // then
        assertThat(list).isNotNull();
        assertThat(list.getUserId()).isEqualTo(1L);
        assertThat(list.getCollectionId()).isEqualTo(1L);
        assertThat(list.getList()).hasSize(0);
    }

    @Test
    public void getContentsWithScore_ContentExist_ReturnResult() throws Exception {
        // given
        Movie movie = spy(ContentMother.movie().build());

        given(movie.getId()).willReturn(1L);
        given(contentRepository.getContentScore(anySet())).willReturn(Map.of(1L, 3.0));

        // when
        List<ContentDto.MainListItem> contentsWithScore = contentService.getContentsWithScore(List.of(movie));

        // then
        assertThat(contentsWithScore).hasSize(1);
        assertThat(contentsWithScore.get(0).getId()).isEqualTo(1L);
    }

    @Test
    public void getAwardList_ContentExist_AwardWithImage() throws Exception {
        // given
        User user = spy(UserMother.defaultUser().build());
        Collection collection = spy(CollectionMother.defaultCollection(user).build());
        Movie movie1 = ContentMother.movie().build();
        Movie movie2 = ContentMother.movie().build();
        Movie movie3 = ContentMother.movie().build();
        Movie movie4 = ContentMother.movie().build();

        given(collection.getId()).willReturn(1L);
        given(collectionRepository.getAward(eq(ContentTypeParameter.MOVIES)))
                .willReturn(List.of(collection));
        given(contentRepository.getContentsInCollection(anyLong(), any(Pageable.class)))
                .willReturn(List.of(movie1, movie2, movie3, movie4));

        // when
        List<ContentDto.ListForAward> awardList = contentService.getAwardList(ContentTypeParameter.MOVIES);

        // then
        assertThat(awardList).hasSize(1);
        assertThat(awardList.get(0).getList()).hasSize(1);
        assertThat(awardList.get(0).getList().get(0).getImages()).hasSize(4);
    }

    @Test
    public void getAwardDetail_ContentExist_AwardDetail() throws Exception {
        // given
        User user = spy(UserMother.defaultUser().build());
        Collection collection = spy(CollectionMother.defaultCollection(user).build());
        Movie movie = spy(ContentMother.movie().build());

        given(user.getId()).willReturn(1L);
        given(collection.getId()).willReturn(1L);
        given(movie.getId()).willReturn(1L);
        given(collectionRepository.findById(anyLong())).willReturn(Optional.of(collection));
        given(contentRepository.getContentsInCollection(anyLong(), any(Pageable.class)))
                .willReturn(List.of(movie));
        given(contentRepository.getContentScore(anySet())).willReturn(Map.of(1L, 3.0));

        // when
        ContentDto.MainList awardDetail = contentService.getAwardDetail(1L, PageRequest.of(0, 10));

        // then
        assertThat(awardDetail).isNotNull();
        assertThat(awardDetail.getList()).hasSize(1);
        assertThat(awardDetail.getList().get(0).getScore()).isEqualTo(3.0);
    }

    @Test
    public void getAwardDetail_AwardNotExist_ThrowException() throws Exception {
        // given
        given(collectionRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> contentService.getAwardDetail(1L, PageRequest.of(0, 10)));

        // then
        assertThat(throwable).isInstanceOf(EntityNotExistException.class);
    }

    @Test
    public void getAwardDetail_OwnerIsNotAdmin_ThrowException() throws Exception {
        // given
        User user = spy(UserMother.defaultUser().build());
        Collection collection = spy(CollectionMother.defaultCollection(user).build());

        given(user.getId()).willReturn(2L);
        given(collectionRepository.findById(anyLong())).willReturn(Optional.of(collection));

        // when
        Throwable throwable = catchThrowable(() -> contentService.getAwardDetail(1L, PageRequest.of(0, 10)));

        // then
        assertThat(throwable).isInstanceOf(EntityNotExistException.class);
    }

    @Test
    public void getCollectionDetail_ContentExist_CollectionDetail() throws Exception {
        // given
        User user = spy(UserMother.defaultUser().build());
        Collection collection = spy(CollectionMother.defaultCollection(user).build());
        Movie movie = spy(ContentMother.movie().build());

        given(collection.getId()).willReturn(1L);
        given(movie.getId()).willReturn(1L);
        given(collectionRepository.countContentById(collection.getId())).willReturn(10L);
        given(collectionRepository.findById(anyLong())).willReturn(Optional.of(collection));
        given(contentRepository.getContentsInCollection(anyLong(), any(Pageable.class)))
                .willReturn(List.of(movie));
        given(contentRepository.getContentScore(anySet())).willReturn(Map.of(1L, 3.0));

        // when
        ContentDto.CollectionDetail collectionDetail = contentService.getCollectionDetail(1L, PageRequest.of(0, 10));

        // then
        assertThat(collectionDetail).isNotNull();
        assertThat(collectionDetail.getContentCount()).isEqualTo(10L);
        assertThat(collectionDetail.getList()).hasSize(1);
        assertThat(collectionDetail.getList().get(0).getScore()).isEqualTo(3.0);
    }

    @Test
    public void getCollectionContentsWithScore_ContentExist_CollectionItemList() throws Exception {
        // given
        Movie movie = spy(ContentMother.movie().build());

        given(movie.getId()).willReturn(1L);
        given(contentRepository.getContentScore(anySet())).willReturn(Map.of(1L, 3.0));

        // when
        List<ContentDto.CollectionItem> contentsWithScore = contentService.getCollectionContentsWithScore(List.of(movie));

        // then
        assertThat(contentsWithScore).hasSize(1);
        assertThat(contentsWithScore.get(0).getId()).isEqualTo(1L);
        assertThat(contentsWithScore.get(0).getScore()).isEqualTo(3.0);
        assertThat(contentsWithScore.get(0).getType()).isEqualTo("M");
    }

    @Test
    public void searchByType_MovieContent_SearchItem() throws Exception {
        // given
        Movie movie = spy(ContentMother.movie().build());

        given(searchRepository.searchTypeContentsReturnIds(anyString(), anyString(), anyInt(), anyInt()))
                .willReturn(List.of(1L));
        given(contentRepository.findAllById(anyIterable()))
                .willReturn(List.of(movie));

        // when
        List<ContentDto.SearchItem> list = contentService.searchByType(ContentTypeParameter.MOVIES, "movie", 1, 10);

        // then
        assertThat(list).hasSize(1);
        assertThat(list.get(0)).isInstanceOf(ContentDto.SearchItem.class);
    }
}