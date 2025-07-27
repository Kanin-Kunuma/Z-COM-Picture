package com.zcom.zcompicturebackend.controller;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zcom.zcompicturebackend.annotation.AuthCheck;
import com.zcom.zcompicturebackend.common.BaseResponse;
import com.zcom.zcompicturebackend.common.DeleteRequest;
import com.zcom.zcompicturebackend.common.ResultUtils;
import com.zcom.zcompicturebackend.constant.UserConstant;
import com.zcom.zcompicturebackend.exception.BusinessException;
import com.zcom.zcompicturebackend.exception.ErrorCode;
import com.zcom.zcompicturebackend.exception.ThrowUtils;
import com.zcom.zcompicturebackend.manager.auth.SpaceUserAuthManager;
import com.zcom.zcompicturebackend.model.dto.picture.*;
import com.zcom.zcompicturebackend.model.dto.space.*;
import com.zcom.zcompicturebackend.model.entity.Picture;
import com.zcom.zcompicturebackend.model.entity.Space;
import com.zcom.zcompicturebackend.model.entity.User;
import com.zcom.zcompicturebackend.model.enums.PictureReviewStatusEnum;
import com.zcom.zcompicturebackend.model.enums.SpaceLevelEnum;
import com.zcom.zcompicturebackend.model.vo.PictureTagCategory;
import com.zcom.zcompicturebackend.model.vo.PictureVO;
import com.zcom.zcompicturebackend.model.vo.SpaceVO;
import com.zcom.zcompicturebackend.service.PictureService;
import com.zcom.zcompicturebackend.service.SpaceService;
import com.zcom.zcompicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

        @Resource
        private UserService userService;

        @Resource
        private SpaceService spaceService;

        @Resource
        private StringRedisTemplate stringRedisTemplate;

        @Resource
        private SpaceUserAuthManager spaceUserAuthManager;


        /**
         * 本地缓存（构造）
         */
        private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
                .initialCapacity(1024)
                .maximumSize(10_000L)   // 最大 10000 条
                .expireAfterWrite(Duration.ofMinutes(5))    // 缓存 5 分钟后移除
                .build();



        @PostMapping("/add")
        public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
            ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
            User loginUser = userService.getLoginUser(request);
            long newId = spaceService.addSpace(spaceAddRequest, loginUser);
            return ResultUtils.success(newId);
        }


        /**
         * 删除空间
         */
        @PostMapping("/delete")
        public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
            if (deleteRequest == null || deleteRequest.getId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            User loginUser = userService.getLoginUser(request);
            long id = deleteRequest.getId();
            // 判断是否存在
            Space oldSpace = spaceService.getById(id);
            ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
            // 仅本人或管理员可删除
            if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 操作数据库
            boolean result = spaceService.removeById(id);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            return ResultUtils.success(true);
        }

        /**
         * 更新空间（仅管理员可用）
         * @param spaceUpdateRequest
         * @param request
         * @return
         */
        @PostMapping("/update")
        @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
        public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
            if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            // 将实体类和 DTO 进行转换
            Space space = new Space();
            BeanUtils.copyProperties(spaceUpdateRequest, space);
            // 自动填充数据
            spaceService.fillSpaceBySpaceLevel(space);
            // 数据校验
            spaceService.validSpace(space, false);
            // 判断是否存在
            long id = spaceUpdateRequest.getId();
            Space oldSpace = spaceService.getById(id);
            ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
            // 操作数据库
            boolean result = spaceService.updateById(space);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            return ResultUtils.success(true);
        }

        /**
         * 根据 id 获取空间（仅管理员可用）
         */
        @GetMapping("/get")
        @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
        public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
            ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
            // 查询数据库
            Space picture = spaceService.getById(id);
            ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
            // 获取封装类
            return ResultUtils.success(picture);
        }

        /**
         * 根据 id 获取空间（封装类）
         */
        @GetMapping("/get/vo")
        public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
            ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
            // 查询数据库
            Space space = spaceService.getById(id);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
            SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
            User loginUser = userService.getLoginUser(request);
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            spaceVO.setPermissionList(permissionList);
            // 获取封装类
            return ResultUtils.success(spaceVO);
        }

        /**
         * 分页获取空间列表（仅管理员可用）
         */
        @PostMapping("/list/page")
        @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
        public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest pictureQueryRequest) {
            long current = pictureQueryRequest.getCurrent();
            long size = pictureQueryRequest.getPageSize();
            // 查询数据库
            Page<Space> picturePage = spaceService.page(new Page<>(current, size),
                    spaceService.getQueryWrapper(pictureQueryRequest));
            return ResultUtils.success(picturePage);
        }

        /**
         * 分页获取空间列表（封装类）
         * 普通用户也可以使用
         */
        @PostMapping("/list/page/vo")
        public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest pictureQueryRequest,
                                                                 HttpServletRequest request) {
            long current = pictureQueryRequest.getCurrent();
            long size = pictureQueryRequest.getPageSize();
            // 限制爬虫
            ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
            // 查询数据库
            Page<Space> picturePage = spaceService.page(new Page<>(current, size),
                    spaceService.getQueryWrapper(pictureQueryRequest));
            // 获取封装类
            return ResultUtils.success(spaceService.getSpaceVOPage(picturePage, request));
        }

        /**
         * 编辑空间（给用户使用）
         */
        @PostMapping("/edit")
        public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
            if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            // 在此处将实体类和 DTO 进行转换
            Space space = new Space();
            BeanUtils.copyProperties(spaceEditRequest, space);
            // 自动填充数据
            spaceService.fillSpaceBySpaceLevel(space);
            // 设置编辑时间
            space.setEditTime(new Date());
            // 数据校验
            spaceService.validSpace(space, false);
            User loginUser = userService.getLoginUser(request);
            // 判断是否存在
            long id = spaceEditRequest.getId();
            Space oldSpace = spaceService.getById(id);
            ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
            // 仅本人或管理员可编辑
            spaceService.checkSpaceAuth(loginUser, oldSpace);
/*            if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }*/
            // 操作数据库
            boolean result = spaceService.updateById(space);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            return ResultUtils.success(true);
        }

        /**
         *
         * @return
         */
        @GetMapping("/list/level")
            public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
                List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values()) // 获取所有枚举
                        .map(spaceLevelEnum -> new SpaceLevel(
                                spaceLevelEnum.getValue(),
                                spaceLevelEnum.getText(),
                                spaceLevelEnum.getMaxCount(),
                                spaceLevelEnum.getMaxSize()))
                        .collect(Collectors.toList());
                return ResultUtils.success(spaceLevelList);
            }


}
