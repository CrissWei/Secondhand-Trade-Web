package com.ledao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledao.entity.Contact;
import com.ledao.entity.ContactInformation;
import com.ledao.entity.User;
import com.ledao.service.ContactService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MessageController layer
 *
 * @author LeDao
 * @company
 * @create 2022-01-12 13:53
 */
//http://localhost/contact/toMyContactPage
@Controller
@RequestMapping("/contact")
public class ContactController {

    @Resource
    private ContactService contactService;

    /**
     * Jump to my message interface
     *
     * @return
     */
    @RequestMapping("/toMyContactPage")
    public ModelAndView toMyContactPage(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        QueryWrapper<Contact> contactQueryWrapper = new QueryWrapper<>();
        contactQueryWrapper.orderByDesc("time");
        contactQueryWrapper.eq("userId", currentUser.getId());
        Page<Contact> contactPage = new Page<>(1, 10);
        List<Contact> contactList = contactService.list(contactQueryWrapper, contactPage);
        mav.addObject("contactList", contactList);
        mav.addObject("title",
                "my message--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myContact");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Add or modify a message
     *
     * @param contact
     * @re
     */
    @RequestMapping("/save")
    public ModelAndView save(Contact contact, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        //Added when id is null
        if (contact.getId() == null) {
            contact.setTime(new Date());
            contactService.add(contact);
            mav.addObject("contactAddSuccess", true);
        } else {
            Contact trueContact = contactService.findById(contact.getId());
            if (contact.getContent() != null) {
                trueContact.setReply(null);
            }
            trueContact.setContent(contact.getContent());
            contactService.update(trueContact);
            mav.addObject("contactModifySuccess", true);
        }
        QueryWrapper<Contact> contactQueryWrapper = new QueryWrapper<>();
        contactQueryWrapper.eq("userId", currentUser.getId());
        contactQueryWrapper.orderByDesc("time");
        Page<Contact> contactPage = new Page<>(1, 10);
        List<Contact> contactList = contactService.list(contactQueryWrapper, contactPage);
        mav.addObject("contactList", contactList);
        mav.addObject("title", "my message--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myContact");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Delete message based on id
     *
     * @param id
     * @return
     */
    @RequestMapping("/delete")
    public ModelAndView delete(Integer id, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        int key = contactService.delete(id);
        QueryWrapper<Contact> contactQueryWrapper = new QueryWrapper<>();
        contactQueryWrapper.orderByDesc("time");
        contactQueryWrapper.eq("userId", currentUser.getId());
        Page<Contact> contactPage = new Page<>(1, 10);
        List<Contact> contactList = contactService.list(contactQueryWrapper, contactPage);
        mav.addObject("contactList", contactList);
        mav.addObject("title", "my message--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myContact");
        mav.addObject("mainPageKey", "#b");
        //删除成功时
        if (key > 0) {
            mav.addObject("deleteContactSuccess", true);
        } else {
            mav.addObject("deleteContactSuccess", false);
        }
        mav.setViewName("index");
        return mav;
    }

    /**
     * View message details
     *
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/findById")
    public Map<String, Object> findById(Integer id) {
        Map<String, Object> resultMap = new HashMap<>(16);
        Contact contact = contactService.findById(id);
        if (contact != null) {
            resultMap.put("success", true);
            resultMap.put("contact", contact);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }
}
