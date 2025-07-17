package core.project.messaging.article

import core.project.messaging.domain.articles.entities.Comment
import core.project.messaging.domain.articles.enumerations.CommentType
import core.project.messaging.domain.articles.values_objects.CommentDates
import core.project.messaging.domain.articles.values_objects.CommentIdentifiers
import core.project.messaging.domain.articles.values_objects.CommentText
import core.project.messaging.domain.articles.values_objects.Reference
import core.project.messaging.domain.commons.exceptions.DomainValidationException
import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException
import core.project.messaging.util.TestDataGenerator
import spock.lang.Specification

class CommentTest extends Specification {

    def "should create parent type comment successfully with valid parameters"() {
        given:
        def identifiers = new CommentIdentifiers(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        def commentText = TestDataGenerator.generateCommentText()
        def reference = new Reference(CommentType.PARENT, null, null)

        when:
        def comment = Comment.of(identifiers, commentText, reference)

        then:
        comment != null
        comment.id() != null
        comment.userId() != null
        comment.articleId() != null
        comment.text() != null
        comment.likes() == 0
        comment.reference() != null
        comment.events() != null
        comment.reference().commentType() == CommentType.PARENT
        comment.reference().parentCommentID() == null
        comment.reference().respondTo() == null
    }

    def "should create child type comment successfully with valid parameters"() {
        given:
        def identifiers = new CommentIdentifiers(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        def commentText = TestDataGenerator.generateCommentText()
        def reference = new Reference(CommentType.CHILD, UUID.randomUUID(), UUID.randomUUID())

        when:
        def comment = Comment.of(identifiers, commentText, reference)

        then:
        comment != null
        comment.id() != null
        comment.userId() != null
        comment.articleId() != null
        comment.text() != null
        comment.likes() == 0
        comment.reference() != null
        comment.events() != null
        comment.reference().commentType() == CommentType.CHILD
        comment.reference().parentCommentID() != null
        comment.reference().respondTo() != null
    }

    def "should throw exception when commentIdentifiers is null"() {
        given:
        def identifiers = null
        def commentText = TestDataGenerator.generateCommentText()
        def reference = new Reference(CommentType.CHILD, UUID.randomUUID(), UUID.randomUUID())

        when:
        Comment.of(identifiers, commentText, reference)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Comment identifiers can't be null."
    }

    def "should throw exception when commentText is null"() {
        given:
        def identifiers = new CommentIdentifiers(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        def commentText = null
        def reference = new Reference(CommentType.CHILD, UUID.randomUUID(), UUID.randomUUID())

        when:
        Comment.of(identifiers, commentText, reference)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Comment text cannot be null."
    }

    def "should throw exception when reference is null"() {
        given:
        def identifiers = new CommentIdentifiers(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        def commentText = TestDataGenerator.generateCommentText()
        def reference = null

        when:
        Comment.of(identifiers, commentText, reference)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Reference cannot be null."
    }

    def "should throw exception when likes is negative in fromRepository"() {
        given:
        def identifiers = new CommentIdentifiers(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        def commentText = TestDataGenerator.generateCommentText()
        def reference = new Reference(CommentType.CHILD, UUID.randomUUID(), UUID.randomUUID())
        def invalidLikesCount = -1;

        when:
        Comment.fromRepository(
                identifiers,
                commentText,
                reference,
                invalidLikesCount,
                CommentDates.defaultDates()
        )

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "LikesCount can`t be negative."
    }

    def "should increment likes by one"() {
        given:
        def identifiers = new CommentIdentifiers(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        def commentText = TestDataGenerator.generateCommentText()
        def reference = new Reference(CommentType.CHILD, UUID.randomUUID(), UUID.randomUUID())

        when:
        def comment = Comment.of(identifiers, commentText, reference)

        then:
        comment.likes() == 0

        when:
        comment.incrementLikes()

        then:
        comment.likes() == 1
    }

    def "should decrement likes by one and thrown exception if try to decrement 0 likes"() {
        given:
        def identifiers = new CommentIdentifiers(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        def commentText = TestDataGenerator.generateCommentText()
        def reference = new Reference(CommentType.CHILD, UUID.randomUUID(), UUID.randomUUID())

        when:
        def comment = Comment.of(identifiers, commentText, reference)

        then:
        comment.likes() == 0

        when:
        comment.incrementLikes()

        then:
        comment.likes() == 1

        when:
        comment.decrementLikes()

        then:
        comment.likes() == 0

        when:
        comment.decrementLikes()

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Likes count is already zero"
    }

    def "should change comment text successfully"() {
        given:
        def identifiers = new CommentIdentifiers(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        def commentText = TestDataGenerator.generateCommentText()
        def reference = new Reference(CommentType.CHILD, UUID.randomUUID(), UUID.randomUUID())

        when:
        def comment = Comment.of(identifiers, commentText, reference)

        then:
        comment.likes() == 0

        when:
        comment.incrementLikes()

        then:
        comment.likes() == 1
    }

    def "should throw exception when changing text to null"() {
        given:
        def identifiers = new CommentIdentifiers(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        def commentText = TestDataGenerator.generateCommentText()
        def reference = new Reference(CommentType.CHILD, UUID.randomUUID(), UUID.randomUUID())

        when:
        def comment = Comment.of(identifiers, commentText, reference)

        then:
        notThrown(DomainValidationException)

        when:
        comment.changeText(null)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Comment value can`t be null"
    }

    def "should mark comment as deleted"() {
        given:
        def identifiers = new CommentIdentifiers(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        def commentText = TestDataGenerator.generateCommentText()
        def reference = new Reference(CommentType.CHILD, UUID.randomUUID(), UUID.randomUUID())

        when:
        def comment = Comment.of(identifiers, commentText, reference)

        then:
        notThrown(DomainValidationException)

        when:
        comment.delete()

        then:
        comment.text().value() == CommentText.DELETED_COMMENT
    }
}
