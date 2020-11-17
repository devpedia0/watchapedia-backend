package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.ParticipantDto;
import com.devpedia.watchapedia.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void addParticipant(@RequestPart("body") @Valid ParticipantDto.ParticipantInsertRequest request,
                               @RequestPart(value = "file", required = false) MultipartFile file) {
        participantService.addWithImage(request, file);
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

    @GetMapping("/participants")
    public List<ParticipantDto.ParticipantInfo> getParticipants(@RequestParam(required = false) String search,
                                                                @RequestParam @Positive int page,
                                                                @RequestParam @Min(1)@Max(20) int size) {
        return participantService.searchWithPaging(search, page, size);
    }
}
