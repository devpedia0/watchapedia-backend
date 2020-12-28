package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Tag;
import com.devpedia.watchapedia.dto.TagDto;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    /**
     * 태그 정보를 추가한다.
     * @param request 태그 요청
     */
    public void addTag(TagDto.TagInsertRequest request) {
        Tag tag = Tag.builder()
                .description(request.getDescription())
                .build();
        tagRepository.save(tag);
    }

    /**
     * 태그 정보를 삭제한다.
     * @param id 태그 id
     */
    public void delete(Long id) {
        Optional<Tag> optionalTag = tagRepository.findById(id);
        Tag tag = optionalTag.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        tagRepository.delete(tag);
    }

    /**
     * 태그 정보를 검색한다.
     * @param query 태그 설명
     * @param pageable pageable
     * @return 태그 정보 리스트
     */
    public List<TagDto.TagInfo> searchWithPaging(String query, Pageable pageable) {
        List<Tag> list = tagRepository.findByDescriptionContaining(query, pageable);

        return list.stream()
                .map(TagDto.TagInfo::new)
                .collect(Collectors.toList());
    }
}
