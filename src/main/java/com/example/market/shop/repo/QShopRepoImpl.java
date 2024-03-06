package com.example.market.shop.repo;

import com.example.market.shop.dto.ShopSearchParams;
import com.example.market.shop.entity.QShop;
import com.example.market.shop.entity.QShopItem;
import com.example.market.shop.entity.QShopItemOrder;
import com.example.market.shop.entity.Shop;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class QShopRepoImpl implements QShopRepo {
    private final JPAQueryFactory queryFactory;
    private final QShop shop = new QShop("s");
    private final QShopItem item = new QShopItem("i");
    private final QShopItemOrder order = new QShopItemOrder("o");


    @Override
    public Page<Shop> searchShops(ShopSearchParams params, Pageable pageable) {
        List<Shop> content = queryFactory
                .selectFrom(shop)
                .join(shop.items, item)
                .leftJoin(item.orders, order)
                .where(
                        nameContains(params.getName()),
                        categoryEquals(params.getCategory())
                )
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> countQuery = queryFactory
                .select(shop.count())
                .from(shop)
                .where();
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameContains(String name) {
        return name == null ? null : shop.name.containsIgnoreCase(name);
    }

    private BooleanExpression categoryEquals(Shop.Category category) {
        return category == null ? null : shop.category.eq(category);
    }
}
