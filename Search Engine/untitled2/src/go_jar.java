import java.awt.*;
import java.io.*;


import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class go_jar extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int yoyo=0;
//        response.setContentType("text/html");
//
//        String page ="";
//        page+="<!DOCTYPE html>\n" +
//                "<html lang=\"en\">\n" +
//                "<head>\n" +
//                "    <meta charset=\"UTF-8\">\n" +
//                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
//                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
//                "    <title>Document</title>\n" +
//                "</head>\n" +
//                "<body onload=\"myFunction()\">\n" +
//                "    \n" +
//                "\n" +
//                "<button>noooooourrr</button>\n" +
//                "<h1>ahmeeed</h1>"+
//
//                "<script>\n" +
//                "function myFunction() {\n" +
////                "window.open('', '_self', '');"+
//                " window.close();\n" +
//                "}"+
//                "</script>    "+
//                "</body>\n" +
//
//
//                "</html>";
//
//
//        response.getWriter().println(page);
        try
        {
//constructor of file class having file as argument
            File file = new File("C:\\Users\\Asus\\Desktop\\GooN.lnk");
            if(!Desktop.isDesktopSupported())//check if Desktop is supported by Platform or not
            {
                System.out.println("not supported");
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            if(file.exists())         //checks file exists or not
                desktop.open(file);              //opens the specified file
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


    }


}
