$(function(){
    $('#query').focus();
    $("#search_btn").click(function () {
        var search_content = $.trim($('#query').val());
        if(search_content){
            $.cookie("queryHistary", search_content);
            window.location.href = "/seacher.html";
        }
    });
    document.onkeydown = function(event){
        //enter键
        if(event.keyCode == 13){
            var search_content = $.trim($('#query').val());
            if(search_content){
                $.cookie("queryHistary", search_content);
                window.location.href = "/seacher.html";
            }
        }
        // //中文
        // if(event.keyCode == 229){
        //     window.opener.location.href = "http://10.79.10.253:8080/seacher.html"
        // }
        // //英文大写
        if(event.keyCode <= 90 && event.keyCode >= 65){
            var testHtml = '<div class="row" style="height: 20px; background:#F1F1F1"></div> ' +
                '<div class="col-md-1" style="background:#F1F1F1"> ' +
                '<a href="/"><img src="img/seacher_logo_small.png"></a> ' +
                '</div> ' +
                '<div class="col-md-7" > ' +
                '<div class="form-group"> ' +
                '<div class="input-group"> ' +
                '<input id = "query" type="text" class="typeahead form-control input-private-lg" autocomplete="off" name="query" value maxlength="255"> ' +
                '<span class="input-group-btn"> ' +
                '<button class="btn btn-primary" id = "search_btn" style="letter-spacing: 1px;"> ' +
                '<span class="glyphicon glyphicon-search" aria-hidden="true"></span> 搜索 ' +
                '</button> </span> </div> </div> </div>'
            $('#test').html(testHtml);
            // var keyValue = String.fromCharCode(event.keyCode);
            // $('#query').val(keyValue);
            // $.cookie("queryHistary", keyValue);
            // window.location.href = "/seacher.html";
        }
        // //数字
        // if(event.keyCode <= 57 && event.keyCode >= 48){
        //     var keyValue = String.fromCharCode(event.keyCode);
        //     $('#query').val(keyValue);
        //     $.cookie("queryHistary", keyValue);
        //     window.location.href = "/seacher.html";
        // }
    }
});