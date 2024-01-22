package com.luoying.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 */
@Data
public class UploadFileRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 图片用途
     */
    private String biz;
}