package com.luoying.model.vo;

import com.luoying.model.entity.InterfaceInfo;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口信息封装类
 *
 * @author 落樱的悔恨
 */
@Data
public class InvokeInterfaceInfoVO extends InterfaceInfo implements Serializable {

    private Long totalInvokeNum;

    private static final long serialVersionUID = 1L;
}