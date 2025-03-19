<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>회원가입 | 스터디메이트</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>
<div class="auth-header">
    <a href="${pageContext.request.contextPath}/index">
        <img src="${pageContext.request.contextPath}/image/header-logo.png" style="height: 32px"/>
    </a>
</div>
<div class="auth-main">
    <h1 style="font-size: 1.7em">회원가입</h1>
    <form action="${pageContext.request.contextPath}/auth/signup/verify" method="post" style="margin-top: 30px">
        <div class="auth-input-div">
            <input type="text" name="id" placeholder="아이디" class="auth-input"/>
        </div>
        <div class="auth-input-div">
            <input type="password" name="password" placeholder="비밀번호" class="auth-input"/>
        </div>
        <div class="auth-input-div">
            <input type="text" name="name" placeholder="활동명" class="auth-input"/>
        </div>
        <div class="auth-input-div album" >
            <c:forEach var="one" items="${avatars}">
                <div class="album-item">
                    <div>
                        <label for="${one.name}">
                            <img src="${pageContext.request.contextPath}${one.imageUrl}"
                                 style="width: 80%; object-fit: cover"/>
                        </label>
                    </div>
                    <div style="text-align: center;">
                        <input type="radio" name="avatarId" style="display: block" id="${one.name}" value="${one.id}"/>
                    </div>
                </div>
            </c:forEach>
        </div>
        <div class="auth-input-div">
            <button type="submit" class="auth-input">확인</button>
        </div>
    </form>
</div>
</body>
</html>
