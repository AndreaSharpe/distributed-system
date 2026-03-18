package com.example.productservice.mapper;

import com.example.productservice.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
    List<Product> findAll();
    Product findById(@Param("id") Long id);
    int insertProduct(Product product);
    int updateProduct(Product product);
    int deleteById(@Param("id") Long id);
}
