package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.config.TestConfig;
import com.devpedia.watchapedia.domain.Interest;
import com.devpedia.watchapedia.domain.Score;
import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.dto.enums.InterestContentOrder;
import com.devpedia.watchapedia.dto.enums.RatingContentOrder;
import com.devpedia.watchapedia.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase
@Import(TestConfig.class)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/action_counts.sql"})
    public void getUserActionCounts_DataExist_ReturnActionCounts() throws Exception {
        // given
        Long userId = 1L;

        // when
        UserDto.UserActionCounts userActionCounts = userRepository.getUserActionCounts(userId);

        // then
        assertThat(userActionCounts.getMovie().getRatingCount()).isEqualTo(1);
        assertThat(userActionCounts.getMovie().getWishCount()).isEqualTo(1);
        assertThat(userActionCounts.getMovie().getWatchingCount()).isEqualTo(1);
        assertThat(userActionCounts.getMovie().getNotInterestCount()).isEqualTo(1);
        assertThat(userActionCounts.getMovie().getCommentCount()).isEqualTo(1);

        assertThat(userActionCounts.getBook().getRatingCount()).isEqualTo(1);
        assertThat(userActionCounts.getBook().getWishCount()).isEqualTo(1);
        assertThat(userActionCounts.getBook().getWatchingCount()).isEqualTo(1);
        assertThat(userActionCounts.getBook().getNotInterestCount()).isEqualTo(1);
        assertThat(userActionCounts.getBook().getCommentCount()).isEqualTo(1);

        assertThat(userActionCounts.getTvShow().getRatingCount()).isEqualTo(1);
        assertThat(userActionCounts.getTvShow().getWishCount()).isEqualTo(1);
        assertThat(userActionCounts.getTvShow().getWatchingCount()).isEqualTo(1);
        assertThat(userActionCounts.getTvShow().getNotInterestCount()).isEqualTo(1);
        assertThat(userActionCounts.getTvShow().getCommentCount()).isEqualTo(1);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/action_counts.sql"})
    public void getUserActionCounts_NoDataUser_ReturnZero() throws Exception {
        // given
        Long userId = 2L;

        // when
        UserDto.UserActionCounts userActionCounts = userRepository.getUserActionCounts(userId);

        // then
        assertThat(userActionCounts.getMovie().getRatingCount()).isEqualTo(0);
        assertThat(userActionCounts.getMovie().getWishCount()).isEqualTo(0);
        assertThat(userActionCounts.getMovie().getWatchingCount()).isEqualTo(0);
        assertThat(userActionCounts.getMovie().getNotInterestCount()).isEqualTo(0);
        assertThat(userActionCounts.getMovie().getCommentCount()).isEqualTo(0);

        assertThat(userActionCounts.getBook().getRatingCount()).isEqualTo(0);
        assertThat(userActionCounts.getBook().getWishCount()).isEqualTo(0);
        assertThat(userActionCounts.getBook().getWatchingCount()).isEqualTo(0);
        assertThat(userActionCounts.getBook().getNotInterestCount()).isEqualTo(0);
        assertThat(userActionCounts.getBook().getCommentCount()).isEqualTo(0);

        assertThat(userActionCounts.getTvShow().getRatingCount()).isEqualTo(0);
        assertThat(userActionCounts.getTvShow().getWishCount()).isEqualTo(0);
        assertThat(userActionCounts.getTvShow().getWatchingCount()).isEqualTo(0);
        assertThat(userActionCounts.getTvShow().getNotInterestCount()).isEqualTo(0);
        assertThat(userActionCounts.getTvShow().getCommentCount()).isEqualTo(0);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql"})
    public void findUserScores_AllScore_ReturnAll() throws Exception {
        // given
        Long userId = 1L;

        // when
        List<Score> scores = userRepository.findUserScores(userId, ContentTypeParameter.MOVIES,
                null, RatingContentOrder.AVG_SCORE, PageRequest.of(0, 10));

        // then
        assertThat(scores).hasSize(10);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql"})
    public void findUserScores_CertainScore_ReturnScore() throws Exception {
        // given
        Long userId = 1L;

        // when
        List<Score> scores = userRepository.findUserScores(userId, ContentTypeParameter.MOVIES,
                2.5, RatingContentOrder.AVG_SCORE, PageRequest.of(0, 10));

        // then
        assertThat(scores).hasSize(1);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql"})
    public void findUserScores_OrderNull_OrderByTitle() throws Exception {
        // given
        Long userId = 1L;

        // when
        List<Score> scores = userRepository.findUserScores(userId, ContentTypeParameter.MOVIES,
                null, null, PageRequest.of(0, 10));

        // then
        assertThat(scores).hasSize(10);
        assertThat(scores).isSortedAccordingTo(Comparator.comparing(o -> o.getContent().getMainTitle()));
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql"})
    public void findUserScores_NoData_EmptyList() throws Exception {
        // given
        Long userId = 2L;

        // when
        List<Score> scores = userRepository.findUserScores(userId, ContentTypeParameter.MOVIES,
                null, null, PageRequest.of(0, 10));

        // then
        assertThat(scores).hasSize(0);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql"})
    public void findUserGroupedScore_HasOneEach_ReturnScores() throws Exception {
        // given
        Long userId = 1L;

        // when
        List<Score> groupedScore = userRepository.findUserGroupedScore(userId, ContentTypeParameter.MOVIES, 1);

        // then
        assertThat(groupedScore).hasSize(10);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql"})
    public void findUserGroupedScore_NoData_EmptyList() throws Exception {
        // given
        Long userId = 2L;

        // when
        List<Score> groupedScore = userRepository.findUserGroupedScore(userId, ContentTypeParameter.MOVIES, 1);

        // then
        assertThat(groupedScore).hasSize(0);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql"})
    public void getGroupedScoreCount_HasOneEach_ReturnCounts() throws Exception {
        // given
        Long userId = 1L;

        // when
        Map<String, Integer> counts = userRepository.getGroupedScoreCount(userId, ContentTypeParameter.MOVIES);

        // then
        assertThat(counts).hasSize(10);
        assertThat(counts.get("0.5")).isEqualTo(1);
        assertThat(counts.get("1.0")).isEqualTo(1);
        assertThat(counts.get("1.5")).isEqualTo(1);
        assertThat(counts.get("2.0")).isEqualTo(1);
        assertThat(counts.get("2.5")).isEqualTo(1);
        assertThat(counts.get("3.0")).isEqualTo(1);
        assertThat(counts.get("3.5")).isEqualTo(1);
        assertThat(counts.get("4.0")).isEqualTo(1);
        assertThat(counts.get("4.5")).isEqualTo(1);
        assertThat(counts.get("5.0")).isEqualTo(1);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql"})
    public void getGroupedScoreCount_NoDataUser_EmptyMap() throws Exception {
        // given
        Long userId = 2L;

        // when
        Map<String, Integer> counts = userRepository.getGroupedScoreCount(userId, ContentTypeParameter.MOVIES);

        // then
        assertThat(counts).hasSize(0);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_interests.sql"})
    public void findUserInterestContent_CertainInterestState_ReturnInterest() throws Exception {
        // given
        Long userId = 1L;

        // when
        List<Interest> interests = userRepository.findUserInterestContent(userId, ContentTypeParameter.MOVIES, InterestState.WISH,
                InterestContentOrder.AVG_SCORE, PageRequest.of(0, 10));

        // then
        assertThat(interests).hasSize(1);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_interests.sql"})
    public void findUserInterestContent_OrderNull_OrderByTitle() throws Exception {
        // given
        Long userId = 1L;

        // when
        List<Interest> interests = userRepository.findUserInterestContent(userId, ContentTypeParameter.MOVIES, InterestState.WISH,
                null, PageRequest.of(0, 10));

        // then
        assertThat(interests).hasSize(1);
        assertThat(interests).isSortedAccordingTo(Comparator.comparing(o -> o.getContent().getMainTitle()));
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql"})
    public void getRatingAnalysis_DataExist_ReturnAnalysis() throws Exception {
        // given
        Long userId = 1L;

        double sum = 0;
        for (double d = 0.5; d <= 5.0; d+= 0.5) {
            sum += d;
        }
        sum = (sum * 3);

        // when
        UserDto.UserRatingAnalysis analysis = userRepository.getRatingAnalysis(userId);

        // then
        assertThat(analysis.getTotalCount()).isEqualTo(30);
        assertThat(analysis.getMovieCount()).isEqualTo(10);
        assertThat(analysis.getBookCount()).isEqualTo(10);
        assertThat(analysis.getTvShowCount()).isEqualTo(10);
        assertThat(analysis.getAverage()).isEqualTo(sum / 30);
        assertThat(analysis.getMostRating()).isEqualTo(0.5);
        assertThat(analysis.getDistribution().get("0.5")).isEqualTo(3);
        assertThat(analysis.getDistribution().get("1.0")).isEqualTo(3);
        assertThat(analysis.getDistribution().get("1.5")).isEqualTo(3);
        assertThat(analysis.getDistribution().get("2.0")).isEqualTo(3);
        assertThat(analysis.getDistribution().get("2.5")).isEqualTo(3);
        assertThat(analysis.getDistribution().get("3.0")).isEqualTo(3);
        assertThat(analysis.getDistribution().get("3.5")).isEqualTo(3);
        assertThat(analysis.getDistribution().get("4.0")).isEqualTo(3);
        assertThat(analysis.getDistribution().get("4.5")).isEqualTo(3);
        assertThat(analysis.getDistribution().get("5.0")).isEqualTo(3);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql"})
    public void getRatingAnalysis_DataNotExist_EmptyAnalysis() throws Exception {
        // given
        Long userId = 2L;

        // when
        UserDto.UserRatingAnalysis analysis = userRepository.getRatingAnalysis(userId);

        // then
        assertThat(analysis.getTotalCount()).isEqualTo(0);
        assertThat(analysis.getMovieCount()).isEqualTo(0);
        assertThat(analysis.getBookCount()).isEqualTo(0);
        assertThat(analysis.getTvShowCount()).isEqualTo(0);
        assertThat(analysis.getAverage()).isEqualTo(0.0);
        assertThat(analysis.getMostRating()).isEqualTo(0.0);
        assertThat(analysis.getDistribution().get("0.5")).isEqualTo(0);
        assertThat(analysis.getDistribution().get("1.0")).isEqualTo(0);
        assertThat(analysis.getDistribution().get("1.5")).isEqualTo(0);
        assertThat(analysis.getDistribution().get("2.0")).isEqualTo(0);
        assertThat(analysis.getDistribution().get("2.5")).isEqualTo(0);
        assertThat(analysis.getDistribution().get("3.0")).isEqualTo(0);
        assertThat(analysis.getDistribution().get("3.5")).isEqualTo(0);
        assertThat(analysis.getDistribution().get("4.0")).isEqualTo(0);
        assertThat(analysis.getDistribution().get("4.5")).isEqualTo(0);
        assertThat(analysis.getDistribution().get("5.0")).isEqualTo(0);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql",
            "classpath:sql/participant_favorite.sql"})
    public void getFavoritePerson_Exist_FavoritePerson() throws Exception {
        // given
        Long userId = 1L;

        // when
        List<UserDto.FavoritePerson> favorite = userRepository.getFavoritePerson(userId,
                ContentTypeParameter.MOVIES, "감독", 10);

        // then
        assertThat(favorite).hasSize(1);
        assertThat(favorite.get(0).getId()).isEqualTo(1L);
        assertThat(favorite.get(0).getCount()).isEqualTo(3);
        assertThat(favorite.get(0).getScore()).isEqualTo((5.0 + 4.5 + 4.0) / 3);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql",
            "classpath:sql/tag_favorite.sql"})
    public void getFavoriteTag_Exist_FavoriteTag() throws Exception {
        // given
        Long userId = 1L;

        // when
        List<UserDto.FavoriteCommon> favorite = userRepository.getFavoriteTag(userId, ContentTypeParameter.MOVIES, 10);

        // then
        assertThat(favorite).hasSize(1);
        assertThat(favorite.get(0).getDescription()).isEqualTo("Tag");
        assertThat(favorite.get(0).getCount()).isEqualTo(3);
        assertThat(favorite.get(0).getScore()).isEqualTo((5.0 + 4.5 + 4.0) / 3);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/score_country_favorite.sql"})
    public void getFavoriteCountry_Exist_FavoriteCountry() throws Exception {
        // given
        Long userId = 1L;

        // when
        List<UserDto.FavoriteCommon> favorite = userRepository.getFavoriteCountry(userId, 10);

        // then
        assertThat(favorite).hasSize(1);
        assertThat(favorite.get(0).getDescription()).isEqualTo("KR");
        assertThat(favorite.get(0).getCount()).isEqualTo(3);
        assertThat(favorite.get(0).getScore()).isEqualTo((5.0 + 4.5 + 4.0) / 3);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql"})
    public void getTotalRunningTime_MovieExist_ReturnTotalTime() throws Exception {
        // given
        Long userId = 1L;

        // when
        int totalRunningTime = userRepository.getTotalRunningTime(userId);

        // then
        assertThat(totalRunningTime).isEqualTo(100 * 10);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/user_scores.sql"})
    public void getTotalRunningTime_MovieNotExist_ReturnZero() throws Exception {
        // given
        Long userId = 2L;

        // when
        int totalRunningTime = userRepository.getTotalRunningTime(userId);

        // then
        assertThat(totalRunningTime).isEqualTo(0);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/action_counts.sql"})
    public void getActionCounts_Exist_ReturnCounts() throws Exception {
        // given
        Set<Long> ids = Set.of(1L);

        // when
        Map<Long, UserDto.ActionCounts> actionCounts = userRepository.getActionCounts(ids);

        // then
        assertThat(actionCounts).hasSize(1);
        assertThat(actionCounts.get(1L).getRatingCount()).isEqualTo(3);
        assertThat(actionCounts.get(1L).getWishCount()).isEqualTo(3);
        assertThat(actionCounts.get(1L).getRatingCount()).isEqualTo(3);
        assertThat(actionCounts.get(1L).getRatingCount()).isEqualTo(3);
        assertThat(actionCounts.get(1L).getRatingCount()).isEqualTo(3);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/action_counts.sql"})
    public void getActionCounts_RetrieveTwo_ReturnTwo() throws Exception {
        // given
        Set<Long> ids = Set.of(1L, 2L);

        // when
        Map<Long, UserDto.ActionCounts> actionCounts = userRepository.getActionCounts(ids);

        // then
        assertThat(actionCounts).hasSize(2);

        assertThat(actionCounts.get(1L).getRatingCount()).isEqualTo(3);
        assertThat(actionCounts.get(1L).getWishCount()).isEqualTo(3);
        assertThat(actionCounts.get(1L).getRatingCount()).isEqualTo(3);
        assertThat(actionCounts.get(1L).getRatingCount()).isEqualTo(3);
        assertThat(actionCounts.get(1L).getRatingCount()).isEqualTo(3);

        assertThat(actionCounts.get(2L).getRatingCount()).isEqualTo(0);
        assertThat(actionCounts.get(2L).getWishCount()).isEqualTo(0);
        assertThat(actionCounts.get(2L).getRatingCount()).isEqualTo(0);
        assertThat(actionCounts.get(2L).getRatingCount()).isEqualTo(0);
        assertThat(actionCounts.get(2L).getRatingCount()).isEqualTo(0);
    }
}