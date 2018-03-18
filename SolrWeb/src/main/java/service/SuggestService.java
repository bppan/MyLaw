package service;

import dao.SolrServer;
import log.MyLogger;
import model.SuggestValue;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Description：
 * Author: Administrator
 * Created:  2018/1/8 13:46
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class SuggestService {
    private static Logger LOGGER = MyLogger.getMyLogger(SuggestService.class);
    private SolrServer solrServer;

    public SuggestService() {
        this.solrServer = SolrServer.getSolrServer();
    }

    public List<SuggestValue> doSuggest(String keyWord) {
        List<SuggestValue> best_suggest = new ArrayList<SuggestValue>();
        SolrQuery params = new SolrQuery();
        params.set("qt", "/suggest");
        params.setQuery(keyWord);
        QueryResponse response = null;
        try {
            response = this.solrServer.getSolrClient().query(params);
            LOGGER.info("Suggest cost time: " + response.getQTime());
        } catch (Exception e) {
            LOGGER.error("Suggest err: " + e.getMessage());
        }
        try {
            SpellCheckResponse spellCheckResponse = response.getSpellCheckResponse();
            if (spellCheckResponse != null) {
                List<SpellCheckResponse.Suggestion> suggestionList = spellCheckResponse.getSuggestions();
                if (suggestionList.size() == 0) {
                    return best_suggest;
                }
                SpellCheckResponse.Suggestion suggestion = suggestionList.get(suggestionList.size() - 1);
                List<String> suggestedWordList = suggestion.getAlternatives();
                for (String word : suggestedWordList) {
                    SuggestValue suggest = new SuggestValue();
                    suggest.setValue(word);
                    best_suggest.add(suggest);
                }
            }
        } catch (Exception e) {
            LOGGER.info("GetSpellCheckResponse null： " + e.toString());
        }

        return best_suggest;
    }

    public String getCollaction(String keyWord) {
        String result = "";
        SolrQuery params = new SolrQuery();
        params.set("qt", "/suggest");
        params.setQuery(keyWord);
        QueryResponse response = null;
        try {
            response = this.solrServer.getSolrClient().query(params);
            System.out.println("查询耗时：" + response.getQTime());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        SpellCheckResponse spellCheckResponse = response.getSpellCheckResponse();

        List<SpellCheckResponse.Collation> collatedList = spellCheckResponse.getCollatedResults();
        if (collatedList != null) {
            for (SpellCheckResponse.Collation collation : collatedList) {
                System.out.println("collated query String: " + collation.getCollationQueryString());
                System.out.println();
                return collation.getCollationQueryString();
            }
        }
        return result;
    }
}
