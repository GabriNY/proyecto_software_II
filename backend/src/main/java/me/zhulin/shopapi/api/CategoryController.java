package me.zhulin.shopapi.api;

import me.zhulin.shopapi.entity.ProductCategory;
import me.zhulin.shopapi.entity.ProductInfo;
import me.zhulin.shopapi.service.CategoryService;
import me.zhulin.shopapi.service.ProductService;
import me.zhulin.shopapi.vo.response.CategoryPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    public CategoryController(CategoryService categoryService,
                              ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping("/category/{type}")
    public CategoryPage showOne(
            @PathVariable("type") Integer categoryType,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "3") Integer size) {

        ProductCategory categoria =
                categoryService.findByCategoryType(categoryType);

        Page<ProductInfo> productos = productService.findAllInCategory(
                categoryType, PageRequest.of(page - 1, size));

        return new CategoryPage(categoria.getCategoryName(), productos);
    }
}