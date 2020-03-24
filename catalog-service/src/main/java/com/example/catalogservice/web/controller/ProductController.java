package com.example.catalogservice.web.controller;

import com.example.catalogservice.entity.Product;
import com.example.catalogservice.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

  private final ProductService productService;

  @Autowired
  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping
  public List<Product> getAllProducts() {
    return productService.findAllProducts();
  }

  @GetMapping("/{code}")
  public ResponseEntity<Product> getProductById(@PathVariable String code) {
    return productService.findProductByCode(code).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Product createProduct(@RequestBody @Validated Product product) {
    return productService.saveProduct(product);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Product> updateProduct(@PathVariable Long id,
      @RequestBody Product product) {
    return productService.findProductById(id).map(productObj -> {
      product.setId(id);
      return ResponseEntity.ok(productService.saveProduct(product));
    }).orElseGet(() -> ResponseEntity.notFound().build());

  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Product> deleteProduct(@PathVariable Long id) {
    return productService.findProductById(id).map(product -> {
      productService.deleteProductById(id);
      return ResponseEntity.ok(product);
    }).orElseGet(() -> ResponseEntity.notFound().build());
  }
}
