
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.concurrent.CyclicBarrier;


public class myMain {

        public void main_clawler(String[] args) throws InterruptedException {
                // TODO Auto-generated method stub

                Object obj0 = new Object();
                Object obj1 = new Object();
                Object obj2 = new Object();
                Object obj3 = new Object();
                String ur = "mongodb://localhost:27017";
                MongoClient client = MongoClients.create(ur);

                MongoDatabase db = client.getDatabase("searchengine");

                MongoCollection col0 = db.getCollection("Crawler0");
                MongoCollection col1 = db.getCollection("Crawler1");
                MongoCollection col2 = db.getCollection("Crawler2");
                MongoCollection arr_com = db.getCollection("Array_Compact");

                Document temp ;

                if(col0.countDocuments()==0)
                {
                        try {
                                File myObj = new File("seeds.txt");
                                Scanner myReader = new Scanner(myObj);
                                int i=0;
                                while (myReader.hasNextLine()) {
                                        String data = myReader.nextLine();
                                        temp = new Document("url", data);
                                        col0.insertOne(temp);
                                        i++;
                                }
                                myReader.close();
                        } catch (FileNotFoundException e) {
                                System.out.println("An error occurred.");
                                e.printStackTrace();
                        }
                }

                Scanner sc = new Scanner(System.in);
                System.out.println("Input thread number : ");
                int Thread_Number = sc.nextInt();
                CyclicBarrier barrier = new CyclicBarrier(Thread_Number, null);
                Thread[] arr_th = new Thread[Thread_Number];
                for(int i=0;i<Thread_Number;i++) {
                        arr_th[i] = new Thread(new Crawler(obj0,obj1,obj2,obj3,barrier));
                        arr_th[i].start();
                }

                for(int i=0;i<Thread_Number;i++) {
                        arr_th[i].join();
                }
                col0.deleteMany(new BasicDBObject());
                col1.deleteMany(new BasicDBObject());
                arr_com.deleteMany(new BasicDBObject());
        }

}
