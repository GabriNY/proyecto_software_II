
package me.zhulin.shopapi.entity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CartTest {
    @Test
    void carritoNuevoTieneProductosVacios() {
        Cart cart = new Cart();
        assertNotNull(cart.getProducts());
        assertTrue(cart.getProducts().isEmpty());
    }
    @Test
    void carritoSeAsociaConUsuario() {
        User user = new User();
        Cart cart = new Cart(user);
        assertSame(user, cart.getUser());
    }
}
