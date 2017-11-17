import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;

import java.text.ParseException;
import java.util.Arrays;

import Mongo.MongoDB;
import org.bson.BsonType;
import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import com.mongodb.MongoNamespace;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.annotations.ThreadSafe;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.RenameCollectionOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

/**
 * @Author : Administrator
 * @Date : 2017/11/17 15:36
 * @Description :
 */
public class Main5 {
    public static void main(String[] args){
        MongoDB mongoDB = MongoDB.getMongoDB();
        System.out.println(mongoDB.collection.getNamespace());

        FindIterable<Document> findIterable = mongoDB.collection.find(new Document("url","http://www.pkulaw.cn/fulltext_form.aspx?Db=chl&Gid=a5270ef910a13181&keyword=&EncodingName=&Search_Mode=&Search_IsTitle=0"));
        findIterable.forEach(new Block<Document>() {
            public void apply(final Document document) {
                System.out.println(document.get("title"));
            }
        });
        if (findIterable.first() == null){
            System.out.println("aaa");
        }

        String title = "中华人民共和国海关法(2017北大法宝整理版)";
        String regEx_delet = "\\(.*北大法宝.*\\)";//定义空格回车换行符
        Pattern p_space = Pattern.compile(regEx_delet, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(title);
        title = m_space.replaceAll("");
        System.out.println(":" + title);
//        lawDocument.setTitle(title);
//        if(mongoDB.collection.find(new Document("department","全国人民代表大会")).to != null){
//            System.out.println("no");
//        }else {
//            System.out.println("yes");
//        }
    }
}
