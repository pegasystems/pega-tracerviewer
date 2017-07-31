<script language="JavaScript">

// --------------------------------------
// Record name: TracerTraceGeneralScripts v.01-21
// --------------------------------------

// constant
var TRACE_EVENT_MILLISECS = 100;      // number of milliseconds for timer
var TRACE_EVENT_TIMEOUT_MILLISECS = 2;   // number of milliseconds for timer

var TRACE_EVENT_FUNC = "traceEvent()";
var TRACE_EVENT_LOCATION = "";
var TRACE_FAST_EVENT_LOCATION = "";
var TRACE_EVENT_LOCATION_WITHFORCE = "";
var TRACE_EVENT_RESPOND = "";
var TRACE_REPORTDIRECTORY = "/webwb/WbReports/Rule-Obj-Activity/";
var TRACE_NUMBER_OF_TRIALS = 50;
var TRACE_DOPLAY = 0;       //for response from toggling a 3-states pause button
var TRACE_DOPAUSE = 1;      //as above
var TRACE_DOCONTINUE = 2;   //as above

var TR_TRACEEVENT = 0;
var TR_MAKEREQUESTOR = 1;
var TR_STOPTRACEEVENT = 2;
var TR_CLOSETRACEEVENT = 4;
var TR_DISPLAYWATCHCHANGES = 2;

var FUNC_NOTATALL = 0;                   // for xmlTraceEvent DOM
var FUNC_CHECKANDSTARTTRACEEVENT = 1;    //    "
var FUNC_MAKETRACEREQUEST = 2;           //    "
var FUNC_LETCHECKOPTIONS = 3;            //    "
var FUNC_CALLTRACEEVENT = 4;             //    "
var FUNC_STOPTRACEEVENT = 5;             //    "
var FUNC_TRACEANDPARSEEVENT = 6;         //    "
var FUNC_DONOTTRACEEVENT = 7;            //    "
var FUNC_MAKEHTTPCONNECTION = 8;         //    "
var FUNC_CLOSEALLWINDOWS = 9;            //    "
var FUNC_DOSTARTNEWTRACER = 10;          //    "
var FUNC_NOTATALL = 11;                  //    "
var FUNC_FIRSTCALLTRACEEVENT = 12;       //

var FUNC_COMPLETESTOP = 999;             // totally stop tracing events

var FUNC2_NOTATALL = 0;                  // for xmlCommand DOM
var FUNC2_GETTRACEOPTIONS = 1;           //    "
var FUNC2_CALLTRACEEVENT = 2;            //    "
var FUNC2_DOMAKEBREAKPOINT = 3;          //    "
var FUNC2_DOREMOVEBREAKPOINT = 4;        //    "
var FUNC2_DOGETNEXTBREAKPOINT = 5;       //    "
var FUNC2_POPUPWINDOWFORBREAK = 6;       //    "
var FUNC2_DOSETBREAKPOINT = 7;           //    "
var FUNC2_DOGETACTIVITYLIST = 8;         //    "
var FUNC2_GETCURRENTVALUE = 9;           //    "
var FUNC2_SETNEWVALUE = 10;              //    "
var FUNC2_SETWATCHVAR = 11;              //    "
var FUNC2_REMOVEWATCHVAR = 12;           //    "
var FUNC2_GETNEXTWATCHVAR = 13;          //    "
var FUNC2_DOLISTWATCHVAR = 14;           //    "
var FUNC2_CLOSEALLWINDOWS = 15;          //    "
var FUNC2_DOSTARTNEWTRACER = 16;         //    "
var FUNC2_SHOWPAGE = 17;

var CALL_NOTATALL = 0;
var CALL_TRACEEVENT = 1;

// variables
var nTimeOut = 100;
var gMaxEventsPerRequest = 200;

var traceEventArray = new Array();
var DebugConnectionArray = new Array();
var classNameArray = new Array();
var atArray = new Array();
var atArray2 = new Array();
var traceDebugOptionArray = new Array();
var breakpointArray = new Array();
var watchVarArray = new Array();
var ruleSetsArray = new Array();
var bAlreadyLoadClassNameList = false;

var nOptTraceClassLoad = 0;
var nOptTraceException = 1;
var nOptTraceJContextBegin = 2;
var nOptTraceActivityBegin = 3;
var nOptTraceActivityEnd = 4;
var nOptTraceStepBegin = 5;
var nOptTraceStepEnd = 6;
var nOptTraceWhenBegin = 7;
var nOptTraceWhenEnd = 8;
var nOptTraceInputEditBegin = 9;
var nOptTraceInputEditEnd = 10;
var nOptTraceModelBegin = 11;
var nOptTraceModelEnd = 12;
var nOptExceptionBreak = 13;
var nOptStatusFailBreak = 14;
var nOptStatusWarnBreak = 15;
var nOptTraceAccessDenied = 16;
var nOptExpandJavaPage = 17;
var nOptAbbreviateEvents = 18;
var nOptMaxTraceEventsDisplayed = 19;
var nOptLocalVariables = 20;
var nOptTraceActionBegin = 21;
var nOptTraceActionEnd = 22;

var objConnectionWnd = null;
var objRequestorWnd = null;
var objTraceOptionWnd = null;
var objBreakWnd = null;
var objWnd = null;
var objZeusTraceServletWnd = null;
var objDownloadTraceOptionsWnd = null;
var objBreakpointWnd = null;
var objWatchVarWnd = null;
var objCommandWnd = null;
var objClipboardWnd = null;

var aPropertyPanelWnds = [];

//var xmlDocument = null;
var xmlTraceEvent = null;
var xmlCommand = null;

var startTraceEventID = "";
var doConnectionID = "";
var displayConnectionID = "";
var checkConnectionID = "";
var checkLoadStatusID = "";
var breakpointID = "";
var traceOptionsID = "";
var getDebugConnectionID = "";
var traceEventID = "";
var connectionID = "";
var nodeID = "";
var newConnectionID = "";
var connectionID_bak = "";
var debugConnectionID = "";
var traceDebugConnectionID = "";
var mURL = "";
var mCount = 0; // for counting Trace Events.
var strLocation = "";
var strLocationF = "";
var bFirstTime = true;
var objWd = null;
var nRetry = 0;
var mEventMessageCall = 0;
var curClassName = "";
var currentRequest = "";
var nTraceRequest = TR_TRACEEVENT;
var stopTraceFrom = "";
var connectionStatus = false;
var currentBreakpointIndex = -1;
var prvcurrentBreakpointIndex = -1;
var breakpointForm = null;
var watchVarForm = null;
var bAlreadyGetTraceOptionCookies = false;
var bAlreadyFoundTraceOptions = false;
var numOfBreakpoints = 0;
var numOfWatchVars = 0;
var bStopAtBreakpoint = false;
var nNextFunction;          // for xmlTraceEvent DOM object
var nNextCommandFunction;   // for xmlCommand DOM object
var bStopTraceResponse = false;
var gDebugFirstTime = true;
var gMessagesTableHTML;
var gDebugMessages = false;
var gReadyForMessage = false;
var gReadyState = false;
var gStepContinueEnabled = false;
var gTracerPaused = false;
var gpzConnID = "";
var gToolbarEnabled = false;
var gCheckFormReadyID = null;
var gNumOfTrials = 0;
var gStartNewTracer = false;
var gRuleSetsNums = 0;
var gDonotSendMoreDisconnect = false;
var gTotallyStop = false;
var gTraceEventID = 0;
var gEndOfAsyncTraceEventSent = false;

// URI's
var gURLServer = "<pega:reference name="pxThread.pxReqHomeURI" mode="normal" />" + "/"; //make sure it ends with /
var strRequestorURI = "<pega:reference name="pxThread.pxReqURI" mode="normal" />";
var TRACE_TRACERSERVLET_V3 = "<pega:reference name="pxRequestor.pxReqContextPath" mode="normal" />" + "/PRTraceServlet";

var gpxUserRuleSetList = "<% tools.appendString(tools.getPrimaryPage().getString("pxUserRuleSetList")); %>";
var pyUserEventTypesList = "<% tools.appendString(tools.getPrimaryPage().getString("pyUserEventTypesList")); %>";
var pyEventTypesList = "";
var pyRuleSetsList = "";
var strReqSchemeGenScript = "<pega:reference name="pxRequestor.pxReqScheme" mode="normal" />";
var pyPageNameList = "<% tools.appendString(tools.getPrimaryPage().getString("pyPageNameList")); %>";


//Define new variable to hold Tracer option setings from database instead of cookies (cou\okies are eliminated)
var pyTraceException = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceException")); %>";
var pyTraceJContextBegin = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceJContextBegin")); %>";
var pyTraceActivityBegin = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceActivityBegin")); %>";
var pyTraceActivityEnd = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceActivityEnd")); %>";
var pyTraceStepBegin = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceStepBegin")); %>";
var pyTraceStepEnd = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceStepEnd")); %>";
var pyTraceWhenBegin = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceWhenBegin")); %>";
var pyTraceWhenEnd = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceWhenEnd")); %>";
var pyTraceInputEditBegin= "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceInputEditBegin")); %>";
var pyTraceInputEditEnd = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceInputEditEnd")); %>";
var pyTraceModelBegin = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceModelBegin")); %>";
var pyTraceModelEnd = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceModelEnd")); %>";
var pyTraceActionBegin = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceActionBegin")); %>";
var pyTraceActionEnd = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceActionEnd")); %>";
var pyExceptionBreak = "<% tools.appendString(tools.getPrimaryPage().getString("pyExceptionBreak")); %>";
var pyStatusFailBreak = "<% tools.appendString(tools.getPrimaryPage().getString("pyStatusFailBreak")); %>";
var pyStatusWarnBreak = "<% tools.appendString(tools.getPrimaryPage().getString("pyStatusWarnBreak")); %>";
var pyTraceAccessDenied = "<% tools.appendString(tools.getPrimaryPage().getString("pyTraceAccessDenied")); %>";
var pyExpandJavaPage = "<% tools.appendString(tools.getPrimaryPage().getString("pyExpandJavaPage")); %>";
var pyAbbreviateEvents = "<% tools.appendString(tools.getPrimaryPage().getString("pyAbbreviateEvents")); %>";
var pyLocalVariables = "<% tools.appendString(tools.getPrimaryPage().getString("pyLocalVariables")); %>";
var pySessionType = "<% tools.appendString(tools.getPrimaryPage().getString("pySessionType")); %>";
var pyWatchInsKey = "<% tools.appendString(tools.getPrimaryPage().getString("pyWatchInsKey")); %>";
var pyWatchClassName = "<% tools.appendString(tools.getPrimaryPage().getString("pyWatchClassName")); %>";
var pyMaxTraceEventsDisplayed = "<% tools.appendString(tools.getPrimaryPage().getString("pyMaxTraceEventsDisplayed")); %>";

/*SE-27020 : Modify the changes of SE-25201 and obtain the connectionID from the new transient property that has been set in the TracerOptionsAvailable activity*/   
var Node_ID= "<% tools.appendString(tools.getPrimaryPage().getString("pyNodeID")); %>";
var Connection_ID= "<% tools.appendString(tools.getPrimaryPage().getString("pyRemoteClientConnection")); %>";
var tracerSettingsDataPageName = "<% tools.appendString(tools.getPrimaryPage().getName()); %>";




// Holds the type of queue that the server is using (used by the Save button)
var queueType = "";

// setup the event handlers, so the rollovers, mouse down and up will be used
zUtil_setUpEventHandlers();

// check ok, so the buttons will work
zUtil_setOkToUpdate(true);

gZUtil_initialLoadCallbackOk = true;

// general callback function, must be present
function zUtilCallback_loadDoc() {
  //We already call initRunTracer on html load...
  //initRunTracer();
}

//
// functions
//

function XMLDomControl(){
	this.readyState = 0;
	this.async = true;
	this.onreadystatechange = function(){};	
	this.parseError = new Object();
	this.parseError.errorCode = 0;
	this.documentElement = new Object();
	this.documentElement.nodeName = null;
	this.documentElement.getAttribute = null;
}

XMLDomControl.prototype.load = function(url){
	this.readyState = 0;
	//$.get(url, this.buildCallback(this));
	$.ajax({
		url: url,
		success: this.buildCallback(this),
		async: this.async,
		cache:false
	});
	return true;
};

XMLDomControl.prototype.buildCallback = function(aXObject){
	return function(data, textStatus, jqXHR){
		aXObject.readyState = 4;
                  aXObject.documentElement = $.parseXML(jqXHR.responseText);
                  aXObject.xml = jqXHR.responseXML;	
	         aXObject.onreadystatechange();
		return true;
	};
};

function XMLToString(oXML) {
    if (!window.XMLSerializer) {      
        return oXML.xml;  //if IE  
    } else {      
        return (new XMLSerializer()).serializeToString(oXML);   //other browsers 
    }  
}

function setInnerText(element, text){
	if(element.textContent == undefined){
		element.innerText = text;
	} else{
		if(text == "  ")
			element.innerHTML = "&nbsp;"
		else
     	 element.textContent = text;
   	}
}

XMLDomControl.prototype.getElementsByTagName = function(tagName){
	var elemList = $(tagName, this.documentElement);
	elemList.item = function(index){
		var thisObj = this;
		return thisObj[index];
	}
	return elemList;
}

XMLDomControl.prototype.loadXML = function(xmlString){
	this.documentElement = $.parseXML(xmlString);
	this.xml = xmlString;
	return true;
}

function initRunTracer()
{
    //writeSaneEventMessage("initRunTracer");
    //alert("initRunTracer");
    writeEventInfo("initRunTracer");

    // get the connection ID passed from the calling application.
    gDonotSendMoreDisconnect = false;
    var href = parent.document.location.href;
    var nPos = href.indexOf("ConnectionID=");
    if (nPos >= 0)
    {
        connectionID = href.substring(nPos+13, href.length);
        nPos = connectionID.indexOf("&");
        if (nPos > 0)
            connectionID = connectionID.substring(0, nPos);
		
      	nPos = href.indexOf("NodeID=");
      	if(nPos >= 0) {
                      	nodeID = href.substring(nPos+7, href.length);
                    }      
    }
    else
    {
        nPos = href.indexOf("pzConnID=");
        if (nPos >= 0)
        {
            connectionID = href.substring(nPos+13, href.length);
            nPos = connectionID.indexOf("&");
            if (nPos > 0)
                connectionID = connectionID.substring(0, nPos);
			
          	nPos = href.indexOf("NodeID=");
      		if(nPos >= 0) {
                      	nodeID = href.substring(nPos+7, href.length);
                    }                
        }
      else if(href.indexOf("pyactivitypzZZZ")>0 && connectionID=="") 
       { 
           connectionID = Connection_ID; 
           nodeID = Node_ID;
       } 
    }

	if (connectionID == "" && (href.indexOf("RedirectAndRun")!= -1))
	 {     
	       nPos = href.indexOf("Location=");
                var location = href.substring(nPos+9, href.length);
               nPos = location.indexOf("&");
               if (nPos > 0)
                    location = location.substring(0, nPos);
               if(location != "")
               {   
                   location = unescape(location);
                   nPos = location.indexOf("ConnectionID=");
                    if (nPos >= 0)
                    {
                        connectionID = location.substring(nPos+13, location.length);
                        nPos = connectionID.indexOf("&");
                        if (nPos > 0) {                          	
                            connectionID = connectionID.substring(0, nPos);
                          }
                    }
                    else
                    {
                        nPos = location.indexOf("pzConnID=");
                        if (nPos >= 0)
                        {
                            connectionID = location.substring(nPos+13, location.length);
                            nPos = connectionID.indexOf("&");
                            if (nPos > 0)
                                connectionID = connectionID.substring(0, nPos);
                        }
                    }
                 	nPos = location.indexOf("NodeID=");
                 	if(nPos >= 0) {
                      	nodeID = location.substring(nPos+7, location.length);
                    }
                 
               }
	 }

    gpzConnID = connectionID;

    if (connectionID == "")
    {
        alert("Connection ID is empty. Please restart Tracer.");
        gStartNewTracer = false;
        gDonotSendMoreDisconnect = true;
         writeEventInfo("Please restart Tracer");
         //disconnectExit();
        return;
    }
	
	if (connectionID != null)
	{
		// If this is a  Backgorund tracer - (connectionID contains the string 'pzDebugBGConnection')
		// 1. The BGTracerkey is appended to the title
		// 2. Confirm dialog that is shown when this tracer window is closed
		var newTitle = "";
		var connectionSuffix = "pzDebugBGConnection";
		var dbgPos = connectionID.indexOf(connectionSuffix);
		if (dbgPos > 0) {
			newTitle = connectionID.substring(0,dbgPos);
			window.onbeforeunload = function() { 
				return "You cannot view again the tracer events of this asynchronous execution after you close this window" ;
			};
		}
		if(newTitle != "") {
			parent.document.title = parent.document.title + " - " + newTitle;
		}
	}

    initSettings(); //init all settings including Options, Event Types, and RuleSets.

    gStartNewTracer = false;
    //
    // make xmlTraceEvent DOM object
    //
    if (xmlTraceEvent == null) {
        xmlTraceEvent = new XMLDomControl('Microsoft.XMLDOM');
        //xmlTraceEvent = new XMLDomControl("Microsoft.FreeThreadedXMLDOM");
    }

    if (xmlTraceEvent == null)
    {
        alert("Tracer - PegaRULES - initRunTracer(): cannot allocate memory for xmTraceEvent DOM object!!!");
        return;
    }

    //
    // make xmlCommand DOM object
    //
    if (xmlCommand == null) {
        xmlCommand = new XMLDomControl('Microsoft.XMLDOM');
    }

    if (xmlCommand == null)
    {
        alert("Tracer - PegaRULES - initRunTracer(): cannot allocate memory for xmCommand DOM object!!!");
        return;
    }
    establishTracerSession();
}

function initSettings()
{
    writeEventInfo("initSettings");

    // Init the trace options
    traceDebugOptionArray[nOptTraceClassLoad] = "N";
    traceDebugOptionArray[nOptTraceException] = pyTraceException;
    traceDebugOptionArray[nOptTraceJContextBegin] = pyTraceJContextBegin;
    traceDebugOptionArray[nOptTraceActivityBegin] = pyTraceActivityBegin;
    traceDebugOptionArray[nOptTraceActivityEnd] = pyTraceActivityEnd;
    traceDebugOptionArray[nOptTraceStepBegin] = pyTraceStepBegin;
    traceDebugOptionArray[nOptTraceStepEnd] = pyTraceStepEnd;
    traceDebugOptionArray[nOptTraceWhenBegin] = pyTraceWhenBegin;
    traceDebugOptionArray[nOptTraceWhenEnd] = pyTraceWhenEnd;
    traceDebugOptionArray[nOptTraceInputEditBegin] = pyTraceInputEditBegin;
    traceDebugOptionArray[nOptTraceInputEditEnd] = pyTraceInputEditEnd;
    traceDebugOptionArray[nOptTraceModelBegin] = pyTraceModelBegin;
    traceDebugOptionArray[nOptTraceModelEnd] = pyTraceModelEnd;
	traceDebugOptionArray[nOptTraceActionBegin] = pyTraceActionBegin;
	traceDebugOptionArray[nOptTraceActionEnd] = pyTraceActionEnd;
    traceDebugOptionArray[nOptExceptionBreak] = pyExceptionBreak;
    traceDebugOptionArray[nOptStatusFailBreak] = pyStatusFailBreak;
    traceDebugOptionArray[nOptStatusWarnBreak] = pyStatusWarnBreak;
    traceDebugOptionArray[nOptTraceAccessDenied] = pyTraceAccessDenied;
    traceDebugOptionArray[nOptExpandJavaPage] = pyExpandJavaPage;
    traceDebugOptionArray[nOptAbbreviateEvents] = pyAbbreviateEvents;
    traceDebugOptionArray[nOptMaxTraceEventsDisplayed] = pyMaxTraceEventsDisplayed;
    traceDebugOptionArray[nOptLocalVariables] = pyLocalVariables;
    
    parent.MenuRow.setDisplayMaxEvents(pyMaxTraceEventsDisplayed);

    //
    // Init EventTypes
    //
    var nPos = -1;
    var sNmae = "";
    var sValue = "";
    var nIndex = 0;

    pyEventTypesList = "";
    nPos = pyUserEventTypesList.indexOf(";");

    while (nPos > 0)
    {
        sName = pyUserEventTypesList.substring(0, nPos);
        pyUserEventTypesList = pyUserEventTypesList.substring(nPos+1,pyUserEventTypesList.length);
        nPos = sName.indexOf("/");
        if (nPos > 0)
        {
            sValue = sName.substring(nPos+1, nPos+2);
            sName = sName.substring(0, nPos);
            if (sValue == "Y" || sValue == "y")
            {
                nIndex += 1;
                pyEventTypesList += "&eventType" + nIndex + "=" + sName;
            }
        }
        else
            break;
        nPos = pyUserEventTypesList.indexOf(";");
    }

    //
    // init RuleSets
    //
    nIndex = 0;
    pyRuleSetsList = "";
    nPos = gpxUserRuleSetList.indexOf(";");
    while (nPos > 0)
    {
        sName = gpxUserRuleSetList.substring(0, nPos);
        gpxUserRuleSetList = gpxUserRuleSetList.substring(nPos+1, gpxUserRuleSetList.length);
        nPos = sName.indexOf(":");
        if (nPos > 0)
        {
            sValue = sName.substring(nPos+1, nPos+2);
            sName = sName.substring(0, nPos);
            if (sValue == "Y" || sValue == "y")
            {
                nIndex += 1;
                pyRuleSetsList += "&ruleSet" + nIndex + "=" + sName;
            }
        }
        else
            break;
        nPos = gpxUserRuleSetList.indexOf(";");
    }
}

function establishTracerSession()
{
    // make an http connection to the server
    writeEventInfo("Wait for connection to ... (1)");

    nNextFunction = FUNC_CHECKANDSTARTTRACEEVENT;
    xmlTraceEvent.onreadystatechange=checkIfFunctionReady;
    try
    {
        // send request to server
        var loadResult = xmlTraceEvent.load(TRACE_TRACERSERVLET_V3);
        if (!loadResult)
          alert("Issue loading: establishTracerSession");
    }
    catch (exception)
    {
        alert("Tracer - PegaRULES - makeHTTPConnection(): Exception!!!");
        return;
    }
}

function checkIfFunctionReady()
{
    if (xmlTraceEvent == null)
        return;  // if we suspend Tracer, it is the case.

    if (nNextFunction == FUNC_COMPLETESTOP)
        return;  // do nothing because it is completely stopped

	// Check for errors from the server before continuing...
    if (xmlTraceEvent)
    {  
       elemList = xmlTraceEvent.getElementsByTagName("CmdStatus");
       if (elemList.length > 0) {
          var cmdStatus = (elemList[0].textContent || elemList[0].text);
          if (cmdStatus.indexOf("error") >= 0) {
              elemList = xmlTraceEvent.getElementsByTagName("CmdResponse");
              if (elemList.length > 0) {
                  var aMessage = "";
                  var cmdResponse = (elemList[0].textContent || elemList[0].text);
                  aMessage = cmdResponse + " - Please restart tracer.";
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
                  writeEventInfo("Please restart tracer...");
                  return;
              }
          }
       }
    } 

    var xmlState = xmlTraceEvent.readyState;

    if (xmlState == 4) {
        //writeEventMessage("Receive trace event");
        //alert("xmlTraceEvent.onreadystatechange: Received trace event");

        var xmlError = xmlTraceEvent.parseError;
        if (xmlError.errorCode != 0) {
            // received an error
            writeEventMessage("Error Received for xmlTraceEvent DOM: " + xmlError.errorCode +
                " - Error reason: " + xmlError.reason);
            return;
        }
      
        switch (nNextFunction)
        {
        case FUNC_CHECKANDSTARTTRACEEVENT:
            checkAndStartTraceEvent();
            break;

        case FUNC_MAKETRACEREQUEST:
            makeTraceRequest();
            break;

        case FUNC_LETCHECKOPTIONS:
            letCheckOptions();
            break;

        case FUNC_CALLTRACEEVENT:

            //var currentTime = currentTimeMillis();
            //gTimeSpent = "Call traceEvent() at " + currentTime;
            //writeEventMessage(gTimeSpent);
            if (gTraceEventID)
            {
                clearInterval(gTraceEventID);
                gTraceEventID = 0;
            }
            pyMaxTraceEventsDisplayed = parent.MenuRow.getDisplayMaxEvents();
            if (pyMaxTraceEventsDisplayed > 0) {
                traceEvent();
            } else {
                writeEventInfo("Tracing...Not reading events.");
            }
            break;

        case FUNC_FIRSTCALLTRACEEVENT:
            firstCallTraceEvent();
            break;

        case FUNC_TRACEANDPARSEEVENT:
            if (!xmlError.errorCode)
            {
                parseTraceEvent();
            }
            
            switch (nTraceRequest)
            {
            case TR_MAKEREQUESTOR:
                 nTraceRequest = TR_TRACEEVENT
                 doMakeRequestor();
                 return;
                 break;

            case TR_STOPTRACEEVENT:
                nTraceRequest = TR_TRACEEVENT
                if (!bStopAtBreakpoint)
                {
                    doDisconnection();
                    return;
                }
                else
                    writeEventMessage("Cannot stop tracing events. Finish debugging and stop!.");
                break;

            //case TR_CLOSETRACEEVENT:
            //    disconnect();
            //   return;

            case TR_TRACEEVENT:
                break;
            }
            // Call traceEvent() for sending trace request to get event information
            if (!bStopAtBreakpoint && !gEndOfAsyncTraceEventSent)
            {
                nNextFunction = FUNC_CALLTRACEEVENT;
                gTraceEventID = setInterval("checkIfFunctionReady()", nTimeOut);
                if (gTraceEventID <= 0)
                {
                    alert("Cannot setInterval() for checkIfFunctionReady()");
                }
            }
            break;

        case FUNC_STOPTRACEEVENT:
            nNextFunction = FUNC_COMPLETESTOP;
            stopTraceEvent();
            break;

        //case FUNC_CLOSEALLWINDOWS:
        //    xmlTraceEvent = null;
        //    doCloseAllWindows();
        //    break;

        case FUNC_DONOTTRACEEVENT:
            gObjTraceEventBeep.focus();
            //doDisconnection();
            break;

        case FUNC_MAKEHTTPCONNECTION:
            makeHTTPConnection();
            break;

        case FUNC_DOSTARTNEWTRACER:
            doStartNewTracer();
            break;
        }
    }
}

function checkIfCommandFunctionReady()
{
    if (xmlCommand == null)
        return;  // if we suspend Tracer, it is the case.

    var xmlState = xmlCommand.readyState;

    if (xmlState == 4) {

        var xmlError = xmlCommand.parseError;
        if (xmlError.errorCode != 0) {
            // received an error
            writeEventMessage("Error Received for xmlCommand DOM: " + xmlError.errorCode +
                " - Error reason: " + xmlError.reason);
            return;
        }

        switch (nNextCommandFunction)
        {
        case FUNC2_GETTRACEOPTIONS:
            getTraceOptions();
            break;

        case FUNC2_CALLTRACEEVENT:
            traceEvent();
            break;

        case FUNC2_DOMAKEBREAKPOINT:
            doMakeBreakpoint();
            break;

        case FUNC2_DOREMOVEBREAKPOINT:
            doRemoveBreakpoint();
            break;

        case FUNC2_DOGETNEXTBREAKPOINT:
            //doGetNextBreakpoint();
            break;

        case FUNC2_POPUPWINDOWFORBREAK:
            //writeEventMessage("Stopped at a breakpoint ...");
            //alert("Please press Continue button if you done checking!!!");
            break;

        case FUNC2_DOSETBREAKPOINT:
            doSetBreakpoint();
            break;

        case FUNC2_DOGETACTIVITYLIST:
            doGetActivityList();
            break;

        case FUNC2_GETCURRENTVALUE:
            doGetCurrentValue();
            break;

        case FUNC2_SETNEWVALUE:
            doSetNewValue();
            break;

        case FUNC2_SETWATCHVAR:
            doSetWatchVar();
            break;

        case FUNC2_REMOVEWATCHVAR:
            doRemoveWatchVar();
            break;

        case FUNC2_GETNEXTWATCHVAR:
            //doGetNextWatchVar();
            break;

        case FUNC2_DOLISTWATCHVAR:
            //doListWatchVar();
            break;

        case FUNC2_CLOSEALLWINDOWS:
            doCloseAllWindows();
            break;

        case FUNC2_DOSTARTNEWTRACER:
            doStartNewTracer();
            break;

        case FUNC2_SHOWPAGE:
            doShowPage();
            break;

        case FUNC2_NOTATALL:
            break;

        }
    }
}

function checkIfCompleteLoad()
{
    if (gObjTraceEventBeep == null)
        return;
    if (!gObjTraceEventBeep.onerror)
    {
        alert("checkIfCompletLoad");
        gObjTraceEventBeep.focus();
    }
}

function closeAllWindows()
{

    $("#traceEvent-TABLE").empty();

    if (gStartNewTracer)
    {
        //alert("closeAllWindows: gStartNewTracer: " + gStartNewTracer);
        gDonotSendMoreDisconnect = true; //This will help not to send a second disconnect request because
                                         // parent parent.document.location.replace(strURL) will call closeAllWindows()
                                         // again and will go to else branch and no need to send a second disconnect request.
                                         // disconnectExit() is only called when closing Tracer
        disconnect();
        gStartNewTracer = false;
      
      	// SE-27020 : Use safeURL instead of hardcoded query string
      	var objTempSafeURL = SafeURL_createFromURL(strRequestorURI);
        objTempSafeURL.put("pyActivity","Data-TRACERSettings.pzStartTracerSession");
        objTempSafeURL.put("ConnectionID" , newConnectionID);      
      	objTempSafeURL.put("NodeID" , nodeID);
      
        var strURL = objTempSafeURL.toURL();
      
        if (xmlTraceEvent != null)
        {
            delete xmlTraceEvent;
            xmlTraceEvent = null;
        }
        parent.document.location.replace(strURL);
    }
    else
    {
        if (!gDonotSendMoreDisconnect)
            disconnectExit();
    }
    doCloseAllWindows();
}

function doCloseAllWindows()
{
    // close connection list window
    if (objRequestorWnd != null)
    {
        if (!objRequestorWnd.closed)
            objRequestorWnd.close();
    }
    // close option-settings window
    if (objTraceOptionWnd != null)
    {
        if (!objTraceOptionWnd.closed)
            objTraceOptionWnd.close();
    }
    // close breakpoint window
    if (objBreakpointWnd != null)
    {
        if (!objBreakpointWnd.closed)
            objBreakpointWnd.close();
    }
    // close WatchVar window
    if (objWatchVarWnd != null)
    {
        if (!objWatchVarWnd.closed)
            objWatchVarWnd.close();
    }
    // close clipbord window
    if (objClipboardWnd != null)
    {
        if (!objClipboardWnd.closed)
            objClipboardWnd.close();
    }
  
  for(var i = 0; i < aPropertyPanelWnds.length; i++){
    if(aPropertyPanelWnds[i] && !aPropertyPanelWnds[i].closed){
      aPropertyPanelWnds[i].close();
    }
  }
}

function getClipboard(enabled)
{
    openWindowForInput("GetClipboard&pzDebugConnection=","0","0","82","59");
}

function getClipboard_save(enabled)
{
    nNextFunction = "";
    if (!gClipBoardViewerAvailable)
    return;

    //openWindowForInput("GetClipboard&pzDebugConnection=","0","0","82","59");
    if (!enabled)
        return;

    // do nothing if the tracer is stopping at breakpoint
    if (bStopAtBreakpoint)
        return;

    var bOpenNewWindow = false;
    if (objClipboardWnd == null)
        bOpenNewWindow = true;
    else if (objClipboardWnd.closed)
        bOpenNewWindow = true;
    if (bOpenNewWindow)
    {
        var nHeight = 500;
        var nWidth = 660;

        var nTop = (window.screen.height / 2) - (nHeight / 2);
        var nLeft = (window.screen.width / 2) - (nWidth / 2);
        var strFeatures = "left="+nLeft+",top="+nTop+",height="+nHeight+",width="+nWidth;
        var strURL = strRequestorURI + "?pyStream=TraceClipboard&pzConnID=" + gpzConnID;

        var strForm = "";
        objClipboardWnd = window.open(strURL, strForm, "status=yes,toolbar=no,menubar=no,location=no,scrollbars=yes,resizable=yes" + strFeatures);
        if (objClipboardWnd.opener == null)
            objClipboardWnd.opener = self;
    }
    objClipboardWnd.focus();
}

function getCookieData(label)
{
    var labelLen = label.length;
    var cLen = document.cookie.length;
    var i = 0;
    var cEnd;
    while(i < cLen)
    {
        var j = i + labelLen
        if (document.cookie.substring(i, j) == label)
        {
            cEnd = document.cookie.indexOf(";",j)
            if (cEnd == -1)
            {
                cEnd = document.cookie.length;
            }
            return unescape(document.cookie.substring(j, cEnd));
        }
        i++;
    }
    return "";
}

var gMessageHeader = "";

// There are many calls to writeEventMessage all throughout the tracer,
// many of them are debug lines.  At some point, these were shut off.  Now,
// we want to allow messages to display in the Messages fram, use this
// method.
//
function writeSaneEventMessage(strMessage)
{

    if (strMessage == "" && parent.Messages.document.body.innerHTML == '<SPAN style="FONT-FAMILY: arial"></SPAN>') {
        return;
    }
    var lclMessage = "<span style='font-family:arial;'>" + gMessageHeader + strMessage + "</span>";
    parent.Messages.document.body.innerHTML = (lclMessage);
}

function writeEventMessage(strMessage)
{
    return;
}

function writeEventMessage_save(strMessage)
{
    parent.Messages.document.open();
    parent.Messages.document.write(strMessage);
    parent.Messages.document.close();
}

function writeEventMessage_save2(strMessage)
{
    if (!gDebugMessages)
        return;

    if (!gReadyForMessage)
        return;
    mEventMessageCall++;
    if (gDebugFirstTime) {

        gMessagesTableHTML = parent.Messages.document.all("messages-TABLE").outerHTML;
        gDebugFirstTime = false;
    }
    // create a new row
    var theRow = parent.Messages.document.all("messages-TABLE").insertRow(1);

    theRow.onclick = null;
    theRow.style.cursor = ""; //"hand";
    theRow.id="messageRow";

    theCell = theRow.insertCell();
    theCell.innerHTML = "&nbsp;&nbsp;&nbsp;";
    theCell.className = "eventDataBold";
    theCell.id = "messageBlanks";

    // line
    theCell = theRow.insertCell();
    theCell.innerHTML = "&nbsp;&nbsp;" + mEventMessageCall + "&nbsp;";
    theCell.className = "eventDataBold";
    theCell.id = "messageLineNumber";

    // Time
    theCell = theRow.insertCell();
    //theCell.innerHTML= "&nbsp;" + currentTimeMillis() + "&nbsp;";
    //theCell.innerHTML= "&nbsp;" + "00:00:00" + "&nbsp;";
    theCell.className = "eventData";
    theCell.id = "messageTime";

    theCell = theRow.insertCell();
    theCell.innerHTML = "&nbsp;" + strMessage + "&nbsp;";
    theCell.className = "eventData";
    theCell.id = "message";
}

function writeEventInfo(strMessage)
{
    if (parent == null)
        return;
    if (parent.EventInfo == null)
        return;
    if (parent.EventInfo.document == null)
        return;
    parent.EventInfo.document.open();
    parent.EventInfo.document.write("<span style='font-family:arial'>"+strMessage+"</span>");
    parent.EventInfo.document.close();
}

function checkConnection()
{
    var strData = parent.EventInfo.document.documentElement.innerHTML;
    if (strData == null)
        return;

    //writeEventMessage("checkConnection(): strData = " + strData);
    if (strData == "" || strData == null)
        return;

    var nPos = strData.indexOf("<keepalive/>");
    if (nPos < 0)
        nPos = strData.indexOf("<KEEPALIVE/>");

    if (nPos >= 0)
    {
        window.clearInterval(checkConnectionID);
        //writeEventMessage("Connection is established...");
        getDebugConnection();
    }
}

function makeConnection(ev)
{
    var readyState = isEnabled(ev);
    if (checkIfStopMode())
        return; // do nothing if Tracer is in STOP mode

    if (checkTracerPaused())
        return; // do nothing

    if (objRequestorWnd != null)
    {
        if (!objRequestorWnd.closed)
        {
            objRequestorWnd.focus();
            return;
        }
    }

    if (!gConnectionListEnable)
    return;

    if (!readyState)
        return;

    gReadyState = false;
    var nHeight = 450;
    var nWidth = 680;
    var nWndTop = window.screen.height/2  - nHeight / 2;
    var nWndLeft = window.screen.width /2 - nWidth / 2;
    var strFeatures = "left="+nWndLeft+",top="+nWndTop+",height="+nHeight+",width="+nWidth;
    var strURL = "";
    var strForm = "";
    objRequestorWnd = window.open(strURL,strForm,"status=no,toolbar=no,menubar=no,location=no,scrollbars=no,resizable=yes " + strFeatures);
    if (objRequestorWnd.opener == null)
        objRequestorWnd.parent = self;

    //In order to stop tracing Tracer itsself, we use record-built-in script
    //strURL = strRequestorURI + "?pyStream=TraceConnectionListDialog";
    //objRequestorWnd.document.location.replace(strURL);
    openConnectionListDialog();
}

function makeNewHost(hostName, portNumber, newRequestor)
{
    connectionID = newRequestor;
    if (traceEventID != "")
    stopTraceEvent();
    var strLocation = strReqSchemeGenScript + "://" + hostName + ":" + portNumber + strRequestorURI + "?pyStream=Tracer";
    parent.document.location.replace(strLocation);
}

function currentTimeMillis()
{
    var now = new Date();
    return now.getTime();
}

function traceWait(milliSecs)
{
    var now = new Date();
    now = now.getTime();
    var endTime = now + milliSecs;
    while (endTime - now > 0)
    {
      now = new Date();
      now = now.getTime();
    }
}

function startNewTracer(pzConnID)
{
    //alert("startNewTracer " + pzConnID);
    setReadyState(false);
    newConnectionID = pzConnID;
    //gpzConnID = pzConnID;
    nNextFunction = FUNC_STOPTRACEEVENT;
    //nNextCommandFunction = FUNC2_DOSTARTNEWTRACER;
    //xmlCommand.onreadystatechange=checkIfCommandFunctionReady;
    //disconnect();
    doStartNewTracer();
}

function startNewParallelTracer(pzDebugBGConnId)
{
    newConnectionID = pzDebugBGConnId;
    doStartNewParallelTracer();
}

function doStartNewTracer()
{
     gStartNewTracer = true;
     closeAllWindows();
}

function doStartNewParallelTracer()
{
	// The connectionId is suffixed with string 'pzDebugBGConnection' which is identifies this connection as a background tracing connection.
	var strURL = strRequestorURI + "?pyActivity=Data-TRACERSettings.pzStartTracerSession&ConnectionID=" + newConnectionID + "pzDebugBGConnection";
	var nHeight = 600;
	var nWidth = 1000;

	var nTop = (window.screen.height / 2) - (nHeight / 2);
	var nLeft = (window.screen.width / 2) - (nWidth / 2);
	var strFeatures = "left="+nLeft+",top="+nTop+",height="+nHeight+",width="+nWidth;

	var strForm = "";
	var objParallelTracerWnd = window.open(strURL, strForm, "status=yes,toolbar=no,menubar=no,location=no,scrollbars=yes,resizable=yes " + strFeatures);
	if (objParallelTracerWnd.opener == null)
		objParallelTracerWnd.opener = self;
    	objParallelTracerWnd.focus();
}

function makeRequestor(requestor, forced)
{
    if ((connectionStatus == true) && (connectionID == requestor))
    {
        return; // do nothing
    }
    gpzConnID = requestor;
    writeEventInfo("Wait for connection " + requestor + " ...");
    nTraceRequest = TR_MAKEREQUESTOR;
    newConnectionID = requestor;
    deleteTraceEvents(true);
    doMakeRequestor();
}

function doMakeRequestor()
{
    //writeSaneEventMessage("doMakeRequestor: sending disconnect");
    var strURL = "";
    strURL = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=disconnect&pzForceDisconnect=Y&pzDebugConnection=" + connectionID +"&pzNodeID=" + nodeID  + "&pySessionType=" + pySessionType;
    connectionID = newConnectionID;
    gpzConnID = connectionID;
    bAlreadyGetTraceOptionCookies = false;
    nNextFunction = FUNC_MAKEHTTPCONNECTION;
    xmlTraceEvent.onreadystatechange=checkIfFunctionReady;
    bStopTraceResponse = false;
    try
    {
        // send request to server
        var loadResult = xmlTraceEvent.load(strURL);
        if (!loadResult)
          alert("Issue loading: doMakeRequestor");
    }
    catch (exception)
    {
        alert("Tracer - PegaRULES - makeRequestor(): Exception!!!");
        return;
    }
}
function disconnectExit()
{
    //writeSaneEventMessage("disconnectExit: sending disconnect");
    //alert("disconnectExit()");
    var strURL = "";
    //strURL = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=disconnect&pzForceDisconnect=Y&pzDebugConnection=" + (connectionID == "" ? connectionID_bak : connectionID) + "&pzXmlOnly=true&pzDestroyQueue=true";
    strURL = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=disconnect&pzForceDisconnect=Y&pzDebugConnection=" + (connectionID == "" ? connectionID_bak : connectionID) + "&pzXmlOnly=true" +"&pzNodeID=" + nodeID  + "&pySessionType=" + pySessionType ;
    if(!gEndOfAsyncTraceEventSent) {
	//In the case of background tracer, by the time end of asyn tracer event is sent the session would have been deleted, so not required to destroy queue.
	strURL += "&pzDestroyQueue=true";
    }
    nNextCommandFunction = FUNC2_NOTATALL;
    //if (xmlTraceEvent != null)
    //  xmlTraceEvent.onreadystatechange=checkIfCommandFunctionReady;
    var xmlCommand = new XMLDomControl('Microsoft.XMLDOM');
    xmlCommand.async = false;
    try
    {
        // send request to server
        var loadResult = xmlCommand.load(strURL);
        if (!loadResult)
          alert("Issue loading: disconnectExit");
    }
    catch (exception)
    {
        alert ("Tracer - PegaRULES - disconnect(): Exception!!!");
        return;
    }
}

function disconnect()
{
    //writeSaneEventMessage("disconnect: sending disconnect");
    var strURL = "";
    strURL = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=disconnect&pzForceDisconnect=Y&pzDebugConnection=" + connectionID + "&pzXmlOnly=true" +"&pzNodeID=" + nodeID  + "&pySessionType=" + pySessionType;

    if (xmlCommand == null) {
        xmlCommand = new XMLDomControl('Microsoft.XMLDOM');
    }

    if (xmlCommand == null)
    {
        alert("Tracer - PegaRULES - disconnect(): cannot allocate memory for xmCommand DOM object!!!");
        return;
    }

    xmlCommand.async = false;
    try
    {
        // send request to server
        var loadResult = xmlCommand.load(strURL);
        if (!loadResult)
          alert("Issue loading: disconnect");
    }
    catch (exception)
    {
        //alert ("Tracer - PegaRULES - disconnect(): Exception!!!");
        return;
    }
}

function handleCases(option)
{
    switch (option) {
    case TRACE_DOPLAY:
        break;
    case TRACE_DOPAUSE:
        break;
    case TRACE_DOCONTINUE:
        sendCommand(1);
        return;
    default:
        return;
    }

    if (!gPausePlayEnable)
    return;

    if (!gReadyState)
        return;

    gReadyState = false;
    writeEventInfo("Please wait ...");
    if (option == TRACE_DOPLAY)
    {
        resumeTraceEvent();
        return;
    }

    // Make sure stop trace loop before sending disconect request to stop tracing
    //enablePause();
    //nTraceRequest = TR_STOPTRACEEVENT;
    nNextFunction == FUNC_COMPLETESTOP;
    doDisconnection();
}

function makeDisconnection(strDebugRequest, form, nTop, nLeft, nHeight, nWidth, disconnect)
{
    if (checkIfStopMode())
        return; // Do nothing if Tracer is in STOP mode.

    if (!gPausePlayEnable)
        return;

    if (!gReadyState)
        return;

    gReadyState = false;
    writeEventInfo("Please wait ...");
    if (disconnect == false)
    {
        resumeTraceEvent();
        return;
    }

    // Make sure stop trace loop before sending disconect request to stop tracing
    //enablePause();
    //nTraceRequest = TR_STOPTRACEEVENT;
    nNextFunction == FUNC_COMPLETESTOP;
    doDisconnection();
}

function doDisconnection()
{
    //writeSaneEventMessage("doDisconnection: sending disconnect");
    var strURL = "";
    strURL = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=disconnect&pzDebugConnection=" + connectionID +"&pzNodeID=" + nodeID  + "&pySessionType=" + pySessionType;
    connectionID_bak = connectionID;
    connectionID = "";
    if (gTraceEventID > 0)
        clearInterval(gTraceEventID);
    nNextFunction = FUNC_STOPTRACEEVENT;
    xmlTraceEvent.onreadystatechange=checkIfFunctionReady;
    bStopTraceResponse = false;
    try {
        // send request to server
        var loadResult = xmlTraceEvent.load(strURL);
        if (!loadResult)
          alert("Issue loading: doDisconnection");
    }
    catch (exception)
    {
        alert("Tracer - PegaRULES - makeDisconnection(): Exception!!!");
        return;
    }
}

function getDebugConnection()
{
    var strZeusSessionCookie = "ZeusSessionCookie=";
    var nLen = document.cookie.length;
    var i = 0;
    var nEnd;
    var strCookie = "Cookie = " + document.cookie;
    if (getDebugConnectionID != "")
    {
        window.clearInterval(getDebugConnectionID);
        getDebugConnectionID = "";
    }
    //alert(strCookie);
    debugConnectionID = getCookieData(strZeusSessionCookie);
    if (debugConnectionID == "") {
      getDebugConnectionID = window.setInterval("getDebugConnection()", TRACE_EVENT_MILLISECS);
    }
    //alert("debugConnectionID: " + debugConnectionID);
}

function checkLoadStatus()
{
    return;
}

function checkLoadStatus_save()
{
    var strStatus = objWd.defaultStatus;
    if (strStatus == "Done")
    {
        clearInterval(checkLoadStatusID);
        parent.Messages.document.open();
        parent.Messages.document.write("Loading URL is done...");
        parent.Messages.document.close();
        // write data received from server to EventInfo
        parent.EventInfo.document.open();
        parent.EventInfo.document.write(objWd.document.documentElement.innerHTML);
        parent.EventInfo.close();
    }
    else
    {
        parent.Messages.document.open();
        parent.Messages.document.write("Loading URL is not done...");
        parent.Messages.document.close();
    }
}

function checkLoadTraceOptionsStatus()
{
    var strStatus = objWd.defaultStatus;
    if (strStatus == "Done")
    {
        clearInterval(checkLoadStatusID);
        objWd.document.all("pzConnectionID").value = connectionID;
    }
}

function openWindowForHelpAbout(nTop, nLeft, nHeight, nWidth)
{
    if (nTop > 100 || nLeft > 100 || nHeight > 100 || nWidth > 100)
    {
        alert("form dimension must be between 0 and 100");
        return;
    }

    var nWndTop = window.screen.height * nTop / 100;
    var nWndLeft = window.screen.width * nLeft / 100;
    var nWndHeight = window.screen.height * nHeight / 100;
    var nWndWidth = window.screen.width * nWidth / 100;
    var strFeatures = "left="+nWndLeft+",top="+nWndTop+",height="+nWndHeight+",width="+nWndWidth;
    var strURL = "";
    var strForm = "";
    var objHelp = null;

    objHelp = window.open(strURL,strForm,"status=no,toolbar=no,menubar=no,location=no,scrollbars=no,resizable=yes " + strFeatures);
    objHelp.document.open();
    objHelp.document.write("<HTML><HEAD><TITLE>Tracer - PegaRULES - Help About</TITLE></HEAD><H1><CENTER>Tracer - PegaRULES</CENTER></H1><HR noshade><BR><CENTER>Version 2.0</CENTER></HTML>");
    objHelp.document.write("<BR><CENTER><INPUT class=largeButton type=button value=\"OK\" title=\"Click here to exit\" onclick=\"self.close();\"></CENTER>");
    objHelp.document.close();
    objHelp.focus();
}

// Command trace servlet to establish session.
//
function checkAndStartTraceEvent()
{
    //alert("Session type:" + pySessionType);
    writeEventInfo("Wait for connection to ... (2)");
    traceWait(1);
    writeEventInfo("Wait for connection to ... (3)");
    startTraceEvent("connect&pzCommandSession=");
}

function startTraceEvent(strDebugRequest)
{
    var strURL = "";

    /* We don't allow you to get to this point if you
       don't already have a valid connection.
    if (connectionID == "")
    {
        var paramLabel = "Please enter a valid connection:";
        var paramInput = "workbench_";
        alert("Please restart Tracer.");
        gStartNewTracer = false;
        gDonotSendMoreDisconnect = true;
        writeEventInfo("Please restart Tracer");
        return;
    } 
    */
    writeEventInfo("Wait for connection to ... (4)");
    currentRequest = "StartTraceEvents";
    strURL = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=" + strDebugRequest + connectionID
    			+ "&pzDebugConnection=" + connectionID
    			+ "&pySessionType=" + pySessionType +"&pzNodeID=" + nodeID;

    // If this is a rules watch, send along the inskey
    if (pySessionType=="RULEWATCH") {
    	strURL = strURL + "&pyWatchInsKey=" + escape(pyWatchInsKey) + "&pyWatchClassName=" + escape(pyWatchClassName);
    	gMessageHeader = "<span>RULEWATCH:<br/>"+pyWatchInsKey+"</span>";
    	writeSaneEventMessage("");
    }

    strLocation = strURL;
    strLocationF = strLocation + "&pzForceDisconnect=Y";
    TRACE_EVENT_RESPOND = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=autocontinue&pzCommandSession=" + connectionID + "&pzDebugConnection=" + connectionID + "&pySessionType=" + pySessionType +"&pzNodeID=" + nodeID;
    TRACE_EVENT_LOCATION = strLocation;
    TRACE_EVENT_LOCATION_WITHFORCE = strLocationF;

    nNextFunction = FUNC_MAKETRACEREQUEST;
    xmlTraceEvent.onreadystatechange=checkIfFunctionReady;
    try
    {
        // send request to server
        var loadResult = xmlTraceEvent.load(TRACE_EVENT_LOCATION);
        if (!loadResult)
          alert("Issue loading: startTraceEvent");
    }
    catch (exception)
    {
        alert("Tracer - PegaRULES - startTraceEvent(): Exception!!! " + TRACE_EVENT_LOCATION);
        return;
    }
}

function makeTraceRequest()
{
    if (xmlTraceEvent && xmlTraceEvent.documentElement && xmlTraceEvent.documentElement.nodeName == "pagedata") {
      alert("pagedata");
      var lclQueueType = xmlTraceEvent.documentElement.getAttribute("queueType");
      if (lclQueueType) {
        if (queueType != "" && queueType != lclQueueType) {
	  if (lclQueueType == "memory") {
	    alert("The remote server has switched to a memory based queue. You will only be able to save events that are visible in your tracer window.");
	  } else if (lclQueueType == "header") {
	    alert("The remote server has swithced to a file based queue. You will be only be able to save events that occur after this point.");
          }
	}
        queueType = lclQueueType;
      }
    }

    //var currentTime = currentTimeMillis();
    //gTimeSpent += " | before call settings at " + currentTime + " : " + (currentTime-gPrevTime);
    //gPrevTime = currentTime;
    writeEventInfo("Wait for connection to ... (5)");
    TRACE_EVENT_LOCATION = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=TraceApp&pzMaxEvents="+gMaxEventsPerRequest+"&pzForceDisconnect=Y&pzCommandSession=" + connectionID + "&pzDebugConnection=" + connectionID + "&pySessionType=" + pySessionType + "&pzNodeID=" + nodeID;
    connectionStatus = true;
    // send a Trace request
    nNextFunction = FUNC_LETCHECKOPTIONS;
    //nNextFunction = FUNC_FIRSTCALLTRACEEVENT;
    xmlTraceEvent.onreadystatechange=checkIfFunctionReady;
    //gTimeSpent += " || before xmlTraceEvent.load(" + TRACE_EVENT_LOCATION + ")";
    try
    {
        var loadResult = xmlTraceEvent.load(TRACE_EVENT_LOCATION);
        if (!loadResult)
          alert("Issue loading: makeTraceRequest");
    }
    catch (exception)
    {
        alert("Tracer - PegaRULES - makeTraceRequest(): Exception");
        return;
    }
    //currentTime = currentTimeMillis();
    //gTimeSpent += " || after xmlTraceEvent.load(" + TRACE_EVENT_LOCATION + "): " + (currentTime - gPrevTime) + " || ";
}

function firstCallTraceEvent()
{
    gReadyState = true;
    writeEventInfo("Tracing...");
    //currentTime = currentTimeMillis();
    //gTimeSpent += " | firstCallTraceEvent() at " + currentTime + " : " + (currentTime - gPrevTime);
    //gTimeSpent += " | total wait time = " + (currentTime - gStartTime);
    gDebugMessages = true;
    gReadyForMessage = true;
    gNumOfTrials = 0;
    gToolbarEnabled = false;
    //writeEventMessage(gTimeSpent);
    callCheckFormReady();
    //enableToolbar(3);
    //traceEvent();
}

function callCheckFormReady()
{
    gCheckFormReadyID = window.setInterval("checkFormReady()", TRACE_EVENT_MILLISECS);
}

function callTraceEvent()
{
    nNextFunction = FUNC_CALLTRACEEVENT;
    var strURL = "";
    strURL = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=Trace&pzForceDisconnect=N&pzCommandSession=" + connectionID + "&pzDebugConnection=" + connectionID + "&pySessionType=" + pySessionType + "&pzNodeID=" + nodeID;
    xmlTraceEvent.onreadystatechange=checkIfFunctionReady;
    try
    {
        var loadResult = xmlTraceEvent.load(strURL);
        if (!loadResult)
          alert("Issue loading: callTraceEvent");
    }
    catch (exception)
    {
        alert("Tracer - PegaRULES - firstCallTraceEvent(): Exception");
        return;
    }
}

function letCheckOptions()
{
    //var currentTime = currentTimeMillis();
    //gTimeSpent += " | wait for MakeTraceRequest(): " + (currentTime - gPrevTime);
    //gPrevTime = currentTime;
    writeEventInfo("Wait for connection to ... (6)");
    //writeEventMessage("Done makeTraceRequest() " + TRACE_EVENT_LOCATION);
    if (!bAlreadyGetTraceOptionCookies)
    {
        bAlreadyGetTraceOptionCookies = true;
        var bRet = checkTraceOptions();
        return;
    }
    //currentTime = currentTimeMillis();
    //gTimeSpent += " | before call TraceEvent(): " + (currentTime - gPrevTime);
    //alert(gTimeSpent);
    //writeEventMessage(gTimeSpent);

    gReadyState = true;
    //enableToolbar(2);
    //traceEvent();
    firstCallTraceEvent();
}

function openWindowForInput(strDebugRequest, nTop, nLeft, nHeight, nWidth)
{
    if (nTop > 100 || nLeft > 100 || nHeight > 100 || nWidth > 100)
    {
      alert("form dimension must be between 0 and 100");
      return;
    }
    var paramLabel = "";
    var paramInput = "";
    paramLabel = "Please complete:";
    paramInput = "pzDebugRequest=" + strDebugRequest;

    var nWndTop = window.screen.height * nTop / 100;
    var nWndLeft = window.screen.width * nLeft / 100;
    var nWndHeight = window.screen.height * nHeight / 100;
    var nWndWidth = window.screen.width * nWidth / 100;
    var strFeatures = "left="+nWndLeft+",top="+nWndTop+",height="+nWndHeight+",width="+nWndWidth;
    var strURL = "";
    var strForm = "";

    strURL = TRACE_TRACERSERVLET_V3 +  "?pzDebugRequest=" + strDebugRequest + connectionID +"&pzNodeID=" + nodeID;

    objClipboardWnd = window.open(strURL,strForm,"status=yes,toolbar=no,menubar=no,location=no,scrollbars=yes,resizable=yes " + strFeatures);

    if (objClipboardWnd.opener == null)
        objClipboardWnd.opener = self;
    objClipboardWnd.focus();
}

function displayConnectionList()
{
    if (!gConnectionListEnable)
    return;

    var strStatus = objConnectionWnd.defaultStatus;
    var strData = "";
    var nConnections = 4;
    if (strStatus == "Done")
    {
      // we are at the replaced document
      window.clearInterval(displayConnectionID);
      objConnectionWnd.ConnectionList.document.location.replace("/webwb/TraceTest.htm");
    }
}

function establishConnection()
{
    var strStatus = objConnectionWnd.defaultStatus;
    if (strStatus == "Done")
    {
        // get innerHTML of received data and    if it is for <pagedata>
        var strData = objConnectionWnd.document.documentElement.innerHTML;
        var nPos = strData.indexOf("<pagedata>");
        if (nPos < 0)
            nPos = strData.indexOf("<PAGEDATA>");
        if (nPos >= 0)
        {
            // we are at the document containing GetConnectionList's data.
            // we get all connections and store before replacing with another URL.
            writeEventMessage("establishConnection(): strData = " + strData);
            window.clearInterval(doConnectionID);
            var strURL = "/webwb/TraceConnectionList.htm";
            objConnectionWnd.document.location.replace(strURL);
            objConnectionWnd.focus();
            displayConnectionID = window.setInterval("displayConnectionList()", TRACE_EVENT_MILLISECS);
            objConnectionWnd.defaultStatus = "";
        }
    }
}

function doConnection(strDebugRequest, nTop, nLeft, nHeight, nWidth)
{
    if (nTop > 100 || nLeft > 100 || nHeight > 100 || nWidth > 100)
    {
      alert("form dimension must be between 0 and 100");
      return;
    }
    var nWndTop = window.screen.height * nTop / 100;
    var nWndLeft = window.screen.width * nLeft / 100;
    var nWndHeight = window.screen.height * nHeight / 100;
    var nWndWidth = window.screen.width * nWidth / 100;
    var strFeatures = "left="+nWndLeft+",top="+nWndTop+",height="+nWndHeight+",width="+nWndWidth;
    var strURL = "";
    strURL = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=" + strDebugRequest;
    var strForm = "";
    objConnectionWnd = window.open(strURL,strForm,"status=yes,toolbar=no,menubar=no,location=no,scrollbars=yes,resizable=yes " + strFeatures);
    if (objConnectionWnd.opener == null)
        objConnectionWnd.opener = self;
    objConnectionWnd.focus();
    doConnectionID = window.setInterval("establishConnection()", TRACE_EVENT_MILLISECS);
}

function checkCmdStatus()
{
    var retValue = false;

    var response = objCommandWnd.document.documentElement.innerHTML;
    //alert("response from make breakpoint = " + response);
    if (response != null)
    {
        var nPos = response.indexOf("<CMDSTATUS>");
        if (nPos > 0)
        {
            var nEndPos = response.indexOf("</CMDSTATUS>");
            if (nEndPos > 0)
            {
                if ((response.substring(nPos+11, nEndPos) == "success") || (response.substring(nPos+11, nEndPos) == "SUCCESS"))
                    retValue = true;
            }
        }
    }
    return retValue;
}

function checkTraceOptions()
{
    //writeEventInfo("Checking Trace Options");
    var strPostData = "";

    bAlreadyFoundTraceOptions = true;

    //send Option settings to server
    strPostData = "pzDebugRequest=settings&pzSetCmd=SetOptions&";
    strPostData = strPostData + "pzOptTraceClassLoad=" + traceDebugOptionArray[nOptTraceClassLoad] + "&" + "pzOptTraceException=";
    strPostData = strPostData + traceDebugOptionArray[nOptTraceException] + "&" + "pzOptTraceJContextBegin=" 
                    + traceDebugOptionArray[nOptTraceJContextBegin];
    strPostData = strPostData + "&" + "pzOptTraceActivityBegin=" + traceDebugOptionArray[nOptTraceActivityBegin] + "&";
    strPostData = strPostData + "pzOptTraceActivityEnd=" + traceDebugOptionArray[nOptTraceActivityEnd] + "&" + "pzOptTraceStepBegin=";
    strPostData = strPostData + traceDebugOptionArray[nOptTraceStepBegin] + "&" + "pzOptTraceStepEnd=" + traceDebugOptionArray[nOptTraceStepEnd];
    strPostData = strPostData + "&" + "pzOptTraceWhenBegin=" + traceDebugOptionArray[nOptTraceWhenBegin] + "&" + "pzOptTraceWhenEnd=";
    strPostData = strPostData + traceDebugOptionArray[nOptTraceWhenEnd] + "&" + "pzOptTraceInputEditBegin=";
    strPostData = strPostData + traceDebugOptionArray[nOptTraceInputEditBegin] + "&" + "pzOptTraceInputEditEnd=";
    strPostData = strPostData + traceDebugOptionArray[nOptTraceInputEditEnd] + "&" + "pzOptTraceModelBegin=";
    strPostData = strPostData + traceDebugOptionArray[nOptTraceModelBegin] + "&" + "pzOptTraceModelEnd=";
    strPostData = strPostData + traceDebugOptionArray[nOptTraceModelEnd] + "&" + "pzOptExceptionBreak=" + traceDebugOptionArray[nOptExceptionBreak];
    strPostData = strPostData + "&" + "pzOptStatusFailBreak=" + traceDebugOptionArray[nOptStatusFailBreak] + "&" + "pzOptStatusWarnBreak=";
    strPostData = strPostData + traceDebugOptionArray[nOptStatusWarnBreak];
    strPostData = strPostData + "&pzOptTraceAccessDenied=" + traceDebugOptionArray[nOptTraceAccessDenied];
    strPostData = strPostData + "&pzOptExpandJavaPage=" + traceDebugOptionArray[nOptExpandJavaPage];
    strPostData = strPostData + "&pzOptAbbreviateEvents=" + traceDebugOptionArray[nOptAbbreviateEvents];
    strPostData = strPostData + "&pzOptCollectLocalVars=" + traceDebugOptionArray[nOptLocalVariables];
	strPostData = strPostData + "&pzOptTraceActionBegin=" + traceDebugOptionArray[nOptTraceActionBegin];
	strPostData = strPostData + "&pzOptTraceActionEnd=" + traceDebugOptionArray[nOptTraceActionEnd];

    //add event types
    strPostData += pyEventTypesList;

    //Add Rulesets
    strPostData += pyRuleSetsList;

	// Add PageNames
	strPostData += "&pzOptSetPageNames=" + pyPageNameList;

    //add connectionID
    strPostData = strPostData + "&pzDebugConnection=";

    // add sessionType
    strPostData = strPostData + "&pySessionType=" + pySessionType;

    sendTraceOptionsViaPost(strPostData, CALL_TRACEEVENT);
    //traceEvent();
    return true;
}

// date - any instance of the Date object
// * hand all instances of the Date object to this function for "repairs"
function fixDate(date) {
    var base = new Date(0);
    var skew = base.getTime();
    if (skew > 0)
        date.setTime(date.getTime() - skew);
}

function setReadyState(value)
{
    //alert("setReadyState: " + value);
    gReadyState = value;
    if(value==false) 
      writeEventInfo("Waiting...");
}	

function getWebWBDir()
{
    return gURLServer;
}

function checkFormReady()
{
    var bReady = false;
    var form = parent.MenuRow.document;
    if (form == "undefined" || form == "" || form == null) {
        if (gNumOfTrials > TRACE_NUMBER_OF_TRIALS)
        {
            window.clearInterval(gCheckFormReadyID);
            alert("Unable to check if the form was ready. Please restart Tracer.");
            return;
        }
        gNumOfTrials += 1;
    }
    else {
        window.clearInterval(gCheckFormReadyID);
        enableToolbar(3);
        traceEvent();
        gNumOfTrials=0;
    }
}

function checkIfStopMode()
{
    var bRetVal = false;

    if (gTotallyStop)
        return true; // totally stop

    if (bStopAtBreakpoint || gOpenWatchVar)
    {
        bRetVal = true;
        //parent.status = "Waiting...";
    }

    return bRetVal;
}

function checkTracerPaused()
{
    var bRet = false;

    if (gTracerPaused)
        bRet = true;

    return bRet;
}

function closeAllDialogs()
{
    // close connectionList
    if (objRequestorWnd != null)
    {
        if (!objRequestorWnd.closed)
            objRequestorWnd.close();
    }
    // close option-settings window
    if (objTraceOptionWnd != null)
    {
        if (!objTraceOptionWnd.closed)
            objTraceOptionWnd.close();
    }
    // close breakpoint window
    if (objBreakpointWnd != null)
    {
        if (!objBreakpointWnd.closed)
            objBreakpointWnd.close();
    }
    breakpointArray = new Array();
    numOfBreakpoints = 0;

    // close WatchVar window
    if (objWatchVarWnd  != null)
    {
        if (!objWatchVarWnd.closed)
            objWatchVarWnd.close();
    }
    watchVarArray = new Array();
    numOfWatchVars = 0;
}

</script>