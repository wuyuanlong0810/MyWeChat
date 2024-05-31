package com.easychat.redis;

import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.dto.TokenUserInfoDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: 吴远龙
 * @Date: 2024-05-24 20:06
 */
@Component("redisComponent")
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    /**
     * 获取心跳
     *
     * @param userId
     * @return
     */
    public Long getUserHeartBeat(String userId) {
        return (Long) redisUtils.get(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }

    public void saveHeartBeat(String userId){
        redisUtils.set(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId,System.currentTimeMillis(),30);
    }

    public void removeHeartBeat(String userId){
        redisUtils.del(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }



    /**
     * 存token
     *
     * @param tokenUserInfoDto
     */
    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto) {
        redisUtils.set(Constants.REDIS_KEY_WS_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto);//永久
        redisUtils.set(Constants.REDIS_KEY_WS_TOKEN_USERID + tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken());//永久
    }
    public TokenUserInfoDto getTokenUserInfoDto(String token){
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
    }


    public SysSettingDto getSysSetting() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (sysSettingDto == null) {
            return new SysSettingDto();
        }
        return sysSettingDto;
    }

    public void saveSysSetting(SysSettingDto sysSettingDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
    }

    /**
     * 清空用户的联系人
     * @param userId 用户ID
     */
    public void cleanUserContact(String userId) {
        redisUtils.del(Constants.REDIS_KEY_USER_CONTACT + userId);
    }

    /**
     * 批量添加用户联系人
     * @param userId 用户ID
     * @param contactIdList 联系人ID列表
     */
    public void addUserContactBatch(String userId, List<String> contactIdList) {
        redisUtils.lSet(Constants.REDIS_KEY_USER_CONTACT + userId, contactIdList);//永久
    }

    public List<String> getUserContactList(String userId){
        List<Object> objectList = redisUtils.lGet(Constants.REDIS_KEY_USER_CONTACT + userId,0L,-1L); // This is your original list of Object
        List<String> stringList = objectList.stream()
                .filter(obj -> obj instanceof String)
                .map(obj -> (String) obj)
                .collect(Collectors.toList());
        return stringList;
    }


}
