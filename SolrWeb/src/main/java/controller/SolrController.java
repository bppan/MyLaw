package controller;

import log.MyLogger;
import model.Document;
import model.SuggestValue;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import service.QueryService;
import service.SuggestService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/8 13:38
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
@Controller
@RequestMapping(value = "/ruclaw")
public class SolrController {

    private static Logger LOGGER = MyLogger.getMyLogger(SolrController.class);
    public List<SuggestValue> best_suggest;

    @RequestMapping(value = "/suggest", method = RequestMethod.GET)
    @ResponseBody
    public List<SuggestValue> suggest(String keyword) {
        LOGGER.info("suggest keyword:" + keyword);
        SuggestService suggestService = new SuggestService();
        this.best_suggest = suggestService.doSuggest(keyword);
        return this.best_suggest;
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> query(String query_string, int start, int rows, String sortField) {
        Map<String, Object> map = new HashMap<String, Object>();
        LOGGER.info("query :" + query_string + " start:" + start +" rows:" + rows);
        QueryService queryService = new QueryService();
        List<Document> resultList = queryService.getQueryResult(query_string, start*rows, rows, sortField);
        long numFound = queryService.getNumFound();
        int QTime = queryService.getQTime();
        map.put("numFound", numFound);
        map.put("QTime", QTime);
        map.put("resultList", resultList);
        return map;
    }

}
