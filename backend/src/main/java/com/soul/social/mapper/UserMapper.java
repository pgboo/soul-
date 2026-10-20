package com.soul.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soul.social.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * user 表 Mapper。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
