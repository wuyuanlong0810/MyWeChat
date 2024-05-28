package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.exception.BusinessException;
import com.easychat.redis.RedisComponent;
import com.easychat.redis.RedisUtils;
import com.easychat.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-23 21:29
 */
@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserInfoService userInfoService;


    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/checkcode")//返回验证码
    public ResponseVO checkCode() {

        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);

        String code = captcha.text();//验证码

        String base64 = captcha.toBase64();//验证码图片
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.set(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, 300);
        Map<String, String> result = new HashMap<>();

        result.put("checkCode", base64);
        result.put("checkCodeKey", checkCodeKey);

        return getSuccessResponseVO(result);
    }

    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @Email @NotEmpty String email,
                               @NotEmpty String password,
                               @NotEmpty String nickName,
                               @NotEmpty String checkCode) {
        if (!checkCode.equals(redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
            throw new BusinessException("验证码错误");
        }

        redisUtils.del(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);

        userInfoService.register(email, password, nickName);

        return getSuccessResponseVO(null);
    }

    @RequestMapping("/login")
    public ResponseVO login(@NotEmpty String checkCodeKey,
                            @Email @NotEmpty String email,
                            @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password,
                            @NotEmpty String checkCode) {
        if (!checkCode.equals(redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
            throw new BusinessException("验证码错误");
        }
        redisUtils.del(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);

        UserInfoVO userInfoVO = userInfoService.login(email, password);

        return getSuccessResponseVO(userInfoVO);
    }

    @GlobalInterceptor
    @RequestMapping("/getSysSetting")
    public ResponseVO getSysSetting() {
        return getSuccessResponseVO(redisComponent.getSysSetting());
    }
}
