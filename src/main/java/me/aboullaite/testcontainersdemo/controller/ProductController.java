package me.aboullaite.testcontainersdemo.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.aboullaite.testcontainersdemo.messaging.InventoryEventProducer;
import me.aboullaite.testcontainersdemo.model.InventoryEvent;
import me.aboullaite.testcontainersdemo.model.Product;
import me.aboullaite.testcontainersdemo.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final InventoryEventProducer inventoryEventProducer;

  @GetMapping
  public List<Product> getAllProducts() {
    return productService.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    return productService.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/lowstock/{threshold}")
  public List<Product> getLowStockProducts(@PathVariable Integer threshold) {
    return productService.findLowStock(threshold);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Product createProduct(@RequestBody Product product) {
    return productService.save(product);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
    return productService.findById(id)
        .map(existingProduct -> {
          product.setId(id);
          return ResponseEntity.ok(productService.save(product));
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    return productService.findById(id)
        .map(product -> {
          productService.deleteById(id);
          return ResponseEntity.ok().<Void>build();
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/stock/add/{quantity}")
  public ResponseEntity<Void> addStock(@PathVariable Long id, @PathVariable Integer quantity) {
    InventoryEvent event = InventoryEvent.builder()
        .productId(id)
        .quantityChanged(quantity)
        .operation("INCREASE")
        .build();
    inventoryEventProducer.sendInventoryEvent(event);
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/{id}/stock/reduce/{quantity}")
  public ResponseEntity<Void> reduceStock(@PathVariable Long id, @PathVariable Integer quantity) {
    InventoryEvent event = InventoryEvent.builder()
        .productId(id)
        .quantityChanged(quantity)
        .operation("DECREASE")
        .build();
    inventoryEventProducer.sendInventoryEvent(event);
    return ResponseEntity.accepted().build();
  }
}
