package com.devpedia.watchapedia.domain;

import com.devpedia.watchapedia.domain.enums.ImageCategory;
import com.devpedia.watchapedia.util.UrlUtil;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

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

    public static Image of(MultipartFile file, ImageCategory category) {
        String originFileName = file.getOriginalFilename();
        String ext = originFileName.substring(originFileName.lastIndexOf(".") + 1);
        String fileName = String.format("%s.%s", UUID.randomUUID().toString(), ext);
        String filePath = UrlUtil.getCategorizedFilePath(fileName, category);

        return Image.builder()
                .originName(originFileName)
                .name(fileName)
                .extention(ext)
                .size(file.getSize())
                .path(filePath)
                .build();
    }
}
