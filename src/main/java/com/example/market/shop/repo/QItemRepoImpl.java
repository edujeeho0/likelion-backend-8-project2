package com.example.market.shop.repo;

import com.example.market.shop.dto.ItemSearchParams;
import com.example.market.shop.entity.QShopItem;
import com.example.market.shop.entity.ShopItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class QItemRepoImpl implements QItemRepo {
    private final JPAQueryFactory queryFactory;
    private final QShopItem item = new QShopItem("i");

    @Override
    public Page<ShopItem> searchItems(ItemSearchParams params, Pageable pageable) {
        List<ShopItem> content = queryFactory
                .selectFrom(item)
                .where(
                        nameContains(params.getName()),
                        priceBetween(params.getPriceFloor(), params.getPriceCeil())
                )
                .orderBy(item.price.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> countQuery = queryFactory
                .select(item.count())
                .from(item)
                .where(

                        nameContains(params.getName()),
                        priceBetween(params.getPriceFloor(), params.getPriceCeil())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameContains(String name) {
        return name == null ? null : item.name.containsIgnoreCase(name);
    }

    private BooleanExpression priceBetween(Integer floor, Integer ceil) {
        if (floor == null && ceil == null) return null;
        if (floor == null) return priceCeil(ceil);
        if (ceil == null) return priceFloor(floor);
        return item.price.between(floor, ceil);
    }

    private BooleanExpression priceFloor(Integer value) {
        return value == null ? null : item.price.goe(value);
    }

    private BooleanExpression priceCeil(Integer value) {
        return value == null ? null : item.price.loe(value);
    }
}
