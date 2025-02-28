package me.aboullaite.testcontainersdemo.web;

import lombok.RequiredArgsConstructor;
import me.aboullaite.testcontainersdemo.messaging.InventoryEventProducer;
import me.aboullaite.testcontainersdemo.model.InventoryEvent;
import me.aboullaite.testcontainersdemo.model.Product;
import me.aboullaite.testcontainersdemo.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

  private final ProductService productService;
  private final InventoryEventProducer inventoryEventProducer;

  @GetMapping("/")
  public String index(Model model) {
    List<Product> products = productService.findAll();
    model.addAttribute("products", products);
    model.addAttribute("newProduct", new Product());
    return "index";
  }

  @GetMapping("/products/{id}")
  public String getProduct(@PathVariable Long id, Model model) {
    productService.findById(id).ifPresent(product -> model.addAttribute("product", product));
    return "product-detail";
  }

  @PostMapping("/products")
  public String createProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
    Product savedProduct = productService.save(product);
    redirectAttributes.addFlashAttribute("message", "Product created successfully!");
    return "redirect:/";
  }

  @PostMapping("/products/{id}/stock/add")
  public String addStock(@PathVariable Long id, @RequestParam Integer quantity, RedirectAttributes redirectAttributes) {
    InventoryEvent event = InventoryEvent.builder()
        .productId(id)
        .quantityChanged(quantity)
        .operation("INCREASE")
        .build();
    inventoryEventProducer.sendInventoryEvent(event);
    redirectAttributes.addFlashAttribute("message", "Stock increase request sent!");
    return "redirect:/products/" + id;
  }

  @PostMapping("/products/{id}/stock/reduce")
  public String reduceStock(@PathVariable Long id, @RequestParam Integer quantity, RedirectAttributes redirectAttributes) {
    InventoryEvent event = InventoryEvent.builder()
        .productId(id)
        .quantityChanged(quantity)
        .operation("DECREASE")
        .build();
    inventoryEventProducer.sendInventoryEvent(event);
    redirectAttributes.addFlashAttribute("message", "Stock reduction request sent!");
    return "redirect:/products/" + id;
  }

  @PostMapping("/products/{id}/delete")
  public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    productService.deleteById(id);
    redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
    return "redirect:/";
  }

  @GetMapping("/lowstock")
  public String getLowStockProducts(Model model) {
    List<Product> lowStockProducts = productService.findLowStock(5);
    model.addAttribute("products", lowStockProducts);
    model.addAttribute("title", "Low Stock Products");
    return "product-list";
  }
}