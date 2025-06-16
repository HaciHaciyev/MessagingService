package core.project.messaging.domain.commons.exceptions;

public class IllegalDomainAccessException extends DomainValidationException {
    public IllegalDomainAccessException(String message) {
        super(message);
    }
}
