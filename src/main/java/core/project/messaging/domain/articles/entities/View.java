package core.project.messaging.domain.articles.entities;

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class View {
    private final UUID id;
    private final UUID articleID;
    private final UUID readerID;
    private final LocalDateTime viewedDate;

    private View(UUID id, UUID articleID, UUID readerID, LocalDateTime viewedDate) {
        if (id == null) throw new IllegalDomainArgumentException("ID can`t be null");
        if (articleID == null) throw new IllegalDomainArgumentException("ArticleID can`t be null");
        if (readerID == null) throw new IllegalDomainArgumentException("ReaderID can`t be null");
        if (viewedDate == null) throw new IllegalDomainArgumentException("ViewedData can`t be null");

        this.id = id;
        this.articleID = articleID;
        this.readerID = readerID;
        this.viewedDate = viewedDate;
    }

    public static View of(UUID id, UUID articleID, UUID readerID) {
        return new View(id, articleID, readerID, LocalDateTime.now());
    }

    public static View fromRepository(UUID id, UUID articleID, UUID readerID, LocalDateTime viewedDate) {
        return new View(id, articleID, readerID, viewedDate);
    }

    public UUID id() {
        return id;
    }

    public UUID articleID() {
        return articleID;
    }

    public UUID readerID() {
        return readerID;
    }

    public LocalDateTime viewedDate() {
        return viewedDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (View) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.articleID, that.articleID) &&
                Objects.equals(this.readerID, that.readerID) &&
                Objects.equals(this.viewedDate, that.viewedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, articleID, readerID, viewedDate);
    }

    @Override
    public String toString() {
        return "View[" +
                "id=" + id + ", " +
                "articleID=" + articleID + ", " +
                "readerID=" + readerID + ", " +
                "viewedDate=" + viewedDate + ']';
    }
}
