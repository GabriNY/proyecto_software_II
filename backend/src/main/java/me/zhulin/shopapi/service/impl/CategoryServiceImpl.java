package me.zhulin.shopapi.service.impl;

import me.zhulin.shopapi.entity.ProductCategory;
import me.zhulin.shopapi.enums.ResultEnum;
import me.zhulin.shopapi.exception.MyException;
import me.zhulin.shopapi.repository.ProductCategoryRepository;
import me.zhulin.shopapi.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    public CategoryServiceImpl(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }

    @Override
    public List<ProductCategory> findAll() {
        return productCategoryRepository.findAllByOrderByCategoryType();
    }

    @Override
    public ProductCategory findByCategoryType(Integer categoryType) {
        ProductCategory categoria =
                productCategoryRepository.findByCategoryType(categoryType);

        if (categoria == null) {
            throw new MyException(ResultEnum.CATEGORY_NOT_FOUND);
        }

        return categoria;
    }

    @Override
    public List<ProductCategory> findByCategoryTypeIn(List<Integer> categoryTypeList) {
        return productCategoryRepository
                .findByCategoryTypeInOrderByCategoryTypeAsc(categoryTypeList);
    }

    @Override
    @Transactional
    public ProductCategory save(ProductCategory productCategory) {
        return productCategoryRepository.save(productCategory);
    }
}