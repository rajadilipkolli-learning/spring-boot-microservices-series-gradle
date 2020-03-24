package com.example.catalogservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

  @Id
  @SequenceGenerator(name = "product_id_generator", sequenceName = "product_id_seq", allocationSize = 1)
  @GeneratedValue(generator = "product_id_generator")
  private Long id;

  @NotBlank(message = "Code cannot be empty")
  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String name;

  private String description;

  private double price;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Product product = (Product) o;
    return this.code.equals(product.getCode());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.code);
  }
}
