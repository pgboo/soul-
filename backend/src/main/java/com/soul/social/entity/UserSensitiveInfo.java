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
 * 用户敏感信息表（手机号/openid/unionid/password，禁止出现在任何 Resp）。对应表 user_sensitive_info。
 */
@Data
@TableName("user_sensitive_info")
public class UserSensitiveInfo implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 手机号（敏感） */
    private String phone;

    /** 微信 openid（敏感） */
    private String openid;

    /** 微信 unionid（敏感） */
    private String unionid;

    /** 密码（BCrypt，仅后台账号用） */
    private String password;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
