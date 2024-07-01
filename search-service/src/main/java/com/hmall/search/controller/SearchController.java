package com.hmall.search.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.query.ItemPageQuery;
import com.hmall.common.domain.PageDTO;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.po.ItemDoc;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


//@RequiredArgsConstructor
@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
public class SearchController {
//    private final ItemClient itemClient;

//    private final IItemService itemService;

//    @ApiOperation("搜索商品")
//    @GetMapping("/list")
//    public PageDTO<ItemDTO> search(ItemPageQuery query) {
//        // 分页查询
////        Page<Item> result = itemService.lambdaQuery()
////                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
////                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
////                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
////                .eq(Item::getStatus, 1)
////                .between(query.getMaxPrice() != null, Item::getPrice, query.getMinPrice(), query.getMaxPrice())
////                .page(query.toMpPage("update_time", false));
////        // 封装并返回
////        return PageDTO.of(result, ItemDTO.class);
//        return itemClient.search(query);
//
//    }

    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<ItemDoc> search(ItemPageQuery query) throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("192.168.44.128:9200")
        ));
        SearchRequest request = new SearchRequest("items");
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // Create a function score query
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] functions = {
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        QueryBuilders.termQuery("isAD", "true"), // Filter condition
                        ScoreFunctionBuilders.weightFactorFunction(1000) // Weight
                )
        };

        if (StrUtil.isNotBlank(query.getKey())){


            boolQueryBuilder.must(QueryBuilders.matchQuery("name",query.getKey()));
            boolQueryBuilder.must(QueryBuilders.functionScoreQuery(QueryBuilders.matchQuery("name",query.getKey()),functions));

        }
        else
        {
            boolQueryBuilder.must(QueryBuilders.functionScoreQuery(functions));

        }


        if (StrUtil.isNotBlank(query.getCategory()))
            boolQueryBuilder.filter(QueryBuilders.termQuery("category",query.getCategory()));
        if (StrUtil.isNotBlank(query.getBrand()))
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand",query.getBrand()));
        if (query.getMinPrice()!=null)
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gt(query.getMinPrice()));
        if (query.getMaxPrice()!=null&&query.getMaxPrice()>0)
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lt(query.getMaxPrice()));

//        int pageSize= query.getPageSize(),startNum=(query.getPageNo()-1)*pageSize;
        request.source().query(boolQueryBuilder).from(query.from()).size(query.getPageSize());
        // sort
        List<OrderItem> orders = query.toMpPage("updateTime", false).getOrders();
        for (OrderItem orderItem : orders) {
            request.source().sort("_score",SortOrder.DESC).sort(orderItem.getColumn(), orderItem.isAsc() ? SortOrder.ASC : SortOrder.DESC);
        }
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        client.close();
        return parseResponseResult(response,query.getPageNo().longValue());



    }

    @ApiOperation("聚合商品")
    @PostMapping("/filters")
    public Map  aggItem(ItemPageQuery query) throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("192.168.44.128:9200")
        ));
        SearchRequest searchRequest = new SearchRequest("items");
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (StrUtil.isNotBlank(query.getKey())) {
            queryBuilder.must(QueryBuilders.matchQuery("name", query.getKey()));
        }
        if (StrUtil.isNotBlank(query.getBrand())) {
            queryBuilder.filter(QueryBuilders.termQuery("brand", query.getBrand()));
        }
        if (StrUtil.isNotBlank(query.getCategory())) {
            queryBuilder.filter(QueryBuilders.matchQuery("category", query.getCategory()));
        }
        if (query.getMaxPrice() != null) {
            queryBuilder.filter(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()).lte(query.getMaxPrice()));
        }

        String categoryAgg = "category_agg";
        String brandAgg = "brand_agg";
        searchRequest.source().query(queryBuilder).aggregation(
                AggregationBuilders.terms(categoryAgg).field("category"))
                .aggregation(AggregationBuilders.terms(brandAgg).field("brand"));
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        HashMap<String, List<String>> resultMap = new HashMap<>();
        Terms terms = response.getAggregations().get(categoryAgg);
        if (terms != null) {
            resultMap.put("category",terms.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList()));
        }
        terms = response.getAggregations().get(brandAgg);
        if (terms != null) {
            resultMap.put("brand",terms.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList()));
        }

        // 封装并返回
        return resultMap;

    }

    private PageDTO<ItemDoc> parseResponseResult(SearchResponse response,Long pageNum) {
        PageDTO<ItemDoc> itemDocPageDTO = new PageDTO<>();
        long value = response.getHits().getTotalHits().value;
        itemDocPageDTO.setTotal(value);
        itemDocPageDTO.setPages(pageNum);
        List<ItemDoc> itemDocList=new ArrayList<>();

        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            ItemDoc itemDoc = JSONUtil.toBean(sourceAsString, ItemDoc.class);
//            System.out.println("itemDoc = " + itemDoc);
            itemDocList.add(itemDoc);
        }
        itemDocPageDTO.setList(itemDocList);
        return itemDocPageDTO;
    }
}
