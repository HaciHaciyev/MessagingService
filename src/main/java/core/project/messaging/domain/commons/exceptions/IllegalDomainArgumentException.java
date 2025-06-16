package core.project.messaging.domain.commons.exceptions;

public class IllegalDomainArgumentException extends DomainValidationException {
    public IllegalDomainArgumentException(String message) {
        super(message);
    }
}
