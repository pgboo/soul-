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
 * 用户隐私设置表。对应用户主页 show_age/show_city 过滤。
 */
@Data
@TableName("user_privacy_setting")
public class UserPrivacySetting implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 展示年龄:0否 1是 */
    private Integer showAge;

    /** 展示城市:0否 1是 */
    private Integer showCity;

    /** 展示在线状态:0否 1是 */
    private Integer showOnline;

    /** 允许被推荐:0否 1是 */
    private Integer allowRecommend;

    /** 允许陌生人私信:0否 1是 */
    private Integer allowStrangerMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
