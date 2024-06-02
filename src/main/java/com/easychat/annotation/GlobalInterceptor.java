package com.easychat.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-24 22:49
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalInterceptor {//全局拦截器
    boolean checkLogin() default true;//校验登录，在不用校验的地方用false

    boolean checkAdmin() default false;//校验管理员权限，在需要时为true


}
