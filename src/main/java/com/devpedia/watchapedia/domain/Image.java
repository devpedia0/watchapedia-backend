package com.devpedia.watchapedia.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id @GeneratedValue
    @Column(name = "image_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String originName;

    @Column(nullable = false)
    private String extention;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private Long size;

    @Builder
    public Image(String name, String originName, String extention, String path, Long size) {
        this.name = name;
        this.originName = originName;
        this.extention = extention;
        this.path = path;
        this.size = size;
    }
}
