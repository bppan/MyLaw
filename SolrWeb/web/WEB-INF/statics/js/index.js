$(function () {
    $('#query').focus();
    $.cookie('querySortFiled', null);
    $.cookie("queryHistary", null);
    $.cookie('queryStart', 0);
    $("#search_btn").click(function () {
        var search_content = $.trim($('#query').val());
        if (search_content) {
            $.cookie("queryHistary", search_content);
            window.location.href = "/ruclaw.html";
        }
    });
    document.onkeydown = function (event) {
        //enter键
        if (event.keyCode == 13) {
            var search_content = $.trim($('#query').val());
            if (search_content) {
                $.cookie("queryHistary", search_content);
                window.location.href = "/ruclaw.html";
            }
        }
    }
});