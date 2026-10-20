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
 * 打招呼记录表（用户主页 greetedToday 判断用）。
 */
@Data
@TableName("greet_record")
public class GreetRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    /** 状态:1已发送 */
    private Integer status;

    /** 对方已读:0否 1是 */
    private Integer readFlag;

    /** 对方已回复:0否 1是 */
    private Integer replyFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
