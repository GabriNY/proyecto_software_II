package me.zhulin.shopapi.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import me.zhulin.shopapi.enums.OrderStatusEnum;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lab 05 - Actividad 3 (DOMINIO) - Módulo Pedidos.
 * Caracteriza el comportamiento real de OrderMain: constructor OrderMain(User),
 * cálculo del monto total y los invariantes declarados con Bean Validation.
 */
@DisplayName("Dominio - OrderMain")
class OrderMainTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    private User buyer;

    @BeforeAll
    static void initValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @BeforeEach
    void setUp() {
        buyer = new User();
        buyer.setEmail("ana@ucsp.edu.pe");
        buyer.setName("Ana");
        buyer.setPhone("987654321");
        buyer.setAddress("Av. Salaverry 301");

        Cart cart = new Cart();
        cart.getProducts().add(item("P001", "10.50", 2));   // 21.00
        cart.getProducts().add(item("P002", "5.00", 3));    // 15.00
        buyer.setCart(cart);
    }

    @AfterEach
    void tearDown() {
        buyer = null;
    }

    // ---------------- Constructor OrderMain(User) ----------------

    @Test
    @DisplayName("Copia los datos del comprador")
    void constructor_copiaDatosDelComprador() {
        // Arrange: el comprador ya está listo en setUp()
        // Act
        OrderMain order = new OrderMain(buyer);
        // Assert
        assertAll(
                () -> assertEquals("ana@ucsp.edu.pe", order.getBuyerEmail()),
                () -> assertEquals("Ana", order.getBuyerName()),
                () -> assertEquals("987654321", order.getBuyerPhone()),
                () -> assertEquals("Av. Salaverry 301", order.getBuyerAddress()));
    }

    @Test
    @DisplayName("El total es la suma de precio x cantidad de cada item")
    void constructor_calculaMontoTotal() {
        OrderMain order = new OrderMain(buyer);

        // 2 x 10.50 + 3 x 5.00 = 36.00 (compareTo ignora la escala del BigDecimal)
        assertEquals(0, new BigDecimal("36.00").compareTo(order.getOrderAmount()));
    }

    @Test
    @DisplayName("Límite: con el carrito vacío el total es 0")
    void constructor_carritoVacio_montoCero() {
        buyer.getCart().setProducts(new HashSet<>());

        OrderMain order = new OrderMain(buyer);

        assertEquals(0, BigDecimal.ZERO.compareTo(order.getOrderAmount()));
    }

    @Test
    @DisplayName("Un pedido nuevo empieza en estado NEW (0)")
    void constructor_estadoInicialNew() {
        OrderMain order = new OrderMain(buyer);

        assertEquals(OrderStatusEnum.NEW.getCode(), order.getOrderStatus());
    }

    @Test
    @DisplayName("Caracterización: un usuario sin carrito provoca NullPointerException")
    void constructor_usuarioSinCarrito_lanzaNPE() {
        buyer.setCart(null);

        assertThrows(NullPointerException.class, () -> new OrderMain(buyer));
    }

    // ---------------- Invariantes (Bean Validation) ----------------
    // Se valida campo por campo con validateProperty: orderId es @NotNull pero
    // recién se genera al persistir, así que validar el objeto entero siempre fallaría.

    @Test
    @DisplayName("Un pedido construido correctamente no viola sus invariantes")
    void pedidoValido_noTieneViolaciones() {
        OrderMain order = new OrderMain(buyer);

        for (String field : new String[]{"buyerEmail", "buyerName", "buyerPhone",
                "buyerAddress", "orderAmount", "orderStatus"}) {
            assertTrue(violations(order, field).isEmpty(), "No debería haber violación en " + field);
        }
    }

    @Test
    @DisplayName("INVARIANTE: el email del comprador no puede estar vacío")
    void emailVacio_violaInvariante() {
        OrderMain order = new OrderMain(buyer);
        order.setBuyerEmail("");

        assertFalse(violations(order, "buyerEmail").isEmpty());
    }

    @Test
    @DisplayName("INVARIANTE: el monto no puede ser null")
    void montoNulo_violaInvariante() {
        OrderMain order = new OrderMain(buyer);
        order.setOrderAmount(null);

        assertFalse(violations(order, "orderAmount").isEmpty());
    }

    @Test
    @DisplayName("INVARIANTE: el estado no puede ser null")
    void estadoNulo_violaInvariante() {
        OrderMain order = new OrderMain(buyer);
        order.setOrderStatus(null);

        assertFalse(violations(order, "orderStatus").isEmpty());
    }

    // ---------------- Helpers ----------------

    private static ProductInOrder item(String productId, String price, int count) {
        ProductInOrder line = new ProductInOrder();
        line.setProductId(productId);
        line.setProductPrice(new BigDecimal(price));
        line.setCount(count);
        return line;
    }

    private Set<ConstraintViolation<OrderMain>> violations(OrderMain order, String field) {
        return validator.validateProperty(order, field);
    }
}
