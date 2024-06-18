package com.mywechat.aspect;

import com.mywechat.annotation.GlobalInterceptor;
import com.mywechat.entity.constants.Constants;
import com.mywechat.entity.dto.TokenUserInfoDto;
import com.mywechat.entity.enums.ResponseCodeEnum;
import com.mywechat.exception.BusinessException;
import com.mywechat.redis.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class GlobalOperationAspect {//全局切面

    @Resource
    private RedisUtils redisUtils;

    private static final Logger logger = LoggerFactory.getLogger(GlobalOperationAspect.class);


    @Before("@annotation(com.mywechat.annotation.GlobalInterceptor)")
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
