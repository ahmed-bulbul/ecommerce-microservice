package com.bulbul.productservice.service;


import com.bulbul.productservice.model.ProductRequest;
import com.bulbul.productservice.model.ProductResponse;

public interface ProductService {
    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);
}
