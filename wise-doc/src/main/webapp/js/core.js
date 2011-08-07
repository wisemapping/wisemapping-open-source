var core={};core.ColorPicker=function(){this.palette="7x10";
this._palettes={"7x10":[["fff","fcc","fc9","ff9","ffc","9f9","9ff","cff","ccf","fcf"],["ccc","f66","f96","ff6","ff3","6f9","3ff","6ff","99f","f9f"],["c0c0c0","f00","f90","fc6","ff0","3f3","6cc","3cf","66c","c6c"],["999","c00","f60","fc3","fc0","3c0","0cc","36f","63f","c3c"],["666","900","c60","c93","990","090","399","33f","60c","939"],["333","600","930","963","660","060","366","009","339","636"],["000","300","630","633","330","030","033","006","309","303"]],"3x4":[["ffffff","00ff00","008000","0000ff"],["c0c0c0","ffff00","ff00ff","000080"],["808080","ff0000","800080","000000"]]}
};
core.ColorPicker.buildRendering=function(){this.domNode=document.createElement("table");
with(this.domNode){cellPadding="0";
cellSpacing="1";
border="1";
style.backgroundColor="white"
}var colors=this._palettes[this.palette];
for(var i=0;
i<colors.length;
i++){var tr=this.domNode.insertRow(-1);
for(var j=0;
j<colors[i].length;
j++){if(colors[i][j].length==3){colors[i][j]=colors[i][j].replace(/(.)(.)(.)/,"$1$1$2$2$3$3")
}var td=tr.insertCell(-1);
with(td.style){backgroundColor="#"+colors[i][j];
border="1px solid gray";
width=height="15px";
fontSize="1px"
}td.color="#"+colors[i][j];
td.onmouseover=function(e){this.style.borderColor="white"
};
td.onmouseout=function(e){this.style.borderColor="gray"
};
td.innerHTML="&nbsp;"
}}};
core.ColorPicker.onClick=function(e){this.onColorSelect(e.currentTarget.color);
e.currentTarget.style.borderColor="gray"
};
core.ColorPicker.onColorSelect=function(color){};core.Loader={load:function(scriptPath,stylePath,jsFileName){var headElement=document.getElementsByTagName("head");
var htmlDoc=headElement.item(0);
var baseUrl=this.baseUrl(jsFileName);
if(scriptPath&&scriptPath.length>0){for(var i=0;
i<scriptPath.length;
i++){this.includeScriptNode(baseUrl+scriptPath[i])
}}if(stylePath&&stylePath.length>0){for(var i=0;
i<stylePath.length;
i++){this.includeStyleNode(baseUrl+stylePath[i])
}}},baseUrl:function(jsFileName){var headElement=document.getElementsByTagName("head");
var htmlDoc=headElement.item(0);
var headChildren=htmlDoc.childNodes;
var result=null;
for(var i=0;
i<headChildren.length;
i++){var node=headChildren.item(i);
if(node.nodeName&&node.nodeName.toLowerCase()=="script"){var libraryUrl=node.src;
if(libraryUrl.indexOf(jsFileName)!=-1){var index=libraryUrl.lastIndexOf("/");
index=libraryUrl.lastIndexOf("/",index-1);
result=libraryUrl.substring(0,index)
}}}if(result==null){throw"Could not obtain the base url directory."
}return result
},includeScriptNode:function(filename){var html_doc=document.getElementsByTagName("head").item(0);
var js=document.createElement("script");
js.setAttribute("language","javascript");
js.setAttribute("type","text/javascript");
js.setAttribute("src",filename);
html_doc.appendChild(js);
return false
},includeStyleNode:function(filename){var html_doc=document.getElementsByTagName("head").item(0);
var js=document.createElement("link");
js.setAttribute("rel","stylesheet");
js.setAttribute("type","text/css");
js.setAttribute("href",filename);
html_doc.appendChild(js);
return false
}};var Log4js={version:"1.0",applicationStartDate:new Date(),loggers:{},getLogger:function(categoryName){if(!(typeof categoryName=="string")){categoryName="[default]"
}if(!Log4js.loggers[categoryName]){Log4js.loggers[categoryName]=new Log4js.Logger(categoryName)
}return Log4js.loggers[categoryName]
},getDefaultLogger:function(){return Log4js.getLogger("[default]")
},attachEvent:function(element,name,observer){if(element.addEventListener){element.addEventListener(name,observer,false)
}else{if(element.attachEvent){element.attachEvent("on"+name,observer)
}}}};
Log4js.extend=function(destination,source){for(property in source){destination[property]=source[property]
}return destination
};
Log4js.bind=function(fn,object){return function(){return fn.apply(object,arguments)
}
};
Log4js.Level=function(level,levelStr){this.level=level;
this.levelStr=levelStr
};
Log4js.Level.prototype={toLevel:function(sArg,defaultLevel){if(sArg===null){return defaultLevel
}if(typeof sArg=="string"){var s=sArg.toUpperCase();
if(s=="ALL"){return Log4js.Level.ALL
}if(s=="DEBUG"){return Log4js.Level.DEBUG
}if(s=="INFO"){return Log4js.Level.INFO
}if(s=="WARN"){return Log4js.Level.WARN
}if(s=="ERROR"){return Log4js.Level.ERROR
}if(s=="FATAL"){return Log4js.Level.FATAL
}if(s=="OFF"){return Log4js.Level.OFF
}if(s=="TRACE"){return Log4js.Level.TRACE
}return defaultLevel
}else{if(typeof sArg=="number"){switch(sArg){case ALL_INT:return Log4js.Level.ALL;
case DEBUG_INT:return Log4js.Level.DEBUG;
case INFO_INT:return Log4js.Level.INFO;
case WARN_INT:return Log4js.Level.WARN;
case ERROR_INT:return Log4js.Level.ERROR;
case FATAL_INT:return Log4js.Level.FATAL;
case OFF_INT:return Log4js.Level.OFF;
case TRACE_INT:return Log4js.Level.TRACE;
default:return defaultLevel
}}else{return defaultLevel
}}},toString:function(){return this.levelStr
},valueOf:function(){return this.level
}};
Log4js.Level.OFF_INT=Number.MAX_VALUE;
Log4js.Level.FATAL_INT=50000;
Log4js.Level.ERROR_INT=40000;
Log4js.Level.WARN_INT=30000;
Log4js.Level.INFO_INT=20000;
Log4js.Level.DEBUG_INT=10000;
Log4js.Level.TRACE_INT=5000;
Log4js.Level.ALL_INT=Number.MIN_VALUE;
Log4js.Level.OFF=new Log4js.Level(Log4js.Level.OFF_INT,"OFF");
Log4js.Level.FATAL=new Log4js.Level(Log4js.Level.FATAL_INT,"FATAL");
Log4js.Level.ERROR=new Log4js.Level(Log4js.Level.ERROR_INT,"ERROR");
Log4js.Level.WARN=new Log4js.Level(Log4js.Level.WARN_INT,"WARN");
Log4js.Level.INFO=new Log4js.Level(Log4js.Level.INFO_INT,"INFO");
Log4js.Level.DEBUG=new Log4js.Level(Log4js.Level.DEBUG_INT,"DEBUG");
Log4js.Level.TRACE=new Log4js.Level(Log4js.Level.TRACE_INT,"TRACE");
Log4js.Level.ALL=new Log4js.Level(Log4js.Level.ALL_INT,"ALL");
Log4js.CustomEvent=function(){this.listeners=[]
};
Log4js.CustomEvent.prototype={addListener:function(method){this.listeners.push(method)
},removeListener:function(method){var foundIndexes=this.findListenerIndexes(method);
for(var i=0;
i<foundIndexes.length;
i++){this.listeners.splice(foundIndexes[i],1)
}},dispatch:function(handler){for(var i=0;
i<this.listeners.length;
i++){try{this.listeners[i](handler)
}catch(e){log4jsLogger.warn("Could not run the listener "+this.listeners[i]+". \n"+e)
}}},findListenerIndexes:function(method){var indexes=[];
for(var i=0;
i<this.listeners.length;
i++){if(this.listeners[i]==method){indexes.push(i)
}}return indexes
}};
Log4js.LoggingEvent=function(categoryName,level,message,exception,logger){this.startTime=new Date();
this.categoryName=categoryName;
this.message=message;
this.exception=exception;
this.level=level;
this.logger=logger
};
Log4js.LoggingEvent.prototype={getFormattedTimestamp:function(){if(this.logger){return this.logger.getFormattedTimestamp(this.startTime)
}else{return this.startTime.toGMTString()
}}};
Log4js.Logger=function(name){this.loggingEvents=[];
this.appenders=[];
this.category=name||"";
this.level=Log4js.Level.FATAL;
this.dateformat=Log4js.DateFormatter.DEFAULT_DATE_FORMAT;
this.dateformatter=new Log4js.DateFormatter();
this.onlog=new Log4js.CustomEvent();
this.onclear=new Log4js.CustomEvent();
this.appenders.push(new Log4js.Appender(this));
try{window.onerror=this.windowError.bind(this)
}catch(e){}};
Log4js.Logger.prototype={addAppender:function(appender){if(appender instanceof Log4js.Appender){appender.setLogger(this);
this.appenders.push(appender)
}else{throw"Not instance of an Appender: "+appender
}},setAppenders:function(appenders){for(var i=0;
i<this.appenders.length;
i++){this.appenders[i].doClear()
}this.appenders=appenders;
for(var j=0;
j<this.appenders.length;
j++){this.appenders[j].setLogger(this)
}},setLevel:function(level){this.level=level
},log:function(logLevel,message,exception){var loggingEvent=new Log4js.LoggingEvent(this.category,logLevel,message,exception,this);
this.loggingEvents.push(loggingEvent);
this.onlog.dispatch(loggingEvent)
},clear:function(){try{this.loggingEvents=[];
this.onclear.dispatch()
}catch(e){}},isTraceEnabled:function(){if(this.level.valueOf()<=Log4js.Level.TRACE.valueOf()){return true
}return false
},trace:function(message){if(this.isTraceEnabled()){this.log(Log4js.Level.TRACE,message,null)
}},isDebugEnabled:function(){if(this.level.valueOf()<=Log4js.Level.DEBUG.valueOf()){return true
}return false
},debug:function(message){if(this.isDebugEnabled()){this.log(Log4js.Level.DEBUG,message,null)
}},debug:function(message,throwable){if(this.isDebugEnabled()){this.log(Log4js.Level.DEBUG,message,throwable)
}},isInfoEnabled:function(){if(this.level.valueOf()<=Log4js.Level.INFO.valueOf()){return true
}return false
},info:function(message){if(this.isInfoEnabled()){this.log(Log4js.Level.INFO,message,null)
}},info:function(message,throwable){if(this.isInfoEnabled()){this.log(Log4js.Level.INFO,message,throwable)
}},isWarnEnabled:function(){if(this.level.valueOf()<=Log4js.Level.WARN.valueOf()){return true
}return false
},warn:function(message){if(this.isWarnEnabled()){this.log(Log4js.Level.WARN,message,null)
}},warn:function(message,throwable){if(this.isWarnEnabled()){this.log(Log4js.Level.WARN,message,throwable)
}},isErrorEnabled:function(){if(this.level.valueOf()<=Log4js.Level.ERROR.valueOf()){return true
}return false
},error:function(message){if(this.isErrorEnabled()){this.log(Log4js.Level.ERROR,message,null)
}},error:function(message,throwable){if(this.isErrorEnabled()){this.log(Log4js.Level.ERROR,message,throwable)
}},isFatalEnabled:function(){if(this.level.valueOf()<=Log4js.Level.FATAL.valueOf()){return true
}return false
},fatal:function(message){if(this.isFatalEnabled()){this.log(Log4js.Level.FATAL,message,null)
}},fatal:function(message,throwable){if(this.isFatalEnabled()){this.log(Log4js.Level.FATAL,message,throwable)
}},windowError:function(msg,url,line){var message="Error in ("+(url||window.location)+") on line "+line+" with message ("+msg+")";
this.log(Log4js.Level.FATAL,message,null)
},setDateFormat:function(format){this.dateformat=format
},getFormattedTimestamp:function(date){return this.dateformatter.formatDate(date,this.dateformat)
}};
Log4js.Appender=function(){this.logger=null
};
Log4js.Appender.prototype={doAppend:function(loggingEvent){return 
},doClear:function(){return 
},setLayout:function(layout){this.layout=layout
},setLogger:function(logger){logger.onlog.addListener(Log4js.bind(this.doAppend,this));
logger.onclear.addListener(Log4js.bind(this.doClear,this));
this.logger=logger
}};
Log4js.Layout=function(){return 
};
Log4js.Layout.prototype={format:function(loggingEvent){return""
},getContentType:function(){return"text/plain"
},getHeader:function(){return null
},getFooter:function(){return null
},getSeparator:function(){return""
}};
Log4js.ConsoleAppender=function(isInline){this.layout=new Log4js.PatternLayout(Log4js.PatternLayout.TTCC_CONVERSION_PATTERN);
this.inline=isInline;
this.accesskey="d";
this.tagPattern=null;
this.commandHistory=[];
this.commandIndex=0;
this.popupBlocker=false;
this.outputElement=null;
this.docReference=null;
this.winReference=null;
if(this.inline){Log4js.attachEvent(window,"load",Log4js.bind(this.initialize,this))
}};
Log4js.ConsoleAppender.prototype=Log4js.extend(new Log4js.Appender(),{setAccessKey:function(key){this.accesskey=key
},initialize:function(){if(!this.inline){var doc=null;
var win=null;
window.top.consoleWindow=window.open("",this.logger.category,"left=0,top=0,width=700,height=700,scrollbars=no,status=no,resizable=yes;toolbar=no");
window.top.consoleWindow.opener=self;
win=window.top.consoleWindow;
if(!win){this.popupBlocker=true;
alert("Popup window manager blocking the Log4js popup window to bedisplayed.\n\nPlease disabled this to properly see logged events.")
}else{doc=win.document;
doc.open();
doc.write("<!DOCTYPE html PUBLIC -//W3C//DTD XHTML 1.0 Transitional//EN ");
doc.write("  http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd>\n\n");
doc.write("<html><head><title>Log4js - "+this.logger.category+"</title>\n");
doc.write('</head><body style="background-color:darkgray"></body>\n');
win.blur();
win.focus()
}this.docReference=doc;
this.winReference=win
}else{this.docReference=document;
this.winReference=window
}this.outputCount=0;
this.tagPattern=".*";
this.logElement=this.docReference.createElement("div");
this.docReference.body.appendChild(this.logElement);
this.logElement.style.display="none";
this.logElement.style.position="absolute";
this.logElement.style.left="0px";
this.logElement.style.width="100%";
this.logElement.style.textAlign="left";
this.logElement.style.fontFamily="lucida console";
this.logElement.style.fontSize="100%";
this.logElement.style.backgroundColor="darkgray";
this.logElement.style.opacity=0.9;
this.logElement.style.zIndex=2000;
this.toolbarElement=this.docReference.createElement("div");
this.logElement.appendChild(this.toolbarElement);
this.toolbarElement.style.padding="0 0 0 2px";
this.buttonsContainerElement=this.docReference.createElement("span");
this.toolbarElement.appendChild(this.buttonsContainerElement);
if(this.inline){var closeButton=this.docReference.createElement("button");
closeButton.style.cssFloat="right";
closeButton.style.styleFloat="right";
closeButton.style.color="black";
closeButton.innerHTML="close";
closeButton.onclick=Log4js.bind(this.toggle,this);
this.buttonsContainerElement.appendChild(closeButton)
}var clearButton=this.docReference.createElement("button");
clearButton.style.cssFloat="right";
clearButton.style.styleFloat="right";
clearButton.style.color="black";
clearButton.innerHTML="clear";
clearButton.onclick=Log4js.bind(this.logger.clear,this.logger);
this.buttonsContainerElement.appendChild(clearButton);
this.tagFilterContainerElement=this.docReference.createElement("span");
this.toolbarElement.appendChild(this.tagFilterContainerElement);
this.tagFilterContainerElement.style.cssFloat="left";
this.tagFilterContainerElement.appendChild(this.docReference.createTextNode("Log4js - "+this.logger.category));
this.tagFilterContainerElement.appendChild(this.docReference.createTextNode(" | Level Filter: "));
this.tagFilterElement=this.docReference.createElement("input");
this.tagFilterContainerElement.appendChild(this.tagFilterElement);
this.tagFilterElement.style.width="200px";
this.tagFilterElement.value=this.tagPattern;
this.tagFilterElement.setAttribute("autocomplete","off");
Log4js.attachEvent(this.tagFilterElement,"keyup",Log4js.bind(this.updateTags,this));
Log4js.attachEvent(this.tagFilterElement,"click",Log4js.bind(function(){this.tagFilterElement.select()
},this));
this.outputElement=this.docReference.createElement("div");
this.logElement.appendChild(this.outputElement);
this.outputElement.style.overflow="auto";
this.outputElement.style.clear="both";
this.outputElement.style.height=(this.inline)?("200px"):("650px");
this.outputElement.style.width="100%";
this.outputElement.style.backgroundColor="black";
this.inputContainerElement=this.docReference.createElement("div");
this.inputContainerElement.style.width="100%";
this.logElement.appendChild(this.inputContainerElement);
this.inputElement=this.docReference.createElement("input");
this.inputContainerElement.appendChild(this.inputElement);
this.inputElement.style.width="100%";
this.inputElement.style.borderWidth="0px";
this.inputElement.style.margin="0px";
this.inputElement.style.padding="0px";
this.inputElement.value="Type command here";
this.inputElement.setAttribute("autocomplete","off");
Log4js.attachEvent(this.inputElement,"keyup",Log4js.bind(this.handleInput,this));
Log4js.attachEvent(this.inputElement,"click",Log4js.bind(function(){this.inputElement.select()
},this));
if(this.inline){window.setInterval(Log4js.bind(this.repositionWindow,this),500);
this.repositionWindow();
var accessElement=this.docReference.createElement("button");
accessElement.style.position="absolute";
accessElement.style.top="-100px";
accessElement.accessKey=this.accesskey;
accessElement.onclick=Log4js.bind(this.toggle,this);
this.docReference.body.appendChild(accessElement)
}else{this.show()
}},toggle:function(){if(this.logElement.style.display=="none"){this.show();
return true
}else{this.hide();
return false
}},show:function(){this.logElement.style.display="";
this.outputElement.scrollTop=this.outputElement.scrollHeight;
this.inputElement.select()
},hide:function(){this.logElement.style.display="none"
},output:function(message,style){var shouldScroll=(this.outputElement.scrollTop+(2*this.outputElement.clientHeight))>=this.outputElement.scrollHeight;
this.outputCount++;
style=(style?style+=";":"");
style+="padding:1px;margin:0 0 5px 0";
if(this.outputCount%2===0){style+=";background-color:#101010"
}message=message||"undefined";
message=message.toString();
this.outputElement.innerHTML+="<pre style='"+style+"'>"+message+"</pre>";
if(shouldScroll){this.outputElement.scrollTop=this.outputElement.scrollHeight
}},updateTags:function(){var pattern=this.tagFilterElement.value;
if(this.tagPattern==pattern){return 
}try{new RegExp(pattern)
}catch(e){return 
}this.tagPattern=pattern;
this.outputElement.innerHTML="";
this.outputCount=0;
for(var i=0;
i<this.logger.loggingEvents.length;
i++){this.doAppend(this.logger.loggingEvents[i])
}},repositionWindow:function(){var offset=window.pageYOffset||this.docReference.documentElement.scrollTop||this.docReference.body.scrollTop;
var pageHeight=self.innerHeight||this.docReference.documentElement.clientHeight||this.docReference.body.clientHeight;
this.logElement.style.top=(offset+pageHeight-this.logElement.offsetHeight)+"px"
},doAppend:function(loggingEvent){if(this.popupBlocker){return 
}if((!this.inline)&&(!this.winReference||this.winReference.closed)){this.initialize()
}if(this.tagPattern!==null&&loggingEvent.level.toString().search(new RegExp(this.tagPattern,"igm"))==-1){return 
}var style="";
if(loggingEvent.level.toString().search(/ERROR/)!=-1){style+="color:red"
}else{if(loggingEvent.level.toString().search(/FATAL/)!=-1){style+="color:red"
}else{if(loggingEvent.level.toString().search(/WARN/)!=-1){style+="color:orange"
}else{if(loggingEvent.level.toString().search(/DEBUG/)!=-1){style+="color:green"
}else{if(loggingEvent.level.toString().search(/INFO/)!=-1){style+="color:white"
}else{style+="color:yellow"
}}}}}this.output(this.layout.format(loggingEvent),style)
},doClear:function(){this.outputElement.innerHTML=""
},handleInput:function(e){if(e.keyCode==13){var command=this.inputElement.value;
switch(command){case"clear":this.logger.clear();
break;
default:var consoleOutput="";
try{consoleOutput=eval(this.inputElement.value)
}catch(e){this.logger.error("Problem parsing input <"+command+">"+e.message);
break
}this.logger.trace(consoleOutput);
break
}if(this.inputElement.value!==""&&this.inputElement.value!==this.commandHistory[0]){this.commandHistory.unshift(this.inputElement.value)
}this.commandIndex=0;
this.inputElement.value=""
}else{if(e.keyCode==38&&this.commandHistory.length>0){this.inputElement.value=this.commandHistory[this.commandIndex];
if(this.commandIndex<this.commandHistory.length-1){this.commandIndex+=1
}}else{if(e.keyCode==40&&this.commandHistory.length>0){if(this.commandIndex>0){this.commandIndex-=1
}this.inputElement.value=this.commandHistory[this.commandIndex]
}else{this.commandIndex=0
}}}},toString:function(){return"Log4js.ConsoleAppender[inline="+this.inline+"]"
}});
Log4js.MetatagAppender=function(){this.currentLine=0
};
Log4js.MetatagAppender.prototype=Log4js.extend(new Log4js.Appender(),{doAppend:function(loggingEvent){var now=new Date();
var lines=loggingEvent.message.split("\n");
var headTag=document.getElementsByTagName("head")[0];
for(var i=1;
i<=lines.length;
i++){var value=lines[i-1];
if(i==1){value=loggingEvent.level.toString()+": "+value
}else{value="> "+value
}var metaTag=document.createElement("meta");
metaTag.setAttribute("name","X-log4js:"+this.currentLine);
metaTag.setAttribute("content",value);
headTag.appendChild(metaTag);
this.currentLine+=1
}},toString:function(){return"Log4js.MetatagAppender"
}});
Log4js.AjaxAppender=function(loggingUrl){this.isInProgress=false;
this.loggingUrl=loggingUrl||"logging.log4js";
this.threshold=1;
this.timeout=2000;
this.loggingEventMap=new Log4js.FifoBuffer();
this.layout=new Log4js.XMLLayout();
this.httpRequest=null
};
Log4js.AjaxAppender.prototype=Log4js.extend(new Log4js.Appender(),{doAppend:function(loggingEvent){log4jsLogger.trace("> AjaxAppender.append");
if(this.loggingEventMap.length()<=this.threshold||this.isInProgress===true){this.loggingEventMap.push(loggingEvent)
}if(this.loggingEventMap.length()>=this.threshold&&this.isInProgress===false){this.send()
}log4jsLogger.trace("< AjaxAppender.append")
},doClear:function(){log4jsLogger.trace("> AjaxAppender.doClear");
if(this.loggingEventMap.length()>0){this.send()
}log4jsLogger.trace("< AjaxAppender.doClear")
},setThreshold:function(threshold){log4jsLogger.trace("> AjaxAppender.setThreshold: "+threshold);
this.threshold=threshold;
log4jsLogger.trace("< AjaxAppender.setThreshold")
},setTimeout:function(milliseconds){this.timeout=milliseconds
},send:function(){if(this.loggingEventMap.length()>0){log4jsLogger.trace("> AjaxAppender.send");
this.isInProgress=true;
var a=[];
for(var i=0;
i<this.loggingEventMap.length()&&i<this.threshold;
i++){a.push(this.layout.format(this.loggingEventMap.pull()))
}var content=this.layout.getHeader();
content+=a.join(this.layout.getSeparator());
content+=this.layout.getFooter();
var appender=this;
if(this.httpRequest===null){this.httpRequest=this.getXmlHttpRequest()
}this.httpRequest.onreadystatechange=function(){appender.onReadyStateChanged.call(appender)
};
this.httpRequest.open("POST",this.loggingUrl,true);
this.httpRequest.setRequestHeader("Content-type",this.layout.getContentType());
this.httpRequest.setRequestHeader("REFERER",location.href);
this.httpRequest.setRequestHeader("Content-length",content.length);
this.httpRequest.setRequestHeader("Connection","close");
this.httpRequest.send(content);
appender=this;
try{window.setTimeout(function(){log4jsLogger.trace("> AjaxAppender.timeout");
appender.httpRequest.onreadystatechange=function(){return 
};
appender.httpRequest.abort();
appender.isInProgress=false;
if(appender.loggingEventMap.length()>0){appender.send()
}log4jsLogger.trace("< AjaxAppender.timeout")
},this.timeout)
}catch(e){log4jsLogger.fatal(e)
}log4jsLogger.trace("> AjaxAppender.send")
}},onReadyStateChanged:function(){log4jsLogger.trace("> AjaxAppender.onReadyStateChanged");
var req=this.httpRequest;
if(this.httpRequest.readyState!=4){log4jsLogger.trace("< AjaxAppender.onReadyStateChanged: readyState "+req.readyState+" != 4");
return 
}var success=((typeof req.status==="undefined")||req.status===0||(req.status>=200&&req.status<300));
if(success){log4jsLogger.trace("  AjaxAppender.onReadyStateChanged: success");
this.isInProgress=false
}else{var msg="  AjaxAppender.onReadyStateChanged: XMLHttpRequest request to URL "+this.loggingUrl+" returned status code "+this.httpRequest.status;
log4jsLogger.error(msg)
}log4jsLogger.trace("< AjaxAppender.onReadyStateChanged: readyState == 4")
},getXmlHttpRequest:function(){log4jsLogger.trace("> AjaxAppender.getXmlHttpRequest");
var httpRequest=false;
try{if(window.XMLHttpRequest){httpRequest=new XMLHttpRequest();
if(httpRequest.overrideMimeType){httpRequest.overrideMimeType(this.layout.getContentType())
}}else{if(window.ActiveXObject){try{httpRequest=new ActiveXObject("Msxml2.XMLHTTP")
}catch(e){httpRequest=new ActiveXObject("Microsoft.XMLHTTP")
}}}}catch(e){httpRequest=false
}if(!httpRequest){log4jsLogger.fatal("Unfortunatelly your browser does not support AjaxAppender for log4js!")
}log4jsLogger.trace("< AjaxAppender.getXmlHttpRequest");
return httpRequest
},toString:function(){return"Log4js.AjaxAppender[loggingUrl="+this.loggingUrl+", threshold="+this.threshold+"]"
}});
Log4js.FileAppender=function(file){this.layout=new Log4js.SimpleLayout();
this.isIE="undefined";
this.file=file||"log4js.log";
try{this.fso=new ActiveXObject("Scripting.FileSystemObject");
this.isIE=true
}catch(e){try{netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
this.fso=Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
this.isIE=false
}catch(e){log4jsLogger.error(e)
}}};
Log4js.FileAppender.prototype=Log4js.extend(new Log4js.Appender(),{doAppend:function(loggingEvent){try{var fileHandle=null;
if(this.isIE==="undefined"){log4jsLogger.error("Unsupported ")
}else{if(this.isIE){fileHandle=this.fso.OpenTextFile(this.file,8,true);
fileHandle.WriteLine(this.layout.format(loggingEvent));
fileHandle.close()
}else{netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
this.fso.initWithPath(this.file);
if(!this.fso.exists()){this.fso.create(0,384)
}fileHandle=Components.classes["@mozilla.org/network/file-output-stream;1"].createInstance(Components.interfaces.nsIFileOutputStream);
fileHandle.init(this.fso,4|8|16,52,0);
var line=this.layout.format(loggingEvent);
fileHandle.write(line,line.length);
fileHandle.close()
}}}catch(e){log4jsLogger.error(e)
}},doClear:function(){try{if(this.isIE){var fileHandle=this.fso.GetFile(this.file);
fileHandle.Delete()
}else{netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
this.fso.initWithPath(this.file);
if(this.fso.exists()){this.fso.remove(false)
}}}catch(e){log4jsLogger.error(e)
}},toString:function(){return"Log4js.FileAppender[file="+this.file+"]"
}});
Log4js.WindowsEventAppender=function(){this.layout=new Log4js.SimpleLayout();
try{this.shell=new ActiveXObject("WScript.Shell")
}catch(e){log4jsLogger.error(e)
}};
Log4js.WindowsEventAppender.prototype=Log4js.extend(new Log4js.Appender(),{doAppend:function(loggingEvent){var winLevel=4;
switch(loggingEvent.level){case Log4js.Level.FATAL:winLevel=1;
break;
case Log4js.Level.ERROR:winLevel=1;
break;
case Log4js.Level.WARN:winLevel=2;
break;
default:winLevel=4;
break
}try{this.shell.LogEvent(winLevel,this.level.format(loggingEvent))
}catch(e){log4jsLogger.error(e)
}},toString:function(){return"Log4js.WindowsEventAppender"
}});
Log4js.JSAlertAppender=function(){this.layout=new Log4js.SimpleLayout()
};
Log4js.JSAlertAppender.prototype=Log4js.extend(new Log4js.Appender(),{doAppend:function(loggingEvent){alert(this.layout.getHeader()+this.layout.format(loggingEvent)+this.layout.getFooter())
},toString:function(){return"Log4js.JSAlertAppender"
}});
Log4js.MozillaJSConsoleAppender=function(){this.layout=new Log4js.SimpleLayout();
try{netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
this.jsConsole=Components.classes["@mozilla.org/consoleservice;1"].getService(Components.interfaces.nsIConsoleService);
this.scriptError=Components.classes["@mozilla.org/scripterror;1"].createInstance(Components.interfaces.nsIScriptError)
}catch(e){log4jsLogger.error(e)
}};
Log4js.MozillaJSConsoleAppender.prototype=Log4js.extend(new Log4js.Appender(),{doAppend:function(loggingEvent){try{netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
this.scriptError.init(this.layout.format(loggingEvent),null,null,null,null,this.levelCode(loggingEvent),loggingEvent.categoryName);
this.jsConsole.logMessage(this.scriptError)
}catch(e){log4jsLogger.error(e)
}},toString:function(){return"Log4js.MozillaJSConsoleAppender"
},levelCode:function(loggingEvent){var retval;
switch(loggingEvent.level){case Log4js.Level.FATAL:retval=2;
break;
case Log4js.Level.ERROR:retval=0;
break;
case Log4js.Level.WARN:retval=1;
break;
default:retval=1;
break
}return retval
}});
Log4js.OperaJSConsoleAppender=function(){this.layout=new Log4js.SimpleLayout()
};
Log4js.OperaJSConsoleAppender.prototype=Log4js.extend(new Log4js.Appender(),{doAppend:function(loggingEvent){opera.postError(this.layout.format(loggingEvent))
},toString:function(){return"Log4js.OperaJSConsoleAppender"
}});
Log4js.SafariJSConsoleAppender=function(){this.layout=new Log4js.SimpleLayout()
};
Log4js.SafariJSConsoleAppender.prototype=Log4js.extend(new Log4js.Appender(),{doAppend:function(loggingEvent){window.console.log(this.layout.format(loggingEvent))
},toString:function(){return"Log4js.SafariJSConsoleAppender"
}});
Log4js.BrowserConsoleAppender=function(){this.consoleDelegate=null;
if(window.console){this.consoleDelegate=new Log4js.SafariJSConsoleAppender()
}else{if(window.opera){this.consoleDelegate=new Log4js.OperaJSConsoleAppender()
}else{if(netscape){this.consoleDelegate=new Log4js.MozillaJSConsoleAppender()
}else{log4jsLogger.error("Unsupported Browser")
}}}};
Log4js.BrowserConsoleAppender.prototype=Log4js.extend(new Log4js.Appender(),{doAppend:function(loggingEvent){this.consoleDelegate.doAppend(loggingEvent)
},doClear:function(){this.consoleDelegate.doClear()
},setLayout:function(layout){this.consoleDelegate.setLayout(layout)
},toString:function(){return"Log4js.BrowserConsoleAppender: "+this.consoleDelegate.toString()
}});
Log4js.SimpleLayout=function(){this.LINE_SEP="\n";
this.LINE_SEP_LEN=1
};
Log4js.SimpleLayout.prototype=Log4js.extend(new Log4js.Layout(),{format:function(loggingEvent){return loggingEvent.level.toString()+" - "+loggingEvent.message+this.LINE_SEP
},getContentType:function(){return"text/plain"
},getHeader:function(){return""
},getFooter:function(){return""
}});
Log4js.BasicLayout=function(){this.LINE_SEP="\n"
};
Log4js.BasicLayout.prototype=Log4js.extend(new Log4js.Layout(),{format:function(loggingEvent){return loggingEvent.categoryName+"~"+loggingEvent.startTime.toLocaleString()+" ["+loggingEvent.level.toString()+"] "+loggingEvent.message+this.LINE_SEP
},getContentType:function(){return"text/plain"
},getHeader:function(){return""
},getFooter:function(){return""
}});
Log4js.HtmlLayout=function(){return 
};
Log4js.HtmlLayout.prototype=Log4js.extend(new Log4js.Layout(),{format:function(loggingEvent){return'<div style="'+this.getStyle(loggingEvent)+'">'+loggingEvent.getFormattedTimestamp()+" - "+loggingEvent.level.toString()+" - "+loggingEvent.message+"</div>\n"
},getContentType:function(){return"text/html"
},getHeader:function(){return"<html><head><title>log4js</head><body>"
},getFooter:function(){return"</body></html>"
},getStyle:function(loggingEvent){var style;
if(loggingEvent.level.toString().search(/ERROR/)!=-1){style="color:red"
}else{if(loggingEvent.level.toString().search(/FATAL/)!=-1){style="color:red"
}else{if(loggingEvent.level.toString().search(/WARN/)!=-1){style="color:orange"
}else{if(loggingEvent.level.toString().search(/DEBUG/)!=-1){style="color:green"
}else{if(loggingEvent.level.toString().search(/INFO/)!=-1){style="color:white"
}else{style="color:yellow"
}}}}}return style
}});
Log4js.XMLLayout=function(){return 
};
Log4js.XMLLayout.prototype=Log4js.extend(new Log4js.Layout(),{format:function(loggingEvent){var useragent="unknown";
try{useragent=navigator.userAgent
}catch(e){useragent="unknown"
}var referer="unknown";
try{referer=location.href
}catch(e){referer="unknown"
}var content='<log4js:event logger="';
content+=loggingEvent.categoryName+'" level="';
content+=loggingEvent.level.toString()+'" useragent="';
content+=useragent+'" referer="';
content+=referer.replace(/&/g,"&amp;")+'" timestamp="';
content+=loggingEvent.getFormattedTimestamp()+'">\n';
content+="\t<log4js:message><![CDATA["+this.escapeCdata(loggingEvent.message)+"]]></log4js:message>\n";
if(loggingEvent.exception){content+=this.formatException(loggingEvent.exception)
}content+="</log4js:event>\n";
return content
},getContentType:function(){return"text/xml"
},getHeader:function(){return'<log4js:eventSet version="'+Log4js.version+'" xmlns:log4js="http://log4js.berlios.de/2007/log4js/">\n'
},getFooter:function(){return"</log4js:eventSet>\n"
},getSeparator:function(){return"\n"
},formatException:function(ex){if(ex){var exStr="\t<log4js:throwable>";
if(ex.message){exStr+="\t\t<log4js:message><![CDATA["+this.escapeCdata(ex.message)+"]]></log4js:message>\n"
}if(ex.description){exStr+="\t\t<log4js:description><![CDATA["+this.escapeCdata(ex.description)+"]]></log4js:description>\n"
}exStr+="\t\t<log4js:stacktrace>";
exStr+='\t\t\t<log4js:location fileName="'+ex.fileName+'" lineNumber="'+ex.lineNumber+'" />';
exStr+="\t\t</log4js:stacktrace>";
exStr="\t</log4js:throwable>";
return exStr
}return null
},escapeCdata:function(str){return str.replace(/\]\]>/,"]]>]]&gt;<![CDATA[")
}});
Log4js.JSONLayout=function(){this.df=new Log4js.DateFormatter()
};
Log4js.JSONLayout.prototype=Log4js.extend(new Log4js.Layout(),{format:function(loggingEvent){var useragent="unknown";
try{useragent=navigator.userAgent
}catch(e){useragent="unknown"
}var referer="unknown";
try{referer=location.href
}catch(e){referer="unknown"
}var jsonString='{\n "LoggingEvent": {\n';
jsonString+='\t"logger": "'+loggingEvent.categoryName+'",\n';
jsonString+='\t"level": "'+loggingEvent.level.toString()+'",\n';
jsonString+='\t"message": "'+loggingEvent.message+'",\n';
jsonString+='\t"referer": "'+referer+'",\n';
jsonString+='\t"useragent": "'+useragent+'",\n';
jsonString+='\t"timestamp": "'+this.df.formatDate(loggingEvent.startTime,"yyyy-MM-ddThh:mm:ssZ")+'",\n';
jsonString+='\t"exception": "'+loggingEvent.exception+'"\n';
jsonString+="}}";
return jsonString
},getContentType:function(){return"text/json"
},getHeader:function(){return'{"Log4js": [\n'
},getFooter:function(){return"\n]}"
},getSeparator:function(){return",\n"
}});
Log4js.PatternLayout=function(pattern){if(pattern){this.pattern=pattern
}else{this.pattern=Log4js.PatternLayout.DEFAULT_CONVERSION_PATTERN
}};
Log4js.PatternLayout.TTCC_CONVERSION_PATTERN="%r %p %c - %m%n";
Log4js.PatternLayout.DEFAULT_CONVERSION_PATTERN="%m%n";
Log4js.PatternLayout.ISO8601_DATEFORMAT="yyyy-MM-dd HH:mm:ss,SSS";
Log4js.PatternLayout.DATETIME_DATEFORMAT="dd MMM YYYY HH:mm:ss,SSS";
Log4js.PatternLayout.ABSOLUTETIME_DATEFORMAT="HH:mm:ss,SSS";
Log4js.PatternLayout.prototype=Log4js.extend(new Log4js.Layout(),{getContentType:function(){return"text/plain"
},getHeader:function(){return null
},getFooter:function(){return null
},format:function(loggingEvent){var regex=/%(-?[0-9]+)?(\.?[0-9]+)?([cdmnpr%])(\{([^\}]+)\})?|([^%]+)/;
var formattedString="";
var result;
var searchString=this.pattern;
while((result=regex.exec(searchString))){var matchedString=result[0];
var padding=result[1];
var truncation=result[2];
var conversionCharacter=result[3];
var specifier=result[5];
var text=result[6];
if(text){formattedString+=""+text
}else{var replacement="";
switch(conversionCharacter){case"c":var loggerName=loggingEvent.categoryName;
if(specifier){var precision=parseInt(specifier,10);
var loggerNameBits=loggingEvent.categoryName.split(".");
if(precision>=loggerNameBits.length){replacement=loggerName
}else{replacement=loggerNameBits.slice(loggerNameBits.length-precision).join(".")
}}else{replacement=loggerName
}break;
case"d":var dateFormat=Log4js.PatternLayout.ISO8601_DATEFORMAT;
if(specifier){dateFormat=specifier;
if(dateFormat=="ISO8601"){dateFormat=Log4js.PatternLayout.ISO8601_DATEFORMAT
}else{if(dateFormat=="ABSOLUTE"){dateFormat=Log4js.PatternLayout.ABSOLUTETIME_DATEFORMAT
}else{if(dateFormat=="DATE"){dateFormat=Log4js.PatternLayout.DATETIME_DATEFORMAT
}}}}replacement=(new Log4js.SimpleDateFormat(dateFormat)).format(loggingEvent.startTime);
break;
case"m":replacement=loggingEvent.message;
break;
case"n":replacement="\n";
break;
case"p":replacement=loggingEvent.level.toString();
break;
case"r":replacement=""+loggingEvent.startTime.toLocaleTimeString();
break;
case"%":replacement="%";
break;
default:replacement=matchedString;
break
}var len;
if(truncation){len=parseInt(truncation.substr(1),10);
replacement=replacement.substring(0,len)
}if(padding){if(padding.charAt(0)=="-"){len=parseInt(padding.substr(1),10);
while(replacement.length<len){replacement+=" "
}}else{len=parseInt(padding,10);
while(replacement.length<len){replacement=" "+replacement
}}}formattedString+=replacement
}searchString=searchString.substr(result.index+result[0].length)
}return formattedString
}});
if(!Array.prototype.push){Array.prototype.push=function(){var startLength=this.length;
for(var i=0;
i<arguments.length;
i++){this[startLength+i]=arguments[i]
}return this.length
}
}Log4js.FifoBuffer=function(){this.array=new Array()
};
Log4js.FifoBuffer.prototype={push:function(obj){this.array[this.array.length]=obj;
return this.array.length
},pull:function(){if(this.array.length>0){var firstItem=this.array[0];
for(var i=0;
i<this.array.length-1;
i++){this.array[i]=this.array[i+1]
}this.array.length=this.array.length-1;
return firstItem
}return null
},length:function(){return this.array.length
}};
Log4js.DateFormatter=function(){return 
};
Log4js.DateFormatter.DEFAULT_DATE_FORMAT="yyyy-MM-ddThh:mm:ssO";
Log4js.DateFormatter.prototype={formatDate:function(vDate,vFormat){var vDay=this.addZero(vDate.getDate());
var vMonth=this.addZero(vDate.getMonth()+1);
var vYearLong=this.addZero(vDate.getFullYear());
var vYearShort=this.addZero(vDate.getFullYear().toString().substring(3,4));
var vYear=(vFormat.indexOf("yyyy")>-1?vYearLong:vYearShort);
var vHour=this.addZero(vDate.getHours());
var vMinute=this.addZero(vDate.getMinutes());
var vSecond=this.addZero(vDate.getSeconds());
var vTimeZone=this.O(vDate);
var vDateString=vFormat.replace(/dd/g,vDay).replace(/MM/g,vMonth).replace(/y{1,4}/g,vYear);
vDateString=vDateString.replace(/hh/g,vHour).replace(/mm/g,vMinute).replace(/ss/g,vSecond);
vDateString=vDateString.replace(/O/g,vTimeZone);
return vDateString
},addZero:function(vNumber){return((vNumber<10)?"0":"")+vNumber
},O:function(date){var os=Math.abs(date.getTimezoneOffset());
var h=String(Math.floor(os/60));
var m=String(os%60);
h.length==1?h="0"+h:1;
m.length==1?m="0"+m:1;
return date.getTimezoneOffset()<0?"+"+h+m:"-"+h+m
}};
var log4jsLogger=Log4js.getLogger("Log4js");
log4jsLogger.addAppender(new Log4js.ConsoleAppender());
log4jsLogger.setLevel(Log4js.Level.ALL);core.Monitor=function(fadeElement,logContentElem){$assert(fadeElement,"fadeElement can not be null");
$assert(logContentElem,"logContentElem can not be null");
this.pendingMessages=[];
this.inProgress=false;
this._container=fadeElement;
this._currentMessage=null;
this._logContentElem=logContentElem;
this._fxOpacity=fadeElement.effect("opacity",{duration:6000})
};
core.Monitor.prototype._logMessage=function(msg,msgKind){this._fxOpacity.clearTimer();
if(msgKind==core.Monitor.MsgKind.ERROR){msg="<div id='small_error_icon'>"+msg+"</div>"
}this._currentMessage=msg;
this._fxOpacity.start(1,0);
this._logContentElem.innerHTML=msg
};
core.Monitor.prototype.logError=function(userMsg){this.logMessage(userMsg,core.Monitor.MsgKind.ERROR)
};
core.Monitor.prototype.logFatal=function(userMsg){this.logMessage(userMsg,core.Monitor.MsgKind.FATAL)
};
core.Monitor.prototype.logMessage=function(msg,msgKind){if(!msgKind){msgKind=core.Monitor.MsgKind.INFO
}if(msgKind==core.Monitor.MsgKind.FATAL){new Windoo.Alert(msg,{window:{theme:Windoo.Themes.aero,title:"Outch!!. An unexpected error.",onClose:function(){}}})
}else{var messages=this.pendingMessages;
var monitor=this;
if(!this.executer){monitor._logMessage(msg,msgKind);
var disptacher=function(){if(messages.length>0){var msgToDisplay=messages.shift();
monitor._logMessage(msgToDisplay)
}if(messages.length==0){$clear(monitor.executer);
monitor.executer=null;
monitor._fxOpacity.hide();
this._currentMessage=null
}};
this.executer=disptacher.periodical(600)
}else{if(this._currentMessage!=msg){messages.push(msg)
}}}};
core.Monitor.setInstance=function(monitor){this.monitor=monitor
};
core.Monitor.getInstance=function(){var result=this.monitor;
if(result==null){result={logError:function(){},logMessage:function(){}}
}return result
};
core.Monitor.MsgKind={INFO:1,WARNING:2,ERROR:3,FATAL:4};core.Point=function(x,y){this.x=x;
this.y=y
};
core.Point.prototype.setValue=function(x,y){this.x=x;
this.y=y
};
core.Point.prototype.inspect=function(){return"{x:"+this.x+",y:"+this.y+"}"
};
core.Point.prototype.clone=function(){return new core.Point(this.x,this.y)
};
core.Point.fromString=function(point){var values=point.split(",");
return new core.Point(values[0],values[1])
};core.UserAgent={isMozillaFamily:function(){return this.browser=="Netscape"||this.browser=="Firefox"
},isIE:function(){return this.browser=="Explorer"
},init:function(){this.browser=this.searchString(this.dataBrowser)||"An unknown browser";
this.version=this.searchVersion(navigator.userAgent)||this.searchVersion(navigator.appVersion)||"an unknown version";
this.OS=this.searchString(this.dataOS)||"an unknown OS"
},searchString:function(data){for(var i=0;
i<data.length;
i++){var dataString=data[i].string;
var dataProp=data[i].prop;
this.versionSearchString=data[i].versionSearch||data[i].identity;
if(dataString){if(dataString.indexOf(data[i].subString)!=-1){return data[i].identity
}}else{if(dataProp){return data[i].identity
}}}},searchVersion:function(dataString){var index=dataString.indexOf(this.versionSearchString);
if(index==-1){return 
}return parseFloat(dataString.substring(index+this.versionSearchString.length+1))
},dataBrowser:[{string:navigator.userAgent,subString:"OmniWeb",versionSearch:"OmniWeb/",identity:"OmniWeb"},{string:navigator.vendor,subString:"Apple",identity:"Safari"},{string:navigator.vendor,subString:"Google Inc.",identity:"Chrome"},{prop:window.opera,identity:"Opera"},{string:navigator.vendor,subString:"iCab",identity:"iCab"},{string:navigator.vendor,subString:"KDE",identity:"Konqueror"},{string:navigator.userAgent,subString:"Firefox",identity:"Firefox"},{string:navigator.vendor,subString:"Camino",identity:"Camino"},{string:navigator.userAgent,subString:"Netscape",identity:"Netscape"},{string:navigator.userAgent,subString:"MSIE",identity:"Explorer",versionSearch:"MSIE"},{string:navigator.userAgent,subString:"Gecko",identity:"Mozilla",versionSearch:"rv"},{string:navigator.userAgent,subString:"Mozilla",identity:"Netscape",versionSearch:"Mozilla"}],dataOS:[{string:navigator.platform,subString:"Win",identity:"Windows"},{string:navigator.platform,subString:"Mac",identity:"Mac"},{string:navigator.platform,subString:"Linux",identity:"Linux"}]};
core.UserAgent.init();core.Utils={escapeInvalidTags:function(text){return text
}};
function $defined(obj){return(obj!=undefined)
}objects={};
objects.extend=function(subClass,baseClass){function inheritance(){}inheritance.prototype=baseClass.prototype;
subClass.prototype=new inheritance();
subClass.prototype.constructor=subClass;
subClass.baseConstructor=baseClass;
subClass.superClass=baseClass.prototype
};
$assert=function(assert,message){if(!$defined(assert)||!assert){var stack;
try{null.eval()
}catch(e){stack=e
}wLogger.error(message+","+stack)
}};
Math.sign=function(value){return(value>=0)?1:-1
};
function $import(src){var scriptElem=document.createElement("script");
scriptElem.setAttribute("src",src);
scriptElem.setAttribute("type","text/javascript");
document.getElementsByTagName("head")[0].appendChild(scriptElem)
}core.Utils.getMousePosition=function(event){var xcoord=-1;
var ycoord=-1;
if(!$defined(event)){if($defined(window.event)){event=window.event
}else{throw"Could not obtain mouse position"
}}if($defined(event.$extended)){event=event.event
}if(typeof (event.pageX)=="number"){xcoord=event.pageX;
ycoord=event.pageY
}else{if(typeof (event.clientX)=="number"){xcoord=event.clientX;
ycoord=event.clientY;
var badOldBrowser=(window.navigator.userAgent.indexOf("Opera")+1)||(window.ScriptEngine&&ScriptEngine().indexOf("InScript")+1)||(navigator.vendor=="KDE");
if(!badOldBrowser){if(document.body&&(document.body.scrollLeft||document.body.scrollTop)){xcoord+=document.body.scrollLeft;
ycoord+=document.body.scrollTop
}else{if(document.documentElement&&(document.documentElement.scrollLeft||document.documentElement.scrollTop)){xcoord+=document.documentElement.scrollLeft;
ycoord+=document.documentElement.scrollTop
}}}}else{throw"Could not obtain mouse position"
}}return{x:xcoord,y:ycoord}
};
core.Utils.workOutDivElementPosition=function(divElement){var curleft=0;
var curtop=0;
if($defined(divElement.offsetParent)){curleft=divElement.offsetLeft;
curtop=divElement.offsetTop;
while(divElement=divElement.offsetParent){curleft+=divElement.offsetLeft;
curtop+=divElement.offsetTop
}}return{x:curleft,y:curtop}
};
core.Utils.innerXML=function(node){if($defined(node.innerXML)){return node.innerXML
}else{if($defined(node.xml)){return node.xml
}else{if($defined(XMLSerializer)){return(new XMLSerializer()).serializeToString(node)
}}}};
core.Utils.createDocument=function(){var doc=null;
var _document=window.document;
if($defined(window.ActiveXObject)){var prefixes=["MSXML2","Microsoft","MSXML","MSXML3"];
for(var i=0;
i<prefixes.length;
i++){try{doc=new ActiveXObject(prefixes[i]+".XMLDOM")
}catch(e){}if($defined(doc)){break
}}}else{if((_document.implementation)&&(_document.implementation.createDocument)){doc=_document.implementation.createDocument("","",null)
}}return doc
};
core.Utils.createDocumentFromText=function(str,mimetype){if(!$defined(mimetype)){mimetype="text/xml"
}if($defined(window.DOMParser)){var parser=new DOMParser();
return parser.parseFromString(str,mimetype)
}else{if($defined(window.ActiveXObject)){var domDoc=core.Utils.createDocument();
if($defined(domDoc)){domDoc.async=false;
domDoc.loadXML(str);
return domDoc
}}}return null
};
core.Utils.calculateRelationShipPointCoordinates=function(topic,controlPoint){var size=topic.getSize();
var position=topic.getPosition();
var m=(position.y-controlPoint.y)/(position.x-controlPoint.x);
var y,x;
var gap=5;
if(controlPoint.y>position.y+(size.height/2)){y=position.y+(size.height/2)+gap;
x=position.x-((position.y-y)/m);
if(x>position.x+(size.width/2)){x=position.x+(size.width/2)
}else{if(x<position.x-(size.width/2)){x=position.x-(size.width/2)
}}}else{if(controlPoint.y<position.y-(size.height/2)){y=position.y-(size.height/2)-gap;
x=position.x-((position.y-y)/m);
if(x>position.x+(size.width/2)){x=position.x+(size.width/2)
}else{if(x<position.x-(size.width/2)){x=position.x-(size.width/2)
}}}else{if(controlPoint.x<(position.x-size.width/2)){x=position.x-(size.width/2)-gap;
y=position.y-(m*(position.x-x))
}else{x=position.x+(size.width/2)+gap;
y=position.y-(m*(position.x-x))
}}}return new core.Point(x,y)
};
core.Utils.calculateDefaultControlPoints=function(srcPos,tarPos){var y=srcPos.y-tarPos.y;
var x=srcPos.x-tarPos.x;
var m=y/x;
var l=Math.sqrt(y*y+x*x)/3;
var fix=1;
if(srcPos.x>tarPos.x){fix=-1
}var x1=srcPos.x+Math.sqrt(l*l/(1+(m*m)))*fix;
var y1=m*(x1-srcPos.x)+srcPos.y;
var x2=tarPos.x+Math.sqrt(l*l/(1+(m*m)))*fix*-1;
var y2=m*(x2-tarPos.x)+tarPos.y;
return[new core.Point(-srcPos.x+x1,-srcPos.y+y1),new core.Point(-tarPos.x+x2,-tarPos.y+y2)]
};
core.Utils.setVisibilityAnimated=function(elems,isVisible,doneFn){core.Utils.animateVisibility(elems,isVisible,doneFn)
};
core.Utils.setChildrenVisibilityAnimated=function(rootElem,isVisible){var children=core.Utils._addInnerChildrens(rootElem);
core.Utils.animateVisibility(children,isVisible)
};
core.Utils.animateVisibility=function(elems,isVisible,doneFn){var _fadeEffect=null;
var _opacity=(isVisible?0:1);
if(isVisible){elems.forEach(function(child,index){if($defined(child)){child.setOpacity(_opacity);
child.setVisibility(isVisible)
}})
}var fadeEffect=function(index){var step=10;
if((_opacity<=0&&!isVisible)||(_opacity>=1&&isVisible)){$clear(_fadeEffect);
_fadeEffect=null;
elems.forEach(function(child,index){if($defined(child)){child.setVisibility(isVisible)
}});
if($defined(doneFn)){doneFn.attempt()
}}else{var fix=1;
if(isVisible){fix=-1
}_opacity-=(1/step)*fix;
elems.forEach(function(child,index){if($defined(child)){child.setOpacity(_opacity)
}})
}};
_fadeEffect=fadeEffect.periodical(10)
};
core.Utils.animatePosition=function(elems,doneFn,designer){var _moveEffect=null;
var i=10;
var step=10;
var moveEffect=function(){if(i>0){var keys=elems.keys();
for(var j=0;
j<keys.length;
j++){var id=keys[j];
var mod=elems.get(id);
var allTopics=designer._getTopics();
var currentTopic=allTopics.filter(function(node){return node.getId()==id
})[0];
var xStep=(mod.originalPos.x-mod.newPos.x)/step;
var yStep=(mod.originalPos.y-mod.newPos.y)/step;
var newPos=currentTopic.getPosition().clone();
newPos.x+=xStep;
newPos.y+=yStep;
currentTopic.setPosition(newPos,false)
}}else{$clear(_moveEffect);
var keys=elems.keys();
for(var j=0;
j<keys.length;
j++){var id=keys[j];
var mod=elems.get(id);
var allTopics=designer._getTopics();
var currentTopic=allTopics.filter(function(node){return node.getId()==id
})[0];
currentTopic.setPosition(mod.originalPos,false)
}if($defined(doneFn)){doneFn.attempt()
}}i--
};
_moveEffect=moveEffect.periodical(10)
};
core.Utils._addInnerChildrens=function(elem){var children=[];
var childs=elem._getChildren();
for(var i=0;
i<childs.length;
i++){var child=childs[i];
children.push(child);
children.push(child.getOutgoingLine());
var relationships=child.getRelationships();
children=children.concat(relationships);
var innerChilds=core.Utils._addInnerChildrens(child);
children=children.concat(innerChilds)
}return children
};core.WaitDialog=new Class({yPos:0,xPos:0,initialize:function(){},activate:function(changeCursor,dialogContent){this.content=dialogContent;
this._initLightboxMarkup();
this.displayLightbox("block");
if(changeCursor){window.document.body.style.cursor="wait"
}},changeContent:function(dialogContent,changeCursor){this.content=dialogContent;
if(!$("lbContent")){window.document.body.style.cursor="pointer";
return 
}this.processInfo();
if(changeCursor){window.document.body.style.cursor="wait"
}else{window.document.body.style.cursor="auto"
}},displayLightbox:function(display){if(display!="none"){this.processInfo()
}$("overlay").style.display=display;
$("lightbox").style.display=display
},processInfo:function(){if($("lbContent")){$("lbContent").dispose()
}var lbContentElement=new Element("div").setProperty("id","lbContent");
lbContentElement.innerHTML=this.content.innerHTML;
lbContentElement.inject($("lbLoadMessage"),"before");
$("lightbox").className="done"
},actions:function(){lbActions=document.getElementsByClassName("lbAction");
for(i=0;
i<lbActions.length;
i++){$(lbActions[i]).addEvent("click",function(){this[lbActions[i].rel].pass(this)
}.bind(this));
lbActions[i].onclick=function(){return false
}
}},deactivate:function(time){if($("lbContent")){$("lbContent").dispose()
}this.displayLightbox("none");
window.document.body.style.cursor="default"
},_initLightboxMarkup:function(){var bodyElem=document.getElementsByTagName("body")[0];
var overlayElem=new Element("div").setProperty("id","overlay");
overlayElem.inject(bodyElem);
var lightboxElem=new Element("div").setProperty("id","lightbox");
lightboxElem.addClass("loading");
var lbLoadMessageElem=new Element("div").setProperty("id","lbLoadMessage");
lbLoadMessageElem.inject(lightboxElem);
lightboxElem.inject(bodyElem)
}});var wLogger=new Log4js.getLogger("WiseMapping");
wLogger.setLevel(Log4js.Level.ALL);
if($defined(window.LoggerService)){Log4js.WiseServerAppender=function(){this.layout=new Log4js.SimpleLayout()
};
Log4js.WiseServerAppender.prototype=Log4js.extend(new Log4js.Appender(),{doAppend:function(loggingEvent){try{var message=this.layout.format(loggingEvent);
var level=this.levelCode(loggingEvent);
window.LoggerService.logError(level,message)
}catch(e){alert(e)
}},toString:function(){return"Log4js.WiseServerAppender"
},levelCode:function(loggingEvent){var retval;
switch(loggingEvent.level){case Log4js.Level.FATAL:retval=3;
break;
case Log4js.Level.ERROR:retval=3;
break;
case Log4js.Level.WARN:retval=2;
break;
default:retval=1;
break
}return retval
}});
wLogger.addAppender(new Log4js.WiseServerAppender())
};core.Executor=new Class({options:{isLoading:true},initialize:function(options){this._pendingFunctions=[]
},setLoading:function(isLoading){this.options.isLoading=isLoading;
if(!isLoading){this._pendingFunctions.forEach(function(item){var result=item.fn.attempt(item.args,item.bind);
$assert(result!=false,"execution failed")
});
this._pendingFunctions=[]
}},isLoading:function(){return this.options.isLoading
},delay:function(fn,delay,bind,args){if(this.options.isLoading){this._pendingFunctions.push({fn:fn,bind:bind,args:args})
}else{fn.delay(delay,bind,args)
}}});
core.Executor.instance=new core.Executor();