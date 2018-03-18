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
                        <div class='panel-heading'>知识实体详情</div>
                        <div class='panel-body' style='text-align: left;overflow :auto' id = "knowledgeDetail">
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
