package ru.project.service.enums;
public enum ServiceCommand {
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start"),
    ADD("/add"),
    ALLINFO("/allinfo"),
    DELETE("/delete"),
    TAKE("/take"),
    INFO("/info"),
    SHOW_EXP_SOON("/showExpiringSoon");

    private final String value;

    ServiceCommand(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ServiceCommand fromValue(String v) {
        for (ServiceCommand c : ServiceCommand.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }
}
