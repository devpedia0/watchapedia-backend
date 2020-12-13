package com.devpedia.watchapedia.job;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.repository.BatchRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class RankingInsertJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource dataSource;

    private final PasswordEncoder passwordEncoder;
    private final BatchRepository batchRepository;

    public PlatformTransactionManager jpaTransactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Job rankingInsertJob() {
        return jobBuilderFactory.get("rankingInsertJob")
                .start(rankingInsertStep1())
                .build();
    }

    @Bean
    public Step rankingInsertStep1() {
        return stepBuilderFactory.get("rankingInsertStep1")
                .transactionManager(jpaTransactionManager())
                .<Index, List<Ranking>>chunk(1)
                .reader(rankingInsertReader())
                .processor(rankingInsertProcessor())
                .writer(rankingInsertWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Index> rankingInsertReader() {
        return new JdbcCursorItemReaderBuilder<Index>()
                .fetchSize(1)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Index.class))
                .sql("select * from seq_0_to_0")
                .name("jdbcCursorItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<Index, List<Ranking>> rankingInsertProcessor() {
        return index -> {
            int bookSize = 90;
            int movieSize = 50;
            int tvSize = 50;

            List<Book> randBook = batchRepository.getRandBook(bookSize);
            List<Movie> randMovie = batchRepository.getRandMovie(movieSize);
            List<TvShow> randTvShow = batchRepository.getRandTvShow(tvSize);

            List<Ranking> rankings = new ArrayList<>();

            int mars = 0, netflix = 0, box_office = 0;
            int all_best_seller  = 0, new_best_seller = 0, most_searched = 0;
            int korea_tv = 0, whatcha_tv = 0, netflix_tv = 0;

            for (Movie movie : randMovie) {
                if (mars < 30) {
                    rankings.add(new Ranking((long) mars + 1, "movies", "mars", movie));
                    mars++;
                } else if (netflix < 10) {
                    rankings.add(new Ranking((long) netflix + 1, "movies", "netflix", movie));
                    netflix++;
                } else {
                    rankings.add(new Ranking((long) box_office + 1, "movies", "box_office", movie));
                    box_office++;
                }
                log.debug("counts " + mars + " : " + netflix + " : " + box_office);
            }

            for (Book book : randBook) {
                if (all_best_seller < 30) {
                    rankings.add(new Ranking((long) all_best_seller + 1, "books", "all_best_seller", book));
                    all_best_seller++;
                } else if (new_best_seller < 30) {
                    rankings.add(new Ranking((long) new_best_seller + 1, "books", "new_best_seller", book));
                    new_best_seller++;
                } else {
                    rankings.add(new Ranking((long) most_searched + 1, "books", "most_searched", book));
                    most_searched++;
                }
            }

            for (TvShow tvShow : randTvShow) {
                if (korea_tv < 30) {
                    rankings.add(new Ranking((long) korea_tv + 1, "tv_shows", "korea_tv", tvShow));
                    korea_tv++;
                } else if (whatcha_tv < 10) {
                    rankings.add(new Ranking((long) whatcha_tv + 1, "tv_shows", "whatcha_tv", tvShow));
                    whatcha_tv++;
                } else {
                    rankings.add(new Ranking((long) netflix_tv + 1, "tv_shows", "netflix_tv", tvShow));
                    netflix_tv++;
                }
            }

            return rankings;
        };
    }

    @Bean
    public JpaItemListWriter<Ranking> rankingInsertWriter() {
        JpaItemWriter<Ranking> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        JpaItemListWriter<Ranking> listWriter = new JpaItemListWriter<>(jpaItemWriter);
        listWriter.setEntityManagerFactory(entityManagerFactory);
        return listWriter;
    }

    @Data
    static class Index {
        private int seq;
    }

    static class JpaItemListWriter<T> extends JpaItemWriter<List<T>> {
        private JpaItemWriter<T> jpaItemWriter;

        public JpaItemListWriter(JpaItemWriter<T> jpaItemWriter) {
            this.jpaItemWriter = jpaItemWriter;
        }

        @Override
        public void write(List<? extends List<T>> items) {
            List<T> totalList = new ArrayList<>();

            for (List<T> item : items) {
                totalList.addAll(item);
            }

            jpaItemWriter.write(totalList);
        }
    }
}