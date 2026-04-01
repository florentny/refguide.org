

function renderNav() {
    var navRoot = document.getElementById('topnav-root');
    if (navRoot && window.TopNav && window.navItems) {
        ReactDOM.createRoot(navRoot).render(
            React.createElement(TopNav, { items: window.navItems })
        );
    }
}

function renderAccordion() {
    var accRoot = document.getElementById('accordion-root');
    if (accRoot && window.AccordionMenu && window.treeMenuData) {
        var root = ReactDOM.createRoot(accRoot);
        root.render(
            React.createElement(AccordionMenu, { data: window.treeMenuData })
        );
    }
}

function mainInit() {
    renderNav();
    renderAccordion();

    creategrid();

    var hash = location.hash;
    if (hash !== "") {
        goToByScroll(hash);
    }

    $(window).resize(creategrid);

    $scrollingDiv = $("#accordion-root");
    if ($scrollingDiv.length) {
        $divpos = Math.round($scrollingDiv.position().top - 15);
        $margin = 0;

        $(window).scroll(function() {
            doScroll();
        });
    }
}


function doScroll() {

    topz=$(window).scrollTop();
    heightz=$(window).height();
    bottomz=topz+heightz;
    heightDiv=$scrollingDiv.height() + 50;

    if(topz > $divpos) {
        if(heightDiv<heightz) {
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
                $margin=(topz - $divpos);
            }
            else {
                $scrollingDiv.stop().animate({marginTop:((topz + heightz) - (heightDiv+$divpos)) + "px"}, 500);
                $margin=(topz + heightz) - (heightDiv+$divpos);
            }
        }
    }
    else {
        $scrollingDiv.stop();
	$scrollingDiv.css("marginTop", "0px");
        $margin=0;
    }
}

function FamInit() {
    renderNav();
    renderAccordion();
    SpeciesInit();

    if ($.fn.button) {
        $(".buttonType").button();
        $(".buttonType1").button();
    }

    $scrollingDiv = $("#accordion-root");
    if ($scrollingDiv.length) {
        $divpos = Math.round($scrollingDiv.position().top - 15);
        $margin = 0;

        $(window).scroll(function() {
            doScroll();
        });
    }
}

function SpeciesInit() {
    renderNav();
}

function photoInit() {
    renderNav();
}

function resize() {
    $("#leftcolumn").height($(document).height() - $('#topsection').height());
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
        }, 2000);
}
