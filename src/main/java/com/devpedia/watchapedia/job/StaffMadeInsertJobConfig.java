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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class StaffMadeInsertJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private final BatchRepository batchRepository;
    private final EntityManager em;

    public PlatformTransactionManager jpaTransactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Job staffMadeInsertJob() {
        return jobBuilderFactory.get("staffMadeInsertJob")
                .start(staffMadeInsertStep1())
                .build();
    }

    @Bean
    public Step staffMadeInsertStep1() {
        return stepBuilderFactory.get("staffMadeInsertStep1")
                .transactionManager(jpaTransactionManager())
                .<StaffMadeInfo, Collection>chunk(10)
                .reader(staffMadeInsertReader())
                .processor(staffMadeInsertProcessor())
                .writer(staffMadeInsertJobWriter())
                .build();
    }

    @Bean
    public JsonItemReader<StaffMadeInfo> staffMadeInsertReader() {
        return new JsonItemReaderBuilder<StaffMadeInfo>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(StaffMadeInfo.class))
                .resource(new ClassPathResource("./staffmade.json"))
                .name("jsonCursorItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<StaffMadeInfo, Collection> staffMadeInsertProcessor() {
        return staffMadeInfo -> {
            User user = em.find(User.class, 1L);

            Collection collection = batchRepository.findCollectionByName(staffMadeInfo.title);
            if (collection == null) {
                collection = Collection.builder()
                        .user(user)
                        .title(staffMadeInfo.title)
                        .description(null)
                        .build();
                em.persist(collection);
            }

            switch (staffMadeInfo.dtype) {
                case "M":
                    List<Movie> randMovie = batchRepository.getRandMovie(generateRandCount());

                    for (Movie movie : randMovie) {
                        CollectionContent cc = CollectionContent.builder()
                                .collection(collection)
                                .content(movie)
                                .build();
                        em.persist(cc);
                    }
                    break;
                case "B":
                    List<Book> randBook = batchRepository.getRandBook(generateRandCount());

                    for (Book book : randBook) {
                        CollectionContent cc = CollectionContent.builder()
                                .collection(collection)
                                .content(book)
                                .build();
                        em.persist(cc);
                    }
                    break;
                case "S":
                    List<TvShow> randTvShow = batchRepository.getRandTvShow(generateRandCount());

                    for (TvShow tvShow : randTvShow) {
                        CollectionContent cc = CollectionContent.builder()
                                .collection(collection)
                                .content(tvShow)
                                .build();
                        em.persist(cc);
                    }
                    break;
            }

            return collection;
        };
    }

    @Bean
    public JpaItemWriter<Collection> staffMadeInsertJobWriter() {
        JpaItemWriter<Collection> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    private int generateRandCount() {
        return (int) Math.floor(Math.random() * 40) + 81;
    }

    @Data
    static class StaffMadeInfo {
        private String dtype;
        private String title;
    }
}