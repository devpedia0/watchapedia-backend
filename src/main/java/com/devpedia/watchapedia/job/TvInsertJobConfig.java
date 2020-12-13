package com.devpedia.watchapedia.job;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.ImageCategory;
import com.devpedia.watchapedia.repository.ParticipantRepository;
import com.devpedia.watchapedia.service.S3Service;
import com.devpedia.watchapedia.util.UrlUtil;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
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
import javax.sql.DataSource;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class TvInsertJobConfig {

    public static final String BASE_PATH = "tv/images/";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private final S3Service s3Service;
    private final ParticipantRepository participantRepository;
    private final EntityManager em;

    public PlatformTransactionManager jpaTransactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Job tvInsertJob() {
        return jobBuilderFactory.get("tvInsertJob")
                .start(tvInsertStep1())
                .build();
    }

    @Bean
    public Step tvInsertStep1() {
        return stepBuilderFactory.get("tvInsertStep1")
                .transactionManager(jpaTransactionManager())
                .<TvInsertBatchDto, TvShow>chunk(30)
                .reader(tvInsertReader())
                .processor(tvInsertProcessor())
                .writer(tvInsertWriter())
                .build();
    }

    @Bean
    public JsonItemReader<TvInsertBatchDto> tvInsertReader() {
        return new JsonItemReaderBuilder<TvInsertBatchDto>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(TvInsertBatchDto.class))
                .resource(new ClassPathResource("tv/tv_dump.json"))
                .name("jsonCursorItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<TvInsertBatchDto, TvShow> tvInsertProcessor() {
        return batchDto -> {
            TvShow tvshow = TvShow.builder()
                    .posterImage(null)
                    .mainTitle(batchDto.mainTitle)
                    .category(batchDto.category)
                    .description(batchDto.description == null ? "-" : batchDto.description)
                    .productionDate(LocalDate.of(batchDto.productionDate, 1, 1))
                    .countryCode(batchDto.countryCode != null ? batchDto.countryCode : "KO")
                    .originTitle(batchDto.originTitle)
                    .isNetflixContent(batchDto.isNetflixContent)
                    .isWatchaContent(batchDto.isWatchaContent)
                    .build();

            ClassPathResource resource1 = new ClassPathResource(BASE_PATH + "poster/" + batchDto.poster);
            File poster = resource1.getFile();
            Image posterImage = createImage(poster, ImageCategory.POSTER);
            s3Service.upload(poster, posterImage.getPath());
            tvshow.setPosterImage(posterImage);

            for (String galleryPath : batchDto.gallery) {
                ClassPathResource resource2 = new ClassPathResource(BASE_PATH + "gallery/" + galleryPath);
                File gallery = resource2.getFile();
                Image galleryImage = createImage(gallery, ImageCategory.GALLERY);
                s3Service.upload(gallery, galleryImage.getPath());

                ContentImage ci = ContentImage.builder()
                        .content(tvshow)
                        .image(galleryImage)
                        .build();
                em.persist(ci);
                tvshow.addContentImage(ci);

            }

            for (Role role : batchDto.roles) {
                Participant p1 = participantRepository.findByName(role.name);
                if (p1 == null) {
                    Image profileImage = null;
                    if (role.profile != null) {
                        ClassPathResource resource3 = new ClassPathResource(BASE_PATH + "profile/" + role.profile);
                        File profile = resource3.getFile();
                        profileImage = createImage(profile, ImageCategory.PARTICIPANT_PROFILE);
                        s3Service.upload(profile, profileImage.getPath());
                    }
                    p1 = Participant.builder()
                            .name(role.name)
                            .description(role.description)
                            .job(role.job)
                            .profileImage(profileImage)
                            .build();
                    em.persist(p1);
                }

                ContentParticipant cp = ContentParticipant.builder()
                        .participant(p1)
                        .content(tvshow)
                        .role(role.role)
                        .characterName(role.characterName)
                        .build();
                tvshow.addContentParticipant(cp);
            }

            return tvshow;
        };
    }

    @Bean
    public JpaItemWriter<TvShow> tvInsertWriter() {
        JpaItemWriter<TvShow> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    private Image createImage(File file, ImageCategory category) {
        String originFileName = file.getName();
        String ext = originFileName.substring(originFileName.lastIndexOf(".") + 1);
        String fileName = String.format("%s.%s", UUID.randomUUID().toString(), ext);
        String filePath = UrlUtil.getCategorizedFilePath(fileName, category);

        return Image.builder()
                .originName(originFileName)
                .name(fileName)
                .extention(ext)
                .size(file.length())
                .path(filePath)
                .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TvInsertBatchDto {
        private String mainTitle;
        private String category;
        private Integer productionDate;
        private String description;
        private String originTitle;
        private String countryCode;
        private Boolean isWatchaContent;
        private Boolean isNetflixContent;
        private String poster;
        private List<String> gallery;
        private List<Role> roles;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Role {
        private String role;
        private String characterName;
        private String name;
        private String description;
        private String job;
        private String profile;
    }
}