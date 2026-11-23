package me.zhulin.shopapi.service.impl;

import me.zhulin.shopapi.entity.OrderMain;
import me.zhulin.shopapi.entity.ProductInOrder;
import me.zhulin.shopapi.enums.OrderStatusEnum;
import me.zhulin.shopapi.enums.ResultEnum;
import me.zhulin.shopapi.exception.MyException;
import me.zhulin.shopapi.repository.OrderRepository;
import me.zhulin.shopapi.repository.ProductInfoRepository;
import me.zhulin.shopapi.service.OrderService;
import me.zhulin.shopapi.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductInfoRepository productInfoRepository;
    private final ProductService productService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductInfoRepository productInfoRepository,
                            ProductService productService) {
        this.orderRepository = orderRepository;
        this.productInfoRepository = productInfoRepository;
        this.productService = productService;
    }

    @Override
    public Page<OrderMain> findAll(Pageable pageable) {
        return orderRepository.findAllByOrderByOrderStatusAscCreateTimeDesc(pageable);
    }

    @Override
    public Page<OrderMain> findByStatus(Integer status, Pageable pageable) {
        return orderRepository.findAllByOrderStatusOrderByCreateTimeDesc(status, pageable);
    }

    @Override
    public Page<OrderMain> findByBuyerEmail(String email, Pageable pageable) {
        return orderRepository.findAllByBuyerEmailOrderByOrderStatusAscCreateTimeDesc(email, pageable);
    }

    @Override
    public Page<OrderMain> findByBuyerPhone(String phone, Pageable pageable) {
        return orderRepository.findAllByBuyerPhoneOrderByOrderStatusAscCreateTimeDesc(phone, pageable);
    }

    @Override
    public OrderMain findOne(Long orderId) {
        OrderMain orderMain = orderRepository.findByOrderId(orderId);
        if (orderMain == null) {
            throw new MyException(ResultEnum.ORDER_NOT_FOUND);
        }
        return orderMain;
    }

    @Override
    @Transactional
    public OrderMain finish(Long orderId) {
        OrderMain orderMain = buscarPedidoNuevo(orderId);
        orderMain.setOrderStatus(OrderStatusEnum.FINISHED.getCode());
        orderRepository.save(orderMain);
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public OrderMain cancel(Long orderId) {
        OrderMain orderMain = buscarPedidoNuevo(orderId);
        orderMain.setOrderStatus(OrderStatusEnum.CANCELED.getCode());
        orderRepository.save(orderMain);
        restaurarStock(orderMain);
        return orderRepository.findByOrderId(orderId);
    }

    /** Busca el pedido y verifica que siga en estado NEW (solo esos se pueden finalizar o cancelar). */
    private OrderMain buscarPedidoNuevo(Long orderId) {
        OrderMain orderMain = findOne(orderId);
        if (!orderMain.getOrderStatus().equals(OrderStatusEnum.NEW.getCode())) {
            throw new MyException(ResultEnum.ORDER_STATUS_ERROR);
        }
        return orderMain;
    }

    /** Devuelve al stock las unidades de cada producto del pedido que todavía exista. */
    private void restaurarStock(OrderMain orderMain) {
        for (ProductInOrder productInOrder : orderMain.getProducts()) {
            if (productInfoRepository.findByProductId(productInOrder.getProductId()) != null) {
                productService.increaseStock(productInOrder.getProductId(), productInOrder.getCount());
            }
        }
    }
}