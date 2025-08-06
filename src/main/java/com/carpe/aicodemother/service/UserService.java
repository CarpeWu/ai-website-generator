package com.carpe.aicodemother.service;

import com.carpe.aicodemother.model.dto.UserQueryRequest;
import com.carpe.aicodemother.model.vo.LoginUserVO;
import com.carpe.aicodemother.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.carpe.aicodemother.model.entity.User;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author jaeger
 */
public interface UserService extends IService<User> {

    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    /**
     *  获取当前登录用户
     * @param request
     * @return
     * 为什么不脱敏:
     * 以后改成微服务, 可能有很多其他服务也要使用获取 当前用户 session 的方法,
     * 有些微服务需要获取的数据可能不止是脱敏后的信息
     *
     */
    User getLoginUser(HttpServletRequest request);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    /**
     *  用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param request 用于更新 session
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     * @return 脱敏后的用户
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 加密
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);
}
