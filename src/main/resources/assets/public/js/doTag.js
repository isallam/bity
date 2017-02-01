/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var DoTag = {
    contextList: [],
    init: function ()
    {
        // TBD...
    },

    containerName: null,

    /**
     * execut()
     * @param container
     * @param doStatement
     */
    tag: function (containerName, oid, tagText)
    {
        this.containerName = containerName;
        
        var doStatement = "Create @Tag {m_label = \"" + 
                              tagText + "\", m_Ref = " + oid + "}";

        writeToStatus("Tagging using DO: " + doStatement)

        //this.clearLocateLists()
        var msg = {"qType": "DOUpdate",
            "qContext": this.containerName,
            "doStatement": doStatement,
            "verbose": 2};
        WebSocketHandler.sendMessage(msg, this)
        this.inQuery = true;
    },

    /***
     * allow us to do any post query stuff
     * @param context
     */
    executeCompleted: function (context) {

        //Start the ForceLink algorithm:
        //sigma.layouts.startForceLink();
        document.body.style.cursor = 'auto';

        this.inQuery = false;

    }

}

