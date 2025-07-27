package com.zcom.zcompicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zcom.zcompicturebackend.exception.BusinessException;
import com.zcom.zcompicturebackend.model.dto.user.UserQueryRequest;
import com.zcom.zcompicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zcom.zcompicturebackend.model.vo.LoginUserVO;
import com.zcom.zcompicturebackend.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Administrator
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-03-07 15:42:09
 */
public interface UserService extends IService<User> {


//    /**
//     * 用户注册(初始代码)
//     *
//     * @param userAccount   用户账户
//     * @param userPassword  用户密码
//     * @param checkPassword 校验密码
//     * @return 新用户 id
//     */
//    long userRegister(String userAccount, String userPassword, String checkPassword);
//
//
//    /**
//     * 用户注册（二次拓展, 新增用户上传头像）-
//     *
//     * @param userAccount   用户账户
//     * @param userPassword  用户密码
//     * @param checkPassword 校验密码
//     * @param userAvatar    用户头像
//     * @return 新用户 id
//     */
//    long userRegister(String userAccount, String userPassword, String checkPassword, String userAvatar);


    /**
     * 用户注册（三次拓展, 新增用户上传自己的用户名）
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param userAvatar    用户头像
     * @param userName      用户名
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String userAvatar, String userName);


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取加密后的密码
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获得脱敏后的登录用户信息
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获得脱敏后的用户信息
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获得脱敏后的用户信息列表
     * @param userList
     * @return 脱敏后的用户列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 用户注销
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User>                                                                                                                        getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 判断是否为管理员
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户上传头像（本地图片上传）
     * @param file 图片文件
     * @param request 请求
     * @return 头像URL
     */
    String uploadAvatar(MultipartFile file, HttpServletRequest request);
}
