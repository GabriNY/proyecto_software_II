package me.zhulin.shopapi.api;

import me.zhulin.shopapi.entity.ProductCategory;
import me.zhulin.shopapi.entity.ProductInfo;
import me.zhulin.shopapi.enums.ResultEnum;
import me.zhulin.shopapi.exception.MyException;
import me.zhulin.shopapi.service.CategoryService;
import me.zhulin.shopapi.service.ProductService;
import me.zhulin.shopapi.vo.response.CategoryPage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductService productService;

    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        categoryController = new CategoryController(
                categoryService, productService);
    }

    @Test
    void showOneDevuelveNombreYProductosDeLaCategoria() {
        ProductCategory books = new ProductCategory();
        books.setCategoryType(0);
        books.setCategoryName("Books");

        Page<ProductInfo> pagina =
                new PageImpl<>(List.of(new ProductInfo()));

        when(categoryService.findByCategoryType(0))
                .thenReturn(books);

        when(productService.findAllInCategory(
                0, PageRequest.of(0, 3)))
                .thenReturn(pagina);

        CategoryPage resultado =
                categoryController.showOne(0, 1, 3);

        assertEquals("Books", resultado.getCategory());
        assertSame(pagina, resultado.getPage());
    }

    @Test
    void showOneCategoriaInexistenteLanzaExcepcion() {
        when(categoryService.findByCategoryType(9))
                .thenThrow(new MyException(
                        ResultEnum.CATEGORY_NOT_FOUND));

        assertThrows(MyException.class,
                () -> categoryController.showOne(9, 1, 3));
    }
}