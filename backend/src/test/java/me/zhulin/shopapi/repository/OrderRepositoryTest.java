package me.zhulin.shopapi.repository;

import me.zhulin.shopapi.entity.OrderMain;
import me.zhulin.shopapi.enums.OrderStatusEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lab 05 - Actividad 4 (REPOSITORIO) - Módulo Pedidos.
 * Pruebas de persistencia de OrderRepository con @DataJpaTest sobre H2 en memoria.
 * Cada prueba corre dentro de una transacción que se revierte al terminar.
 *
 * Los datos de prueba usan emails y teléfonos propios, para que los resultados
 * no dependan de los datos de ejemplo de import.sql.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Repositorio - OrderRepository")
class OrderRepositoryTest {

    private static final int NEW = OrderStatusEnum.NEW.getCode();
    private static final int FINISHED = OrderStatusEnum.FINISHED.getCode();

    private static final String ANA_EMAIL = "pedidos.ana@ucsp.edu.pe";
    private static final String LUIS_EMAIL = "pedidos.luis@ucsp.edu.pe";
    private static final String ANA_PHONE = "900000001";
    private static final String LUIS_PHONE = "900000002";

    @Autowired
    private OrderRepository orderRepository;

    private long pedidosPrevios;
    private OrderMain anaNew;
    private OrderMain anaFinished;
    private OrderMain luisNew;

    @BeforeEach
    void setUp() {
        pedidosPrevios = orderRepository.count();

        anaFinished = orderRepository.save(order(ANA_EMAIL, ANA_PHONE, FINISHED, "80.00"));
        anaNew = orderRepository.save(order(ANA_EMAIL, ANA_PHONE, NEW, "50.00"));
        luisNew = orderRepository.save(order(LUIS_EMAIL, LUIS_PHONE, NEW, "20.00"));
        orderRepository.flush();
    }

    @AfterEach
    void tearDown() {
        // Borra solo los pedidos creados por la prueba. Igual @DataJpaTest
        // revierte la transacción al terminar, así que la BD queda limpia.
        orderRepository.deleteAll(List.of(anaNew, anaFinished, luisNew));
    }

    @Test
    @DisplayName("save genera el id del pedido nuevo")
    void save_pedidoNuevo_generaId() {
        // Arrange + Act: los pedidos se guardaron en setUp()
        // Assert
        assertNotNull(anaNew.getOrderId());
        assertEquals(pedidosPrevios + 3, orderRepository.count());
    }

    @Test
    @DisplayName("findByOrderId con un id existente devuelve el pedido guardado")
    void findByOrderId_existente_devuelvePedido() {
        OrderMain found = orderRepository.findByOrderId(anaNew.getOrderId());

        assertNotNull(found);
        assertEquals(ANA_EMAIL, found.getBuyerEmail());
        assertEquals(0, new BigDecimal("50.00").compareTo(found.getOrderAmount()));
    }

    @Test
    @DisplayName("findByOrderId con un id inexistente devuelve null")
    void findByOrderId_inexistente_devuelveNull() {
        assertNull(orderRepository.findByOrderId(-1L));
    }

    @Test
    @DisplayName("findById acepta el id Long del pedido")
    void findById_conIdLong_encuentraPedido() {
        assertTrue(orderRepository.findById(luisNew.getOrderId()).isPresent());
    }

    @Test
    @DisplayName("findAllByOrderStatus devuelve solo pedidos con ese estado")
    void findAllByOrderStatus_new_devuelveSoloNuevos() {
        Page<OrderMain> page = orderRepository
                .findAllByOrderStatusOrderByCreateTimeDesc(NEW, PageRequest.of(0, 100));
        List<Long> ids = ids(page);

        page.forEach(o -> assertEquals(NEW, o.getOrderStatus().intValue()));
        assertTrue(ids.contains(anaNew.getOrderId()));
        assertTrue(ids.contains(luisNew.getOrderId()));
        assertFalse(ids.contains(anaFinished.getOrderId()));
    }

    @Test
    @DisplayName("Los pedidos de un comprador salen ordenados por estado (NEW antes que FINISHED)")
    void findAllByBuyerEmail_ordenaPorEstado() {
        Page<OrderMain> page = orderRepository
                .findAllByBuyerEmailOrderByOrderStatusAscCreateTimeDesc(ANA_EMAIL, PageRequest.of(0, 10));

        assertEquals(List.of(NEW, FINISHED), statuses(page));
    }

    @Test
    @DisplayName("Un comprador sin pedidos obtiene una página vacía")
    void findAllByBuyerEmail_sinPedidos_paginaVacia() {
        Page<OrderMain> page = orderRepository
                .findAllByBuyerEmailOrderByOrderStatusAscCreateTimeDesc("nadie@ucsp.edu.pe", PageRequest.of(0, 10));

        assertTrue(page.isEmpty());
    }

    @Test
    @DisplayName("findAllByBuyerPhone filtra por teléfono")
    void findAllByBuyerPhone_filtraPorTelefono() {
        Page<OrderMain> page = orderRepository
                .findAllByBuyerPhoneOrderByOrderStatusAscCreateTimeDesc(LUIS_PHONE, PageRequest.of(0, 10));

        assertEquals(List.of(luisNew.getOrderId()), ids(page));
    }

    @Test
    @DisplayName("findAll ordenado devuelve todos los pedidos con el estado en orden ascendente")
    void findAllOrdenado_estadosAscendentes() {
        Page<OrderMain> page = orderRepository
                .findAllByOrderByOrderStatusAscCreateTimeDesc(PageRequest.of(0, 100));
        List<Integer> statuses = statuses(page);

        assertEquals(pedidosPrevios + 3, page.getTotalElements());
        for (int i = 1; i < statuses.size(); i++) {
            assertTrue(statuses.get(i - 1) <= statuses.get(i), "Estados fuera de orden en la posición " + i);
        }
    }

    @Test
    @DisplayName("La paginación respeta el tamaño de página")
    void findAllByBuyerEmail_paginacion() {
        Page<OrderMain> page = orderRepository
                .findAllByBuyerEmailOrderByOrderStatusAscCreateTimeDesc(ANA_EMAIL, PageRequest.of(0, 1));

        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    // ---------------- Helpers ----------------

    private static OrderMain order(String email, String phone, int status, String amount) {
        OrderMain o = new OrderMain();
        o.setBuyerEmail(email);
        o.setBuyerName("Comprador de prueba");
        o.setBuyerPhone(phone);
        o.setBuyerAddress("Arequipa");
        o.setOrderAmount(new BigDecimal(amount));
        o.setOrderStatus(status);
        return o;
    }

    private static List<Long> ids(Page<OrderMain> page) {
        return page.getContent().stream().map(OrderMain::getOrderId).toList();
    }

    private static List<Integer> statuses(Page<OrderMain> page) {
        return page.getContent().stream().map(OrderMain::getOrderStatus).toList();
    }
}
