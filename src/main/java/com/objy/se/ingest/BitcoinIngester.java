/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objy.se.ingest;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.utils.BlockFileLoader;

import com.objy.db.Objy;
import com.objy.db.TransactionMode;
import java.util.Date;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.Script.ScriptType;


/**
 *
 * @author ibrahim
 */
public class BitcoinIngester {

    com.objy.data.Reference prevBlock = null;
    NetworkParameters np = null;
    Context ctx = null;
    BlockFileLoader bfl = null;

    // Data structures for caching Address and Transaction
    Map<String, String> transactionCache = new HashMap<>();
    Map<String, String> addressCache = new HashMap<>();

    int numBlock = 0;
    int numTransaction = 0;
    int numInput = 0;
    int numOutput = 0;
    int numAddress = 0;
    
    long startTimeTotal = 0;
    long startTime = 0;

    int localNumTransaction = 0;
    int localNumInput = 0;
    int localNumOutput = 0;
    int localNumAddress = 0;
    int numObjectsPerCommit = 0;
            
    com.objy.db.Transaction objyTrx = null;
    com.objy.db.Connection connection = null;
    ObjyAccess objyAccess = new ObjyAccess();
  
    public BitcoinIngester(String bootFile) {
      np = new MainNetParams();
      ctx = Context.getOrCreate(np);

      // Objy initialization
      Objy.enableConfiguration();
      connection = new com.objy.db.Connection(bootFile);
    }
  
    private boolean setupBlockFileLoader(String blocksPath) {
        File blocksFolder = new File(blocksPath);
      File[] listOfFiles = blocksFolder.listFiles(
              new FilenameFilter() {
                  @Override
                  public boolean accept(File dir, String name) {
                      return name.matches("blk.*.dat");
                  }
              });
      Arrays.sort(listOfFiles);
      
      try {
        if (blocksFolder == null || listOfFiles == null) {
          System.out.println("Failed to find data in '" + blocksFolder.getCanonicalPath() + "'");
          System.out.println("... nothing to do... exiting.");
          return false;
        }
//        for (File f : listOfFiles) {
//          System.out.println("File: '" + f.getName() + "'");
//        }
      } catch (IOException ioEx) {
        System.out.println(ioEx.getMessage());
        return false;
      }

      List<File> blockChainFiles = Arrays.asList(listOfFiles);
          
      bfl = new BlockFileLoader(np, blockChainFiles);

      return true;
    }

    private void setupObjySchema() {
        //objyTrx.start(TransactionMode.READ_UPDATE);
        // make sure Schema is fine.
        objyAccess.createSchema();
        //objyTrx.commit();
    }

    private void processBlocks() {
      
      startTime = startTimeTotal = System.currentTimeMillis();
      
      objyTrx = new com.objy.db.Transaction(TransactionMode.READ_UPDATE, "write_session");
      //objyTrx.start(TransactionMode.READ_UPDATE);
      objyAccess.setupCache();
      
      // Iterate over the blocks in the dataset.
      for (Block block : bfl) {
        //System.out.println("BLOCK: " + block.toString());
        String blockHash = block.getHashAsString();
        String blockMerkelRoot = block.getMerkleRoot().toString();
        String prevBlockHash = block.getPrevBlockHash().toString();
        Date blockTime = block.getTime();
        long version = block.getVersion();
        // create Block Object
        com.objy.data.Instance blockInstance = 
                objyAccess.createBlock(numBlock, version, prevBlockHash, blockMerkelRoot, 
                blockTime, blockHash, prevBlock);
        
        numBlock++;
        prevBlock = new com.objy.data.Reference(blockInstance);
        
        processTransactions(block);
        
        int commitEvery = 10000; 
        if ((numBlock % commitEvery) == 0)
        {
          numObjectsPerCommit = commitEvery + localNumTransaction + localNumInput +
                  localNumOutput + localNumAddress;
          long diff = System.currentTimeMillis() - startTime;
          System.out.println("at blk: " + numBlock + " ..t: " + 
                  diff + " msec." + " - #Tx: " + localNumTransaction + " - #In: " +
                  localNumInput + " - #Out: " + localNumOutput + " - #Addr: " + 
                  localNumAddress + " - #Total: " + numObjectsPerCommit);
          localNumTransaction = 0;
          localNumInput = 0;
          localNumOutput = 0;
          localNumAddress = 0;
          startTime = System.currentTimeMillis();
        }
        
        if ((numBlock % 100) == 0)
        {
          objyTrx.commit();
          objyTrx.start(TransactionMode.READ_UPDATE);
        }
      }
      objyTrx.commit();
    }

    private void printStats() {
        long diffTimeTotal = System.currentTimeMillis() - startTimeTotal;
        System.out.println("\n Total Time: " + diffTimeTotal / 1000.0 + " sec.");
        System.out.println("# Blocks: " + numBlock + " - # Transacions: " + numTransaction);
        System.out.println("# Inputs: " + numInput + " - # Outputs: " + numOutput);

    }
    
    
    
    private void processTransactions(Block block) {
        List<Transaction> trxList = block.getTransactions();
        for (Transaction transaction : trxList) {
          String transactionHash = transaction.getHashAsString();
          int transactionId = 0; // no ID is used.
          // create Transaction Object
          com.objy.data.Instance transactionInstance = 
                  objyAccess.createTransaction(transactionId, transactionHash);
          
          objyAccess.addTransactionToBlock(
                  new com.objy.data.Reference(transactionInstance), prevBlock);
          
          transactionCache.put(transactionHash, transactionInstance.getObjectId().toString());
          numTransaction++;
          localNumTransaction++;
          
          com.objy.data.Reference transactionRef = 
                  new com.objy.data.Reference(transactionInstance);
          processInputs(transaction, transactionRef);
          processOutpts(transaction, transactionRef);
          
        }
    }

    private void processInputs(Transaction transaction,
            com.objy.data.Reference transactionRef) {
      
        // process inputs
        List<TransactionInput> inputList = transaction.getInputs();
        int inputId = 0;
        for (TransactionInput input : inputList) {

          //Sha256Hash hashValue = input.getHash();
          Transaction upTrx = input.getParentTransaction();
          String upTrxHash = upTrx.getHash().toString();
          String upTrxOid = transactionCache.get(upTrxHash);
          boolean isCoinBase = input.isCoinBase();

          com.objy.data.Instance inputInstance = 
                  objyAccess.createInput(inputId++, upTrxHash, upTrxOid, isCoinBase);
      
          objyAccess.addInputToTransaction(
                  new com.objy.data.Reference(inputInstance), transactionRef);
          
          numInput++;
          localNumInput++;
        }
    }

    private void processOutpts(Transaction transaction,
            com.objy.data.Reference transactionRef) {
      
        List<TransactionOutput> outputList = transaction.getOutputs();
        
        int outputId = 0;
        for (TransactionOutput output : outputList) {
          numOutput++;
          localNumOutput++;
          //Sha256Hash hashValue = output.getHash();
          try {
            
            //String addressHash = null;
            Script script = output.getScriptPubKey();
            ScriptType scriptType = script.getScriptType();
//            boolean sentToAddress = script.isSentToAddress();
//            boolean payToScript = script.isPayToScriptHash();
//            boolean sentToCLTVPaymentChannel = script.isSentToCLTVPaymentChannel();
//            boolean sentToRawPubKey = script.isSentToRawPubKey();
//            boolean setToMultiSig = script.isSentToMultiSig();
//            String scriptStr = script.toString();
            
            byte[] public_key = null;
            
            if (script.isSentToRawPubKey()) {
                byte[] key = script.getPubKey();
                public_key = org.bitcoinj.core.Utils.sha256hash160(key);
                //public_key = address_bytes;
            }
            else
            {  
                  Address address = script.getToAddress(np);
                  public_key = address.getHash160();
            }

            String publicKeyStr = public_key.toString();
            //System.out.println("OUTPUT: " + output.toString());            
            /****
            byte[] pubKeyHash = script.getPubKeyHash();
            
            Address addressP2PKHScript = output.getAddressFromP2PKHScript(np);
            if (addressP2PKHScript != null) {
              byte[] hash60 = addressP2PKHScript.getHash160();
              addressHash = hash60.toString();
            }
            if (addressHash == null) {
              // try this to get the address (TBD... verify the meaning of such!
              Address address = output.getAddressFromP2SH(np);
              if (address != null) {
                byte[] hash60 = address.getHash160();
                addressHash = hash60.toString();
              }
            }
            */
            
            //int outputIndex = output.getIndex();
            Coin coin = output.getValue();
            
            // lookup Address in cache.
            String addressOid = addressCache.get(publicKeyStr);
            
            if (addressOid == null && publicKeyStr != null) 
            {
              addressOid = createAddress(publicKeyStr);
            }
            
            com.objy.data.Instance outputInstance = 
                    objyAccess.createOutput(outputId++, publicKeyStr, 
                            addressOid, coin.getValue());
            
            objyAccess.addOutputToTransaction(
                    new com.objy.data.Reference(outputInstance), transactionRef);
            
            
          } catch(org.bitcoinj.core.ScriptException ex) {
            System.out.println("... error getting address: " + ex.getMessage());
            System.out.println("OUTPUT: " + output.toString());
            ex.printStackTrace();
          }
        }
    }

    private String createAddress(String publicAddress) {
        com.objy.data.Instance instance = 
                objyAccess.createAddress(publicAddress);
        String addressOid = instance.getObjectId().toString();
        addressCache.put(publicAddress, addressOid);
        
        return addressOid;
    }

   
    /**
     * 
     * @param args 
     */
    public static void main(String[] args)
    {

      if (args.length < 2) {
        System.out.println("Params missing: <blocks dire path> <boot_file_path");
        return;
      }
      String blocksPath = args[0];
      String bootFile = args[1];
      
      System.out.println("Bitcoin blocks data: " + blocksPath);
      System.out.println("Objectivity federation: " + bootFile);
      
      BitcoinIngester ingester = new BitcoinIngester(bootFile);
      
      boolean success = ingester.setupBlockFileLoader(blocksPath);
      
      if (!success) {
        System.err.println("Failed to setup the block files loader.");
        return;
      }
      
      ingester.setupObjySchema();
      
      ingester.processBlocks();
      
      ingester.printStats();
    }  

}
