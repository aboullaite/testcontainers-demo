package me.aboullaite.testcontainersdemo.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aboullaite.testcontainersdemo.model.InventoryEvent;
import me.aboullaite.testcontainersdemo.service.ProductService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryEventConsumer {

  private final ProductService productService;

  @KafkaListener(topics = "${app.kafka.inventory-topic}", groupId = "${spring.kafka.consumer.group-id}")
  public void consumeInventoryEvent(InventoryEvent event) {
    log.info("Consuming inventory event: {}", event);

    productService.findById(event.getProductId()).ifPresent(product -> {
      if ("INCREASE".equals(event.getOperation())) {
        product.setStock(product.getStock() + event.getQuantityChanged());
      } else if ("DECREASE".equals(event.getOperation())) {
        product.setStock(Math.max(0, product.getStock() - event.getQuantityChanged()));
      }
      productService.save(product);
    });
  }
}