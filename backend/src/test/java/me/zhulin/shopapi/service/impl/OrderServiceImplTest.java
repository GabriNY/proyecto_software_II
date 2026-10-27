package me.zhulin.shopapi.service.impl;

import me.zhulin.shopapi.entity.OrderMain;
import me.zhulin.shopapi.entity.ProductInOrder;
import me.zhulin.shopapi.entity.ProductInfo;
import me.zhulin.shopapi.enums.OrderStatusEnum;
import me.zhulin.shopapi.exception.MyException;
import me.zhulin.shopapi.repository.OrderRepository;
import me.zhulin.shopapi.repository.ProductInfoRepository;
import me.zhulin.shopapi.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

// ---- Imports añadidos en el Lab 05 ----
import me.zhulin.shopapi.enums.ResultEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductInfoRepository productInfoRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderMain orderMain;

    private ProductInfo productInfo;

    @BeforeEach
    public void setUp() {
        orderMain = new OrderMain();
        orderMain.setOrderId(1L);
        orderMain.setOrderStatus(OrderStatusEnum.NEW.getCode());

        ProductInOrder productInOrder = new ProductInOrder();
        productInOrder.setProductId("1");
        productInOrder.setCount(10);

        Set<ProductInOrder> set = new HashSet<>();
        set.add(productInOrder);

        orderMain.setProducts(set);

        productInfo = new ProductInfo();
        productInfo.setProductStock(10);
    }

    @Test
    public void finishSuccessTest() {
        when(orderRepository.findByOrderId(orderMain.getOrderId())).thenReturn(orderMain);

        OrderMain orderMainReturn = orderService.finish(orderMain.getOrderId());

        assertThat(orderMainReturn.getOrderId(), is(orderMain.getOrderId()));
        assertThat(orderMainReturn.getOrderStatus(), is(OrderStatusEnum.FINISHED.getCode()));
    }

    @Test
    public void finishStatusCanceledTest() {
        assertThrows(MyException.class, () -> {
            orderMain.setOrderStatus(OrderStatusEnum.CANCELED.getCode());

            when(orderRepository.findByOrderId(orderMain.getOrderId())).thenReturn(orderMain);

            OrderMain orderMainReturn = orderService.finish(orderMain.getOrderId());

            assertThat(orderMainReturn.getOrderId(), is(orderMain.getOrderId()));
            assertThat(orderMainReturn.getOrderStatus(), is(OrderStatusEnum.FINISHED.getCode()));
        });
    }

    @Test
    public void finishStatusFinishedTest() {
        assertThrows(MyException.class, () -> {
            orderMain.setOrderStatus(OrderStatusEnum.FINISHED.getCode());

            when(orderRepository.findByOrderId(orderMain.getOrderId())).thenReturn(orderMain);

            OrderMain orderMainReturn = orderService.finish(orderMain.getOrderId());

            assertThat(orderMainReturn.getOrderId(), is(orderMain.getOrderId()));
            assertThat(orderMainReturn.getOrderStatus(), is(OrderStatusEnum.FINISHED.getCode()));
        });
    }

    @Test
    public void cancelSuccessTest() {
        when(orderRepository.findByOrderId(orderMain.getOrderId())).thenReturn(orderMain);
        when(productInfoRepository.findByProductId(orderMain.getProducts().iterator().next().getProductId())).thenReturn(productInfo);

        OrderMain orderMainReturn = orderService.cancel(orderMain.getOrderId());

        assertThat(orderMainReturn.getOrderId(), is(orderMain.getOrderId()));
        assertThat(orderMainReturn.getOrderStatus(), is(OrderStatusEnum.CANCELED.getCode()));
        assertThat(orderMainReturn.getProducts().iterator().next().getCount(), is(10));
    }

    @Test
    public void cancelNoProduct() {
        when(orderRepository.findByOrderId(orderMain.getOrderId())).thenReturn(orderMain);
        orderMain.setProducts(new HashSet<>());

        OrderMain orderMainReturn = orderService.cancel(orderMain.getOrderId());

        assertThat(orderMainReturn.getOrderId(), is(orderMain.getOrderId()));
        assertThat(orderMainReturn.getOrderStatus(), is(OrderStatusEnum.CANCELED.getCode()));
    }

    @Test
    public void cancelStatusCanceledTest() {
        assertThrows(MyException.class, () -> {
            orderMain.setOrderStatus(OrderStatusEnum.CANCELED.getCode());

            when(orderRepository.findByOrderId(orderMain.getOrderId())).thenReturn(orderMain);

            orderService.cancel(orderMain.getOrderId());
        });
    }

    @Test
    public void cancelStatusFinishTest() {
        assertThrows(MyException.class, () -> {
            orderMain.setOrderStatus(OrderStatusEnum.FINISHED.getCode());

            when(orderRepository.findByOrderId(orderMain.getOrderId())).thenReturn(orderMain);

            orderService.cancel(orderMain.getOrderId());
        });
    }

    // =====================================================================
    // Lab 05 - Actividad 5 (SERVICIO DE APLICACIÓN) - Módulo Pedidos
    // Casos NO cubiertos por las 7 pruebas originales de arriba.
    // Los repositorios y ProductService son DOBLES (mocks de Mockito).
    // =====================================================================

    @AfterEach
    public void tearDown() {
        orderMain = null;
        productInfo = null;
    }

    // ---------------- findOne ----------------

    @Test
    @DisplayName("findOne devuelve el pedido que entrega el repositorio")
    public void findOneExistenteTest() {
        // Arrange
        when(orderRepository.findByOrderId(1L)).thenReturn(orderMain);
        // Act
        OrderMain result = orderService.findOne(1L);
        // Assert
        assertSame(orderMain, result);
        verify(orderRepository).findByOrderId(1L);
    }

    @Test
    @DisplayName("findOne de un pedido inexistente lanza ORDER_NOT_FOUND")
    public void findOneInexistenteTest() {
        // el mock devuelve null por defecto: el pedido "no existe"
        MyException ex = assertThrows(MyException.class, () -> orderService.findOne(99L));

        assertEquals(ResultEnum.ORDER_NOT_FOUND.getMessage(), ex.getMessage());
    }

    // ---------------- finish ----------------

    @Test
    @DisplayName("finish guarda el pedido y devuelve el pedido releído del repositorio")
    public void finishGuardaYReleeTest() {
        when(orderRepository.findByOrderId(1L)).thenReturn(orderMain);

        orderService.finish(1L);

        verify(orderRepository).save(orderMain);
        verify(orderRepository, times(2)).findByOrderId(1L);   // findOne + relectura final
    }

    @Test
    @DisplayName("finish rechazado (pedido CANCELED) no guarda ni cambia el estado")
    public void finishRechazadoNoGuardaTest() {
        orderMain.setOrderStatus(OrderStatusEnum.CANCELED.getCode());
        when(orderRepository.findByOrderId(1L)).thenReturn(orderMain);

        assertThrows(MyException.class, () -> orderService.finish(1L));

        assertEquals(OrderStatusEnum.CANCELED.getCode(), orderMain.getOrderStatus());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("finish de un pedido inexistente lanza ORDER_NOT_FOUND y no guarda")
    public void finishPedidoInexistenteTest() {
        MyException ex = assertThrows(MyException.class, () -> orderService.finish(99L));

        assertEquals(ResultEnum.ORDER_NOT_FOUND.getMessage(), ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    // ---------------- cancel: restitución de stock ----------------

    @Test
    @DisplayName("cancel restituye el stock de cada producto con su cantidad")
    public void cancelRestituyeStockTest() {
        ProductInOrder segundo = new ProductInOrder();
        segundo.setProductId("2");
        segundo.setCount(3);
        orderMain.getProducts().add(segundo);

        when(orderRepository.findByOrderId(1L)).thenReturn(orderMain);
        when(productInfoRepository.findByProductId("1")).thenReturn(productInfo);
        when(productInfoRepository.findByProductId("2")).thenReturn(new ProductInfo());

        orderService.cancel(1L);

        verify(orderRepository).save(orderMain);
        verify(productService).increaseStock("1", 10);
        verify(productService).increaseStock("2", 3);
    }

    @Test
    @DisplayName("cancel no restituye stock de un producto que ya no existe en el catálogo")
    public void cancelProductoInexistenteNoRestituyeTest() {
        when(orderRepository.findByOrderId(1L)).thenReturn(orderMain);
        // productInfoRepository.findByProductId("1") devuelve null por defecto

        OrderMain result = orderService.cancel(1L);

        assertEquals(OrderStatusEnum.CANCELED.getCode(), result.getOrderStatus());
        verify(productService, never()).increaseStock(anyString(), anyInt());
    }

    @Test
    @DisplayName("cancel con productos mixtos solo restituye los que siguen en el catálogo")
    public void cancelProductosMixtosTest() {
        ProductInOrder eliminado = new ProductInOrder();
        eliminado.setProductId("BORRADO");
        eliminado.setCount(5);
        orderMain.getProducts().add(eliminado);

        when(orderRepository.findByOrderId(1L)).thenReturn(orderMain);
        when(productInfoRepository.findByProductId("1")).thenReturn(productInfo);

        orderService.cancel(1L);

        verify(productService).increaseStock("1", 10);
        verify(productService, never()).increaseStock("BORRADO", 5);
    }

    @Test
    @DisplayName("cancel rechazado (pedido FINISHED) no guarda ni restituye stock")
    public void cancelRechazadoSinEfectosTest() {
        orderMain.setOrderStatus(OrderStatusEnum.FINISHED.getCode());
        when(orderRepository.findByOrderId(1L)).thenReturn(orderMain);

        assertThrows(MyException.class, () -> orderService.cancel(1L));

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("cancel de un pedido inexistente lanza ORDER_NOT_FOUND sin tocar el stock")
    public void cancelPedidoInexistenteTest() {
        MyException ex = assertThrows(MyException.class, () -> orderService.cancel(99L));

        assertEquals(ResultEnum.ORDER_NOT_FOUND.getMessage(), ex.getMessage());
        verifyNoInteractions(productService);
    }

    // ---------------- búsquedas paginadas ----------------

    @Test
    @DisplayName("findAll delega en el repositorio y devuelve su página")
    public void findAllDelegaTest() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<OrderMain> page = new PageImpl<>(List.of(orderMain));
        when(orderRepository.findAllByOrderByOrderStatusAscCreateTimeDesc(pageable)).thenReturn(page);

        assertSame(page, orderService.findAll(pageable));
        verify(orderRepository).findAllByOrderByOrderStatusAscCreateTimeDesc(pageable);
    }

    @Test
    @DisplayName("findByStatus delega con el estado y la página recibidos")
    public void findByStatusDelegaTest() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<OrderMain> page = new PageImpl<>(List.of(orderMain));
        when(orderRepository.findAllByOrderStatusOrderByCreateTimeDesc(0, pageable)).thenReturn(page);

        assertSame(page, orderService.findByStatus(0, pageable));
        verify(orderRepository).findAllByOrderStatusOrderByCreateTimeDesc(0, pageable);
    }

    @Test
    @DisplayName("findByBuyerEmail delega con el email recibido")
    public void findByBuyerEmailDelegaTest() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<OrderMain> page = new PageImpl<>(List.of(orderMain));
        when(orderRepository.findAllByBuyerEmailOrderByOrderStatusAscCreateTimeDesc("ana@ucsp.edu.pe", pageable))
                .thenReturn(page);

        assertSame(page, orderService.findByBuyerEmail("ana@ucsp.edu.pe", pageable));
        verify(orderRepository).findAllByBuyerEmailOrderByOrderStatusAscCreateTimeDesc("ana@ucsp.edu.pe", pageable);
    }

    @Test
    @DisplayName("findByBuyerPhone delega con el teléfono recibido")
    public void findByBuyerPhoneDelegaTest() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<OrderMain> page = new PageImpl<>(List.of(orderMain));
        when(orderRepository.findAllByBuyerPhoneOrderByOrderStatusAscCreateTimeDesc("987654321", pageable))
                .thenReturn(page);

        assertSame(page, orderService.findByBuyerPhone("987654321", pageable));
        verify(orderRepository).findAllByBuyerPhoneOrderByOrderStatusAscCreateTimeDesc("987654321", pageable);
    }
}
