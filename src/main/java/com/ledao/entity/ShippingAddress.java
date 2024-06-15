package com.ledao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName t_shipping_address
 */
@TableName(value ="t_shipping_address")
@Data
public class ShippingAddress implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @TableField(value = "userId")
    private Integer userId;

    /**
     * 
     */
    @TableField(value = "content")
    private String content;

    /**
     * 
     */
    @TableField(value = "addDate")
    private LocalDateTime addDate;

    /**
     * 
     */
    @TableField(value = "useOrNot")
    private Integer useOrNot;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}