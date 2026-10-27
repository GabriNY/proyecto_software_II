
package me.zhulin.shopapi.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductInOrderTest {

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
    void cantidadUnoEsValida() {
        ProductInOrder product = new ProductInOrder();
        product.setCount(1);

        Set<ConstraintViolation<ProductInOrder>> violations =
                validator.validateProperty(product, "count");

        assertTrue(violations.isEmpty());
    }

    @Test
    void cantidadCeroEsInvalida() {
        ProductInOrder product = new ProductInOrder();
        product.setCount(0);

        Set<ConstraintViolation<ProductInOrder>> violations =
                validator.validateProperty(product, "count");

        assertFalse(violations.isEmpty());
    }

    @Test
    void stockNegativoEsInvalido() {
        ProductInOrder product = new ProductInOrder();
        product.setProductStock(-1);

        Set<ConstraintViolation<ProductInOrder>> violations =
                validator.validateProperty(product, "productStock");

        assertFalse(violations.isEmpty());
    }

    @Test
    void stockCeroEsValido() {
        ProductInOrder product = new ProductInOrder();
        product.setProductStock(0);

        Set<ConstraintViolation<ProductInOrder>> violations =
                validator.validateProperty(product, "productStock");

        assertTrue(violations.isEmpty());
    }
}
