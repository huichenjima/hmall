package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

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
public class ElasticBatchTest {
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
        BulkRequest bulkRequest = new BulkRequest();
        Item item = itemService.getById(546872L);
        Item item2 = itemService.getById(317580L);

        IndexRequest request=new IndexRequest("items").id(item.getId().toString());
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        String jsonStr = JSONUtil.toJsonStr(itemDoc);
        request.source(jsonStr,XContentType.JSON);

        IndexRequest request2=new IndexRequest("items").id(item2.getId().toString());
        ItemDoc itemDoc2 = BeanUtil.copyProperties(item2, ItemDoc.class);
        String jsonStr2 = JSONUtil.toJsonStr(itemDoc2);
        request2.source(jsonStr2,XContentType.JSON);

        bulkRequest.add(request, request2);
        BulkResponse bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println("bulk = " + bulk);

    }
    @Test
    public void test3() throws IOException {
        int pageNo=1,pageSize=500;
        while (true)
        {
            BulkRequest bulkRequest = new BulkRequest();
            Page<Item> page = itemService.lambdaQuery().eq(Item::getStatus, 1).page(new Page<>(pageNo, pageSize));
            List<Item> records = page.getRecords();
            if (records==null|| CollUtil.isEmpty(records))
                return;
            records.forEach(item -> {

                ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
                IndexRequest request = new IndexRequest("items").id(itemDoc.getId()).source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
                bulkRequest.add(request);

            });

            BulkResponse bulk1 = client.bulk(bulkRequest, RequestOptions.DEFAULT);
//            System.out.println("bulk1 = " + bulk1);
            pageNo++;


        }





    }


    @AfterEach
    void afterAll() throws IOException {
     if (client!=null)
         client.close();
        
    }


}
