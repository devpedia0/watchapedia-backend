package com.devpedia.watchapedia.job;

import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
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

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class UserInsertJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource dataSource;

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public PlatformTransactionManager jpaTransactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Job userInsertJob() {
        return jobBuilderFactory.get("userInsertJob")
                .start(userInsertStep1())
                .build();
    }

    @Bean
    public Step userInsertStep1() {
        return stepBuilderFactory.get("userInsertStep1")
                .transactionManager(jpaTransactionManager())
                .<Index, User>chunk(100)
                .reader(userInsertReader())
                .processor(userInsertProcessor())
                .writer(userInsertWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Index> userInsertReader() {
        return new JdbcCursorItemReaderBuilder<Index>()
                .fetchSize(500)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Index.class))
                .sql("select * from seq_0_to_2000")
                .name("jdbcCursorItemReader")
                .build();
    }

    @Bean
    public ItemProcessor<Index, User> userInsertProcessor() {
        return index -> batch(index.seq);
    }

    @Bean
    public JpaItemWriter<User> userInsertWriter() {
        JpaItemWriter<User> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    public User batch(int idx) {
        String first = "김이박최정강조윤장임한오서신권황안송류전홍고문양손배조백허유남심노정하곽성차주우구신임나전민유진지엄채원천방공강현함변염양변여추노도소신석선설마주연방위표명기반왕모장남탁국여진구";
        String last = "가강건경고관광구규근기길나남노누다단달담대덕도동두라래로루리마만명무문미민바박백범별병보사산상새서석선설섭성세소솔수숙순숭슬승시신아안애엄여연영예오옥완요용우원월위유윤율으은의이익인일자잔장재전정제조종주준중지진찬창채천철초춘충치탐태택판하한해혁현형혜호홍화환회효훈휘희운모배부림봉혼황량린을비솜공면탁온디항후려균묵송욱휴언들견추걸삼열웅분변양출타흥겸곤번식란더손술반빈실직악람권복심헌엽학개평늘랑향울련";
        String[] domain = { "naver.com", "gmail.com", "daum.net"};

        String randName = getRandName(first, last);
        String randEmail = getRandEmail(domain, idx);

        return User.builder()
                .name(randName)
                .password(passwordEncoder.encode("1234"))
                .countryCode("KR")
                .email(randEmail)
                .build();
    }

    private String getRandName(String first, String last) {
        String text = "";

        text += first.charAt((int) Math.floor(Math.random() * first.length()));
        for (var i = 0; i < 2; i++)
            text += last.charAt((int) Math.floor(Math.random() * last.length()));

        return text;
    }

    private String getRandEmail(String[] domain, int num) {
        int emailDigitCount = (int) Math.floor(Math.random() * 3) + 5;
        String text = "";

        for (int i = 0; i < emailDigitCount; i++) {
            char digit = (char) (Math.floor(Math.random() * 26) + 97);
            text += digit;
        }
        text += num + "@";
        text += domain[(int) Math.floor(Math.random() * domain.length)];

        return text;
    }

    static class Index {
        private int seq;

        public void setSeq(int seq) {
            this.seq = seq;
        }

        public int getSeq() {
            return seq;
        }
    }
}