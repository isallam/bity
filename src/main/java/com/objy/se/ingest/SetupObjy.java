/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objy.se.ingest;

import com.objy.db.Connection;
import com.objy.db.Objy;

/**
 * Create the schema and import the placement model
 * 
 * @author ibrahim
 */
public class SetupObjy {
  
    public static void main(String[] args)
    {

      if (args.length < 1) {
        System.out.println("Params missing: <boot_file_path");
        return;
      }
      String bootFile = args[0];
      
      System.out.println("Objectivity federation: " + bootFile);
      
      // Objy initialization
      Objy.enableConfiguration();
      Connection connection = new Connection(bootFile);
      
      ObjyAccess objyAccess = new ObjyAccess();

      // make sure Schema is fine.
      objyAccess.createSchema();

      // import placement.
      // ... TBD.
    }
  
}
