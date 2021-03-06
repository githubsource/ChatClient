package com.socket;

import static com.socket.EncryptionUtil.bytesToFile;
import static com.socket.EncryptionUtil.decrypt;
import com.ui.ChatFrame;
import java.io.*;
import java.net.*;
import java.security.PrivateKey;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Download implements Runnable{
    
    public ServerSocket server;
    public Socket socket;
    public int port;
    public String saveTo = "";
    public InputStream In;
    public FileOutputStream Out;
    public ChatFrame ui;
    
    public Download(String saveTo, ChatFrame ui){
        try {
            server = new ServerSocket(0);
            port = server.getLocalPort();
            this.saveTo = saveTo;
            this.ui = ui;
        } 
        catch (IOException ex) {
            System.out.println("Exception [Download : Download(...)]");
        }
    }

    @Override
    public void run() {
        try {
            socket = server.accept();
            System.out.println("Download : "+socket.getRemoteSocketAddress());
            
            In = socket.getInputStream();
            Out = new FileOutputStream(saveTo);
            
           // byte[] one = getBytesForOne();
            //byte[] two = getBytesForTwo();
            byte[] combined = new byte[0];// = new byte[one.length + two.length];
   
            byte[] cypher;
            byte[] buffer = new byte[1024];
            int count;
            
            while((count = In.read(buffer)) >= 0){
               // Out.write(buffer, 0, count);
                byte[] tmp = new byte[combined.length + count];
                for (int i = 0; i < tmp.length; ++i)
                {
                    tmp[i] = i < combined.length ? combined[i] : buffer[i - combined.length];
                }
                combined = tmp;
            }
            
            // decrypt
         /*  
            String pr = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJMsiaf9SKRqTP08SIbQ/Wi718oH\n"+
"PiYI4h5zHXdsRG6rRGvnm+XhxGObSq165tSxdLfD5O0TUWuS1/XsTddYA1JaT58D8zfpFS6zFCOn\n" +
"GafkCTpP2MtI4lZc+zuueIJqSZVGrXn8frE+akoXHr3N2csoulvNIL+gcj73jjVIxbJ/AgMBAAEC\n" +
"gYEAj6n8PhYLISP7P3/nNGMx21jyTm4/hpVJVXv9maQAMR/STJrsHb0DHlBq12mPhrQrQW9iOuKO\n" +
"qjnPwmoHmlx58CDOktzvSZahIyjdu7Jfh3yAUv7xFAwT4ITLsYVZ37t4cAw8v++MW0c1dWiaPh5G\n" +
"n24zDpGs5WGTHtW/v+YnnqECQQDHPYLcn5TZXLIBclzNlugY/SE2fyfrTQkvU9R1zRL5y3YlxdPu\n" +
"NkdDaRY34L30t7OYy/GXbXs66BGQ/jIdIuZpAkEAvRnbwuzhkS+0UQNbr4lqnUseHrhUIwH5qWY7\n" +
"QV0WT1GJk7t6X3NfRBA8/tkyEQZgwbWnGoPuhRYpG39qQLHEpwJANH8sn90cZzlZXbA4a6M7fHoV\n" +
"6joO1pzxspqv/GoQeej2NHWvpbB/jm99/zMkWfqdQ8FY4sFiGt9S4ZVYtvyfIQJBAJgkTeOuom0l\n" +
"bHYhtYqtjNGwtjMRKuBfnnbkgZg9RM3cVEw/8l0JfgRM7EY+iUGZWA+CD0gODcGV6szs3pfebtEC\n" +
"QC6jXEIMZWvyA9hv2723g6rHqb5ItHfnzIo7jCnUtzknADogAjHRZAtnDHQIvaW6mJGJZOusUMgc\n" +
"BsVAO9TMJTw=";
                */   
            try {
                 String pr = FileUtil.readFile(ui.filePrivateKey);
                final byte[] plainBytes = EncryptionUtil.decrypt(combined, CipherUtil.loadPrivateKey(pr));

                //File cipherFile = bytesToFile(cipherBytes, "/Users/tranngocdien/Documents/java project/file/key/abc_cipher.txt");

                File plainFile = bytesToFile(plainBytes, saveTo);
            } catch (Exception e) {
                File plainFile = bytesToFile(combined, saveTo);
            }
                
           // Out.flush();
            
            ui.jTextArea1.append("[Application > Me] : Download complete\n");
            
            if(Out != null){ Out.close(); }
            if(In != null){ In.close(); }
            if(socket != null){ socket.close(); }
        } 
        catch (Exception ex) {
            System.out.println("Exception [Download : run(...)] : " + ex);
        }
    }
}