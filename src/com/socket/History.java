package com.socket;

import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import com.ui.HistoryFrame;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class History {
    
    public String filePath;
    
    public History(String filePath){
        this.filePath = filePath;
    }
    
    public void addMessagea(Message msg, String time){
        
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filePath);
 
            Node data = doc.getFirstChild();
            
            Element message = doc.createElement("message");
            Element _sender = doc.createElement("sender"); _sender.setTextContent(msg.sender);
            Element _content = doc.createElement("content"); _content.setTextContent(msg.content);
            Element _recipient = doc.createElement("recipient"); _recipient.setTextContent(msg.recipient);
            Element _time = doc.createElement("time"); _time.setTextContent(time);
            
            message.appendChild(_sender); message.appendChild(_content); message.appendChild(_recipient); message.appendChild(_time);
            data.appendChild(message);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);
 
	   } 
           catch(Exception ex){
		System.out.println("Exceptionmodify xml");
	   }
	}
   
    public void FillTable(HistoryFrame frame){
      
        DefaultTableModel model = (DefaultTableModel) frame.jTable1.getModel();
    
        try{
            File fXmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            
            NodeList nList = doc.getElementsByTagName("message");
            
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    model.addRow(new Object[]{getTagValue("sender", eElement), getTagValue("content", eElement), getTagValue("recipient", eElement), getTagValue("time", eElement)});
                }
            }
        }
        catch(Exception ex){
            System.out.println("Filling Exception");
        }
        
        //add
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        String url = "jdbc:mysql://localhost:3306/chat";
        String user = "root";
        String password = "";

        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            rs = st.executeQuery("SELECT VERSION()");

            if (rs.next()) {
                System.out.println(rs.getString(1));
            }

        } catch (SQLException ex) {
            

        } finally {
        }
        DefaultTableModel model2 = (DefaultTableModel) frame.jTable2.getModel();
        
                try {
                    Statement stmt = con.createStatement();
                    PreparedStatement psmt = con.prepareStatement("SELECT * FROM users");
                    rs = psmt.executeQuery();
                     // get column names
                    int len = rs.getMetaData().getColumnCount();
                    Vector cols= new Vector(len);
                    for(int i=1; i<=len; i++) // Note starting at 1
                    cols.add(rs.getMetaData().getColumnName(i));
                    
                    // Add Data
                    Vector data = new Vector();
                    while(rs.next())
                    {
                        Vector row = new Vector(len);
                        for(int i=1; i<=len; i++)
                        {
                            row.add(rs.getString(i));
                        }
                        data.add(row);
                        model2.addRow(data);
                    }
                    
                    System.out.println(data);
                    // Now create the table
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }  
                
                
                    model2.addRow(new Object[]{"a", "b", "c", "d"});
    }
    
    public static String getTagValue(String sTag, Element eElement) {
	NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
	return nValue.getNodeValue();
  }
}
