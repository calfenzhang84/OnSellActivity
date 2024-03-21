package com.jiuzhang.seckill;

import com.jiuzhang.seckill.service.SeckillActivityService;
import com.jiuzhang.seckill.util.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.UUID;

@SpringBootTest
public class RedisDemoTest {

    @Resource
    private RedisService redisService;
    @Resource
    SeckillActivityService seckillActivityService;

    @Test
    public void stockTest() {
        redisService.setValue("stock:19", 10L);
    }

    @Test
    public void getStockTest() {
        String stock = redisService.getValue("stock:19");
        System.out.println(stock);
    }

    @Test
    public void stockDeductValidatorTest() {
        boolean result = redisService.stockDeductValidator("stock:19");
        System.out.println("result:" + result);
        String stock = redisService.getValue("stock:19");
        System.out.println("stock:" + stock);
    }


    @Test
    public void revertStock() {
        String stock = redisService.getValue("stock:19");
        System.out.println("revert to previous stock：" + stock);

        redisService.revertStock("stock:19");

        stock = redisService.getValue("stock:19");
        System.out.println("revert to previous stock：" + stock);
    }

    @Test
    public void removeLimitMember() {
        redisService.removeLimitMember(19, 1234);
    }

    @Test
    public void pushSeckillInfoToRedisTest(){
        seckillActivityService.pushSeckillInfoToRedis(19);
    }
    @Test
    public void getSekillInfoFromRedis() {
        String seclillInfo = redisService.getValue("seckillActivity:" + 19);
        System.out.println(seclillInfo);
        String seckillCommodity = redisService.getValue("seckillCommodity:"+1001);
        System.out.println(seckillCommodity);
    }

    /**
     * Test to get lock under high concurreny environment
     */
    @Test
    public void  testConcurrentAddLock() {
        for (int i = 0; i < 10; i++) {
            String requestId = UUID.randomUUID().toString();
            // result: true false false false false false false false false false
            // only the first one got the lock
            System.out.println(redisService.tryGetDistributedLock("A", requestId,1000));
        }
    }
    /**
     * Test to get lock and release immediately under high concurrency environment
     */
    @Test
    public void  testConcurrent() {
        for (int i = 0; i < 10; i++) {
            String requestId = UUID.randomUUID().toString();
            // result true true true true true true true true true true
            System.out.println(redisService.tryGetDistributedLock("A", requestId,1000));
            redisService.releaseDistributedLock("A", requestId);
        }
    }

}
