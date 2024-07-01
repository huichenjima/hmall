package com.hmall.item.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * ClassName: ElasticTest
 * Package: com.hmall.item.es
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/21 21:24
 * @Version 1.0
 */
public class ElasticTest {
    private RestHighLevelClient client;

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

    @AfterEach
    void afterAll() throws IOException {
     if (client!=null)
         client.close();
        
    }
    //创建索引库
    @Test
    public void test2() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest("items");
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        client.indices().create(request, RequestOptions.DEFAULT);


    }
    //查询
    @Test
    public void test3() throws IOException {

        GetIndexRequest request = new GetIndexRequest("items");
//        GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println("exists = " + exists);

//        System.out.println(getIndexResponse.getMappings());


    }

    //查询
    @Test
    public void test4() throws IOException {

        DeleteIndexRequest request = new DeleteIndexRequest("items");
        client.indices().delete(request,RequestOptions.DEFAULT);


    }

    private static final String MAPPING_TEMPLATE = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\":{\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\"\n" +
            "      },\n" +
            "      \"price\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"stock\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"image\":{\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"category\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"brand\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"sold\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"commentCount\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"isAD\":{\n" +
            "        \"type\": \"boolean\"\n" +
            "      },\n" +
            "      \"updateTime\":{\n" +
            "        \"type\": \"date\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

}
