package com.hmall.item.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.item.domain.dto.OrderDetailDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.aspectj.lang.annotation.Pointcut;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * ClassName: PayStatusListener
 * Package: com.hmall.trade.llistener
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/14 12:32
 * @Version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ItemChangeListener {
    private final IItemService itemService;
    private RestHighLevelClient client;

    public RestHighLevelClient getClient() {
        return client;
    }

    public void setClient(RestHighLevelClient client) {
        this.client = client;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "item.insert.success.queue",durable = "true"),
            exchange = @Exchange(name = "item.direct",type = "direct"),
            key = "insert.success"
    ))
    public void ListenInsertItem(Item item) throws IOException {
        log.info("监听成功，开始向索引库中插入数据");
//        client=new RestHighLevelClient(RestClient.builder(
//                HttpHost.create("192.168.44.128:9200")
//        ));
        IndexRequest request=new IndexRequest("items").id(item.getId().toString());
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        String jsonStr = JSONUtil.toJsonStr(itemDoc);
        request.source(jsonStr, XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);

//        client.close();
//






    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "item.delete.success.queue",durable = "true"),
            exchange = @Exchange(name = "item.direct",type = "direct"),
            key = "delete.success"
    ))
    public void ListenDeleteItem(Long id) throws IOException {
        log.info("监听成功，开始向索引库中删除数据");
//        client=new RestHighLevelClient(RestClient.builder(
//                HttpHost.create("192.168.44.128:9200")
//        ));
        DeleteRequest request=new DeleteRequest("items",id.toString());
        client.delete(request,RequestOptions.DEFAULT);
//        client.close();




    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "item.status.success.queue",durable = "true"),
            exchange = @Exchange(name = "item.direct",type = "direct"),
            key = "status.success"
    ))
    public void ListenStatusItem(Item item) throws IOException {
        //因为itemdoc中没有status，index中全是上架商品
        //这里的逻辑应该是如果变为上架状态，那么应该向索引库中添加，下架则删除
        log.info("监听修改状态成功");
        Integer status = item.getStatus();
//        client = new RestHighLevelClient(RestClient.builder(
//                HttpHost.create("192.168.44.128:9200")
//        ));
        // 如果是上架 则添加
        if (status == 1)
        {
            IndexRequest request=new IndexRequest("items").id(item.getId().toString());
            Item item1 = itemService.getById(item.getId());
            ItemDoc itemDoc = BeanUtil.copyProperties(item1, ItemDoc.class);
            String jsonStr = JSONUtil.toJsonStr(itemDoc);
            request.source(jsonStr, XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);


        }
        else{
            // 反之下架则要删除
            DeleteRequest request=new DeleteRequest("items",item.getId().toString());
            client.delete(request,RequestOptions.DEFAULT);
        }
//        client.close();


    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "item.update.success.queue",durable = "true"),
            exchange = @Exchange(name = "item.direct",type = "direct"),
            key = "update.success"
    ))
    public void ListenUpdateItem(Item item) throws IOException {
        //这里不一定是对某个字段更新，所以直接用覆盖更新
//        client = new RestHighLevelClient(RestClient.builder(
//                HttpHost.create("192.168.44.128:9200")
//        ));
        IndexRequest request=new IndexRequest("items").id(item.getId().toString());
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        String jsonStr = JSONUtil.toJsonStr(itemDoc);
        request.source(jsonStr, XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
//        client.close();





    }
    //doc中没有库存这个属性所以不用管
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(name = "item.deduct.success.queue",durable = "true"),
//            exchange = @Exchange(name = "item.direct",type = "direct"),
//            key = "deduct.success"
//    ))
//    public void ListenDeductItem(List<OrderDetailDTO> items){
//
//
//
//
//    }








}
