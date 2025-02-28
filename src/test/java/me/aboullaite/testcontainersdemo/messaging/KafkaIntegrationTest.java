package me.aboullaite.testcontainersdemo.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import me.aboullaite.testcontainersdemo.AbstractIntegrationTest;
import me.aboullaite.testcontainersdemo.model.InventoryEvent;
import me.aboullaite.testcontainersdemo.model.Product;
import me.aboullaite.testcontainersdemo.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class KafkaIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private ProductService productService;

  @Autowired
  private InventoryEventProducer producer;

  @Test
  void shouldProcessInventoryEvent() {
    // Given
    Product product = Product.builder()
        .name("Kafka Test Product")
        .price(BigDecimal.valueOf(49.99))
        .stock(10)
        .build();

    product = productService.save(product);

    // When
    InventoryEvent event = InventoryEvent.builder()
        .productId(product.getId())
        .quantityChanged(5)
        .operation("INCREASE")
        .build();

    producer.sendInventoryEvent(event);

    // Then
    final Product finalProduct = product;
    await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Product updatedProduct = productService.findById(finalProduct.getId()).orElseThrow();
      assertThat(updatedProduct.getStock()).isEqualTo(15);
    });

    // Test decrease operation
    InventoryEvent decreaseEvent = InventoryEvent.builder()
        .productId(product.getId())
        .quantityChanged(3)
        .operation("DECREASE")
        .build();

    producer.sendInventoryEvent(decreaseEvent);

    final Product finalProduct1 = product;
    await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Product updatedProduct = productService.findById(finalProduct1.getId()).orElseThrow();
      assertThat(updatedProduct.getStock()).isEqualTo(12);
    });
  }
}
