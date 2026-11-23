package me.zhulin.shopapi.api;


import me.zhulin.shopapi.entity.Cart;
import me.zhulin.shopapi.entity.ProductInOrder;
import me.zhulin.shopapi.entity.User;
import me.zhulin.shopapi.exception.MyException;
import me.zhulin.shopapi.form.ItemForm;
import me.zhulin.shopapi.service.CartService;
import me.zhulin.shopapi.service.ProductInOrderService;
import me.zhulin.shopapi.service.ProductService;
import me.zhulin.shopapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;

/**
 * Created By Zhu Lin on 3/11/2018.
 */
@CrossOrigin
@RestController
@RequestMapping("/cart")
public class CartController {
    
    private final CartService cartService;

    private final UserService userService;

    private final ProductService productService;

    private final ProductInOrderService productInOrderService;

    public CartController(
            CartService cartService,
            UserService userService,
            ProductService productService,
            ProductInOrderService productInOrderService) {

        this.cartService = cartService;
        this.userService = userService;
        this.productService = productService;
        this.productInOrderService = productInOrderService;
    }


    
    @PostMapping("")
    public ResponseEntity<Cart> mergeCart(
            @RequestBody Collection<ProductInOrder> productInOrders,
            Principal principal) {

        User user = userService.findOne(principal.getName());

        try {
            cartService.mergeLocalCart(productInOrders, user);

            return ResponseEntity.ok(cartService.getCart(user));

        } catch (MyException e) {

            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("")
    public Cart getCart(Principal principal) {
        User user = userService.findOne(principal.getName());
        return cartService.getCart(user);
    }


    
    @PostMapping("/add")
    public boolean addToCart(
            @RequestBody ItemForm form,
            Principal principal) {

        try {
            var productInfo =
                    productService.findOne(form.getProductId());

            ProductInOrder producto =
                    new ProductInOrder(
                            productInfo,
                            form.getQuantity()
                    );

            ResponseEntity<Cart> respuesta =
                    mergeCart(
                            Collections.singleton(producto),
                            principal
                    );

            return respuesta.getStatusCode().is2xxSuccessful();

        } catch (MyException e) {
            return false;
        }
    }


    @PutMapping("/{itemId}")
    public ProductInOrder modifyItem(@PathVariable("itemId") String itemId, @RequestBody Integer quantity, Principal principal) {
        User user = userService.findOne(principal.getName());
         productInOrderService.update(itemId, quantity, user);
        return productInOrderService.findOne(itemId, user);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable("itemId") String itemId, Principal principal) {
        User user = userService.findOne(principal.getName());
         cartService.delete(itemId, user);
         // flush memory into DB
    }


    @PostMapping("/checkout")
    public ResponseEntity checkout(Principal principal) {
        User user = userService.findOne(principal.getName());// Email as username
        cartService.checkout(user);
        return ResponseEntity.ok(null);
    }


}
