package com.ledao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.ledao.entity.*;
import com.ledao.service.*;
import com.ledao.util.DateUtil;
import com.ledao.util.ImageUtil;
import com.ledao.util.PageUtil;
import com.ledao.util.RedisUtil;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.*;

/**
 * home controller
 *
 * @author LeDao
 * @company
 * @create 2022-01-03 14:03
 */
@Controller
public class IndexController {

    @Value("${articleImageFilePath}")
    private String articleImageFilePath;

    @Value("${wantToBuyId}")
    private Integer wantToBuyId;

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

    @Resource
    private MessageService messageService;

    @Resource
    private ReserveRecordService reserveRecordService;

    /**
     * Administrator login
     *
     * @param user
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/login")
    public Map<String, Object> login(User user, HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>(16);
        String checkCode = (String) session.getAttribute("checkCode");
        //Verification code is correct
        if (checkCode.equals(user.getImageCode())) {
        //if (true) {//if(true)是取消验证码
            User currentUser = userService.findByUserName(user.getUserName());
            //when user exists
            if (currentUser != null) {
                //The logged in user is not an administrator
                if (currentUser.getType() == 1) {
                    //User is not banned
                    if (currentUser.getStatus() == 1) {
                        //When the password is correct
                        if (currentUser.getPassword().equals(user.getPassword())) {
                            resultMap.put("success", true);
                            resultMap.put("currentUserType", currentUser.getType());
                            session.setAttribute("currentUserAdmin", currentUser);
                        } else {
                            resultMap.put("success", false);
                            resultMap.put("errorInfo", "The username or password is wrong, " +
                                    "please re-enter!!");
                        }
                    } else {
                        resultMap.put("success", false);
                        resultMap.put("errorInfo", "Your account has been banned. " +
                                "If you want to unban it, please contact the administrator." +
                                "\nThe administrator's email is: 1234567890@qq.com");
                    }
                } else {
                    resultMap.put("success", false);
                    resultMap.put("errorInfo", "Please log in as administrator!!");
                }
            } else {
                resultMap.put("success", false);
                resultMap.put("errorInfo", "The username or password is wrong, please re-enter!!");
            }
        } else {
            resultMap.put("success", false);
            resultMap.put("errorInfo", "The verification code is wrong, please re-enter!!");
        }
        return resultMap;
    }

    /**
     * logout
     *
     * @param session
     * @return
     */
    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("currentUserAdmin");
        return "redirect:/login.html";
    }

    /**
     * Get current logged in user information
     *
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/getUserInfo")
    public Map<String, Object> getUserInfo(HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>(16);
        User currentUser = (User) session.getAttribute("currentUserAdmin");
        if (currentUser != null) {
            resultMap.put("success", true);
            resultMap.put("currentUserAdmin", currentUser);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    /**
     * front page
     *
     * @return
     */
    @RequestMapping("/")
    public ModelAndView root() {
        ModelAndView mav = new ModelAndView();
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
        goodsQueryWrapper.ne("goodsTypeId", wantToBuyId);
        goodsQueryWrapper.eq("state", 1);
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
        goodsQueryWrapper2.ne("goodsTypeId", wantToBuyId);
        goodsQueryWrapper2.eq("state", 1);
        Page<Goods> goodsPage2 = new Page<>(1, 9);
        List<Goods> goodsHotList = goodsService.list(goodsPage2, goodsQueryWrapper2);
        for (Goods goods : goodsHotList) {
            getFirstImageInGoodsContent(goods);
            goods.setGoodsTypeName(goodsTypeService.findById(goods.getGoodsTypeId()).getName());
        }
        mav.addObject("goodsHotList", goodsHotList);
        //Get a list of recommended products
        QueryWrapper<Goods> goodsQueryWrapper3 = new QueryWrapper<>();
        //recommended
        goodsQueryWrapper3.eq("isRecommend", 1);
        //Coming soon
        goodsQueryWrapper3.eq("state", 1);
        //Not buying
        goodsQueryWrapper3.ne("goodsTypeId", wantToBuyId);
        Page<Goods> goodsPage3 = new Page<>(1, 9);
        List<Goods> goodsRecommendList = goodsService.list(goodsPage3, goodsQueryWrapper3);
        for (Goods goods : goodsRecommendList) {
            getFirstImageInGoodsContent(goods);
            goods.setGoodsTypeName(goodsTypeService.findById(goods.getGoodsTypeId()).getName());
        }
        Collections.shuffle(goodsRecommendList);
        mav.addObject("goodsRecommendList", goodsRecommendList);
        mav.addObject("isHome", true);
        mav.addObject("title", "frontPage--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/indexFirst");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Get the first picture from product details
     *
     * @param goods
     */
    public static void getFirstImageInGoodsContent(Goods goods) {

        //Blog content
        String goodsInfo = goods.getContent();
        //Capture content from blogs
        Document document = Jsoup.parse(goodsInfo);
        //Propose .jpg image
        Elements jpgs = document.select("img[src$=.jpg]");
        if (jpgs.size() > 0) {
            String imageName = String.valueOf(jpgs.get(0));
            int begin = imageName.indexOf("/static/images/articleImage/");
            int last = imageName.indexOf(".jpg");
            goods.setImageName(imageName.substring(begin + "/static/images/articleImage/".length(), last));
        } else {
            goods.setImageName("1");
        }
    }

    /**
     * Jump to user login interface
     *
     * @return
     */
    @RequestMapping("/toLoginPage")
    public ModelAndView toLoginPage() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("title", "User login--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/login");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Jump to user registration interface
     *
     * @return
     */
    @RequestMapping("/toRegisterPage")
    public ModelAndView toRegisterPage() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("title",
                "User registration--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/register");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Jump to the password retrieval interface
     *
     * @return
     */
    @RequestMapping("/toResetPasswordPage")
    public ModelAndView toResetPasswordPage() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("title",
                "Retrieve password--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/resetPassword");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Jump to contact us interface
     *
     * @return
     */
    @RequestMapping("/toContactPage")
    public ModelAndView toContactPage(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            ModelAndView mav2 = new ModelAndView("redirect:/toLoginPage");
            return mav2;
        }
        mav.addObject("title",
                "contact us--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/contact");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Jump to personal center interface
     *
     * @return
     */
    @RequestMapping("/toPersonalHubsPage")
    public ModelAndView toPersonalHubsPage(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            ModelAndView mav2 = new ModelAndView("redirect:/toLoginPage");
            return mav2;
        }
        mav.addObject("title",
                "Personal center--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/personalHubs");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Jump to view personal information interface
     *
     * @return
     */
    @RequestMapping("/toPersonalInfoPage")
    public ModelAndView toPersonalInfoPage(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            ModelAndView mav2 = new ModelAndView("redirect:/toLoginPage");
            return mav2;
        }
        mav.addObject("title",
                "Personal center--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/personalInfo");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Jump to product publishing interface
     *
     * @return
     */
    @RequestMapping("/toAddGoodsPage")
    public ModelAndView toAddGoodsPage(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            ModelAndView mav2 = new ModelAndView("redirect:/toLoginPage");
            return mav2;
        }
        QueryWrapper<GoodsType> goodsTypeQueryWrapper = new QueryWrapper<>();
        goodsTypeQueryWrapper.orderByAsc("sortNum");
        List<GoodsType> goodsTypeList = goodsTypeService.list(goodsTypeQueryWrapper);
        mav.addObject("goodsTypeList", goodsTypeList);
        mav.addObject("title", "Post a product--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/addGoods");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Jump to my product management interface
     *
     * @return
     */
    @RequestMapping("/toGoodsManagePage")
    public ModelAndView toGoodsManagePage(HttpSession session, Goods searchGoods) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            ModelAndView mav2 = new ModelAndView("redirect:/toLoginPage");
            return mav2;
        }
        QueryWrapper<Goods> goodsQueryWrapper = new QueryWrapper<>();
        goodsQueryWrapper.eq("userId", currentUser.getId());
        goodsQueryWrapper.orderByDesc("addTime");
        if (searchGoods.getName() != null) {
            goodsQueryWrapper.like("name", searchGoods.getName());
            mav.addObject("name", searchGoods.getName());
        }
        if (searchGoods.getGoodsTypeId() != null) {
            goodsQueryWrapper.eq("goodsTypeId", searchGoods.getGoodsTypeId());
            mav.addObject("goodsTypeId", searchGoods.getGoodsTypeId());
        }
        if (searchGoods.getState() != null) {
            goodsQueryWrapper.eq("state", searchGoods.getState());
            mav.addObject("state", searchGoods.getState());
        }
        if (searchGoods.getIsRecommend() != null) {
            goodsQueryWrapper.eq("isRecommend", searchGoods.getIsRecommend());
            mav.addObject("recommend", searchGoods.getIsRecommend());
        }
        List<Goods> goodsList = goodsService.list(goodsQueryWrapper);
        for (Goods goods : goodsList) {
            goods.setGoodsTypeName(goodsTypeService.findById(goods.getGoodsTypeId()).getName());
        }
        //商品分类列表
        QueryWrapper<GoodsType> goodsTypeQueryWrapper = new QueryWrapper<>();
        goodsTypeQueryWrapper.orderByAsc("sortNum");
        List<GoodsType> goodsTypeList = goodsTypeService.list(goodsTypeQueryWrapper);
        mav.addObject("goodsTypeList", goodsTypeList);
        mav.addObject("goodsList", goodsList);
        mav.addObject("title", "My product management--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/goodsManage");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Jump to my message interface
     *
     * @return
     */
    @RequestMapping("/toMyMessagePage")
    public ModelAndView toMyMessagePage(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            ModelAndView mav2 = new ModelAndView("redirect:/toLoginPage");
            return mav2;
        }
        QueryWrapper<Message> messageQueryWrapper = new QueryWrapper<>();
        messageQueryWrapper.eq("userId", currentUser.getId());
        messageQueryWrapper.orderByDesc("time");
        List<Message> messageList = messageService.list(messageQueryWrapper);
        mav.addObject("messageList", messageList);
        mav.addObject("title", "my message--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myMessage");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * ckeditor upload pictures
     *
     * @param file
     * @param CKEditorFuncNum
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/ckeditorUpload")
    public String ckeditorUpload(@RequestParam("upload") MultipartFile file, String CKEditorFuncNum) throws Exception {
        // Get file name
        String fileName = file.getOriginalFilename();
        // Get file suffix
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //Splice new file name
        String newFileName1 = DateUtil.getCurrentDateStr2() + System.currentTimeMillis() + ".jpg";
        FileUtils.copyInputStreamToFile(file.getInputStream(), new File(articleImageFilePath + "/" + newFileName1));
        //New file name 2
        String newFileName2 = DateUtil.getCurrentDateStr2() + System.currentTimeMillis() + ".jpg";
        //Compress Pictures
        ImageUtil.compressImage(new File(articleImageFilePath + newFileName1), new File(articleImageFilePath + newFileName2));
        StringBuffer sb = new StringBuffer();
        sb.append("<script type=\"text/javascript\">");
        sb.append("window.parent.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ",'" + "/static/images/articleImage/" + newFileName2 + "','')");
        sb.append("</script>");
        return sb.toString();
    }

    /**
     * Jump to purchase page
     *
     * @return
     */
    @RequestMapping("/toWantToBuyPage")
    public ModelAndView toWantToBuyPage() {
        ModelAndView mav = new ModelAndView();
        //Product category list
        QueryWrapper<GoodsType> goodsTypeQueryWrapper = new QueryWrapper<>();
        goodsTypeQueryWrapper.orderByAsc("sortNum");
        List<GoodsType> goodsTypeList = goodsTypeService.list(goodsTypeQueryWrapper);
        for (int i = 0; i < goodsTypeList.size(); i++) {
            if (wantToBuyId.equals(goodsTypeList.get(i).getId())) {
                goodsTypeList.remove(goodsTypeList.get(i));
                i--;
            }
        }
        mav.addObject("goodsTypeList", goodsTypeList);
        //Get a list of recommended products
        QueryWrapper<Goods> goodsQueryWrapper3 = new QueryWrapper<>();
        goodsQueryWrapper3.eq("state", 1);
        goodsQueryWrapper3.eq("isRecommend", 1);
        Page<Goods> goodsPage3 = new Page<>(1, 9);
        List<Goods> goodsRecommendList = goodsService.list(goodsPage3, goodsQueryWrapper3);
        for (Goods goods2 : goodsRecommendList) {
            getFirstImageInGoodsContent(goods2);
            goods2.setGoodsTypeName(goodsTypeService.findById(goods2.getGoodsTypeId()).getName());
        }
        Collections.shuffle(goodsRecommendList);
        mav.addObject("goodsRecommendList", goodsRecommendList);
        //Get a want list
        QueryWrapper<Goods> goodsQueryWrapper2 = new QueryWrapper<>();
        goodsQueryWrapper2.eq("goodsTypeId", wantToBuyId);
        goodsQueryWrapper2.eq("state", 1);
        goodsQueryWrapper2.orderByDesc("addTime");
        List<Goods> goodsWantToBuyList = goodsService.list(goodsQueryWrapper2);
        mav.addObject("isWantToBuy", true);
        mav.addObject("goodsWantToBuyList", goodsWantToBuyList);
        mav.addObject("title", "Users want to buy--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/wantToBuy");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Jump to category page
     *
     * @return
     */
    @RequestMapping("/toSortPage")
    public ModelAndView toSortPage(Integer goodsTypeId, Integer page) {
        //Number of products displayed per page
        int pageSize = 9;
        if (page == null) {
            page = 1;
        }
        ModelAndView mav = new ModelAndView();
        //Get product list
        QueryWrapper<Goods> goodsQueryWrapper = new QueryWrapper<>();
        goodsQueryWrapper.eq("goodsTypeId", goodsTypeId);
        goodsQueryWrapper.eq("state", 1);
        Page<Goods> goodsPage = new Page<>(page, pageSize);
        List<Goods> goodsList = goodsService.list(goodsPage, goodsQueryWrapper);
        for (Goods goods : goodsList) {
            getFirstImageInGoodsContent(goods);
        }
        mav.addObject("goodsList", goodsList);
        //Product category list
        QueryWrapper<GoodsType> goodsTypeQueryWrapper = new QueryWrapper<>();
        goodsTypeQueryWrapper.orderByAsc("sortNum");
        List<GoodsType> goodsTypeList = goodsTypeService.list(goodsTypeQueryWrapper);
        for (int i = 0; i < goodsTypeList.size(); i++) {
            if (wantToBuyId.equals(goodsTypeList.get(i).getId())) {
                goodsTypeList.remove(goodsTypeList.get(i));
                i--;
            }
        }
        mav.addObject("goodsTypeList", goodsTypeList);
        //获取推荐商品列表
        QueryWrapper<Goods> goodsQueryWrapper3 = new QueryWrapper<>();
        goodsQueryWrapper3.eq("isRecommend", 1);
        goodsQueryWrapper3.eq("state", 1);
        Page<Goods> goodsPage3 = new Page<>(1, 9);
        List<Goods> goodsRecommendList = goodsService.list(goodsPage3, goodsQueryWrapper3);
        for (Goods goods2 : goodsRecommendList) {
            getFirstImageInGoodsContent(goods2);
            goods2.setGoodsTypeName(goodsTypeService.findById(goods2.getGoodsTypeId()).getName());
        }
        Collections.shuffle(goodsRecommendList);
        mav.addObject("goodsRecommendList", goodsRecommendList);
        mav.addObject("isSort", true);
        mav.addObject("goodsTypeName", goodsTypeService.findById(goodsTypeId).getName());
        StringBuilder param = new StringBuilder();
        param.append("&goodsTypeId=").append(goodsTypeId);
        mav.addObject("pageCode", PageUtil.genPagination1("/toSortPage",
                goodsService.getCount(goodsQueryWrapper), page, pageSize, param.toString()));
        mav.addObject("title",
                "classification--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/sortPage");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Jump to my shopping cart interface
     * @param session
     * @return
     */
    @RequestMapping("/toMyShoppingCart")
    public ModelAndView toMyShoppingCart(HttpSession session) {
        ModelAndView mav = new ModelAndView();
        Gson gson = new Gson();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            ModelAndView mav2 = new ModelAndView("redirect:/toLoginPage");
            return mav2;
        }
        String shoppingCartName = currentUser.getId() + "_shoppingCart";
        List<String> shoppingCartGoodsStr = RedisUtil.listRange(shoppingCartName, 0L, -1L);
        List<Goods> shoppingCartGoodsList = new ArrayList<>();
        if (shoppingCartGoodsStr.size()>0) {
            for (String s : shoppingCartGoodsStr) {
                Goods goods = gson.fromJson(s, Goods.class);
                shoppingCartGoodsList.add(goods);
                getFirstImageInGoodsContent(goods);
            }
        }
        Iterator<Goods> iterator = shoppingCartGoodsList.iterator();
        while (iterator.hasNext()) {
            Goods goods = goodsService.findById(iterator.next().getId());
            if (goods == null) {
                iterator.remove();
            }
        }
        mav.addObject("shoppingCartGoodsList", shoppingCartGoodsList);
        mav.addObject("shoppingCartGoodsListSize", shoppingCartGoodsList.size());
        //商品分类列表
        QueryWrapper<GoodsType> goodsTypeQueryWrapper = new QueryWrapper<>();
        goodsTypeQueryWrapper.orderByAsc("sortNum");
        List<GoodsType> goodsTypeList = goodsTypeService.list(goodsTypeQueryWrapper);
        for (int i = 0; i < goodsTypeList.size(); i++) {
            if (wantToBuyId.equals(goodsTypeList.get(i).getId())) {
                goodsTypeList.remove(goodsTypeList.get(i));
                i--;
            }
        }
        mav.addObject("goodsTypeList", goodsTypeList);
        //Get a list of recommended products
        QueryWrapper<Goods> goodsQueryWrapper3 = new QueryWrapper<>();
        goodsQueryWrapper3.eq("isRecommend", 1);
        goodsQueryWrapper3.eq("state", 1);
        Page<Goods> goodsPage3 = new Page<>(1, 9);
        List<Goods> goodsRecommendList = goodsService.list(goodsPage3, goodsQueryWrapper3);
        for (Goods goods2 : goodsRecommendList) {
            getFirstImageInGoodsContent(goods2);
            goods2.setGoodsTypeName(goodsTypeService.findById(goods2.getGoodsTypeId()).getName());
        }
        Collections.shuffle(goodsRecommendList);
        mav.addObject("goodsRecommendList", goodsRecommendList);
        mav.addObject("title", "my shopping cart--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myShoppingCart");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }


    /**
     * Jump to my order interface
     *
     * @param session
     * @param searchReserveRecord
     * @return
     */
    @RequestMapping("/toMyReserveRecordPage")
    public ModelAndView toMyReserveRecordPage(HttpSession session, ReserveRecord searchReserveRecord) {
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            ModelAndView mav2 = new ModelAndView("redirect:/toLoginPage");
            return mav2;
        }
        QueryWrapper<ReserveRecord> reserveRecordQueryWrapper = new QueryWrapper<>();
        if (searchReserveRecord.getState() != null) {
            reserveRecordQueryWrapper.eq("state", searchReserveRecord.getState());
            mav.addObject("state", searchReserveRecord.getState());
        }
        if (searchReserveRecord.getGoodsName() != null) {
            List<Integer> goodsIdList = new ArrayList<>();
            QueryWrapper<Goods> goodsQueryWrapper = new QueryWrapper<>();
            goodsQueryWrapper.like("name", searchReserveRecord.getGoodsName());
            mav.addObject("goodsName", searchReserveRecord.getGoodsName());
            List<Goods> goodsList = goodsService.list(goodsQueryWrapper);
            if (goodsList.size() > 0) {
                for (Goods goods : goodsList) {
                    goodsIdList.add(goods.getId());
                }
            } else {
                goodsIdList.add(-1);
            }
            reserveRecordQueryWrapper.in("goodsId", goodsIdList);
        }
        reserveRecordQueryWrapper.eq("userId", currentUser.getId());
        reserveRecordQueryWrapper.orderByDesc("reserveTime");
        List<ReserveRecord> reserveRecordList = reserveRecordService.list(reserveRecordQueryWrapper);
        for (ReserveRecord reserveRecord : reserveRecordList) {
            reserveRecord.setGoodsName(goodsService.findById(reserveRecord.getGoodsId()).getName());
        }
        mav.addObject("reserveRecordList", reserveRecordList);
        mav.addObject("title", "My Order--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/myReserveRecord");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    @ResponseBody
    @RequestMapping("/setRedisKey")
    public String setRedisKey() {
        RedisUtil.setKey("a", "1");
        RedisUtil.setKeyTime("a", 10);
        System.out.println(new Date() + ": " + "The key is set and the expiration time is" + 10 + "seconds");
        return "Setup successful";
    }

    /**
     * Jump to test interface
     *
     * @return
     */
    @RequestMapping("/toTestPage")
    public ModelAndView toTestPage() {
        ModelAndView mav = new ModelAndView();
        List<GoodsType> goodsTypeList = goodsTypeService.list(null);
        mav.addObject("goodsTypeList", goodsTypeList);
        mav.addObject("title", "Test interface--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/test");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Get product collection based on product category id
     *
     * @param goodsTypeId
     * @return
     */
    @ResponseBody
    @RequestMapping("/getGoodsListByGoodsTypeId")
    public List<Goods> getGoodsListByGoodsTypeId(Integer goodsTypeId) {
        QueryWrapper<Goods> goodsQueryWrapper = new QueryWrapper<>();
        goodsQueryWrapper.eq("goodsTypeId", goodsTypeId);
        return goodsService.list(goodsQueryWrapper);
    }
}
