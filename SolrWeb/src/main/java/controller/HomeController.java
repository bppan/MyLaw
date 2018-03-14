package controller;

import dao.MongoDB;
import log.MyLogger;
import model.Article;
import model.SuggestValue;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import service.LawService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    //映射一个action
    @RequestMapping(value = "/paper", method = RequestMethod.GET)
    public ModelAndView lawPaper(String id) {
        LOGGER.info("Request mapping ruclaw/paper papaer id is:" + id);
        ModelAndView mav = new ModelAndView();
        try {
            LawService lawService = new LawService();
            Document law = lawService.getLawDocument(id);
            if (law != null) {
                mav.setViewName("law");
                mav.addObject("titleHtml", lawService.getTitleHtml(law));
                mav.addObject("title", law.getString("title").trim());
                mav.addObject("contentHtml", lawService.getContentHtml(law));
            } else {
                mav.setViewName("error");
                mav.addObject("title", "Not Found");
                mav.addObject("contentHtml", "<p class='text-center'><font color='#dd4b39'>"+"law paper id:" + id + " Not Found</font></p>");
                LOGGER.warn("Request mapping ruclaw/paper not found id:" + id);
            }
        }catch (Exception e){
            mav.setViewName("error");
            mav.addObject("title", "Server Error");
            mav.addObject("contentHtml", "<p class='text-center'><font color='#dd4b39'>"+"law paper id:" + id + " Server Error</font></p>");
            LOGGER.error("Request mapping ruclaw/paper err:" + e);
        }
        //返回一个index.jsp这个视图
        return mav;
    }

    @RequestMapping(value = "/article", method = RequestMethod.GET)
    @ResponseBody
    public List<Article> lawArticle(String lawArticle) {
        LOGGER.info("Request mapping ruclaw/article is:" + lawArticle);
        List<Article> resultList = null;
        try {
            Pattern titlePattern = Pattern.compile("(《(.*?)》)(第[零一二三四五六七八九十百千万]+条)?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher matcherTitle = titlePattern.matcher(lawArticle);
            if(matcherTitle.find()){
                LOGGER.info("find name :" + matcherTitle.group());
                String lawName = matcherTitle.group(2);
                String lawTiaoName = matcherTitle.group(3);
                LawService lawService = new LawService();
                resultList = lawService.getArtcileContent(lawName, lawTiaoName);
            }
        }catch (Exception e){
            LOGGER.error("Request mapping ruclaw/article error:" + e);
        }
        //返回一个index.jsp这个视图
        return resultList;
    }

}
