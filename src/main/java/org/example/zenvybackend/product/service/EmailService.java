package org.example.zenvybackend.product.service;

import org.example.zenvybackend.product.entity.Product;

public interface EmailService {
    void sendProductCreatedEmail(Product product);
}
