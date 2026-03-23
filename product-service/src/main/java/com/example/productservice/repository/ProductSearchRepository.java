package com.example.productservice.repository;

import com.example.productservice.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    List<ProductDocument> findByNameContainingOrDescriptionContaining(String nameKeyword, String descKeyword);
}
