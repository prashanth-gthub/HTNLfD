package edu.wpi.htnlfd;

import edu.wpi.htnlfd.model.*;
import org.w3c.dom.*;
import java.io.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

// TODO: Auto-generated Javadoc
/**
 * The Class DomManipulation.
 */
public class DomManipulation {

   /** The factory. */
   public DocumentBuilderFactory factory;

   /** The builder. */
   private DocumentBuilder builder;

   /** The document. */
   private Document document;

   /** The Constant xmlnsValue. */
   public static final String xmlnsValue = "http://www.cs.wpi.edu/~rich/cetask/cea-2018-ext";

   /** The Constant namespace. */
   public static final String namespace = "urn:disco.wpi.edu:htnlfd:setTable1";

   /** The Constant namespacePrefix. */
   public static final String namespacePrefix;
   
   static {
      String[] dNSNameArray = namespace.split(":");
      namespacePrefix = dNSNameArray[dNSNameArray.length - 1];
   }

   /**
    * Instantiates a new dom manipulation.
    */
   public DomManipulation () {
      factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      
      try {
         builder = factory.newDocumentBuilder();
         // document = builder.newDocument();
      } catch (ParserConfigurationException e) {
         throw new RuntimeException(
               "An error occured while creating the document builder", e);
      }
   }

   /**
    * Builds the dom.
    *
    * @param taskmodel the taskmodel
    * @return the node
    */
   public Node buildDOM (TaskModel taskmodel) {
       return taskmodel.toNode(document);
   }

   /**
    * Write dom.
    *
    * @param fileName the file name
    * @param taskmodel the taskmodel
    * @throws Exception the exception
    */
   public void writeDOM (String fileName, TaskModel taskmodel)
         throws Exception {

      // Writing document into xml file
      document = builder.newDocument();
      DOMSource domSource = new DOMSource(document);
      File demonstrationFile = new File(fileName);
      if ( !demonstrationFile.exists() )
         demonstrationFile.createNewFile();

      try (FileOutputStream fileOutputStream = new FileOutputStream(
            demonstrationFile, false)) {

         StreamResult streamResult = new StreamResult(fileOutputStream);
         TransformerFactory tf = TransformerFactory.newInstance();
         Transformer transformer = tf.newTransformer();

         buildDOM(taskmodel);

         // Adding indentation and omitting xml declaration
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty(
               "{http://xml.apache.org/xslt}indent-amount", "2");
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         transformer.transform(domSource, streamResult);

      } catch (Exception e) {

         throw e;
      }

   }
   

   /**
    * Write dom.
    *
    * @param stream the stream
    * @param taskmodel the taskmodel
    * @throws TransformerException the transformer exception
    */
   public void writeDOM (PrintStream stream, TaskModel taskmodel) throws TransformerException
        {
      try {
         TransformerFactory tf = TransformerFactory.newInstance();
         Transformer transformer;
         
            transformer = tf.newTransformer();
         
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         
         Document doc = builder.newDocument();
         DOMSource domSource = new DOMSource(doc);
         taskmodel.toNode(doc);
         
         transformer.transform(domSource, new StreamResult(stream));
      } catch (TransformerConfigurationException e) {
         throw e;
      } catch (TransformerException e) {
         throw e;
      }

   }
   
   
}
