package edu.wpi.htnlfd.dto;

public class Input {
   String name;
   String Type;
   String modified;
   public String getName () {
      return name;
   }
   public void setName (String name) {
      this.name = name;
   }
   public String getType () {
      return Type;
   }
   public void setType (String type) {
      Type = type;
   }
   public String getModified () {
      return modified;
   }
   public void setModified (String modified) {
      this.modified = modified;
   }
   public Input (String name, String type, String modified) {
      super();
      this.name = name;
      Type = type;
      this.modified = modified;
   }
   public Input () {
      // TODO Auto-generated constructor stub
   }
   public Input (String name, String type) {
      super();
      this.name = name;
      Type = type;
   }
   
   
}
