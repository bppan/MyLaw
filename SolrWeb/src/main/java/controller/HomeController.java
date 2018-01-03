package controller;

import log.MyLogger;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/2 20:58
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
// 注解标注此类为springmvc的controller，url映射为"/hello"
@Controller
@RequestMapping(value = "/hello", method = RequestMethod.GET)
public class HomeController {
    private static Logger LOGGER = MyLogger.getMyLogger(HomeController.class);
    //映射一个action
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public  String index(){
        //输出日志文件
        LOGGER.info("the first jsp pages");
        System.out.println("the first jsp pages");
        //返回一个index.jsp这个视图
        return "hello";
    }
    @RequestMapping(value = "/getAjax", method = RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> save(String name){
        //输出日志文件
        LOGGER.info("the first ajax:" + name);
        System.out.println("the first ajax:" +name);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("success", true);
        map.put("name", name);
        //返回一个index.jsp这个视图
        return map;
    }
}
