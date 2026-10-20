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
 * 用户灵魂测试记录表（仅保留最后一次）。
 */
@Data
@TableName("user_soul_test_record")
public class UserSoulTestRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String resultCode;

    /** 答题明细 JSON */
    private String answerJson;

    /** 分值统计 JSON */
    private String scoreJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
