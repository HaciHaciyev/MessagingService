package core.project.messaging.domain.user.value_objects;

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;

import java.time.LocalDateTime;
import java.util.Objects;

public record AccountDates(LocalDateTime creationDate,
                           LocalDateTime lastUpdateDate) {

    public AccountDates {
        if (Objects.isNull(creationDate) || Objects.isNull(lastUpdateDate)) {
            throw new IllegalDomainArgumentException("The creationDate and lastUpdateDate must not be null");
        }
    }

    public static AccountDates defaultEvents() {
        return new AccountDates(LocalDateTime.now(), LocalDateTime.now());
    }
}
