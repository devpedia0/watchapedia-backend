package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Image;
import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.domain.enums.ImageCategory;
import com.devpedia.watchapedia.dto.ParticipantDto;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.ParticipantRepository;
import com.devpedia.watchapedia.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipantService {

    private final S3Service s3Service;
    private final ParticipantRepository participantRepository;

    public void addWithImage(ParticipantDto.ParticipantInsertRequest request, MultipartFile file) {
        Image profileImage = null;

        if (file != null && !file.isEmpty()) {
            profileImage = Image.of(file, ImageCategory.POSTER);
            s3Service.upload(file, profileImage.getPath());
        }

        Participant participant = Participant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .profileImage(profileImage)
                .build();

        participantRepository.save(participant);
    }

    public List<ParticipantDto.ParticipantInfo> findAll() {
        List<Participant> list = participantRepository.findAll();

        return list.stream()
                .map(participant -> ParticipantDto.ParticipantInfo.builder()
                        .id(participant.getId())
                        .name(participant.getName())
                        .description(participant.getDescription())
                        .profileImagePath(
                                participant.getProfileImage() != null
                                        ? UrlUtil.getCloudFrontUrl(participant.getProfileImage().getPath()) : null)
                        .build())
                .collect(Collectors.toList());
    }

    public List<ParticipantDto.ParticipantInfo> searchWithPaging(String search, int page, int size) {
        List<Participant> list = participantRepository.searchWithPaging(search, page, size);

        return list.stream()
                .map(participant -> ParticipantDto.ParticipantInfo.builder()
                        .id(participant.getId())
                        .name(participant.getName())
                        .description(participant.getDescription())
                        .profileImagePath(
                                participant.getProfileImage() != null
                                        ? UrlUtil.getCloudFrontUrl(participant.getProfileImage().getPath()) : null)
                        .build())
                .collect(Collectors.toList());
    }

    public void update(Long participantId, ParticipantDto.ParticipantUpdateRequest request) {
        Participant participant = participantRepository.findById(participantId);
        if (participant == null) throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        participant.updateInfo(request.getName(), request.getDescription());
    }

    public void delete(Long id) {
        Participant participant = participantRepository.findById(id);
        if (participant == null) throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        participantRepository.delete(participant);
    }
}
