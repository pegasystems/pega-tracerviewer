<!-- BEGIN:Rule-HTML-Fragement.TracerEventScripts -->
<pega:include name="TracerDisplayPropertiesScripts"/>
<pega:include name="DesktopWrapperInclude"/>

<script>
// Time outs
var TRACE_TIMEOUT = TRACE_EVENT_TIMEOUT_MILLISECS;
var KEEPALIVE_TIMEOUT = 500;
var REQUEST_NOTHING = 99;

var REQUEST_TRACE = 1;
var REQUEST_KEEPALIVE = 2;
var gFirstTime = true;
var gTraceEventTableHTML;
var gCurrentSelectIcon;
var nRequest = REQUEST_TRACE ;
var gObjTraceEventBeep = null;
var gOpenWatchVar = false;
var gClickFromSpecialCell = false;
var gIgnoreActvityFlag = false;
var gNeedReEnableToolbar = false;
var gPrevActivityNum = 0;
var eventTables = [];
var theDoc = null;
var gIndex = 0;
var remoteNodeId = "";

var gAccessDataPage = null;

var gViewJavaCodeAvailable = false;
gViewJavaCodeAvailable = "<% tools.appendString(tools.getPrimaryPage().getString("pxViewJavaCodeAvailable")); %>";
  
gViewJavaCodeAvailable = (gViewJavaCodeAvailable == "true") ? true : false;

var strRequestorURIevents = "<pega:reference name="pxThread.pxReqURI" mode="normal" />";
var strReqScheme = "<pega:reference name="pxRequestor.pxReqScheme" mode="normal" />";
var strServerPort = "<pega:reference name="pxRequestor.pxReqServerPort" mode="normal" />";
var portalVersion = "<pega:reference name="pxThread.pyPortalVersion" mode="normal" />";
String.prototype.startsWith = function (string) {     return(this.indexOf(string) === 0); }; //Bind startsWith function to String prototype

//
// Functions
//
function createTableForRequestor(requestorId, remoteNodeName) {
	var theDoc = parent.TraceEvent.document;

	if (gFirstTime) {
		if (theDoc == null) {
			return;
		}
		if (theDoc.getElementById("traceEvent-CONTAINER") == null) {
			return;
		}
		gTraceEventTableHTML = theDoc.getElementById("traceEvent-CONTAINER").outerHTML;
		gFirstTime = false;
	}

	if(theDoc.getElementById("traceEvent-REQCONTAINER-" + requestorId) == null) {
		var container = theDoc.getElementById("traceEvent-CONTAINER");
		var nodeNameDisplayString = "";
      

		var reqContainer = theDoc.createElement("DIV");
		reqContainer.id = "traceEvent-REQCONTAINER-" + requestorId;
		
        if (remoteNodeName.length != 0) {
            nodeNameDisplayString = " (" + remoteNodeName + ")";
        }
      
		var labelDiv = theDoc.createElement("DIV");
		labelDiv.style.width="calc(100%-22px)";
		labelDiv.style.height = "20";
      	labelDiv.style.marginLeft = "20px";
        labelDiv.style.paddingTop = "5";
		labelDiv.style.backgroundColor = "rgb(184, 209, 231)";
		labelDiv.style.borderWidth = "thin";
		labelDiv.style.borderStyle = "ridge";
		labelDiv.style.borderColor = "black";
		labelDiv.style.cursor = "pointer";
		labelDiv.style.clear = "left";
		labelDiv.innerHTML = "<h2 style=\"margin-top:0;\">&nbsp;-&nbsp;Events for Requestor: " + requestorId + " on Node: " + remoteNodeId + nodeNameDisplayString + "</h2>";
		reqContainer.appendChild(labelDiv);
		$(labelDiv).click(function() {
			var table = theDoc.getElementById("traceEvent-TABLE-" + requestorId);
			if(table == null)
				return;
			
			if(table.style.display == 'none') {
				labelDiv.innerHTML = "<h2 style=\"margin-top:0;\">&nbsp;-&nbsp;Events for Requestor: " + requestorId + " on Node: " + remoteNodeId + nodeNameDisplayString + "</h2>";
				table.style.display = '';
			} else {
				labelDiv.innerHTML = "<h2 style=\"margin-top:0;\">&nbsp;+&nbsp;Events for Requestor: " + requestorId + " on Node: " + remoteNodeId + nodeNameDisplayString + "</h2>";
				table.style.display = 'none';
			}
		});
		
		container.appendChild(reqContainer);		
	}
	
	if(theDoc.getElementById("traceEvent-TABLE-" + requestorId) == null) {
		var reqContainer = theDoc.getElementById("traceEvent-REQCONTAINER-" + requestorId);
		
		var reqEventTable = theDoc.createElement("TABLE");
		reqEventTable.cellPadding = "0";
		reqEventTable.cellSpacing = "0";
		reqEventTable.style.tableLayout = "fixed";
		reqEventTable.style.textAlign = "left";
		reqEventTable.id = "traceEvent-TABLE-" + requestorId;
		eventTables.push(reqEventTable);
		
		var theTBody = theDoc.createElement("TBODY");
		theTBody.id = "traceEvent-TBODY-" + requestorId;
		//feenr
		var tempRow = theDoc.createElement("TR");
		theTBody.appendChild(tempRow);
		reqEventTable.insertBefore(theTBody, null);
		reqEventTable.style.tableLayout = "fixed";
		reqEventTable.align = "left";
		$(reqEventTable).on('click', selectEventRow);
		
		reqContainer.appendChild(reqEventTable);
	}
	
	return theDoc.getElementById("traceEvent-TABLE-" + requestorId);
}

function traceEvent()
{
	pyMaxTraceEventsDisplayed = parent.MenuRow.getDisplayMaxEvents();

	writeEventInfo("Tracing ..."); // For Test Only + mCount);
	//	writeEventInfo("Tracing ..." + mCount);
	//	writeEventInfo("Tracing DME..." + pyMaxTraceEventsDisplayed);
	
	if (!gReadyForMessage)
		gReadyForMessage = true;

	if (xmlTraceEvent == null) {
		xmlTraceEvent = new XMLDomControl();
	}

	if (xmlTraceEvent == null)
	{
		alert("PegaRULESTracer - traceEvent(): cannot allocate memory for xmlTraceEvent, DOM object!!!");
		return;
	}
	else
	{
		nNextFunction = FUNC_TRACEANDPARSEEVENT;
		xmlTraceEvent.onreadystatechange=checkIfFunctionReady;

		// send request to server
		var strURL;
		if (nRequest == REQUEST_TRACE)
		{
			nTimeOut = TRACE_TIMEOUT;
			strURL = TRACE_EVENT_LOCATION;
		}
		else if (nRequest == REQUEST_KEEPALIVE)
		{
			setReadyState(true);
			nTimeOut = KEEPALIVE_TIMEOUT;
			strURL = TRACE_EVENT_RESPOND;
		}
		else if (nRequest == REQUEST_NOTHING)
		{
			 writeEventInfo("Please restart Tracer");
			 return;
		}
		else
		{
			setReadyState(true);
			nTimeOut = KEEPALIVE_TIMEOUT;
			strURL = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=autocontinue&pzCommandSession=" + debugConnectionID + "&pzDebugConnection=" + connectionID + "&pzXmlOnly=true" +"&pzNodeID=" + nodeID + "&pySessionType=" + pySessionType;
		}

		try
		{
			//xmlTraceEvent.async = false;
			//writeEventMessage("Send to server: " + strURL);

			var loadResult = xmlTraceEvent.load(strURL);
			if (!loadResult)
				alert("Issue loading: traceEvent");
		}
		catch (exception)
		{
			alert ("PegaRULESTracer - traceEvent(): Exception!!!");
			return;
		}
	}
}

function parseTraceEvent()
{
	var pageDataNode = xmlTraceEvent.documentElement.getElementsByTagName("pagedata")[0];
	if(pageDataNode){
		var lclQueueType = pageDataNode.getAttribute("queueType");
		if (lclQueueType) {
			if (queueType != "" && queueType != lclQueueType) {
				if (lclQueueType == "memory") {
					alert("The remote server has switched to a memory based queue. You will only be able to save events that are visible in your tracer window.");
				} else if (lclQueueType == "file") {
					alert("The remote server has swithced to a file based queue. You will be only be able to save events that occur after this point.");
				}
			}
			queueType = lclQueueType;
		}
	}

	var currentStepStatus;
	var theDoc = parent.TraceEvent.document;

	bStopAtBreakpoint = false;
	gOpenWatchVar = false;
	if (gStepContinueEnabled) {
		gStepContinueEnabled = false;
		//enableStepContinue(false);
	}

	var elemList = xmlTraceEvent.getElementsByTagName("keepalive");
	if (elemList.length > 0) {
		nRequest = REQUEST_KEEPALIVE;
		//writeEventInfo("Tracing (Keep alive) ...");
		TRACE_EVENT_RESPOND = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=Trace&pzMaxEvents="+ gMaxEventsPerRequest + "&pzCommandSession=" + debugConnectionID + "&pzDebugConnection=" + connectionID + "&pzXmlOnly=true" +"&pzNodeID=" + nodeID + "&pySessionType=" + pySessionType;
		return;
	}

	// check if requestor is removed due to the timeout. And if it is, pop up a message to user for restarting TRACER
	elemList = xmlTraceEvent.getElementsByTagName("CmdStatus");
	if (elemList.length > 0) {
		var cmdStatus = (elemList[0].textContent || elemList[0].text);
		if (cmdStatus.indexOf("error") >= 0) {
			elemList = xmlTraceEvent.getElementsByTagName("CmdResponse");
			if (elemList.length > 0) {
				var aMessage = "";
				var cmdResponse = (elemList[0].textContent || elemList[0].text);
				if ((cmdResponse.indexOf("Client") >= 0) || (cmdResponse.indexOf("session") >= 0)) {
					//alert("parseTraceEven() cmdStatus = " + cmdStatus);
					aMessage = "Please restart Tracer because " + cmdResponse;
					alert(aMessage);
					reDisableToolbar();
					enableStepContinue(false);
					gTotallyStop = true;
					nRequest = REQUEST_NOTHING;
					if (gTraceEventID) {
						clearInterval(gTraceEventID);
						gTraceEventID = 0;
					}
					nNextFunction = FUNC_COMPLETESTOP;
					//delete xmlTraceEvent; // remove xmlTraceEvent object
					return;
				}
			}
		}
	}

	elemList = xmlTraceEvent.getElementsByTagName("TraceEventHeader");

	if (elemList.length > 0) {
		var rowsArray = [];
		nRequest = REQUEST_TRACE;
		var eventListLength = elemList.length;
		writeEventInfo("Tracing ...");
		for (indx=0; indx < eventListLength; indx++) {
			TRACE_EVENT_LOCATION = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=Trace&pzMaxEvents="+gMaxEventsPerRequest +"&pzCommandSession=" + debugConnectionID + "&pzDebugConnection=" + connectionID + "&pzXmlOnly=true" +"&pzNodeID=" + nodeID + "&pySessionType=" + pySessionType;
			mCount = mCount + 1;
			var strXml = XMLToString(elemList.item(indx));
			gIndex += 1;
			var activityNumber = "";
			var activityName = "";
			var alertLabel = "";
			var DBTData = "";
			var eventKey = "";
			var eventName = "";
			var eventNode = null;
			var eventType = "";
			var adpTracerKey = "";
			var adpTracerRackKey ="";
			var adpLoadPageName="";
			var adpQueueEvent = false;
			var aaQueueEvent = false;
			var hasMessages = false;
			var instanceName = "";
			var instanceHandle = "";
			var interaction = "";
			var threadname = "";
			var requestorId = "";
            var remoteNodeName = "";
			var interactionBytes = "";
			var interactionQueryParam = "";
			var openClass = "";
			var mStepStatus = "";
			var stepMethod = "";
			var stepNumber = "";
			var primaryPageName = "";
			var actionSourcePageName = "";
			var actionTargetPageName = "";
			var sInsKey = null;
			var sKeyName = null;
			var sRSName = null;
			var sRSVersion = null;
			var timeStamp = "";
			var sequenceNumber = "";
			var oneEventNode = elemList.item(indx);
			eventNode = oneEventNode.getElementsByTagName("ActivityNumber")[0];
			if (eventNode != null)
				activityNumber = (eventNode.textContent || eventNode.text);
			else {
				var opList = pega.util.Dom.getElementsByAttribute("Value", "*", "OptionalProperties", oneEventNode);
				if (opList.length > 0) {
					strXml = opList.item(0).xml;
					var nPos = -1;
					nPos = strXml.indexOf("<ActivityNumber>");
					if (nPos > 0) {
						strXml = strXml.substring(0, nPos);
						nPos = strXml.indexOf("<ActivityNumber>");
						if (nPos >= 0)
							activityNumber = strXml.substring(nPos+16,strXml.length);
					}
				}
			}

			eventNode = oneEventNode.getElementsByTagName("ActivityName")[0];
			if (eventNode != null) {
				activityName = (eventNode.textContent || eventNode.text);
				if (activityName == "jZeusDoActivity")
					return;
			}

			eventNode = oneEventNode.getElementsByTagName("PrimaryPageName")[0];
			if (eventNode != null) {
				primaryPageName = (eventNode.textContent || eventNode.text);
                                primaryPageName = getDPNameFromParameterizedDPName(primaryPageName);
			} else { // Handle old code as well
				eventNode = oneEventNode.getElementsByTagName("StepPageName")[0];
				if (eventNode != null) {
					primaryPageName = (eventNode.textContent || eventNode.text);
				}
			}
			
			// source page name if any, in case of Action Begin and Action End events
			eventNode = oneEventNode.getElementsByTagName("ActionSourcePageName")[0];
			if (eventNode != null) {
				actionSourcePageName = (eventNode.textContent || eventNode.text);
			}
			
			// target page name if any, in case of Action Begin and Action End events
			eventNode = oneEventNode.getElementsByTagName("ActionTargetPageName")[0];
                       // alert(eventNode.textContent);
			if (eventNode != null) {

				actionTargetPageName = (eventNode.textContent || eventNode.text);
                                
			}
			
			// Checking if step page has any messages in it. 
			eventNode = oneEventNode.getElementsByTagName("pzStatus")[0];
			if(eventNode != null) {
				var statusMsg = (eventNode.textContent || eventNode.text);
				if(statusMsg.indexOf("false") >= 0){ // pzStatus will be false only when the page has any messages or errors in it. The default value is "valid"
				hasMessages = true;
				}
			}
					
			eventNode = oneEventNode.getElementsByTagName("Interaction")[0];
			if (eventNode != null) {
				interaction  = (eventNode.textContent || eventNode.text);
			}

			eventNode = oneEventNode.getElementsByTagName("ThreadName")[0];
			if (eventNode != null) {
				threadname  = (eventNode.textContent || eventNode.text);
			}
			
			eventNode = oneEventNode.getElementsByTagName("RequestorID")[0];
			if (eventNode != null) {
				requestorId  = (eventNode.textContent || eventNode.text);
			}
          
            eventNode = oneEventNode.getElementsByTagName("NodeID")[0];
            if (eventNode != null) {
              	remoteNodeId = (eventNode.textContent || eventNode.text);
            }
          
            eventNode = oneEventNode.getElementsByTagName("NodeName")[0];
            if (eventNode != null) {
              	remoteNodeName = (eventNode.textContent || eventNode.text);
            }
			
			eventNode = oneEventNode.getAttribute("inskey");
			if (eventNode != null) {
				sInsKey = eventNode;
			}

			eventNode = oneEventNode.getAttribute("keyname");
			if (eventNode != null) {
				sKeyName = eventNode;
			}

			eventNode = oneEventNode.getAttribute("rsname");
			if (eventNode != null) {
				sRSName = eventNode;
			}

			eventNode = oneEventNode.getAttribute("rsvers");
			if (eventNode != null) {
				sRSVersion = eventNode;
			}

			eventNode = oneEventNode.getElementsByTagName("EventKey")[0];
			if (eventNode != null)
				eventKey = (eventNode.textContent || eventNode.text || "");
			else {
				eventNode = oneEventNode.getElementsByTagName("InstanceName")[0];
				if (eventNode != null)
					eventKey = (eventNode.textContent || eventNode.text || "");
			}

			eventNode = oneEventNode.getElementsByTagName("EventType")[0];
			if (eventNode != null) {
				eventType = (eventNode.textContent || eventNode.text);
			}
			
			var tracerKeyNode = oneEventNode.getElementsByTagName("BGTracerKey")[0];
			if (tracerKeyNode != null){
				adpTracerKey = (tracerKeyNode.textContent || tracerKeyNode.text);
			}


			var tracerRackKeyNode = oneEventNode.getElementsByTagName("BGTracerRackKey")[0];
			if (tracerRackKeyNode != null){
				adpTracerRackKey = (tracerRackKeyNode.textContent || tracerRackKeyNode.text);
			}


			var adpPageNameNode = oneEventNode.getElementsByTagName("ADPLoadPageName")[0];
			if (adpPageNameNode != null){
				adpLoadPageName = (adpPageNameNode.textContent || adpPageNameNode.text);
			}



			var adpQueueEventNode = oneEventNode.getElementsByTagName("ADPQueueEvent")[0];
			if (adpQueueEventNode != null && (adpQueueEventNode.textContent || adpQueueEventNode.text) == "true"){
				adpQueueEvent = true;
			}
			
			var aaQueueEventNode = oneEventNode.getElementsByTagName("AsynchronousActivityQueueEvent")[0];
			if (aaQueueEventNode != null && (aaQueueEventNode.textContent || aaQueueEventNode.text) == "true"){
				aaQueueEvent = true;
			}

			eventNode = oneEventNode.getElementsByTagName("EventName")[0];
			if (eventNode != null) {
				eventName = (eventNode.textContent || eventNode.text);
				
				if (eventName != null && eventName == "AsyncTracerEnd")
					gEndOfAsyncTraceEventSent = true;
			}

			eventNode = oneEventNode.getElementsByTagName("Elapsed")[0];
			if (eventNode != null) {
				timeStamp = (eventNode.textContent || eventNode.text);
				if (timeStamp.length == 0) {
					timeStamp = "0.000"; 
				} else {
					var num = new Number(timeStamp)/1000;
					timeStamp = num.toFixed(4);
				}
			}

			eventNode = oneEventNode.getElementsByTagName("mStepStatus")[0];

			if (eventNode != null) {
				mStepStatus =  (eventNode.textContent || eventNode.text);
			} else {
				var WhenStatusNode = oneEventNode.getElementsByTagName("WhenStatus")[0];
				if (WhenStatusNode != null) {
					mStepStatus = (WhenStatusNode.textContent || WhenStatusNode.text);
				}
			}

			eventNode = oneEventNode.getElementsByTagName("StepMethod")[0];
			if (eventNode != null)
				stepMethod = (eventNode.textContent || eventNode.text);

			eventNode = oneEventNode.getElementsByTagName("StepNumber")[0];
			if (eventNode != null)
				stepNumber = (eventNode.textContent || eventNode.text);

			eventNode = oneEventNode.getElementsByTagName("Break")[0];
			if (eventNode != null) {
				parent.status = "Waiting...";

				var watchange = (eventNode.textContent || eventNode.text || "");
				if (watchange.indexOf("WatchChange") >= 0) {
					gOpenWatchVar = true;
				}
				bStopAtBreakpoint = true;
				reDisableToolbar();
				gNeedReEnableToolbar = true;
			} else {
				parent.status = "Done";
				if (gNeedReEnableToolbar) {
					gNeedReEnableToolbar = false;
					reEnableToolbar();
				}
			}

			// DB Trace
			eventNode = oneEventNode.getElementsByTagName("DBTSQLOperation")[0];
			if (eventNode != null) {
				stepMethod = (eventNode.textContent || eventNode.text);
			}
			
			eventNode = oneEventNode.getElementsByTagName("DBTNote")[0];
			if (eventNode != null) {
				DBTData = (eventNode.textContent || eventNode.text);
			}
			if ( (DBTData == null) ||  DBTData == "") {
				eventNode = oneEventNode.getElementsByTagName("DBTHighLevelOp")[0];
				if ((eventNode != null) && ((eventNode.textContent || eventNode.text) != "root")) {
					DBTData = (eventNode.textContent || eventNode.text);
				}
			}
			
			if (mStepStatus == "") {
				eventNode = oneEventNode.getElementsByTagName("DBTSize")[0];
				if (eventNode != null) {
					mStepStatus = (eventNode.textContent || eventNode.text);
				}
			}
			if (primaryPageName == "") {
				eventNode = oneEventNode.getElementsByTagName("DBTTableName")[0];
				if (eventNode != null) {
					primaryPageName = (eventNode.textContent || eventNode.text);
				}
			}

			// Alert
			eventNode = oneEventNode.getElementsByTagName("AlertLabel")[0];
			if (eventNode != null) {
				alertLabel = (eventNode.textContent || eventNode.text);
			}
			
			var theTable = createTableForRequestor(requestorId, remoteNodeName);

			if (gIndex >= pyMaxTraceEventsDisplayed)
				gIndex = 0;
			if (mCount > pyMaxTraceEventsDisplayed){
				var tableRowCount = theTable.getElementsByTagName("TR").length;
				if(tableRowCount > 2){
					theTable.deleteRow(tableRowCount-2);
				}
				else{
					rowsArray["traceEvent-TBODY-" + requestorId].rows.shift();
				}
			}
			
			var theTBody = theDoc.getElementById("traceEvent-TBODY-" + requestorId);

			// Interaction
			eventNode = oneEventNode.getElementsByTagName("InteractionBytes")[0];
			if (eventNode != null) {
				interactionBytes  = (eventNode.textContent || eventNode.text);
			}

			eventNode = oneEventNode.getElementsByTagName("InteractionQueryParam")[0];
			if (eventNode != null) {
				interactionQueryParam  = (eventNode.textContent || eventNode.text);
			}

			eventNode = oneEventNode.getElementsByTagName("Sequence")[0];
			if (eventNode != null) {
				sequenceNumber = (eventNode.textContent || eventNode.text);
			}

			// create a new row
			//  var theRow = theTable.insertRow(1);
			var theRow = theDoc.createElement("TR");
			
			//$(theRow).on('click', selectEventRow);
			theRow.style.cursor = "hand";
			theRow.id="eventRow";
			
			// Set row color
			if ( ( parseInt(activityNumber) % 2) == 0) {
				theRow.className = "eventTable";
			} else {
				theRow.className = "eventTableDark";
			}
			if ( activityNumber == "") {
				theRow.className = "eventTableNoActivity";
			}
			// select icon
			var theCell = theDoc.createElement("TD");
			theRow.insertBefore(theCell, null);
			setInnerText(theCell, "   ");
			theCell.className = "eventDataBold";
			theCell.style.width = "15px";
			theCell.bgColor = "white";
			theCell.id = "eventSelectIcon";
			// Sequence Number 
			theCell = theDoc.createElement("TD");
			theRow.insertBefore(theCell, null);
			setInnerText(theCell," " + sequenceNumber + " ");
			theCell.id = "eventSequenceNumber";
			theCell.style.width = "20px";
			theCell.style.display = "none";
			// line
			theCell = theDoc.createElement("TD");
			theRow.insertBefore(theCell, null);
			setInnerText(theCell," " + mCount + " ");
			theCell.setAttribute('title', "" + mCount + "");
			theCell.className = "eventDataBoldCenter";
			theCell.style.width = "75px";
			theCell.id = "eventLineNumber";
			if (bStopAtBreakpoint) {
				theCell.bgColor = "yellow";
			}
			//thread
			theCell = theDoc.createElement("TD");
			theRow.insertBefore(theCell, null);
			setInnerText(theCell," " + threadname + " ");
			theCell.setAttribute('title', "" + threadname + "");
			theCell.className = "EventDataCenter";
			theCell.style.width = "75px";
			theCell.id = "threadname";
			if (bStopAtBreakpoint) {
				theCell.bgColor = "yellow";
			}
			// interaction
			theCell = theDoc.createElement("TD");
			theRow.insertBefore(theCell, null);
			setInnerText(theCell, " " + interaction + " ");
			theCell.setAttribute('title', "" + interaction + "");
			theCell.style.width = "50px";
			theCell.className = "eventDataCenter";
			if (bStopAtBreakpoint) {
				theCell.bgColor = "yellow";
			}

			if (eventType == "Interaction") {
			    // Rule number
				theCell = theDoc.createElement("TD");
				theCell.style.width = "50px";
				theRow.insertBefore(theCell, null);				
				setInnerText(theCell, " ") ;
				theCell.className = "eventData";

				//Step method
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "200px";
              	theCell.style.width = "20%";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " ");
				theCell.className = "eventDataRight";

				//Step page
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "200px";
              	theCell.style.width = "30%";
				theRow.insertBefore(theCell, null);
				theCell.setAttribute("colSpan", "3");
				setInnerText(theCell, " ");
				theCell.className = "eventData";

				//Step
				theCell = theDoc.createElement("TD");
				theCell.style.width = "50px";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " ");
				theCell.className = "eventData";

				//Step status
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "75px";
              	theCell.style.width = "10%";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " " + interactionBytes + "(b)  ");				
				theCell.className = "eventElementData";

				//Event type
				theCell = theDoc.createElement("TD");
				theCell.style.width = "110px";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " " + eventName + " ");				
				theCell.className = "eventData";
								
				// Blank column - Elapsed column
				theCell = theDoc.createElement("TD");
				theCell.style.width = "100px";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " " + timeStamp + " ");
				theCell.className = "eventData";
				theCell.setAttribute('title', " ");

				// Blank Name Column
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "200px";
              	theCell.style.width = "20%";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " " + interactionQueryParam + " ");
				theCell.setAttribute('title', " ");
				theCell.className = "eventData";                                                       

				// column - Ruleset
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "200px";
              	theCell.style.width = "20%";
				theRow.insertBefore(theCell, null);
				theCell.className = "eventData";					
				setInnerText(theCell, " ");
				theCell.noWrap = true;
				theCell.setAttribute('title', " ");

			} else if (eventType == "Alert") {
				// Set row color
				theRow.className = "eventTableAlertTrace";
	
				// Rule number
				theCell = theDoc.createElement("TD");
				theCell.style.width = "50px";
				theRow.insertBefore(theCell, null);				
				setInnerText(theCell, " ") ;
				theCell.className = "eventData";

				//Step method
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "200px";
              	theCell.style.width = "20%";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " ");
				theCell.className = "eventDataRight";

				//Step page
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "200px";
              	theCell.style.width = "30%";
				theRow.insertBefore(theCell, null);
				theCell.setAttribute("colSpan", "3");
				setInnerText(theCell, " ");
				theCell.className = "eventData";

				//Step
				theCell = theDoc.createElement("TD");
				theCell.style.width = "50px";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " ");
				theCell.className = "eventData";

				//Step status
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "75px";
              	theCell.style.width = "10%";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " " + eventType + " ");				
				theCell.className = "eventElementData";

				//Event type
				theCell = theDoc.createElement("TD");
				theCell.style.width = "110px";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " " + eventName + " ");				
				theCell.className = "eventData";
								
				// Blank column - Elapsed column
				theCell = theDoc.createElement("TD");
				theCell.style.width = "100px";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " ");
				theCell.className = "eventData";
				theCell.setAttribute('title', " ");

				// Blank Name Column
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "200px";
              	theCell.style.width = "20%";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " " + alertLabel + " ");
				theCell.setAttribute('title', " ");
				theCell.className = "eventData";                                                       

				// column - Ruleset
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "200px";
              	theCell.style.width = "20%";
				theRow.insertBefore(theCell, null);
				theCell.className = "eventData";					
				setInnerText(theCell, " ");
				theCell.noWrap = true;
				theCell.setAttribute('title', " ");
			} else if (eventType == "ADP Load") {
				
				theRow.className = "eventTableADPLoad";
				
				if(!gEndOfAsyncTraceEventSent) {
		
					// Blank column - Rule# Column
					theCell = theDoc.createElement("TD");
					theCell.style.width = "50px";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " ");
					theCell.className = "eventData";
					theCell.setAttribute('title', " ");

					// ADP Activity - Step Method column
					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "200px";
					theCell.style.width = "20%";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " " + eventName + " ");
					theCell.className = "eventData";
					theCell.setAttribute('title', eventName);
					
					// ADP Name - Step Page Column
					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "200px";
					theCell.style.width = "30%";
					theRow.insertBefore(theCell, null);
					theCell.setAttribute("colSpan", "3");
					setInnerText(theCell, " " + adpLoadPageName + " ");
					theCell.className = "eventData";
					theCell.setAttribute('title', adpLoadPageName);
					
					// Blank column - Step# Column
					theCell = theDoc.createElement("TD");
					theCell.style.width = "50px";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " ");
					theCell.className = "eventData";
					theCell.setAttribute('title', " ");
					
					// Status - Status Column
					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "75px";
					theCell.style.width = "10%";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " " + mStepStatus + " ");
					theCell.setAttribute('title', "" + mStepStatus + "");
					theCell.className = "eventData";
					theCell.noWrap = true;
					currentStepStatus = mStepStatus.toUpperCase();
					if (currentStepStatus.indexOf("WARN") >= 0) {
						theCell.bgColor = "gold";
					} else if (currentStepStatus.indexOf("FAIL") >= 0) {
						theCell.bgColor = "tomato";
					} else if (currentStepStatus.indexOf("EXCEPTION") >= 0) {
						theCell.bgColor = "red";
					} else if (bStopAtBreakpoint)
						theCell.bgColor = "yellow";
					theCell.setAttribute('title', mStepStatus);

					// ADP Load  - Event Type Column
					theCell = theDoc.createElement("TD");
					theCell.style.width = "110px";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, ""+ " " + eventType + " ");
					if (adpQueueEvent){
						theCell.className = "eventElementDataSelect";
						theCell.id="bgTracerSessionID";
						theCell.text = adpTracerRackKey;
						$(theCell).on('click', startADPTrace);
					} else {
						theCell.className = "eventData";
					}
					theCell.setAttribute('title', eventType);
					
					// Blank column - Elapsed column
					theCell = theDoc.createElement("TD");
					theCell.style.width = "100px";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " ");
					theCell.className = "eventData";
				        theCell.setAttribute('title', " ");

					// BGTracerKey - Name Column
					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "200px";
					theCell.style.width = "20%";
					theRow.insertBefore(theCell, null);
					var displayName = buildActivityName(adpTracerKey);
					displayName = buildDataPageDisplayName(displayName);
					theCell.innerHTML = displayName + "&nbsp;";
					theCell.setAttribute('title', " " + displayName);
					theCell.className = "eventData";
					theCell.id = "elementInstanceName";
					if(eventKey.startsWith("RULE-DECLARE-PAGES") && eventKey.indexOf("[") >= 0 ) //Check if Declare Page with parameters
					{
						  //inner text format should be of the following format = RULE-DECLARE-PAGES DECLARE_<name> #<timestamp> GMT
						   theCell.text = eventKey.substring(0, eventKey.indexOf("[") - 1);
					}
					else {
	           				   theCell.text = eventKey;
					}
					$(theCell).on('click', getRecord);

					// column - Ruleset
					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "200px";
					theCell.style.width = "20%";
					theRow.insertBefore(theCell, null);
					theCell.className = "eventData";					
					setInnerText(theCell, " " + sRSName + " " + sRSVersion+ " ");
					theCell.noWrap = true;
				        theCell.setAttribute('title', sRSName + " " + sRSVersion);

				} else {
					
					
					// Blank column - Rule# Column
					theCell = theDoc.createElement("TD");
					theCell.style.width = "50px";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " ");
					theCell.className = "eventData";
					theCell.setAttribute('title', " ");

					// Blank Step Method column
					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "200px";
					theCell.style.width = "20%";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " ");
					theCell.className = "eventData";
					theCell.setAttribute('title', " ");
					
					// Blank Step Page Column
					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "200px";
					theCell.style.width = "30%";
					theRow.insertBefore(theCell, null);
					theCell.setAttribute("colSpan", "3");
					setInnerText(theCell, " ");
					theCell.className = "eventData";
					theCell.setAttribute('title', " ");
					
					// Blank Step# Column
					theCell = theDoc.createElement("TD");
					theCell.style.width = "50px";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " ");
					theCell.className = "eventData";
					theCell.setAttribute('title', " ");
					
					// Blank Status Column
					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "75px";
					theCell.style.width = "10%";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " ");
					theCell.setAttribute('title', " ");
					theCell.className = "eventData";
					
					// End of trace event
					theCell = theDoc.createElement("TD");
					theCell.style.width = "100px";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, "End of trace");
					theCell.className = "eventData";
					
					// Blank column - Elapsed column
					theCell = theDoc.createElement("TD");
					theCell.style.width = "100px";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " ");
					theCell.className = "eventData";
				    theCell.setAttribute('title', " ");

					// Blank Name Column
					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "200px";
					theCell.style.width = "20%";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " ");
					theCell.setAttribute('title', " ");
					theCell.className = "eventData";                                                       

					// column - Ruleset
					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "200px";
					theCell.style.width = "20%";
					theRow.insertBefore(theCell, null);
					theCell.className = "eventData";					
					setInnerText(theCell, " ");
					theCell.noWrap = true;
				    theCell.setAttribute('title', " ");
				}
			} else {
				// activity
				theCell = theDoc.createElement("TD");
				theCell.style.width = "50px";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " " + activityNumber + " ");
				theCell.setAttribute('title', "" + activityNumber + "");
				theCell.className = "eventDataCenter";
				if (bStopAtBreakpoint)
					theCell.bgColor = "yellow";

				// step method
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "200px";
              	theCell.style.width = "20%";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " " + stepMethod + " ");
				theCell.setAttribute('title', "" + stepMethod + "");
				theCell.className = "eventData";
				theCell.noWrap = true;
				if (bStopAtBreakpoint) {
					theCell.bgColor = "yellow";
				}

				// Show either target page or primary page for Data Transform action events
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "200px";
              	theCell.style.width = "30%";
				theRow.insertBefore(theCell, null);
				if(actionTargetPageName==""){
					setInnerText(theCell, " " + primaryPageName + " ");
					theCell.setAttribute('title', "" + primaryPageName + "");
					theCell.className = "eventData";
					theCell.noWrap = true;
					$(theCell).on('click', selectPrimaryPageName);
					if (bStopAtBreakpoint){
						theCell.bgColor = "yellow";
					}
				}else{
					// Action's target page
					setInnerText(theCell, " " + actionTargetPageName + " ");
					theCell.setAttribute('title', "" + actionTargetPageName + "");
					theCell.className = "eventData";
					theCell.noWrap = true;
					$(theCell).on('click', selectActionTargetPageName);
				}

				// Change the title and add different color if the step page has any messages in it.
				if(hasMessages) {
					theCell.setAttribute('title', "" + primaryPageName + " has Messages in it ");
					theCell.bgColor = "Orange";
				}
				if (actionSourcePageName == "") {
					theCell.setAttribute("colSpan", "3");
				} else {
					// Target and source seperator
					theCell = theDoc.createElement("TD");
					theCell.style.width = "5px";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, "<--");
					theCell.className = "eventDataCenter";
					theCell.noWrap = true;
					if (bStopAtBreakpoint) {
						theCell.bgColor = "yellow";
					}
					// Action's source page
					theCell = theDoc.createElement("TD");
					theCell.style.width = "50px";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " " + actionSourcePageName + " ");
					theCell.setAttribute('title', "" + actionSourcePageName + "");
					theCell.className = "eventData";
					theCell.noWrap = true;
					$(theCell).on('click', selectActionSourcePageName);
				}

				// step number
				theCell = theDoc.createElement("TD");
				theCell.style.width = "50px";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " " + stepNumber + " ");
				theCell.setAttribute('title', "" + stepNumber + "");
				theCell.className = "eventDataBoldCenter";
				theCell.noWrap = true;
				if (bStopAtBreakpoint) {
					theCell.bgColor = "yellow";
				}

				// step status
				theCell = theDoc.createElement("TD");
				theCell.style.maxWidth = "75px";
              	theCell.style.width = "10%";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " " + mStepStatus + " ");
				theCell.setAttribute('title', "" + mStepStatus + "");
				theCell.className = "eventData";
				theCell.noWrap = true;
				currentStepStatus = mStepStatus.toUpperCase();
				if (currentStepStatus.indexOf("WARN") >= 0) {
					theCell.bgColor = "gold";
				} else if (currentStepStatus.indexOf("FAIL") >= 0) {
					theCell.bgColor = "tomato";
				} else if (currentStepStatus.indexOf("EXCEPTION") >= 0) {
					theCell.bgColor = "red";
				} else if (bStopAtBreakpoint)
					theCell.bgColor = "yellow";

				// event name
				if (eventType == "Asynchronous Activity") {
					theRow.className = "eventTableADPLoad";
					// Event Type Column
					theCell = theDoc.createElement("TD");
					theCell.style.width = "100px";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, ""+ eventName);
					if (aaQueueEvent){
						theCell.className = "eventElementDataSelect";
						theCell.id="bgTracerSessionID";
						theCell.text = adpTracerRackKey;
						$(theCell).on('click', startADPTrace);
					} else {
						theCell.className = "eventData";
					}
					theCell.setAttribute('title', eventType);
					
				} else {
					theCell = theDoc.createElement("TD");
					theCell.style.width = "100px";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " " + eventName + " ");
					theCell.setAttribute('title', "" + eventName + "");
					theCell.className = "eventData";
					theCell.noWrap = true;
					if (eventType.toUpperCase().indexOf("EXCEPTION") >= 0)
						theCell.bgColor = "red";
					else if (bStopAtBreakpoint)
						theCell.bgColor = "yellow";
				}

				// timestamp (elapsed)
				theCell = theDoc.createElement("TD");
				theCell.style.width = "100px";
				theRow.insertBefore(theCell, null);
				setInnerText(theCell, " " + timeStamp + " ");
				theCell.setAttribute('title', "" + timeStamp + "");
				theCell.className = "eventData";

				if ( (eventType == "DB Query") || (eventType == "DB Cache") ) {

					// Set row color
					theRow.className = "eventTableDBTrace";

					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "200px";
					theCell.style.width = "40%";;
					theRow.insertBefore(theCell, null);
					theCell.setAttribute("colSpan", "2");
					setInnerText(theCell, " " + DBTData + " ");
					theCell.className = "eventElementData ";

				} else if ( (eventType == "Log Messages") || (eventType == "Debug") ) {
					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "200px";
					theCell.style.width = "40%";
					theRow.insertBefore(theCell, null);
					theCell.setAttribute("colSpan", "2");
					setInnerText(theCell, " " + eventKey + " ");
					theCell.className = "eventElementData ";
				} else { // Start Standard Event
			
					// event key
					//
					// create padding so as the activity number increases, each event will indent more
					var activityNum = parseInt(activityNumber);
					var padding = "";
					// name
					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "200px";
					theCell.style.width = "20%";
					theRow.insertBefore(theCell, null);
					var sRuleSetColumnValue = null;
					if (isInstanceHandle(eventKey) && gTracerAccessibility) {
						var sReducedKey = buildActivityName(eventKey);
						var sLineValue = null;

						if(eventKey.startsWith("RULE-DECLARE-PAGES") && eventKey.indexOf("[") >= 0 ) //Check if Declare Page with parameters
						{
						  //inner text format should be of the following format = RULE-DECLARE-PAGES DECLARE_<name> #<timestamp> GMT
						   theCell.text = eventKey.substring(0, eventKey.indexOf("[") - 1);
						}
						else {
	           				   theCell.text = eventKey;
						}
						if (sInsKey != null) {							
							if(eventKey != null && eventKey.startsWith("RULE-DECLARE-PAGES")){ sLineValue = buildActivityName(sKeyName); }
							else{ sLineValue = sKeyName; } // + " " + sRSName + " " + sRSVersion;
							sRuleSetColumnValue = sRSName + " " + sRSVersion;
						} else {
							sLineValue = sReducedKey;
						}
						sLineValue = buildDataPageDisplayName(sLineValue);
						theCell.innerHTML = padding + sLineValue + "&nbsp;";
						theCell.className = "eventElementDataSelect";
						theCell.id = "elementInstanceName";
						//theCell.text = eventKey;
						$(theCell).on('click', getRecord);
                        if(eventKey != null && eventKey.startsWith("RULE-DECLARE-PAGES"))
						    theCell.setAttribute('title', sLineValue);
						else
						    theCell.setAttribute('title', sReducedKey);						
						theCell.noWrap = true;
					} else {
						if (isInstanceWithKeys(eventKey)) {
							theCell.innerHTML= eventKey + "&nbsp;";
							theCell.className = "eventElementDataSelect";
							theCell.id = "elementInstanceName";
							theCell.text = eventKey;
							$(theCell).on('click', getRecordWithKeys);
							theCell.noWrap = true;
                          
						} else {
							if (isInstanceHandle(eventKey))
								theCell.innerHTML= padding + buildActivityName(eventKey) + "&nbsp;";
							else
								theCell.innerHTML= padding + eventKey + "&nbsp;";
							theCell.className = "eventData";
						}
                        theCell.setAttribute('title', eventKey);
                      
					}

					if (sRuleSetColumnValue == null) {
						sRuleSetColumnValue = "";
					}

					theCell = theDoc.createElement("TD");
					theCell.style.maxWidth = "200px";
					theCell.style.width = "20%";
					theRow.insertBefore(theCell, null);
					setInnerText(theCell, " " + sRuleSetColumnValue + " ");
					theCell.setAttribute('title', "" + sRuleSetColumnValue + "");
					theCell.className = "eventData";
					theCell.noWrap = true;
					if (bStopAtBreakpoint) {
						theCell.bgColor = "yellow";
					}
				}
			}
			
			if(gEndOfAsyncTraceEventSent) {
				writeEventInfo("Finished Tracing.");
			}

			// Save the activity for using it late in displaying indentation in the case of Model
			if ((eventType != "Model Begin") && (eventType != "Model End")) {
				gPrevActivityNum = activityNum;
			}

			if (pega.util.Dom.getElementsByAttribute("Value", "*", "Connected", oneEventNode).length > 0) {
				return;
			} else {
				if (pega.util.Dom.getElementsByAttribute("Value", "*", "FromZeusTrace", oneEventNode).length > 0) {
					return;
				}
			}
			
			if(rowsArray["traceEvent-TBODY-" + requestorId] == null) {
				rowsArray["traceEvent-TBODY-" + requestorId] = {};
				rowsArray["traceEvent-TBODY-" + requestorId].element = theTBody;
				rowsArray["traceEvent-TBODY-" + requestorId].rows = [];
			}
			rowsArray["traceEvent-TBODY-" + requestorId].rows.push(theRow);
		}
		renderRows(rowsArray);
      
      	if (bStopAtBreakpoint) {
			writeEventMessage("Stopped at breakpoint. Click Step or Continue icon for resuming the process.");
			if (gOpenWatchVar)
				displayWatchVarInfo(parent);
			else
				displayBreakInfo();
			parent.focus();
			return;
		} else {
			// Clear WatchVar display
			parent.MenuRow.clearIt();
		}
	} else {
		TRACE_EVENT_LOCATION = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=autocontinue&pzCommandSession=" + debugConnectionID + "&pzDebugConnection=" + connectionID + "&pzXmlOnly=true" +"&pzNodeID=" + nodeID + "&pySessionType=" + pySessionType;
	}
}

function renderRows(rowsArray){
	for(var elemId in rowsArray) {
		var element = rowsArray[elemId].element;
		$(element).prepend(rowsArray[elemId].rows.reverse());
	}
}

function isInstanceHandle(eventKey)
{
	var bRet = false;
	var nPos = -1;
	nPos = eventKey.indexOf(" GMT")
	if (nPos >= 0)
		bRet = true;

	return bRet;
}

function isInstanceWithKeys(eventKey)
{
	var bRet = false;
	var nPos = -1;
	var aRule = "";

	aRule = eventKey.substring(0,5);
	aRule = aRule.toLowerCase();
	if (aRule.indexOf("rule-") >= 0)
	{
		nPos = eventKey.indexOf(".")
		if (nPos >= 0)
			bRet = true;
	}
	return bRet;
}

function isItAbsolutelyIgnored(eventKey)
{
	var bRet = false;
	var nPos = -1;

	//eventKey = eentKey.toUpperCase();
	nPos = eventKey.indexOf("TRACERRULESETOPTIONSSAVE");
	if (nPos >= 0)
	{
		bRet = true;
	}

	return bRet;
}

function isItIgnoredThisActivity(eventKey)
{
	var bRet = false;
	var nPos = -1;
	nPos = eventKey.indexOf("@BASECLASS WBOPEN");
	if (nPos >= 0)
	{
		bRet = true;
		return bRet;
	}
	nPos = eventKey.indexOf("CODE- SHOWSTREAM");
	if (nPos >= 0)
	{
		bRet = true;
		return bRet;
	}
	nPos = eventKey.indexOf("@BASECLASS OPENBYHANDLE");
	if (nPos >= 0)
	{
		bRet = true;
		return bRet;
	}
	nPos = eventKey.indexOf("RULE-OBJ-ACTIVITY PYDEFAULT");
	if (nPos >= 0)
	{
		bRet = true;
		return bRet;
	}
	nPos = eventKey.indexOf("RULE- RULERESOLUTIONAVAILABLE");
	if (nPos >= 0)
	{
		bRet = true;
		return bRet;
	}
	nPos = eventKey.indexOf("@BASECLASS STEPSTATUSFAIL");
	if (nPos >= 0)
	{
		bRet = true;
		return bRet;
	}
	nPos = eventKey.indexOf("RULE-OBJ-WHEN PYDEFAULT");
	if (nPos >= 0)
	{
		bRet = true;
		return bRet;
	}

	nPos = eventKey.indexOf("RULE-RULESET-NAME USECHECKOUTTRUE");
	if (nPos >= 0)
	{
		bRet = true;
		return bRet;
	}
	nPos = eventKey.indexOf("RULE-RULESET-NAME PYDEFAULT");
	if (nPos >= 0)
	{
		bRet = true;
		return bRet;
	}

	nPos = eventKey.indexOf("DATA-TRACERSETTINGS TRACERRULESETOPTIONSSAVE");
	if (nPos >= 0)
	{
		bRet = true;
	}

	return bRet;
}

function buildActivityName(eventKey)
{
	var strRet = "";
	var nPos = -1;

	nPos = eventKey.indexOf(" ");
	if (nPos >= 0)
	{
		strRet = eventKey.substring(nPos + 1, eventKey.length);
	}

	return strRet;
}

function buildJavaClassName(eventKey)
{
	var strRet = "";
	var nPos = -1;

	// drop the obj claas prefix and tailing GMT
	nPos = eventKey.indexOf(" ");
	if (nPos >= 0)
	{
		var strSub = "";
		var strTemp = eventKey.substring(nPos+1, eventKey.length-4);
		strTemp = strTemp.toLowerCase();
		nPos = strTemp.indexOf("#");
		if (nPos >= 0)
		{
			strSub = strTemp.substring(nPos+1,strTemp.length);
			strTemp = strTemp.substring(0,nPos);
			strTemp = zUtil_reformatString(strTemp,"-","_");
			strTemp = zUtil_reformatString(strTemp," ","_");
			strSub = zUtil_reformatString(strSub,"t","");
			strSub = zUtil_reformatString(strSub,".","");
			strRet = strTemp + strSub;
		}
	}

	return strRet;
}

function checkEnventKey(eventKey)
{
	var bRet = false;

	var nPos = eventKey.indexOf("_");
	if (nPos > 0)
		bRet = true;

	return bRet;
}

function deleteTraceEvents(enabled)
{
	if (checkIfStopMode())
		return; // do nothing if Tracer is in STOP mode!.
	if (!enabled)
		return;
	if (gFirstTime)
		return;

	mCount = 0;
	gIndex = 0;
	parent.TraceEvent.document.getElementById("traceEvent-CONTAINER").outerHTML = gTraceEventTableHTML
	// send a request to server to reset activity counter.
	var strRequest = "";
	strRequest  = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=settings&pzSetCmd=ResetCounter" + "&pzDebugConnection=" + connectionID +"&pzNodeID=" + nodeID + "&pzTraceEventNodeID=" + remoteNodeId + "&pySessionType=" + pySessionType;
	nNextCommandFunction = FUNC2_NOTATALL;
	xmlCommand.onreadystatechange=checkIfCommandFunctionReady;
	try
	{
		var loadResult = xmlCommand.load(strRequest);

	}
	catch (exception)
	{
		alert ("PegaRULESTracer - deleteTraceEvents(): Exception!!!");
		return;
	}
}

function stopTraceEvent()
{
	writeEventInfo("Tracing stopped.");
	gReadyState = true;
	reDisableToolbar();
	enablePause();
}


function resumeTraceEvent()
{
	//mCount = 0;
	//mCall = 0;
	connectionID = connectionID_bak;
	bAlreadyGetTraceOptionCookies = false;
	//writeEventMessage("");  // clear event message.
	startTraceEvent("connect&pzCommandSession=");
}

function getRecord(event)
{
         if(event == null)
		event = parent.TraceEvent.window.event;
	if (gTotallyStop)
	{
		alert("Please restart Tracer");
		return;
	}
	if (bStopAtBreakpoint)
	{
		//alert("Cannot open a record when Tracer is under Break mode");
		return;
	}
	gClickFromSpecialCell = true;
	var className = "";
	var insKey = "";
	var theElement = event.srcElement || event.target;
	var theCell;

	theCell = theElement.parentElement;

	if (theCell != null)
	{
		var instanceElement = pega.util.Dom.getElementsById("elementInstanceName", theCell)[0];;
		var insKey = zUtil_reformatString(instanceElement.text, "&nbsp;", "");
		gIgnoreActvityFlag = true;
		openRecord(insKey);
	}
}

function openRecord(insKey)
{
	//alert("openRecord(className = " + className + " insKey = " + insKey + ")");
	// if portal version is greater than 0, its indicates the use of Designer desktop environment

	if (portalVersion > 0)
	{
		openRule(insKey,"true");
	}
	else
	{
		var ruleFormWindowName = zUtil_computeWindowName(insKey);
		var myUrl = strRequestorURIevents + "?pyActivity=WBOpen&InsHandle=" + escape(insKey);
		window.open(myUrl,ruleFormWindowName,'width=800,height=650,resizable,scrollbars');
	}
}

function getRecordWithKeys(event)
{
         if(event == null)
		event = parent.TraceEvent.window.event;
	if (gTotallyStop)
	{
		alert("Please restart Tracer");
		return;
	}
	if (bStopAtBreakpoint)
	{
		//alert("Cannot open a record when Tracer is under Break mode");
		return;
	}
	gClickFromSpecialCell = true;
	var className = "";
	var insKey = "";
	var theElement = event.srcElement || event.target;
	var theCell;

	theCell = theElement.parentElement;

	if (theCell != null)
	{
		var instanceElement = theCell.getElementById("elementInstanceName");
		var insKey = zUtil_reformatString(instanceElement.text, "&nbsp;", "");

		if (portalVersion > 0)
		{
			var keyPart = "";
			var objClass = "";
			var idx = insKey.indexOf(' ');
			objClass = insKey.substring(0, idx);
			keyParts = insKey.substring(idx+1).replace('.', '!');
			openRuleByClassAndName(keyParts,objClass);
		}
		else
		{
			gIgnoreActvityFlag = true;
			var ruleFormWindowName = zUtil_computeWindowName(insKey);
			var myUrl = strRequestorURIevents + "?pyActivity=OpenRecord&InsKeys=" + escape(insKey);
			window.open(myUrl,ruleFormWindowName,'width=800,height=650,resizable,scrollbars');
		}
	}
}

function displayTraceEvent(sequenceNumber, lineNumber) {
	if (!gViewJavaCodeAvailable)
	return;

	if(traceEventArray[sequenceNumber] == null){
		var strUrl = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=getEvent&pzCommandSession=" + debugConnectionID + "&pzDebugConnection=" + (connectionID == "" ? connectionID_bak : connectionID) + "&pzEvent=" + sequenceNumber +"&pzNodeID=" + nodeID + "&pzTraceEventNodeID=" + remoteNodeId + "&pySessionType=" + pySessionType ;		
		$.ajax({
			url: strUrl,
			cache:false,
			dataType: "xml"			
		}).done(function(xml){traceEventArray[sequenceNumber] = XMLToString(xml);}).done(function(xml){displayTraceEventsCallback(sequenceNumber, lineNumber);});
	}else{
		displayTraceEventsCallback(sequenceNumber, lineNumber);
	}	
}

function displayTraceEventsCallback(sequenceNumber, lineNumber){
	var nTop = 0;
	var nLeft = 0;
	var nHeight = 60;
	var nWidth = 70;
	var nWndTop = window.screen.height * nTop / 100;
	var nWndLeft = window.screen.width * nLeft / 100;
	var nWndHeight = window.screen.height * nHeight / 100;
	var nWndWidth = window.screen.width * nWidth / 100;
	var strFeatures = "left="+nWndLeft+",top="+nWndTop+",height="+nWndHeight+",width="+nWndWidth;
	var strURL = "";
	var strForm = "";
	var objDisplayEventTraceWnd = window.open(strURL,strForm,"status=no,toolbar=no,menubar=no,location=no,scrollbars=yes,resizable=yes " + strFeatures);
  	aPropertyPanelWnds.push(objDisplayEventTraceWnd);
	objDisplayEventTraceWnd.focus();

	if (objDisplayEventTraceWnd.opener == null)
		//objDisplayEventTraceWnd.parent = self;
		objDisplayEventTraceWnd.opener =  self;
		
	// parse the TraceEvent
	var traceEventInfo;
	traceEventInfo = traceEventArray[sequenceNumber];

	var nPos;
	var nEndPos;
	var aName;
	var aValue;
	var aEventType;
	var pagePropertyName;

	var TraceEventSeq;
	var strData = "";
	var elemList = null;
	
         var aStepMethod="";

	var xmlDom = new XMLDomControl();
	if (xmlDom == null)
	{
		alert("displayTraceEvent(" + sequenceNumber + "): Cannot allocate memory for xmlDom");
		return;
	}
	
	var bLoaded = xmlDom.loadXML(traceEventInfo);
	
	if (!bLoaded)
	{
		alert("displayTraceEvent(" + sequenceNumber + "): Cannot load trace information into xmlDom");
		return;
	}

	var strTitle = "Properties on Page TraceEvent [" + lineNumber + "]";
	var strDialog1 = strDialogHtmlTemplate;
	strDialog1 = strDialog1.replace(/#DIALOGTITLE/gi, strTitle );
	strDialog1 = strDialog1.replace(/#ICONDIALOGCLASS/gi, "iconDialogProperties");
	strDialog1 = strDialog1.replace(/#FORMNAME/gi, "form2");
	strDialog1 = strDialog1.replace(/#FORMID/gi, "form2");
	var strHeader = "<TABLE border=\"0\" cellpadding=\"2\" cellspacing=\"0\" width=\"100%\">" +
		"<TR>" +
		"<TD NOWRAP=\"NOWRAP\" class=\"dialogHeaderLabel\">Properties on Page TraceEvent [" + lineNumber + "] </TD>" +
		"</TR>" +
		"</TABLE>";
	strDialog1 = strDialog1.replace(/#HEADERDATA/gi, strHeader);

	strData = "<TABLE WIDTH='100%' cellspacing='0' border='0'>";

	// Event Header
	strData += "<TR CLASS='eventElementTitleBarStyle'><TD VALIGN='TOP' COLSPAN=2>&nbsp;Header&nbsp;</TD></TR>";
	elemList = xmlDom.getElementsByTagName("Sequence");
	if (elemList.length > 0)
	{
		aName = "Sequence";
		aValue = (elemList[0].textContent || elemList[0].text);
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
	elemList = xmlDom.getElementsByTagName("Interaction");
	if (elemList.length > 0)
	{
		aName = "Interaction";
		aValue = (elemList[0].textContent || elemList[0].text);
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
	elemList = xmlDom.getElementsByTagName("DateTime");
	if (elemList.length > 0)
	{
		aName = "Timestamp";
		aValue = (elemList[0].textContent || elemList[0].text);
		// convert to readable time
		aValue = zUtil_convertZeusDTGToString(aValue, gDateFormat) + "&nbsp;&nbsp;(" + aValue + ")";
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
	elemList = xmlDom.getElementsByTagName("Elapsed");
	if (elemList.length > 0)
	{
		aName = "Elapsed Time";
		aValue = (elemList[0].textContent || elemList[0].text);
		if (aValue.length == 0) {
			aValue = "0.000"; 
		} else {
			var num = new Number(aValue)/1000;
			aValue = num.toFixed(4);
		}
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;(s)&nbsp;</TD></TR>";
	}
	elemList = xmlDom.getElementsByTagName("EventType");
	if (elemList.length > 0)
	{
		aName = "Event Type";
		aValue = (elemList[0].textContent || elemList[0].text);
		aEventType = aValue;
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
	elemList = xmlDom.getElementsByTagName("EventName");
	if (elemList.length > 0)
	{
		aName = "Event Name";
		aValue = (elemList[0].textContent || elemList[0].text);
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
	elemList = xmlDom.getElementsByTagName("EventKey");
	if (elemList.length > 0)
	{
		aName = "Event Key";
		aValue = (elemList[0].textContent || elemList[0].text);
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
	elemList = xmlDom.getElementsByTagName("ThreadName");
	if (elemList.length > 0)
	{
		aName = "Thread Name";
		aValue = (elemList[0].textContent || elemList[0].text);
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
         elemList = xmlDom.getElementsByTagName("RequestorID");
	if (elemList.length > 0)
	{
		aName = "Requestor ID";
		aValue = (elemList[0].textContent || elemList[0].text);
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
        elemList = xmlDom.getElementsByTagName("CorrelationID");
	if (elemList.length > 0)
	{
		aName = "Correlation ID";
		aValue = (elemList[0].textContent || elemList[0].text);
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
    elemList = xmlDom.getElementsByTagName("NodeID");
	if (elemList.length > 0)
	{
		aName = "Node ID";
		aValue = (elemList[0].textContent || elemList[0].text);
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
	elemList = xmlDom.getElementsByTagName("WorkPool");
	if (elemList.length > 0)
	{
		aName = "Work Pool";
		aValue = (elemList[0].textContent || elemList[0].text);
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
	elemList = xmlDom.getElementsByTagName("ActivePALStat");
	if (elemList.length > 0)
	{
		aName = "Active PAL Stat";
		aValue = (elemList[0].textContent || elemList[0].text);
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
	elemList = xmlDom.getElementsByTagName("LastStep");
	if (elemList.length > 0)
	{
		aName = "Last Step";
		aValue = (elemList[0].textContent || elemList[0].text);
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}
	elemList = xmlDom.getElementsByTagName("FirstInput");
	if (elemList.length > 0)
	{
		aName = "Input";
		aValue = (elemList[0].textContent || elemList[0].text);
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}

	elemList = xmlDom.getElementsByTagName("TraceEvent");
	if (elemList.length > 0) {
		aName = "Ruleset Name";
		aValue = elemList.item(0).getAttribute("rsname");
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}

	elemList = xmlDom.getElementsByTagName("TraceEvent");
	if (elemList.length > 0) {
		aName = "Ruleset Version";
		aValue = elemList.item(0).getAttribute("rsvers");
		strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
	}

	// Interaction Event
	if (aEventType == "Interaction") {
		strData += "<TR CLASS='eventElementTitleBarStyle'><TD VALIGN='TOP' COLSPAN=2>&nbsp;Interaction&nbsp;</TD></TR>";
	
		elemList = xmlDom.getElementsByTagName("InteractionBytes");
		if (elemList.length > 0)
		{
			aName = "Bytes";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("InteractionQueryParam");
		if (elemList.length > 0)
		{
			aName = "Query Param";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("InteractionQueryData");
		if (elemList.length > 0)
		{
			aName = "Query Data";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += buildTokenTable(aName, aValue, "&", "=", 1, "eventElementData");
		}
		elemList = xmlDom.getElementsByTagName("InteractionPAL");
		if (elemList.length > 0)
		{
			aName = "PAL";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += buildTokenTable(aName, aValue, ";", "=", 0, "eventDataRight");
		}

	}
	// Alert Event
	else if (aEventType == "Alert") {
		strData += "<TR CLASS='eventElementTitleBarStyle'><TD VALIGN='TOP' COLSPAN=2>&nbsp;Alert&nbsp;</TD></TR>";
	
		elemList = xmlDom.getElementsByTagName("EventName");
		if (elemList.length > 0)
		{
			aName = "Message ID";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("AlertLabel");
		if (elemList.length > 0)
		{
			aName = "Label";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("AlertType");
		if (elemList.length > 0)
		{
			aName = "Type";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("AlertTimestamp");
		if (elemList.length > 0)
		{
			aName = "Timestamp";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("AlertKPIThreshold");
		if (elemList.length > 0)
		{
			aName = "KPI Threshold";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("AlertKPIValue");
		if (elemList.length > 0)
		{
			aName = "KPI Value";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("AlertUniqueInt");
		if (elemList.length > 0)
		{
			aName = "Unique Integer";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("AlertLine");
		if (elemList.length > 0)
		{
			aName = "Data";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
	}

	// DB Trace Event
	else if ( (aEventType == "DB Query") || (aEventType == "DB Cache") ) {
		strData += "<TR CLASS='eventElementTitleBarStyle'><TD VALIGN='TOP' COLSPAN=2>&nbsp;DB Trace&nbsp;</TD></TR>";
	
		elemList = xmlDom.getElementsByTagName("DBTDatabaseName");
		if (elemList.length > 0)
		{
			aName = "Database Name";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("DBTSize");
		if (elemList.length > 0)
		{
			aName = "Size";
			aValue = (elemList[0].textContent || elemList[0].text);
			if (aValue != "-1") {
				strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
			}
		}
		elemList = xmlDom.getElementsByTagName("DBTTableName");
		if (elemList.length > 0)
		{
			aName = "Table Name";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("DBTSQLOperation");
		if (elemList.length > 0)
		{
			aName = "SQL Operation";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("DBTSQL");
		if (elemList.length > 0)
		{
			aName = "SQL";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
			elemList = xmlDom.getElementsByTagName("DBTSQLInserts");
			if (elemList.length > 0)
			{
				aName = "SQL Inserts";
				aValue = (elemList[0].textContent || elemList[0].text);
				strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'><xmp>" + aValue + "</xmp></TD></TR>";
			}
		}
		elemList = xmlDom.getElementsByTagName("DBTCacheType");
		if (elemList.length > 0)
		{
			aName = "Cache Type";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("DBTConnectionID");
		if (elemList.length > 0)
		{
			aName = "Connection ID";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("DBTHighLevelOpID");
		if (elemList.length > 0)
		{
			aName = "High Level Op ID";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("DBTHighLevelOp");
		if (elemList.length > 0)
		{
			aName = "High Level Op";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("DBTLabel");
		if (elemList.length > 0)
		{
			aName = "Label";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("DBTNote");
		if (elemList.length > 0)
		{
			aName = "Note";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("DBTObjectClass");
		if (elemList.length > 0)
		{
			aName = "Object Class";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
	} else if(aEventType == "SOAP Messages"){
		elemList = xmlDom.getElementsByTagName("SOAPMessage");
		if (elemList.length > 0) {
			aName = "Soap&nbsp;Message";
			aValue = (elemList[0].textContent || elemList[0].text);
			aValue = aValue
				.replace(/&/g, "&amp;")
				.replace(/</g, "&lt;")
				.replace(/>/g, "&gt;")
				.replace(/"/g, "&quot;")
				.replace(/'/g, "&#039;");
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
	} else {
		// Standard Trace Event
		strData += "<TR CLASS='eventElementTitleBarStyle'><TD VALIGN='TOP' COLSPAN=2>&nbsp;Standard&nbsp;</TD></TR>";

		elemList = xmlDom.getElementsByTagName("ActivityName");
		if (elemList.length > 0)
		{
			aName = "Activity Name";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("ActivityNumber");
		if (elemList.length > 0)
		{
			aName = "Activity Number";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("ParameterPageName");
		if (elemList.length > 0)
		{
			aName = "Parameter Page Name";
			aValue = (elemList[0].textContent || elemList[0].text);
			pagePropertyName = "ParameterPageContent";
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" +
				aName + "&nbsp;</TD><TD CLASS='eventElementDataSelect' " +
				"onclick=\"opener.displayPropertyPage(" + sequenceNumber + ",'" + aValue + "','" + pagePropertyName + "');\">&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("PrimaryPageClass");
		if (elemList.length > 0)
		{
			aName = "Primary Page Class";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("PrimaryPageName");
		if (elemList.length > 0)
		{
			aName = "Primary Page Name";
			aValue = (elemList[0].textContent || elemList[0].text);
			if (aValue.substring(0,1) != "\"")
			{
				if (aValue.length > 0)
				{
					pagePropertyName = "PrimaryPageContent";
					strData += "<TR CLASS='eventTable'><TD CLASS='eventElementDataBold'>&nbsp;" +
						aName + "&nbsp;</TD><TD CLASS='eventElementDataSelect' " +
						"onclick=\"opener.displayPropertyPage(" + sequenceNumber + ",'" + aValue + "','" + pagePropertyName + "');\">&nbsp;" + aValue + "&nbsp;</TD></TR>";
				}
				else
				{
					strData += "<TR CLASS='eventTable'><TD CLASS='eventElementDataBold'>&nbsp;" +
						aName + "&nbsp;</TD><TD CLASS='eventElementDataSelect'>&nbsp;&nbsp;</TD></TR>";
				}
			}
		}

		elemList = xmlDom.getElementsByTagName("LocalVariables");
		if (elemList.length > 0) {
			strData += "<TR CLASS='eventTable'><TD CLASS='eventElementDataBold'>&nbsp;Local Variables&nbsp;</TD><TD CLASS='eventElementDataSelect' " +
						"onclick=\"opener.displayPropertyPage(" + sequenceNumber + ",'LocalVariables','LocalVariables');\">&nbsp;View Variables&nbsp;</TD></TR>";
		}

		elemList = xmlDom.getElementsByTagName("NamedPages");
		if (elemList.length > 0) {
			strData += "<TR CLASS='eventTable'><TD CLASS='eventElementDataBold'>&nbsp;Additional Named Pages&nbsp;</TD><TD CLASS='eventElementDataSelect'><table>";
			var nodePages = elemList.item(0);

			var pageNode = nodePages.firstChild;
			for (i = 0; i < nodePages.childNodes.length; i++) {
				var pagePropertyName = "NamedPages/" + pageNode.nodeName;
				strData += "<tr><td onclick=\"opener.displayPropertyPage(" + sequenceNumber + ",'" + pageNode.nodeName + "','" + pagePropertyName + "');\">&nbsp;<a style=\"color: blue;\">" + pageNode.nodeName + "</a>&nbsp;</TD></tr>";
				pageNode = pageNode.nextSibling;
			}

			strData += "</table></TD></TR>";
		}

		// New for ver 03-04-01
		elemList = xmlDom.getElementsByTagName("AccessDenialReason");
		if (elemList.length > 0) {
		  aName = "Access Denial Reason";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}

		//New for ver 03-04-01
		elemList = xmlDom.getElementsByTagName("AccessSnapshotPageName");
		if (elemList.length > 0) {
			elemList = xmlDom.getElementsByTagName("AccessSnapshotPageContent");
			if (elemList.length>0) {
				gAccessDataPage = "<?xml version=\"1.0\" ?>\n\n" + elemList.item(0).xml;
			}
			aName = "Access Snapshot Page";
			aValue = "=unnamed=";
			strData += "<TR CLASS='eventTable'><TD CLASS='eventElementDataBold'>&nbsp;" +
					aName + "&nbsp;</TD><TD CLASS='eventElementDataSelect' " +
					"onclick=\"opener.displayUnformattedXML(opener.gAccessDataPage);\">&nbsp;" + aValue + "&nbsp;</TD></TR>";

		}
		elemList = xmlDom.getElementsByTagName("StepMethod");
		if (elemList.length > 0)
		{
			aName = "Step Method";
			aValue = (elemList[0].textContent || elemList[0].text);

                           aStepMethod = aValue;

			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("StepNumber");
		if (elemList.length > 0)
		{
			aName = "Step Number";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("StepStatus");
		if (elemList.length > 0)
		{
			aName = "Step Status";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("WhenStatus");
		if (elemList.length > 0)
		{
			aName = "When Status";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("mStepStatus");
		if (elemList.length > 0)
		{
			var colorChange = "";
			aName = "Step Status";
			aValue =(elemList[0].textContent || elemList[0].text);
			var statusValue = aValue.toUpperCase();
			if (statusValue.indexOf("WARN") >= 0 ) {
				colorChange = " STYLE='background-color : gold' ";
			}
			else if (statusValue.indexOf("FAIL") >=0 ) {
				colorChange = " STYLE='background-color : tomato' ";
			}
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData' " + colorChange + ">&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("mStepStatusInfo");
		if (elemList.length > 0)
		{
			aName = "Step Status Info";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData' " + colorChange + "><PRE>&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("OptionalProperties");
		if (elemList.length > 0)
		{

			aName = "Optional Properties";
			elemList = xmlDom.getElementsByTagName("OptionalPropertiesDescription");
			if (elemList.length > 0) {
                             
				aValue = (elemList[0].textContent || elemList[0].text);
			} else {
				aValue = "=unnamed=";
			}
			strData += "<TR CLASS='eventTable'><TD CLASS='eventElementDataBold'>&nbsp;" +
				aName + "&nbsp;</TD><TD CLASS='eventElementDataSelect' " +
				"onclick=\"opener.displayCustomPage(" + sequenceNumber + ",'" + aValue + "','" + aName + "');\">&nbsp;" + aValue + "&nbsp;</TD></TR>";
		}
		elemList = xmlDom.getElementsByTagName("ExceptionTrace");
		if (elemList.length > 0)
		{
			aName = "Exception Trace";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD CLASS='eventElementData'>&nbsp;" + aValue + "&nbsp;</TD></TR>";
			//strData += buildHyperLinks(aName, aValue, 2);
		}
		elemList = xmlDom.getElementsByTagName("JavaStackTrace");
		if (elemList.length > 0)
		{
			aName = "Java Stack Trace";
			aValue = (elemList[0].textContent || elemList[0].text);
			strData += buildHyperLinks(aName, aValue, 1);
		}
	}
	strData += "<TR><TD>";
	strData += "</TD></TR></TABLE>";
	strData += "<TD></TR></TABLE>";
	strData += "</td></tr></table>";

	strDialog1 = strDialog1.replace(/#DIALOGBODY/gi, strData);
	strDialog1 = strDialog1.replace(/#DIALOGMESSAGES/gi, "");

	var strButtons = "<table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">";
	strButtons += "<tr><td nowrap=\"nowrap\" align=\"center\">";
	strButtons += "&nbsp;<button onclick='self.close();' id='button1' name='button1' title='Click here to exit this window' class='littleButton'>";
	strButtons += "<span class='buttonLeft'></span>";
	strButtons += "<span class='buttonMiddle'>";
	strButtons += "<span class='buttonText' onmouseout='this.className=\"buttonText\"' ";
	strButtons += " onmouseover='this.className=\"buttonTextHover\"'>";
	strButtons += "Close</span></span><span class='buttonRight'></span>";
	strButtons += "</TD></TR></TABLE>";

	strDialog1 = strDialog1.replace(/#BUTTONDATA/gi, strButtons);

	objDisplayEventTraceWnd.document.open();
	objDisplayEventTraceWnd.document.write(strDialog1);
	objDisplayEventTraceWnd.document.close();
}

function getSource(nIndex, nSource)
{
	if (gTotallyStop) {

		alert("Please restart Tracer");
		return;
	}
	var atElement = "";
	atElement = atArray[nIndex];


	if (nSource == 1) {
		atElement = atArray[nIndex];
	} else {
		atElement = atArray2[nIndex];




	}
	
	//This regular expression gets any number of characters, numbers and periods preceded by a space. We then strip off everything after and including the last period.
	var re = /\s[0-9a-z\._]+/
	var fileNameArr = re.exec(atElement);
	fileName = fileNameArr[0].substring(1, fileNameArr[0].lastIndexOf("."))
	
	var objWnd = window.open("","sourceCode", "width=700,height=600,status=yes,toolbar=no,menubar=no,location=no,scrollbars=yes,resizable=yes");
	if (objWnd.opener == null) {
		objWnd.opener = self;
	}
    aPropertyPanelWnds.push(objWnd);
	//myCodeURL = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=GetSource&className="+fileName;
	var myCodeURL = "?pyActivity=Code-Pega-.pyExtractCode&className="+fileName;
	objWnd.location = myCodeURL;
}

function callActivity(url) {
	//call activity
	var xmlHttpObj = new ActiveXObject("Microsoft.XMLHTTP");
	xmlHttpObj.Open("GET", url, false);
	xmlHttpObj.send();

	return xmlHttpObj.responseText;
}

function displayUnformattedXML(data) {
	/* Hack Alert:
	window.open() seems to require that you open your new window via URL if you want XML to be
	displayed appropriately (as XML vs. as HTML (i.e. no mark-up)).
	If there's a better way to do this feel free to change it.
	*/
	var xmlHttpObj = new ActiveXObject("Microsoft.XMLHTTP");
	xmlHttpObj.Open("POST", gsServerReqURI + "?pyActivity=Data-TRACERSettings.SetAccessPage", false);


	xmlHttpObj.setRequestHeader("Content-Type", "application/x-www-form-urlencoded"); //Bug-13547

	xmlHttpObj.send("doc=" + encodeURIComponent(data));

	var theUrl = gsServerReqURI + "?pyActivity=Data-TRACERSettings.DisplayUnformattedXML";
	window.open(theUrl, "ACCESSDATA");
}

function displayXML(anXml)
{
	var nTop = 0;
	var nLeft = 0;
	var nHeight = 60;
	var nWidth = 70;
	var nWndTop = window.screen.height * nTop / 100;
	var nWndLeft = window.screen.width * nLeft / 100;
	var nWndHeight = window.screen.height * nHeight / 100;
	var nWndWidth = window.screen.width * nWidth / 100;
	var strFeatures = "left="+nWndLeft+",top="+nWndTop+",height="+nWndHeight+",width="+nWndWidth;
	var strURL = "";
	var strForm = "";
	var strHTML = "";

	var objWnd = window.open(strURL,strForm,"status=yes,toolbar=no,menubar=no,location=no,scrollbars=yes,resizable=yes " + strFeatures);
	if (objWnd != null)
	{
		objWnd.document.open();
		objWnd.document.write(anXml);
		objWnd.document.close();
		objWnd.focus();
	}
}

//New for Ver 03-02-01
function displayCustomPage(aCount,pageName,customPropertyName)
{
	if(traceEventArray[aCount] == null){
		var strUrl = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=getEvent&pzCommandSession=" + debugConnectionID + "&pzDebugConnection=" + (connectionID == "" ? connectionID_bak : connectionID) + "&pzEvent=" + aCount +"&pzNodeID=" + nodeID + "&pySessionType=" + pySessionType + "&pzTraceEventNodeID=" + remoteNodeId;		
		$.ajax({
			url: strUrl,
			cache:false,
			dataType: "xml"			
		}).done(function(xml){traceEventArray[aCount] = XMLToString(xml);}).done(function(xml){displayCustomPageCallback(aCount, pageName, customPropertyName);});
	}else{
		displayCustomPageCallback(aCount, pageName, customPropertyName);
	}	
}

function displayCustomPageCallback(aCount, pageName, customPropertyName){
	var traceEventInfo;
	var nPos, nPos1;

	traceEventInfo = traceEventArray[aCount];
	
	var xmlDom = new XMLDomControl();
	if (xmlDom == null)
	{
		alert("displayCustomPage(" + aCount + "): Cannot allocate memory for xmlDom");
		return;
	}
	xmlDom.async = false;
	var bLoaded = xmlDom.loadXML(traceEventInfo);
	if (!bLoaded)
	{
		alert("displayCustomPage(" + aCount + "): Cannot load trace information into xmlDom");
		return;
	}

	displayPage(xmlDom.xml,pageName,"OptionalProperties");
}

function displayPropertyPage(aCount,pageName,pagePropertyName){
	if(traceEventArray[aCount] == null){
		var strUrl = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=getEvent&pzCommandSession=" + debugConnectionID + "&pzDebugConnection=" + (connectionID == "" ? connectionID_bak : connectionID) + "&pzEvent=" + aCount +"&pzNodeID=" + nodeID + "&pzTraceEventNodeID=" + remoteNodeId;		
		$.ajax({
			url: strUrl,
			cache:false,
			dataType: "xml"			
		}).done(function(xml){traceEventArray[aCount] = XMLToString(xml);}).done(function(xml){displayPropertyPageCallback(aCount,pageName,pagePropertyName);});
	}else{
		displayPropertyPageCallback(aCount,pageName,pagePropertyName);
	}	
}

function displayPropertyPageCallback(aCount,pageName,pagePropertyName){
	var traceEventInfo;
	var nPos, nPos1;

	traceEventInfo = traceEventArray[aCount];
		
	//check and rearrange xml for PZ__ERROR
	nPos = traceEventInfo.indexOf("<PZ__ERROR DATAFLD=", 0);
	if (nPos > 0)
	{
		var nMsgIndex = 0;
		nPos1 = traceEventInfo.indexOf("</PZ__ERROR>", nPos);
		while (nPos1 > 0) {
			nMsgIndex++;

			var dataString = traceEventInfo.substring(nPos);
			var pos = dataString.indexOf(">");
			dataString = dataString.substring(0,pos+1);
			var dataField = dataString.substring(20,pos-1);
			traceEventInfo = traceEventInfo.replace(dataString,"<message_"+(nMsgIndex)+">"+dataField+": ");
			traceEventInfo = traceEventInfo.replace("PZ__ERROR","message_"+(nMsgIndex));
			nPos = traceEventInfo.indexOf("<PZ__ERROR DATAFLD=", 0);
			nPos1 = traceEventInfo.indexOf("</PZ__ERROR>", nPos);
		}
	}

	var xmlDom = new XMLDomControl();


	if (xmlDom == null)
	{
		alert("displayPropertyPage(" + aCount + "): Cannot allocate memory for xmlDom");
		return;
	}
	xmlDom.async = false;
	var bLoaded = xmlDom.loadXML(traceEventInfo);
	if (!bLoaded)
	{
		alert("displayPropertyPage(" + aCount + "): Cannot load trace information into xmlDom");
		return;
	}

	//alert(xmlDom.xml);

	displayPage(xmlDom.xml,pageName,pagePropertyName);

}

function selectPrimaryPageName(event) {
	selectPageName(event, "PrimaryPage");
}

function selectActionSourcePageName(event) {
	selectPageName(event, "ActionSourcePage");
}

function selectActionTargetPageName(event) {
	selectPageName(event, "ActionTargetPage");
}

function selectPageName(event, pageType) {
	if(event == null)
		event = parent.TraceEvent.window.event;
	var theElement = event.srcElement || event.target;
	var theRow;

	theRow = theElement.parentElement;
	
	if (theRow != null){
		var sequenceNumberElement = pega.util.Dom.getElementsById("eventSequenceNumber", theRow)[0];
		var lineNumber = zUtil_reformatString(sequenceNumberElement.innerHTML, "&nbsp;", "");
		if ($.trim($(theElement).text())!="") {			
			gClickFromSpecialCell = true;
			if(traceEventArray[lineNumber] == null){
				var strUrl = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=getEvent&pzCommandSession=" + debugConnectionID + "&pzDebugConnection=" + (connectionID == "" ? connectionID_bak : connectionID) + "&pzEvent=" + lineNumber +"&pzNodeID=" + nodeID + "&pzTraceEventNodeID=" + remoteNodeId + "&pySessionType=" + pySessionType;		
				$.ajax({
				url: strUrl,
				cache:false,
				dataType: "xml"			
				}).done(function(xml){traceEventArray[lineNumber] = XMLToString(xml);}).done(function(xml){selectPageNameCallback(event, theRow, lineNumber, pageType);});
			} else{
				selectPageNameCallback(event, theRow, lineNumber, pageType);
			}
		}
	}
}

function selectPageNameCallback(event, theRow, lineNumber, pageType) {
		// parse the TraceEvent
		var traceEventInfo;
		traceEventInfo = traceEventArray[lineNumber];

		var aValue;
		var elemList = null;

		var xmlDom = new XMLDomControl();
		if (xmlDom == null)
		{
			alert("selectPageName(" + lineNumber+ "): Cannot allocate memory for xmlDom. Page Type: " + pageType);
			return;
		}
		var bLoaded = xmlDom.loadXML(traceEventInfo);
		if (!bLoaded)
		{
			alert("selectPageName(" + lineNumber+ "): Cannot load trace information into xmlDom. Page Type: " + pageType);
			return;
		}
		
		if (pageType == "ActionSourcePage") {
			elemList = xmlDom.getElementsByTagName("ActionSourcePageName");
		} else if(pageType == "ActionTargetPage"){
			elemList = xmlDom.getElementsByTagName("ActionTargetPageName");
		}else {
			elemList = xmlDom.getElementsByTagName("PrimaryPageName");
		}
		
		if (elemList.length > 0)
		{
			aValue = (elemList[0].textContent || elemList[0].text);
			if (aValue.substring(0,1) != "\"")
			{
				if (aValue.length > 0)
				{
					if (pageType == "PrimaryPage") {
						displayPropertyPage(lineNumber, aValue, "PrimaryPageContent");
					} else if (pageType == "ActionSourcePage") {
						displayPropertyPage(lineNumber, aValue, "ActionSourcePageContent");
					}else if(pageType == "ActionTargetPage"){
						displayPropertyPage(lineNumber, aValue, "ActionTargetPageContent");
					}
				}
			}
		}
}

function selectEventRow(event) {
	if(event == null)
		event = parent.TraceEvent.window.event;
	var theElement = event.srcElement || event.target;
	var theRow;

	// Fixed Bug no: B-12918
	// if the theElement.tagName is IMG its parent is TD and its parent is TR and
	// if the element .tagName is TD is parent is the TR

	if (theElement.tagName == "IMG"){
		theRow = theElement.parentElement.parentElement;
	}
	else{
		theRow = theElement.parentElement;
	}

	if (theRow != null){
		var sequenceNumberElement = $("#eventSequenceNumber", theRow)[0];

		if(sequenceNumberElement.innerHTML) // Bug-15945 :: checking whether the sequenceNumberElement has an object
	        {
			var sequenceNumber = zUtil_reformatString(sequenceNumberElement.innerHTML, "&nbsp;", "");
		}
		
		var lineNumberElement = $("#eventLineNumber", theRow)[0];

		if(lineNumberElement.innerHTML) // Bug-15945 :: checking whether the eventLineNumber has an object
	    {
			var lineNumber = zUtil_reformatString(lineNumberElement.innerHTML, "&nbsp;", "");
			if (gCurrentSelectIcon != null) {
			gCurrentSelectIcon.innerHTML = "&nbsp;&nbsp;&nbsp;";
		}

		var selectIconElement = $("#eventSelectIcon",theRow)[0];
		selectIconElement.innerHTML = "<IMG SRC='images/zselectarrow.gif' WIDTH='14' HEIGHT='13'>";

		gCurrentSelectIcon = selectIconElement;

		// Return if click on hyperlink of Tracer's Name column to open a record.
		if (gClickFromSpecialCell)
		{
			gClickFromSpecialCell = false;
			return;
		}
        displayTraceEvent(sequenceNumber, lineNumber);
		//window.setTimeout( "parent.MenuRow.displayTraceEvent(" + sequenceNumber + "," + lineNumber + ")", 10);
	        }
	}
}

function handleBreakpoint()
{

	var nHeight = 330;
	var nWidth = 430;
	var nTop = (window.screen.height / 2) - (nHeight / 2);
	var nLeft = (window.screen.width / 2) - (nWidth / 2);

	var strFeatures = "left="+nLeft+",top="+nTop+",height="+nHeight+",width="+nWidth;
	var strURL = strRequestorURIevents + "?pyStream=TraceBeepForBreak";
	//var strURL = "/WebWB/TraceEventBeep.htm";
	var strForm = "";
	//gObjTraceEventBeep = window.open(strURL,strForm,"status=no,toolbar=no,menubar=no,location=no,scrollbars=yes,resizable=no " + strFeatures);
	gObjTraceEventBeep = window.open("",strForm,"status=no,toolbar=no,menubar=no,location=no,scrollbars=no,resizable=no " + strFeatures);
	if (gObjTraceEventBeep != null)
	{
		//nNextFunction = FUNC_DONOTTRACEEVENT;
		//gObjTraceEventBeep.onLoad = checkIfCompleteLoad;
		//gObjTraceEventBeep.location.replace(strURL);
		gObjTraceEventBeep.document.open();
		//var strMessage = "<HTML><BODY STYLE='margin-left: 5px; margin-top: 5px;' BORDER = '0' BGCOLOR='#FFFFFF'>";
		//strMessage += "<FORM>";
		//strMessage += "<TABLE>";

		var strMessage = "<HTML><HEAD><TITLE></TITLE>" +
		//"<base href=\" +
		//strReqScheme + "://" + strServerPort + "/WebWB/\">" +
		"<base href=\"" + gURLServer + "\">" +
		"<BODY STYLE=\"margin-left: 5px; margin-top: 5px;\" BGCOLOR=\"#FFFFFF\">" +
		"<LINK HREF=\"procomstylesheet.css\" REL=\"STYLESHEET\" TYPE=\"text/css\">" +
		"</HEAD>";

		strMessage += "<TITLE>PegaRULESTracer - Stop at Breakpoint" + "</TITLE>";

		strMessage += "<CENTER><H1>Pay Attention,Please!!!</H1></CENTER>";
		strMessage += "<TABLE BORDER=0 WIDTH='100%'>";
		strMessage += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementTitleBarStyle'>&nbsp;Debug Message&nbsp;</TD></TR>";
		strMessage += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;&nbsp</TD></TR>";
		strMessage += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;&nbsp</TD></TR>";
		strMessage += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold' ALIGN='middle' width='100%'>Please close this message window, check run-time information, and press either Step or Continue icon from PegaRULESTracer's Toolbar to keep process going</TD></TR>";
		strMessage += "<TR><TD>&nbsp;&nbsp</TD></TR>";
		strMessage += "<TR><TD>&nbsp;&nbsp</TD></TR>";
		strMessage += "<TR><TD>&nbsp;&nbsp</TD></TR>";
		strMessage += "<TR><TD ALIGN='MIDDLE'><input class='littleButton' type=button value=OK onclick=self.close()></TD></TR>";
		strMessage += "</TABLE></BODY></HTML>"
		gObjTraceEventBeep.document.write(strMessage);
		gObjTraceEventBeep.document.close();

		gObjTraceEventBeep.focus();
	}
}

function buildTokenTable(aName, aValue, aToke1, aToke2, aStartIndex, aValColClass) {
	var strData = "";

	strData += "<TR CLASS='eventTable'><TD VALIGN='TOP' CLASS='eventElementDataBold'>&nbsp;" + aName + "&nbsp;</TD><TD>&nbsp;</TD></TR>";

	// create a row for each parameter value pair
	var endOffset=0;
	if (aStartIndex == 0) {
		endOffset = 1;
	}
	var valueArray1 = aValue.split(aToke1);
	var el1;

	strData += "<TR><TD></TD><TD>";
	strData += "<TABLE>";
	// for each line, create a row in the table
	for (el1=aStartIndex; el1 < valueArray1.length-endOffset; el1++) {
		var rowNum=el1;
		if (aStartIndex==0) 
			rowNum=el1+1;
		var valueArray2 = valueArray1[el1].split(aToke2);
		var el2;
		for (el2=0; el2 < valueArray2.length; el2++) {
			strData += "<TR CLASS='eventTable'>";
			strData += "<TD VALIGN='TOP' CLASS='eventElementLineNumberStyle'>" + rowNum + "&nbsp;</TD>";
			strData += "<TD CLASS='eventElementData'>" + valueArray2[el2] + "</TD>";
			strData += "<TD CLASS='" + aValColClass + "'>" + valueArray2[++el2] + "</TD>";
			strData += "</TR>";
		}
	}
	strData += "</TABLE>";
	strData += "</TD></TR>";

	return (strData);
}

function buildHyperLinks(aName, aValue, nSource)
{
	var atNums = 0;
	var nPos = -1;
	var nEndPos = 0;
	var strData = "";
	//var strBeforeAt = "";

	strData += "<TR CLASS='eventElementTitleBarStyle'><TD VALIGN='TOP' COLSPAN=2>&nbsp;" + aName + "&nbsp;</TD></TR>";

	nPos = aValue.indexOf("at ");
	if (nPos >= 0)
	{
		///strBeforeAt = aValue.substring(0,nPos);
		nEndPos = aValue.indexOf(")", nPos);
		while (nEndPos >= 0)
		{
			if ((aValue.substring(nPos,nEndPos+1)).indexOf("at Pega.jContext") < 0 &&
				 (aValue.substring(nPos,nEndPos+1)).indexOf("StackTrace.getStackTrace") < 0 &&
				 (aValue.substring(nPos,nEndPos+1)).indexOf("engine.tracer.TraceEvent") < 0 &&
				 (aValue.substring(nPos,nEndPos+1)).indexOf("engine.runtime.Executable.activityProlog") < 0 &&
				 (aValue.substring(nPos,nEndPos+1)).indexOf("engine.runtime.Executable.activityEpilog") < 0 &&
				 (aValue.substring(nPos,nEndPos+1)).indexOf("engine.tracer.TracerSession.traceActivity") < 0 &&
				 (aValue.substring(nPos,nEndPos+1)).indexOf("engine.tracer.TracerSession.traceActivityStepBegin") < 0 &&
				 (aValue.substring(nPos,nEndPos+1)).indexOf("engine.runtime.Executable.activityStepProlog") < 0 &&
				 (aValue.substring(nPos,nEndPos+1)).indexOf("engine.runtime.Executable.activityStepEpilog") < 0 &&
				 (aValue.substring(nPos,nEndPos+1)).indexOf("engine.tracer.TracerSession.traceActivityPrecondition") < 0 &&
				 (aValue.substring(nPos,nEndPos+1)).indexOf("engine.runtime.Executable.activityStepStartPreconditions") < 0 &&
				 (aValue.substring(nPos,nEndPos+1)).indexOf("engine.runtime.Executable.activityStepFinishPreconditions") < 0 &&
				 (aValue.substring(nPos,nEndPos+1)).indexOf("at Pega.TraceAppInterface") < 0)
			{
				if (nSource == 1)
					atArray[atNums] = aValue.substring(nPos,nEndPos+1);
				else
					atArray2[atNums] = aValue.substring(nPos,nEndPos+1);

				++atNums;
			}
			aValue = aValue.substring(nEndPos+1,aValue.length);
			nPos = aValue.indexOf("at ");
			if (nPos >= 0)
				nEndPos = aValue.indexOf(")", nPos);
			else
				break;  // exit of loop
		}
	}
	var atNumsPlus1 = atNums + 1;
	
	for (var j = 0; j < atNums; j++)
	{
		
		if (nSource == 1) {
			if (atArray[j].indexOf("at com.pegarules.generated") == 0) {
				strData += "<TR CLASS='eventTableJava'><TD CLASS='eventElementDataSelect' onclick=\"opener.getSource(" + j + ", 1);\" COLSPAN=2>&nbsp;"  + atArray[j] + "&nbsp;</TD></TR>";
			} else {
				strData += "<TR CLASS='eventTableJava'><TD CLASS='eventElementData' COLSPAN=2>&nbsp;"  + atArray[j] + "&nbsp;</TD></TR>";
			}
		} else {
			if (atArray[j].startsWith("com.pegarules.generated")) {
				strData += "<TR CLASS='eventTableJava'><TD CLASS='eventElementDataSelect' onclick=\"opener.getSource(" + j + ", 2);\" COLSPAN=2>&nbsp;"  + atArray2[j] + "&nbsp;</TD></TR>";
			} else {
				strData += "<TR CLASS='eventTableJava'><TD CLASS='eventElementData' COLSPAN=2>&nbsp;"  + atArray2[j] + "&nbsp;</TD></TR>";
			}
		}
	}
	strData += "</TR>";
	return (strData);
}

function sendCommand(nCommand)
{
	// Clear WatchVar display
	parent.MenuRow.clearIt();

	if (nCommand == 2)
	{
	   if (!gStepAvailable)
		return;
	}

	var strRequest;

	nTraceRequest = TR_TRACEEVENT;

	switch (nCommand)
	{
	case 1: // Continue command
	   if (!bStopAtBreakpoint)
			return;
		strRequest = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=continue&pzCommandSession=" + debugConnectionID + "&pzDebugConnection=" + connectionID +"&pzNodeID=" + nodeID + "&pzTraceEventNodeID=" + remoteNodeId + "&pySessionType=" + pySessionType;

	   nNextFunction = FUNC_TRACEANDPARSEEVENT;
	   break;
	case 2: // Step command
		if (!bStopAtBreakpoint)
			return;
		strRequest = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=step&pzCommandSession=" + debugConnectionID + "&pzDebugConnection=" + connectionID +"&pzNodeID=" + nodeID + "&pzTraceEventNodeID=" + remoteNodeId + "&pySessionType=" + pySessionType;
	   nNextFunction = FUNC_TRACEANDPARSEEVENT;
	   break;
	}
	xmlTraceEvent.onreadystatechange=checkIfFunctionReady;
	try
	{
		// send request to server via xmlCommand object
		var loadResult = xmlTraceEvent.load(strRequest);

	}
	catch (exception)
	{
		alert ("PegaRULESTracer - sendCommand() via xmlTraceEvent DOM: Exception!!!");
		return;
	}
}

function ThereIsNoBreakpointWatchVar()
{
	if (numOfBreakpoints <= 0 && numOfWatchVars <= 0)
	{
		//alert("ThereIsNoBreakpointWatchVar(): return true");
		return true;
	}

	//alert("ThereIsNoBreakpointWatchVar(): return false");
	return false;
}

function saveAs() {
	switch (queueType) {
	case "header":
		saveAsXML();
		break;
	case "memory":
		alert("The remote server has encountered an internal error. Please restart tracer.");
		break;
	default:
		saveAsXML();
		

	}
}

function saveAsXML() {
	strLocation = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=downloadSession&pzDebugConnection=" + (connectionID == "" ? connectionID_bak : connectionID) +"&pzNodeID=" + nodeID + "&pySessionType=" + pySessionType;
	// window.open(strLocation);
	// window.location = strLocation;
	top.frames["TraceEvent"].location = strLocation;
}

function saveAsCSV() {

	if (eventTables == null || eventTables.length == 0) {
		alert( "No data to save." );
		return;
	}

	var fullpath = null;
	try {
		fullpath = pthf.browseForFilename("", "Comma Seperated Values (*.csv)", "csv");
	}
	catch(e) {
		alert("pegaclientsupport.ocx doesn't appear to be installed on this computer -- please contact your system administrator.");
		return;
	}

	if(fullpath==null || fullpath=="") {
	  return;
	}

	var line = "";

	// access the tables body.
	for(var t = 0; t < eventTables.length; t++) {
		var theTable = eventTables[t];
		var indxa = 0;
		var rows = theTable.rows;
		var row = rows.item(indxa);
		while( row != null ) {

			var indxb = 1;
			var data = row.cells;
			var datum = data.item(indxb);
			while( datum != null ) {

				line = line + datum.innerText;
				indxb = indxb + 1;
				datum = data.item(indxb);
				if( datum != null ) {
					line = line + ",";
				} else {
					line = line + "#\r\n";
				}
			}

			indxa = indxa + 1;
			row = rows.item(indxa);
		}
	}	

	var success = pthf.save(fullpath, line);
	if (success==false) {
	  alert("Wasn't able to save the PAL info to '" + fullpath +"'.");
	}
}

function startADPTrace() {

	if (this!= null){
		var pzDebugBGConnId = this.text;
		startNewParallelTracer(pzDebugBGConnId);
	}
	if(event) {
		if(event.stopPropagation) event.stopPropagation();
		if(event.preventDefault) event.preventDefault();
	}
	else{
		parent.TraceEvent.window.event.cancelBubble = true;

	}
}

function buildDataPageDisplayName(lineValue) {
    if(lineValue != null && (lineValue.startsWith("D_") || lineValue.startsWith("DECLARE_") )) {
        if(lineValue.indexOf("[") >=0) {							    
		return lineValue.substring(0, lineValue.indexOf("#")) + lineValue.substring(lineValue.indexOf("["), lineValue.length);
	} else {
		return lineValue.substring(0, lineValue.indexOf("#"));
	}
    }
    return lineValue;
}

function getDPNameFromParameterizedDPName(primaryPageName) {
	if(primaryPageName != null && (primaryPageName.startsWith("D_") || primaryPageName.startsWith("Declare_")) && (primaryPageName.indexOf("_pa") > 0)) {
	     return primaryPageName.substring(0, primaryPageName.indexOf("_pa"));
	}
	return primaryPageName;
}

</script>


<!-- END:Rule-HTML-Fragement.TracerEventScripts -->