$(function(){
    //Transcripts.adaptScreen();
    alert("okay");
    var parm ={
        'name':'潘北平'
    };
    $.ajax("/hello/getAjax", {
        type: "POST",
        dataType: "json",
        data:parm||{},
        success: function (resultInfo) {
            alert(resultInfo.name);
        },
        error: function (request, textStatus, errorThrown) {
            alert("服务器出错！");
        }
    });
})