package me.zhulin.shopapi.api;

import me.zhulin.shopapi.entity.OrderMain;
import me.zhulin.shopapi.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class OrderController {

    private static final SimpleGrantedAuthority ROL_CLIENTE = new SimpleGrantedAuthority("ROLE_CUSTOMER");

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/order")
    public Page<OrderMain> orderList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                     @RequestParam(value = "size", defaultValue = "10") Integer size,
                                     Authentication authentication) {
        PageRequest request = PageRequest.of(page - 1, size);
        if (esCliente(authentication)) {
            return orderService.findByBuyerEmail(authentication.getName(), request);
        }
        return orderService.findAll(request);
    }

    @PatchMapping("/order/cancel/{id}")
    public ResponseEntity<OrderMain> cancel(@PathVariable("id") Long orderId, Authentication authentication) {
        OrderMain orderMain = orderService.findOne(orderId);
        if (esCliente(authentication) && !esDueno(authentication, orderMain)) {
            return noAutorizado();
        }
        return ResponseEntity.ok(orderService.cancel(orderId));
    }

    @PatchMapping("/order/finish/{id}")
    public ResponseEntity<OrderMain> finish(@PathVariable("id") Long orderId, Authentication authentication) {
        if (esCliente(authentication)) {
            return noAutorizado();
        }
        return ResponseEntity.ok(orderService.finish(orderId));
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<OrderMain> show(@PathVariable("id") Long orderId, Authentication authentication) {
        OrderMain orderMain = orderService.findOne(orderId);
        if (esCliente(authentication) && !esDueno(authentication, orderMain)) {
            return noAutorizado();
        }
        return ResponseEntity.ok(orderMain);
    }

    private boolean esCliente(Authentication authentication) {
        return authentication.getAuthorities().contains(ROL_CLIENTE);
    }

    private boolean esDueno(Authentication authentication, OrderMain orderMain) {
        return authentication.getName().equals(orderMain.getBuyerEmail());
    }

    private ResponseEntity<OrderMain> noAutorizado() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}