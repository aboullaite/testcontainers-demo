package me.aboullaite.testcontainersdemo.service;

import lombok.RequiredArgsConstructor;
import me.aboullaite.testcontainersdemo.ProductRepository;
import me.aboullaite.testcontainersdemo.model.Product;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Cacheable(value = "products", key = "#id")
  public Optional<Product> findById(Long id) {
    // Simulating slow operation to demonstrate caching
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return productRepository.findById(id);
  }

  public List<Product> findAll() {
    return productRepository.findAll();
  }

  public List<Product> findLowStock(Integer threshold) {
    return productRepository.findByStockLessThan(threshold);
  }

  @CacheEvict(value = "products", key = "#product.id")
  public Product save(Product product) {
    return productRepository.save(product);
  }

  @CacheEvict(value = "products", key = "#id")
  public void deleteById(Long id) {
    productRepository.deleteById(id);
  }
}
