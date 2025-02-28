package me.aboullaite.testcontainersdemo.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aboullaite.testcontainersdemo.model.InventoryEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryEventProducer {

  private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

  @Value("${app.kafka.inventory-topic}")
  private String topic;

  public void sendInventoryEvent(InventoryEvent event) {
    log.info("Sending inventory event: {}", event);
    kafkaTemplate.send(topic, String.valueOf(event.getProductId()), event);
  }
}