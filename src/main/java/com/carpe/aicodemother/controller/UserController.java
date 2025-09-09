package com.carpe.aicodemother.controller;

import cn.hutool.core.bean.BeanUtil;
import com.carpe.aicodemother.annotation.AuthCheck;
import com.carpe.aicodemother.common.BaseResponse;
import com.carpe.aicodemother.common.DeleteRequest;
import com.carpe.aicodemother.common.ResultUtils;
import com.carpe.aicodemother.constant.UserConstant;
import com.carpe.aicodemother.exception.BusinessException;
import com.carpe.aicodemother.exception.ErrorCode;
import com.carpe.aicodemother.exception.ThrowUtils;
import com.carpe.aicodemother.model.dto.user.*;
import com.carpe.aicodemother.model.vo.LoginUserVO;
import com.carpe.aicodemother.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.carpe.aicodemother.model.entity.User;
import com.carpe.aicodemother.service.UserService;
import com.carpe.aicodemother.manager.CosManager;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户 控制层。
 *

 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }


    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 返回脱敏后的信息!!!
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 保存用户。
     *
     * @param user 用户
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody User user) {
        return userService.save(user);
    }

    /**
     * 根据主键删除用户。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return userService.removeById(id);
    }

    /**
     * 根据主键更新用户。
     *
     * @param user 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody User user) {
        return userService.updateById(user);
    }

    /**
     * 查询所有用户。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<User> list() {
        return userService.list();
    }

    /**
     * 根据主键获取用户。
     *
     * @param id 用户主键
     * @return 用户详情
     */
    @GetMapping("getInfo/{id}")
    public User getInfo(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * 分页查询用户。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<User> page(Page<User> page) {
        return userService.page(page);
    }

    /**
     * 上传用户头像
     */
    /**
     * 更新当前用户信息（普通用户可用）
     */
    @PostMapping("/update/profile")
    public BaseResponse<Boolean> updateUserProfile(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        // 只允许用户更新自己的信息
        User user = new User();
        user.setId(loginUser.getId());
        
        // 只允许更新用户名，其他敏感信息不允许更新
        if (userUpdateRequest.getUserName() != null) {
            user.setUserName(userUpdateRequest.getUserName());
        }
        
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 参数校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        // 文件大小限制（5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过5MB");
        }
        
        // 文件类型校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }
        
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!".jpg".equals(fileExtension) && !".jpeg".equals(fileExtension) && !".png".equals(fileExtension)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持jpg、jpeg、png格式的图片");
        }
        
        try {
            // 生成唯一文件名
            String fileName = "avatar/" + loginUser.getId() + "/" + UUID.randomUUID().toString() + fileExtension;
            
            // 创建临时文件
            File tempFile = File.createTempFile("avatar", fileExtension);
            file.transferTo(tempFile);
            
            // 上传到COS
            String avatarUrl = cosManager.uploadFile(fileName, tempFile);
            
            // 删除临时文件
            tempFile.delete();
            
            if (avatarUrl == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "头像上传失败");
            }
            
            // 更新用户头像URL
            User updateUser = new User();
            updateUser.setId(loginUser.getId());
            updateUser.setUserAvatar(avatarUrl);
            boolean result = userService.updateById(updateUser);
            
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新用户头像失败");
            }
            
            return ResultUtils.success(avatarUrl);
            
        } catch (IOException e) {
            log.error("头像上传失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "头像上传失败");
        }
    }
}
