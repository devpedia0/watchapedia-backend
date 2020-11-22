package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.dto.BookDto;
import com.devpedia.watchapedia.dto.MovieDto;
import com.devpedia.watchapedia.dto.ParticipantDto;
import com.devpedia.watchapedia.dto.TvShowDto;
import com.devpedia.watchapedia.repository.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class ContentServiceTest {

    @InjectMocks
    private ContentService contentService;

    @Mock
    private S3Service s3Service;
    @Mock
    private ContentRepository contentRepository;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private TagRepository tagRepository;

    @Test
    public void saveBook_Correct_SaveBook() throws Exception {
        // given
        ParticipantDto.ParticipantRole role = ParticipantDto.ParticipantRole.builder()
                .characterName("cname")
                .role("role")
                .participantId(1L)
                .build();

        Participant participant = mock(Participant.class);
        Tag tag = mock(Tag.class);

        BookDto.BookInsertRequest request = BookDto.BookInsertRequest.builder()
                .category("category")
                .contents("content")
                .description("desc")
                .elaboration("elabor")
                .mainTitle("mainTitle")
                .page(11000)
                .productionDate(LocalDate.of(2020, 10, 30))
                .subtitle("subtitle")
                .roles(Collections.singletonList(role))
                .tags(Collections.singletonList(1L))
                .build();

        MultipartFile file = new MockMultipartFile("image.jpg", new byte[100]);

        given(participantRepository.findListIn(anySet()))
                .willReturn(Collections.singletonList(participant));
        given(tagRepository.findListIn(anySet()))
                .willReturn(Collections.singletonList(tag));
        given(participant.getId()).willReturn(1L);
        given(tag.getId()).willReturn(1L);

        // when
        contentService.saveBookWithImage(request, file);

        // then
        verify(contentRepository, times(1)).save(any(Book.class));
    }

    @Test
    public void saveBook_RolesAndTagsNull_SaveWithoutThem() throws Exception {
        // given
        BookDto.BookInsertRequest request = BookDto.BookInsertRequest.builder()
                .category("category")
                .contents("content")
                .description("desc")
                .elaboration("elabor")
                .mainTitle("mainTitle")
                .page(11000)
                .productionDate(LocalDate.of(2020, 10, 30))
                .subtitle("subtitle")
                .roles(null)
                .tags(null)
                .build();

        MultipartFile file = new MockMultipartFile("image.jpg", new byte[100]);

        // when
        contentService.saveBookWithImage(request, file);

        // then
        verify(contentRepository, times(1)).save(any(Book.class));
    }

    @Test
    public void saveMovie_Correct_SaveMovie() throws Exception {
        // given
        ParticipantDto.ParticipantRole role = ParticipantDto.ParticipantRole.builder()
                .characterName("cname")
                .role("role")
                .participantId(1L)
                .build();

        Participant participant = mock(Participant.class);
        Tag tag = mock(Tag.class);

        MovieDto.MovieInsertRequest request = MovieDto.MovieInsertRequest.builder()
                .category("category")
                .description("desc")
                .mainTitle("mainTitle")
                .productionDate(LocalDate.of(2020, 10, 30))
                .bookRate(12.13)
                .countryCode("KR")
                .isNetflixContent(true)
                .isWatchaContent(true)
                .originTitle("title")
                .runningTimeInMinutes(110)
                .totalAudience(10000L)
                .roles(Collections.singletonList(role))
                .tags(Collections.singletonList(1L))
                .build();

        MultipartFile file = new MockMultipartFile("image.jpg", new byte[100]);

        given(participantRepository.findListIn(anySet()))
                .willReturn(Collections.singletonList(participant));
        given(tagRepository.findListIn(anySet()))
                .willReturn(Collections.singletonList(tag));
        given(participant.getId()).willReturn(1L);
        given(tag.getId()).willReturn(1L);

        // when
        contentService.saveMovieWithImage(request, file);

        // then
        verify(contentRepository, times(1)).save(any(Movie.class));
    }

    @Test
    public void saveMovie_RolesAndTagsNull_SaveWithoutThem() throws Exception {
        // given
        MovieDto.MovieInsertRequest request = MovieDto.MovieInsertRequest.builder()
                .category("category")
                .description("desc")
                .mainTitle("mainTitle")
                .productionDate(LocalDate.of(2020, 10, 30))
                .bookRate(12.13)
                .countryCode("KR")
                .isNetflixContent(true)
                .isWatchaContent(true)
                .originTitle("title")
                .runningTimeInMinutes(110)
                .totalAudience(10000L)
                .roles(null)
                .tags(null)
                .build();

        MultipartFile file = new MockMultipartFile("image.jpg", new byte[100]);

        // when
        contentService.saveMovieWithImage(request, file);

        // then
        verify(contentRepository, times(1)).save(any(Movie.class));
    }

    @Test
    public void saveTvShow_Correct_SaveTvShow() throws Exception {
        // given
        ParticipantDto.ParticipantRole role = ParticipantDto.ParticipantRole.builder()
                .characterName("cname")
                .role("role")
                .participantId(1L)
                .build();

        Participant participant = mock(Participant.class);
        Tag tag = mock(Tag.class);

        TvShowDto.TvShowInsertRequest request = TvShowDto.TvShowInsertRequest.builder()
                .category("category")
                .description("desc")
                .mainTitle("mainTitle")
                .productionDate(LocalDate.of(2020, 10, 30))
                .countryCode("KR")
                .isNetflixContent(true)
                .isWatchaContent(true)
                .originTitle("title")
                .roles(Collections.singletonList(role))
                .tags(Collections.singletonList(1L))
                .build();

        MultipartFile file = new MockMultipartFile("image.jpg", new byte[100]);

        given(participantRepository.findListIn(anySet()))
                .willReturn(Collections.singletonList(participant));
        given(tagRepository.findListIn(anySet()))
                .willReturn(Collections.singletonList(tag));
        given(participant.getId()).willReturn(1L);
        given(tag.getId()).willReturn(1L);

        // when
        contentService.saveTvShowWithImage(request, file);

        // then
        verify(contentRepository, times(1)).save(any(TvShow.class));
    }

    @Test
    public void saveTvShow_RolesAndTagsNull_SaveWithoutThem() throws Exception {
        // given
        TvShowDto.TvShowInsertRequest request = TvShowDto.TvShowInsertRequest.builder()
                .category("category")
                .description("desc")
                .mainTitle("mainTitle")
                .productionDate(LocalDate.of(2020, 10, 30))
                .countryCode("KR")
                .isNetflixContent(true)
                .isWatchaContent(true)
                .originTitle("title")
                .roles(null)
                .tags(null)
                .build();

        MultipartFile file = new MockMultipartFile("image.jpg", new byte[100]);

        // when
        contentService.saveTvShowWithImage(request, file);

        // then
        verify(contentRepository, times(1)).save(any(TvShow.class));
    }
}