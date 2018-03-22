package controller;

import log.MyLogger;
import model.Article;
import model.GraphPath;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import service.LawService;
import service.Neo4jService;

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
@RequestMapping(value = "/ruclaw")
public class HomeController {
    private static Logger LOGGER = MyLogger.getMyLogger(HomeController.class);

    //返回法律内容页面
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
                mav.addObject("contentHtml", "<p class='text-center'><font color='#dd4b39'>" + "law paper id:" + id + " Not Found</font></p>");
                LOGGER.warn("Request mapping ruclaw/paper not found id:" + id);
            }
        } catch (Exception e) {
            mav.setViewName("error");
            mav.addObject("title", "Server Error");
            mav.addObject("contentHtml", "<p class='text-center'><font color='#dd4b39'>" + "law paper id:" + id + " Server Error</font></p>");
            LOGGER.error("Request mapping ruclaw/paper err:" + e);
        }
        return mav;
    }

    //返回法律内容页面url
    @RequestMapping(value = "/paperUrl", method = RequestMethod.GET)
    @ResponseBody
    public String lawpaperUrl(String id) {
        LOGGER.info("Request mapping ruclaw/article is:" + id);
        try {
            LawService lawService = new LawService();
            return lawService.getPaperUrl(id);
        } catch (Exception e) {
            LOGGER.error("Request mapping ruclaw/article error:" + e);
            return "";
        }
    }

    //返回法律条款内容
    @RequestMapping(value = "/article", method = RequestMethod.GET)
    @ResponseBody
    public List<Article> lawArticle(String lawArticle) {
        LOGGER.info("Request mapping ruclaw/article is:" + lawArticle);
        List<Article> resultList = null;
        try {
            Pattern titlePattern = Pattern.compile("(《(.*?)》)(第[零一二三四五六七八九十百千万]+条)?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher matcherTitle = titlePattern.matcher(lawArticle);
            if (matcherTitle.find()) {
                LOGGER.info("find name :" + matcherTitle.group());
                String lawName = matcherTitle.group(2);
                String lawTiaoName = matcherTitle.group(3);
                LawService lawService = new LawService();
                resultList = lawService.getArtcileContent(lawName, lawTiaoName);
            }
        } catch (Exception e) {
            LOGGER.error("Request mapping ruclaw/article error:" + e);
        }
        return resultList;
    }

    //返回法律条款内容
    @RequestMapping(value = "/abstract", method = RequestMethod.POST)
    @ResponseBody
    public Article lawArticleAbstarct(String id) {
        LOGGER.info("Request mapping ruclaw/abstract is:" + id);
        Article resultArticle = new Article();
        try {
            LawService lawService = new LawService();
            resultArticle = lawService.getLawContent(id);
            return resultArticle;
        } catch (Exception e) {
            LOGGER.error("Request mapping ruclaw/abstract error:" + e);
            return resultArticle;
        }
    }

    //返回法律图谱关系页面
    @RequestMapping(value = "/graph", method = RequestMethod.GET)
    public ModelAndView graphView(String id) {
        LOGGER.info("Request mapping ruclaw/graph law id is:" + id);
        ModelAndView mav = new ModelAndView();
        mav.setViewName("graph");
        mav.addObject("id", id);
        return mav;
    }

    //返回图谱关系
    @RequestMapping(value = "/graphPath", method = RequestMethod.POST)
    @ResponseBody
    public List<GraphPath> graphPath(String id, int layerNum, int limitNum, String nodeType) {
        LOGGER.info("Request mapping ruclaw/graphPath is:" + id + " layerNum: " + layerNum + " limitNum: " + limitNum + "nodeType: " + nodeType);
        List<GraphPath> graphPathList = new ArrayList<>();
        try {
            Neo4jService neo4jService = new Neo4jService();
            graphPathList = neo4jService.getGraph(id, layerNum, limitNum, nodeType);
        } catch (Exception e) {
            LOGGER.error("Request mapping ruclaw/graphPath error:" + e);
        }
        //返回一个index.jsp这个视图
        return graphPathList;
    }

    //返回接节点图谱关系
    @RequestMapping(value = "/nodeGraphPath", method = RequestMethod.POST)
    @ResponseBody
    public List<GraphPath> nodeGraphPath(String id, int limitNum) {
        LOGGER.info("Request mapping ruclaw/nodeGraphPath is:" + id + " limit num: " + limitNum);
        List<GraphPath> graphPathList = new ArrayList<>();
        try {
            Neo4jService neo4jService = new Neo4jService();
            graphPathList = neo4jService.getNodeGraph(id, limitNum);
        } catch (Exception e) {
            LOGGER.error("Request mapping ruclaw/graphPath error:" + e);
        }
        //返回一个index.jsp这个视图
        return graphPathList;
    }

    //返回推荐数据
    @RequestMapping(value = "/recommend", method = RequestMethod.GET)
    @ResponseBody
    public List<model.Document> getRecommend(String id, int limitNum){
        LOGGER.info("Request mapping ruclaw/recommend law id is:" + id);
        List<model.Document> reusltRecommendList = new ArrayList<>();
        try {
            Neo4jService neo4jService = new Neo4jService();
            reusltRecommendList = neo4jService.getRelationshipLaw(id, limitNum);
            return reusltRecommendList;
        } catch (Exception e) {
            LOGGER.error("Request mapping ruclaw/graphPath error:" + e);
            return reusltRecommendList;
        }
    }

    //返回推荐数据
    @RequestMapping(value = "/autoAnswer", method = RequestMethod.POST)
    @ResponseBody
    public String getAutoAnswer(String question){
        LOGGER.info("Request mapping ruclaw/autoAnswer: " + question);
        try {
            return "The function is being developed...";
        } catch (Exception e) {
            LOGGER.error("Request mapping ruclaw/autoAnswer error:" + e);
            return "The function is being developed...";
        }
    }


}
