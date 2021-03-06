package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.ImageCategory;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.DetailDto;
import com.devpedia.watchapedia.dto.ParticipantDto;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.InvalidFileException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.*;
import com.devpedia.watchapedia.repository.collection.CollectionRepository;
import com.devpedia.watchapedia.repository.content.ContentRepository;
import com.devpedia.watchapedia.repository.participant.ParticipantRepository;
import com.devpedia.watchapedia.repository.tag.TagRepository;
import com.devpedia.watchapedia.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public static final Integer AWARD_POSTER_IMAGE_COUNT = 4;

    public static final int SEARCH_RESULT_LIST_PAGE = 1;
    public static final int SEARCH_RESULT_LIST_SIZE = 9;

    public static final String CONTENT_JOB_AUTHOR = "저자";

    public static final int DETAIL_COMMENT_PAGE_SIZE = 3;
    public static final int DETAIL_COLLECTION_PAGE_SIZE = 5;
    public static final int DETAIL_SIMILAR_PAGE_SIZE = 12;

    private final S3Service s3Service;
    private final UserService userService;
    private final ContentRepository contentRepository;
    private final ParticipantRepository participantRepository;
    private final TagRepository tagRepository;
    private final CollectionRepository collectionRepository;
    private final ElasticSearchRepository searchRepository;
    private final CommentRepository commentRepository;
    private final ScoreRepository scoreRepository;
    private final InterestRepository interestRepository;
    private final CommentLikeRepository commentLikeRepository;

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

        List<Participant> participants = participantRepository.findAllById(map.keySet());

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

        List<Tag> addedTags = tagRepository.findAllById(tags);

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
     * @param type 컨텐츠 타입 Enum
     * @param score 기준 평점
     * @param size 반환 개수
     * @return 평점 리스트
     */
    public ContentDto.MainList getHighScoreList(ContentTypeParameter type, double score, int size) {
        List<Content> highScoreContents = contentRepository.getContentsScoreIsGreaterThan(type, score, size);
        return ContentDto.MainList.builder()
                .type(LIST_TYPE_SCORE)
                .title("평균별점이 높은 작품")
                .list(getContentsWithScore(highScoreContents))
                .build();
    }

    /**
     * 해당 컨텐츠 종류에서 해당 직업을 가진 인물 중
     * 가장 많은 작품에 참여한 인물의 작품 리스트를 가져온다.
     * @param type 컨텐츠 종류 Enum
     * @param job 직업(ex. 감독, 배우, 역자)
     * @param size 반환 개수
     * @return 화제의 인물 리스트
     */
    public ContentDto.MainList getPeopleList(ContentTypeParameter type, String job, int size) {
        Participant people = participantRepository.findMostFamous(type, job);
        List<Content> famousContents = contentRepository.getContentsHasParticipant(type, people.getId(), size);
        return ContentDto.MainList.builder()
                .type(LIST_TYPE_POPULAR)
                .title(String.format("화제의 %s %s의 작품", job, people.getName()))
                .list(getContentsWithScore(famousContents))
                .build();
    }

    /**
     * 해당 컨텐츠 종류에서 해당 태그가
     * 포함된 작품 리스트를 가져온다.
     * @param type 컨텐츠 종류 Enum
     * @param tag 태그
     * @param size 반환 개수
     * @return 화제의 인물 리스트
     */
    public ContentDto.MainList getTagList(ContentTypeParameter type, Tag tag, int size) {
        List<Content> taggedContents = contentRepository.getContentsTagged(type, tag.getId(), size);
        return ContentDto.MainList.builder()
                .type(LIST_TYPE_TAG)
                .title(String.format("#%s", tag.getDescription()))
                .list(getContentsWithScore(taggedContents))
                .build();
    }

    /**
     * 해당 컬렉션에
     * 포함된 작품 리스트를 가져온다.
     * @param collection 컬렉션
     * @param size 반환 개수
     * @return 화제의 인물 리스트
     */
    public ContentDto.MainListForCollection getCollectionList(Collection collection, int size) {
        List<Content> collectionMovies = contentRepository.getContentsInCollection(collection.getId(), PageRequest.of(0, size));
        return ContentDto.MainListForCollection.builder()
                .type(LIST_TYPE_COLLECTION)
                .collectionId(collection.getId())
                .userId(collection.getUser().getId())
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
    public List<ContentDto.MainListItem> getContentsWithScore(List<Content> contents) {
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
    private <T extends Content> Map<Long, Double> getContentScore(List<T> contents) {
        Set<Long> ids = contents.stream()
                .map(Content::getId)
                .collect(Collectors.toSet());
        return contentRepository.getContentScore(ids);
    }

    /**
     * 해당 컨텐츠 종류에 해당하는 왓챠피디아 컬렉션 리스트를 조회한다.
     * 조회결과는 컬렉션 정보, 보여줄 이미지 리스트
     * @param type 컨텐츠 종류 Enum
     * @return 왓챠피디아 컬렉션 리스트
     */
    public <T extends Content> List<ContentDto.ListForAward> getAwardList(ContentTypeParameter type) {
        List<Collection> awardCollection = collectionRepository.getAward(type);
        ContentDto.ListForAward awardList = ContentDto.ListForAward.builder()
                .type(LIST_TYPE_AWARD)
                .title("왓챠피디아 컬렉션")
                .list(convertCollections(awardCollection))
                .build();
        return Collections.singletonList(awardList);
    }

    /**
     * 왓챠피디아 컬렉션 상세 정보 및 컨텐츠 리스트 조회.
     * @param id 컬렉션 아이디
     * @param pageable pageable
     * @return 컬렉션 상세 정보 및 컨텐츠 리스트
     */
    public ContentDto.MainList getAwardDetail(Long id, Pageable pageable) {
        Optional<Collection> optionalCollection = collectionRepository.findById(id);
        Collection collection = optionalCollection.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        if (!collection.getUser().getId().equals(AWARD_ADMIN_ID))
            throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);

        List<Content> contents = contentRepository.getContentsInCollection(collection.getId(), pageable);
        return ContentDto.MainList.builder()
                .type(LIST_TYPE_AWARD)
                .title(collection.getTitle())
                .list(getContentsWithScore(contents))
                .build();
    }

    /**
     * 유저 컬렉션 상세 정보 및 컨텐츠 리스트 조회.
     * @param id 컬렉션 아이디
     * @param pageable pageable
     * @return 컬렉션 상세 정보 및 컨텐츠 리스트
     */
    public ContentDto.CollectionDetail getCollectionDetail(Long id, Pageable pageable) {
        Optional<Collection> optionalCollection = collectionRepository.findById(id);
        Collection collection = optionalCollection.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));

        Long contentCount = collectionRepository.countContentById(collection.getId());
        List<Content> contents = contentRepository.getContentsInCollection(collection.getId(), pageable);
        return ContentDto.CollectionDetail.builder()
                .userName(collection.getUser().getName())
                .title(collection.getTitle())
                .description(collection.getDescription())
                .contentCount(contentCount != null ? contentCount.intValue() : 0)
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
        List<UserDto.SearchUserItem> userList = userService.getUserSearchList(query, PageRequest.of(SEARCH_RESULT_LIST_PAGE - 1, SEARCH_RESULT_LIST_SIZE));

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
        List<Content> contents = contentRepository.findAllById(ids);
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

    /**
     * 컨텐츠 상세 정보를 조회한다.
     * @param contentId 컨텐츠 id
     * @param tokenId 토큰 유저 id
     * @return 컨텐츠 상세 정보
     */
    public DetailDto.ContentDetail getContentDetail(Long contentId, Long tokenId) {
        Optional<Content> optionalContent = contentRepository.findById(contentId);
        Content content = optionalContent.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));

        return DetailDto.ContentDetail.builder()
                .context(getUserContext(content, tokenId))
                .contentInfo(getContentInfo(content))
                .participants(getParticipants(content))
                .comments(getCommentInfo(content.getId(), tokenId, PageRequest.of(0, DETAIL_COMMENT_PAGE_SIZE)))
                .galleries(getGalleries(content))
                .scores(getScoreAnalysis(content))
                .collections(getCollectionInfo(content.getId(), PageRequest.of(0, DETAIL_COLLECTION_PAGE_SIZE)))
                .similar(getSimilar(content.getId(), PageRequest.of(0, DETAIL_SIMILAR_PAGE_SIZE)))
                .build();
    }

    /**
     * 컨텐츠 상세 페이지의 초기 코멘트 리스트와
     * 해당 컨텐츠의 총 코멘트 개수를 구한다.
     * @param contentId 컨텐츠 id
     * @param userId 토큰 유저 id
     * @param pageable pageable
     * @return 컨텐츠 코멘트 정보
     */
    public DetailDto.CommentInfo getCommentInfo(Long contentId, Long userId, Pageable pageable) {
        int count = contentRepository.countComments(contentId).intValue();
        List<DetailDto.CommentDetail> list = contentRepository.getComments(contentId, userId != null ? userId: 0, pageable);
        return DetailDto.CommentInfo.builder()
                .count(count)
                .list(list)
                .build();
    }

    /**
     * 해당 컨텐츠와 유사한 컨텐츠 리스트를 구한다.
     * 현재는 카테고리가 동일한 컨텐츠를 구한다.
     * @param contentId 컨텐츠 id
     * @param pageable pageable
     * @return 유사한 컨텐츠 리스트
     */
    public List<ContentDto.CollectionItem> getSimilar(Long contentId, Pageable pageable) {
        Optional<Content> optionalContent = contentRepository.findById(contentId);
        Content content = optionalContent.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));

        String category = content.getCategory().split("/")[0];
        List<Content> contents = contentRepository.findByCategoryContainingAndDtype(category, content.getDtype(), pageable);
        return getCollectionContentsWithScore(contents);
    }

    /**
     * 해당 컨텐츠를 포함하는 컬렉션 개수와 리스트를 구한다.
     * @param contentId 컨텐츠 id
     * @param pageable pageable
     * @return 컬렉션 정보
     */
    public DetailDto.CollectionInfo getCollectionInfo(Long contentId, Pageable pageable) {
        Page<Collection> collections = collectionRepository.getContentCollection(contentId, pageable);
        return DetailDto.CollectionInfo.builder()
                .count((int) collections.getTotalElements())
                .list(convertCollections(collections.getContent()))
                .build();
    }

    /**
     * 각 컬렉션 별 포함한 컨텐츠 4개의 포스터 정보를 구해서 리턴한다.
     * @param collections 컬렉션 리스트
     * @return 포함된 컨텐츠 4개의 포스터 정보
     */
    private List<ContentDto.CollectionFourImages> convertCollections(List<Collection> collections) {
        List<ContentDto.CollectionFourImages> list = new ArrayList<>();
        for (Collection collection : collections) {
            List<Content> contentsInCollection =
                    contentRepository.getContentsInCollection(collection.getId(), PageRequest.of(0, AWARD_POSTER_IMAGE_COUNT));
            list.add(new ContentDto.CollectionFourImages(collection, contentsInCollection));
        }
        return list;
    }

    /**
     * 컨텐츠의 평점 관련 통계 정보를 반환한다.
     * @param content 컨텐츠
     * @return 평점 정보
     */
    public DetailDto.ScoreAnalysis getScoreAnalysis(Content content) {
        return contentRepository.getScoreAnalysis(content.getId());
    }

    /**
     * 해당 유저의 해당 컨텐츠 관련 활동 정보를 반환한다.
     * @param content 컨텐츠
     * @param userId 유저 id
     * @return 활동 정보
     */
    private DetailDto.UserContext getUserContext(Content content, Long userId) {
        if (userId == null) return null;
        return contentRepository.getUserContext(content.getId(), userId);
    }

    /**
     * 컨텐츠 엔티티로부터 각 형태에 맞는 컨텐츠 정보를 반환한다.
     * @param content 컨텐츠
     * @return 컨텐츠 정보
     */
    public Object getContentInfo(Content content) {
        Object result = null;

        if (content instanceof Movie) {
            result = getMovieInfo((Movie) content);
        } else if (content instanceof Book) {
            result = getBookInfo((Book) content);
        } else if (content instanceof TvShow){
            result = getTvShowInfo((TvShow) content);
        }

        return result;
    }

    /**
     * 영화의 상세 정보를 구한다.
     * @param movie 영화
     * @return 영화 상세 정보
     */
    private DetailDto.MovieDetail getMovieInfo(Movie movie) {
        return DetailDto.MovieDetail.of(movie);
    }

    /**
     * 책의 상세 정보를 구한다.
     * 책의 경우 저자의 정보도 같이 반환함.
     * @param book 책
     * @return 책 상세 정보
     */
    private DetailDto.BookDetail getBookInfo(Book book) {
        List<Participant> authors = participantRepository.findContentParticipantHasJob(book.getId(), CONTENT_JOB_AUTHOR);
        Participant author = authors.size() > 0 ? authors.get(0) : null;
        return DetailDto.BookDetail.of(book, author);
    }

    /**
     * 티비프로그램 상세 정보를 구한다.
     * @param tvShow 티비프로그램
     * @return 티비 상세 정보
     */
    private DetailDto.TvShowDetail getTvShowInfo(TvShow tvShow) {
        return DetailDto.TvShowDetail.of(tvShow);
    }

    /**
     * 해당 컨텐츠에 참여한 참여자 리스트를 구한다.
     * @param content 컨텐츠
     * @return 참여자 리스트
     */
    public List<DetailDto.ContentRole> getParticipants(Content content) {
        return content.getParticipants().stream()
                .map(DetailDto.ContentRole::of)
                .collect(Collectors.toList());
    }

    /**
     * 해당 컨텐츠의 갤러리 이미지 리스트를 구한다.
     * @param content 컨텐츠
     * @return 갤러리 이미지 리스트
     */
    public List<String> getGalleries(Content content) {
        return content.getImages().stream()
                .map(contentImage -> UrlUtil.getCloudFrontUrl(contentImage.getImage().getPath()))
                .collect(Collectors.toList());
    }

    /**
     * 코멘트를 생성하거나 수정한다,
     * 코멘트가 존재하지 않으면 생성, 이미 존재하면 수정하여 적용함.
     * @param contentId 컨텐츠 Id
     * @param userId 코멘트 유저 id
     * @param request 코멘트 요청(내용)
     */
    public void createOrEditComment(Long contentId, Long userId, DetailDto.CommentRequest request) {
        User user = userService.getUserIfExistOrThrow(userId);
        Content content = getContentIfExistOrThrow(contentId);

        Optional<Comment> optionalComment = commentRepository.findById(new Comment.CommentId(user.getId(), content.getId()));
        if (optionalComment.isEmpty()) {
            Comment comment = Comment.builder()
                    .user(user)
                    .content(content)
                    .description(request.getDescription())
                    .containsSpoiler(false)
                    .build();
            commentRepository.save(comment);
        } else {
            Comment comment = optionalComment.get();
            comment.edit(request.getDescription());
        }
    }

    /**
     * 코멘트를 조회 후 존재하면 삭제한다.
     * @param contentId 컨텐츠 id
     * @param userId 코멘트 유저 id
     */
    public void deleteComment(Long contentId, Long userId) {
        User user = userService.getUserIfExistOrThrow(userId);
        Content content = getContentIfExistOrThrow(contentId);
        Optional<Comment> optionalComment = commentRepository.findById(new Comment.CommentId(user.getId(), content.getId()));
        Comment comment = optionalComment.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        commentRepository.delete(comment);
    }

    /**
     * 컨텐츠를 가져오고 존재하지 않으면 Exception 을 던진다.
     * @param contentId 컨텐츠 id
     * @return 컨텐츠
     */
    private Content getContentIfExistOrThrow(Long contentId) {
        Optional<Content> optionalContent = contentRepository.findById(contentId);
        return optionalContent.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
    }

    /**
     * 평점을 생성하거나 수정한다,
     * 평점이 존재하지 않으면 생성, 이미 존재하면 수정하여 적용함.
     * @param contentId 컨텐츠 Id
     * @param userId 평점 유저 id
     * @param request 평점 요청(점수)
     */
    public void createOrEditScore(Long contentId, Long userId, DetailDto.ScoreRequest request) {
        User user = userService.getUserIfExistOrThrow(userId);
        Content content = getContentIfExistOrThrow(contentId);

        Optional<Score> optionalScore = scoreRepository.findById(new Score.ScoreId(user.getId(), content.getId()));
        if (optionalScore.isEmpty()) {
            Score score = Score.builder()
                    .user(user)
                    .content(content)
                    .score(request.getScore())
                    .build();
            scoreRepository.save(score);
        } else {
            Score score = optionalScore.get();
            score.edit(request.getScore());
        }
    }

    /**
     * 관심 정보를 생성하거나 수정한다,
     * 관심 정보가 존재하지 않으면 생성, 이미 존재하면 수정하여 적용함.
     * @param contentId 컨텐츠 Id
     * @param userId 관심 유저 id
     * @param request 관심 요청(관심 상태)
     */
    public void createOrEditInterest(Long contentId, Long userId, DetailDto.InterestRequest request) {
        User user = userService.getUserIfExistOrThrow(userId);
        Content content = getContentIfExistOrThrow(contentId);

        Optional<Interest> optionalInterest = interestRepository.findById(new Interest.InterestId(user.getId(), content.getId()));
        if (optionalInterest.isEmpty()) {
            Interest interest = Interest.builder()
                    .user(user)
                    .content(content)
                    .state(request.getState())
                    .build();
            interestRepository.save(interest);
        } else {
            Interest interest = optionalInterest.get();
            interest.edit(request.getState());
        }
    }

    /**
     * 유저의 평점 정보를 삭제한다.
     * @param contentId 컨텐츠 id
     * @param userId 평점 유저 id
     */
    public void deleteScore(Long contentId, Long userId) {
        User user = userService.getUserIfExistOrThrow(userId);
        Content content = getContentIfExistOrThrow(contentId);
        Optional<Score> optionalScore = scoreRepository.findById(new Score.ScoreId(user.getId(), content.getId()));
        Score score = optionalScore.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        scoreRepository.delete(score);
    }

    /**
     * 유저의 관심 정보를 삭제한다.
     * @param contentId 컨텐츠 id
     * @param userId 관심 유저 id
     */
    public void deleteInterest(Long contentId, Long userId) {
        User user = userService.getUserIfExistOrThrow(userId);
        Content content = getContentIfExistOrThrow(contentId);
        Optional<Interest> optionalInterest = interestRepository.findById(new Interest.InterestId(user.getId(), content.getId()));
        Interest interest = optionalInterest.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        interestRepository.delete(interest);
    }

    /**
     * 코멘트에 좋아요 표시를 한다.
     * @param contentId 컨텐츠 id
     * @param commentUserId 코멘트 유저 id
     * @param likeUserId 좋아요 유저 id
     */
    public void createCommentLike(Long contentId, Long commentUserId, Long likeUserId) {
        Comment comment = getCommentIfExistOrThrow(contentId, commentUserId);
        User user = userService.getUserIfExistOrThrow(likeUserId);

        Optional<CommentLike> optionalLike = commentLikeRepository.findById(new CommentLike.CommentLikeId(comment.getId(), likeUserId));
        if (optionalLike.isEmpty()) {
            CommentLike like = CommentLike.builder()
                    .comment(comment)
                    .user(user)
                    .build();
            commentLikeRepository.save(like);
        }
    }

    /**
     * 코멘트에 좋아요를 삭제한다.
     * @param contentId 컨텐츠 id
     * @param commentUserId 코멘트 유저 id
     * @param likeUserId 좋아요 유저 id
     */
    public void deleteCommentLike(Long contentId, Long commentUserId, Long likeUserId) {
        Comment comment = getCommentIfExistOrThrow(contentId, commentUserId);
        Optional<CommentLike> optionalLike = commentLikeRepository.findById(new CommentLike.CommentLikeId(comment.getId(), likeUserId));
        CommentLike commentLike = optionalLike.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        commentLikeRepository.delete(commentLike);
    }

    /**
     * 코멘트가 존재하면 가져오고 없으면 Exception
     * @param contentId 컨텐츠 id
     * @param commentUserId 코멘트 유저 id
     * @return 코멘트
     */
    private Comment getCommentIfExistOrThrow(Long contentId, Long commentUserId) {
        Optional<Comment> optionalComment = commentRepository.findById(new Comment.CommentId(commentUserId, contentId));
        return optionalComment.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
    }

    /**
     * 코멘트 상세 정보를 가져온다.
     * @param contentId 컨텐츠 id
     * @param commentUserId 코멘트 유저 id
     * @param tokenId 토큰 유저
     * @return 코멘트 상세 정보
     */
    public DetailDto.CommentDetail getCommentDetail(Long contentId, Long commentUserId, Long tokenId) {
        DetailDto.CommentDetail comment = contentRepository.getComment(contentId, commentUserId, tokenId);
        if (comment == null) throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        return comment;
    }
}
