package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Content;
import com.devpedia.watchapedia.domain.Image;
import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.domain.enums.ImageCategory;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.ParticipantDto;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.content.ContentRepository;
import com.devpedia.watchapedia.repository.participant.ParticipantRepository;
import com.devpedia.watchapedia.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipantService {

    private final S3Service s3Service;
    private final ParticipantRepository participantRepository;
    private final ContentService contentService;
    private final ContentRepository contentRepository;

    /**
     * 인물 이미지와 함께 인물 정보를 등록한다.
     * 인물 이미지가 없으면 없이 저장한다.
     * @param request 인물 정보
     * @param profile 프로필 사진
     */
    public void addWithImage(ParticipantDto.ParticipantInsertRequest request, MultipartFile profile) {
        Image profileImage = null;

        if (profile != null && !profile.isEmpty()) {
            profileImage = Image.of(profile, ImageCategory.PARTICIPANT_PROFILE);
            s3Service.upload(profile, profileImage.getPath());
        }

        Participant participant = request.toEntity(profileImage);

        participantRepository.save(participant);
    }

    /**
     * 참여자를 이름으로 검색한다.
     * @param query 참여자 이름
     * @param pageable pageable
     * @return 참여자 리스트
     */
    public List<ParticipantDto.ParticipantInfo> searchWithPaging(String query, Pageable pageable) {
        List<Participant> list = participantRepository.findByNameContaining(query, pageable);

        return list.stream()
                .map(ParticipantDto.ParticipantInfo::new)
                .collect(Collectors.toList());
    }

    /**
     * 참여자 정보를 수정한다.
     * @param id 참여자 id
     * @param request 참여자 수정 정보
     */
    public void update(Long id, ParticipantDto.ParticipantUpdateRequest request) {
        Optional<Participant> optionalParticipant = participantRepository.findById(id);
        Participant participant = optionalParticipant.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        participant.updateInfo(request.getName(), request.getJob(), request.getDescription());
    }

    /**
     * 참여자 정보를 삭제한다.
     * @param id 참여자 id
     */
    public void delete(Long id) {
        Optional<Participant> optionalParticipant = participantRepository.findById(id);
        Participant participant = optionalParticipant.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        participantRepository.delete(participant);
    }

    /**
     * 참여자 상세정보와 참여자가 참여한 컨텐츠 리스트를 구한다.
     * @param id 참여자 id
     * @param pageable pageable(참여한 컨텐츠)
     * @return 참여자 정보
     */
    public ParticipantDto.ParticipantInfo getParticipantInfo(Long id, Pageable pageable) {
        Optional<Participant> optionalParticipant = participantRepository.findById(id);
        Participant participant = optionalParticipant.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        return ParticipantDto.ParticipantInfo.builder()
                .id(participant.getId())
                .name(participant.getName())
                .job(participant.getJob())
                .description(participant.getDescription())
                .profileImagePath(
                        participant.getProfileImage() != null
                        ? UrlUtil.getCloudFrontUrl(participant.getProfileImage().getPath()) : null
                )
                .contents(getParticipantContents(participant.getId(), pageable))
                .build();
    }

    /**
     * 참여자가 참여한 컨텐츠 리스트를 구한다.
     * @param id 참여자 id
     * @param pageable pageable
     * @return 컨텐츠 dto 리스트
     */
    private List<ContentDto.CollectionItem> getParticipantContents(Long id, Pageable pageable) {
        List<Content> contents = contentRepository.findContentByParticipant(id, pageable);
        return contentService.getCollectionContentsWithScore(contents);
    }
}
