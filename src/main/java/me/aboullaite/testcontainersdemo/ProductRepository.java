package me.aboullaite.testcontainersdemo;

import java.util.List;
import me.aboullaite.testcontainersdemo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
  List<Product> findByStockLessThan(Integer stock);
}
