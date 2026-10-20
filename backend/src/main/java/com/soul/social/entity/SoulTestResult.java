package com.soul.social.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 灵魂测试结果类型表。
 */
@Data
@TableName("soul_test_result")
public class SoulTestResult implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 结果编码 */
    private String resultCode;

    /** 结果名称 */
    private String name;

    private String icon;

    private String description;

    /** 社交建议 */
    private String socialAdvice;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
