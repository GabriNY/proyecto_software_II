
package me.zhulin.shopapi.service.impl;

import me.zhulin.shopapi.entity.ProductInOrder;
import me.zhulin.shopapi.entity.User;
import me.zhulin.shopapi.repository.ProductInOrderRepository;
import me.zhulin.shopapi.service.ProductInOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductInOrderServiceImpl
        implements ProductInOrderService {

    
    private final ProductInOrderRepository productInOrderRepository;

    public ProductInOrderServiceImpl(
            ProductInOrderRepository productInOrderRepository) {

        this.productInOrderRepository = productInOrderRepository;
    }


    @Override
    @Transactional
    public void update(
            String itemId,
            Integer quantity,
            User user) {

        user.getCart()
                .buscarProducto(itemId)
                .ifPresent(productInOrder -> {

                    productInOrder.setCount(quantity);

                    productInOrderRepository.save(productInOrder);
                });
    }

    @Override
    public ProductInOrder findOne(
            String itemId,
            User user) {

        return user.getCart()
                .buscarProducto(itemId)
                .orElse(null);
    }
}
