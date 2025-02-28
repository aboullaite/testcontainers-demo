package me.aboullaite.testcontainersdemo;

import me.aboullaite.testcontainersdemo.model.Product;
import me.aboullaite.testcontainersdemo.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TestcontainersDemoApplicationTests extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ProductService productService;

  @Test
  void shouldDisplayHomePageWithProducts() throws Exception {
    // Given
    Product product = Product.builder()
        .name("Test Product")
        .description("A product for testing")
        .price(BigDecimal.valueOf(29.99))
        .stock(10)
        .build();

    Product savedProduct = productService.save(product);

    // When & Then
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Test Product")))
        .andExpect(content().string(containsString("$29.99")))
        .andExpect(view().name("index"));
  }

  @Test
  void shouldDisplayProductDetail() throws Exception {
    // Given
    Product product = Product.builder()
        .name("Detail Test Product")
        .description("Testing product detail page")
        .price(BigDecimal.valueOf(49.99))
        .stock(5)
        .build();

    Product savedProduct = productService.save(product);

    // When & Then
    mockMvc.perform(get("/products/" + savedProduct.getId()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Detail Test Product")))
        .andExpect(content().string(containsString("Testing product detail page")))
        .andExpect(content().string(containsString("$49.99")))
        .andExpect(view().name("product-detail"));
  }

  @Test
  void shouldCreateNewProduct() throws Exception {
    // When & Then
    mockMvc.perform(post("/products")
            .param("name", "New Product")
            .param("description", "Created through web form")
            .param("price", "19.99")
            .param("stock", "15")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(flash().attributeExists("message"));

    // Verify product was created
    await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
      assertThat(productService.findAll()
          .stream()
          .anyMatch(p -> p.getName().equals("New Product"))).isTrue();
    });
  }

  @Test
  void shouldUpdateStockViaKafka() throws Exception {
    // Given
    Product product = Product.builder()
        .name("Kafka Test")
        .description("Testing Kafka integration")
        .price(BigDecimal.valueOf(39.99))
        .stock(10)
        .build();

    Product savedProduct = productService.save(product);

    // When adding stock
    mockMvc.perform(post("/products/" + savedProduct.getId() + "/stock/add")
            .param("quantity", "5")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/products/" + savedProduct.getId()))
        .andExpect(flash().attributeExists("message"));

    // Then verify stock is updated via Kafka
    await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      Product updatedProduct = productService.findById(savedProduct.getId()).orElseThrow();
      assertThat(updatedProduct.getStock()).isEqualTo(15);
    });
  }
}