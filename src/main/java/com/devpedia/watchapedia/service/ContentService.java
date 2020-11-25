package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.ImageCategory;
import com.devpedia.watchapedia.dto.*;
import com.devpedia.watchapedia.exception.InvalidFileException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.*;
import com.devpedia.watchapedia.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ContentService {

    private final S3Service s3Service;
    private final ContentRepository contentRepository;
    private final ParticipantRepository participantRepository;
    private final TagRepository tagRepository;

    public void saveBookWithImage(BookDto.BookInsertRequest request, MultipartFile poster, List<MultipartFile> gallery) {
        if (isInvalidImageFile(poster))
            throw new InvalidFileException(ErrorCode.IMAGE_FORMAT_INVALID, "포스터 이미지 파일이 올바르지 않습니다");

        Image posterImage = Image.of(poster, ImageCategory.POSTER);
        s3Service.upload(poster, posterImage.getPath());

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

        addChildren(book, request.getRoles(), request.getTags(), gallery);

        contentRepository.save(book);
    }

    public void saveMovieWithImage(MovieDto.MovieInsertRequest request, MultipartFile poster, List<MultipartFile> gallery) {
        if (isInvalidImageFile(poster))
            throw new InvalidFileException(ErrorCode.IMAGE_FORMAT_INVALID, "포스터 이미지 파일이 올바르지 않습니다");

        Image posterImage = Image.of(poster, ImageCategory.POSTER);
        s3Service.upload(poster, posterImage.getPath());

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

        addChildren(movie, request.getRoles(), request.getTags(), gallery);

        contentRepository.save(movie);
    }

    public void saveTvShowWithImage(TvShowDto.TvShowInsertRequest request, MultipartFile poster, List<MultipartFile> gallery) {
        if (isInvalidImageFile(poster))
            throw new InvalidFileException(ErrorCode.IMAGE_FORMAT_INVALID, "포스터 이미지 파일이 올바르지 않습니다");

        Image posterImage = Image.of(poster, ImageCategory.POSTER);
        s3Service.upload(poster, posterImage.getPath());

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

        addChildren(tvShow, request.getRoles(), request.getTags(), gallery);

        contentRepository.save(tvShow);
    }

    private void addChildren(Content content, List<ParticipantDto.ParticipantRole> roles, List<Long> tags, List<MultipartFile> gallery) {
        addParticipants(content, roles);
        addTags(content, tags);
        addGallery(content, gallery);
    }

    private void addParticipants(Content content, List<ParticipantDto.ParticipantRole> roles) {
        if (content == null || roles == null) return;

        Map<Long, ParticipantDto.ParticipantRole> map = roles.stream()
                .collect(Collectors.toMap(ParticipantDto.ParticipantRole::getParticipantId, role -> role));

        List<Participant> participants = participantRepository.findListIn(map.keySet());

        for (Participant participant : participants) {
            ParticipantDto.ParticipantRole role = map.get(participant.getId());
            content.addParticipant(participant, role.getRole(), role.getCharacterName());
        }
    }

    private void addTags(Content content, List<Long> tags) {
        if (content == null || tags == null) return;

        List<Tag> addedTags = tagRepository.findListIn(new HashSet<>(tags));

        for (Tag tag : addedTags) {
            content.addTag(tag);
        }
    }

    private void addGallery(Content content, List<MultipartFile> gallery) {
        if (content == null || gallery == null) return;

        for (MultipartFile file : gallery) {
            if (isInvalidImageFile(file)) continue;
            Image galleryImage = Image.of(file, ImageCategory.GALLERY);
            s3Service.upload(file, galleryImage.getPath());

            content.addImage(galleryImage);
        }
    }

    private boolean isInvalidImageFile(MultipartFile file) {
        return file.isEmpty() || file.getSize() == 0 ||
                file.getContentType() == null || !file.getContentType().contains("image");
    }

    public List<ContentDto.CommonContentInfo> searchAllWithPaging(String query, int page, int size) {
        List<Content> list = contentRepository.searchWithPaging(Content.class, query, page, size);

        return list.stream()
                .map(content -> ContentDto.CommonContentInfo.builder()
                        .id(content.getId())
                        .contentType(content.getDtype())
                        .mainTitle(content.getMainTitle())
                        .productionDate(content.getProductionDate())
                        .posterImagePath(UrlUtil.getCloudFrontUrl(content.getPosterImage().getPath()))
                        .build())
                .collect(Collectors.toList());
    }
}
