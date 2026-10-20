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
 * 用户登录日志表。
 */
@Data
@TableName("user_login_log")
public class UserLoginLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 登录方式:1手机号 2微信 */
    private Integer loginType;

    private String loginIp;

    private String deviceInfo;

    /** 结果:1成功 0失败 */
    private Integer loginStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
