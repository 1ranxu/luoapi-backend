package com.luoying.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 *  上传图片状态vo
 */
@Data
public class ImageVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    private String uid;
    /**
     * 文件名
     */
    private String name;
    /**
     * 图片上传状态
     */
    private String status;
    /**
     * 图片的url
     */
    private String url;
}