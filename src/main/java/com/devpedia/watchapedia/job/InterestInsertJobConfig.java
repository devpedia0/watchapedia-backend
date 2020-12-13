package com.devpedia.watchapedia.job;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.InterestState;
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

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class InterestInsertJobConfig {

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
    public Job interestInsertJob() {
        return jobBuilderFactory.get("interestInsertJob")
                .start(interestInsertStep1())
                .build();
    }

    @Bean
    public Step interestInsertStep1() {
        return stepBuilderFactory.get("interestInsertStep1")
                .transactionManager(jpaTransactionManager())
                .<User, List<Interest>>chunk(100)
                .reader(interestInsertReader())
                .processor(interestInsertProcessor())
                .writer(interestInsertListWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<User> interestInsertReader() {
        return new JpaPagingItemReaderBuilder<User>()
                .name("jpaScoreItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("select u from User u")
                .build();
    }

    @Bean
    public ItemProcessor<User, List<Interest>> interestInsertProcessor() {
        return user -> {
            int bookSize = (int) Math.floor(Math.random() * 30) + 15;
            int movieSize = (int) Math.floor(Math.random() * 30) + 15;
            int tvSize = (int) Math.floor(Math.random() * 30) + 10;

            List<Book> randBook = batchRepository.getRandBook(bookSize);
            List<Movie> randMovie = batchRepository.getRandMovie(movieSize);
            List<TvShow> randTvShow = batchRepository.getRandTvShow(tvSize);

            List<Interest> interests = new ArrayList<>();

            for (Book book : randBook) {
                interests.add(Interest.builder()
                        .content(book)
                        .user(user)
                        .state(generateRandState((int) (book.getId() % 3)))
                        .build());
            }

            for (Movie movie : randMovie) {
                interests.add(Interest.builder()
                        .content(movie)
                        .user(user)
                        .state(generateRandState((int) (movie.getId() % 3)))
                        .build());
            }

            for (TvShow tvShow : randTvShow) {
                interests.add(Interest.builder()
                        .content(tvShow)
                        .user(user)
                        .state(generateRandState((int) (tvShow.getId() % 3)))
                        .build());
            }

            return interests;
        };
    }

    @Bean
    public JpaItemListWriter<Interest> interestInsertListWriter() {
        JpaItemWriter<Interest> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        JpaItemListWriter<Interest> listWriter = new JpaItemListWriter<>(jpaItemWriter);
        listWriter.setEntityManagerFactory(entityManagerFactory);
        return listWriter;
    }

    private InterestState generateRandState(int range) {
        if (range == 0)
            return InterestState.WISH;
        else if (range == 1)
            return InterestState.WATCHING;
        else
            return InterestState.NOT_INTEREST;
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