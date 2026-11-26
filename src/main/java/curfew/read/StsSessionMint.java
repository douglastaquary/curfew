package curfew.read;

import java.util.Optional;

import curfew.scan.Minted;
import curfew.scan.SessionMint;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.GetSessionTokenRequest;
import software.amazon.awssdk.services.sts.model.GetSessionTokenResponse;

/**
 * Pede uma sessão AWS que acaba em menos de 24 horas. Não guarda o segredo.
 */
public final class StsSessionMint implements SessionMint {

    public static final int MAX_SECONDS = 23 * 3600;

    @Override
    public Optional<Minted> mint(int hours) {
        return mint(env("CURFEW_AWS_ACCESS_KEY_ID"), env("CURFEW_AWS_SECRET_ACCESS_KEY"), hours);
    }

    Optional<Minted> mint(String accessKeyId, String secret, int hours) {
        int seconds = seconds(hours);
        if (seconds < 0 || accessKeyId == null || accessKeyId.isBlank() || secret == null || secret.isBlank()) {
            return Optional.empty();
        }
        try (StsClient sts = StsClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secret)))
                .httpClient(UrlConnectionHttpClient.create())
                .build()) {
            GetSessionTokenResponse response = sts.getSessionToken(GetSessionTokenRequest.builder()
                    .durationSeconds(seconds)
                    .build());
            Credentials credentials = response.credentials();
            if (credentials == null || credentials.secretAccessKey() == null || credentials.secretAccessKey().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new Minted(
                    credentials.accessKeyId(),
                    credentials.secretAccessKey(),
                    credentials.sessionToken(),
                    credentials.expiration()));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    public static int seconds(int hours) {
        if (hours < 1 || hours > 23) {
            return -1;
        }
        int seconds = hours * 3600;
        if (seconds < 900 || seconds > MAX_SECONDS) {
            return -1;
        }
        return seconds;
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value;
    }
}
