//
// (c) 2007-2012 Florent Charpin
//  www@reefguide.org
//

var times = 0;
var curCol = 0;
var img_width = 240;
var img_width_act = 216;
var thumb_act = "thumb";
var cell_size = "celltd";
var check_cookie = 0;
var firstrun = 0;

function sizebig() {
    img_width = 400;
    img_width_act = 370;
    thumb_act = "thumb2";
    cell_size = "celltdbig";
    curCol = 0;
    Set_Cookie("Reefsize", "2");
    creategrid();
}

function sizebig1() {
    img_width = 316;
    img_width_act = 293;
    thumb_act = "thumb4";
    cell_size = "celltdbig1";
    curCol = 0;
    Set_Cookie("Reefsize", "3");
    creategrid();
}

function sizesmall() {
    img_width = 160;
    img_width_act = 144;
    thumb_act = "thumb3";
    cell_size = "celltdsmall";
    curCol = 0;
    Set_Cookie("Reefsize", "0");
    creategrid();
}

function sizereg() {
    img_width = 240;
    img_width_act = 216;
    thumb_act = "thumb";
    cell_size = "celltd";
    curCol = 0;
    Set_Cookie("Reefsize", "1");
    creategrid();
}

function Set_Cookie(name,value) {
    var cookieString = name + "=" +escape(value) + ";path=/" ;
    document.cookie = cookieString;
}

function Get_Cookie(name) {
    var start = document.cookie.indexOf(name+"=");
    var len = start+name.length+1;
    if ((!start) && (name != document.cookie.substring(0,name.length))) return null;
    if (start == -1) return null;
    var end = document.cookie.indexOf(";",len);
    if (end == -1) end = document.cookie.length;
    return unescape(document.cookie.substring(len,end));
}

function creategrid() {
    
    var myWidth = 0, myHeight = 0;
    if( typeof( window.innerWidth ) == 'number' ) {
        //Non-IE
        myWidth = window.innerWidth;
        myHeight = window.innerHeight;
    } else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
        //IE 6+ in 'standards compliant mode'
        myWidth = document.documentElement.clientWidth;
        myHeight = document.documentElement.clientHeight;
    } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
        //IE 4 compatible
        myWidth = document.body.clientWidth;
        myHeight = document.body.clientHeight;
    }
    
    if(check_cookie == 0) {

        var cookie = Get_Cookie("Reefsize");
        if(myWidth < 920) {
            if(cookie == null) {
                cookie = "0";
            }
        }
        if(myWidth > 2200) {
            if(cookie == null) {
                cookie = "3";
            }
        }
        if(cookie != null) {
            if(cookie == "0") {
                img_width = 160;
                img_width_act = 144;
                thumb_act = "thumb3";
                cell_size = "celltdsmall";
                curCol = 0;
                //setInitVal(0);
            }
            if(cookie == "2") {
                img_width = 400;
                img_width_act = 370;
                thumb_act = "thumb2";
                cell_size = "celltdbig";
                curCol = 0;
               // setInitVal(3);
            }
            if(cookie == "3") {
                img_width = 316;
                img_width_act = 293;
                thumb_act = "thumb4";
                cell_size = "celltdbig1";
                curCol = 0;
                //setInitVal(2);
            }
        }

        check_cookie = 1;
    }
    

    
    //var TTwidth =  document.images['tstimg'].width;
    var TTwidth = myWidth;
    var numpix = img_reef.length;
    var layout = new Array();
    var numCol = Math.floor((TTwidth - panelOffset) / img_width);
    var numRow = Math.ceil(numpix / numCol);
    if((maxCol != 0) && (numCol > maxCol))
        numCol = maxCol;
    
    if(curCol == numCol)
        return;
    
    curCol = numCol;
    
    var cum = 0;
    var count = 0;
    
    
    var ie_bug = true;
    try {
        document.createElement('<td colspan="3">');
    } catch(e) {
        ie_bug = false;
    }

    var mybodyT =document.getElementById("TopTable");
    var deltable = mybodyT.getElementsByTagName("tbody")[0];
    if(deltable != null)
        mybodyT.removeChild(deltable);


    var mytable;
    mybody = document.createElement("tbody");

    mybodyT.appendChild(mybody);



    var  start = 0;
    var  colpos = 0;
    var mycurrent_row;
    var from = 0;
    var to = 0;
    var span = 0;
    var color = new Array();
    color[0] = "green";
    color[1] = "blue";
    color[2] = "yellow";
    var colorNum = 0;


    for(var i = 0; i < numpix; i++) {
        span = 1;
        if(colpos == 0) {
            main_tr = document.createElement("tr");
            mybody.appendChild(main_tr);
            var end = i+numCol;
            if(end > numpix)
                end = numpix;
            for(var j = i; j < end;) {
                var dispHeader = false;
                var newCat = false;
                if((i != 0) && (j == i)) {
                    if(ref_reef[start] < end) {
                        //if(ref_reef[start-1] > (j - numCol))
                        dispHeader = true;
                        span = ref_reef[start] - j;
                    }
                    else {
                        if(ref_reef[start-1] > (j - numCol)) {
                            dispHeader = true;
                            span = numCol;
                            if((j+numCol) >= end)
                                span = end - j;
                        }
                    
                    }
                }
            
                if(ref_reef[start] == j) {
                    dispHeader = true;
                    newCat = true;
                    if(ref_reef[start+1] >= i+numCol) {
                        span = i+numCol-j;
                    }
                    else {
                        span = ref_reef[start+1] - j;
                    }
                }
            
                if(dispHeader) {
                    if(ie_bug) {
                        mycurrent_cell =  document.createElement('<td colspan="' + span + '">');
                    } else {      
                        mycurrent_cell = document.createElement("td");
                        mycurrent_cell.setAttribute("colspan",span);
                
                    }
                    mycurrent_cell.setAttribute("class","catrow");
            
                    div = document.createElement("div");
                    div.setAttribute("class","catheader");
                    
                    if(newCat)
                    {
                        if(i != 0)
                            div.setAttribute("id", cat_reef[start].replace(/ /g, "_"));
                        currenttext = document.createTextNode(cat_reef[start]);
                        var link = document.createElement("a");
                        link.setAttribute("href", cat_reef[start].replace(/ /g, "_") + ".html");
                        link.appendChild(currenttext);
                        div.appendChild(link);
                    }
                    else
                    {
                        currenttext = document.createTextNode(cat_reef[start-1]);
                        link = document.createElement("a");
                        link.setAttribute("href", cat_reef[start-1].replace(/ /g, "_") + ".html");
                        link.appendChild(currenttext);
                        div.appendChild(link);
                    }
                    mycurrent_cell.appendChild(div);
                    main_tr.appendChild(mycurrent_cell);
                    if(newCat)
                        start = start + 1;
                    j = j + span;
                }
                else {
                    if(ie_bug) {
                        mycurrent_cell =  document.createElement('<td colspan="' + span + '">');
                    } else {
                        mycurrent_cell = document.createElement("td");
                        mycurrent_cell.setAttribute("colspan",span);
                    }
                    mycurrent_cell.setAttribute("id","catrow");
                    main_tr.appendChild(mycurrent_cell);
                    j = j + span;
                }
            }
        
        
            main_tr=document.createElement("tr");
            mybody.appendChild(main_tr);
        }
    
        mycurrent_cell = document.createElement("td");
    
        mycurrent_cell.setAttribute("class", cell_size);
        link = document.createElement("a");
        link.setAttribute("href", link_reef[i]);
        if(ie_bug) {
            currentimg = document.createElement('<img class="' + 'selframe">');
        } else {
            currentimg = document.createElement("img");
            currentimg.setAttribute("class", "selframe");
        }
        currentimg.setAttribute("src", img_reef[i].replace("thumb", thumb_act));
        currentimg.setAttribute("width", img_width_act);
        currentimg.setAttribute("alt", name_reef[i]  + " - " + name_sci[i]);
        if(name_sci[i] != "")
            currentimg.setAttribute("title", name_reef[i] + " - " + name_sci[i]);
        else
            currentimg.setAttribute("title", name_reef[i]);
        link.appendChild(currentimg);
        mycurrent_cell.appendChild(link);
        main_tr.appendChild(mycurrent_cell);


        colpos++;
        if((colpos == numCol) || (i == (numpix - 1))) {
            colpos = 0;
            main_tr=document.createElement("tr");
            mybody.appendChild(main_tr);
    
            for(j = from; j <= i; j++) {
                var div="";
                if(ie_bug) {
                    mycurrent_cell = document.createElement('<td class="nameid">');
                    div = document.createElement('<div class="nameid">');
                }
                else {
                    mycurrent_cell = document.createElement("td");
                    mycurrent_cell.setAttribute("class","nameid");
                    div = document.createElement("div");
                    div.setAttribute("class","nameid");
                }
                mycurrent_cell.setAttribute("width", img_width);
                if(ie_bug) {
                    link = document.createElement('<a class="nameid">');
                }
                else {
                    link = document.createElement("a");
                    link.setAttribute("class","nameid");
                }
                link.setAttribute("href", link_reef[j]);
                currenttext = document.createTextNode(name_reef[j]);
                link.appendChild(currenttext);
                div.appendChild(link);
                mycurrent_cell.appendChild(div);
                main_tr.appendChild(mycurrent_cell);
        
            }
            from = i+1;
    
        }
    }

}


function hideLevel( _id) {
    var thisLevel = document.getElementById( _id );
    thisLevel.style.display = "none";
    thisLevel = document.getElementById( 'tree' + _id);
    thisLevel.className = "close";
}

function show( _id) {
    var thisLevel = document.getElementById( _id );
    if ( thisLevel.style.display == "none") {
        thisLevel.style.display = "block";
        thisLevel = document.getElementById( 'tree' + _id );
        thisLevel.className = "open";
    }
    else {
        hideLevel( _id);
    }
}

