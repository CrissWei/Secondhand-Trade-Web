package com.ledao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledao.entity.*;
import com.ledao.service.*;
import com.ledao.util.DateUtil;
import com.ledao.util.ImageUtil;
import com.ledao.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ledao.controller.IndexController.getFirstImageInGoodsContent;

/**
 * Front-end userController layer
 *
 * @author LeDao
 * @company
 * @create 2022-01-09 14:00
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    private ConfigProperties configProperties;

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private UserService userService;

    @Resource
    private CarouselService carouselService;

    @Resource
    private AnnouncementService announcementService;

    @Resource
    private GoodsTypeService goodsTypeService;

    @Resource
    private GoodsService goodsService;

    /**
     * Front desk user login
     *
     * @param user
     * @param session
     * @return
     */
    @RequestMapping("/login")
    public ModelAndView login(User user, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = userService.findByUserName(user.getUserName());
        //when user exists
        if (currentUser != null) {
            //The logged in user identity is not an ordinary user but an administrator
            if (currentUser.getType() != 2) {
                mav.addObject("userNameLogin", user.getUserName());
                mav.addObject("passwordLogin", user.getPassword());
                mav.addObject("title", "User login--Campus second-hand trading platform");
                mav.addObject("mainPage", "page/login");
                mav.addObject("isUserOrNot", false);
            }else {
                //When the password is correct
                if (user.getPassword().equals(currentUser.getPassword())) {
                    session.setAttribute("currentUser", currentUser);
                    //Get carousel image list
                    QueryWrapper<Carousel> carouselQueryWrapper = new QueryWrapper<>();
                    carouselQueryWrapper.orderByAsc("sortNum");
                    List<Carousel> carouselList = carouselService.list(carouselQueryWrapper);
                    mav.addObject("carouselList", carouselList);
                    //Get announcement list
                    QueryWrapper<Announcement> announcementQueryWrapper = new QueryWrapper<>();
                    announcementQueryWrapper.orderByAsc("sortNum");
                    List<Announcement> announcementList = announcementService.list(announcementQueryWrapper);
                    mav.addObject("announcementList", announcementList);
                    //Get 9 recently released products
                    QueryWrapper<Goods> goodsQueryWrapper = new QueryWrapper<>();
                    goodsQueryWrapper.orderByDesc("addTime");
                    goodsQueryWrapper.eq("state", 1);
                    goodsQueryWrapper.ne("goodsTypeId", configProperties.getWantToBuyId());
                    Page<Goods> goodsPage = new Page<>(1, 9);
                    List<Goods> goodsNewList = goodsService.list(goodsPage, goodsQueryWrapper);
                    for (Goods goods : goodsNewList) {
                        getFirstImageInGoodsContent(goods);
                        goods.setGoodsTypeName(goodsTypeService.findById(goods.getGoodsTypeId()).getName());
                    }
                    mav.addObject("goodsNewList", goodsNewList);
                    //Get 9 popular items
                    QueryWrapper<Goods> goodsQueryWrapper2 = new QueryWrapper<>();
                    goodsQueryWrapper2.orderByDesc("click");
                    goodsQueryWrapper2.eq("state", 1);
                    goodsQueryWrapper2.ne("goodsTypeId", configProperties.getWantToBuyId());
                    Page<Goods> goodsPage2 = new Page<>(1, 9);
                    List<Goods> goodsHotList = goodsService.list(goodsPage2, goodsQueryWrapper2);
                    for (Goods goods : goodsHotList) {
                        getFirstImageInGoodsContent(goods);
                        goods.setGoodsTypeName(goodsTypeService.findById(goods.getGoodsTypeId()).getName());
                    }
                    mav.addObject("goodsHotList", goodsHotList);
                    //Get a list of recommended products
                    QueryWrapper<Goods> goodsQueryWrapper3 = new QueryWrapper<>();
                    goodsQueryWrapper3.eq("isRecommend", 1);
                    goodsQueryWrapper3.ne("goodsTypeId", configProperties.getWantToBuyId());
                    goodsQueryWrapper3.eq("state", 1);
                    Page<Goods> goodsPage3 = new Page<>(1, 9);
                    List<Goods> goodsRecommendList = goodsService.list(goodsPage3, goodsQueryWrapper3);
                    for (Goods goods : goodsRecommendList) {
                        getFirstImageInGoodsContent(goods);
                        goods.setGoodsTypeName(goodsTypeService.findById(goods.getGoodsTypeId()).getName());
                    }
                    Collections.shuffle(goodsRecommendList);
                    mav.addObject("goodsRecommendList", goodsRecommendList);
                    mav.addObject("title", "front page--Campus second-hand trading platform");
                    mav.addObject("mainPage", "page/indexFirst");
                    mav.addObject("loginSuccess", true);
                } else {
                    mav.addObject("userNameLogin", user.getUserName());
                    mav.addObject("passwordLogin", user.getPassword());
                    mav.addObject("title", "User login--Campus second-hand trading platform");
                    mav.addObject("mainPage", "page/login");
                    mav.addObject("loginSuccess", false);
                }
            }
        } else {
            mav.addObject("userNameLogin", user.getUserName());
            mav.addObject("passwordLogin", user.getPassword());
            mav.addObject("title", "User login--Campus second-hand trading platform");
            mav.addObject("mainPage", "page/login");
            mav.addObject("loginSuccess", false);
        }
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * User logout
     *
     * @param session
     * @param response
     * @throws IOException
     */
    @RequestMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response) throws IOException {
        session.removeAttribute("currentUser");
        response.sendRedirect("/toLoginPage");
    }

    /**
     * Add or modify users
     *
     * @param user
     * @param file
     * @param session
     * @return
     * @throws Exception
     */
    @RequestMapping("/save")
    public ModelAndView save(User user, @RequestParam("userImage") MultipartFile file, HttpSession session) throws Exception {
        //When the uploaded image exists
        if (!file.isEmpty()) {
            //When modifying the user, delete the original avatar
            if (user.getId() != null) {
                FileUtils.deleteQuietly(new File(configProperties.getUserImageFilePath() + userService.findById(user.getId()).getImageName()));
            }
            //Get the uploaded file name
            String fileName = file.getOriginalFilename();
            //Get file suffix
            String suffixName = null;
            if (fileName != null) {
                suffixName = fileName.split("\\.")[1];
            }
            //New file name 1
            String newFileName1 = DateUtil.getCurrentDateStr2() + System.currentTimeMillis() + "." + suffixName;
            //upload
            FileUtils.copyInputStreamToFile(file.getInputStream(), new File(configProperties.getUserImageFilePath() + newFileName1));
            //New file name 2
            String newFileName2 = DateUtil.getCurrentDateStr2() + System.currentTimeMillis() + "." + suffixName;
            //Compress Pictures
            ImageUtil.compressImage(new File(configProperties.getUserImageFilePath() + newFileName1), new File(configProperties.getUserImageFilePath() + newFileName2));
            user.setImageName(newFileName2);
        }
        //When adding a user
        if (user.getId() == null) {
            user.setStatus(1);
            user.setType(2);
            userService.add(user);
            ModelAndView mav = new ModelAndView();
            mav.addObject("successRegister", true);
            mav.addObject("title", "User login");
            mav.addObject("mainPage", "page/login");
            mav.addObject("mainPageKey", "#b");
            mav.setViewName("index");
            return mav;
        } else {
            User trueUser = userService.findById(user.getId());
            trueUser.setPassword(user.getPassword());
            trueUser.setNickName(user.getNickName());
            if (!file.isEmpty()) {
                trueUser.setImageName(user.getImageName());
            }
            userService.update(trueUser);
            ModelAndView mav = new ModelAndView();
            session.setAttribute("currentUser", trueUser);
            mav.addObject("title", "Personal center--Campus second-hand trading platform");
            mav.addObject("mainPage", "page/personalInfo");
            mav.addObject("mainPageKey", "#b");
            mav.addObject("modifyUserSuccess", true);
            mav.setViewName("index");
            return mav;
        }
    }

    /**
     * Determine whether the username already exists when the user registers
     *
     * @param userName
     * @return
     */
    @ResponseBody
    @RequestMapping("/existUserWithUserName")
    public Map<String, Object> existUserWithUserName(String userName) {
        Map<String, Object> resultMap = new HashMap<>(16);
        User user = userService.findByUserName(userName);
        if (user != null) {
            resultMap.put("success", true);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    /**
     * Determine whether the mailbox exists in the database
     *
     * @param email
     * @return
     */
    @ResponseBody
    @RequestMapping("/existEmail")
    public Map<String, Object> existEmail(String email) {
        Map<String, Object> resultMap = new HashMap<>(16);
        User user = userService.findByEmail(email);
        if (user != null) {
            resultMap.put("success", true);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    /**
     * Get verification code when registering or retrieving password
     *
     * @param session
     * @param email
     * @param type
     * @return
     */
    @ResponseBody
    @RequestMapping("/getVerificationCode")
    public Map<String, Object> getVerificationCode(HttpSession session, String email, Integer type) {
        Map<String, Object> resultMap = new HashMap<>(16);
        //Generate 6-digit verification code
        String registerCode = StringUtil.genSixRandomNum();
        SimpleMailMessage message = new SimpleMailMessage();
        //Sender's QQ email
        message.setFrom(configProperties.getSendMailPerson());
        //Recipient email
        message.setTo(email);
        //Email Subject
        message.setSubject("Email from the campus second-hand trading platform");
        //1 is for registration, 2 is for retrieving password
        int codeTypeRegister = 1, codeTypeResetPassword = 2;
        if (type == codeTypeRegister) {
            //content of email
            message.setText("The verification code for registration is:" + registerCode);
            session.setAttribute("registerCode", registerCode);
        } else if (type == codeTypeResetPassword) {
            //content of email
            message.setText("The verification code to retrieve your password is:" + registerCode);
            session.setAttribute("resetPasswordCode", registerCode);
        }
        //send email
        javaMailSender.send(message);
        resultMap.put("success", true);
        return resultMap;
    }

    /**
     * Get the registration verification code in the session
     *
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/getRegisterCode")
    public Map<String, Object> getRegisterCode(HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>(16);
        String registerCode = (String) session.getAttribute("registerCode");
        System.out.println(registerCode);
        if (registerCode != null) {
            resultMap.put("imageCode", registerCode);
            resultMap.put("success", true);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    /**
     * Get the password retrieval verification code in the session
     *
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/getResetPasswordCode")
    public Map<String, Object> getResetPasswordCode(HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>(16);
        String resetPasswordCode = (String) session.getAttribute("resetPasswordCode");
        System.out.println(resetPasswordCode);
        if (resetPasswordCode != null) {
            resultMap.put("imageCode", resetPasswordCode);
            resultMap.put("success", true);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    @ResponseBody
    @RequestMapping("/resetPassword")
    public ModelAndView resetPassword(User user) {
        ModelAndView mav = new ModelAndView();
        User resetPasswordUser = userService.findByEmail(user.getEmail());
        resetPasswordUser.setPassword(user.getPassword());
        userService.update(resetPasswordUser);
        mav.addObject("successResetPassword", true);
        mav.addObject("title", "User login");
        mav.addObject("mainPage", "page/login");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Verify user status before logging in
     *
     * @param userName
     * @return
     */
    @ResponseBody
    @RequestMapping("/checkLoginUserState")
    public Map<String, Object> checkLoginUserState(String userName) {
        Map<String, Object> resultMap = new HashMap<>(16);
        User user = userService.findByUserName(userName);
        if (user != null) {
            resultMap.put("success", true);
            resultMap.put("status", user.getStatus());
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }
}
