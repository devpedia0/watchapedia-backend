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
    public User(String name, String email, String password, String description, String countryCode, List<String> roles) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.description = description;
        this.countryCode = countryCode;
        this.roles = roles;
        this.accessRange = AccessRange.PUBLIC;
        this.isEmailAgreed = false;
        this.isSmsAgreed = false;
        this.isPushAgreed = false;
        this.isDeleted = false;
    }

    // 연관관계 메서드
    public void addCollection(Collection collection) {
        this.collections.add(collection);
        collection.setUser(this);
    }
}
