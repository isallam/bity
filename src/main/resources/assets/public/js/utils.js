
var Utils = {
		
    removeSelectOptions : function(obj) {
        while(obj.options.length > 1) // we'll need to leave the first for "All..."
            obj.remove(obj.options.length-1);
    },

    hide : function(elemId) {
	var elem = document.getElementById(elemId);
	elem.style.visibility = 'hidden';
    },

    show : function(elemId) {
	document.getElementById(elemId).style.visibility = 'visible';
    },

	toggle : function(elemId) {
		var elem = document.getElementById(elemId);
		if (elem.style.visibility === 'visible')
			elem.style.visibility = 'hidden';
		else
			elem.style.visibility = 'visible';
	},

    eraseElem : function (elemId) {
	document.getElementById(elemId).style.display = 'none';
    },

    ratifyElem : function (elemId) {
	document.getElementById(elemId).style.display = 'block';
    }
};

var simpleTemplate = '<div class="arrow"></div>' +
	' <div class="sigma-tooltip-header">{{label}}</div>' +
	'  <div class="sigma-tooltip-body">' +
	'    <table>' +
	'      <tr><th>ID</th> <td>{{data.m_Id}}</td></tr>' +
	'      <tr><th>OID</th> <td>{{id}}</td></tr>' +
	'    </table>' +
	'  </div>' +
	'  <div class="sigma-tooltip-footer">Number of connections: {{degree}}</div>';

var blockTemplate = '<div class="arrow"></div>' +
	' <div class="sigma-tooltip-header">{{label}}</div>' +
	'  <div class="sigma-tooltip-body">' +
	'    <table>' +
	'      <tr><th>ID</th> <td>{{data.m_Id}}</td></tr>' +
	'      <tr><th>Version</th> <td>{{data.m_Version}}</td></tr>' +
	'      <tr><th>Time</th> <td>{{data.m_Time}}</td></tr>' +
	'      <tr><th>Hash</th> <td>{{data.m_Hash}}</td></tr>' +
	'      <tr><th># Transactions</th> <td>{{data.m_Transactions}}</td></tr>' +
	'      <tr><th>OID</th> <td>{{id}}</td></tr>' +
	'    </table>' +
	'  </div>' +
	'  <div class="sigma-tooltip-footer">Number of connections: {{degree}}</div>';

var transactionTemplate = '<div class="arrow"></div>' +
	' <div class="sigma-tooltip-header">{{label}}</div>' +
	'  <div class="sigma-tooltip-body">' +
	'    <table>' +
//	'      <tr><th>ID</th> <td>{{data.m_Id}}</td></tr>' +
	'      <tr><th>Hash</th> <td>{{data.m_Hash}}</td></tr>' +
	'      <tr><th># Inputs</th> <td>{{data.m_Inputs}}</td></tr>' +
	'      <tr><th># Outputs</th> <td>{{data.m_Outputs}}</td></tr>' +
	'      <tr><th>OID</th> <td>{{id}}</td></tr>' +
	'    </table>' +
	'  </div>' +
	'  <div class="sigma-tooltip-footer">Number of connections: {{degree}}</div>';

var inputTemplate = '<div class="arrow"></div>' +
	' <div class="sigma-tooltip-header">{{label}}</div>' +
	'  <div class="sigma-tooltip-body">' +
	'    <table>' +
//	'      <tr><th>ID</th> <td>{{data.m_Id}}</td></tr>' +
	'      <tr><th>isCoinBase</th> <td>{{data.m_IsCoinBase}}</td></tr>' +
	'      <tr><th>OID</th> <td>{{id}}</td></tr>' +
	'    </table>' +
	'  </div>' +
	'  <div class="sigma-tooltip-footer">Number of connections: {{degree}}</div>';

var outputTemplate = '<div class="arrow"></div>' +
	' <div class="sigma-tooltip-header">{{label}}</div>' +
	'  <div class="sigma-tooltip-body">' +
	'    <table>' +
//	'      <tr><th>ID</th> <td>{{data.m_Id}}</td></tr>' +
	'      <tr><th>Value</th> <td>{{data.m_Value}}</td></tr>' +
	'      <tr><th>OID</th> <td>{{id}}</td></tr>' +
	'    </table>' +
	'  </div>' +
	'  <div class="sigma-tooltip-footer">Number of connections: {{degree}}</div>';
    
var addressTemplate = '<div class="arrow"></div>' +
	' <div class="sigma-tooltip-header">{{label}}</div>' +
	'  <div class="sigma-tooltip-body">' +
	'    <table>' +
	'      <tr><th>Hash</th> <td>{{data.m_Hash}}</td></tr>' +
	'      <tr><th># Outputs</th> <td>{{data.m_Outputs}}</td></tr>' +
	'      <tr><th>OID</th> <td>{{id}}</td></tr>' +
	'    </table>' +
	'  </div>' +
	'  <div class="sigma-tooltip-footer">Number of connections: {{degree}}</div>';

var tagTemplate = '<div class="arrow"></div>' +
	' <div class="sigma-tooltip-header">{{label}}</div>' +
	'  <div class="sigma-tooltip-body">' +
	'    <table>' +
	'      <tr><th>Label</th> <td>{{data.m_Label}}</td></tr>' +
	'      <tr><th>Ref  </th> <td>{{data.m_Ref}}</td></tr>' +
	'      <tr><th>OID</th> <td>{{id}}</td></tr>' +
	'    </table>' +
	'  </div>' +
	'  <div class="sigma-tooltip-footer">Number of connections: {{degree}}</div>';


function getTemplate(nodeLabel) {
	console.log("Getting template for: ", nodeLabel);

	if (nodeLabel === 'Block')
		return blockTemplate;
	else if (nodeLabel === 'Transaction')
		return transactionTemplate;
	else if (nodeLabel === 'Input')
			return inputTemplate;
	else if (nodeLabel === 'Output')
			return outputTemplate;
    else if (nodeLabel === 'Address')
            return addressTemplate;
    else if (nodeLabel === 'Tag')
            return tagTemplate;
	else
		return simpleTemplate;

	// var nodeTemplate = 		    '<div class="arrow"></div>' +
	// ' <div class="sigma-tooltip-header">{{label}}</div>' +
	// '  <div class="sigma-tooltip-body">' +
	// '    <table>' +
	// '      <tr><th>ID</th> <td>{{data.m_Id}}</td></tr>' +
	// '      <tr><th>OID</th> <td>{{data.oid}}</td></tr>' +
	// '      <tr><th>Edges</th> <td>{{data.edges}}</td></tr>' +
	// '    </table>' +
	// '  </div>' +
	// '  <div class="sigma-tooltip-footer">Number of connections: {{degree}}</div>';

	// return nodeTemplate;

}

function getNodeSize (node) {
	var label = node.label;
	var retVal = 6;

//	if (label === 'Transaction') {
//		retVal = node.data.m_Inputs + node.data.m_Outputs/10;
//	} else if (label === 'Block') {
//      retVal = node.data.m_Transactions/10;
//    } else if (label === 'Address') {
//      retVal = node.data.m_Outputs/10;
//    } else if (label === 'Output') {
//      retVal = node.data.m_Value / 1000000000
//    }
	return retVal
};

function getColor (node) {
	var label = node.label;
	var retVal = '#556677'

	if (!iconUrls[label]) {
		console.error("Unknown URL for node: " + node);
	}
	else {
		retVal = iconUrls[label][1]
		if (retVal == null)
			retVal = '#660044'
	}
	return retVal
};

function getUrl (type) {
  retVal = 'icons/blue.png'
  if (!iconUrls[type]) {
    console.error("Unknown URL for node: " + node);
  }
  else {
    retVal = iconUrls[type][0]
  }
  return retVal
};

function getTrxType(node) {
	var trxType = "";
	if (node.label == 'Transaction') {
		trxType = "Order"
		if (node.data.m_Type == 'G')
			trxType = 'CancelReplace'
		else if (node.data.m_Type == '8')
			trxType = 'Fill'
		else if (node.data.m_Type == 'F')
			trxType = 'Cancel'
	}
	return trxType;
}

function isString (obj) {
  return (Object.prototype.toString.call(obj) === '[object String]');
}

function formPattern(patternList, edgeList) {
  var patternString = "";
  var first = true;
  edgeList.forEach(function(edgeElem) {
    // find source
    var sourceNode = patternList[edgeElem.source];
    //var attribute = elem.n.data;
    if (!first){
//      if (sourceNode.edge != null)
//        patternString += "-[:" + sourceNode.edge.attr +"]->";
//      else 
        patternString += "-->";
    } else {
      first = false;
    }
    var paramListString = "";
    var paramListFirst = true;
    sourceNode.paramList.forEach(function (param) {
      var attributeString = attributeToDO(sourceNode.className, param);
      if (attributeString != null) {
        if (!paramListFirst) {
          paramListString += " AND ";
        } else {
          paramListFirst = false;
        }
        paramListString += attributeString;
      }
    });
    patternString += "(:" + sourceNode.className + " { " + paramListString + "})";
  });
  return patternString;
}

function printInfo(collectedInfo) {
  console.log(collectedInfo.classes);
  if (collectedInfo.next != null)
  {
    console.log(' --> ')
    printInfo(collectedInfo.next);
  }
}

function analyzePathElement(pathElement, collectedInfo)
{
  for (var key in pathElement.to) {
    if (collectedInfo.next == null)
      collectedInfo.next = {};
    var nextElement = pathElement.to[key];
    analyzePathElement(nextElement, collectedInfo.next);
  }
  
  if (collectedInfo.classes == null) {
    collectedInfo.classes = []; 
    collectedInfo.objects = [];
  }
  collectedInfo.classes.push(pathElement.from.className);
  collectedInfo.objects.push(pathElement.from);
  
}

function analyzePaths(pathList) 
{ 
  var collectedInfos = [];
  for (var key in pathList) {
    var collectedInfo = {}
    analyzePathElement(pathList[key], collectedInfo);
    collectedInfos.push(collectedInfo);
  }
  return collectedInfos;
}

function findEntityWithSource(elemEntity, id)
{
  var elem = null;
  if (elemEntity.from.id === id) {
    elem = elemEntity;
  } else if (elemEntity.to != []) {
    for (var i = 0; i < elemEntity.to.length; i++) {
      elem = findEntityWithSource(elemEntity.to[i], id)
      if (elem !== null)
        return elem;
    }
  }
  return elem;
}

function findEntityInPath(pathList, id)
{
  var elem = null;
  for (var i = 0; i < pathList.length; i++) {
      elem = findEntityWithSource(pathList[i], id);
      if (elem !== null)
        break;
  }
  return elem;
}

function getPaths(nodeList, sigmaGraph)
{
  var pathList = [];
  var ids = [];
  for (var key in nodeList) {
    ids.push(key);
  }
  sigmaGraph.graph.edges().forEach(function (e) {
    
    if (ids.includes(e.source) && ids.includes(e.target)) {

      var entity = findEntityInPath(pathList, e.source);
      
      if (entity == null)
      {
        // create an entity
        entity = {
              from: nodeList[e.source],
              to: [] //[{id: e.target, data: nodeList[e.target]}]
            };
        pathList.push(entity);
      }
      if (entity !== null) // add the "to" information
      {
        // to avoid loops, then we need to check if our target for this entry is
        // already in the path.
        if (findEntityInPath(pathList, e.target) == null)
        {
          entity.to.push({ 
              from: nodeList[e.target],
              to: []
            });
        }
      }
//      else {
//        if (findEntityInPath(pathList, e.target) == null)
//        {
//          var aTarget = {
//              from: nodeList[e.target],
//              to: []
//            };
//          entity.to.push(aTarget);
//        }
//      }
    }
  });
  return pathList;
}


function attributeToDO(className, attr) {
  if (className !== 'Block' && attr.key === 'm_Id')
    return null; // we'll ignore all other m_Id except for block.
  var attrName = attr.key;
  var attrValue = attr.value;
  if (attrName === 'm_Inputs' || attrName === 'm_Outputs')
    attrName = "LENGTH(" + attrName + ")";
  
  if (typeof attrValue === 'string')
    attrValue = '"' + attrValue + '"';

  var attributeString = attrName + ' == ' + attrValue;
  return attributeString;
}

function findElementWithSource_working_but_need_refactoring(elemEntity, id)
{
  var elem = null;
  if (elemEntity.from.id === id) {
    elem = elemEntity;
  } else if (elemEntity.to != {}) {
    for (var key in elemEntity.to) {
      elem = findElementWithSource(elemEntity.to[key], id)
      if (elem != null)
        return elem;
    }
  }
  return elem;
}

function getPaths_working_but_need_refactoring(nodeList, sigmaGraph)
{
  var pathList = {};
  var ids = [];
  for (var key in nodeList) {
    ids.push(key);
  }
  sigmaGraph.graph.edges().forEach(function (e) {
    if (ids.includes(e.source) && ids.includes(e.target)) {
      var elem = pathList[e.source];
      console.log("ArraySize1: ", Object.keys(pathList).length);
      //console.log("ArraySize2: ", pathList.keys().length);
      if (elem == null && Object.keys(pathList).length > 0) {
        // go through all elements to see if the e.source is actually a target id inside
        // then add the target to such element...
        for (var key in pathList) {
          elem = findElementWithSource(pathList[key], e.source);
          if (elem != null)
            break;
        }
      }
      
      if (elem == null)
      {
          pathList[e.source] = {
              from: nodeList[e.source],
              to: {} //[{id: e.target, data: nodeList[e.target]}]
            };
            // to avoid loops, then we need to check if our target for this entry is
            // already in the path.
            //if (!nodeInPath(e.target))
            {
              pathList[e.source].to[e.target] = { 
                  from: nodeList[e.target],
                  to: {}
                };
            }
      }
      else {
        //if (!nodeInPath(e.target))
        {
          var aTarget = {
              from: nodeList[e.target],
              to: {}
            };
          elem.to[e.target] = aTarget;
        }
      }
    }
  });
  return pathList;
}
