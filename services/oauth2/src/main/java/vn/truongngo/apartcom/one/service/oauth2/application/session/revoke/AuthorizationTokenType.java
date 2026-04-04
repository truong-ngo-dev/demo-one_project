package vn.truongngo.apartcom.one.service.oauth2.application.session.revoke;

/**
 * Token type dùng để lookup Authorization Record — tránh leak Spring AS types vào application layer.
 */
public enum AuthorizationTokenType {
    ACCESS_TOKEN("access_token"),
    REFRESH_TOKEN("refresh_token"),
    ID_TOKEN("id_token");

    private final String value;

    AuthorizationTokenType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
