<%--
  Created by IntelliJ IDEA.
  User: beiping_pan
  Date: 2018/3/16
  Time: 21:43
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD//XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<html>
    <head>
        <title>Knowledge graph</title>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="icon" href="../img/favicon.ico" type="image/x-icon">
        <link rel="stylesheet" type="text/css" href="../css/bootstrap.css">
        <link rel="stylesheet" type="text/css" href="../css/knowledgeGraph.css">
        <script src="../js/jquery-2.2.0.min.js"></script>
        <script src="../js/vis.min.js"></script>
        <script src="../js/graph.js"></script>
        <script src="../js/bootstrap.min.js"></script>
        <link href="../css/vis.min.css" rel="stylesheet" type="text/css" />
    </head>
    <body>
        <div id ="lawId" style="display:none">${id}</div>
        <!-- Head -->
        <div class="container-fluid" style="background:#F1F1F1">
            <div class="row" style="margin-top: 20px; margin-bottom:0; background:#F1F1F1;">
                <div class="col-md-1 col-sm-1 col-xs-1" style="background:#F1F1F1;height: 50px">
                    <a href="/ruclaw.html"><img width="90" height="36" style="position: absolute; right: 0;left: 15px;margin: auto;" src="../img/ruclaw_small_logo.png"></a>
                </div>
            </div>
        </div>
        <!-- content -->
        <div class="container-fluid" >
            <div class="row" style="padding-top: 30px; padding-bottom: 15px;">
                <div class="col-md-8 col-sm-12 col-xs-12" style="min-height: 785px;">
                    <div id="graph" class="graphCanvas"></div>
                </div>
                <div class="col-md-3 col-sm-12 col-xs-12">
                    <div class='panel panel-default'>
                        <div class='panel-heading'><span class="glyphicon glyphicon-list" aria-hidden="true"></span>
                            <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne" style="color: black">知识实体详情</a>
                        </div>
                        <div id="collapseOne" class="panel-collapse collapse">
                            <div class='panel-body' style='text-align: left;overflow :auto' >
                                <div id = "knowledgeDetail"></div>
                                <div class="col-lg-12" style="padding: 0">
                                    <div class="input-group pull-right">
                                        <span class="input-group-addon">单击展开限制节点数</span>
                                        <input id = "limitNodeNum" type="text" class="form-control" aria-label="..." value="25" onkeyup="(this.v=function(){this.value=this.value.replace(/[^0-9-]+/,'');}).call(this)" onblur="this.v();">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
                            <a data-toggle="collapse" data-parent="#accordion" href="#collapseTwo" style="color: black">图谱展示初始化配置</a>
                        </div>
                        <div id="collapseTwo" class="panel-collapse collapse">
                            <div class='panel-body' style='text-align: left;overflow :auto' id = "knowledgeDetail2">
                                <div class="col-lg-12" style="padding:0;">
                                    <span class='label label-info'>提示</span><span> 请输入数字，深度和和限制个数增大等待时间将延长。</span>
                                </div>
                                <div class="col-lg-5" style="padding: 15px 0 0 0">
                                    <div class="input-group pull-left">
                                        <span class="input-group-addon">关系深度</span>
                                        <input id = "depth" type="text" class="form-control" aria-label="..." value="4" onkeyup="(this.v=function(){this.value=this.value.replace(/[^0-9-]+/,'');}).call(this)" onblur="this.v();">
                                    </div>
                                </div>
                                <div class="col-lg-2" style="padding: 15px 0 0 0"></div>
                                <div class="col-lg-5" style="padding: 15px 0 0 0">
                                    <div class="input-group pull-right">
                                        <span class="input-group-addon">限制个数</span>
                                        <input id = "limitNum" type="text" class="form-control" aria-label="..." value="25" onkeyup="(this.v=function(){this.value=this.value.replace(/[^0-9-]+/,'');}).call(this)" onblur="this.v();">
                                    </div>
                                </div>
                                <div class="col-lg-12" style="margin-top: 16px;padding: 0">
                                    <div class="form-group pull-left">
                                        节点类别：
                                        <label class="radio-inline">
                                            <input type="radio" value="all" name="nodeType" checked>全部
                                        </label>
                                        <label class="radio-inline">
                                            <input type="radio" value="law" name="nodeType">法律
                                        </label>
                                        <label class="radio-inline">
                                            <input type="radio" value="article" name="nodeType">法条
                                        </label>
                                        <label class="radio-inline">
                                            <input type="radio" value="paragraph" name="nodeType">法款
                                        </label>
                                    </div>
                                </div>
                                <div class="col-lg-12" style="padding:0;">
                                    <span id = "initButton" type="button" class="btn btn-primary pull-right">重置</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!-- 底部版权 -->
        <div class="container foot" style = "background-color: #F1F1F1; width:100% ;font-size: 12px;min-height: 50px; margin-bottom: 0px">
            <span>
                <p class="text-center" style = "vertical-align:middle;margin: 0;line-height: 50px;">
                    This web is developed by BDAI.Copyright © 2018 Renmin University of China.
                </p>
            </span>
        </div>
    </body>
</html>
