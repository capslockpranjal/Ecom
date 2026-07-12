package org.example.zenvybackend.common.email;

import java.util.List;

record ResendEmailRequest(String from, List<String> to, String subject, String text) {
}
