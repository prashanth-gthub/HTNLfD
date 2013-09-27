package edu.wpi.htnlfd.dto;

public class Output {
   String name;
   String Type;
   public Output (String name, String type) {
      super();
      this.name = name;
      Type = type;
   }
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
   
   
}
