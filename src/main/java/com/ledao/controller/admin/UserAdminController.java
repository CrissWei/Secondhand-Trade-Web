package com.ledao.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledao.entity.PageBean;
import com.ledao.entity.User;
import com.ledao.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Background userController layer
 *
 * @author LeDao
 * @company
 * @create 2022-01-04 12:13
 */
@RestController
@RequestMapping("/admin/user")
public class UserAdminController {

    @Resource
    private UserService userService;

    /**
     * Query users with paging conditions
     * @param user
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/list")
    public Map<String, Object> list(User user, @RequestParam(value = "page", required = false)
            Integer page, @RequestParam(value = "rows", required = false) Integer rows) {
        Map<String, Object> resultMap = new HashMap<>(16);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (user.getUserName() != null) {
            userQueryWrapper.like("userName", user.getUserName());
        }
        userQueryWrapper.eq("type", 2);
        Page<User> userPage = new Page<>(page, rows);
        List<User> userList = userService.list(userQueryWrapper, userPage);
        Integer total = userService.getCount(userQueryWrapper);
        resultMap.put("rows", userList);
        resultMap.put("total", total);
        return resultMap;
    }

    /**
     * Add or modify users
     * @param user
     * @return
     */
    @RequestMapping("/save")
    public Map<String, Object> save(User user) {
        Map<String, Object> resultMap = new HashMap<>(16);
        int result;
        //ID exists, modify the user
        if (user.getId() != null) {
            user.setType(2);
            user.setStatus(1);
            result = userService.update(user);
        } else {//id does not exist, add user
            user.setType(2);
            user.setStatus(1);
            result = userService.add(user);
        }
        if (result > 0) {
            resultMap.put("success", true);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    /**
     * Ban or unban users
     * @param id
     * @param status
     * @return
     */
    @RequestMapping("/banUser")
    public Map<String, Object> banUser(Integer id, Integer status) {
        Map<String, Object> resultMap = new HashMap<>(16);
        User user = userService.findById(id);
        user.setStatus(status);
        int key = userService.update(user);
        if (key > 0) {
            resultMap.put("success", true);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    /**
     * Delete users, you can delete them in batches
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Map<String, Object> delete(String ids) {
        Map<String, Object> resultMap = new HashMap<>(16);
        String[] idsStr = ids.split(",");
        int key = 0;
        for (int i = 0; i < idsStr.length; i++) {
            userService.delete(Integer.parseInt(idsStr[i]));
            key++;
        }
        if (key > 0) {
            resultMap.put("success", true);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }
}
