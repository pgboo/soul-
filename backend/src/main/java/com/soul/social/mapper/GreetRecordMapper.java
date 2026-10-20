package com.soul.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soul.social.entity.GreetRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * greet_record 表 Mapper（主页 greetedToday 判断）。
 */
@Mapper
public interface GreetRecordMapper extends BaseMapper<GreetRecord> {
}
