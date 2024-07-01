package com.hmall.common.interceptors;

import cn.hutool.core.util.StrUtil;
import com.hmall.common.utils.UserContext;
import org.apache.catalina.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ClassName: UserInfoInterceptor
 * Package: com.hmall.common.interceptors
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/10 16:31
 * @Version 1.0
 */

public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userInfo = request.getHeader("user-info");
        if (StrUtil.isNotBlank(userInfo))
        {
            UserContext.setUser(Long.valueOf(userInfo));
        }
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清理用户
        UserContext.removeUser();
    }
}
