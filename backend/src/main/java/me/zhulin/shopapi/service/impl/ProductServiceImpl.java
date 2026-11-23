package me.zhulin.shopapi.service.impl;

import me.zhulin.shopapi.entity.ProductInfo;
import me.zhulin.shopapi.enums.ProductStatusEnum;
import me.zhulin.shopapi.enums.ResultEnum;
import me.zhulin.shopapi.exception.MyException;
import me.zhulin.shopapi.repository.ProductInfoRepository;
import me.zhulin.shopapi.service.CategoryService;
import me.zhulin.shopapi.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private static final int ESTADO_MAXIMO = ProductStatusEnum.DOWN.getCode();

    private final ProductInfoRepository productInfoRepository;
    private final CategoryService categoryService;

    public ProductServiceImpl(ProductInfoRepository productInfoRepository,
                              CategoryService categoryService) {
        this.productInfoRepository = productInfoRepository;
        this.categoryService = categoryService;
    }

    @Override
    public ProductInfo findOne(String productId) {
        return productInfoRepository.findByProductId(productId);
    }

    @Override
    public Page<ProductInfo> findUpAll(Pageable pageable) {
        return productInfoRepository
                .findAllByProductStatusOrderByProductIdAsc(
                        ProductStatusEnum.UP.getCode(), pageable);
    }

    @Override
    public Page<ProductInfo> findAll(Pageable pageable) {
        return productInfoRepository.findAllByOrderByProductId(pageable);
    }

    @Override
    public Page<ProductInfo> findAllInCategory(Integer categoryType,
                                               Pageable pageable) {
        return productInfoRepository
                .findAllByCategoryTypeOrderByProductIdAsc(
                        categoryType, pageable);
    }

    @Override
    @Transactional
    public void increaseStock(String productId, int amount) {
        ProductInfo productInfo = buscarExistente(productId);
        productInfo.setProductStock(productInfo.getProductStock() + amount);
        productInfoRepository.save(productInfo);
    }

    @Override
    @Transactional
    public void decreaseStock(String productId, int amount) {
        ProductInfo productInfo = buscarExistente(productId);

        int nuevoStock = productInfo.getProductStock() - amount;

        if (nuevoStock <= 0) {
            throw new MyException(ResultEnum.PRODUCT_NOT_ENOUGH);
        }

        productInfo.setProductStock(nuevoStock);
        productInfoRepository.save(productInfo);
    }

    @Override
    @Transactional
    public ProductInfo offSale(String productId) {
        return cambiarEstado(productId, ProductStatusEnum.DOWN);
    }

    @Override
    @Transactional
    public ProductInfo onSale(String productId) {
        return cambiarEstado(productId, ProductStatusEnum.UP);
    }

    @Override
    public ProductInfo update(ProductInfo productInfo) {
        categoryService.findByCategoryType(productInfo.getCategoryType());

        if (productInfo.getProductStatus() > ESTADO_MAXIMO) {
            throw new MyException(ResultEnum.PRODUCT_STATUS_ERROR);
        }

        return productInfoRepository.save(productInfo);
    }

    @Override
    public ProductInfo save(ProductInfo productInfo) {
        return update(productInfo);
    }

    @Override
    public void delete(String productId) {
        productInfoRepository.delete(buscarExistente(productId));
    }

    private ProductInfo buscarExistente(String productId) {
        ProductInfo productInfo = findOne(productId);

        if (productInfo == null) {
            throw new MyException(ResultEnum.PRODUCT_NOT_EXIST);
        }

        return productInfo;
    }

    private ProductInfo cambiarEstado(String productId,
                                       ProductStatusEnum nuevoEstado) {
        ProductInfo productInfo = buscarExistente(productId);

        if (nuevoEstado.getCode().equals(productInfo.getProductStatus())) {
            throw new MyException(ResultEnum.PRODUCT_STATUS_ERROR);
        }

        productInfo.setProductStatus(nuevoEstado.getCode());

        return productInfoRepository.save(productInfo);
    }
}