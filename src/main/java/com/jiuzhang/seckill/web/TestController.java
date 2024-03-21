package com.jiuzhang.seckill.web;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class TestController {

    @ResponseBody
    @RequestMapping("hello")
    public String hello(){
        String result;
        try (Entry entry = SphU.entry("HelloResource")){
            result  = "Hello Sentinel";
            return result;
        }catch (BlockException ex) {
            log.error(ex.toString());
            result = "System Busy";
            return  result;
        }
    }

    /**
     *  Define polcy for rater limiter
     *  1.create a policy set for rate limiter
     *  2.create policy for rate limiter
     *  3.put policy into policy set
     *  4.load policy of rate limiter
     *  @PostConstruct implment method after current class construction is done
     */
    @PostConstruct
    public void seckillsFlow(){
        //1.create a policy set for rate limiter
        List<FlowRule> rules = new ArrayList<>();
        //2.create policy for rate limiter
        FlowRule rule = new FlowRule();
        //define resourse for sentinel to work on
        rule.setResource("seckills");
        //define,type of QPS
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        //define request of QPS
        rule.setCount(1);

        FlowRule rule2 = new FlowRule();
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setCount(2);
        rule2.setResource("HelloResource");
        //3.put policy into policy set
        rules.add(rule);
        rules.add(rule2);
        //4.load policy of rate limiter
        FlowRuleManager.loadRules(rules);
    }
}