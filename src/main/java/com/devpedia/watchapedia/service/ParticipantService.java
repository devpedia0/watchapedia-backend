package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Image;
import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.domain.enums.ImageCategory;
import com.devpedia.watchapedia.dto.ParticipantDto;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.participant.ParticipantRepository;
import com.devpedia.watchapedia.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    public void addWithImage(ParticipantDto.ParticipantInsertRequest request, MultipartFile profile) {
        Image profileImage = null;

        if (profile != null && !profile.isEmpty()) {
            profileImage = Image.of(profile, ImageCategory.PARTICIPANT_PROFILE);
            s3Service.upload(profile, profileImage.getPath());
        }

        Participant participant = request.toEntity(profileImage);

        participantRepository.save(participant);
    }

    public List<ParticipantDto.ParticipantInfo> searchWithPaging(String query, int page, int size) {
        List<Participant> list = participantRepository.findByNameContaining(query, PageRequest.of(page - 1, size));

        return list.stream()
                .map(ParticipantDto.ParticipantInfo::new)
                .collect(Collectors.toList());
    }

    public void update(Long id, ParticipantDto.ParticipantUpdateRequest request) {
        Optional<Participant> optionalParticipant = participantRepository.findById(id);
        Participant participant = optionalParticipant.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        participant.updateInfo(request.getName(), request.getJob(), request.getDescription());
    }

    public void delete(Long id) {
        Optional<Participant> optionalParticipant = participantRepository.findById(id);
        Participant participant = optionalParticipant.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        participantRepository.delete(participant);
    }
}
