var Transcripts = Transcripts || {};
var content = content || {};
$(function () {
    content._seach_suggest = $('#seach_suggest');
    content._seach_message = $('#seach_message');
    content._seach_content = $('#seach_content');

    var queryHistary = $.cookie('queryHistary');
    if (queryHistary) {
        $('.typeahead').typeahead('val', queryHistary);
        $('#user_input').val(queryHistary);
        Transcripts.sendQuest();
    } else {
        $('#seach_content').html("<p>请输入内容后按\"回车键\"或点击\"搜索\"按钮进行检索</p>");
    }
    $("#search_button").click(function () {
        $('.typeahead').typeahead('close');
        $.cookie('queryStart', 0);
        $.cookie('querySortFiled', "score");
        Transcripts.sendQuest();
    });
    document.onkeydown = function (event) {
        if (event.keyCode == 13) {
            var isFocus = $("#user_input").is(":focus");
            if (true == isFocus) {
                $('.typeahead').typeahead('close');
                $.cookie('queryStart', 0);
                $.cookie('querySortFiled', "score");
                Transcripts.sendQuest();
            }
            var isQuestionFocus = $("#question").is(":focus");
            if (isQuestionFocus) {
                autoAnswer();
            }
            return false;
        }
    };
    $("#user_input").bind("blur", function () {
        if (!$('#user_input').val()) {
            var queryHistary = $.cookie('queryHistary');
            if (queryHistary) {
                $('#user_input').val(queryHistary);
            }
        }
    });
    bindSortFieldEvent();

    $("#submitQuestionButton").click(function () {
        autoAnswer();
    });

});

function autoAnswer() {
    var question = $.trim($("#question").val())
    if (question != "") {
        $("#question").val("");
    } else {
        return;
    }
    $.ajax("/ruclaw/autoAnswer", {
        type: "POST",
        dataType: "text",
        data: {question: question},
        success: function (answer) {
            addAnswer(question, answer);
        },
        error: function (request, textStatus, errorThrown) {
            addAnswer(question, "服务器出错！");
        }
    });
}

function addAnswer(question, answer) {
    var content = $("#autoAnswer").val();
    content += "Q: " + question + "\n";
    content += "A: " + answer + "\n";
    $("#autoAnswer").val(content);
}

Transcripts.cleanContent = function () {
    content._seach_content.empty();
    content._seach_message.empty();
    content._seach_suggest.empty();
};

Transcripts.sendQuest = function () {
    var search_content = $.trim($('#user_input').val());
    if (search_content) {
        $('#title').html(search_content + " - Ruclaw 搜索");
        var queryStart = $.cookie('queryStart');
        var querySortFiled = $.cookie('querySortFiled');
        if (querySortFiled == 'null') {
            querySortFiled = getSelectSortField();
        } else if (!querySortFiled) {
            querySortFiled = getSelectSortField();
        }
        if (queryStart) {
            Transcripts.getResultList(search_content, parseInt(queryStart), 10, querySortFiled);
        } else {
            Transcripts.getResultList(search_content, 0, 10, querySortFiled);
        }
    } else {
        window.location.href = "/index.html";
    }
};


Transcripts.getResultList = function (query, start, rows, sortField) {
    $.cookie('queryStart', start);
    $.cookie('queryHistary', query);
    $.cookie('querySortFiled', sortField);
    activeSortFiled(sortField);
    $('#fade_mask').show();
    var parm = {
        query_string: $.trim(query),
        start: start,
        rows: rows,
        sortField: sortField
    };
    $.ajax("/ruclaw/query", {
        type: "POST",
        dataType: "json",
        data: parm || {},
        success: function (resultInfo) {
            $('#fade_mask').hide();
            Transcripts.showContent(resultInfo, start, rows);
        },
        error: function (request, textStatus, errorThrown) {
            $('#fade_mask').hide();
        }
    });
};

Transcripts.showContent = function (resultInfo, start, rows) {
    initBottomIndex(start + 1, resultInfo.numFound);
    if (resultInfo.numFound > 0) {
        Transcripts.addviewList(resultInfo, start, rows);
    } else {
        $('#seach_content').html("<p>很抱歉没有找到任何结果</p>");
        $('#adv').empty();
    }
    var resultNum = resultInfo.numFound.toString();
    var costTime = resultInfo.QTime;
    var message_html = "";
    var numString = "";
    var count = 0;
    for (var i = resultNum.length - 1; i >= 0; i--) {
        count++;
        if (count == 4) {
            numString = resultNum[i] + ',' + numString;
            count = 0;
        } else {
            numString = resultNum[i] + numString;
        }
    }
    if (start == 0) {
        message_html += "<div class='col-md-12 col-xs-12' style = 'color: #808080;font-size: 13px;font-family: arial;'>找到约" + numString + "条结果（用时" + costTime + "毫秒）</div>";
    } else {
        message_html += "<div class='col-md-12 col-xs-12' style = 'color: #808080;font-size: 13px;font-family: arial;'>找到约" + numString + "条结果，以下是第" + parseInt(start + 1) + "页（用时" + costTime + "毫秒）</div>";
    }
    $('#seach_message').html(message_html);
    $('#seach_suggest').empty();
}

Transcripts.addviewList = function (resultInfo, start, rows) {
    var content_html = getContent(resultInfo.resultList);
    $('#seach_content').html(content_html);
    $(".lawTitle").click(function () {
        getRecommend($(this).attr("id"));
    });
    if (resultInfo.resultList.length > 0) {
        var recommendId = resultInfo.resultList[0].id;
        getRecommend(recommendId);
    } else {
        $("#recommend").hide();
    }
};

function bindSortFieldEvent() {
    $(".sortFiled").click(function () {
        if ($(this).hasClass("active")) {
            return;
        }
        clearActiveSortFiled();
        var queryStart = $.cookie('queryStart');
        var search_content = $.cookie('queryHistary');
        querySortFiled = $(this).attr('id');
        Transcripts.getResultList(search_content, parseInt(queryStart), 10, querySortFiled);
    });
}

function clearActiveSortFiled() {
    var index = $(".sortFiled");
    for (var i = 0; i < index.length; i++) {
        $($(".sortFiled")[i]).removeClass('active');
    }
}

function activeSortFiled(sortField) {
    clearActiveSortFiled();
    var index = $(".sortFiled");
    for (var i = 0; i < index.length; i++) {
        if ($($(".sortFiled")[i]).attr("id") == sortField) {
            $($(".sortFiled")[i]).addClass('active');
            break;
        }
    }
}

function getSelectSortField() {
    var index = $(".sortFiled");
    for (var i = 0; i < index.length; i++) {
        if ($($(".sortFiled")[i]).hasClass('active')) {
            return $($(".sortFiled")[i]).attr('id');
        }
    }
    return "score";
}

// function refreshRecommendNewPage(queryResultList) {
//     if(queryResultList.length > 0){
//         var recommendId = queryResultList[0].id;
//         getRecommend(recommendId);
//     }else {
//         $("#recommend").hide();
//     }
// }

// function refreshAdsAndEvaluation(resultInfo, start, rows) {
//     var rand_num_ads = parseInt(Math.random() * 3);
//     var fondNum = parseInt(resultInfo.numFound);
//     var indexNum = rows;
//     if ((start + 1) * rows > fondNum) {
//         indexNum = fondNum - start * rows;
//     }
//     var html_ads = "";
//     for (var i = 0; i < rand_num_ads; i++) {
//         var rand_index = parseInt(Math.random() * indexNum);
//         html_ads += "<div class='panel panel-default'>" +
//             "<div class='panel-heading'>自动问答</div>" +
//             "<div class='panel-body' style='text-align: left;overflow :auto'>" +
//             "<a href=" + resultInfo.resultList[rand_index].contentUrl + " target='_blank' style='color: #666'><p>" + resultInfo.resultList[rand_index].content + "</p></a>" +
//             "</div></div>";
//     }
//     if(resultInfo.resultList.length > 0){
//         var recommendId = resultInfo.resultList[0].id;
//         var resultRecommend = getRecommend(recommendId);
//         html_ads += "<div class='panel panel-default'>" +
//             "<div class='panel-heading'>推荐阅读</div>" +
//             "<div class='panel-body' style='overflow :auto'>";
//         for (var i = 0; i < resultRecommend.length; i++) {
//             html_ads += "<a href=" + resultInfo.resultList[i].contentUrl + " target='_blank'><p>" + resultInfo.resultList[i].title + "</p></a>";
//         }
//         html_ads += "</div></div>";
//     }
//
//     return html_ads;
// }

function getRecommend(lawId) {
    $("#recommend").hide();
    $.ajax("/ruclaw/recommend", {
        type: "GET",
        dataType: "json",
        data: {
            id: lawId,
            limitNum: 5
        },
        success: function (recommendList) {
            if (recommendList.length > 0) {
                $("#recommend").show();
                var recommentContentHtml = "";
                for (var i = 0; i < recommendList.length; i++) {
                    recommentContentHtml += "<a href=" + recommendList[i].contentUrl + " target='_blank' style='color: #545454; font-size: 13px;'><p>" + recommendList[i].title + "</p></a>";
                }
                $("#recommendContent").html(recommentContentHtml);
            }
        }, error: function (request, textStatus, errorThrown) {
            $("#recommendContent").html("<p>服务器出错！<p>");
        }
    });
}

function getContent(resultList) {
    var html = "";
    for (var i = 0; i < resultList.length; i++) {
        html += "<div class='row' style='margin-bottom: 15px'><div class='col-md-12'>" +
            "<h4 style='margin-bottom: 5px; font-family: arial;' class='lawTitle' id='" + resultList[i].id + "'>" +
            "<a href=" + resultList[i].contentUrl + " target='_blank' style='color:#1a0dab'>" + resultList[i].title + "</a></h4>" +
            "<p style='font-size: 13px; font-family: arial;line-height: 1.4; word-wrap: break-word; word-break: break-word; margin-top: 0; margin-bottom: 3px;color: #545454;'>" + resultList[i].content + "</p>" +
            "<p class='pull-left' style='margin-top: 0;margin-bottom: 3px; font-family: arial;'>" +
            "<span class='label label-default' style='background-color: white;color:#545454; padding-left: 0;padding-right.9em; display:block;float:left;'>[发布单位]" + resultList[i].department + "</span>" +
            "<span class='label label-default' style='background-color: white;color:#545454; padding-left: 0;padding-right.9em; display:block;float:left;'>[发文字号]" + resultList[i].release_number + "</span>" +
            "<span class='label label-default' style='background-color: white;color:#545454; padding-left: 0;padding-right.9em; display:block;float:left;'>[发布日期]" + resultList[i].release_date + "</span>" +
            "<span class='label label-default' style='background-color: white;color:#545454; padding-left: 0;padding-right.9em; display:block;float:left;'>[生效日期]" + resultList[i].implement_date + "</span>" +
            "<span class='label label-default' style='background-color: white;color:#545454; padding-left: 0;padding-right.9em; display:block;float:left;'>[法规类别]" + resultList[i].category + "</span>" +
            "<span class='label label-default' style='background-color: white;color:#545454; padding-left: 0;padding-right.9em; display:block;float:left;'>[法规级别]" + resultList[i].level + "</span>" +
            "<span class='label label-default' style='background-color: white;color:#545454; padding-left: 0;padding-right.9em; display:block;float:left;'>[时效性]" + resultList[i].timeless + "</span>" +
            "</p>" +
            "<p class='pull-left' style='margin: 0;padding: 0;font-size: 14px;line-height: 1.4;'>" +
            "<a href=" + resultList[i].graphUrl + " target='_blank' style='color:#006621;word-break:break-all;'>" + resultList[i].graphUrl + "</a></p>" +
            "</div></div>";
    }
    return html;
}

function initBottomIndex(startIndex, rowSize) {
    if (rowSize == 0) {
        return;
    }
    var totlePage = Math.ceil(rowSize / 10);
    var html = "<nav><ul class='pagination'>";
    if (startIndex != 1) {
        html += "<li><a href='#' aria-label='Previous'>" +
            "<span aria-hidden='true' class='indexP'>上一页</span></a></li>";
    }
    var up_num = 0;
    var begin = startIndex - 5;
    if (begin < 1) {
        begin = 1;
    }
    for (var i = begin; i < startIndex; i++) {
        html += "<li class='index' id = '" + i + "'><a href='#'>" + i + "</a></li>";
        up_num++;
    }
    for (var i = startIndex; i < startIndex + 11 - up_num && i <= totlePage; i++) {
        if (i == startIndex) {
            html += "<li class='index active' id = '" + i + "'><a href='#'>" + i + "</a></li>";
        } else {
            html += "<li class='index ' id = '" + i + "'><a href='#'>" + i + "</a></li>";
        }
    }
    if (startIndex != totlePage) {
        html += "<li>" +
            "<a href='#' aria-label='Next'><span aria-hidden='true' class ='indexN'>下一页</span>" +
            "</a></li></ul></nav>";
    }
    if (totlePage <= 1) {
        html = "";
    }
    $('#nav').html(html);
    bindIndexEvent();
    bindNextIndexEvent(rowSize);
    bindPreviousIndexEvent(rowSize);
}

function bindIndexEvent() {
    $(".index").click(function () {
        clearActive();
        var message_index = parseInt($(this).attr('id')) - 1;
        var search_content = $.cookie('queryHistary');
        var sortFiled = getSelectSortField();
        $('#user_input').val(search_content);
        if (search_content) {
            var start = message_index;
            var rows = 10;
            Transcripts.getResultList(search_content, start, rows, sortFiled);
        }
    });
}

function bindNextIndexEvent(rowSize) {
    $(".indexP").click(function () {
        previousActive(rowSize);
    });
}

function bindPreviousIndexEvent(rowSize) {
    $(".indexN").click(function () {
        nextActive(rowSize);
    });
}

function clearActive() {
    var index = $(".index");
    for (var i = 0; i < index.length; i++) {
        $($(".index")[i]).removeClass('active');
    }
}

function nextActive(rowSize) {
    var index = $(".index");
    for (var i = 0; i < index.length; i++) {
        if ($($(".index")[i]).hasClass('active')) {
            $($(".index")[i]).removeClass('active');
            var search_content = $.cookie('queryHistary');
            var sortField = getSelectSortField();
            $('#user_input').val(search_content);
            if (i + 1 >= index.length) {
                var message_index = parseInt($($(".index")[i]).attr('id'));
                Transcripts.getResultList(search_content, message_index, 10, sortField);
                break;
            } else {
                var message_index = parseInt($($(".index")[i + 1]).attr('id')) - 1;
                Transcripts.getResultList(search_content, message_index, 10, sortField);
                break;
            }
        }
    }
}

function previousActive(rowSize) {
    var index = $(".index");
    for (var i = 0; i < index.length; i++) {
        if ($($(".index")[i]).hasClass('active')) {
            $($(".index")[i]).removeClass('active');
            var search_content = $.cookie('queryHistary');
            var sortFiled = getSelectSortField();
            $('#user_input').val(search_content);
            if (i - 1 < 0) {
                var message_index = parseInt($($(".index")[0]).attr('id')) - 1;
                Transcripts.getResultList(search_content, message_index, 10, sortFiled);
                break;
            } else {
                var message_index = parseInt($($(".index")[i - 1]).attr('id')) - 1;
                Transcripts.getResultList(search_content, message_index, 10, sortFiled);
                break;
            }
        }
    }
}



