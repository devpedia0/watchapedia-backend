package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.ImageCategory;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.ParticipantDto;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.InvalidFileException;
import com.devpedia.watchapedia.exception.ValueNotMatchException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ContentService {

    public static final String LIST_TYPE_SCORE = "score";
    public static final String LIST_TYPE_POPULAR = "people";
    public static final String LIST_TYPE_TAG = "tag";
    public static final String LIST_TYPE_COLLECTION = "collection";
    public static final String LIST_TYPE_AWARD = "award";
    public static final Long AWARD_ADMIN_ID = 1L;

    public static final int SEARCH_RESULT_LIST_PAGE = 1;
    public static final int SEARCH_RESULT_LIST_SIZE = 9;




    private final S3Service s3Service;
    private final UserService userService;
    private final ContentRepository contentRepository;
    private final ParticipantRepository participantRepository;
    private final TagRepository tagRepository;
    private final CollectionRepository collectionRepository;
    private final ElasticSearchRepository searchRepository;

    /**
     * 컨텐츠와 컨텐츠에 해당하는 태그, 인물, 갤러리 등을 저장한다.
     * @param content 컨텐츠
     * @param poster 포스터 이미지
     * @param children 인물, 태그, 갤러리
     */
    protected void createContent(Content content, MultipartFile poster, ContentDto.ContentChildren children) {
        addPosterImage(content, poster);
        addChildren(content, children.getRoles(), children.getTags(), children.getGallery());
        contentRepository.save(content);
    }

    /**
     * S3에 포스터 이미지를 업로드하고 이미지를 컨텐츠에 Set
     * @param content 컨텐츠
     * @param poster 포스터 이미지
     */
    private void addPosterImage(Content content, MultipartFile poster) {
        Image posterImage = createImage(poster, ImageCategory.POSTER);
        content.setPosterImage(posterImage);
    }

    /**
     * Multipart 를 Image 로 변환해 반환하고 S3에 업로드한다.
     * @param image 이미지 파일
     * @param category 이미지 종류
     * @return 만들어진 Image 엔티티
     */
    private Image createImage(MultipartFile image, ImageCategory category) {
        if (isInvalidImageFile(image))
            throw new InvalidFileException(ErrorCode.IMAGE_FORMAT_INVALID, "이미지 파일이 올바르지 않습니다");

        Image posterImage = Image.of(image, category);
        s3Service.upload(image, posterImage.getPath());

        return posterImage;
    }

    /**
     * 컨텐츠에 해당하는 자식들을 컨텐츠에 추가한다.
     * @param content 컨텐츠
     * @param roles 포함된 인물들과 역할, 역할명
     * @param tags 태그
     * @param gallery 갤러리 이미지
     */
    private void addChildren(Content content, List<ParticipantDto.ParticipantRole> roles, List<Long> tags, List<MultipartFile> gallery) {
        addParticipants(content, roles);
        addTags(content, tags);
        addGallery(content, gallery);
    }

    /**
     * 컨텐츠에 포함된 인물들을 Set 한다.
     * @param content 컨텐츠
     * @param roles 인물 및 역할, 역할명
     */
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

    /**
     * 컨텐츠에 태그들을 Set.
     * @param content 컨텐츠
     * @param tags 태그
     */
    private void addTags(Content content, List<Long> tags) {
        if (content == null || tags == null) return;

        List<Tag> addedTags = tagRepository.findListIn(new HashSet<>(tags));

        for (Tag tag : addedTags) {
            content.addTag(tag);
        }
    }

    /**
     * 갤러리 이미지를 S3에 업로드하고 컨텐츠에 Set
     * @param content 컨텐츠
     * @param gallery 갤러리 이미지 리스트
     */
    private void addGallery(Content content, List<MultipartFile> gallery) {
        if (content == null || gallery == null) return;

        for (MultipartFile file : gallery) {
            Image galleryImage = createImage(file, ImageCategory.GALLERY);
            content.addImage(galleryImage);
        }
    }

    /**
     * 컨텐츠 이미지 저장 시 올바른 형식의 파일인지 체크한다.
     * @param file 이미지 파일
     * @return 이미지 올바른 파일 여부
     */
    private boolean isInvalidImageFile(MultipartFile file) {
        return file.isEmpty() || file.getSize() == 0 ||
                file.getContentType() == null || !file.getContentType().contains("image");
    }

    /**
     * 해당 컨텐츠 종류 중 특정 평점 이상인 작품을 개수만큼 조회한다.
     * @param tClass 컨텐츠 종류 Class
     * @param score 기준 평점
     * @param size 반환 개수
     * @return 평점 리스트
     */
    public <T extends Content> ContentDto.MainList getHighScoreList(Class<T> tClass, double score, int size) {
        List<T> movies = contentRepository.getContentsScoreIsGreaterThan(tClass, score, size);
        return ContentDto.MainList.builder()
                .type(LIST_TYPE_SCORE)
                .title("평균별점이 높은 작품")
                .list(getContentsWithScore(movies))
                .build();
    }

    /**
     * 해당 컨텐츠 종류에서 해당 직업을 가진 인물 중
     * 가장 많은 작품에 참여한 인물의 작품 리스트를 가져온다.
     * @param tClass 컨텐츠 종류 Class
     * @param job 직업(ex. 감독, 배우, 역자)
     * @param size 반환 개수
     * @return 화제의 인물 리스트
     */
    public <T extends Content> ContentDto.MainList getPeopleList(Class<T> tClass, String job, int size) {
        Participant people = participantRepository.findMostParticipatedHasJob(classTypeToString(tClass), job);
        List<T> contents = contentRepository.getContentsHasParticipant(tClass, people, size);
        return ContentDto.MainList.builder()
                .type(LIST_TYPE_POPULAR)
                .title(String.format("화제의 %s %s의 작품", job, people.getName()))
                .list(getContentsWithScore(contents))
                .build();
    }

    /**
     * 해당 컨텐츠 종류에서 해당 태그가
     * 포함된 작품 리스트를 가져온다.
     * @param tClass 컨텐츠 종류 Class
     * @param tag 태그
     * @param size 반환 개수
     * @return 화제의 인물 리스트
     */
    public <T extends Content> ContentDto.MainList getTagList(Class<T> tClass, Tag tag, int size) {
        List<T> tagMovies = contentRepository.getContentsTagged(tClass, tag, size);
        return ContentDto.MainList.builder()
                .type(LIST_TYPE_TAG)
                .title(String.format("#%s", tag.getDescription()))
                .list(getContentsWithScore(tagMovies))
                .build();
    }

    /**
     * 해당 컨텐츠 종류에서 해당 컬렉션에
     * 포함된 작품 리스트를 가져온다.
     * @param tClass 컨텐츠 종류 Class
     * @param collection 컬렉션
     * @param size 반환 개수
     * @return 화제의 인물 리스트
     */
    public <T extends Content> ContentDto.MainListForCollection getCollectionList(Class<T> tClass, Collection collection,
                                                                                  int size) {
        List<T> collectionMovies = contentRepository.getContentsInCollection(tClass, collection, 1, size);
        return ContentDto.MainListForCollection.builder()
                .type(LIST_TYPE_COLLECTION)
                .id(collection.getId())
                .title(String.format("%s님의 컬렉션", collection.getUser().getName()))
                .subtitle(collection.getTitle())
                .list(getContentsWithScore(collectionMovies))
                .build();
    }

    /**
     * 주어진 컨텐츠에 평균 평점을 Set 해서 반환한다.
     * @param contents 컨텐츠 리스트
     * @return 평균 평점이 포함된 리스트
     */
    public <T extends Content> List<ContentDto.MainListItem> getContentsWithScore(List<T> contents) {
        Map<Long, Double> contentScore = getContentScore(contents);
        return contents.stream()
                .map(content -> ContentDto.MainListItem.of(content, contentScore.get(content.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 컬렉션의 평균평점을 구해서
     * key: content_id, value: 평균평점
     * 형태의 Map 으로 반환한다.
     * @param contents 컨텐츠 리스트
     * @return 평균평점 Map
     */
    protected <T extends Content> Map<Long, Double> getContentScore(List<T> contents) {
        Set<Long> ids = contents.stream()
                .map(Content::getId)
                .collect(Collectors.toSet());
        return contentRepository.getContentScore(ids);
    }

    /**
     * 해당 컨텐츠 종류에 해당하는 왓챠피디아 컬렉션 리스트를 조회한다.
     * 조회결과는 컬렉션 정보, 보여줄 이미지 리스트
     * @param tClass 컨텐츠 종류 Class
     * @return 왓챠피디아 컬렉션 리스트
     */
    public <T extends Content> List<ContentDto.ListForAward> getAwardList(Class<T> tClass) {
        List<Collection> awardCollection = collectionRepository.getAward(classTypeToString(tClass));

        List<ContentDto.AwardItem> items = new ArrayList<>();
        for (Collection collection : awardCollection) {
            List<T> contentsInCollection = contentRepository.getContentsInCollection(tClass, collection, 1, 4);
            items.add(new ContentDto.AwardItem(collection, contentsInCollection));
        }
        ContentDto.ListForAward awardList = ContentDto.ListForAward.builder()
                .type(LIST_TYPE_AWARD)
                .title("왓챠피디아 컬렉션")
                .list(items)
                .build();

        return Collections.singletonList(awardList);
    }

    /**
     * 컨텐츠 클래스 타입을 DB에 저장되는 String 값로 변환한다.
     * @param tClass 컨텐츠 종류 Class
     * @return 컨텐츠 종류 String
     */
    public <T extends Content> String classTypeToString(Class<T> tClass) {
        if (tClass == Movie.class) return "M";
        else if (tClass == Book.class) return "B";
        else if (tClass == TvShow.class) return "S";
        else throw new ValueNotMatchException(ErrorCode.CONTENT_TYPE_NOT_VALID);
    }

    /**
     * 왓챠피디아 컬렉션 상세 정보 및 컨텐츠 리스트 조회.
     * @param id 컬렉션 아이디
     * @param page 페이지
     * @param size 사이즈
     * @return 컬렉션 상세 정보 및 컨텐츠 리스트
     */
    public ContentDto.MainList getAwardDetail(Long id, int page, int size) {
        Collection collection = collectionRepository.findById(id);
        if (collection == null || !collection.getUser().getId().equals(AWARD_ADMIN_ID))
            throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);

        List<Content> contents = contentRepository.getContentsInCollection(Content.class, collection, page, size);
        return ContentDto.MainList.builder()
                .type(LIST_TYPE_AWARD)
                .title(collection.getTitle())
                .list(getContentsWithScore(contents))
                .build();
    }

    /**
     * 유저 컬렉션 상세 정보 및 컨텐츠 리스트 조회.
     * @param id 컬렉션 아이디
     * @param page 페이지
     * @param size 사이즈
     * @return 컬렉션 상세 정보 및 컨텐츠 리스트
     */
    public ContentDto.CollectionDetail getCollectionDetail(Long id, int page, int size) {
        Collection collection = collectionRepository.findById(id);
        if (collection == null)
            throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);

        Integer contentCount = collectionRepository.getContentCount(collection.getId());
        List<Content> contents = contentRepository.getContentsInCollection(Content.class, collection, page, size);
        return ContentDto.CollectionDetail.builder()
                .userName(collection.getUser().getName())
                .title(collection.getTitle())
                .description(collection.getDescription())
                .contentCount(contentCount)
                .list(getCollectionContentsWithScore(contents))
                .build();
    }

    /**
     * 주어진 컨텐츠에 평균 평점을 Set 해서 반환한다.
     * @param contents 컨텐츠 리스트
     * @return 평균 평점이 포함된 리스트
     */
    public <T extends Content> List<ContentDto.CollectionItem> getCollectionContentsWithScore(List<T> contents) {
        Map<Long, Double> contentScore = getContentScore(contents);
        return contents.stream()
                .map(content -> ContentDto.CollectionItem.of(content, contentScore.get(content.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 통합 검색 결과를 반환한다.
     * - 상위 검색 결과
     * - 영화
     * - 티비쇼
     * - 책
     * - 유저
     * @param query 검색어
     * @return 통합 검색 결과
     */
    public ContentDto.SearchResult getSearchResult(String query) throws IOException {
        Map<String, List<Long>> ids = searchRepository.searchAllContentsReturnIds(query, SEARCH_RESULT_LIST_PAGE, SEARCH_RESULT_LIST_SIZE);
        List<Object> topList = getSearchList(ids.get(ElasticSearchRepository.TYPE_TOP_RESULT));
        List<Object> movieList = getSearchList(ids.get(ElasticSearchRepository.TYPE_MOVIE));
        List<Object> tvShowList = getSearchList(ids.get(ElasticSearchRepository.TYPE_TV_SHOW));
        List<Object> bookList = getSearchList(ids.get(ElasticSearchRepository.TYPE_BOOK));
        List<UserDto.SearchUserItem> userList = userService.getUserSearchList(query, SEARCH_RESULT_LIST_PAGE, SEARCH_RESULT_LIST_SIZE);

        return ContentDto.SearchResult.builder()
                .topResults(topList)
                .movies(movieList)
                .tvShows(tvShowList)
                .books(bookList)
                .users(userList)
                .build();
    }

    /**
     * 컨텐츠 타입 별 검색 결과를 반환한다.
     * @param typeParameter 컨텐츠 타입 Enum
     * @param query 검색어
     * @param page 페이지
     * @param size 사이즈
     * @return 검색 결과
     */
    public List<Object> searchByType(ContentTypeParameter typeParameter, String query, int page, int size) throws IOException {
        List<Long> ids = searchRepository.searchTypeContentsReturnIds(typeParameter.getDtype(), query, page, size);
        return getSearchList(ids);
    }

    /**
     * 컨텐츠의 ID 리스트를 인자로 받아서
     * 해당 컨텐츠를 조회 후 컨텐츠 타입 별로
     * 알맞은 DTO 형태로 변환해서 반환한다.
     * @param ids 컨텐츠 ID 리스트
     * @return 검색 결과 DTO 리스트(SearchMovieItem, SearchTvShowItem, SearchBookItem)
     */
    private List<Object> getSearchList(List<Long> ids) {
        List<Content> contents = contentRepository.findListIn(Content.class, new HashSet<>(ids));
        List<Object> result = new ArrayList<>();

        for (Content content : contents) {
            if (content instanceof Movie)
                result.add(ContentDto.SearchMovieItem.of((Movie) content));
            else if (content instanceof TvShow)
                result.add(ContentDto.SearchTvShowItem.of((TvShow) content));
            else if (content instanceof Book)
                result.add(ContentDto.SearchBookItem.of((Book) content));
        }

        return result;
    }
}
