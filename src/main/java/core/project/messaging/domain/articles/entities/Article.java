package core.project.messaging.domain.articles.entities;

import core.project.messaging.domain.articles.enumerations.ArticleStatus;
import core.project.messaging.domain.articles.enumerations.ArticleTag;
import core.project.messaging.domain.articles.events.ArticleEvents;
import core.project.messaging.domain.articles.values_objects.Content;
import core.project.messaging.domain.articles.values_objects.Header;
import core.project.messaging.domain.articles.values_objects.Summary;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Article {
    private final UUID id;
    private final UUID authorId;
    private Header header;
    private Summary summary;
    private Content content;
    private ArticleStatus status;
    private int views;
    private int likes;
    private final ArticleEvents events;
    private final Set<Comment> comments;
    private final Set<ArticleTag> tags;

    public Article(UUID id, UUID authorId, Header header, Summary summary, Content content,
                   ArticleStatus status, int views, int likes, ArticleEvents events, Set<Comment> comments,
                   Set<ArticleTag> tags) {
        Objects.requireNonNull(id, "ID must not be null.");
        Objects.requireNonNull(authorId, "Author ID must not be null.");
        Objects.requireNonNull(header, "Header must not be null.");
        Objects.requireNonNull(summary, "Summary must not be null.");
        Objects.requireNonNull(content, "Content must not be null.");
        Objects.requireNonNull(status, "Status must not be null.");
        Objects.requireNonNull(events, "Events must not be null.");
        Objects.requireNonNull(comments, "Comments must not be null.");
        Objects.requireNonNull(tags, "Tags must not be null.");
        if (likes < 0) {
            throw new IllegalArgumentException("Likes must be greater than 0.");
        }
        if (views < 0) {
            throw new IllegalArgumentException("Views must be greater than 0.");
        }

        this.id = id;
        this.authorId = authorId;
        this.header = header;
        this.summary = summary;
        this.content = content;
        this.status = status;
        this.views = views;
        this.likes = likes;
        this.events = events;
        this.comments = comments;
        this.tags = tags;
    }

    public UUID id() {
        return id;
    }

    public UUID authorId() {
        return authorId;
    }

    public Header header() {
        return header;
    }

    public Summary summary() {
        return summary;
    }

    public Content content() {
        return content;
    }

    public ArticleStatus status() {
        return status;
    }

    public int views() {
        return views;
    }

    public int likes() {
        return likes;
    }

    public ArticleEvents events() {
        return events;
    }

    public Set<Comment> comments() {
        return comments;
    }

    public Set<ArticleTag> tags() {
        return tags;
    }
}
