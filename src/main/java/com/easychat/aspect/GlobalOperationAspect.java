package com.easychat.aspect;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.ResponseCodeEnum;
import com.easychat.exception.BusinessException;
import com.easychat.redis.RedisUtils;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-24 22:46
 */
@Aspect
@Component
public class GlobalOperationAspect {

    @Resource
    private RedisUtils redisUtils;

    private static final Logger logger = LoggerFactory.getLogger(GlobalOperationAspect.class);


    @Before("@annotation(com.easychat.annotation.GlobalInterceptor)")
    public void interceptorDo(JoinPoint joinPoint) {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);

        if (interceptor == null) {
            return;
        }
        if (interceptor.checkLogin() || interceptor.checkAdmin()) {
            checkLogin(interceptor.checkAdmin());
        }


    }

    private void checkLogin(Boolean checkAdmin) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("token");
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }

        if (checkAdmin && !tokenUserInfoDto.getAdmin()) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }
}
