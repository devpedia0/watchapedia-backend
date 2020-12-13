package com.devpedia.watchapedia.job;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.repository.BatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ScoreInsertJobConfig extends DefaultBatchConfigurer {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource dataSource;

    private final PasswordEncoder passwordEncoder;
    private final BatchRepository batchRepository;

    public PlatformTransactionManager jpaTransactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Override
    public void setDataSource(DataSource dataSource) {}

    @Bean
    public Job scoreInsertJob() {
        return jobBuilderFactory.get("scoreInsertJob")
                .start(scoreInsertStep1())
                .build();
    }

    @Bean
    public Step scoreInsertStep1() {
        return stepBuilderFactory.get("scoreInsertStep1")
                .transactionManager(jpaTransactionManager())
                .<User, List<Score>>chunk(100)
                .reader(scoreInsertReader())
                .processor(scoreInsertProcessor())
                .writer(scoreInsertListWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<User> scoreInsertReader() {
        return new JpaPagingItemReaderBuilder<User>()
                .name("jpaScoreItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("select u from User u")
                .build();
    }

    @Bean
    public ItemProcessor<User, List<Score>> scoreInsertProcessor() {
        return user -> {
            int bookSize = (int) Math.floor(Math.random() * 11) + 15;
            int movieSize = (int) Math.floor(Math.random() * 11) + 15;
            int tvSize = (int) Math.floor(Math.random() * 11) + 5;

            List<Book> randBook = batchRepository.getRandBook(bookSize);
            List<Movie> randMovie = batchRepository.getRandMovie(movieSize);
            List<TvShow> randTvShow = batchRepository.getRandTvShow(tvSize);

            List<Score> scores = new ArrayList<>();

            for (Book book : randBook) {
                scores.add(Score.builder()
                        .content(book)
                        .user(user)
                        .score(generateRandScore((int) (book.getId() % 3)))
                        .build());
            }

            for (Movie movie : randMovie) {
                scores.add(Score.builder()
                        .content(movie)
                        .user(user)
                        .score(generateRandScore((int) (movie.getId() % 3)))
                        .build());
            }

            for (TvShow tvShow : randTvShow) {
                scores.add(Score.builder()
                        .content(tvShow)
                        .user(user)
                        .score(generateRandScore((int) (tvShow.getId() % 3)))
                        .build());
            }

            return scores;
        };
    }

    @Bean
    public JpaItemListWriter<Score> scoreInsertListWriter() {
        JpaItemWriter<Score> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        JpaItemListWriter<Score> listWriter = new JpaItemListWriter<>(jpaItemWriter);
        listWriter.setEntityManagerFactory(entityManagerFactory);
        return listWriter;
    }

    private double generateRandScore(int range) {
        int score = 0;
        if (range == 0)
            score = 5;
        else if (range == 1)
            score = (int) Math.floor(Math.random() * 4) + 5;
        else
            score = 10;
        return (double) score / 2;
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