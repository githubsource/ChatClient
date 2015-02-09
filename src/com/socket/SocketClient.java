package com.socket;

import static com.socket.EncryptionUtil.decrypt;
import com.ui.ChatFrame;
import com.ui.Login;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class SocketClient implements Runnable{
    
    public int port;
    public String serverAddr;
    public Socket socket;
    public ChatFrame ui;
    public ObjectInputStream In;
    public ObjectOutputStream Out;
    public History hist;
    public Login loginFrm;
    
    public SocketClient(ChatFrame frame, Login frm) throws IOException{
        this.loginFrm = frm;
        ui = frame; this.serverAddr = ui.serverAddr; this.port = ui.port;
        socket = new Socket(InetAddress.getByName(serverAddr), port);
            
        Out = new ObjectOutputStream(socket.getOutputStream());
        Out.flush();
        In = new ObjectInputStream(socket.getInputStream());
        
        hist = ui.hist;
    }

    public SocketClient(int port, String serverAddr, Socket socket, ChatFrame ui, 
            ObjectInputStream In, ObjectOutputStream Out, History hist) {
        this.port = port;
        this.serverAddr = serverAddr;
        this.socket = socket;
        this.ui = ui;
        this.In = In;
        this.Out = Out;
        this.hist = hist;
    }
    
   

    @Override
    public void run() {
        boolean keepRunning = true;
        while(keepRunning){
            try {
                Message msg = (Message) In.readObject();
                
                if(!(msg.type.equals("signup") || msg.type.equals("signout") ||
                       msg.type.equals("test") || msg.type.equals("login") || msg.type.equals("newuser")) ) {
                    System.out.println("Incoming prev decrypt : "+msg.toString());                  
                    System.out.println("check filePrivateKey: " + ui.filePrivateKey);
                    String pr = FileUtil.readFile(ui.filePrivateKey);
                    System.out.println("private key : "+pr);
                    String txt = EncryptionUtil.cipherTxt2PlainTxt(msg.content, pr);
                    msg.content = txt;
                }
                
                
                System.out.println("Incoming : "+msg.toString());
                
                if(msg.type.equals("message")){
                    if(msg.recipient.equals(ui.username)){
                        ui.jTextArea1.append("["+msg.sender +" > Me] : " + msg.content + "\n");
                    }
                    else{
                        ui.jTextArea1.append("["+ msg.sender +" > "+ msg.recipient +"] : " + msg.content + "\n");
                    }
                                            
                    if(!msg.content.equals(".bye") && !msg.sender.equals(ui.username)){
                        String msgTime = (new Date()).toString();
                        /*
                        try{
                            hist.addMessage(msg, msgTime);
                            DefaultTableModel table = (DefaultTableModel) ui.historyFrame.jTable1.getModel();
                            table.addRow(new Object[]{msg.sender, msg.content, "Me", msgTime});
                        }
                        catch(Exception ex){}  
                                */
                    }
                }
                else if(msg.type.equals("login")){
                    if(msg.content.equals("TRUE")){
                        ui.txtLogin.setEnabled(false); ui.jButton3.setEnabled(false);                        
                        ui.btnSend.setEnabled(true); ui.btnSelectFile.setEnabled(true);
                        ui.jTextArea1.append("[SERVER > Me] : Login Successful\n");
                        ui.txtUsername.setEnabled(false); ui.txtPassword.setEnabled(false);
                        
                        //ui.setVisible(true);
                        
                        //
                        ui.setLocation(loginFrm.getLocation());
                        ui.setVisible(true);
                        //this.dispose();
                        loginFrm.setVisible(false);
                        
                    }
                    else{
                        //ui.jTextArea1.append("[SERVER > Me] : Login Failed\n");
                        loginFrm.loginFailed();
                    }
                }
                else if(msg.type.equals("test")){
                    ui.btnConnect.setEnabled(false);
                    ui.txtLogin.setEnabled(true); ui.jButton3.setEnabled(true);
                    ui.txtUsername.setEnabled(true); ui.txtPassword.setEnabled(true);
                    ui.txtServerAddress.setEditable(false); ui.txtPort.setEditable(false);
                    ui.jButton7.setEnabled(true);
                }
                else if(msg.type.equals("newuser")){
                    if(!msg.content.equals(ui.username)){
                        boolean exists = false;
                        for(int i = 0; i < ui.model.getSize(); i++){
                            if(ui.model.getElementAt(i).equals(msg.content)){
                                exists = true; break;
                            }
                        }
                        if(!exists){ ui.model.addElement(msg.content); }
                    }
                }
                else if(msg.type.equals("signup")){
                    if(msg.content.equals("TRUE")){
                        ui.txtLogin.setEnabled(false); ui.jButton3.setEnabled(false);
                        ui.btnSend.setEnabled(true); ui.btnSelectFile.setEnabled(true);
                        ui.jTextArea1.append("[SERVER > Me] : Singup Successful\n");
                    }
                    else{
                        ui.jTextArea1.append("[SERVER > Me] : Signup Failed\n");
                    }
                }
                else if(msg.type.equals("signout")){
                    if(msg.content.equals(ui.username)){
                        ui.jTextArea1.append("["+ msg.sender +" > Me] : Bye\n");
                        ui.btnConnect.setEnabled(true); ui.btnSend.setEnabled(false); 
                        ui.txtServerAddress.setEditable(true); ui.txtPort.setEditable(true);
                        
                        for(int i = 1; i < ui.model.size(); i++){
                            ui.model.removeElementAt(i);
                        }
                        
                        ui.clientThread.stop();
                    }
                    else{
                        ui.model.removeElement(msg.content);
                        ui.jTextArea1.append("["+ msg.sender +" > All] : "+ msg.content +" has signed out\n");
                    }
                }
                else if(msg.type.equals("upload_req")){
                    
                    if(JOptionPane.showConfirmDialog(ui, ("Accept '"+msg.content+"' from "+msg.sender+" ?")) == 0){
                        
                        JFileChooser jf = new JFileChooser();
                        jf.setSelectedFile(new File(msg.content));
                        int returnVal = jf.showSaveDialog(ui);
                       
                        String saveTo = jf.getSelectedFile().getPath();
                        if(saveTo != null && returnVal == JFileChooser.APPROVE_OPTION){
                            Download dwn = new Download(saveTo, ui);
                            Thread t = new Thread(dwn);
                            t.start();
                            //send(new Message("upload_res", (""+InetAddress.getLocalHost().getHostAddress()), (""+dwn.port), msg.sender));
                            send(new Message("upload_res", ui.username, (""+dwn.port), msg.sender));
                        }
                        else{
                            send(new Message("upload_res", ui.username, "NO", msg.sender));
                        }
                    }
                    else{
                        send(new Message("upload_res", ui.username, "NO", msg.sender));
                    }
                }
                else if(msg.type.equals("upload_res")){
                    if(!msg.content.equals("NO")){
                        int port  = Integer.parseInt(msg.content);
                        String addr = msg.sender;
                        
                        ui.btnSelectFile.setEnabled(false); ui.btnSendFile.setEnabled(false);
                        Upload upl = new Upload(addr, port, ui.file, ui);
                        Thread t = new Thread(upl);
                        t.start();
                    }
                    else{
                        ui.jTextArea1.append("[SERVER > Me] : "+msg.sender+" rejected file request\n");
                    }
                }
                else{
                    ui.jTextArea1.append("[SERVER > Me] : Unknown message type\n");
                }
            }
            catch(Exception ex) {
                keepRunning = false;
                ui.jTextArea1.append("[Application > Me] : Connection Failure\n");
                ui.btnConnect.setEnabled(true); ui.txtServerAddress.setEditable(true); ui.txtPort.setEditable(true);
                ui.btnSend.setEnabled(false); ui.btnSelectFile.setEnabled(false); ui.btnSelectFile.setEnabled(false);
                
                for(int i = 1; i < ui.model.size(); i++){
                    ui.model.removeElementAt(i);
                }
                
                ui.clientThread.stop();
                
                System.out.println("Exception SocketClient run()");
                ex.printStackTrace();
            }
        }
    }
    
    public void send(Message msg){
        try {
            Out.writeObject(msg);
            Out.flush();
            System.out.println("Outgoing : "+msg.toString());
            
            if(msg.type.equals("message") && !msg.content.equals(".bye")){
                String msgTime = (new Date()).toString();
             /*
                try{
                    hist.addMessage(msg, msgTime);               
                    DefaultTableModel table = (DefaultTableModel) ui.historyFrame.jTable1.getModel();
                    table.addRow(new Object[]{"Me", msg.content, msg.recipient, msgTime});
                }
                catch(Exception ex){}
                     */
            }
        } 
        catch (IOException ex) {
            System.out.println("Exception SocketClient send()");
        }
    }
    
    public void closeThread(Thread t){
        t = null;
    }
}
