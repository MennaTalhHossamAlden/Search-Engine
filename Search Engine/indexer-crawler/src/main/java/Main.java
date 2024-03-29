import ca.rmen.porterstemmer.PorterStemmer;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;


import java.io.*;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class Main {
    static MongoClient client = MongoClients.create("mongodb://localhost:27017");
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
        if(doc_temp_list.size()>0)
            col3.insertMany(doc_temp_list);

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
// some time passes

        Bson bson = eq("asaشصيsdw", null);
        //col2.deleteMany(bson);
        col.deleteMany(bson);

        Scanner sc = new Scanner(System.in);
        System.out.println("Input thread number : ");
        int N = sc.nextInt();
        Indexer[] arr_indexer = new Indexer[N];
        boolean[] visited = new boolean[N];
        Object obj = new Object();
        int count_of_doc=0;
        for (int i = 0; i < N; i++) {
            arr_indexer[i] = new Indexer(obj, client, db, col, col2,count_of_doc);
            visited[i] = false;
        }
        FindIterable<Document> urls_docs =( FindIterable<Document> ) col2.find().limit((int)col2.countDocuments());
//        FindIterable<Document> urls_docs = col2.find();
        int index = 0;
        for (Document doc_url : urls_docs) {
            while (true) {
                if (!arr_indexer[index].isAlive()) {
                    if (visited[index]) {
                        arr_indexer[index] = new Indexer(obj, client, db, col, col2,count_of_doc);
                        visited[index] = false;
                    }
                    arr_indexer[index].set_url(doc_url);
                    arr_indexer[index].start();
                    count_of_doc++;
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

        for (Indexer thread : arr_indexer) {
            thread.join();
        }

        bson = eq("asaشصيsdw", null);
        col3.deleteMany(bson);

        shrink_database();

        System.out.println("inserted");
        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        System.out.println(elapsedTime);
    }
}