package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;

/**
 * ClassName: ElasticTest
 * Package: com.hmall.item.es
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/21 21:24
 * @Version 1.0
 */
@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticSearchTest {
    private RestHighLevelClient client;
    @Autowired
    private IItemService itemService;

    @BeforeEach
    void setUp() {
        client=new RestHighLevelClient(RestClient.builder(
                HttpHost.create("192.168.44.128:9200")
        ));

    }

    //查询全部
    @Test
    public void test2() throws IOException {
        SearchRequest request=new SearchRequest("items");
        request.source().query(QueryBuilders.matchAllQuery());
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        long total = response.getHits().getTotalHits().value;
        System.out.println("total = " + total);
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {
            String sourceAsString = documentFields.getSourceAsString();
            ItemDoc itemDoc = JSONUtil.toBean(sourceAsString, ItemDoc.class);
            System.out.println(itemDoc);
        }

//        System.out.println("response = " + response);

    }
    @Test
    public void test3() throws IOException {

        SearchRequest request=new SearchRequest("items");
        request.source().query(QueryBuilders.matchQuery("name","脱脂牛奶"))
                .sort("sold", SortOrder.DESC)
                .sort("price",SortOrder.ASC)
                .from(0)
                .size(2);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            ItemDoc itemDoc = JSONUtil.toBean(sourceAsString, ItemDoc.class);
            System.out.println("itemDoc = " + itemDoc);
        }



    }
    @Test
    public void test4() throws IOException {

        SearchRequest request=new SearchRequest("items");
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.matchQuery("name","脱脂牛奶"));
        boolQueryBuilder.filter(QueryBuilders.termQuery("brand","德亚"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lt(30000));
        request.source().query(boolQueryBuilder);
//        request.source().query(QueryBuilders.matchQuery("name","脱脂牛奶"))
//                .query(QueryBuilders.termQuery("brand","德亚"))
//                .query(QueryBuilders.rangeQuery("price").lt("300"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        parseResponseResult(response);


    }

    private void parseResponseResult(SearchResponse response) {
        long value = response.getHits().getTotalHits().value;
        System.out.println("total = " + value);
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            ItemDoc itemDoc = JSONUtil.toBean(sourceAsString, ItemDoc.class);
            System.out.println("itemDoc = " + itemDoc);
        }
    }
    @Test
    public void test5() throws IOException {
        int pageNo=1,pageSize=5;

        SearchRequest request=new SearchRequest("items");
        request.source().query(QueryBuilders.matchAllQuery())
                .sort("sold", SortOrder.DESC)
                .sort("price",SortOrder.ASC)
                .from((pageNo-1)*pageSize)
                .size(pageSize);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        parseResponseResult(response);


    }

    @Test
    public void test6() throws IOException {
        int pageNo=1,pageSize=5;

        SearchRequest request=new SearchRequest("items");
        request.source().query(QueryBuilders.matchQuery("name","脱脂牛奶"))
                .sort("sold", SortOrder.DESC)
                .sort("price",SortOrder.ASC)
                .from((pageNo-1)*pageSize)
                .size(pageSize)
                .highlighter(SearchSourceBuilder.highlight().field("name").preTags("<em>").postTags("</em>"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        parseResponse2Result(response);


    }
    private void parseResponse2Result(SearchResponse response) {
        long value = response.getHits().getTotalHits().value;
        System.out.println("total = " + value);
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {

            String sourceAsString = hit.getSourceAsString();
            ItemDoc itemDoc = JSONUtil.toBean(sourceAsString, ItemDoc.class);
            //处理高亮情况
            Map<String, HighlightField> hfs = hit.getHighlightFields();
            if (hfs!=null&&(!hfs.isEmpty()))
            {
                HighlightField name = hfs.get("name");
                String hfname = name.getFragments()[0].toString();
                itemDoc.setName(hfname);
            }
            System.out.println("itemDoc = " + itemDoc);
        }
    }


    @AfterEach
    void afterAll() throws IOException {
     if (client!=null)
         client.close();
        
    }


}
