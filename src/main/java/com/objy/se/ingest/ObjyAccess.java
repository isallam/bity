/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objy.se.ingest;

import com.objy.data.ClassBuilder;
import com.objy.data.DataSpecification;
import com.objy.data.Instance;
import com.objy.data.LogicalType;
import com.objy.data.Reference;
import com.objy.data.Storage;
import com.objy.data.Variable;
import com.objy.data.dataSpecificationBuilder.IntegerSpecificationBuilder;
import com.objy.data.dataSpecificationBuilder.ListSpecificationBuilder;
import com.objy.data.dataSpecificationBuilder.ReferenceSpecificationBuilder;
import com.objy.data.schemaProvider.SchemaProvider;
import com.objy.db.ObjectId;
import com.objy.db.ObjectivityException;
import com.objy.db.TransactionMode;
import com.objy.db.TransactionScope;
import java.util.Date;

/**
 *
 * @author ibrahim
 */
class ClassCache {

  String name;
  com.objy.data.Class classRef;
  com.objy.data.Variable value = new com.objy.data.Variable();
  com.objy.data.Variable stringValue = new com.objy.data.Variable();
};

class BlockClass extends ClassCache {

  com.objy.data.Attribute idAttr;
  com.objy.data.Attribute versionAttr;
  com.objy.data.Attribute timeAttr;
  com.objy.data.Attribute hashAttr;
  com.objy.data.Attribute prevBlockHashAttr;
  com.objy.data.Attribute merkleRootHashAttr;
  com.objy.data.Attribute prevBlockAttr;
  com.objy.data.Attribute nextBlockAttr;
  com.objy.data.Attribute transactionsAttr;
};

class TransactionClass extends ClassCache {

  com.objy.data.Attribute idAttr;
  com.objy.data.Attribute hashAttr;
  com.objy.data.Attribute blockAttr;
  com.objy.data.Attribute inputsAttr;
  com.objy.data.Attribute outputsAttr;
};

class InputClass extends ClassCache {

  com.objy.data.Attribute idAttr;
  com.objy.data.Attribute isCoinBaseAttr;
  com.objy.data.Attribute upTxHashAttr;
  com.objy.data.Attribute upTxAttr;
  com.objy.data.Attribute transactionAttr;
};

class OutputClass extends ClassCache {

  com.objy.data.Attribute idAttr;
  com.objy.data.Attribute valueAttr;
  com.objy.data.Attribute publicAddressAttr;
  com.objy.data.Attribute addressAttr;
  com.objy.data.Attribute transactionAttr;
};

class AddressClass extends ClassCache {

  //com.objy.data.Attribute hashAttr;
  com.objy.data.Attribute publicAddressAttr;
  com.objy.data.Attribute outputsAttr;
};

public class ObjyAccess {

  // class names
  final String BlockClassName = "Block";
  final String TransactionClassName = "Transaction";
  final String InputClassName = "Input";
  final String OutputClassName = "Output";
  final String AddressClassName = "Address";

  final String BlockIdAttr = "m_Id";
  final String BlockVersionAttr = "m_Version";
  final String BlockTimeAttr = "m_Time";
  final String BlockHashAttr = "m_Hash";
  final String BlockPrevBlockHashAttr = "m_PrevBlockHash";
  final String BlockMerkleRootHashAttr = "m_MerkleRootHash";
  final String BlockPrevBlockAttr = "m_PrevBlock";
  final String BlockNextBlockAttr = "m_NextBlock";
  final String BlockTransactionsAttr = "m_Transactions";

  final String TransactionIdAttr = "m_Id";
  final String TransactionHashAttr = "m_Hash";
  final String TransactionBlockAttr = "m_Block";
  final String TransactionInputsAttr = "m_Inputs";
  final String TransactionOutputsAttr = "m_Outputs";

  final String InputIdAttr = "m_Id";
  final String InputIsCoinBaseAttr = "m_IsCoinBase";
  final String InputUpTxHashAttr = "m_UpTxHash";
  final String InputUpTxAttr = "m_UpTx";
  final String InputTransactionAttr = "m_Transaction";

  final String OutputIdAttr = "m_Id";
  final String OutputValueAttr = "m_Value";
  final String OutputPublicAddressAttr = "m_PublicAddress";
  final String OutputAddressAttr = "m_Address";
  final String OutputTransactionAttr = "m_Transaction";

  //final String AddressHashAttr = "m_Hash";
  final String AddressPublicAddressAttr = "m_PublicAddress";
  final String AddressOutputsAttr = "m_Outputs";

  // more caching 
  BlockClass blockClass             = new BlockClass();
  TransactionClass transactionClass = new TransactionClass();
  InputClass inputClass             = new InputClass();
  OutputClass outputClass           = new OutputClass();
  AddressClass addressClass         = new AddressClass();

  /**
   * createSchema
   *
   * @return
   */
  boolean createSchema() {
    // -----------------------------------------------------------
    // Some needed specs for various references and collections...
    // -----------------------------------------------------------

    try (TransactionScope tx = new TransactionScope(TransactionMode.READ_UPDATE))
    {
    // Block Reference
    DataSpecification blockRefSpec
            = new ReferenceSpecificationBuilder() 
                .setReferencedClass(BlockClassName)
                .build();
    
    // transaction Reference 
    DataSpecification transactionRefSpec
            = new ReferenceSpecificationBuilder() 
                    .setReferencedClass(TransactionClassName)
                    .build();
    // transaction List                 
    DataSpecification transactionsSpec
            = new ListSpecificationBuilder()
                    .setElementSpecification(transactionRefSpec)
                    .setCollectionName("SegmentedArray")
                    .build();
    // input reference
    DataSpecification inputsRefSpec
            = new ReferenceSpecificationBuilder() 
                    .setReferencedClass(InputClassName)
                    .build();
    // Input list
    DataSpecification inputsSpec
            = new ListSpecificationBuilder()
                    .setElementSpecification(inputsRefSpec)
                    .setCollectionName("SegmentedArray")
                    .build();
    // Output reference
    DataSpecification outputRefSpec
            = new ReferenceSpecificationBuilder() 
                    .setReferencedClass(OutputClassName)
                    .build();
    // Output list
    DataSpecification outputsSpec
            = new ListSpecificationBuilder()
                    .setElementSpecification(outputRefSpec)
                    .setCollectionName("SegmentedArray")
                    .build();
    // Output list for address
    DataSpecification outputsForAddressSpec
            = new ListSpecificationBuilder()
                    .setElementSpecification(outputRefSpec)
                    .setCollectionName("SegmentedArray")
                    .build();
    // Address reference
    DataSpecification addressRefSpec
            = new ReferenceSpecificationBuilder()
                    .setReferencedClass(AddressClassName)
                    .build();

    // Embedded string spec (currently not used)
//    DataSpecification stringSpec
//            = new StringSpecifictaionBuilder()
//                    .setEncoding(com.objy.data.StringEncoding.Utf8)
//                    .setStorage(com.objy.data.StringStorage.Fixed)
//                    .setFixedLength(66)
//                    .build();


// -------------------
    // Block Class
    // -------------------
    com.objy.data.Class blockClassRep = new ClassBuilder(BlockClassName)
                  .setSuperclass("ooObj")
                  .addAttribute(LogicalType.INTEGER, BlockIdAttr)
                  .addAttribute(LogicalType.INTEGER, BlockVersionAttr)
                  .addAttribute(LogicalType.DATE_TIME, BlockTimeAttr)
                  .addAttribute(LogicalType.STRING, BlockHashAttr)
                  .addAttribute(LogicalType.STRING, BlockPrevBlockHashAttr)
                  .addAttribute(LogicalType.STRING, BlockMerkleRootHashAttr)
                    //.addAttribute("hash", stringSpec)
                    //.addAttribute("prevBlockHash", stringSpec)
                    //.addAttribute("merkleRootHash", stringSpec)
                  .addAttribute(BlockPrevBlockAttr, blockRefSpec)
                  .addAttribute(BlockNextBlockAttr, blockRefSpec)
                  .addAttribute(BlockTransactionsAttr, transactionsSpec)
                  .build();
    // -------------------
    // Transaction Class
    // -------------------
    com.objy.data.Class transactionClassRep = new ClassBuilder(TransactionClassName)
                  .setSuperclass("ooObj")
                  .addAttribute(LogicalType.INTEGER, TransactionIdAttr) 
                  //.addAttribute(com.objy.data.LogicalType.DateTime, "time")
                  .addAttribute(LogicalType.STRING, TransactionHashAttr)
                  .addAttribute(TransactionBlockAttr, blockRefSpec)
                  .addAttribute(TransactionInputsAttr, inputsSpec)
                  .addAttribute(TransactionOutputsAttr, outputsSpec)
                  .build();

    // -------------------
    // Input Class
    // -------------------
    com.objy.data.Class inputClassRep = new ClassBuilder(InputClassName)
                  .setSuperclass("ooObj")
                  .addAttribute(LogicalType.INTEGER, InputIdAttr)
                  .addAttribute(InputTransactionAttr, transactionRefSpec)
                  .addAttribute(LogicalType.STRING, InputUpTxHashAttr)
                  .addAttribute(InputUpTxAttr, transactionRefSpec)
                  .addAttribute(LogicalType.BOOLEAN, InputIsCoinBaseAttr)
                  .build();
    // -------------------
    // Address Class
    // -------------------
    com.objy.data.Class addressClassRep = new ClassBuilder(AddressClassName)
                  .setSuperclass("ooObj")
                  //.addAttribute(LogicalType.STRING, AddressHashAttr)
                  .addAttribute(LogicalType.STRING, AddressPublicAddressAttr)
                  .addAttribute(AddressOutputsAttr, outputsForAddressSpec)
                  .build();
    // -------------------
    // output Class
    // -------------------
    com.objy.data.Class outputClassRep = new ClassBuilder(OutputClassName)
                  .setSuperclass("ooObj")
                  .addAttribute(LogicalType.INTEGER, OutputIdAttr)
                  .addAttribute(OutputTransactionAttr, transactionRefSpec)
                  .addAttribute(LogicalType.STRING, OutputPublicAddressAttr)
                  .addAttribute(OutputAddressAttr, addressRefSpec)
                  .addAttribute(OutputValueAttr, 
                          new IntegerSpecificationBuilder(
                                  Storage.Integer.B64).build())
                  .build();

    SchemaProvider provider = SchemaProvider.getDefaultPersistentProvider();
    provider.represent(blockClassRep);
    provider.represent(transactionClassRep);
    provider.represent(inputClassRep);
    provider.represent(outputClassRep);
    provider.represent(addressClassRep);
    
    tx.complete();

    }
    catch(ObjectivityException e){
      e.printStackTrace();
    }
    return true;
  }

  /**
   *
   * @return
   */
  boolean setupCache() {
    // cache classes.
    blockClass.classRef = com.objy.data.Class.lookupClass(BlockClassName);
    blockClass.idAttr = blockClass.classRef.lookupAttribute(BlockIdAttr);
    blockClass.versionAttr = blockClass.classRef.lookupAttribute(BlockVersionAttr);
    blockClass.timeAttr = blockClass.classRef.lookupAttribute(BlockTimeAttr);
    blockClass.hashAttr = blockClass.classRef.lookupAttribute(BlockHashAttr);
    blockClass.prevBlockHashAttr = blockClass.classRef.lookupAttribute(BlockPrevBlockHashAttr);
    blockClass.merkleRootHashAttr = blockClass.classRef.lookupAttribute(BlockMerkleRootHashAttr);
    blockClass.prevBlockAttr = blockClass.classRef.lookupAttribute(BlockPrevBlockAttr);
    blockClass.nextBlockAttr = blockClass.classRef.lookupAttribute(BlockNextBlockAttr);
    blockClass.transactionsAttr = blockClass.classRef.lookupAttribute(BlockTransactionsAttr);

    transactionClass.classRef = com.objy.data.Class.lookupClass(TransactionClassName);
    transactionClass.idAttr = transactionClass.classRef.lookupAttribute(TransactionIdAttr);
    transactionClass.hashAttr = transactionClass.classRef.lookupAttribute(TransactionHashAttr);
    transactionClass.blockAttr = transactionClass.classRef.lookupAttribute(TransactionBlockAttr);
    transactionClass.inputsAttr = transactionClass.classRef.lookupAttribute(TransactionInputsAttr);
    transactionClass.outputsAttr = transactionClass.classRef.lookupAttribute(TransactionOutputsAttr);

    inputClass.classRef = com.objy.data.Class.lookupClass(InputClassName);
    inputClass.idAttr = inputClass.classRef.lookupAttribute(InputIdAttr);
    inputClass.isCoinBaseAttr = inputClass.classRef.lookupAttribute(InputIsCoinBaseAttr);
    inputClass.upTxHashAttr = inputClass.classRef.lookupAttribute(InputUpTxHashAttr);
    inputClass.upTxAttr = inputClass.classRef.lookupAttribute(InputUpTxAttr);
    inputClass.transactionAttr = inputClass.classRef.lookupAttribute(InputTransactionAttr);

    outputClass.classRef = com.objy.data.Class.lookupClass(OutputClassName);
    outputClass.idAttr = outputClass.classRef.lookupAttribute(OutputIdAttr);
    outputClass.valueAttr = outputClass.classRef.lookupAttribute(OutputValueAttr);
    outputClass.publicAddressAttr = outputClass.classRef.lookupAttribute(OutputPublicAddressAttr);
    outputClass.addressAttr = outputClass.classRef.lookupAttribute(OutputAddressAttr);
    outputClass.transactionAttr = outputClass.classRef.lookupAttribute(OutputTransactionAttr);

    addressClass.classRef = com.objy.data.Class.lookupClass(AddressClassName);
    //addressClass.hashAttr = addressClass.classRef.lookupAttribute(AddressHashAttr);
    addressClass.publicAddressAttr = addressClass.classRef.lookupAttribute(AddressPublicAddressAttr);
    addressClass.outputsAttr = addressClass.classRef.lookupAttribute(AddressOutputsAttr);

    return true;
  }

  /**
   *
   * @param id
   * @param version
   * @param prevBlockHash
   * @param blockMerkleRoot
   * @param blkTime
   * @param hash
   * @param prevBlock
   * @return
   */
  com.objy.data.Instance createBlock(
          int id, long version, String prevBlockHash,
          String blockMerkleRoot, Date blkTime, String hash,
          com.objy.data.Reference prevBlock) {
    
    com.objy.data.Instance instance = 
            com.objy.data.Instance.createPersistent(blockClass.classRef);

    instance.getAttributeValue(blockClass.idAttr, blockClass.value);
    blockClass.value.set(id);

    instance.getAttributeValue(blockClass.versionAttr, blockClass.value);
    blockClass.value.set(version);

    instance.getAttributeValue(blockClass.timeAttr, blockClass.value);
    blockClass.value.set(new com.objy.db.DateTime(blkTime));
  
    //blockClass.stringValue.set(hash);
    instance.getAttributeValue(blockClass.hashAttr, blockClass.value);
    blockClass.value.set(hash);

    //blockClass.stringValue.set(prevBlockHash);
    instance.getAttributeValue(blockClass.prevBlockHashAttr, blockClass.value);
    blockClass.value.set(prevBlockHash);

    //blockClass.stringValue.set(blockMerkleRoot);
    instance.getAttributeValue(blockClass.merkleRootHashAttr, blockClass.value);
    blockClass.value.set(blockMerkleRoot);

    if (prevBlock != null) {
      instance.getAttributeValue(blockClass.prevBlockAttr, blockClass.value);
      blockClass.value.set(prevBlock);
      // set next block on prevBlock
      prevBlock.getReferencedObject().getAttributeValue(
              blockClass.nextBlockAttr, blockClass.value);
      blockClass.value.set(new Reference(instance));
    }
  
    return instance;
  }

  /**
   *
   * @param id
   * @param hash
   * @return
   */
  com.objy.data.Instance createTransaction(
          int id, String hash) {
    
    com.objy.data.Instance instance = 
            com.objy.data.Instance.createPersistent(transactionClass.classRef);

    instance.getAttributeValue(transactionClass.idAttr, transactionClass.value);
    transactionClass.value.set(id);

    //transactionClass.stringValue.set(hash);
    //instance.getAttributeValue("hash").set<objydata::Utf8String>(value);
    instance.getAttributeValue(transactionClass.hashAttr, transactionClass.value);
    transactionClass.value.set(hash);

    /**
     * //.addAttribute(objydata::LogicalType.DateTime, "time")
     *
     */
    return instance;
  }

  /**
   *
   * @param id
   * @param upTxHash
   * @param upTrxRef
   * @param isCoinBase
   * @return
   */
  com.objy.data.Instance createInput(
          int id, String upTxHash,
          String upTrxOid, boolean isCoinBase) {
    
    com.objy.data.Instance instance = 
            com.objy.data.Instance.createPersistent(inputClass.classRef);

    instance.getAttributeValue(inputClass.idAttr, inputClass.value);
    inputClass.value.set(id);

    instance.getAttributeValue(inputClass.isCoinBaseAttr, inputClass.value);
    inputClass.value.set(isCoinBase);

    //inputClass.stringValue.set(upTxHash);
    instance.getAttributeValue(inputClass.upTxHashAttr, inputClass.value);
    inputClass.value.set(upTxHash);

    if (!isCoinBase/* && !upTrxRef*/) {
      instance.getAttributeValue(inputClass.upTxAttr, inputClass.value);
      inputClass.value.set(new com.objy.data.Reference(ObjectId.fromString(upTrxOid)));
    }

    return instance;
  }

  /**
   *
   * @param id
   * @param address
   * @param addressRef
   * @param trxValue
   * @return
   */
  com.objy.data.Instance createOutput(
          int id, String publicAddress,
          String addressOid, long trxValue) {
    
    com.objy.data.Instance instance = 
            com.objy.data.Instance.createPersistent(outputClass.classRef);

    instance.getAttributeValue(outputClass.idAttr, outputClass.value);
    outputClass.value.set(id);

    instance.getAttributeValue(outputClass.valueAttr, outputClass.value);
    outputClass.value.set(trxValue);

    //outputClass.stringValue.set(address);
    instance.getAttributeValue(outputClass.publicAddressAttr, outputClass.value);
    outputClass.value.set(publicAddress);

    if (null != addressOid) {
      Reference addressRef = new Reference(ObjectId.fromString(addressOid));

      instance.getAttributeValue(outputClass.addressAttr, outputClass.value);
      outputClass.value.set(addressRef);

      // add output to address
      addressRef.getReferencedObject().getAttributeValue(
              addressClass.outputsAttr, addressClass.value);
      addressClass.value.listValue().add(new Variable(new Reference(instance)));

    }
    return instance;
  }

  /**
   *
   * @param hash
   * @return
   */
  com.objy.data.Instance createAddress(String publicAddress) {
    
    com.objy.data.Instance instance = 
            com.objy.data.Instance.createPersistent(addressClass.classRef);

    //addressClass.stringValue.set(hash);
    //instance.getAttributeValue(addressClass.hashAttr, addressClass.value);
    //addressClass.value.set(hash);

    instance.getAttributeValue(addressClass.publicAddressAttr, addressClass.value);
    addressClass.value.set(publicAddress);

    return instance;
  }

  boolean addTransactionToBlock(com.objy.data.Reference transaction, 
          com.objy.data.Reference block) {

    block.getReferencedObject().getAttributeValue(
            blockClass.transactionsAttr, blockClass.value);
    blockClass.value.listValue().add(new Variable(transaction));

    // add block to transaction.
    transaction.getReferencedObject().getAttributeValue(
            transactionClass.blockAttr, transactionClass.value);  
    transactionClass.value.set(block);

    return true;
  }

  boolean addInputToTransaction(com.objy.data.Reference input, 
          com.objy.data.Reference transaction) {
    transaction.getReferencedObject().getAttributeValue(
            transactionClass.inputsAttr, transactionClass.value);
    transactionClass.value.listValue().add(new Variable(input));

    // add transaction to input
    input.getReferencedObject().getAttributeValue(
            inputClass.transactionAttr, inputClass.value);
    inputClass.value.set(transaction);

    return true;
  }

  boolean addOutputToTransaction(com.objy.data.Reference output, 
          com.objy.data.Reference transaction) {
    transaction.getReferencedObject().getAttributeValue(
            transactionClass.outputsAttr, transactionClass.value);
    transactionClass.value.listValue().add(new Variable(output));

    // add transaction to input
    output.getReferencedObject().getAttributeValue(
            inputClass.transactionAttr, outputClass.value);
    outputClass.value.set(transaction);

    return true;
  }

}
