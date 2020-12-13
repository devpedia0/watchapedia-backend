package com.devpedia.watchapedia.job;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.repository.BatchRepository;
import lombok.*;
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
public class CollectionInsertJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private final BatchRepository batchRepository;
    private final EntityManager em;

    public PlatformTransactionManager jpaTransactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Job collectionInsertJob() {
        return jobBuilderFactory.get("collectionInsertJob")
                .start(collectionInsertStep1())
                .build();
    }

    @Bean
    public Step collectionInsertStep1() {
        return stepBuilderFactory.get("collectionInsertStep1")
                .transactionManager(jpaTransactionManager())
                .<CollectionInfo, Collection>chunk(10)
                .reader(collectionInsertReader())
                .processor(collectionInsertProcessor())
                .writer(collectionInsertWriter())
                .build();
    }

    @Bean
    public JsonItemReader<CollectionInfo> collectionInsertReader() {
        return new JsonItemReaderBuilder<CollectionInfo>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(CollectionInfo.class))
                .resource(new ClassPathResource("./collection.json"))
                .name("jsonCursorItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<CollectionInfo, Collection> collectionInsertProcessor() {
        return collectionInfo -> {
//            User user = batchRepository.getRandUser();
//
//            Collection collection = Collection.builder()
//                    .user(user)
//                    .title(collectionInfo.title)
//                    .description(collectionInfo.description)
//                    .build();
//
//            em.persist(collection);

            Collection collection = batchRepository.findCollectionByName(collectionInfo.title);

            switch (collectionInfo.dtype) {
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
    public JpaItemWriter<Collection> collectionInsertWriter() {
        JpaItemWriter<Collection> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    private int generateRandCount() {
        return (int) Math.floor(Math.random() * 60) + 21;
    }

    @Data
    static class CollectionInfo {
        private String dtype;
        private String title;
        private String description;
    }
}