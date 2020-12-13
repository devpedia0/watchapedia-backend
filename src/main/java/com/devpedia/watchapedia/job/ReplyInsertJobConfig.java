package com.devpedia.watchapedia.job;

import com.devpedia.watchapedia.domain.Comment;
import com.devpedia.watchapedia.domain.CommentLike;
import com.devpedia.watchapedia.domain.Reply;
import com.devpedia.watchapedia.domain.User;
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
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ReplyInsertJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource dataSource;

    private final BatchRepository batchRepository;
    private final EntityManager em;

    public PlatformTransactionManager jpaTransactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Job replyInsertJob() {
        return jobBuilderFactory.get("replyInsertJob")
                .start(replyInsertStep1())
                .build();
    }

    @Bean
    public Step replyInsertStep1() {
        return stepBuilderFactory.get("replyInsertStep1")
                .transactionManager(jpaTransactionManager())
                .<Index, List<Reply>>chunk(10)
                .reader(replyInsertReader())
                .processor(replyInsertProcessor())
                .writer(replyInsertListWriter())
//                .faultTolerant().skip(Exception.class)
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Index> replyInsertReader() {
        return new JdbcCursorItemReaderBuilder<Index>()
                .fetchSize(10)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Index.class))
                .sql("select * from seq_0_to_500")
                .name("jdbcCursorItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<Index, List<Reply>> replyInsertProcessor() {
        return index -> {
            log.debug("index: " + index);
            User user = batchRepository.getRandUser();
            List<Comment> comments = batchRepository.getRandComment(40, generateRandomMod(), 1500);

            List<Reply> result = new ArrayList<>();

            for (Comment comment : comments) {
                log.debug("size: " + result.size());
                result.add(new Reply(comment, user, "-"));
            }

            return result;
        };
    }

    @Bean
    public JpaItemListWriter<Reply> replyInsertListWriter() {
        JpaItemWriter<Reply> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        JpaItemListWriter<Reply> listWriter = new JpaItemListWriter<>(jpaItemWriter);
        listWriter.setEntityManagerFactory(entityManagerFactory);
        return listWriter;
    }

    private int generateRandomMod() {
        int num = (int) Math.floor(Math.random() * 20) + 1;
//        return 0;
        if (num < 12) return 0;
        else if (num < 16) return 1;
        else if (num < 19) return 2;
//        else if (num < 20) return 3;
        else return 3;
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