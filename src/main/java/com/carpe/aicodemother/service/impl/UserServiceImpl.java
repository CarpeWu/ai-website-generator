package com.carpe.aicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.carpe.aicodemother.exception.BusinessException;
import com.carpe.aicodemother.exception.ErrorCode;
import com.carpe.aicodemother.model.dto.user.UserQueryRequest;
import com.carpe.aicodemother.model.enums.UserRoleEnum;
import com.carpe.aicodemother.model.vo.LoginUserVO;
import com.carpe.aicodemother.model.vo.UserVO;
import com.carpe.aicodemother.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.carpe.aicodemother.model.entity.User;
import com.carpe.aicodemother.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.carpe.aicodemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author jaeger
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
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
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }


    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        // 拿到数据库中“最新的用户信息”，避免使用 session 中的旧数据
        // session 中的 User 对象，那就是一个“缓存”，不是实时数据
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
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
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }


    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 4. 获取脱敏后的用户信息
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
       if (user == null) {
           return null;
       }
       LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户不能少于4位");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于8位");
        }
        if (checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "确认密码长度不能少于8位");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 2. 检差是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount).limit(1);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        /** myabtis-plus可用:
        if (this.mapper.exists(queryWrapper)) {
            // SQL: SELECT EXISTS(SELECT 1 FROM user WHERE userAccount = 'john123' LIMIT 1)
            // 返回: true/false
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");

            SELECT 1 是返回常量 1 的查询
            在 EXISTS 中使用 可以最高效地检查记录是否存在
            性能最优 因为不需要读取真实的列数据
            业界标准 写法，语义清晰
            所以当你看到 SELECT 1 时，通常意味着"我只关心是否有匹配的记录，不关心具体的数据内容"。
        }
         */
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败, 数据库错误");
        }
        
        // 5. 更新用户名为'用户XXXX'格式（XXXX为用户ID后4位）
        Long userId = user.getId();
        String userIdStr = String.valueOf(userId);
        String lastFourDigits = userIdStr.length() >= 4 ? 
            userIdStr.substring(userIdStr.length() - 4) : 
            String.format("%04d", userId % 10000);
        user.setUserName("用户" + lastFourDigits);
        this.updateById(user);
        
        return userId;
    }
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值, 混淆密码
        final String SALT = "carpe";
        return DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes(StandardCharsets.UTF_8));
    }
}
