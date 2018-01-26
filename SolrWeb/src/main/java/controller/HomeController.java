package controller;

import dao.MongoDB;
import log.MyLogger;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

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
@RequestMapping(value = "/ruclaw", method = RequestMethod.GET)
public class HomeController {
    private static Logger LOGGER = MyLogger.getMyLogger(HomeController.class);
    private static MongoDB mongoDB = MongoDB.getMongoDB();
    //映射一个action
    @RequestMapping(value = "/paper", method = RequestMethod.GET)
    public ModelAndView lawPaper(String id) {
        System.out.println(id);
        Document law = mongoDB.getDocumentById(id);
        ModelAndView mav = new ModelAndView();
        if(law != null){
            mav.setViewName("law");
            mav.addObject("title", law.getString("title"));
            mav.addObject("content", law.getString("content").replaceAll("\n", "<br>"));
        }else {
            mav.setViewName("error");
            mav.addObject("id", id);
            mav.addObject("title", "Not Found");
        }
        //返回一个index.jsp这个视图
        return mav;
    }

}
