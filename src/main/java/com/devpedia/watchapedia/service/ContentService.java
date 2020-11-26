package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.ImageCategory;
import com.devpedia.watchapedia.dto.*;
import com.devpedia.watchapedia.exception.InvalidFileException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.*;
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
        Book book = request.toEntity();
        createContent(book, poster, new ContentDto.ContentChildren(request.getRoles(), request.getTags(), gallery));
    }

    public void saveMovieWithImage(MovieDto.MovieInsertRequest request, MultipartFile poster, List<MultipartFile> gallery) {
        Movie movie = request.toEntity();
        createContent(movie, poster, new ContentDto.ContentChildren(request.getRoles(), request.getTags(), gallery));
    }

    public void saveTvShowWithImage(TvShowDto.TvShowInsertRequest request, MultipartFile poster, List<MultipartFile> gallery) {
        TvShow tvShow = request.toEntity();
        createContent(tvShow, poster, new ContentDto.ContentChildren(request.getRoles(), request.getTags(), gallery));
    }

    private void createContent(Content content, MultipartFile poster, ContentDto.ContentChildren children) {
        addPosterImage(content, poster);
        addChildren(content, children.getRoles(), children.getTags(), children.getGallery());
    }

    private void addPosterImage(Content content, MultipartFile poster) {
        Image posterImage = createPosterImage(poster);
        content.setPosterImage(posterImage);
    }

    private Image createPosterImage(MultipartFile poster) {
        if (isInvalidImageFile(poster))
            throw new InvalidFileException(ErrorCode.IMAGE_FORMAT_INVALID, "포스터 이미지 파일이 올바르지 않습니다");

        Image posterImage = Image.of(poster, ImageCategory.POSTER);
        s3Service.upload(poster, posterImage.getPath());

        return posterImage;
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
                .map(ContentDto.CommonContentInfo::new)
                .collect(Collectors.toList());
    }
}
