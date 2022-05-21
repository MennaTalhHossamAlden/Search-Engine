import java.awt.Desktop;
import java.io.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.jndi.toolkit.url.Uri;
import org.bson.conversions.Bson;

import java.net.URI;

import static com.mongodb.client.model.Filters.eq;

public class go_on {
    public static void main(String[] args) throws Exception {

        String ur = "mongodb://localhost:27017";
        MongoClient client = MongoClients.create(ur);

        MongoDatabase db = client.getDatabase("searchengine");

        MongoCollection col = db.getCollection("queries");
//        int theme =0;


        File f = new File("C:\\Users\\Asus\\Downloads\\apache-tomcat-10.0.20-windows-x64\\apache-tomcat-10.0.20\\webapps\\ROOT\\engine.html");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
//        if(theme ==0)
            bw.write(" <style>\n" +
                        "        :root {\n" +
                        "            --main-color:white;\n" +
                        "            --second-color:black;\n" +
                        "            --header-box-shadow: rgb(70, 70, 70);\n" +
                        "            --header-colors1:#5e5e5e;\n" +
                        "            --header-colors2:#ececec;\n" +
                        "            --cards-box-shadow: #666;\n" +
                        "            --cards-background-color:#f2f2f2;\n" +
                        "            --cards-input-hover-box-shadow:rgb(75, 75, 75);\n" +
                        "             --cards-imput-placeholder: #b7b7b7;\n" +
                    "--mic-color:black;\n"+
                    "--opacity:0.15;\n"+
                        "\n" +
                        "        }\n" +
                        "\n" +
                        "    </style> \n");
//        else if (theme ==1)
//            bw.write( "<style>\n" +
//                        "        :root {\n" +
//                        "      --main-color: #2b373d;\n" +
//                        "    --second-color: white;\n" +
//                        "    --header-box-shadow: rgb(167 64 0 / 36%);\n" +
//                        "    --header-colors1: #151f25;\n" +
//                        "    --header-colors2: #6e7e87;\n" +
//                        "    --cards-box-shadow: rgb(200 77 0);\n" +
//                        "    --cards-background-color: #687881;\n" +
//                        "    --cards-input-hover-box-shadow: rgb(255 98 0);\n" +
//                        "    --cards-imput-placeholder: #29212146;\n" +
//                    "--mic-color:black;\n"+
//                        "\n" +
//                        "        } </style>  \n\n");

        bw.write( "    <link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"https://use.fontawesome.com/releases/v5.9.0/css/all.css\">\n" +
                "\n" +
                "    <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n" +
                "                <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n" +
                "                <link href=\"https://fonts.googleapis.com/css2?family=Comfortaa:wght@500&display=swap\" rel=\"stylesheet\">\n" +
                "    <title>GO ON!</title>\n" +
                "    <link rel = \"icon\" href = \"logoofweb.png\" type = \"image/x-icon\">\n" +
                "    \n" +
                "    \n" +
                "</head>\n" +
                "<body>\n" +
                "    <header>\n" +
                "        <nav>\n" +
                "          <!-- <img src=\"\" alt=\"calender\" style=\"height:60px;\"> -->\n");

//                if(theme ==0)
                    bw.write("<form action=\"change_theme\" method=\"GET\" id=\"change_theme\" >\n" +
                        "                            \n" +
                        "                            <input type=\"number\" name=\"theme\" value=\"1\" hidden />  \n" +
                        "                            <input type=\"submit\" value=\"Dark Mode\" /></form>\n");
//                if(theme ==1)
//                    bw.write("<form action=\"change_theme\" method=\"GET\" id=\"change_theme\" >\n" +
//                            "                            \n" +
//                            "                            <input type=\"number\" name=\"theme\" value=\"0\" hidden /> \n" +
//                            "                            <input type=\"submit\" value=\"Light Mode\" /></form>\n");

        bw.write("</nav>\n" +
                "    </header>\n" +
                "<div class=\"video\">\n" +
                        " </div>"+
                "    <div class=\"sign  \">\n" +
                "\n" +
                "        <div class=\"logo\">\n");


//        if(theme ==0)
            bw.write("<img src=\"GooN.png\" alt=\"\"></div>\n");
//                    else if(theme ==1)
//            bw.write("<img src=\"GooN-w.png\" alt=\"\"></div>\n");


        bw.write("<p>DON'T&nbsp;&nbsp;&nbsp;GOOGLE&nbsp;&nbsp;&nbsp;IT</p>\n" +
                "        \n" +
                "        <form action=\"queryrequest\" method=\"get\" id=\"searching\">\n" +
                "        \n" +
                "<div class=\"search_box\">\n" +
                "            <!-- <input type=\"text\" name=\"search\" placeholder= \"Please Don't\"> -->\n" +
                "            <!-- <button type = \"submit\" name=\"submit\" style=\"Display:inline;\">Search</button> -->\n" +
                "        <input list=\"search\" id=\"my-input\" name=\"search\" placeholder= \"Please Don't\" autofocus>\n" +
                "            <datalist id=\"search\">           ");
//                        "            <option value=\"programming techniques\">\n" +
//                        "            <option value=\"The World\">\n" );

        for (int i = 0; i < col.countDocuments(); i++) {
           org.bson.Document d = (org.bson.Document) col.find(eq("_id",i)).first();
            String word = (String) d.get("query");
            bw.write( "            <option value=\""+word+"\">\n");

        }
        int didi=0;


        bw.write(    "</datalist>    <input type=\"number\" name=\"count\" value=\"1\" hidden style=\"display: none;\">  \n" +
                "                            <input type=\"number\" name=\"theme\" value=\"0\" hidden style=\"display: none;\" > \n" +
                "        <button type=\"button\"><i class=\"fas fa-microphone\"></i></button>\n" +
                "</div>        \n" +

                "<input type=\"submit\" value=\"Submit\" id=\"\" style=\"display: none;\" >\n" +
                "        \n" +
                "\n" +
                "        </form>\n" +
                "\n" +
                "    </div>\n" +
                "\n" +
                "\n" +
                "<div class=\"video2\">\n" +
                        " </div>  \n"+
                "    <br>\n" +
                "\n" +
                "    <footer>\n" +
                "        \n" +
                "    </footer>\n" +
                "\n" +
                "    <script src=\"main.js\"> </script>\n" +
                "<style>\n" +
                        "    .video{\n" +
                        "        background-image: url(\"Untitled-111.png\");\n" +
                        "        background-repeat: no-repeat;\n" +
                        "        background-size:cover;\n" +
                        "        animation-delay: 3s;\n" +
                        "    }\n" +
                        "    \n" +
                        ".video2{\n" +
                        "        background-image: url(\"Untitled-111s.png\");\n" +
                        "        background-repeat: no-repeat;\n" +
                        "        background-size:cover;\n" +
                        "        animation-delay: 3s;\n" +
                        "    }\n" +
                        "</style>\n" +
                        "\n" +
                        "<script>\n" +
                        "    function changeBg(){\n" +
                        "        const images = [\n" +
                        "            'url(\"Untitled-111.png\")',\n" +
                        "            'url(\"Untitled-111A.png\")',\n" +
                        "            'url(\"Untitled-111F.png\")',\n" +
                        "'url(\"Untitled-111C.png\")',\n" +
                        "        ]\n" +
                        "\n" +
                        "            const back = document.getElementsByClassName(\"video\")[0];\n" +
                        "            const r = images[Math.floor(Math.random()*images.length)];\n" +
                        "            back.style.backgroundImage = r;\n" +
                        "            back.style.animationDelay = \"2s\";\n" +
                        "        \n" +
                        "    }\n" +
                        "\n" +
                        "    setInterval(changeBg, 5000);\n" +
                        "\n" +
                        "    function changeBg2(){\n" +
                        "        const images = [\n" +
                        "            'url(\"Untitled-111s.png\")',\n" +
                        "            'url(\"Untitled-111As.png\")',\n" +
                        "            'url(\"Untitled-111Fs.png\")',\n" +
                        "            'url(\"Untitled-111Cs.png\")',\n" +
                        "        ]\n" +
                        "\n" +
                        "            const back = document.getElementsByClassName(\"video2\")[0];\n" +
                        "            const r = images[Math.floor(Math.random()*images.length)];\n" +
                        "            back.style.backgroundImage = r;\n" +
                        "            back.style.animationDelay = \"2s\";\n" +
                        "        \n" +
                        "    }\n" +
                        "\n" +
                        "    setInterval(changeBg2, 5000);\n" +
                        "</script>"+
                "    \n" +
                "</body>\n" +
                "</html>");
        bw.close();
        URI uri = new URI("http://localhost:8080/engine.html");
        Desktop.getDesktop().browse(uri);
    }
}
