package com.soul.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soul.social.entity.AuditRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * audit_record 表 Mapper。
 */
@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {
}
