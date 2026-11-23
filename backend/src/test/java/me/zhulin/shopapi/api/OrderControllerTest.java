package me.zhulin.shopapi.api;

import me.zhulin.shopapi.entity.OrderMain;
import me.zhulin.shopapi.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private static final String CLIENTE_1 = "customer1@email.com";
    private static final String CLIENTE_2 = "customer2@email.com";
    private static final String EMPLEADO = "employee1@email.com";

    @Mock
    private OrderService orderService;

    private OrderController orderController;
    private OrderMain pedido;

    @BeforeEach
    void setUp() {
        // Arrange común: el controlador recibe sus dependencias por constructor
        orderController = new OrderController(orderService);
        pedido = new OrderMain();
        pedido.setOrderId(1L);
        pedido.setBuyerEmail(CLIENTE_1);
    }

    private Authentication usuario(String email, String rol) {
        return new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority(rol)));
    }

    @Test
    void showClienteDuenoDevuelveSuPedido() {
        when(orderService.findOne(1L)).thenReturn(pedido);

        ResponseEntity<OrderMain> respuesta = orderController.show(1L, usuario(CLIENTE_1, "ROLE_CUSTOMER"));

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(pedido, respuesta.getBody());
    }

    @Test
    void showClienteAjenoDevuelve401() {
        when(orderService.findOne(1L)).thenReturn(pedido);

        ResponseEntity<OrderMain> respuesta = orderController.show(1L, usuario(CLIENTE_2, "ROLE_CUSTOMER"));

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
    }

    @Test
    void showEmpleadoVeCualquierPedido() {
        when(orderService.findOne(1L)).thenReturn(pedido);

        ResponseEntity<OrderMain> respuesta = orderController.show(1L, usuario(EMPLEADO, "ROLE_EMPLOYEE"));

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    }

    @Test
    void finishClienteDevuelve401YNoFinaliza() {
        ResponseEntity<OrderMain> respuesta = orderController.finish(1L, usuario(CLIENTE_1, "ROLE_CUSTOMER"));

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        verify(orderService, never()).finish(anyLong());
    }

    @Test
    void finishEmpleadoFinalizaElPedido() {
        when(orderService.finish(1L)).thenReturn(pedido);

        ResponseEntity<OrderMain> respuesta = orderController.finish(1L, usuario(EMPLEADO, "ROLE_EMPLOYEE"));

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        verify(orderService).finish(1L);
    }

    @Test
    void cancelClienteAjenoDevuelve401YNoCancela() {
        when(orderService.findOne(1L)).thenReturn(pedido);

        ResponseEntity<OrderMain> respuesta = orderController.cancel(1L, usuario(CLIENTE_2, "ROLE_CUSTOMER"));

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        verify(orderService, never()).cancel(anyLong());
    }

    @Test
    void orderListClienteSoloBuscaSusPedidos() {
        orderController.orderList(1, 10, usuario(CLIENTE_1, "ROLE_CUSTOMER"));

        verify(orderService).findByBuyerEmail(CLIENTE_1, PageRequest.of(0, 10));
        verify(orderService, never()).findAll(any());
    }
}