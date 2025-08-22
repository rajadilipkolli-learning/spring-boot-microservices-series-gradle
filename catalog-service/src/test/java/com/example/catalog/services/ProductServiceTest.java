package com.example.catalog.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import com.example.catalog.entities.Product;
import com.example.catalog.repositories.ProductRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final String TEST_PRODUCT_CODE = "P001";
    private static final String NEW_PRODUCT_NAME = "New Product";
    private static final String UPDATED_PRODUCT_NAME = "Updated Product";
    private static final String PRODUCT_1 = "Product 1";
    private static final String PRODUCT_2 = "Product 2";
    private static final String PRODUCT_3 = "Product 3";
    private static final double TEST_PRICE = 30.0;
    private static final String TEST_DESCRIPTION = "Test Description";

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private List<Product> productList;

    @BeforeEach
    void setUp() {
        productList = new ArrayList<>();
        productList.add(new Product(1L, TEST_PRODUCT_CODE, PRODUCT_1, null, TEST_PRICE));
        productList.add(new Product(2L, "P002", PRODUCT_2, null, 40.0));
        productList.add(new Product(3L, "P003", PRODUCT_3, null, 50.0));
    }

    @Test
    void shouldReturnAllProducts() {
        given(productRepository.findAll()).willReturn(productList);

        List<Product> result = productService.findAllProducts();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyElementsOf(productList);
        verify(productRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoProducts() {
        given(productRepository.findAll()).willReturn(Collections.emptyList());

        List<Product> result = productService.findAllProducts();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(productRepository).findAll();
    }

    @Test
    void shouldReturnProductById() {
        Long productId = 1L;
        Product product = new Product(productId, TEST_PRODUCT_CODE, PRODUCT_1, null, TEST_PRICE);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        Optional<Product> result = productService.findProductById(productId);

        assertThat(result).isPresent();
        Product foundProduct = result.get();
        assertThat(foundProduct.getId()).isEqualTo(productId);
        assertThat(foundProduct.getCode()).isEqualTo(TEST_PRODUCT_CODE);
        assertThat(foundProduct.getName()).isEqualTo(PRODUCT_1);
        verify(productRepository).findById(productId);
    }

    @Test
    void shouldReturnEmptyWhenProductNotFound() {
        Long productId = 99L;
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        Optional<Product> result = productService.findProductById(productId);

        assertThat(result).isEmpty();
        verify(productRepository).findById(productId);
    }

    @Test
    void shouldSaveProduct() {
        Product inputProduct = new Product(null, TEST_PRODUCT_CODE, NEW_PRODUCT_NAME, TEST_DESCRIPTION, TEST_PRICE);
        Product mockedProduct = new Product(1L, TEST_PRODUCT_CODE, NEW_PRODUCT_NAME, TEST_DESCRIPTION, TEST_PRICE);
        given(productRepository.save(any(Product.class))).willReturn(mockedProduct);

        Product savedProduct = productService.saveProduct(inputProduct);

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isEqualTo(1L);
        assertThat(savedProduct.getCode()).isEqualTo(TEST_PRODUCT_CODE);
        assertThat(savedProduct.getName()).isEqualTo(NEW_PRODUCT_NAME);
        assertThat(savedProduct.getDescription()).isEqualTo(TEST_DESCRIPTION);
        verify(productRepository).save(inputProduct);
    }

    @Test
    void shouldUpdateExistingProduct() {
        Long productId = 1L;
        Product product = new Product(productId, TEST_PRODUCT_CODE, UPDATED_PRODUCT_NAME, TEST_DESCRIPTION, TEST_PRICE);
        given(productRepository.save(any(Product.class))).willReturn(product);

        Product updatedProduct = productService.saveProduct(product);

        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getId()).isEqualTo(productId);
        assertThat(updatedProduct.getName()).isEqualTo(UPDATED_PRODUCT_NAME);
        assertThat(updatedProduct.getPrice()).isEqualTo(TEST_PRICE);
        assertThat(updatedProduct.getDescription()).isEqualTo(TEST_DESCRIPTION);
        verify(productRepository).save(product);
    }

    @Test
    void shouldDeleteProductById() {
        Long productId = 1L;
        willDoNothing().given(productRepository).deleteById(productId);

        productService.deleteProductById(productId);

        verify(productRepository).deleteById(productId);
    }

    @Test
    void shouldHandleDeleteWhenProductNotFound() {
        Long productId = 99L;
        willThrow(new EmptyResultDataAccessException(1)).given(productRepository).deleteById(productId);

        assertThatThrownBy(() -> productService.deleteProductById(productId))
                .isInstanceOf(EmptyResultDataAccessException.class);

        verify(productRepository).deleteById(productId);
    }

    @Test
    void shouldHandleSaveWithNullProduct() {
        Product nullProduct = null;
        assertThatThrownBy(() -> productService.saveProduct(nullProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product cannot be null");
    }
}
