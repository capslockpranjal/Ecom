package org.example.zenvybackend.product.service;

import org.example.zenvybackend.product.entity.Product;

public interface ProductEmailService {

    void sendProductCreatedEmail(Product product);
}
