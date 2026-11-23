package me.zhulin.shopapi.service.impl;

import java.util.Collection;
import java.util.Optional;
import me.zhulin.shopapi.entity.Cart;
import me.zhulin.shopapi.entity.OrderMain;
import me.zhulin.shopapi.entity.ProductInOrder;
import me.zhulin.shopapi.entity.User;
import me.zhulin.shopapi.enums.ResultEnum;
import me.zhulin.shopapi.exception.MyException;
import me.zhulin.shopapi.repository.CartRepository;
import me.zhulin.shopapi.repository.OrderRepository;
import me.zhulin.shopapi.repository.ProductInOrderRepository;
import me.zhulin.shopapi.service.CartService;
import me.zhulin.shopapi.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created By Zhu Lin on 3/11/2018.
 */
@Service
public class CartServiceImpl implements CartService {
    
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final ProductInOrderRepository productInOrderRepository;
    private final CartRepository cartRepository;
    public CartServiceImpl(
        ProductService productService,
        OrderRepository orderRepository,
        ProductInOrderRepository productInOrderRepository,
        CartRepository cartRepository) {

        this.productService = productService;
        this.orderRepository = orderRepository;
        this.productInOrderRepository = productInOrderRepository;
        this.cartRepository = cartRepository;
    }


    @Override
    public Cart getCart(User user) {
        return user.getCart();
    }

        
    @Override
    @Transactional
    public void mergeLocalCart(
            Collection<ProductInOrder> productInOrders,
            User user) {

        Cart cart = user.getCart();

        productInOrders.forEach(nuevoProducto -> {

            ProductInOrder producto =
                    agregarOActualizarProducto(cart, nuevoProducto);

            productInOrderRepository.save(producto);
        });

        cartRepository.save(cart);
    }
    private ProductInOrder agregarOActualizarProducto(
            Cart cart,
            ProductInOrder nuevoProducto) {

        Optional<ProductInOrder> existente =
                cart.buscarProducto(nuevoProducto.getProductId());

        if (existente.isPresent()) {

            ProductInOrder producto = existente.get();

            producto.setCount(
                    producto.getCount() + nuevoProducto.getCount()
            );

            return producto;
        }

        nuevoProducto.setCart(cart);
        cart.getProducts().add(nuevoProducto);

        return nuevoProducto;
    }
    
    @Override
    @Transactional
    public void delete(String itemId, User user) {

        if (user == null || itemId == null || itemId.isBlank()) {
            throw new MyException(ResultEnum.PARAM_ERROR);
        }

        user.getCart()
                .buscarProducto(itemId)
                .ifPresent(producto -> {

                    producto.setCart(null);

                    productInOrderRepository.deleteById(
                            producto.getId()
                    );
                });
    }


    @Override
    @Transactional
    public void checkout(User user) {

        OrderMain order = new OrderMain(user);

        orderRepository.save(order);

        user.getCart()
                .getProducts()
                .forEach(producto ->
                        moverProductoAlPedido(producto, order));
    }
    private void moverProductoAlPedido(
        ProductInOrder producto,
        OrderMain order) 
        {

        producto.setCart(null);

        producto.setOrderMain(order);

        productService.decreaseStock(
                producto.getProductId(),
                producto.getCount()
        );

        productInOrderRepository.save(producto);
    }
}
