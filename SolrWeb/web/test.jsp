<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <style type="text/css">
        body {
            padding:0;
            margin:0;
        }
        #nav {
            width:100%;
            height:60px;
            background:#39f;
            color:#fff;
            line-height:60px;
            text-align:center;
            padding:0;
            margin:0;
            list-style:none;
        }
        #nav li {
            float:left;
            width:20%;
            height:60px;
        }
        .fix {
            position:fixed;
            top:0;
            left:0;
        }
    </style>
</head>

<div class="wrap">
    <h1>在线书城</h1>
    <p>有没有一本书让你仿佛遇到春风十里</p>
    <ul id="nav">
        <li>加入购物车</li>
        <li>加入收藏</li>
        <li>立即购买</li>
    </ul>
    <div class="con">
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
        <p>好书有好事有好诗</p>
    </div>
</div>

<script type="text/javascript">
    var tit = document.getElementById("nav");
    //alert(tit);
    //占位符的位置
    var rect = tit.getBoundingClientRect();//获得页面中导航条相对于浏览器视窗的位置
    var inser = document.createElement("div");
    tit.parentNode.replaceChild(inser,tit);
    inser.appendChild(tit);
    inser.style.height = rect.height + "px";

    //获取距离页面顶端的距离
    var titleTop = tit.offsetTop;
    //滚动事件
    document.onscroll = function(){
        //获取当前滚动的距离
        var btop = document.body.scrollTop||document.documentElement.scrollTop;
        //如果滚动距离大于导航条据顶部的距离
        if(btop>titleTop){
            //为导航条设置fix
            tit.className = "clearfix fix";
        }else{
            //移除fixed
            tit.className = "clearfix";
        }
    }
</script>
</html>