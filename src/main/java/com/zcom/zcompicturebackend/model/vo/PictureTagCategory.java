package com.zcom.zcompicturebackend.model.vo;


import lombok.Data;

import java.util.List;

@Data
public class PictureTagCategory {

    /**
     * 标签类
     */
    private List<String> tagList;


    /**
     * 分类列表
     */
    private List<String> categoryList;

}
