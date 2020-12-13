package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BatchRepository {

    private final EntityManager em;

    public <T extends Content> Content getRandContentWithBias(Class<T> type, int mod, int value) {
        String table = "";
        if (type == Movie.class) {
            table = "movie";
        } else if (type == Book.class) {
            table = "book";
        } else {
            table = "tv_show";
        }
        return (Content) em.createNativeQuery(
                "select * " +
                        "from " + table + " c " +
                        "join content c2 on c.content_id = c2.content_id " +
                        "where c.content_id % :mod = :value " +
                        "order by RAND() " +
                        "limit 1", type)
                .setParameter("mod", mod)
                .setParameter("value", value)
                .getSingleResult();
    }

    public List<Book> getRandBook(int size) {
        return em.createNativeQuery(
                "select * " +
                        "from book b " +
                        "join content c on b.content_id = c.content_id " +
                        "order by RAND() " +
                        "limit :size", Book.class)
                .setParameter("size", size)
                .getResultList();
    }

    public List<Movie> getRandMovie(int size) {
        return em.createNativeQuery(
                "select * " +
                        "from movie m " +
                        "join content c on m.content_id = c.content_id " +
                        "order by RAND() " +
                        "limit :size", Movie.class)
                .setParameter("size", size)
                .getResultList();
    }

    public List<TvShow> getRandTvShow(int size) {
        return em.createNativeQuery(
                "select * " +
                        "from tv_show t " +
                        "join content c on t.content_id = c.content_id " +
                        "order by RAND() " +
                        "limit :size", TvShow.class)
                .setParameter("size", size)
                .getResultList();
    }

    public User getRandUser() {
        return (User) em.createNativeQuery(
                "select * " +
                        "from user u " +
                        "order by RAND() " +
                        "limit 1", User.class)
                .getSingleResult();

    }

    public List<Tag> getRandTag(int size) {
        return em.createNativeQuery(
                "select * " +
                        "from tag t " +
                        "order by RAND() " +
                        "limit :size", Tag.class)
                .setParameter("size", size)
                .getResultList();
    }

    public List<Comment> getRandComment(int mod, int value, int size) {
        return em.createNativeQuery(
                "select * " +
                        "from comment c " +
                        "where (content_id + user_id) % :mod = :value " +
                        "order by RAND() " +
                        "limit :size", Comment.class)
                .setParameter("mod", mod)
                .setParameter("value", value)
                .setParameter("size", size)
                .getResultList();
    }

    public Collection findCollectionByName(String title) {
        return em.createQuery(
                "select c " +
                        "from Collection c " +
                        "where c.title = :title", Collection.class)
                .setParameter("title", title)
                .getSingleResult();
    }
}
