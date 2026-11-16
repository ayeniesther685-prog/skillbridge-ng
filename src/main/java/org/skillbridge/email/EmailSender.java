package org.skillbridge.email;

/**
 * Transport-agnostic email sender. The Gmail SMTP implementation is the
 * default for beta; a Resend implementation can be added later by simply
 * implementing this interface and setting app.email.provider=resend.
 *
 * Implementations MUST NOT throw on invalid recipient email — they should
 * log and return false so that registration does not fail because of a
 * bad address. Only throw when the transport itself is broken.
 */
public interface EmailSender {

    /**
     * Sends a multipart email (HTML + plain text).
     *
     * @return true if sent successfully, false if the transport failed
     */
    boolean send(String to, String subject, String htmlBody, String textBody);
}
