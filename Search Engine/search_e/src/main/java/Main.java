import ca.rmen.porterstemmer.PorterStemmer;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.io.BsonOutput;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.print.DocFlavor;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class Main {
    static MongoClient client = MongoClients.create("mongodb+srv://ahmedmadbouly:1030507090@cluster0.yenue.mongodb.net/test");
    static MongoDatabase db = client.getDatabase("searchengine");
    static MongoCollection col = db.getCollection("words2");
    static MongoCollection col2 = db.getCollection("Crawler2");

    static MongoCollection col3 = db.getCollection("words");

    public static void shrink_database() {
        HashMap<String, Document> map = new HashMap<String, Document>();//Creating HashMap
        FindIterable<Document> data_base = col.find();
        for (Document doc : data_base) {
            if (map.get(doc.get("word")) == null) {
                map.put((String) doc.get("word"),doc);
            }else{
                Document temp=map.get(doc.get("word"));
                temp.replace("df",temp.get("df"),(int)temp.get("df")+1);
                temp.append("doc"+String.valueOf((int)temp.get("df")),doc.get("doc1"));
                map.replace((String) doc.get("word"),map.get( doc.get("word")),temp);
            }
        }



        List<Document> doc_temp_list = new ArrayList<Document>();
        for (Map.Entry<String, Document> set : map.entrySet()) {
            List<String> doc1 = new ArrayList();
            doc_temp_list.add(set.getValue());
        }

        Bson bson = eq("asaشصيsdw", null);
        col.deleteMany(bson);
        if(doc_temp_list.size()>0)
            col.insertMany(doc_temp_list);

    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        myMain main_2 = new myMain();
//        main_2.delel();
//        main_2.main_clawler(args);

        // write your code here



        //  MongoCollection col3 = db.getCollection("Crawler3");

        //delete collection


//        Document dsa=new Document("url","https://www.w3.org/TR/2011/WD-html-markup-20110113/syntax.html#cdata-sections");
//        col2.insertOne(dsa);
        Bson bson = eq("asaشصيsdw", null);
        //col2.deleteMany(bson);
        col.deleteMany(bson);

        Scanner sc = new Scanner(System.in);
        System.out.println("Input thread number : ");
        int N = sc.nextInt();
        Indexer[] arr_indexer = new Indexer[N];
        boolean[] visited = new boolean[N];
        Object obj = new Object();
        for (int i = 0; i < N; i++) {
            arr_indexer[i] = new Indexer(obj, client, db, col, col2);
            visited[i] = false;
        }
        FindIterable<Document> urls_docs = col2.find();
        int index = 0;
        for (Document doc_url : urls_docs) {
            while (true) {
                if (!arr_indexer[index].isAlive()) {
                    if (visited[index]) {
                        arr_indexer[index] = new Indexer(obj, client, db, col, col2);
                        visited[index] = false;
                    }
                    arr_indexer[index].set_url(doc_url);
                    arr_indexer[index].start();
                    visited[index] = true;
                    index++;
                    index = index % N;
                    break;
                } else {
                    index++;
                    index = index % N;
                }
            }
        }

        shrink_database();

        bson = eq("asaشصيsdw", null);
        col3.deleteMany(bson);

        FindIterable<Document> f = col.find();
        for (Document d: f
        ) {
            col3.insertOne(d);
        }
    }
}