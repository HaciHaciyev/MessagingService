package core.project.messaging.domain.articles.entities;

import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.events.ArticleEvents;
import core.project.messaging.domain.articles.values_objects.ArticleTag;
import core.project.messaging.domain.articles.values_objects.Body;
import core.project.messaging.domain.articles.values_objects.Header;
import core.project.messaging.domain.articles.values_objects.Summary;

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
    private ArticleEvents events;

    public static final int MAX_SIZE_OF_TAGS = 8;

    private Article(UUID id, UUID authorId, Set<ArticleTag> tags, long views, long likes,
                    Header header, Summary summary, Body body,
                    ArticleStatus status, ArticleEvents events) {

        Objects.requireNonNull(id, "Article id cannot be null.");
        Objects.requireNonNull(authorId, "Author ID cannot be null.");
        Objects.requireNonNull(tags, "Article tags cannot be null.");
        Objects.requireNonNull(header, "Article header cannot be null.");
        Objects.requireNonNull(summary, "Article summary cannot be null.");
        Objects.requireNonNull(body, "Article body cannot be null.");
        Objects.requireNonNull(status, "Article status cannot be null.");
        Objects.requireNonNull(events, "Article events cannot be null.");

        this.id = id;
        this.authorId = authorId;
        this.tags = tags;
        this.header = header;
        this.summary = summary;
        this.body = body;
        this.status = status;
        this.events = events;
    }

    public static Article of(UUID authorId, Set<ArticleTag> tags, Header header, Summary summary, Body body, ArticleStatus status) {
        if (status != null && status.equals(ArticleStatus.ARCHIVED)) {
            throw new IllegalArgumentException("Article can`t be created with archived status.");
        }
        if (tags.size() < 3|| tags.size() > 8) {
            throw new IllegalArgumentException("You need at least create 3 tags for Article and no more than 8.");
        }

        return new Article(UUID.randomUUID(), authorId, tags, 0L, 0L,
                header, summary, body, status, ArticleEvents.defaultEvents());
    }

    public static Article fromRepository(UUID id, UUID authorId, Set<ArticleTag> tags, long views, long likes,
                                         Header header, Summary summary, Body body,
                                         ArticleStatus status, ArticleEvents events) {
        if (views < 0 || likes < 0) {
            throw new IllegalArgumentException("Views | likes can`t be negative.");
        }

        return new Article(id, authorId, tags, views, likes, header, summary, body, status, events);
    }

    public UUID id() {
        return id;
    }

    public UUID authorId() {
        return authorId;
    }

    public ArticleEvents events() {
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
        Objects.requireNonNull(header, "Header cannot be null.");
        this.header = header;
    }

    public Summary summary() {
        return summary;
    }

    public void changeSummary(Summary summary) {
        Objects.requireNonNull(summary, "Summary must not be null.");
        this.summary = summary;
    }

    public Body body() {
        return body;
    }

    public void changeBody(Body body) {
        Objects.requireNonNull(body, "Body must not be null.");
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
        Objects.requireNonNull(tag, "Tag must not be null.");
        if (tags.size() == MAX_SIZE_OF_TAGS) {
            throw new IllegalArgumentException("Max size of tags: %d".formatted(MAX_SIZE_OF_TAGS));
        }

        tags.add(tag);
    }

    public void removeTag(ArticleTag tag) {
        Objects.requireNonNull(tag, "Tag must not be null.");
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
