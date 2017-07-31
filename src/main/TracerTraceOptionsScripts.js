<script language="JavaScript">

var strRequestorURIoptions = "{pxThread.pxReqURI Normal}";
  
{LITERAL}

var intervalID = 0;

function objRuleSet(RuleSet, Enabled)
{
	this.RuleSet = RuleSet;
	this.Enabled = Enabled;
}

// Called before displaying the options window after options button
function setTraceOptions(readyState)
{
	//writeSaneEventMessage("DEBUG: TracerTraceOptionsScripts: setTraceOptions");

	if (checkIfStopMode())
		return;	// Do nothing when Tracer is in STOP mode

	if (checkTracerPaused()){
		//Don't disable settings even if tracer is paused. BUG-107837
		//return; // Do nothing
	}

	if (!gTraceOptionsAvailable)
		return;

	if (!readyState)
		return;

	setReadyState(false);	// set to not ready.

	if (!bAlreadyFoundTraceOptions)
	{
		// get Trace options.
		var strMsg = "Download Trace options of the current tracer application ...";
		writeEventInfo(strMsg);
		var strRequest = "";
		strRequest = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=settings&pzSetCmd=GetOptions&pzDebugConnection=" + connectionID +"&pzNodeID=" + nodeID;

		nNextCommandFunction = FUNC2_GETTRACEOPTIONS;
		xmlCommand.onreadystatechange=checkIfCommandFunctionReady;
		try
		{
			// send request to server via xmlCommand object
			var loadResult = xmlCommand.load(strRequest);
		}
		catch (exception)
		{
			alert("PegaRULESTracer - setTraceOptions(): Exception!!!");
			return;
		}
	}
	else
	{
		displayTraceOptionsWindow();
	}
}

function getTraceOptions()
{
	//writeSaneEventMessage("DEBUG: TracerTraceOptionsScripts getTraceOptions");
	var elemList = xmlCommand.getElementsByTagName("OptTraceClassLoad");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceClassLoad] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptTraceException");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceException] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptTraceJContextBegin");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceJContextBegin] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptTraceActivityBegin");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceActivityBegin] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptTraceActivityEnd");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceActivityEnd] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptTraceStepBegin");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceStepBegin] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptTraceStepEnd");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceStepEnd] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptTraceWhenBegin");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceWhenBegin] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptTraceWhenEnd");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceWhenEnd] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptTraceInputEditBegin");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceInputEditBegin] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptTraceInputEditEnd");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceInputEditEnd] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptTraceModelBegin");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceModelBegin] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptTraceModelEnd");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptTraceModelEnd] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptExceptionBreak");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptExceptionBreak] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptStatusFailBreak");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptStatusFailBreak] = elemList.item(0).text;
	}

	elemList = xmlCommand.getElementsByTagName("OptStatusWarnBreak");
	if (elemList.length > 0)
	{
		traceDebugOptionArray[nOptStatusWarnBreak] = elemList.item(0).text;
	}

	// Get RuleSets' settings
	elemList = xmlCommand.getElementsByTagName("OptTraceRuleSets");
	if (elemList.length > 0)
	{
		// Parse the string to get RuleSet settings
		var RuleSets = "";
		var nPos = -1;
		var nIdx = 0;
		var aRuleSetString;
		var aRuleSet;
		var aRuleSetEnabled;
		RuleSets = elemList.item(0).text;
		nPos = RuleSets.indexOf(";");
		while (nPos >= 0)
		{
			aRuleSetString = RuleSets.substring(0, nPos);
			RuleSets = RuleSets.substring(nPos+1,RuleSets.length);
			nPos = aRuleSetString.indexOf("|");
			if (nPos >= 0)
			{
				aRuleSet = aRuleSetString.substring(0, nPos);
				aRuleSetEnabled = (aRuleSetString.substring(nPos+1,aRuleSetString.length) == "Y")?true:false;
				// Setting
				ruleSetsArray[nIdx] = new objRuleSet(aRuleSet, aRuleSetEnabled);
				nIdx += 1;
			}
			nPos = RuleSets.indexOf(";");
		}
	}
	displayTraceOptionsWindow();
	setReadyState(true);
}


// called after user selects teh options button and before options window displayed
function displayTraceOptionsWindow()
{
	//writeSaneEventMessage("DEBUG: TracerTraceOptionsScripts displayTraceOptionsWindow");

	if (objTraceOptionWnd != null)
	{
		if (!objTraceOptionWnd.closed)
		{
			objTraceOptionWnd.focus();
			setReadyState(true);
			return;
		}
	}

	var nHeight = 560;
	var nWidth = 560;
	var nTop = (window.screen.height / 2) - (nHeight / 2);
	var nLeft = (window.screen.width / 2) - (nWidth / 2);

	var strFeatures = "left="+nLeft+",top="+nTop+",height="+nHeight+",width="+nWidth;
	//In order to stop tracing Tracer itself, use record-built-in script in openOptionsDialog()
	var strURL = strRequestorURIoptions + "?pyStream=TraceOptionsDialog&pyPrimaryPageName=" + tracerSettingsDataPageName;
	//var strURL = "";
	var strForm = "";
	objTraceOptionWnd = window.open(strURL,strForm,"status=no,toolbar=no,menubar=no,location=no,scrollbars=no,resizable=yes " + strFeatures);
	if (objTraceOptionWnd.opener == null)
		objTraceOptionWnd.opener = self;
	//openOptionsDialog();
	objTraceOptionWnd.focus();
}


function getTraceOptionByIndex(nIndex)
{
	return unescape(traceDebugOptionArray[nIndex]);
}

function setTraceOptionByIndex(nIndex, strValue)
{
	traceDebugOptionArray[nIndex] = strValue;
}

function sendTraceTemporaryStop(opt)
{
	//writeSaneEventMessage("DEBUG: TracerTraceOptionsScripts: sendTraceTemporaryStop");

	var pzOptTraceTempoarayYesNo = (opt)? "Y" : "N";
	var strRequest = "";
	var xmlTempCommand = null;

	xmlTempCommand = new XMLDomControl('Microsoft.XMLDOM');
	strRequest = TRACE_TRACERSERVLET_V3 + "?pzDebugRequest=settings&pzSetCmd=SetTemporaryStop" +
		"&pzOptTraceTemporaryStop=" + pzOptTraceTempoarayYesNo + "&pzDebugConnection=" + connectionID +"&pzNodeID=" + nodeID;

	if (xmlTempCommand == null)
	{
		alert("PegaRULESTracer - sendTraceTemporaryStop(): xmlTempCommand is not allocated!!!");
		return;
	}

	xmlTempCommand.async = false;
	try
	{
		var loadResult = xmlTempCommand.load(strRequest);
	}
	catch (exception)
	{
		alert("PegaRULESTracer - sendTraceTemporaryStop() via xmlTempCommand DOM: Exception!!!");
	}
	delete xmlTempCommand;
}

function identifyIfPageIsParamDataPage(strPostData, callCommand){
	//writeSaneEventMessage("DEBUG: TracerTraceOptionsScripts: sendTraceOptionsViaPost " + callCommand);
	var dataPgRet = "false";
	var strURL = TRACE_TRACERSERVLET_V3;
	// writeEventInfo("Send Trace Options to server ..." + strPostData.length);
	if (callCommand == CALL_TRACEEVENT) {
		nNextFunction = FUNC_FIRSTCALLTRACEEVENT;
	} else {
		nNextCommandFunction = FUNC2_NOTATALL;
	}

	try
	{
		strPostData = strPostData + "&pzDebugConnection=" + connectionID +"&pzNodeID=" + nodeID;
		var xmlHTTP;
		if (window.XMLHttpRequest){
			xmlHTTP = new window.XMLHttpRequest();
			xmlHTTP.Open = xmlHTTP.open;
			xmlHTTP.Send = xmlHTTP.send;
		} else{
			xmlHTTP = new ActiveXObject("Microsoft.XMLHTTP");
		}
		xmlHTTP.Open("POST", strURL, false);

		xmlHTTP.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");//Bug-13547

		xmlHTTP.Send(strPostData);
		var strReturn = xmlHTTP.responseText;
		/* Create the DOM object. */
	  	var objDoc = returnXMLDOMObject();
		if(window.DOMParser){
			var parser = new DOMParser();
			objDoc = parser.parseFromString(strReturn,"text/xml");
		}
		else {
			objDoc.loadXML(strReturn);
		}
		var response = objDoc.getElementsByTagName("CmdResponse");
		if(response[0] && (response[0].textContent || response[0].text))
			dataPgRet = response[0].textContent ? response[0].textContent : response[0].text;
		return dataPgRet;
	}
	catch (e)
	{
		alert("PegaRULESTracer - identifyIfPageIsParamDataPage(): Exception: " + e.name);
		return dataPgRet;
	}

}

function returnXMLDOMObject(){
	var _rss ;
	if (window.ActiveXObject)
	{
	   _rss=new ActiveXObject("microsoft.xmldom"); 
	}
	else if(document.implementation && document.implementation.createDocument)
	{
	  _rss=document.implementation.createDocument("","",null);
	}
	return _rss;
}

// Called when tracer first started to send options to tracer session
// Called after clicking "OK" on options window to send updated options to tracer session
function sendTraceOptionsViaPost(strPostData, callCommand)
{
	//writeSaneEventMessage("DEBUG: TracerTraceOptionsScripts: sendTraceOptionsViaPost " + callCommand);

	var strURL = TRACE_TRACERSERVLET_V3;
	// writeEventInfo("Send Trace Options to server ..." + strPostData.length);
	if (callCommand == CALL_TRACEEVENT) {
		nNextFunction = FUNC_FIRSTCALLTRACEEVENT;
	} else {
		nNextCommandFunction = FUNC2_NOTATALL;
	}

	try
	{

		if (connectionID!="")//BUG-252557
        {
			strPostData = strPostData + "&pzDebugConnection=" + connectionID +"&pzNodeID=" + nodeID;
			//alert("sendTraceOptionsViaPost: strURL : "+ strURL);
			//alert("sendTraceOptionsViaPost: strPostData: "+ strPostData);
			var xmlHTTP;
			if (window.XMLHttpRequest){
				xmlHTTP = new window.XMLHttpRequest();
				xmlHTTP.Open = xmlHTTP.open;
				xmlHTTP.Send = xmlHTTP.send;
			} else{
				xmlHTTP = new ActiveXObject("Microsoft.XMLHTTP");
			}
			xmlHTTP.Open("POST", strURL, false);

			xmlHTTP.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");//Bug-13547

			xmlHTTP.Send(strPostData);
			//xmlTraceEvent = xmlHTTP.responseXML;
			xmlTraceEvent.xml = xmlHTTP.responseXML;
			xmlTraceEvent.readyState = 4;
		}
		checkIfFunctionReady();
	}
	catch (e)
	{
		alert("PegaRULESTracer - sendTraceOptionsViaPost(): Exception: " + e.name);
		return;
	}
}

function initTraceOptions(objWnd)
{
	//writeSaneEventMessage("DEBUG: TracerTraceOptionsScripts initTraceOptions");

	if (getTraceOptionByIndex(0) == "Y")
		objWnd.document.body.all("pyTraceClassLoad").checked = true;
	else
		objWnd.document.body.all("pyTraceClassLoad").checked = false;

	if (getTraceOptionByIndex(1) == "Y")
		objWnd.document.body.all("pyTraceException").checked = true;
	else
	    objWnd.document.body.all("pyTraceException").checked = false;

	//if (opener.getTraceOptionByIndex(2) == "Y")
	//    document.body.all("pyTraceJContextBegin").checked = true;
	//else
	//    document.body.all("pyTraceJContextBegin").checked = false;

	if (getTraceOptionByIndex(3) == "Y")
		objWnd.document.body.all("pyTraceActivityBegin").checked = true;
	else
		objWnd.document.body.all("pyTraceActivityBegin").checked = false;

	if (getTraceOptionByIndex(4) == "Y")
		objWnd.document.body.all("pyTraceActivityEnd").checked = true;
	else
		objWnd.document.body.all("pyTraceActivityEnd").checked = false;

	if (getTraceOptionByIndex(5) == "Y")
		objWnd.document.body.all("pyTraceStepBegin").checked = true;
	else
		objWnd.document.body.all("pyTraceStepBegin").checked = false;

	if (getTraceOptionByIndex(6) == "Y")
		objWnd.document.body.all("pyTraceStepEnd").checked = true;
	else
		objWnd.document.body.all("pyTraceStepEnd").checked = false;

	if (getTraceOptionByIndex(7) == "Y")
		objWnd.document.body.all("pyTraceWhenBegin").checked = true;
	else
		objWnd.document.body.all("pyTraceWhenBegin").checked = false;

	if (getTraceOptionByIndex(8) == "Y")
		objWnd.document.body.all("pyTraceWhenEnd").checked = true;
	else
		objWnd.document.body.all("pyTraceWhenEnd").checked = false;

	if (getTraceOptionByIndex(9) == "Y")
		objWnd.document.body.all("pyTraceInputEditBegin").checked = true;
	else
		objWnd.document.body.all("pyTraceInputEditBegin").checked = false;

	if (getTraceOptionByIndex(10) == "Y")
		objWnd.document.body.all("pyTraceInputEditEnd").checked = true;
	else
		objWnd.document.body.all("pyTraceInputEditEnd").checked = false;

	if (getTraceOptionByIndex(11) == "Y")
		objWnd.document.body.all("pyTraceModelBegin").checked = true;
	else
		objWnd.document.body.all("pyTraceModelBegin").checked = false;

	if (getTraceOptionByIndex(12) == "Y")
		objWnd.document.body.all("pyTraceModelEnd").checked = true;
	else
		objWnd.document.body.all("pyTraceModelEnd").checked = false;
		
	if (getTraceOptionByIndex(21) == "Y")
		objWnd.document.body.all("pyTraceActionBegin").checked = true;
	else
		objWnd.document.body.all("pyTraceActionBegin").checked = false;
		
	if (getTraceOptionByIndex(22) == "Y")
		objWnd.document.body.all("pyTraceActionEnd").checked = true;
	else
		objWnd.document.body.all("pyTraceActionEnd").checked = false;

	if (getTraceOptionByIndex(13) == "Y")
		objWnd.document.body.all("pyExceptionBreak").checked = true;
	else
		objWnd.document.body.all("pyExceptionBreak").checked = false;

	if (getTraceOptionByIndex(14) == "Y")
		objWnd.document.body.all("pyStatusFailBreak").checked = true;
	else
		objWnd.document.body.all("pyStatusFailBreak").checked = false;

	if (getTraceOptionByIndex(15) == "Y")
		objWnd.document.body.all("pyStatusWarnBreak").checked = true;
	else
		objWnd.document.body.all("pyStatusWarnBreak").checked = false;

	if (getTraceOptionsByIndex(16) == "Y")
		objWnd.document.body.all("pyTraceAccessDenied").checked = true;
	else
		objWnd.document.body.all("pyTraceAccessDenied").checked = false;

	var nIndex = 0;
	var RuleSetOptionID = "";
	var aRuleSet = '';
	var nPos = -1;
	for (var nIdx = 0; nIdx < gRuleSetsNums; nIdx++)
	{
		nIndex = nIdx + 1;
		RuleSetOptionID = "pzRuleSetOption" + nIndex;
		aRuleSet = ruleSetsArray[nIdx].RuleSet;
		nPos = aRuleSet.indexOf("@pegasystems.com");
		if (nPos < 0)
		{
			if (ruleSetsArray[nIdx].Enabled)
				objTraceOptionWnd.document.body.all(RuleSetOptionID).checked = true;
			else
				objTraceOptionWnd.document.body.all(RuleSetOptionID).checked = false;
		}
		else
		{
			ruleSetsArray[nIdx].Enabled = true;
		}
	}

	this.defaultStatus = "Done";
	setReadyState(true);
}

function sndTraceOptions(objWnd)
{
	//writeSaneEventMessage("DEBUG: TracerTraceOptionsScripts sndTraceOptions");

	/* This function no longer appears to be called (but I'm not entirely
	positive).  The options window that pops up is actually a
	Rule-Obj-HTML -- @baseclass.TraceOptionsDialog. It has a function called
	sendTraceOptions(...).
	--dorid */

	var pzOptTraceClassLoadYesNo = objTraceOptionWnd.document.body.all("pyTraceClassLoad").checked ? "Y" : "N";
	setTraceOptionByIndex(0,pzOptTraceClassLoadYesNo);
	var pzOptTraceExceptionYesNo = objTraceOptionWnd.document.body.all("pyTraceException").checked ? "Y" : "N";
	setTraceOptionByIndex(1,pzOptTraceExceptionYesNo);

	var pzOptTraceJContextBeginYesNo = "N"; //document.body.all("pyTraceJContextBegin").checked ? "Y" : "N";
	setTraceOptionByIndex(2,pzOptTraceJContextBeginYesNo);

	var pzOptTraceActivityBeginYesNo = objTraceOptionWnd.document.body.all("pyTraceActivityBegin").checked ? "Y" : "N";
	setTraceOptionByIndex(3,pzOptTraceActivityBeginYesNo);

	var pzOptTraceActivityEndYesNo = objTraceOptionWnd.document.body.all("pyTraceActivityEnd").checked ? "Y" : "N";
	setTraceOptionByIndex(4,pzOptTraceActivityEndYesNo);

	var pzOptTraceStepBeginYesNo = objTraceOptionWnd.document.body.all("pyTraceStepBegin").checked ? "Y" : "N";
	setTraceOptionByIndex(5,pzOptTraceStepBeginYesNo);

	var pzOptTraceStepEndYesNo = objTraceOptionWnd.document.body.all("pyTraceStepEnd").checked ? "Y" : "N";
	setTraceOptionByIndex(6,pzOptTraceStepEndYesNo);

	var pzOptTraceWhenBeginYesNo = objTraceOptionWnd.document.body.all("pyTraceWhenBegin").checked ? "Y" : "N";
	setTraceOptionByIndex(7,pzOptTraceWhenBeginYesNo);

	var pzOptTraceWhenEndYesNo = objTraceOptionWnd.document.body.all("pyTraceWhenEnd").checked ? "Y" : "N";
	setTraceOptionByIndex(8,pzOptTraceWhenEndYesNo);

	var pzOptTraceInputEditBeginYesNo = objTraceOptionWnd.document.body.all("pyTraceInputEditBegin").checked ? "Y" : "N";
	setTraceOptionByIndex(9,pzOptTraceInputEditBeginYesNo);

	var pzOptTraceInputEditEndYesNo = objTraceOptionWnd.document.body.all("pyTraceInputEditEnd").checked ? "Y" : "N";
	setTraceOptionByIndex(10,pzOptTraceInputEditEndYesNo);

	var pzOptTraceModelBeginYesNo = objTraceOptionWnd.document.body.all("pyTraceModelBegin").checked ? "Y" : "N";
	setTraceOptionByIndex(11,pzOptTraceModelBeginYesNo);

	var pzOptTraceModelEndYesNo = objTraceOptionWnd.document.body.all("pyTraceModelEnd").checked ? "Y" : "N";
	setTraceOptionByIndex(12,pzOptTraceModelEndYesNo);

	var pzOptTraceActionBeginYesNo = objTraceOptionWnd.document.body.all("pyTraceActionBegin").checked ? "Y" : "N";
	setTraceOptionByIndex(21,pzOptTraceActionBeginYesNo);
	
	var pzOptTraceActionEndYesNo = objTraceOptionWnd.document.body.all("pyTraceActionEnd").checked ? "Y" : "N";
	setTraceOptionByIndex(22,pzOptTraceActionEndYesNo);

	var pzOptExceptionBreakYesNo = objTraceOptionWnd.document.body.all("pyExceptionBreak").checked ? "Y" : "N";
	setTraceOptionByIndex(13,pzOptExceptionBreakYesNo);

	var pzOptStatusFailBreakYesNo = objTraceOptionWnd.document.body.all("pyStatusFailBreak").checked ? "Y" : "N";
	setTraceOptionByIndex(14,pzOptStatusFailBreakYesNo);

	var pzOptStatusWarnBreakYesNo = objTraceOptionWnd.document.body.all("pyStatusWarnBreak").checked ? "Y" : "N";
	setTraceOptionByIndex(15,pzOptStatusWarnBreakYesNo);

	var pzOptTraceAccessDeniedYesNo = objTraceOptionWnd.document.body.all("pyTraceAccessDenied").checked ? "Y" : "N";
	setTraceOptionByIndex(16,pzOptTraceAccessDeniedYesNo);

	var pzDebugConnectionID = objTraceOptionWnd.document.all("pzConnectionID").value;

	//alert("RulseSet1 = " + objTraceOptionWnd.document.all("pzRuleSet1").name);
	//update RuleSetOption checkbox values into their array
	var RuleSetOptionID = "";
	var aRuleSet = "";
	var nPos = -1;
	var nIndex = 0;
	for (var nIdx = 0; nIdx < gRuleSetsNums; nIdx++)
	{
		nIndex = nIdx + 1;
		RuleSetOptionID = "pzRuleSetOption" + nIndex;
		aRuleSet = ruleSetsArray[nIdx].RuleSet;
		nPos = aRuleSet.indexOf("@pegasystems.com");
		if (nPos < 0)
		{
			ruleSetsArray[nIdx].Enabled = objTraceOptionWnd.document.body.all(RuleSetOptionID).checked;
		}
	}

	var strURL = "";
	strURL =  "pzDebugRequest=settings&pzSetCmd=SetOptions";
	strURL += "&pzOptTraceClassLoad=" + pzOptTraceClassLoadYesNo;
	strURL += "&pzOptTraceException=" + pzOptTraceExceptionYesNo;
	strURL += "&pzOptTraceJContextBegin=" + pzOptTraceJContextBeginYesNo;
	strURL += "&pzOptTraceActivityBegin=" + pzOptTraceActivityBeginYesNo;
	strURL += "&pzOptTraceActivityEnd=" + pzOptTraceActivityEndYesNo;
	strURL += "&pzOptTraceStepBegin=" + pzOptTraceStepBeginYesNo;
	strURL += "&pzOptTraceStepEnd=" + pzOptTraceStepEndYesNo;
	strURL += "&pzOptTraceWhenBegin=" + pzOptTraceWhenBeginYesNo;
	strURL += "&pzOptTraceWhenEnd=" + pzOptTraceWhenEndYesNo;
	strURL += "&pzOptTraceInputEditBegin=" + pzOptTraceInputEditBeginYesNo;
	strURL += "&pzOptTraceInputEditEnd=" + pzOptTraceInputEditEndYesNo;
	strURL += "&pzOptTraceModelBegin=" + pzOptTraceModelBeginYesNo;
	strURL += "&pzOptTraceModelEnd=" + pzOptTraceModelEndYesNo;
	strURL += "&pzOptTraceActionBegin=" + pzOptTraceActionBeginYesNo;
	strURL += "&pzOptTraceActionEnd=" + pzOptTraceActionEndYesNo;
	strURL += "&pzOptExceptionBreak=" + pzOptExceptionBreakYesNo;
	strURL += "&pzOptStatusFailBreak=" + pzOptStatusFailBreakYesNo;
	strURL += "&pzOptStatusWarnBreak=" + pzOptStatusWarnBreakYesNo;
	strURL += "&pzOptTraceAccessDenied=" + pzOptTraceAccessDeniedYesNo;
    strURL += "&pzNodeID=" + nodeID;
	strURL += "&pzDebugConnection=";

	sendTraceOptions(strURL, 0);
}

function checkStatusForCloseOptionWindow()
{
	//writeSaneEventMessage("DEBUG: TracerTraceOptionsScripts checkStatusForCloseOptionWindow()");

	//String aheader = objTraceOptionWnd.document.all("HEAD").text;
	traceWait(1000);
	if (intervalID > 0)
		clearInterval(intervalID);
	objTraceOptionWnd.close(); // close this window.
}

function openOptionsDialog()
{
	//writeSaneEventMessage("DEBUG: TracerTraceOptionsScripts openOptionsDialog()");

	var strData = "";
	var strList = "";
	strData += "<HTML><HEAD><Title>Trace Options</Title>";
	strData += "</HEAD>";
	strData += "<BODY onload='opener.displayTraceOptions();'>";
	strData += "<FORM>";
	strData += "</FORM>";
	strData += "</BODY>";
	strData += "</HTML>";
	objTraceOptionWnd.document.open();
	objTraceOptionWnd.document.write(strData);
	objTraceOptionWnd.document.close();
}

function doSendAndSubmit(objWnd, oForm)
{
	//writeSaneEventMessage("DEBUG: TracerTraceOptionsScripts doSendAndSubmit()");

	/* This function no longer appears to be called (but I'm not entirely
	positive).  The options window that pops up is actually a
	Rule-Obj-HTML -- @baseclass.TraceOptionsDialog.
	--dorid */

	sndTraceOptions(objWnd);
	oForm.submit();
	window.close(objWnd);
}

function displayTraceOptions()
{
	//writeSaneEventMessage("DEBUG: TracerTraceOptionsScripts displayTraceOptions()");

	/* This function no longer appears to be called.  The options window that
	pops up is actually a Rule-Obj-HTML -- @baseclass.TraceOptionsDialog.
	--dorid */

	var strData = "";
	var strList = "";
	strData += "<HTML><HEAD><Title>Trace Options</Title>";
	strData += "<BODY STYLE=\"margin-left: 5px; margin-top: 5px;\" BGCOLOR=\"#FFFFFF\">";
	strData += "<LINK HREF=\"procomstylesheet.css\" REL=\"STYLESHEET\" TYPE=\"text/css\">";
	strData += "</HEAD>";
	strData += "<BODY STYLE='margin-top: 5px; margin-bottom: 5px; margin-left: 5px; margin-right: 5px;' onload='opener.initTraceOptions(this);'>";
	strData += "<FORM id=form2 name=form2 method='POST' action='{" + strRequestorURI + " pyActivity=Data-TRACERSettings.TracerRuleSetOptionsSave PRIMARY}'>";
	strData += "<CENTER>";
	strData += "<TABLE>";
	strData += "<TR>";
	strData += "<TD ALIGN='LEFT' VALIGN='TOP'><IMG SRC='images/ztracertraceoptions.gif' WIDTH='400' HEIGHT='40' BORDER='0'>";
	strData += "<TABLE CLASS='tableBoxStyle' WIDTH='100%'>";
	strData += "<TR>";
	strData += "<TD><TABLE WIDTH='100%'>";

	strData += "<TR>";
	strData += "<TD ALIGN='LEFT' VALIGN='TOP'>";
	strData += "<H3>Events to Trace</H3>";

	strData += "<TABLE CELLPADDING='0' CELLSPACING='0'>";
	strData += "<TR>";
	strData += "<TD>&nbsp;</TD>";
	strData += "<TD CLASS='dataLabelStyle'>&nbsp;Start?&nbsp;</TD>";
	strData += "<TD CLASS='dataLabelStyle'>&nbsp;End?&nbsp;</TD>";
	strData += "</TR>";
	strData += "<TR>";
	strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;Class Loading;&nbsp;</TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyTraceClassLoad' CLASS='Checkbox'></TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'></TD>";
	strData += "</TR>";
	strData += "<TR>";
	strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;Exception:&nbsp;</TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyTraceException' CLASS='Checkbox'></TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'></TD>";
	strData += "</TR>";
	strData += "<TR>";
	strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;Access Denied:&nbsp;</TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyTraceAccessDenied' CLASS='Checkbox'></TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'></TD>";
	strData += "</TR>";
	strData += "<TR>";
	strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;Activities:&nbsp;</TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyTraceActivityBegin'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX'ID='pyTraceActivityEnd'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "</TR>";
	strData += "<TR>";
	strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;Activity Steps:&nbsp;</TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyTraceStepBegin'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyTraceStepEnd'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "</TR>";
	strData += "<TR>";
	strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;When Blocks:&nbsp;</TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyTraceWhenBegin'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyTraceWhenEnd'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "</TR>";
	strData += "<TR>";
	strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;Input Edit Blocks:&nbsp;</TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyTraceInputEditBegin'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyTraceInputEditEnd'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "</TR>";
	strData += "<TR>";
	strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;Model Blocks:&nbsp;</TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyTraceModelBegin'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyTraceModelEnd'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "</TR>";
	strData += "</TABLE>";
	strData += "</TD>";
	strData += "<TD ALIGN='LEFT' VALIGN='TOP'>";
	strData += "<H3>Break Conditions</H3>";
	strData += "<TABLE CELLPADDING='0' CELLSPACING='0'>";
	strData += "<TR>";
	strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;Exception?&nbsp;</TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyExceptionBreak'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "</TR>";
	strData += "<TR>";
	strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;Fail Status?&nbsp;</TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyStatusFailBreak'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "</TR>";
	strData += "<TR>";
	strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;Warn Status?&nbsp;</TD>";
	strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pyStatusWarnBreak'";
	strData += "CLASS='Checkbox'></TD>";
	strData += "</TR>";
	strData += "</TABLE>";
	strData += "</TD>";
	strData += "</TR>";
	strData += "</TABLE>";
	strData += "</TD>";
	strData += "</TR>";
	strData += "</TABLE>";
	strData += "</TD>";
	strData += "</TR>";
	strData += "<CENTER>";
	strData += "<TABLE>";
	strData += "<TR>";
	strData += "<TD ALIGN='LEFT' VALIGN='TOP' WIDTH='400' HEIGHT='40' BORDER='0'>";
	strData += "<TABLE CLASS='tableBoxStyle' WIDTH='100%'>";
	strData += "<TR>";
	strData += "<TD ALIGN='LEFT'>";
	strData += "<H3>RuleSets</H3>";
	strData += "<TABLE CELLPADDING='0' CELLSPACING='0'>";

	var aRuleSet = '';
	var nPos = -1;
	if (gRuleSetsNums <= 0)
	{
		alert("gRuleSetsNums (1): " + gRuleSetsNums);
		var aTmpRuleSets = gRuleSets;
		var aEnabled = "";
		nPos = aTmpRuleSets.indexOf(";");
		while (nPos >=0)
		{
			aRuleSet = aTmpRuleSets.substring(0,nPos);
			aTmpRuleSets = aTmpRuleSets.substring(nPos+1,gRuleSets.length);
			nPos = aRuleSet.indexOf(":");
			if (nPos >= 0)
			{
				aEnabled = aRuleSet.substring(nPos+1,aRuleSet.length);
				aEnabled = aEnabled.toLowerCase();
				aRuleSet = aRuleSet.substring(0, nPos);
			}
			if (aEnabled == "yes")
				ruleSetsArray[gRuleSetsNums] = new objRuleSet(aRuleSet, true);
			else
				ruleSetsArray[gRuleSetsNums] = new objRuleSet(aRuleSet, false);
			gRuleSetsNums += 1;
			nPos = aRuleSet.indexOf("@pegasystems.com");	// search private ruleset
			if (nPos < 0)
			{
				// display only non-private ruleSet
				strData += "<TR>";
				strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;" + aRuleSet + "&nbsp;</TD>";
				strData += "<INPUT TYPE='hidden' name='" + "param.RuleSet" + gRuleSetsNums + "' value='" + aRuleSet + "' ";
				//strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pzRuleSet" + gRuleSetsNums + "' name='" + aRuleSet +"'";
				strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pzRuleSetOption" + gRuleSetsNums + "' name='param.RuleSetOption" + gRuleSetsNums +"'";
	   			strData += "CLASS='Checkbox'></TD>";
	    		strData += "</TR>";
			}
	    	nPos = aTmpRuleSets .indexOf(";");
		}
	}
	else
	{
		var nIndex = 0;
		alert("gRuleSetsNums = " + gRuleSetsNums);
		for (var nIdx=0; nIdx < gRuleSetsNums; nIdx++)
		{
			nIndex = nIdx + 1;
			aRuleSet = ruleSetsArray[nIdx].RuleSet;
			nPos = aRuleSet.indexOf("@pegasystems.com");
			if (nPos < 0)
			{
				strData += "<TR>";
				strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'>&nbsp;" + aRuleSet + "&nbsp;</TD>";
				strData += "<INPUT TYPE='hidden' ID='pzRuleSet" + nIndex + "' name='" + "param.RuleSet" + nIndex + "' value='" + aRuleSet + "' ";
				strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='CHECKBOX' ID='pzRuleSetOption" + nIndex + "' name='param.RuleSetOption" + nIndex +"'";
	   			strData += "CLASS='Checkbox'></TD>";
	    		strData += "</TR>";
	    	}
	    	else
	    	{
	    		strData += "<TR>";
				strData += "<TD CLASS='dataLabelStyle' NOWRAP='NOWRAP'></TD>";
				strData += "<INPUT TYPE='hidden' ID='pzRuleSet" + nIndex + "' name='" +  "param.RuleSet" + nIndex + "' value='" + aRuleSet + "' ";
				strData += "<TD CLASS='dataStyle' ALIGN='CENTER'><INPUT TYPE='hidden' ID='pzRuleSetOption" + nIndex + "' name='param.RuleSetOption" + nIndex + "' value=Yes";
	   			strData += "CLASS='Checkbox'></TD>";
	    		strData += "</TR>";
	    	}
		}
	}

	//strData += "<TR><TD CLASS='dataStyle' ALIGN='CENTER'> <INPUT TYPE='hidden' ID='pzRuleSetNums' name='param.RuleSetNums' value='" + gRuleSetNums + "'></TD></TR>";

	strData += "</TABLE>";

	strData += "</TD>";
	strData += "</TR>";
	strData += "</TABLE>";
	strData += "</TD>";
	strData += "</TR>";
	strData += "</TABLE>";

	strData += "</TABLE>";
	strData += "<BR>";
	strData += "<TABLE>";
	strData += "<TR>";
	strData += "<TD><INPUT TYPE='BUTTON' NAME='Ok' VALUE='OK' CLASS='littleButton' onclick='opener.doSendAndSubmit(this, this.form);'></TD>";
	strData += "<TD><INPUT TYPE='BUTTON' NAME='Cancel' VALUE='Cancel' onclick='window.close();' ";
	strData += "CLASS='littleButton'></TD>";
	strData += "</TR>";
	strData += "</TABLE>";
	strData += "<input type='text' style='display: none;' id='pzConnectionID'>";
	strData += "</CENTER>";
	strData += "</FORM>";
	strData += "</BODY>";
    strData += "</HTML>";
    objTraceOptionWnd.document.open();
    objTraceOptionWnd.document.write(strData);
    objTraceOptionWnd.document.close();
}

function setEventTypesList(eventTypesList) {
	pyEventTypesList = eventTypesList;
}

function setRuleSetsList(ruleSetsList) {
	pyRuleSetsList = ruleSetsList;
}

function setPageNamesList(pageNamesList){
	pyPageNameList = pageNamesList;
}


{/LITERAL}

</SCRIPT>