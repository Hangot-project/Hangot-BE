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

import java.util.Collections;
import java.util.List;

import static com.hanyang.api.dataset.domain.QDataset.dataset;
import static com.querydsl.core.types.dsl.Expressions.numberTemplate;


@Repository
@RequiredArgsConstructor
public class DatasetSearchRepository {
    private final JPAQueryFactory queryFactory;
    private static final int MAX_TOTAL_ELEMENT = 50;
    private static final int PAGE_SIZE = 10;

    public Page<Dataset> searchDatasetList(DataSearch dataSearch){
        JPAQuery<Dataset> query = queryFactory.selectFrom(dataset)
                .where(titleLike(dataSearch.getKeyword()),
                        organizationIn(dataSearch.getOrganization()),
                        typeIn(dataSearch.getType()),
                        themeIn(dataSearch.getTheme()));

        switch (dataSearch.getSort().name()) {
            case "스크랩" -> {
                query.orderBy(dataset.scrap.desc());
            }
            case "조회" -> {
                query.orderBy(dataset.view.desc());
            }
            case "인기" ->{
                query.orderBy(dataset.popular.desc());
            }
            default -> {
                query.orderBy(dataset.createdDate.desc());
            }
        }

        List<Dataset> content = query.limit(MAX_TOTAL_ELEMENT).fetch();

        Pageable pageable = PageRequest.of(dataSearch.getPage(), PAGE_SIZE);
        int startIndex = dataSearch.getPage() * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, content.size());


        List<Dataset> pageContent =  startIndex < content.size() ? content.subList(startIndex, endIndex) : Collections.emptyList();

        return new PageImpl<>(pageContent, pageable, content.size());

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

    private BooleanExpression themeIn(List<String> themeList){
        return themeList != null ? dataset.datasetThemeList.any().theme.in(themeList) : null;
    }

    private BooleanExpression typeIn(List<String> typeList) {
        return typeList != null ? dataset.type.in(typeList) : null;
    }
}
