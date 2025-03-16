package core.project.messaging.domain.user.enumerations;

public enum UserRole {

    ROLE_USER("User"), NONE("None");

    private final String userRole;

    UserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUserRole() {
        return this.userRole;
    }
}
