package com.hmall.api.client.fallback;

import cn.hutool.core.collection.CollUtil;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class ItemClientFallback implements FallbackFactory<ItemClient> {

    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                // 库存扣减业务出发事务回滚，查询失败，抛出异常
                throw  new BizIllegalException(cause);
            }

            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("商品查询异常",ids,cause);
                // 查询购物车允许失败，查询失败，返回空集合
                return CollUtils.emptyList();
            }
        };
    }
}
