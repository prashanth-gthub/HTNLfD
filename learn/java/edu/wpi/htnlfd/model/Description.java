package edu.wpi.htnlfd.model;
import edu.wpi.cetask.TaskEngine;
import org.w3c.dom.*;
import javax.xml.namespace.QName;
import javax.xml.xpath.*;


// extend the cetask class for convenient access to xpath methods
//  extends edu.wpi.cetask.Description
abstract class Description {

  /* protected Description (Node node, TaskEngine engine, XPath xpath) {
      super(node, engine, xpath);
 
   }
   */
   

   // note that unlike node in edu.wpi.cetask.Description, this field is
   // *not* final, so you can modify it!
  /* protected Node description;

   @Override
   protected Object xpath (String path, QName returnType) {
      try { return  xpath.evaluate(path, description, returnType); }
      catch (XPathExpressionException e) { throw new RuntimeException(e); }
   }

   @Override
   protected String xpath (String path) {
      try { return xpath.evaluate(path, description); }
      catch (XPathExpressionException e) { throw new RuntimeException(e); }
   }

   @Override
   protected QName parseQName (String qname) {
      String prefix;
      int i = qname.indexOf(':');
      if ( i >= 0 ) {
         prefix = qname.substring(0, i);
         String ns = description.lookupNamespaceURI(prefix);
         if ( ns == null )
            throw new IllegalArgumentException("Unknown namespace prefix "+qname
                  +" in "+getNamespace());
         return new QName(ns, qname.substring(i+1), prefix);
      } else // default namespace is from 'about', not xmlns
         return new QName(getNamespace(), qname);
   }*/
   
}

