import java.io.*;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

import org.bson.Document;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import static com.mongodb.client.model.Filters.eq;

import ca.rmen.porterstemmer.PorterStemmer;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.*;


import java.util.List;
class QProcessor {
    MongoClient client;
    MongoDatabase db;
    MongoCollection col;
    String query = "";
    String[] temp_query;

    String[] original_query;
    List<String> temp_stop_words = new ArrayList();
    boolean only_stops = true;
    HashMap<String, Integer> map = new HashMap<String, Integer>();//Creating HashMap

    public static void hash(HashMap<String, Integer> map) {
        File file = new File(
                "C:\\Users\\Asus\\Desktop\\face\\myproj\\stop_words.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Declaring a string variable
        String st = "";
        // Condition holds true till
        // there is character in a string
        while (true) {
            try {
                if ((st = br.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            map.put(st, -1);
        }
    }

    public QProcessor(MongoClient client, MongoDatabase db, MongoCollection col) {
        this.client = client;
        this.db = db;
        this.col = col;
        hash(map);
    }

    public void setQuery(String s) {
        this.query = s;
    }

    public String getQuery() {
        return this.query;
    }

    public void process() {
        int stop_words_counter = 0;
        int spaces_counter = 0;
        int normal_words_counter = 0;
        only_stops = true;
        temp_query = query.split(" ");
        original_query = query.split(" ");
        temp_stop_words = new ArrayList();
        PorterStemmer stem = new PorterStemmer();

        for (int i = 0; i < temp_query.length; i++) {


            if (temp_query[i].equals(" ")) {
                spaces_counter++;
                temp_query[i] = "";
            } else if (map.get(temp_query[i].toLowerCase()) != null) {
                stop_words_counter++;
                temp_stop_words.add(temp_query[i]);
                temp_query[i] = "";
            } else {
                only_stops = false;
                normal_words_counter++;
                String test_punc = temp_query[i].replaceAll("\\p{Punct}", "");
                if (test_punc.length() != 0 && temp_query[i].length() != 0) {
                    temp_query[i] = temp_query[i].replaceAll("\\p{Punct}", "");
                }
                temp_query[i] = (stem.stemWord(temp_query[i]));
            }
        }
        for (int i = 0; i < temp_query.length; i++) {
            if(temp_query[i]!="")
                System.out.print(temp_query[i]+" ");
        }
    }

    public List<Document> query_proc(String query, ArrayList<String> my_l) {
        while (query.length() > 0 && query.charAt(0) == ' ') {
            query = query.substring(1, query.length() - 1);
        }

        List<String> Arr_non_Phrase = new ArrayList<String>();
        List<String> Arr_Phrase = new ArrayList<String>();
        List<Document> temp = new ArrayList<Document>();
        List<Document> ret = new ArrayList<Document>();
        String whole_query_string = "";
        char dummy;
        String collector = "";
        boolean started_a_phrase = false;


        for (int i = 0; i < query.length(); i++) {

            dummy = query.charAt(i);
            System.out.println(dummy); //////////////////////////////////test zone

            if (dummy == ' ' && collector == "") continue;
            else if (dummy == '"') {
                if (started_a_phrase) {
                    {
                        Arr_Phrase.add(collector);
                        whole_query_string += collector + " ";
                    }
                    collector = "";
                } else if (collector != "") {
                    Arr_non_Phrase.add(collector);
                    whole_query_string += collector + " ";
                    collector = "";
                }
                started_a_phrase = !started_a_phrase;
            } else if (started_a_phrase) {
                collector += dummy;
            } else if (dummy == ' ' && !started_a_phrase && collector != "") {
                Arr_non_Phrase.add(collector);
                whole_query_string += collector + " ";
                collector = "";
            } else  //non phrase
            {
                collector += dummy;
                if (i == query.length() - 1) {
                    Arr_non_Phrase.add(collector);
                    whole_query_string += collector + " ";
                }
            }
        }
        System.out.println("whole query");
        if ((Arr_non_Phrase.size() != 0 && Arr_Phrase.size() != 0) || Arr_non_Phrase.size() > 1) {
            //call phrase search on the whole query here with parameter: List called WholeQuery that contains the only one element that is the whole query without "
            temp = phase_search(whole_query_string);
            ret.addAll(temp);
            System.out.println(whole_query_string);//////////////////////test zone
        }
        System.out.println("phrase array");
        for (int i = 0; i < Arr_Phrase.size(); i++) {
            //call here phrase search
            temp = phase_search(Arr_Phrase.get(i));
            ret.addAll(temp);
            System.out.println(Arr_Phrase.get(i));
        }
        System.out.println("non phrase array");
        for (int i = 0; i < Arr_non_Phrase.size(); i++) {
            //call here query processor
            setQuery(Arr_non_Phrase.get(i));
            process();
            temp = retrive();
            ret.addAll(temp);
            System.out.println(Arr_non_Phrase.get(i));
        }
        my_l.add(whole_query_string);
        return ret;
    }
        public List<Document> retrive() {

            List<Document> docs = new ArrayList<Document>();
            if (!only_stops) {
                for (int i = 0; i < temp_query.length; i++) {
                    if (temp_query[i] != "") {
                        Document temp = (Document) col.find(eq("word", temp_query[i])).first();
                        if (temp != null)
                            temp.append("query_word", original_query[i]);
                        System.out.print(temp_query[i] + " ");
                        if (temp != null) {
                            docs.add(temp);
                        }
                    }
                }
            } else {
                for (int i = 0; i < temp_stop_words.size(); i++) {
                    Document temp = (Document) col.find(eq("word", temp_stop_words.get(i))).first();
                    if (temp != null)
                        temp.append("query_word", original_query[i]);
                    System.out.print(temp_stop_words.get(i) + " ");
                    if (temp != null) {
                        docs.add(temp);
                    }
                }
            }
            return docs;
        }
    public static List<Document> phase_search(String s) {
        HashMap<String, Document> GoToRank = new HashMap<String, Document>();//Creating HashMap
        List<Document> returned = new ArrayList<Document>();
        String ur = "mongodb://localhost:27017";
        MongoClient client = MongoClients.create(ur);
        MongoDatabase db = client.getDatabase("searchengine");
        MongoCollection col = db.getCollection("words");
        MongoCollection col2 = db.getCollection("Crawler2");

        String[] phrase = s.split(" ");
        int xy=0;
        for (int i = 0; i < phrase.length; i++) {
            PorterStemmer stem = new PorterStemmer();
            phrase[i] = (stem.stemWord(phrase[i]));
            Document doc_word = (Document) col.find(new Document("word", phrase[i])).first();
            if (doc_word != null) {
                int df = (int) doc_word.get("df");
                for (int j = 1; j <= df; j++) {
                    List<String> tem_lis = (List<String>) doc_word.get("doc" + String.valueOf(j));
                    if(col2.find(eq("url",tem_lis.get(0))).first()==null)
                        continue;
                    if (GoToRank.get((String) tem_lis.get(0)) == null) {
                       int tf = Integer.parseInt(tem_lis.get(1)) ;
                        List<String> tem_lis2 =new ArrayList<String>();
                        tem_lis2.add((String) tem_lis.get(0));
                        tem_lis2.add(String.valueOf(tf));
                        tem_lis2.add((String) tem_lis.get(2));
                        Document doc_word2 = new Document("word", s).append("doc1", tem_lis2);
                        GoToRank.put(tem_lis.get(0), doc_word2);
                    } else {
                        List<String> t = (List<String>) GoToRank.get((String) tem_lis.get(0)).get("doc1");
                        int tf = Integer.parseInt(tem_lis.get(1)) + Integer.parseInt(t.get(1)) ;
                        int priority = Integer.parseInt(tem_lis.get(2)) + Integer.parseInt(t.get(2));
                        List<String> toadd = new ArrayList<String>();
                        toadd.add((String) tem_lis.get(0));
                        toadd.add(String.valueOf(tf));
                        toadd.add(String.valueOf(priority));
                        Document doc_word2 = new Document("word", s).append("doc1", toadd);
                        GoToRank.put(tem_lis.get(0), doc_word2);
                    }
                }
            }
        }
        for (Map.Entry<String, Document> set : GoToRank.entrySet()) {
            // Printing all elements of a Map

            String text = (String)((Document)col2.find(eq("url",set.getKey())).first()).get("text");


                if (text.contains(s)) {
                    Document d = set.getValue();
                    d.append("df",1);
                    returned.add(d);
                }

        }
        return returned;
    }

}

class Ranker {

    public Ranker (List<Document> doc_list){
        String ur = "mongodb://localhost:27017";
        MongoClient client = MongoClients.create(ur);
        MongoDatabase db = client.getDatabase("searchengine");
        MongoCollection col2 = db.getCollection("Crawler2");
        MongoCollection Search_Index = db.getCollection("words");
        MongoCollection Ranker = db.getCollection("Ranker");
        Ranker.deleteMany(new BasicDBObject());

        ////////////////connect to database////////////////////////

        for (int i = 0; i < doc_list.size(); i++) {
            Document doc_word=doc_list.get(i);
            String word=(String) doc_word.get("word");
            String query_word=(String) doc_word.get("query_word");
            int df = (int) doc_word.get("df");
            for (int j = 1; j <= df; j++) {
                List<String> One_URL_Info = (List<String>) doc_word.get("doc" + String.valueOf(j));
                String url = One_URL_Info.get(0);
                double TF =  Integer.parseInt(One_URL_Info.get(1));
                Document doc_in_col2 = (Document) col2.find(eq("url", url)).first();
                if(doc_in_col2==null)
                    continue;


                int Doc_Total_words;
                if(doc_in_col2.get("total_words")!=null)
                    Doc_Total_words = (int)doc_in_col2.get("total_words");
                else Doc_Total_words=600;

                if(query_word==null) query_word = word;

                double Normalized_TF = TF/Doc_Total_words;
                if(Normalized_TF < 0.5) //spam
                {

                    int priority = Integer.parseInt(One_URL_Info.get(2));
                    int Doc_Popularity= (int)doc_in_col2.get("counter");
                    double IDF = Math.log( (double)col2.countDocuments() / df);
                    double TF_IDF = Normalized_TF * IDF;
                    double Rank = TF_IDF + priority ;
                    String mys = ((String)((Document)col2.find(eq("url",url)).first()).get("text"));
                    if(mys != null && mys.contains(query_word))
                    {
                        Rank*=10;
                    }
                    Document Found = (Document) Ranker.find(eq("URL", url)).first();
                    if (Found == null)
                        Ranker.insertOne(new Document("URL", url).append("Rank", Rank+ Doc_Popularity).append("words_counter",1).append("Word1",word));
                    else {
                        double new_Rank = Rank + (double) Found.get("Rank");
                        int new_words_counter = 1 + (int) Found.get("words_counter");

                        Document temp = new Document("URL", Found.get("URL")).append("Rank", new_Rank).append("words_counter", new_words_counter);
                        {
                            int k = 1;
                            while (true) {
                                if (Found.get("Word"+String.valueOf(k))!=null){
                                    temp.append("Word"+String.valueOf(k),Found.get("Word"+String.valueOf(k)));
                                }else
                                    break;
                                k++;
                            }
                        }
                        temp.append("Word"+String.valueOf(new_words_counter),word);
                        Ranker.replaceOne(Found,temp);

                    }

                }
            }
        }
    }
}

public class query extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String name = request.getParameter("search");
        String message =" ";
        int count = Integer.parseInt(request.getParameter("count"));
        int theme = Integer.parseInt(request.getParameter("theme"));

        name = name.toLowerCase(Locale.ROOT);

        String ur = "mongodb://localhost:27017";
        MongoClient client = MongoClients.create(ur);

        MongoDatabase db = client.getDatabase("searchengine");
        MongoCollection col2 = db.getCollection("Ranker");
        MongoCollection col3 = db.getCollection("Crawler2");
        MongoCollection old_q = db.getCollection("queries");
        MongoCollection<Document> col = db.getCollection("words");
        if((org.bson.Document) old_q.find(eq("query", name)).first() == null)
            old_q.insertOne(new org.bson.Document("_id",old_q.countDocuments()).append("query",name));

        ArrayList<String> my_l=new ArrayList<String>();
        if (count==1) {
            QProcessor search = new QProcessor(client, db, col);
            Ranker ranking = new Ranker(search.query_proc(name,my_l));
            name = my_l.get(0);
        }

        name = name.replaceAll("\"","");
        if(name.charAt(0)==' ')
            name = name.substring(1,name.length());
        if (name.charAt(name.length()-1)==' ')
            name = name.substring(0,name.length()-1);

        String[] arr_q = name.split(" ");


        int loop = (int)col2.countDocuments();
        double num_of_pages=loop/10.0;
        num_of_pages = (int)Math.ceil(num_of_pages);
        int end =Math.min(10,loop-10*(count-1));
        ArrayList<org.bson.Document> retrived = new ArrayList<org.bson.Document>();
        FindIterable<org.bson.Document> di = col2.find().sort(new org.bson.Document ("Rank",-1)).skip(10*(count-1)).limit(10);
        for(org.bson.Document doc:di)
        {
            retrived.add(doc);
        }


        outer:
        for (int i = 0; i < end; i++) {

            retrived = new ArrayList<org.bson.Document>();
            di = col2.find().sort(new org.bson.Document ("Rank",-1)).skip(10*(count-1)).limit(10);
            for(org.bson.Document doc:di)
            {
                retrived.add(doc);
            }
            org.bson.Document d = retrived.get(i);
            String url = (String) d.get("URL");
            d = (org.bson.Document)col3.find(eq("url",url)).first();
            String title=" ";
            if(((String)d.get("title")).equals(""))
            {
                URL u = new URL(url);
                title = u.getPath();
                title = title.substring(1,title.length());
            }
            else title = (String)d.get("title");

            String myS = (String)d.get("text");

            String new_name=" ";

            boolean myb=!myS.contains(" "+name+" ");
            if(myb)
            {
                for (int j = 0; j < arr_q.length; j++) {
                    if (myS.contains(arr_q[j]))
                    {
                        new_name=arr_q[j];
                        break;
                    }
                }
            }
            else {
                new_name = name;
            }

            int min = Math.min(90,myS.indexOf(" "+new_name+" "));
            int max = Math.min(90,myS.length()-myS.indexOf(" "+new_name+" "));
            String txt = myS.substring(myS.indexOf(" "+new_name+" ") -min, myS.indexOf(" "+new_name+" ") + max);
            String[] arr = txt.split(" ",2);
            if(arr.length>1)
            {
                txt = arr[1];
            }


            // check if there is an error or message in the text (we dont return the website)

            char[] arrk = txt.toCharArray();
            int myCounter=0;

            for (int j = 0; j < arrk.length; j++) {
                if(arrk[j]=='<')
                    myCounter++;
                else if(arrk[j]=='>')
                    myCounter--;
                if(myCounter==-1)
                {
                    col2.deleteOne(eq("url",url));
                    continue outer;
                }
            }

            if(myCounter!=0)
            {
                col2.deleteOne(eq("url",url));
                continue;
            }

            // end of check
            if(txt.length()<2) continue;

            String[] splitaia = txt.split(" "+new_name+" ",2);

            message+="<div class=\"sign cards \">\n<div><div><h2 style=\"margin: 0;\"><a href=\""+url+"\" title=\"Go to the site\">"+title+"</a></h2></div>" +"<p>"+url+"</p>"+"<div class=\"kiki\"> "+splitaia[0];
            if(splitaia.length>1)
            {
                message+=" <strong>"+ " "+new_name+" " +"</strong> "+splitaia[1];
            }
            message+="...</div></div></div><br>";
        }

        response.setContentType("text/html");

        String page = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n";
        if (theme==1)
            page += "<style>\n" +
                    "        :root {\n" +
                    "                --main-color: #2b373d;\n" +
                    "                --second-color: white;\n" +
                    "                --header-box-shadow: rgb(167 64 0 / 36%);\n" +
                    "                --header-colors1: #6e7e87;\n" +
                    "                --header-colors2: #151f25;\n" +
                    "                --title-color: #8ab4f8;\n" +
                    "                --visited-color: #c58af9;\n" +
                    "                --hoverd-links: #70a7ff;\n" +
                    "                --snippet-color: white;\n" +
                    "                --color-of-goon: rgb(255 255 255);\n" +
                    "                --color-of-goon-hover: #c4c4c4;\n" +
                    "                --no-res: #4e6f83;\n" +
                    "                --footer-hover: #869ba654;;\n" +
                    "--list-color:#869ba6;\n"+

                    "             --cards-imput-placeholder: #b7b7b742;\n" +
                    "                --link-color: #009f00;\n" +
                    "        }\n" +
                    "\n" +
                    "</style> \n" +
                    "\n" +
                    "\n" ;
        else if(theme==0)
            page += "<style>\n" +
                    "        :root {\n" +
                    "            --main-color:white;\n" +
                    "            --second-color:black;\n" +
                    "            --header-box-shadow: rgb(70, 70, 70);\n" +
                    "            --header-colors1:#5e5e5e;\n" +
                    "            --header-colors2:#ececec;\n" +
                    "            --title-color:#0000c3;\n" +
                    "            --visited-color: #4e0097;\n" +
                    "            --hoverd-links: #0000ff;\n" +
                    "            --snippet-color: black;\n" +
                    "            --color-of-goon: rgb(52, 52, 52);\n" +
                    "            --color-of-goon-hover: #666;\n" +
                    "            --no-res:#4e6f83;\n" +
                    "            --footer-hover:#b7b7b7;\n" +
                    "    --cards-imput-placeholder: #29212142;\n" +
                    "--list-color:#626262;\n"+
                    "            --link-color:green;\n" +
                    "        }\n" +
                    "\n" +
                    "</style> ";

        page+="    <link rel=\"stylesheet\" href=\"https://use.fontawesome.com/releases/v5.9.0/css/all.css\">\n" +
                "\n"  +
                " <link rel=\"stylesheet\" href=\"reel.css\">\n" +
                "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n" +
                "<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n" +
                "<link href=\"https://fonts.googleapis.com/css2?family=Comfortaa:wght@500&display=swap\" rel=\"stylesheet\">"+
                "    <title>Search</title>\n" +
                "<link rel = \"icon\" href = \"logoofweb.png\" type = \"image/x-icon\">"+
                "</head>\n" +
                "<body>\n" +
                "<header>\n" +
                "        <nav>\n" +
                "          <!-- <img src=\"\" alt=\"calender\" style=\"height:60px;\"> -->\n" +
                "        <h1>\n" +
                "            <form action=\"BackHome\">\n" +

                "<input type=\"submit\" value=\"GOON\">\n" +
                "\n" +

                "              \n" +
                "         </form>\n" +

                "        </h1>";
        if(theme ==0)
            page += "<form action=\"queryrequest\" method=\"GET\" id=\"queryrequest\" >\n" +
                    "                            \n" +
                    "                            <input type=\"number\" name=\"theme\" value=\"1\" hidden style=\"display: none;\"/>  \n" +
                    "<input type=\"text\" name=\"search\" placeholder= \"Please Don't\" value=\""+name+"\"hidden style=\"display: none;\" />"+
                    "<input type=\"number\" name=\"count\" value=\""+count+"\" hidden style=\"display: none;\" />  " +
                    "                            <input type=\"submit\" value=\"Dark Mode\" /></form>\n";
        if(theme ==1)
            page +="<form action=\"queryrequest\" method=\"GET\" id=\"queryrequest\" >\n" +
                    "                            \n" +
                    "                            <input type=\"number\" name=\"theme\" value=\"0\" hidden style=\"display: none;\"/> \n" +
                    "<input type=\"text\" name=\"search\" placeholder= \"Please Don't\" value=\""+name+"\"hidden style=\"display: none;\" />"+
                    "<input type=\"number\" name=\"count\" value=\""+count+"\" hidden style=\"display: none;\" />  " +
                    "                            <input type=\"submit\" value=\"Light Mode\" /></form>\n";


        page+=  "    <div class=\"engine_inside  \">\n" +
                " <form action=\"queryrequest\" method=\"get\" id=\"searching\">" +

                " <div class=\"search_box\">" +
                "                <input list=\"search\" id=\"my-input\" name=\"search\" placeholder= \"Try Again\" required>" +
                "  <datalist id=\"search\">";

        for (int i = 0; i < old_q.countDocuments(); i++) {
            org.bson.Document d = (org.bson.Document) old_q.find(eq("_id",i)).first();
            String word = (String) d.get("query");
            page+= "            <option value="+word+">";
        }


        page+=     "</datalist>    <input type=\"number\" name=\"count\" value=\"1\" hidden style=\"display: none;\">" +
                "        <button type=\"button\"><i class=\"fas fa-microphone\"></i></button>" +
                "                </div>" +
                "   <input type=\"number\" name=\"theme\" value="+theme+" hidden style=\"display: none;\" />  \n" +
                "                <input type=\"submit\" value=\"Submit\"  style=\"display: none;\" >" +
                "             </form>" +
                "</div>"+
                "        </nav>\n" +
                "    </header>"+
                "<br>";

        if(retrived.size()>0)
        {
            page += message;

            page += "<footer>\n" ;


            if(count==1)
                page+="<form class=\"my_form\" action=\"queryrequest\" method=\"GET\" id=\"queryrequest\" style=\"display: inline;\">" +
                        "                            <input type=\"number\" name=\"theme\" value="+theme+" hidden style=\"display: none;\"/>  \n" +
                        "<input type=\"text\" name=\"search\" placeholder= \"Please Don't\" value=\""+name+"\"hidden style=\"display: none;\" />"+
                        "<input type=\"number\" name=\"count\" value=\""+(count-1)+"\" hidden style=\"display: none;\" />  " +
                        "<input type=\"submit\" value=\"<\" disabled/></form>"+

                        "<form class=\"my_form2\" action=\"queryrequest\" method=\"GET\" id=\"queryrequest\" style=\"display: inline;\">" +
                        "                            <input type=\"number\" name=\"theme\" value="+theme+" hidden style=\"display: none;\"/>  \n" +
                        "<input type=\"text\" name=\"search\" placeholder= \"Please Don't\" value=\""+name+"\"hidden style=\"display: none;\" />"+
                        "<input type=\"number\" name=\"count\" value=\""+(count)+"\"min=1 max="+(int)num_of_pages+" />  " +
                        "<input type=\"submit\" value=\"\" \"hidden style=\"display: none;\"></form>"+
                        "<P>"+"/"+(int)num_of_pages+"</p>"+

                        "<form class=\"my_form\" action=\"queryrequest\" method=\"GET\" id=\"queryrequest\" style=\"display: inline;\">" +
                        "                            <input type=\"number\" name=\"theme\" value="+theme+" hidden style=\"display: none;\"/>  \n" +
                        "<input type=\"text\" name=\"search\" placeholder= \"Please Don't\" value=\""+name+"\"hidden style=\"display: none;\" />"+
                        "<input type=\"number\" name=\"count\" value=\""+(count+1)+"\" hidden style=\"display: none;\" />  " +
                        "<input type=\"submit\" value=\">\" /></form>";
            else if(count==Math.ceil(col2.countDocuments()/10.0))
                page+="<form class=\"my_form\" action=\"queryrequest\" method=\"GET\" id=\"queryrequest\" style=\"display: inline;\">" +
                        "                            <input type=\"number\" name=\"theme\" value="+theme+" hidden style=\"display: none;\"/>  \n" +
                        "<input type=\"text\" name=\"search\" placeholder= \"Please Don't\" value=\""+name+"\"hidden style=\"display: none;\" />"+
                        "<input type=\"number\" name=\"count\" value=\""+(count-1)+"\" hidden style=\"display: none;\" />  " +
                        "<input type=\"submit\" value=\"<\" /></form>"+

                        "<form class=\"my_form2\" action=\"queryrequest\" method=\"GET\" id=\"queryrequest\" style=\"display: inline;\">" +
                        "                            <input type=\"number\" name=\"theme\" value="+theme+" hidden style=\"display: none;\"/>  \n" +
                        "<input type=\"text\" name=\"search\" placeholder= \"Please Don't\" value=\""+name+"\"hidden style=\"display: none;\" />"+
                        "<input type=\"number\" name=\"count\" value=\""+(count)+"\"min=1 max="+(int)num_of_pages+" />  " +
                        "<input type=\"submit\" value=\"\" \"hidden style=\"display: none;\"></form>"+
                        "<P>"+"/"+(int)num_of_pages+"</p>"+

                        "<form class=\"my_form\" action=\"queryrequest\" method=\"GET\" id=\"queryrequest\" style=\"display: inline;\">" +
                        "                            <input type=\"number\" name=\"theme\" value="+theme+" hidden style=\"display: none;\"/>  \n" +
                        "<input type=\"text\" name=\"search\" placeholder= \"Please Don't\" value=\""+name+"\"hidden style=\"display: none;\" />"+
                        "<input type=\"number\" name=\"count\" value=\""+(count+1)+"\" hidden style=\"display: none;\" />  " +
                        "<input type=\"submit\" value=\">\" disabled/></form>";
            else
                page+="<form class=\"my_form\" action=\"queryrequest\" method=\"GET\" id=\"queryrequest\" style=\"display: inline;\">" +
                        "                            <input type=\"number\" name=\"theme\" value="+theme+" hidden style=\"display: none;\"/>  \n" +
                        "<input type=\"text\" name=\"search\" placeholder= \"Please Don't\" value=\""+name+"\"hidden style=\"display: none;\" />"+
                        "<input type=\"number\" name=\"count\" value=\""+(count-1)+"\" hidden style=\"display: none;\" />  " +
                        "<input type=\"submit\" value=\"<\" /></form>"+

                        "<form class=\"my_form2\" action=\"queryrequest\" method=\"GET\" id=\"queryrequest\" style=\"display: inline;\">" +
                        "                            <input type=\"number\" name=\"theme\" value="+theme+" hidden style=\"display: none;\"/>  \n" +
                        "<input type=\"text\" name=\"search\" placeholder= \"Please Don't\" value=\""+name+"\"hidden style=\"display: none;\" />"+
                        "<input type=\"number\" name=\"count\" value=\""+(count)+"\"min=1 max="+(int)num_of_pages+" />  " +
                        "<input type=\"submit\" value=\"\" \"hidden style=\"display: none;\"></form>"+
                        "<P>"+"/"+(int)num_of_pages+"</p>"+

                        "<form class=\"my_form\" action=\"queryrequest\" method=\"GET\" id=\"queryrequest\" style=\"display: inline;\">" +
                        "                            <input type=\"number\" name=\"theme\" value="+theme+" hidden style=\"display: none;\"/>  \n" +
                        "<input type=\"text\" name=\"search\" placeholder= \"Please Don't\" value=\""+name+"\"hidden style=\"display: none;\" />"+
                        "<input type=\"number\" name=\"count\" value=\""+(count+1)+"\" hidden style=\"display: none;\" />  " +
                        "<input type=\"submit\" value=\">\" /></form>"
                        + "</footer>\n" ;
        }

        else
            page+= "<br><br><br><br><div class=\"image\">\n" +
                    "    <img src=\"3adel.png\" alt=\"\">\n" +
                    "<p>طب ما تجرب كويري تانية يبشمهندس</p>\n" +
                    "    </div>\n" ;



        page += "\n" +
                "\n" +
                "    \n" +
                "    <script src=\"main.js\"> </script>\n" +
                "</body>\n" +
                "</html>";
        response.getWriter().println(page);
    }
}

