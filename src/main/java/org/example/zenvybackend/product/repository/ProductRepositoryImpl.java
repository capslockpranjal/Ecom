package org.example.zenvybackend.product.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.zenvybackend.category.entity.Category;
import org.example.zenvybackend.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Product> findFilteredCustomerVisibleProducts(
            List<Category> categories,
            String brand,
            Double minPrice,
            Double maxPrice,
            Map<String, String> metadataFilters,
            Pageable pageable
    ) {
        StringBuilder where = new StringBuilder("""
                p.category IN :categories
                AND p.isDeleted = false
                AND p.isActive = true
                AND EXISTS (
                    SELECT 1 FROM ProductVariation pv
                    WHERE pv.product = p
                    AND pv.isDeleted = false
                    AND pv.isActive = true
                """);

        if (brand != null && !brand.isBlank()) {
            where.append(" AND LOWER(p.brand) = LOWER(:brand)");
        }
        if (minPrice != null) {
            where.append(" AND pv.price >= :minPrice");
        }
        if (maxPrice != null) {
            where.append(" AND pv.price <= :maxPrice");
        }
        if (metadataFilters != null) {
            int index = 0;
            for (Map.Entry<String, String> entry : metadataFilters.entrySet()) {
                if (entry.getKey() == null || entry.getKey().isBlank()
                        || entry.getValue() == null || entry.getValue().isBlank()) {
                    continue;
                }
                where.append(" AND pv.metadata LIKE :metadataPattern").append(index);
                index++;
            }
        }
        where.append(")");

        String selectJpql = "SELECT DISTINCT p FROM Product p WHERE " + where;
        String countJpql = "SELECT COUNT(DISTINCT p.id) FROM Product p WHERE " + where;

        TypedQuery<Product> query = entityManager.createQuery(selectJpql, Product.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);

        query.setParameter("categories", categories);
        countQuery.setParameter("categories", categories);

        if (brand != null && !brand.isBlank()) {
            query.setParameter("brand", brand.trim());
            countQuery.setParameter("brand", brand.trim());
        }
        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
            countQuery.setParameter("minPrice", minPrice);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
            countQuery.setParameter("maxPrice", maxPrice);
        }
        if (metadataFilters != null) {
            int index = 0;
            for (Map.Entry<String, String> entry : metadataFilters.entrySet()) {
                if (entry.getKey() == null || entry.getKey().isBlank()
                        || entry.getValue() == null || entry.getValue().isBlank()) {
                    continue;
                }
                String pattern = "%\"" + entry.getKey().trim() + "\":\"" + entry.getValue().trim() + "\"%";
                String paramName = "metadataPattern" + index;
                query.setParameter(paramName, pattern);
                countQuery.setParameter(paramName, pattern);
                index++;
            }
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Product> content = query.getResultList();
        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
