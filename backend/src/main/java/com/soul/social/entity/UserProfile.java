package com.soul.social.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户资料表。对应表 user_profile。
 */
@Data
@TableName("user_profile")
public class UserProfile implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String nickname;

    private String avatar;

    /** 性别:0未知 1男 2女 */
    private Integer gender;

    private LocalDate birthday;

    private String city;

    private String sign;

    private String intro;

    /** 情感状态:0未设置 1单身 2恋爱中 */
    private Integer emotionStatus;

    /** 交友目的:0未设置 1找朋友 2找对象 3随便聊聊 */
    private Integer friendGoal;

    /** 灵魂号 */
    private String soulId;

    /** 资料完整度百分比 */
    private Integer completePercent;

    /** 昵称修改次数 */
    private Integer nicknameModifyCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
