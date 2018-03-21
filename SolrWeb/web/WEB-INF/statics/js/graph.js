// 创建节点数组
var nodes = new vis.DataSet([]);
// 创建关系数组
var edges = new vis.DataSet([]);

$(document).ready(function () {
    // 创建一个网络
    var container = document.getElementById('graph');
    // vis数据
    var data = {
        nodes: nodes,
        edges: edges
    };
    var options = {
        interaction: {hover: true},
        edges: {
            smooth: true,//是否显示方向箭头
            arrows: {to: true}//箭头指向to节点
        }
    };
    // 初始化网络
    var knowledgeGraph = new vis.Network(container, data, options);
    initGraph();
    //增加节点信息
    knowledgeGraph.on("click", function (params) {
        if (params.nodes.length == 0) {
            return;
        }
        getNodeGraph(nodes, edges, params.nodes[0]);
    });
    knowledgeGraph.on("hoverNode", function (params) {
        refreshKnowledgeContent(params.node);
    });
    knowledgeGraph.on("doubleClick", function (params) {
        if (params.nodes.length == 0) {
            return;
        }
        var splitId = params.nodes[0].split("-");
        $.ajax("/ruclaw/paperUrl", {
            type: "GET",
            dataType: "text",
            data: {id: params.nodes[0]},
            success: function (paperUrl) {
                if (!$.trim(paperUrl) == '') {
                    window.open(paperUrl);
                }
            }, error: function (request, textStatus, errorThrown) {
            }
        });
    });
    refreshKnowledgeContent($("#lawId").text());
    //为初始化面板
    $("#initButton").click(function () {
        initGraph();
    });
});
function initGraph(){
    nodes.clear();
    edges.clear();
    var depth = parseInt($("#depth").val());
    var limitNum = parseInt($("#limitNum").val());
    var nodeType = $("input[name='nodeType']:checked").val();
    getGraph(nodes, edges, $("#lawId").text(), depth, limitNum, nodeType)
}

function getColor(id) {
    var splitId = id.split("-");
    if (splitId.length <= 1) {
        return 'rgb(104,189,246)';
    }
    if (splitId.length <= 2) {
        return 'rgb(109,206,158)';
    }
    if (splitId.length <= 3) {
        return 'rgb(255,117,110)';
    }
    return 'rgb(104,189,246)';
}

function refreshKnowledgeContent(id) {
    var splitId = id.split("-");
    var contentHtml = "";
    $.ajax("/ruclaw/abstract", {
        type: "POST",
        dataType: "json",
        data: {id: id},
        success: function (data) {
            contentHtml += "<p><span class='label label-primary'>知识ID</span><span>  " + data.id + "</span></p>";
            contentHtml += "<p><span class='label label-primary'>知识名称</span><span>  " + data.name + "</span></p>";
            re = new RegExp("\n", "g");
            if (splitId.length == 1) {
                if (data.release_date && data.release_date != "") {
                    contentHtml += "<p><span class='label label-primary'>发布日期</span><span>  " + data.release_date + "</span></p>";
                }
                if (data.implement_date && data.implement_date != "") {
                    contentHtml += "<p><span class='label label-primary'>实施日期</span><span>  " + data.implement_date + "</span></p>";
                }
                if (data.release_number && data.release_number != "") {
                    contentHtml += "<p><span class='label label-primary'>发文字号</span><span>  " + data.release_number + "</span></p>";
                }
                if (data.timeless && data.timeless != "") {
                    contentHtml += "<p><span class='label label-primary'>有效性</span><span>  " + data.timeless + "</span></p>";
                }
                contentHtml += "<p><span class='label label-primary'>内容摘要</span><p>" + data.articleContent.replace(re, "<br/>") + "</p></p>";
            } else {
                contentHtml += "<p><span class='label label-primary'>知识内容</span><span>  " + data.articleContent.replace(re, "<br/>") + "</span></p>";
            }
            contentHtml += "<p><span class='label label-info'>提示</span><span>  更多详情请双击知识点，单击知识点节点关系继续展开。</span></p>";
            $('#knowledgeDetail').html(contentHtml);
        }, error: function (request, textStatus, errorThrown) {
            $('#knowledgeDetail').html("<p><span class='label label-danger'>服务器出错，请稍后！</span></p>");
        }
    });
}

function getGraph(nodes, edges, id, layerNum, limitNum, nodeType) {
    var parm = {
        id: id,
        layerNum: layerNum,
        limitNum: limitNum,
        nodeType: nodeType
    };
    $.ajax("/ruclaw/graphPath", {
        type: "POST",
        dataType: "json",
        data: parm || {},
        success: function (resultInfo) {
            createNodeAdnEdge(nodes, edges, resultInfo);
        },
        error: function (request, textStatus, errorThrown) {
        }
    });
}

function getNodeGraph(nodes, edges, id) {
    var parm = {
        id: id
    };
    $.ajax("/ruclaw/nodeGraphPath", {
        type: "POST",
        dataType: "json",
        data: parm || {},
        success: function (resultInfo) {
            createNodeAdnEdge(nodes, edges, resultInfo);
        },
        error: function (request, textStatus, errorThrown) {
        }
    });
}

function createNodeAdnEdge(nodes, edges, resultInfo) {
    for (var i = 0; i < resultInfo.length; i++) {
        try {
            if (!$.trim(resultInfo[i].startNode.id) == '') {
                var simpleName = resultInfo[i].startNode.name;
                if (resultInfo[i].startNode.name.length > 4) {
                    simpleName = resultInfo[i].startNode.name.substring(0, 4) + "...";
                }
                nodes.add({
                    font: {size: 10},
                    color: getColor(resultInfo[i].startNode.id),
                    id: resultInfo[i].startNode.id,
                    label: simpleName,
                    title: resultInfo[i].startNode.name,
                    shape: 'circle'
                });
            }
        }
        catch (err) {
        }
        try {
            if (!$.trim(resultInfo[i].endNode.id) == '') {
                var simpleName = resultInfo[i].endNode.name;
                if (resultInfo[i].endNode.name.length > 4) {
                    simpleName = resultInfo[i].endNode.name.substring(0, 4) + "...";
                }
                nodes.add({
                    font: {size: 10},
                    color: getColor(resultInfo[i].endNode.id),
                    id: resultInfo[i].endNode.id,
                    label: simpleName,
                    title: resultInfo[i].endNode.name,
                    shape: 'circle'
                });
            }
        }
        catch (err) {
        }
        try {
            if (!$.trim(resultInfo[i].relationShip) == '') {
                edges.add({
                    font: {size: 10},
                    id: resultInfo[i].startNode.id + resultInfo[i].relationShip + "+" + resultInfo[i].endNode.id,
                    from: resultInfo[i].startNode.id,
                    to: resultInfo[i].endNode.id,
                    label: resultInfo[i].relationShip,
                    title: resultInfo[i].relationShip
                });
            }
        }
        catch (err) {
        }
    }
}