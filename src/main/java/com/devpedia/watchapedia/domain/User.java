package com.devpedia.watchapedia.domain;

import com.devpedia.watchapedia.domain.enums.AccessRange;
import com.devpedia.watchapedia.domain.enums.AccessRangeConverter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "delete_yn = 'N'")
public class User extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String description;

    @Column(nullable = false)
    private String countryCode;

    @Convert(converter = AccessRangeConverter.class)
    @Column(nullable = false)
    private AccessRange accessRange;

    @Column(name = "email_agree_yn", nullable = false)
    private Boolean isEmailAgreed;

    @Column(name = "sms_agree_yn", nullable = false)
    private Boolean isSmsAgreed;

    @Column(name = "push_agree_yn", nullable = false)
    private Boolean isPushAgreed;

    @Column(name = "delete_yn", nullable = false)
    private Boolean isDeleted;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Collection> collections = new ArrayList<>();

    @Builder
    public User(String name, String email, String password, String countryCode) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.countryCode = countryCode;
        this.roles = Collections.singletonList("USER");
        this.accessRange = AccessRange.PUBLIC;
        this.isEmailAgreed = false;
        this.isSmsAgreed = false;
        this.isPushAgreed = false;
        this.isDeleted = false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setAccessRange(AccessRange accessRange) {
        this.accessRange = accessRange;
    }

    public void setEmailAgreed(Boolean emailAgreed) {
        isEmailAgreed = emailAgreed;
    }

    public void setSmsAgreed(Boolean smsAgreed) {
        isSmsAgreed = smsAgreed;
    }

    public void setPushAgreed(Boolean pushAgreed) {
        isPushAgreed = pushAgreed;
    }
}
