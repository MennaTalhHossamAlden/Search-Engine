import ca.rmen.porterstemmer.PorterStemmer;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.io.BsonOutput;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.print.DocFlavor;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class Indexer extends Thread {
    Object obj;
    HashMap<String, Integer> map = new HashMap<String, Integer>();//Creating HashMap
    HashMap<String, List<Integer>> words_map = new HashMap<String, List<Integer>>();//Creating HashMap

    int useful_tags = 0;
    int dis_useful_tags = 0;
    boolean dont_care = false;

    MongoClient client;
    MongoDatabase db;
    MongoCollection col ;
    MongoCollection col2 ;

    int total_words = 0;
    boolean finish = false;
    boolean end = false;
    int counter = 0;
    int total = 0;
    String tag = "";
    String word = "";
    int i = 0;
    String url = "";
    Document doc_url;
    int count_of_doc;
    String text_crawl="";

    public Indexer(Object obj,MongoClient client,MongoDatabase db,MongoCollection col,MongoCollection col2,int count_of_doc) {
        this.client=client;
        this.col=col;
        this.col2=col2;
        this.obj = obj;
        this.count_of_doc = count_of_doc;
        hash(map);
    }

    private void hash(HashMap<String, Integer> map) {
        File file = new File(
                "stop_words.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String st = "";

        while (true) {
            try {
                if (!((st = br.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            map.put(st, -1);
        }
    }

    private int clc_priority(String s) {
        s = s.toLowerCase();
        switch (s) {

            case "title":
                dont_care = false;
                useful_tags++;
                return 10;

            case "h1":
            case "h2":
            case "h3":
            case "h4":
            case "h5":
            case "h6":
                dont_care = false;
                useful_tags++;
                return 10 - Integer.parseInt(s.substring(1, 2));
            case "span":
            case "em":
            case "strong":
            case "b":
            case "big":
            case "u":

                dont_care = false;
                useful_tags++;
                return 3;
            case "p":
                dont_care = false;
                useful_tags++;
                return 2;
            case "div":
            case "a":
            case "head":
            case "body":
            case "code":
            case "dfn":
            case "pre":
            case "li":
            case "ol":
            case "ul":
            case "i":
            case "html":
            case "header":
            case "nav":
            case "main":
            case "aside":
            case "footer":
            case "center":
            case "font":
            case "form":
            case "marquee":
            case "menu":
            case "option":
            case "small":
            case "table":
            case "dd":
            case "dl":
            case "dt":
            case "th":
            case "cite":
                dont_care = false;
                useful_tags++;
                return 1;
            default:
                dont_care = true;
                dis_useful_tags++;
                return 0;
        }

    }

    private boolean has_end(String tag) {
        tag = tag.toLowerCase();
        switch (tag) {
            case "area":
            case "base":
            case "br":
            case "col":
            case "embed":
            case "hr":
            case "img":
            case "input":
            case "keygen":
            case "link":
            case "meta":
            case "param":
            case "source":
            case "track":
            case "wbr":
            case "!--":
            case "dt":
            case "li":
            case "option":

                return false;
            default:
                return true;

        }
    }


    private void insert_word(String word) {
        synchronized (obj) {
            Document doc_word = (Document) col.find(new Document("word", word.toLowerCase())).first();
            if (doc_word == null) {
                doc_word = new Document("word", word).append("df", 1);
                List<String> temp_list = new ArrayList<String>();
                temp_list.add(url);
                temp_list.add("1");
                temp_list.add(String.valueOf(total));
                temp_list.add(String.valueOf(counter));
                temp_list.add(String.valueOf(i));
                doc_word.append("doc1", temp_list);
                col.insertOne(doc_word);

            } else {
                Document updated_doc = new Document("word", word);
                int df = (int) doc_word.get("df");
                boolean exist = false;
                int pos_exist = 0;
                for (int j = 1; j <= df; j++) {
                    List<String> tem_lis = (List<String>) doc_word.get("doc" + String.valueOf(j));
                    if (tem_lis.get(0).equalsIgnoreCase(url) && counter <= Integer.parseInt(tem_lis.get(3))
                            && i <= Integer.parseInt(tem_lis.get(4))) {
                        exist = true;
                        pos_exist = j;
                    }
                }
                if (exist) {
                    updated_doc.append("df", df);
                    List<String> tem_lis = (List<String>) doc_word.get("doc" + String.valueOf(pos_exist));
                    List<String> temp_list = new ArrayList<String>();
                    for (int j = 1; j <= df; j++) {
                        if (j != pos_exist)
                            updated_doc.append("doc" + String.valueOf(j), doc_word.get("doc" + String.valueOf(j)));
                    }
                    temp_list.add(url);
                    temp_list.add("1");
                    temp_list.add(String.valueOf(total));
                    temp_list.add(String.valueOf(counter));
                    temp_list.add(String.valueOf(i));
                    updated_doc.append("doc" + String.valueOf(pos_exist), temp_list);

                    col.replaceOne(doc_word, updated_doc);
                } else {
                    List<String> tem_lis = (List<String>) doc_word.get("doc" + String.valueOf(df));
                    List<String> temp_list = new ArrayList<String>();
                    if (!tem_lis.get(0).equalsIgnoreCase(url)) {
                        df += 1;
                    }
                    updated_doc.append("df", df);
                    for (int j = 1; j < df; j++) {
                        updated_doc.append("doc" + String.valueOf(j), doc_word.get("doc" + String.valueOf(j)));
                    }
                    if (!tem_lis.get(0).equalsIgnoreCase(url)) {
                        temp_list.add(url);
                        temp_list.add("1");
                        temp_list.add(String.valueOf(total));
                    } else {
                        temp_list.add(tem_lis.get(0));
                        temp_list.add(String.valueOf(Integer.parseInt(tem_lis.get(1)) + 1));
                        temp_list.add(String.valueOf(total + Integer.parseInt(tem_lis.get(2))));
                    }
                    temp_list.add(String.valueOf(counter));
                    temp_list.add(String.valueOf(i));
                    updated_doc.append("doc" + String.valueOf(df), temp_list);
                    col.replaceOne(doc_word, updated_doc);
                }
            }
        }
    }

    public void set_url(Document temp_url) {
        this.doc_url = temp_url;
    }

    @Override
    public void run() {

        String val = "";
        url = (String) doc_url.get("url");
        URL URL = null;
        URLConnection connection = null;
        try {
            URL = new URL(url);
            connection = URL.openConnection();
            String redirect = connection.getHeaderField("Location");
            if (redirect != null) {
                connection = new URL(redirect).openConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (IOException ex231) {
            System.out.println("something wrong");
            col2.deleteOne(doc_url);
            doc_url.append("total_words", 0);
            col2.insertOne(doc_url);
            return;
        }
        total_words = 0;
        finish = false;
        end = false;
        counter = 0;
        total = 0;
        tag = "";
        word = "";

        while (true) {
            try {
                if ((val = br.readLine()) == null) break;
            } catch (IOException e) {
                //  e.printStackTrace();
                break;
            }
            counter++;
            for (i = 0; i < val.length(); i++) {
                if (dont_care) {
                    ///// we couldnt read words here but we need to get out from this mode
                    //// so here we only check when to end this mode
                    //// when the tage that cases this dont care ended then the dont care will be false
                    switch ((char) val.charAt(i)) {
                        case '>':
                            finish = !finish;
                            end = end || !has_end(tag);
                            dont_care = !end;
                            if (end) dis_useful_tags--;
                            end = !end && !has_end(tag);
                            break;
                        case '<':
                            end = true;
                            break;
                    }
                    continue;
                }
                if ((char) val.charAt(i) == '<') { //open tag
                    finish = false;
                    tag = "";
                    i++;
                    //// ignoe spaces
                    while (i < val.length() && (char) val.charAt(i) == ' ') {
                        i++;
                    }
                    //close tag or normal tag
                    end = i < val.length() && (char) val.charAt(i) == '/';
                    if (end) {
                        i++;
                        useful_tags--;
                    }
                    while (i < val.length() && (char) val.charAt(i) != ' ' && (char) val.charAt(i) != '>') {
                        //read tag
                        tag = tag + val.charAt(i++);
                    }
                } else if ((char) val.charAt(i) == '/' && i > 1 && (char) val.charAt(i - 1) == '<') {
                    end = true;
                    useful_tags--;
                }
                if (i < val.length() && (char) val.charAt(i) == '>') {
                    // here we handle the nested tags
                    finish = true;
                    end = false;
                    continue;
                }

                ///////// here read the word
                if (finish && !end) {
                    int p = clc_priority(tag);
                    if (dont_care)
                        continue;
                    total = p;

                    while (i < val.length()) {
                        if ((char) val.charAt(i) != ' ' && (char) val.charAt(i) != '<')
                            word = word + val.charAt(i++);
                        else {

                            word = word.toLowerCase();
                            String test_punc = word.replaceAll("\\p{Punct}", "");
                            if (test_punc.length() != 0 && word.length() != 0) {
                                word = test_punc;
                            }
                            text_crawl+=word;
                            text_crawl+=" ";
                            PorterStemmer stem = new PorterStemmer();
                            word = (stem.stemWord(word));

                            if (word.equals(""))
                                break;
                            if (tag.equalsIgnoreCase("title")) {
                                map.replace(word.toLowerCase(), 1);
                            }
                            if (map.get(word.toLowerCase()) == null || map.get(word.toLowerCase()) != -1) {
                                ///////// make document , then breake
                                ////////    insert in database  //////////
                                total_words++;

                                // System.out.println(word);
                                //insert_word(word);
                                if (words_map.get(word) == null) {
                                    List<Integer> list = new ArrayList();
                                    list.add(1);
                                    list.add(total);
                                    words_map.put(word, list);
                                } else {
                                    List<Integer> list = words_map.get(word);
                                    list.set(0, list.get(0) + 1);
                                    list.set(1, list.get(1) + total);
                                    words_map.replace(word, words_map.get(word), list);
                                }
                            }
                            word = "";
                            break;
                        }
                    }
                }
            }
        }
        insert_words();
    }

    private void insert_words() {
        synchronized (obj) {
            List<Document> doc_temp_list = new ArrayList<Document>();
            for (Map.Entry<String, List<Integer>> set : words_map.entrySet()) {
                List<String> doc1 = new ArrayList();
                doc1.add(url);
                doc1.add(String.valueOf(set.getValue().get(0)));
                doc1.add(String.valueOf(set.getValue().get(1)));
                Document doc = new Document("word", set.getKey()).append("df", 1).append("doc1", doc1);
                doc_temp_list.add(doc);
            }
            System.out.println(count_of_doc);
            System.out.print("finsih indexing  url :");
            System.out.println(url);
            System.out.println("total number of words " + total_words);
            System.out.println("-----------------------------------");
            if (total_words != 0)
                col.insertMany(doc_temp_list);
            col2.deleteOne(doc_url);
            doc_url.append("total_words", total_words);
            doc_url.append("text", text_crawl);
            col2.insertOne(doc_url);
        }
    }
}