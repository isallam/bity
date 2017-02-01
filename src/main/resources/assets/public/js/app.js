/* global DemoMode, DoQuery, Utils */

/**
 * Main app file
 */

var qboxDefaultText = 'from Address return *';


function writeToStatus(text) {
    var statusText = document.getElementById('statusBox');
    statusText.value += '\n' + text;
    statusText.scrollTop = statusText.scrollHeight;
}

function doInit() {

    var qbox = document.getElementById("qbox");
    if (qbox)
        qbox.value = qboxDefaultText

    // hide similarity buttton
    //Utils.eraseElem('table-btn');

	// hide similarity buttton
	Utils.eraseElem('similar-nodes-btn')
	Utils.eraseElem('select-nodes-btn')

	// hide all the extra GraphContainers.
	hideOtherGraphContainers();

    // websocket stuff.
    if (!window.WebSocket) {
        content.html($('<p>', {text: 'Sorry, but your browser doesn\'t '
                    + 'support WebSockets.'}));
        input.hide();
        $('span').hide();
    } else {
        //var ws = new WebSocket(host+"/query");
        window.ws = null;
        window.wsReady = false;
    }

    // Initialize the FullScreen plugin with a button:
    sigma.plugins.fullScreen({
        container: 'workContainer',
        btnId: 'graph-btn'
    });

    DoQuery.init();
}

// add onClick for th eimages.


function doQuery() {
    var queryBox = getQueryBox();
    //Utils.ratifyElem('table-btn')
    DoQuery.execute('graphContainer', queryBox.value);
}

function getQueryBox() {
    return document.getElementById("qbox");
}

function getMaxResults() {
    var elem = document.getElementById("max-results");
    var maxResults = elem.value;
    return maxResults;
}

function getLocateButton() {
    return document.getElementById('locate-btn');
}

function getResetViewButton() {
    return document.getElementById('reset-view-btn');
}

function getTableViewButton() {
    return document.getElementById('table-btn');
}

function doResetView() {

    // DoQuery.accountList.selectedIndex = 0;
    // DoQuery.basketList.selectedIndex = 0;
    // DoQuery.serviceList.selectedIndex = 0;
    // DoQuery.firmList.selectedIndex = 0;

    if (DoQuery.locate)
        DoQuery.locate.center(DoQuery.locateZoomDef);
}

function doLayout1View() {
  DoQuery.doForceLayout();
}

function doLayout2View() {
  DoQuery.doNicerLayout();
}

function toggleModelDisplay() {
    Utils.toggle('model-graph')
}

function doShowResultsTable() {
    // showing the results table.
}

function doSelectNodes() {
		// activate lasso
		DoQuery.lasso.activate();
		DoQuery.selectedNodes = null;
		Utils.eraseElem('select-nodes-btn')
		Utils.ratifyElem('similar-nodes-btn')
}

function doNodeSimilarity() {
	if (DoQuery.selectedNodes.length <= 1) // we need at least two nodes.
		return;

	Utils.eraseElem('similar-nodes-btn')
	DoQuery.lasso.deactivate();
	DoQuery.doSimilarity(DoQuery)
}

function hideOtherGraphContainers() {
	Utils.hide('gc1');
	Utils.hide('gc2');
	Utils.hide('gc3');
	Utils.hide('gc4');

	// clearGraphContainer('gc1');
	// clearGraphContainer('gc2');
	// clearGraphContainer('gc3');
	// clearGraphContainer('gc4');
}

function processSelection() {
    var qOption = document.getElementById("qOption");
    var queryString = getQueryBox().value;
    if (qOption.value == 'GetBlock') {
        queryString = 'from Block where m_Id==99 return *';
    } else if (qOption.value == 'BlockToAddress') {
        //queryString = 'Match p=(:Person)-[*1..2]->(:Email) return p';
        queryString = "MATCH p = (:Block{m_Id==\"0\"})-[:m_Transactions]->(:Transaction)" +
            "-[:m_Outputs]->(:Output)-->(:Address) RETURN p";
    } else if (qOption.value === 'SatoshiAnalysis1') {
      //queryString = 'MATCH p = (:Block{m_Id=="0"})-[:m_Transactions]->(:Transaction)' + 
      //      '-[:m_Outputs]->(:Output)-->(:Address)-[:m_Outputs]->(:Output)-->(:Transaction)' +
      //      '-[:m_Inputs]->(:Input{m_IsCoinBase == false}) RETURN p'
      queryString = 'MATCH p = (:Block{m_Id=="0"})-->(:Transaction)-->(:Output)' +
            '-->(:Address)-->(:Output)-->(:Transaction)-->(:Input{m_IsCoinBase == false})' + 
            '-->() RETURN p';
    } else if (qOption.value === 'Wikileaks1') {
      queryString = 'match p = (:Address {m_Hash == "1HB5XMLmzFVj8ALj6mfBsbifRoD4miY36v"})' +
              '-->()-->(:Transaction)-->(:Input)-->(:Transaction) return p';
    } else if (qOption.value === 'NotGenerated1') {
      queryString = 'match p = (:Transaction)-->(:Input {m_IsCoinBase == false}) return p';
    } else if (qOption.value === 'NotGenerated2') {
      queryString = 'match p = (:Transaction {length(m_Inputs) > 100 and length(m_Outputs) > 10})-->(:Input {m_IsCoinBase == false}) return p';
    } else if (qOption.value == 'OneToMany') {
      queryString = 'MATCH p = (:Transaction {length(m_Inputs)==1 and length(m_Outputs)>200})-->() return p';
    } else if (qOption.value == 'ManyToOne') {
      queryString = 'MATCH p = (:Transaction {length(m_Inputs)>200 and length(m_Outputs)==1})-->() return p';
    }
    getQueryBox().value = queryString;
    writeToStatus("Executing: " + qOption.value);
}

function doTag() {
  var elem = document.getElementById("tag-text");
  var oid = elem.getAttribute("oid");
  console.log("taging object, OID: " + oid + " with", elem.value);
  DoTag.tag('statusBox', oid, elem.value);
}

function doPersonAutoComplete() {
    //console.log("GOT list auto...");
    var elem = document.getElementById("person-select");
    var dataList = document.getElementById("person-datalist");

    //console.log("GOT: ", elem.value);
    var doStatement = "From Person where firstName =~~\"^" + elem.value + ".*\" return *";

    DoSearch.execute('person-datalist', doStatement);
}

function doDomainAutoComplete() {
    //console.log("GOT list auto...");
    var elem = document.getElementById("domain-select");
    var dataList = document.getElementById("domain-datalist");

    //console.log("GOT: ", elem.value);
    var doStatement = "From Domain where domain =~~\"^" + elem.value + ".*\" return *";

    DoSearch.execute('domain-datalist', doStatement);
}

//function doSearchPersonToDomain() {
//    var personSelect = document.getElementById("person-select");
//    var domainSelect = document.getElementById("domain-select");
//    var personSelectVal = personSelect.value;
//    var domainSelectVal = domainSelect.value;
//    var findPerson = false;
//    var findDomain = false;
//
//    // let's decide what to do. Either find the person, the domain, navigate or do nothing.
//    if (personSelectVal !== "") {
//        findPerson = true;
//    }
//    if (domainSelectVal !== "") {
//        findDomain = true;
//    }
//
//    if (findPerson && findDomain) {
//        // split on firstName and lastName
//        var names = personSelectVal.split(" ");
//        if (names.length == 2) {
//            var doString = "MATCH p = (:Person{firstName == \"" + names[0] +
//                "\"AND lastName == \"" + names[1] + "\"})-[:sends]->(:Communication)" +
//                "-[:recipient]->(:Person)-->(:Domain{domain==\"" + domainSelectVal + "\"}) RETURN p";
//            DoQuery.execute('graphContainer', doString);
//            Utils.ratifyElem('table-btn')
//        } else {
//            writeToStatus("Error identifying the name: " + names.toString());
//        }
//        // navigagte.
//    }
//    else if (findPerson) {
//        // split on firstName and lastName
//        var names = personSelectVal.split(" ");
//        if (names.length == 2) {
//            var doString = "From Person where firstName == \"" +
//                names[0] + "\" and lastName == \"" + names[1] + "\" return *";
//            DoQuery.execute('graphContainer', doString);
//        } else {
//            writeToStatus("Error identifying the name: " + names.toString());
//        }
//    }
//    else if (findDomain) {
//        var doString = "From Domain where domain == \"" +
//            domainSelectVal + "\" return *";
//        DoQuery.execute('graphContainer', doString);
//    }
//}