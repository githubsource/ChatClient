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
            String pr = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKrF6jX3gSRlrYYerDvwDud6s/Eu\n" +
"oSvqGfg7uwJx2gh3RnB9eb9MKhG4MQa0bXEp7Y84mRoyZPxXoRBtWS2R+PaqKelpGdwiMAgbavAf\n" +
"caDeqFxXkLFYd4ucgK++l8fBybIYg7yMagafEza/IQ/cD5GN2UxQdbCOSTaym4ECkLBVAgMBAAEC\n" +
"gYB4W2aXRGnrKhIjHYkL8UO/1xXtHI0Q8zv8XLSPo4gkQVQOwhBFR7u6o1NCQ3WCtSctspkNAolz\n" +
"45qrhjW8zU1l+cHjKzdt3URLtIX3VLPS44KtlCiPmTfWOXNhIKNHs8ulqYD7AE9pUJAVZsmyuqgl\n" +
"QzIMlK+bBhIHtzkmnEbL3QJBAO/nsEiXBu4qRFsxkKreeynG8pU5mjdDcTZ/etGyjkIij3/LYo7r\n" +
"nnwvHIgqXzOkJfhzswxQqQR7uRjmZfrUZRcCQQC2Oubo5+vI6fPr2FB7ROr+W+8VZJlAO7dK2r6Y\n" +
"FSjJqiznmL17JO5/E4jrkyJA/iPq+d2qvs/OJXNOg4pZDVFzAkEA2HDIe4U+bGtXxoq+QVp99eAX\n" +
"Bgi1GLzRDGEQ9tXIQOSbYKmnHth24QVEEZlg0N98nl4MIMU45+GTymI7iYRMOwJAfgvB+mmo0sDY\n" +
"QlLOYGVsMeI/PknmIuLrRnCFksZX/x2hj9Q7g/koqmdFtsR/1fqzt217YQY40LtgxXcA89XddwJB\n" +
"AMnRRPtwlMN7wgDkPa80Y4Uex/k24ueT0F+/6vu0x1OYibcugMaHtm26aRp7CxV/rAgIZdYqcd7r\n" +
"0FKoFYgMcas=";
                   */
            String pr = FileUtil.readFile(ui.filePrivateKey);
            final byte[] plainBytes = decrypt(combined, CipherUtil.loadPrivateKey(pr));
            
            //File cipherFile = bytesToFile(cipherBytes, "/Users/tranngocdien/Documents/java project/file/key/abc_cipher.txt");

            File plainFile = bytesToFile(plainBytes, saveTo);
            
            
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