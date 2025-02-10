package com.hanyang.dataportal.dataset.repository;

import com.hanyang.dataportal.dataset.domain.Dataset;
import com.hanyang.dataportal.dataset.domain.vo.Organization;
import com.hanyang.dataportal.dataset.domain.vo.Theme;
import com.hanyang.dataportal.dataset.domain.vo.Type;
import com.hanyang.dataportal.dataset.dto.DataSearch;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.hanyang.dataportal.dataset.domain.QDataset.dataset;
import static com.hanyang.dataportal.resource.domain.QResource.resource;
import static com.hanyang.dataportal.user.domain.QScrap.scrap;


@Repository
@RequiredArgsConstructor
public class DatasetSearchRepository {
    private final JPAQueryFactory queryFactory;

    public Page<Dataset> searchDatasetList(DataSearch dataSearch){
        Pageable pageable = PageRequest.of(dataSearch.getPage(), 10);
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
                query.orderBy(Expressions.numberTemplate(Integer.class, "{0} + 5 * {1}", dataset.view, dataset.scrap).desc());
            }
            default -> {
                query.orderBy(dataset.createdDate.desc());
            }
        }


        List<Dataset> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()).fetch();

        Long count = queryFactory.select(dataset.count())
                .from(dataset)
                .where(titleLike(dataSearch.getKeyword()),
                        organizationIn(dataSearch.getOrganization()),
                        typeIn(dataSearch.getType()),
                        themeIn(dataSearch.getTheme()))
                .fetchOne();
        return new PageImpl<>(content, pageable, count);
    }

    private BooleanExpression titleLike(String keyword) {
        return keyword != null ? dataset.title.contains(keyword) : null;
    }

    private BooleanExpression organizationIn(List<Organization> organizationList) {
        return organizationList !=null  ? dataset.organization.in(organizationList) : null;
    }

    private BooleanExpression themeIn(List<Theme> themeList){
        return themeList != null ? dataset.datasetThemeList.any().theme.in(themeList) : null;
    }

    private BooleanExpression typeIn(List<Type> typeList) {
        return typeList != null ? dataset.resource.type.in(typeList) : null;
    }
}
