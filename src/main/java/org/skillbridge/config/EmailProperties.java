package org.skillbridge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bound to {@code app.email.*} in application.properties.
 *
 * All values are env-driven at runtime; nothing here is a secret.
 */
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

    /** gmail | resend | smtp — switchable without code changes. */
    private String provider = "gmail";

    /** From-address shown in outgoing emails. */
    private String from = "noreply@skillbridge.ng";

    /** Base URL used to construct verification links (e.g., https://beta.skillbridge.ng). */
    private String appBaseUrl = "http://localhost:5173";

    /** How long a verification token is valid, in hours. */
    private int verificationTtlHours = 24;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getAppBaseUrl() { return appBaseUrl; }
    public void setAppBaseUrl(String appBaseUrl) { this.appBaseUrl = appBaseUrl; }

    public int getVerificationTtlHours() { return verificationTtlHours; }
    public void setVerificationTtlHours(int verificationTtlHours) { this.verificationTtlHours = verificationTtlHours; }
}
