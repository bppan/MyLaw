<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2018/1/2
  Time: 17:14
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Bootstrap 101 Template</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <!-- Bootstrap -->
  <link href="/css/bootstrap.min.css" rel="stylesheet" media="screen">
</head>
<body>
<h1>${name}</h1>
<script src="/js/jquery-2.2.0.min.js"></script>
<script src="/js/bootstrap.min.js"></script>

<div class="btn-group">
  <button class="btn">Left</button>
  <button class="btn">Middle</button>
  <button class="btn">Right</button>

</div>
<i class="icon-search"></i>
<i class="icon-search icon-white"></i>
<span class="label label-warning">注意！</span>
<span class="glyphicon glyphicon-search" aria-hidden="true"></span>

<p>
  <button class="btn btn-large btn-primary" type="button">Large button</button>
  <button class="btn btn-large" type="button">Large button</button>
</p>
<p>
  <button class="btn btn-primary" type="button">Default button</button>
  <button class="btn" type="button">Default button</button>
</p>
<p>
  <button class="btn btn-small btn-primary" type="button">Small button</button>
  <button class="btn btn-small" type="button">Small button</button>
</p>
<p>
  <button class="btn btn-mini btn-primary" type="button">Mini button</button>
  <button class="btn btn-mini" type="button">Mini button</button>
</p>

<div class="pagination">
  <ul>
    <li><a href="#">Prev</a></li>
    <li><a href="#">1</a></li>
    <li><a href="#">2</a></li>
    <li><a href="#">3</a></li>
    <li><a href="#">4</a></li>
    <li><a href="#">5</a></li>
    <li><a href="#">Next</a></li>
  </ul>

  <div class="modal fade">
    <div class="modal-header">
      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
      <h3>${name}</h3>
    </div>
    <div class="modal-body">
      <p>One fine body…</p>
    </div>
    <div class="modal-footer">
      <a href="#" class="btn">关闭</a>
      <a href="#" class="btn btn-primary">Save changes</a>
    </div>
  </div>

</div>
</body>
</html>
