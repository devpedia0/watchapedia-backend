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
import java.util.List;

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

}