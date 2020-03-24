package com.example.catalogservice.web.controller;

import com.example.catalogservice.common.AbstractIntegrationTest;
import com.example.catalogservice.entity.Product;
import com.example.catalogservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerIT extends AbstractIntegrationTest {

  @Autowired
  private ProductRepository productRepository;

  private List<Product> productList = null;

  @BeforeEach
  void setUp() {
    this.productRepository.deleteAll();

    productList = new ArrayList<>();
    this.productList.add(new Product(1L, "P001", "Product 1", null, 25));
    this.productList.add(new Product(2L, "P002", "Product 2", null, 30));
    this.productList.add(new Product(3L, "P003", "Product 3", null, 35));

    productList = productRepository.saveAll(productList);
  }

  @Test
  void shouldFetchAllProducts() throws Exception {
    this.mockMvc.perform(get("/api/products")).andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(productList.size())));
  }

  @Test
  void shouldFindProductByCode() throws Exception {
    Product product = productList.get(0);
    String productCode = product.getCode();

    this.mockMvc.perform(get("/api/products/{id}", productCode))
        .andExpect(status().isOk()).andExpect(jsonPath("$.code", is(product.getCode())));
  }

  @Test
  void shouldCreateNewProduct() throws Exception {
    Product product = new Product(null, "P004", "Product 4", null, 40);
    this.mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(product)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code", is(product.getCode())));

  }

  @Test
  void shouldReturn400WhenCreateNewProductWithoutCode() throws Exception {
    Product product = new Product(null, null, null, null, 0);

    this.mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(product)))
        .andExpect(status().isBadRequest())
        .andExpect(header().string("Content-Type", is("application/problem+json")))
        .andExpect(jsonPath("$.type",
            is("https://zalando.github.io/problem/constraint-violation")))
        .andExpect(jsonPath("$.title", is("Constraint Violation")))
        .andExpect(jsonPath("$.status", is(400)))
        .andExpect(jsonPath("$.violations", hasSize(1)))
        .andExpect(jsonPath("$.violations[0].field", is("code")))
        .andExpect(jsonPath("$.violations[0].message", is("Code cannot be empty")))
        .andReturn();
  }

  @Test
  void shouldUpdateProduct() throws Exception {
    Product product = productList.get(0);
    product.setDescription("Updated Product Description");

    this.mockMvc.perform(
        put("/api/products/{id}", product.getId()).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(product))).andExpect(status().isOk())
        .andExpect(jsonPath("$.description", is(product.getDescription())));

  }

  @Test
  void shouldDeleteProduct() throws Exception {
    Product product = productList.get(0);

    this.mockMvc.perform(delete("/api/products/{id}", product.getId()))
        .andExpect(status().isOk()).andExpect(jsonPath("$.code", is(product.getCode())));

  }

}
