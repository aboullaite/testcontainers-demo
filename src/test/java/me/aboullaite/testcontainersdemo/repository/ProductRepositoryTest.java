package me.aboullaite.testcontainersdemo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import me.aboullaite.testcontainersdemo.AbstractIntegrationTest;
import me.aboullaite.testcontainersdemo.ProductRepository;
import me.aboullaite.testcontainersdemo.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest extends AbstractIntegrationTest {

  @Autowired
  private ProductRepository productRepository;

  @Test
  void shouldSaveAndFindProduct() {
    // Given
    Product product = Product.builder()
        .name("Test Product")
        .description("Test Description")
        .price(BigDecimal.valueOf(99.99))
        .stock(10)
        .build();

    // When
    Product savedProduct = productRepository.save(product);

    // Then
    assertThat(savedProduct.getId()).isNotNull();
    assertThat(productRepository.findById(savedProduct.getId()))
        .isPresent()
        .get()
        .satisfies(found -> {
          assertThat(found.getName()).isEqualTo("Test Product");
          assertThat(found.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
          assertThat(found.getStock()).isEqualTo(10);
        });
  }

  @Test
  void shouldFindProductsByLowStock() {
    // Given
    Product product1 = Product.builder()
        .name("Low Stock Product")
        .price(BigDecimal.valueOf(10.99))
        .stock(3)
        .build();

    Product product2 = Product.builder()
        .name("High Stock Product")
        .price(BigDecimal.valueOf(5.99))
        .stock(20)
        .build();

    productRepository.saveAll(List.of(product1, product2));

    // When
    List<Product> lowStockProducts = productRepository.findByStockLessThan(5);

    // Then
    assertThat(lowStockProducts).hasSize(1);
    assertThat(lowStockProducts.get(0).getName()).isEqualTo("Low Stock Product");
  }
}