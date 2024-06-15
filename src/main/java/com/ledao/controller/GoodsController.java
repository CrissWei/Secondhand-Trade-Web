package com.ledao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.ledao.entity.*;
import com.ledao.entity.Goods;
import com.ledao.service.*;
import com.ledao.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.*;

import static com.ledao.controller.IndexController.getFirstImageInGoodsContent;

/**
 * Front desk product Controller layer
 *
 * @author LeDao
 * @company
 * @create 2022-01-15 22:20
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Value("${wantToBuyId}")
    private Integer wantToBuyId;

    @Resource
    private GoodsService goodsService;

    @Resource
    private GoodsTypeService goodsTypeService;

    @Resource
    private UserService userService;

    @Resource
    private ContactInformationService contactInformationService;

    @Resource
    private ReserveRecordService reserveRecordService;

    @Resource
    private MessageService messageService;

    @Resource
    private ShippingAddressService shippingAddressService;

    /**
     * View product details
     *
     * @param id
     * @return
     */
    @RequestMapping("/{id}")
    public ModelAndView details(@PathVariable("id") Integer id) {
        ModelAndView mav = new ModelAndView();
        //Products to display
        Goods goods = goodsService.findById(id);
        goods.setClick(goods.getClick() + 1);
        goods.setGoodsTypeName(goodsTypeService.findById(goods.getGoodsTypeId()).getName());
        goods.setUser(userService.findById(goods.getUserId()));
        QueryWrapper<ContactInformation> contactInformationQueryWrapper = new QueryWrapper<>();
        contactInformationQueryWrapper.eq("userId", goods.getUserId());
        goods.setContactInformationList(contactInformationService.list(contactInformationQueryWrapper));
        getFirstImageInGoodsContent(goods);
        mav.addObject("goods", goods);
        goodsService.update(goods);
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
        goodsQueryWrapper3.eq("isRecommend", 1);
        goodsQueryWrapper3.eq("state", 1);
        Page<Goods> goodsPage3 = new Page<>(1, 9);
        List<Goods> goodsRecommendList = goodsService.list(goodsPage3, goodsQueryWrapper3);
        for (Goods goods2 : goodsRecommendList) {
            getFirstImageInGoodsContent(goods2);
            goods2.setGoodsTypeName(goodsTypeService.findById(goods2.getGoodsTypeId()).getName());
        }
        for (int i = 0; i < goodsRecommendList.size(); i++) {
            if (goodsRecommendList.get(i).getId().equals(goods.getId())) {
                goodsRecommendList.remove(goodsRecommendList.get(i));
                i--;
            }
        }
        Collections.shuffle(goodsRecommendList);
        mav.addObject("goodsRecommendList", goodsRecommendList);
        //Get seller's email
        String emailStr = userService.findById(goods.getUserId()).getEmail();
        mav.addObject("emailStr", emailStr);
        mav.addObject("title", goods.getName() + "--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/goodsDetails");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Add or modify products
     *
     * @param goods
     * @return
     */
    @RequestMapping("/save")
    public ModelAndView save(Goods goods) {
        ModelAndView mav = new ModelAndView("redirect:/toGoodsManagePage");
        if (goods.getId() == null) {
            goods.setAddTime(new Date());
            goods.setState(0);
            goods.setIsRecommend(0);
            goods.setClick(0);
            goodsService.add(goods);
        } else {
            //The corresponding product in the database, that is: the product before modification
            Goods trueGoods = goodsService.findById(goods.getId());
            goods.setState(0);
            //If the price is modified, the last price will be updated.
            if (goods.getPriceNow() != trueGoods.getPriceNow()) {
                goods.setPriceLast(trueGoods.getPriceNow());
            }
            goodsService.update(goods);
        }
        return mav;
    }

    /**
     * Search for products based on product name
     * @param name
     * @return
     */
    @RequestMapping("/search")
    public ModelAndView search(String name, Integer goodsType) {
        ModelAndView mav = new ModelAndView();
        QueryWrapper<Goods> goodsQueryWrapper = new QueryWrapper<>();
        goodsQueryWrapper.like("name", name);
        goodsQueryWrapper.orderByDesc("click");
        if (goodsType == 1) {
            goodsQueryWrapper.ne("goodsTypeId", wantToBuyId);
        } else if (goodsType == 2) {
            goodsQueryWrapper.eq("goodsTypeId", wantToBuyId);
            goodsQueryWrapper.eq("state", 1);
        }
        List<Goods> goodsList = goodsService.list(goodsQueryWrapper);
        for (Goods goods : goodsList) {
            goods.setGoodsTypeName(goodsTypeService.findById(goods.getGoodsTypeId()).getName());
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
        //Get a list of recommended products
        QueryWrapper<Goods> goodsQueryWrapper3 = new QueryWrapper<>();
        goodsQueryWrapper3.eq("isRecommend", 1);
        goodsQueryWrapper3.eq("state", 1);
        Page<Goods> goodsPage3 = new Page<>(1, 9);
        List<Goods> goodsRecommendList = goodsService.list(goodsPage3, goodsQueryWrapper3);
        for (Goods goods : goodsRecommendList) {
            getFirstImageInGoodsContent(goods);
            goods.setGoodsTypeName(goodsTypeService.findById(goods.getGoodsTypeId()).getName());
        }
        Collections.shuffle(goodsRecommendList);
        mav.addObject("goodsRecommendList", goodsRecommendList);
        mav.addObject("name", name);
        mav.addObject("goodsType", goodsType);
        mav.addObject("title",
                "the result of search(" + name + ")--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/goodsSearchResult");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }

    /**
     * Add items to shopping cart
     *
     * @param goodsId
     * @return
     */
    @ResponseBody
    @RequestMapping("/addGoodsToShoppingCart")
    public Map<String, Object> addGoodsToShoppingCart(Integer goodsId, HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>(16);
        Gson gson = new Gson();
        Goods goods = goodsService.findById(goodsId);
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resultMap.put("success", false);
            resultMap.put("errorInfo", "Your login status has expired, please log in again! !");
            return resultMap;
        }
        String shoppingCartName = currentUser.getId() + "_shoppingCart";
        List<String> shoppingCartGoodsStr = RedisUtil.listRange(shoppingCartName, 0L, -1L);
        for (int i = 0; i < shoppingCartGoodsStr.size(); i++) {
            Goods goods1 = gson.fromJson(shoppingCartGoodsStr.get(i), Goods.class);
            if (goods.getId().equals(goods1.getId())) {
                resultMap.put("success", false);
                resultMap.put("errorInfo", "Failed to add to shopping cart. " +
                        "This product is already in your shopping cart! !");
                return resultMap;
            }
        }
        boolean key = RedisUtil.listRightPush(shoppingCartName, gson.toJson(goods));
        if (key) {
            resultMap.put("success", true);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    /**
     * Remove items from shopping cart
     *
     * @param goodsId
     * @return
     */
    @ResponseBody
    @RequestMapping("/deleteGoodsInShoppingCart")
    public Map<String, Object> deleteGoodsInShoppingCart(Integer goodsId, HttpSession session) {
        Map<String, Object> resultMap = new HashMap<>(16);
        Gson gson = new Gson();
        Goods goods = goodsService.findById(goodsId);
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resultMap.put("success", false);
            resultMap.put("errorInfo", "Your login status has expired, please log in again! !");
            return resultMap;
        }
        String shoppingCartName = currentUser.getId() + "_shoppingCart";
        List<String> shoppingCartGoodsStr = RedisUtil.listRange(shoppingCartName, 0L, -1L);
        boolean deleteKey = false;
        for (int i = 0; i < shoppingCartGoodsStr.size(); i++) {
            Goods goods1 = gson.fromJson(shoppingCartGoodsStr.get(i), Goods.class);
            if (goods.getId().equals(goods1.getId())) {
                shoppingCartGoodsStr.remove(shoppingCartGoodsStr.get(i));
                i--;
                deleteKey = true;
                RedisUtil.delKey(shoppingCartName);
                break;
            }
        }
        if (shoppingCartGoodsStr.size() > 0) {
            for (String s : shoppingCartGoodsStr) {
                RedisUtil.listRightPush(shoppingCartName, s);
            }
        }
        if (deleteKey) {
            resultMap.put("success", true);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    /**
     * Modify product status
     *
     * @param goodsId
     * @param state
     * @return
     */
    @ResponseBody
    @RequestMapping("/updateGoodsState")
    public Map<String, Object> updateGoodsState(Integer goodsId, Integer state, String reason) {
        Map<String, Object> resultMap = new HashMap<>(16);
        Goods goods = goodsService.findById(goodsId);
        //If the reservation is canceled, goods.get State() is the current state, and state is the target state.
        if (goods.getState() == 4 && state == 1) {
            ReserveRecord reserveRecord = reserveRecordService.findByGoodsIdAndState(goodsId, 0);
            reserveRecord.setState(1);
            reserveRecordService.update(reserveRecord);
            //When the seller cancels the buyer's reservation, the system sends a message to the buyer
            Message message = new Message();
            message.setUserId(userService.findById(reserveRecord.getUserId()).getId());
            message.setContent("Items you ordered（" + goods.getName() + "）The reservation has been canceled by the seller！！");
            message.setTime(new Date());
            message.setIsRead(0);
            messageService.add(message);
        }
        //If the product status is set to review failed
        if (state == 2) {
            goods.setReason(reason.split(",")[0]);
        }
        //If the item status is set to Transaction Successful
        if (state == 5) {
            ReserveRecord reserveRecord = reserveRecordService.findByGoodsIdAndState(goodsId, 0);
            reserveRecord.setState(2);
            reserveRecordService.update(reserveRecord);
        }
        goods.setState(state);
        int key = goodsService.update(goods);
        if (key > 0) {
            resultMap.put("success", true);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    /**
     * Delete product
     *
     * @param goodsId
     * @return
     */
    @ResponseBody
    @RequestMapping("/delete")
    public Map<String, Object> delete(Integer goodsId) {
        Map<String, Object> resultMap = new HashMap<>(16);
        int key = goodsService.deleteById(goodsId);
        if (key > 0) {
            resultMap.put("success", true);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    /**
     * Get the product based on the product id (returned after Ajax request)
     *
     * @param goodsId
     * @return
     */
    @ResponseBody
    @RequestMapping("/findById")
    public Map<String, Object> findById(Integer goodsId) {
        Map<String, Object> resultMap = new HashMap<>(16);
        Goods goods = goodsService.findById(goodsId);
        int key = 0;
        if (goods != null) {
            key = 1;
        }
        if (key > 0) {
            resultMap.put("success", true);
            resultMap.put("goods", goods);
        } else {
            resultMap.put("success", false);
        }
        return resultMap;
    }

    @RequestMapping("/buy")
    public ModelAndView buy(Integer goodsId, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        Goods goods = goodsService.findById(goodsId);
        goods.setGoodsTypeName(goodsTypeService.findById(goods.getGoodsTypeId()).getName());
        getFirstImageInGoodsContent(goods);
        QueryWrapper<ShippingAddress> shippingAddressQueryWrapper = new QueryWrapper<>();
        User currentUser = (User) session.getAttribute("currentUser");
        shippingAddressQueryWrapper.eq("userId", currentUser.getId());
        shippingAddressQueryWrapper.eq("useOrNot", 1);
        List<ShippingAddress> shippingAddressList = shippingAddressService.list(shippingAddressQueryWrapper);
        ShippingAddress shippingAddress = null;
        if (shippingAddressList.size() > 0) {
            shippingAddress = shippingAddressList.get(0);
        }
        mav.addObject("goods", goods);
        mav.addObject("shippingAddress", shippingAddress);
        mav.addObject("title", "buy goods--Campus second-hand trading platform");
        mav.addObject("mainPage", "page/buyGoods");
        mav.addObject("mainPageKey", "#b");
        mav.setViewName("index");
        return mav;
    }
}
