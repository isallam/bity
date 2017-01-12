/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objy.se.ingest;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;

/**
 *
 * @author ibrahim
 */
public class BitcoinIngester {

    public static void main(String[] args)
    {
      NetworkParameters np = new MainNetParams();
      Context ctx = Context.getOrCreate(np);
      
      List<File> blockChainFiles = new ArrayList<>();
      for (int i = 0; i < 10; i++)
      {
        String blockFileName = "blk0000" + i + ".dat";
        blockChainFiles.add(new File("/home/ibrahim/.bitcoin/blocks/"+blockFileName));
      }
      BlockFileLoader bfl = new BlockFileLoader(np, blockChainFiles);

      // Data structures to keep the statistics.
      Map<String, Integer> monthlyTxCount = new HashMap<>();
      Map<String, Integer> monthlyBlockCount = new HashMap<>();

      int numBlock = 0;
      int numTransaction = 0;
      int numInput = 0;
      int numOutput = 0;
      
      long startTime = System.currentTimeMillis();
      // Iterate over the blocks in the dataset.
      for (Block block : bfl) {
        //System.out.println("BLOCK: " + block.toString());
        numBlock++;
        String blockHash = block.getHashAsString();
        
        List<Transaction> trxList = block.getTransactions();
        for (Transaction trx : trxList) {
          numTransaction++;
          String hash = trx.getHashAsString();
          List<TransactionInput> inputList = trx.getInputs();
          for (TransactionInput input : inputList) {
            numInput++;
            //Sha256Hash hashValue = input.getHash();
            Transaction upTrx = input.getConnectedTransaction();
          }
          
          List<TransactionOutput> outputList = trx.getOutputs();
          for (TransactionOutput output : outputList) {
            numOutput++;
            //Sha256Hash hashValue = output.getHash();
            Address address1 = output.getAddressFromP2PKHScript(np);
            if (address1 != null) {
              byte[] hash60 = address1.getHash160();
            }
            Address address2 = output.getAddressFromP2SH(np);
            if (address2 != null) {
              byte[] hash60 = address2.getHash160();
            }
          }
        }
        if ((numBlock % 10000) == 0)
        {
          long diff = System.currentTimeMillis() - startTime;
          System.out.println("at block: " + numBlock + "time: " + diff + " msec.");
          startTime = System.currentTimeMillis();
        }
      }
      
      System.out.println("# Blocks: " + numBlock + "- # Transacions: " + numTransaction);
      System.out.println("# Inputs: " + numInput + " - # Outputs: " + numOutput);
    	
    }  
}
