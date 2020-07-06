

function mainInit() {
    
    $(function() {
        $( "#accordion-top" ).accordion({
            autoHeight: false,
            navigation: false,
            animated: "bounceslide",
            collapsible: true,
            header: 'h3',
            active: activeSel,
            change: function(event, ui) {
                doScroll();
            }
        });
    });
    
    $( "a", ".ba" ).button();
    $( "ul.topnav > li > ul.subnav").button();
    
    $("ul.subnav").parent().append("<span></span>"); //Only shows drop down trigger when js is enabled (Adds empty span tag after ul.subnav*)  
  
    $("li.ba").hover(function() {
        $(this).parent().find("ul.subnav").slideUp("fast");
    });
    
    $("ul.subnav").parent().find(".ba").hover(function() {
        $(this).parent().parent().find("ul.subnav").not($(this).parent().find("ul.subnav")).slideUp("fast");
        $(this).parent().find("ul.subnav").show();//slideDown("slow");

    }).hover(function() {
        $(this).addClass("subhover");
    }, function(){
        $(this).removeClass("subhover");
    });
    
    $("#contentcolumn").hover(function() {
        $("ul.subnav").slideUp("slow");
    });
    
    $("#leftcolumn").hover(function() {
        $("ul.subnav").slideUp("slow");
    });
    
    $("ul.subnav").hover(function() {
        $(this).hover(function() {
            }, function(){
                $(this).slideUp("slow");
            });
    });
   

    initMenu();
    
    $("#small").click(function(event){
        sizesmall();
        $("#small div").removeClass("arrow2").addClass("ui-icon ui-icon-check arrow");
        $("#regular div").removeClass("ui-icon ui-icon-check arrow").addClass("arrow2");
        $("#large div").removeClass("ui-icon ui-icon-check arrow").addClass("arrow2");
        $("#huge div").removeClass("ui-icon ui-icon-check arrow").addClass("arrow2");

    });
    
    $("#regular").click(function(event){
        sizereg();
        $("#small div").removeClass("ui-icon ui-icon-check arrow").addClass("arrow2");
        $("#regular div").removeClass("arrow2").addClass("ui-icon ui-icon-check arrow");
        $("#large div").removeClass("ui-icon ui-icon-check arrow").addClass("arrow2");
        $("#huge div").removeClass("ui-icon ui-icon-check arrow").addClass("arrow2");

    });
    
    $("#large").click(function(event){
        sizebig1();
        $("#large div").removeClass("arrow2").addClass("ui-icon ui-icon-check arrow");
        $("#regular div").removeClass("ui-icon ui-icon-check arrow").addClass("arrow2");
        $("#small div").removeClass("ui-icon ui-icon-check arrow").addClass("arrow2");
        $("#huge div").removeClass("ui-icon ui-icon-check arrow").addClass("arrow2");

    });
    
    $("#huge").click(function(event){
        sizebig();
        $("#huge div").removeClass("arrow2").addClass("ui-icon ui-icon-check arrow");
        $("#regular div").removeClass("ui-icon ui-icon-check arrow").addClass("arrow2");
        $("#large div").removeClass("ui-icon ui-icon-check arrow").addClass("arrow2");
        $("#small div").removeClass("ui-icon ui-icon-check arrow").addClass("arrow2");

    });
    
    
    creategrid();
    
    if(img_width == 160) {
        $("#small div").removeClass("arrow2").addClass("ui-icon ui-icon-check arrow");
    }
    else if(img_width == 240) {
        $("#regular div").removeClass("arrow2").addClass("ui-icon ui-icon-check arrow");
    }
    else if(img_width == 316) {
        $("#large div").removeClass("arrow2").addClass("ui-icon ui-icon-check arrow");
    }
    else if(img_width == 400) {
        $("#huge div").removeClass("arrow2").addClass("ui-icon ui-icon-check arrow");
    }
    
    var hash = location.hash
    
    if(hash != "") {
        goToByScroll(hash);
    }
   
    $(window).resize(creategrid);
    
//    window.setTimeout(function() {
//            $("#topsection").removeClass("banner1");
//            $("#topsection").addClass("banner2");
//        }, 2000);
//    

    $scrollingDiv = $("#accordion-top");
    $divpos = Math.round($scrollingDiv.position().top - 15);
    $margin=0;
    
    $(window).scroll(function() {
       doScroll();
    });
            
}


function doScroll() {
   
    topz=$(window).scrollTop();
    heightz=$(window).height();
    bottomz=topz+heightz;
    heightDiv=$scrollingDiv.height() + 50;

    if(topz > $divpos) {
        if(heightDiv<heightz) {
          //$scrollingDiv.css("marginTop", (topz - $divpos) + "px");
          $scrollingDiv.stop().animate({marginTop:(topz - $divpos) + "px"}, 500);
          $margin=(topz - $divpos);
        }
        else {
            topv=(($divpos - topz) + $margin);
            bottomv=(topv+heightDiv)-heightz;
            if((topv < 0) && (bottomv > 0)) {
                    // Nothing 
            }
            else if(topv > 0) {
                $scrollingDiv.stop().animate({marginTop:(topz - $divpos) + "px"}, 500);
                //$scrollingDiv.css("marginTop", (topz - $divpos) + "px");
                $margin=(topz - $divpos);
            }
            else {
                $scrollingDiv.stop().animate({marginTop:((topz + heightz) - (heightDiv+$divpos)) + "px"}, 500);
                //$scrollingDiv.css("marginTop", ((topz + heightz) - (heightDiv+$divpos)) + "px");
                $margin=(topz + heightz) - (heightDiv+$divpos);
            }
        }
    }
    else {
        $scrollingDiv.stop();
	$scrollingDiv.css("marginTop", "0px");
        $margin=0;
    }
    //$("div.catheader > a").text($divpos + " / " + topz + " / " + $margin + " / " + (($divpos - topz) + $margin));
}

function FamInit() {
	$(function() {
        $( "#accordion-top" ).accordion({
            autoHeight: false,
            navigation: false,
            animated: "bounceslide",
            collapsible: true,
            header: 'h3',
            active: 999
        });
        });
	SpeciesInit();
	initMenu();
	
	$(".buttonType").button();
	$(".buttonType1").button();
	
    $scrollingDiv = $("#accordion-top");
    $divpos = Math.round($scrollingDiv.position().top - 15);
    $margin=0;
    
	$(window).scroll(function() {
       doScroll();
    });
}

function SpeciesInit() {

    $( "a", ".ba" ).button();
    $( "ul.topnav > li > ul.subnav").button();
    
  
       $("li.ba").hover(function() {
        $(this).parent().find("ul.subnav").slideUp("fast");
    });
    
    $("ul.subnav").parent().find(".ba").hover(function() {
        $(this).parent().parent().find("ul.subnav").not($(this).parent().find("ul.subnav")).slideUp("fast");
        $(this).parent().find("ul.subnav").show();//slideDown("slow");

    }).hover(function() {
        $(this).addClass("subhover");
    }, function(){
        $(this).removeClass("subhover");
    });
    
    $("#contentcolumn").hover(function() {
        $("ul.subnav").slideUp("slow");
    });
    
    $("#leftcolumn").hover(function() {
        $("ul.subnav").slideUp("slow");
    });
    
    $("ul.subnav").hover(function() {
        $(this).hover(function() {
            }, function(){
                $(this).slideUp("slow");
            });
    });
    


}

function photoInit() {
    SpeciesInit();

//    var newWidth = $('#gallery img').css('width');
//    var newWidth2 = $('#content').css('width');
//    $('#content').css('width' ,newWidth); 
//    newWidth2 = $('#content').css('width');
//    newWidth2 = $('#content').css('width');
    


}

function resize() {
    $("#leftcolumn").height($(document).height() - $('#topsection').height());

}

function initMenu() {
    $('ul.menusec1 > li > ul').hide();
    $('ul.menusec1 li a').click(
        function() {
            $(this).next().slideToggle('fast');
        }
        );
    $('ul.menusecopen').show();
    
    
}

function goToByScroll(id) { 
    offset = $(id).offset();
    if(offset != null) {
        if(offset.top >300)
            $('html,body').animate({scrollTop: ($(id).offset().top - 10)},'slow');
    }
    
    $.expr[":"].econtains = function(obj, index, meta, stack){
        return (obj.textContent || obj.innerText || $(obj).text() || "") == meta[3];
    }
    
    sel = id.replace(/#/g, "").replace(/_/g, " ");
    catline = ".catheader:econtains('" + sel + "')";
    sel = ".catheader:econtains('" + sel + "') a";
    $(catline).css("border-color", "red");
    $(sel).css("color", "red");
    window.setTimeout(function() {
            $(catline).css("border-color", "#dcd637");
            $(sel).css("color", "#dcd637");
            //$(sel).hover.css("color", "black");
        }, 2000);

    
}

 

     
     