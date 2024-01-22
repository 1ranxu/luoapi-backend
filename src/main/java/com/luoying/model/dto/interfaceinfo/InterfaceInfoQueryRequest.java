package com.luoying.model.dto.interfaceinfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.luoying.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 接口查询请求
 *
 * @author 落樱的悔恨
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoQueryRequest extends PageRequest implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 扣减积分数
     */
    private Long reduceScore;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 接口状态 0-关闭 1-开启
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}