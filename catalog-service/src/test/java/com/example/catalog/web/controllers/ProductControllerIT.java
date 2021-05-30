package com.example.catalog.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.catalog.common.AbstractIntegrationTest;
import com.example.catalog.entities.Product;
import com.example.catalog.repositories.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ProductControllerIT extends AbstractIntegrationTest {

    @Autowired private ProductRepository productRepository;

    private List<Product> productList = null;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        productList = new ArrayList<>();
        productList.add(new Product(1L, "PR001", "First Product", "Description 1", 50d));
        productList.add(new Product(2L, "PR002", "Second Product", "Description 2", 150d));
        productList.add(new Product(3L, "PR003", "Third Product", "Description 3", 250d));
        productList = productRepository.saveAll(productList);
    }

    @Test
    void shouldFetchAllProducts() throws Exception {
        this.mockMvc
                .perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(productList.size())));
    }

    @Test
    void shouldFindProductById() throws Exception {
        Product product = productList.get(0);
        Long productId = product.getId();

        this.mockMvc
                .perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(product.getCode())));
    }

    @Test
    void shouldCreateNewProduct() throws Exception {
        Product product = new Product(null, "PR004", "New Product", null, 350d);
        this.mockMvc
                .perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(product.getCode())));
    }

    @Test
    void shouldReturn400WhenCreateNewProductWithoutText() throws Exception {
        Product product = new Product(null, null, null, null, 0d);

        this.mockMvc
                .perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("code")))
                .andExpect(jsonPath("$.violations[0].message", is("Code cannot be empty")))
                .andExpect(jsonPath("$.violations[1].field", is("name")))
                .andExpect(jsonPath("$.violations[1].message", is("Name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        Product product = productList.get(0);
        product.setName("Updated Product");

        this.mockMvc
                .perform(
                        put("/api/products/{id}", product.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(product.getName())));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        Product product = productList.get(0);

        this.mockMvc
                .perform(delete("/api/products/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(product.getCode())));
    }
}
