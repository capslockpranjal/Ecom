package org.example.zenvybackend.common.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "email.provider", havingValue = "resend")
public class ResendEmailService implements EmailService {

    private final RestClient resendRestClient;

    @Value("${email.from}")
    private String fromAddress;

    @Override
    public void sendEmail(String to, String subject, String body) {
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException("EMAIL_FROM is required when email.provider=resend");
        }

        ResendEmailRequest request = new ResendEmailRequest(
                fromAddress,
                List.of(to),
                subject,
                body
        );

        resendRestClient.post()
                .uri("/emails")
                .body(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        (req, res) -> {
                            throw new IllegalStateException(
                                    "Resend API request failed with status " + res.getStatusCode().value()
                            );
                        })
                .toBodilessEntity();
    }
}
