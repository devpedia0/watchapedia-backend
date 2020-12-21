package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.dto.DetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DetailRepository {
    private final EntityManager em;

    /**
     * 컨텐츠에 달린 코멘트 개수 정보
     * @param contentId 컨텐츠 아이디
     * @return 컨텐츠 코멘트 개수
     */
    public Long getAllCommentCount(long contentId) {
        Long commentCount = (Long)em.createQuery(
                "select count(c.user.id) from Comment c " +
                         "where c.content.id = :contentId")
                .setParameter("contentId", contentId)
                .getSingleResult();
        return commentCount;
    }

    /**
     * 컨텐츠에 속한 각 코멘트별 상세 정보
     * @param contentId 컨텐츠 아이디
     * @param detailUserLikeStateSet 코멘트별 좋아요 상태 Set
     * @return 코멘트 상세 정보 리스트
     */
    public List<DetailDto.addDetailCommentInfoList> getCommentInfoList(Long contentId, Set<Long> detailUserLikeStateSet) {
        List<Object[]> commentObj= em.createQuery("select c,count(c.user.id) as likeCount, i.state as interestState from Comment c " +
                "join CommentLike cl on c.id = cl.comment.id " +
                "join User u on c.user.id = u.id " +
                "join Interest i on i.content.id = c.content.id " +
                "where c.content.id=:contentId and cl.comment.user.id = c.user.id " +
                "group by c.user.id order by c.user.id")
                .setParameter("contentId", contentId)
                .getResultList();

        List<Object[]> scoreObj= em.createQuery("select s.user.id,s.score as userCount from Score s " +
                "join Comment c on c.user.id = s.user.id and c.content.id = :contentId " +
                "where c.content.id=:contentId and s.user.id = c.user.id " +
                "group by c.user.id order by c.user.id")
                .setParameter("contentId", contentId)
                .getResultList();

        List<Object[]> replyObj = em.createNativeQuery("SELECT r.comment_user_id, count(r.reply_user_id) FROM reply r \n" +
                "LEFT JOIN comment c ON r.comment_user_id = c.user_id AND r.content_id = c.content_id\n" +
                "WHERE c.content_id = :contentId AND r.comment_user_id = c.user_id " +
                "GROUP BY c.user_id order by c.user_id")
                .setParameter("contentId", contentId)
                .getResultList();

        Map<Long, Double> scoreMap = new HashMap<>();
        Map<Long, Long> replyMap = new HashMap<>();
        for(Object[] obj : scoreObj) {
            Long scoreId = (Long) obj[0];
            Double scoreAvg = (Double) obj[1];
            scoreMap.put(scoreId, scoreAvg);
        }
        for(Object[] obj : replyObj) {
            Long replyId = ((BigInteger) obj[0]).longValue();
            Long replyCount = ((BigInteger) obj[1]).longValue();
            replyMap.put(replyId,replyCount);
        }

        List<DetailDto.addDetailCommentInfoList> detailCommentInfoList = new ArrayList<>();
        for(Object[] obj : commentObj){
            Comment c = (Comment)obj[0];
            Long userLikeCount = (Long)obj[1];
            InterestState interestState = (InterestState)obj[2];

            if(scoreMap.containsKey(c.getUser().getId()) || replyMap.containsKey(c.getUser().getId())){
                DetailDto.addDetailCommentInfoList build = DetailDto.addDetailCommentInfoList.builder()
                        .name(c.getUser().getName())
                        .description(c.getDescription())
                        .containsSpoiler(c.getContainsSpoiler())
                        .interestState(interestState.getDescription())
                        .userLikeCount(userLikeCount)
                        .userScore((!scoreMap.containsKey(c.getUser().getId())) ? 0.0 : scoreMap.get(c.getUser().getId()))
                        .userReplyCount(!replyMap.containsKey(c.getUser().getId()) ? 0L : replyMap.get(c.getUser().getId()))
                        .userLikeState(detailUserLikeStateSet.contains(c.getUser().getId()) ? true : false)
                        .build();
                detailCommentInfoList.add(build);
            }
        }
        return detailCommentInfoList;
    }

    /**
     * 컨텐츠별 컨텐츠 정보 조회
     * @param tClass 클래스 타입
     * @param contentId 컨텐츠 아이디
     * @param <T> 상속 제네릭 타입
     * @return 컨텐츠 영화, 티비쇼, 책 정보
     */
    public <T extends Content> T findByContentId(Class<T> tClass, long contentId) {
        return em.find(tClass, contentId);
    }
    /**
     * 태그 아이디값 조회 및 태그 리스트 조회
     * @param contentId 컨텐츠 아이디
     * @param <T> 상속 제네릭 타입
     * @return 태그 리스트
     */
    public <T extends Content> List<Tag> getTagList(long contentId) {
        return em.createNativeQuery("SELECT t.* " +
        "from content_tag ct " +
        "LEFT JOIN content c ON ct.content_id = c.content_id " +
        "LEFT JOIN movie m on m.content_id = c.content_id " +
        "LEFT JOIN tv_show ts on ts.content_id = c.content_id " +
        "LEFT JOIN book b on b.content_id = c.content_id " +
        "LEFT JOIN tag t ON ct.tag_id = t.tag_id WHERE m.content_id =:contentId",Tag.class)
                .setParameter("contentId", contentId)
                .setFirstResult(0)
                .getResultList();
    }

    /**
     * 컨텐츠 타입 값 조회
     * @param contentId 컨텐츠 아이디
     * @return 컨텐츠 정보 조
     */
    public Content getType(long contentId){
        return em.find(Content.class, contentId);
    }

    /**
     * 비슷한 작품 타입별 컨텐츠 리스트
     * @param tClass 클래스 타입
     * @param ids 컨텐츠 아이디
     * @param <T> 상속 제네릭 타입
     * @return
     */
    public <T extends Content> T findListIn(Class<T> tClass, Long ids) {
        return em.createQuery(
                "select c " +
                        "from "+ tClass.getSimpleName() + " c " +
                        "where c.id in :ids", tClass)
                .setParameter("ids", ids)
                .getSingleResult();
    }

    /**
     * 출연/배우 리스트 조회
     * @param id 출연/배우 리스트 아이디 값
     * @return 출연/배우 상세 정보 조회
     */
    public List<Participant> getParticipantList(long id) {
        return em.createNativeQuery("select p.* FROM content_participant cp " +
                "left join content c on c.content_id = cp.content_id " +
                "left join participant p ON cp.participant_id = p.participant_id " +
                "where cp.content_id=:id", Participant.class)
                .setParameter("id", id)
                .setFirstResult(0)
                .getResultList();
    }

    public List<Image> getImageList(long id) {
        return em.createNativeQuery("select i.* from content_image ci "+
                "left join content c ON c.content_id = ci.content_id " +
                "left join image i ON i.image_id = ci.image_id " +
                "where ci.content_id =:id", Image.class)
                .setParameter("id", id)
                .setFirstResult(0)
                .getResultList();
    }

    /**
     * 컨텐츠 랭킹 평균점수 반환
     * @param id 컨텐츠 아이디
     * @return 컨텐츠 랭킹 평균 점수 리스트(0.5 단위)
     */
    public DetailDto.DetailRankingScoreInfoList getRankingContentScore(Long id) {
        List<Object[]> detailRankingScoreInfoLists= em.createNativeQuery(
                "select avg(s.score), count(s.score) " +
                        "from score s " +
                        "where s.content_id = :id " +
                        "group by content_id")
                .setParameter("id", id)
                .getResultList();
        return new DetailDto.DetailRankingScoreInfoList((Double) detailRankingScoreInfoLists.get(0)[0],((BigInteger)detailRankingScoreInfoLists.get(0)[1]).intValue());
    }

    /**
     * 컨텐츠 랭킹별 점수 반환
     * @param id 컨텐츠 아이디
     * @param dtype
     * @return 컨텐츠 랭킹 평균 점수 리스트(0.5 단위)
     */
    public Map<Double,Integer> getScoreCount(Long id, String dtype){
        List<Object[]> groupCount = em.createNativeQuery(
                "select s.score, count(s.score) from score s " +
                        "left join content c on c.content_id = :id and c.dtype = :dtype " +
                        "where s.content_id = :id " +
                        "group by s.score")
                .setParameter("id", id)
                .setParameter("dtype", dtype)
                .getResultList();
        return groupCount.stream().collect(Collectors.toMap(o1 -> (Double) o1[0], o2 -> ((BigInteger)o2[1]).intValue()));
    }

    /**
     * user_id = 1에 해당하는 유저을 왓챠피디아 어드민으로보고
     * 해당 유저가 가지고 있는 컬렉션을 왓챠피디아 컬렉션으로 한다.
     * 왓챠피디아 컬렉션을 컨텐츠 타입별로 가지고 온다.
     * @param type 컨텐츠 타입 String(M: 영화, B: 책, S:TV)
     * @return 왓챠피디아 지정 컬렉션 리스트
     */
    public List<Collection> getAward(String type) {
        return em.createQuery(
                "select c " +
                        "from Collection c " +
                        "join CollectionContent cc on c.id = cc.collection.id " +
                        "join cc.content ct " +
                        "where c.user.id = 1" +
                        "and ct.dtype = :type " +
                        "group by cc.collection", Collection.class)
                .setParameter("type", type)
                .getResultList();
    }

    /**
     * 해당 컬렉션에 포함되는 컨텐츠를 개수만큼 반환한다.
     * @param tClass 컨텐츠 종류 Class
     * @param collection 컬렉션
     * @param size 반환 개수
     * @return 컬렉션에 담긴 컨텐츠 리스트
     */
    public <T extends Content> List<T> getContentsInCollection(Class<T> tClass, Collection collection, int size) {
        return em.createQuery(
                "select m " +
                        "from CollectionContent cc " +
                        "join cc.content c " +
                        "join " +  tClass.getSimpleName() + " m on m.id = c.id " +
                        "where cc.collection.id = :id", tClass)
                .setParameter("id", collection.getId())
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * 비슷한 작품 컨텐츠 조회
     * @param tagId 태그 아이디
     * @param dtype 타입별 영화, 티비쇼, 책
     * @return 비슷한 작품 아이디 Set 리스트
     */
    public Set<Long> getSimilarContentsId(Long tagId, String dtype) {
        List<BigInteger> contentList = em.createNativeQuery(
                "select c.content_id from content c " +
                        "left join content_tag ct on c.content_id = ct.content_id " +
                        "where ct.tag_id = :tagId and c.dtype = :dtype")
                .setParameter("tagId", tagId)
                .setParameter("dtype", dtype)
                .getResultList();
        Set<Long> contentIdList = new HashSet<>();
        for(BigInteger l : contentList){
            contentIdList.add(l.longValue());
        }
        return contentIdList;
    }

    /**
     * 유저 평가 정보 리스트 조회(로그인 인증되었을때)
     * @param userId 유저 아이디
     * @param contentId 컨텐츠 아이
     * @return 유저 평가 리스트
     */
    public DetailDto.DetailUserCommentInfoList findDetailUserInfo(Long userId, long contentId) {
        List<Object[]> DetailUserObj = em.createQuery("select c.content.id, c.user.id, c.description, s.score, i.state from Comment c " +
                "join Interest i on c.content.id = i.content.id " +
                "join Score s on s.content.id = c.content.id " +
                "where c.content.id = :contentId and c.user.id = :userId group by c.content.id")
                .setParameter("userId", userId)
                .setParameter("contentId", contentId)
                .getResultList();

        DetailDto.DetailUserCommentInfoList addDetailUserCommentInfoList = null;
        for(Object[] obj : DetailUserObj){
            Long contentIdObj = (Long)obj[0];
            Long userIdObj = (Long)obj[1];
            String description = (String)obj[2];
            Double scoreAvg = (Double) obj[3];
            InterestState interestState = (InterestState) obj[4];
            addDetailUserCommentInfoList = DetailDto.DetailUserCommentInfoList.builder()
                    .contentIdObj(contentIdObj)
                    .userIdObj(userIdObj)
                    .description(description)
                    .scoreAvg(scoreAvg)
                    .interestState(interestState.getDescription())
                    .build();
        }
        return addDetailUserCommentInfoList;
    }

    /**
     * 출연/배우 상세 정보 리스트
     * @param participantId 출연/배우 아이디 값
     * return 출연/배우 상세 정보 리스트
     */
    public List<DetailDto.addDetailParticipantInfoList> findDetailParticipantInfo(long participantId) {
        List<Object[]> detailParticipantObj = em.createQuery("select c, avg(s.score) from Content c " +
                "join ContentParticipant cp on c.id= cp.content.id " +
                "join Participant p on p.id = cp.participant.id " +
                "join Score s on s.content.id = c.id " +
                "where p.id = :participantId group by c.id")
                .setParameter("participantId", participantId)
                .getResultList();
        List<DetailDto.addDetailParticipantInfoList> addDetailParticipantInfoList = new ArrayList<>();
        for(Object[] obj : detailParticipantObj){
            Content content = (Content) obj[0];
            Double scoreAvg =(Double) obj[1];
            addDetailParticipantInfoList.add(new DetailDto.addDetailParticipantInfoList(content, scoreAvg));
        }
        return addDetailParticipantInfoList;
    }

    /**
     * 유저 좋아요 상태 정보
     * @param userId 유저 아이디
     * @param contentId 컨텐츠 아이디
     * @return 유저 좋아요 상태 정보 리스트
     */
    public Set<Long> findDetailUserLikeStateInfo(Long userId, long contentId) {
        List<BigInteger> detailUserStateObj = em.createNativeQuery("SELECT l.comment_user_id FROM main.comment c \n" +
                "LEFT JOIN main.comment_like l ON c.content_id = l.content_id\n" +
                "WHERE l.like_user_id = :userId AND c.content_id = :contentId GROUP BY l.comment_user_id")
                .setParameter("userId", userId)
                .setParameter("contentId", contentId)
                .getResultList();
        Set<Long> s = new HashSet<>();
        for(BigInteger b : detailUserStateObj){

            s.add(b.longValue());
        }
        return s;
    }

    /**
     * 상세 페이지 좋아요 추가 기
     * @param request 좋아요 ID, 컨텐츠 ID, 코멘트 ID
     * @return 좋아요 추가 성공:1 , 실패: 0
     */

    public Long insertDetailLike(DetailDto.detailCommentLikeInfo request) {
        long stateCode = -1;
        try {
            stateCode = em.createNativeQuery("insert into comment_like (like_user_id, content_id, comment_user_id) " +
                    "values (" + request.getLikeUserId() + "," + request.getContentId() + "," + request.getCommentUserId() + ")")
                    .executeUpdate();
        }catch(ConstraintViolationException e){
            e.printStackTrace();
            stateCode = 0;
        }
        return stateCode;
    }

    /**
     * 상세페이지 좋아요 삭제
     * @param request 좋아요 ID, 컨텐츠 ID, 코멘트 ID
     * @return 좋아요 삭제 성공:1
     */
    public Long deleteDetailLike(DetailDto.detailCommentLikeInfo request) {
        long stateCode = em.createNativeQuery("DELETE FROM comment_like " +
                "WHERE like_user_id = :likeUserId AND content_id = :contentId AND comment_user_id = :commentUserId")
                .setParameter("likeUserId", request.getLikeUserId())
                .setParameter("contentId", request.getContentId())
                .setParameter("commentUserId", request.getCommentUserId())
                .executeUpdate();
        return stateCode;
    }

    /**
     * 상세페이지 코멘트 추가
     * @param request Comment 엔티티 정보
     * @return 상태코드 성공:1, 실패:0
     */
    public Long insertDetailComment(DetailDto.detailCommentInsertInfo request) {
        long stateCode = -1;
        String tf = "N";
        try {
            stateCode = em.createNativeQuery(
                    "INSERT INTO main.comment (content_id, user_id, create_datetime, update_datetime, spoiler_yn, description, delete_yn) " +
                    "VALUES ("+request.getContentId() +","+request.getUserId()+","+LocalDateTime.now()+","+LocalDateTime.now()+","+tf+","+request.getDescription()+","+tf+")")
                    .executeUpdate();
        }catch(ConstraintViolationException e){
            e.printStackTrace();
            stateCode = 0;
        }
        return stateCode;
    }

    /**
     * 상세페이지 코멘트 삭제
     * @param request
     * @return
     */
    public Long deleteDetailComment(DetailDto.detailCommentDeleteInfo request) {
        return 0L;
    }

    /**
     * 상세페이지 코멘트 수정
     * @param request
     * @return
     */
    public Long updateDetailComment(DetailDto.detailCommentUpdateInfo request) {
        return 0L;
    }
}
