package com.ledao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledao.entity.ContactInformation;
import com.ledao.entity.User;
import com.ledao.service.ContactInformationService;
import com.ledao.service.GoodsService;
import com.ledao.service.ReserveRecordService;
import com.ledao.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.naming.Name;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contact Controller layer
 *
 * @author LeDao
 * @company
 * @create 2022-01-12 17:21
 */
@Controller
@RequestMapping("/contactInformation")
public class ContactInformationController {

    @Resource
    private ContactInformationService contactInformationService;

    @Resource
    private GoodsService goodsService;

    @Resource
    private UserService userService;

    @Resource
    private ReserveRecordService reserveRecordService;

    /**
     * Add or modify contact information
     *
     * @param contactInformation
     * @return
     */
    @RequestMapping("/save")
    public ModelAndView save(ContactInformation contactInformation, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        if (contactInformation.getId() == null) {
            int key = contactInformationService.add(contactInformation);
            if (key > 0) {
                mav.addObject("addContactInformationSuccess", true);
            } else {
                mav.addObject("addContactInformationSuccess", false);
            }
        } else {
            int key = contactInformationService.update(contactInformation);
            if (key > 0) {
                mav.addObject("updateContactInformationSuccess", true);
            } else {
                mav.addObject("updateContactInformationSuccess", false);
            }
        }
        QueryWrapper<ContactInformation> contactInformationQueryWrapper = new QueryWrapper<>();
        contactInformationQueryWrapper.eq("userId", currentUser.getId());
        Page<ContactInformation> contactInformationPage = new Page<>(1, 100);
        List<ContactInformation> contactInformationList = contactInformationService.list(contactInformationQueryWrapper, contactInformationPage);
        mav.addObject("contactInformationList", contactInformationList);
        mav.addObject("title", "Contact information--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myContactInformation");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Jump to my contact interface
     *
     * @return
     */
    //http://localhost/contactInformation/toMyContactInformationPage
    @RequestMapping("/toMyContactInformationPage")
    public ModelAndView toMyContactInformation(ContactInformation contactInformation, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        QueryWrapper<ContactInformation> contactInformationQueryWrapper = new QueryWrapper<>();
        contactInformationQueryWrapper.eq("userId", currentUser.getId());
        if (contactInformation.getName() != null) {
            contactInformationQueryWrapper.like("name", contactInformation.getName());
        }
        Page<ContactInformation> contactInformationPage = new Page<>(1, 100);
        List<ContactInformation> contactInformationList = contactInformationService.list(contactInformationQueryWrapper, contactInformationPage);
        mav.addObject("name", contactInformation.getName());
        mav.addObject("contactInformationList", contactInformationList);
        mav.addObject("title", "Contact information--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myContactInformation");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    @RequestMapping("/delete")
    public ModelAndView delete(Integer id, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        int key = contactInformationService.deleteById(id);
        if (key > 0) {
            mav.addObject("deleteContactInformation", true);
        } else {
            mav.addObject("deleteContactInformation", false);
        }
        QueryWrapper<ContactInformation> contactInformationQueryWrapper = new QueryWrapper<>();
        contactInformationQueryWrapper.eq("userId", currentUser.getId());
        Page<ContactInformation> contactInformationPage = new Page<>(1, 100);
        List<ContactInformation> contactInformationList = contactInformationService.list(contactInformationQueryWrapper, contactInformationPage);
        mav.addObject("contactInformationList", contactInformationList);
        mav.addObject("title", "Contact information--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myContactInformation");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Check whether the user's contact name already exists
     *
     * @param name
     * @param userId
     * @return
     */
    @ResponseBody
    @RequestMapping("/checkSaveContactInformationName")
    public Map<String, Object> checkSaveContactInformationName(String name, Integer userId) {
        Map<String, Object> resultMap = new HashMap<>(16);
        ContactInformation contactInformation = contactInformationService.findByNameAndUserId(name, userId);
        if (contactInformation != null) {
            resultMap.put("success", true);
            resultMap.put("name", name);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    /**
     * Find contact information based on id
     *
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/findById")
    public Map<String, Object> findById(Integer id) {
        Map<String, Object> resultMap = new HashMap<>(16);
        ContactInformation contactInformation = contactInformationService.findById(id);
        if (contactInformation != null) {
            resultMap.put("success", true);
            resultMap.put("contactInformation", contactInformation);
        }
        return resultMap;
    }

    /**
     * Get contact information based on product id
     *
     * @param goodsId
     * @return
     */
    @ResponseBody
    @RequestMapping("/getListByGoodsId")
    public Map<String, Object> getListByGoodsId(Integer goodsId) {
        Map<String, Object> resultMap = new HashMap<>(16);
        QueryWrapper<ContactInformation> contactInformationQueryWrapper = new QueryWrapper<>();
        contactInformationQueryWrapper.eq("userId", reserveRecordService.findByGoodsId(goodsId).getUserId());
        List<ContactInformation> contactInformationList = contactInformationService.list(contactInformationQueryWrapper);
        StringBuilder contactInformationStr = new StringBuilder();
        if (contactInformationList.size() > 0) {
            for (ContactInformation contactInformation : contactInformationList) {
                contactInformationStr.append(contactInformation.getName()).append("：").append(contactInformation.getContent()).append("；");
            }
        } else {
            contactInformationStr.append("Buyer's email：").append(userService.findById(reserveRecordService.findByGoodsId(goodsId).getUserId()).getEmail());
        }
        resultMap.put("success", true);
        resultMap.put("contactInformationStr", contactInformationStr);
        return resultMap;
    }
}
