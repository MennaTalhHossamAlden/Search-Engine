import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.ArrayList;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;

public class Crawler implements Runnable {
    static int index = 0;
    String url;
    final Object obj0;
    final Object obj1;
    final Object obj2;
    final Object obj3;
    char[] first_char = new char[50];
    CyclicBarrier barrier;

    public Crawler(Object obj0, Object obj1, Object obj2, Object obj3, CyclicBarrier barrier) {
        //this.array_compact.add("element");
        this.obj0 = obj0;
        this.obj1 = obj1;
        this.obj2 = obj2;
        this.obj3 = obj3;
        this.barrier = barrier;
    }

    public boolean check_index(String url) throws IOException {
        URL URL = new URL(url);
        URLConnection connection = URL.openConnection();
        String redirect = connection.getHeaderField("Location");
        if (redirect != null) {
            connection = new URL(redirect).openConnection();
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            // br.close();
            return true;
        } catch (IOException ex231) {
            return false;
            // System.out.println("something wrong");
        }

    }

    public void run() {
        String ur = "mongodb://localhost:27017";
        MongoClient client = MongoClients.create(ur);

        MongoDatabase db = client.getDatabase("searchengine");
        MongoCollection col0 = db.getCollection("Crawler0");
        MongoCollection col1 = db.getCollection("Crawler1");
        MongoCollection col2 = db.getCollection("Crawler2");
        MongoCollection arr_com = db.getCollection("Array_Compact");

        index = (int) col2.countDocuments();

        ///////////////////////////////////////////////////////////////////////////////////////////////////video rec recrawling part
        while (col1.countDocuments() != 0) {
            String U_R_L;
            synchronized (obj0) {
                if (col1.countDocuments() == 0) {
                    continue;
                }
                org.bson.Document d = (org.bson.Document) col1.find().first();
                U_R_L = (String) d.get("url");

                Bson query = eq("url", U_R_L);
                col1.deleteOne(query);
            }
            if (U_R_L != null) {
                Document doc = request(U_R_L, arr_com);
                // boolean
                try {
                    if (doc != null && check_index(U_R_L)) {
                        for (Element link : doc.select("a[href^=http://]")) {
                            String next = link.absUrl("href");
                            synchronized (obj1) {
                                if ((org.bson.Document) col2.find(eq("url", next)).first() == null) {
                                    org.bson.Document temp = new org.bson.Document("url", next);
                                    col0.insertOne(temp);
                                }
                                else {
//                                    System.out.println("counter increased");
                                    org.bson.Document doc4 = (org.bson.Document) col2.find(eq("url", next)).first();
                                    col2.deleteOne(doc4);
                                    int c = (int)doc4.get("counter");
                                    c++;
                                    doc4.replace("counter",c);
                                    col2.insertOne(doc4);
                                }
                            }
                        }
                        synchronized (obj2) {
                            if ((org.bson.Document) col2.find(eq("url", U_R_L)).first() == null) {
                                System.out.println("Link " + index + "  " + U_R_L);
                                index++;
                                col2.insertOne(new org.bson.Document("url", U_R_L).append("counter",1).append("text",doc.text()).append("title",doc.title()));
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        try {
            this.barrier.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
        while (index < 100) {
            String U_R_L="";
            Bson query;
            synchronized (obj0) {
                if(col0.countDocuments()==0) {
                    continue;
                }
                org.bson.Document d = (org.bson.Document) col0.find().first();
                U_R_L = (String) d.get("url");
                query = eq("url", U_R_L);
                col0.deleteMany(query);
                col1.insertOne(new org.bson.Document("url", U_R_L));
            }
            Document doc = request(U_R_L, arr_com);

            try {
                if (doc != null && check_index(U_R_L)) {
                    for (Element link : doc.select("a[href^=http://]")) {
                        String next = link.absUrl("href");
                        synchronized (obj1) {
                            if ((org.bson.Document) col2.find(eq("url", next)).first() == null) {
                                org.bson.Document temp = new org.bson.Document("url", next);
                                col0.insertOne(temp);
                            }
                            else {
//                                System.out.println("counter increased");
                                org.bson.Document doc4 = (org.bson.Document) col2.find(eq("url", next)).first();
                                col2.deleteOne(doc4);
                                int c = (int)doc4.get("counter");
                                c++;
                                doc4.replace("counter",c);
                                col2.insertOne(doc4);
                            }
                        }
                    }
                    synchronized (obj2) {
                        if ((org.bson.Document) col2.find(eq("url", U_R_L)).first() == null) {
                            System.out.println("Link " + index + "  " + U_R_L);
                            index++;
                            col2.insertOne(new org.bson.Document("url", U_R_L).append("counter",1).append("text",doc.text()).append("title",doc.getElementsByTag("title").text()));
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            query = eq("url", U_R_L);
            col1.deleteOne(query);
        }
    }
    private Document request(String url, MongoCollection arr_com) {
        try {
            Connection con = Jsoup.connect(url);
            Document doc = con.get();

            if (con.response().statusCode() == 200) {//it means it's okay to visit this website

                URL u = new URL(url);
                String k = u.getProtocol() + "://" + u.getHost() + "/";
                k = k + "robots.txt";

                Connection con2 = Jsoup.connect(k);
                Document doc2 = con2.get();

                String myS = doc2.text();

                if (myS.contains("User-agent: *")) {
                    String all_search = "User-agent: \\*";
                    String[] temp1 = myS.split(all_search, 2);
                    //System.out.printf("we tried %s \n",url);
                    String[] temp3 = temp1[1].split("User-agent:");

                    ArrayList<String> arr_of_dis = new ArrayList<String>();

                    while (temp3[0].contains("Disallow:")) {
                        String myT = temp3[0].substring(temp3[0].indexOf("Disallow: ") + 10, temp3[0].length());
                        temp3[0] = myT;
                        if (myT.length() > 100) {
                            myT = myT.substring(0, myT.indexOf(" "));
                            arr_of_dis.add(myT);
                        }

                        if (myT.equals("/"))
                            return null;
                    }
                    String section = u.getPath();

                    for (String o : arr_of_dis) {

                        if (section.contains(o) && !o.isEmpty()) {
                            System.out.printf("we were checking %s\n we can't visited this link due to the robots.txt\n", url);
                            return null;
                        }

                    }

                    arr_of_dis.clear();
                }

                String htmlString = doc.text();
                String[] arr = htmlString.split(" ");

                int loop_on = Math.min(arr.length, 100);

                int i = 0;
                for (int j = 0; j < loop_on; j += 2) {
                    if (arr[j].length() != 0) {
                        first_char[i] = arr[j].charAt(0);
                        i++;
                    }
                }
                String s = String.valueOf(first_char);

                synchronized (obj3) {
                    if ((org.bson.Document) arr_com.find(eq("compact_string", s)).first() != null)
                        return null;
                    arr_com.insertOne(new org.bson.Document("compact_string", s));
                }

                return doc;
            }

        } catch (IOException e) {
            return null;
        }
        return null;
    }

}
