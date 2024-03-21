
package com.jiuzhang.seckill.mq;


import com.alibaba.fastjson.JSON;
import com.jiuzhang.seckill.db.dao.OrderDao;
import com.jiuzhang.seckill.db.dao.SeckillActivityDao;
import com.jiuzhang.seckill.db.po.Order;
import com.jiuzhang.seckill.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_check", consumerGroup = "pay_check_group")
public class PayStatusCheckListener implements RocketMQListener<MessageExt> {
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RedisService redisService;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Got order payment status info:" + message);
        Order order = JSON.parseObject(message, Order.class);
        //1.Query order
        Order orderInfo = orderDao.queryOrder(order.getOrderNo());
        //2.verify if order payment is done or not
        if (orderInfo.getOrderStatus() != 2) {
            //3.close order if payment is not done
            log.info("未完成支付关闭订单,订单号：" + orderInfo.getOrderNo());
            orderInfo.setOrderStatus(99);
            orderDao.updateOrder(orderInfo);
            //4.revert data base stock
            seckillActivityDao.revertStock(order.getSeckillActivityId());
            // revert redis stock
            redisService.revertStock("stock:" + order.getSeckillActivityId());
            //5.remove user out of the item done payment set
            redisService.removeLimitMember(order.getSeckillActivityId(), order.getUserId());
        }
    }
}

