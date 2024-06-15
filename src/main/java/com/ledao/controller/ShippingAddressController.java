package com.ledao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledao.entity.ShippingAddress;
import com.ledao.entity.User;
import com.ledao.service.ShippingAddressService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LeDao
 * @company
 * @createDate 2024-05-11 22:38
 */
@Controller
@RequestMapping("/shippingAddress")
public class ShippingAddressController {

    @Resource
    private ShippingAddressService shippingAddressService;

    @RequestMapping("/toMyShippingAddressPage")
    public ModelAndView toMyShippingAddress(ShippingAddress shippingAddress, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        QueryWrapper<ShippingAddress> shippingAddressQueryWrapper = new QueryWrapper<>();
        shippingAddressQueryWrapper.eq("userId", currentUser.getId());
        shippingAddressQueryWrapper.orderByDesc("useOrNot").orderByDesc("addDate");
        if (shippingAddress.getContent() != null) {
            shippingAddressQueryWrapper.like("content", shippingAddress.getContent());
        }
        Page<ShippingAddress> shippingAddressPage = new Page<>(1, 100);
        List<ShippingAddress> shippingAddressList = shippingAddressService.list(shippingAddressQueryWrapper, shippingAddressPage);
        mav.addObject("content", shippingAddress.getContent());
        mav.addObject("shippingAddressList", shippingAddressList);
        mav.addObject("title", "Contact information--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myShippingAddress");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    @RequestMapping("/save")
    public ModelAndView save(ShippingAddress shippingAddress, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        if (shippingAddress.getId() == null) {
            QueryWrapper<ShippingAddress> shippingAddressQueryWrapper2 = new QueryWrapper<>();
            shippingAddressQueryWrapper2.eq("userId", currentUser.getId());
            if (shippingAddressService.getCount(shippingAddressQueryWrapper2) > 0) {
                shippingAddress.setUseOrNot(0);
            } else {
                shippingAddress.setUseOrNot(1);
            }
            shippingAddress.setAddDate(LocalDateTime.now());
            int key = shippingAddressService.add(shippingAddress);
            if (key > 0) {
                mav.addObject("addShippingAddressSuccess", true);
            } else {
                mav.addObject("addShippingAddressSuccess", false);
            }
        } else {
            int key = shippingAddressService.update(shippingAddress);
            if (key > 0) {
                mav.addObject("updateShippingAddressSuccess", true);
            } else {
                mav.addObject("updateShippingAddressSuccess", false);
            }
        }
        QueryWrapper<ShippingAddress> shippingAddressQueryWrapper = new QueryWrapper<>();
        shippingAddressQueryWrapper.eq("userId", currentUser.getId());
        shippingAddressQueryWrapper.orderByDesc("useOrNot").orderByDesc("addDate");
        Page<ShippingAddress> shippingAddressPage = new Page<>(1, 100);
        List<ShippingAddress> shippingAddressList = shippingAddressService.list(shippingAddressQueryWrapper, shippingAddressPage);
        mav.addObject("shippingAddressList", shippingAddressList);
        mav.addObject("title", "Contact information--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myShippingAddress");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    @RequestMapping("/delete")
    public ModelAndView delete(Integer id, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        int key = shippingAddressService.deleteById(id);
        if (key > 0) {
            mav.addObject("deleteShippingAddress", true);
            QueryWrapper<ShippingAddress> shippingAddressQueryWrapper = new QueryWrapper<>();
            shippingAddressQueryWrapper.eq("userId", currentUser.getId());
            List<ShippingAddress> shippingAddressList = shippingAddressService.list(shippingAddressQueryWrapper);
            if (shippingAddressList.size() > 0) {
                ShippingAddress shippingAddress = shippingAddressList.get(0);
                shippingAddress.setUseOrNot(1);
                shippingAddressService.update(shippingAddress);
            }
        } else {
            mav.addObject("deleteShippingAddress", false);
        }
        QueryWrapper<ShippingAddress> shippingAddressQueryWrapper = new QueryWrapper<>();
        shippingAddressQueryWrapper.eq("userId", currentUser.getId());
        shippingAddressQueryWrapper.orderByDesc("useOrNot").orderByDesc("addDate");
        Page<ShippingAddress> shippingAddressPage = new Page<>(1, 100);
        List<ShippingAddress> shippingAddressList = shippingAddressService.list(shippingAddressQueryWrapper, shippingAddressPage);
        mav.addObject("shippingAddressList", shippingAddressList);
        mav.addObject("title", "Contact information--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myShippingAddress");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    @ResponseBody
    @RequestMapping("/findById")
    public Map<String, Object> findById(Integer id) {
        Map<String, Object> resultMap = new HashMap<>(16);
        ShippingAddress shippingAddress = shippingAddressService.findById(id);
        if (shippingAddress != null) {
            resultMap.put("success", true);
            resultMap.put("shippingAddress", shippingAddress);
        }
        return resultMap;
    }

    @RequestMapping("/setDefaultShippingAddress")
    public ModelAndView setDefaultShippingAddress(Integer id, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        QueryWrapper<ShippingAddress> shippingAddressQueryWrapper = new QueryWrapper<>();
        shippingAddressQueryWrapper.eq("userId", currentUser.getId());
        shippingAddressQueryWrapper.orderByDesc("useOrNot").orderByDesc("addDate");
        Page<ShippingAddress> shippingAddressPage = new Page<>(1, 100);
        List<ShippingAddress> shippingAddressList = shippingAddressService.list(shippingAddressQueryWrapper, shippingAddressPage);
        for (ShippingAddress shippingAddress : shippingAddressList) {
            shippingAddress.setUseOrNot(0);
            shippingAddressService.update(shippingAddress);
        }
        ShippingAddress shippingAddress = shippingAddressService.findById(id);
        shippingAddress.setUseOrNot(1);
        int key = shippingAddressService.update(shippingAddress);
        if (key > 0) {
            mav.addObject("setDefaultShippingAddressSuccess", true);
        } else {
            mav.addObject("setDefaultShippingAddressSuccess", false);
        }
        List<ShippingAddress> shippingAddressList2 = shippingAddressService.list(shippingAddressQueryWrapper, shippingAddressPage);
        mav.addObject("shippingAddressList", shippingAddressList2);
        mav.addObject("title", "Contact information--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myShippingAddress");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }
}
