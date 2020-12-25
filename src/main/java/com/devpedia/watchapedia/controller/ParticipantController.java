package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.ParticipantDto;
import com.devpedia.watchapedia.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @PostMapping(value = "/admin/participants", consumes = {MediaType.MULTIPART_MIXED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public void addParticipant(@RequestPart("body") @Valid ParticipantDto.ParticipantInsertRequest request,
                               @RequestPart(value = "profile", required = false) MultipartFile profile) {
        participantService.addWithImage(request, profile);
    }

    @PutMapping("/admin/participants/{id}")
    public void editParticipant(@PathVariable("id") Long participantId,
                                @RequestBody ParticipantDto.ParticipantUpdateRequest request) {
        participantService.update(participantId, request);
    }

    @DeleteMapping("/admin/participants/{id}")
    public void deleteParticipant(@PathVariable("id") Long participantId) {
        participantService.delete(participantId);
    }

    @GetMapping("/admin/participants")
    public List<ParticipantDto.ParticipantInfo> getParticipants(@RequestParam(required = false) String query,
                                                                @RequestParam @Positive int page,
                                                                @RequestParam @Min(1)@Max(20) int size) {
        return participantService.searchWithPaging(query, PageRequest.of(page - 1, size));
    }

    @GetMapping("/participants/{id}")
    public ParticipantDto.ParticipantInfo getParticipantInfo(@PathVariable Long id, @RequestParam @Min(1)@Max(20) int size) {
        return participantService.getParticipantInfo(id, PageRequest.of(0, size));
    }

    @GetMapping("/participants/{id}/contents")
    public List<ContentDto.CollectionItem> getParticipantContents(@PathVariable Long id,
                                                                  @RequestParam @Positive int page,
                                                                  @RequestParam @Min(1)@Max(20) int size) {
        return participantService.getParticipantInfo(id, PageRequest.of(page - 1, size)).getContents();
    }
}
