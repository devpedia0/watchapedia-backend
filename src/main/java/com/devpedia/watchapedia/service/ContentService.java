package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.ImageCategory;
import com.devpedia.watchapedia.dto.BookDto;
import com.devpedia.watchapedia.dto.MovieDto;
import com.devpedia.watchapedia.dto.ParticipantDto;
import com.devpedia.watchapedia.dto.TvShowDto;
import com.devpedia.watchapedia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ContentService {

    private final S3Service s3Service;
    private final MovieRepository movieRepository;
    private final BookRepository bookRepository;
    private final TvShowRepository tvShowRepository;
    private final ParticipantRepository participantRepository;
    private final TagRepository tagRepository;

    public void saveBookWithImage(BookDto.BookInsertRequest request, MultipartFile file) {
        Image posterImage = Image.of(file, ImageCategory.POSTER);

        s3Service.upload(file, posterImage.getPath());

        Book book = Book.builder()
                .posterImage(posterImage)
                .mainTitle(request.getMainTitle())
                .subtitle(request.getSubtitle())
                .category(request.getCategory())
                .descrption(request.getDescription())
                .productionDate(request.getProductionDate())
                .page(request.getPage())
                .elaboration(request.getElaboration())
                .contents(request.getContents())
                .build();

        addParticipantsAndTags(book, request.getRoleList(), request.getTagList());

        bookRepository.save(book);
    }

    public void saveMovieWithImage(MovieDto.MovieInsertRequest request, MultipartFile file) {
        Image posterImage = Image.of(file, ImageCategory.POSTER);

        s3Service.upload(file, posterImage.getPath());

        Movie movie = Movie.builder()
                .posterImage(posterImage)
                .mainTitle(request.getMainTitle())
                .category(request.getCategory())
                .description(request.getDescription())
                .productionDate(request.getProductionDate())
                .countryCode(request.getCountryCode())
                .originTitle(request.getOriginTitle())
                .runningTimeInMinutes(request.getRunningTimeInMinutes())
                .bookRate(request.getBookRate())
                .totalAudience(request.getTotalAudience())
                .isNetflixContent(request.getIsNetflixContent())
                .isWatchaContent(request.getIsWatchaContent())
                .build();

        addParticipantsAndTags(movie, request.getRoleList(), request.getTagList());

        movieRepository.save(movie);
    }

    public void saveTvShowWithImage(TvShowDto.TvShowInsertRequest request, MultipartFile file) {
        Image posterImage = Image.of(file, ImageCategory.POSTER);

        s3Service.upload(file, posterImage.getPath());

        TvShow tvShow = TvShow.builder()
                .posterImage(posterImage)
                .mainTitle(request.getMainTitle())
                .category(request.getCategory())
                .description(request.getDescription())
                .productionDate(request.getProductionDate())
                .countryCode(request.getCountryCode())
                .originTitle(request.getOriginTitle())
                .isNetflixContent(request.getIsNetflixContent())
                .isWatchaContent(request.getIsWatchaContent())
                .build();

        addParticipantsAndTags(tvShow, request.getRoleList(), request.getTagList());

        tvShowRepository.save(tvShow);
    }

    private void addParticipantsAndTags(Content content, List<ParticipantDto.ParticipantRole> roleList, List<Long> tagList) {
        addParticipants(content, roleList);
        addTags(content, tagList);
    }

    private void addParticipants(Content content, List<ParticipantDto.ParticipantRole> roleList) {
        if (content == null || roleList == null) return;

        Map<Long, ParticipantDto.ParticipantRole> map = roleList.stream()
                .collect(Collectors.toMap(ParticipantDto.ParticipantRole::getParticipantId, role -> role));

        List<Participant> participants = participantRepository.findListIn(map.keySet());

        for (Participant participant : participants) {
            ParticipantDto.ParticipantRole role = map.get(participant.getId());
            content.addParticipant(participant, role.getRole(), role.getCharacterName());
        }
    }

    private void addTags(Content content, List<Long> tagList) {
        if (content == null || tagList == null) return;

        List<Tag> tags = tagRepository.findListIn(new HashSet<>(tagList));

        for (Tag tag : tags) {
            content.addTag(tag);
        }
    }


}
