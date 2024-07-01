package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class ElasticDocTest {
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

    //创建文档
    @Test
    public void test2() throws IOException {
        Item item = itemService.getById(317578L);
        IndexRequest request=new IndexRequest("items").id(item.getId().toString());
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        itemDoc.setPrice(10000);
        String jsonStr = JSONUtil.toJsonStr(itemDoc);
        request.source(jsonStr,XContentType.JSON);
        IndexResponse index = client.index(request, RequestOptions.DEFAULT);


    }
    //获取文档
    @Test
    public void test3() throws IOException {
        Item item = itemService.getById(317578L);
        GetRequest request = new GetRequest("items").id(item.getId().toString());
//        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
//        String jsonStr = JSONUtil.toJsonStr(itemDoc);
//        request.source(jsonStr,XContentType.JSON);
        GetResponse documentFields = client.get(request, RequestOptions.DEFAULT);
        System.out.println("documentFields = " + documentFields);
//        Map<String, Object> source = documentFields.getSource();

//        ItemDoc itemDoc = JSONUtil.toBean(JSONUtil.toJsonStr(source), ItemDoc.class);
        String sourceAsString = documentFields.getSourceAsString();
        ItemDoc itemDoc = JSONUtil.toBean(sourceAsString, ItemDoc.class);
        System.out.println("itemDoc = " + itemDoc);

//        System.out.println(documentFields.getSource());


    }

    //删除文档
    @Test
    public void test4() throws IOException {
        Item item = itemService.getById(317578L);
        DeleteRequest request = new DeleteRequest("items",item.getId().toString());
//        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
//        String jsonStr = JSONUtil.toJsonStr(itemDoc);
//        request.source(jsonStr,XContentType.JSON);
        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(delete);


    }

    //更新文档
    @Test
    public void test5() throws IOException {
        Item item = itemService.getById(317578L);
        UpdateRequest request = new UpdateRequest("items",item.getId().toString());
        request.doc(
                "price","20000",
                "sold","100"
        );
        UpdateResponse update = client.update(request, RequestOptions.DEFAULT);
        System.out.println("update = " + update);


    }


    @AfterEach
    void afterAll() throws IOException {
     if (client!=null)
         client.close();
        
    }


}
