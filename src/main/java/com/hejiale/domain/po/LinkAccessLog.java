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
 * 短链接访问日志表，记录每次点击详情，用于统计分析
 * </p>
 *
 * @author hejiale
 * @since 2026-03-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("link_access_log")
@ApiModel(value="LinkAccessLog对象", description="短链接访问日志表，记录每次点击详情，用于统计分析")
public class LinkAccessLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID，自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "所属短码，关联short_link表")
    @TableField("link_id")
    private String linkId;

    @ApiModelProperty(value = "访问者IP地址（支持IPv6）")
    @TableField("ip")
    private String ip;

    @ApiModelProperty(value = "省份（通过IP解析）")
    @TableField("province")
    private String province;

    @ApiModelProperty(value = "城市")
    @TableField("city")
    private String city;

    @ApiModelProperty(value = "User-Agent原始字符串")
    @TableField("ua")
    private String ua;

    @ApiModelProperty(value = "操作系统类型")
    @TableField("os")
    private String os;

    @ApiModelProperty(value = "浏览器类型")
    @TableField("browser")
    private String browser;

    @ApiModelProperty(value = "访问时间")
    @TableField("create_time")
    private LocalDateTime createTime;


}
