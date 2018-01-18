var Transcripts = Transcripts || {};
var content = content || {};
$(function () {
    content._seach_suggest = $('#seach_suggest');
    content._seach_message = $('#seach_message');
    content._seach_content = $('#seach_content');
    content._nav = $('#nav');
    content._adv = $('#adv');
    content._ad1 = $('#ad1');
    content._ad2 = $('#ad2');
    content._ev1 = $('#ev1');

    var queryHistary = $.cookie('queryHistary');
    if (queryHistary) {
        $('.typeahead').typeahead('val', queryHistary);
        $('#user_input').val(queryHistary);
        Transcripts.sendQuest();
    } else {
        $('#seach_content').html("<p>请按\"回车键\"或点击\"搜索\"按钮进行检索</p>");
    }
    $("#search_button").click(function () {
        $('.typeahead').typeahead('close');
        $.cookie('queryStart', 0);
        Transcripts.sendQuest();
    });
    document.onkeydown = function (event) {
        if (event.keyCode == 13) {
            $('.typeahead').typeahead('close');
            $.cookie('queryStart', 0);
            Transcripts.sendQuest();
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
});

Transcripts.cleanContent = function () {
    content._seach_content.empty();
    content._seach_message.empty();
    content._seach_suggest.empty();
    content._nav.empty();
    content._ev1.empty();
    content._nav.empty();
    content._adv.empty();
    content._ad1.empty();
    content._ad2.empty();
};

Transcripts.sendQuest = function () {
    var search_content = $.trim($('#user_input').val());
    if (search_content) {
        var queryStart = $.cookie('queryStart');
        if (queryStart) {
            Transcripts.getResultList(search_content, parseInt(queryStart), 10);
        } else {
            Transcripts.getResultList(search_content, 0, 10);
        }
    } else {
        window.location.href = "/index.html";
    }
};

Transcripts.getResultList = function (query, start, rows) {
    $.cookie('queryStart', start);
    $.cookie('queryHistary', query);
    $('#fade_mask').show();
    var parm = {
        query_string: $.trim(query),
        start: start,
        rows: rows
    };
    $.ajax("/solr/query", {
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
    var ads_evaluation_html = refreshAdsAndEvaluation(resultInfo, start, rows);
    $('#adv').html(ads_evaluation_html);
};

function refreshAdsAndEvaluation(resultInfo, start, rows) {
    var rand_num_ads = parseInt(Math.random() * 3);
    var fondNum = parseInt(resultInfo.numFound);
    var indexNum = rows;
    if ((start + 1) * rows > fondNum) {
        indexNum = fondNum - start * rows;
    }
    var html_ads = "";
    for (var i = 0; i < rand_num_ads; i++) {
        var rand_index = parseInt(Math.random() * indexNum);
        html_ads += "<div class='panel panel-default'>" +
            "<div class='panel-heading'>图谱关系</div>" +
            "<div class='panel-body' style='text-align: left;overflow :auto'>" +
            "<a href=" + resultInfo.resultList[rand_index].url + " target='_blank' style='color: #666'><p>" + resultInfo.resultList[rand_index].content + "</p></a>" +
            "</div></div>";
    }

    html_ads += "<div class='panel panel-default'>" +
        "<div class='panel-heading'>推荐阅读</div>" +
        "<div class='panel-body' style='overflow :auto'>";
    for (var i = 0; i < 3; i++) {
        var rand_index = parseInt(Math.random() * indexNum);
        html_ads += "<a href=" + resultInfo.resultList[rand_index].url + " target='_blank'><p>" + resultInfo.resultList[rand_index].title + "</p></a>";
    }
    html_ads += "</div></div>";
    return html_ads;
}

function getContent(resultList) {
    var html = "";
    for (var i = 0; i < resultList.length; i++) {
        html += "<div class='row' style='margin-bottom: 15px'><div class='col-md-12'>" +
            "<h4 style='margin-bottom: 5px; font-family: arial;'><a href=" + resultList[i].url + " target='_blank' style='color:#1a0dab'>" + resultList[i].title + "</a></h4>" +
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
            "<p class='pull-left' style='margin: 0;padding: 0;font-size: 14px;line-height: 1.4; '>" +
            "<a href=" + resultList[i].url + " target='_blank' style='color:#006621;word-break:break-all;'>" + resultList[i].url + "</a></p>" +
            "</div></div>";
    }
    return html;
}

function initBottomIndex(startIndex, rowSize) {
    if (rowSize == 0) {
        return;
    }
    var totlePage = parseInt(rowSize / 10) + 1;
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
        $('#user_input').val(search_content);
        if (search_content) {
            var start = message_index;
            var rows = 10;
            Transcripts.getResultList(search_content, start, rows);
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
            $('#user_input').val(search_content);
            if (i + 1 >= index.length) {
                var message_index = parseInt($($(".index")[i]).attr('id'));
                Transcripts.getResultList(search_content, message_index, 10);
                break;
            } else {
                var message_index = parseInt($($(".index")[i + 1]).attr('id')) - 1;
                Transcripts.getResultList(search_content, message_index, 10);
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
            $('#user_input').val(search_content);
            if (i - 1 < 0) {
                var message_index = parseInt($($(".index")[0]).attr('id')) - 1;
                Transcripts.getResultList(search_content, message_index, 10);
                break;
            } else {
                var message_index = parseInt($($(".index")[i - 1]).attr('id')) - 1;
                Transcripts.getResultList(search_content, message_index, 10);
                break;
            }
        }
    }
}



