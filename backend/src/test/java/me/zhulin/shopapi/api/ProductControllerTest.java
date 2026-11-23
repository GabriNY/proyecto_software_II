package me.zhulin.shopapi.api;

import me.zhulin.shopapi.entity.ProductInfo;
import me.zhulin.shopapi.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    private ProductController productController;

    @BeforeEach
    void setUp() {
        productController = new ProductController(productService);
    }

    private ProductInfo producto(String id) {
        ProductInfo p = new ProductInfo();
        p.setProductId(id);
        return p;
    }

    @Test
    void showOneDevuelveElProducto() {
        ProductInfo producto = producto("B0001");

        when(productService.findOne("B0001"))
                .thenReturn(producto);

        assertSame(producto, productController.showOne("B0001"));
    }

    @Test
    void createConCodigoRepetidoDevuelve400YNoGuarda() {
        ProductInfo nuevo = producto("B0001");
        BindingResult errores =
                new BeanPropertyBindingResult(nuevo, "product");

        when(productService.findOne("B0001"))
                .thenReturn(producto("B0001"));

        ResponseEntity<?> respuesta =
                productController.create(nuevo, errores);

        assertEquals(HttpStatus.BAD_REQUEST,
                respuesta.getStatusCode());

        verify(productService, never()).save(any());
    }

    @Test
    void createProductoNuevoDevuelve200YGuarda() {
        ProductInfo nuevo = producto("X0001");
        BindingResult errores =
                new BeanPropertyBindingResult(nuevo, "product");

        when(productService.findOne("X0001")).thenReturn(null);
        when(productService.save(nuevo)).thenReturn(nuevo);

        ResponseEntity<?> respuesta =
                productController.create(nuevo, errores);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        verify(productService).save(nuevo);
    }

    @Test
    void editConIdDistintoDevuelve400() {
        ProductInfo cuerpo = producto("B0002");
        BindingResult errores =
                new BeanPropertyBindingResult(cuerpo, "product");

        ResponseEntity<?> respuesta =
                productController.edit("B0001", cuerpo, errores);

        assertEquals(HttpStatus.BAD_REQUEST,
                respuesta.getStatusCode());

        verify(productService, never()).update(any());
    }
}