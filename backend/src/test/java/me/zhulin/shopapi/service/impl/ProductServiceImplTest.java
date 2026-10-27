
package me.zhulin.shopapi.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import me.zhulin.shopapi.entity.ProductInfo;
import me.zhulin.shopapi.enums.ProductStatusEnum;
import me.zhulin.shopapi.exception.MyException;
import me.zhulin.shopapi.repository.ProductInfoRepository;
import me.zhulin.shopapi.service.CategoryService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductServiceImplTest {
    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductInfoRepository productInfoRepository;

    @Mock
    private CategoryService categoryService;

    private ProductInfo productInfo;

    @BeforeEach
    public void setUp() {
        productInfo = new ProductInfo();
        productInfo.setProductId("1");
        productInfo.setProductStock(10);
        productInfo.setProductStatus(1);
    }

    @Test
    public void increaseStockTest() {
        when(productInfoRepository.findByProductId("1")).thenReturn(productInfo);
        productService.increaseStock("1", 10);
        assertEquals(20, productInfo.getProductStock().intValue());
        Mockito.verify(productInfoRepository, Mockito.times(1)).save(productInfo);
    }

    @Test
    public void increaseStockExceptionTest() {
        assertThrows(MyException.class, () -> productService.increaseStock("1", 10));
        Mockito.verify(productInfoRepository, Mockito.times(1)).findByProductId("1");
        Mockito.verify(productInfoRepository, Mockito.never()).save(Mockito.any(ProductInfo.class));
    }

    @Test
    public void decreaseStockTest() {
        when(productInfoRepository.findByProductId("1")).thenReturn(productInfo);
        productService.decreaseStock("1", 9);
        assertEquals(1, productInfo.getProductStock().intValue());
        Mockito.verify(productInfoRepository, Mockito.times(1)).save(productInfo);
    }

    @Test
    public void decreaseStockValueLesserEqualTest() {
        when(productInfoRepository.findByProductId("1")).thenReturn(productInfo);
        assertThrows(MyException.class, () -> productService.decreaseStock("1", 10));
        assertEquals(10, productInfo.getProductStock().intValue());
        Mockito.verify(productInfoRepository, Mockito.never()).save(Mockito.any(ProductInfo.class));
    }

    @Test
    public void decreaseStockExceptionTest() {
        assertThrows(MyException.class, () -> productService.decreaseStock("1", 10));
        Mockito.verify(productInfoRepository, Mockito.times(1)).findByProductId("1");
        Mockito.verify(productInfoRepository, Mockito.never()).save(Mockito.any(ProductInfo.class));
    }

    @Test
    public void offSaleTest() {
        productInfo.setProductStatus(ProductStatusEnum.UP.getCode());
        ProductInfo productoGuardado = new ProductInfo();
        productoGuardado.setProductId("1");
        productoGuardado.setProductStatus(ProductStatusEnum.DOWN.getCode());
        when(productInfoRepository.findByProductId("1")).thenReturn(productInfo);
        when(productInfoRepository.save(productInfo)).thenReturn(productoGuardado);
        ProductInfo resultado = productService.offSale("1");
        assertEquals(ProductStatusEnum.DOWN.getCode(), productInfo.getProductStatus().intValue());
        assertSame(productoGuardado, resultado);
        Mockito.verify(productInfoRepository, Mockito.times(1)).save(productInfo);
    }

    @Test
    public void offSaleStatusDownTest() {
        productInfo.setProductStatus(ProductStatusEnum.DOWN.getCode());
        when(productInfoRepository.findByProductId("1")).thenReturn(productInfo);
        assertThrows(MyException.class, () -> productService.offSale("1"));
        assertEquals(ProductStatusEnum.DOWN.getCode(), productInfo.getProductStatus().intValue());
        Mockito.verify(productInfoRepository, Mockito.times(1)).findByProductId("1");
        Mockito.verify(productInfoRepository, Mockito.never()).save(Mockito.any(ProductInfo.class));
    }

    @Test
    public void offSaleProductNullTest() {
        assertThrows(MyException.class, () -> productService.offSale("1"));
        Mockito.verify(productInfoRepository, Mockito.times(1)).findByProductId("1");
        Mockito.verify(productInfoRepository, Mockito.never()).save(Mockito.any(ProductInfo.class));
    }

    @Test
    public void onSaleTest() {
        productInfo.setProductStatus(ProductStatusEnum.DOWN.getCode());
        ProductInfo productoGuardado = new ProductInfo();
        productoGuardado.setProductId("1");
        productoGuardado.setProductStatus(ProductStatusEnum.UP.getCode());
        when(productInfoRepository.findByProductId("1")).thenReturn(productInfo);
        when(productInfoRepository.save(productInfo)).thenReturn(productoGuardado);
        ProductInfo resultado = productService.onSale("1");
        assertEquals(ProductStatusEnum.UP.getCode(), productInfo.getProductStatus().intValue());
        assertSame(productoGuardado, resultado);
        Mockito.verify(productInfoRepository, Mockito.times(1)).save(productInfo);
    }

    @Test
    public void onSaleStatusUpTest() {
        productInfo.setProductStatus(ProductStatusEnum.UP.getCode());
        when(productInfoRepository.findByProductId("1")).thenReturn(productInfo);
        assertThrows(MyException.class, () -> productService.onSale("1"));
        assertEquals(ProductStatusEnum.UP.getCode(), productInfo.getProductStatus().intValue());
        Mockito.verify(productInfoRepository, Mockito.times(1)).findByProductId("1");
        Mockito.verify(productInfoRepository, Mockito.never()).save(Mockito.any(ProductInfo.class));
    }

    @Test
    public void onSaleProductNullTest() {
        assertThrows(MyException.class, () -> productService.onSale("1"));
        Mockito.verify(productInfoRepository, Mockito.times(1)).findByProductId("1");
        Mockito.verify(productInfoRepository, Mockito.never()).save(Mockito.any(ProductInfo.class));
    }

    @Test
    public void updateTest() {
        productInfo.setCategoryType(2);
        productInfo.setProductStatus(0);
        ProductInfo productoGuardado = new ProductInfo();
        productoGuardado.setProductId("1");
        when(productInfoRepository.save(productInfo)).thenReturn(productoGuardado);
        ProductInfo resultado = productService.update(productInfo);
        assertSame(productoGuardado, resultado);
        Mockito.verify(categoryService, Mockito.times(1)).findByCategoryType(2);
        Mockito.verify(productInfoRepository, Mockito.times(1)).save(productInfo);
    }

    @Test
    public void updateProductStatusBiggerThenOneTest() {
        productInfo.setCategoryType(2);
        productInfo.setProductStatus(2);
        assertThrows(MyException.class, () -> productService.update(productInfo));
        Mockito.verify(categoryService, Mockito.times(1)).findByCategoryType(2);
        Mockito.verify(productInfoRepository, Mockito.never()).save(Mockito.any(ProductInfo.class));
    }

    @Test
    public void deleteTest() {
        when(productInfoRepository.findByProductId("1")).thenReturn(productInfo);
        productService.delete("1");
        Mockito.verify(productInfoRepository, Mockito.times(1)).delete(productInfo);
    }

    @Test
    public void deleteProductNullTest() {
        assertThrows(MyException.class, () -> productService.delete("1"));
        Mockito.verify(productInfoRepository, Mockito.times(1)).findByProductId("1");
        Mockito.verify(productInfoRepository, Mockito.never()).delete(Mockito.any(ProductInfo.class));
    }

    @Test
    public void findOneProductoExistenteTest() {
        when(productInfoRepository.findByProductId("1")).thenReturn(productInfo);
        ProductInfo resultado = productService.findOne("1");
        assertSame(productInfo, resultado);
        Mockito.verify(productInfoRepository, Mockito.times(1)).findByProductId("1");
    }

    @Test
    public void findOneProductoInexistenteTest() {
        String idInexistente = "LAB05-NO-EXISTE";
        ProductInfo resultado = productService.findOne(idInexistente);
        assertNull(resultado);
        Mockito.verify(productInfoRepository, Mockito.times(1)).findByProductId(idInexistente);
    }

    @Test
    public void findUpAllTest() {
        productInfo.setProductStatus(ProductStatusEnum.UP.getCode());
        PageRequest pageable = PageRequest.of(0, 5);
        Page<ProductInfo> paginaEsperada = new PageImpl<>(List.of(productInfo), pageable, 1);
        when(productInfoRepository.findAllByProductStatusOrderByProductIdAsc(
                ProductStatusEnum.UP.getCode(), pageable)).thenReturn(paginaEsperada);
        Page<ProductInfo> resultado = productService.findUpAll(pageable);
        assertSame(paginaEsperada, resultado);
        assertEquals(1, resultado.getTotalElements());
        Mockito.verify(productInfoRepository, Mockito.times(1))
                .findAllByProductStatusOrderByProductIdAsc(ProductStatusEnum.UP.getCode(), pageable);
    }

    @Test
    public void findAllTest() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<ProductInfo> paginaEsperada = new PageImpl<>(List.of(productInfo), pageable, 1);
        when(productInfoRepository.findAllByOrderByProductId(pageable)).thenReturn(paginaEsperada);
        Page<ProductInfo> resultado = productService.findAll(pageable);
        assertSame(paginaEsperada, resultado);
        assertEquals(1, resultado.getTotalElements());
        Mockito.verify(productInfoRepository, Mockito.times(1)).findAllByOrderByProductId(pageable);
    }

    @Test
    public void findAllInCategoryTest() {
        Integer categoria = 2;
        PageRequest pageable = PageRequest.of(0, 5);
        Page<ProductInfo> paginaEsperada = new PageImpl<>(List.of(productInfo), pageable, 1);
        when(productInfoRepository.findAllByCategoryTypeOrderByProductIdAsc(
                categoria, pageable)).thenReturn(paginaEsperada);
        Page<ProductInfo> resultado = productService.findAllInCategory(categoria, pageable);
        assertSame(paginaEsperada, resultado);
        assertEquals(1, resultado.getTotalElements());
        Mockito.verify(productInfoRepository, Mockito.times(1))
                .findAllByCategoryTypeOrderByProductIdAsc(categoria, pageable);
    }

    @Test
    public void saveProductoValidoTest() {
        productInfo.setCategoryType(2);
        productInfo.setProductStatus(0);
        ProductInfo productoGuardado = new ProductInfo();
        productoGuardado.setProductId("1");
        when(productInfoRepository.save(productInfo)).thenReturn(productoGuardado);
        ProductInfo resultado = productService.save(productInfo);
        assertSame(productoGuardado, resultado);
        Mockito.verify(categoryService, Mockito.times(1)).findByCategoryType(2);
        Mockito.verify(productInfoRepository, Mockito.times(1)).save(productInfo);
    }
}