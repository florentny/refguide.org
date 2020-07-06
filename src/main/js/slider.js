
var slider;
var activeSlider=0; 
var pos=1;

function initSlider() {
    slider=document.getElementById('slider1');
    slider.max=4;
    slider.min=2;
}

function attachSliderEvents() {
    addAnEvent(slider, 'mousedown', sliderMouseDown);
    addAnEvent(slider, 'mousemove', sliderMouseMove);
    addAnEvent(slider, 'mouseup', sliderMouseUp);
    //var outslider=document.getElementById('outslider');
    //addAnEvent(slider, 'mouseover', sliderMouseOver);
    if(document.all)
	    addAnEvent(slider, 'mouseleave', sliderMouseLeave);
    else
	    addAnEvent(slider, 'mouseout', sliderMouseOut);
}

function drawSliderByVal(slider) {
	var knob=slider.getElementsByTagName('img')[0];
	var p=(slider.val-slider.min)/(slider.max-slider.min);
        p=p*3;
        var p1=Math.round(p);
        p=p1/3;
        var x=(slider.scrollWidth-1)*p-5;
	knob.style.left=x+"px";
        if(p1!=pos) {
            pos=p1;
            if(pos==0)
                sizesmall();
            if(pos==1)
                sizereg();
            if(pos==2) {
                sizebig1();
            }
            if(pos==3)
                sizebig();
        }
	
}

function setInitVal(val) {
    var p=val/3;
    pos=val;
    var sliderx=document.getElementById('slider1');
    var knob=slider.getElementsByTagName('img')[0];
    var x=(sliderx.scrollWidth-1)*p-5;
    knob.style.left=x+"px";
}

function setSliderByClientX(slider, clientX) {
	var p=(clientX-slider.offsetLeft-7)/(slider.scrollWidth-15);
	slider.val=(slider.max-slider.min)*p + slider.min;
	if (slider.val>slider.max) slider.val=slider.max;
	if (slider.val<slider.min) slider.val=slider.min;

	drawSliderByVal(slider);
	//slider.onchange(slider.val, slider.num);
}

function stopEvent(event) {
	if (event.preventDefault) {
		event.preventDefault();
		event.stopPropagation();
	} else {
		event.returnValue=false;
		event.cancelBubble=true;
	}
}

function sliderMouseDown(e) {
      sliderClick(e);
      activeSlider=1;
      stopEvent(e);
}

function sliderMouseUp(e) {
      activeSlider=0;
      stopEvent(e);
}

function sliderMouseOver(e) {
      //activeSlider=0;
    
}

function isMouseLeaveOrEnter(e, handler)
{		
    if (e.type != 'mouseout' && e.type != 'mouseover') return false;
    var reltg = e.relatedTarget || e.toElement;
    while (reltg && reltg != handler) reltg = reltg.parentNode;
    return (reltg != handler); // && typeof reltg != 'undefined'
}
        
function sliderMouseOut(e) {
    
    if(isMouseLeaveOrEnter(e, this))
        activeSlider=0;
    
      //var relTarg = e.relatedTarget || e.toElement;
      //if(relTarg == this)
      //    activeSlider=0;
    
}

function sliderMouseLeave(e) {
        activeSlider=0;
}

function sliderMouseMove(e) {
      if(activeSlider==1)
        sliderClick(e);
    stopEvent(e);
}

function sliderClick(e) {
	setSliderByClientX(slider, e.clientX);
}

function addAnEvent(el, evname, func) {
    if (el.attachEvent) { // IE
        el.attachEvent("on" + evname, func);
    } else if (el.addEventListener) { // Gecko / W3C
        el.addEventListener(evname, func, false);
    } else {
        el["on" + evname] = func;
    }
}

//addAnEvent(window, 'load', attachSliderEvents);