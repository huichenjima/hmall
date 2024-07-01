package com.hmall.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * ClassName: DynamicRouteLoader
 * Package: com.hmall.gateway.routers
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/10 20:46
 * @Version 1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteLoader {


    private final NacosConfigManager nacosConfigManager;

    private final RouteDefinitionWriter writer;

    private final String dataId= "gateway-routes.json";

    private final String group="DEFAULT_GROUP";

    private final Set<String> routeIds=new HashSet<>();
    @PostConstruct
    public void initRouteConfigListener() throws NacosException {
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {

                        // 监听到配置变更，需要去更新路由表
                        updateConfigInfo(configInfo);


                    }
                });
        //第一次读到配置，也需要更新路由表
        updateConfigInfo(configInfo);


    }


    public void updateConfigInfo(String configInfo){
        log.debug("监听到路由配置信息:{}",configInfo);
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        for (String routeId : routeIds) {
            writer.delete(Mono.just(routeId)).subscribe();
        }
        routeIds.clear();
        for (RouteDefinition routeDefinition : routeDefinitions) {
            //更新路由表
            writer.save(Mono.just(routeDefinition)).subscribe();
            routeIds.add(routeDefinition.getId());
        }

    }

}
