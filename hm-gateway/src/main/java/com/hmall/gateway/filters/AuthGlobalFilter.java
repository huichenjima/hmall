package com.hmall.gateway.filters;

import cn.hutool.core.collection.CollUtil;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.Cookie;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

/**
 * ClassName: AuthGlobalFilter
 * Package: com.hmall.gateway.filters
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/10 15:23
 * @Version 1.0
 */
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher antPathMatcher=new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
//        URI uri = request.getURI();
//        List<String> excludePaths = authProperties.getExcludePaths();
        if (isExclude(request.getPath().toString())){
            //  放行
           return chain.filter(exchange);
        }

        HttpHeaders headers = request.getHeaders();
        List<String> authorization = headers.get("authorization");
        String token=null;
        if (!CollUtil.isEmpty(authorization))
            token=authorization.get(0);
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
        } catch (UnauthorizedException e) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //校验通过
//        System.out.println("userId = " + userId);
        String userInfo=userId.toString();
        ServerWebExchange serverWebExchange = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        return chain.filter(serverWebExchange);
    }

    private boolean isExclude(String path) {
        List<String> excludePaths = authProperties.getExcludePaths();
        for (String pathPattern: excludePaths) {
            if (antPathMatcher.match(pathPattern, path))
                return true;



        }
        return false;


    }


    @Override
    public int getOrder() {
        return -1;
    }
}
