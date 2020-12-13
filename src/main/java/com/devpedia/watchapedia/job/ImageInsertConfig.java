package com.devpedia.watchapedia.job;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.repository.BatchRepository;
import com.devpedia.watchapedia.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ImageInsertConfig {

    public static final String BASE_PATH = "tv/images/";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private final S3Service s3Service;

    public PlatformTransactionManager jpaTransactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Job imageInsertJob() {
        return jobBuilderFactory.get("imageInsertJob")
                .start(imageInsertStep1())
                .build();
    }

    @Bean
    public Step imageInsertStep1() {
        return stepBuilderFactory.get("imageInsertStep1")
                .transactionManager(jpaTransactionManager())
                .<TvShow, Tag>chunk(100)
                .reader(imageInsertReader())
                .processor(imageInsertProcessor())
                .writer(imageInsertWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<TvShow> imageInsertReader() {
        return new JpaPagingItemReaderBuilder<TvShow>()
                .name("jpaScoreItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("select t from TvShow t")
                .build();
    }

    @Bean
    public ItemProcessor<TvShow, Tag> imageInsertProcessor() {
        return tvShow -> {
            saveImageS3(tvShow.getPosterImage(), "poster/");
            log.debug("tvid = " + tvShow.getId().toString());
            List<ContentParticipant> participants = tvShow.getParticipants();
            for (ContentParticipant participant : participants) {
                if (participant.getParticipant().getProfileImage() != null)
                    saveImageS3(participant.getParticipant().getProfileImage(), "profile/");
            }

            List<ContentImage> gallery = tvShow.getImages();
            for (ContentImage contentImage : gallery) {
                saveImageS3(contentImage.getImage(), "gallery/");
            }

            return Tag.builder().build();
        };
    }

    private void saveImageS3(Image image, String resourcePath) {
        try {
            log.debug(image.getOriginName() + " : " + image.getName());
            ClassPathResource resource = new ClassPathResource(BASE_PATH + resourcePath + image.getOriginName());
            File file = resource.getFile();
            s3Service.upload(file, image.getPath());
        } catch (IOException e) {
            log.debug("image passsed : " +  image.getOriginName());
        }

    }

    @Bean
    public NoOpWriter<Tag> imageInsertWriter() {
        NoOpWriter<Tag> writer = new NoOpWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    static class NoOpWriter<T> extends JpaItemWriter<T> {
        @Override
        public void write(List<? extends T> items) {}
    }
}