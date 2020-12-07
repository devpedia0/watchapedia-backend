package com.devpedia.watchapedia.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionContent {

    @EmbeddedId
    private CollectionContentId id;

    @MapsId("contentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @MapsId("collectionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @Builder
    public CollectionContent(Content content, Collection collection) {
        this.id = new CollectionContentId(content.getId(), collection.getId());
        this.content = content;
        this.collection = collection;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectionContentId implements Serializable {
        private Long contentId;
        private Long collectionId;
    }
}
