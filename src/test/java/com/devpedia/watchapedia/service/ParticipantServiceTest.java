package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.builder.ParticipantMother;
import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.dto.ParticipantDto;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.repository.participant.ParticipantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @InjectMocks
    private ParticipantService participantService;

    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private S3Service s3Service;

    @Test
    public void addWithImage_WithFile_AddWithImage() throws Exception {
        // given
        ParticipantDto.ParticipantInsertRequest request = ParticipantDto.ParticipantInsertRequest.builder()
                .name("name")
                .job("job")
                .description("desc")
                .build();
        MultipartFile file = new MockMultipartFile("image.jpg", new byte[100]);

        // when
        participantService.addWithImage(request, file);

        // then
        verify(s3Service, times(1)).upload(any(MultipartFile.class), anyString());
        verify(participantRepository, times(1)).save(any(Participant.class));
    }

    @Test
    public void addWithImage_WithoutFile_AddWithoutImage() throws Exception {
        // given
        ParticipantDto.ParticipantInsertRequest request = ParticipantDto.ParticipantInsertRequest.builder()
                .name("name")
                .job("job")
                .description("desc")
                .build();
        MultipartFile file = null;

        // when
        participantService.addWithImage(request, file);

        // then
        verify(s3Service, times(0)).upload(any(MultipartFile.class), anyString());
        verify(participantRepository, times(1)).save(any(Participant.class));
    }

    @Test
    public void searchWithPaging_ParticipantWithoutImage_ReturnList() throws Exception {
        // given
        Participant participant = ParticipantMother.defaultParticipant("배우").build();

        given(participantRepository.findByNameContaining(anyString(), any(Pageable.class)))
                .willReturn(List.of(participant));

        // when
        List<ParticipantDto.ParticipantInfo> list = participantService.searchWithPaging("p", PageRequest.of(0, 10));

        // then
        assertThat(list).hasSize(1);
    }

    @Test
    public void update_ExistId_UpdateInfo() throws Exception {
        // given
        ParticipantDto.ParticipantUpdateRequest request = ParticipantDto.ParticipantUpdateRequest.builder()
                .name("modName")
                .job("modJob")
                .description("modDesc")
                .build();

        Participant participant = ParticipantMother.defaultParticipant("배우").build();

        given(participantRepository.findById(anyLong()))
                .willReturn(Optional.of(participant));

        // when
        participantService.update(1L, request);

        // then
        assertThat(participant.getName()).isEqualTo(request.getName());
        assertThat(participant.getDescription()).isEqualTo(request.getDescription());
    }

    @Test
    public void update_NotExistId_ThrowException() throws Exception {
        // given
        ParticipantDto.ParticipantUpdateRequest request = ParticipantDto.ParticipantUpdateRequest.builder()
                .name("modName")
                .job("modJob")
                .description("modDesc")
                .build();

        given(participantRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> participantService.update(1L, request));

        // then
        assertThat(throwable).isInstanceOf(EntityNotExistException.class);
    }

    @Test
    public void delete_ExistId_Delete() throws Exception {
        // given
        Participant participant = ParticipantMother.defaultParticipant("배우").build();

        given(participantRepository.findById(anyLong()))
                .willReturn(Optional.of(participant));

        // when
        participantService.delete(1L);

        // then
        verify(participantRepository, times(1)).delete(any(Participant.class));
    }

    @Test
    public void delete_NotExistId_ThrowException() throws Exception {
        // given
        given(participantRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> participantService.delete(1L));

        // then
        assertThat(throwable).isInstanceOf(EntityNotExistException.class);
    }
}