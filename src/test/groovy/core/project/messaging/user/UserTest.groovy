package core.project.messaging.user

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException
import core.project.messaging.domain.commons.exceptions.IllegalDomainStateException
import core.project.messaging.util.TestDataGenerator
import spock.lang.Specification

class UserTest extends Specification {

    def "should create unverified user successfully with valid arguments"() {
        when:
        def user = TestDataGenerator.unverifiedUser()

        then:
        user != null
        user.id() != null
        !user.isEnable()
        user.email() != null
        user.firstname() != null
        user.surname() != null
        user.username() != null
        user.password() != null
        user.rating() != null
        user.partners().isEmpty()
    }

    def "should create verified user successfully with valid arguments"() {
        when:
        def user = TestDataGenerator.verifiedUser()

        then:
        user != null
        user.id() != null
        user.isEnable()
        user.email() != null
        user.firstname() != null
        user.surname() != null
        user.username() != null
        user.password() != null
        user.rating() != null
        user.partners().isEmpty()
    }

    def "should throw IllegalDomainArgumentException for null fields"() {
        when:
        TestDataGenerator.user(null, false)

        then:
        thrown(IllegalDomainArgumentException)
    }

    def "should add and remove partners bidirectionally"() {
        given:
        def user1 = TestDataGenerator.verifiedUser()
        def user2 = TestDataGenerator.verifiedUser()

        when:
        user1.addPartner(user2)

        then:
        user1.containsPartner(user2)
        user2.containsPartner(user1)

        when:
        user1.removePartner(user2)

        then:
        !user1.containsPartner(user2)
        !user2.containsPartner(user1)
    }

    def "should not add self as partner"() {
        given:
        def user = TestDataGenerator.verifiedUser()

        when:
        user.addPartner(user)

        then:
        !user.containsPartner(user)
    }

    def "should thrown an exception when adding partner to unverified account"() {
        given:
        def unverifiedUser = TestDataGenerator.unverifiedUser()
        def partner = TestDataGenerator.verifiedUser()

        when:
        unverifiedUser.addPartner(partner)

        then:
        def e = thrown(IllegalDomainStateException)
        e.getMessage() == "Can`t add partner to unverified account."
    }
}