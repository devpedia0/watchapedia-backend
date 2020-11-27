package com.devpedia.watchapedia.dto;

import com.devpedia.watchapedia.domain.Book;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.List;

public class BookDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookInsertRequest {
        @NotBlank
        private String mainTitle;
        @NotBlank
        private String category;
        @NotBlank
        private LocalDate productionDate;
        @NotBlank
        private String description;
        @NotBlank
        private String subtitle;
        @PositiveOrZero
        private Integer page;

        private String contents;

        private String elaboration;

        private List<ParticipantDto.ParticipantRole> roles;

        private List<Long> tags;

        public Book toEntity() {
            return Book.builder()
                    .posterImage(null)
                    .mainTitle(this.mainTitle)
                    .subtitle(this.subtitle)
                    .category(this.category)
                    .descrption(this.description)
                    .productionDate(this.productionDate)
                    .page(this.page)
                    .elaboration(this.elaboration)
                    .contents(this.contents)
                    .build();
        }
    }
}
