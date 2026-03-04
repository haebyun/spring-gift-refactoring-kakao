package gift.auth;

public interface OAuthLoginClient {
    OAuthTokenResponse requestAccessToken(String code);

    OAuthUserResponse requestUserInfo(String accessToken);
}
