package com.zcom.zcompicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcom.zcompicturebackend.constant.UserConstant;
import com.zcom.zcompicturebackend.exception.BusinessException;
import com.zcom.zcompicturebackend.exception.ErrorCode;
import com.zcom.zcompicturebackend.manager.auth.StpKit;
import com.zcom.zcompicturebackend.model.dto.user.UserQueryRequest;
import com.zcom.zcompicturebackend.model.entity.User;
import com.zcom.zcompicturebackend.model.enums.UserRoleEnum;
import com.zcom.zcompicturebackend.model.vo.LoginUserVO;
import com.zcom.zcompicturebackend.model.vo.UserVO;
import com.zcom.zcompicturebackend.service.UserService;
import com.zcom.zcompicturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import com.zcom.zcompicturebackend.manager.CosManager;
import javax.annotation.Resource;
import com.zcom.zcompicturebackend.config.CosClientConfig;


import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.zcom.zcompicturebackend.constant.UserConstant.USER_LOGIN_STATE;


/**
 * @author Administrator
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-03-07 15:42:09
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService{

    @Resource
    private CosManager cosManager;
    @Resource
    private com.zcom.zcompicturebackend.config.CosClientConfig cosClientConfig;

        /**
         * 用户注册功能
         * @param userAccount   用户账户
         * @param userPassword  用户密码
         * @param checkPassword 校验密码
         * @return
         */
        @Override
        public long userRegister(String userAccount, String userPassword, String checkPassword, String userAvatar, String userName) {
            // 1. 校验参数
            if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
            }

            if (userAccount.length() < 4) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
            }

            if (userPassword.length() < 8 || checkPassword.length() < 8) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
            }

            if (!userPassword.equals(checkPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
            }

            // 2. 检查用户账号是否和数据库中已有的账号重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                deleteCosAvatarIfNeeded(userAvatar);
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 3. 密码要进行再加密处理
            String encryptPassword = getEncryptPassword(userPassword);
            // 4. 插入数据到数据库中
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            if (userName == null) {
                // todo 调用生成随机名的接口
                user.setUserName("无名");
            }
            else {
                user.setUserName(userName);
            }
            user.setUserRole(UserRoleEnum.USER.getValue());
            // 新增功能, 设置用户头像, 如果前端没有提供头像URL, 则设置为null或者用steam的默认头像(后续用户可以自己设置头像)
            if (userAvatar == null) {
                user.setUserAvatar("https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/fe/fef49e7fa7e1997310d705b2a6158ff8dc1cdfeb.jpg");
            }
            else {
                user.setUserAvatar(userAvatar);
            }


            try {
                boolean saveResult = this.save(user);
                if (!saveResult) {
                    deleteCosAvatarIfNeeded(userAvatar);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败, 数据库错误");
                }
                return user.getId();
            } catch (BusinessException e) {
                deleteCosAvatarIfNeeded(userAvatar);
                throw e;
            }
        }

        /**
         * 注册失败时自动删除COS头像图片
         */
        private void deleteCosAvatarIfNeeded(String userAvatar) {
            if (StrUtil.isNotBlank(userAvatar) && userAvatar.startsWith(cosClientConfig.getHost())) {
                String key = userAvatar.replace(cosClientConfig.getHost() + "/", "");
                cosManager.deleteObject(key);
            }
        }

        /**
         * 用户登录功能
         * @param userAccount  用户账户
         * @param userPassword 用户密码
         * @param request
         * @return
         */
        @Override
        public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
            // 1. 校验
            if (StrUtil.hasBlank(userAccount, userPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
            }

            if (userAccount.length() < 4) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
            }

            if (userPassword.length() < 8) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
            }

            // 2. 对用户传递的密码进行加密
            String encryptPassword = getEncryptPassword(userPassword);

            // 3. 查询数据库中的用户是否存在, 若不存在, 则抛异常
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            queryWrapper.eq("userPassword", encryptPassword);
            User user  = this.baseMapper.selectOne(queryWrapper);
            // 不存在, 抛异常
            if (user == null) {
                // 打印日志(log4j)
                log.info("user login failed, userAccount cannot match userPassword");
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
            }

            // 4. 保存用户的登录状态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            // 记录用户登录态到 Sa-token, 便于空间鉴权时使用, 注意保证该用户信息与 SpringSession 中的信息过期时间一致
            StpKit.SPACE.login(user.getId());
            StpKit.SPACE.getSession().set(USER_LOGIN_STATE, user);
            return this.getLoginUserVO(user);
        }


        /**
         * 获取加密后的密码
         * @param userPassword 用户密码
         * @return
         */
        @Override
        public String getEncryptPassword(String userPassword) {
            // 盐值，混淆密码
            final String SALT = "Z-COM";
            return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        }

        @Override
        public User getLoginUser(HttpServletRequest request) {
            // 判断是否登录
            Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
            User currentUser = (User) userObj;
            if (currentUser == null || currentUser.getId() == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            // 从数据库中查询(追求性能的话可以注释, 直接返回上述结果)
            Long userID = currentUser.getId();
            currentUser = this.getById(userID);
            if (currentUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            return currentUser;
        }

        /**
         * 获得脱敏类的用户信息
         * @param user  用户
         * @return  脱敏后的用户信息
         */
        @Override
        public LoginUserVO getLoginUserVO(User user) {
            if (user == null) {
                return null;
            }
            LoginUserVO loginnUserVO = new LoginUserVO();
            BeanUtil.copyProperties(user, loginnUserVO);
            return loginnUserVO;
        }

        @Override
        public UserVO getUserVO(User user) {
            if (user == null) {
                return null;
            }
            UserVO userVO = new UserVO();
            BeanUtil.copyProperties(user, userVO);
            return userVO;
        }

        @Override
        public List<UserVO> getUserVOList(List<User> userList) {
            if (CollUtil.isEmpty(userList)) {
                return new ArrayList<>();
            }
            // 利用了lambda表达式
            return userList.stream()
                    .map(this::getUserVO)
                    .collect(Collectors.toList());
        }

        public boolean userLogout(HttpServletRequest request) {
            // 判断是否登录
            Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
            if (userObj == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
            }
            // 移除登录态
            request.getSession().removeAttribute(USER_LOGIN_STATE);
            return true;
        }

        /**
         * 用户上传头像（本地图片上传）
         * @param file 图片文件
         * @param request 请求
         * @return 头像URL
         */
        @Override
        public String uploadAvatar(MultipartFile file, HttpServletRequest request) {
            if (file == null || file.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "未选择文件");
            }
            // 校验图片类型
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".png";
            if (!suffix.matches("\\.(jpg|jpeg|png|webp|bmp|gif)$")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅支持图片格式");
            }
            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;
            String key = "avatar/" + fileName;
            // 临时文件
            File tempFile;
            try {
                tempFile = File.createTempFile("avatar_", suffix);
                file.transferTo(tempFile);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件处理失败");
            }
            try {
                cosManager.putObject(key, tempFile);
            } finally {
                tempFile.delete();
            }
            // 拼接头像URL
            String avatarUrl = cosClientConfig.getHost() + "/" + key;
            // 只返回URL，不做用户信息更新
            return avatarUrl;
        }

        @Override
        public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
            if (userQueryRequest == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
            }
            Long id = userQueryRequest.getId();
            String userAccount = userQueryRequest.getUserAccount();
            String userName = userQueryRequest.getUserName();
            String userProfile = userQueryRequest.getUserProfile();
            String userRole = userQueryRequest.getUserRole();
            String sortField = userQueryRequest.getSortField();
            String sortOrder = userQueryRequest.getSortOrder();
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
            queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
            queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
            queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
            queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
            queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
            return queryWrapper;
        }

        @Override
        public boolean isAdmin(User user) {
            return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
        }



}




