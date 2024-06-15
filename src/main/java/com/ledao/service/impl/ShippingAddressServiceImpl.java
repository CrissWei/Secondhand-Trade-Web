package com.ledao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledao.entity.ShippingAddress;
import com.ledao.mapper.ShippingAddressMapper;
import com.ledao.service.ShippingAddressService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author LeDao
* @description 针对表【t_shipping_address】的数据库操作Service实现
* @createDate 2024-05-11 22:29:19
*/
@Service
public class ShippingAddressServiceImpl implements ShippingAddressService{

    @Resource
    private ShippingAddressMapper shippingAddressMapper;

    @Override
    public List<ShippingAddress> list(QueryWrapper<ShippingAddress> shippingAddressQueryWrapper, Page<ShippingAddress> shippingAddressPage) {
        return shippingAddressMapper.selectPage(shippingAddressPage,shippingAddressQueryWrapper).getRecords();
    }

    @Override
    public List<ShippingAddress> list(QueryWrapper<ShippingAddress> shippingAddressQueryWrapper) {
        return shippingAddressMapper.selectList(shippingAddressQueryWrapper);
    }

    @Override
    public Integer getCount(QueryWrapper<ShippingAddress> shippingAddressQueryWrapper) {
        return shippingAddressMapper.selectCount(shippingAddressQueryWrapper);
    }

    @Override
    public int add(ShippingAddress shippingAddress) {
        return shippingAddressMapper.insert(shippingAddress);
    }

    @Override
    public int update(ShippingAddress shippingAddress) {
        return shippingAddressMapper.updateById(shippingAddress);
    }

    @Override
    public ShippingAddress findById(Integer id) {
        return shippingAddressMapper.selectById(id);
    }

    @Override
    public int deleteById(Integer id) {
        return shippingAddressMapper.deleteById(id);
    }
}




