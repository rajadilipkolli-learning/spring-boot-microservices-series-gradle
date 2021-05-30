package com.example.catalogservice.web.controller;

import com.example.catalogservice.entity.Product;
import com.example.catalogservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.catalogservice.utils.Constants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@ActiveProfiles({ PROFILE_TEST })
class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ProductService productService;

  @Autowired
  private ObjectMapper objectMapper;

  private List<Product> productList;

  @BeforeAll
  void setUp() {
    this.productList = new ArrayList<>();
    this.productList.add(new Product(1L, "P001", "Product 1", null, 25));
    this.productList.add(new Product(2L, "P002", "Product 2", null, 30));
    this.productList.add(new Product(3L, "P003", "Product 3", null, 35));

    objectMapper.registerModule(new ProblemModule());
    objectMapper.registerModule(new ConstraintViolationProblemModule());
  }

  @Test
  void shouldFetchAllProducts() throws Exception {
    given(productService.findAllProducts()).willReturn(this.productList);

    this.mockMvc.perform(get("/api/products")).andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(productList.size())));
  }

  @Test
  void shouldFindProductByCode() throws Exception {
    String productCode = "P001";
    Product product = new Product(1L, "P001", "Product 1", null, 25);
    given(productService.findProductByCode(productCode)).willReturn(Optional.of(product));

    this.mockMvc.perform(get("/api/products/{code}", productCode))
        .andExpect(status().isOk()).andExpect(jsonPath("$.code", is(product.getCode())));
  }

  @Test
  void shouldReturn404WhenFetchingNonExistingProduct() throws Exception {
    Long productId = 1L;
    given(productService.findProductById(productId)).willReturn(Optional.empty());

    this.mockMvc.perform(get("/api/products/{id}", productId))
        .andExpect(status().isNotFound());

  }

  @Test
  void shouldCreateNewProduct() throws Exception {
    given(productService.saveProduct(any(Product.class)))
        .willAnswer((invocation) -> invocation.getArgument(0));

    Product product = new Product(1L, "P001", "Product 1", null, 25);
    this.mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(product)))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.id", notNullValue()))
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
    Long productId = 1L;
    Product product = new Product(productId, "P001", "Product 1", null, 25);
    given(productService.findProductById(productId)).willReturn(Optional.of(product));
    given(productService.saveProduct(any(Product.class)))
        .willAnswer((invocation) -> invocation.getArgument(0));

    this.mockMvc.perform(
        put("/api/products/{id}", product.getId()).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(product))).andExpect(status().isOk())
        .andExpect(jsonPath("$.code", is(product.getCode())));

  }

  @Test
  void shouldReturn404WhenUpdatingNonExistingProduct() throws Exception {
    Long productId = 1L;
    given(productService.findProductById(productId)).willReturn(Optional.empty());
    Product product = new Product(productId, "P001", "Product 1", null, 25);

    this.mockMvc.perform(
        put("/api/products/{id}", productId).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(product)))
        .andExpect(status().isNotFound());

  }

  @Test
  void shouldDeleteProduct() throws Exception {
    Long productId = 1L;
    Product product = new Product(productId, "P001", "Product 1", null, 25);
    given(productService.findProductById(productId)).willReturn(Optional.of(product));
    doNothing().when(productService).deleteProductById(product.getId());

    this.mockMvc.perform(delete("/api/products/{id}", product.getId()))
        .andExpect(status().isOk()).andExpect(jsonPath("$.code", is(product.getCode())));

  }

  @Test
  void shouldReturn404WhenDeletingNonExistingProduct() throws Exception {
    Long productId = 1L;
    given(productService.findProductById(productId)).willReturn(Optional.empty());

    this.mockMvc.perform(delete("/api/products/{id}", productId))
        .andExpect(status().isNotFound());

  }

}
