
package me.zhulin.shopapi.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import me.zhulin.shopapi.entity.ProductInfo;

@DataJpaTest
@ActiveProfiles("test")
class ProductInfoRepositoryTest {

    @Autowired
    private ProductInfoRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void guardarYRecuperarProducto() {
        ProductInfo product = new ProductInfo();
        product.setProductId("LAB05-PRODUCT-001");
        product.setProductName("Laptop");
        product.setProductPrice(new BigDecimal("49.90"));
        product.setProductStock(10);
        repository.saveAndFlush(product);
        entityManager.clear();
        Optional<ProductInfo> resultado = repository.findById("LAB05-PRODUCT-001");
        assertTrue(resultado.isPresent());
        assertEquals("Laptop", resultado.get().getProductName());
        assertEquals(0, new BigDecimal("49.90")
                .compareTo(resultado.get().getProductPrice()));
        assertEquals(Integer.valueOf(10), resultado.get().getProductStock());
    }

    @Test
    void buscarProductoPorIdExistente() {
        ProductInfo product = new ProductInfo();
        product.setProductId("LAB05-PRODUCT-002");
        product.setProductName("Mouse Gamer");
        product.setProductPrice(new BigDecimal("89.90"));
        product.setProductStock(15);
        repository.saveAndFlush(product);
        entityManager.clear();
        ProductInfo resultado = repository.findByProductId("LAB05-PRODUCT-002");
        assertNotNull(resultado);
        assertEquals("LAB05-PRODUCT-002", resultado.getProductId());
        assertEquals("Mouse Gamer", resultado.getProductName());
    }

    @Test
    void buscarProductoPorIdInexistente() {
        String idInexistente = "LAB05-NO-EXISTE-999";
        ProductInfo resultado = repository.findByProductId(idInexistente);
        assertNull(resultado);
    }

    @Test
    void filtrarProductosPorEstado() {
        ProductInfo producto1 = new ProductInfo();
        producto1.setProductId("LAB05-EST-001");
        producto1.setProductName("Laptop");
        producto1.setProductPrice(new BigDecimal("1500.00"));
        producto1.setProductStock(10);
        producto1.setProductStatus(0);
        ProductInfo producto2 = new ProductInfo();
        producto2.setProductId("LAB05-EST-002");
        producto2.setProductName("Mouse");
        producto2.setProductPrice(new BigDecimal("50.00"));
        producto2.setProductStock(20);
        producto2.setProductStatus(0);
        ProductInfo producto3 = new ProductInfo();
        producto3.setProductId("LAB05-EST-003");
        producto3.setProductName("Teclado");
        producto3.setProductPrice(new BigDecimal("100.00"));
        producto3.setProductStock(5);
        producto3.setProductStatus(1);
        repository.saveAllAndFlush(List.of(producto1, producto2, producto3));
        entityManager.clear();
        Page<ProductInfo> pagina = repository.findAllByProductStatusOrderByProductIdAsc(
                        0, PageRequest.of(0, 1000));
        List<String> ids = pagina.getContent().stream()
                .map(ProductInfo::getProductId)
                .toList();
        assertTrue(ids.contains("LAB05-EST-001"));
        assertTrue(ids.contains("LAB05-EST-002"));
        assertFalse(ids.contains("LAB05-EST-003"));
        assertTrue(pagina.getContent().stream()
                .allMatch(p -> Integer.valueOf(0)
                        .equals(p.getProductStatus())));
        assertTrue(ids.indexOf("LAB05-EST-001")
                < ids.indexOf("LAB05-EST-002"));
    }

    @Test
    void filtrarProductosPorCategoria() {
        ProductInfo producto1 = new ProductInfo();
        producto1.setProductId("LAB05-CAT-001");
        producto1.setProductName("Laptop");
        producto1.setProductPrice(new BigDecimal("1500.00"));
        producto1.setProductStock(10);
        producto1.setCategoryType(1);
        ProductInfo producto2 = new ProductInfo();
        producto2.setProductId("LAB05-CAT-002");
        producto2.setProductName("Mouse");
        producto2.setProductPrice(new BigDecimal("50.00"));
        producto2.setProductStock(20);
        producto2.setCategoryType(1);
        ProductInfo producto3 = new ProductInfo();
        producto3.setProductId("LAB05-CAT-003");
        producto3.setProductName("Teclado");
        producto3.setProductPrice(new BigDecimal("100.00"));
        producto3.setProductStock(5);
        producto3.setCategoryType(2);
        repository.saveAllAndFlush(List.of(producto1, producto2, producto3));
        entityManager.clear();
        Page<ProductInfo> pagina = repository.findAllByCategoryTypeOrderByProductIdAsc(1, PageRequest.of(0, 1000));
        List<String> ids = pagina.getContent().stream()
                .map(ProductInfo::getProductId)
                .toList();
        assertTrue(ids.contains("LAB05-CAT-001"));
        assertTrue(ids.contains("LAB05-CAT-002"));
        assertFalse(ids.contains("LAB05-CAT-003"));
        assertTrue(pagina.getContent().stream()
                .allMatch(p -> Integer.valueOf(1)
                        .equals(p.getCategoryType())));
        assertEquals(ids.stream().sorted().toList(), ids);
    }

    @Test
    void paginacionDeProductosRespetaOrdenYTamanio() {
        for (int i = 1; i <= 3; i++) {
            ProductInfo product = new ProductInfo();
            product.setProductId("LAB05-PAG-00" + i);
            product.setProductName("Producto " + i);
            product.setProductPrice(new BigDecimal("100.00"));
            product.setProductStock(10);
            repository.save(product);
        }
        repository.flush();
        entityManager.clear();
        long totalProductos = repository.count();
        Page<ProductInfo> pagina0 = repository.findAllByOrderByProductId(PageRequest.of(0, 2));
        Page<ProductInfo> pagina1 = repository.findAllByOrderByProductId(PageRequest.of(1, 2));
        assertEquals(2, pagina0.getNumberOfElements());
        assertEquals((int) Math.min(2L, totalProductos - 2), pagina1.getNumberOfElements());
        assertEquals(totalProductos, pagina0.getTotalElements());
        assertEquals(totalProductos, pagina1.getTotalElements());
        List<String> idsPagina0 = pagina0.getContent().stream()
                .map(ProductInfo::getProductId)
                .toList();
        List<String> idsPagina1 = pagina1.getContent().stream()
                .map(ProductInfo::getProductId)
                .toList();
        assertEquals(idsPagina0.stream().sorted().toList(), idsPagina0);
        assertEquals(idsPagina1.stream().sorted().toList(), idsPagina1);
        assertTrue(idsPagina0.stream()
                .noneMatch(idsPagina1::contains));
        assertTrue(idsPagina0.get(1)
                .compareTo(idsPagina1.get(0)) < 0);
    }

}