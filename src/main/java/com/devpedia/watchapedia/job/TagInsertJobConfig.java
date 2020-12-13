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
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class TagInsertJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private final BatchRepository batchRepository;

    public PlatformTransactionManager jpaTransactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Job tagInsertJob() {
        return jobBuilderFactory.get("tagInsertJob")
                .start(tagInsertStep1())
                .build();
    }

    @Bean
    public Step tagInsertStep1() {
        return stepBuilderFactory.get("tagInsertStep1")
                .transactionManager(jpaTransactionManager())
                .<Content, List<ContentTag>>chunk(100)
                .reader(tagInsertReader())
                .processor(tagInsertProcessor())
                .writer(tagInsertListWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Content> tagInsertReader() {
        return new JpaPagingItemReaderBuilder<Content>()
                .name("jpaScoreItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("select c from Content c")
                .build();
    }

    @Bean
    public ItemProcessor<Content, List<ContentTag>> tagInsertProcessor() {
        return content -> {
            if (content.getTags().size() != 0) return null;

            List<Tag> randTags = batchRepository.getRandTag(generateRandCount());

            List<ContentTag> cts = new ArrayList<>();

            for (Tag tag : randTags) {
                cts.add(ContentTag.builder()
                        .tag(tag)
                        .content(content)
                        .build());
            }

            return cts;
        };
    }

    @Bean
    public JpaItemListWriter<ContentTag> tagInsertListWriter() {
        JpaItemWriter<ContentTag> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        JpaItemListWriter<ContentTag> listWriter = new JpaItemListWriter<>(jpaItemWriter);
        listWriter.setEntityManagerFactory(entityManagerFactory);
        return listWriter;
    }

    private int generateRandCount() {
        return (int) Math.floor(Math.random() * 2) + 2;
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