package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Book;
import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.domain.Tag;
import com.devpedia.watchapedia.dto.BookDto;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.repository.collection.CollectionRepository;
import com.devpedia.watchapedia.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BookService {

    private static final double HIGH_SCORE = 4.0;
    private static final int HIGH_SCORE_LIST_SIZE = 10;

    private static final String POPULAR_JOB_WRITER = "저자";
    private static final int POPULAR_LIST_SIZE = 10;

    private static final int RANDOM_TAG_COUNT = 2;
    private static final int TAG_LIST_SIZE = 10;

    private static final int RANDOM_COLLECTION_COUNT = 3;
    private static final int COLLECTION_LIST_SIZE = 10;

    private final ContentService contentService;
    private final TagRepository tagRepository;
    private final CollectionRepository collectionRepository;

    /**
     * 책 삽입 어드민용 API
     * 책 정보와 포스터 이미지, 갤러리 이미지를 저장한다.
     * @param request 책 정보
     * @param poster 포스터 이미지
     * @param gallery 갤러리 이미지
     */
    public void saveWithImage(BookDto.BookInsertRequest request, MultipartFile poster, List<MultipartFile> gallery) {
        Book book = request.toEntity();
        contentService.createContent(book, poster, new ContentDto.ContentChildren(request.getRoles(), request.getTags(), gallery));
    }

    /**
     * 평점이 높은 책들을 조회한다.
     * @return 평점이 높은 책 리스트
     */
    public List<ContentDto.MainList> getHighScoreList() {
        ContentDto.MainList highScoreList = contentService.getHighScoreList(ContentTypeParameter.BOOKS, HIGH_SCORE, HIGH_SCORE_LIST_SIZE);
        return Collections.singletonList(highScoreList);
    }

    /**
     * 화제의 인물 리스트 조회.
     * 가장 많은 작품에 참여한 인물의 작품들을 구한다.
     * @return 화제의 인물의 책 리스트(저자)
     */
    public List<ContentDto.MainList> getPopularList() {
        ContentDto.MainList actorList = contentService.getPeopleList(ContentTypeParameter.BOOKS, POPULAR_JOB_WRITER, POPULAR_LIST_SIZE);
        return Collections.singletonList(actorList);
    }

    /**
     * 랜덤 태그를 뽑고 거기에 해당하는 작품들 조회.
     * @return 태그에 포함된 책 리스트들
     */
    public List<ContentDto.MainList> getTagList() {
        List<ContentDto.MainList> result = new ArrayList<>();
        List<Tag> tags = tagRepository.findByRandom(PageRequest.of(0, RANDOM_TAG_COUNT));
        for (Tag tag : tags) {
            ContentDto.MainList tagList = contentService.getTagList(ContentTypeParameter.BOOKS, tag, TAG_LIST_SIZE);
            result.add(tagList);
        }
        return result;
    }

    /**
     * 랜덤으로 뽑힌 컬렉션에 포함된 책을 조회.
     * @return 컬렉션에 포함된 책 리스트들
     */
    public List<ContentDto.MainListForCollection> getCollectionList() {
        List<ContentDto.MainListForCollection> result = new ArrayList<>();
        List<Collection> collections = collectionRepository.getRandom(ContentTypeParameter.BOOKS, RANDOM_COLLECTION_COUNT);

        for (Collection collection : collections) {
            ContentDto.MainListForCollection collectionList = contentService.getCollectionList(collection, COLLECTION_LIST_SIZE);
            result.add(collectionList);
        }
        return result;
    }

    /**
     * 왓챠피디아 지정 책 컬렉션과 컬렉션에 해당하는 이미지 리스트를 조회.
     * @return 왓챠피디아 컬렉션
     */
    public List<ContentDto.ListForAward> getAwardList() {
        return contentService.getAwardList(ContentTypeParameter.BOOKS);
    }
}
