package com.devpedia.watchapedia.service;
import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.dto.*;
import com.devpedia.watchapedia.repository.DetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DetailService {
    private final DetailRepository detailRepository;
    private static final String LIST_TYPE_AWARD = "award";

    /**
     * 상세 페이지 정보
     * @param contentId 컨텐츠 아이디
     * @param userId 유저 아이디(로그인 체크)
     * @return 상세페이지 기본정보, 유저 평가정보, 비슷한작품, 랭킹 정보, 컬렉션 정보 , 코멘트 정
     */
    public DetailDto.DetailContentInfoList detailInfoList(long contentId, Long userId) {
        Movie movie = null;
        TvShow tvShow = null;
        Book book = null;

        List<ParticipantDto.ParticipantInfo> participantList = new ArrayList<>();
        List<ImageDto.ImageInfo> imageList = new ArrayList<>();
        List<TagDto.TagInfo> tagList = new ArrayList<>();

        DetailDto.DetailContentInfoList detailContentInfoList = null;
        DetailDto.DetailUserCommentInfoList detailUserCommentInfoList = null;
        Set<Long> detailUserLikeStateSet = new HashSet<>();
        String dtype = "";

        List<Object> similarContentsList = new ArrayList<>();
        Movie movieSimilarContents = null;
        TvShow tvShowSimilarContents = null;
        Book bookSimilarContents = null;

        Content content = detailRepository.getType(contentId);
        dtype = content.getDtype();

        // 랭킹 평균 점수 정보
        DetailDto.DetailRankingScoreInfoList detailRankingScoreInfoList = detailRepository.getRankingContentScore(contentId);
        Map<Double, Integer> detailScoreCountList = detailRepository.getScoreCount(contentId, dtype);

        // 유저 평가 정보
        if(userId != null) {
            detailUserCommentInfoList= detailRepository.findDetailUserInfo(userId, contentId);
            detailUserLikeStateSet = detailRepository.findDetailUserLikeStateInfo(userId, contentId);
        }

        // 타입별 컨텐츠 정보
        List<Collection> awardCollection = detailRepository.getAward(dtype);
        if(dtype.equals("M")) {
            movie = detailRepository.findByContentId(Movie.class, contentId);
        } else if(dtype.equals("S")) {
            tvShow = detailRepository.findByContentId(TvShow.class, contentId);
        } else if(dtype.equals("B")) {
            book = detailRepository.findByContentId(Book.class, contentId);
        }

        // 컬렉션 정보
        List<ContentDto.AwardItem> collectionItemList = new ArrayList<>();
        for (Collection collection : awardCollection) {
            if(dtype.equals("M")) {
                List<Movie> contentsInCollection = detailRepository.getContentsInCollection(Movie.class, collection, 4);
                collectionItemList.add(new ContentDto.AwardItem(collection, contentsInCollection));
            } else if(dtype.equals("S")) {
                List<TvShow> contentsInCollection = detailRepository.getContentsInCollection(TvShow.class, collection, 4);
                collectionItemList.add(new ContentDto.AwardItem(collection, contentsInCollection));
            } else if(dtype.equals("B")) {
                List<Book> contentsInCollection = detailRepository.getContentsInCollection(Book.class, collection, 4);
                collectionItemList.add(new ContentDto.AwardItem(collection, contentsInCollection));
            }
        }
        ContentDto.ListForAward awardList = ContentDto.ListForAward.builder()
                .type(LIST_TYPE_AWARD)
                .title("왓챠피디아 컬렉션")
                .list(collectionItemList)
                .build();

        // 태그, 출연/배우, 컨텐츠 이미지 정보
        List<Tag> tags = detailRepository.getTagList(contentId);
        List<Participant> participants = detailRepository.getParticipantList(contentId);
        List<Image> images = detailRepository.getImageList(contentId);

        // 전체 코멘트 개수
        Long allCommentCount = detailRepository.getAllCommentCount(contentId);

        // 코멘트 정보
        List<DetailDto.addDetailCommentInfoList> detailCommentInfoList = detailRepository.getCommentInfoList(contentId, detailUserLikeStateSet);

        // 출연/배우 정보
        for(Participant participant : participants){
            participantList.add(new ParticipantDto.ParticipantInfo(participant));
        }
        // 컨텐츠 이미지 정보
        for(Image image : images){
            imageList.add(new ImageDto.ImageInfo(image));
        }
        // 비슷한 작품 정보
        for(Tag tag : tags){
            Set<Long> s = detailRepository.getSimilarContentsId(tag.getId(), dtype);
            tagList.add(new TagDto.TagInfo(tag.getId(),tag.getDescription()));
            for(Long contentIds : s){
                Content contents = detailRepository.getType(contentIds);
                // 랭킹 평균 점수
                DetailDto.DetailRankingScoreInfoList detailRankingUserScoreInfoList = detailRepository.getRankingContentScore(contentIds);
                Map<Double, Integer> detailUserScoreCountList = detailRepository.getScoreCount(contentIds, dtype);
                if(dtype.equals("M")) {
                    movieSimilarContents =detailRepository.findListIn(Movie.class, contentIds);
                } else if(dtype.equals("S")) {
                    tvShowSimilarContents = detailRepository.findListIn(TvShow.class,contentIds);
                } else if(dtype.equals("B")) {
                    bookSimilarContents = detailRepository.findListIn(Book.class,contentIds);
                }
                similarContentsList.add(setDetailSmilarInfo(contents
                        ,detailRankingUserScoreInfoList
                        , detailUserScoreCountList,movieSimilarContents,tvShowSimilarContents,bookSimilarContents,dtype));
            }
        }
        // 최종 상세 정보 리스트
        detailContentInfoList = new DetailDto.DetailContentInfoList(content.getMainTitle(), dtype, setDetailInfo(
                movie, tvShow, book, dtype, content, participantList, imageList, tagList, detailCommentInfoList, detailRankingScoreInfoList, detailScoreCountList,awardList,similarContentsList, detailUserCommentInfoList,allCommentCount));
        return detailContentInfoList;
    }
    private Object setDetailInfo(Movie movie, TvShow tvShow
            , Book book
            , String dtype
            , Content content
            , List<ParticipantDto.ParticipantInfo> participantList
            , List<ImageDto.ImageInfo> imageList
            , List<TagDto.TagInfo> tagList
            , List<DetailDto.addDetailCommentInfoList> detailCommentInfoList
            , DetailDto.DetailRankingScoreInfoList detailRankingScoreInfoList
            , Map<Double, Integer> detailScoreCountList
            , ContentDto.ListForAward awardList
            , List<Object> similarContentsList
            , DetailDto.DetailUserCommentInfoList detailUserCommentInfoList
            , Long allCommentCount) {
        DetailDto.addDetailInfo addDetailMovieInfo = null;
        DetailDto.addDetailInfo addDetailTvShowInfo = null;
        DetailDto.addDetailInfo addDetailBookInfo = null;
        if(movie != null && dtype.equals("M")){
            addDetailMovieInfo = new DetailDto.addDetailInfo(
                    content
                    , detailRankingScoreInfoList
                    , detailScoreCountList
                    , movie.getCategory()
                    , movie.getDescription()
                    , movie.getOriginTitle()
                    , movie.getCountryCode()
                    , movie.getIsNetflixContent()
                    , movie.getIsWatchaContent()
                    , detailCommentInfoList
                    , participantList
                    , imageList
                    , tagList
                    , awardList
                    , similarContentsList
                    , detailUserCommentInfoList
                    , allCommentCount);
        }else if(tvShow != null && dtype.equals("S")){
            addDetailTvShowInfo= new DetailDto.addDetailInfo(
                    content
                    , detailRankingScoreInfoList
                    , detailScoreCountList
                    , tvShow.getCategory()
                    , tvShow.getDescription()
                    , tvShow.getOriginTitle()
                    , tvShow.getCountryCode()
                    , tvShow.getIsNetflixContent()
                    , tvShow.getIsWatchaContent()
                    , detailCommentInfoList
                    , participantList
                    , imageList
                    , tagList
                    , awardList
                    , similarContentsList
                    , detailUserCommentInfoList
                    , allCommentCount);
        }
        else if(book != null && dtype.equals("B")){
            addDetailBookInfo = new DetailDto.addDetailInfo(
                    content
                    , detailRankingScoreInfoList
                    , detailScoreCountList
                    , book.getCategory()
                    , book.getProductionDate()
                    , book.getDescription()
                    , detailCommentInfoList
                    , participantList
                    , imageList
                    , tagList
                    , awardList
                    , similarContentsList
                    , detailUserCommentInfoList
                    , allCommentCount);
        }

        if(addDetailMovieInfo != null){
            return addDetailMovieInfo;
        }else if(addDetailTvShowInfo != null){
            return addDetailTvShowInfo;
        }else {
            return addDetailBookInfo;
        }
    }

    private Object setDetailSmilarInfo(Content content, DetailDto.DetailRankingScoreInfoList detailRankingUserScoreInfoList, Map<Double, Integer> detailUserScoreCountList, Movie movie, TvShow tvShow, Book book, String dtype){
        DetailDto.addDetailSimilarInfo addDetailMovieInfo = null;
        DetailDto.addDetailSimilarInfo addDetailTvShowInfo = null;
        DetailDto.addDetailSimilarInfo addDetailBookInfo = null;

        if(movie != null && dtype.equals("M")){
            addDetailMovieInfo = new DetailDto.addDetailSimilarInfo(
                    content
                    , detailRankingUserScoreInfoList
                    , detailUserScoreCountList
                    , movie.getCategory()
                    , movie.getDescription()
                    , movie.getOriginTitle()
                    , movie.getCountryCode()
                    , movie.getIsNetflixContent()
                    , movie.getIsWatchaContent()
                   );
        }else if(tvShow != null && dtype.equals("S")){
            addDetailTvShowInfo= new DetailDto.addDetailSimilarInfo(
                    content
                    , detailRankingUserScoreInfoList
                    , detailUserScoreCountList
                    , tvShow.getCategory()
                    , tvShow.getDescription()
                    , tvShow.getOriginTitle()
                    , tvShow.getCountryCode()
                    , tvShow.getIsNetflixContent()
                    , tvShow.getIsWatchaContent()
            );
        }
        else if(book != null && dtype.equals("B")){
            addDetailBookInfo = new DetailDto.addDetailSimilarInfo(
                    content
                    , detailRankingUserScoreInfoList
                    , detailUserScoreCountList
                    , book.getCategory()
                    , book.getProductionDate()
                    , book.getDescription());
        }

        if(addDetailMovieInfo != null){
            return addDetailMovieInfo;
        }else if(addDetailTvShowInfo != null){
            return addDetailTvShowInfo;
        }else {
            return addDetailBookInfo;
        }
    }

    public DetailDto.DetailUserCommentInfoList searchDetailInfo(Long userId, long contentId) {
        return detailRepository.findDetailUserInfo(userId, contentId);
    }

    public List<DetailDto.addDetailParticipantInfoList> searchParticipantDetailInfo(long participantId) {
        return detailRepository.findDetailParticipantInfo(participantId);
    }
    public Long insertDetailLike(DetailDto.detailCommentLikeInfo request) {
        return detailRepository.insertDetailLike(request);
    }

    public Long deleteDetailLike(DetailDto.detailCommentLikeInfo request) {
        return detailRepository.deleteDetailLike(request);
    }

    public Long insertDetailComment(DetailDto.detailCommentInsertInfo request) {
        return detailRepository.insertDetailComment(request);
    }

    public Long deleteDetailComment(DetailDto.detailCommentDeleteInfo request) {
        return detailRepository.deleteDetailComment(request);
    }

    public Long updateDetailComment(DetailDto.detailCommentUpdateInfo request) {
        return detailRepository.updateDetailComment(request);
    }
}
