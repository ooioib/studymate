<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>로그인 실패 | 스터디메이트</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>
<div class="auth-header">
  <a href="${pageContext.request.contextPath}/index">
    <img src="${pageContext.request.contextPath}/image/header-logo.png" style="height: 32px"/>
  </a>
</div>
<div class="auth-main">
  <h1 style="font-size: 1.7em">로그인 실패</h1>
  <p style="color: red; font-size: 0.9em; margin: 5px 0;">${errorMessage}</p>
  <form action="${pageContext.request.contextPath}/auth/login/verify" method="post" style="margin-top: 30px">
    <div class="auth-input-div">
      <input type="text" placeholder="아이디" class="auth-input" name="id"/>
    </div>
    <div class="auth-input-div">
      <input type="password" placeholder="비밀번호" class="auth-input" name="password"/>
    </div>
    <div class="auth-input-div">
      <button type="submit" class="auth-input">다시 시도</button>
    </div>
  </form>
  <p>
    스터디 메이트가 처음이신가요? <a href="${pageContext.request.contextPath}/auth/signup"><b>회원가입</b></a>
  </p>
</div>
</body>
</html>
