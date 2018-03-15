package graph;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import log.MyLogger;
import mongo.MongoServer;
import org.apache.log4j.Logger;
import org.bson.Document;
import util.DateParse;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description：
 * Author: beiping_pan
 * Created:  2018/2/28 14:55
 * Copyright: Copyright (c) 2017
 * Version: 0.0.1
 * Modified By:
 */
public class CreateBetweenLawRelationShip {
    private static MongoServer mongoServer = MongoServer.getMongoDB();
    private static Logger LOGGER = MyLogger.getMyLogger(CreateBetweenLawRelationShip.class);
    private static String[] relationFront = {"未依据", "依据", "根据", "未依照", "依照", "未按照", "按照",
            "不适用", "适用", "不符合", "符合", "引用",
            "不属于", "属于","不得违反","不违反", "未违反","违反", "未实施", "实施", "不参照", "参照", "未涉及", "不涉及", "涉及"};
    private static String[] relationBehind = {"同时废止"};
    private MongoCollection<Document> fromCollection;
    private MongoCollection<Document> toCollection;

    public CreateBetweenLawRelationShip(String fromCollectionName, String toCollectionName) {
        this.fromCollection = mongoServer.getCollection(fromCollectionName);
        this.toCollection = mongoServer.getCollection(toCollectionName);
    }

    public void create() {
        FindIterable<Document> iterables = this.fromCollection.find().noCursorTimeout(true).batchSize(10000);
        MongoCursor<Document> cursor = iterables.iterator();
        int num = 0;
        try {
            while (cursor.hasNext()) {
                Document fromLaw = cursor.next();
                String url = fromLaw.getString("url");
                LOGGER.info("create current url: " + url);
                long startTime = System.currentTimeMillis();
                FindIterable<Document> findIterable = this.toCollection.find(new Document("url", url)).limit(1).noCursorTimeout(true);
                if (findIterable.first() != null) {
                    Document toLaw = findIterable.first();
                    //遍历到在库中的law，开始穿件该law对应的关系
                    createLawTiaoRelationShipBetweenAndLaw(toLaw);
                }
                long endTime = System.currentTimeMillis();
                num++;
                LOGGER.info("Create law num:" + num + " cost time:" + (endTime - startTime) +
                        " from collection: " + this.fromCollection.getNamespace().getCollectionName() + " to Collection: " + this.toCollection.getNamespace().getCollectionName());
            }
        } catch (Exception e) {
            LOGGER.info("Create law err: " + e);
        } finally {
            LOGGER.info("Create law done...");
            cursor.close();
        }
    }

    //遍历法律文档条款，查找可能存在的关系
    @SuppressWarnings("unchecked")
    public void createLawTiaoRelationShipBetweenAndLaw(Document law) {
        String lawId = law.getObjectId("_id").toString();
        List<Document> documentList = (List<Document>) law.get("articles");
        //如果不是法律法规，自行跳过，不做处理
        if (documentList.size() <= 1) {
            LOGGER.info("the law have no tiao relationship");
        } else {
            for (int i = 0; i < documentList.size(); i++) {
                String articleId = lawId + "-" + i;
                List<String> para = (List<String>) documentList.get(i).get("paragraph");
                if (para.size() <= 1) {
                    if (para.size() == 1) {
                        //如果仅有一款
                        LOGGER.info(documentList.get(i).getString("name") + " the law have one para");
                        createFromAndToLawRelationShip(articleId, para.get(0), law);
                    }
                } else {
                    //如果仅有多款款
                    LOGGER.info(documentList.get(i).getString("name") + "the law have many para");
                    for (int j = 0; j < para.size(); j++) {
                        String paragraphId = articleId + "-" + j;
                        createFromAndToLawRelationShip(paragraphId, para.get(j), law);
                    }
                }
            }
        }
    }

    //根据当前遍历到的条款，查找是否存在关系，如果存在，并在neo4j中创建关系
    public void createFromAndToLawRelationShip(String id, String lawSentence, Document law) {
        //将当前款进行换行分割
        String[] sentenceArray = lawSentence.split("\n");
        HashSet<Integer> relationshipIndex = new HashSet<>();
        for (String sentence : sentenceArray) {
            //根据前置关系词遍历关系词是否在该句子中
            LOGGER.info("begin search relationFront...");
            for (String relationShip : relationFront) {
                Pattern pattern_relation = Pattern.compile(relationShip, Pattern.CASE_INSENSITIVE);
                Matcher m_relation = pattern_relation.matcher(sentence);
                while (m_relation.find()) {
                    if (relationshipIndex.add(m_relation.end())) {
                        LOGGER.info("find relationFront: " + relationShip + " startIndex:" + m_relation.start());
                        //找到关键词后，直接开始寻找后面对应的法律法规
                        findBackLawName2(id, m_relation.end(), sentence, relationShip, law);
                    }
                }
            }
            LOGGER.info("done search relationFront...");
            LOGGER.info("begin search relationBehind...");
            //根据后置关系词遍历关系词是否在该句子中
            for (String relationShip : relationBehind) {
                Pattern pattern_relation = Pattern.compile(relationShip, Pattern.CASE_INSENSITIVE);
                Matcher m_relation = pattern_relation.matcher(sentence);
                while (m_relation.find()) {
                    if (relationshipIndex.add(m_relation.end())) {
                        //找到关键词后，直接开始寻找后面对应的法律法规
                        LOGGER.info("find relationBehind: " + relationShip);
                        findFrontLawName(id, m_relation.start(), sentence, relationShip, law);
                    }
                }
            }
            //清空关系词位置信息
            relationshipIndex.clear();
            LOGGER.info("done relationBehind...");
        }
        LOGGER.info("done createFromAndToLawRelationShip");
    }

    public void findFrontLawName(String id, int startIndex, String sentence, String relationShipTag, Document law) {
        String subSentence = sentence.substring(0, startIndex);
        LOGGER.info("findFrontLawName sentence is: " + subSentence);
        //如果关系词后面跟的不是书名号，或者长度小于1直接跳过
        if (subSentence.length() < 3) {
            return;
        }
        boolean startName = false;
        StringBuilder name = new StringBuilder();
        for (int i = subSentence.length() - 1; i >= 0; ) {
            char word = subSentence.charAt(i);
            if (word == '）' && !startName) {
                //判断是否是小括号赘述,如果是直接跳过小括号
                String kuaoHao = "（(.*?)）";//定义括号
                Pattern regEx_kuaoHao = Pattern.compile(kuaoHao, Pattern.CASE_INSENSITIVE);
                Matcher m_kuaoHao = regEx_kuaoHao.matcher(subSentence.substring(0, i + 1));
                if (m_kuaoHao.find() && m_kuaoHao.end() - 1 == i) {
                    i -= m_kuaoHao.group(0).length();
                    continue;
                }
            }
            if (word == '》') {
                startName = true;
                i--;
                continue;
            }
            if (startName) {
                name.append(word);
                i--;
                if (i >= 0 && subSentence.charAt(i) == '《') {
                    startName = false;
                }
                continue;
            }
            if (word == '《' && !startName) {
                String lawName = name.reverse().toString();
                RelationShipLaw relationShipLaw = new RelationShipLaw(lawName, null, null, null);
                //继续向前推进寻找时间
                i--;
                while (i >= 0) {
                    char tempWord = subSentence.charAt(i);
                    if (tempWord == '日' || tempWord == '号') {
                        Pattern p = Pattern.compile("(\\d{1,4}[-|\\/|年|\\.]\\d{1,2}[-|\\/|月|\\.]\\d{1,2}([日|号])+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                        Matcher matcher = p.matcher(subSentence.substring(0, i + 1));
                        while (matcher.find()) {
                            if (matcher.end() == subSentence.substring(0, i + 1).length()) {
                                relationShipLaw.setReleaseTime(matcher.group());
                                i -= matcher.group().length();
                                break;
                            }
                        }
                        if (i < 0) {
                            break;
                        }
                    }
                    if (tempWord == '月') {
                        Pattern p = Pattern.compile("(\\d{1,4}[-|\\/|年|\\.]\\d{1,2}月)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                        Matcher matcher = p.matcher(subSentence.substring(0, i + 1));
                        while (matcher.find()) {
                            if (matcher.end() == subSentence.substring(0, i + 1).length()) {
                                relationShipLaw.setReleaseTime(matcher.group());
                                i -= matcher.group().length();
                                break;
                            }
                        }
                        if (i < 0) {
                            break;
                        }
                    }
                    if (tempWord == '年') {
                        Pattern p = Pattern.compile("(\\d{1,4}年)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                        Matcher matcher = p.matcher(subSentence.substring(0, i + 1));
                        while (matcher.find()) {
                            if (matcher.end() == subSentence.substring(0, i + 1).length()) {
                                relationShipLaw.setReleaseTime(matcher.group());
                                i -= matcher.group().length();
                                break;
                            }
                        }
                        if (i < 0) {
                            break;
                        }
                    }
                    if (tempWord == '》') {
                        break;
                    }
                    if (tempWord == '，' || tempWord == '。') {
                        i--;
                        break;
                    }
                    i--;
                }
                findLawNameFromDbAndCreateRelationBack(law, id, relationShipTag, relationShipLaw);
                name.delete(0, name.length());
                continue;
            }
            if (word == '。' && !startName) {
                break;
            }
            i--;
        }
    }

    public void findBackLawName2(String id, int startIndex, String sentence, String relationShipTag, Document law) {
        String subSentence = sentence.substring(startIndex);
        LOGGER.info("findBackLawName sentence is: " + subSentence);
        String regEx_law = "(《(.*?)》)+(（(.*?)）)?(第[零一二三四五六七八九十百千万]+条)?(之[零一二三四五六七八九十百千万]+)?(第[零一二三四五六七八九十百千万]+款)?(第[零一二三四五六七八九十百千万]+项)?";
        String regEx_law2 = "((.*?)(法|规定)+)+(第[零一二三四五六七八九十百千万]+条)?(之[零一二三四五六七八九十百千万]+)?(第[零一二三四五六七八九十百千万]+款)?(第[零一二三四五六七八九十百千万]+项)?";
        String regEx_lawTiaoAndKuan = "(第[零一二三四五六七八九十百千万]+条)?(之[零一二三四五六七八九十百千万]+)?(第[零一二三四五六七八九十百千万]+款)?(第[零一二三四五六七八九十百千万]+项)?";
        int i = 0;
        RelationShipLaw relationShipLaw = new RelationShipLaw(null, null, null, null);
        while (i < subSentence.length()) {
            Pattern pattern_law = Pattern.compile(regEx_law, Pattern.CASE_INSENSITIVE);
            Matcher m_law = pattern_law.matcher(subSentence.substring(i));
            String law_name = null;
            String law_tiao = null;
            String law_kuan = null;
            String law_xiang = null;
            boolean findLaw = false;
            if (m_law.find() && m_law.start() == 0) {
                law_name = m_law.group(2);
                law_tiao = m_law.group(5);
                String law_tiao_add = m_law.group(6);
                law_kuan = m_law.group(7);
                law_xiang = m_law.group(8);
                if (law_tiao_add != null) {
                    law_tiao += law_tiao_add;
                }
                i += m_law.group(0).length();
                findLaw = true;
            }
            Pattern pattern_law2 = Pattern.compile(regEx_law2, Pattern.CASE_INSENSITIVE);
            Matcher m_law2 = pattern_law2.matcher(subSentence.substring(i));
            if (!findLaw && m_law2.find() && m_law2.start() == 0) {
                law_name = m_law2.group(1);
                law_tiao = m_law2.group(4);
                String law_tiao_add = m_law2.group(5);
                law_kuan = m_law2.group(6);
                law_xiang = m_law2.group(7);
                if (law_tiao_add != null) {
                    law_tiao += law_tiao_add;
                }
                i += m_law2.group(0).length();
                findLaw = true;
            }
            Pattern pattern_lawTiaoAndKuan = Pattern.compile(regEx_lawTiaoAndKuan, Pattern.CASE_INSENSITIVE);
            Matcher m_lawTiaoAndKuan = pattern_lawTiaoAndKuan.matcher(subSentence.substring(i));
            if (!findLaw && m_lawTiaoAndKuan.find() && m_lawTiaoAndKuan.start() == 0 && !m_lawTiaoAndKuan.group().isEmpty()) {
                law_tiao = m_lawTiaoAndKuan.group(1);
                String law_tiao_add = m_lawTiaoAndKuan.group(2);
                law_kuan = m_lawTiaoAndKuan.group(3);
                law_xiang = m_lawTiaoAndKuan.group(4);
                if (law_tiao_add != null) {
                    law_tiao += law_tiao_add;
                }
                i += m_lawTiaoAndKuan.group(0).length();
                findLaw = true;
            }
            if (!findLaw || i > subSentence.length()) {
                break;
            }

            if (law_name != null) {
                relationShipLaw.setLawName(law_name);
                relationShipLaw.setTiaoName(law_tiao);
                relationShipLaw.setKuanName(law_kuan);
                relationShipLaw.setXiangName(law_xiang);
            }
            if (law_name == null && law_tiao != null) {
                relationShipLaw.setTiaoName(law_tiao);
                relationShipLaw.setKuanName(law_kuan);
                relationShipLaw.setXiangName(law_xiang);
            }
            if (law_name == null && law_tiao == null && law_kuan != null) {
                relationShipLaw.setKuanName(law_kuan);
                relationShipLaw.setXiangName(law_xiang);
            }
            if (law_name == null && law_tiao == null && law_kuan == null) {
                relationShipLaw.setXiangName(law_xiang);
            }
            findLawNameFromDbAndCreateRelationFront(law, id, relationShipTag, relationShipLaw);
            if (i < subSentence.length()) {
                if (subSentence.charAt(i) == '和' || subSentence.charAt(i) == '及' || subSentence.charAt(i) == '、') {
                    Matcher m_law_continue = pattern_law.matcher(subSentence.substring(i + 1));
                    if (m_law_continue.find() && m_law_continue.start() == 0) {
                        i++;
                        continue;
                    }
                    Matcher m_law2_continue = pattern_law2.matcher(subSentence.substring(i + 1));
                    if (m_law2_continue.find() && m_law2_continue.start() == 0) {
                        i++;
                        continue;
                    }
                    Matcher m_lawTIaoAndKuan_continue = pattern_lawTiaoAndKuan.matcher(subSentence.substring(i + 1));
                    if (m_lawTIaoAndKuan_continue.find() && m_lawTIaoAndKuan_continue.start() == 0 && !m_lawTIaoAndKuan_continue.group().isEmpty()) {
                        i++;
                        continue;
                    }
                } else {
                    break;
                }
            }
            break;
        }
    }

    public void findBackLawName(String id, int startIndex, String sentence, String relationShipTag, Document law) {
        String subSentence = sentence.substring(startIndex);
        LOGGER.info("findBackLawName sentence is: " + subSentence);
        //如果关系词后面跟的不是书名号，或者长度小于1直接跳过
        if (subSentence.length() < 1 || subSentence.charAt(0) != '《') {
            return;
        }
        boolean startName = false;
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < subSentence.length(); ) {
            char word = subSentence.charAt(i);
            if (word == '《') {
                startName = true;
                i++;
                continue;
            }
            if (startName) {
                name.append(word);
                i++;
                if (i < subSentence.length() && subSentence.charAt(i) == '》') {
                    startName = false;
                }
                continue;
            }
            if (word == '》') {
                startName = false;
                RelationShipLaw relationShipLaw = new RelationShipLaw(name.toString(), null, null, null);
                i++;
                //判断后面是否有小括号赘述,如果有直接跳过小括号
                String kuaoHao = "（(.*?)）";//定义括号
                Pattern regEx_kuaoHao = Pattern.compile(kuaoHao, Pattern.CASE_INSENSITIVE);
                Matcher m_kuaoHao = regEx_kuaoHao.matcher(subSentence.substring(i));
                if (m_kuaoHao.find() && m_kuaoHao.start() == 0) {
                    i += m_kuaoHao.group(0).length();
                }
                if (i >= subSentence.length()) {
                    findLawNameFromDbAndCreateRelationFront(law, id, relationShipTag, relationShipLaw);
                    LOGGER.info("findLawNameFromDbAndCreateRelationFront title:" + name.toString());
                    name.delete(0, name.length());
                    continue;
                }
                //查看法律名后是否还跟有哪一条哪一款哪一项详细细信息
                //由于可能存在并列后关系，因此此部分应该循环查找
                while (i < subSentence.length()) {
                    String reg_1 = "(第[零一二三四五六七八九十百千万]+条)?(之[零一二三四五六七八九十百千万]+)?(第[零一二三四五六七八九十百千万]+款)?(第[零一二三四五六七八九十百千万]+项)?";
                    Pattern regEx_tiao = Pattern.compile(reg_1, Pattern.CASE_INSENSITIVE);
                    Matcher m_tiao = regEx_tiao.matcher(subSentence.substring(i));
                    if (m_tiao.find() && m_tiao.start() == 0) {
                        String tiao = m_tiao.group(1);
                        System.out.println("tiao: " + tiao);
                        String tiaoAdd = m_tiao.group(2);
                        System.out.println("tiaoAdd: " + tiaoAdd);
                        String kuan = m_tiao.group(3);
                        System.out.println("kuan: " + kuan);
                        String xiang = m_tiao.group(4);
                        System.out.println("xiang: " + xiang);
                        if (tiaoAdd != null) {
                            tiao += tiaoAdd;
                        }
                        if (tiao != null) {
                            relationShipLaw.setTiaoName(tiao);
                            relationShipLaw.setKuanName(kuan);
                            relationShipLaw.setXiangName(xiang);
                        }
                        if (tiao == null && kuan != null) {
                            relationShipLaw.setKuanName(kuan);
                            relationShipLaw.setXiangName(xiang);
                        }
                        if (tiao == null && kuan == null) {
                            relationShipLaw.setXiangName(xiang);
                        }
                        findLawNameFromDbAndCreateRelationFront(law, id, relationShipTag, relationShipLaw);
                        i += m_tiao.group(0).length();
                        if (i < subSentence.length()) {
                            if (subSentence.charAt(i) == '和' || subSentence.charAt(i) == '及' || subSentence.charAt(i) == '、') {
                                Matcher m_tiao_continue = regEx_tiao.matcher(subSentence.substring(i + 1));
                                if (m_tiao_continue.find() && m_tiao_continue.start() == 0 && subSentence.charAt(i + 1) == '第') {
                                    System.out.println("next : " + m_tiao_continue.start());
                                    i++;
                                    continue;
                                }
                            }
                        }
                        break;
                    } else {
                        findLawNameFromDbAndCreateRelationFront(law, id, relationShipTag, relationShipLaw);
                        break;
                    }
                }
                name.delete(0, name.length());
                //向后看一位如果没有继续词，直接退出
                if (i < subSentence.length()) {
                    if (subSentence.charAt(i) == '《') {
                        continue;
                    }
                    if (subSentence.charAt(i) == '和' || subSentence.charAt(i) == '及' || subSentence.charAt(i) == '、') {
                        i++;
                        continue;
                    } else {
                        break;
                    }
                }
            }
            if (word == '。' || word == '，') {
                break;
            }
            i++;
        }
    }

    @SuppressWarnings("unchecked")
    public void findLawNameFromDbAndCreateRelationFront(Document law, String id, String relationShipTag, RelationShipLaw relationShipLaw) {
        LOGGER.info("find name is: " + relationShipLaw.getLawName() + " find tiao is: " + relationShipLaw.getTiaoName() + " find kuan is: " +
                relationShipLaw.getKuanName() + " xiang is:" + relationShipLaw.getXiangName());
        String relationshipDetail = relationShipLaw.getTiaoName() + relationShipLaw.getKuanName() + relationShipLaw.getXiangName();
        String release_date = law.getString("release_date");
        if (relationShipLaw.getLawName() == null || relationShipLaw.getLawName().isEmpty()) {
            return;
        }
        List<Document> findDocumentList = findLawWithTitleFromDb(relationShipLaw.getLawName());
        for (Document lawDocument : findLawWithTitleFromDb("中华人民共和国" + relationShipLaw.getLawName())) {
            findDocumentList.add(lawDocument);
        }
        Graph graph = new Graph();
        for (Document findDocumentLaw : findDocumentList) {
            String implement_date = findDocumentLaw.getString("implement_date");
            //找到实施时间比当前法律发布时间最大小的一个
            if (implement_date == null || implement_date.isEmpty() || release_date.compareTo(implement_date) > 0) {
                String lawId = findDocumentLaw.getObjectId("_id").toString();
                //如果法律名后没有条
                if (relationShipLaw.getTiaoName() == null || relationShipLaw.getTiaoName().isEmpty()) {
                    graph.createRelationshipLawAuto(id, lawId, relationShipTag, relationshipDetail);
                    return;
                }
                //如果法律有条，后面没有款信息
                if (relationShipLaw.getKuanName() == null || relationShipLaw.getKuanName().isEmpty()) {
                    String tiaoName = relationShipLaw.getTiaoName().trim();
                    List<Document> documentList = (List<Document>) findDocumentLaw.get("articles");
                    for (int i = 0; i < documentList.size(); i++) {
                        if (documentList.get(i).getString("name").trim().equals(tiaoName)) {
                            String articleId = lawId + "-" + i;
                            graph.createRelationshipLawAuto(id, articleId, relationShipTag, relationshipDetail);
                            break;
                        }
                    }
                    return;
                }
                //如果法律后有条有款，后面没有项信息或者有项信息（当前只精确到款，项的不做处理）
                String tiaoName = relationShipLaw.getTiaoName().trim();
                List<Document> documentList = (List<Document>) findDocumentLaw.get("articles");
                for (int i = 0; i < documentList.size(); i++) {
                    if (documentList.get(i).getString("name").trim().equals(tiaoName)) {
                        String articleId = lawId + "-" + i;
                        int kuanNum = relationShipLaw.getKuanNum() - 1;
                        List<String> para = (List<String>) documentList.get(i).get("paragraph");
                        if (para.size() > kuanNum) {
                            String paraId = articleId + "-" + kuanNum;
                            graph.createRelationshipLawAuto(id, paraId, relationShipTag, relationshipDetail);
                        } else {
                            graph.createRelationshipLawAuto(id, articleId, relationShipTag, relationshipDetail);
                        }
                        break;
                    }
                }
                return;
            }
        }
    }

    public List<Document> findLawWithTitleFromDb(String title) {
        List<Document> findDocumentList = new ArrayList<>();
        FindIterable<Document> findIterable = this.toCollection.find(new Document("title", title)).sort(new Document("release_date", -1)).noCursorTimeout(true);
        MongoCursor<Document> cursor = findIterable.iterator();
        try {
            while (cursor.hasNext()) {
                Document findDocumentLaw = cursor.next();
                findDocumentList.add(findDocumentLaw);
            }
            return findDocumentList;
        } catch (Exception e) {
            LOGGER.error(title + " find document from db err: " + e);
            return findDocumentList;
        } finally {
            cursor.close();
        }
    }

    public void findLawNameFromDbAndCreateRelationBack(Document law, String id, String relationShipTag, RelationShipLaw relationShipLaw) {
        String release_date = law.getString("release_date");
        String relationshipDetail = relationShipLaw.getTiaoName() + relationShipLaw.getKuanName() + relationShipLaw.getXiangName();
        LOGGER.info("find name is: " + relationShipLaw.getLawName() + " releaseTime: " + relationShipLaw.getReleaseTime());
        String theLawReleasDate = relationShipLaw.getReleaseTime();
        if(relationShipLaw.getLawName() == null || relationShipLaw.getLawName().isEmpty()){
            return;
        }
        //根据文本中的检索的法律名查找法律库，找到实施时间比当前法律发布时间最大小的一个
        FindIterable<Document> findIterable = this.toCollection.find(new Document("title", relationShipLaw.getLawName())).sort(new Document("release_date", -1)).noCursorTimeout(true);
        MongoCursor<Document> cursor = findIterable.iterator();
        Graph graph = new Graph();
        try {
            while (cursor.hasNext()) {
                Document findDocumentLaw = cursor.next();
                String findDocumentLaw_relase_date = findDocumentLaw.getString("release_date");
                Date findDocumentLawDate = DateParse.getStringToDate(findDocumentLaw_relase_date);
                if (theLawReleasDate == null || theLawReleasDate.isEmpty()) {
                    if (release_date.compareTo(findDocumentLaw_relase_date) > 0) {
                        String lawId = findDocumentLaw.getObjectId("_id").toString();
                        graph.createRelationshipLawAuto(id, lawId, relationShipTag, relationshipDetail);
                        break;
                    }
                    continue;
                }
                if (theLawReleasDate.contains("年") && theLawReleasDate.contains("月") && (theLawReleasDate.contains("日") || theLawReleasDate.contains("号"))) {
                    Date documentDate = DateParse.getStringToDate(theLawReleasDate.trim());
                    if (DateParse.getYear(documentDate) == DateParse.getYear(findDocumentLawDate) &&
                            DateParse.getMonthOfYear(documentDate) == DateParse.getMonthOfYear(findDocumentLawDate) &&
                            DateParse.getDayofMonth(documentDate) == DateParse.getDayofMonth(findDocumentLawDate)) {
                        String lawId = findDocumentLaw.getObjectId("_id").toString();
                        graph.createRelationshipLawAuto(id, lawId, relationShipTag, relationshipDetail);
                    }
                    continue;
                }
                if (theLawReleasDate.contains("年") && theLawReleasDate.contains("月")) {
                    Date documentDate = DateParse.getStringToDate(theLawReleasDate.trim());
                    if (DateParse.getYear(documentDate) == DateParse.getYear(findDocumentLawDate) &&
                            DateParse.getMonthOfYear(documentDate) == DateParse.getMonthOfYear(findDocumentLawDate)) {
                        String lawId = findDocumentLaw.getObjectId("_id").toString();
                        graph.createRelationshipLawAuto(id, lawId, relationShipTag, relationshipDetail);
                    }
                    continue;
                }
                if (theLawReleasDate.contains("年")) {
                    Date documentDate = DateParse.getStringToDate(theLawReleasDate.trim());
                    if (DateParse.getYear(documentDate) == DateParse.getYear(findDocumentLawDate)) {
                        String lawId = findDocumentLaw.getObjectId("_id").toString();
                        graph.createRelationshipLawAuto(id, lawId, relationShipTag, relationshipDetail);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("findLawNameFromDbAndCreateRelationBack err: " + e);
        } finally {
            LOGGER.info("findLawNameFromDbAndCreateRelationBack done");
            cursor.close();
        }
    }
}

