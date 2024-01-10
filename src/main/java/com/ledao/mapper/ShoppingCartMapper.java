package com.ledao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledao.entity.Goods;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ShoppingCartMapper extends BaseMapper<Goods> {
}
