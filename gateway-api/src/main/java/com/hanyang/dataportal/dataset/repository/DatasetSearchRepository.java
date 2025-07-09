package com.hanyang.dataportal.dataset.repository;

import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.dataset.domain.QDataset;
import com.hanyang.dataportal.dataset.domain.vo.Organization;
import com.hanyang.dataportal.dataset.domain.vo.Theme;
import com.hanyang.dataportal.dataset.domain.vo.Type;
import com.hanyang.dataportal.dataset.dto.DataSearch;
import com.hanyang.dataportal.resource.domain.QResource;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.JPAExpressions;
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

import static com.hanyang.dataportal.dataset.domain.QDataset.dataset;
import static com.hanyang.dataportal.resource.domain.QResource.resource;
import static com.hanyang.dataportal.user.domain.QScrap.scrap;
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
            case "다운로드" -> {
                query.orderBy(dataset.download.desc());
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

    private BooleanExpression titleLike(String searchWord) {
        if (searchWord == null || searchWord.trim().isEmpty()) {
            return null;
        }

        final String formattedSearchWord = "\"" + searchWord + "\"";
        return numberTemplate(Double.class, "function('match_against', {0}, {1})",
                dataset.title, formattedSearchWord)
                .gt(0);
    }

    private BooleanExpression organizationIn(List<Organization> organizationList) {
        return organizationList !=null  ? dataset.organization.in(organizationList) : null;
    }

    private BooleanExpression themeIn(List<Theme> themeList){
        return themeList != null ? dataset.datasetThemeList.any().theme.in(themeList) : null;
    }

    private BooleanExpression typeIn(List<Type> typeList) {
        if (typeList == null || typeList.isEmpty()) {
            return null;
        }

        return dataset.datasetId.in(
                JPAExpressions
                        .select(resource.dataset.datasetId)
                        .from(resource)
                        .where(resource.type.in(typeList))
        );
    }
}
