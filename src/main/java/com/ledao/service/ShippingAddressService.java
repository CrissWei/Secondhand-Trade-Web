package com.ledao.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledao.entity.ShippingAddress;
import com.ledao.entity.ShippingAddress;

import java.util.List;

/**
* @author LeDao
* @description 针对表【t_shipping_address】的数据库操作Service
* @createDate 2024-05-11 22:29:19
*/
public interface ShippingAddressService {

    /**
     * 分页条件查询
     *
     * @param shippingAddressQueryWrapper
     * @param shippingAddressPage
     * @return
     */
    List<ShippingAddress> list(QueryWrapper<ShippingAddress> shippingAddressQueryWrapper, Page<ShippingAddress> shippingAddressPage);

    /**
     * 不分页条件查询
     *
     * @param shippingAddressQueryWrapper
     * @return
     */
    List<ShippingAddress> list(QueryWrapper<ShippingAddress> shippingAddressQueryWrapper);

    /**
     * 获取记录数
     *
     * @param shippingAddressQueryWrapper
     * @return
     */
    Integer getCount(QueryWrapper<ShippingAddress> shippingAddressQueryWrapper);

    /**
     * 添加
     *
     * @param shippingAddress
     * @return
     */
    int add(ShippingAddress shippingAddress);

    /**
     * 修改
     *
     * @param shippingAddress
     * @return
     */
    int update(ShippingAddress shippingAddress);

    /**
     * 根据id查找
     *
     * @param id
     * @return
     */
    ShippingAddress findById(Integer id);

    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    int deleteById(Integer id);
}
