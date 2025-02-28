package me.aboullaite.testcontainersdemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {
  private Long productId;
  private Integer quantityChanged;
  private String operation; // "INCREASE" or "DECREASE"
}