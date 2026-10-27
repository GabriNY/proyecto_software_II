package me.zhulin.shopapi.repository;
import me.zhulin.shopapi.entity.Cart;
import me.zhulin.shopapi.entity.User;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@ActiveProfiles("test")
class CartRepositoryTest {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private EntityManager entityManager;
    @Test
    void guardarCarritoDeUsuario() {
        // ARRANGE: Crear usuario válido
        User user = new User();
        user.setEmail("carrito-test@example.com");
        user.setPassword("password123");
        user.setName("Usuario Prueba");
        user.setPhone("999888777");
        user.setAddress("Dirección de prueba");
        user.setActive(true);
        // Persistir usuario primero
        entityManager.persist(user);
        entityManager.flush();
        // Crear carrito asociado al usuario
        Cart cart = new Cart(user);
        // ACT: Guardar carrito en H2
        Cart saved = cartRepository.saveAndFlush(cart);
        // ASSERT: Comprobar identificador compartido
        assertNotNull(saved);
        assertEquals(user.getId().longValue(), saved.getCartId());
    }
    
    @Test
    void recuperarCarritoPorId() {
        // ARRANGE
        User user = new User();
        user.setEmail("recuperar-test@example.com");
        user.setPassword("password123");
        user.setName("Usuario Prueba");
        user.setPhone("999888777");
        user.setAddress("Dirección de prueba");
        user.setActive(true);

        entityManager.persist(user);
        entityManager.flush();

        Cart cart = new Cart(user);
        cartRepository.saveAndFlush(cart);

        Long cartId = cart.getCartId();

        // Limpiamos el contexto de persistencia
        entityManager.clear();

        // ACT
        Optional<Cart> recovered = cartRepository.findById(cartId);

        // ASSERT
        assertTrue(recovered.isPresent());
        assertEquals(cartId, recovered.get().getCartId());
    }
    
    @Test
    void eliminarCarritoExistente() {
        // ARRANGE
        User user = new User();
        user.setEmail("eliminar-test@example.com");
        user.setPassword("password123");
        user.setName("Usuario Prueba");
        user.setPhone("999888777");
        user.setAddress("Dirección de prueba");
        user.setActive(true);

        entityManager.persist(user);
        entityManager.flush();

        Cart cart = new Cart(user);
        Cart saved = cartRepository.saveAndFlush(cart);

        Long cartId = saved.getCartId();

        // ACT
        cartRepository.delete(saved);
        cartRepository.flush();
        entityManager.clear();

        // ASSERT
        Cart deleted = entityManager.find(Cart.class, cartId);

        assertNull(deleted);
        assertNotNull(entityManager.find(User.class, user.getId()));
    }
    
    @Test
    void buscarCarritoInexistente() {
        // ARRANGE
        Long idInexistente = 999999L;

        // ACT
        Optional<Cart> resultado =
                cartRepository.findById(idInexistente);

        // ASSERT
        assertTrue(resultado.isEmpty());
    }

}
