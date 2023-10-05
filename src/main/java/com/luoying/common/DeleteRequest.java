package com.luoying.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 * @author 落樱的悔恨
 */
@Data
public class DeleteRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}