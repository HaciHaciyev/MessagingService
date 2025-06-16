package core.project.messaging.domain.user.value_objects;

import core.project.messaging.domain.commons.exceptions.IllegalDomainArgumentException;
import core.project.messaging.domain.user.enumerations.InvitationResult;

public record PartnershipInvitation(InvitationResult result, String message) {

    public PartnershipInvitation {
        if (result == null)
            throw new IllegalDomainArgumentException("Result can`t be null");
        if (message == null)
            throw new IllegalDomainArgumentException("Message can`t be null");
    }
}
