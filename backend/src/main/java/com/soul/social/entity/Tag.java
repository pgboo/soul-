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
 * 标签表。
 */
@Data
@TableName("tag")
public class Tag implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String name;

    /** 热门标签:0否 1是 */
    private Integer hotFlag;

    /** 用于匹配:0否 1是 */
    private Integer matchFlag;

    /** 用于推荐:0否 1是 */
    private Integer recommendFlag;

    private Integer sort;

    /** 状态:0禁用 1启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDelete;
}
