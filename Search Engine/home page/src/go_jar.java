import java.awt.*;
import java.io.*;

import jakarta.servlet.http.*;
import java.io.IOException;


public class go_jar extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {


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
