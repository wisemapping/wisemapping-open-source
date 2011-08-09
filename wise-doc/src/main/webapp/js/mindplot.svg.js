var web2d={};
web2d.peer={svg:{}};
web2d.peer.utils={};web2d.peer.utils.EventUtils={broadcastChangeEvent:function(elementPeer,type){var listeners=elementPeer.getChangeEventListeners(type);
if($defined(listeners)){for(var i=0;
i<listeners.length;
i++){var listener=listeners[i];
listener.call(elementPeer,null)
}}var children=elementPeer.getChildren();
for(var i=0;
i<children.length;
i++){var child=children[i];
web2d.peer.utils.EventUtils.broadcastChangeEvent(child,type)
}}};web2d.peer.utils.TransformUtil={workoutScale:function(elementPeer){var current=elementPeer.getParent();
var width=1;
var height=1;
while(current){var coordSize=current.getCoordSize();
var size=current.getSize();
width=width*(parseInt(size.width)/coordSize.width);
height=height*(parseInt(size.height)/coordSize.height);
current=current.getParent()
}return{width:width,height:height}
}};web2d.peer.svg.ElementPeer=function(svgElement){this._native=svgElement;
this._dblClickListeners=new Hash();
this._size={width:1,height:1};
this._changeListeners={}
};
web2d.peer.svg.ElementPeer.prototype.svgNamespace="http://www.w3.org/2000/svg";
web2d.peer.svg.ElementPeer.prototype.linkNamespace="http://www.w3.org/1999/xlink";
web2d.peer.svg.ElementPeer.prototype.setChildren=function(children){this._children=children
};
web2d.peer.svg.ElementPeer.prototype.getChildren=function(){var result=this._children;
if(!$defined(result)){result=[];
this._children=result
}return result
};
web2d.peer.svg.ElementPeer.prototype.getParent=function(){return this._parent
};
web2d.peer.svg.ElementPeer.prototype.setParent=function(parent){this._parent=parent
};
web2d.peer.svg.ElementPeer.prototype.appendChild=function(elementPeer){elementPeer.setParent(this);
var children=this.getChildren();
children.include(elementPeer);
this._native.appendChild(elementPeer._native);
web2d.peer.utils.EventUtils.broadcastChangeEvent(this,"strokeStyle")
};
web2d.peer.svg.ElementPeer.prototype.removeChild=function(elementPeer){elementPeer.setParent(null);
var children=this.getChildren();
var length=children.length;
children.erase(elementPeer);
var newLength=children.length;
if(newLength>=length){throw"Could not remove the element."
}this._native.removeChild(elementPeer._native)
};
web2d.peer.svg.ElementPeer.prototype.addEventListener=function(type,listener){if(type=="dblclick"){var dblListener=function(e){if(e.detail>=2){listener.call(this,e)
}};
this._dblClickListeners[listener]=dblListener;
this._native.addEventListener(type,dblListener,false)
}else{this._native.addEventListener(type,listener,false)
}};
web2d.peer.svg.ElementPeer.prototype.removeEventListener=function(type,listener){if(type=="dblclick"){var dblClickListener=this._dblClickListeners[listener];
if(dblClickListener==null){throw"Could not find listener to remove"
}type="click";
this._native.removeEventListener(type,dblClickListener,false);
delete this._dblClickListeners[listener]
}else{this._native.removeEventListener(type,listener,false)
}};
web2d.peer.svg.ElementPeer.prototype.setSize=function(width,height){if($defined(width)&&this._size.width!=parseInt(width)){this._size.width=parseInt(width);
this._native.setAttribute("width",parseInt(width))
}if($defined(height)&&this._size.height!=parseInt(height)){this._size.height=parseInt(height);
this._native.setAttribute("height",parseInt(height))
}web2d.peer.utils.EventUtils.broadcastChangeEvent(this,"strokeStyle")
};
web2d.peer.svg.ElementPeer.prototype.getSize=function(){return{width:this._size.width,height:this._size.height}
};
web2d.peer.svg.ElementPeer.prototype.setFill=function(color,opacity){if($defined(color)){this._native.setAttribute("fill",color)
}if($defined(opacity)){this._native.setAttribute("fill-opacity",opacity)
}};
web2d.peer.svg.ElementPeer.prototype.getFill=function(){var color=this._native.getAttribute("fill");
var opacity=this._native.getAttribute("fill-opacity");
return{color:color,opacity:Number(opacity)}
};
web2d.peer.svg.ElementPeer.prototype.getStroke=function(){var vmlStroke=this._native;
var color=vmlStroke.getAttribute("stroke");
var dashstyle=this._stokeStyle;
var opacity=vmlStroke.getAttribute("stroke-opacity");
var width=vmlStroke.getAttribute("stroke-width");
return{color:color,style:dashstyle,opacity:opacity,width:width}
};
web2d.peer.svg.ElementPeer.prototype.__stokeStyleToStrokDasharray={solid:[],dot:[1,3],dash:[4,3],longdash:[10,2],dashdot:[5,3,1,3]};
web2d.peer.svg.ElementPeer.prototype.setStroke=function(width,style,color,opacity){if($defined(width)){this._native.setAttribute("stroke-width",width+"px")
}if($defined(color)){this._native.setAttribute("stroke",color)
}if($defined(style)){var dashArrayPoints=this.__stokeStyleToStrokDasharray[style];
var scale=1/web2d.peer.utils.TransformUtil.workoutScale(this).width;
var strokeWidth=this._native.getAttribute("stroke-width");
strokeWidth=parseFloat(strokeWidth);
var scaledPoints=[];
for(var i=0;
i<dashArrayPoints.length;
i++){scaledPoints[i]=dashArrayPoints[i]*strokeWidth;
scaledPoints[i]=(scaledPoints[i]*scale)+"px"
}this._stokeStyle=style
}if($defined(opacity)){this._native.setAttribute("stroke-opacity",opacity)
}};
web2d.peer.svg.ElementPeer.prototype.setVisibility=function(isVisible){this._native.setAttribute("visibility",(isVisible)?"visible":"hidden")
};
web2d.peer.svg.ElementPeer.prototype.isVisible=function(){var visibility=this._native.getAttribute("visibility");
return !(visibility=="hidden")
};
web2d.peer.svg.ElementPeer.prototype.updateStrokeStyle=function(){var strokeStyle=this._stokeStyle;
if(this.getParent()){if(strokeStyle&&strokeStyle!="solid"){this.setStroke(null,strokeStyle)
}}};
web2d.peer.svg.ElementPeer.prototype.attachChangeEventListener=function(type,listener){var listeners=this.getChangeEventListeners(type);
if(!$defined(listener)){throw"Listener can not be null"
}listeners.push(listener)
};
web2d.peer.svg.ElementPeer.prototype.getChangeEventListeners=function(type){var listeners=this._changeListeners[type];
if(!$defined(listeners)){listeners=[];
this._changeListeners[type]=listeners
}return listeners
};
web2d.peer.svg.ElementPeer.prototype.moveToFront=function(){this._native.parentNode.appendChild(this._native)
};
web2d.peer.svg.ElementPeer.prototype.moveToBack=function(){this._native.parentNode.insertBefore(this._native,this._native.parentNode.firstChild)
};
web2d.peer.svg.ElementPeer.prototype.setCursor=function(type){this._native.style.cursor=type
};web2d.peer.svg.ElipsePeer=function(){var svgElement=window.document.createElementNS(this.svgNamespace,"ellipse");
web2d.peer.svg.ElementPeer.call(this,svgElement);
this.attachChangeEventListener("strokeStyle",web2d.peer.svg.ElementPeer.prototype.updateStrokeStyle);
this._position={x:0,y:0}
};
objects.extend(web2d.peer.svg.ElipsePeer,web2d.peer.svg.ElementPeer);
web2d.peer.svg.ElipsePeer.prototype.setSize=function(width,height){web2d.peer.svg.ElipsePeer.superClass.setSize.call(this,width,height);
if($defined(width)){this._native.setAttribute("rx",width/2)
}if($defined(height)){this._native.setAttribute("ry",height/2)
}var pos=this.getPosition();
this.setPosition(pos.x,pos.y)
};
web2d.peer.svg.ElipsePeer.prototype.setPosition=function(cx,cy){var size=this.getSize();
cx=cx+size.width/2;
cy=cy+size.height/2;
if($defined(cx)){this._native.setAttribute("cx",cx)
}if($defined(cy)){this._native.setAttribute("cy",cy)
}};
web2d.peer.svg.ElipsePeer.prototype.getPosition=function(){return this._position
};web2d.peer.svg.Font=function(){this._size=10;
this._style="normal";
this._weight="normal"
};
web2d.peer.svg.Font.prototype.init=function(args){if($defined(args.size)){this._size=parseInt(args.size)
}if($defined(args.style)){this._style=args.style
}if($defined(args.weight)){this._weight=args.weight
}};
web2d.peer.svg.Font.prototype.getHtmlSize=function(scale){var result=0;
if(this._size==6){result=this._size*scale.height*43/32
}if(this._size==8){result=this._size*scale.height*42/32
}else{if(this._size==10){result=this._size*scale.height*42/32
}else{if(this._size==15){result=this._size*scale.height*42/32
}}}return result
};
web2d.peer.svg.Font.prototype.getGraphSize=function(scale){return this._size*43/32
};
web2d.peer.svg.Font.prototype.getSize=function(){return parseInt(this._size)
};
web2d.peer.svg.Font.prototype.getStyle=function(){return this._style
};
web2d.peer.svg.Font.prototype.getWeight=function(){return this._weight
};
web2d.peer.svg.Font.prototype.setSize=function(size){this._size=size
};
web2d.peer.svg.Font.prototype.setStyle=function(style){this._style=style
};
web2d.peer.svg.Font.prototype.setWeight=function(weight){this._weight=weight
};
web2d.peer.svg.Font.prototype.getWidthMargin=function(){var result=0;
if(this._size==10||this._size==6){result=4
}return result
};web2d.peer.svg.ArialFont=function(){web2d.peer.svg.Font.call(this);
this._fontFamily="Arial"
};
objects.extend(web2d.peer.svg.ArialFont,web2d.peer.svg.Font);
web2d.peer.svg.ArialFont.prototype.getFontFamily=function(){return this._fontFamily
};
web2d.peer.svg.ArialFont.prototype.getFont=function(){return web2d.Font.ARIAL
};web2d.peer.svg.PolyLinePeer=function(){var svgElement=window.document.createElementNS(this.svgNamespace,"polyline");
web2d.peer.svg.ElementPeer.call(this,svgElement);
this.setFill("none");
this.breakDistance=10
};
objects.extend(web2d.peer.svg.PolyLinePeer,web2d.peer.svg.ElementPeer);
web2d.peer.svg.PolyLinePeer.prototype.setFrom=function(x1,y1){this._x1=x1;
this._y1=y1;
this._updatePath()
};
web2d.peer.svg.PolyLinePeer.prototype.setTo=function(x2,y2){this._x2=x2;
this._y2=y2;
this._updatePath()
};
web2d.peer.svg.PolyLinePeer.prototype.setStrokeWidth=function(width){this._native.setAttribute("stroke-width",width)
};
web2d.peer.svg.PolyLinePeer.prototype.setColor=function(color){this._native.setAttribute("stroke",color)
};
web2d.peer.svg.PolyLinePeer.prototype.setStyle=function(style){this._style=style;
this._updatePath()
};
web2d.peer.svg.PolyLinePeer.prototype.getStyle=function(){return this._style
};
web2d.peer.svg.PolyLinePeer.prototype._updatePath=function(){if(this._style=="Curved"){this._updateMiddleCurvePath()
}else{if(this._style=="Straight"){this._updateStraightPath()
}else{this._updateCurvePath()
}}};
web2d.peer.svg.PolyLinePeer.prototype._updateStraightPath=function(){if($defined(this._x1)&&$defined(this._x2)&&$defined(this._y1)&&$defined(this._y2)){var path=web2d.PolyLine.buildStraightPath(this.breakDistance,this._x1,this._y1,this._x2,this._y2);
this._native.setAttribute("points",path)
}};
web2d.peer.svg.PolyLinePeer.prototype._updateMiddleCurvePath=function(){var x1=this._x1;
var y1=this._y1;
var x2=this._x2;
var y2=this._y2;
if($defined(x1)&&$defined(x2)&&$defined(y1)&&$defined(y2)){var diff=x2-x1;
var middlex=(diff/2)+x1;
var signx=1;
var signy=1;
if(diff<0){signx=-1
}if(y2<y1){signy=-1
}var path=x1+", "+y1+" "+(middlex-10*signx)+", "+y1+" "+middlex+", "+(y1+10*signy)+" "+middlex+", "+(y2-10*signy)+" "+(middlex+10*signx)+", "+y2+" "+x2+", "+y2;
this._native.setAttribute("points",path)
}};
web2d.peer.svg.PolyLinePeer.prototype._updateCurvePath=function(){if($defined(this._x1)&&$defined(this._x2)&&$defined(this._y1)&&$defined(this._y2)){var path=web2d.PolyLine.buildCurvedPath(this.breakDistance,this._x1,this._y1,this._x2,this._y2);
this._native.setAttribute("points",path)
}};web2d.peer.svg.CurvedLinePeer=function(){var svgElement=window.document.createElementNS(this.svgNamespace,"path");
web2d.peer.svg.ElementPeer.call(this,svgElement);
this._style={fill:"#495879"};
this._updateStyle();
this._customControlPoint_1=false;
this._customControlPoint_2=false;
this._control1=new core.Point();
this._control2=new core.Point();
this._lineStyle=true
};
objects.extend(web2d.peer.svg.CurvedLinePeer,web2d.peer.svg.ElementPeer);
web2d.peer.svg.CurvedLinePeer.prototype.setSrcControlPoint=function(control){this._customControlPoint_1=true;
var change=this._control1.x!=control.x||this._control1.y!=control.y;
if($defined(control.x)){this._control1=control;
this._control1.x=parseInt(this._control1.x);
this._control1.y=parseInt(this._control1.y)
}if(change){this._updatePath()
}};
web2d.peer.svg.CurvedLinePeer.prototype.setDestControlPoint=function(control){this._customControlPoint_2=true;
var change=this._control2.x!=control.x||this._control2.y!=control.y;
if($defined(control.x)){this._control2=control;
this._control2.x=parseInt(this._control2.x);
this._control2.y=parseInt(this._control2.y)
}if(change){this._updatePath()
}};
web2d.peer.svg.CurvedLinePeer.prototype.isSrcControlPointCustom=function(){return this._customControlPoint_1
};
web2d.peer.svg.CurvedLinePeer.prototype.isDestControlPointCustom=function(){return this._customControlPoint_2
};
web2d.peer.svg.CurvedLinePeer.prototype.setIsSrcControlPointCustom=function(isCustom){this._customControlPoint_1=isCustom
};
web2d.peer.svg.CurvedLinePeer.prototype.setIsDestControlPointCustom=function(isCustom){this._customControlPoint_2=isCustom
};
web2d.peer.svg.CurvedLinePeer.prototype.getControlPoints=function(){return[this._control1,this._control2]
};
web2d.peer.svg.CurvedLinePeer.prototype.setFrom=function(x1,y1){var change=this._x1!=parseInt(x1)||this._y1!=parseInt(y1);
this._x1=parseInt(x1);
this._y1=parseInt(y1);
if(change){this._updatePath()
}};
web2d.peer.svg.CurvedLinePeer.prototype.setTo=function(x2,y2){var change=this._x2!=parseInt(x2)||this._y2!=parseInt(y2);
this._x2=parseInt(x2);
this._y2=parseInt(y2);
if(change){this._updatePath()
}};
web2d.peer.svg.CurvedLinePeer.prototype.getFrom=function(){return new core.Point(this._x1,this._y1)
};
web2d.peer.svg.CurvedLinePeer.prototype.getTo=function(){return new core.Point(this._x2,this._y2)
};
web2d.peer.svg.CurvedLinePeer.prototype.setStrokeWidth=function(width){this._style["stroke-width"]=width;
this._updateStyle()
};
web2d.peer.svg.CurvedLinePeer.prototype.setColor=function(color){this._style.stroke=color;
this._style.fill=color;
this._updateStyle()
};
web2d.peer.svg.CurvedLinePeer.prototype.updateLine=function(avoidControlPointFix){this._updatePath(avoidControlPointFix)
};
web2d.peer.svg.CurvedLinePeer.prototype.setLineStyle=function(style){this._lineStyle=style;
if(this._lineStyle){this._style.fill=this._fill
}else{this._fill=this._style.fill;
this._style.fill="none"
}this._updateStyle();
this.updateLine()
};
web2d.peer.svg.CurvedLinePeer.prototype.getLineStyle=function(){return this._lineStyle
};
web2d.peer.svg.CurvedLinePeer.prototype.setShowEndArrow=function(visible){this._showEndArrow=visible;
this.updateLine()
};
web2d.peer.svg.CurvedLinePeer.prototype.isShowEndArrow=function(){return this._showEndArrow
};
web2d.peer.svg.CurvedLinePeer.prototype.setShowStartArrow=function(visible){this._showStartArrow=visible;
this.updateLine()
};
web2d.peer.svg.CurvedLinePeer.prototype.isShowStartArrow=function(){return this._showStartArrow
};
web2d.peer.svg.CurvedLinePeer.prototype._updatePath=function(avoidControlPointFix){if($defined(this._x1)&&$defined(this._y1)&&$defined(this._x2)&&$defined(this._y2)){this._calculateAutoControlPoints(avoidControlPointFix);
var path="M"+this._x1+","+this._y1+" C"+(this._control1.x+this._x1)+","+(this._control1.y+this._y1)+" "+(this._control2.x+this._x2)+","+(this._control2.y+this._y2)+" "+this._x2+","+this._y2+(this._lineStyle?" "+(this._control2.x+this._x2)+","+(this._control2.y+this._y2+3)+" "+(this._control1.x+this._x1)+","+(this._control1.y+this._y1+5)+" "+this._x1+","+(this._y1+7)+" Z":"");
this._native.setAttribute("d",path)
}};
web2d.peer.svg.CurvedLinePeer.prototype._updateStyle=function(){var style="";
for(var key in this._style){style+=key+":"+this._style[key]+" "
}this._native.setAttribute("style",style)
};
web2d.peer.svg.CurvedLinePeer.prototype._calculateAutoControlPoints=function(avoidControlPointFix){var defaultpoints=core.Utils.calculateDefaultControlPoints(new core.Point(this._x1,this._y1),new core.Point(this._x2,this._y2));
if(!this._customControlPoint_1&&!($defined(avoidControlPointFix)&&avoidControlPointFix==0)){this._control1.x=defaultpoints[0].x;
this._control1.y=defaultpoints[0].y
}if(!this._customControlPoint_2&&!($defined(avoidControlPointFix)&&avoidControlPointFix==1)){this._control2.x=defaultpoints[1].x;
this._control2.y=defaultpoints[1].y
}};
web2d.peer.svg.CurvedLinePeer.prototype.setDashed=function(length,spacing){if($defined(length)&&$defined(spacing)){this._native.setAttribute("stroke-dasharray",length+","+spacing)
}else{this._native.setAttribute("stroke-dasharray","")
}};web2d.peer.svg.ArrowPeer=function(){var svgElement=window.document.createElementNS(this.svgNamespace,"path");
web2d.peer.svg.ElementPeer.call(this,svgElement);
this._style={};
this._controlPoint=new core.Point();
this._fromPoint=new core.Point()
};
objects.extend(web2d.peer.svg.ArrowPeer,web2d.peer.svg.ElementPeer);
web2d.peer.svg.ArrowPeer.prototype.setFrom=function(x,y){this._fromPoint.x=x;
this._fromPoint.y=y;
this._redraw()
};
web2d.peer.svg.ArrowPeer.prototype.setControlPoint=function(point){this._controlPoint=point;
this._redraw()
};
web2d.peer.svg.ArrowPeer.prototype.setStrokeColor=function(color){this.setStroke(null,null,color,null)
};
web2d.peer.svg.ArrowPeer.prototype.setStrokeWidth=function(width){this.setStroke(width)
};
web2d.peer.svg.ArrowPeer.prototype.setDashed=function(isDashed,length,spacing){if($defined(isDashed)&&isDashed&&$defined(length)&&$defined(spacing)){this._native.setAttribute("stroke-dasharray",length+","+spacing)
}else{this._native.setAttribute("stroke-dasharray","")
}};
web2d.peer.svg.ArrowPeer.prototype._updateStyle=function(){var style="";
for(var key in this._style){style+=key+":"+this._style[key]+" "
}this._native.setAttribute("style",style)
};
web2d.peer.svg.ArrowPeer.prototype._redraw=function(){var x,y,xp,yp;
if(this._controlPoint.y==0){this._controlPoint.y=1
}var y0=this._controlPoint.y;
var x0=this._controlPoint.x;
var x2=x0+y0;
var y2=y0-x0;
var x3=x0-y0;
var y3=y0+x0;
var m=y2/x2;
var mp=y3/x3;
var l=6;
var pow=Math.pow;
x=(x2==0?0:Math.sqrt(pow(l,2)/(1+pow(m,2))));
x*=Math.sign(x2);
y=(x2==0?l*Math.sign(y2):m*x);
xp=(x3==0?0:Math.sqrt(pow(l,2)/(1+pow(mp,2))));
xp*=Math.sign(x3);
yp=(x3==0?l*Math.sign(y3):mp*xp);
var path="M"+this._fromPoint.x+","+this._fromPoint.y+" L"+(x+this._fromPoint.x)+","+(y+this._fromPoint.y)+"M"+this._fromPoint.x+","+this._fromPoint.y+" L"+(xp+this._fromPoint.x)+","+(yp+this._fromPoint.y);
this._native.setAttribute("d",path)
};web2d.peer.svg.TextPeer=function(){var svgElement=window.document.createElementNS(this.svgNamespace,"text");
web2d.peer.svg.ElementPeer.call(this,svgElement);
this._native.setAttribute("focusable","true");
this._position={x:0,y:0};
this._font=new web2d.Font("Arial",this)
};
objects.extend(web2d.peer.svg.TextPeer,web2d.peer.svg.ElementPeer);
web2d.peer.svg.TextPeer.prototype.appendChild=function(element){this._native.appendChild(element._native)
};
web2d.peer.svg.TextPeer.prototype.setText=function(text){text=core.Utils.escapeInvalidTags(text);
var child=this._native.firstChild;
if($defined(child)){this._native.removeChild(child)
}this._text=text;
var textNode=window.document.createTextNode(text);
this._native.appendChild(textNode)
};
web2d.peer.svg.TextPeer.prototype.getText=function(){return this._text
};
web2d.peer.svg.TextPeer.prototype.setPosition=function(x,y){this._position={x:x,y:y};
var height=this._font.getSize();
if($defined(this._parent)&&$defined(this._native.getBBox)){height=this.getHeight()
}var size=parseInt(height);
this._native.setAttribute("y",y+size*3/4);
this._native.setAttribute("x",x)
};
web2d.peer.svg.TextPeer.prototype.getPosition=function(){return this._position
};
web2d.peer.svg.TextPeer.prototype.setFont=function(font,size,style,weight){if($defined(font)){this._font=new web2d.Font(font,this)
}if($defined(style)){this._font.setStyle(style)
}if($defined(weight)){this._font.setWeight(weight)
}if($defined(size)){this._font.setSize(size)
}this._updateFontStyle()
};
web2d.peer.svg.TextPeer.prototype._updateFontStyle=function(){this._native.setAttribute("font-family",this._font.getFontFamily());
this._native.setAttribute("font-size",this._font.getGraphSize());
this._native.setAttribute("font-style",this._font.getStyle());
this._native.setAttribute("font-weight",this._font.getWeight());
var scale=this._font.getFontScale();
this._native.xFontScale=scale.toFixed(1)
};
web2d.peer.svg.TextPeer.prototype.setColor=function(color){this._native.setAttribute("fill",color)
};
web2d.peer.svg.TextPeer.prototype.getColor=function(){return this._native.getAttribute("fill")
};
web2d.peer.svg.TextPeer.prototype.setTextSize=function(size){this._font.setSize(size);
this._updateFontStyle()
};
web2d.peer.svg.TextPeer.prototype.setContentSize=function(width,height){this._native.xTextSize=width.toFixed(1)+","+height.toFixed(1)
};
web2d.peer.svg.TextPeer.prototype.setStyle=function(style){this._font.setStyle(style);
this._updateFontStyle()
};
web2d.peer.svg.TextPeer.prototype.setWeight=function(weight){this._font.setWeight(weight);
this._updateFontStyle()
};
web2d.peer.svg.TextPeer.prototype.setFontFamily=function(family){var oldFont=this._font;
this._font=new web2d.Font(family,this);
this._font.setSize(oldFont.getSize());
this._font.setStyle(oldFont.getStyle());
this._font.setWeight(oldFont.getWeight());
this._updateFontStyle()
};
web2d.peer.svg.TextPeer.prototype.getFont=function(){return{font:this._font.getFont(),size:parseInt(this._font.getSize()),style:this._font.getStyle(),weight:this._font.getWeight()}
};
web2d.peer.svg.TextPeer.prototype.setSize=function(size){this._font.setSize(size);
this._updateFontStyle()
};
web2d.peer.svg.TextPeer.prototype.getWidth=function(){var computedWidth=this._native.getBBox().width;
var width=parseInt(computedWidth);
width=width+this._font.getWidthMargin();
return width
};
web2d.peer.svg.TextPeer.prototype.getHeight=function(){var computedHeight=this._native.getBBox().height;
return parseInt(computedHeight)
};
web2d.peer.svg.TextPeer.prototype.getHtmlFontSize=function(){return this._font.getHtmlSize()
};web2d.peer.svg.WorkspacePeer=function(element){this._element=element;
var svgElement=window.document.createElementNS(this.svgNamespace,"svg");
web2d.peer.svg.ElementPeer.call(this,svgElement);
this._native.setAttribute("focusable","true");
this._native.setAttribute("id","workspace")
};
objects.extend(web2d.peer.svg.WorkspacePeer,web2d.peer.svg.ElementPeer);
web2d.peer.svg.WorkspacePeer.prototype.setCoordSize=function(width,height){var viewBox=this._native.getAttribute("viewBox");
var coords=[0,0,0,0];
if(viewBox!=null){coords=viewBox.split(/ /)
}if($defined(width)){coords[2]=width
}if($defined(height)){coords[3]=height
}this._native.setAttribute("viewBox",coords.join(" "));
this._native.setAttribute("preserveAspectRatio","none");
web2d.peer.utils.EventUtils.broadcastChangeEvent(this,"strokeStyle")
};
web2d.peer.svg.WorkspacePeer.prototype.getCoordSize=function(){var viewBox=this._native.getAttribute("viewBox");
var coords=[1,1,1,1];
if(viewBox!=null){coords=viewBox.split(/ /)
}return{width:coords[2],height:coords[3]}
};
web2d.peer.svg.WorkspacePeer.prototype.setCoordOrigin=function(x,y){var viewBox=this._native.getAttribute("viewBox");
var coords=[0,0,0,0];
if(viewBox!=null){coords=viewBox.split(/ /)
}if($defined(x)){coords[0]=x
}if($defined(y)){coords[1]=y
}this._native.setAttribute("viewBox",coords.join(" "))
};
web2d.peer.svg.WorkspacePeer.prototype.appendChild=function(child){web2d.peer.svg.WorkspacePeer.superClass.appendChild.call(this,child);
web2d.peer.utils.EventUtils.broadcastChangeEvent(child,"onChangeCoordSize")
};
web2d.peer.svg.WorkspacePeer.prototype.getCoordOrigin=function(child){var viewBox=this._native.getAttribute("viewBox");
var coords=[1,1,1,1];
if(viewBox!=null){coords=viewBox.split(/ /)
}var x=parseFloat(coords[0]);
var y=parseFloat(coords[1]);
return{x:x,y:y}
};
web2d.peer.svg.WorkspacePeer.prototype.getPosition=function(){return{x:0,y:0}
};web2d.peer.svg.GroupPeer=function(){var svgElement=window.document.createElementNS(this.svgNamespace,"g");
web2d.peer.svg.ElementPeer.call(this,svgElement);
this._native.setAttribute("preserveAspectRatio","none");
this._coordSize={width:1,height:1};
this._native.setAttribute("focusable","true");
this._position={x:0,y:0};
this._coordOrigin={x:0,y:0}
};
objects.extend(web2d.peer.svg.GroupPeer,web2d.peer.svg.ElementPeer);
web2d.peer.svg.GroupPeer.prototype.setCoordSize=function(width,height){var change=this._coordSize.width!=width||this._coordSize.height!=height;
this._coordSize.width=width;
this._coordSize.height=height;
if(change){this.updateTransform()
}web2d.peer.utils.EventUtils.broadcastChangeEvent(this,"strokeStyle")
};
web2d.peer.svg.GroupPeer.prototype.getCoordSize=function(){return{width:this._coordSize.width,height:this._coordSize.height}
};
web2d.peer.svg.GroupPeer.prototype.updateTransform=function(){var sx=this._size.width/this._coordSize.width;
var sy=this._size.height/this._coordSize.height;
var cx=this._position.x-this._coordOrigin.x*sx;
var cy=this._position.y-this._coordOrigin.y*sy;
this._native.setAttribute("transform","translate("+cx+","+cy+") scale("+sx+","+sy+")")
};
web2d.peer.svg.GroupPeer.prototype.setCoordOrigin=function(x,y){var change=x!=this._coordOrigin.x||y!=this._coordOrigin.y;
if($defined(x)){this._coordOrigin.x=x
}if($defined(y)){this._coordOrigin.y=y
}if(change){this.updateTransform()
}};
web2d.peer.svg.GroupPeer.prototype.setSize=function(width,height){var change=width!=this._size.width||height!=this._size.height;
web2d.peer.svg.GroupPeer.superClass.setSize.call(this,width,height);
if(change){this.updateTransform()
}};
web2d.peer.svg.GroupPeer.prototype.setPosition=function(x,y){var change=x!=this._position.x||y!=this._position.y;
if($defined(x)){this._position.x=parseInt(x)
}if($defined(y)){this._position.y=parseInt(y)
}if(change){this.updateTransform()
}};
web2d.peer.svg.GroupPeer.prototype.getPosition=function(){return{x:this._position.x,y:this._position.y}
};
web2d.peer.svg.GroupPeer.prototype.appendChild=function(child){web2d.peer.svg.GroupPeer.superClass.appendChild.call(this,child);
web2d.peer.utils.EventUtils.broadcastChangeEvent(child,"onChangeCoordSize")
};
web2d.peer.svg.GroupPeer.prototype.getCoordOrigin=function(){return{x:this._coordOrigin.x,y:this._coordOrigin.y}
};web2d.peer.svg.RectPeer=function(arc){var svgElement=window.document.createElementNS(this.svgNamespace,"rect");
web2d.peer.svg.ElementPeer.call(this,svgElement);
this._arc=arc;
this.attachChangeEventListener("strokeStyle",web2d.peer.svg.ElementPeer.prototype.updateStrokeStyle)
};
objects.extend(web2d.peer.svg.RectPeer,web2d.peer.svg.ElementPeer);
web2d.peer.svg.RectPeer.prototype.setPosition=function(x,y){if($defined(x)){this._native.setAttribute("x",parseInt(x))
}if($defined(y)){this._native.setAttribute("y",parseInt(y))
}};
web2d.peer.svg.RectPeer.prototype.getPosition=function(){var x=this._native.getAttribute("x");
var y=this._native.getAttribute("y");
return{x:parseInt(x),y:parseInt(y)}
};
web2d.peer.svg.RectPeer.prototype.setSize=function(width,height){web2d.peer.svg.RectPeer.superClass.setSize.call(this,width,height);
var min=width<height?width:height;
if($defined(this._arc)){var arc=(min/2)*this._arc;
this._native.setAttribute("rx",arc);
this._native.setAttribute("ry",arc)
}};web2d.peer.svg.ImagePeer=function(){var svgElement=window.document.createElementNS(this.svgNamespace,"image");
web2d.peer.svg.ElementPeer.call(this,svgElement);
this._position={x:0,y:0};
this._href=""
};
objects.extend(web2d.peer.svg.ImagePeer,web2d.peer.svg.ElementPeer);
web2d.peer.svg.ImagePeer.prototype.setPosition=function(x,y){this._position={x:x,y:y};
this._native.setAttribute("y",y);
this._native.setAttribute("x",x)
};
web2d.peer.svg.ImagePeer.prototype.getPosition=function(){return this._position
};
web2d.peer.svg.ImagePeer.prototype.setHref=function(url){this._native.setAttributeNS(this.linkNamespace,"href",url);
this._href=url
};
web2d.peer.svg.ImagePeer.prototype.getHref=function(){return this._href
};web2d.peer.svg.TimesFont=function(){web2d.peer.svg.Font.call(this);
this._fontFamily="times"
};
objects.extend(web2d.peer.svg.TimesFont,web2d.peer.svg.Font);
web2d.peer.svg.TimesFont.prototype.getFontFamily=function(){return this._fontFamily
};
web2d.peer.svg.TimesFont.prototype.getFont=function(){return web2d.Font.TIMES
};web2d.peer.svg.LinePeer=function(){var svgElement=window.document.createElementNS(this.svgNamespace,"line");
web2d.peer.svg.ElementPeer.call(this,svgElement);
this.attachChangeEventListener("strokeStyle",web2d.peer.svg.ElementPeer.prototype.updateStrokeStyle)
};
objects.extend(web2d.peer.svg.LinePeer,web2d.peer.svg.ElementPeer);
web2d.peer.svg.LinePeer.prototype.setFrom=function(x1,y1){this._x1=x1;
this._y1=y1;
this._native.setAttribute("x1",x1);
this._native.setAttribute("y1",y1)
};
web2d.peer.svg.LinePeer.prototype.setTo=function(x2,y2){this._x2=x2;
this._y2=y2;
this._native.setAttribute("x2",x2);
this._native.setAttribute("y2",y2)
};
web2d.peer.svg.LinePeer.prototype.getFrom=function(){return new core.Point(this._x1,this._y1)
};
web2d.peer.svg.LinePeer.prototype.getTo=function(){return new core.Point(this._x2,this._y2)
};
web2d.peer.svg.LinePeer.prototype.setArrowStyle=function(startStyle,endStyle){if($defined(startStyle)){}if($defined(endStyle)){}};web2d.peer.svg.TahomaFont=function(){web2d.peer.svg.Font.call(this);
this._fontFamily="tahoma"
};
objects.extend(web2d.peer.svg.TahomaFont,web2d.peer.svg.Font);
web2d.peer.svg.TahomaFont.prototype.getFontFamily=function(){return this._fontFamily
};
web2d.peer.svg.TahomaFont.prototype.getFont=function(){return web2d.Font.TAHOMA
};web2d.peer.svg.VerdanaFont=function(){web2d.peer.svg.Font.call(this);
this._fontFamily="verdana"
};
objects.extend(web2d.peer.svg.VerdanaFont,web2d.peer.svg.Font);
web2d.peer.svg.VerdanaFont.prototype.getFontFamily=function(){return this._fontFamily
};
web2d.peer.svg.VerdanaFont.prototype.getFont=function(){return web2d.Font.VERDANA
};web2d.Element=function(peer,attributes){this._peer=peer;
if(peer==null){throw"Element peer can not be null"
}this._dispatcherByEventType=new Hash({});
if($defined(attributes)){this._initialize(attributes)
}};
web2d.Element.prototype._SIGNATURE_MULTIPLE_ARGUMENTS=-1;
web2d.Element.prototype._initialize=function(attributes){var batchExecute={};
for(var key in attributes){var funcName=this._attributeNameToFuncName(key,"set");
var funcArgs=batchExecute[funcName];
if(!$defined(funcArgs)){funcArgs=[]
}var signature=this._propertyNameToSignature[key];
var argPositions=signature[1];
if(argPositions!=this._SIGNATURE_MULTIPLE_ARGUMENTS){funcArgs[argPositions]=attributes[key]
}else{funcArgs=attributes[key].split(" ")
}batchExecute[funcName]=funcArgs
}for(var key in batchExecute){var func=this[key];
if(!$defined(func)){throw"Could not find function: "+key
}func.apply(this,batchExecute[key])
}};
web2d.Element.prototype.setSize=function(width,height){this._peer.setSize(width,height)
};
web2d.Element.prototype.setPosition=function(cx,cy){this._peer.setPosition(cx,cy)
};
web2d.Element.prototype._supportedEvents=["click","dblclick","mousemove","mouseout","mouseover","mousedown","mouseup"];
web2d.Element.prototype.addEventListener=function(type,listener){if(!this._supportedEvents.include(type)){throw"Unsupported event type: "+type
}if(!this._dispatcherByEventType[type]){this._dispatcherByEventType[type]=new web2d.EventDispatcher(this);
var eventListener=this._dispatcherByEventType[type].eventListener;
this._peer.addEventListener(type,eventListener)
}this._dispatcherByEventType[type].addListener(type,listener)
};
web2d.Element.prototype.removeEventListener=function(type,listener){var dispatcher=this._dispatcherByEventType[type];
if(dispatcher==null){throw"There is no listener previously registered"
}var result=dispatcher.removeListener(type,listener);
if(dispatcher.getListenersCount()<=0){this._peer.removeEventListener(type,dispatcher.eventListener);
this._dispatcherByEventType[type]=null
}};
web2d.Element.prototype.getType=function(){throw"Not implemeneted yet. This method must be implemented by all the inherited objects."
};
web2d.Element.prototype.getFill=function(){return this._peer.getFill()
};
web2d.Element.prototype.setFill=function(color,opacity){this._peer.setFill(color,opacity)
};
web2d.Element.prototype.getPosition=function(){return this._peer.getPosition()
};
web2d.Element.prototype.setStroke=function(width,style,color,opacity){if(style!=null&&style!=undefined&&style!="dash"&&style!="dot"&&style!="solid"&&style!="longdash"&&style!="dashdot"){throw"Unsupported stroke style: '"+style+"'"
}this._peer.setStroke(width,style,color,opacity)
};
web2d.Element.prototype._propertyNameToSignature={size:["size",-1],width:["size",0,"width"],height:["size",1,"height"],position:["position",-1],x:["position",0,"x"],y:["position",1,"y"],stroke:["stroke",-1],strokeWidth:["stroke",0,"width"],strokeStyle:["stroke",1,"style"],strokeColor:["stroke",2,"color"],strokeOpacity:["stroke",3,"opacity"],fill:["fill",-1],fillColor:["fill",0,"color"],fillOpacity:["fill",1,"opacity"],coordSize:["coordSize",-1],coordSizeWidth:["coordSize",0,"width"],coordSizeHeight:["coordSize",1,"height"],coordOrigin:["coordOrigin",-1],coordOriginX:["coordOrigin",0,"x"],coordOriginY:["coordOrigin",1,"y"],visibility:["visibility",0],opacity:["opacity",0]};
web2d.Element.prototype._attributeNameToFuncName=function(attributeKey,prefix){var signature=this._propertyNameToSignature[attributeKey];
if(!$defined(signature)){throw"Unsupported attribute: "+attributeKey
}var firstLetter=signature[0].charAt(0);
return prefix+firstLetter.toUpperCase()+signature[0].substring(1)
};
web2d.Element.prototype.setAttribute=function(key,value){var funcName=this._attributeNameToFuncName(key,"set");
var signature=this._propertyNameToSignature[key];
if(signature==null){throw"Could not find the signature for:"+key
}var argPositions=signature[1];
var args=[];
if(argPositions!==this._SIGNATURE_MULTIPLE_ARGUMENTS){args[argPositions]=value
}else{if(typeof value=="array"){args=value
}else{var strValue=String(value);
args=strValue.split(" ")
}}var setter=this[funcName];
if(setter==null){throw"Could not find the function name:"+funcName
}setter.apply(this,args)
};
web2d.Element.prototype.getAttribute=function(key){var funcName=this._attributeNameToFuncName(key,"get");
var signature=this._propertyNameToSignature[key];
if(signature==null){throw"Could not find the signature for:"+key
}var getter=this[funcName];
if(getter==null){throw"Could not find the function name:"+funcName
}var getterResult=getter.apply(this,[]);
var attibuteName=signature[2];
if(!$defined(attibuteName)){throw"Could not find attribute mapping for:"+key
}var result=getterResult[attibuteName];
if(!$defined(result)){throw"Could not find attribute with name:"+attibuteName
}return result
};
web2d.Element.prototype.setOpacity=function(opacity){this._peer.setStroke(null,null,null,opacity);
this._peer.setFill(null,opacity)
};
web2d.Element.prototype.setVisibility=function(isVisible){this._peer.setVisibility(isVisible)
};
web2d.Element.prototype.isVisible=function(){return this._peer.isVisible()
};
web2d.Element.prototype.moveToFront=function(){this._peer.moveToFront()
};
web2d.Element.prototype.moveToBack=function(){this._peer.moveToBack()
};
web2d.Element.prototype.getStroke=function(){return this._peer.getStroke()
};
web2d.Element.prototype.setCursor=function(type){this._peer.setCursor(type)
};
web2d.Element.prototype.getParent=function(){return this._peer.getParent()
};web2d.Elipse=function(attributes){var peer=web2d.peer.Toolkit.createElipse();
var defaultAttributes={width:40,height:40,x:5,y:5,stroke:"1 solid black",fillColor:"blue"};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}web2d.Element.call(this,peer,defaultAttributes)
};
objects.extend(web2d.Elipse,web2d.Element);
web2d.Elipse.prototype.getType=function(){return"Elipse"
};
web2d.Elipse.prototype.getSize=function(){return this._peer.getSize()
};web2d.EventDispatcher=function(element){this._listeners=[];
var dispatcher=this;
this.eventListener=function(event){for(var i=0;
i<dispatcher._listeners.length;
i++){if(dispatcher._listeners[i]!=null){dispatcher._listeners[i].call(element,event||window.event)
}}}
};
web2d.EventDispatcher.prototype.addListener=function(type,listener){if(!$defined(listener)){throw"Listener can not be null."
}this._listeners.include(listener)
};
web2d.EventDispatcher.prototype.removeListener=function(type,listener){if(!$defined(listener)){throw"Listener can not be null."
}var length=this._listeners.length;
this._listeners.erase(listener);
var newLength=this._listeners.length;
if(newLength>=length){throw"There is not listener to remove"
}};
web2d.EventDispatcher.prototype.getListenersCount=function(){return this._listeners.length
};web2d.Font=function(fontFamily,textPeer){var font="web2d.peer.Toolkit.create"+fontFamily+"Font();";
this._peer=eval(font);
this._textPeer=textPeer
};
web2d.Font.prototype.getHtmlSize=function(){var scale=web2d.peer.utils.TransformUtil.workoutScale(this._textPeer);
return this._peer.getHtmlSize(scale)
};
web2d.Font.prototype.getGraphSize=function(){var scale=web2d.peer.utils.TransformUtil.workoutScale(this._textPeer);
return this._peer.getGraphSize(scale)
};
web2d.Font.prototype.getFontScale=function(){return web2d.peer.utils.TransformUtil.workoutScale(this._textPeer).height
};
web2d.Font.prototype.getSize=function(){return this._peer.getSize()
};
web2d.Font.prototype.getStyle=function(){return this._peer.getStyle()
};
web2d.Font.prototype.getWeight=function(){return this._peer.getWeight()
};
web2d.Font.prototype.getFontFamily=function(){return this._peer.getFontFamily()
};
web2d.Font.prototype.setSize=function(size){return this._peer.setSize(size)
};
web2d.Font.prototype.setStyle=function(style){return this._peer.setStyle(style)
};
web2d.Font.prototype.setWeight=function(weight){return this._peer.setWeight(weight)
};
web2d.Font.prototype.getFont=function(){return this._peer.getFont()
};
web2d.Font.prototype.getWidthMargin=function(){return this._peer.getWidthMargin()
};
web2d.Font.ARIAL="Arial";
web2d.Font.TIMES="Times";
web2d.Font.TAHOMA="Tahoma";
web2d.Font.VERDANA="Verdana";web2d.Group=function(attributes){var peer=web2d.peer.Toolkit.createGroup();
var defaultAttributes={width:50,height:50,x:50,y:50,coordOrigin:"0 0",coordSize:"50 50"};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}web2d.Element.call(this,peer,defaultAttributes)
};
objects.extend(web2d.Group,web2d.Element);
web2d.Group.prototype.removeChild=function(element){if(!$defined(element)){throw"Child element can not be null"
}if(element==this){throw"It's not posible to add the group as a child of itself"
}var elementType=element.getType();
if(elementType==null){throw"It seems not to be an element ->"+element
}this._peer.removeChild(element._peer)
};
web2d.Group.prototype.appendChild=function(element){if(!$defined(element)){throw"Child element can not be null"
}if(element==this){throw"It's not posible to add the group as a child of itself"
}var elementType=element.getType();
if(elementType==null){throw"It seems not to be an element ->"+element
}if(elementType=="Workspace"){throw"A group can not have a workspace as a child"
}this._peer.appendChild(element._peer)
};
web2d.Group.prototype.getType=function(){return"Group"
};
web2d.Group.prototype.setCoordSize=function(width,height){this._peer.setCoordSize(width,height)
};
web2d.Group.prototype.getCoordSize=function(){return this.peer.getCoordSize()
};
web2d.Group.prototype.setCoordOrigin=function(x,y){this._peer.setCoordOrigin(x,y)
};
web2d.Group.prototype.getCoordOrigin=function(){return this._peer.getCoordOrigin()
};
web2d.Group.prototype.getSize=function(){return this._peer.getSize()
};
web2d.Group.prototype.setFill=function(color,opacity){throw"Unsupported operation. Fill can not be set to a group"
};
web2d.Group.prototype.setStroke=function(width,style,color,opacity){throw"Unsupported operation. Stroke can not be set to a group"
};
web2d.Group.prototype.getCoordSize=function(){return this._peer.getCoordSize()
};
web2d.Group.prototype.appendDomChild=function(DomElement){if(!$defined(DomElement)){throw"Child element can not be null"
}if(DomElement==this){throw"It's not posible to add the group as a child of itself"
}this._peer._native.appendChild(DomElement)
};web2d.Image=function(attributes){var peer=web2d.peer.Toolkit.createImage();
web2d.Element.call(this,peer,attributes)
};
objects.extend(web2d.Image,web2d.Element);
web2d.Image.prototype.getType=function(){return"Image"
};
web2d.Image.prototype.setHref=function(href){this._peer.setHref(href)
};
web2d.Image.prototype.getHref=function(){return this._peer.getHref()
};
web2d.Image.prototype.getSize=function(){return this._peer.getSize()
};web2d.Line=function(attributes){var peer=web2d.peer.Toolkit.createLine();
var defaultAttributes={strokeColor:"#495879",strokeWidth:1,strokeOpacity:1};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}web2d.Element.call(this,peer,defaultAttributes)
};
objects.extend(web2d.Line,web2d.Element);
web2d.Line.prototype.getType=function(){return"Line"
};
web2d.Line.prototype.setFrom=function(x,y){this._peer.setFrom(x,y)
};
web2d.Line.prototype.setTo=function(x,y){this._peer.setTo(x,y)
};
web2d.Line.prototype.getFrom=function(){return this._peer.getFrom()
};
web2d.Line.prototype.getTo=function(){return this._peer.getTo()
};
web2d.Line.prototype.setArrowStyle=function(startStyle,endStyle){this._peer.setArrowStyle(startStyle,endStyle)
};
web2d.Line.prototype.setPosition=function(cx,cy){throw"Unsupported operation"
};
web2d.Line.prototype.setSize=function(width,height){throw"Unsupported operation"
};
web2d.Line.prototype.setFill=function(color,opacity){throw"Unsupported operation"
};web2d.PolyLine=function(attributes){var peer=web2d.peer.Toolkit.createPolyLine();
var defaultAttributes={strokeColor:"blue",strokeWidth:1,strokeStyle:"solid",strokeOpacity:1};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}web2d.Element.call(this,peer,defaultAttributes)
};
objects.extend(web2d.PolyLine,web2d.Element);
web2d.PolyLine.prototype.getType=function(){return"PolyLine"
};
web2d.PolyLine.prototype.setFrom=function(x,y){this._peer.setFrom(x,y)
};
web2d.PolyLine.prototype.setTo=function(x,y){this._peer.setTo(x,y)
};
web2d.PolyLine.prototype.setStyle=function(style){this._peer.setStyle(style)
};
web2d.PolyLine.prototype.getStyle=function(){return this._peer.getStyle()
};
web2d.PolyLine.buildCurvedPath=function(dist,x1,y1,x2,y2){var signx=1;
var signy=1;
if(x2<x1){signx=-1
}if(y2<y1){signy=-1
}var path;
if(Math.abs(y1-y2)>2){var middlex=x1+((x2-x1>0)?dist:-dist);
path=x1.toFixed(1)+", "+y1.toFixed(1)+" "+middlex.toFixed(1)+", "+y1.toFixed(1)+" "+middlex.toFixed(1)+", "+(y2-5*signy).toFixed(1)+" "+(middlex+5*signx).toFixed(1)+", "+y2.toFixed(1)+" "+x2.toFixed(1)+", "+y2.toFixed(1)
}else{path=x1.toFixed(1)+", "+y1.toFixed(1)+" "+x2.toFixed(1)+", "+y2.toFixed(1)
}return path
};
web2d.PolyLine.buildStraightPath=function(dist,x1,y1,x2,y2){var middlex=x1+((x2-x1>0)?dist:-dist);
return x1+", "+y1+" "+middlex+", "+y1+" "+middlex+", "+y2+" "+x2+", "+y2
};web2d.CurvedLine=function(attributes){var peer=web2d.peer.Toolkit.createCurvedLine();
var defaultAttributes={strokeColor:"blue",strokeWidth:1,strokeStyle:"solid",strokeOpacity:1};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}web2d.Element.call(this,peer,defaultAttributes)
};
objects.extend(web2d.CurvedLine,web2d.Element);
web2d.CurvedLine.prototype.getType=function(){return"CurvedLine"
};
web2d.CurvedLine.prototype.setFrom=function(x,y){this._peer.setFrom(x,y)
};
web2d.CurvedLine.prototype.setTo=function(x,y){this._peer.setTo(x,y)
};
web2d.CurvedLine.prototype.getFrom=function(){return this._peer.getFrom()
};
web2d.CurvedLine.prototype.getTo=function(){return this._peer.getTo()
};
web2d.CurvedLine.prototype.setShowEndArrow=function(visible){this._peer.setShowEndArrow(visible)
};
web2d.CurvedLine.prototype.isShowEndArrow=function(){return this._peer.isShowEndArrow()
};
web2d.CurvedLine.prototype.setShowStartArrow=function(visible){this._peer.setShowStartArrow(visible)
};
web2d.CurvedLine.prototype.isShowStartArrow=function(){return this._peer.isShowStartArrow()
};
web2d.CurvedLine.prototype.setSrcControlPoint=function(control){this._peer.setSrcControlPoint(control)
};
web2d.CurvedLine.prototype.setDestControlPoint=function(control){this._peer.setDestControlPoint(control)
};
web2d.CurvedLine.prototype.getControlPoints=function(){return this._peer.getControlPoints()
};
web2d.CurvedLine.prototype.isSrcControlPointCustom=function(){return this._peer.isSrcControlPointCustom()
};
web2d.CurvedLine.prototype.isDestControlPointCustom=function(){return this._peer.isDestControlPointCustom()
};
web2d.CurvedLine.prototype.setIsSrcControlPointCustom=function(isCustom){this._peer.setIsSrcControlPointCustom(isCustom)
};
web2d.CurvedLine.prototype.setIsDestControlPointCustom=function(isCustom){this._peer.setIsDestControlPointCustom(isCustom)
};
web2d.CurvedLine.prototype.updateLine=function(avoidControlPointFix){return this._peer.updateLine(avoidControlPointFix)
};
web2d.CurvedLine.prototype.setStyle=function(style){this._peer.setLineStyle(style)
};
web2d.CurvedLine.prototype.getStyle=function(){return this._peer.getLineStyle()
};
web2d.CurvedLine.prototype.setDashed=function(length,spacing){this._peer.setDashed(length,spacing)
};
web2d.CurvedLine.SIMPLE_LINE=false;
web2d.CurvedLine.NICE_LINE=true;web2d.Arrow=function(attributes){var peer=web2d.peer.Toolkit.createArrow();
var defaultAttributes={strokeColor:"black",strokeWidth:1,strokeStyle:"solid",strokeOpacity:1};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}web2d.Element.call(this,peer,defaultAttributes)
};
objects.extend(web2d.Arrow,web2d.Element);
web2d.Arrow.prototype.getType=function(){return"Arrow"
};
web2d.Arrow.prototype.setFrom=function(x,y){this._peer.setFrom(x,y)
};
web2d.Arrow.prototype.setControlPoint=function(point){this._peer.setControlPoint(point)
};
web2d.Arrow.prototype.setStrokeColor=function(color){this._peer.setStrokeColor(color)
};
web2d.Arrow.prototype.setStrokeWidth=function(width){this._peer.setStrokeWidth(width)
};
web2d.Arrow.prototype.setDashed=function(isDashed,length,spacing){this._peer.setDashed(isDashed,length,spacing)
};
web2d.Arrow.prototype.reDraw=function(){this._peer._redraw()
};web2d.Rect=function(arc,attributes){if(arc&&arc>1){throw"Arc must be 0<=arc<=1"
}if(arguments.length<=0){var rx=0;
var ry=0
}var peer=web2d.peer.Toolkit.createRect(arc);
var defaultAttributes={width:40,height:40,x:5,y:5,stroke:"1 solid black",fillColor:"green"};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}web2d.Element.call(this,peer,defaultAttributes)
};
objects.extend(web2d.Rect,web2d.Element);
web2d.Rect.prototype.getType=function(){return"Rect"
};
web2d.Rect.prototype.getSize=function(){return this._peer.getSize()
};web2d.Text=function(attributes){var peer=web2d.peer.Toolkit.createText();
web2d.Element.call(this,peer,attributes)
};
objects.extend(web2d.Text,web2d.Element);
web2d.Text.prototype.getType=function(){return"Text"
};
web2d.Text.prototype.setText=function(text){this._peer.setText(text)
};
web2d.Text.prototype.setTextSize=function(width,height){this._peer.setContentSize(width,height)
};
web2d.Text.prototype.getText=function(){return this._peer.getText()
};
web2d.Text.prototype.setFont=function(font,size,style,weight){this._peer.setFont(font,size,style,weight)
};
web2d.Text.prototype.setColor=function(color){this._peer.setColor(color)
};
web2d.Text.prototype.getColor=function(){return this._peer.getColor()
};
web2d.Text.prototype.setStyle=function(style){this._peer.setStyle(style)
};
web2d.Text.prototype.setWeight=function(weight){this._peer.setWeight(weight)
};
web2d.Text.prototype.setFontFamily=function(family){this._peer.setFontFamily(family)
};
web2d.Text.prototype.getFont=function(){return this._peer.getFont()
};
web2d.Text.prototype.setSize=function(size){this._peer.setSize(size)
};
web2d.Text.prototype.getHtmlFontSize=function(){return this._peer.getHtmlFontSize()
};
web2d.Text.prototype.getWidth=function(){return this._peer.getWidth()
};
web2d.Text.prototype.getHeight=function(){return parseInt(this._peer.getHeight())
};web2d.peer.ToolkitSVG={init:function(){},createWorkspace:function(element){return new web2d.peer.svg.WorkspacePeer(element)
},createGroup:function(element){return new web2d.peer.svg.GroupPeer()
},createElipse:function(){return new web2d.peer.svg.ElipsePeer()
},createLine:function(){return new web2d.peer.svg.LinePeer()
},createPolyLine:function(){return new web2d.peer.svg.PolyLinePeer()
},createCurvedLine:function(){return new web2d.peer.svg.CurvedLinePeer()
},createArrow:function(){return new web2d.peer.svg.ArrowPeer()
},createText:function(){return new web2d.peer.svg.TextPeer()
},createImage:function(){return new web2d.peer.svg.ImagePeer()
},createRect:function(arc){return new web2d.peer.svg.RectPeer(arc)
},createArialFont:function(){return new web2d.peer.svg.ArialFont()
},createTimesFont:function(){return new web2d.peer.svg.TimesFont()
},createVerdanaFont:function(){return new web2d.peer.svg.VerdanaFont()
},createTahomaFont:function(){return new web2d.peer.svg.TahomaFont()
}};
web2d.peer.Toolkit=web2d.peer.ToolkitSVG;web2d.Workspace=function(attributes){this._htmlContainer=this._createDivContainer();
var peer=web2d.peer.Toolkit.createWorkspace(this._htmlContainer);
var defaultAttributes={width:"200px",height:"200px",stroke:"1px solid #edf1be",fillColor:"white",coordOrigin:"0 0",coordSize:"200 200"};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}web2d.Element.call(this,peer,defaultAttributes);
this._htmlContainer.appendChild(this._peer._native);
this._disableTextSelection()
};
objects.extend(web2d.Workspace,web2d.Element);
web2d.Workspace.prototype._disableTextSelection=function(){var contaier=this._htmlContainer;
function disabletext(e){return false
}function reEnable(){return true
}contaier.onselectstart=new Function("return false");
if($defined(window.sidebar)){contaier.onmousedown=disabletext;
contaier.onclick=reEnable
}};
web2d.Workspace.prototype.getType=function(){return"Workspace"
};
web2d.Workspace.prototype.appendChild=function(element){if(!$defined(element)){throw"Child element can not be null"
}var elementType=element.getType();
if(elementType==null){throw"It seems not to be an element ->"+element
}if(elementType=="Workspace"){throw"A workspace can not have a workspace as a child"
}this._peer.appendChild(element._peer)
};
web2d.Workspace.prototype.addItAsChildTo=function(element){if(!$defined(element)){throw"Workspace div container can not be null"
}element.appendChild(this._htmlContainer)
};
web2d.Workspace.prototype._createDivContainer=function(domElement){var container=window.document.createElement("div");
container.id="workspaceContainer";
container.style.overflow="hidden";
container.style.position="relative";
container.style.top="0px";
container.style.left="0px";
container.style.height="688px";
container.style.border="1px solid red";
return container
};
web2d.Workspace.prototype.setSize=function(width,height){if($defined(width)){this._htmlContainer.style.width=width
}if($defined(height)){this._htmlContainer.style.height=height
}this._peer.setSize(width,height)
};
web2d.Workspace.prototype.setCoordSize=function(width,height){this._peer.setCoordSize(width,height)
};
web2d.Workspace.prototype.setCoordOrigin=function(x,y){this._peer.setCoordOrigin(x,y)
};
web2d.Workspace.prototype.getCoordOrigin=function(){return this._peer.getCoordOrigin()
};
web2d.Workspace.prototype._getHtmlContainer=function(){return this._htmlContainer
};
web2d.Workspace.prototype.setFill=function(color,opacity){this._htmlContainer.style.backgroundColor=color;
if(opacity||opacity===0){throw"Unsupported operation. Opacity not supported."
}};
web2d.Workspace.prototype.getFill=function(){var color=this._htmlContainer.style.backgroundColor;
return{color:color}
};
web2d.Workspace.prototype.getSize=function(){var width=this._htmlContainer.style.width;
var height=this._htmlContainer.style.height;
return{width:width,height:height}
};
web2d.Workspace.prototype.setStroke=function(width,style,color,opacity){if(style!="solid"){throw"Not supported style stroke style:"+style
}this._htmlContainer.style.border=width+" "+style+" "+color;
if(opacity||opacity===0){throw"Unsupported operation. Opacity not supported."
}};
web2d.Workspace.prototype.getCoordSize=function(){return this._peer.getCoordSize()
};
web2d.Workspace.prototype.removeChild=function(element){if(!$defined(element)){throw"Child element can not be null"
}if(element==this){throw"It's not posible to add the group as a child of itself"
}var elementType=element.getType();
if(elementType==null){throw"It seems not to be an element ->"+element
}this._peer.removeChild(element._peer)
};
web2d.Workspace.prototype.dumpNativeChart=function(){var elem=this._htmlContainer;
return elem.innerHTML
};/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

// (C) Copyright 2007 WiseMapping.com. All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF WiseMapping.com
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
// ....................................................................

var mindplot = {};
mindplot.util = {};
mindplot.commands = {};
mindplot.layout = {};mindplot.EventBus = new Class({
    Extends:Options,
    Implements:Events,
    options: {

    },
    initialize: function(options) {
        this.setOptions(options);
    }

});

mindplot.EventBus.events = {
    NodeResizeEvent:'NodeResizeEvent',
    NodeMoveEvent:'NodeMoveEvent',
    NodeDisconnectEvent:'NodeDisconnectEvent',
    NodeConnectEvent:'NodeConnectEvent',
    NodeRepositionateEvent:'NodeRepositionateEvent',
    NodeShrinkEvent:'NodeShrinkEvent',
    NodeMouseOverEvent:'NodeMouseOverEvent',
    NodeMouseOutEvent:'NodeMouseOutEvent'
};

mindplot.EventBus.instance = new mindplot.EventBus();/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
mindplot.model = {};
mindplot.model.Mindmap = new Class({
        initialize : function() {
            this._branches = [];
            this._description = null;
            this._version = null;
            this._relationships = [];
        },

        getCentralTopic : function() {
            return this._branches[0];
        },

        getDescription : function() {
            return this._description;
        },

        getId : function() {
            return this._iconType;
        },


        setId : function(id) {
            this._iconType = id;
        },

        getVersion : function() {
            return this._version;
        },


        setVersion : function(version) {
            this._version = version;
        },

        addBranch : function(nodeModel) {
            $assert(nodeModel && nodeModel.isNodeModel(), 'Add node must be invoked with model objects');
            if (this._branches.length == 0) {
                $assert(nodeModel.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE, "First element must be the central topic");
                nodeModel.setPosition(0, 0);
            } else {
                $assert(nodeModel.getType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE, "Mindmaps only have one cental topic");
            }

            this._branches.push(nodeModel);
        },

        getBranches : function() {
            return this._branches;
        },

        getRelationships : function() {
            return this._relationships;
        },

        connect : function(parent, child) {
            // Child already has a parent ?
            var branches = this.getBranches();
            $assert(!child.getParent(), 'Child model seems to be already connected');

            //  Connect node...
            parent._appendChild(child);

            // Remove from the branch ...
            branches.erase(child);
        },

        disconnect : function(child) {
            var parent = child.getParent();
            $assert(child, 'Child can not be null.');
            $assert(parent, 'Child model seems to be already connected');

            parent._removeChild(child);

            var branches = this.getBranches();
            branches.push(child);

        },

        hasAlreadyAdded : function(node) {
            var result = false;

            // Check in not connected nodes.
            var branches = this._branches;
            for (var i = 0; i < branches.length; i++) {
                result = branches[i]._isChildNode(node);
                if (result) {
                    break;
                }
            }
        },

        createNode : function(type, id) {
            $assert(type, "node type can not be null");
            return this._createNode(type, id);
        },

        _createNode : function(type, id) {
            $assert(type, 'Node type must be specified.');
            var result = new mindplot.model.NodeModel(type, this, id);
            return result;
        },

        createRelationship : function(fromNode, toNode) {
            $assert(fromNode, 'from node cannot be null');
            $assert(toNode, 'to node cannot be null');

            return new mindplot.model.RelationshipModel(fromNode, toNode);
        },

        addRelationship : function(relationship) {
            this._relationships.push(relationship);
        },

        removeRelationship : function(relationship) {
            this._relationships.erase(relationship);
        },

        inspect : function() {
            var result = '';
            result = '{ ';

            var branches = this.getBranches();
            for (var i = 0; i < branches.length; i++) {
                var node = branches[i];
                if (i != 0) {
                    result = result + ', ';
                }

                result = result + this._toString(node);
            }

            result = result + ' } ';

            return result;
        },

        _toString : function(node) {
            var result = node.inspect();
            var children = node.getChildren();

            for (var i = 0; i < children.length; i++) {
                var child = children[i];

                if (i == 0) {
                    result = result + '-> {';
                } else {
                    result = result + ', ';
                }

                result = result + this._toString(child);

                if (i == children.length - 1) {
                    result = result + '}';
                }
            }

            return result;
        }
    }
);/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.model.NodeModel = new Class({
    initialize:function(type, mindmap, id) {
        $assert(type, 'Node type can not be null');
        $assert(mindmap, 'mindmap can not be null');

        this._order = null;
        this._type = type;
        this._children = [];
        this._icons = [];
        this._links = [];
        this._notes = [];
        this._size = {width:50,height:20};
        this._position = null;
        if ($defined(id)) {
            if (!$defined(mindplot.model.NodeModel._uuid) || id > mindplot.model.NodeModel._uuid) {
                mindplot.model.NodeModel._uuid = id;
            }
            this._id = id;
        } else {
            this._id = mindplot.model.NodeModel._nextUUID();
        }
        this._mindmap = mindmap;
        this._text = null;
        this._shapeType = null;
        this._fontFamily = null;
        this._fontSize = null;
        this._fontStyle = null;
        this._fontWeight = null;
        this._fontColor = null;
        this._borderColor = null;
        this._backgroundColor = null;
        this._areChildrenShrinked = false;
    },

    clone  : function() {
        var result = new mindplot.model.NodeModel(this._type, this._mindmap);
        result._order = this._order;
        result._type = this._type;
        result._children = this._children.map(function(item, index) {
            var model = item.clone();
            model._parent = result;
            return model;
        });


        result._icons = this._icons;
        result._links = this._links;
        result._notes = this._notes;
        result._size = this._size;
        result._position = this._position;
        result._id = this._id;
        result._mindmap = this._mindmap;
        result._text = this._text;
        result._shapeType = this._shapeType;
        result._fontFamily = this._fontFamily;
        result._fontSize = this._fontSize;
        result._fontStyle = this._fontStyle;
        result._fontWeight = this._fontWeight;
        result._fontColor = this._fontColor;
        result._borderColor = this._borderColor;
        result._backgroundColor = this._backgroundColor;
        result._areChildrenShrinked = this._areChildrenShrinked;
        return result;
    },

    areChildrenShrinked  : function() {
        return this._areChildrenShrinked;
    },

    setChildrenShrinked  : function(value) {
        this._areChildrenShrinked = value;
    },

    getId  : function() {
        return this._id;
    },


    setId  : function(id) {
        this._id = id;
        if (mindplot.model.NodeModel._uuid < id) {
            mindplot.model.NodeModel._uuid = id;
        }
    },

    getType  : function() {
        return this._type;
    },

    setText  : function(text) {
        this._text = text;
    },

    getText  : function() {
        return this._text;
    },

    isNodeModel  : function() {
        return true;
    },

    isConnected  : function() {
        return this._parent != null;
    },

    createLink  : function(url) {
        $assert(url, 'Link URL must be specified.');
        return new mindplot.model.LinkModel(url, this);
    },

    addLink  : function(link) {
        $assert(link && link.isLinkModel(), 'Only LinkModel can be appended to Mindmap object as links');
        this._links.push(link);
    },

    _removeLink  : function(link) {
        $assert(link && link.isLinkModel(), 'Only LinkModel can be appended to Mindmap object as links');
        this._links.erase(link);
    },

    createNote  : function(text) {
        $assert(text != null, 'note text must be specified.');
        return new mindplot.model.NoteModel(text, this);
    },

    addNote  : function(note) {
        $assert(note && note.isNoteModel(), 'Only NoteModel can be appended to Mindmap object as links');
        this._notes.push(note);
    },

    _removeNote  : function(note) {
        $assert(note && note.isNoteModel(), 'Only NoteModel can be appended to Mindmap object as links');
        this._notes.erase(note);
    },

    createIcon  : function(iconType) {
        $assert(iconType, 'IconType must be specified.');
        return new mindplot.model.IconModel(iconType, this);
    },

    addIcon  : function(icon) {
        $assert(icon && icon.isIconModel(), 'Only IconModel can be appended to Mindmap object as icons');
        this._icons.push(icon);
    },

    _removeIcon  : function(icon) {
        $assert(icon && icon.isIconModel(), 'Only IconModel can be appended to Mindmap object as icons');
        this._icons.erase(icon);
    },

    removeLastIcon  : function() {
        this._icons.pop();
    },

    _appendChild  : function(child) {
        $assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object');
        this._children.push(child);
        child._parent = this;
    },

    _removeChild  : function(child) {
        $assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object.');
        this._children.erase(child);
        child._parent = null;
    },

    setPosition  : function(x, y) {
        $assert(x, "x coordinate must be defined");
        $assert(y, "y coordinate must be defined");

        if (!$defined(this._position)) {
            this._position = new core.Point();
        }
        this._position.x = parseInt(x);
        this._position.y = parseInt(y);
    },

    getPosition  : function() {
        return this._position;
    },

    setFinalPosition  : function(x, y) {
        $assert(x, "x coordinate must be defined");
        $assert(y, "y coordinate must be defined");

        if (!$defined(this._finalPosition)) {
            this._finalPosition = new core.Point();
        }
        this._finalPosition.x = parseInt(x);
        this._finalPosition.y = parseInt(y);
    },

    getFinalPosition  : function() {
        return this._finalPosition;
    },

    setSize  : function(width, height) {
        this._size.width = width;
        this._size.height = height;
    },

    getSize  : function() {
        return {width:this._size.width,height:this._size.height};
    },

    getChildren  : function() {
        return this._children;
    },

    getIcons  : function() {
        return this._icons;
    },

    getLinks  : function() {
        return this._links;
    },

    getNotes  : function() {
        return this._notes;
    },

    getParent  : function() {
        return this._parent;
    },

    getMindmap  : function() {
        return this._mindmap;
    },

    setParent  : function(parent) {
        $assert(parent != this, 'The same node can not be parent and child if itself.');
        this._parent = parent;
    },

    canBeConnected  : function(sourceModel, sourcePosition, targetTopicHeight) {
        $assert(sourceModel != this, 'The same node can not be parent and child if itself.');
        $assert(sourcePosition, 'childPosition can not be null.');
        $assert(targetTopicHeight, 'childrenWidth can not be null.');

        // Only can be connected if the node is in the left or rigth.
        var targetModel = this;
        var mindmap = targetModel.getMindmap();
        var targetPosition = targetModel.getPosition();
        var result = false;

        if (sourceModel.getType() == mindplot.model.NodeModel.MAIN_TOPIC_TYPE) {
            // Finally, check current node ubication.
            var targetTopicSize = targetModel.getSize();
            var yDistance = Math.abs(sourcePosition.y - targetPosition.y);
            var gap = 35 + targetTopicHeight / 2;
            if (targetModel.getChildren().length > 0) {
                gap += Math.abs(targetPosition.y - targetModel.getChildren()[0].getPosition().y);
            }

            if (yDistance <= gap) {
                // Circular connection ?
                if (!sourceModel._isChildNode(this)) {
                    var toleranceDistance = (targetTopicSize.width / 2) + targetTopicHeight;

                    var xDistance = sourcePosition.x - targetPosition.x;
                    var isTargetAtRightFromCentral = targetPosition.x >= 0;

                    if (isTargetAtRightFromCentral) {
                        if (xDistance >= -targetTopicSize.width / 2 && xDistance <= mindplot.model.NodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE / 2 + (targetTopicSize.width / 2)) {
                            result = true;
                        }

                    } else {
                        if (xDistance <= targetTopicSize.width / 2 && Math.abs(xDistance) <= mindplot.model.NodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE / 2 + (targetTopicSize.width / 2)) {
                            result = true;
                        }
                    }
                }
            }
        } else {
            throw "No implemented yet";
        }
        return result;
    },

    _isChildNode  : function(node) {
        var result = false;
        if (node == this) {
            result = true;
        } else {
            var children = this.getChildren();
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                result = child._isChildNode(node);
                if (result) {
                    break;
                }
            }
        }
        return result;

    },

    connectTo  : function(parent) {
        var mindmap = this.getMindmap();
        mindmap.connect(parent, this);
        this._parent = parent;
    },

    disconnect  : function() {
        var mindmap = this.getMindmap();
        mindmap.disconnect(this);
    },

    getOrder  : function() {
        return this._order;
    },

    getShapeType  : function() {
        return this._shapeType;
    },

    setShapeType  : function(type) {
        this._shapeType = type;
    },

    setOrder  : function(value) {
        this._order = value;
    },

    setFontFamily  : function(value) {
        this._fontFamily = value;
    },

    getOrder  : function() {
        return this._order;
    },

    getFontFamily  : function() {
        return this._fontFamily;
    },

    setFontStyle  : function(value) {
        this._fontStyle = value;
    },

    getFontStyle  : function() {
        return this._fontStyle;
    },

    setFontWeight  : function(value) {
        this._fontWeight = value;
    },

    getFontWeight  : function() {
        return this._fontWeight;
    },

    setFontColor  : function(value) {
        this._fontColor = value;
    },

    getFontColor  : function() {
        return this._fontColor;
    },

    setFontSize  : function(value) {
        this._fontSize = value;
    },

    getFontSize  : function() {
        return this._fontSize;
    },

    getBorderColor  : function() {
        return this._borderColor;
    },

    setBorderColor  : function(color) {
        this._borderColor = color;
    },

    getBackgroundColor  : function() {
        return this._backgroundColor;
    },

    setBackgroundColor  : function(color) {
        this._backgroundColor = color;
    },

    deleteNode  : function() {
        var mindmap = this._mindmap;

        // if it has children nodes, Their must be disconnected.
        var lenght = this._children;
        for (var i = 0; i < lenght; i++) {
            var child = this._children[i];
            mindmap.disconnect(child);
        }

        var parent = this._parent;
        if ($defined(parent)) {
            // if it is connected, I must remove it from the parent..
            mindmap.disconnect(this);
        }

        // It's an isolated node. It must be a hole branch ...
        var branches = mindmap.getBranches();
        branches.erase(this);

    },

   inspect  : function() {
        return '(type:' + this.getType() + ' , id: ' + this.getId() + ')';
    }
});

mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE = 'CentralTopic';
mindplot.model.NodeModel.MAIN_TOPIC_TYPE = 'MainTopic';
mindplot.model.NodeModel.DRAGGED_TOPIC_TYPE = 'DraggedTopic';

mindplot.model.NodeModel.SHAPE_TYPE_RECT = 'rectagle';
mindplot.model.NodeModel.SHAPE_TYPE_ROUNDED_RECT = 'rounded rectagle';
mindplot.model.NodeModel.SHAPE_TYPE_ELIPSE = 'elipse';
mindplot.model.NodeModel.SHAPE_TYPE_LINE = 'line';

mindplot.model.NodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE = 220;

/**
 * @todo: This method must be implemented.
 */
mindplot.model.NodeModel._nextUUID = function() {
    if (!$defined(this._uuid)) {
        this._uuid = 0;
    }

    this._uuid = this._uuid + 1;
    return this._uuid;
}

/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
mindplot.model.RelationshipModel = new Class({
    initialize:function(fromNode, toNode) {
        $assert(fromNode, 'from node type can not be null');
        $assert(toNode, 'to node type can not be null');

        this._id = mindplot.model.RelationshipModel._nextUUID();
        this._fromNode = fromNode;
        this._toNode = toNode;
        this._lineType = mindplot.ConnectionLine.SIMPLE_CURVED;
        this._srcCtrlPoint = null;
        this._destCtrlPoint = null;
        this._endArrow = true;
        this._startArrow = false;
    },

    getFromNode : function() {
        return this._fromNode;
    },

    getToNode : function() {
        return this._toNode;
    },

    getId : function() {
        return this._id;
    },

    getLineType : function() {
        return this._lineType;
    },

    setLineType : function(lineType) {
        this._lineType = lineType;
    },

    getSrcCtrlPoint : function() {
        return this._srcCtrlPoint;
    },

    setSrcCtrlPoint : function(srcCtrlPoint) {
        this._srcCtrlPoint = srcCtrlPoint;
    },

    getDestCtrlPoint : function() {
        return this._destCtrlPoint;
    },

    setDestCtrlPoint : function(destCtrlPoint) {
        this._destCtrlPoint = destCtrlPoint;
    },

    getEndArrow : function() {
        return this._endArrow;
    },

    setEndArrow : function(endArrow) {
        this._endArrow = endArrow;
    },

    getStartArrow : function() {
        return this._startArrow;
    },

    setStartArrow : function(startArrow) {
        this._startArrow = startArrow;
    },

    clone : function(model) {
        var result = new mindplot.model.RelationshipModel(this._fromNode, this._toNode);
        result._id = this._id;
        result._lineType = this._lineType;
        result._srcCtrlPoint = this._srcCtrlPoint;
        result._destCtrlPoint = this._destCtrlPoint;
        result._endArrow = this._endArrow;
        result._startArrow = this._startArrow;
        return result;
    },

    inspect : function() {
        return '(fromNode:' + this.getFromNode().getId() + ' , toNode: ' + this.getToNode().getId() + ')';
    }
});


/**
 * @todo: This method must be implemented.
 */
mindplot.model.RelationshipModel._nextUUID = function() {
    if (!$defined(this._uuid)) {
        this._uuid = 0;
    }

    this._uuid = this._uuid + 1;
    return this._uuid;
}

/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.MindmapDesigner = new Class({
        initialize: function(profile, divElement) {
            $assert(profile, "profile must be defined");
            $assert(profile.zoom, "zoom must be defined");
            $assert(divElement, "divElement must be defined");

            // Undo manager ...
            this._actionRunner = new mindplot.DesignerActionRunner(this);
            mindplot.DesignerActionRunner.setInstance(this._actionRunner);

            // Initial Zoom
            this._zoom = profile.zoom;
            this._viewMode = profile.viewMode;

            // Init Screen manager..
            var screenManager = new mindplot.ScreenManager(profile.width, profile.height, divElement);

            this._workspace = new mindplot.Workspace(profile, screenManager, this._zoom);

            //create editor
            var editorClass = mindplot.TextEditorFactory.getTextEditorFromName(mindplot.EditorOptions.textEditor);
            this._editor = new editorClass(this, this._actionRunner);


            // Init layout managers ...
            this._topics = [];
//    var layoutManagerClass = mindplot.layout.LayoutManagerFactory.getManagerByName(mindplot.EditorOptions.LayoutManager);
//    this._layoutManager = new layoutManagerClass(this);
            this._layoutManager = new mindplot.layout.OriginalLayoutManager(this);

            // Register handlers..
            this._registerEvents();

            this._relationships = {};

            this._events = {};
        },

        _getTopics : function() {
            return this._topics;
        },

        getCentralTopic : function() {
            var topics = this._getTopics();
            return topics[0];
        },


        addEventListener : function(eventType, listener) {

            this._events[eventType] = listener;

        },

        _fireEvent : function(eventType, event) {
            var listener = this._events[eventType];
            if (listener != null) {
                listener(event);
            }
        },

        _registerEvents : function() {
            var mindmapDesigner = this;
            var workspace = this._workspace;
            var screenManager = workspace.getScreenManager();

            if (!$defined(this._viewMode) || ($defined(this._viewMode) && !this._viewMode)) {
                // Initialize workspace event listeners.
                // Create nodes on double click...
                screenManager.addEventListener('click', function(event) {
                    if (workspace.isWorkspaceEventsEnabled()) {
                        var t = mindmapDesigner.getEditor().isVisible();
                        mindmapDesigner.getEditor().lostFocus();
                        // @todo: Puaj hack...
                        mindmapDesigner._cleanScreen();
                    }
                });

                screenManager.addEventListener('dblclick', function(event) {
                    if (workspace.isWorkspaceEventsEnabled()) {
                        mindmapDesigner.getEditor().lostFocus();
                        // Get mouse position
                        var pos = screenManager.getWorkspaceMousePosition(event);

                        // Create a new topic model ...
                        var mindmap = mindmapDesigner.getMindmap();
                        var model = mindmap.createNode(mindplot.model.NodeModel.MAIN_TOPIC_TYPE);
                        model.setPosition(pos.x, pos.y);

                        // Get central topic ...
                        var centralTopic = mindmapDesigner.getCentralTopic();
                        var centralTopicId = centralTopic.getId();

                        // Execute action ...
                        var command = new mindplot.commands.AddTopicCommand(model, centralTopicId, true);
                        this._actionRunner.execute(command);
                    }
                }.bind(this));
            }
        },

        _buildNodeGraph : function(model) {
            var workspace = this._workspace;

            // Create node graph ...
            var topic = mindplot.NodeGraph.create(model);

            this._layoutManager.addHelpers(topic);

            // Append it to the workspace ...
            var topics = this._topics;
            topics.push(topic);

            // Add Topic events ...
            this._layoutManager.registerListenersOnNode(topic);

            // Connect Topic ...
            var isConnected = model.isConnected();
            if (isConnected) {
                // Improve this ...
                var targetTopicModel = model.getParent();
                var targetTopicId = targetTopicModel.getId();
                var targetTopic = null;

                for (var i = 0; i < topics.length; i++) {
                    var t = topics[i];
                    if (t.getModel() == targetTopicModel) {
                        targetTopic = t;
                        // Disconnect the node. It will be connected again later ...
                        model.disconnect();
                        break;
                    }
                }
                $assert(targetTopic, "Could not find a topic to connect");
                topic.connectTo(targetTopic, workspace);
            }

            return  topic;
        },

        onObjectFocusEvent : function(currentObject, event) {
            this.getEditor().lostFocus();
            var selectableObjects = this.getSelectedObjects();
            // Disable all nodes on focus but not the current if Ctrl key isn't being pressed
            if (!$defined(event) || event.ctrlKey == false) {
                for (var i = 0; i < selectableObjects.length; i++) {
                    var selectableObject = selectableObjects[i];
                    if (selectableObject.isOnFocus() && selectableObject != currentObject) {
                        selectableObject.setOnFocus(false);
                    }
                }
            }
        },

        zoomOut : function() {
            var scale = this._zoom * 1.2;
            if (scale <= 4) {
                this._zoom = scale;
                this._workspace.setZoom(this._zoom);
            }
            else {
                core.Monitor.getInstance().logMessage('Sorry, no more zoom can be applied. \n Why do you need more?');
            }

        },

        zoomIn : function() {
            var scale = this._zoom / 1.2;
            if (scale >= 0.3) {
                this._zoom = scale;
                this._workspace.setZoom(this._zoom);
            }
            else {
                core.Monitor.getInstance().logMessage('Sorry, no more zoom can be applied. \n Why do you need more?');
            }
        },

        createChildForSelectedNode : function() {

            var nodes = this._getSelectedNodes();
            if (nodes.length <= 0) {
                // If there are more than one node selected,
                core.Monitor.getInstance().logMessage('Could not create a topic. Only one node must be selected.');
                return;

            }
            if (nodes.length > 1) {

                // If there are more than one node selected,
                core.Monitor.getInstance().logMessage('Could not create a topic. One topic must be selected.');
                return;
            }

            // Add new node ...
            var centalTopic = nodes[0];
            var parentTopicId = centalTopic.getId();
            var childModel = centalTopic.createChildModel(this._layoutManager.needsPrepositioning());

            var command = new mindplot.commands.AddTopicCommand(childModel, parentTopicId, true);
            this._actionRunner.execute(command);
        },

        createSiblingForSelectedNode : function() {
            var nodes = this._getSelectedNodes();
            if (nodes.length <= 0) {
                // If there are more than one node selected,
                core.Monitor.getInstance().logMessage('Could not create a topic. Only one node must be selected.');
                return;

            }
            if (nodes.length > 1) {
                // If there are more than one node selected,
                core.Monitor.getInstance().logMessage('Could not create a topic. One topic must be selected.');
                return;
            }

            var topic = nodes[0];
            if (topic.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                // Central topic doesn't have siblings ...
                this.createChildForSelectedNode();

            } else {
                var parentTopic = topic.getOutgoingConnectedTopic();
                var siblingModel = topic.createSiblingModel(this._layoutManager.needsPrepositioning());
                var parentTopicId = parentTopic.getId();
                var command = new mindplot.commands.AddTopicCommand(siblingModel, parentTopicId, true);

                this._actionRunner.execute(command);
            }
        },

        addRelationShip2SelectedNode : function(event) {
            var screen = this._workspace.getScreenManager();
            var pos = screen.getWorkspaceMousePosition(event);
            var selectedTopics = this.getSelectedNodes();
            if (selectedTopics.length > 0 &&
                (!$defined(this._creatingRelationship) || ($defined(this._creatingRelationship) && !this._creatingRelationship))) {
                this._workspace.enableWorkspaceEvents(false);
                var fromNodePosition = selectedTopics[0].getPosition();
                this._relationship = new web2d.CurvedLine();
                this._relationship.setStyle(web2d.CurvedLine.SIMPLE_LINE);
                this._relationship.setDashed(2, 2);
                this._relationship.setFrom(fromNodePosition.x, fromNodePosition.y);
                this._relationship.setTo(pos.x, pos.y);
                this._workspace.appendChild(this._relationship);
                this._creatingRelationship = true;
                this._relationshipMouseMoveFunction = this._relationshipMouseMove.bindWithEvent(this);
                this._relationshipMouseClickFunction = this._relationshipMouseClick.bindWithEvent(this, selectedTopics[0]);
                this._workspace.getScreenManager().addEventListener('mousemove', this._relationshipMouseMoveFunction);
                this._workspace.getScreenManager().addEventListener('click', this._relationshipMouseClickFunction);
            }
        },

        _relationshipMouseMove : function(event) {
            var screen = this._workspace.getScreenManager();
            var pos = screen.getWorkspaceMousePosition(event);
            this._relationship.setTo(pos.x - 1, pos.y - 1); //to prevent click event target to be the line itself
            event.preventDefault();
            event.stop();
            return false;
        },

        _relationshipMouseClick : function (event, fromNode) {
            var target = event.target;
            while (target.tagName != "g" && $defined(target.parentNode)) {
                target = target.parentNode;
            }
            if ($defined(target.virtualRef)) {
                var targetNode = target.virtualRef;
                this.addRelationship(fromNode, targetNode);
            }
            this._workspace.removeChild(this._relationship);
            this._relationship = null;
            this._workspace.getScreenManager().removeEventListener('mousemove', this._relationshipMouseMoveFunction);
            this._workspace.getScreenManager().removeEventListener('click', this._relationshipMouseClickFunction);
            this._creatingRelationship = false;
            this._workspace.enableWorkspaceEvents(true);
            event.preventDefault();
            event.stop();
            return false;
        },

        addRelationship : function(fromNode, toNode) {
            // Create a new topic model ...
            var mindmap = this.getMindmap();
            var model = mindmap.createRelationship(fromNode.getModel().getId(), toNode.getModel().getId());

            var command = new mindplot.commands.AddRelationshipCommand(model, mindmap);
            this._actionRunner.execute(command);
        },

        needsSave : function() {
            return this._actionRunner.hasBeenChanged();
        },

        autoSaveEnabled : function(value) {
            if ($defined(value) && value) {
                var autosave = function() {

                    if (this.needsSave()) {
                        this.save(null, false);
                    }
                };
                autosave.bind(this).periodical(30000);
            }
        },

        save : function(onSavedHandler, saveHistory) {
            var persistantManager = mindplot.PersistanceManager;
            var mindmap = this._mindmap;

            var properties = {zoom:this._zoom, layoutManager:this._layoutManager.getClassName()};
            persistantManager.save(mindmap, properties, onSavedHandler, saveHistory);
            this._fireEvent("save", {type:saveHistory});

            // Refresh undo state...
            this._actionRunner.markAsChangeBase();
        },

        loadFromCollaborativeModel: function(collaborationManager){
            var mindmap = collaborationManager.buildWiseModel();
            this._loadMap(1, mindmap);

            // Place the focus on the Central Topic
            var centralTopic = this.getCentralTopic();
            this._goToNode.attempt(centralTopic, this);

            this._fireEvent("loadsuccess");
        },

        loadFromXML : function(mapId, xmlContent) {
            $assert(xmlContent, 'mindmapId can not be null');
            $assert(xmlContent, 'xmlContent can not be null');

            // Explorer Hack with local files ...
            var domDocument = core.Utils.createDocumentFromText(xmlContent);

            var serializer = mindplot.XMLMindmapSerializerFactory.getSerializerFromDocument(domDocument);
            var mindmap = serializer.loadFromDom(domDocument);

            this._loadMap(mapId, mindmap);

            // Place the focus on the Central Topic
            var centralTopic = this.getCentralTopic();
            this._goToNode.attempt(centralTopic, this);

            this._fireEvent("loadsuccess");

        },

        load : function(mapId) {
            $assert(mapId, 'mapName can not be null');

            // Build load function ...
            var persistantManager = mindplot.PersistanceManager;

            // Loading mindmap ...
            var mindmap = persistantManager.load(mapId);

            // Finally, load the map in the editor ...
            this._loadMap(mapId, mindmap);

            // Place the focus on the Central Topic
            var centralTopic = this.getCentralTopic();
            this._goToNode.attempt(centralTopic, this);

            this._fireEvent("loadsuccess");
        },

        _loadMap : function(mapId, mindmapModel) {
            var designer = this;
            if (mindmapModel != null) {
                mindmapModel.setId(mapId);
                designer._mindmap = mindmapModel;

                // Building node graph ...
                var branches = mindmapModel.getBranches();
                for (var i = 0; i < branches.length; i++) {
                    // NodeModel -> NodeGraph ...
                    var nodeModel = branches[i];
                    var nodeGraph = this._nodeModelToNodeGraph(nodeModel);

                    // Update shrink render state...
                    nodeGraph.setBranchVisibility(true);
                }
                var relationships = mindmapModel.getRelationships();
                for (var j = 0; j < relationships.length; j++) {
                    var relationship = this._relationshipModelToRelationship(relationships[j]);
                }
            }
            core.Executor.instance.setLoading(false);
            this._getTopics().forEach(function(topic) {
                delete topic.getModel()._finalPosition;
            });
            this._fireEvent("loadsuccess");

        },


        getMindmap : function() {
            return this._mindmap;
        },

        undo : function() {
            this._actionRunner.undo();
        },

        redo : function() {
            this._actionRunner.redo();
        },

        _nodeModelToNodeGraph : function(nodeModel, isVisible) {
            $assert(nodeModel, "Node model can not be null");
            var nodeGraph = this._buildNodeGraph(nodeModel);

            if ($defined(isVisible))
                nodeGraph.setVisibility(isVisible);

            var children = nodeModel.getChildren().slice();

            children = this._layoutManager.prepareNode(nodeGraph, children);

            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                if ($defined(child))
                    this._nodeModelToNodeGraph(child);
            }

            var workspace = this._workspace;
            workspace.appendChild(nodeGraph);
            return nodeGraph;
        },

        _relationshipModelToRelationship : function(model) {
            $assert(model, "Node model can not be null");
            var relationship = this._buildRelationship(model);
            var sourceTopic = relationship.getSourceTopic();
            sourceTopic.addRelationship(relationship);
            var targetTopic = relationship.getTargetTopic();
            targetTopic.addRelationship(relationship);
            relationship.setVisibility(sourceTopic.isVisible() && targetTopic.isVisible());
            var workspace = this._workspace;
            workspace.appendChild(relationship);
            relationship.redraw();
            return relationship;
        },

        createRelationship : function(model) {
            this._mindmap.addRelationship(model);
            return this._relationshipModelToRelationship(model);
        },

        removeRelationship : function(model) {
            this._mindmap.removeRelationship(model);
            var relationship = this._relationships[model.getId()];
            var sourceTopic = relationship.getSourceTopic();
            sourceTopic.removeRelationship(relationship);
            var targetTopic = relationship.getTargetTopic();
            targetTopic.removeRelationship(relationship);
            this._workspace.removeChild(relationship);
            delete this._relationships[model.getId()];
        },

        _buildRelationship : function (model) {
            var workspace = this._workspace;
            var elem = this;

            var fromNodeId = model.getFromNode();
            var toNodeId = model.getToNode();

            var fromTopic = null;
            var toTopic = null;
            var topics = this._topics;

            for (var i = 0; i < topics.length; i++) {
                var t = topics[i];
                if (t.getModel().getId() == fromNodeId) {
                    fromTopic = t;
                }
                if (t.getModel().getId() == toNodeId) {
                    toTopic = t;
                }
                if (toTopic != null && fromTopic != null) {
                    break;
                }
            }

            // Create node graph ...
            var relationLine = new mindplot.RelationshipLine(fromTopic, toTopic, model.getLineType());
            if ($defined(model.getSrcCtrlPoint())) {
                var srcPoint = model.getSrcCtrlPoint().clone();
                relationLine.setSrcControlPoint(srcPoint);
            }
            if ($defined(model.getDestCtrlPoint())) {
                var destPoint = model.getDestCtrlPoint().clone();
                relationLine.setDestControlPoint(destPoint);
            }


            relationLine.getLine().setDashed(3, 2);
            relationLine.setShowEndArrow(model.getEndArrow());
            relationLine.setShowStartArrow(model.getStartArrow());
            relationLine.setModel(model);

            //Add Listeners
            var elem = this;
            relationLine.addEventListener('onfocus', function(event) {
                elem.onObjectFocusEvent.attempt([relationLine, event], elem);
            });

            // Append it to the workspace ...
            this._relationships[model.getId()] = relationLine;

            return  relationLine;
        },

        getEditor : function() {
            return this._editor;
        },

        _removeNode : function(node) {
            if (node.getTopicType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                var parent = node._parent;
                node.disconnect(this._workspace);

                //remove children
                while (node._getChildren().length > 0) {
                    this._removeNode(node._getChildren()[0]);
                }

                this._workspace.removeChild(node);
                this._topics.erase(node);

                // Delete this node from the model...
                var model = node.getModel();
                model.deleteNode();

                if ($defined(parent)) {
                    this._goToNode(parent);
                }
            }
        },

        deleteCurrentNode : function() {

            var validateFunc = function(selectedObject) {
                return selectedObject.getType() == mindplot.RelationshipLine.type || selectedObject.getTopicType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE
            };
            var validateError = 'Central topic can not be deleted.';
            var selectedObjects = this._getValidSelectedObjectsIds(validateFunc, validateError);
            if (selectedObjects.nodes.length > 0 || selectedObjects.relationshipLines.length > 0) {
                var command = new mindplot.commands.DeleteTopicCommand(selectedObjects);
                this._actionRunner.execute(command);
            }

        },

        setFont2SelectedNode : function(font) {
            var validSelectedObjects = this._getValidSelectedObjectsIds();
            var topicsIds = validSelectedObjects.nodes;
            if (topicsIds.length > 0) {
                var commandFunc = function(topic, font) {
                    var result = topic.getFontFamily();
                    topic.setFontFamily(font, true);

                    core.Executor.instance.delay(topic.updateNode, 0, topic);
                    /*var updated = function() {
                     topic.updateNode();
                     };
                     updated.delay(0);*/
                    return result;
                }
                var command = new mindplot.commands.GenericFunctionCommand(commandFunc, font, topicsIds);
                this._actionRunner.execute(command);
            }
        },

        setStyle2SelectedNode : function() {
            var validSelectedObjects = this._getValidSelectedObjectsIds();
            var topicsIds = validSelectedObjects.nodes;
            if (topicsIds.length > 0) {
                var commandFunc = function(topic) {
                    var result = topic.getFontStyle();
                    var style = (result == "italic") ? "normal" : "italic";
                    topic.setFontStyle(style, true);
                    return result;
                }
                var command = new mindplot.commands.GenericFunctionCommand(commandFunc, "", topicsIds);
                this._actionRunner.execute(command);
            }
        },

        setFontColor2SelectedNode : function(color) {
            var validSelectedObjects = this._getValidSelectedObjectsIds();
            var topicsIds = validSelectedObjects.nodes;
            if (topicsIds.length > 0) {
                var commandFunc = function(topic, color) {
                    var result = topic.getFontColor();
                    topic.setFontColor(color, true);
                    return result;
                }
                var command = new mindplot.commands.GenericFunctionCommand(commandFunc, color, topicsIds);
                command.discartDuplicated = "fontColorCommandId";
                this._actionRunner.execute(command);
            }
        },

        setBackColor2SelectedNode : function(color) {

            var validateFunc = function(topic) {
                return topic.getShapeType() != mindplot.model.NodeModel.SHAPE_TYPE_LINE
            };
            var validateError = 'Color can not be setted to line topics.';
            var validSelectedObjects = this._getValidSelectedObjectsIds(validateFunc, validateError);;
            var topicsIds = validSelectedObjects.nodes;

            if (topicsIds.length > 0) {
                var commandFunc = function(topic, color) {
                    var result = topic.getBackgroundColor();
                    topic.setBackgroundColor(color);
                    return result;
                }
                var command = new mindplot.commands.GenericFunctionCommand(commandFunc, color, topicsIds);
                command.discartDuplicated = "backColor";
                this._actionRunner.execute(command);
            }
        },


        _getValidSelectedObjectsIds : function(validate, errorMsg) {
            var result = {"nodes":[],"relationshipLines":[]};
            var selectedNodes = this._getSelectedNodes();
            var selectedRelationshipLines = this.getSelectedRelationshipLines();
            if (selectedNodes.length == 0 && selectedRelationshipLines.length == 0) {
                core.Monitor.getInstance().logMessage('At least one element must be selected to execute this operation.');
            } else {
                var isValid = true;
                for (var i = 0; i < selectedNodes.length; i++) {
                    var selectedNode = selectedNodes[i];
                    if ($defined(validate)) {
                        isValid = validate(selectedNode);
                    }

                    // Add node only if it's valid.
                    if (isValid) {
                        result.nodes.push(selectedNode.getId());
                    } else {
                        core.Monitor.getInstance().logMessage(errorMsg);
                    }
                }
                for (var j = 0; j < selectedRelationshipLines.length; j++) {
                    var selectedLine = selectedRelationshipLines[j];
                    isValid = true;
                    if ($defined(validate)) {
                        isValid = validate(selectedLine);
                    }

                    if (isValid) {
                        result.relationshipLines.push(selectedLine.getId());
                    } else {
                        core.Monitor.getInstance().logMessage(errorMsg);
                    }
                }
            }
            return result;
        },

        setBorderColor2SelectedNode : function(color) {
            var validateFunc = function(topic) {
                return topic.getShapeType() != mindplot.model.NodeModel.SHAPE_TYPE_LINE
            };
            var validateError = 'Color can not be setted to line topics.';
            var validSelectedObjects = this._getValidSelectedObjectsIds(validateFunc, validateError);
            ;
            var topicsIds = validSelectedObjects.nodes;

            if (topicsIds.length > 0) {
                var commandFunc = function(topic, color) {
                    var result = topic.getBorderColor();
                    topic.setBorderColor(color);
                    return result;
                }
                var command = new mindplot.commands.GenericFunctionCommand(commandFunc, color, topicsIds);
                command.discartDuplicated = "borderColorCommandId";
                this._actionRunner.execute(command);
            }
        },

        setFontSize2SelectedNode : function(size) {
            var validSelectedObjects = this._getValidSelectedObjectsIds();
            var topicsIds = validSelectedObjects.nodes;
            if (topicsIds.length > 0) {
                var commandFunc = function(topic, size) {
                    var result = topic.getFontSize();
                    topic.setFontSize(size, true);

                    core.Executor.instance.delay(topic.updateNode, 0, topic);
                    /*var updated = function() {
                     topic.updateNode();
                     };
                     updated.delay(0);*/
                    return result;
                }
                var command = new mindplot.commands.GenericFunctionCommand(commandFunc, size, topicsIds);
                this._actionRunner.execute(command);
            }
        },

        setShape2SelectedNode : function(shape) {
            var validateFunc = function(topic) {
                return !(topic.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE && shape == mindplot.model.NodeModel.SHAPE_TYPE_LINE)
            };
            var validateError = 'Central Topic shape can not be changed to line figure.';
            var validSelectedObjects = this._getValidSelectedObjectsIds(validateFunc, validateError);
            var topicsIds = validSelectedObjects.nodes;

            if (topicsIds.length > 0) {
                var commandFunc = function(topic, size) {
                    var result = topic.getShapeType();
                    topic.setShapeType(size, true);
                    return result;
                }
                var command = new mindplot.commands.GenericFunctionCommand(commandFunc, shape, topicsIds);
                this._actionRunner.execute(command);
            }
        },


        setWeight2SelectedNode : function() {
            var validSelectedObjects = this._getValidSelectedObjectsIds();
            var topicsIds = validSelectedObjects.nodes;
            if (topicsIds.length > 0) {
                var commandFunc = function(topic) {
                    var result = topic.getFontWeight();
                    var weight = (result == "bold") ? "normal" : "bold";
                    topic.setFontWeight(weight, true);

                    core.Executor.instance.delay(topic.updateNode, 0, topic);
                    /*var updated = function() {
                     topic.updateNode();
                     };
                     updated.delay(0);*/
                    return result;
                }
                var command = new mindplot.commands.GenericFunctionCommand(commandFunc, "", topicsIds);
                this._actionRunner.execute(command);
            }
        },

        addImage2SelectedNode : function(iconType) {
            var validSelectedObjects = this._getValidSelectedObjectsIds();
            var topicsIds = validSelectedObjects.nodes;
            if (topicsIds.length > 0) {

                var command = new mindplot.commands.AddIconToTopicCommand(topicsIds[0], iconType);
                this._actionRunner.execute(command);
            }
        },

        addLink2Node : function(url) {
            var validSelectedObjects = this._getValidSelectedObjectsIds();
            var topicsIds = validSelectedObjects.nodes;
            if (topicsIds.length > 0) {
                var command = new mindplot.commands.AddLinkToTopicCommand(topicsIds[0], url);
                this._actionRunner.execute(command);
            }
        },

        addLink2SelectedNode : function() {
            var selectedTopics = this.getSelectedNodes();
            var topic = null;
            if (selectedTopics.length > 0) {
                topic = selectedTopics[0];
                if (!$defined(topic._hasLink)) {
                    var msg = new Element('div');
                    var urlText = new Element('div').inject(msg);
                    urlText.innerHTML = "URL:"
                    var formElem = new Element('form', {'action': 'none', 'id':'linkFormId'});
                    var urlInput = new Element('input', {'type': 'text', 'size':30});
                    urlInput.inject(formElem);
                    formElem.inject(msg)

                    var okButtonId = "linkOkButtonId";
                    formElem.addEvent('submit', function(e) {
                        $(okButtonId).fireEvent('click', e);
                        e = new Event(e);
                        e.stop();
                    });


                    var okFunction = function() {
                        var url = urlInput.value;
                        var result = false;
                        if ("" != url.trim()) {
                            this.addLink2Node(url);
                            result = true;
                        }
                        return result;
                    }.bind(this);
                    var dialog = mindplot.LinkIcon.buildDialog(this, okFunction, okButtonId);
                    dialog.adopt(msg).show();

                    // IE doesn't like too much this focus action...
                    if (!core.UserAgent.isIE()) {
                        urlInput.focus();
                    }
                }
            } else {
                core.Monitor.getInstance().logMessage('At least one topic must be selected to execute this operation.');
            }
        },

        addNote2Node : function(text) {
            var validSelectedObjects = this._getValidSelectedObjectsIds();
            var topicsIds = validSelectedObjects.nodes;
            if (topicsIds.length > 0) {
                var command = new mindplot.commands.AddNoteToTopicCommand(topicsIds[0], text);
                this._actionRunner.execute(command);
            }
        },

        addNote2SelectedNode : function() {
            var selectedTopics = this.getSelectedNodes();
            var topic = null;
            if (selectedTopics.length > 0) {
                topic = selectedTopics[0];
                if (!$defined(topic._hasNote)) {
                    var msg = new Element('div');
                    var text = new Element('div').inject(msg);
                    var formElem = new Element('form', {'action': 'none', 'id':'noteFormId'});
                    var textInput = new Element('textarea').setStyles({'width':280, 'height':50});
                    textInput.inject(formElem);
                    formElem.inject(msg);

                    var okButtonId = "noteOkButtonId";
                    formElem.addEvent('submit', function(e) {
                        $(okButtonId).fireEvent('click', e);
                        e = new Event(e);
                        e.stop();
                    });


                    var okFunction = function() {
                        var text = textInput.value;
                        var result = false;
                        if ("" != text.trim()) {
                            this.addNote2Node(text);
                            result = true;
                        }
                        return result;
                    }.bind(this);
                    var dialog = mindplot.Note.buildDialog(this, okFunction, okButtonId);
                    dialog.adopt(msg).show();

                    // IE doesn't like too much this focus action...
                    if (!core.UserAgent.isIE()) {
                        textInput.focus();
                    }
                }
            } else {
                core.Monitor.getInstance().logMessage('At least one topic must be selected to execute this operation.');
            }
        },

        removeLastImageFromSelectedNode : function() {
            var nodes = this._getSelectedNodes();
            if (nodes.length == 0) {
                core.Monitor.getInstance().logMessage('A topic must be selected in order to execute this operation.');
            } else {
                var elem = nodes[0];
                elem.removeLastIcon(this);
                core.Executor.instance.delay(elem.updateNode, 0, elem);
                /*var executor = function(editor)
                 {
                 return function()
                 {
                 elem.updateNode();
                 };
                 };

                 setTimeout(executor(this), 0);*/
            }
        },


        _getSelectedNodes : function() {
            var result = new Array();
            for (var i = 0; i < this._topics.length; i++) {
                if (this._topics[i].isOnFocus()) {
                    result.push(this._topics[i]);
                }
            }
            return result;
        },

        getSelectedRelationshipLines : function() {
            var result = new Array();
            for (var id in this._relationships) {
                var relationship = this._relationships[id];
                if (relationship.isOnFocus()) {
                    result.push(relationship);
                }
            }
            return result;
        },

        getSelectedNodes : function() {
            return this._getSelectedNodes();
        },

        getSelectedObjects : function() {
            var selectedNodes = this.getSelectedNodes();
            var selectedRelationships = this.getSelectedRelationshipLines();
            selectedRelationships.extend(selectedNodes);
            return selectedRelationships;
        },

        keyEventHandler : function(event) {
            if (this._workspace.isWorkspaceEventsEnabled()) {
                var evt = (event) ? event : window.event;

                if (evt.keyCode == 8) {
                    if ($defined(event)) {
                        if ($defined(event.preventDefault)) {
                            event.preventDefault();
                        } else {
                            event.returnValue = false;
                        }
                        new Event(event).stop();
                    }
                    else
                        evt.returnValue = false;
                }
                else {
                    evt = new Event(event);
                    var key = evt.key;
                    if (!this._editor.isVisible()) {
                        if (((evt.code >= 65 && evt.code <= 90) || (evt.code >= 48 && evt.code <= 57)) && !(evt.control || evt.meta)) {
                            if ($defined(evt.shift)) {
                                key = key.toUpperCase();
                            }
                            this._showEditor(key);
                        }
                        else {
                            switch (key) {
                                case 'delete':
                                    this.deleteCurrentNode();
                                    break;
                                case 'enter':
                                    if (!evt.meta) {
                                        this.createSiblingForSelectedNode();
                                        break;
                                    }
                                case 'insert':
                                    this.createChildForSelectedNode();
                                    break;
                                case 'right':
                                    var nodes = this._getSelectedNodes();
                                    if (nodes.length > 0) {
                                        var node = nodes[0];
                                        if (node.getTopicType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                                            this._goToSideChild(node, 'RIGHT');
                                        }
                                        else {
                                            if (node.getPosition().x < 0) {
                                                this._goToParent(node);
                                            }
                                            else if (!node.areChildrenShrinked()) {
                                                this._goToChild(node);
                                            }
                                        }
                                    }
                                    break;
                                case 'left':
                                    var nodes = this._getSelectedNodes();
                                    if (nodes.length > 0) {
                                        var node = nodes[0];
                                        if (node.getTopicType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                                            this._goToSideChild(node, 'LEFT');
                                        }
                                        else {
                                            if (node.getPosition().x > 0) {
                                                this._goToParent(node);
                                            }
                                            else if (!node.areChildrenShrinked()) {
                                                this._goToChild(node);
                                            }
                                        }
                                    }
                                    break;
                                case'up':
                                    var nodes = this._getSelectedNodes();
                                    if (nodes.length > 0) {
                                        var node = nodes[0];
                                        if (node.getTopicType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                                            this._goToBrother(node, 'UP');
                                        }
                                    }
                                    break;
                                case 'down':
                                    var nodes = this._getSelectedNodes();
                                    if (nodes.length > 0) {
                                        var node = nodes[0];
                                        if (node.getTopicType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                                            this._goToBrother(node, 'DOWN');
                                        }
                                    }
                                    break;
                                case 'f2':
                                    this._showEditor();
                                    break;
                                case 'space':

                                    var nodes = this._getSelectedNodes();
                                    if (nodes.length > 0) {
                                        var topic = nodes[0];

                                        var model = topic.getModel();
                                        var isShrink = !model.areChildrenShrinked();
                                        topic.setChildrenShrinked(isShrink);
                                    }
                                    break;
                                case 'backspace':
                                    evt.preventDefault();
                                    break;
                                case 'esc':
                                    var nodes = this._getSelectedNodes();
                                    for (var i = 0; i < nodes.length; i++) {
                                        var node = nodes[i];
                                        node.setOnFocus(false);
                                    }
                                    break;
                                case 'z':
                                    if (evt.control || evt.meta) {
                                        if (evt.shift) {
                                            this.redo();
                                        }
                                        else {
                                            this.undo();
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                        evt.stop();
                    }
                }
            }
        },

        _showEditor : function(key) {
            var nodes = this._getSelectedNodes();
            if (nodes.length == 1) {
                var node = nodes[0];
                if (key && key != "") {
                    this._editor.setInitialText(key);
                }
                this._editor.getFocusEvent.attempt(node, this._editor);
            }
        },

        _goToBrother : function(node, direction) {
            var brothers = node._parent._getChildren();
            var target = node;
            var y = node.getPosition().y;
            var x = node.getPosition().x;
            var dist = null;
            for (var i = 0; i < brothers.length; i++) {
                var sameSide = (x * brothers[i].getPosition().x) >= 0;
                if (brothers[i] != node && sameSide) {
                    var brother = brothers[i];
                    var brotherY = brother.getPosition().y;
                    if (direction == "DOWN" && brotherY > y) {
                        var distancia = y - brotherY;
                        if (distancia < 0) {
                            distancia = distancia * (-1);
                        }
                        if (dist == null || dist > distancia) {
                            dist = distancia;
                            target = brothers[i];
                        }
                    }
                    else if (direction == "UP" && brotherY < y) {
                        var distancia = y - brotherY;
                        if (distancia < 0) {
                            distancia = distancia * (-1);
                        }
                        if (dist == null || dist > distancia) {
                            dist = distancia;
                            target = brothers[i];
                        }
                    }
                }
            }
            this._goToNode(target);
        },

        _goToNode : function(node) {
            node.setOnFocus(true);
            this.onObjectFocusEvent.attempt(node, this);
        },

        _goToSideChild : function(node, side) {
            var children = node._getChildren();
            if (children.length > 0) {
                var target = children[0];
                var top = null;
                for (var i = 0; i < children.length; i++) {
                    var child = children[i];
                    var childY = child.getPosition().y;
                    if (side == 'LEFT' && child.getPosition().x < 0) {
                        if (top == null || childY < top) {
                            target = child;
                            top = childY;
                        }
                    }
                    if (side == 'RIGHT' && child.getPosition().x > 0) {
                        if (top == null || childY < top) {
                            target = child;
                            top = childY;
                        }
                    }
                }

                this._goToNode(target);
            }
        },

        _goToParent : function(node) {
            var parent = node._parent;
            this._goToNode(parent);
        },

        _goToChild : function(node) {
            var children = node._getChildren();
            if (children.length > 0) {
                var target = children[0];
                var top = target.getPosition().y;
                for (var i = 0; i < children.length; i++) {
                    var child = children[i];
                    if (child.getPosition().y < top) {
                        top = child.getPosition().y;
                        target = child;
                    }
                }
                this._goToNode(target);
            }
        },

        getWorkSpace : function() {
            return this._workspace;
        },

        findRelationShipsByTopicId : function(topicId) {
            var result = [];
            for (var relationshipId in this._relationships) {
                var relationship = this._relationships[relationshipId];
                if (relationship.getModel().getFromNode() == topicId || relationship.getModel().getToNode() == topicId) {
                    result.push(relationship);
                }
            }
            return result;
        }
    }
);
/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.ScreenManager = new Class({
    initialize:function(width, height, divElement) {
        $assert(divElement, "can not be null");
        this._divContainer = divElement;
        this._offset = {x:0,y:0};
    },

    setScale : function(scale) {
        $assert(scale, 'Screen scale can not be null');
        this._workspaceScale = scale;
    },

    addEventListener : function(event, listener) {
        $(this._divContainer).addEvent(event, listener);
    },

    removeEventListener : function(event, listener) {
        $(this._divContainer).removeEvent(event, listener);
    },

    getWorkspaceElementPosition : function(e) {
        // Retrive current element position.
        var elementPosition = e.getPosition();
        var x = elementPosition.x;
        var y = elementPosition.y;

        // Add workspace offset.
        x = x - this._offset.x;
        y = y - this._offset.y;

        // Scale coordinate in order to be relative to the workspace. That's coordSize/size;
        x = x / this._workspaceScale;
        y = y / this._workspaceScale;

        // Subtract div position.
        /*    var containerElem = this.getContainer();
         var containerPosition = core.Utils.workOutDivElementPosition(containerElem);
         x = x + containerPosition.x;
         y = y + containerPosition.y;*/

        // Remove decimal part..
        return {x:x,y:y};
    },

    getWorkspaceIconPosition : function(e) {
        // Retrieve current icon position.
        var image = e.getImage();
        var elementPosition = image.getPosition();
        var imageSize = e.getSize();

        //Add group offset
        var iconGroup = e.getGroup();
        var group = iconGroup.getNativeElement();
        var coordOrigin = group.getCoordOrigin();
        var groupSize = group.getSize();
        var coordSize = group.getCoordSize();

        var scale = {x:coordSize.width / parseInt(groupSize.width), y:coordSize.height / parseInt(groupSize.height)};

        var x = (elementPosition.x - coordOrigin.x - (parseInt(imageSize.width) / 2)) / scale.x;
        var y = (elementPosition.y - coordOrigin.y - (parseInt(imageSize.height) / 2)) / scale.y;

        //Retrieve iconGroup Position
        var groupPosition = iconGroup.getPosition();
        x = x + groupPosition.x;
        y = y + groupPosition.y;

        //Retrieve topic Position
        var topic = iconGroup.getTopic();
        var topicPosition = this.getWorkspaceElementPosition(topic);
        topicPosition.x = topicPosition.x - (parseInt(topic.getSize().width) / 2);


        // Remove decimal part..
        return {x:x + topicPosition.x,y:y + topicPosition.y};
    },

    getWorkspaceMousePosition : function(e) {
        // Retrive current mouse position.
        var mousePosition = this._getMousePosition(e);
        var x = mousePosition.x;
        var y = mousePosition.y;

        // Subtract div position.
        var containerElem = this.getContainer();
        var containerPosition = core.Utils.workOutDivElementPosition(containerElem);
        x = x - containerPosition.x;
        y = y - containerPosition.y;

        // Scale coordinate in order to be relative to the workspace. That's coordSize/size;
        x = x * this._workspaceScale;
        y = y * this._workspaceScale;

        // Add workspace offset.
        x = x + this._offset.x;
        y = y + this._offset.y;

        // Remove decimal part..
        return new core.Point(x, y);
    },

    /**
     * http://www.howtocreate.co.uk/tutorials/javascript/eventinfo
     */
    _getMousePosition : function(event) {
        return core.Utils.getMousePosition(event);
    },

    getContainer : function() {
        return this._divContainer;
    },

    setOffset : function(x, y) {
        this._offset.x = x;
        this._offset.y = y;
    }});
/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.Workspace = new Class({
    initialize: function(profile, screenManager, zoom) {
        // Create a suitable container ...
        $assert(screenManager, 'Div container can not be null');
        this._zoom = zoom;
        this._screenManager = screenManager;
        this._screenWidth = profile.width;
        this._screenHeight = profile.height;

        // Initalize web2d workspace.
        var workspace = this._createWorkspace(profile);
        this._workspace = workspace;

        var screenContainer = screenManager.getContainer();
        // Fix the height of the container ....
        screenContainer.style.height = this._screenHeight + "px";

        // Append to the workspace...
        workspace.addItAsChildTo(screenContainer);
        this.setZoom(zoom, true);

        // Register drag events ...
        this._registerDragEvents();

        this._eventsEnabled = true;

    },

    _updateScreenManager: function() {
        var zoom = this._zoom;
        this._screenManager.setScale(zoom);

        var coordOriginX = -((this._screenWidth * this._zoom) / 2);
        var coordOriginY = -((this._screenHeight * this._zoom) / 2);
        this._screenManager.setOffset(coordOriginX, coordOriginY);
    },

    _createWorkspace: function(profile) {
        // Initialize workspace ...
        var coordOriginX = -(this._screenWidth / 2);
        var coordOriginY = -(this._screenHeight / 2);

        var workspaceProfile = {
            width: this._screenWidth + "px",
            height: this._screenHeight + "px",
            coordSizeWidth:this._screenWidth,
            coordSizeHeight:this._screenHeight,
            coordOriginX:coordOriginX,
            coordOriginY:coordOriginY,
            fillColor:'transparent',
            strokeWidth:0
        };
        web2d.peer.Toolkit.init();
        return  new web2d.Workspace(workspaceProfile);
    },

    appendChild: function(shape) {
        if ($defined(shape.addToWorkspace)) {
            shape.addToWorkspace(this);
        } else {
            this._workspace.appendChild(shape);
        }
    },

    removeChild: function(shape) {
        // Element is a node, not a web2d element?
        if ($defined(shape.removeFromWorkspace)) {
            shape.removeFromWorkspace(this);
        } else {
            this._workspace.removeChild(shape);
        }
    },

    addEventListener: function(type, listener) {
        this._workspace.addEventListener(type, listener);
    },

    removeEventListener: function(type, listener) {
        this._workspace.removeEventListener(type, listener);
    },

    getSize: function() {
        return this._workspace.getCoordSize();
    },

    setZoom: function(zoom, center) {
        this._zoom = zoom;
        var workspace = this._workspace;

        // Update coord scale...
        var coordWidth = zoom * this._screenWidth;
        var coordHeight = zoom * this._screenHeight;
        workspace.setCoordSize(coordWidth, coordHeight);

        // Center topic....
        var coordOriginX;
        var coordOriginY;
        if (center) {
            coordOriginX = -(coordWidth / 2);
            coordOriginY = -(coordHeight / 2);
        } else {
            var coordOrigin = workspace.getCoordOrigin();
            coordOriginX = coordOrigin.x;
            coordOriginY = coordOrigin.y;
        }

        workspace.setCoordOrigin(coordOriginX, coordOriginY);

        // Update screen.
        this._screenManager.setOffset(coordOriginX, coordOriginY);
        this._screenManager.setScale(zoom);
    },

    getScreenManager: function() {
        return this._screenManager;
    },

    enableWorkspaceEvents: function(value) {
        this._eventsEnabled = value;
    },

    isWorkspaceEventsEnabled: function() {
        return this._eventsEnabled;
    },

    dumpNativeChart: function() {
        var workspace = this._workspace;
        return workspace.dumpNativeChart();
    },
    _registerDragEvents: function() {
        var workspace = this._workspace;
        var screenManager = this._screenManager;
        this._dragging = true;
        var mWorkspace = this;
        var mouseDownListener = function(event) {
            if (!$defined(workspace.mouseMoveListener)) {
                if (mWorkspace.isWorkspaceEventsEnabled()) {
                    mWorkspace.enableWorkspaceEvents(false);

                    var mouseDownPosition = screenManager.getWorkspaceMousePosition(event);
                    var originalCoordOrigin = workspace.getCoordOrigin();

                    workspace.mouseMoveListener = function(event) {

                        var currentMousePosition = screenManager.getWorkspaceMousePosition(event);

                        var offsetX = currentMousePosition.x - mouseDownPosition.x;
                        var coordOriginX = -offsetX + originalCoordOrigin.x;

                        var offsetY = currentMousePosition.y - mouseDownPosition.y;
                        var coordOriginY = -offsetY + originalCoordOrigin.y;

                        workspace.setCoordOrigin(coordOriginX, coordOriginY);

                        // Change cursor.
                        if (core.UserAgent.isMozillaFamily()) {
                            window.document.body.style.cursor = "-moz-grabbing";
                        } else {
                            window.document.body.style.cursor = "move";
                        }
                        event.preventDefault();
                    }.bindWithEvent(this);
                    screenManager.addEventListener('mousemove', workspace.mouseMoveListener);

                    // Register mouse up listeners ...
                    workspace.mouseUpListener = function(event) {

                        screenManager.removeEventListener('mousemove', workspace.mouseMoveListener);
                        screenManager.removeEventListener('mouseup', workspace.mouseUpListener);
                        workspace.mouseUpListener = null;
                        workspace.mouseMoveListener = null;
                        window.document.body.style.cursor = 'default';

                        // Update screen manager offset.
                        var coordOrigin = workspace.getCoordOrigin();
                        screenManager.setOffset(coordOrigin.x, coordOrigin.y);
                        mWorkspace.enableWorkspaceEvents(true);
                    },
                        screenManager.addEventListener('mouseup', workspace.mouseUpListener);
                }
            } else {
                workspace.mouseUpListener();
            }
        };
        screenManager.addEventListener('mousedown', mouseDownListener);
    }
});


/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.ShirinkConnector = new Class({
    initialize: function(topic) {

        var elipse = new web2d.Elipse(mindplot.Topic.prototype.INNER_RECT_ATTRIBUTES);
        this._elipse = elipse;
        elipse.setFill('#f7f7f7');

        elipse.setSize(mindplot.Topic.CONNECTOR_WIDTH, mindplot.Topic.CONNECTOR_WIDTH);
        elipse.addEventListener('click', function(event) {
            var model = topic.getModel();
            var isShrink = !model.areChildrenShrinked();

            var actionRunner = mindplot.DesignerActionRunner.getInstance();
            var topicId = topic.getId();

            var commandFunc = function(topic, isShrink) {
                topic.setChildrenShrinked(isShrink);
                return !isShrink;
            };

            var command = new mindplot.commands.GenericFunctionCommand(commandFunc, isShrink, [topicId]);
            actionRunner.execute(command);

            var e = new Event(event).stop();
            e.preventDefault();

        });

        elipse.addEventListener('mousedown', function(event) {
            // Avoid node creation ...
            var e = new Event(event).stop();
            e.preventDefault();
        });

        elipse.addEventListener('dblclick', function(event) {
            // Avoid node creation ...
            event = new Event(event).stop();
            event.preventDefault();

        });

        elipse.addEventListener('mouseover', function(event) {
            this.setFill('#009900');
        });

        elipse.addEventListener('mouseout', function(event) {
            var color = topic.getBackgroundColor();
            this.setFill(color);
        });

        elipse.setCursor('default');
        this._fillColor = '#f7f7f7';
        var model = topic.getModel();
        this.changeRender(model.areChildrenShrinked());

    },
    changeRender: function(isShrink) {
        var elipse = this._elipse;
        if (isShrink) {
            elipse.setStroke('2', 'solid');
        } else {
            elipse.setStroke('1', 'solid');
        }
    },

    setVisibility: function(value) {
        this._elipse.setVisibility(value);
    },

    setOpacity: function(opacity) {
        this._elipse.setOpacity(opacity);
    },

    setFill: function(color) {
        this._fillColor = color;
        this._elipse.setFill(color);
    },

    setAttribute: function(name, value) {
        this._elipse.setAttribute(name, value);
    },

    addToWorkspace: function(group) {
        group.appendChild(this._elipse);
    },


    setPosition: function(x, y) {
        this._elipse.setPosition(x, y);
    },

    moveToBack: function() {
        this._elipse.moveToBack();
    },

    moveToFront: function() {
        this._elipse.moveToFront();
    }
});/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.NodeGraph = new Class({
    initialize:function(nodeModel) {
        $assert(nodeModel,"model can not be null");
        this._mouseEvents = true;
        this.setModel(nodeModel);
        this._onFocus = false;
    },

    getType : function() {
        var model = this.getModel();
        return model.getType();
    },

    setId : function(id) {
        this.getModel().setId(id);
    },

    _set2DElement : function(elem2d) {
        this._elem2d = elem2d;
    },

    get2DElement : function() {
        $assert(this._elem2d, 'NodeGraph has not been initialized propertly');
        return this._elem2d;
    },

    setPosition : function(point) {
        // Elements are positioned in the center.
        var size = this._model.getSize();
        this._elem2d.setPosition(point.x - (size.width / 2), point.y - (size.height / 2));
        this._model.setPosition(point.x, point.y);
    },

    addEventListener : function(type, listener) {
        var elem = this.get2DElement();
        elem.addEventListener(type, listener);
    },

    isNodeGraph : function() {
        return true;
    },

    setMouseEventsEnabled : function(isEnabled) {
        this._mouseEvents = isEnabled;
    },

    isMouseEventsEnabled : function() {
        return this._mouseEvents;
    },

    getSize : function() {
        return this._model.getSize();
    },

    setSize : function(size) {
        this._model.setSize(size.width, size.height);
    },

    getModel
        :
        function() {
            $assert(this._model, 'Model has not been initialized yet');
            return  this._model;
        }
    ,

    setModel : function(model) {
        $assert(model, 'Model can not be null');
        this._model = model;
    },

    getId : function() {
        return this._model.getId();
    },

    setOnFocus : function(focus) {
        this._onFocus = focus;
        var outerShape = this.getOuterShape();
        if (focus) {
            outerShape.setFill('#c7d8ff');
            outerShape.setOpacity(1);

        } else {
            // @todo: node must not know about the topic.

            outerShape.setFill(mindplot.Topic.OUTER_SHAPE_ATTRIBUTES.fillColor);
            outerShape.setOpacity(0);
        }
        this.setCursor('move');
    },

    isOnFocus : function() {
        return this._onFocus;
    },

    dispose : function(workspace) {
        workspace.removeChild(this);
    },

    createDragNode : function() {
        var dragShape = this._buildDragShape();
        return  new mindplot.DragTopic(dragShape, this);
    },

    _buildDragShape : function() {
        $assert(false, '_buildDragShape must be implemented by all nodes.');
    },

    getPosition : function() {
        var model = this.getModel();
        return model.getPosition();
    }
});

mindplot.NodeGraph.create = function(nodeModel) {
    $assert(nodeModel, 'Model can not be null');

    var type = nodeModel.getType();
    $assert(type, 'Node model type can not be null');

    var result;
    if (type == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
        result = new mindplot.CentralTopic(nodeModel);
    } else
    if (type == mindplot.model.NodeModel.MAIN_TOPIC_TYPE) {
        result = new mindplot.MainTopic(nodeModel);
    } else {
        assert(false, "unsupported node type:" + type);
    }

    return result;
}/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */


mindplot.Topic = new Class({
    Extends:mindplot.NodeGraph,
    initialize : function(model) {
        this.parent(model);
        this._children = [];
        this._parent = null;
        this._relationships = [];
        this._isInWorkspace = false;
        this._helpers = [];

        this._buildShape();
        this.setMouseEventsEnabled(true);

        // Positionate topic ....
        var model = this.getModel();
        var pos = model.getPosition();
        if (pos != null && model.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
            this.setPosition(pos);
        }
    },

    setShapeType : function(type) {
        this._setShapeType(type, true);

    },

    getParent : function() {
        return this._parent;
    },

    _setShapeType : function(type, updateModel) {
        // Remove inner shape figure ...
        var model = this.getModel();
        if ($defined(updateModel) && updateModel) {
            model.setShapeType(type);
        }

        var innerShape = this.getInnerShape();
        if (innerShape != null) {
            var dispatcherByEventType = innerShape._dispatcherByEventType;
            // Remove old shape ...
            this._removeInnerShape();

            // Create a new one ...
            innerShape = this.getInnerShape();

            //Let's register all the events. The first one is the default one. The others will be copied.
            //this._registerDefaultListenersToElement(innerShape, this);

            var dispatcher = dispatcherByEventType['mousedown'];
            if ($defined(dispatcher)) {
                for (var i = 1; i < dispatcher._listeners.length; i++) {
                    innerShape.addEventListener('mousedown', dispatcher._listeners[i]);
                }
            }

            // Update figure size ...
            var size = model.getSize();
            this.setSize(size, true);

            var group = this.get2DElement();
            group.appendChild(innerShape);

            // Move text to the front ...
            var text = this.getTextShape();
            text.moveToFront();

            //Move iconGroup to front ...
            var iconGroup = this.getIconGroup();
            if ($defined(iconGroup)) {
                iconGroup.moveToFront();
            }
            //Move connector to front
            var connector = this.getShrinkConnector();
            if ($defined(connector)) {
                connector.moveToFront();
            }

            //Move helpers to front
            this._helpers.forEach(function(helper) {
                helper.moveToFront();
            });

        }

    },

    getShapeType : function() {
        var model = this.getModel();
        var result = model.getShapeType();
        if (!$defined(result)) {
            result = this._defaultShapeType();
        }
        return result;
    },

    _removeInnerShape : function() {
        var group = this.get2DElement();
        var innerShape = this.getInnerShape();
        group.removeChild(innerShape);
        this._innerShape = null;
    },

    getInnerShape : function() {
        if (!$defined(this._innerShape)) {
            // Create inner box.
            this._innerShape = this.buildShape(this.INNER_RECT_ATTRIBUTES);

            // Update bgcolor ...
            var bgColor = this.getBackgroundColor();
            this._setBackgroundColor(bgColor, false);

            // Update border color ...
            var brColor = this.getBorderColor();
            this._setBorderColor(brColor, false);

            // Define the pointer ...
            if (this.getType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                this._innerShape.setCursor('move');
            } else {
                this._innerShape.setCursor('default');
            }

        }
        return this._innerShape;
    },


    buildShape : function(attributes, type) {
        var result;
        if (!$defined(type)) {
            type = this.getShapeType();
        }

        if (type == mindplot.model.NodeModel.SHAPE_TYPE_RECT) {
            result = new web2d.Rect(0, attributes);
        }
        else if (type == mindplot.model.NodeModel.SHAPE_TYPE_ELIPSE) {
            result = new web2d.Elipse(attributes);
        }
        else if (type == mindplot.model.NodeModel.SHAPE_TYPE_ROUNDED_RECT) {
            result = new web2d.Rect(0.3, attributes);
        }
        else if (type == mindplot.model.NodeModel.SHAPE_TYPE_LINE) {
            result = new web2d.Line({strokeColor:"#495879",strokeWidth:1, strokeOpacity:1});
            result.setSize = function(width, height) {
                this.size = {width:width, height:height};
                result.setFrom(-1, height);
                result.setTo(width + 1, height);

                // Lines will have the same color of the default connection lines...
                var stokeColor = mindplot.ConnectionLine.getStrokeColor();
                result.setStroke(1, 'solid', stokeColor);
            };

            result.getSize = function() {
                return this.size;
            };

            result.setPosition = function() {
            };

            var setStrokeFunction = result.setStroke;
            result.setFill = function(color) {

            };

            result.setStroke = function(color) {

            };
        }
        else {
            $assert(false, "Unsupported figure type:" + type);
        }

        result.setPosition(0, 0);
        return result;
    },


    setCursor : function(type) {
        var innerShape = this.getInnerShape();
        innerShape.setCursor(type);

        var outerShape = this.getOuterShape();
        outerShape.setCursor(type);

        var textShape = this.getTextShape();
        textShape.setCursor(type);
    },

    getOuterShape : function() {
        if (!$defined(this._outerShape)) {
            var rect = this.buildShape(mindplot.Topic.OUTER_SHAPE_ATTRIBUTES, mindplot.model.NodeModel.SHAPE_TYPE_ROUNDED_RECT);
            rect.setPosition(-2, -3);
            rect.setOpacity(0);
            this._outerShape = rect;
        }

        return this._outerShape;
    },

    getTextShape : function() {
        if (!$defined(this._text)) {
            var model = this.getModel();
            this._text = this._buildTextShape();

            // Set Text ...
            var text = this.getText();
            this._setText(text, false);
        }
        return this._text;
    },

    getOrBuildIconGroup : function() {
        if (!$defined(this._icon)) {
            this._icon = this._buildIconGroup();
            var group = this.get2DElement();
            group.appendChild(this._icon.getNativeElement());
            this._icon.moveToFront();
        }
        return this._icon;
    },

    getIconGroup : function() {
        return this._icon;
    },

    _buildIconGroup : function(disableEventsListeners) {
        var result = new mindplot.IconGroup(this);
        var model = this.getModel();

        //Icons
        var icons = model.getIcons();
        for (var i = 0; i < icons.length; i++) {
            // Update model identifier ...
            var iconModel = icons[i];
            var icon = new mindplot.ImageIcon(iconModel, this, designer);
            result.addIcon(icon);
        }

        //Links
        var links = model.getLinks();
        for (var i = 0; i < links.length; i++) {
            this._hasLink = true;
            this._link = new mindplot.LinkIcon(links[i], this, designer);
            result.addIcon(this._link);
        }

        //Notes
        var notes = model.getNotes();
        for (var i = 0; i < notes.length; i++) {
            this._hasNote = true;
            this._note = new mindplot.Note(notes[i], this, designer);
            result.addIcon(this._note);
        }

        return result;
    },

    addLink : function(url, designer) {
        var iconGroup = this.getOrBuildIconGroup();
        var model = this.getModel();
        var linkModel = model.createLink(url);
        model.addLink(linkModel);
        this._link = new mindplot.LinkIcon(linkModel, this, designer);
        iconGroup.addIcon(this._link);
        this._hasLink = true;
    },

    addNote : function(text, designer) {
        var iconGroup = this.getOrBuildIconGroup();
        var model = this.getModel();
        text = escape(text);
        var noteModel = model.createNote(text)
        model.addNote(noteModel);
        this._note = new mindplot.Note(noteModel, this, designer);
        iconGroup.addIcon(this._note);
        this._hasNote = true;
    },

    addIcon : function(iconType, designer) {
        var iconGroup = this.getOrBuildIconGroup();
        var model = this.getModel();

        // Update model ...
        var iconModel = model.createIcon(iconType);
        model.addIcon(iconModel);

        var imageIcon = new mindplot.ImageIcon(iconModel, this, designer);
        iconGroup.addIcon(imageIcon);

        return imageIcon;
    },

    removeIcon : function(iconModel) {

        //Removing the icon from MODEL
        var model = this.getModel();
        model._removeIcon(iconModel);

        //Removing the icon from UI
        var iconGroup = this.getIconGroup();
        if ($defined(iconGroup)) {
            var imgIcon = iconGroup.findIconFromModel(iconModel);
            iconGroup.removeImageIcon(imgIcon);
            if (iconGroup.getIcons().length == 0) {
                this.get2DElement().removeChild(iconGroup.getNativeElement());
                this._icon = null;
            }
            this.updateNode();
        }
    },

    removeLink : function() {
        var model = this.getModel();
        var links = model.getLinks();
        model._removeLink(links[0]);
        var iconGroup = this.getIconGroup();
        if ($defined(iconGroup)) {
            iconGroup.removeIcon(mindplot.LinkIcon.IMAGE_URL);
            if (iconGroup.getIcons().length == 0) {
                this.get2DElement().removeChild(iconGroup.getNativeElement());
                this._icon = null;
            }
            this.updateNode.delay(0, this);
        }
        this._link = null;
        this._hasLink = false;
    },

    removeNote : function() {
        var model = this.getModel();
        var notes = model.getNotes();
        model._removeNote(notes[0]);
        var iconGroup = this.getIconGroup();
        if ($defined(iconGroup)) {
            iconGroup.removeIcon(mindplot.Note.IMAGE_URL);
            if (iconGroup.getIcons().length == 0) {
                this.get2DElement().removeChild(iconGroup.getNativeElement());
                this._icon = null;
            }
        }
        /*var elem = this;
         var executor = function(editor)
         {
         return function()
         {
         elem.updateNode();
         };
         };

         setTimeout(executor(this), 0);*/
        core.Executor.instance.delay(this.updateNode, 0, this);
        this._note = null;
        this._hasNote = false;
    },

    addRelationship : function(relationship) {
        this._relationships.push(relationship);
    },

    removeRelationship : function(relationship) {
        this._relationships.erase(relationship);
    },

    getRelationships : function() {
        return this._relationships;
    },

    _buildTextShape : function(disableEventsListeners) {
        var result = new web2d.Text();
        var font = {};

        var family = this.getFontFamily();
        var size = this.getFontSize();
        var weight = this.getFontWeight();
        var style = this.getFontStyle();
        result.setFont(family, size, style, weight);

        var color = this.getFontColor();
        result.setColor(color);

        if (!disableEventsListeners) {
            // Propagate mouse events ...
            var topic = this;
            result.addEventListener('mousedown', function(event) {
                var eventDispatcher = topic.getInnerShape()._dispatcherByEventType['mousedown'];
                if ($defined(eventDispatcher)) {
                    eventDispatcher.eventListener(event);
                }
            });

            if (this.getType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                result.setCursor('move');
            } else {
                result.setCursor('default');
            }
        }

        // Positionate node ...
        this._offset = this.getOffset();
        var iconOffset = this.getIconOffset();
        result.setPosition(iconOffset + this._offset, this._offset / 2);
        return result;
    },

    getIconOffset : function() {
        var iconGroup = this.getIconGroup();
        var size = 0;
        if ($defined(iconGroup)) {
            size = iconGroup.getSize().width;
        }
        return size;
    },

    getOffset : function(value, updateModel) {
        var offset = 18;

        if (mindplot.model.NodeModel.MAIN_TOPIC_TYPE == this.getType()) {
            var parent = this.getModel().getParent();
            if (parent && mindplot.model.NodeModel.MAIN_TOPIC_TYPE == parent.getType()) {
                offset = 6;
            }
            else {
                offset = 8;
            }
        }
        return offset;
    },

    setFontFamily : function(value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setFontFamily(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontFamily(value);
        }
        /*var elem = this;
         var executor = function(editor)
         {
         return function()
         {
         elem.updateNode(updateModel);
         };
         };

         setTimeout(executor(this), 0);*/
        core.Executor.instance.delay(this.updateNode, 0, this, [updateModel]);
    },

    setFontSize : function(value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setSize(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontSize(value);
        }
        /*var elem = this;
         var executor = function(editor)
         {
         return function()
         {
         elem.updateNode(updateModel);
         };
         };

         setTimeout(executor(this), 0);*/
        core.Executor.instance.delay(this.updateNode, 0, this, [updateModel]);

    },

    setFontStyle : function(value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setStyle(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontStyle(value);
        }
        /*var elem = this;
         var executor = function(editor)
         {
         return function()
         {
         elem.updateNode(updateModel);
         };
         };

         setTimeout(executor(this), 0);*/
        core.Executor.instance.delay(this.updateNode, 0, this, [updateModel]);
    },

    setFontWeight : function(value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setWeight(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontWeight(value);
        }
    },

    getFontWeight : function() {
        var model = this.getModel();
        var result = model.getFontWeight();
        if (!$defined(result)) {
            var font = this._defaultFontStyle();
            result = font.weight;
        }
        return result;
    },

    getFontFamily : function() {
        var model = this.getModel();
        var result = model.getFontFamily();
        if (!$defined(result)) {
            var font = this._defaultFontStyle();
            result = font.font;
        }
        return result;
    },

    getFontColor : function() {
        var model = this.getModel();
        var result = model.getFontColor();
        if (!$defined(result)) {
            var font = this._defaultFontStyle();
            result = font.color;
        }
        return result;
    },

    getFontStyle : function() {
        var model = this.getModel();
        var result = model.getFontStyle();
        if (!$defined(result)) {
            var font = this._defaultFontStyle();
            result = font.style;
        }
        return result;
    },

    getFontSize : function() {
        var model = this.getModel();
        var result = model.getFontSize();
        if (!$defined(result)) {
            var font = this._defaultFontStyle();
            result = font.size;
        }
        return result;
    },

    setFontColor : function(value, updateModel) {
        var textShape = this.getTextShape();
        textShape.setColor(value);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setFontColor(value);
        }
    },

    _setText : function(text, updateModel) {
        var textShape = this.getTextShape();
        textShape.setText(text);
        /*var elem = this;
         var executor = function(editor)
         {
         return function()
         {
         elem.updateNode(updateModel);
         };
         };

         setTimeout(executor(this), 0);*/
        core.Executor.instance.delay(this.updateNode, 0, this, [updateModel]);

        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setText(text);
        }
    },

    setText : function(text) {
        this._setText(text, true);
    },

    getText : function() {
        var model = this.getModel();
        var result = model.getText();
        if (!$defined(result)) {
            result = this._defaultText();
        }
        return result;
    },

    setBackgroundColor : function(color) {
        this._setBackgroundColor(color, true);
    },

    _setBackgroundColor : function(color, updateModel) {
        var innerShape = this.getInnerShape();
        innerShape.setFill(color);

        var connector = this.getShrinkConnector();
        connector.setFill(color);
        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setBackgroundColor(color);
        }
    },

    getBackgroundColor : function() {
        var model = this.getModel();
        var result = model.getBackgroundColor();
        if (!$defined(result)) {
            result = this._defaultBackgroundColor();
        }
        return result;
    },

    setBorderColor : function(color) {
        this._setBorderColor(color, true);
    },

    _setBorderColor : function(color, updateModel) {
        var innerShape = this.getInnerShape();
        innerShape.setAttribute('strokeColor', color);

        var connector = this.getShrinkConnector();
        connector.setAttribute('strokeColor', color);


        if ($defined(updateModel) && updateModel) {
            var model = this.getModel();
            model.setBorderColor(color);
        }
    },

    getBorderColor : function() {
        var model = this.getModel();
        var result = model.getBorderColor();
        if (!$defined(result)) {
            result = this._defaultBorderColor();
        }
        return result;
    },

    _buildShape : function() {
        var groupAttributes = {width: 100, height:100,coordSizeWidth:100,coordSizeHeight:100};
        var group = new web2d.Group(groupAttributes);
        group._peer._native.virtualRef = this;
        this._set2DElement(group);

        // Shape must be build based on the model width ...
        var outerShape = this.getOuterShape();
        var innerShape = this.getInnerShape();
        var textShape = this.getTextShape();
        var shrinkConnector = this.getShrinkConnector();

        // Update figure size ...
        var model = this.getModel();
        var size = model.getSize();
        this._setSize(size);

        // Add to the group ...
        group.appendChild(outerShape);
        group.appendChild(innerShape);
        group.appendChild(textShape);

        if (model.getLinks().length != 0 || model.getNotes().length != 0 || model.getIcons().length != 0) {
            iconGroup = this.getOrBuildIconGroup();
        }

        if (this.getType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
            shrinkConnector.addToWorkspace(group);
        }

        // Register listeners ...
        this._registerDefaultListenersToElement(group, this);
//    this._registerDefaultListenersToElement(innerShape, this);
//    this._registerDefaultListenersToElement(textShape, this);

    },

    _registerDefaultListenersToElement : function(elem, topic) {
        var mouseOver = function(event) {
            if (topic.isMouseEventsEnabled()) {
                topic.handleMouseOver(event);
            }
        };
        elem.addEventListener('mouseover', mouseOver);

        var outout = function(event) {
            if (topic.isMouseEventsEnabled()) {
                topic.handleMouseOut(event);
            }
        };
        elem.addEventListener('mouseout', outout);

        // Focus events ...
        var mouseDown = function(event) {
            topic.setOnFocus(true);
        };
        elem.addEventListener('mousedown', mouseDown);
    },

    areChildrenShrinked : function() {
        var model = this.getModel();
        return model.areChildrenShrinked();
    },

    isCollapsed : function() {
        var model = this.getModel();
        var result = false;

        var current = this.getParent();
        while (current && !result) {
            result = current.areChildrenShrinked();
            current = current.getParent();
        }
        return result;
    },

    setChildrenShrinked : function(value) {
        // Update Model ...
        var model = this.getModel();
        model.setChildrenShrinked(value);

        // Change render base on the state.
        var shrinkConnector = this.getShrinkConnector();
        shrinkConnector.changeRender(value);

        // Hide children ...
        core.Utils.setChildrenVisibilityAnimated(this, !value);
        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeShrinkEvent, [this]);
    },

    getShrinkConnector : function() {
        var result = this._connector;
        if (this._connector == null) {
            this._connector = new mindplot.ShirinkConnector(this);
            this._connector.setVisibility(false);
            result = this._connector;

        }
        return result;
    },

    handleMouseOver : function(event) {
        var outerShape = this.getOuterShape();
        outerShape.setOpacity(1);
        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeMouseOverEvent, [this]);
    },

    handleMouseOut : function(event) {
        var outerShape = this.getOuterShape();
        if (!this.isOnFocus()) {
            outerShape.setOpacity(0);
        }
        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeMouseOutEvent, [this]);
    },

    /**
     * Point: references the center of the rect shape.!!!
     */
    setPosition : function(point) {
        // Elements are positioned in the center.
        // All topic element must be positioned based on the innerShape.
        var size = this.getSize();

        var cx = Math.round(point.x - (size.width / 2));
        var cy = Math.round(point.y - (size.height / 2));

        // Update visual position.
        this._elem2d.setPosition(cx, cy);

        // Update model's position ...
        var model = this.getModel();
        model.setPosition(point.x, point.y);

        // Update connection lines ...
        this._updateConnectionLines();

        // Check object state.
        this.invariant();
    },

    getOutgoingLine : function() {
        return this._outgoingLine;
    },

    getIncomingLines : function() {
        var result = [];
        var children = this._getChildren();
        for (var i = 0; i < children.length; i++) {
            var node = children[i];
            var line = node.getOutgoingLine();
            if ($defined(line)) {
                result.push(line);
            }
        }
        return result;
    },

    getOutgoingConnectedTopic : function() {
        var result = null;
        var line = this.getOutgoingLine();
        if ($defined(line)) {
            result = line.getTargetTopic();
        }
        return result;
    },


    _updateConnectionLines : function() {
        // Update this to parent line ...
        var outgoingLine = this.getOutgoingLine();
        if ($defined(outgoingLine)) {
            outgoingLine.redraw();
        }

        // Update all the incoming lines ...
        var incomingLines = this.getIncomingLines();
        for (var i = 0; i < incomingLines.length; i++) {
            incomingLines[i].redraw();
        }

        // Update relationship lines
        for (var j = 0; j < this._relationships.length; j++) {
            this._relationships[j].redraw();
        }
    },

    setBranchVisibility : function(value) {
        var current = this;
        var parent = this;
        while (parent != null && parent.getType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
            current = parent;
            parent = current.getParent();
        }
        current.setVisibility(value);
    },


    setVisibility : function(value) {
        this._setTopicVisibility(value);

        // Hide all children...
        this._setChildrenVisibility(value);

        this._setRelationshipLinesVisibility(value);
    },

    moveToBack : function() {
//    this._helpers.forEach(function(helper, index){
//        helper.moveToBack();
//    });
        // Update relationship lines
        for (var j = 0; j < this._relationships.length; j++) {
            this._relationships[j].moveToBack();
        }
        var connector = this.getShrinkConnector();
        if ($defined(connector)) {
            connector.moveToBack();
        }

        this.get2DElement().moveToBack();


    },

    moveToFront : function() {

        this.get2DElement().moveToFront();
        var connector = this.getShrinkConnector();
        if ($defined(connector)) {
            connector.moveToFront();
        }
        // Update relationship lines
        for (var j = 0; j < this._relationships.length; j++) {
            this._relationships[j].moveToFront();
        }
    },

    isVisible : function() {
        var elem = this.get2DElement();
        return elem.isVisible();
    },

    _setRelationshipLinesVisibility : function(value) {
        //var relationships = designer.findRelationShipsByTopicId(this.getId());
        this._relationships.forEach(function(relationship, index) {
            relationship.setVisibility(value);
        });
    },

    _setTopicVisibility : function(value) {
        var elem = this.get2DElement();
        elem.setVisibility(value);

        if (this.getIncomingLines().length > 0) {
            var connector = this.getShrinkConnector();
            connector.setVisibility(value);
        }

        var textShape = this.getTextShape();
        textShape.setVisibility(value);

    },

    setOpacity : function(opacity) {
        var elem = this.get2DElement();
        elem.setOpacity(opacity);

        this.getShrinkConnector().setOpacity(opacity);

        var textShape = this.getTextShape();
        textShape.setOpacity(opacity);
    },

    _setChildrenVisibility : function(isVisible) {

        // Hide all children.
        var children = this._getChildren();
        var model = this.getModel();

        isVisible = isVisible ? !model.areChildrenShrinked() : isVisible;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            child.setVisibility(isVisible);

            var outgoingLine = child.getOutgoingLine();
            outgoingLine.setVisibility(isVisible);
        }

    },

    invariant : function() {
        var line = this._outgoingLine;
        var model = this.getModel();
        var isConnected = model.isConnected();

        // Check consitency...
        if ((isConnected && !line) || (!isConnected && line)) {
            // $assert(false,'Illegal state exception.');
        }
    },

    /**
     * type:
     *    onfocus
     */
    addEventListener : function(type, listener) {
        // Translate to web 2d events ...
        if (type == 'onfocus') {
            type = 'mousedown';
        }

        /* var textShape = this.getTextShape();
         textShape.addEventListener(type, listener);

         var outerShape = this.getOuterShape();
         outerShape.addEventListener(type, listener);

         var innerShape = this.getInnerShape();
         innerShape.addEventListener(type, listener);*/
        var shape = this.get2DElement();
        shape.addEventListener(type, listener);
    },

    removeEventListener : function(type, listener) {
        // Translate to web 2d events ...
        if (type == 'onfocus') {
            type = 'mousedown';
        }
        /*var textShape = this.getTextShape();
         textShape.removeEventListener(type, listener);

         var outerShape = this.getOuterShape();
         outerShape.removeEventListener(type, listener);

         var innerShape = this.getInnerShape();
         innerShape.removeEventListener(type, listener);*/

        var shape = this.get2DElement();
        shape.removeEventListener(type, listener);
    },


    _setSize : function(size) {
        $assert(size, "size can not be null");
        $assert($defined(size.width), "size seem not to be a valid element");

        mindplot.NodeGraph.prototype.setSize.call(this, size);

        var outerShape = this.getOuterShape();
        var innerShape = this.getInnerShape();
        var connector = this.getShrinkConnector();

        outerShape.setSize(size.width + 4, size.height + 6);
        innerShape.setSize(size.width, size.height);
    },

    setSize : function(size, force, updatePosition) {
        var oldSize = this.getSize();
        if (oldSize.width != size.width || oldSize.height != size.height || force) {
            this._setSize(size);

            // Update the figure position(ej: central topic must be centered) and children position.
            this._updatePositionOnChangeSize(oldSize, size, updatePosition);

            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeResizeEvent, [this]);

        }
    },

    _updatePositionOnChangeSize : function(oldSize, newSize, updatePosition) {
        $assert(false, "this method must be overided");
    },

    disconnect : function(workspace) {
        var outgoingLine = this.getOutgoingLine();
        if ($defined(outgoingLine)) {
            $assert(workspace, 'workspace can not be null');

            this._outgoingLine = null;

            // Disconnect nodes ...
            var targetTopic = outgoingLine.getTargetTopic();
            targetTopic._removeChild(this);

            // Update model ...
            var childModel = this.getModel();
            childModel.disconnect();

            this._parent = null;

            // Remove graphical element from the workspace...
            outgoingLine.removeFromWorkspace(workspace);

            // Remove from workspace.
            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeDisconnectEvent, [targetTopic, this]);

            // Change text based on the current connection ...
            var model = this.getModel();
            if (!model.getText()) {
                var text = this.getText();
                this._setText(text, false);
            }
            if (!model.getFontSize()) {
                var size = this.getFontSize();
                this.setFontSize(size, false);
            }

            // Hide connection line?.
            if (targetTopic._getChildren().length == 0) {
                var connector = targetTopic.getShrinkConnector();
                connector.setVisibility(false);
            }

        }
    },

    getOrder : function() {
        var model = this.getModel();
        return model.getOrder();
    },

    setOrder : function(value) {
        var model = this.getModel();
        model.setOrder(value);
    },

    connectTo : function(targetTopic, workspace, isVisible) {
        $assert(!this._outgoingLine, 'Could not connect an already connected node');
        $assert(targetTopic != this, 'Cilcular connection are not allowed');
        $assert(targetTopic, 'Parent Graph can not be null');
        $assert(workspace, 'Workspace can not be null');

        // Connect Graphical Nodes ...
        targetTopic._appendChild(this);
        this._parent = targetTopic;

// Update model ...
        var targetModel = targetTopic.getModel();
        var childModel = this.getModel();
        childModel.connectTo(targetModel);

// Update topic position based on the state ...
        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeConnectEvent, [targetTopic, this]);

        // Create a connection line ...
        var outgoingLine = new mindplot.ConnectionLine(this, targetTopic);
        if ($defined(isVisible))
            outgoingLine.setVisibility(isVisible);
        this._outgoingLine = outgoingLine;
        workspace.appendChild(outgoingLine);

        // Update figure is necessary.
        this.updateTopicShape(targetTopic);

        // Change text based on the current connection ...
        var model = this.getModel();
        if (!model.getText()) {
            var text = this.getText();
            this._setText(text, false);
        }
        if (!model.getFontSize()) {
            var size = this.getFontSize();
            this.setFontSize(size, false);
        }
        var textShape = this.getTextShape();

        // Display connection node...
        var connector = targetTopic.getShrinkConnector();
        connector.setVisibility(true);

        // Redraw line ...
        outgoingLine.redraw();
    },

    _appendChild : function(child) {
        var children = this._getChildren();
        children.push(child);
    },

    _removeChild : function(child) {
        var children = this._getChildren();
        children.erase(child);
    },

    _getChildren : function() {
        var result = this._children;
        if (!$defined(result)) {
            this._children = [];
            result = this._children;
        }
        return result;
    },

    removeFromWorkspace : function(workspace) {
        var elem2d = this.get2DElement();
        workspace.removeChild(elem2d);
        var line = this.getOutgoingLine();
        if ($defined(line)) {
            workspace.removeChild(line);
        }
        this._isInWorkspace = false;
    },

    addToWorkspace : function(workspace) {
        var elem = this.get2DElement();
        workspace.appendChild(elem);
        this._isInWorkspace = true;
    },

    isInWorkspace : function() {
        return this._isInWorkspace;
    },

    createDragNode : function() {
        var dragNode =  mindplot.NodeGraph.prototype.createDragNode.call(this);

        // Is the node already connected ?
        var targetTopic = this.getOutgoingConnectedTopic();
        if ($defined(targetTopic)) {
            dragNode.connectTo(targetTopic);
        }
        return dragNode;
    },

    updateNode : function(updatePosition) {
        if (this.isInWorkspace()) {
            var textShape = this.getTextShape();
            var sizeWidth = textShape.getWidth();
            var sizeHeight = textShape.getHeight();
            var font = textShape.getFont();
            var iconOffset = this.getIconOffset();
            var height = sizeHeight + this._offset;
            var width = sizeWidth + this._offset * 2 + iconOffset + 2;
            var pos = this._offset / 2 - 1;
            if (this.getShapeType() == mindplot.model.NodeModel.SHAPE_TYPE_ELIPSE) {
                var factor = 0.25;
                height = (width * factor < height ? height : width * factor);
                pos = (height - sizeHeight + 3) / 2;
            }

            var newSize = {width:width,height:height};
            this.setSize(newSize, false, updatePosition);

            // Positionate node ...
            textShape.setPosition(iconOffset + this._offset + 2, pos);
            textShape.setTextSize(sizeWidth, sizeHeight);
            var iconGroup = this.getIconGroup();
            if ($defined(iconGroup))
                iconGroup.updateIconGroupPosition();
        }
    },

    INNER_RECT_ATTRIBUTES : {stroke:'0.5 solid'},

    addHelper : function(helper) {
        helper.addToGroup(this.get2DElement());
        this._helpers.push(helper);
    }
});


mindplot.Topic.CONNECTOR_WIDTH = 6;
mindplot.Topic.OUTER_SHAPE_ATTRIBUTES = {fillColor:'#dbe2e6',stroke:'1 solid #77555a',x:0,y:0};

/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.CentralTopic = new Class({

    Extends:mindplot.Topic,
    initialize: function(model) {
        this.parent(model);
    },

    workoutIncomingConnectionPoint : function(sourcePosition) {
        return this.getPosition();
    },

    getTopicType : function() {
        return mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE;
    },

    setCursor : function(type) {
        type = (type == 'move') ? 'default' : type;
        mindplot.Topic.prototype.setCursor.call(this, type);
    },

    isConnectedToCentralTopic : function() {
        return false;
    },

    createChildModel : function(prepositionate) {
        // Create a new node ...
        var model = this.getModel();
        var mindmap = model.getMindmap();
        var childModel = mindmap.createNode(mindplot.model.NodeModel.MAIN_TOPIC_TYPE);

        if (prepositionate) {
            if (!$defined(this.___siblingDirection)) {
                this.___siblingDirection = 1;
            }

            // Position following taking into account this internal flag ...
            if (this.___siblingDirection == 1) {

                childModel.setPosition(150, 0);
            } else {
                childModel.setPosition(-150, 0);
            }
            this.___siblingDirection = -this.___siblingDirection;
        }
        // Create a new node ...
        childModel.setOrder(0);

        return childModel;
    },

    _defaultShapeType : function() {
        return  mindplot.model.NodeModel.SHAPE_TYPE_ROUNDED_RECT;
    },


    updateTopicShape : function() {

    },

    _updatePositionOnChangeSize : function(oldSize, newSize, updatePosition) {

        // Center main topic ...
        var zeroPoint = new core.Point(0, 0);
        this.setPosition(zeroPoint);
    },

    _defaultText : function() {
        return "Central Topic";
    },

    _defaultBackgroundColor : function() {
        return "#f7f7f7";
    },

    _defaultBorderColor : function() {
        return "#023BB9";
    },

    _defaultFontStyle : function() {
        return {
            font:"Verdana",
            size: 10,
            style:"normal",
            weight:"bold",
            color:"#023BB9"
        };
    }
});/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.MainTopic = new Class({
    Extends: mindplot.Topic,
    initialize : function(model) {
        this.parent(model);
    },

    INNER_RECT_ATTRIBUTES : {stroke:'0.5 solid #009900'},

    createSiblingModel : function(positionate) {
        var siblingModel = null;
        var parentTopic = this.getOutgoingConnectedTopic();
        if (parentTopic != null) {
            // Create a new node ...
            var model = this.getModel();
            var mindmap = model.getMindmap();
            siblingModel = mindmap.createNode(mindplot.model.NodeModel.MAIN_TOPIC_TYPE);

            // Positionate following taking into account the sibling positon.
            if (positionate && parentTopic.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                var pos = this.getPosition();
                siblingModel.setPosition(pos.x, pos.y);
            }

            // Create a new node ...
            var order = this.getOrder() + 1;
            siblingModel.setOrder(order);
        }
        return siblingModel;
    },

    createChildModel : function(prepositionate) {
        // Create a new node ...
        var model = this.getModel();
        var mindmap = model.getMindmap();
        var childModel = mindmap.createNode(mindplot.model.NodeModel.MAIN_TOPIC_TYPE);

        // Get the hights model order position ...
        var children = this._getChildren();
        var order = -1;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.getOrder() > order) {
                order = child.getOrder();
            }
        }
        // Create a new node ...
        childModel.setOrder(order + 1);
        return childModel;
    },


    _buildDragShape : function() {
        var innerShape = this.buildShape(this.INNER_RECT_ATTRIBUTES);
        var size = this.getSize();
        innerShape.setSize(size.width, size.height);
        innerShape.setPosition(0, 0);
        innerShape.setOpacity(0.5);
        innerShape.setCursor('default');
        innerShape.setVisibility(true);

        var brColor = this.getBorderColor();
        innerShape.setAttribute("strokeColor", brColor);

        var bgColor = this.getBackgroundColor();
        innerShape.setAttribute("fillColor", bgColor);

        //  Create group ...
        var groupAttributes = {width: 100, height:100,coordSizeWidth:100,coordSizeHeight:100};
        var group = new web2d.Group(groupAttributes);
        group.appendChild(innerShape);

        // Add Text ...
        var textShape = this._buildTextShape(true);
        var text = this.getText();
        textShape.setText(text);
        textShape.setOpacity(0.5);
        group.appendChild(textShape);

        return group;
    },


    _defaultShapeType : function() {
        return mindplot.model.NodeModel.SHAPE_TYPE_LINE;
    },

    updateTopicShape : function(targetTopic, workspace) {
        // Change figure based on the connected topic ...
        var model = this.getModel();
        var shapeType = model.getShapeType();
        if (targetTopic.getType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
            if (!$defined(shapeType)) {
                // Get the real shape type ...
                shapeType = this.getShapeType();
                this._setShapeType(shapeType, false);
            }
        }
        this._helpers.forEach(function(helper) {
            helper.moveToFront();
        });
    },

    disconnect : function(workspace) {
        mindplot.Topic.prototype.disconnect.call(this, workspace);
        var size = this.getSize();

        var model = this.getModel();
        var shapeType = model.getShapeType();
        if (!$defined(shapeType)) {
            // Change figure ...
            shapeType = this.getShapeType();
            this._setShapeType(mindplot.model.NodeModel.SHAPE_TYPE_ROUNDED_RECT, false);
        }
        var innerShape = this.getInnerShape();
        innerShape.setVisibility(true);
    },

    getTopicType : function() {
        return "MainTopic";
    },

    _updatePositionOnChangeSize : function(oldSize, newSize, updatePosition) {

        if (updatePosition == false && this.getModel().getFinalPosition()) {
            this.setPosition(this.getModel().getFinalPosition(), false);
        }
        else {
            var xOffset = Math.round((newSize.width - oldSize.width) / 2);
            var pos = this.getPosition();
            if ($defined(pos)) {
                if (pos.x > 0) {
                    pos.x = pos.x + xOffset;
                } else {
                    pos.x = pos.x - xOffset;
                }
                this.setPosition(pos);
            }
        }
    },

    setPosition : function(point, fireEvent) {
        mindplot.Topic.prototype.setPosition.call(this, point);

        // Update board zero entry position...
        if (fireEvent != false)
            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeMoveEvent, [this]);
    },

    workoutIncomingConnectionPoint : function(sourcePosition) {
        $assert(sourcePosition, 'sourcePoint can not be null');
        var pos = this.getPosition();
        var size = this.getSize();

        var isAtRight = mindplot.util.Shape.isAtRight(sourcePosition, pos);
        var result = mindplot.util.Shape.calculateRectConnectionPoint(pos, size, isAtRight);
        if (this.getShapeType() == mindplot.model.NodeModel.SHAPE_TYPE_LINE) {
            result.y = result.y + (this.getSize().height / 2);
        }

        // Move a little the position...
        var offset = mindplot.Topic.CONNECTOR_WIDTH / 2;
        if (this.getPosition().x > 0) {
            result.x = result.x + offset;
        } else {
            result.x = result.x - offset;
        }

        result.x = Math.ceil(result.x);
        result.y = Math.ceil(result.y);
        return result;

    },

    workoutOutgoingConnectionPoint : function(targetPosition) {
        $assert(targetPosition, 'targetPoint can not be null');
        var pos = this.getPosition();
        var size = this.getSize();

        var isAtRight = mindplot.util.Shape.isAtRight(targetPosition, pos);
        var result;
        if (this.getShapeType() == mindplot.model.NodeModel.SHAPE_TYPE_LINE) {
//        if (!this.isConnectedToCentralTopic())
//        {
            result = new core.Point();
            if (!isAtRight) {
                result.x = pos.x + (size.width / 2);
            } else {
                result.x = pos.x - (size.width / 2);
            }
            result.y = pos.y + (size.height / 2);
            /*} else
             {
             // In this case, connetion line is not used as shape figure.
             result = mindplot.util.Shape.calculateRectConnectionPoint(pos, size, isAtRight, true);
             result.y = pos.y + (size.height / 2);
             */
            /*if(result.y>0){
             result.y+=1;
             }*/
            /*

             // Correction factor ...
             if (!isAtRight)
             {
             result.x = result.x + 2;
             } else
             {
             result.x = result.x - 2;
             }

             }*/
        } else {
            result = mindplot.util.Shape.calculateRectConnectionPoint(pos, size, isAtRight, true);
        }
        result.x = Math.ceil(result.x);
        result.y = Math.ceil(result.y);
        return result;
    },


    isConnectedToCentralTopic : function() {
        var model = this.getModel();
        var parent = model.getParent();

        return parent && parent.getType() === mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE;
    },

    _defaultText : function() {
        var targetTopic = this.getOutgoingConnectedTopic();
        var result = "";
        if ($defined(targetTopic)) {
            if (targetTopic.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                result = "Main Topic";
            } else {
                result = "Sub Topic";
            }
        } else {
            result = "Isolated Topic";
        }
        return result;
    },

    _defaultFontStyle : function() {
        var targetTopic = this.getOutgoingConnectedTopic();
        var result;
        if ($defined(targetTopic)) {
            if (targetTopic.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                result = {
                    font:"Arial",
                    size: 8,
                    style:"normal",
                    weight:"normal",
                    color:"#525c61"
                };
            } else {
                result = {
                    font:"Arial",
                    size: 6,
                    style:"normal",
                    weight:"normal",
                    color:"#525c61"
                };
            }
        } else {
            result = {
                font:"Verdana",
                size: 8,
                style:"normal",
                weight:"normal",
                color:"#525c61"
            };
        }
        return result;
    },

    _defaultBackgroundColor : function() {
        return "#E0E5EF";
    },

    _defaultBorderColor : function() {
        return '#023BB9';
    },
    addSibling : function() {
        var order = this.getOrder();
    }
});
/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.DragTopic = function(dragShape, draggedNode)
{
    $assert(dragShape, 'Rect can not be null.');
    $assert(draggedNode, 'draggedNode can not be null.');

    this._elem2d = dragShape;
    this._order = null;
    this._draggedNode = draggedNode;
    this._position = new core.Point();
};

mindplot.DragTopic.initialize = function(workspace)
{
    var pivot = mindplot.DragTopic.__getDragPivot();
    workspace.appendChild(pivot);
};

mindplot.DragTopic.prototype.setOrder = function(order)
{
    this._order = order;
};

mindplot.DragTopic.prototype.setPosition = function(x, y)
{
    this._position.setValue(x, y);

    // Elements are positioned in the center.
    // All topic element must be positioned based on the innerShape.
    var draggedNode = this._draggedNode;
    var size = draggedNode.getSize();

    var cx = Math.ceil(x - (size.width / 2));
    var cy = Math.ceil(y - (size.height / 2));

    // Update visual position.
    this._elem2d.setPosition(cx, cy);
};

mindplot.DragTopic.prototype.getInnerShape = function()
{
    return this._elem2d;
};

mindplot.DragTopic.prototype.disconnect = function(workspace)
{
    // Clear connection line ...
    var dragPivot = this._getDragPivot();
    dragPivot.disconnect(workspace);
};

mindplot.DragTopic.prototype.canBeConnectedTo = function(targetTopic)
{
    $assert(targetTopic, 'parent can not be null');

    var result = true;
    if (!targetTopic.areChildrenShrinked() && !targetTopic.isCollapsed())
    {
        // Dragged node can not be connected to himself.
        if (targetTopic == this._draggedNode)
        {
            result = false;
        } else
        {
            var draggedNode = this.getDraggedTopic();
            var topicPosition = this.getPosition();

            var targetTopicModel = targetTopic.getModel();
            var childTopicModel = draggedNode.getModel();

           result = targetTopicModel.canBeConnected(childTopicModel, topicPosition, 18);
        }
    } else
    {
        result = false;
    }
    return result;
};

mindplot.DragTopic.prototype.connectTo = function(parent)
{
    $assert(parent, 'Parent connection node can not be null.');

    var dragPivot = this._getDragPivot();
    dragPivot.connectTo(parent);
};

mindplot.DragTopic.prototype.getDraggedTopic = function()
{
    return  this._draggedNode;
};


mindplot.DragTopic.prototype.removeFromWorkspace = function(workspace)
{
    // Remove drag shadow.
    workspace.removeChild(this._elem2d);

    // Remove pivot shape. To improve performace it will not be removed. Only the visilility will be changed.
    var dragPivot = this._getDragPivot();
    dragPivot.setVisibility(false);
};

mindplot.DragTopic.prototype.addToWorkspace = function(workspace)
{
    workspace.appendChild(this._elem2d);
    var dragPivot = this._getDragPivot();

    dragPivot.addToWorkspace(workspace);
    dragPivot.setVisibility(true);
};

mindplot.DragTopic.prototype._getDragPivot = function()
{
    return mindplot.DragTopic.__getDragPivot();
};

mindplot.DragTopic.__getDragPivot = function()
{
    var result = mindplot.DragTopic._dragPivot;
    if (!$defined(result))
    {
        result = new mindplot.DragPivot();
        mindplot.DragTopic._dragPivot = result;
    }
    return result;
};


mindplot.DragTopic.prototype.getPosition = function()
{
    return this._position;
};

mindplot.DragTopic.prototype.isDragTopic = function()
{
    return true;
};

mindplot.DragTopic.prototype.updateDraggedTopic = function(workspace)
{
    $assert(workspace, 'workspace can not be null');

    var dragPivot = this._getDragPivot();
    var draggedTopic = this.getDraggedTopic();

    var isDragConnected = this.isConnected();
    var actionRunner = mindplot.DesignerActionRunner.getInstance();
    var topicId = draggedTopic.getId();
    var command = new mindplot.commands.DragTopicCommand(topicId);

  if (isDragConnected)
    {

        var targetTopic = this.getConnectedToTopic();
        if (targetTopic.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE)
        {
            // Update topic position ...
            var dragPivotPosition = dragPivot.getPosition();

            // Must position the dragged topic taking into account the current node size.
            var pivotSize = dragPivot.getSize();
            var draggedTopicSize = draggedTopic.getSize();
            var xOffset = draggedTopicSize.width - pivotSize.width;
            xOffset = Math.round(xOffset / 2);

            if (dragPivotPosition.x > 0)
            {
                dragPivotPosition.x = parseInt(dragPivotPosition.x) + xOffset;
            }
            else
            {
                dragPivotPosition.x = parseInt(dragPivotPosition.x) - xOffset;
            }
            // Set new position ...
            command.setPosition(dragPivotPosition);

        } else
        {
            // Main topic connections can be positioned only with the order ...
            command.setOrder(this._order);
        }

        // Set new parent topic ..
        command.setParetTopic(targetTopic);
    } else {

        // If the node is not connected, positionate based on the original drag topic position.
        var dragPosition = this.getPosition();
        command = new mindplot.commands.DragTopicCommand(topicId, dragPosition);
        command.setPosition(dragPosition);
    }
    actionRunner.execute(command);
};

mindplot.DragTopic.prototype.setBoardPosition = function(point)
{
    $assert(point, 'point can not be null');
    var dragPivot = this._getDragPivot();
    dragPivot.setPosition(point);
};


mindplot.DragTopic.prototype.getBoardPosition = function(point)
{
    $assert(point, 'point can not be null');
    var dragPivot = this._getDragPivot();
    return dragPivot.getPosition();
};

mindplot.DragTopic.prototype.getConnectedToTopic = function()
{
    var dragPivot = this._getDragPivot();
    return dragPivot.getTargetTopic();
};

mindplot.DragTopic.prototype.isConnected = function()
{
    return this.getConnectedToTopic() != null;
};

mindplot.DragTopic.PIVOT_SIZE = {width:50,height:10};
/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
 */

mindplot.DragManager = function(workspace)
{
    this._workspace = workspace;
    this._listeners = {};
};

mindplot.DragManager.prototype.add = function(node)
{
    // Add behaviour ...
    var workspace = this._workspace;
    var screen = workspace.getScreenManager();
    var dragManager = this;

    var mouseDownListener = function(event)
    {
        if (workspace.isWorkspaceEventsEnabled())
        {
            // Disable double drag... 
            workspace.enableWorkspaceEvents(false);

            // Set initial position.
            var dragNode = node.createDragNode();
            var mousePos = screen.getWorkspaceMousePosition(event);
            dragNode.setPosition(mousePos.x, mousePos.y);

            // Register mouse move listener ...
            var mouseMoveListener = dragManager._buildMouseMoveListener(workspace, dragNode, dragManager);
            screen.addEventListener('mousemove', mouseMoveListener);

            // Register mouse up listeners ...
            var mouseUpListener = dragManager._buildMouseUpListener(workspace, node, dragNode, dragManager);
            screen.addEventListener('mouseup', mouseUpListener);

            // Execute Listeners ..
            var startDragListener = dragManager._listeners['startdragging'];
            startDragListener(event, node);

            // Change cursor.
            window.document.body.style.cursor = 'move';
        }
    };
    dragManager._mouseDownListener = mouseDownListener;

    node.addEventListener('mousedown', mouseDownListener);
};

mindplot.DragManager.prototype.remove = function(node)
{
    var nodes = this._topics;
    var contained = false;
    var index = -1;
    for (var i = 0; i < nodes.length; i++) {
        if (nodes[i] == node) {
            contained = true;
            index = i;
        }
    }
    if (contained)
    {
        elem = new Array();
    }
};

mindplot.DragManager.prototype._buildMouseMoveListener = function(workspace, dragNode, dragManager)
{
    var screen = workspace.getScreenManager();
    var result = function(event) {

        if (!dragNode._isInTheWorkspace)
        {
            // Add shadow node to the workspace.
            workspace.appendChild(dragNode);
            dragNode._isInTheWorkspace = true;
        }

        var pos = screen.getWorkspaceMousePosition(event);
        dragNode.setPosition(pos.x, pos.y);

        // Call mouse move listeners ...
        var dragListener = dragManager._listeners['dragging'];
        if ($defined(dragListener))
        {
            dragListener(event, dragNode);
        }

        event.preventDefault();
    }.bindWithEvent(this);
    dragManager._mouseMoveListener = result;
    return result;
};

mindplot.DragManager.prototype._buildMouseUpListener = function(workspace, node, dragNode, dragManager)
{
    var screen = workspace.getScreenManager();
    var result = function(event) {

        $assert(dragNode.isDragTopic, 'dragNode must be an DragTopic');

        // Remove drag node from the workspace.
        var hasBeenDragged = dragNode._isInTheWorkspace;
        if (dragNode._isInTheWorkspace)
        {
            dragNode.removeFromWorkspace(workspace);
        }

        // Remove all the events.
        screen.removeEventListener('mousemove', dragManager._mouseMoveListener);
        screen.removeEventListener('mouseup', dragManager._mouseUpListener);

        // Help GC
        dragManager._mouseMoveListener = null;
        dragManager._mouseUpListener = null;

        // Execute Listeners only if the node has been moved.
        var endDragListener = dragManager._listeners['enddragging'];
        endDragListener(event, dragNode);

        if (hasBeenDragged)
        {
            dragNode._isInTheWorkspace = false;
        }

        // Change the cursor to the default.
        window.document.body.style.cursor = 'default';

        workspace.enableWorkspaceEvents(true);

    };
    dragManager._mouseUpListener = result;
    return result;
};

/**
 * type:
 *  - startdragging.
 *  - dragging
 *  - enddragging
 */
mindplot.DragManager.prototype. addEventListener = function(type, listener)
{
    this._listeners[type] = listener;
};

mindplot.DragManager.DRAG_PRECISION_IN_SEG = 100;
/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.DragPivot = new Class({
    initialize:function() {
        this._position = new core.Point();
        this._size = mindplot.DragTopic.PIVOT_SIZE;
        this._line = null;

        this._straightLine = this._buildStraightLine();
        this._curvedLine = this._buildCurvedLine();
        this._dragPivot = this._buildRect();
        this._connectRect = this._buildRect();
        this._targetTopic = null;
    },

    getTargetTopic : function() {
        return this._targetTopic;
    },

    _buildStraightLine : function() {
        var line = new web2d.CurvedLine();
        line.setStyle(web2d.CurvedLine.SIMPLE_LINE);
        line.setStroke(1, 'solid', '#CC0033');
        line.setOpacity(0.4);
        line.setVisibility(false);
        return line;
    },

    _buildCurvedLine : function() {
        var line = new web2d.CurvedLine();
        line.setStyle(web2d.CurvedLine.SIMPLE_LINE);
        line.setStroke(1, 'solid', '#CC0033');
        line.setOpacity(0.4);
        line.setVisibility(false);
        return line;
    },

    _redraw : function(pivotPosition) {
        // Update line position.
        $assert(this.getTargetTopic(), 'Illegal invocation. Target node can not be null');

        var pivotRect = this._getPivotRect();
        var currentPivotPosition = pivotRect.getPosition();

        // Pivot position has not changed. In this case, position change is not required.
        var targetTopic = this.getTargetTopic();
        if (currentPivotPosition.x != pivotPosition.x || currentPivotPosition.y != pivotPosition.y) {
            var position = this._position;
            var fromPoint = targetTopic.workoutIncomingConnectionPoint(position);

            // Calculate pivot connection point ...
            var size = this._size;
            var targetPosition = targetTopic.getPosition();
            var line = this._line;

            // Update Line position.
            var isAtRight = mindplot.util.Shape.isAtRight(targetPosition, position);
            var pivotPoint = mindplot.util.Shape.calculateRectConnectionPoint(position, size, isAtRight);
            line.setFrom(pivotPoint.x, pivotPoint.y);

            // Update rect position
            pivotRect.setPosition(pivotPosition.x, pivotPosition.y);

            // Display elements if it's required...
            if (!pivotRect.isVisible()) {
                // Make line visible only when the position has been already changed.
                // This solve several strange effects ;)
                var targetPoint = targetTopic.workoutIncomingConnectionPoint(pivotPoint);
                line.setTo(targetPoint.x, targetPoint.y);

                this.setVisibility(true);
            }
        }
    },

    setPosition : function(point) {
        this._position = point;

        // Update visual position.
        var size = this.getSize();

        var cx = point.x - (parseInt(size.width) / 2);
        var cy = point.y - (parseInt(size.height) / 2);

        // Update line  ...
        if (this.getTargetTopic()) {
            var pivotPosition = {x:cx,y:cy};
            this._redraw(pivotPosition);
        }
    },

    getPosition : function() {
        return this._position;
    },

    _buildRect : function() {
        var size = this._size;
        var rectAttributes = {fillColor:'#CC0033',opacity:0.4,width:size.width,height:size.height,strokeColor:'#FF9933'};
        var rect = new web2d.Rect(0, rectAttributes);
        rect.setVisibility(false);
        return rect;
    },

    _buildConnectRect : function() {
        var size = this._size;
        var rectAttributes = {fillColor:'#CC0033',opacity:0.4,width:size.width,height:size.height,strokeColor:'#FF9933'};
        var result = new web2d.Rect(0, rectAttributes);
        return result;
    },

    _getPivotRect : function() {
        return this._dragPivot;
    },

    getSize : function() {
        var elem2d = this._getPivotRect();
        return elem2d.getSize();
    },

    setVisibility : function(value) {
        var pivotRect = this._getPivotRect();
        pivotRect.setVisibility(value);

        var connectRect = this._connectRect;
        connectRect.setVisibility(value);
        if ($defined(this._line)) {
            this._line.setVisibility(value);
        }
    },

    addToWorkspace : function(workspace) {
        var pivotRect = this._getPivotRect();
        workspace.appendChild(pivotRect);

        var connectToRect = this._connectRect;
        workspace.appendChild(connectToRect);

        // Add a hidden straight line ...
        var straighLine = this._straightLine;
        straighLine.setVisibility(false);
        workspace.appendChild(straighLine);
        straighLine.moveToBack();

        // Add a hidden curved line ...
        var curvedLine = this._curvedLine;
        curvedLine.setVisibility(false);
        workspace.appendChild(curvedLine);
        curvedLine.moveToBack();

        // Add a connect rect ...
        var connectRect = this._connectRect;
        connectRect.setVisibility(false);
        workspace.appendChild(connectRect);
        connectRect.moveToBack();
    },

    removeFromWorkspace : function(workspace) {
        var shape = this._getPivotRect();
        workspace.removeChild(shape);

        var connectToRect = this._connectRect;
        workspace.removeChild(connectToRect);

        if ($defined(this._straightLine)) {
            workspace.removeChild(this._straightLine);
        }

        if ($defined(this._curvedLine)) {
            workspace.removeChild(this._curvedLine);
        }
    },

    connectTo : function(targetTopic) {
        $assert(!this._outgoingLine, 'Could not connect an already connected node');
        $assert(targetTopic != this, 'Cilcular connection are not allowed');
        $assert(targetTopic, 'parent can not be null');

        this._targetTopic = targetTopic;
        if (targetTopic.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
            this._line = this._straightLine;
        } else {
            this._line = this._curvedLine;
        }

        // Connected to Rect ...
        var connectRect = this._connectRect;
        var targetSize = targetTopic.getSize();
        var width = targetSize.width;
        var height = targetSize.height;
        connectRect.setSize(width, height);

        var targetPosition = targetTopic.getPosition();
        var cx = Math.ceil(targetPosition.x - (width / 2));
        var cy = Math.ceil(targetPosition.y - (height / 2));
        connectRect.setPosition(cx, cy);

        // Change elements position ...
        var pivotRect = this._getPivotRect();
        pivotRect.moveToFront();

    },

    disconnect : function(workspace) {
        $assert(workspace, 'workspace can not be null.');
        $assert(this._targetTopic, 'There are not connected topic.');

        this.setVisibility(false);
        this._targetTopic = null;
        this._line = null;
    }
});
/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.Board = new Class({
    initialize : function(defaultHeight, referencePoint) {
        $assert(referencePoint, "referencePoint can not be null");
        this._defaultWidth = defaultHeight;
        this._entries = new mindplot.BidirectionalArray();
        this._referencePoint = referencePoint;
    },

    getReferencePoint : function() {
        return this._referencePoint;
    },

    _removeEntryByOrder : function(order, position) {
        var board = this._getBoard(position);
        var entry = board.lookupEntryByOrder(order);

        $assert(!entry.isAvailable(), 'Entry must not be available in order to be removed.Entry Order:' + order);
        entry.removeTopic();
        board.update(entry);
    },

    removeTopicFromBoard : function(topic) {
        var position = topic.getPosition();
        var order = topic.getOrder();

        this._removeEntryByOrder(order, position);
        topic.setOrder(null);
    },

    positionateDragTopic :function(dragTopic) {
        throw "this method must be overrided";
    },

    getHeight: function() {
        var board = this._getBoard();
        return board.getHeight();
    }
});

/**
 * ---------------------------------------
 */
mindplot.BidirectionalArray = new Class({

    initialize: function() {
        this._leftElem = [];
        this._rightElem = [];
    },

    get :function(index, sign) {
        $assert(index, 'Illegal argument, index must be passed.');
        if ($defined(sign)) {
            $assert(index >= 0, 'Illegal absIndex value');
            index = index * sign;
        }

        var result = null;
        if (index >= 0 && index < this._rightElem.length) {
            result = this._rightElem[index];
        } else if (index < 0 && Math.abs(index) < this._leftElem.length) {
            result = this._leftElem[Math.abs(index)];
        }
        return result;
    },

    set : function(index, elem) {
        $assert(index, 'Illegal index value');

        var array = (index >= 0) ? this._rightElem : this._leftElem;
        array[Math.abs(index)] = elem;
    },

    length : function(index) {
        $assert(index, 'Illegal index value');
        return (index >= 0) ? this._rightElem.length : this._leftElem.length;
    },

    upperLength : function() {
        return this.length(1);
    },

    lowerLength : function() {
        return this.length(-1);
    },

    inspect : function() {
        var result = '{';
        var lenght = this._leftElem.length;
        for (var i = 0; i < lenght; i++) {
            var entry = this._leftElem[lenght - i - 1];
            if (entry != null) {
                if (i != 0) {
                    result += ', ';
                }
                result += entry.inspect();
            }
        }

        lenght = this._rightElem.length;
        for (var i = 0; i < lenght; i++) {
            var entry = this._rightElem[i];
            if (entry != null) {
                if (i != 0) {
                    result += ', ';
                }
                result += entry.inspect();
            }
        }
        result += '}';

        return result;

    }
});/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.CentralTopicBoard = new Class({
    Extends: mindplot.Board,
    initialize:function(centralTopic, layoutManager) {
        var point = new core.Point(0, 0);
        this._layoutManager = layoutManager;
        this._rightBoard = new mindplot.VariableDistanceBoard(50, point);
        this._leftBoard = new mindplot.VariableDistanceBoard(50, point);
        this._centralTopic = centralTopic;
    },

    _getBoard : function(position) {
        return (position.x >= 0) ? this._rightBoard : this._leftBoard;
    },

    positionateDragTopic : function(dragTopic) {
        $assert(dragTopic != null, 'dragTopic can not be null');
        $assert(dragTopic.isDragTopic, 'dragTopic must be DragTopic instance');

        // This node is a main topic node. Position
        var dragPos = dragTopic.getPosition();
        var board = this._getBoard(dragPos);

        // Look for entry  ...
        var entry = board.lookupEntryByPosition(dragPos);

        // Calculate 'y' position base on the entry ...
        var yCoord;
        if (!entry.isAvailable() && entry.getTopic() != dragTopic.getDraggedTopic()) {
            yCoord = entry.getLowerLimit();
        } else {
            yCoord = entry.workoutEntryYCenter();
        }


        // MainTopic can not be positioned over the drag topic ...
        var centralTopic = this._centralTopic;
        var centralTopicSize = centralTopic.getSize();
        var halfWidth = (centralTopicSize.width / 2);
        if (Math.abs(dragPos.x) < halfWidth + 60) {
            var distance = halfWidth + 60;
            dragPos.x = (dragPos.x > 0) ? distance : -distance;
        }

        // Update board position.
        var pivotPos = new core.Point(dragPos.x, yCoord);
        dragTopic.setBoardPosition(pivotPos);
    },


    addBranch : function(topic) {
        // Update topic position ...
        var position = topic.getPosition();

        var order = topic.getOrder();
        var board = this._getBoard(position);
        var entry = null;
        if (order != null) {
            entry = board.lookupEntryByOrder(order);
        } else {
            entry = board.lookupEntryByPosition(position);
        }

        // If the entry is not available, I must swap the the entries...
        if (!entry.isAvailable()) {
            board.freeEntry(entry);
        }

        // Add it to the board ...
        entry.setTopic(topic);
        board.update(entry);
    },

    updateChildrenPosition : function(topic, xOffset, modifiedTopics) {
        var board = this._rightBoard;
        var oldReferencePosition = board.getReferencePoint();
        var newReferencePosition = new core.Point(oldReferencePosition.x + xOffset, oldReferencePosition.y);
        board.updateReferencePoint(newReferencePosition);

        board = this._leftBoard;
        oldReferencePosition = board.getReferencePoint();
        newReferencePosition = new core.Point(oldReferencePosition.x - xOffset, oldReferencePosition.y);
        board.updateReferencePoint(newReferencePosition);
    },

    repositionate : function() {
        //@todo: implement ..
    }
});/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.MainTopicBoard = new Class({
    Extends:mindplot.Board,
    initialize:function(topic, layoutManager) {
        this._layoutManager = layoutManager;
        this._topic = topic;
        this._board = null;
        this._height = 0;
    },


    _getBoard: function() {
        if (!$defined(this._board)) {
            var topic = this._topic;
            this._board = new mindplot.FixedDistanceBoard(mindplot.MainTopicBoard.DEFAULT_MAIN_TOPIC_HEIGHT, topic, this._layoutManager);
        }
        return this._board;
    },

    updateReferencePoint : function(position) {
        this._board.updateReferencePoint(position);
    },

    updateChildrenPosition : function(topic) {
        var board = this._getBoard();
        board.updateReferencePoint();
    },

    positionateDragTopic : function(dragTopic) {
        $assert(dragTopic != null, 'dragTopic can not be null');
        $assert(dragTopic.isDragTopic, 'dragTopic must be DragTopic instance');

        // This node is a main topic node. Position
        var dragPos = dragTopic.getPosition();
        var board = this._getBoard();

        // Look for entry  ...
        var entry = board.lookupEntryByPosition(dragPos);

        // Calculate 'y' position base on the entry ...
        var yCoord;
        if (!entry.isAvailable() && entry.getTopic() != dragTopic.getDraggedTopic()) {
            yCoord = entry.getLowerLimit();
        } else {
            yCoord = entry.workoutEntryYCenter();
        }

        // Update board position.
        var targetTopic = dragTopic.getConnectedToTopic();
        var xCoord = this._workoutXBorderDistance(targetTopic);

        // Add the size of the pivot to the distance ...
        var halfPivotWidth = mindplot.DragTopic.PIVOT_SIZE.width / 2;
        xCoord = xCoord + ((dragPos.x > 0) ? halfPivotWidth : -halfPivotWidth);

        var pivotPos = new core.Point(xCoord, yCoord);
        dragTopic.setBoardPosition(pivotPos);

        var order = entry.getOrder();
        dragTopic.setOrder(order);
    }
    ,

    /**
     * This x distance doesn't take into account the size of the shape.
     */
    _workoutXBorderDistance : function(topic) {
        $assert(topic, 'topic can not be null');
        var board = this._getBoard();
        return board.workoutXBorderDistance(topic);
    },

    addBranch : function(topic) {
        var order = topic.getOrder();
        $assert(order, "Order must be defined");

        // If the entry is not available, I must swap the the entries...
        var board = this._getBoard();
        var entry = board.lookupEntryByOrder(order);
        if (!entry.isAvailable()) {
            board.freeEntry(entry);
        }

        // Add the topic to the board ...
        board.addTopic(order, topic);

        // Repositionate all the parent topics ...
        var currentTopic = this._topic;
        if (currentTopic.getOutgoingConnectedTopic()) {
            var parentTopic = currentTopic.getOutgoingConnectedTopic();
            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeRepositionateEvent, [parentTopic]);
        }
    },

    repositionate : function() {
        var board = this._getBoard();
        board.repositionate();
    },

    removeTopicFromBoard : function(topic) {
        var board = this._getBoard();
        board.removeTopic(topic);

        // Repositionate all the parent topics ...
        var parentTopic = this._topic;
        if (parentTopic.getOutgoingConnectedTopic()) {
            var connectedTopic = parentTopic.getOutgoingConnectedTopic();
            mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeRepositionateEvent, [connectedTopic]);
        }
    }
});

mindplot.MainTopicBoard.DEFAULT_MAIN_TOPIC_HEIGHT = 18;
/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.ConnectionLine = new Class({
    initialize:function(sourceNode, targetNode, lineType) {
        $assert(targetNode, 'parentNode node can not be null');
        $assert(sourceNode, 'childNode node can not be null');
        $assert(sourceNode != targetNode, 'Cilcular connection');

        this._targetTopic = targetNode;
        this._sourceTopic = sourceNode;

        var strokeColor = mindplot.ConnectionLine.getStrokeColor();
        var line;
        if (targetNode.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
            line = this._createLine(lineType, mindplot.ConnectionLine.CURVED);
            //        line = new web2d.Line();
            if (line.getType() == "CurvedLine") {
                var ctrlPoints = this._getCtrlPoints(sourceNode, targetNode);
                line.setSrcControlPoint(ctrlPoints[0]);
                line.setDestControlPoint(ctrlPoints[1]);
            }
            line.setStroke(1, 'solid', strokeColor);
        } else {
            line = this._createLine(lineType, mindplot.ConnectionLine.SIMPLE_CURVED);
            if (line.getType() == "CurvedLine") {
                var ctrlPoints = this._getCtrlPoints(sourceNode, targetNode);
                line.setSrcControlPoint(ctrlPoints[0]);
                line.setDestControlPoint(ctrlPoints[1]);
            }
            //        line = new web2d.PolyLine();
            line.setStroke(1, 'solid', strokeColor);
        }

        this._line2d = line;
    },

    _getCtrlPoints : function(sourceNode, targetNode) {
        var srcPos = sourceNode.workoutOutgoingConnectionPoint(targetNode.getPosition());
        var destPos = targetNode.workoutIncomingConnectionPoint(sourceNode.getPosition());
        var deltaX = (srcPos.x - destPos.x) / 3;
        return [new core.Point(deltaX, 0), new core.Point(-deltaX, 0)];
    },

    _createLine : function(lineType, defaultStyle) {
        if (!$defined(lineType)) {
            lineType = defaultStyle;
        }
        lineType = parseInt(lineType);
        this._lineType = lineType;
        var line = null;
        switch (lineType) {
            case mindplot.ConnectionLine.POLYLINE:
                line = new web2d.PolyLine();
                break;
            case mindplot.ConnectionLine.CURVED:
                line = new web2d.CurvedLine();
                break;
            case mindplot.ConnectionLine.SIMPLE_CURVED:
                line = new web2d.CurvedLine();
                line.setStyle(web2d.CurvedLine.SIMPLE_LINE);
                break;
            default:
                line = new web2d.Line();
                break;
        }
        return line;
    },

    setVisibility : function(value) {
        this._line2d.setVisibility(value);
    },

    isVisible : function() {
        return this._line2d.isVisible();
    },

    setOpacity : function(opacity) {
        this._line2d.setOpacity(opacity);
    },

    redraw : function() {
        var line2d = this._line2d;
        var sourceTopic = this._sourceTopic;
        var sourcePosition = sourceTopic.getPosition();

        var targetTopic = this._targetTopic;
        var targetPosition = targetTopic.getPosition();

        var sPos,tPos;
        sPos = sourceTopic.workoutOutgoingConnectionPoint(targetPosition, false);
        tPos = targetTopic.workoutIncomingConnectionPoint(sourcePosition, false);

        line2d.setFrom(tPos.x, tPos.y);
        line2d.setTo(sPos.x, sPos.y);

        if (line2d.getType() == "CurvedLine") {
            var ctrlPoints = this._getCtrlPoints(this._sourceTopic, this._targetTopic);
            line2d.setSrcControlPoint(ctrlPoints[0]);
            line2d.setDestControlPoint(ctrlPoints[1]);
        }
//    line2d.moveToBack();

        // Add connector ...
        this._positionateConnector(targetTopic);

    },

    _positionateConnector : function(targetTopic) {
        var targetPosition = targetTopic.getPosition();
        var offset = mindplot.Topic.CONNECTOR_WIDTH / 2;
        var targetTopicSize = targetTopic.getSize();
        var y;
        if (targetTopic.getShapeType() == mindplot.model.NodeModel.SHAPE_TYPE_LINE) {
            y = targetTopicSize.height;
        } else {
            y = targetTopicSize.height / 2;
        }
        y = y - offset;

        var connector = targetTopic.getShrinkConnector();
        if (Math.sign(targetPosition.x) > 0) {
            var x = targetTopicSize.width;
            connector.setPosition(x, y);
        }
        else {
            var x = -mindplot.Topic.CONNECTOR_WIDTH;
            connector.setPosition(x, y);
        }
    },

    setStroke : function(color, style, opacity) {
        var line2d = this._line2d;
        this._line2d.setStroke(null, null, color, opacity);
    },

    addToWorkspace : function(workspace) {
        workspace.appendChild(this._line2d);
        this._line2d.moveToBack();
    },

    removeFromWorkspace : function(workspace) {
        workspace.removeChild(this._line2d);
    },

    getTargetTopic : function() {
        return this._targetTopic;
    },

    getSourceTopic : function() {
        return this._sourceTopic;
    },

    getLineType : function() {
        return this._lineType;
    },

    getLine : function() {
        return this._line2d;
    },

    getModel : function() {
        return this._model;
    },

    setModel : function(model) {
        this._model = model;
    },

    getType : function() {
        return "ConnectionLine";
    },

    getId : function() {
        return this._model.getId();
    },

    moveToBack : function() {
        this._line2d.moveToBack();
    },

    moveToFront : function() {
        this._line2d.moveToFront();
    }
});

mindplot.ConnectionLine.getStrokeColor = function() {
    return '#495879';
};

mindplot.ConnectionLine.SIMPLE = 0;
mindplot.ConnectionLine.POLYLINE = 1;
mindplot.ConnectionLine.CURVED = 2;
mindplot.ConnectionLine.SIMPLE_CURVED = 3;/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
mindplot.RelationshipLine = new Class({
    Extends: mindplot.ConnectionLine,
    initialize:function(sourceNode, targetNode, lineType) {
        this.parent(sourceNode, targetNode, lineType);

        this._line2d.setIsSrcControlPointCustom(false);
        this._line2d.setIsDestControlPointCustom(false);
        this._isOnfocus = false;
        this._focusShape = this._createLine(this.getLineType(), mindplot.ConnectionLine.SIMPLE_CURVED);
        this._focusShape.setStroke(2, "solid", "#3f96ff");
        var ctrlPoints = this._line2d.getControlPoints();
        this._focusShape.setSrcControlPoint(ctrlPoints[0]);
        this._focusShape.setDestControlPoint(ctrlPoints[1]);
        this._focusShape.setVisibility(false);
        this._onFocus = false;
        this._isInWorkspace = false;
        this._controlPointsController = new mindplot.ControlPoint();

        var strokeColor = mindplot.RelationshipLine.getStrokeColor();
        this._startArrow = new web2d.Arrow();
        this._endArrow = new web2d.Arrow();
        this._startArrow.setStrokeColor(strokeColor);
        this._startArrow.setStrokeWidth(2);
        this._endArrow.setStrokeColor(strokeColor);
        this._endArrow.setStrokeWidth(2);
        this._line2d.setStroke(1, 'solid', strokeColor);

    },

    setStroke : function(color, style, opacity) {
        // @Todo: How this is supported in mootools ?
        mindplot.ConnectionLine.prototype.setStroke.call(this, color, style, opacity);
        this._startArrow.setStrokeColor(color);
    },

    redraw : function() {
        var line2d = this._line2d;
        var sourceTopic = this._sourceTopic;
        var sourcePosition = sourceTopic.getPosition();

        var targetTopic = this._targetTopic;
        var targetPosition = targetTopic.getPosition();

        var sPos,tPos;
        this._line2d.setStroke(2);
        var ctrlPoints = this._line2d.getControlPoints();
        if (!this._line2d.isDestControlPointCustom() && !this._line2d.isSrcControlPointCustom()) {
            var defaultPoints = core.Utils.calculateDefaultControlPoints(sourcePosition, targetPosition);
            ctrlPoints[0].x = defaultPoints[0].x;
            ctrlPoints[0].y = defaultPoints[0].y;
            ctrlPoints[1].x = defaultPoints[1].x;
            ctrlPoints[1].y = defaultPoints[1].y;
        }
        var spoint = new core.Point();
        spoint.x = parseInt(ctrlPoints[0].x) + parseInt(sourcePosition.x);
        spoint.y = parseInt(ctrlPoints[0].y) + parseInt(sourcePosition.y);
        var tpoint = new core.Point();
        tpoint.x = parseInt(ctrlPoints[1].x) + parseInt(targetPosition.x);
        tpoint.y = parseInt(ctrlPoints[1].y) + parseInt(targetPosition.y);
        sPos = core.Utils.calculateRelationShipPointCoordinates(sourceTopic, spoint);
        tPos = core.Utils.calculateRelationShipPointCoordinates(targetTopic, tpoint);

        line2d.setFrom(sPos.x, sPos.y);
        line2d.setTo(tPos.x, tPos.y);

        line2d.moveToFront();

        //Positionate Arrows
        this._positionateArrows();

        // Add connector ...
        this._positionateConnector(targetTopic);

        if (this.isOnFocus()) {
            this._refreshSelectedShape();
        }
        this._focusShape.moveToBack();
        this._controlPointsController.redraw();
    },

    _positionateArrows : function() {
        this._endArrow.setVisibility(this.isVisible() && this._showEndArrow);
        this._startArrow.setVisibility(this.isVisible() && this._showStartArrow);

        var tpos = this._line2d.getTo();
        this._endArrow.setFrom(tpos.x, tpos.y);
        var spos = this._line2d.getFrom();
        this._startArrow.setFrom(spos.x, spos.y);
        this._endArrow.moveToBack();
        this._startArrow.moveToBack();

        if (this._line2d.getType() == "CurvedLine") {
            var controlPoints = this._line2d.getControlPoints();
            this._startArrow.setControlPoint(controlPoints[0]);
            this._endArrow.setControlPoint(controlPoints[1]);
        } else {
            this._startArrow.setControlPoint(this._line2d.getTo());
            this._endArrow.setControlPoint(this._line2d.getFrom());
        }
    },

    addToWorkspace : function(workspace) {
        workspace.appendChild(this._focusShape);
        workspace.appendChild(this._controlPointsController);
        this._controlPointControllerListener = this._initializeControlPointController.bindWithEvent(this, workspace);
        this._line2d.addEventListener('click', this._controlPointControllerListener);
        this._isInWorkspace = true;

        workspace.appendChild(this._startArrow);
        workspace.appendChild(this._endArrow);

        mindplot.ConnectionLine.prototype.addToWorkspace.call(this, workspace);
    },

    _initializeControlPointController : function(event, workspace) {
        this.setOnFocus(true);
    },

    removeFromWorkspace : function(workspace) {
        workspace.removeChild(this._focusShape);
        workspace.removeChild(this._controlPointsController);
        this._line2d.removeEventListener('click', this._controlPointControllerListener);
        this._isInWorkspace = false;
        workspace.removeChild(this._startArrow);
        workspace.removeChild(this._endArrow);

        mindplot.ConnectionLine.prototype.removeFromWorkspace.call(this, workspace);
    },

    getType : function() {
        return mindplot.RelationshipLine.type;
    },

    setOnFocus : function(focus) {
        // Change focus shape
        if (focus) {
            this._refreshSelectedShape();
            this._controlPointsController.setLine(this);
        }
        this._focusShape.setVisibility(focus);

        this._controlPointsController.setVisibility(focus);
        this._onFocus = focus;
    },

    _refreshSelectedShape : function () {
        var sPos = this._line2d.getFrom();
        var tPos = this._line2d.getTo();
        var ctrlPoints = this._line2d.getControlPoints();
        this._focusShape.setFrom(sPos.x, sPos.y);
        this._focusShape.setTo(tPos.x, tPos.y);
        var shapeCtrlPoints = this._focusShape.getControlPoints();
        shapeCtrlPoints[0].x = ctrlPoints[0].x;
        shapeCtrlPoints[0].y = ctrlPoints[0].y;
        shapeCtrlPoints[1].x = ctrlPoints[1].x;
        shapeCtrlPoints[1].y = ctrlPoints[1].y;
        this._focusShape.updateLine();
        //this._focusShape.setSrcControlPoint(ctrlPoints[0]);
        //this._focusShape.setDestControlPoint(ctrlPoints[1]);
    },

    addEventListener : function(type, listener) {
        // Translate to web 2d events ...
        if (type == 'onfocus') {
            type = 'mousedown';
        }

        var line = this._line2d;
        line.addEventListener(type, listener);
    },

    isOnFocus : function() {
        return this._onFocus;
    },

    isInWorkspace : function() {
        return this._isInWorkspace;
    },

    setVisibility : function(value) {
        mindplot.ConnectionLine.prototype.setVisibility.call(this, value);
        this._endArrow.setVisibility(this._showEndArrow && value);
        this._startArrow.setVisibility(this._showStartArrow && value);
    },

    setOpacity : function(opacity) {
        mindplot.ConnectionLine.prototype.setOpacity.call(this, opacity);
        if (this._showEndArrow)
            this._endArrow.setOpacity(opacity);
        if (this._showStartArrow)
            this._startArrow.setOpacity(opacity);
    },

    setShowEndArrow : function(visible) {
        this._showEndArrow = visible;
        if (this._isInWorkspace)
            this.redraw();
    },

    setShowStartArrow : function(visible) {
        this._showStartArrow = visible;
        if (this._isInWorkspace)
            this.redraw();
    },

    isShowEndArrow : function() {
        return this._showEndArrow;
    },

    isShowStartArrow : function() {
        return this._showStartArrow;
    },

    setFrom : function(x, y) {
        this._line2d.setFrom(x, y);
        this._startArrow.setFrom(x, y);
    },

    setTo : function(x, y) {
        this._line2d.setTo(x, y);
        this._endArrow.setFrom(x, y);
    },

    setSrcControlPoint : function(control) {
        this._line2d.setSrcControlPoint(control);
        this._startArrow.setControlPoint(control);
    },

    setDestControlPoint : function(control) {
        this._line2d.setDestControlPoint(control);
        this._endArrow.setControlPoint(control);
    },

    getControlPoints : function() {
        return this._line2d.getControlPoints();
    },

    isSrcControlPointCustom : function() {
        return this._line2d.isSrcControlPointCustom();
    },

    isDestControlPointCustom : function() {
        return this._line2d.isDestControlPointCustom();
    },

    setIsSrcControlPointCustom : function(isCustom) {
        this._line2d.setIsSrcControlPointCustom(isCustom);
    },

    setIsDestControlPointCustom : function(isCustom) {
        this._line2d.setIsDestControlPointCustom(isCustom);
    }});


mindplot.RelationshipLine.type = "RelationshipLine";
mindplot.RelationshipLine.getStrokeColor = function() {
    return '#9b74e6';
}
/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.DragTopicPositioner = new Class({
    initialize:function(layoutManager) {
        $assert(layoutManager, 'layoutManager can not be null');
        this._layoutManager = layoutManager;
        this._topics = layoutManager.getDesigner()._getTopics();
        this._workspace = layoutManager.getDesigner().getWorkSpace();
    },

    positionateDragTopic : function(dragTopic) {
        // Workout the real position of the element on the board.
        var dragTopicPosition = dragTopic.getPosition();
        var draggedTopic = dragTopic.getDraggedTopic();

        // Topic can be connected ?
        this._checkDragTopicConnection(dragTopic);

        // Position topic in the board
        if (dragTopic.isConnected()) {
            var targetTopic = dragTopic.getConnectedToTopic();
            var topicBoard = this._layoutManager.getTopicBoardForTopic(targetTopic);
            topicBoard.positionateDragTopic(dragTopic);
        }
    },

    _checkDragTopicConnection : function(dragTopic) {
        var topics = this._topics;

        // Must be disconnected from their current connection ?.
        var mainTopicToMainTopicConnection = this._lookUpForMainTopicToMainTopicConnection(dragTopic);
        var currentConnection = dragTopic.getConnectedToTopic();
        if ($defined(currentConnection)) {
            // MainTopic->MainTopicConnection.
            if (currentConnection.getType() == mindplot.model.NodeModel.MAIN_TOPIC_TYPE) {
                if (mainTopicToMainTopicConnection != currentConnection) {
                    dragTopic.disconnect(this._workspace);
                }
            }
            else if (currentConnection.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                // Distance if greater that the allowed.
                var dragXPosition = dragTopic.getPosition().x;
                var currentXPosition = currentConnection.getPosition().x;

                if ($defined(mainTopicToMainTopicConnection)) {
                    // I have to change the current connection to a main topic.
                    dragTopic.disconnect(this._workspace);
                } else
                if (Math.abs(dragXPosition - currentXPosition) > mindplot.DragTopicPositioner.CENTRAL_TO_MAINTOPIC_MAX_HORIZONTAL_DISTANCE) {
                    dragTopic.disconnect(this._workspace);
                }
            }
        }

        // Finally, connect nodes ...
        if (!dragTopic.isConnected()) {
            var centalTopic = topics[0];
            if ($defined(mainTopicToMainTopicConnection)) {
                dragTopic.connectTo(mainTopicToMainTopicConnection);
            } else if (Math.abs(dragTopic.getPosition().x - centalTopic.getPosition().x) <= mindplot.DragTopicPositioner.CENTRAL_TO_MAINTOPIC_MAX_HORIZONTAL_DISTANCE) {
                dragTopic.connectTo(centalTopic);
            }
        }
    },

    _lookUpForMainTopicToMainTopicConnection : function(dragTopic) {
        var topics = this._topics;
        var result = null;
        var clouserDistance = -1;
        var draggedNode = dragTopic.getDraggedTopic();
        var distance = null;

        // Check MainTopic->MainTopic connection...
        for (var i = 0; i < topics.length; i++) {
            var targetTopic = topics[i];
            var position = dragTopic.getPosition();
            if (targetTopic.getType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE && targetTopic != draggedNode) {
                var canBeConnected = dragTopic.canBeConnectedTo(targetTopic);
                if (canBeConnected) {
                    var targetPosition = targetTopic.getPosition();
                    var fix = position.y > targetPosition.y;
                    var gap = 0;
                    if (targetTopic._getChildren().length > 0) {
                        gap = Math.abs(targetPosition.y - targetTopic._getChildren()[0].getPosition().y)
                    }
                    var yDistance = Math.abs(position.y - fix * gap - targetPosition.y);
                    if (distance == null || yDistance < distance) {
                        result = targetTopic;
                        distance = yDistance;
                    }

                }
            }
        }
        return result;
    }
});

mindplot.DragTopicPositioner.CENTRAL_TO_MAINTOPIC_MAX_HORIZONTAL_DISTANCE = 400;
/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.TextEditor = new Class({
    initialize:function(designer, actionRunner) {
        this._designer = designer;
        this._screenManager = designer.getWorkSpace().getScreenManager();
        this._container = this._screenManager.getContainer();
        this._actionRunner = actionRunner;
        this._isVisible = false;

        //Create editor ui
        this._createUI();

        this._addListeners();

    },

    _createUI:function() {
        this._size = {width:500, height:100};
        this._myOverlay = new Element('div').setStyles({position:"absolute", display: "none", zIndex: "8", top: 0, left:0, width:"500px", height:"100px"});
        var inputContainer = new Element('div').setStyles({border:"none", overflow:"auto"}).inject(this._myOverlay);
        this.inputText = new Element('input').setProperties({type:"text", tabindex:'-1', id:"inputText", value:""}).setStyles({border:"none", background:"transparent"}).inject(inputContainer);
        var spanContainer = new Element('div').setStyle('visibility', "hidden").inject(this._myOverlay);
        this._spanText = new Element('span').setProperties({id: "spanText", tabindex:"-1"}).setStyle('white-space', "nowrap").setStyle('nowrap', 'nowrap').inject(spanContainer);
        this._myOverlay.inject(this._container);
    },

    _addListeners:function() {
        var elem = this;
        this.applyChanges = true;
        this.inputText.onkeyup = function (evt) {
            var event = new Event(evt);
            var key = event.key;
            switch (key) {
                case 'esc':
                    elem.applyChanges = false;
                case 'enter':
                    var executor = function(editor) {
                        return function() {
                            elem.lostFocus(true);
                            $(document.documentElement).fireEvent('focus');
                        };
                    };
                    setTimeout(executor(this), 3);

                    break;
                default:
                    var span = $('spanText');
                    var input = $('inputText');
                    span.innerHTML = input.value;
                    var size = input.value.length + 1;
                    input.size = size;
                    if (span.offsetWidth > (parseInt(elem._myOverlay.style.width) - 100)) {
                        elem._myOverlay.style.width = (span.offsetWidth + 100) + "px";
                    }
                    break;
            }
        };
        //Register onLostFocus/onBlur event
        $(this.inputText).addEvent('blur', this.lostFocusEvent.bind(this));
        $(this._myOverlay).addEvent('click', this.clickEvent.bindWithEvent(this));
        $(this._myOverlay).addEvent('dblclick', this.clickEvent.bindWithEvent(this));
        $(this._myOverlay).addEvent('mousedown', this.mouseDownEvent.bindWithEvent(this));

        var elem = this;
        var onComplete = function() {
            this._myOverlay.setStyle('display', "none");
            this._isVisible = false;
            this.inputText.setStyle('opacity', 1);

            this.setPosition(0, 0);
            if (elem._currentNode != null) {
                this._currentNode.getTextShape().setVisibility(true);
                if (this.applyChanges) {
                    this._updateNode();
                }
                this.applyChanges = true;
                this._currentNode = null;
            }

            setTimeout("$('ffoxWorkarroundInput').focus();", 0);
        };
        this.fx = new Fx.Tween(this.inputText, {property: 'opacity', duration: 10});
        this.fx.addEvent('onComplete', onComplete.bind(this));
    },

    lostFocusEvent : function () {
        this.fx.options.duration = 10;
        this.fx.start(1, 0);
        //myAnim.animate();
    },

    isVisible : function () {
        return this._isVisible;
    },

    getFocusEvent: function (node) {
        //console.log('focus event');
        if (this.isVisible()) {
            this.getFocusEvent.delay(10, this);
        }
        else {
            //console.log('calling init');
            this.init(node);
        }
        //console.log('focus event done');
    },

    setInitialText : function (text) {
        this.initialText = text;
    },

    _updateNode : function () {

        if ($defined(this._currentNode) && this._currentNode.getText() != this.getText()) {
            var text = this.getText();
            var topicId = this._currentNode.getId();

            var commandFunc = function(topic, value) {
                var result = topic.getText();
                topic.setText(value);
                return result;
            };
            var command = new mindplot.commands.GenericFunctionCommand(commandFunc, text, [topicId]);
            this._actionRunner.execute(command);
        }
    },

    listenEventOnNode : function(topic, eventName, stopPropagation) {
        var elem = this;
        topic.addEventListener(eventName, function (event) {
            if (elem._designer.getWorkSpace().isWorkspaceEventsEnabled()) {
                mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeMouseOutEvent, [topic ]);
                elem.lostFocus();
                elem.getFocusEvent.attempt(topic, elem);

                if (stopPropagation) {
                    if ($defined(event.stopPropagation)) {
                        event.stopPropagation(true);
                    } else {
                        event.cancelBubble = true;
                    }
                }
            }
        });
    },

    init : function (nodeGraph) {
        //console.log('init method');
        nodeGraph.getTextShape().setVisibility(false);
        this._currentNode = nodeGraph;

        //set Editor Style
        var nodeText = nodeGraph.getTextShape();
        var text;
        var selectText = true;
        if (this.initialText && this.initialText != "") {
            text = this.initialText;
            this.initialText = null;
            selectText = false;
        }
        else
            text = nodeText.getText();

        var font = nodeText.getFont();
        font.size = nodeText.getHtmlFontSize();
        font.color = nodeText.getColor();

        this.setStyle(font);

        //set editor's initial text
        this.setText(text);

        //set editor's initial size
        var editor = this;
        var executor = function(editor) {
            return function() {
                //console.log('setting editor in init thread');
                var scale = web2d.peer.utils.TransformUtil.workoutScale(editor._currentNode.getTextShape()._peer);
                var elemSize = editor._currentNode.getSize();
                //var textSize = editor.getSize();
                var pos = editor._screenManager.getWorkspaceElementPosition(editor._currentNode);

                var textWidth = editor._currentNode.getTextShape().getWidth();
                var textHeight = editor._currentNode.getTextShape().getHeight();
                var iconGroup = editor._currentNode.getIconGroup();
                var iconGroupSize;
                if ($defined(iconGroup)) {
                    iconGroupSize = editor._currentNode.getIconGroup().getSize();
                }
                else {
                    iconGroupSize = {width:0, height:0};
                }
                var position = {x:0,y:0};
                position.x = pos.x - ((textWidth * scale.width) / 2) + (((iconGroupSize.width) * scale.width) / 2);
                var fixError = 1;
                position.y = pos.y - ((textHeight * scale.height) / 2) - fixError;

                editor.setEditorSize(elemSize.width, elemSize.height, scale);
                //console.log('setting position:'+pos.x+';'+pos.y);
                editor.setPosition(position.x, position.y, scale);
                editor.showTextEditor(selectText);
                //console.log('setting editor done');
            };
        };

        setTimeout(executor(this), 10);
        //console.log('init done');
    },

    setStyle : function (fontStyle) {
        var inputField = $("inputText");
        var spanField = $("spanText");
        if (!$defined(fontStyle.font)) {
            fontStyle.font = "Arial";
        }
        if (!$defined(fontStyle.style)) {
            fontStyle.style = "normal";
        }
        if (!$defined(fontStyle.weight)) {
            fontStyle.weight = "normal";
        }
        if (!$defined(fontStyle.size)) {
            fontStyle.size = 12;
        }
        inputField.style.fontSize = fontStyle.size + "px";
        inputField.style.fontFamily = fontStyle.font;
        inputField.style.fontStyle = fontStyle.style;
        inputField.style.fontWeight = fontStyle.weight;
        inputField.style.color = fontStyle.color;
        spanField.style.fontFamily = fontStyle.font;
        spanField.style.fontStyle = fontStyle.style;
        spanField.style.fontWeight = fontStyle.weight;
        spanField.style.fontSize = fontStyle.size + "px";
    },

    setText : function(text) {
        var inputField = $("inputText");
        inputField.size = text.length + 1;
        //this._myOverlay.cfg.setProperty("width", (inputField.size * parseInt(inputField.style.fontSize) + 100) + "px");
        this._myOverlay.style.width = (inputField.size * parseInt(inputField.style.fontSize) + 100) + "px";
        var spanField = $("spanText");
        spanField.innerHTML = text;
        inputField.value = text;
    },

    getText : function() {
        return $('inputText').value;
    },

    setEditorSize : function (width, height, scale) {
        //var scale = web2d.peer.utils.TransformUtil.workoutScale(this._currentNode.getTextShape()._peer);
        this._size = {width:width * scale.width, height:height * scale.height};
        //this._myOverlay.cfg.setProperty("width",this._size.width*2+"px");
        this._myOverlay.style.width = this._size.width * 2 + "px";
        //this._myOverlay.cfg.setProperty("height",this._size.height+"px");
        this._myOverlay.style.height = this._size.height + "px";
    },

    getSize : function () {
        return {width:$("spanText").offsetWidth,height:$("spanText").offsetHeight};
    },

    setPosition : function (x, y, scale) {
        $(this._myOverlay).setStyles({top : y + "px", left: x + "px"});
        //this._myOverlay.style.left = x + "px";
    },

    showTextEditor : function(selectText) {
        //this._myOverlay.show();
        //var myAnim = new YAHOO.util.Anim('inputText',{opacity: {to:1}}, 0.10, YAHOO.util.Easing.easeOut);
        //$('inputText').style.opacity='1';
        var elem = this;
        //myAnim.onComplete.subscribe(function(){
        //elem._myOverlay.show();
        elem._myOverlay.setStyle('display', "block");
        this._isVisible = true;
        //elem.cfg.setProperty("visible", false);
        //elem._myOverlay.cfg.setProperty("xy", [0, 0]);
        //elem._myOverlay.cfg.setProperty("visible", true);
        //select the text in the input
        $('inputText').disabled = false;

        if ($('inputText').createTextRange) //ie
        {
            var range = $('inputText').createTextRange();
            var pos = $('inputText').value.length;
            if (selectText) {
                range.select();
                range.move("character", pos);
            }
            else {
                range.move("character", pos);
                range.select();
            }
        }
        else if (selectText) {
            $('inputText').setSelectionRange(0, $('inputText').value.length);
        }

        var executor = function(editor) {
            return function() {
                try {
                    $('inputText').focus();
                }
                catch (e) {

                }
            };
        };
        setTimeout(executor(this), 0);
        //});
        //myAnim.animate();

    },

    lostFocus : function(bothBrowsers) {
        if (this.isVisible()) {
            //the editor is opened in another node. lets Finish it.
            var fireOnThis = $('inputText');
            fireOnThis.fireEvent('blur');
        }
    },
    clickEvent : function(event) {
        if (this.isVisible()) {
            if ($defined(event.stopPropagation)) {
                event.stopPropagation(true);
            } else {
                event.cancelBubble = true;
            }
            event.preventDefault();
        }

    },
    mouseDownEvent : function(event) {
        if (this.isVisible()) {
            if ($defined(event.stopPropagation)) {
                event.stopPropagation(true);
            } else {
                event.cancelBubble = true;
            }
        }
    }

});

/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.RichTextEditor = mindplot.TextEditor.extend({
    initialize:function(screenManager,actionRunner){
        this.parent(screenManager, actionRunner);
    },
    _createUI:function(){
        //Create editor ui
        this._size = {width:440, height:200};
        this._myOverlay = new Element('div').setStyles({position:"absolute", display: "none", zIndex: "8", top: "50%", left:"50%", marginLeft:"-200px", marginTop:"-90px", width:"400px", height:"180px"});
        var inputContainer = new Element('div').setStyles({border:"none", overflow:"auto"}).inject(this._myOverlay);
        this.inputText = new Element('textarea').setProperties({tabindex:'-1', id:"inputText2", value:""}).setStyles({width:"398px", height:"175px", border:"none", background:"transparent"}).inject(inputContainer);
        this._myOverlay.inject(this._screenManager.getContainer());
        this._editorNode = new web2d.Rect(0.3,mindplot.Topic.OUTER_SHAPE_ATTRIBUTES);
        this._editorNode.setSize(50,20);
        this._editorNode.setVisibility(false);
        this._designer.getWorkSpace().appendChild(this._editorNode);
        this._addListeners();
    },
    _addListeners:function(){

        $(this._myOverlay).addEvent('click', function(event){
            event.preventDefault();
            event.stop();
        }.bindWithEvent(this));
        $(this._myOverlay).addEvent('dblclick', function(event){
            event.preventDefault();
            event.stop();
        }.bindWithEvent(this));
    },
    getFocusEvent:function(node){
        var screenSize = this._designer.getWorkSpace().getSize();
        var coordOrigin = this._designer.getWorkSpace()._workspace.getCoordOrigin();
        var middlePosition = {x:parseInt(screenSize.width)/2 + parseInt(coordOrigin.x), y:parseInt(screenSize.height)/2 + parseInt(coordOrigin.y)};

        this._designer.getWorkSpace().enableWorkspaceEvents(false);
        var position = node.getPosition().clone();
        var size = node.getSize();
        this._editorNode.setPosition(position.x-(size.width/2), position.y-(size.height/2));
        position = this._editorNode.getPosition();
        this._editorNode.setSize(size.width, size.height);
        this._editorNode.moveToFront();
        this._editorNode.setVisibility(true);
        var scale = web2d.peer.utils.TransformUtil.workoutScale(node.getOuterShape());
//        scale.width=1;
//        scale.height = 1;
        var steps = 10;
        this._delta = {width:((this._size.width/scale.width)-size.width)/steps, height:((this._size.height/scale.height)-size.height)/steps};
        var finx = (middlePosition.x-(((this._size.width)/2)/scale.width));
        var finy = (middlePosition.y-((this._size.height/2)/scale.height));
        var step = 10;
        var d = {x:(position.x - finx)/step, y:(position.y - finy)/step};
        var _animEffect = null;
        var effect = function(){
            if(step>=0){
                var xStep= (position.x -finx)/step;
                var yStep= (position.y -finy)/step;
                var pos = {x:position.x - d.x*(10-step), y: position.y -d.y *(10-step)};

                var size = this._editorNode.getSize();
                this._editorNode.setSize(size.width + this._delta.width, size.height + this._delta.height);
                this._editorNode.setPosition(pos.x, pos.y);
                if(step>0)
                    this._editorNode.setOpacity(1-step/10);
                step--;
            }else{
                $clear(_animEffect);
                this._editorNode.setSize((this._size.width/scale.width), (this._size.height/scale.height));
                this.init(node);
            }
        }.bind(this);
        _animEffect = effect.periodical(10);
        $(this.inputText).value = $defined(this.initialText)&& this.initialText!=""? this.initialText: node.getText();
        this._editor = new nicEditor({iconsPath: '../images/nicEditorIcons.gif', buttonList : ['bold','italic','underline','removeformat','forecolor', 'fontSize', 'fontFamily', 'xhtml']}).panelInstance("inputText2");
    },
    init:function(node){
        this._currentNode = node;
        this.applyChanges = false;
        $(this._myOverlay.setStyle('display','block'));
        inst = this._editor.instanceById("inputText2");
        inst.elm.focus();



        //becarefull this._editor is not mootools!!
        this._editor.addEvent('blur',function(event){
            this._myOverlay.setStyle('display','none');
                var text = this._text;
                this._text = this._editor.instanceById("inputText2").getContent();
                if(text!=this._text){
                    this.applyChanges = true;
                }
                console.log('bye');
                this.lostFocusListener();
                this._editor.removeInstance("inputText2");
                this._editor.destruct();
                this._editor = null;

        }.bind(this));

        this._editor.fireEvent();
        $(this.inputText).focus();
    },
    getText:function(){
        return this._text;
    },
    lostFocusListener:function(){
        this._hideNode();
        if (this._currentNode != null)
            {
                if(this.applyChanges)
                {
                    this._updateNode();
                }
                this.applyChanges=true;
                this._currentNode = null;
            }
    },
    _hideNode:function(){
        var _animEffect = null;
        var step = 10;
        var position = this._editorNode.getPosition();
        var finx = this._currentNode.getPosition().x - this._currentNode.getSize().width/2;
        var finy = this._currentNode.getPosition().y - this._currentNode.getSize().height/2;
        var d = {x:(position.x - finx)/step, y:(position.y - finy)/step};
        var effect = function(){
            if(step>=0){
                var pos = {x:position.x - d.x*(10-step), y: position.y - d.y*(10-step)};

                var size = this._editorNode.getSize();
                this._editorNode.setSize(size.width - this._delta.width, size.height - this._delta.height);
                this._editorNode.setPosition(pos.x, pos.y);
                this._editorNode.setOpacity(step/10);
                step--;
            }else{
                $clear(_animEffect);
                this._designer.getWorkSpace().enableWorkspaceEvents(true);
                this._editorNode.setVisibility(false);            }
        }.bind(this);
        _animEffect = effect.periodical(10);
    }
});
/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
mindplot.TextEditorFactory = {};

mindplot.TextEditorFactory.getTextEditorFromName = function(name) {
    var editorClass = null;
    if (name == "RichTextEditor") {
        editorClass = mindplot.RichTextEditor;
    } else {
        editorClass = mindplot.TextEditor;
    }
    return editorClass;
};/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.VariableDistanceBoard = new Class({
    Extends: mindplot.Board,
    initialize: function(defaultHeight, referencePoint) {
        this.parent(defaultHeight, referencePoint);
        var zeroEntryCoordinate = referencePoint.y;
        var entry = this.createBoardEntry(zeroEntryCoordinate - (defaultHeight / 2), zeroEntryCoordinate + (defaultHeight / 2), 0);
        this._entries.set(0, entry);
    },

    lookupEntryByOrder:function(order) {
        var entries = this._entries;
        var index = this._orderToIndex(order);

        var result = entries.get(index);
        if (!$defined(result)) {
            // I've not found a entry. I have to create a new one.
            var i = 1;
            var zeroEntry = entries.get(0);
            var distance = zeroEntry.getWidth() / 2;
            var indexSign = Math.sign(index);
            var absIndex = Math.abs(index);
            while (i < absIndex) {
                // Move to the next entry ...
                var entry = entries.get(i, indexSign);
                if (entry != null) {
                    distance += entry.getWidth();
                } else {
                    distance += this._defaultWidth;
                }
                i++;
            }

            // Calculate limits ...
            var upperLimit = -1;
            var lowerLimit = -1;
            var offset = zeroEntry.workoutEntryYCenter();
            if (index >= 0) {
                lowerLimit = offset + distance;
                upperLimit = lowerLimit + this._defaultWidth;
            } else {
                upperLimit = offset - distance;
                lowerLimit = upperLimit - this._defaultWidth;
            }

            result = this.createBoardEntry(lowerLimit, upperLimit, order);
        }
        return result;
    },

    createBoardEntry:function(lowerLimit, upperLimit, order) {
        return  new mindplot.BoardEntry(lowerLimit, upperLimit, order);
    },

    updateReferencePoint:function(position) {
        var entries = this._entries;
        var referencePoint = this._referencePoint;

        // Update zero entry current position.
        this._referencePoint = position.clone();
        var yOffset = position.y - referencePoint.y;

        var i = -entries.lowerLength();
        for (; i <= entries.length(1); i++) {
            var entry = entries.get(i);
            if (entry != null) {
                var upperLimit = entry.getUpperLimit() + yOffset;
                var lowerLimit = entry.getLowerLimit() + yOffset;
                entry.setUpperLimit(upperLimit);
                entry.setLowerLimit(lowerLimit);

                // Update topic position ...
                if (!entry.isAvailable()) {
                    var topic = entry.getTopic();
                    var topicPosition = topic.getPosition();
                    topicPosition.y = topicPosition.y + yOffset;

                    // MainTopicToCentral must be positioned based on the referencePoint.
                    var xOffset = position.x - referencePoint.x;
                    topicPosition.x = topicPosition.x + xOffset;

                    topic.setPosition(topicPosition);
                }
            }
        }
    },

    lookupEntryByPosition:function(pos) {
        $assert(pos, 'position can not be null');
        var entries = this._entries;
        var zeroEntry = entries.get(0);
        if (zeroEntry.isCoordinateIn(pos.y)) {
            return zeroEntry;
        }

        // Is Upper or lower ?
        var sign = -1;
        if (pos.y >= zeroEntry.getUpperLimit()) {
            sign = 1;
        }

        var i = 1;
        var tempEntry = this.createBoardEntry();
        var currentEntry = zeroEntry;
        while (true) {
            // Move to the next entry ...
            var index = i * sign;
            var entry = entries.get(index);
            if ($defined(entry)) {
                currentEntry = entry;
            } else {
                // Calculate boundaries...
                var lowerLimit, upperLimit;
                if (sign > 0) {
                    lowerLimit = currentEntry.getUpperLimit();
                    upperLimit = lowerLimit + this._defaultWidth;
                }
                else {
                    upperLimit = currentEntry.getLowerLimit();
                    lowerLimit = upperLimit - this._defaultWidth;
                }

                // Update current entry.
                currentEntry = tempEntry;
                currentEntry.setLowerLimit(lowerLimit);
                currentEntry.setUpperLimit(upperLimit);

                var order = this._indexToOrder(index);
                currentEntry.setOrder(order);
            }

            // Have I found the item?
            if (currentEntry.isCoordinateIn(pos.y)) {
                break;
            }
            i++;
        }
        return currentEntry;
    },

    update:function(entry) {
        $assert(entry, 'Entry can not be null');
        var order = entry.getOrder();
        var index = this._orderToIndex(order);

        this._entries.set(index, entry);

    },
    freeEntry:function(entry) {
        var order = entry.getOrder();
        var entries = this._entries;

        var index = this._orderToIndex(order);
        var indexSign = Math.sign(index);

        var currentTopic = entry.getTopic();
        var i = Math.abs(index) + 1;
        while (currentTopic) {
            var e = entries.get(i, indexSign);
            if ($defined(currentTopic) && !$defined(e)) {
                var entryOrder = this._indexToOrder(i * indexSign);
                e = this.lookupEntryByOrder(entryOrder);
            }

            // Move the topic to the next entry ...
            var topic = null;
            if ($defined(e)) {
                topic = e.getTopic();
                if ($defined(currentTopic)) {
                    e.setTopic(currentTopic);
                }
                this.update(e);
            }
            currentTopic = topic;
            i++;
        }

        // Clear the entry topic ...
        entry.setTopic(null);
    },

    _orderToIndex:function(order) {
        var index = Math.round(order / 2);
        return ((order % 2) == 0) ? index : -index;
    },

    _indexToOrder:function(index) {
        var order = Math.abs(index) * 2;
        return (index >= 0) ? order : order - 1;
    },

    inspect:function() {
        return this._entries.inspect();
    }

});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.util.Shape =
{
    isAtRight: function(sourcePoint, targetPoint)
    {
        $assert(sourcePoint, "Source can not be null");
        $assert(targetPoint, "Target can not be null");
        return sourcePoint.x < targetPoint.x;
    },
    workoutDistance: function(sourceNode, targetNode)
    {
        var sPos = sourceNode.getPosition();
        var tPos = targetNode.getPosition();

        var x = tPos.x - sPos.x;
        var y = tPos.y - sPos.y;

        var hip = y * y + x * x;
        return hip;
    },
    calculateRectConnectionPoint: function(rectCenterPoint, rectSize, isAtRight)
    {
        $assert(rectCenterPoint, 'rectCenterPoint can  not be null');
        $assert(rectSize, 'rectSize can  not be null');
        $assert(isAtRight, 'isRight can  not be null');

        // Node is placed at the right ?
        var result = new core.Point();

        // This is used fix a minor difference ...z
        var correctionHardcode = 2;
        if (isAtRight)
        {
            result.setValue(rectCenterPoint.x - (rectSize.width / 2) + correctionHardcode, rectCenterPoint.y);
        } else
        {
            result.setValue(parseFloat(rectCenterPoint.x) + (rectSize.width / 2) - correctionHardcode, rectCenterPoint.y);
        }

        return result;
    },
    _getRectShapeOffset : function(sourceTopic, targetTopic)
    {

        var tPos = targetTopic.getPosition();
        var sPos = sourceTopic.getPosition();

        var tSize = targetTopic.getSize();

        var x = sPos.x - tPos.x;
        var y = sPos.y - tPos.y;

        var gradient = 0;
        if ($defined(x))
        {
            gradient = y / x;
        }

        var area = this._getSector(gradient, x, y);
        var xOff = -1;
        var yOff = -1;
        if (area == 1 || area == 3)
        {
            xOff = tSize.width / 2;
            yOff = xOff * gradient;

            xOff = xOff * ((x < 0) ? -1 : 1);
            yOff = yOff * ((x < 0) ? -1 : 1);


        } else
        {
            yOff = tSize.height / 2;
            xOff = yOff / gradient;

            yOff = yOff * ((y < 0) ? -1 : 1);
            xOff = xOff * ((y < 0) ? -1 : 1);
        }


        // Controll boundaries.
        if (Math.abs(xOff) > tSize.width / 2)
        {
            xOff = ((tSize.width / 2) * Math.sign(xOff));
        }

        if (Math.abs(yOff) > tSize.height / 2)
        {
            yOff = ((tSize.height / 2) * Math.sign(yOff));
        }

        return {x:xOff,y:yOff};
    },

/**
 *  Sector are numered following the clockwise direction.
 */
    _getSector : function(gradient, x, y)
    {
        var result;
        if (gradient < 0.5 && gradient > -0.5)
        {
            // Sector 1 and 3
            if (x >= 0)
            {
                result = 1;
            } else
            {
                result = 3;
            }

        } else
        {
            // Sector 2 and 4
            if (y <= 0)
            {
                result = 4;
            } else
            {
                result = 2;
            }
        }

        return result;
    }
};

/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.FixedDistanceBoard = new Class({
    Extends:mindplot.Board,
    initialize:function(defaultHeight, topic, layoutManager) {
        this._topic = topic;
        this._layoutManager = layoutManager;
        var reference = topic.getPosition();
        this.parent(defaultHeight, reference);
        this._height = defaultHeight;
        this._entries = [];
    },

    getHeight : function() {
        return this._height;
    },

    lookupEntryByOrder : function(order) {
        var result = null;
        var entries = this._entries;
        if (order < entries.length) {
            result = entries[order];
        }

        if (result == null) {
            var defaultHeight = this._defaultWidth;
            var reference = this.getReferencePoint();
            if (entries.length == 0) {
                var yReference = reference.y;
                result = this.createBoardEntry(yReference - (defaultHeight / 2), yReference + (defaultHeight / 2), 0);
            } else {
                var entriesLenght = entries.length;
                var lastEntry = entries[entriesLenght - 1];
                var lowerLimit = lastEntry.getUpperLimit();
                var upperLimit = lowerLimit + defaultHeight;
                result = this.createBoardEntry(lowerLimit, upperLimit, entriesLenght + 1);
            }
        }
        return result;
    },

    createBoardEntry : function(lowerLimit, upperLimit, order) {
        var result = new mindplot.BoardEntry(lowerLimit, upperLimit, order);
        var xPos = this.workoutXBorderDistance();
        result.setXPosition(xPos);
        return result;
    },

    updateReferencePoint : function() {
        var entries = this._entries;
        var parentTopic = this.getTopic();
        var parentPosition = parentTopic.workoutIncomingConnectionPoint(parentTopic.getPosition());
        var referencePoint = this.getReferencePoint();
        var yOffset = parentPosition.y - referencePoint.y;

        for (var i = 0; i < entries.length; i++) {
            var entry = entries[i];

            if ($defined(entry)) {
                var upperLimit = entry.getUpperLimit() + yOffset;
                var lowerLimit = entry.getLowerLimit() + yOffset;
                entry.setUpperLimit(upperLimit);
                entry.setLowerLimit(lowerLimit);

                // Fix x position ...
                var xPos = this.workoutXBorderDistance();
                entry.setXPosition(xPos);
                entry.update();
            }
        }
        this._referencePoint = parentPosition.clone();

    },

    /**
     * This x distance doesn't take into account the size of the shape.
     */
    workoutXBorderDistance : function() {
        var topic = this.getTopic();

        var topicPosition = topic.getPosition();
        var topicSize = topic.getSize();
        var halfTargetWidth = topicSize.width / 2;
        var result;
        if (topicPosition.x >= 0) {
            // It's at right.
            result = topicPosition.x + halfTargetWidth + mindplot.FixedDistanceBoard.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE;
        } else {
            result = topicPosition.x - (halfTargetWidth + mindplot.FixedDistanceBoard.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE);
        }
        return result;
    },

    getTopic : function() {
        return this._topic;
    },

    freeEntry : function(entry) {
        var newEntries = [];
        var entries = this._entries;
        var order = 0;
        for (var i = 0; i < entries.length; i++) {
            var e = entries[i];
            if (e == entry) {
                order++;
            }
            newEntries[order] = e;
            order++;
        }
        this._entries = newEntries;
    },

    repositionate : function() {
        // Workout width and update topic height.
        var entries = this._entries;
        var height = 0;
        var model = this._topic.getModel();
        if (entries.length >= 1 && !model.areChildrenShrinked()) {
            for (var i = 0; i < entries.length; i++) {
                var e = entries[i];
                if (e && e.getTopic()) {
                    var topic = e.getTopic();
                    var topicBoard = this._layoutManager.getTopicBoardForTopic(topic);
                    var topicBoardHeight = topicBoard.getHeight();


                    height += topicBoardHeight + mindplot.FixedDistanceBoard.INTER_TOPIC_DISTANCE;
                }
            }
        }
        else {
            var topic = this._topic;
            height = topic.getSize().height + mindplot.FixedDistanceBoard.INTER_TOPIC_DISTANCE;
        }

        var oldHeight = this._height;
        this._height = height;

        // I must update all the parent nodes first...
        if (oldHeight != this._height) {
            var topic = this._topic;
            var parentTopic = topic.getParent();
            if (parentTopic != null) {
                var board = this._layoutManager.getTopicBoardForTopic(parentTopic);
                board.repositionate();
            }
        }


        // @todo: Esto hace backtraking. Hay que cambiar la implementacion del set position de
        // forma tal que no se mande a hacer el update de todos los hijos.

        // Workout center the new topic center...
        var refence = this.getReferencePoint();
        var lowerLimit;
        if (entries.length > 0) {
            var l = 0;
            for (l = 0; l < entries.length; l++) {
                if ($defined(entries[l]))
                    break;
            }
            var topic = entries[l].getTopic();
            var firstNodeHeight = topic.getSize().height;
            lowerLimit = refence.y - (height / 2) - (firstNodeHeight / 2) + 1;
        }

        var upperLimit = null;

        // Start moving all the elements ...
        var newEntries = [];
        var order = 0;
        for (var i = 0; i < entries.length; i++) {
            var e = entries[i];
            if (e && e.getTopic()) {

                var currentTopic = e.getTopic();
                e.setLowerLimit(lowerLimit);

                // Update entry ...
                var topicBoard = this._layoutManager.getTopicBoardForTopic(currentTopic);
                var topicBoardHeight = topicBoard.getHeight();

                upperLimit = lowerLimit + topicBoardHeight + mindplot.FixedDistanceBoard.INTER_TOPIC_DISTANCE;
                e.setUpperLimit(upperLimit);
                lowerLimit = upperLimit;

                e.setOrder(order);
                currentTopic.setOrder(order);

                e.update();
                newEntries[order] = e;
                order++;
            }
        }
        this._entries = newEntries;
    },

    removeTopic : function(topic) {
        var order = topic.getOrder();
        var entry = this.lookupEntryByOrder(order);
        $assert(!entry.isAvailable(), "Illegal state");

        entry.setTopic(null);
        topic.setOrder(null);
        this._entries.erase(entry);

        // Repositionate all elements ...
        this.repositionate();
    },

    addTopic : function(order, topic) {

        // If the entry is not available, I must swap the the entries...
        var entry = this.lookupEntryByOrder(order);
        if (!entry.isAvailable()) {
            this.freeEntry(entry);
            // Create a dummy entry ...
            // Puaj, do something with this...
            entry = this.createBoardEntry(-1, 0, order);
            this._entries[order] = entry;
        }
        this._entries[order] = entry;

        // Add to the board ...
        entry.setTopic(topic, false);

        // Repositionate all elements ...
        this.repositionate();
    },

    lookupEntryByPosition : function(pos) {
        $assert(pos, 'position can not be null');

        var entries = this._entries;
        var result = null;
        for (var i = 0; i < entries.length; i++) {
            var entry = entries[i];
            if (pos.y < entry.getUpperLimit() && pos.y >= entry.getLowerLimit()) {
                result = entry;
            }
        }

        if (result == null) {
            var defaultHeight = this._defaultWidth;
            if (entries.length == 0) {
                var reference = this.getReferencePoint();
                var yReference = reference.y;
                result = this.createBoardEntry(yReference - (defaultHeight / 2), yReference + (defaultHeight / 2), 0);
            } else {
                var firstEntry = entries[0];
                if (pos.y < firstEntry.getLowerLimit()) {
                    var upperLimit = firstEntry.getLowerLimit();
                    var lowerLimit = upperLimit - defaultHeight;
                    result = this.createBoardEntry(lowerLimit, upperLimit, 0);
                } else {
                    var entriesLenght = entries.length;
                    var lastEntry = entries[entriesLenght - 1];
                    var lowerLimit = lastEntry.getUpperLimit();
                    var upperLimit = lowerLimit + defaultHeight;
                    result = this.createBoardEntry(lowerLimit, upperLimit, entriesLenght);
                }
            }
        }

        return result;
    }
})
    ;
mindplot.FixedDistanceBoard.INTER_TOPIC_DISTANCE = 6;
mindplot.FixedDistanceBoard.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE = 60;

/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.BoardEntry = new Class({
    initialize:function(lowerLimit, upperLimit, order) {
        if ($defined(lowerLimit) && $defined(upperLimit)) {
            $assert(lowerLimit < upperLimit, 'lowerLimit can not be greater that upperLimit');
        }
        this._upperLimit = upperLimit;
        this._lowerLimit = lowerLimit;
        this._order = order;
        this._topic = null;
        this._xPos = null;
    },


    getUpperLimit : function() {
        return this._upperLimit;
    },

    setXPosition : function(xPosition) {
        this._xPos = xPosition;
    },

    workoutEntryYCenter : function() {
        return this._lowerLimit + ((this._upperLimit - this._lowerLimit) / 2);
    },

    setUpperLimit : function(value) {
        $assert(value, "upper limit can not be null");
        $assert(!isNaN(value), "illegal value");
        this._upperLimit = value;
    },

    isCoordinateIn : function(coord) {
        return this._lowerLimit <= coord && coord < this._upperLimit;
    },

    getLowerLimit : function() {
        return this._lowerLimit;
    },

    setLowerLimit : function(value) {
        $assert(value, "upper limit can not be null");
        $assert(!isNaN(value), "illegal value");
        this._lowerLimit = value;
    },

    setOrder : function(value) {
        this._order = value;
    },

    getWidth : function() {
        return Math.abs(this._upperLimit - this._lowerLimit);
    },


    getTopic : function() {
        return this._topic;
    },


    removeTopic : function() {
        $assert(!this.isAvailable(), "Entry doesn't have a topic.");
        var topic = this.getTopic();
        this.setTopic(null);
        topic.setOrder(null);
    },


    update : function() {
        var topic = this.getTopic();
        this.setTopic(topic);
    },

    setTopic : function(topic, updatePosition) {
        if (!$defined(updatePosition) || ($defined(updatePosition) && !updatePosition)) {
            updatePosition = true;
        }

        this._topic = topic;
        if ($defined(topic)) {
            // Fixed positioning. Only for main topic ...
            var position = null;
            var topicPosition = topic.getPosition();

            // Must update position base on the border limits?
            if ($defined(this._xPos)) {
                position = new core.Point();

                // Update x position ...
                var topicSize = topic.getSize();
                var halfTopicWidh = parseInt(topicSize.width / 2);
                halfTopicWidh = (this._xPos > 0) ? halfTopicWidh : -halfTopicWidh;
                position.x = this._xPos + halfTopicWidh;
                position.y = this.workoutEntryYCenter();
            } else {

                // Central topic
                this._height = topic.getSize().height;
                var xPos = topicPosition.x;
                var yPos = this.workoutEntryYCenter();
                position = new core.Point(xPos, yPos);
            }

            // @todo: No esta de mas...
            topic.setPosition(position);
            topic.setOrder(this._order);
        }
        else {
            this._height = this._defaultWidth;
        }
    },

    isAvailable : function() {
        return !$defined(this._topic);
    },

    getOrder : function() {
        return this._order;
    },

    inspect : function() {
        return '(order: ' + this._order + ', lowerLimit:' + this._lowerLimit + ', upperLimit: ' + this._upperLimit + ', available:' + this.isAvailable() + ')';
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
mindplot.ModelCodeName ={};

mindplot.ModelCodeName.BETA = "beta";
mindplot.ModelCodeName.PELA = "pela";
/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.XMLMindmapSerializer_Pela = new Class({

    toXML : function(mindmap) {
        $assert(mindmap, "Can not save a null mindmap");

        var document = core.Utils.createDocument();

        // Store map attributes ...
        var mapElem = document.createElement("map");
        var name = mindmap.getId();
        if ($defined(name)) {
            mapElem.setAttribute('name', name);
        }
        var version = mindmap.getVersion();
        if ($defined(version)) {
            mapElem.setAttribute('version', version);
        }

        document.appendChild(mapElem);

        // Create branches ...
        var topics = mindmap.getBranches();
        for (var i = 0; i < topics.length; i++) {
            var topic = topics[i];
            var topicDom = this._topicToXML(document, topic);
            mapElem.appendChild(topicDom);
        }

        // Create Relationships
        var relationships = mindmap.getRelationships();
        if (relationships.length > 0) {
//        var relationshipDom=document.createElement("relationships");
//        mapElem.appendChild(relationshipDom);
            for (var j = 0; j < relationships.length; j++) {
                var relationDom = this._relationshipToXML(document, relationships[j]);
                mapElem.appendChild(relationDom);
            }
        }

        return document;
    },

    _topicToXML : function(document, topic) {
        var parentTopic = document.createElement("topic");

        // Set topic attributes...
        if (topic.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
            parentTopic.setAttribute("central", true);
        } else {
            var parent = topic.getParent();
//        if (parent == null || parent.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE)
//        {
            var pos = topic.getPosition();
            parentTopic.setAttribute("position", pos.x + ',' + pos.y);
//        } else
//        {
            var order = topic.getOrder();
            parentTopic.setAttribute("order", order);
//        }
        }

        var text = topic.getText();
        if ($defined(text)) {
            parentTopic.setAttribute('text', text);
        }

        var shape = topic.getShapeType();
        if ($defined(shape)) {
            parentTopic.setAttribute('shape', shape);
        }

        if (topic.areChildrenShrinked()) {
            parentTopic.setAttribute('shrink', true);
        }

        // Font properties ...
        var id = topic.getId();
        parentTopic.setAttribute('id', id);

        var font = "";

        var fontFamily = topic.getFontFamily();
        font += (fontFamily ? fontFamily : '') + ';';

        var fontSize = topic.getFontSize();
        font += (fontSize ? fontSize : '') + ';';

        var fontColor = topic.getFontColor();
        font += (fontColor ? fontColor : '') + ';';

        var fontWeight = topic.getFontWeight();
        font += (fontWeight ? fontWeight : '') + ';';

        var fontStyle = topic.getFontStyle();
        font += (fontStyle ? fontStyle : '') + ';';

        if ($defined(fontFamily) || $defined(fontSize) || $defined(fontColor)
            || $defined(fontWeight) || $defined(fontStyle)) {
            parentTopic.setAttribute('fontStyle', font);
        }

        var bgColor = topic.getBackgroundColor();
        if ($defined(bgColor)) {
            parentTopic.setAttribute('bgColor', bgColor);
        }

        var brColor = topic.getBorderColor();
        if ($defined(brColor)) {
            parentTopic.setAttribute('brColor', brColor);
        }

        //ICONS
        var icons = topic.getIcons();
        for (var i = 0; i < icons.length; i++) {
            var icon = icons[i];
            var iconDom = this._iconToXML(document, icon);
            parentTopic.appendChild(iconDom);
        }

        //LINKS
        var links = topic.getLinks();
        for (var i = 0; i < links.length; i++) {
            var link = links[i];
            var linkDom = this._linkToXML(document, link);
            parentTopic.appendChild(linkDom);
        }

        var notes = topic.getNotes();
        for (var i = 0; i < notes.length; i++) {
            var note = notes[i];
            var noteDom = this._noteToXML(document, note);
            parentTopic.appendChild(noteDom);
        }

        //CHILDREN TOPICS
        var childTopics = topic.getChildren();
        for (var i = 0; i < childTopics.length; i++) {
            var childTopic = childTopics[i];
            var childDom = this._topicToXML(document, childTopic);
            parentTopic.appendChild(childDom);

        }

        return parentTopic;
    },

    _iconToXML : function(document, icon) {
        var iconDom = document.createElement("icon");
        iconDom.setAttribute('id', icon.getIconType());
        return iconDom;
    },

    _linkToXML : function(document, link) {
        var linkDom = document.createElement("link");
        linkDom.setAttribute('url', link.getUrl());
        return linkDom;
    },

    _noteToXML : function(document, note) {
        var noteDom = document.createElement("note");
        noteDom.setAttribute('text', note.getText());
        return noteDom;
    },

    _relationshipToXML : function(document, relationship) {
        var relationDom = document.createElement("relationship");
        relationDom.setAttribute("srcTopicId", relationship.getFromNode());
        relationDom.setAttribute("destTopicId", relationship.getToNode());
        var lineType = relationship.getLineType();
        relationDom.setAttribute("lineType", lineType);
        if (lineType == mindplot.ConnectionLine.CURVED || lineType == mindplot.ConnectionLine.SIMPLE_CURVED) {
            if ($defined(relationship.getSrcCtrlPoint())) {
                var srcPoint = relationship.getSrcCtrlPoint();
                relationDom.setAttribute("srcCtrlPoint", srcPoint.x + "," + srcPoint.y);
            }
            if ($defined(relationship.getDestCtrlPoint())) {
                var destPoint = relationship.getDestCtrlPoint();
                relationDom.setAttribute("destCtrlPoint", destPoint.x + "," + destPoint.y);
            }
        }
        relationDom.setAttribute("endArrow", relationship.getEndArrow());
        relationDom.setAttribute("startArrow", relationship.getStartArrow());
        return relationDom;
    },

    loadFromDom : function(dom) {
        $assert(dom, "Dom can not be null");
        var rootElem = dom.documentElement;

        // Is a wisemap?.
        $assert(rootElem.tagName == mindplot.XMLMindmapSerializer_Pela.MAP_ROOT_NODE, "This seem not to be a map document.");

        this._idsMap = new Hash();
        // Start the loading process ...
        var mindmap = new mindplot.model.Mindmap();

        var version = rootElem.getAttribute("version");
        mindmap.setVersion(version);

        var children = rootElem.childNodes;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.nodeType == 1) {
                switch (child.tagName) {
                    case "topic":
                        var topic = this._deserializeNode(child, mindmap);
                        mindmap.addBranch(topic);
                        break;
                    case "relationship":
                        var relationship = this._deserializeRelationship(child, mindmap);
                        if (relationship != null)
                            mindmap.addRelationship(relationship);
                        break;
                }
            }
        }
        this._idsMap = null;
        return mindmap;
    },

    _deserializeNode : function(domElem, mindmap) {
        var type = (domElem.getAttribute('central') != null) ? mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE : mindplot.model.NodeModel.MAIN_TOPIC_TYPE;
        // Load attributes...
        var id = domElem.getAttribute('id');
        if ($defined(id)) {
            id = parseInt(id);
        }

        if (this._idsMap.has(id)) {
            id = null;
        } else {
            this._idsMap.set(id, domElem);
        }

        var topic = mindmap.createNode(type, id);

        var text = domElem.getAttribute('text');
        if ($defined(text)) {
            topic.setText(text);
        }

        var order = domElem.getAttribute('order');
        if ($defined(order)) {
            topic.setOrder(parseInt(order));
        }

        var shape = domElem.getAttribute('shape');
        if ($defined(shape)) {
            topic.setShapeType(shape);
        }

        var isShrink = domElem.getAttribute('shrink');
        if ($defined(isShrink)) {
            topic.setChildrenShrinked(isShrink);
        }

        var fontStyle = domElem.getAttribute('fontStyle');
        if ($defined(fontStyle)) {
            var font = fontStyle.split(';');

            if (font[0]) {
                topic.setFontFamily(font[0]);
            }

            if (font[1]) {
                topic.setFontSize(font[1]);
            }

            if (font[2]) {
                topic.setFontColor(font[2]);
            }

            if (font[3]) {
                topic.setFontWeight(font[3]);
            }

            if (font[4]) {
                topic.setFontStyle(font[4]);
            }
        }

        var bgColor = domElem.getAttribute('bgColor');
        if ($defined(bgColor)) {
            topic.setBackgroundColor(bgColor);
        }

        var borderColor = domElem.getAttribute('brColor');
        if ($defined(borderColor)) {
            topic.setBorderColor(borderColor);
        }

        var position = domElem.getAttribute('position');
        if ($defined(position)) {
            var pos = position.split(',');
            topic.setPosition(pos[0], pos[1]);
            topic.setFinalPosition(pos[0], pos[1]);
        }

        //Creating icons and children nodes
        var children = domElem.childNodes;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.nodeType == 1) {
                $assert(child.tagName == "topic" || child.tagName == "icon" || child.tagName == "link" || child.tagName == "note", 'Illegal node type:' + child.tagName);
                if (child.tagName == "topic") {
                    var childTopic = this._deserializeNode(child, mindmap);
                    childTopic.connectTo(topic);
                } else if (child.tagName == "icon") {
                    var icon = this._deserializeIcon(child, topic);
                    topic.addIcon(icon);
                } else if (child.tagName == "link") {
                    var link = this._deserializeLink(child, topic);
                    topic.addLink(link);
                } else if (child.tagName == "note") {
                    var note = this._deserializeNote(child, topic);
                    topic.addNote(note);
                }
            }
        }
        ;
        return topic;
    },

    _deserializeIcon : function(domElem, topic) {
        return topic.createIcon(domElem.getAttribute("id"));
    },

    _deserializeLink : function(domElem, topic) {
        return topic.createLink(domElem.getAttribute("url"));
    },

    _deserializeNote : function(domElem, topic) {
        return topic.createNote(domElem.getAttribute("text"));
    },

    _deserializeRelationship : function(domElement, mindmap) {
        var srcId = domElement.getAttribute("srcTopicId");
        var destId = domElement.getAttribute("destTopicId");
        var lineType = domElement.getAttribute("lineType");
        var srcCtrlPoint = domElement.getAttribute("srcCtrlPoint");
        var destCtrlPoint = domElement.getAttribute("destCtrlPoint");
        var endArrow = domElement.getAttribute("endArrow");
        var startArrow = domElement.getAttribute("startArrow");
        //If for some reason a relationship lines has source and dest nodes the same, don't import it.
        if (srcId == destId) {
            return null;
        }
        var model = mindmap.createRelationship(srcId, destId);
        model.setLineType(lineType);
        if ($defined(srcCtrlPoint) && srcCtrlPoint != "") {
            model.setSrcCtrlPoint(core.Point.fromString(srcCtrlPoint));
        }
        if ($defined(destCtrlPoint) && destCtrlPoint != "") {
            model.setDestCtrlPoint(core.Point.fromString(destCtrlPoint));
        }
        model.setEndArrow(endArrow == "true");
        model.setStartArrow(startArrow == "true");
        return model;
    }
});

mindplot.XMLMindmapSerializer_Pela.MAP_ROOT_NODE = 'map';/*    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
mindplot.XMLMindmapSerializer_Beta = new Class({

    toXML : function(mindmap) {
        $assert(mindmap, "Can not save a null mindmap");

        var document = core.Utils.createDocument();

        // Store map attributes ...
        var mapElem = document.createElement("map");
        var name = mindmap.getId();
        if ($defined(name)) {
            mapElem.setAttribute('name', name);
        }
        document.appendChild(mapElem);

        // Create branches ...
        var topics = mindmap.getBranches();
        for (var i = 0; i < topics.length; i++) {
            var topic = topics[i];
            var topicDom = this._topicToXML(document, topic);
            mapElem.appendChild(topicDom);
        }

        return document;
    },

    _topicToXML : function(document, topic) {
        var parentTopic = document.createElement("topic");

        // Set topic attributes...
        if (topic.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
            parentTopic.setAttribute("central", true);
        } else {
            var parent = topic.getParent();
            if (parent == null || parent.getType() == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {
                var pos = topic.getPosition();
                parentTopic.setAttribute("position", pos.x + ',' + pos.y);
            } else {
                var order = topic.getOrder();
                parentTopic.setAttribute("order", order);
            }
        }

        var text = topic.getText();
        if ($defined(text)) {
            parentTopic.setAttribute('text', text);
        }

        var shape = topic.getShapeType();
        if ($defined(shape)) {
            parentTopic.setAttribute('shape', shape);
        }

        if (topic.areChildrenShrinked()) {
            parentTopic.setAttribute('shrink', true);
        }

        // Font properties ...
        var font = "";

        var fontFamily = topic.getFontFamily();
        font += (fontFamily ? fontFamily : '') + ';';

        var fontSize = topic.getFontSize();
        font += (fontSize ? fontSize : '') + ';';

        var fontColor = topic.getFontColor();
        font += (fontColor ? fontColor : '') + ';';

        var fontWeight = topic.getFontWeight();
        font += (fontWeight ? fontWeight : '') + ';';

        var fontStyle = topic.getFontStyle();
        font += (fontStyle ? fontStyle : '') + ';';

        if ($defined(fontFamily) || $defined(fontSize) || $defined(fontColor)
            || $defined(fontWeight) || $defined(fontStyle)) {
            parentTopic.setAttribute('fontStyle', font);
        }

        var bgColor = topic.getBackgroundColor();
        if ($defined(bgColor)) {
            parentTopic.setAttribute('bgColor', bgColor);
        }

        var brColor = topic.getBorderColor();
        if ($defined(brColor)) {
            parentTopic.setAttribute('brColor', brColor);
        }

        //ICONS
        var icons = topic.getIcons();
        for (var i = 0; i < icons.length; i++) {
            var icon = icons[i];
            var iconDom = this._iconToXML(document, icon);
            parentTopic.appendChild(iconDom);
        }

        //LINKS
        var links = topic.getLinks();
        for (var i = 0; i < links.length; i++) {
            var link = links[i];
            var linkDom = this._linkToXML(document, link);
            parentTopic.appendChild(linkDom);
        }

        var notes = topic.getNotes();
        for (var i = 0; i < notes.length; i++) {
            var note = notes[i];
            var noteDom = this._noteToXML(document, note);
            parentTopic.appendChild(noteDom);
        }

        //CHILDREN TOPICS
        var childTopics = topic.getChildren();
        for (var i = 0; i < childTopics.length; i++) {
            var childTopic = childTopics[i];
            var childDom = this._topicToXML(document, childTopic);
            parentTopic.appendChild(childDom);

        }

        return parentTopic;
    },

    _iconToXML : function(document, icon) {
        var iconDom = document.createElement("icon");
        iconDom.setAttribute('id', icon.getIconType());
        return iconDom;
    },

    _linkToXML : function(document, link) {
        var linkDom = document.createElement("link");
        linkDom.setAttribute('url', link.getUrl());
        return linkDom;
    },

    _noteToXML : function(document, note) {
        var noteDom = document.createElement("note");
        noteDom.setAttribute('text', note.getText());
        return noteDom;
    },

    loadFromDom : function(dom) {
        $assert(dom, "Dom can not be null");
        var rootElem = dom.documentElement;

        // Is a wisemap?.
        $assert(rootElem.tagName == mindplot.XMLMindmapSerializer_Beta.MAP_ROOT_NODE, "This seem not to be a map document.");

        // Start the loading process ...
        var mindmap = new mindplot.model.Mindmap();

        var children = rootElem.childNodes;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.nodeType == 1) {
                var topic = this._deserializeNode(child, mindmap);
                mindmap.addBranch(topic);
            }
        }
        return mindmap;
    },

    _deserializeNode : function(domElem, mindmap) {
        var type = (domElem.getAttribute('central') != null) ? mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE : mindplot.model.NodeModel.MAIN_TOPIC_TYPE;
        var topic = mindmap.createNode(type);

        // Load attributes...
        var text = domElem.getAttribute('text');
        if ($defined(text)) {
            topic.setText(text);
        }

        var order = domElem.getAttribute('order');
        if ($defined(order)) {
            topic.setOrder(order);
        }

        var shape = domElem.getAttribute('shape');
        if ($defined(shape)) {
            topic.setShapeType(shape);
        }

        var isShrink = domElem.getAttribute('shrink');
        if ($defined(isShrink)) {
            topic.setChildrenShrinked(isShrink);
        }

        var fontStyle = domElem.getAttribute('fontStyle');
        if ($defined(fontStyle)) {
            var font = fontStyle.split(';');

            if (font[0]) {
                topic.setFontFamily(font[0]);
            }

            if (font[1]) {
                topic.setFontSize(font[1]);
            }

            if (font[2]) {
                topic.setFontColor(font[2]);
            }

            if (font[3]) {
                topic.setFontWeight(font[3]);
            }

            if (font[4]) {
                topic.setFontStyle(font[4]);
            }
        }

        var bgColor = domElem.getAttribute('bgColor');
        if ($defined(bgColor)) {
            topic.setBackgroundColor(bgColor);
        }

        var borderColor = domElem.getAttribute('brColor');
        if ($defined(borderColor)) {
            topic.setBorderColor(borderColor);
        }

        var position = domElem.getAttribute('position');
        if ($defined(position)) {
            var pos = position.split(',');
            topic.setPosition(pos[0], pos[1]);
        }

        //Creating icons and children nodes
        var children = domElem.childNodes;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.nodeType == 1) {
                $assert(child.tagName == "topic" || child.tagName == "icon" || child.tagName == "link" || child.tagName == "note", 'Illegal node type:' + child.tagName);
                if (child.tagName == "topic") {
                    var childTopic = this._deserializeNode(child, mindmap);
                    childTopic.connectTo(topic);
                } else if (child.tagName == "icon") {
                    var icon = this._deserializeIcon(child, topic);
                    topic.addIcon(icon);
                } else if (child.tagName == "link") {
                    var link = this._deserializeLink(child, topic);
                    topic.addLink(link);
                } else if (child.tagName == "note") {
                    var note = this._deserializeNote(child, topic);
                    topic.addNote(note);
                }
            }
        }

        return topic;
    },

    _deserializeIcon : function(domElem, topic) {
        return topic.createIcon(domElem.getAttribute("id"));
    },

    _deserializeLink : function(domElem, topic) {
        return topic.createLink(domElem.getAttribute("url"));
    },

    _deserializeNote : function(domElem, topic) {
        return topic.createNote(domElem.getAttribute("text"));
    }});

mindplot.XMLMindmapSerializer_Beta.MAP_ROOT_NODE = 'map';/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
mindplot.Beta2PelaMigrator = new Class({
    initialize : function(betaSerializer) {
        this._betaSerializer = betaSerializer;
        this._pelaSerializer = new mindplot.XMLMindmapSerializer_Pela();
    },

    toXML : function(mindmap) {
        return this._pelaSerializer.toXML(mindmap);
    },

    loadFromDom : function(dom) {
        var mindmap = this._betaSerializer.loadFromDom(dom);
        mindmap.setVersion(mindplot.ModelCodeName.PELA);
        return mindmap;
    }
});
/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
mindplot.XMLMindmapSerializerFactory = {};

mindplot.XMLMindmapSerializerFactory.getSerializerFromMindmap = function(mindmap) {
    return mindplot.XMLMindmapSerializerFactory.getSerializer(mindmap.getVersion());
};

mindplot.XMLMindmapSerializerFactory.getSerializerFromDocument = function(domDocument) {
    var rootElem = domDocument.documentElement;
    return mindplot.XMLMindmapSerializerFactory.getSerializer(rootElem.getAttribute("version"))
};


mindplot.XMLMindmapSerializerFactory.getSerializer = function(version) {
    if (!$defined(version)) {
        version = mindplot.ModelCodeName.BETA;
    }
    var codeNames = mindplot.XMLMindmapSerializerFactory._codeNames;
    var found = false;
    var serializer = null;
    for (var i = 0; i < codeNames.length; i++) {
        if (!found) {
            found = codeNames[i].codeName == version;
            if (found)
                serializer = new (codeNames[i].serializer)();
        } else {
            var migrator = codeNames[i].migrator;
            serializer = new migrator(serializer);
        }
    }

    return serializer;
};

mindplot.XMLMindmapSerializerFactory._codeNames =
    [
        {
            codeName:mindplot.ModelCodeName.BETA,
            serializer: mindplot.XMLMindmapSerializer_Beta,
            migrator:function() {//todo:error
            }
        },
        {
            codeName:mindplot.ModelCodeName.PELA,
            serializer:mindplot.XMLMindmapSerializer_Pela,
            migrator:mindplot.Beta2PelaMigrator
        }
    ];/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.PersistanceManager = {};

mindplot.PersistanceManager.save = function(mindmap, editorProperties, onSavedHandler,saveHistory)
{
    $assert(mindmap, "mindmap can not be null");
    $assert(editorProperties, "editorProperties can not be null");

    var mapId = mindmap.getId();

    var serializer = mindplot.XMLMindmapSerializerFactory.getSerializerFromMindmap(mindmap);
    var xmlMap = serializer.toXML(mindmap);
    var xmlMapStr = core.Utils.innerXML(xmlMap);

    var pref = JSON.toString(editorProperties);
    window.MapEditorService.saveMap(mapId, xmlMapStr, pref,saveHistory,
    {
        callback:function(response) {

            if (response.msgCode != "OK")
            {
                monitor.logError("Save could not be completed. Please,try again in a couple of minutes.");
                wLogger.error(response.msgDetails);
            } else
            {
                // Execute on success handler ...
                if ($defined(onSavedHandler))
                {
                    onSavedHandler();
                }
            }
        },
        errorHandler:function(message) {
            var monitor = core.Monitor.getInstance();
            monitor.logError("Save could not be completed. Please,try again in a couple of minutes.");
            wLogger.error(message);
        },
        verb:"POST",
        async: false
    });

};

mindplot.PersistanceManager.load = function(mapId)
{
    $assert(mapId, "mapId can not be null");

    var result = {r:null};
    window.MapEditorService.loadMap(mapId, {
        callback:function(response) {

            if (response.msgCode == "OK")
            {
                // Explorer Hack with local files ...
                var xmlContent = response.content;
                var domDocument = core.Utils.createDocumentFromText(xmlContent);
                var serializer = mindplot.XMLMindmapSerializerFactory.getSerializerFromDocument(domDocument);
                var mindmap = serializer.loadFromDom(domDocument);
                mindmap.setId(mapId);

                result.r = mindmap;
            } else
            {
                // Handle error message ...
                var msg = response.msgDetails;
                var monitor = core.Monitor.getInstance();
                monitor.logFatal("We're sorry, an error has occurred and we can't load your map. Please try again in a few minutes.");
                wLogger.error(msg);
            }
        },
        verb:"GET",
        async: false,
        errorHandler:function(msg) {
            var monitor = core.Monitor.getInstance();
            monitor.logFatal("We're sorry, an error has occurred and we can't load your map. Please try again in a few minutes.");
            wLogger.error(msg);
        }
    });

    return result.r;
};


/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.EditorProperties = new Class({
    initialize:function() {
        this._zoom = 0;
        this._position = 0;
    },

    setZoom : function(zoom) {
        this._zoom = zoom;
    },

    getZoom : function() {
        return this._zoom;
    },

    asProperties : function() {
        return "zoom=" + this._zoom + "\n";
    }
});




/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.IconGroup = new Class({
    initialize : function(topic) {
        var offset = topic.getOffset();

        this.options = {
            width:0,
            height:0,
            x:offset.x / 2,
            y:offset.y,
            icons:[],
            topic:topic,
            nativeElem:new web2d.Group({width: 2, height:2,x: offset, y:offset, coordSizeWidth:1,coordSizeHeight:1})
        };
        this.updateIconGroupPosition();
        this.registerListeners();
    },

    setPosition : function(x, y) {
        this.options.x = x;
        this.options.y = y;
        this.options.nativeElem.setPosition(x, y);
    },

    getPosition : function() {
        return {x:this.options.x, y:this.options.y};
    },

    setSize : function(width, height) {
        this.options.width = width;
        this.options.height = height;
        this.options.nativeElem.setSize(width, height);
        this.options.nativeElem.setCoordSize(width, height);
    },

    getSize : function() {
        return {width:this.options.width, height:this.options.height};
    },

    addIcon : function(icon) {
        icon.setGroup(this);
        var newIcon = icon.getImage();
        var nativeElem = this.options.nativeElem;
        var iconSize = newIcon.getSize();
        var size = nativeElem.getSize();
        newIcon.setPosition(size.width, 0);
        this.options.icons.extend([icon]);

        nativeElem.appendChild(newIcon);

        size.width = size.width + iconSize.width;
        if (iconSize.height > size.height) {
            size.height = iconSize.height;
        }

        nativeElem.setCoordSize(size.width, size.height);
        nativeElem.setSize(size.width, size.height);
        this.options.width = size.width;
        this.options.height = size.height;
    },

    getIcons : function() {
        return this.options.icons;
    },

    removeIcon : function(url) {
        this._removeIcon(this.getIcon(url));
    },

    removeImageIcon : function(icon) {

        var imgIcon = this.getImageIcon(icon);
        this._removeIcon(imgIcon);
    },

    getIcon : function(url) {
        var result = null;
        this.options.icons.each(function(el, index) {
            var nativeImage = el.getImage();
            if (nativeImage.getHref() == url) {
                result = el;
            }
        }, this);
        return result;
    },

    getImageIcon : function(icon) {
        var result = null;
        this.options.icons.each(function(el, index) {
            if (result == null && $defined(el.getModel().isIconModel) && el.getId() == icon.getId() && el.getUiId() == icon.getUiId()) {
                result = el;
            }
        }, this);
        return result;
    },

    findIconFromModel : function(iconModel) {
        var result = null;
        this.options.icons.each(function(el, index) {
            var elModel = el.getModel();
            if (result == null && $defined(elModel.isIconModel) && elModel.getId() == iconModel.getId()) {
                result = el;
            }
        }, this);

        if (result == null) {
            throw "Icon can no be found.";
        }

        return result;
    },

    _removeIcon : function(icon) {
        var nativeImage = icon.getImage();
        this.options.icons.erase(icon);
        var iconSize = nativeImage.getSize();
        var size = this.options.nativeElem.getSize();
        var position = nativeImage.getPosition();
        var childs = this.options.nativeElem.removeChild(nativeImage);
        this.options.icons.each(function(icon, index) {
            var img = icon.getImage();
            var pos = img.getPosition();
            if (pos.x > position.x) {
                img.setPosition(pos.x - iconSize.width, 0);
            }
        }.bind(this));
        size.width = size.width - iconSize.width;
        this.setSize(size.width, size.height);
    },

   getNativeElement : function() {
        return this.options.nativeElem;
    },

    moveToFront : function() {
        this.options.nativeElem.moveToFront();
    },

    registerListeners : function() {
        this.options.nativeElem.addEventListener('click', function(event) {
            // Avoid node creation ...
            if ($defined(event.stopPropagation)) {
                event.stopPropagation(true);
            } else {
                event.cancelBubble = true;
            }

        });
        this.options.nativeElem.addEventListener('dblclick', function(event) {
            // Avoid node creation ...
            if ($defined(event.stopPropagation)) {
                event.stopPropagation(true);
            } else {
                event.cancelBubble = true;
            }

        });
    },

    getTopic : function() {
        return this.options.topic;
    },

    updateIconGroupPosition : function() {
        var offsets = this._calculateOffsets();
        this.setPosition(offsets.x, offsets.y);
    },

    _calculateOffsets : function() {
        var offset = this.options.topic.getOffset();
        var text = this.options.topic.getTextShape();
        var sizeHeight = text.getHtmlFontSize();
        var yOffset = offset;
        var shape = this.options.topic.getShapeType();
        yOffset = text.getPosition().y + (sizeHeight - 18) / 2 + 1;
        return {x:offset, y:yOffset};
    }
});/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.BubbleTip = new Class({
    initialize : function(divContainer) {
        this.options = {
            panel:null,
            container:null,
            divContainer:divContainer,
            content:null,
            onShowComplete:Class.empty,
            onHideComplete:Class.empty,
            width:null,
            height:null,
            form:null
        };
        if ($defined(this.options.form))
            this.scanElements(this.options.form);
        this.buildBubble();
        this._isMouseOver = false;
        this._open = false;
    },
    scanElements : function(form) {
        $$($(form).getElements('a')).each(function(el) {
            if (el.href && el.hasClass('bubble') && !el.onclick) {
                el.addEvent('mouseover', this.click.bindWithEvent(this, el));
            }
        }, this);
    },
    buildBubble : function() {
        var opts = this.options;

        var panel = new Element('div').addClass('bubbleContainer');
        if ($defined(opts.height))
            panel.setStyle('height', opts.height);
        if ($defined(opts.width))
            panel.setStyle('width', opts.width);

        this.center = new Element('div').addClass('bubblePart').addClass('bubbleCenterBlue');
        this.center.inject(panel);
        if (!$defined(opts.divContainer)) {
            opts.divContainer = document.body;
        }
        panel.injectTop(opts.divContainer);
        opts.panel = $(panel);
        opts.panel.setStyle('opacity', 0);
        opts.panel.addEvent('mouseover', function() {
            this._isMouseOver = true;
        }.bind(this));
        opts.panel.addEvent('mouseleave', function(event) {
            this.close(event);
        }.bindWithEvent(this));//this.close.bindWithEvent(this)

    },
    click : function(event, el) {
        return this.open(event, el);
    },
    open : function(event, content, source) {
        this._isMouseOver = true;
        this._evt = new Event(event);
        this.doOpen.delay(500, this, [content,source]);
    },
    doOpen : function(content, source) {
        if ($defined(this._isMouseOver) && !$defined(this._open) && !$defined(this._opening)) {
            this._opening = true;
            var container = new Element('div');
            $(content).inject(container);
            this.options.content = content;
            this.options.container = container;
            $(this.options.container).inject(this.center);
            this.init(this._evt, source);
            $(this.options.panel).effect('opacity', {duration:500, onComplete:function() {
                this._open = true;
                this._opening = false;
            }.bind(this)}).start(0, 100);
        }
    },
    updatePosition : function(event) {
        this._evt = new Event(event);
    },
    close : function(event) {
        this._isMouseOver = false;
        this.doClose.delay(50, this, new Event(event));
    },
    doClose : function(event) {

        if (!$defined(this._isMouseOver) && $defined(this._opening))
            this.doClose.delay(500, this, this._evt);

        if (!$defined(this._isMouseOver) && $defined(this._open)) {
            this.forceClose();
        }
    },
    forceClose : function() {
        this.options.panel.effect('opacity', {duration:100, onComplete:function() {
            this._open = false;
            $(this.options.panel).setStyles({left:0,top:0});
            $(this.options.container).remove();
        }.bind(this)}).start(100, 0);
    },
    init : function(event, source) {
        var opts = this.options;
        var coordinates = $(opts.panel).getCoordinates();
        var panelHeight = coordinates.height; //not total height, but close enough
        var panelWidth = coordinates.width; //not total height, but close enough

        var offset = designer.getWorkSpace().getScreenManager().getWorkspaceIconPosition(source);

        var containerCoords = $(opts.divContainer).getCoordinates();
        var screenWidth = containerCoords.width;
        var screenHeight = containerCoords.height;

        var width = $(this.center).getCoordinates().width;

        var invert = !(offset.y > panelHeight); //hint goes on the bottom
        var invertX = (screenWidth - offset.x > panelWidth); // hint goes on the right
        $(this.options.panel).remove();
        this.buildBubble();
        $(this.options.container).inject(this.center);

        var height = $(this.center).getCoordinates().height;
        $(opts.panel).setStyles({width:width,height:height});
        this.moveTopic(offset, $(opts.panel).getCoordinates().height, $(opts.panel).getCoordinates().width, invert, invertX);
    },
    moveTopic : function(offset, panelHeight, panelWidth, invert, invertX) {
        var f = 1, fX = 1;
        if ($defined(invert))
            f = 0;
        if ($defined(invertX))
            fX = 0;
        var opts = this.options;
        $(opts.panel).setStyles({left:offset.x - (panelWidth * fX), top:offset.y - (panelHeight * f)});
    }

});

mindplot.BubbleTip.getInstance = function(divContainer) {
    var result = mindplot.BubbleTip.instance;
    if (!$defined(result)) {
        mindplot.BubbleTip.instance = new mindplot.BubbleTip(divContainer);
        result = mindplot.BubbleTip.instance;
    }
    return result;
}
/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.Tip = new Class({
    initialize:function(divContainer) {
        this.options = {
            panel:null,
            container:null,
            divContainer:divContainer,
            content:null,
            onShowComplete:Class.empty,
            onHideComplete:Class.empty,
            width:null,
            height:null,
            form:null
        };
        this.buildTip();
        this._isMouseOver = false;
        this._open = false;
    },

    buildTip  : function() {
        var opts = this.options;
        var panel = new Element('div').addClass('bubbleContainer');
        if ($defined(opts.height))
            panel.setStyle('height', opts.height);
        if ($defined(opts.width))
            panel.setStyle('width', opts.width);
        if (!$defined(opts.divContainer)) {
            opts.divContainer = document.body;
        }
        panel.injectTop(opts.divContainer);
        opts.panel = $(panel);
        opts.panel.setStyle('opacity', 0);
        opts.panel.addEvent('mouseover', function() {
            this._isMouseOver = true;
        }.bind(this));
        opts.panel.addEvent('mouseleave', function(event) {
            this.close(event);
        }.bindWithEvent(this));//this.close.bindWithEvent(this)

    },

    click  : function(event, el) {
        return this.open(event, el);
    },

    open  : function(event, content, source) {
        this._isMouseOver = true;
        this._evt = new Event(event);
        this.doOpen.delay(500, this, [content,source]);
    },

    doOpen  : function(content, source) {
        if ($defined(this._isMouseOver) && !$defined(this._open) && !$defined(this._opening)) {
            this._opening = true;
            var container = new Element('div');
            $(content).inject(container);
            this.options.content = content;
            this.options.container = container;
            $(this.options.container).inject(this.options.panel);
            this.init(this._evt, source);
            $(this.options.panel).effect('opacity', {duration:500, onComplete:function() {
                this._open = true;
                this._opening = false;
            }.bind(this)}).start(0, 100);
        }
    },

    updatePosition  : function(event) {
        this._evt = new Event(event);
    },

    close  : function(event) {
        this._isMouseOver = false;
        this.doClose.delay(50, this, new Event(event));
    },

    doClose  : function(event) {

        if (!$defined(this._isMouseOver) && $defined(this._opening))
            this.doClose.delay(500, this, this._evt);

        if (!$defined(this._isMouseOver) && $defined(this._open)) {
            this.forceClose();
        }
    },

    forceClose  : function() {
        this.options.panel.effect('opacity', {duration:100, onComplete:function() {
            this._open = false;
            $(this.options.panel).setStyles({left:0,top:0});
            $(this.options.container).dispose();
        }.bind(this)}).start(100, 0);
    },

    init  : function(event, source) {
        var opts = this.options;
        var coordinates = $(opts.panel).getCoordinates();
        var width = coordinates.width;   //not total width, but close enough
        var height = coordinates.height; //not total height, but close enough

        var offset = designer.getWorkSpace().getScreenManager().getWorkspaceIconPosition(source);

        var containerCoords = $(opts.divContainer).getCoordinates();
        var screenWidth = containerCoords.width;
        var screenHeight = containerCoords.height;

        $(this.options.panel).dispose();
        this.buildTip();
        $(this.options.container).inject(this.options.panel);
        this.moveTopic(offset, $(opts.panel).getCoordinates().height);
    },

    moveTopic  : function(offset, panelHeight) {
        var opts = this.options;
        var width = $(opts.panel).getCoordinates().width;
        $(opts.panel).setStyles({left:offset.x - (width / 2), top:offset.y - (panelHeight * 2) + 35});
    }

});

mindplot.Tip.getInstance = function(divContainer) {
    var result = mindplot.Tip.instance;
    if (!$defined(result)) {
        mindplot.Tip.instance = new mindplot.Tip(divContainer);
        result = mindplot.Tip.instance;
    }
    return result;
}/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.Icon = new Class({
    initialize:function(url) {
        $assert(url, 'topic can not be null');
        this._image = new web2d.Image();
        this._image.setHref(url);
        this._image.setSize(12, 12);
    },

    getImage : function() {
        return this._image;
    },

    setGroup : function(group) {
        this._group = group;
    },

    getGroup : function() {
        return this._group;
    },

    getSize : function() {
        return this._image.getSize();
    }
});


/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.LinkIcon = new Class({

    Extends:mindplot.Icon,
    initialize:function(urlModel, topic, designer) {
        $assert(urlModel, "urlModel can not be null");
        $assert(designer, "designer can not be null");
        $assert(topic, "topic can not be null");

        this.parent(mindplot.LinkIcon.IMAGE_URL);

        var divContainer = designer.getWorkSpace().getScreenManager().getContainer();
        var bubbleTip = mindplot.BubbleTip.getInstance(divContainer);

        this._linkModel = urlModel;
        this._topic = topic;
        this._designer = designer;
        var image = this.getImage();
        var imgContainer = new Element('div').setStyles({textAlign:'center', cursor:'pointer'});
        this._img = new Element('img');
        var url = urlModel.getUrl();
        this._img.src = 'http://open.thumbshots.org/image.pxf?url=' + url;

        if (url.indexOf('http:') == -1) {
            url = 'http://' + url;
        }
        this._img.alt = url;
        this._url = url;
        var openWindow = function() {
            var wOpen;
            var sOptions;

            sOptions = 'status=yes,menubar=yes,scrollbars=yes,resizable=yes,toolbar=yes';
            sOptions = sOptions + ',width=' + (screen.availWidth - 10).toString();
            sOptions = sOptions + ',height=' + (screen.availHeight - 122).toString();
            sOptions = sOptions + ',screenX=0,screenY=0,left=0,top=0';
            var url = this._img.alt;
            wOpen = window.open(url, "link", "width=100px, height=100px");
            wOpen.focus();
            wOpen.moveTo(0, 0);
            wOpen.resizeTo(screen.availWidth, screen.availHeight);
        };

        this._img.addEvent('click', openWindow.bindWithEvent(this));
        this._img.inject(imgContainer);

        var attribution = new Element('div').setStyles({fontSize:10, textAlign:"center"});
        attribution.innerHTML = "<a href='http://www.thumbshots.org' target='_blank' title='About Thumbshots thumbnails' style='color:#08468F'>About Thumbshots thumbnails</a>";

        var container = new Element('div');
        var element = new Element('div').setStyles({borderBottom:'1px solid #e5e5e5'});

        var title = new Element('div').setStyles({fontSize:12, textAlign:'center'});
        this._link = new Element('span');
        this._link.href = url;
        this._link.innerHTML = url;
        this._link.setStyle("text-decoration", "underline");
        this._link.setStyle("cursor", "pointer");
        this._link.inject(title);
        this._link.addEvent('click', openWindow.bindWithEvent(this));
        title.inject(element);

        imgContainer.inject(element);
        attribution.inject(element);
        element.inject(container);

        if (!$defined(designer._viewMode) || ($defined(designer._viewMode) && !designer._viewMode)) {
            var buttonContainer = new Element('div').setStyles({paddingTop:5, textAlign:'center'});
            var editBtn = new Element('input', {type:'button', 'class':'btn-primary', value:'Edit'}).addClass('button').inject(buttonContainer);
            var removeBtn = new Element('input', {type:'button', value:'Remove','class':'btn-primary'}).addClass('button').inject(buttonContainer);

            editBtn.setStyle("margin-right", "3px");
            removeBtn.setStyle("margin-left", "3px");

            removeBtn.addEvent('click', function(event) {
                var command = new mindplot.commands.RemoveLinkFromTopicCommand(this._topic.getId());
                designer._actionRunner.execute(command);
                bubbleTip.forceClose();
            }.bindWithEvent(this));

            var okButtonId = 'okLinkButtonId';
            editBtn.addEvent('click', function(event) {
                var topic = this._topic;
                var designer = this._designer;
                var link = this;
                var okFunction = function(e) {
                    var result = false;
                    var url = urlInput.value;
                    if ("" != url.trim()) {
                        link._img.src = 'http://open.thumbshots.org/image.pxf?url=' + url;
                        link._img.alt = url;
                        link._link.href = url;
                        link._link.innerHTML = url;
                        this._linkModel.setUrl(url);
                        result = true;
                    }
                    return result;
                };
                var msg = new Element('div');
                var urlText = new Element('div').inject(msg);
                urlText.innerHTML = "URL:";

                var formElem = new Element('form', {'action': 'none', 'id':'linkFormId'});
                var urlInput = new Element('input', {'type': 'text', 'size':30,'value':url});
                urlInput.inject(formElem);
                formElem.inject(msg);

                formElem.addEvent('submit', function(e) {
                    $(okButtonId).fireEvent('click', e);
                    e = new Event(e);
                    e.stop();
                });


                var dialog = mindplot.LinkIcon.buildDialog(designer, okFunction, okButtonId);
                dialog.adopt(msg).show();

            }.bindWithEvent(this));
            buttonContainer.inject(container);
        }

        var linkIcon = this;
        image.addEventListener('mouseover', function(event) {
            bubbleTip.open(event, container, linkIcon);
        });
        image.addEventListener('mousemove', function(event) {
            bubbleTip.updatePosition(event);
        });
        image.addEventListener('mouseout', function(event) {
            bubbleTip.close(event);
        });
    },

  getUrl : function() {
        return this._url;
    },

    getModel : function() {
        return this._linkModel;
    }
});

mindplot.LinkIcon.buildDialog = function(designer, okFunction, okButtonId) {
    var windoo = new Windoo({
        title: 'Write link URL',
        theme: Windoo.Themes.wise,
        modal:true,
        buttons:{'menu':false, 'close':false, 'minimize':false, 'roll':false, 'maximize':false},
        destroyOnClose:true,
        height:130
    });

    var cancel = new Element('input', {'type': 'button', 'class':'btn-primary', 'value': 'Cancel'}).setStyle('margin-right', "5px");
    cancel.setStyle('margin-left', "5px");
    cancel.addEvent('click', function(event) {
        $(document).addEvent('keydown', designer.keyEventHandler.bindWithEvent(designer));
        windoo.close();
    }.bindWithEvent(this));

    var ok = new Element('input', {'type': 'button', 'class':'btn-primary','value': 'Ok','id':okButtonId}).setStyle('marginRight', 10);
    ok.addEvent('click', function(event) {
        var couldBeUpdated = okFunction.attempt();
        if (couldBeUpdated) {
            $(document).addEvent('keydown', designer.keyEventHandler.bindWithEvent(designer));
            windoo.close();
        }
    }.bindWithEvent(this));

    var panel = new Element('div', {'styles': {'padding-top': 10, 'text-align': 'right'}}).adopt(ok, cancel);

    windoo.addPanel(panel);
    $(document).removeEvents('keydown');
    return windoo;
};

mindplot.LinkIcon.IMAGE_URL = "../images/world_link.png";

 /*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.Note = new Class({
    Extends: mindplot.Icon,
    initialize : function(textModel, topic, designer) {
        var divContainer = designer.getWorkSpace().getScreenManager().getContainer();
        var bubbleTip = mindplot.BubbleTip.getInstance(divContainer);
        mindplot.Icon.call(this, mindplot.Note.IMAGE_URL);
        this._noteModel = textModel;
        this._topic = topic;
        this._designer = designer;
        var image = this.getImage();
        var imgContainer = new Element('div').setStyles({textAlign:'center'});
        this._textElem = new Element('div').setStyles({'max-height':100,'max-width':300, 'overflow':'auto'});
        var text = unescape(textModel.getText());
        text = text.replace(/\n/ig, "<br/>");
        text = text.replace(/<script/ig, "&lt;script");
        text = text.replace(/<\/script/ig, "&lt;\/script");
        this._textElem.innerHTML = text;
        this._text = textModel.getText();

        this._textElem.inject(imgContainer);

        var container = new Element('div');

        imgContainer.inject(container);

        if (!$defined(designer._viewMode) || ($defined(designer._viewMode) && !designer._viewMode)) {
            var buttonContainer = new Element('div').setStyles({paddingTop:5, textAlign:'center'});
            var editBtn = new Element('input', {type:'button', value:'Edit','class':'btn-primary'}).addClass('button').inject(buttonContainer);
            var removeBtn = new Element('input', {type:'button', value:'Remove','class':'btn-primary'}).addClass('button').inject(buttonContainer);

            editBtn.setStyle("margin-right", "3px");
            removeBtn.setStyle("margin-left", "3px");

            removeBtn.addEvent('click', function(event) {
                var command = new mindplot.commands.RemoveNoteFromTopicCommand(this._topic.getId());
                designer._actionRunner.execute(command);
                bubbleTip.forceClose();
            }.bindWithEvent(this));

            var okButtonId = 'okNoteButtonId';
            editBtn.addEvent('click', function(event) {
                var topic = this._topic;
                var designer = this._designer;
                var note = this;

                var msg = new Element('div');
                var textarea = new Element('div').inject(msg);
                textarea.innerHTML = "Text";

                var formElem = new Element('form', {'action': 'none', 'id':'noteFormId'});
                var text = textModel.getText();
                text = unescape(text);
                var textInput = new Element('textarea', {'value':text}).setStyles({'width':280, 'height':50});
                textInput.inject(formElem);
                formElem.inject(msg);

                var okFunction = function(e) {
                    var result = true;
                    var text = textInput.value;
                    text = escape(text);
                    note._noteModel.setText(text);
                    return result;
                };

                formElem.addEvent('submit', function(e) {
                    $(okButtonId).fireEvent('click', e);
                    e = new Event(e);
                    e.stop();
                });


                var dialog = mindplot.Note.buildDialog(designer, okFunction, okButtonId);
                dialog.adopt(msg).show();

            }.bindWithEvent(this));
            buttonContainer.inject(container);
        }

        var note = this;
        image.addEventListener('mouseover', function(event) {
            var text = textModel.getText();
            text = unescape(text);
            text = text.replace(/\n/ig, "<br/>");
            text = text.replace(/<script/ig, "&lt;script");
            text = text.replace(/<\/script/ig, "&lt;\/script");
            this._textElem.innerHTML = text;

            bubbleTip.open(event, container, note);
        }.bind(this));
        image.addEventListener('mousemove', function(event) {
            bubbleTip.updatePosition(event);
        });
        image.addEventListener('mouseout', function(event) {
            bubbleTip.close(event);
        });
    },

    getText: function() {
        return this._text;
    },

    getModel : function() {
        return this._noteModel;
    },

    buildDialog : function(designer, okFunction, okButtonId) {
        var windoo = new Windoo({
            title: 'Write note',
            theme: Windoo.Themes.wise,
            modal:true,
            buttons:{'menu':false, 'close':false, 'minimize':false, 'roll':false, 'maximize':false},
            destroyOnClose:true,
            height:130
        });

        var cancel = new Element('input', {'type': 'button', 'class':'btn-primary', 'value': 'Cancel','class':'btn-primary'}).setStyle('margin-right', "5px");
        cancel.setStyle('margin-left', "5px");
        cancel.addEvent('click', function(event) {
            $(document).addEvent('keydown', designer.keyEventHandler.bindWithEvent(designer));
            windoo.close();
        }.bindWithEvent(this));

        var ok = new Element('input', {'type': 'button', 'class':'btn-primary', 'value': 'Ok','class':'btn-primary','id':okButtonId}).setStyle('marginRight', 10);
        ok.addEvent('click', function(event) {
            var couldBeUpdated = okFunction.attempt();
            if (couldBeUpdated) {
                $(document).addEvent('keydown', designer.keyEventHandler.bindWithEvent(designer));
                windoo.close();
            }
        }.bindWithEvent(this));

        var panel = new Element('div', {'styles': {'padding-top': 10, 'text-align': 'right'}}).adopt(ok, cancel);

        windoo.addPanel(panel);
        $(document).removeEvents('keydown');
        return windoo;
    }
});

mindplot.Note.IMAGE_URL = "../images/note.png";

/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.ActionIcon = new Class({
    Extends:mindplot.Icon,
    initialize: function(topic, url) {
        mindplot.Icon.call(this, url);
        this._node = topic;
    },
    getNode:function() {
        return this._node;
    },

    setPosition:function(x, y) {
        var size = this.getSize();
        this.getImage().setPosition(x - size.width / 2, y - size.height / 2);
    },

    addEventListener:function(event, fn) {
        this.getImage().addEventListener(event, fn);
    },

    addToGroup:function(group) {
        group.appendChild(this.getImage());
    },

    setVisibility:function(visible) {
        this.getImage().setVisibility(visible);
    },

    isVisible:function() {
        return this.getImage().isVisible();
    },

    setCursor:function(cursor) {
        return this.getImage().setCursor(cursor);
    },

    moveToBack:function(cursor) {
        return this.getImage().moveToBack(cursor);
    },

    moveToFront:function(cursor) {
        return this.getImage().moveToFront(cursor);
    }
});

/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.ImageIcon = new Class(
    {
        Extends:mindplot.Icon,
        initialize:function(iconModel, topic, designer) {
            $assert(iconModel, 'iconModel can not be null');
            $assert(topic, 'topic can not be null');
            $assert(designer, 'designer can not be null');
            this._topic = topic;
            this._iconModel = iconModel;
            this._designer = designer;

            // Build graph image representation ...
            var iconType = iconModel.getIconType();
            var imgUrl = this._getImageUrl(iconType);

            this.parent(imgUrl);

            //Remove
            var divContainer = designer.getWorkSpace().getScreenManager().getContainer();
            var tip = mindplot.Tip.getInstance(divContainer);

            var container = new Element('div');
            var removeImage = new Element('img');
            removeImage.src = "../images/bin.png";
            removeImage.inject(container);

            if (!$defined(designer._viewMode) || ($defined(designer._viewMode) && !designer._viewMode)) {

                removeImage.addEvent('click', function(event) {
                    var actionRunner = designer._actionRunner;
                    var command = new mindplot.commands.RemoveIconFromTopicCommand(this._topic.getId(), iconModel);
                    actionRunner.execute(command);
                    tip.forceClose();
                }.bindWithEvent(this));

                //Icon
                var image = this.getImage();
                image.addEventListener('click', function(event) {
                    var iconType = iconModel.getIconType();
                    var newIconType = this._getNextFamilyIconId(iconType);
                    iconModel.setIconType(newIconType);

                    var imgUrl = this._getImageUrl(newIconType);
                    this._image.setHref(imgUrl);

                    //        // @Todo: Support revert of change icon ...
                    //        var actionRunner = designer._actionRunner;
                    //        var command = new mindplot.commands.ChangeIconFromTopicCommand(this._topic.getId());
                    //        this._actionRunner.execute(command);


                }.bindWithEvent(this));

                var imageIcon = this;
                image.addEventListener('mouseover', function(event) {
                    tip.open(event, container, imageIcon);
                });
                image.addEventListener('mouseout', function(event) {
                    tip.close(event);
                });
                image.addEventListener('mousemove', function(event) {
                    tip.updatePosition(event);
                });

            }
        },

      _getImageUrl : function(iconId) {
            return "../icons/" + iconId + ".png";
        },

        getModel : function() {
            return this._iconModel;
        },

        _getNextFamilyIconId : function(iconId) {

            var familyIcons = this._getFamilyIcons(iconId);
            $assert(familyIcons != null, "Family Icon not found!");

            var result = null;
            for (var i = 0; i < familyIcons.length && result == null; i++) {
                if (familyIcons[i] == iconId) {
                    var nextIconId;
                    //Is last one?
                    if (i == (familyIcons.length - 1)) {
                        result = familyIcons[0];
                    } else {
                        result = familyIcons[i + 1];
                    }
                    break;
                }
            }

            return result;
        },

        _getFamilyIcons : function(iconId) {
            $assert(iconId != null, "id must not be null");
            $assert(iconId.indexOf("_") != -1, "Invalid icon id (it must contain '_')");

            var result = null;
            for (var i = 0; i < mindplot.ImageIcon.prototype.ICON_FAMILIES.length; i++) {
                var family = mindplot.ImageIcon.prototype.ICON_FAMILIES[i];
                var iconFamilyId = iconId.substr(0, iconId.indexOf("_"));

                if (family.id == iconFamilyId) {
                    result = family.icons;
                    break;
                }
            }
            return result;
        },

        getId : function() {
            return this._iconType;
        },

        getUiId : function() {
            return this._uiId;
        }
    }
);


mindplot.ImageIcon.prototype.ICON_FAMILIES = [
    {"id": "face", "icons" : ["face_plain","face_sad","face_crying","face_smile","face_surprise","face_wink"]},
    {"id": "funy", "icons" : ["funy_angel","funy_devilish","funy_glasses","funy_grin","funy_kiss","funy_monkey"]},
    {"id": "conn", "icons" : ["conn_connect","conn_disconnect"]},
    {"id": "sport", "icons" : ["sport_basketball","sport_football","sport_golf","sport_raquet","sport_shuttlecock","sport_soccer","sport_tennis"]},
    {"id": "bulb", "icons" : ["bulb_light_on","bulb_light_off"]},
    {"id": "thumb", "icons" : ["thumb_thumb_up","thumb_thumb_down"]},
    {"id": "tick", "icons" : ["tick_tick","tick_cross"]},
    {"id": "onoff", "icons" : ["onoff_clock","onoff_clock_red","onoff_add","onoff_delete","onoff_status_offline","onoff_status_online"]},
    {"id": "money", "icons" : ["money_money","money_dollar","money_euro","money_pound","money_yen","money_coins","money_ruby"]},
    {"id": "time", "icons" : ["time_calendar","time_clock","time_hourglass"]},
    {"id": "chart", "icons" : ["chart_bar","chart_line","chart_curve","chart_pie","chart_organisation"]},
    {"id": "sign", "icons" : ["sign_warning","sign_info","sign_stop","sign_help","sign_cancel"]},
    {"id": "hard", "icons" : ["hard_cd","hard_computer","hard_controller","hard_driver_disk","hard_ipod","hard_keyboard","hard_mouse","hard_printer"]},
    {"id": "soft", "icons" : ["soft_bug","soft_cursor","soft_database_table","soft_database","soft_feed","soft_folder_explore","soft_rss","soft_penguin"]},
    {"id": "arrow", "icons" : ["arrow_up","arrow_down","arrow_left","arrow_right"]},
    {"id": "arrowc", "icons" : ["arrowc_rotate_anticlockwise","arrowc_rotate_clockwise","arrowc_turn_left","arrowc_turn_right"]},
    {"id": "people", "icons" : ["people_group","people_male1","people_male2","people_female1","people_female2"]},
    {"id": "mail", "icons" : ["mail_envelop","mail_mailbox","mail_edit","mail_list"]},
    {"id": "flag", "icons" : ["flag_blue","flag_green","flag_orange","flag_pink","flag_purple","flag_yellow"]},
    {"id": "bullet", "icons" : ["bullet_black","bullet_blue","bullet_green","bullet_orange","bullet_red","bullet_pink","bullet_purple"]},
    {"id": "tag", "icons" : ["tag_blue","tag_green","tag_orange","tag_red","tag_pink","tag_yellow"]},
    {"id": "object", "icons" : ["object_bell","object_clanbomber","object_key","object_pencil","object_phone","object_magnifier","object_clip","object_music","object_star","object_wizard","object_house","object_cake","object_camera","object_palette","object_rainbow"]}
]



/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.model.IconModel = new Class({
    initialize:function(iconType, topic) {
        $assert(iconType, 'Icon id can not be null');
        $assert(topic, 'topic can not be null');

        this._iconType = iconType;
        this._id = mindplot.model.IconModel._nextUUID();
        this._topic = topic;
    },

    getId : function() {
        return this._id;
    },

    getIconType : function() {
        return this._iconType;
    },


    setIconType : function(iconType) {
        this._iconType = iconType;
    },

    getTopic : function() {
        return this._topic;
    },

    isIconModel : function() {
        return true;
    }});


/**
 * @todo: This method must be implemented.
 */
mindplot.model.IconModel._nextUUID = function() {
    if (!$defined(this._uuid)) {
        this._uuid = 0;
    }

    this._uuid = this._uuid + 1;
    return this._uuid;
};

/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.model.LinkModel = new Class({
    initialize : function(url, topic) {
        $assert(url, 'url can not be null');
        $assert(topic, 'mindmap can not be null');

        this._url = url;
        this._topic = topic;
    },

    getUrl : function() {
        return this._url;
    },

    setUrl : function(url) {
        $assert(url, 'url can not be null');
        this._url = url;
    },

    getTopic : function() {
        return this._topic;
    },

    isLinkModel : function() {
        return true;
    }
});/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.model.NoteModel = new Class({
    initialize : function(text, topic) {
        $assert(text != null, 'note text can not be null');
        $assert(topic, 'mindmap can not be null');
        this._text = text;
        this._topic = topic;
    },

    getText:function() {
            return this._text;
    },

    setText : function(text) {
        this._text = text;
    },

    getTopic : function() {
        return this._topic;
    },

    isNoteModel : function() {
        return true;
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.Command = new Class(
{
    initialize: function()
    {
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        throw "execute must be implemented.";
    },
    undoExecute: function(commandContext)
    {
        throw "undo must be implemented.";
    },
    getId:function()
    {
        return this._id;
    }
});

mindplot.Command._nextUUID = function()
{
    if (!$defined(mindplot.Command._uuid))
    {
        mindplot.Command._uuid = 1;
    }

    mindplot.Command._uuid = mindplot.Command._uuid + 1;
    return mindplot.Command._uuid;
};/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.DesignerActionRunner = new Class({
    initialize: function(designer) {
        this._designer = designer;
        this._undoManager = new mindplot.DesignerUndoManager();
        this._context = new mindplot.CommandContext(this._designer);
    },

    execute:function(command) {
        $assert(command, "command can not be null");
        // Execute action ...
        command.execute(this._context);

        // Enqueue it ...
        this._undoManager.enqueue(command);

        // Fire event
        var event = this._undoManager._buildEvent();
        this._designer._fireEvent("change", event);
    },

    undo: function() {
        this._undoManager.execUndo(this._context);

        // Fire event
        var event = this._undoManager._buildEvent();
        this._designer._fireEvent("change", event);
    },

    redo: function() {
        this._undoManager.execRedo(this._context);

        // Fire event
        var event = this._undoManager._buildEvent();
        this._designer._fireEvent("change", event);

    },

    markAsChangeBase: function() {
        return this._undoManager.markAsChangeBase();
    },
    hasBeenChanged: function() {
        return this._undoManager.hasBeenChanged();
    }
});

mindplot.CommandContext = new Class({
    initialize: function(designer) {
        this._designer = designer;
    },
    findTopics:function(topicsIds) {
        var designerTopics = this._designer._topics;
        if (!(topicsIds instanceof Array)) {
            topicsIds = [topicsIds];
        }

        var result = designerTopics.filter(function(topic) {
            var found = false;
            if (topic != null) {
                var topicId = topic.getId();
                found = topicsIds.contains(topicId);
            }
            return found;

        });
        return result;
    },
    deleteTopic:function(topic) {
        this._designer._removeNode(topic);
    },
    createTopic:function(model, isVisible) {
        $assert(model, "model can not be null");
        var topic = this._designer._nodeModelToNodeGraph(model, isVisible);

        return topic;
    },
    createModel:function() {
        var mindmap = this._designer.getMindmap();
        var model = mindmap.createNode(mindplot.model.NodeModel.MAIN_TOPIC_TYPE);
        return model;
    },
    connect:function(childTopic, parentTopic, isVisible) {
        childTopic.connectTo(parentTopic, this._designer._workspace, isVisible);
    } ,
    disconnect:function(topic) {
        topic.disconnect(this._designer._workspace);
    },
    createRelationship:function(model) {
        $assert(model, "model cannot be null");
        var relationship = this._designer.createRelationship(model);
        return relationship;
    },
    removeRelationship:function(model) {
        this._designer.removeRelationship(model);
    },
    findRelationships:function(lineIds) {
        var result = [];
        lineIds.forEach(function(lineId, index) {
            var line = this._designer._relationships[lineId];
            if ($defined(line)) {
                result.push(line);
            }
        }.bind(this));
        return result;
    },
    getSelectedRelationshipLines:function() {
        return this._designer.getSelectedRelationshipLines();
    }
});

mindplot.DesignerActionRunner.setInstance = function(actionRunner) {
    mindplot.DesignerActionRunner._instance = actionRunner;
};

mindplot.DesignerActionRunner.getInstance = function() {
    return mindplot.DesignerActionRunner._instance;
};
/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.DesignerUndoManager = new Class({
    initialize: function() {
        this._undoQueue = [];
        this._redoQueue = [];
        this._baseId = 0;
    },

    enqueue:function(command) {
        $assert(command, "Command can  not be null");
        var length = this._undoQueue.length;
        if (command.discartDuplicated && length > 0) {
            // Skip duplicated events ...
            var lastItem = this._undoQueue[length - 1];
            if (lastItem.discartDuplicated != command.discartDuplicated) {
                this._undoQueue.push(command);
            }
        } else {
            this._undoQueue.push(command);
        }
        this._redoQueue = [];
    },

    execUndo: function(commandContext) {
        if (this._undoQueue.length > 0) {
            var command = this._undoQueue.pop();
            this._redoQueue.push(command);

            command.undoExecute(commandContext);
        }
    },

    execRedo: function(commandContext) {
        if (this._redoQueue.length > 0) {
            var command = this._redoQueue.pop();
            this._undoQueue.push(command);
            command.execute(commandContext);
        }
    },

    _buildEvent: function() {
        return {undoSteps: this._undoQueue.length, redoSteps:this._redoQueue.length};
    },

    markAsChangeBase: function() {
        var undoLenght = this._undoQueue.length;
        if (undoLenght > 0) {
            var command = this._undoQueue[undoLenght - 1];
            this._baseId = command.getId();
        } else {
            this._baseId = 0;
        }
    },

    hasBeenChanged: function() {
        var result = true;
        var undoLenght = this._undoQueue.length;
        if (undoLenght == 0 && this._baseId == 0) {
            result = false;
        } else if (undoLenght > 0) {
            var command = this._undoQueue[undoLenght - 1];
            result = (this._baseId != command.getId());
        }
        return result;
    }
});/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.ControlPoint = new Class({
    initialize:function() {
        this._controlPointsController = [new web2d.Elipse({width:6, height:6, stroke:'1 solid #6589de',fillColor:'gray', visibility:false}),
            new web2d.Elipse({width:6, height:6, stroke:'1 solid #6589de',fillColor:'gray', visibility:false})];
        this._controlLines = [new web2d.Line({strokeColor:"#6589de", strokeWidth:1, opacity:0.3}),
            new web2d.Line({strokeColor:"#6589de", strokeWidth:1, opacity:0.3})];

        this._isBinded = false;
        this._controlPointsController[0].addEventListener('mousedown', this._mouseDown.bindWithEvent(this, mindplot.ControlPoint.FROM));
        this._controlPointsController[0].addEventListener('click', this._mouseClick.bindWithEvent(this));
        this._controlPointsController[0].addEventListener('dblclick', this._mouseClick.bindWithEvent(this));
        this._controlPointsController[1].addEventListener('mousedown', this._mouseDown.bindWithEvent(this, mindplot.ControlPoint.TO));
        this._controlPointsController[1].addEventListener('click', this._mouseClick.bindWithEvent(this));
        this._controlPointsController[1].addEventListener('dblclick', this._mouseClick.bindWithEvent(this));
    },


    setSide  : function(side) {
        this._side = side;
    },

    setLine  : function(line) {
        if ($defined(this._line)) {
            this._removeLine();
        }
        this._line = line;
        this._createControlPoint();
        this._endPoint = [];
        this._orignalCtrlPoint = [];
        this._orignalCtrlPoint[0] = this._controls[0].clone();
        this._orignalCtrlPoint[1] = this._controls[1].clone();
        this._endPoint[0] = this._line.getLine().getFrom().clone();
        this._endPoint[1] = this._line.getLine().getTo().clone();
    },

    redraw  : function() {
        if ($defined(this._line))
            this._createControlPoint();
    },

    _createControlPoint  : function() {
        this._controls = this._line.getLine().getControlPoints();
        var pos = this._line.getLine().getFrom();
        this._controlPointsController[0].setPosition(this._controls[mindplot.ControlPoint.FROM].x + pos.x, this._controls[mindplot.ControlPoint.FROM].y + pos.y - 3);
        this._controlLines[0].setFrom(pos.x, pos.y);
        this._controlLines[0].setTo(this._controls[mindplot.ControlPoint.FROM].x + pos.x + 3, this._controls[mindplot.ControlPoint.FROM].y + pos.y);
        pos = this._line.getLine().getTo();
        this._controlLines[1].setFrom(pos.x, pos.y);
        this._controlLines[1].setTo(this._controls[mindplot.ControlPoint.TO].x + pos.x + 3, this._controls[mindplot.ControlPoint.TO].y + pos.y);
        this._controlPointsController[1].setPosition(this._controls[mindplot.ControlPoint.TO].x + pos.x, this._controls[mindplot.ControlPoint.TO].y + pos.y - 3);

    },

    _removeLine  : function() {

    },

    _mouseDown  : function(event, point) {
        if (!this._isBinded) {
            this._isBinded = true;
            this._mouseMoveFunction = this._mouseMove.bindWithEvent(this, point);
            this._workspace.getScreenManager().addEventListener('mousemove', this._mouseMoveFunction);
            this._mouseUpFunction = this._mouseUp.bindWithEvent(this, point);
            this._workspace.getScreenManager().addEventListener('mouseup', this._mouseUpFunction);
        }
        event.preventDefault();
        event.stop();
        return false;
    },

    _mouseMove  : function(event, point) {
        var screen = this._workspace.getScreenManager();
        var pos = screen.getWorkspaceMousePosition(event);
        var topic = null;
        if (point == 0) {
            var cords = core.Utils.calculateRelationShipPointCoordinates(this._line.getSourceTopic(), pos);
            this._line.setFrom(cords.x, cords.y);
            this._line.setSrcControlPoint(new core.Point(pos.x - cords.x, pos.y - cords.y));
        } else {
            var cords = core.Utils.calculateRelationShipPointCoordinates(this._line.getTargetTopic(), pos);
            this._line.setTo(cords.x, cords.y);
            this._line.setDestControlPoint(new core.Point(pos.x - cords.x, pos.y - cords.y));
        }
        this._controls[point].x = (pos.x - cords.x);
        this._controls[point].y = (pos.y - cords.y);
        this._controlPointsController[point].setPosition(pos.x - 5, pos.y - 3);
        this._controlLines[point].setFrom(cords.x, cords.y);
        this._controlLines[point].setTo(pos.x - 2, pos.y);
        this._line.getLine().updateLine(point);
        /*event.preventDefault();
         event.stop();
         return false;*/
    },

    _mouseUp  : function(event, point) {
        this._workspace.getScreenManager().removeEventListener('mousemove', this._mouseMoveFunction);
        this._workspace.getScreenManager().removeEventListener('mouseup', this._mouseUpFunction);
        var command = new mindplot.commands.MoveControlPointCommand(this, point);
        designer._actionRunner.execute(command); //todo:Uggly!! designer is global!!
        this._isBinded = false;
        /*event.preventDefault();
         event.stop();
         return false;*/
    },

    _mouseClick  : function(event) {
        event.preventDefault();
        event.stop();
        return false;
    },

    setVisibility  : function(visible) {
        if (visible) {
            this._controlLines[0].moveToFront();
            this._controlLines[1].moveToFront();
            this._controlPointsController[0].moveToFront();
            this._controlPointsController[1].moveToFront();
        }
        this._controlPointsController[0].setVisibility(visible);
        this._controlPointsController[1].setVisibility(visible);
        this._controlLines[0].setVisibility(visible);
        this._controlLines[1].setVisibility(visible);
    },

    addToWorkspace  : function(workspace) {
        this._workspace = workspace;
        workspace.appendChild(this._controlPointsController[0]);
        workspace.appendChild(this._controlPointsController[1]);
        workspace.appendChild(this._controlLines[0]);
        workspace.appendChild(this._controlLines[1]);
    },

    removeFromWorkspace  : function(workspace) {
        this._workspace = null;
        workspace.removeChild(this._controlPointsController[0]);
        workspace.removeChild(this._controlPointsController[1]);
        workspace.removeChild(this._controlLines[0]);
        workspace.removeChild(this._controlLines[1]);
    },

    getControlPoint  : function(index) {
        return this._controls[index];
    },

    getOriginalEndPoint  : function(index) {
        return this._endPoint[index];
    },

    getOriginalCtrlPoint  : function(index) {
        return this._orignalCtrlPoint[index];
    }
});

mindplot.ControlPoint.FROM = 0;
mindplot.ControlPoint.TO = 1;
mindplot.EditorOptions =
{
    LayoutManager:"OriginalLayout",
//    LayoutManager:"FreeMindLayout",
    textEditor:"TextEditor"
//    textEditor:"RichTextEditor"
};
/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.commands.GenericFunctionCommand =new Class(
{
    Extends:mindplot.Command,
   initialize: function(commandFunc,value,topicsIds)
    {
        $assert(commandFunc, "commandFunc must be defined");
        $assert(topicsIds, "topicsIds must be defined");
        this._value = value;
        this._selectedObjectsIds = topicsIds;
        this._commandFunc = commandFunc;
        this._oldValues = [];
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        if (!this.applied)
        {
            var topics = commandContext.findTopics(this._selectedObjectsIds);
            topics.forEach(function(topic)
            {
                var oldValue = this._commandFunc(topic, this._value);
                this._oldValues.push(oldValue);
            }.bind(this));
            this.applied = true;
        } else
        {
            throw "Command can not be applied two times in a row.";
        }

    },
    undoExecute: function(commandContext)
    {
       if (this.applied)
        {
            var topics = commandContext.findTopics(this._selectedObjectsIds);
            topics.forEach(function(topic,index)
            {
                this._commandFunc(topic, this._oldValues[index]);

            }.bind(this));

            this.applied = false;
            this._oldValues = [];
        } else
        {
            throw "undo can not be applied.";
        }
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.commands.DeleteTopicCommand = new Class(
{
    Extends:mindplot.Command,
    initialize: function(topicsIds)
    {
        $assert(topicsIds, "topicsIds must be defined");
        this._selectedObjectsIds = topicsIds;
        this._deletedTopicModels = [];
        this._parentTopicIds = [];
        this._deletedRelationships = [];
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        var topics = commandContext.findTopics(this._selectedObjectsIds.nodes);
        if(topics.length>0){
        topics.forEach(
                function(topic, index)
                {
                    var model = topic.getModel().clone();

                     //delete relationships
                    var relationships = topic.getRelationships();
                    while(relationships.length>0){
                        var relationship = relationships[0];
                        this._deletedRelationships.push(relationship.getModel().clone());
                        commandContext.removeRelationship(relationship.getModel());
                    }

                    this._deletedTopicModels.push(model);

                    // Is connected?.
                    var outTopic = topic.getOutgoingConnectedTopic();
                    var outTopicId = null;
                    if (outTopic != null)
                    {
                        outTopicId = outTopic.getId();
                    }
                    this._parentTopicIds.push(outTopicId);

                    // Finally, delete the topic from the workspace...
                    commandContext.deleteTopic(topic);

                }.bind(this)
                ); }
        var lines = commandContext.findRelationships(this._selectedObjectsIds.relationshipLines);
        if(lines.length>0){
            lines.forEach(function(line,index){
                if(line.isInWorkspace()){
                    this._deletedRelationships.push(line.getModel().clone());
                        commandContext.removeRelationship(line.getModel());
                }
            }.bind(this));
        }
    },
    undoExecute: function(commandContext)
    {

        var topics = commandContext.findTopics(this._selectedObjectsIds);
        var parent = commandContext.findTopics(this._parentTopicIds);

        this._deletedTopicModels.forEach(
                function(model, index)
                {
                    var topic = commandContext.createTopic(model);

                    // Was the topic connected?
                    var parentTopic = parent[index];
                    if (parentTopic != null)
                    {
                        commandContext.connect(topic, parentTopic);
                    }

                }.bind(this)
                );
        this._deletedRelationships.forEach(
            function(relationship, index){
            commandContext.createRelationship(relationship);
        }.bind(this));

        this._deletedTopicModels = [];
        this._parentTopicIds = [];
        this._deletedRelationships = [];
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.commands.DragTopicCommand = new Class(
{
    Extends:mindplot.Command,
    initialize: function(topicId)
    {
        $assert(topicId, "topicId must be defined");
        this._selectedObjectsIds = topicId;
        this._parentTopic = null;
        this._position = null;
        this._order = null;
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {

        var topic = commandContext.findTopics([this._selectedObjectsIds])[0];

        // Save old position ...
        var origParentTopic = topic.getOutgoingConnectedTopic();
        var origOrder = null;
        var origPosition = null;
//        if (topic.getType() == mindplot.model.NodeModel.MAIN_TOPIC_TYPE && origParentTopic != null && origParentTopic.getType() == mindplot.model.NodeModel.MAIN_TOPIC_TYPE)
//        {
            // In this case, topics are positioned using order ...
            origOrder = topic.getOrder();
//        } else
//        {
            origPosition = topic.getPosition().clone();
//        }

        // Disconnect topic ..
        if ($defined(origParentTopic))
        {
            commandContext.disconnect(topic);
        }


        // Set topic order ...
        if (this._order != null)
        {
            topic.setOrder(this._order);
        } else if (this._position != null)
        {
            // Set position ...
            topic.setPosition(this._position);

        } else
        {
            $assert("Illegal commnad state exception.");
        }
        this._order = origOrder;
        this._position = origPosition;

        // Finally, connect topic ...
        if ($defined(this._parentId))
        {
            var parentTopic = commandContext.findTopics([this._parentId])[0];
            commandContext.connect(topic, parentTopic);
        }

        // Backup old parent id ...
        this._parentId = null;
        if ($defined(origParentTopic))
        {
            this._parentId = origParentTopic.getId();
        }

    },
    undoExecute: function(commandContext)
    {
        this.execute(commandContext);
        var selectedRelationships = commandContext.getSelectedRelationshipLines();
        selectedRelationships.forEach(function(relationshipLine,index){
            relationshipLine.redraw();
        });

    },
    setPosition: function(point)
    {
        this._position = point;
    },
    setParetTopic: function(topic) {
        this._parentId = topic.getId();

    },
    setOrder: function(order)
    {
        this._order = order
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.commands.AddTopicCommand = new Class(
{
    Extends:mindplot.Command,
    initialize: function(model, parentTopicId, animated)
    {
        $assert(model, 'Model can not be null');
        this._model = model;
        this._parentId = parentTopicId;
        this._id = mindplot.Command._nextUUID();
        this._animated = $defined(animated)?animated:false;
    },
    execute: function(commandContext)
    {
        // Add a new topic ...

        var topic = commandContext.createTopic(this._model, !this._animated);

        // Connect to topic ...
        if ($defined(this._parentId))
        {
            var parentTopic = commandContext.findTopics(this._parentId)[0];
            commandContext.connect(topic, parentTopic, !this._animated);
        }

        var doneFn = function(){
            // Finally, focus ...
            var designer = commandContext._designer;
            designer.onObjectFocusEvent.attempt(topic, designer);
            topic.setOnFocus(true);
        };
        
        if(this._animated){
            core.Utils.setVisibilityAnimated([topic,topic.getOutgoingLine()],true,doneFn);
        } else
            doneFn.attempt();
    },
    undoExecute: function(commandContext)
    {
        // Finally, delete the topic from the workspace ...
        var topicId = this._model.getId();
        var topic = commandContext.findTopics(topicId)[0];
        var doneFn = function(){
            commandContext.deleteTopic(topic);
        };
        if(this._animated){
            core.Utils.setVisibilityAnimated([topic,topic.getOutgoingLine()],false, doneFn);
        }
        else
            doneFn.attempt();
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.commands.AddLinkToTopicCommand =new Class(
{
    Extends:mindplot.Command,
    initialize: function(topicId,url)
    {
        $assert(topicId, 'topicId can not be null');
        this._selectedObjectsIds = topicId;
        this._url = url;
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        var topic = commandContext.findTopics(this._selectedObjectsIds)[0];
        var updated = function() {
            topic.addLink(this._url,commandContext._designer);
            topic.updateNode();
        }.bind(this);
        updated.delay(0);
    },
    undoExecute: function(commandContext)
    {
        var topic = commandContext.findTopics(this._selectedObjectsIds)[0];
        var updated = function() {
            topic.removeLink();
        }.bind(this);
        updated.delay(0);
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.commands.RemoveLinkFromTopicCommand =new Class(
{
    Extends:mindplot.Command,
    initialize: function(topicId)
    {
        $assert(topicId, 'topicId can not be null');
        this._selectedObjectsIds = topicId;
    },
    execute: function(commandContext)
    {
        var topic = commandContext.findTopics(this._selectedObjectsIds)[0];
        this._url = topic._link.getUrl();
        var updated = function() {
            topic.removeLink();
        }.bind(this);
        updated.delay(0);
    },
    undoExecute: function(commandContext)
    {
        var topic = commandContext.findTopics(this._selectedObjectsIds)[0];
        var updated = function() {
            topic.addLink(this._url,commandContext._designer);
            topic.updateNode();
        }.bind(this);
        updated.delay(0);
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.commands.AddIconToTopicCommand = new Class(
{
    Extends:mindplot.Command,
    initialize: function(topicId, iconType)
    {
        $assert(topicId, 'topicId can not be null');
        $assert(iconType, 'iconType can not be null');
        this._selectedObjectsIds = topicId;
        this._iconType = iconType;
    },
    execute: function(commandContext)
    {
        var topic = commandContext.findTopics(this._selectedObjectsIds)[0];
        var updated = function() {
            var iconImg = topic.addIcon(this._iconType, commandContext._designer);
            this._iconModel = iconImg.getModel();
            topic.updateNode();
        }.bind(this);
        updated.delay(0);
    },
    undoExecute: function(commandContext)
    {
        var topic = commandContext.findTopics(this._selectedObjectsIds)[0];
        var updated = function() {
            topic.removeIcon(this._iconModel);
            topic.updateNode();
        }.bind(this);
        updated.delay(0);
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.commands.RemoveIconFromTopicCommand = new Class(
{
    Extends:mindplot.Command,
    initialize: function(topicId, iconModel)
    {
        $assert(topicId, 'topicId can not be null');
        $assert(iconModel, 'iconId can not be null');
        this._selectedObjectsIds = topicId;
        this._iconModel = iconModel;
    },
    execute: function(commandContext)
    {
        var topic = commandContext.findTopics(this._selectedObjectsIds)[0];
        var updated = function() {
            topic.removeIcon(this._iconModel);
            topic.updateNode();
        }.bind(this);
        updated.delay(0);
    },
    undoExecute: function(commandContext)
    {
        var topic = commandContext.findTopics(this._selectedObjectsIds)[0];
        var updated = function() {
            var iconType = this._iconModel.getIconType();
            var iconImg = topic.addIcon(iconType, commandContext._designer);
            this._iconModel = iconImg.getModel();
            topic.updateNode();
        }.bind(this);
        updated.delay(0);
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.commands.AddNoteToTopicCommand = new Class(
{
    Extends:mindplot.Command,
    initialize: function(topicId,text)
    {
        $assert(topicId, 'topicId can not be null');
        this._selectedObjectsIds = topicId;
        this._text = text;
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        var topic = commandContext.findTopics(this._selectedObjectsIds)[0];
        var updated = function() {
            topic.addNote(this._text,commandContext._designer);
            topic.updateNode();
        }.bind(this);
        updated.delay(0);
    },
    undoExecute: function(commandContext)
    {
        var topic = commandContext.findTopics(this._selectedObjectsIds)[0];
        var updated = function() {
            topic.removeNote();
        }.bind(this);
        updated.delay(0);
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

mindplot.commands.RemoveNoteFromTopicCommand = new Class(
{
    Extends:mindplot.Command,
    initialize: function(topicId)
    {
        $assert(topicId, 'topicId can not be null');
        this._selectedObjectsIds = topicId;
    },
    execute: function(commandContext)
    {
        var topic = commandContext.findTopics(this._selectedObjectsIds)[0];
        this._text = topic._note.getText();
        var updated = function() {
            topic.removeNote();
        }.bind(this);
        updated.delay(0);
    },
    undoExecute: function(commandContext)
    {
        var topic = commandContext.findTopics(this._selectedObjectsIds)[0];
        var updated = function() {
            topic.addNote(this._text,commandContext._designer);
            topic.updateNode();
        }.bind(this);
        updated.delay(0);
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
mindplot.commands.AddRelationshipCommand = new Class(
{
    Extends:mindplot.Command,
    initialize: function(model, mindmap)
    {
        $assert(model, 'Relationship model can not be null');
        this._model = model;
        this._mindmap = mindmap;
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        var relationship = commandContext.createRelationship(this._model);
        // Finally, focus ...
        var designer = commandContext._designer;
        designer.onObjectFocusEvent.attempt(relationship, designer);
        relationship.setOnFocus(true);
    },
    undoExecute: function(commandContext)
    {
        var relationship = commandContext.removeRelationship(this._model);
        this._mindmap.removeRelationship(this._model);
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
mindplot.commands.MoveControlPointCommand = new Class(
{
    Extends:mindplot.Command,
    initialize: function(ctrlPointController, point)
    {
        $assert(ctrlPointController, 'line can not be null');
        this._ctrlPointControler = ctrlPointController;
        this._line = ctrlPointController._line;
        var model = this._line.getModel();
        this._controlPoint = this._ctrlPointControler.getControlPoint(point).clone();
        this._oldControlPoint= this._ctrlPointControler.getOriginalCtrlPoint(point).clone();
        this._originalEndPoint = this._ctrlPointControler.getOriginalEndPoint(point).clone();
        switch (point){
            case 0:
                this._wasCustom = this._line.getLine().isSrcControlPointCustom();
                this._endPoint = this._line.getLine().getFrom().clone();
                break;
            case 1:
                this._wasCustom = this._line.getLine().isDestControlPointCustom();
                this._endPoint = this._line.getLine().getTo().clone();
                break;
        }
        this._id = mindplot.Command._nextUUID();
        this._point = point;
    },
    execute: function(commandContext)
    {
        var model = this._line.getModel();
        switch (this._point){
            case 0:
                model.setSrcCtrlPoint(this._controlPoint.clone());
                this._line.setFrom(this._endPoint.x, this._endPoint.y);
                this._line.setIsSrcControlPointCustom(true);
                this._line.setSrcControlPoint(this._controlPoint.clone());
                break;
            case 1:
                model.setDestCtrlPoint(this._controlPoint.clone());
                this._wasCustom = this._line.getLine().isDestControlPointCustom();
                this._line.setTo(this._endPoint.x, this._endPoint.y);
                this._line.setIsDestControlPointCustom(true);
                this._line.setDestControlPoint(this._controlPoint.clone());
                break;
        }
        if(this._line.isOnFocus()){
            this._line._refreshSelectedShape();
            this._ctrlPointControler.setLine(this._line);
        }
        this._line.getLine().updateLine(this._point);
    },
    undoExecute: function(commandContext)
    {
        var line = this._line;
        var model = line.getModel();
        switch (this._point){
            case 0:
                if($defined(this._oldControlPoint)){
                    line.setFrom(this._originalEndPoint.x, this._originalEndPoint.y);
                    model.setSrcCtrlPoint(this._oldControlPoint.clone());
                    line.setSrcControlPoint(this._oldControlPoint.clone());
                    line.setIsSrcControlPointCustom(this._wasCustom);
                }
            break;
            case 1:
                if($defined(this._oldControlPoint)){
                    line.setTo(this._originalEndPoint.x, this._originalEndPoint.y);
                    model.setDestCtrlPoint(this._oldControlPoint.clone());
                    line.setDestControlPoint(this._oldControlPoint.clone());
                    line.setIsDestControlPointCustom(this._wasCustom);
                }
            break;
        }
        this._line.getLine().updateLine(this._point);
        if(this._line.isOnFocus()){
            this._ctrlPointControler.setLine(line);
            line._refreshSelectedShape();
        }
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
mindplot.commands.freeMind={};

mindplot.commands.freeMind.DragTopicCommand = mindplot.Command.extend(
{
    initialize: function()
    {
        this._modifiedTopics=null;
        this._id = mindplot.Command._nextUUID();
    },
    execute: function(commandContext)
    {
        var keys = this._modifiedTopics.keys();
        for(var i=0; i<keys.length; i++){
            var id = keys[i];
            var modTopic = this._modifiedTopics.get(id);
            var topic = commandContext.findTopics(parseInt(id))[0];

            var position = topic.getPosition();
            var pos = modTopic.newPos;
            if(position.x != pos.x || position.y  != pos.y){
                topic.setPosition(pos.clone(), true);
            }
        }
    },
    undoExecute: function(commandContext)
    {
        var keys = this._modifiedTopics.keys();
        for(var i=0; i<keys.length; i++){
            var id = keys[i];
            var modTopic = this._modifiedTopics.get(id);
            var topic = commandContext.findTopics(parseInt(id))[0];

            var position = topic.getPosition();
            var pos = modTopic.originalPos;
            if(position.x != pos.x || position.y  != pos.y){
                topic.setPosition(pos.clone(), true);
            }
        }
    },
    setModifiedTopics:function(modifiedTopics){
        this._modifiedTopics = modifiedTopics;
    }
});/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
mindplot.commands.freeMind.ReconnectTopicCommand = mindplot.Command.extend(
{
    initialize: function()
    {
        this._modifiedTopics=null;
        this._id = mindplot.Command._nextUUID();
        this._node = null;
        this._targetNode = null;
        this._relationship = null;
        this._oldParent = null;
    },
    execute: function(commandContext)
    {
        var node = commandContext.findTopics(parseInt(this._node))[0];
        var targetNode = commandContext.findTopics(parseInt(this._targetNode))[0];
        var keys = this._modifiedTopics.keys();
        for(var i=0; i<keys.length; i++){
            var id = keys[i];
            var modTopic = this._modifiedTopics.get(id);
            var topic = commandContext.findTopics(parseInt(id))[0];

            var position = topic.getPosition();
            var pos = modTopic.newPos;
            if(position.x != pos.x || position.y  != pos.y){
                topic.setPosition(pos.clone(), true);
            }
            if(id = this._node){
                node._originalPosition = modTopic.originalPos;
            }
        }
        var oldParent = this._oldParent!=null?commandContext.findTopics(parseInt(this._oldParent))[0]:null;
        node.relationship = this._relationship;
        node._relationship_oldParent = oldParent;
        node._relationship_index = this._index;
        commandContext.disconnect(node);
        var parentNode = targetNode;
        if(this._relationship != "Child"){
            parentNode = targetNode.getParent();
            node._relationship_sibling_node = targetNode;
        }
        commandContext.connect(node, parentNode);
        delete node.relationship;
        delete node._relationship_oldParent;
        delete node._relationship_sibling_node;
        delete node._relationship_index;
        delete node._originalPosition;
    },
    undoExecute: function(commandContext)
    {
        var node = commandContext.findTopics(parseInt(this._node))[0];
        var targetNode = this._oldParent!=null?commandContext.findTopics(parseInt(this._oldParent))[0]:null;

        var keys = this._modifiedTopics.keys();
        for(var i=0; i<keys.length; i++){
            var id = keys[i];
            var modTopic = this._modifiedTopics.get(id);
            var topic = commandContext.findTopics(parseInt(id))[0];

            var position = topic.getPosition();
            var pos = modTopic.originalPos;
            if(position.x != pos.x || position.y  != pos.y){
                topic.setPosition(pos.clone(), true);
            }
            if(id = this._node){
                node._originalPosition = modTopic.newPos;
            }
        }
        var oldParent = commandContext.findTopics(parseInt(this._targetNode))[0];
        if(this._relationship != "Child"){
            oldParent = oldParent.getParent();
        }
        if(targetNode!=null){
            node.relationship = "undo";
            node._relationship_oldParent = oldParent;
            node._relationship_index = this._index;
        }
        commandContext.disconnect(node);
        if(targetNode!=null){
            commandContext.connect(node, targetNode);
            delete node.relationship;
            delete node._relationship_oldParent;
            delete node._relationship_index;
        }
        delete node._originalPosition;
    },
    setModifiedTopics:function(modifiedTopics){
        this._modifiedTopics = modifiedTopics;
    },
    setDraggedTopic:function(node, index){
        this._node = node.getId();
        var outgoingConnectedTopic = node.getOutgoingConnectedTopic();
        this._oldParent = outgoingConnectedTopic!=null?outgoingConnectedTopic.getId():null;
        this._index = index;
    },
    setTargetNode:function(node){
        this._targetNode = node.getId();
    },
    setAs:function(relationship){
        this._relationship = relationship;
    }
});mindplot.layout.boards = {};

mindplot.layout.boards.Board = new Class({

    options: {

    },
    initialize: function(node, layoutManager, options) {
        this.setOptions(options);
        this._node = node;
        this._layoutManager = layoutManager;
    },
    getClassName:function() {
        return mindplot.layout.boards.Board.NAME;
    },
    removeTopicFromBoard:function(node, modifiedTopics) {
        $assert(false, "no Board implementation found!");
    },
    addBranch:function(node, modifiedTopics) {
        $assert(false, "no Board implementation found!");
    },
    updateChildrenPosition:function(node, modifiedTopics) {
        $assert(false, "no Board implementation found!");
    },
    setNodeMarginTop:function(node, delta) {
        $assert(false, "no Board implementation found!");
    },
    getNode:function() {
        return this._node;
    }
});

mindplot.layout.boards.Board.NAME = "Board";

mindplot.layout.boards.Board.implement(new Events);
mindplot.layout.boards.Board.implement(new Options);mindplot.layout.boards.freemind = {};

mindplot.layout.boards.freemind.Board = mindplot.layout.boards.Board.extend({
    options:{

    },
    initialize:function(node, layoutManager, options){
        this.parent(node, layoutManager, options);
        this._positionTables = this._createTables();
    },
    _createTables:function(){
        $assert(false, "no Board implementation found!")
    },
    _getTableForNode:function(node, position){
        $assert(false, "no Board implementation found!")
    },
    removeTopicFromBoard:function(node, modifiedTopics){
        var pos;
        if($defined(node._originalPosition))
            pos = node._originalPosition;
        var result = this.findNodeEntryIndex(node, pos);
        $assert(result.index<result.table.length,"node not found. Could not remove");
        this._removeEntry(node, result.table, result.index, modifiedTopics);
    },
    addBranch:function(node,modifiedTopics){
        var pos = (this._layoutManager._isMovingNode?node.getPosition():node.getModel().getFinalPosition() || node.getPosition());
        var entry = new mindplot.layout.boards.freemind.Entry(node, !this._layoutManager._isMovingNode);
        var result = this.findNewNodeEntryIndex(entry);

        // if creating a sibling or child
        if(!this._layoutManager._isMovingNode && this._layoutManager.getDesigner().getSelectedNodes().length>0){
            var selectedNode = this._layoutManager.getDesigner().getSelectedNodes()[0];
            if(!$defined(pos)){
                if(selectedNode.getParent()!= null && node.getParent().getId() == selectedNode.getParent().getId()){
                    //creating a sibling - Lets put the new node below the selected node.
                    var parentBoard = this._layoutManager.getTopicBoardForTopic(selectedNode.getParent());
                    var selectedNodeResult = parentBoard.findNodeEntryIndex(selectedNode);
                    var selectedNodeEntry = selectedNodeResult.table[selectedNodeResult.index];
                    var x = null;
                    if(this._layoutManager._isCentralTopic(selectedNode.getParent())){
                        var nodeX = entry.getNode().getPosition().x;
                        if(Math.sign(nodeX)!=Math.sign(selectedNode.getPosition().x)){
                            x =nodeX *-1;
                        }
                        result.table = selectedNodeResult.table;
                    }
                    entry.setPosition(x, selectedNodeEntry.getPosition()+selectedNodeEntry.getTotalMarginBottom() + entry.getMarginTop());
                    result.index = selectedNodeResult.index+1;
                } else if(node.getParent().getId() == selectedNode.getId()){
                    //creating a child node - Lest put the new node as the last child.
                    var selectedNodeBoard = this._layoutManager.getTopicBoardForTopic(selectedNode);
                    var table = selectedNodeBoard._getTableForNode(node);
                    if(table.length>0){
                        //if no children use the position set by Entry initializer. Otherwise place as last child
                        var lastChild = table[table.length-1];
                        entry.setPosition(null, lastChild.getPosition()+lastChild.getTotalMarginBottom() + entry.getMarginTop());
                    }
                    result.index = table.length;
                }
            }
        }
        this._addEntry(entry, result.table, result.index);
        if($defined(pos)){
            if(result.index>0){
                var prevEntry =result.table[result.index-1];
                entry.setMarginTop(pos.y-(prevEntry.getPosition() + prevEntry.getTotalMarginBottom()));
            }
            else if(result.table.length>1){
                var nextEntry = result.table[1];
                nextEntry.setMarginTop((nextEntry.getPosition() - nextEntry.getTotalMarginTop())-pos.y);
            }
        }
        this._updateTable(result.index, result.table,modifiedTopics, false);
        this._layoutManager._updateParentBoard(node, modifiedTopics);
    },
    _removeEntry:function(node, table, index, modifiedTopics){
        table.splice(index, 1);
        this._updateTable(index>0?index-1:index, table, modifiedTopics, false);
    },
    _addEntry:function(entry, table, index){
        table.splice(index, 0, entry);
    },
    _updateTable:function(index, table, modifiedTopics, updateParents){
        var i = index;
        if(index >= table.length){
            i = table.length -1;
        }
        var delta = null;
        //check from index to 0;
        if(i>0){
            var entry = table[i];
            var prevEntry = table[i-1];

            var margin = entry.getTotalMarginTop() + prevEntry.getTotalMarginBottom();
            var distance = Math.abs(prevEntry.getPosition() - entry.getPosition());
            if(distance!=margin){
                delta = (distance - margin)*Math.sign(prevEntry.getPosition() - entry.getPosition());
                i--;
                while(i >= 0){
                    this._updateEntryPos(table[i], new core.Point(null, delta), modifiedTopics, updateParents);
                    i--;
                }
            }
        }

        i = index;
        delta = null;

        //check from index to length
        if( i<table.length-1){
            entry = table[i];
            var nextEntry = table[i+1];
            var margin = entry.getTotalMarginBottom() + nextEntry.getTotalMarginTop();
            var distance = Math.abs(entry.getPosition() - nextEntry.getPosition());
            if(distance!=margin){
                delta = (distance - margin)*Math.sign(nextEntry.getPosition() - entry.getPosition());
                i++;
                while(i<table.length){
                    this._updateEntryPos(table[i], new core.Point(null, delta), modifiedTopics, updateParents);
                    i++;
                }
            }
        }

    },
    updateChildrenPosition:function(node, modifiedTopics){
        var result = this.findNodeEntryIndex(node);
        this._updateTable(result.index, result.table, modifiedTopics, false);
    },
    findNodeEntryIndex:function(node, position){
        var table = this._getTableForNode(node, position);

        //search for position
        var i;
        for(i = 0; i< table.length ; i++){
            var entry = table[i];
            if (entry.getNode().getId() == node.getId()){
                break;
            }
        }
        return {index:i, table:table};
    },
    findNewNodeEntryIndex:function(entry){
        var table = this._getTableForNode(entry.getNode());
        var position = entry.getPosition();
        //search for position
        var i;
        for(i = 0; i< table.length ; i++){
            var tableEntry = table[i];
            if (tableEntry.getPosition() > position){
                break;
            }
        }
        return {index:i, table:table};
    },
    setNodeMarginTop:function(entry, delta){
        var marginTop = entry.getMarginTop()-delta.y;
        entry.setMarginTop(marginTop);
    },
    setNodeMarginBottom:function(entry, delta){
        var marginBottom = entry.getMarginBottom()-delta.y;
        entry.setMarginBottom(marginBottom);
    },
    setNodeChildrenMarginTop:function(entry, delta){
        entry.setChildrenMarginTop(delta);
    },
    setNodeChildrenMarginBottom:function(entry, delta){
        entry.setChildrenMarginBottom(delta);
    },
    updateEntry:function(node, delta, modifiedTopics){
        var result = this.findNodeEntryIndex(node);
        if(result.index < result.table.length){
            var entry = result.table[result.index];
            if(result.index!=0)
                this.setNodeMarginTop(entry, delta);
            this._updateEntryPos(entry, delta, modifiedTopics, false);
            this._updateTable(result.index, result.table, modifiedTopics, false);
            this._layoutManager._updateParentBoard(entry.getNode(), modifiedTopics);
        }
    },
    _updateEntryPos:function(entry, delta, modifiedTopics, updateParents){
        var pos = entry.getNode().getPosition().clone();
        var newPos = new core.Point(pos.x-(delta.x==null?0:delta.x), pos.y-delta.y);
        entry.setPosition(newPos.x, newPos.y);
        this._layoutManager._updateChildrenBoards(entry.getNode(), delta, modifiedTopics);
        if($defined(modifiedTopics.set)){
            var key = entry.getId();
            if(modifiedTopics.has(key)){
                pos = modifiedTopics.get(key).originalPos;
            }
            modifiedTopics.set(key,{originalPos:pos, newPos:newPos});
        }
    }
});mindplot.layout.boards.freemind.Entry = new Class({
    initialize:function(node, useFinalPosition){
        this._node = node;
        this._DEFAULT_X_GAP = 30;
        var pos = node.getModel().getFinalPosition();
        if(useFinalPosition && $defined(pos)){
            this.setPosition(pos.x, pos.y);
        }
        else{
            pos = node.getPosition();
            if(!$defined(pos)){
                var parent = node.getParent();
                pos = parent.getPosition().clone();
                var pwidth = parent.getSize().width;
                var width = node.getSize().width;
                pos.x = pos.x + Math.sign(pos.x) * (this._DEFAULT_X_GAP + pwidth/2 + width/2);
                node.setPosition(pos, false);

            }
        }
        this._DEFAULT_GAP = 10;
        var height = this.getNode().getSize().height;
        this._minimalMargin = this._DEFAULT_GAP + height/2;
        this._marginTop = this._minimalMargin;
        this._marginBottom = this._minimalMargin;
        this._marginTopChildren=0;
        this._marginBottomChildren=0;
    },
    getNode:function(){
        return this._node;
    },
    getId:function(){
        return this.getNode().getId();
    },
    getPosition:function(){
        return this._node.getPosition().y;
    },
    setPosition:function(x,y){
        var position = this._node.getPosition().clone();
        position.y = y;
        if(null != x){
            position.x = x;
        }

        this._node.setPosition(position, false);
    },
    getMarginTop:function(){
        return this._marginTop;
    },
    setMarginTop:function(value){
        if(value >= this._minimalMargin){
            this._marginTop = value;
        }
    },
    setMarginBottom:function(value){
        if(value >= this._minimalMargin){
            this._marginBottom = value;
        }
    },
    getMarginBottom:function(){
        return this._marginBottom;
    },
    getChildrenMarginTop:function(){
        return this._marginTopChildren;
    },
    setChildrenMarginTop:function(value){
        if(value >= this._minimalMargin){
            this._marginTopChildren = value - this._minimalMargin;
        }else{
            this._marginTopChildren=0;
        }
    },
    setChildrenMarginBottom:function(value){
        if(value >= this._minimalMargin){
            this._marginBottomChildren = value - this._minimalMargin;
        }else{
            this._marginBottomChildren=0;
        }
    },
    getChildrenMarginBottom:function(){
        return this._marginBottomChildren;
    },
    getTotalMarginTop:function(){
        return (this._node.areChildrenShrinked()?0:this._marginTopChildren)+this._marginTop;
    },
    getTotalMarginBottom:function(){
        return (this._node.areChildrenShrinked()?0:this._marginBottomChildren) + this._marginBottom;
    }
});mindplot.layout.boards.freemind.CentralTopicBoard = mindplot.layout.boards.freemind.Board.extend({
    options:{

    },
    initialize:function(node, layoutManager, options){
        this.parent(node, layoutManager, options);
    },
    _createTables:function(){
        return [[],[]];
    },
    _getTableForNode:function(node, altPosition){
        var i = 0;
        var position = node.getPosition();
        if(typeof altPosition != "undefined" && altPosition!=null)
        {
            position = altPosition;
        }
        if(!$defined(position)){
            if(Math.sign(node.getParent().getPosition().x) == -1){
                i=1;
            }
        }
        else if(Math.sign(position.x)==-1)
            i=1;
        return this._positionTables[i];
    }
});mindplot.layout.boards.freemind.MainTopicBoard = mindplot.layout.boards.freemind.Board.extend({
    options:{

    },
    initialize:function(node, layoutManager, options){
        this.parent(node, layoutManager, options);        
    },
    _createTables:function(){
        return [[]];
    },
    _getTableForNode:function(node){
        return this._positionTables[0];
    }
});mindplot.layout.BaseLayoutManager = new Class({

    options: {

    },

    initialize: function(designer, options) {
        this.setOptions(options);
        this._createBoard();
        this._designer = designer;
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeResizeEvent,this._nodeResizeEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeMoveEvent,this._nodeMoveEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeDisconnectEvent,this._nodeDisconnectEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeConnectEvent,this._nodeConnectEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeRepositionateEvent,this._nodeRepositionateEvent.bind(this));
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeShrinkEvent,this._nodeShrinkEvent.bind(this));
    },
    _nodeResizeEvent:function(node){
    },
    _nodeMoveEvent:function(node){
        var modifiedTopics = [];
        this.getTopicBoardForTopic(node).updateChildrenPosition(node, modifiedTopics);
    },
    _nodeDisconnectEvent:function(targetNode, node){
        var modifiedTopics = [];
        this.getTopicBoardForTopic(targetNode).removeTopicFromBoard(node,modifiedTopics);
    },
    _nodeConnectEvent:function(targetNode, node){
        var modifiedTopics = [];
        this.getTopicBoardForTopic(targetNode).addBranch(node,modifiedTopics);
    },
    _nodeRepositionateEvent:function(node){
    },
    _nodeShrinkEvent:function(node){
    },
    _createBoard:function(){
        this._boards = new Hash();
    },
    getTopicBoardForTopic:function(node){
        var id = node.getId();
        var result = this._boards[id];
        if(!$defined(result)){
            result = this._addNode(node);
        }
        return result;
    },
    _addNode:function(node){
        var board = null;
        if (this._isCentralTopic(node))
            board = this._createCentralTopicBoard(node);
        else
            board = this._createMainTopicBoard(node);
        var id = node.getId();
        this._boards[id]=board;
        return board;
    },
    _createMainTopicBoard:function(node){
        return new mindplot.layout.boards.Board(node, this);
    },
    _createCentralTopicBoard:function(node){
        return new mindplot.layout.boards.Board(node, this);
    },
    prepareNode:function(node, children){

    },
    addHelpers:function(node){

    },
    needsPrepositioning:function(){
        return true;
    },
    getDesigner:function(){
        return this._designer;
    },
    _isCentralTopic:function(node){
        var type = node.getModel().getType();
        return type == mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE;
    },
    getClassName:function(){
        return mindplot.layout.BaseLayoutManager.NAME;
    }
});

mindplot.layout.BaseLayoutManager.NAME ="BaseLayoutManager";

mindplot.layout.BaseLayoutManager.implement(new Events);
mindplot.layout.BaseLayoutManager.implement(new Options);/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

mindplot.layout.OriginalLayoutManager = new Class({
    Extends:mindplot.layout.BaseLayoutManager,
    options:{

    },
    initialize:function(designer, options) {
        this.parent(designer, options);
        this._dragTopicPositioner = new mindplot.DragTopicPositioner(this);
        // Init dragger manager.
        var workSpace = this.getDesigner().getWorkSpace();
        this._dragger = this._buildDragManager(workSpace);

        // Add shapes to speed up the loading process ...
        mindplot.DragTopic.initialize(workSpace);
    },
    prepareNode:function(node, children) {
        // Sort children by order to solve adding order in for OriginalLayoutManager...
        var nodesByOrder = new Hash();
        var maxOrder = 0;
        var result = [];
        if (children.length > 0) {
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                var order = child.getOrder();
                if (!$defined(order)) {
                    order = ++maxOrder;
                    child.setOrder(order);
                }

                if (nodesByOrder.has(order)) {
                    if (Math.sign(child.getPosition().x) == Math.sign(nodesByOrder.get(order).getPosition().x)) {
                        //duplicated order. Change order to next available.
                        order = ++maxOrder;
                        child.setOrder(order);
                    }
                } else {
                    nodesByOrder.set(order, child);
                    if (order > maxOrder)
                        maxOrder = order;
                }
                result[order] = child;
            }
        }
        nodesByOrder = null;
        return node.getTopicType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE ? result : children;
    },
    _nodeResizeEvent:function(node) {

    },
    _nodeRepositionateEvent:function(node) {
        this.getTopicBoardForTopic(node).repositionate();
    },
    getDragTopicPositioner : function() {
        return this._dragTopicPositioner;
    },
    _buildDragManager: function(workspace) {
        // Init dragger manager.
        var dragger = new mindplot.DragManager(workspace);
        var topics = this.getDesigner()._getTopics();

        var dragTopicPositioner = this.getDragTopicPositioner();

        dragger.addEventListener('startdragging', function(event, node) {
            // Enable all mouse events.
            for (var i = 0; i < topics.length; i++) {
                topics[i].setMouseEventsEnabled(false);
            }
        });

        dragger.addEventListener('dragging', function(event, dragTopic) {
            // Update the state and connections of the topic ...
            dragTopicPositioner.positionateDragTopic(dragTopic);
        });

        dragger.addEventListener('enddragging', function(event, dragTopic) {
            // Enable all mouse events.
            for (var i = 0; i < topics.length; i++) {
                topics[i].setMouseEventsEnabled(true);
            }
            // Topic must be positioned in the real board postion.
            if (dragTopic._isInTheWorkspace) {
                var draggedTopic = dragTopic.getDraggedTopic();

                // Hide topic during draw ...
                draggedTopic.setBranchVisibility(false);
                var parentNode = draggedTopic.getParent();
                dragTopic.updateDraggedTopic(workspace);


                // Make all node visible ...
                draggedTopic.setVisibility(true);
                if (parentNode != null) {
                    parentNode.setBranchVisibility(true);
                }
            }
        });

        return dragger;
    },
    registerListenersOnNode : function(topic) {
        // Register node listeners ...
        var designer = this.getDesigner();
        topic.addEventListener('onfocus', function(event) {
            designer.onObjectFocusEvent.attempt([topic, event], designer);
        });

        // Add drag behaviour ...
        if (topic.getType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE) {

            // Central Topic doesn't support to be dragged
            var dragger = this._dragger;
            dragger.add(topic);
        }

        // Register editor events ...
        if (!$defined(this.getDesigner()._viewMode) || ($defined(this.getDesigner()._viewMode) && !this.getDesigner()._viewMode)) {
            this.getDesigner()._editor.listenEventOnNode(topic, 'dblclick', true);
        }

    },
    _createMainTopicBoard:function(node) {
        return new mindplot.MainTopicBoard(node, this);
    },
    _createCentralTopicBoard:function(node) {
        return new mindplot.CentralTopicBoard(node, this);
    },
    getClassName:function() {
        return mindplot.layout.OriginalLayoutManager.NAME;
    }
});

mindplot.layout.OriginalLayoutManager.NAME = "OriginalLayoutManager";mindplot.layout.FreeMindLayoutManager = mindplot.layout.BaseLayoutManager.extend({
    options:{

    },
    initialize:function(designer, options){
        this.parent(designer, options);
    },
    _nodeConnectEvent:function(targetNode, node){
        if($defined(node.relationship)){
            this._movingNode(targetNode, node);
        }
        else if(!this._isCentralTopic(node)){
            this.parent(targetNode, node);
        }
    },
    _nodeDisconnectEvent:function(targetNode, node){
        if($defined(node.relationship)){
        }
        else{
            this.parent(targetNode, node);
            this._updateBoard(targetNode,[]);
        }
    },
    _nodeShrinkEvent:function(node){
        this._updateBoard(node,[]);
    },
    prepareNode:function(node, children){
        var layoutManagerName = editorProperties.layoutManager;
        //if last layout used is this one
        if(typeof layoutManagerName != "undefined" && layoutManagerName == this.getClassName()){
            var result = children.sort(function(n1, n2){
                if(n1.getPosition() && n2.getPosition())
                    return n1.getPosition().y>n2.getPosition().y;
                else
                    return true;
            });
        } else {
            //sort childs by order
            var result = children.sort(function(n1, n2){
                if(n1.getOrder() && n2.getOrder())
                    return n1.getOrder()>n2.getOrder();
                else
                    return true;
            });
            delete node.getModel()._finalPosition;
            result = children;
        }
        return result;
    },
    registerListenersOnNode : function(topic)
    {
        var id = topic.getId();
        // Register node listeners ...
        var designer = this.getDesigner();
        topic.addEventListener('onfocus', function(event)
        {
            designer.onObjectFocusEvent.attempt([topic, event], designer);
        });

        // Add drag behaviour ...
        if (topic.getType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE)
        {
            topic.addEventListener("mousedown",this._reconnectMouseDownListener.bindWithEvent(this,[topic]));
        }

         // Register editor events ...
        if (!$defined(this.getDesigner()._viewMode)|| ($defined(this.getDesigner()._viewMode) && !this.getDesigner()._viewMode))
        {
            this.getDesigner()._editor.listenEventOnNode(topic, 'dblclick', true);
        }

    },
    _mousedownListener:function(event,topic){

        var workSpace = this._designer.getWorkSpace();
        if (workSpace.isWorkspaceEventsEnabled())
        {
            // Disable double drag...
            workSpace.enableWorkspaceEvents(false);
            
            var id = topic.getId();
            this._command = new mindplot.commands.freeMind.DragTopicCommand();
            this._modifiedTopics = new Hash();

            var topics = this.getDesigner()._getTopics();
            // Disable all mouse events.
            for (var i = 0; i < topics.length; i++)
            {
                topics[i].setMouseEventsEnabled(false);
            }

            var ev = new Event(event);

            var screen = workSpace.getScreenManager();

            // Set initial position.
            this._mouseInitialPos = screen.getWorkspaceMousePosition(event);
            var pos = topic.getPosition();
            this._mouseInitialPos.x = 0;
            this._mouseInitialPos.y = pos.y - Math.round(this._mouseInitialPos.y);

            this._isMovingNode=false;

            // Register mouse move listener ...
            this._mouseMoveListenerInstance = this._mouseMoveListener.bindWithEvent(this,[topic]);
            screen.addEventListener('mousemove', this._mouseMoveListenerInstance);

            // Register mouse up listeners ...
            this._mouseUpListenerInstance = this._mouseUpListener.bindWithEvent(this,[topic]);
            screen.addEventListener('mouseup', this._mouseUpListenerInstance);

            // Change cursor.
            window.document.body.style.cursor = 'move';
        }
    },
    _mouseMoveListener:function(event, node){
        if(!this._isMovingNode){
            this._isMovingNode=true;
            var screen = this._designer.getWorkSpace().getScreenManager();
            var nodePos = node.getPosition().clone();
            nodePos.x-=this._mouseInitialPos.x;
            nodePos.y-=this._mouseInitialPos.y;
            var pos = screen.getWorkspaceMousePosition(event);
            pos.x = Math.round(pos.x);
            pos.y = Math.round(pos.y);
            //if isolated topic
            if(node.getParent()==null){
                //If still in same side
                if(Math.sign(nodePos.x)==Math.sign(pos.x)){
                    var x = nodePos.x - pos.x;
                    var y = nodePos.y - pos.y;
                    var delta = new core.Point(Math.round(x), Math.round(y));
                    var actualPos = node.getPosition().clone();
                    var newPos = new core.Point(actualPos.x-(delta.x==null?0:delta.x), actualPos.y-delta.y);
                    node.setPosition(newPos, false);
                    this._addToModifiedList(this._modifiedTopics, node.getId(), actualPos, newPos);
                    this._updateChildrenBoards(node, delta, this._modifiedTopics);
                }else{
                    this._changeChildrenSide(node, pos, this._modifiedTopics);
                    node.setPosition(pos.clone(), false);
                    this._addToModifiedList(this._modifiedTopics, node.getId(), nodePos, pos);
                }
            }else{
                //If still in same side
                if(Math.sign(nodePos.x)==Math.sign(pos.x) || (Math.sign(nodePos.x)!=Math.sign(pos.x) && !this._isCentralTopic(node.getParent()))){
                    var x = nodePos.x - pos.x;
                    var y = nodePos.y - pos.y;
                    var delta = new core.Point(Math.round(x), Math.round(y));
                    var board = this.getTopicBoardForTopic(node.getParent());
                    board.updateEntry(node, delta, this._modifiedTopics);
                } else {
                    var parentBoard = this.getTopicBoardForTopic(node.getParent());
                    var entryObj = parentBoard.findNodeEntryIndex(node);
                    var entry = entryObj.table[entryObj.index];
                    parentBoard._removeEntry(node, entryObj.table, entryObj.index, this._modifiedTopics);
                    this._changeChildrenSide(node, pos, this._modifiedTopics);
                    node.setPosition(pos.clone(), false);
                    if($defined(this._modifiedTopics.set)){
                        var key = node.getId();
                        if(this._modifiedTopics.has(key)){
                            nodePos = this._modifiedTopics.get(key).originalPos;
                        }
                        this._modifiedTopics.set(key,{originalPos:nodePos, newPos:pos});
                    }
                    entryObj = parentBoard.findNewNodeEntryIndex(entry);
                    parentBoard._addEntry(entry, entryObj.table, entryObj.index);
                    parentBoard._updateTable(entryObj.index,  entryObj.table, this._modifiedTopics, true);

                }
            }
            this._isMovingNode=false;
        }
        event.preventDefault();
    },
    _changeChildrenSide:function(node, newPos, modifiedTopics){
        var children = node._getChildren();
        if(children.length>0){
            var refPos = node.getPosition();
            for( var i = 0 ; i< children.length ; i++){
                var child = children[i];
                var childPos = child.getPosition().clone();
                var oldPos=childPos.clone();
                childPos.x = newPos.x +(childPos.x - refPos.x)*-1;
                childPos.y = newPos.y +(childPos.y - refPos.y);
                this._changeChildrenSide(child, childPos, modifiedTopics);
                child.setPosition(childPos, false);
                if($defined(modifiedTopics.set)){
                    var key = node.getId();
                    if(modifiedTopics.has(key)){
                        oldPos = this._modifiedTopics.get(key).originalPos;
                    }
                    this._modifiedTopics.set(key,{originalPos:oldPos, newPos:childPos});
                }
            }
        }
    },
    _mouseUpListener:function(event, node){

        var screen = this._designer.getWorkSpace().getScreenManager();
        // Remove all the events.
        screen.removeEventListener('mousemove', this._mouseMoveListenerInstance);
        screen.removeEventListener('mouseup', this._mouseUpListenerInstance);
        delete this._mouseMoveListenerInstance;
        delete this._mouseUpListenerInstance;

        var topics = this.getDesigner()._getTopics();
        // Disable all mouse events.
        for (var i = 0; i < topics.length; i++)
        {
            topics[i].setMouseEventsEnabled(true);
        }

        // Change the cursor to the default.
        window.document.body.style.cursor = 'default';

        this._designer.getWorkSpace().enableWorkspaceEvents(true);

        this._command.setModifiedTopics(this._modifiedTopics);
        var actionRunner = mindplot.DesignerActionRunner.getInstance();
        actionRunner.execute(this._command);
        this._command=null;
        this._modifiedTopics=null;
        this._mouseInitialPos=null;

    },
    getClassName:function(){
        return mindplot.layout.FreeMindLayoutManager.NAME;
    },
    _createMainTopicBoard:function(node){
        return new mindplot.layout.boards.freemind.MainTopicBoard(node, this);
    },
    _createCentralTopicBoard:function(node){
        return new mindplot.layout.boards.freemind.CentralTopicBoard(node, this);
    }
    ,
    _updateParentBoard:function(node, modifiedTopics){
        this._updateBoard(node.getParent(), modifiedTopics);
    },
    _updateBoard:function(node, modifiedTopics){
        var parent = node;
        if(!this._isCentralTopic(parent) && parent.getParent()!=null){
            var parentBoard = this.getTopicBoardForTopic(parent.getParent());
            var result = parentBoard.findNodeEntryIndex(parent);
            var parentEntry = result.table[result.index];
            var board = this.getTopicBoardForTopic(parent);
            var table = board._getTableForNode(null);
            if(table.length>0){
                var firstChild = table[0];
                var marginTop = parentEntry.getPosition()-(firstChild.getPosition()-firstChild.getTotalMarginTop());
                parentBoard.setNodeChildrenMarginTop(parentEntry,marginTop);
                var lastChild = table[table.length-1];
                var marginBottom = (lastChild.getPosition()+lastChild.getTotalMarginBottom())-parentEntry.getPosition();
                parentBoard.setNodeChildrenMarginBottom(parentEntry,marginBottom);
            } else {
                parentBoard.setNodeChildrenMarginTop(parentEntry, 0);
                parentBoard.setNodeChildrenMarginBottom(parentEntry, 0);
            }
            parentBoard._updateTable(result.index, result.table, modifiedTopics, false);
            this._updateParentBoard(parent, modifiedTopics);
        }
    },
    _updateChildrenBoards:function(node, delta, modifiedTopics){
        var board = this.getTopicBoardForTopic(node);
        var topics = board._getTableForNode(null);
        for(var i=0; i<topics.length; i++){
            board._updateEntryPos(topics[i],delta, modifiedTopics, false);
        }
    },
    addHelpers:function(node){
        if (node.getType() != mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE)
            this._addMoveHelper(node);
    },
    _addMoveHelper:function(node){
        var moveShape = new mindplot.ActionIcon(node, mindplot.layout.FreeMindLayoutManager.MOVE_IMAGE_URL);
        moveShape.setCursor('move');
        var positionate = function(node){
            if(node.getId() == this.getNode().getId()){
                var size = this.getNode().getSize();
                this.setPosition(size.width/2,0);
            }
        }.bind(moveShape);
        positionate(node);
        moveShape.setVisibility(false);
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeResizeEvent,positionate);
        var show = function(node){
            if(node.getId() == this.getNode().getId()){
                this.setVisibility(true);
            }
        }.bind(moveShape);
        var hide = function(node){
            if(node.getId() == this.getNode().getId()){
                this.setVisibility(false);
            }
        }.bind(moveShape);
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeMouseOverEvent,show);
        mindplot.EventBus.instance.addEvent(mindplot.EventBus.events.NodeMouseOutEvent,hide);
        node.addHelper(moveShape);
        moveShape.addEventListener("mousedown",this._mousedownListener.bindWithEvent(this,[node]));

    },
    needsPrepositioning:function(){
        return false;
    },
    _reconnectMouseDownListener:function(event, topic){
        var workSpace = this._designer.getWorkSpace();
        if (workSpace.isWorkspaceEventsEnabled())
        {
            // Disable double drag...
            workSpace.enableWorkspaceEvents(false);

            var id = topic.getId();
            this._command = new mindplot.commands.freeMind.ReconnectTopicCommand();
            this._modifiedTopics = new Hash();
            this._mouseOverListeners = new Hash();
            this._mouseOutListeners = new Hash();

            if(topic.getParent()!=null){
                var board = this.getTopicBoardForTopic(topic.getParent());
                this._currentIndex = board.findNodeEntryIndex(topic).index;
            }

            var topics = this.getDesigner()._getTopics();
            // Disable all mouse events.
            for (var i = 0; i < topics.length; i++)
            {
                topics[i].setMouseEventsEnabled(false);
                if(topics[i].getId()!=topic.getId()){
                    var overListener = this._reconnectMouseOverListener.bindWithEvent(topics[i],[this]);
                    topics[i].addEventListener('mouseover',overListener);
                    this._mouseOverListeners.set(topics[i].getId(),overListener);
                    var outListener = this._reconnectMouseOutListener.bindWithEvent(topics[i],[this]);
                    topics[i].addEventListener('mouseout',outListener);
                    this._mouseOutListeners.set(topics[i].getId(),outListener);
                }
            }
            this._updateTopicsForReconnect(topic, mindplot.layout.FreeMindLayoutManager.RECONNECT_NODES_OPACITY);
            var line = topic.getOutgoingLine();
            if($defined(line)){
                line.setVisibility(false);
            }
            this._createIndicatorShapes();

            var ev = new Event(event);

            var screen = workSpace.getScreenManager();

            this._isMovingNode=false;

            // Register mouse move listener ...
            this._mouseMoveListenerInstance = this._reconnectMouseMoveListener.bindWithEvent(this,[topic]);
            screen.addEventListener('mousemove', this._mouseMoveListenerInstance);

            // Register mouse up listeners ...
            this._mouseUpListenerInstance = this._reconnectMouseUpListener.bindWithEvent(this,[topic]);
            screen.addEventListener('mouseup', this._mouseUpListenerInstance);

            // Change cursor.
            window.document.body.style.cursor = 'move';
        }
    },
    _reconnectMouseMoveListener:function(event, node){
        if(!this._isMovingNode){
            this._isMovingNode=true;
            var screen = this._designer.getWorkSpace().getScreenManager();
            var nodePos = node.getPosition().clone();
            var pos = screen.getWorkspaceMousePosition(event);
            pos.x = Math.round(pos.x);
            pos.y = Math.round(pos.y);
            //If still in same side
            if(Math.sign(nodePos.x)==Math.sign(pos.x)){
                var x = nodePos.x - pos.x;
                var y = nodePos.y - pos.y;
                var delta = new core.Point(Math.round(x), Math.round(y));
                var newPos = new core.Point(nodePos.x-(delta.x==null?0:delta.x), nodePos.y-delta.y);
                node.setPosition(newPos, false);
                this._updateChildrenBoards(node, delta, this._modifiedTopics);
            } else {
                this._changeChildrenSide(node, pos, this._modifiedTopics);
                node.setPosition(pos.clone(), false);
//                entryObj = parentBoard.findNewNodeEntryIndex(entry);
//                parentBoard._addEntry(entry, entryObj.table, entryObj.index);
//                parentBoard._updateTable(entryObj.index,  entryObj.table, this._modifiedTopics, true);

            }
            if($defined(this._modifiedTopics.set)){
                var key = node.getId();
                if(this._modifiedTopics.has(key)){
                    nodePos = this._modifiedTopics.get(key).originalPos;
                }
                this._modifiedTopics.set(key,{originalPos:nodePos, newPos:pos});
            }
            this._isMovingNode=false;
        }
        event.preventDefault();
    },
    _reconnectMouseUpListener:function(event, node){
         var screen = this._designer.getWorkSpace().getScreenManager();
        // Remove all the events.
        screen.removeEventListener('mousemove', this._mouseMoveListenerInstance);
        screen.removeEventListener('mouseup', this._mouseUpListenerInstance);
        delete this._mouseMoveListenerInstance;
        delete this._mouseUpListenerInstance;

        var topics = this.getDesigner()._getTopics();
        // Disable all mouse events.
        for (var i = topics.length-1; i >=0; i--)
        {
            topics[i].setMouseEventsEnabled(true);
            if(topics[i].getId()!=node.getId()){
                var overListener = this._mouseOverListeners.get(topics[i].getId());
                topics[i].removeEventListener('mouseover',overListener);
                var outListener = this._mouseOutListeners.get(topics[i].getId());
                topics[i].removeEventListener('mouseout',outListener);
            }
        }

        this._restoreTopicsForReconnect(node);

        this._removeIndicatorShapes(node);

        //Check that it has to be relocated
        if(this._createShape !=null){
            if(this._createShape == "Child"){
                if(node.getParent()!=null && node.getParent().getId() == this._targetNode.getId()){
                    var mod = this._modifiedTopics.get(node.getId());
                    if(Math.sign(mod.originalPos.x) == Math.sign(node.getPosition().x))
                        this._createShape = null;
                }
            }else if(node.getParent()!=null && this._targetNode.getParent()!= null && node.getParent().getId() == this._targetNode.getParent().getId()){
                var chkboard = this.getTopicBoardForTopic(this._targetNode.getParent());
                var mod = this._modifiedTopics.get(node.getId());
                var chk = chkboard.findNodeEntryIndex(node, mod.originalPos);
                if(this._createShape == "Sibling_top"){
                    if(chk.table>this._currentIndex+1){
                        var nextEntry = chk.table[this._currentIndex+1];
                        if(nextEntry.getNode().getId() == this._targetNode.getId()){
                            this._createShape = null;
                        }
                    }
                } else if(this._currentIndex>0){
                    var prevEntry = chk.table[this._currentIndex-1];
                    if(prevEntry.getNode().getId() == this._targetNode.getId()){
                        this._createShape = null;
                    }
                }
            }
        }

        if(this._createShape == null){
            //cancel everything.
            var line = node.getOutgoingLine();
            if($defined(line)){
                line.setVisibility(true);
            }
            core.Utils.animatePosition(this._modifiedTopics, null, this.getDesigner());
        }else{
            this._command.setModifiedTopics(this._modifiedTopics);
            this._command.setDraggedTopic(node, this._currentIndex);
            this._command.setTargetNode(this._targetNode);
            this._command.setAs(this._createShape);
            //todo:Create command
            var actionRunner = mindplot.DesignerActionRunner.getInstance();
            actionRunner.execute(this._command);
        }

        // Change the cursor to the default.
        window.document.body.style.cursor = 'default';

        this._designer.getWorkSpace().enableWorkspaceEvents(true);

        this._command=null;
        this._modifiedTopics=null;
        this._mouseInitialPos=null;
        this._mouseOverListeners=null;
        this._mouseOutListeners=null;
        this._targetNode = null;
        this._createShape = null;
    },
    //function binded to the node with the over event
    _reconnectMouseOverListener:function(event, layoutManager){
        var size = this.getSize();
        var screen = layoutManager.getDesigner().getWorkSpace().getScreenManager();
        var pos = screen.getWorkspaceMousePosition(event);
        pos.x = Math.round(pos.x);
        pos.y = Math.round(pos.y);
        var nodePos = this.getPosition();
        //if it is on the child half side, or it is central topic add it as child
        if(!this.areChildrenShrinked() && (layoutManager._isCentralTopic(this) || this.getParent()==null || ((Math.sign(nodePos.x)>0 && pos.x>nodePos.x) || (Math.sign(nodePos.x)<0 && pos.x<nodePos.x)))){
            layoutManager._updateIndicatorShapes(this, mindplot.layout.FreeMindLayoutManager.RECONNECT_SHAPE_CHILD, pos);
        }else{
            //is a sibling. if mouse in top half sibling goes above this one
            if(pos.y<nodePos.y){
                layoutManager._updateIndicatorShapes(this, mindplot.layout.FreeMindLayoutManager.RECONNECT_SHAPE_SIBLING_TOP);
            }else{
                //if mouse in bottom half sibling goes below this one
                layoutManager._updateIndicatorShapes(this, mindplot.layout.FreeMindLayoutManager.RECONNECT_SHAPE_SIBLING_BOTTOM);
            }
        }
    },
    _createIndicatorShapes:function(){
        if(!$defined(this._createChildShape) || !$defined(this._createSiblingShape)){
            var rectAttributes = {fillColor:'#CC0033',opacity:0.4,width:30,height:30,strokeColor:'#FF9933'};
            var rect = new web2d.Rect(0, rectAttributes);
            rect.setVisibility(false);
            this._createChildShape = rect;

            rect = new web2d.Rect(0, rectAttributes);
            rect.setVisibility(false);
            this._createSiblingShape = rect;
        }
    },
    _updateIndicatorShapes:function(topic, shape, mousePos){
        if(this._createChildShape.getParent()!=null|| this._createSiblingShape.getParent()!=null){
            this._createChildShape.getParent().removeChild(this._createChildShape._peer);
            this._createSiblingShape.getParent().removeChild(this._createSiblingShape._peer);
        }
        topic.get2DElement().appendChild(this._createChildShape);
        topic.get2DElement().appendChild(this._createSiblingShape);
        var size = topic.getSize();
        var position = topic.getPosition();
        if(shape == mindplot.layout.FreeMindLayoutManager.RECONNECT_SHAPE_CHILD){
            this._createChildShape.setSize(size.width/2, size.height);
            var sign = mousePos?Math.sign(mousePos.x):Math.sign(position.x);
            this._createChildShape.setPosition(sign>0?size.width/2:0, 0);
            this._createChildShape.setVisibility(true);
            this._createSiblingShape.setVisibility(false);
            this._createShape = "Child";
            this._targetNode = topic;
        } else if(shape == mindplot.layout.FreeMindLayoutManager.RECONNECT_SHAPE_SIBLING_TOP){
            this._createSiblingShape.setSize(size.width,size.height/2);
            this._createSiblingShape.setPosition(0,0);
            this._createSiblingShape.setVisibility(true);
            this._createChildShape.setVisibility(false);
            this._createShape = "Sibling_top";
            this._targetNode = topic;
        }else if(shape == mindplot.layout.FreeMindLayoutManager.RECONNECT_SHAPE_SIBLING_BOTTOM){
            this._createSiblingShape.setSize(size.width,size.height/2);
            this._createSiblingShape.setPosition(0,size.height/2);
            this._createSiblingShape.setVisibility(true);
            this._createChildShape.setVisibility(false);
            this._createShape = "Sibling_bottom";
            this._targetNode = topic;
        } else {
            this._createSiblingShape.setVisibility(false);
            this._createChildShape.setVisibility(false);
            this._createShape = null;
            this._targetNode = null;
        }
    },
    _removeIndicatorShapes:function(node){
        if(this._createChildShape.getParent()!=null|| this._createSiblingShape.getParent()!=null){
            this._createChildShape.getParent().removeChild(this._createChildShape._peer);
            this._createSiblingShape.getParent().removeChild(this._createSiblingShape._peer);
        }
    },
    _reconnectMouseOutListener:function(event, layoutManager){
        layoutManager._updateIndicatorShapes(this, null);
    },
    _updateTopicsForReconnect:function(topic, opacity){
        topic.setOpacity(opacity);
        topic.moveToBack();
        var children = topic._getChildren();
        for(var k = 0; k<children.length; k++){
            this._updateTopicsForReconnect(children[k], opacity);
        }
    },
    _restoreTopicsForReconnect:function(topic){
        var children = topic._getChildren();
        for(var k = 0; k<children.length; k++){
            this._restoreTopicsForReconnect(children[k]);
        }
        topic.setOpacity(1);
        topic.moveToFront();
    },
    _movingNode:function(targetNode, node){
        var entry;
        if(node._relationship_oldParent!=null){
            var parentBoard = this.getTopicBoardForTopic(node._relationship_oldParent);
            var entryObj;
            if(this._isCentralTopic(node._relationship_oldParent)){
                var oldPos = node._originalPosition;
                entryObj = parentBoard.findNodeEntryIndex(node,oldPos);
            }else{
                entryObj = parentBoard.findNodeEntryIndex(node);
            }
            entry = entryObj.table[entryObj.index];
            parentBoard._removeEntry(node, entryObj.table, entryObj.index, []);
        }
        else{
            //if is an isolated topic, create entry and update margins.
            entry = new mindplot.layout.boards.freemind.Entry(node, false);
            var board = this.getTopicBoardForTopic(node);
            var table = board._getTableForNode(null);
            if(table.length>0){
                var firstChild = table[0];
                var marginTop = entry.getPosition()-(firstChild.getPosition()-firstChild.getTotalMarginTop());
                board.setNodeChildrenMarginTop(entry,marginTop);
                var lastChild = table[table.length-1];
                var marginBottom = (lastChild.getPosition()+lastChild.getTotalMarginBottom())-entry.getPosition();
                board.setNodeChildrenMarginBottom(entry,marginBottom);
            } else {
                board.setNodeChildrenMarginTop(entry, 0);
                board.setNodeChildrenMarginBottom(entry, 0);
            }
        }
        var targetBoard = this.getTopicBoardForTopic(targetNode);
        var table = targetBoard._getTableForNode(node);
        var index;
        if(node.relationship == 'undo'){
            index = node._relationship_index;
            //I need to update all entries because nodes position have been changed by command

        }else{
            if(node.relationship == "Child"){

                var newNodePos=new core.Point();
                if(table.length>0){
                    //if no children use the position set by Entry initializer. Otherwise place as last child
                    var lastChild = table[table.length-1];
                    newNodePos.y = lastChild.getPosition()+lastChild.getTotalMarginBottom() + entry.getTotalMarginTop();
                } else {
                    newNodePos.y = targetNode.getPosition().y;
                }
                var parentPos = targetNode.getPosition();
                var pwidth = targetNode.getSize().width;
                var width = node.getSize().width;
                if(this._isCentralTopic(targetNode)){
                    newNodePos.x = Math.sign(node.getPosition().x) * (entry._DEFAULT_X_GAP + pwidth/2 + width/2)
                }
                else{
                    newNodePos.x = parentPos.x + Math.sign(parentPos.x) * (entry._DEFAULT_X_GAP + pwidth/2 + width/2);
                }

                index = table.length;
            } else {
                //moving as sibling of targetNode

                var sibObj = targetBoard.findNodeEntryIndex(node._relationship_sibling_node);
                var siblingEntry =sibObj.table[sibObj.index];

                var newNodePos=new core.Point();
                if(node.relationship == "Sibling_top"){
                    if(sibObj.index==0){
                        newNodePos.y = siblingEntry.getPosition();
                    }else{
                        newNodePos.y =siblingEntry.getPosition()-siblingEntry.getTotalMarginTop()+entry.getTotalMarginTop();
                    }
                    index = sibObj.index;
                }
                else{
                    newNodePos.y = siblingEntry.getPosition()+siblingEntry.getTotalMarginBottom() + entry.getTotalMarginTop();
                    index = sibObj.index+1;
                }
                var parentPos = targetNode.getPosition();
                var pwidth = targetNode.getSize().width;
                var width = node.getSize().width;
                if(this._isCentralTopic(targetNode)){
                    newNodePos.x = Math.sign(node.getPosition().x) * (entry._DEFAULT_X_GAP + pwidth/2 + width/2)
                }
                else{
                    newNodePos.x = parentPos.x + Math.sign(parentPos.x) * (entry._DEFAULT_X_GAP + pwidth/2 + width/2);
                }
            }
            var nodePos = node.getPosition();
            var x = nodePos.x - newNodePos.x;
            var y = nodePos.y - newNodePos.y;
            var delta = new core.Point(Math.round(x), Math.round(y));
            entry.setPosition(newNodePos.x, newNodePos.y);
            this._updateChildrenBoards(node, delta, []);
        }
        targetBoard._addEntry(entry, table, index);
        targetBoard._updateTable(index,  table, [], true);
        this._updateBoard(targetNode,[]);
        if(node._relationship_oldParent!=null)
            this._updateBoard(node._relationship_oldParent,[]);

        mindplot.EventBus.instance.fireEvent(mindplot.EventBus.events.NodeMouseOutEvent,[node ]);
    },
    _addToModifiedList:function(modifiedTopics, key, originalpos, newPos){
        if($defined(modifiedTopics.set)){
            if(modifiedTopics.has(key)){
                originalpos = modifiedTopics.get(key).originalPos;
            }
            modifiedTopics.set(key,{originalPos:originalpos, newPos:newPos});
        }
    }
});

mindplot.layout.FreeMindLayoutManager.NAME ="FreeMindLayoutManager";
mindplot.layout.FreeMindLayoutManager.MOVE_IMAGE_URL = "../images/move.png";
mindplot.layout.FreeMindLayoutManager.RECONNECT_NODES_OPACITY = 0.4;
mindplot.layout.FreeMindLayoutManager.RECONNECT_SHAPE_CHILD = "child";
mindplot.layout.FreeMindLayoutManager.RECONNECT_SHAPE_SIBLING_TOP = "top";
mindplot.layout.FreeMindLayoutManager.RECONNECT_SHAPE_SIBLING_BOTTOM = "bottom";mindplot.layout.LayoutManagerFactory = {};
mindplot.layout.LayoutManagerFactory.managers = {
    OriginalLayoutManager:mindplot.layout.OriginalLayoutManager,
    FreeMindLayoutManager:mindplot.layout.FreeMindLayoutManager
};
mindplot.layout.LayoutManagerFactory.getManagerByName = function(name) {
    var manager = mindplot.layout.LayoutManagerFactory.managers[name + "Manager"];
    if ($defined(manager)) {
        return manager;
    }
    else {
        return mindplot.layout.LayoutManagerFactory.managers["OriginalLayoutManager"];
    }
};


mindplot.collaboration = {};
mindplot.collaboration.CollaborationManager = new Class({
    initialize:function(){
        this.collaborativeModelReady = false;
        this.collaborativeModelReady = null;
        this.wiseReady = false;
    },
    isCollaborationFrameworkAvailable:function(){
        return $defined(goog.collab.CollaborativeApp);
    },
    setCollaborativeFramework:function(framework){
        this._collaborativeFramework = framework;
        this.collaborativeModelReady = true;
        if(this.wiseReady){
            buildCollaborativeMindmapDesigner();
        }
    },
    setWiseReady:function(ready){
        this.wiseReady=ready;
    },
    isCollaborativeFrameworkReady:function(){
        return this.collaborativeModelReady;
    },
    buildWiseModel: function(){
        return this._collaborativeFramework.buildWiseModel();
    }
});

$wise_collaborationManager = new mindplot.collaboration.CollaborationManager();
mindplot.collaboration.frameworks = {};

mindplot.collaboration.frameworks.AbstractCollaborativeFramework = new Class({
    initialize: function(model, collaborativeModelFactory){
        this._collaborativeModelFactory = collaborativeModelFactory;
        if(!$defined(model)){
            model = this._buildInitialCollaborativeModel();
        }
        this._model = model;
    },
    getModel: function(){
        return this._model;
    },
    buildWiseModel: function(){
        var cmindMap = this.getModel();
        var mindmap = new mindplot.model.Mindmap();
        var branches = cmindMap.getBranches();
        branches.forEach(function(branch){
            var type = branch.getType();
            var id = branch.getId();
            var node = mindmap.createNode(type,id);
            node.setText(branch.getText());
            mindmap.addBranch(node);
        }.bind(this))
        return mindmap;
    },
    _buildInitialCollaborativeModel: function(){
        var mindmap = this._collaborativeModelFactory.buildMindMap();
        this.addMindmap(mindmap);
        var centralTopic = mindmap.createNode(mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE);
        mindmap.addBranch(centralTopic);
        return mindmap;
    },
    addMindmap:function(model){}

});mindplot.collaboration.frameworks.AbstractCollaborativeModelFactory = new Class({
    initialize:function(){},
    buildMindMap:function(){

    },
    buildCollaborativeModelFor:function(model){

    }
});/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
mindplot.collaboration.frameworks.brix={};
mindplot.collaboration.frameworks.brix.model = {};
mindplot.collaboration.frameworks.brix.model.NodeModel = new Class({
    Extends: mindplot.model.NodeModel,
    initialize:function(brixModel, brixFramework, type, mindmap, id) {
        this._brixModel = brixModel;
        this._brixFramework = brixFramework;
        if($defined(this._brixModel)){
            type = this._brixModel.get("type");
            id = this._brixModel.get("id");
        }
        this.parent(type, mindmap, id);
        if(!$defined(this._brixModel)){
            this._brixModel = this._createBrixModel();
        }
    },
    _createBrixModel:function(){
        var model = this._brixFramework.getBrixModel().create("Map");
        model.put("type",this._type);
        model.put("id",this._id);
        model.put("text",this._type==mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE?"Central Topic":"Main Topic");
        model.addListener("valueChanged",this._valueChangedListener.bind(this));
        return model;
    },
    getBrixModel:function(){
        return this._brixModel;
    },
    _valueChangedListener:function(event){
        console.log("property: "+ event.getProperty()+" value: "+event.getNewValue());
    },
    clone  : function() {
        var result = new mindplot.model.NodeModel(this._type, this._mindmap);
        result._order = this._order;
        result._type = this._type;
        result._children = this._children.map(function(item, index) {
            var model = item.clone();
            model._parent = result;
            return model;
        });


        result._icons = this._icons;
        result._links = this._links;
        result._notes = this._notes;
        result._size = this._size;
        result._position = this._position;
        result._id = this._id;
        result._mindmap = this._mindmap;
        result._text = this._text;
        result._shapeType = this._shapeType;
        result._fontFamily = this._fontFamily;
        result._fontSize = this._fontSize;
        result._fontStyle = this._fontStyle;
        result._fontWeight = this._fontWeight;
        result._fontColor = this._fontColor;
        result._borderColor = this._borderColor;
        result._backgroundColor = this._backgroundColor;
        result._areChildrenShrinked = this._areChildrenShrinked;
        return result;
    },

    areChildrenShrinked  : function() {
        return this._areChildrenShrinked;
    },

    setChildrenShrinked  : function(value) {
        this._areChildrenShrinked = value;
    },

    getId  : function() {
        return this._id;
    },


    setId  : function(id) {
        this._id = id;
        if (mindplot.model.NodeModel._uuid < id) {
            mindplot.model.NodeModel._uuid = id;
        }
    },

    getType  : function() {
        return this._type;
    },

    setText  : function(text) {
        this.parent(text);
        this._brixModel.set("text",text);
    },

    getText  : function() {
        return this._text;
    },

    isNodeModel  : function() {
        return true;
    },

    isConnected  : function() {
        return this._parent != null;
    },

    createLink  : function(url) {
        $assert(url, 'Link URL must be specified.');
        return new mindplot.model.LinkModel(url, this);
    },

    addLink  : function(link) {
        $assert(link && link.isLinkModel(), 'Only LinkModel can be appended to Mindmap object as links');
        this._links.push(link);
    },

    _removeLink  : function(link) {
        $assert(link && link.isLinkModel(), 'Only LinkModel can be appended to Mindmap object as links');
        this._links.erase(link);
    },

    createNote  : function(text) {
        $assert(text != null, 'note text must be specified.');
        return new mindplot.model.NoteModel(text, this);
    },

    addNote  : function(note) {
        $assert(note && note.isNoteModel(), 'Only NoteModel can be appended to Mindmap object as links');
        this._notes.push(note);
    },

    _removeNote  : function(note) {
        $assert(note && note.isNoteModel(), 'Only NoteModel can be appended to Mindmap object as links');
        this._notes.erase(note);
    },

    createIcon  : function(iconType) {
        $assert(iconType, 'IconType must be specified.');
        return new mindplot.model.IconModel(iconType, this);
    },

    addIcon  : function(icon) {
        $assert(icon && icon.isIconModel(), 'Only IconModel can be appended to Mindmap object as icons');
        this._icons.push(icon);
    },

    _removeIcon  : function(icon) {
        $assert(icon && icon.isIconModel(), 'Only IconModel can be appended to Mindmap object as icons');
        this._icons.erase(icon);
    },

    removeLastIcon  : function() {
        this._icons.pop();
    },

    _appendChild  : function(child) {
        $assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object');
        this._children.push(child);
        child._parent = this;
    },

    _removeChild  : function(child) {
        $assert(child && child.isNodeModel(), 'Only NodeModel can be appended to Mindmap object.');
        this._children.erase(child);
        child._parent = null;
    },

    setPosition  : function(x, y) {
        $assert(x, "x coordinate must be defined");
        $assert(y, "y coordinate must be defined");

        if (!$defined(this._position)) {
            this._position = new core.Point();
        }
        this._position.x = parseInt(x);
        this._position.y = parseInt(y);
    },

    getPosition  : function() {
        return this._position;
    },

    setFinalPosition  : function(x, y) {
        $assert(x, "x coordinate must be defined");
        $assert(y, "y coordinate must be defined");

        if (!$defined(this._finalPosition)) {
            this._finalPosition = new core.Point();
        }
        this._finalPosition.x = parseInt(x);
        this._finalPosition.y = parseInt(y);
    },

    getFinalPosition  : function() {
        return this._finalPosition;
    },

    setSize  : function(width, height) {
        this._size.width = width;
        this._size.height = height;
    },

    getSize  : function() {
        return {width:this._size.width,height:this._size.height};
    },

    getChildren  : function() {
        return this._children;
    },

    getIcons  : function() {
        return this._icons;
    },

    getLinks  : function() {
        return this._links;
    },

    getNotes  : function() {
        return this._notes;
    },

    getParent  : function() {
        return this._parent;
    },

    getMindmap  : function() {
        return this._mindmap;
    },

    setParent  : function(parent) {
        $assert(parent != this, 'The same node can not be parent and child if itself.');
        this._parent = parent;
    },

    canBeConnected  : function(sourceModel, sourcePosition, targetTopicHeight) {
        $assert(sourceModel != this, 'The same node can not be parent and child if itself.');
        $assert(sourcePosition, 'childPosition can not be null.');
        $assert(targetTopicHeight, 'childrenWidth can not be null.');

        // Only can be connected if the node is in the left or rigth.
        var targetModel = this;
        var mindmap = targetModel.getMindmap();
        var targetPosition = targetModel.getPosition();
        var result = false;

        if (sourceModel.getType() == mindplot.model.NodeModel.MAIN_TOPIC_TYPE) {
            // Finally, check current node ubication.
            var targetTopicSize = targetModel.getSize();
            var yDistance = Math.abs(sourcePosition.y - targetPosition.y);
            var gap = 35 + targetTopicHeight / 2;
            if (targetModel.getChildren().length > 0) {
                gap += Math.abs(targetPosition.y - targetModel.getChildren()[0].getPosition().y);
            }

            if (yDistance <= gap) {
                // Circular connection ?
                if (!sourceModel._isChildNode(this)) {
                    var toleranceDistance = (targetTopicSize.width / 2) + targetTopicHeight;

                    var xDistance = sourcePosition.x - targetPosition.x;
                    var isTargetAtRightFromCentral = targetPosition.x >= 0;

                    if (isTargetAtRightFromCentral) {
                        if (xDistance >= -targetTopicSize.width / 2 && xDistance <= mindplot.model.NodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE / 2 + (targetTopicSize.width / 2)) {
                            result = true;
                        }

                    } else {
                        if (xDistance <= targetTopicSize.width / 2 && Math.abs(xDistance) <= mindplot.model.NodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE / 2 + (targetTopicSize.width / 2)) {
                            result = true;
                        }
                    }
                }
            }
        } else {
            throw "No implemented yet";
        }
        return result;
    },

    _isChildNode  : function(node) {
        var result = false;
        if (node == this) {
            result = true;
        } else {
            var children = this.getChildren();
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                result = child._isChildNode(node);
                if (result) {
                    break;
                }
            }
        }
        return result;

    },

    connectTo  : function(parent) {
        var mindmap = this.getMindmap();
        mindmap.connect(parent, this);
        this._parent = parent;
    },

    disconnect  : function() {
        var mindmap = this.getMindmap();
        mindmap.disconnect(this);
    },

    getOrder  : function() {
        return this._order;
    },

    getShapeType  : function() {
        return this._shapeType;
    },

    setShapeType  : function(type) {
        this._shapeType = type;
    },

    setOrder  : function(value) {
        this._order = value;
    },

    setFontFamily  : function(value) {
        this._fontFamily = value;
    },

    getOrder  : function() {
        return this._order;
    },

    getFontFamily  : function() {
        return this._fontFamily;
    },

    setFontStyle  : function(value) {
        this._fontStyle = value;
    },

    getFontStyle  : function() {
        return this._fontStyle;
    },

    setFontWeight  : function(value) {
        this._fontWeight = value;
    },

    getFontWeight  : function() {
        return this._fontWeight;
    },

    setFontColor  : function(value) {
        this._fontColor = value;
    },

    getFontColor  : function() {
        return this._fontColor;
    },

    setFontSize  : function(value) {
        this._fontSize = value;
    },

    getFontSize  : function() {
        return this._fontSize;
    },

    getBorderColor  : function() {
        return this._borderColor;
    },

    setBorderColor  : function(color) {
        this._borderColor = color;
    },

    getBackgroundColor  : function() {
        return this._backgroundColor;
    },

    setBackgroundColor  : function(color) {
        this._backgroundColor = color;
    },

    deleteNode  : function() {
        var mindmap = this._mindmap;

        // if it has children nodes, Their must be disconnected.
        var lenght = this._children;
        for (var i = 0; i < lenght; i++) {
            var child = this._children[i];
            mindmap.disconnect(child);
        }

        var parent = this._parent;
        if ($defined(parent)) {
            // if it is connected, I must remove it from the parent..
            mindmap.disconnect(this);
        }

        // It's an isolated node. It must be a hole branch ...
        var branches = mindmap.getBranches();
        branches.erase(this);

    },

   inspect  : function() {
        return '(type:' + this.getType() + ' , id: ' + this.getId() + ')';
    }
});

mindplot.model.NodeModel.CENTRAL_TOPIC_TYPE = 'CentralTopic';
mindplot.model.NodeModel.MAIN_TOPIC_TYPE = 'MainTopic';
mindplot.model.NodeModel.DRAGGED_TOPIC_TYPE = 'DraggedTopic';

mindplot.model.NodeModel.SHAPE_TYPE_RECT = 'rectagle';
mindplot.model.NodeModel.SHAPE_TYPE_ROUNDED_RECT = 'rounded rectagle';
mindplot.model.NodeModel.SHAPE_TYPE_ELIPSE = 'elipse';
mindplot.model.NodeModel.SHAPE_TYPE_LINE = 'line';

mindplot.model.NodeModel.MAIN_TOPIC_TO_MAIN_TOPIC_DISTANCE = 220;

/**
 * @todo: This method must be implemented.
 */
mindplot.model.NodeModel._nextUUID = function() {
    if (!$defined(this._uuid)) {
        this._uuid = 0;
    }

    this._uuid = this._uuid + 1;
    return this._uuid;
}

/*
 *    Copyright [2011] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
mindplot.collaboration.frameworks.brix.model.Mindmap = new Class({
        Extends:mindplot.model.Mindmap,
        initialize:function(brixModel, brixFramework){
            this.parent();
            this._brixModel = brixModel;
            this._brixFramework = brixFramework;
            if(!$defined(this._brixModel)){
                this._brixModel = this._createBrixModel();
            }else{
                var branches = this._brixModel.get("branches");
                for(var i=0; i<branches.size(); i++){
                    var node = branches.get(i);
                    var nodeModel = new mindplot.collaboration.frameworks.brix.model.NodeModel(node, this._brixFramework);
                    this.addBranch(nodeModel, false);
                }
            }
        },
        _createBrixModel:function(){
            var model = this._brixFramework.getBrixModel().create("Map");
            var branches = this._brixFramework.getBrixModel().create("List");
            model.put("branches",branches);
            this._brixFramework.addMindmap(model);
            return model;
        },
        getBrixModel:function(){
            return this._brixModel;
        },
        setId : function(id) {
            this._iconType = id;
        },
        setVersion : function(version) {
            this._version = version;
        },
        addBranch : function(nodeModel, addToModel) {
            this.parent(nodeModel);
            if($defined(addToModel) && addToModel){
                var branches = this._brixModel.get("branches");
                branches.add(nodeModel.getBrixModel());
            }
        },
        connect : function(parent, child) {
            this.parent(parent, child);

            // Remove from the branch ...
            var branches = this._brixModel.get("branches");
            var childIndex = null;
            for(var i = 0; i<branches.size(); i++){
                if(branches.get(i)==child.getBrixModel()){
                    childIndex = i;
                    break;
                }
            }
            if(childIndex!=null){
                branches.remove(childIndex);
            }
        },

        disconnect : function(child) {
            var parent = child.getParent();
            $assert(child, 'Child can not be null.');
            $assert(parent, 'Child model seems to be already connected');

            parent._removeChild(child);

            var branches = this.getBranches();
            branches.push(child);

        },
        _createNode : function(type, id) {
            $assert(type, 'Node type must be specified.');
            var result = new mindplot.collaboration.frameworks.brix.model.NodeModel(null, this._brixFramework, type, this, id);
            return result;
        },

        createRelationship : function(fromNode, toNode) {
            $assert(fromNode, 'from node cannot be null');
            $assert(toNode, 'to node cannot be null');

            return new mindplot.model.RelationshipModel(fromNode, toNode);
        },

        addRelationship : function(relationship) {
            this._relationships.push(relationship);
        },

        removeRelationship : function(relationship) {
            this._relationships.erase(relationship);
        }
    }
);mindplot.collaboration.frameworks.brix.BrixCollaborativeModelFactory = new Class({
    Extends:mindplot.collaboration.frameworks.AbstractCollaborativeModelFactory,
    initialize:function(brixFramework){
        this._brixFramework = brixFramework;
    },
    buildMindMap:function(){
        return new mindplot.collaboration.frameworks.brix.model.Mindmap(null, this._brixFramework);
    },
    buildCollaborativeModelFor:function(model){
        return new mindplot.collaboration.frameworks.brix.model.Mindmap(model, this._brixFramework);
    }
});

mindplot.collaboration.frameworks.brix.BrixFramework = new Class({
    Extends: mindplot.collaboration.frameworks.AbstractCollaborativeFramework,

    initialize: function(model, app){
        this._app = app;
        var collaborativeModelFactory = new mindplot.collaboration.frameworks.brix.BrixCollaborativeModelFactory(this);
        var cModel = null;
        var root = this.getBrixModel().getRoot();
        if(!root.isEmpty()){
            cModel = collaborativeModelFactory.buildCollaborativeModelFor(root.get("mindmap"));
        }
        this.parent(cModel, collaborativeModelFactory);
    },
    addMindmap:function(model){
        var root = this.getBrixModel().getRoot();
        root.put("mindmap",model);
    },
    getBrixModel:function(){
        return this._app.getModel();
    },
    buildWiseModel: function(){
          return this.parent();
    }
});
instanciated=false;
mindplot.collaboration.frameworks.brix.BrixFramework.instanciate=function(){
    if($defined(isGoogleBrix) && !instanciated){
        instanciated=true;
        var app = new goog.collab.CollaborativeApp();
        app.start();
        app.addListener('modelLoad', function(model){
            var framework = new mindplot.collaboration.frameworks.brix.BrixFramework(model, app);
            $wise_collaborationManager.setCollaborativeFramework(framework);
        }.bind(this));
    }
};

mindplot.collaboration.frameworks.brix.BrixFramework.instanciate();


/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

if($defined(afterMindpotLibraryLoading))
{
    afterMindpotLibraryLoading();
}
