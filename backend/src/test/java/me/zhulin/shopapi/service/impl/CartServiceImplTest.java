package me.zhulin.shopapi.service.impl;
import java.util.List;
import me.zhulin.shopapi.entity.OrderMain;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import me.zhulin.shopapi.entity.Cart;
import me.zhulin.shopapi.entity.OrderMain;
import me.zhulin.shopapi.entity.ProductInOrder;
import me.zhulin.shopapi.entity.User;
import me.zhulin.shopapi.exception.MyException;
import me.zhulin.shopapi.repository.CartRepository;
import me.zhulin.shopapi.repository.OrderRepository;
import me.zhulin.shopapi.repository.ProductInOrderRepository;
import me.zhulin.shopapi.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CartServiceImplTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private ProductService productService;

    @Mock
    private ProductInOrderRepository productInOrderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    private User user;

    private ProductInOrder productInOrder;

    private Set<ProductInOrder> set;

    private Cart cart;

    @BeforeEach
    public void setUp() {
        user = new User();
        cart = new Cart();

        user.setEmail("email@email.com");
        user.setName("Name");
        user.setPhone("Phone Test");
        user.setAddress("Address Test");

        productInOrder = new ProductInOrder();
        productInOrder.setProductId("1");
        productInOrder.setCount(10);
        productInOrder.setProductPrice(BigDecimal.valueOf(1));

        set = new HashSet<>();
        set.add(productInOrder);

        cart.setProducts(set);

        user.setCart(cart);
    }

    @Test
    public void mergeLocalCartTest() {
        cartService.mergeLocalCart(set, user);

        Mockito.verify(cartRepository, Mockito.times(1)).save(cart);
        Mockito.verify(productInOrderRepository, Mockito.times(1)).save(productInOrder);
    }

    @Test
    public void mergeLocalCartTwoProductTest() {
        ProductInOrder productInOrder2 = new ProductInOrder();
        productInOrder2.setProductId("2");
        productInOrder2.setCount(10);

        user.getCart().getProducts().add(productInOrder2);

        cartService.mergeLocalCart(set, user);

        Mockito.verify(cartRepository, Mockito.times(1)).save(cart);
        Mockito.verify(productInOrderRepository, Mockito.times(1)).save(productInOrder);
        Mockito.verify(productInOrderRepository, Mockito.times(1)).save(productInOrder2);
    }

    @Test
    public void mergeLocalCartNoProductTest() {
        user.getCart().setProducts(new HashSet<>());

        cartService.mergeLocalCart(set, user);

        Mockito.verify(cartRepository, Mockito.times(1)).save(cart);
        Mockito.verify(productInOrderRepository, Mockito.times(1)).save(productInOrder);
    }

    @Test
    public void deleteTest() {
        cartService.delete("1", user);

        Mockito.verify(productInOrderRepository, Mockito.times(1)).deleteById(productInOrder.getId());
    }

    @Test
    public void deleteNoProductTest() {
        assertThrows(MyException.class, () -> {
            cartService.delete("", user);
        });
    }

    @Test
    public void deleteNoUserTest() {
        assertThrows(MyException.class, () -> {
            cartService.delete("1", null);
        });
    }

    @Test
    public void checkoutTest() {
        cartService.checkout(user);

        Mockito.verify(productInOrderRepository, Mockito.times(1)).save(productInOrder);
    }
    
    @Test
    void mergeLocalCartSumaCantidadesDeProductoExistente() {
        // ARRANGE
        // El setUp() ya creó un producto con ID "1"
        // y cantidad 10 dentro del carrito.

        ProductInOrder nuevoProducto = new ProductInOrder();
        nuevoProducto.setProductId("1");
        nuevoProducto.setCount(3);

        // ACT
        cartService.mergeLocalCart(
            List.of(nuevoProducto),
            user
        );

        // ASSERT
        assertEquals(13, productInOrder.getCount());

        verify(productInOrderRepository, times(1))
            .save(productInOrder);

        verify(cartRepository, times(1))
            .save(cart);
    }
    @Test
    void getCartDevuelveCarritoDelUsuario() {
        // ARRANGE
        // El setUp() ya creó user y cart.

        // ACT
        Cart resultado = cartService.getCart(user);

        // ASSERT
        assertSame(cart, resultado);
    }
    @Test
    void deleteProductoInexistenteNoEliminaNada() {
        // ARRANGE
        String productoInexistente = "999";

        // ACT
        cartService.delete(productoInexistente, user);

        // ASSERT
        verify(productInOrderRepository, never())
            .deleteById(any());

        assertEquals(1, cart.getProducts().size());
    }
    
    @Test
    void checkoutActualizaPedidoYStock() {
        // ARRANGE
        String productId = productInOrder.getProductId();
        Integer cantidad = productInOrder.getCount();

        // ACT
        cartService.checkout(user);

        // ASSERT

        // Se solicita guardar el pedido
        verify(orderRepository, times(1))
            .save(any(OrderMain.class));

        // Se solicita disminuir el stock
        verify(productService, times(1))
            .decreaseStock(productId, cantidad);

        // El producto deja de apuntar al carrito
        assertNull(productInOrder.getCart());

        // El producto queda asociado al pedido
        assertNotNull(productInOrder.getOrderMain());

        // Se solicita guardar el producto actualizado
        verify(productInOrderRepository, times(1))
            .save(productInOrder);
    }

}
