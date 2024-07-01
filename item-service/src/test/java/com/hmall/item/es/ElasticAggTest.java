package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
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
public class ElasticAggTest {
    private RestHighLevelClient client;
    @Autowired
    private IItemService itemService;

    @BeforeEach
    void setUp() {
        client=new RestHighLevelClient(RestClient.builder(
                HttpHost.create("192.168.44.128:9200")
        ));

    }
    @Test
    public void test(){
        System.out.println("client="+client);
    }

    @Test
    public void test2() throws IOException {
        SearchRequest request=new SearchRequest("items");
        request.source().aggregation(AggregationBuilders
                .terms("brand_agg")
                .size(10)
                .field("brand"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        parseBucket2Result(response);


    }

    private void parseBucket2Result(SearchResponse response) {
        Aggregations aggregations = response.getAggregations();
        Terms brandAgg = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> buckets = brandAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String brandName = bucket.getKeyAsString();
            System.out.println("brand:"+brandName);
            System.out.println("count:"+bucket.getDocCount());
        }
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
