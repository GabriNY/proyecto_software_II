package me.zhulin.shopapi.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

class ProductInfoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void stockNegativoEsInvalido() {
        ProductInfo product = new ProductInfo();
        product.setProductStock(-1);
        Set<ConstraintViolation<ProductInfo>> violations = validator.validateProperty(product, "productStock");
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(
                violation -> violation.getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType() == Min.class
        ));
    }


    @Test
    void stockCeroEsValido() {
        ProductInfo product = new ProductInfo();
        product.setProductStock(0);
        Set<ConstraintViolation<ProductInfo>> violations = validator.validateProperty(product, "productStock");
        assertTrue(violations.isEmpty());
    }

    @Test
    void nombreNuloEsInvalido() {
        ProductInfo product = new ProductInfo();
        product.setProductName(null);
        Set<ConstraintViolation<ProductInfo>> violations = validator.validateProperty(product, "productName");
        assertFalse(violations.isEmpty());
    }

    @Test
    void precioNuloEsInvalido() {
        ProductInfo product = new ProductInfo();
        product.setProductPrice(null);
        Set<ConstraintViolation<ProductInfo>> violations = validator.validateProperty(product, "productPrice");
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(
                violation -> violation.getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType() == NotNull.class
        ));
    }

    @Test
    void stockNuloEsInvalido() {
        ProductInfo product = new ProductInfo();
        product.setProductStock(null);
        Set<ConstraintViolation<ProductInfo>> violations = validator.validateProperty(product, "productStock");
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(
                violation -> violation.getConstraintDescriptor()
                        .getAnnotation()
                        .annotationType() == NotNull.class
        ));
    }

    @Test
    void nombreValidoNoGeneraViolaciones() {
        ProductInfo product = new ProductInfo();
        product.setProductName("Laptop");
        Set<ConstraintViolation<ProductInfo>> violations = validator.validateProperty(product, "productName");
        assertTrue(violations.isEmpty());
    }

    @Test
    void precioValidoNoGeneraViolaciones() {
        ProductInfo product = new ProductInfo();
        product.setProductPrice(new BigDecimal("49.90"));
        Set<ConstraintViolation<ProductInfo>> violations = validator.validateProperty(product, "productPrice");
        assertTrue(violations.isEmpty());
    }

    @Test
    void productoConDatosValidosNoGeneraViolaciones() {
        ProductInfo product = new ProductInfo();
        product.setProductName("Laptop");
        product.setProductPrice(new BigDecimal("49.90"));
        product.setProductStock(10);
        Set<ConstraintViolation<ProductInfo>> violations =
                validator.validate(product);
        assertTrue(violations.isEmpty());
    }
}