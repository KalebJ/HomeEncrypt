
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JComboBox;

public class Global {
   static Encryption password = new Encryption();
   static StringBuilder secondLevelKey = new StringBuilder();
   static Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
   static String loggedInUser = "";
   static boolean clipboardBehavior = true;
   static Map<String, Integer> Users = new HashMap<String, Integer>();
   static ArrayList<String> PINS = new ArrayList<String>();
   static SortedSet<String> archivedText = new TreeSet<String>();
   static File exitButtonImage = new File("HomeEncryptExitRollover.PNG");
   static Font encryptionFont = new Font("DialogInput",Font.PLAIN,14);
   static boolean individualLength = false;

   static void loadOptions() {
      try {
         File optionFile = new File("options.txt");
         if (optionFile.exists()) {
            DataInputStream in = new DataInputStream (new FileInputStream(optionFile)); //DataInput gives useful methods, but requires a FileInput to construct
            password.setMaxSize(Integer.parseInt(in.readUTF())); //readUTF, reads in next available String
            clipboardBehavior = Boolean.parseBoolean(in.readUTF());
            password.requireSymbol(Boolean.parseBoolean(in.readUTF()));
            loggedInUser = in.readUTF();
            in.close();

         }
      }
      catch(IOException e) {
      }
   }

   private static File plainTextArchive, plainTextUsers;

   static void loadArchive() {
      try {
         plainTextArchive = new File("plainTextArchive.txt");
         plainTextUsers = new File("plainTextUsers.txt");

         if (plainTextUsers.canRead()) {
            BufferedReader in = new BufferedReader(new FileReader(plainTextUsers));
            String line = in.readLine();
            while (line != null) {
               String lineSplit[] = line.split("\\\\s");
               Users.put(lineSplit[0], Integer.parseInt(lineSplit[1]));
               PINS.add(lineSplit[0]);
               line = in.readLine();
               // TODO: Make sure if the file is corrupted it doesn't cause a crash.
            }
            // TODO: PIN.setSelectedItem(loggedInUser);
         } else {
            FileWriter fstream = new FileWriter("plainTextUsers.txt");
            fstream.write("");
         }


         DataInputStream in = new DataInputStream(new FileInputStream(plainTextArchive));

         while (in.available() > 0) { //If there is still more, read in the next String.
            archivedText.add(in.readUTF());
         }
         in.close();
      }
      catch (IOException e) {
      }
   }
   //Duplicate for multiple files support
   static void loadArchive(String fileName) {
      try {
         plainTextArchive = new File(fileName + ".txt");
         DataInputStream in = new DataInputStream(new FileInputStream(plainTextArchive));

         while (in.available() > 0) { //If there is still more, read in the next String.
            archivedText.add(in.readUTF());
         }
         in.close();
      }
      catch (IOException e) {
      }
   }

   static void writeOptions() {
      try {
         File optionFile = new File("options.txt");
         DataOutputStream out = new DataOutputStream (new FileOutputStream(optionFile)); //DataOutput overwrites a file instead of appending
         out.writeUTF(String.valueOf(password.getMaxSize())); //writeUTF writes a string
         out.writeUTF(String.valueOf(Global.clipboardBehavior));
         out.writeUTF(String.valueOf(password.getSymbolStatus()));
         out.writeUTF(loggedInUser);
         out.close();
      }
      catch(IOException e) {
      }
   }
   static void writeArchive() {
      try {
         plainTextArchive = new File("plainTextArchive.txt");
         DataOutputStream out = new DataOutputStream(new FileOutputStream(plainTextArchive));
         String[] textToArchive = archivedText.toArray(new String[0]);
         for (int i = 0; i< textToArchive.length; i++) {
            out.writeUTF(textToArchive[i]);
         }

         out.close();
      } catch (IOException e) {
      }
   }

   //Duplicate for multiple files support
   static void writeArchive(String fileName) {
      try {
         plainTextArchive = new File(fileName + ".txt");
         DataOutputStream out = new DataOutputStream(new FileOutputStream(plainTextArchive));
         String[] textToArchive = archivedText.toArray(new String[0]);
         for (int i = 0; i< textToArchive.length; i++) {
            out.writeUTF(textToArchive[i]);
         }

         out.close();
      } catch (IOException e) {
      }
   }

   static void writeUserArchive() {
      try {
         // Writes user file
         // Syntax: I used a BufferedWriter because I was getting strange
         //   results with writeUTF. We can talk about it.
         BufferedWriter out = new BufferedWriter(new FileWriter(plainTextUsers));
         for (String user : Users.keySet()) {
            out.write(user + "\\s" + Users.get(user) + "\n");
         }
         out.close();
      } catch (IOException e) {
      }
   }

   static void loadFromClipboard() {
      //try {
         //plainComboBox.setSelectedItem(clip.getData(DataFlavor.stringFlavor)); //getData reads from clip, requires a type of Flavor.
      //} catch (UnsupportedFlavorException e) {
         //e.printStackTrace();
      //} catch (IOException e) {
         //e.printStackTrace();
      //}
   }

}
