package com.hanyang.api.dataset.repository;

import com.hanyang.api.dataset.domain.Dataset;
import com.hanyang.api.dataset.dto.DataSearch;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.hanyang.api.dataset.domain.QDataset.dataset;
import static com.querydsl.core.types.dsl.Expressions.numberTemplate;


@Repository
@RequiredArgsConstructor
public class DatasetSearchRepository {
    private final JPAQueryFactory queryFactory;
    private static final int PAGE_SIZE = 10;

    public Page<Dataset> searchDatasetList(DataSearch dataSearch){
        JPAQuery<Dataset> query = queryFactory.selectFrom(dataset)
                .where(titleLike(dataSearch.getKeyword()),
                        organizationIn(dataSearch.getOrganization()),
                        typeIn(dataSearch.getType()),
                        tagIn(dataSearch.getTag()));

        switch (dataSearch.getSort().name()) {
            case "스크랩" -> query.orderBy(dataset.scrap.desc());
            case "조회" -> query.orderBy(dataset.view.desc());
            case "인기" -> query.orderBy(dataset.popular.desc());
            default -> query.orderBy(dataset.createdDate.desc());
        }

        Long totalCountResult = queryFactory.select(dataset.count())
                .from(dataset)
                .where(titleLike(dataSearch.getKeyword()),
                        organizationIn(dataSearch.getOrganization()),
                        typeIn(dataSearch.getType()),
                        tagIn(dataSearch.getTag()))
                .fetchOne();
        long totalCount = totalCountResult != null ? totalCountResult : 0L;

        Pageable pageable = PageRequest.of(dataSearch.getPage(), PAGE_SIZE);
        List<Dataset> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression titleLike(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return null;

        String booleanModeKeyword = "+" + keyword + "*";

        return numberTemplate(Double.class,
                "function('match_against', {0}, {1})",
                dataset.title, booleanModeKeyword).gt(0);
    }

    private BooleanExpression organizationIn(List<String> organizationList) {
        return organizationList !=null  ? dataset.organization.in(organizationList) : null;
    }

    private BooleanExpression tagIn(List<String> tagList){
        return tagList != null ? dataset.tagList.any().tag.in(tagList) : null;
    }

    private BooleanExpression typeIn(List<String> typeList) {
        return typeList != null ? dataset.type.in(typeList) : null;
    }
}
