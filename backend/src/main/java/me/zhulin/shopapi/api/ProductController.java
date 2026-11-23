package me.zhulin.shopapi.api;

import jakarta.validation.Valid;
import me.zhulin.shopapi.entity.ProductInfo;
import me.zhulin.shopapi.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product")
    public Page<ProductInfo> findAll(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "3") Integer size) {

        return productService.findAll(PageRequest.of(page - 1, size));
    }

    @GetMapping("/product/{productId}")
    public ProductInfo showOne(@PathVariable("productId") String productId) {
        return productService.findOne(productId);
    }

    @PostMapping("/seller/product/new")
    public ResponseEntity<?> create(
            @Valid @RequestBody ProductInfo product,
            BindingResult bindingResult) {

        if (productService.findOne(product.getProductId()) != null) {
            bindingResult.rejectValue("productId", "error.product",
                    "There is already a product with the code provided");
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult);
        }

        return ResponseEntity.ok(productService.save(product));
    }

    @PutMapping("/seller/product/{id}/edit")
    public ResponseEntity<?> edit(
            @PathVariable("id") String productId,
            @Valid @RequestBody ProductInfo product,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult);
        }

        if (!productId.equals(product.getProductId())) {
            return ResponseEntity.badRequest().body("Id Not Matched");
        }

        return ResponseEntity.ok(productService.update(product));
    }

    @DeleteMapping("/seller/product/{id}/delete")
    public ResponseEntity<Void> delete(
            @PathVariable("id") String productId) {

        productService.delete(productId);

        return ResponseEntity.ok().build();
    }
}