package me.aboullaite.testcontainersdemo.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;
import me.aboullaite.testcontainersdemo.AbstractIntegrationTest;
import me.aboullaite.testcontainersdemo.config.RedisConfig;
import me.aboullaite.testcontainersdemo.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

@Import({RedisConfig.class})
@EnableCaching
@ImportAutoConfiguration(classes = {
    CacheAutoConfiguration.class,
    RedisAutoConfiguration.class
})
class ProductServiceTest extends AbstractIntegrationTest {

  @Autowired
  private ProductService productService;

  @Autowired
  private CacheManager cacheManager;

  private Product testProduct;

  @BeforeEach
  void setUp() {
    testProduct = Product.builder()
        .name("Cached Product")
        .description("This product will be cached")
        .price(BigDecimal.valueOf(29.99))
        .stock(15)
        .build();

    testProduct = productService.save(testProduct);
  }

  @Test
  void shouldCacheProductWhenFindById() {
    // First call should cache the result
    long startTime = System.currentTimeMillis();
    Optional<Product> firstCall = productService.findById(testProduct.getId());
    long firstCallDuration = System.currentTimeMillis() - startTime;

    // Second call should be faster due to caching
    startTime = System.currentTimeMillis();
    Optional<Product> secondCall = productService.findById(testProduct.getId());
    long secondCallDuration = System.currentTimeMillis() - startTime;

    assertThat(firstCall).isPresent();
    assertThat(secondCall).isPresent();
    assertThat(secondCallDuration).isLessThan(firstCallDuration);

    // Verify that the product is in the cache
    assertThat(cacheManager.getCache("products").get(testProduct.getId())).isNotNull();
  }

  @Test
  void shouldEvictCacheWhenUpdatingProduct() {
    // First load the product into cache
    productService.findById(testProduct.getId());

    // Update the product
    testProduct.setPrice(BigDecimal.valueOf(34.99));
    productService.save(testProduct);

    // Cache should be evicted
    assertThat(cacheManager.getCache("products").get(testProduct.getId())).isNull();

    // Next find should fetch from database and cache again
    Optional<Product> updatedProduct = productService.findById(testProduct.getId());
    assertThat(updatedProduct).isPresent();
    assertThat(updatedProduct.get().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(34.99));

    // Verify it's cached again
    assertThat(cacheManager.getCache("products").get(testProduct.getId())).isNotNull();
  }
}