package com.zcom.zcompicturebackend.model.enums;


import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum UserRoleEnum {

        USER("用户", "user"),
        ADMIN("管理员", "admin");

        private final String text;

        private final String value;

        // 在枚举类中, 构造方法均为私有化
        UserRoleEnum(String text, String value) {
            this.text = text;
            this.value = value;
        }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的 value
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}
