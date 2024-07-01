package com.hmall.item.aspect;

import com.hmall.item.listener.ItemChangeListener;
import org.apache.http.HttpHost;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Aspect
@Component
public class ElasticsearchClientAspect {
//    private ThreadLocal<RestHighLevelClient> clientThreadLocal = new ThreadLocal<>();

    @Pointcut("execution(* com.hmall.item.listener.ItemChangeListener.*(..))")
    public void itemListener() {}

    @Around("itemListener()")
    public Object  createClient(ProceedingJoinPoint joinPoint) {
        ItemChangeListener listener = (ItemChangeListener) joinPoint.getTarget();
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("192.168.44.128:9200")
        ));
        listener.setClient(client);
        Object result = null;
        try {
            result=joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    // 处理关闭客户端时的异常
                    e.printStackTrace();
                }
            }
        }
        return  result;

    }

}
