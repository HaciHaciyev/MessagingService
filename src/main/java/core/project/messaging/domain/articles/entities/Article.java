package core.project.messaging.domain.articles.entities;

import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.values_objects.*;
import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Article {
    private final UUID id;
    private final UUID authorId;
    private final Set<ArticleTag> tags;

    private long views;
    private long likes;
    private Header header;
    private Summary summary;
    private Body body;
    private ArticleStatus status;
    private ArticleDates events;

    public static final int MIN_TAGS_COUNT = 3;
    public static final int MAX_TAGS_COUNT = 8;

    private Article(UUID id, UUID authorId, Set<ArticleTag> tags, long views, long likes,
                    Header header, Summary summary, Body body,
                    ArticleStatus status, ArticleDates events) {

        if (id == null) throw new IllegalDomainArgumentException("Article id cannot be null.");
        if (authorId == null) throw new IllegalDomainArgumentException("Author ID cannot be null.");
        if (tags == null) throw new IllegalDomainArgumentException("Article tags cannot be null.");
        if (header == null) throw new IllegalDomainArgumentException("Article header cannot be null.");
        if (summary == null) throw new IllegalDomainArgumentException("Article summary cannot be null.");
        if (body == null) throw new IllegalDomainArgumentException("Article body cannot be null.");
        if (status == null) throw new IllegalDomainArgumentException("Article status cannot be null.");
        if (events == null) throw new IllegalDomainArgumentException("Article events cannot be null.");

        this.id = id;
        this.authorId = authorId;
        this.tags = tags;
        this.header = header;
        this.summary = summary;
        this.body = body;
        this.views = views;
        this.likes = likes;
        this.status = status;
        this.events = events;
    }

    public static Article of(
            UUID authorId,
            Set<ArticleTag> tags,
            Header header,
            Summary summary,
            Body body,
            ArticleStatus status) {

        if (status != null && status.equals(ArticleStatus.ARCHIVED))
            throw new IllegalDomainArgumentException("Article can`t be created with archived status.");
        if (tags.size() < MIN_TAGS_COUNT || tags.size() > MAX_TAGS_COUNT)
            throw new IllegalDomainArgumentException("You need at least create 3 tags for Article and no more than 8.");

        return new Article(UUID.randomUUID(), authorId, tags, 0L, 0L,
                header, summary, body, status, ArticleDates.defaultEvents());
    }

    public static Article fromRepository(
            UUID id,
            UUID authorId,
            Set<ArticleTag> tags,
            long views,
            long likes,
            Header header,
            Summary summary,
            Body body,
            ArticleStatus status,
            ArticleDates events) {

        if (views < 0 || likes < 0) throw new IllegalDomainArgumentException("Views | likes can`t be negative.");
        return new Article(id, authorId, tags, views, likes, header, summary, body, status, events);
    }

    public UUID id() {
        return id;
    }

    public UUID authorId() {
        return authorId;
    }

    public ArticleDates events() {
        return events;
    }

    public long views() {
        return views;
    }

    public long incrementViews() {
        return views++;
    }

    public long likes() {
        return likes;
    }

    public long incrementLikes() {
        return likes++;
    }

    public Header header() {
        return header;
    }

    public void changeHeader(Header header) {
        if (header == null)
            throw new IllegalDomainArgumentException("Header can`t be null.");
        this.header = header;
    }

    public Summary summary() {
        return summary;
    }

    public void changeSummary(Summary summary) {
        if (summary == null)
            throw new IllegalDomainArgumentException("Summary must not be null.");
        this.summary = summary;
    }

    public Body body() {
        return body;
    }

    public void changeBody(Body body) {
        if (body == null)
            throw new IllegalDomainArgumentException("Body must not be null.");
        this.body = body;
    }

    public ArticleStatus status() {
        return status;
    }

    public void publish() {
        this.status = ArticleStatus.PUBLISHED;
    }

    public void archive() {
        this.status = ArticleStatus.ARCHIVED;
    }

    public Set<ArticleTag> tags() {
        return new HashSet<>(tags);
    }

    public void addTag(ArticleTag tag) {
        if (tag == null) throw new IllegalDomainArgumentException("Tag must not be null.");
        if (tags.size() == MAX_TAGS_COUNT)
            throw new IllegalDomainArgumentException("Max size of tags: %d".formatted(MAX_TAGS_COUNT));

        tags.add(tag);
    }

    public void removeTag(ArticleTag tag) {
        if (tag == null) throw new IllegalDomainArgumentException("Tag must not be null.");
        if (!tags.contains(tag)) throw new IllegalDomainArgumentException("Tag not found.");
        if (tags.size() == MIN_TAGS_COUNT)
            throw new IllegalDomainArgumentException("The minimum required count opf tags is %d".formatted(MIN_TAGS_COUNT));

        tags.remove(tag);
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Article article)) return false;

        return id.equals(article.id) &&
                authorId.equals(article.authorId) &&
                tags.equals(article.tags) &&
                Objects.equals(header, article.header) &&
                Objects.equals(summary, article.summary) &&
                Objects.equals(body, article.body) &&
                status == article.status &&
                Objects.equals(events, article.events);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + authorId.hashCode();
        result = 31 * result + tags.hashCode();
        result = 31 * result + Objects.hashCode(header);
        result = 31 * result + Objects.hashCode(summary);
        result = 31 * result + Objects.hashCode(body);
        result = 31 * result + Objects.hashCode(status);
        result = 31 * result + Objects.hashCode(events);
        return result;
    }

    @Override
    public String toString() {
        return String.format("""
                Article {
                    ID: %s,
                    Author ID: %s,
                    Creation date: %s,
                    Last updated date: %s,
                    Tags: %s,
                    Header: %s,
                    Status: %s
                }
                """, id,
                authorId,
                events.creationDate().toString(),
                events.lastUpdateDate().toString(),
                tags,
                header.value(),
                status
        );
    }
}
