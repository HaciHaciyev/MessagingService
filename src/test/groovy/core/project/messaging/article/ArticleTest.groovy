package core.project.messaging.article

import core.project.messaging.domain.articles.entities.Article
import core.project.messaging.domain.articles.enumerations.ArticleStatus
import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException
import core.project.messaging.util.TestDataGenerator
import spock.lang.Specification

import java.util.concurrent.ThreadLocalRandom

class ArticleTest extends Specification {

    def "should create article successfully with valid parameters"() {
        given:
        def id = UUID.randomUUID()
        def tags = TestDataGenerator.generateTags()
        def header = TestDataGenerator.generateHeader()
        def summary = TestDataGenerator.generateSummary()
        def body = TestDataGenerator.generateBody()
        def status = ArticleStatus.DRAFT

        when:
        def article = Article.of(id, tags, header, summary, body, status)

        then:
        article != null
        article.id() != null
        article.views() == 0
        article.likes() == 0
        article.tags() != null
        article.tags().size() >= 3
        article.header() != null
        article.summary() != null
        article.body() != null
        article.status() != null
        article.status() != ArticleStatus.ARCHIVED
    }

    def "should throw exception when status is ARCHIVED during creation"() {
        given:
        def id = UUID.randomUUID()
        def tags = TestDataGenerator.generateTags()
        def header = TestDataGenerator.generateHeader()
        def summary = TestDataGenerator.generateSummary()
        def body = TestDataGenerator.generateBody()
        def status = ArticleStatus.ARCHIVED

        when:
        Article.of(id, tags, header, summary, body, status)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Article can`t be created with archived status."
    }

    def "should throw exception when tag count is less than minimum"() {
        given:
        def id = UUID.randomUUID()
        def tags = new HashSet(Arrays.asList("Java", "Quarkus"))
        def header = TestDataGenerator.generateHeader()
        def summary = TestDataGenerator.generateSummary()
        def body = TestDataGenerator.generateBody()
        def status = ArticleStatus.PUBLISHED

        when:
        Article.of(id, tags, header, summary, body, status)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "You need at least create 3 tags for Article and no more than 8."
    }

    def "should throw exception when tag count exceeds maximum"() {
        given:
        def id = UUID.randomUUID()
        def tags = TestDataGenerator.generateTags(14)
        def header = TestDataGenerator.generateHeader()
        def summary = TestDataGenerator.generateSummary()
        def body = TestDataGenerator.generateBody()
        def status = ArticleStatus.PUBLISHED

        when:
        Article.of(id, tags, header, summary, body, status)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "You need at least create 3 tags for Article and no more than 8."
    }

    def "should increment views by one"() {
        when:
        article.incrementViews()

        then:
        article.views() == 1

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should increment likes by one"() {
        when:
        article.incrementLikes()

        then:
        article.likes() == 1

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should change header successfully"() {
        given:
        def header = TestDataGenerator.generateHeader()

        when:
        article.changeHeader(header)

        then:
        notThrown(Throwable)

        and:
        article.header() == header

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should throw exception when changing header to null"() {
        given:
        def header = null

        when:
        article.changeHeader(header)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Header can`t be null."

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should change summary successfully"() {
        given:
        def summary = TestDataGenerator.generateSummary()

        when:
        article.changeSummary(summary)

        then:
        notThrown(Throwable)

        and:
        article.summary() == summary

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should throw exception when changing summary to null"() {
        given:
        def summary = null

        when:
        article.changeSummary(summary)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Summary must not be null."

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should change body successfully"() {
        given:
        def body = TestDataGenerator.generateBody()

        when:
        article.changeBody(body)

        then:
        notThrown(Throwable)

        and:
        article.body() == body

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should throw exception when changing body to null"() {
        given:
        def body = null

        when:
        article.changeBody(body)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Body must not be null."

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should publish article"() {
        when:
        article.publish()

        then:
        notThrown(Throwable)

        and:
        article.status() == ArticleStatus.PUBLISHED

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.DRAFT)})
    }

    def "should archive article"() {
        when:
        article.archive()

        then:
        notThrown(Throwable)

        and:
        article.status() == ArticleStatus.ARCHIVED

        where:
        article << (1..10).collect({TestDataGenerator.article(
                ThreadLocalRandom.current().nextInt(0, 2) == 0 ? ArticleStatus.DRAFT : ArticleStatus.PUBLISHED)})
    }

    def "should add tag successfully"() {
        given:
        def tag = TestDataGenerator.generateTag()

        when:
        article.addTag(tag)

        then:
        notThrown(Throwable)

        and:
        article.tags().contains(tag)

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should throw exception when adding null tag"() {
        given:
        def tag = null

        when:
        article.addTag(tag)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Tag must not be null."

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should throw exception when adding tag exceeds max count"() {
        given:
        def id = UUID.randomUUID()
        def tags = TestDataGenerator.generateTags(8)
        def header = TestDataGenerator.generateHeader()
        def summary = TestDataGenerator.generateSummary()
        def body = TestDataGenerator.generateBody()
        def status = ArticleStatus.DRAFT

        def tag = TestDataGenerator.generateTag()
        def article = Article.of(id, tags, header, summary, body, status)

        when:
        article.addTag(tag)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Max size of tags: %d".formatted(Article.MAX_TAGS_COUNT)
    }

    def "should remove tag successfully"() {
        given:
        def tag = article.tags().asList().get(ThreadLocalRandom.current().nextInt(0, article.tags().size()))

        when:
        article.removeTag(tag)

        then:
        notThrown(Throwable)

        and:
        !article.tags().contains(tag)

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should throw exception when removing null tag"() {
        given:
        def tag = null

        when:
        article.removeTag(tag)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Tag must not be null."

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should throw exception when removing non-existing tag"() {
        given:
        def tag = TestDataGenerator.generateTag()

        when:
        article.removeTag(tag)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "Tag not found."

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED)})
    }

    def "should throw exception when removing tag violates minimum count"() {
        given:
        def tag = article.tags().asList().get(ThreadLocalRandom.current().nextInt(0, article.tags().size()))

        when:
        article.removeTag(tag)

        then:
        def e = thrown(IllegalDomainArgumentException)
        e.getMessage() == "The minimum required count opf tags is %d".formatted(Article.MIN_TAGS_COUNT)

        where:
        article << (1..10).collect({TestDataGenerator.article(ArticleStatus.PUBLISHED, 3)})
    }
}
