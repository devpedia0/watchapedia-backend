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
import java.lang.reflect.AnnotatedElement;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class CommentInsertJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private final BatchRepository batchRepository;
    private final EntityManager em;

    public PlatformTransactionManager jpaTransactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Job commentInsertJob() {
        return jobBuilderFactory.get("commentInsertJob")
                .start(commentInsertStep1())
                .build();
    }

    @Bean
    public Step commentInsertStep1() {
        return stepBuilderFactory.get("commentInsertStep1")
                .transactionManager(jpaTransactionManager())
                .<CommentInfo, Comment>chunk(100)
                .reader(commentInsertReader())
                .processor(commentInsertProcessor())
                .writer(commentInsertJobWriter())
//                .faultTolerant().skip(Exception.class)
                .build();
    }

    @Bean
    public JsonItemReader<CommentInfo> commentInsertReader() {
        return new JsonItemReaderBuilder<CommentInfo>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(CommentInfo.class))
                .resource(new ClassPathResource("./comment/comment_dump3.json"))
                .name("jsonCursorItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<CommentInfo, Comment> commentInsertProcessor() {
        return commentInfo -> {
            User user = batchRepository.getRandUser();
            Content content = batchRepository.getRandContentWithBias(generateRandomClass(), 15, generateRandomModOf3());

            return Comment.builder()
                    .content(content)
                    .user(user)
                    .description(commentInfo.description)
                    .containsSpoiler(commentInfo.containsSpoiler)
                    .build();
        };
    }

    @Bean
    public JpaItemWriter<Comment> commentInsertJobWriter() {
        JpaItemWriter<Comment> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    private int generateRandomModOf3() {
        int num = (int) Math.floor(Math.random() * 20) + 1;
        return 0;
//        if (num < 11) return 0;
//        else if (num < 15) return 1;
//        else if (num < 17) return 2;
//        else if (num < 19) return 3;
//        else return 1;
    }

    private Class<? extends Content> generateRandomClass() {
        int num = (int) Math.floor(Math.random() * 3);

        if (num == 0) return Movie.class;
        else if (num == 1) return Book.class;
        else return TvShow.class;
    }

    @Data
    static class CommentInfo {
        private String description;
        private Boolean containsSpoiler;
    }
}