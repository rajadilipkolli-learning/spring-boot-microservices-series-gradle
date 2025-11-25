package com.example.catalog.web.controllers;

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

import com.example.catalog.entities.Product;
import com.example.catalog.services.ProductService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ProductController.class)
@ActiveProfiles("test")
class ProductControllerTest {

    private static final String API_PRODUCTS = "/api/products";
    private static final String API_PRODUCTS_ID = "/api/products/{id}";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_PROBLEM_JSON = "application/problem+json";
    private static final String VIOLATIONS_PATH = "$.violations";
    private static final String VIOLATIONS_FIELD = "$.violations[0].field";
    private static final String VIOLATIONS_MESSAGE = "$.violations[0].message";
    private static final String CODE_FIELD = "code";
    private static final String NAME_FIELD = "name";
    private static final String CODE_EMPTY_MESSAGE = "Code cannot be empty";
    private static final String NAME_EMPTY_MESSAGE = "Name cannot be empty";
    private static final String CODE_PATH = "$.code";
    private static final String NAME_PATH = "$.name";
    private static final String PRODUCT_CODE_1 = "P001";
    private static final String PRODUCT_NAME_1 = "Product 1";
    private static final String PRODUCT_CODE_2 = "P002";
    private static final String PRODUCT_NAME_2 = "Product 2";
    private static final String PRODUCT_CODE_3 = "P003";
    private static final String PRODUCT_NAME_3 = "Product 3";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Product> productList;

    @BeforeEach
    void setUp() {
        this.productList = new ArrayList<>();
        this.productList.add(new Product(1L, PRODUCT_CODE_1, PRODUCT_NAME_1, null, 30d));
        this.productList.add(new Product(2L, PRODUCT_CODE_2, PRODUCT_NAME_2, null, 40d));
        this.productList.add(new Product(3L, PRODUCT_CODE_3, PRODUCT_NAME_3, null, 50d));
    }

    @Test
    void shouldFetchAllProducts() throws Exception {
        given(productService.findAllProducts()).willReturn(this.productList);

        this.mockMvc
                .perform(get(API_PRODUCTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(productList.size())));
    }

    @Test
    void shouldFindProductById() throws Exception {
        Long productId = 1L;
        Product product = new Product(productId, PRODUCT_CODE_1, PRODUCT_NAME_1, null, 30d);
        given(productService.findProductById(productId)).willReturn(Optional.of(product));

        this.mockMvc
                .perform(get(API_PRODUCTS_ID, productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODE_PATH, is(product.getCode())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingProduct() throws Exception {
        Long productId = 1L;
        given(productService.findProductById(productId)).willReturn(Optional.empty());

        this.mockMvc.perform(get(API_PRODUCTS_ID, productId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewProduct() throws Exception {
        given(productService.saveProduct(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

        Product product = new Product(1L, PRODUCT_CODE_1, PRODUCT_NAME_1, null, 30d);
        this.mockMvc
                .perform(post(API_PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath(CODE_PATH, is(product.getCode())));
    }

    @Test
    void shouldReturn400WhenCreateNewProductWithoutCodeAndName() throws Exception {
        Product product = new Product(null, null, null, null, 0d);

        this.mockMvc
                .perform(post(API_PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CONTENT_TYPE, is(APPLICATION_PROBLEM_JSON)))
                .andExpect(jsonPath("$.type", is("https://api.microservices.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath(VIOLATIONS_PATH, hasSize(2)))
                .andExpect(jsonPath(VIOLATIONS_FIELD, is(CODE_FIELD)))
                .andExpect(jsonPath(VIOLATIONS_MESSAGE, is(CODE_EMPTY_MESSAGE)))
                .andExpect(jsonPath("$.violations[1].field", is(NAME_FIELD)))
                .andExpect(jsonPath("$.violations[1].message", is(NAME_EMPTY_MESSAGE)));
    }

    @Test
    void shouldReturn400WhenCreateNewProductWithEmptyCode() throws Exception {
        Product product = new Product(null, "", "Product Name", null, 30d);

        this.mockMvc
                .perform(post(API_PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CONTENT_TYPE, is(APPLICATION_PROBLEM_JSON)))
                .andExpect(jsonPath(VIOLATIONS_PATH, hasSize(1)))
                .andExpect(jsonPath(VIOLATIONS_FIELD, is(CODE_FIELD)))
                .andExpect(jsonPath(VIOLATIONS_MESSAGE, is(CODE_EMPTY_MESSAGE)));
    }

    @Test
    void shouldReturn400WhenCreateNewProductWithEmptyName() throws Exception {
        Product product = new Product(null, PRODUCT_CODE_1, "", null, 30d);

        this.mockMvc
                .perform(post(API_PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CONTENT_TYPE, is(APPLICATION_PROBLEM_JSON)))
                .andExpect(jsonPath(VIOLATIONS_PATH, hasSize(1)))
                .andExpect(jsonPath(VIOLATIONS_FIELD, is(NAME_FIELD)))
                .andExpect(jsonPath(VIOLATIONS_MESSAGE, is(NAME_EMPTY_MESSAGE)));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        Long productId = 1L;
        Product product = new Product(productId, PRODUCT_CODE_1, "Updated Product", null, 30d);
        given(productService.findProductById(productId)).willReturn(Optional.of(product));
        given(productService.saveProduct(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

        this.mockMvc
                .perform(put(API_PRODUCTS_ID, product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODE_PATH, is(product.getCode())))
                .andExpect(jsonPath(NAME_PATH, is(product.getName())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingProduct() throws Exception {
        Long productId = 1L;
        given(productService.findProductById(productId)).willReturn(Optional.empty());
        Product product = new Product(productId, PRODUCT_CODE_1, "Updated Product", null, 30d);

        this.mockMvc
                .perform(put(API_PRODUCTS_ID, productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenUpdateProductWithInvalidData() throws Exception {
        Long productId = 1L;
        Product product = new Product(productId, "", "", null, 30d);
        given(productService.findProductById(productId)).willReturn(Optional.of(product));

        this.mockMvc
                .perform(put(API_PRODUCTS_ID, productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(CONTENT_TYPE, is(APPLICATION_PROBLEM_JSON)))
                .andExpect(jsonPath(VIOLATIONS_PATH, hasSize(2)));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        Long productId = 1L;
        Product product = new Product(productId, PRODUCT_CODE_1, PRODUCT_NAME_1, null, 30d);
        given(productService.findProductById(productId)).willReturn(Optional.of(product));
        doNothing().when(productService).deleteProductById(product.getId());

        this.mockMvc
                .perform(delete(API_PRODUCTS_ID, product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(CODE_PATH, is(product.getCode())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingProduct() throws Exception {
        Long productId = 1L;
        given(productService.findProductById(productId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete(API_PRODUCTS_ID, productId)).andExpect(status().isNotFound());
    }
}
