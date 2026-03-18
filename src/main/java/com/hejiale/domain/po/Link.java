package com.hejiale.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 短链接核心信息表
 * </p>
 *
 * @author hejiale
 * @since 2026-03-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("link")
@ApiModel(value="Link对象", description="短链接核心信息表")
public class Link implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID，自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "短码（如 aB3c9e），Base62编码结果")
    @TableField("link_code")
    private String linkCode;

    @ApiModelProperty(value = "链接标题（方便后台管理显示）")
    @TableField("link_title")
    private String linkTitle;

    @ApiModelProperty(value = "原始长链接")
    @TableField("original_url")
    private String originalUrl;

    @TableField("user_id")
    private Integer userId;

    @ApiModelProperty(value = "是否启用 (1:启用, 0:禁用)")
    @TableField("is_active")
    private Boolean isActive;

    @ApiModelProperty(value = "过期时间（NULL表示永不过期）")
    @TableField("expiration_time")
    private LocalDateTime expirationTime;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;


}
