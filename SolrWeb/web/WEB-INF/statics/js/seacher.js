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

    var search_content = $.trim($('#user_input').val());
    if (search_content) {
        $.cookie('queryHistary', search_content);
    }

    var queryHistary = $.cookie('queryHistary');
    $('#user_input').focus();
    if (queryHistary) {
        $('#user_input').val(queryHistary);
        Transcripts.cleanContent();
        $('#seach_content').html("<p>请按\"回车键\"或点击\"搜索\"按钮进行检索</p>");
    }
    $("#search_btn").click(function () {
        Transcripts.sendQuest();
    });
    document.onkeydown = function (event) {
        if (event.keyCode == 13) {
            Transcripts.sendQuest();
        }
    }
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
        $.cookie('queryHistary', search_content);
        Transcripts.getResultList(search_content, 0, 10);
    } else {
        // window.location.href = "/index.html";
    }
};

Transcripts.getResultList = function (query, start, rows) {
    var parm = {
        query_string: query,
        start: start,
        rows: rows
    };
    $.ajax("/solr/query", {
        type: "POST",
        dataType: "json",
        data: parm || {},
        success: function (resultInfo) {
            initBottomIndex(start + 1, resultInfo.numFound);
            if (resultInfo.numFound > 0) {
                Transcripts.addviewList(resultInfo);
            } else {
                $('#seach_content').empty();
                $('#seach_content').html("<p>很抱歉没有找到任何结果</p>");
                $('#adv').empty();
            }
            $('#seach_message').empty();
            var resultNum = resultInfo.numFound;
            var costTime = resultInfo.QTime;
            var message_html = ""
            if (start == 0) {
                message_html += "<div class='col-md-12' style = 'color: #808080;'>找到约" + resultNum + "条结果（用时" + costTime + "毫秒）</div><hr>";
            } else {
                message_html += "<div class='col-md-12' style = 'color: #808080;'>找到约" + resultNum + "条结果，以下是第" + parseInt(start + 1) + "页（用时" + costTime + "毫秒）</div><hr>";
            }
            $('#seach_message').html(message_html);
            $('#seach_suggest').empty();
        },
        error: function (request, textStatus, errorThrown) {
        }
    });
};

Transcripts.addviewList = function (resultInfo) {
    $('#seach_content').empty();
    var content_html = getContent(resultInfo.resultList);
    $('#seach_content').html(content_html);
    $('#adv').empty();
    var ads_evaluation_html = refreshAdsAndEvaluation(resultInfo);
    $('#adv').html(ads_evaluation_html);
};

function refreshAdsAndEvaluation(resultInfo) {
    var rand_num_ads = parseInt(Math.random() * (2 - 0 + 1));
    var html_ads = "";
    for (var i = 0; i < rand_num_ads; i++) {
        var rand_index = parseInt(Math.random() * (9 - 0 + 1));
        html_ads += "<div class='panel panel-default'>" +
            "<div class='panel-heading'>Ads</div>" +
            "<div class='panel-body' style='text-align: left;'>" +
            "<a href=" + resultInfo.resultList[rand_index].url + " target='_blank'><p><font color='#666'>" + resultInfo.resultList[rand_index].content + "...</font></p></a>" +
            "</div></div>";
    }

    html_ads += "<div class='panel panel-default'>" +
        "<div class='panel-heading'>Recommendation</div>" +
        "<div class='panel-body'>";
    for (var i = 0; i < 3; i++) {
        var rand_index = parseInt(Math.random() * (9 - 0 + 1));
        html_ads += "<a href=" + resultInfo.resultList[rand_index].url + " target='_blank'><p>" + resultInfo.resultList[rand_index].title + "</p></a>";
    }
    html_ads += "</div></div>";
    return html_ads;
}

function getContent(resultList) {
    var html = "";
    for (var i = 0; i < resultList.length; i++) {
        html += "<div class='row'><div class='col-md-12'>" +
            "<h4><a href=" + resultList[i].url + " target='_blank' style='color:#1a0dab'>" + resultList[i].title + "</a></h4>" +
            "<p>" + resultList[i].content + "...</p>" +
            "<ul class='list-inline'>" +
            "<li><a href=" + resultList[i].url + " target='_blank' style='color:#006621'>" + resultList[i].url + "</a></li></ul>" +
            "<p class='pull-left'>"+
            "<span class='label label-default' style='color:#545454'>[发布文号] " + resultList[i].release_number + "</span>" +
            "<span class='label label-default' style='color:#545454'>[发布日期] " + resultList[i].release_date + "</span>" +
            "<span class='label label-default' style='color:#545454'>[实施日期] " + resultList[i].implement_date + "</span>" +
            "<span class='label label-default' style='color:#545454'>[法规类别] "+resultList[i].category+"</span>"+
            "<span class='label label-default' style='color:#545454'>[法规级别] "+resultList[i].level+"</span>"+
            "<span class='label label-default' style='color:#545454'>[时效性] "+resultList[i].timeless+"</span>"+
            "</p>" +
            "</div></div><hr>";
    }
    return html;
};

function initBottomIndex(startIndex, rowSize) {
    $('#nav').empty();
    if (rowSize == 0) {
        return;
    }
    var totlePage = parseInt(rowSize / 10) + 1;
    var html = "<nav><ul class='pagination'>";
    if (startIndex != 1) {
        html += "<li><a href='#' aria-label='Previous'>" +
            "<span aria-hidden='true' class='indexP'>Previous</span></a></li>";
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
    for (var i = startIndex; i < startIndex + 11 - up_num && i < totlePage; i++) {
        if (i == startIndex) {
            html += "<li class='index active' id = '" + i + "'><a href='#'>" + i + "</a></li>";
        } else {
            html += "<li class='index ' id = '" + i + "'><a href='#'>" + i + "</a></li>";
        }
    }
    html += "<li>" +
        "<a href='#' aria-label='Next'><span aria-hidden='true' class ='indexN'>Next</span>" +
        "</a></li></ul></nav>";
    $('#nav').html(html);
    bindIndexEvent();
    bindNextIndexEvent(rowSize);
    bindPreviousIndexEvent(rowSize);
};

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
};

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



