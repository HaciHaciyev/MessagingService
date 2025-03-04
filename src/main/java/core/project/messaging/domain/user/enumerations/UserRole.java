package core.project.messaging.domain.user.enumerations;

import lombok.Getter;

@Getter
public enum UserRole {

    ROLE_USER("User"), NONE("None");

    private final String userRole;

    UserRole(String userRole) {
        this.userRole = userRole;
    }
}
