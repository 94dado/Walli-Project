function Arrange(boolset) {
    var arraychild = $("#features_page").children();
    for (i = 0; i < arraychild.length; i++) {
        if ($(arraychild[i]).hasClass("features_pg")) {
            var arraychildren = $(arraychild[i]).children();
            if (boolset) {
                if ($(arraychildren[1]).hasClass("text_features_pg")) {
                    $(arraychildren[0]).before(arraychildren[1]);
                }
            } else {
                if ($(arraychildren[0]).parent().hasClass("even") && $(arraychildren[0]).hasClass("text_features_pg")) {
                    $(arraychildren[1]).insertBefore(arraychildren[0]);
                }
            }
        }
    }
}

// scroll the one page whit effect
function Scrolling() {
    if (location.pathname.replace(/^\//, '') == this.pathname.replace(/^\//, '') && location.hostname == this.hostname) {
        var target = $(this.hash);
        target = target.length ? target : $('[name=' + this.hash.slice(1) + ']');
        var pixels = -50;
        if ($(window).width() <= 550) {
            OpenAndClose();
            pixels = -70;
        }
        if (target.length) {
            $('html, body').animate({
                scrollTop: target.offset().top + pixels
            }, 1000);
            return false;
        }
    }
}

// check the size of the screen
function Resize() {
    if ($(window).width() <= 550) {
        $(".ct").hide();
        $(".tm").hide();
        $(".ft").hide();
        $(".ab").hide();
        Arrange(true);
    } else {
        $(".ct").show();
        $(".tm").show();
        $(".ft").show();
        $(".ab").show();
        Arrange(false);
    }
}

// menu effect function menager
function OpenAndClose() {
    if ($('.hamburger--elastic').attr("class").indexOf("is-active") == -1) {
        $('.hamburger--elastic').addClass("is-active");
        $('header').animate({
            height: '220px'
        }, 100);
        $(".ct").show(500);
        $(".tm").show(400);
        $(".ft").show(300);
        $(".ab").show(200);
    } else {
        $('.hamburger--elastic').removeClass("is-active");
        $('header').animate({
            height: '70px'
        }, 300);
        $(".ct").hide(200);
        $(".tm").hide(300);
        $(".ft").hide(400);
        $(".ab").hide(500);
    }
}

$(document).ready(function () {
    // resizing and the start and in the while of running
    Resize();
    $(window).resize(Resize);
    // smoothscroll initialization
    $('a[href*="#"]:not([href="#"])').on('click', Scrolling);
    // menu effect
    $('.hamburger-box').on('click', OpenAndClose);
});
