package edu.wpi.htnlfd;

import edu.wpi.htnlfd.model.*;

import org.w3c.dom.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DomManipulation {

   private DocumentBuilderFactory factory;

   private DocumentBuilder builder;

   private Document document;

   private final String xmlnsValue = "http://www.cs.wpi.edu/~rich/cetask/cea-2018-ext";

   public final String namespace = "urn:disco.wpi.edu:htnlfd:setTable1";

   private final String namespacePrefix;

   public DomManipulation () {
      factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);

      String[] dNSNameArray = namespace.split(":");
      namespacePrefix = dNSNameArray[dNSNameArray.length - 1];
      try {
         builder = factory.newDocumentBuilder();
         // document = builder.newDocument();
      } catch (ParserConfigurationException e) {
         throw new RuntimeException(
               "An error occured while creating the document builder", e);
      }
   }

   private void buildDOM (TaskModel taskmodel) {
       taskmodel.toNode(document, xmlnsValue, namespace, namespacePrefix);
   }

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

   
}
