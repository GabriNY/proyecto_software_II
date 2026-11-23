
package me.zhulin.shopapi.api;

import me.zhulin.shopapi.entity.ProductInOrder;
import me.zhulin.shopapi.entity.User;
import me.zhulin.shopapi.enums.ResultEnum;
import me.zhulin.shopapi.exception.MyException;
import me.zhulin.shopapi.service.CartService;
import me.zhulin.shopapi.service.ProductInOrderService;
import me.zhulin.shopapi.service.ProductService;
import me.zhulin.shopapi.service.UserService;
import me.zhulin.shopapi.entity.Cart;
import me.zhulin.shopapi.entity.ProductInfo;
import me.zhulin.shopapi.form.ItemForm;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @InjectMocks
    private CartController cartController;

    @Mock
    private CartService cartService;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private ProductInOrderService productInOrderService;

    @Test
    void mergeCartQueFallaDebeDevolver400() {

        // ARRANGE
        String email = "customer1@email.com";

        Principal principal = () -> email;

        User user = new User();

        Collection<ProductInOrder> productos =
                List.of(new ProductInOrder());

        when(userService.findOne(email))
                .thenReturn(user);

        doThrow(new MyException(
                ResultEnum.PRODUCT_NOT_ENOUGH))
                .when(cartService)
                .mergeLocalCart(productos, user);

        // ACT
        ResponseEntity<?> respuesta =
                cartController.mergeCart(
                        productos, principal);

        // ASSERT
        assertEquals(
                HttpStatus.BAD_REQUEST,
                respuesta.getStatusCode()
        );
    }

    @Test
    void getCartDevuelveCarritoDelUsuario() {

        // ARRANGE
        String email = "customer1@email.com";
        Principal principal = () -> email;

        User user = new User();
        Cart cart = new Cart(user);

        when(userService.findOne(email)).thenReturn(user);
        when(cartService.getCart(user)).thenReturn(cart);

        // ACT
        Cart resultado = cartController.getCart(principal);

        // ASSERT
        assertSame(cart, resultado);
    }

    @Test
    void mergeCartCorrectoDevuelve200() {

        // ARRANGE
        String email = "customer1@email.com";
        Principal principal = () -> email;

        User user = new User();
        Cart cart = new Cart(user);

        Collection<ProductInOrder> productos =
                List.of(new ProductInOrder());

        when(userService.findOne(email)).thenReturn(user);
        when(cartService.getCart(user)).thenReturn(cart);

        // ACT
        ResponseEntity<Cart> respuesta =
                cartController.mergeCart(productos, principal);

        // ASSERT
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(cart, respuesta.getBody());

        verify(cartService).mergeLocalCart(productos, user);
    }

    @Test
    void addToCartConErrorDevuelveFalse() {

        // ARRANGE
        String email = "customer1@email.com";
        Principal principal = () -> email;

        User user = new User();

        ProductInfo producto = crearProducto();

        ItemForm form = new ItemForm();
        form.setProductId("B0001");
        form.setQuantity(2);

        when(userService.findOne(email)).thenReturn(user);
        when(productService.findOne("B0001"))
                .thenReturn(producto);

        doThrow(new MyException(ResultEnum.PRODUCT_NOT_ENOUGH))
                .when(cartService)
                .mergeLocalCart(anyCollection(), eq(user));

        // ACT
        boolean resultado =
                cartController.addToCart(form, principal);

        // ASSERT
        assertFalse(resultado);
    }

    @Test
    void addToCartCorrectoDevuelveTrue() {

        // ARRANGE
        String email = "customer1@email.com";
        Principal principal = () -> email;

        User user = new User();

        ItemForm form = new ItemForm();
        form.setProductId("B0001");
        form.setQuantity(2);

        when(userService.findOne(email)).thenReturn(user);
        when(productService.findOne("B0001"))
                .thenReturn(crearProducto());

        // ACT
        boolean resultado =
                cartController.addToCart(form, principal);

        // ASSERT
        assertTrue(resultado);

        verify(cartService)
                .mergeLocalCart(anyCollection(), eq(user));
    }

    @Test
    void checkoutDevuelve200YCreaPedido() {

        // ARRANGE
        String email = "customer1@email.com";
        Principal principal = () -> email;

        User user = new User();

        when(userService.findOne(email)).thenReturn(user);

        // ACT
        ResponseEntity<?> respuesta =
                cartController.checkout(principal);

        // ASSERT
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());

        verify(cartService).checkout(user);
    }

    private ProductInfo crearProducto() {

        ProductInfo producto = new ProductInfo();

        producto.setProductId("B0001");
        producto.setProductName("Core Java");
        producto.setProductPrice(new BigDecimal("30.00"));
        producto.setProductStock(96);

        return producto;
    }

}
