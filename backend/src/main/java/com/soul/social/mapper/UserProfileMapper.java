package com.soul.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soul.social.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * user_profile 表 Mapper。
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
