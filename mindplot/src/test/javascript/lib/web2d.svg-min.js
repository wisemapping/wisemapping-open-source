var web2d={};
web2d.peer={svg:{}};
web2d.peer.utils={};web2d.peer.utils.EventUtils={broadcastChangeEvent:function(elementPeer,type){var listeners=elementPeer.getChangeEventListeners(type);
if($defined(listeners)){for(var i=0;
i<listeners.length;
i++){var listener=listeners[i];
listener.call(elementPeer,null)
}}var children=elementPeer.getChildren();
for(var j=0;
j<children.length;
j++){var child=children[j];
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
}};web2d.peer.svg.ElementPeer=new Class({initialize:function(svgElement){this._native=svgElement;
if(!this._native.addEvent){for(var key in Element){this._native[key]=Element.prototype[key]
}}this._size={width:1,height:1};
this._changeListeners={}
},setChildren:function(children){this._children=children
},getChildren:function(){var result=this._children;
if(!$defined(result)){result=[];
this._children=result
}return result
},getParent:function(){return this._parent
},setParent:function(parent){this._parent=parent
},append:function(elementPeer){elementPeer.setParent(this);
var children=this.getChildren();
children.include(elementPeer);
this._native.appendChild(elementPeer._native);
web2d.peer.utils.EventUtils.broadcastChangeEvent(this,"strokeStyle")
},removeChild:function(elementPeer){elementPeer.setParent(null);
var children=this.getChildren();
var oldLength=children.length;
children.erase(elementPeer);
$assert(children.length<oldLength,"element could not be removed:"+elementPeer);
this._native.removeChild(elementPeer._native)
},addEvent:function(type,listener){$(this._native).bind(type,listener)
},trigger:function(type,event){$(this._native).trigger(type,event)
},cloneEvents:function(from){this._native.cloneEvents(from)
},removeEvent:function(type,listener){$(this._native).unbind(type,listener)
},setSize:function(width,height){if($defined(width)&&this._size.width!=parseInt(width)){this._size.width=parseInt(width);
this._native.setAttribute("width",parseInt(width))
}if($defined(height)&&this._size.height!=parseInt(height)){this._size.height=parseInt(height);
this._native.setAttribute("height",parseInt(height))
}web2d.peer.utils.EventUtils.broadcastChangeEvent(this,"strokeStyle")
},getSize:function(){return{width:this._size.width,height:this._size.height}
},setFill:function(color,opacity){if($defined(color)){this._native.setAttribute("fill",color)
}if($defined(opacity)){this._native.setAttribute("fill-opacity",opacity)
}},getFill:function(){var color=this._native.getAttribute("fill");
var opacity=this._native.getAttribute("fill-opacity");
return{color:color,opacity:Number(opacity)}
},getStroke:function(){var vmlStroke=this._native;
var color=vmlStroke.getAttribute("stroke");
var dashstyle=this._stokeStyle;
var opacity=vmlStroke.getAttribute("stroke-opacity");
var width=vmlStroke.getAttribute("stroke-width");
return{color:color,style:dashstyle,opacity:opacity,width:width}
},setStroke:function(width,style,color,opacity){if($defined(width)){this._native.setAttribute("stroke-width",width+"px")
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
}},setVisibility:function(isVisible){this._native.setAttribute("visibility",(isVisible)?"visible":"hidden")
},isVisible:function(){var visibility=this._native.getAttribute("visibility");
return !(visibility=="hidden")
},updateStrokeStyle:function(){var strokeStyle=this._stokeStyle;
if(this.getParent()){if(strokeStyle&&strokeStyle!="solid"){this.setStroke(null,strokeStyle)
}}},attachChangeEventListener:function(type,listener){var listeners=this.getChangeEventListeners(type);
if(!$defined(listener)){throw"Listener can not be null"
}listeners.push(listener)
},getChangeEventListeners:function(type){var listeners=this._changeListeners[type];
if(!$defined(listeners)){listeners=[];
this._changeListeners[type]=listeners
}return listeners
},moveToFront:function(){this._native.parentNode.appendChild(this._native)
},moveToBack:function(){this._native.parentNode.insertBefore(this._native,this._native.parentNode.firstChild)
},setCursor:function(type){this._native.style.cursor=type
}});
web2d.peer.svg.ElementPeer.prototype.svgNamespace="http://www.w3.org/2000/svg";
web2d.peer.svg.ElementPeer.prototype.linkNamespace="http://www.w3.org/1999/xlink";
web2d.peer.svg.ElementPeer.prototype.__stokeStyleToStrokDasharray={solid:[],dot:[1,3],dash:[4,3],longdash:[10,2],dashdot:[5,3,1,3]};web2d.peer.svg.ElipsePeer=new Class({Extends:web2d.peer.svg.ElementPeer,initialize:function(){var svgElement=window.document.createElementNS(this.svgNamespace,"ellipse");
this.parent(svgElement);
this.attachChangeEventListener("strokeStyle",web2d.peer.svg.ElementPeer.prototype.updateStrokeStyle);
this._position={x:0,y:0}
},setSize:function(width,height){this.parent(width,height);
if($defined(width)){this._native.setAttribute("rx",width/2)
}if($defined(height)){this._native.setAttribute("ry",height/2)
}var pos=this.getPosition();
this.setPosition(pos.x,pos.y)
},setPosition:function(cx,cy){var size=this.getSize();
cx=cx+size.width/2;
cy=cy+size.height/2;
if($defined(cx)){this._native.setAttribute("cx",cx)
}if($defined(cy)){this._native.setAttribute("cy",cy)
}},getPosition:function(){return this._position
}});web2d.peer.svg.Font=new Class({initialize:function(){this._size=10;
this._style="normal";
this._weight="normal"
},init:function(args){if($defined(args.size)){this._size=parseInt(args.size)
}if($defined(args.style)){this._style=args.style
}if($defined(args.weight)){this._weight=args.weight
}},getHtmlSize:function(scale){var result=0;
if(this._size==6){result=this._size*scale.height*43/32
}if(this._size==8){result=this._size*scale.height*42/32
}else{if(this._size==10){result=this._size*scale.height*42/32
}else{if(this._size==15){result=this._size*scale.height*42/32
}}}return result
},getGraphSize:function(){return this._size*43/32
},getSize:function(){return parseInt(this._size)
},getStyle:function(){return this._style
},getWeight:function(){return this._weight
},setSize:function(size){this._size=size
},setStyle:function(style){this._style=style
},setWeight:function(weight){this._weight=weight
},getWidthMargin:function(){var result=0;
if(this._size==10||this._size==6){result=4
}return result
}});web2d.peer.svg.ArialFont=new Class({Extends:web2d.peer.svg.Font,initialize:function(){this.parent();
this._fontFamily="Arial"
},getFontFamily:function(){return this._fontFamily
},getFont:function(){return web2d.Font.ARIAL
}});web2d.peer.svg.PolyLinePeer=new Class({Extends:web2d.peer.svg.ElementPeer,initialize:function(){var svgElement=window.document.createElementNS(this.svgNamespace,"polyline");
this.parent(svgElement);
this.setFill("none");
this.breakDistance=10
},setFrom:function(x1,y1){this._x1=x1;
this._y1=y1;
this._updatePath()
},setTo:function(x2,y2){this._x2=x2;
this._y2=y2;
this._updatePath()
},setStrokeWidth:function(width){this._native.setAttribute("stroke-width",width)
},setColor:function(color){this._native.setAttribute("stroke",color)
},setStyle:function(style){this._style=style;
this._updatePath()
},getStyle:function(){return this._style
},_updatePath:function(){if(this._style=="Curved"){this._updateMiddleCurvePath()
}else{if(this._style=="Straight"){this._updateStraightPath()
}else{this._updateCurvePath()
}}},_updateStraightPath:function(){if($defined(this._x1)&&$defined(this._x2)&&$defined(this._y1)&&$defined(this._y2)){var path=web2d.PolyLine.buildStraightPath(this.breakDistance,this._x1,this._y1,this._x2,this._y2);
this._native.setAttribute("points",path)
}},_updateMiddleCurvePath:function(){var x1=this._x1;
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
}},_updateCurvePath:function(){if($defined(this._x1)&&$defined(this._x2)&&$defined(this._y1)&&$defined(this._y2)){var path=web2d.PolyLine.buildCurvedPath(this.breakDistance,this._x1,this._y1,this._x2,this._y2);
this._native.setAttribute("points",path)
}}});web2d.peer.svg.CurvedLinePeer=new Class({Extends:web2d.peer.svg.ElementPeer,initialize:function(){var svgElement=window.document.createElementNS(this.svgNamespace,"path");
this.parent(svgElement);
this._style={fill:"#495879"};
this._updateStyle();
this._customControlPoint_1=false;
this._customControlPoint_2=false;
this._control1=new core.Point();
this._control2=new core.Point();
this._lineStyle=true
},setSrcControlPoint:function(control){this._customControlPoint_1=true;
var change=this._control1.x!=control.x||this._control1.y!=control.y;
if($defined(control.x)){this._control1=control;
this._control1.x=parseInt(this._control1.x);
this._control1.y=parseInt(this._control1.y)
}if(change){this._updatePath()
}},setDestControlPoint:function(control){this._customControlPoint_2=true;
var change=this._control2.x!=control.x||this._control2.y!=control.y;
if($defined(control.x)){this._control2=control;
this._control2.x=parseInt(this._control2.x);
this._control2.y=parseInt(this._control2.y)
}if(change){this._updatePath()
}},isSrcControlPointCustom:function(){return this._customControlPoint_1
},isDestControlPointCustom:function(){return this._customControlPoint_2
},setIsSrcControlPointCustom:function(isCustom){this._customControlPoint_1=isCustom
},setIsDestControlPointCustom:function(isCustom){this._customControlPoint_2=isCustom
},getControlPoints:function(){return[this._control1,this._control2]
},setFrom:function(x1,y1){var change=this._x1!=parseInt(x1)||this._y1!=parseInt(y1);
this._x1=parseInt(x1);
this._y1=parseInt(y1);
if(change){this._updatePath()
}},setTo:function(x2,y2){var change=this._x2!=parseInt(x2)||this._y2!=parseInt(y2);
this._x2=parseInt(x2);
this._y2=parseInt(y2);
if(change){this._updatePath()
}},getFrom:function(){return new core.Point(this._x1,this._y1)
},getTo:function(){return new core.Point(this._x2,this._y2)
},setStrokeWidth:function(width){this._style["stroke-width"]=width;
this._updateStyle()
},setColor:function(color){this._style.stroke=color;
this._style.fill=color;
this._updateStyle()
},updateLine:function(avoidControlPointFix){this._updatePath(avoidControlPointFix)
},setLineStyle:function(style){this._lineStyle=style;
if(this._lineStyle){this._style.fill=this._fill
}else{this._fill=this._style.fill;
this._style.fill="none"
}this._updateStyle();
this.updateLine()
},getLineStyle:function(){return this._lineStyle
},setShowEndArrow:function(visible){this._showEndArrow=visible;
this.updateLine()
},isShowEndArrow:function(){return this._showEndArrow
},setShowStartArrow:function(visible){this._showStartArrow=visible;
this.updateLine()
},isShowStartArrow:function(){return this._showStartArrow
},_updatePath:function(avoidControlPointFix){if($defined(this._x1)&&$defined(this._y1)&&$defined(this._x2)&&$defined(this._y2)){this._calculateAutoControlPoints(avoidControlPointFix);
var path="M"+this._x1+","+this._y1+" C"+(this._control1.x+this._x1)+","+(this._control1.y+this._y1)+" "+(this._control2.x+this._x2)+","+(this._control2.y+this._y2)+" "+this._x2+","+this._y2+(this._lineStyle?" "+(this._control2.x+this._x2)+","+(this._control2.y+this._y2+3)+" "+(this._control1.x+this._x1)+","+(this._control1.y+this._y1+5)+" "+this._x1+","+(this._y1+7)+" Z":"");
this._native.setAttribute("d",path)
}},_updateStyle:function(){var style="";
for(var key in this._style){style+=key+":"+this._style[key]+" "
}this._native.setAttribute("style",style)
},_calculateAutoControlPoints:function(avoidControlPointFix){var defaultpoints=mindplot.util.Shape.calculateDefaultControlPoints(new core.Point(this._x1,this._y1),new core.Point(this._x2,this._y2));
if(!this._customControlPoint_1&&!($defined(avoidControlPointFix)&&avoidControlPointFix==0)){this._control1.x=defaultpoints[0].x;
this._control1.y=defaultpoints[0].y
}if(!this._customControlPoint_2&&!($defined(avoidControlPointFix)&&avoidControlPointFix==1)){this._control2.x=defaultpoints[1].x;
this._control2.y=defaultpoints[1].y
}},setDashed:function(length,spacing){if($defined(length)&&$defined(spacing)){this._native.setAttribute("stroke-dasharray",length+","+spacing)
}else{this._native.setAttribute("stroke-dasharray","")
}}});web2d.peer.svg.ArrowPeer=new Class({Extends:web2d.peer.svg.ElementPeer,initialize:function(){var svgElement=window.document.createElementNS(this.svgNamespace,"path");
this.parent(svgElement);
this._style={};
this._controlPoint=new core.Point();
this._fromPoint=new core.Point()
},setFrom:function(x,y){this._fromPoint.x=x;
this._fromPoint.y=y;
this._redraw()
},setControlPoint:function(point){this._controlPoint=point;
this._redraw()
},setStrokeColor:function(color){this.setStroke(null,null,color,null)
},setStrokeWidth:function(width){this.setStroke(width)
},setDashed:function(isDashed,length,spacing){if($defined(isDashed)&&isDashed&&$defined(length)&&$defined(spacing)){this._native.setAttribute("stroke-dasharray",length+","+spacing)
}else{this._native.setAttribute("stroke-dasharray","")
}},_updateStyle:function(){var style="";
for(var key in this._style){style+=key+":"+this._style[key]+" "
}this._native.setAttribute("style",style)
},_redraw:function(){var x,y,xp,yp;
if($defined(this._fromPoint.x)&&$defined(this._fromPoint.y)&&$defined(this._controlPoint.x)&&$defined(this._controlPoint.y)){if(this._controlPoint.y==0){this._controlPoint.y=1
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
}}});web2d.peer.svg.TextPeer=new Class({Extends:web2d.peer.svg.ElementPeer,initialize:function(){var svgElement=window.document.createElementNS(this.svgNamespace,"text");
this.parent(svgElement);
this._position={x:0,y:0};
this._font=new web2d.Font("Arial",this)
},append:function(element){this._native.appendChild(element._native)
},setTextAlignment:function(align){this._textAlign=align
},getTextAlignment:function(){return $defined(this._textAlign)?this._textAlign:"left"
},setText:function(text){while(this._native.firstChild){this._native.removeChild(this._native.firstChild)
}this._text=text;
if(text){var lines=text.split("\n");
var me=this;
lines.forEach(function(line){var tspan=window.document.createElementNS(me.svgNamespace,"tspan");
tspan.setAttribute("dy","1em");
tspan.setAttribute("x",me.getPosition().x);
tspan.textContent=line.length==0?" ":line;
me._native.appendChild(tspan)
})
}},getText:function(){return this._text
},setPosition:function(x,y){this._position={x:x,y:y};
this._native.setAttribute("y",y);
this._native.setAttribute("x",x);
$(this._native).children("tspan").attr("x",x)
},getPosition:function(){return this._position
},getNativePosition:function(){return $(this._native).position()
},setFont:function(font,size,style,weight){if($defined(font)){this._font=new web2d.Font(font,this)
}if($defined(style)){this._font.setStyle(style)
}if($defined(weight)){this._font.setWeight(weight)
}if($defined(size)){this._font.setSize(size)
}this._updateFontStyle()
},_updateFontStyle:function(){this._native.setAttribute("font-family",this._font.getFontFamily());
this._native.setAttribute("font-size",this._font.getGraphSize());
this._native.setAttribute("font-style",this._font.getStyle());
this._native.setAttribute("font-weight",this._font.getWeight())
},setColor:function(color){this._native.setAttribute("fill",color)
},getColor:function(){return this._native.getAttribute("fill")
},setTextSize:function(size){this._font.setSize(size);
this._updateFontStyle()
},setContentSize:function(width,height){this._native.xTextSize=width.toFixed(1)+","+height.toFixed(1)
},setStyle:function(style){this._font.setStyle(style);
this._updateFontStyle()
},setWeight:function(weight){this._font.setWeight(weight);
this._updateFontStyle()
},setFontFamily:function(family){var oldFont=this._font;
this._font=new web2d.Font(family,this);
this._font.setSize(oldFont.getSize());
this._font.setStyle(oldFont.getStyle());
this._font.setWeight(oldFont.getWeight());
this._updateFontStyle()
},getFont:function(){return{font:this._font.getFont(),size:parseInt(this._font.getSize()),style:this._font.getStyle(),weight:this._font.getWeight()}
},setSize:function(size){this._font.setSize(size);
this._updateFontStyle()
},getWidth:function(){var computedWidth;
try{computedWidth=this._native.getBBox().width;
if(computedWidth==0){var bbox=this._native.getBBox();
computedWidth=bbox.width
}}catch(e){computedWidth=10
}var width=parseInt(computedWidth);
width=width+this._font.getWidthMargin();
return width
},getHeight:function(){try{var computedHeight=this._native.getBBox().height
}catch(e){computedHeight=10
}return parseInt(computedHeight)
},getHtmlFontSize:function(){return this._font.getHtmlSize()
}});web2d.peer.svg.WorkspacePeer=new Class({Extends:web2d.peer.svg.ElementPeer,initialize:function(element){this._element=element;
var svgElement=window.document.createElementNS(this.svgNamespace,"svg");
this.parent(svgElement);
this._native.setAttribute("focusable","true");
this._native.setAttribute("id","workspace");
this._native.setAttribute("preserveAspectRatio","none")
},setCoordSize:function(width,height){var viewBox=this._native.getAttribute("viewBox");
var coords=[0,0,0,0];
if(viewBox!=null){coords=viewBox.split(/ /)
}if($defined(width)){coords[2]=width
}if($defined(height)){coords[3]=height
}this._native.setAttribute("viewBox",coords.join(" "));
this._native.setAttribute("preserveAspectRatio","none");
web2d.peer.utils.EventUtils.broadcastChangeEvent(this,"strokeStyle")
},getCoordSize:function(){var viewBox=this._native.getAttribute("viewBox");
var coords=[1,1,1,1];
if(viewBox!=null){coords=viewBox.split(/ /)
}return{width:coords[2],height:coords[3]}
},setCoordOrigin:function(x,y){var viewBox=this._native.getAttribute("viewBox");
var coords=[0,0,0,0];
if(viewBox!=null){coords=viewBox.split(/ /)
}if($defined(x)){coords[0]=x
}if($defined(y)){coords[1]=y
}this._native.setAttribute("viewBox",coords.join(" "))
},append:function(child){this.parent(child);
web2d.peer.utils.EventUtils.broadcastChangeEvent(child,"onChangeCoordSize")
},getCoordOrigin:function(child){var viewBox=this._native.getAttribute("viewBox");
var coords=[1,1,1,1];
if(viewBox!=null){coords=viewBox.split(/ /)
}var x=parseFloat(coords[0]);
var y=parseFloat(coords[1]);
return{x:x,y:y}
},getPosition:function(){return{x:0,y:0}
}});web2d.peer.svg.GroupPeer=new Class({Extends:web2d.peer.svg.ElementPeer,initialize:function(){var svgElement=window.document.createElementNS(this.svgNamespace,"g");
this.parent(svgElement);
this._native.setAttribute("preserveAspectRatio","none");
this._coordSize={width:1,height:1};
this._native.setAttribute("focusable","true");
this._position={x:0,y:0};
this._coordOrigin={x:0,y:0}
},setCoordSize:function(width,height){var change=this._coordSize.width!=width||this._coordSize.height!=height;
this._coordSize.width=width;
this._coordSize.height=height;
if(change){this.updateTransform()
}web2d.peer.utils.EventUtils.broadcastChangeEvent(this,"strokeStyle")
},getCoordSize:function(){return{width:this._coordSize.width,height:this._coordSize.height}
},updateTransform:function(){var sx=this._size.width/this._coordSize.width;
var sy=this._size.height/this._coordSize.height;
var cx=this._position.x-this._coordOrigin.x*sx;
var cy=this._position.y-this._coordOrigin.y*sy;
cx=isNaN(cx)?0:cx;
cy=isNaN(cy)?0:cy;
sx=isNaN(sx)?0:sx;
sy=isNaN(sy)?0:sy;
this._native.setAttribute("transform","translate("+cx+","+cy+") scale("+sx+","+sy+")")
},setOpacity:function(value){this._native.setAttribute("opacity",value)
},setCoordOrigin:function(x,y){var change=x!=this._coordOrigin.x||y!=this._coordOrigin.y;
if($defined(x)){this._coordOrigin.x=x
}if($defined(y)){this._coordOrigin.y=y
}if(change){this.updateTransform()
}},setSize:function(width,height){var change=width!=this._size.width||height!=this._size.height;
this.parent(width,height);
if(change){this.updateTransform()
}},setPosition:function(x,y){var change=x!=this._position.x||y!=this._position.y;
if($defined(x)){this._position.x=parseInt(x)
}if($defined(y)){this._position.y=parseInt(y)
}if(change){this.updateTransform()
}},getPosition:function(){return{x:this._position.x,y:this._position.y}
},append:function(child){this.parent(child);
web2d.peer.utils.EventUtils.broadcastChangeEvent(child,"onChangeCoordSize")
},getCoordOrigin:function(){return{x:this._coordOrigin.x,y:this._coordOrigin.y}
}});web2d.peer.svg.RectPeer=new Class({Extends:web2d.peer.svg.ElementPeer,initialize:function(arc){var svgElement=window.document.createElementNS(this.svgNamespace,"rect");
this.parent(svgElement);
this._arc=arc;
this.attachChangeEventListener("strokeStyle",web2d.peer.svg.ElementPeer.prototype.updateStrokeStyle)
},setPosition:function(x,y){if($defined(x)){this._native.setAttribute("x",parseInt(x))
}if($defined(y)){this._native.setAttribute("y",parseInt(y))
}},getPosition:function(){var x=this._native.getAttribute("x");
var y=this._native.getAttribute("y");
return{x:parseInt(x),y:parseInt(y)}
},setSize:function(width,height){this.parent(width,height);
var min=width<height?width:height;
if($defined(this._arc)){var arc=(min/2)*this._arc;
this._native.setAttribute("rx",arc);
this._native.setAttribute("ry",arc)
}}});web2d.peer.svg.ImagePeer=new Class({Extends:web2d.peer.svg.ElementPeer,initialize:function(){var svgElement=window.document.createElementNS(this.svgNamespace,"image");
this.parent(svgElement);
this._position={x:0,y:0};
this._href="";
this._native.setAttribute("preserveAspectRatio","none")
},setPosition:function(x,y){this._position={x:x,y:y};
this._native.setAttribute("y",y);
this._native.setAttribute("x",x)
},getPosition:function(){return this._position
},setHref:function(url){this._native.setAttributeNS(this.linkNamespace,"href",url);
this._href=url
},getHref:function(){return this._href
}});web2d.peer.svg.TimesFont=new Class({Extends:web2d.peer.svg.Font,initialize:function(){this.parent();
this._fontFamily="times"
},getFontFamily:function(){return this._fontFamily
},getFont:function(){return web2d.Font.TIMES
}});web2d.peer.svg.LinePeer=new Class({Extends:web2d.peer.svg.ElementPeer,initialize:function(){var svgElement=window.document.createElementNS(this.svgNamespace,"line");
this.parent(svgElement);
this.attachChangeEventListener("strokeStyle",web2d.peer.svg.ElementPeer.prototype.updateStrokeStyle)
},setFrom:function(x1,y1){this._x1=x1;
this._y1=y1;
this._native.setAttribute("x1",x1);
this._native.setAttribute("y1",y1)
},setTo:function(x2,y2){this._x2=x2;
this._y2=y2;
this._native.setAttribute("x2",x2);
this._native.setAttribute("y2",y2)
},getFrom:function(){return new core.Point(this._x1,this._y1)
},getTo:function(){return new core.Point(this._x2,this._y2)
},setArrowStyle:function(startStyle,endStyle){if($defined(startStyle)){}if($defined(endStyle)){}}});web2d.peer.svg.TahomaFont=new Class({Extends:web2d.peer.svg.Font,initialize:function(){this.parent();
this._fontFamily="tahoma"
},getFontFamily:function(){return this._fontFamily
},getFont:function(){return web2d.Font.TAHOMA
}});web2d.peer.svg.VerdanaFont=new Class({Extends:web2d.peer.svg.Font,initialize:function(){this.parent();
this._fontFamily="verdana"
},getFontFamily:function(){return this._fontFamily
},getFont:function(){return web2d.Font.VERDANA
}});web2d.Element=new Class({initialize:function(peer,attributes){this._peer=peer;
if(peer==null){throw new Error("Element peer can not be null")
}if($defined(attributes)){this._initialize(attributes)
}},_initialize:function(attributes){var batchExecute={};
for(var key in attributes){var funcName=this._attributeNameToFuncName(key,"set");
var funcArgs=batchExecute[funcName];
if(!$defined(funcArgs)){funcArgs=[]
}var signature=web2d.Element._propertyNameToSignature[key];
var argPositions=signature[1];
if(argPositions!=web2d.Element._SIGNATURE_MULTIPLE_ARGUMENTS){funcArgs[argPositions]=attributes[key]
}else{funcArgs=attributes[key].split(" ")
}batchExecute[funcName]=funcArgs
}for(var key in batchExecute){var func=this[key];
if(!$defined(func)){throw new Error("Could not find function: "+key)
}func.apply(this,batchExecute[key])
}},setSize:function(width,height){this._peer.setSize(width,height)
},setPosition:function(cx,cy){this._peer.setPosition(cx,cy)
},addEvent:function(type,listener){this._peer.addEvent(type,listener)
},trigger:function(type,event){this._peer.trigger(type,event)
},cloneEvents:function(from){this._peer.cloneEvents(from)
},removeEvent:function(type,listener){this._peer.removeEvent(type,listener)
},getType:function(){throw new Error("Not implemeneted yet. This method must be implemented by all the inherited objects.")
},getFill:function(){return this._peer.getFill()
},setFill:function(color,opacity){this._peer.setFill(color,opacity)
},getPosition:function(){return this._peer.getPosition()
},getNativePosition:function(){return this._peer.getNativePosition()
},setStroke:function(width,style,color,opacity){if(style!=null&&style!=undefined&&style!="dash"&&style!="dot"&&style!="solid"&&style!="longdash"&&style!="dashdot"){throw new Error("Unsupported stroke style: '"+style+"'")
}this._peer.setStroke(width,style,color,opacity)
},_attributeNameToFuncName:function(attributeKey,prefix){var signature=web2d.Element._propertyNameToSignature[attributeKey];
if(!$defined(signature)){throw"Unsupported attribute: "+attributeKey
}var firstLetter=signature[0].charAt(0);
return prefix+firstLetter.toUpperCase()+signature[0].substring(1)
},setAttribute:function(key,value){var funcName=this._attributeNameToFuncName(key,"set");
var signature=web2d.Element._propertyNameToSignature[key];
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
},getAttribute:function(key){var funcName=this._attributeNameToFuncName(key,"get");
var signature=web2d.Element._propertyNameToSignature[key];
if(signature==null){throw"Could not find the signature for:"+key
}var getter=this[funcName];
if(getter==null){throw"Could not find the function name:"+funcName
}var getterResult=getter.apply(this,[]);
var attibuteName=signature[2];
if(!$defined(attibuteName)){throw"Could not find attribute mapping for:"+key
}var result=getterResult[attibuteName];
if(!$defined(result)){throw"Could not find attribute with name:"+attibuteName
}return result
},setOpacity:function(opacity){this._peer.setStroke(null,null,null,opacity);
this._peer.setFill(null,opacity)
},setVisibility:function(isVisible){this._peer.setVisibility(isVisible)
},isVisible:function(){return this._peer.isVisible()
},moveToFront:function(){this._peer.moveToFront()
},moveToBack:function(){this._peer.moveToBack()
},getStroke:function(){return this._peer.getStroke()
},setCursor:function(type){this._peer.setCursor(type)
},getParent:function(){return this._peer.getParent()
}});
web2d.Element._SIGNATURE_MULTIPLE_ARGUMENTS=-1;
web2d.Element._supportedEvents=["click","dblclick","mousemove","mouseout","mouseover","mousedown","mouseup"];
web2d.Element._propertyNameToSignature={size:["size",-1],width:["size",0,"width"],height:["size",1,"height"],position:["position",-1],x:["position",0,"x"],y:["position",1,"y"],stroke:["stroke",-1],strokeWidth:["stroke",0,"width"],strokeStyle:["stroke",1,"style"],strokeColor:["stroke",2,"color"],strokeOpacity:["stroke",3,"opacity"],fill:["fill",-1],fillColor:["fill",0,"color"],fillOpacity:["fill",1,"opacity"],coordSize:["coordSize",-1],coordSizeWidth:["coordSize",0,"width"],coordSizeHeight:["coordSize",1,"height"],coordOrigin:["coordOrigin",-1],coordOriginX:["coordOrigin",0,"x"],coordOriginY:["coordOrigin",1,"y"],visibility:["visibility",0],opacity:["opacity",0]};web2d.Elipse=new Class({Extends:web2d.Element,initialize:function(attributes){var peer=web2d.peer.Toolkit.createElipse();
var defaultAttributes={width:40,height:40,x:5,y:5,stroke:"1 solid black",fillColor:"blue"};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}this.parent(peer,defaultAttributes)
},getType:function(){return"Elipse"
},getSize:function(){return this._peer.getSize()
}});web2d.Font=new Class({initialize:function(fontFamily,textPeer){var font="web2d.peer.Toolkit.create"+fontFamily+"Font();";
this._peer=eval(font);
this._textPeer=textPeer
},getHtmlSize:function(){var scale=web2d.peer.utils.TransformUtil.workoutScale(this._textPeer);
return this._peer.getHtmlSize(scale)
},getGraphSize:function(){var scale=web2d.peer.utils.TransformUtil.workoutScale(this._textPeer);
return this._peer.getGraphSize(scale)
},getFontScale:function(){return web2d.peer.utils.TransformUtil.workoutScale(this._textPeer).height
},getSize:function(){return this._peer.getSize()
},getStyle:function(){return this._peer.getStyle()
},getWeight:function(){return this._peer.getWeight()
},getFontFamily:function(){return this._peer.getFontFamily()
},setSize:function(size){return this._peer.setSize(size)
},setStyle:function(style){return this._peer.setStyle(style)
},setWeight:function(weight){return this._peer.setWeight(weight)
},getFont:function(){return this._peer.getFont()
},getWidthMargin:function(){return this._peer.getWidthMargin()
}});
web2d.Font.ARIAL="Arial";
web2d.Font.TIMES="Times";
web2d.Font.TAHOMA="Tahoma";
web2d.Font.VERDANA="Verdana";web2d.Group=new Class({Extends:web2d.Element,initialize:function(attributes){var peer=web2d.peer.Toolkit.createGroup();
var defaultAttributes={width:50,height:50,x:50,y:50,coordOrigin:"0 0",coordSize:"50 50"};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}this.parent(peer,defaultAttributes)
},removeChild:function(element){if(!$defined(element)){throw"Child element can not be null"
}if(element==this){throw"It's not possible to add the group as a child of itself"
}var elementType=element.getType();
if(elementType==null){throw"It seems not to be an element ->"+element
}this._peer.removeChild(element._peer)
},append:function(element){if(!$defined(element)){throw"Child element can not be null"
}if(element==this){throw"It's not posible to add the group as a child of itself"
}var elementType=element.getType();
if(elementType==null){throw"It seems not to be an element ->"+element
}if(elementType=="Workspace"){throw"A group can not have a workspace as a child"
}this._peer.append(element._peer)
},getType:function(){return"Group"
},setCoordSize:function(width,height){this._peer.setCoordSize(width,height)
},setCoordOrigin:function(x,y){this._peer.setCoordOrigin(x,y)
},getCoordOrigin:function(){return this._peer.getCoordOrigin()
},getSize:function(){return this._peer.getSize()
},setFill:function(color,opacity){throw"Unsupported operation. Fill can not be set to a group"
},setStroke:function(width,style,color,opacity){throw"Unsupported operation. Stroke can not be set to a group"
},getCoordSize:function(){return this._peer.getCoordSize()
},appendDomChild:function(DomElement){if(!$defined(DomElement)){throw"Child element can not be null"
}if(DomElement==this){throw"It's not possible to add the group as a child of itself"
}this._peer._native.append(DomElement)
},setOpacity:function(value){this._peer.setOpacity(value)
}});web2d.Image=new Class({Extends:web2d.Element,initialize:function(attributes){var peer=web2d.peer.Toolkit.createImage();
this.parent(peer,attributes)
},getType:function(){return"Image"
},setHref:function(href){this._peer.setHref(href)
},getHref:function(){return this._peer.getHref()
},getSize:function(){return this._peer.getSize()
}});web2d.Line=new Class({Extends:web2d.Element,initialize:function(attributes){var peer=web2d.peer.Toolkit.createLine();
var defaultAttributes={strokeColor:"#495879",strokeWidth:1,strokeOpacity:1};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}this.parent(peer,defaultAttributes)
},getType:function(){return"Line"
},setFrom:function(x,y){this._peer.setFrom(x,y)
},setTo:function(x,y){this._peer.setTo(x,y)
},getFrom:function(){return this._peer.getFrom()
},getTo:function(){return this._peer.getTo()
},setArrowStyle:function(startStyle,endStyle){this._peer.setArrowStyle(startStyle,endStyle)
},setPosition:function(cx,cy){throw"Unsupported operation"
},setSize:function(width,height){throw"Unsupported operation"
},setFill:function(color,opacity){throw"Unsupported operation"
}});web2d.PolyLine=new Class({Extends:web2d.Element,initialize:function(attributes){var peer=web2d.peer.Toolkit.createPolyLine();
var defaultAttributes={strokeColor:"blue",strokeWidth:1,strokeStyle:"solid",strokeOpacity:1};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}this.parent(peer,defaultAttributes)
},getType:function(){return"PolyLine"
},setFrom:function(x,y){this._peer.setFrom(x,y)
},setTo:function(x,y){this._peer.setTo(x,y)
},setStyle:function(style){this._peer.setStyle(style)
},getStyle:function(){return this._peer.getStyle()
},buildCurvedPath:function(dist,x1,y1,x2,y2){var signx=1;
var signy=1;
if(x2<x1){signx=-1
}if(y2<y1){signy=-1
}var path;
if(Math.abs(y1-y2)>2){var middlex=x1+((x2-x1>0)?dist:-dist);
path=x1.toFixed(1)+", "+y1.toFixed(1)+" "+middlex.toFixed(1)+", "+y1.toFixed(1)+" "+middlex.toFixed(1)+", "+(y2-5*signy).toFixed(1)+" "+(middlex+5*signx).toFixed(1)+", "+y2.toFixed(1)+" "+x2.toFixed(1)+", "+y2.toFixed(1)
}else{path=x1.toFixed(1)+", "+y1.toFixed(1)+" "+x2.toFixed(1)+", "+y2.toFixed(1)
}return path
},buildStraightPath:function(dist,x1,y1,x2,y2){var middlex=x1+((x2-x1>0)?dist:-dist);
return x1+", "+y1+" "+middlex+", "+y1+" "+middlex+", "+y2+" "+x2+", "+y2
}});web2d.CurvedLine=new Class({Extends:web2d.Element,initialize:function(attributes){var peer=web2d.peer.Toolkit.createCurvedLine();
var defaultAttributes={strokeColor:"blue",strokeWidth:1,strokeStyle:"solid",strokeOpacity:1};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}this.parent(peer,defaultAttributes)
},getType:function(){return"CurvedLine"
},setFrom:function(x,y){$assert(!isNaN(x),"x must be defined");
$assert(!isNaN(y),"y must be defined");
this._peer.setFrom(x,y)
},setTo:function(x,y){$assert(!isNaN(x),"x must be defined");
$assert(!isNaN(y),"y must be defined");
this._peer.setTo(x,y)
},getFrom:function(){return this._peer.getFrom()
},getTo:function(){return this._peer.getTo()
},setShowEndArrow:function(visible){this._peer.setShowEndArrow(visible)
},isShowEndArrow:function(){return this._peer.isShowEndArrow()
},setShowStartArrow:function(visible){this._peer.setShowStartArrow(visible)
},isShowStartArrow:function(){return this._peer.isShowStartArrow()
},setSrcControlPoint:function(control){this._peer.setSrcControlPoint(control)
},setDestControlPoint:function(control){this._peer.setDestControlPoint(control)
},getControlPoints:function(){return this._peer.getControlPoints()
},isSrcControlPointCustom:function(){return this._peer.isSrcControlPointCustom()
},isDestControlPointCustom:function(){return this._peer.isDestControlPointCustom()
},setIsSrcControlPointCustom:function(isCustom){this._peer.setIsSrcControlPointCustom(isCustom)
},setIsDestControlPointCustom:function(isCustom){this._peer.setIsDestControlPointCustom(isCustom)
},updateLine:function(avoidControlPointFix){return this._peer.updateLine(avoidControlPointFix)
},setStyle:function(style){this._peer.setLineStyle(style)
},getStyle:function(){return this._peer.getLineStyle()
},setDashed:function(length,spacing){this._peer.setDashed(length,spacing)
}});
web2d.CurvedLine.SIMPLE_LINE=false;
web2d.CurvedLine.NICE_LINE=true;web2d.Arrow=new Class({Extends:web2d.Element,initialize:function(attributes){var peer=web2d.peer.Toolkit.createArrow();
var defaultAttributes={strokeColor:"black",strokeWidth:1,strokeStyle:"solid",strokeOpacity:1};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}this.parent(peer,defaultAttributes)
},getType:function(){return"Arrow"
},setFrom:function(x,y){this._peer.setFrom(x,y)
},setControlPoint:function(point){this._peer.setControlPoint(point)
},setStrokeColor:function(color){this._peer.setStrokeColor(color)
},setStrokeWidth:function(width){this._peer.setStrokeWidth(width)
},setDashed:function(isDashed,length,spacing){this._peer.setDashed(isDashed,length,spacing)
}});web2d.Rect=new Class({Extends:web2d.Element,initialize:function(arc,attributes){if(arc&&arc>1){throw"Arc must be 0<=arc<=1"
}if(arguments.length<=0){var rx=0;
var ry=0
}var peer=web2d.peer.Toolkit.createRect(arc);
var defaultAttributes={width:40,height:40,x:5,y:5,stroke:"1 solid black",fillColor:"green"};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}this.parent(peer,defaultAttributes)
},getType:function(){return"Rect"
},getSize:function(){return this._peer.getSize()
}});web2d.Text=new Class({Extends:web2d.Element,initialize:function(attributes){var peer=web2d.peer.Toolkit.createText();
this.parent(peer,attributes)
},getType:function(){return"Text"
},setText:function(text){this._peer.setText(text)
},setTextAlignment:function(align){$assert(align,"align can not be null");
this._peer.setTextAlignment(align)
},setTextSize:function(width,height){this._peer.setContentSize(width,height)
},getText:function(){return this._peer.getText()
},setFont:function(font,size,style,weight){this._peer.setFont(font,size,style,weight)
},setColor:function(color){this._peer.setColor(color)
},getColor:function(){return this._peer.getColor()
},setStyle:function(style){this._peer.setStyle(style)
},setWeight:function(weight){this._peer.setWeight(weight)
},setFontFamily:function(family){this._peer.setFontFamily(family)
},getFont:function(){return this._peer.getFont()
},setSize:function(size){this._peer.setSize(size)
},getHtmlFontSize:function(){return this._peer.getHtmlFontSize()
},getWidth:function(){return this._peer.getWidth()
},getHeight:function(){return parseInt(this._peer.getHeight())
},getFontHeight:function(){var lines=this._peer.getText().split("\n").length;
return Math.round(this.getHeight()/lines)
}});web2d.peer.ToolkitSVG={init:function(){},createWorkspace:function(element){return new web2d.peer.svg.WorkspacePeer(element)
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
web2d.peer.Toolkit=web2d.peer.ToolkitSVG;web2d.Workspace=new Class({Extends:web2d.Element,initialize:function(attributes){this._htmlContainer=this._createDivContainer();
var peer=web2d.peer.Toolkit.createWorkspace(this._htmlContainer);
var defaultAttributes={width:"200px",height:"200px",stroke:"1px solid #edf1be",fillColor:"white",coordOrigin:"0 0",coordSize:"200 200"};
for(var key in attributes){defaultAttributes[key]=attributes[key]
}this.parent(peer,defaultAttributes);
this._htmlContainer.append(this._peer._native)
},getType:function(){return"Workspace"
},append:function(element){if(!$defined(element)){throw"Child element can not be null"
}var elementType=element.getType();
if(elementType==null){throw"It seems not to be an element ->"+element
}if(elementType=="Workspace"){throw"A workspace can not have a workspace as a child"
}this._peer.append(element._peer)
},addItAsChildTo:function(element){if(!$defined(element)){throw"Workspace div container can not be null"
}element.append(this._htmlContainer)
},_createDivContainer:function(){var container=window.document.createElement("div");
container.id="workspaceContainer";
container.style.position="relative";
container.style.top="0px";
container.style.left="0px";
container.style.height="688px";
container.style.border="1px solid red";
return $(container)
},setSize:function(width,height){if($defined(width)){this._htmlContainer.css("width",width)
}if($defined(height)){this._htmlContainer.css("height",height)
}this._peer.setSize(width,height)
},setCoordSize:function(width,height){this._peer.setCoordSize(width,height)
},setCoordOrigin:function(x,y){this._peer.setCoordOrigin(x,y)
},getCoordOrigin:function(){return this._peer.getCoordOrigin()
},_getHtmlContainer:function(){return this._htmlContainer
},setFill:function(color,opacity){this._htmlContainer.css("background-color",color);
if(opacity||opacity===0){throw"Unsupported operation. Opacity not supported."
}},getFill:function(){var color=this._htmlContainer.css("background-color");
return{color:color}
},getSize:function(){var width=this._htmlContainer.css("width");
var height=this._htmlContainer.css("height");
return{width:width,height:height}
},setStroke:function(width,style,color,opacity){if(style!="solid"){throw"Not supported style stroke style:"+style
}this._htmlContainer.css("border",width+" "+style+" "+color);
if(opacity||opacity===0){throw"Unsupported operation. Opacity not supported."
}},getCoordSize:function(){return this._peer.getCoordSize()
},removeChild:function(element){if(!$defined(element)){throw"Child element can not be null"
}if(element==this){throw"It's not possible to add the group as a child of itself"
}var elementType=element.getType();
if(elementType==null){throw"It seems not to be an element ->"+element
}this._peer.removeChild(element._peer)
},dumpNativeChart:function(){var elem=this._htmlContainer;
return elem.innerHTML
}});core.Point=new Class({initialize:function(x,y){this.x=x;
this.y=y
},setValue:function(x,y){this.x=x;
this.y=y
},inspect:function(){return"{x:"+this.x+",y:"+this.y+"}"
},clone:function(){return new core.Point(this.x,this.y)
}});
core.Point.fromString=function(point){var values=point.split(",");
return new core.Point(values[0],values[1])
};