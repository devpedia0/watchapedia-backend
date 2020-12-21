package com.devpedia.watchapedia.dto;

import com.devpedia.watchapedia.domain.Image;
import com.devpedia.watchapedia.domain.Tag;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

public class ImageDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageInfo {
        private Long id;

        private String name;

        private String originName;

        private String extention;

        private String path;

        private Long size;


        public ImageInfo(Image image) {
            this.id = image.getId();
            this.name = image.getName();
            this.originName = image.getOriginName();
            this.extention = image.getExtention();
            this.path = image.getPath();
            this.size = image.getSize();
        }
    }
}
