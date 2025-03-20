<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>공부가 쉬워진다 | 스터디메이트</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css"/>
</head>
<body>
<div class="bottom-border-div">
    <div class="index-header wrap">
        <div style="display:flex; gap:15px; align-items: center">
            <a href="${pageContext.request.contextPath}/index">
                <img src="${pageContext.request.contextPath}/image/header-logo.png" style="height: 35px"/>
            </a>
            <form action="${pageContext.request.contextPath}/study/search" style="margin: 0">
                <input type="text" name="word" style="border-radius: 20px; width:300px; padding:4px 15px;
background-color: #afafaf; color:white" placeholder="스터디 검색" value="${param.word}">
            </form>
        </div>
        <div class="index-main wrap">
            <div>
                <a href="${pageContext.request.contextPath}/study/create">그룹 생성</a> |
                <a href="${pageContext.request.contextPath}/study/search?word">그룹 목록</a>
            </div>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/my/profile">
                <img src="${pageContext.request.contextPath}${userAvatar.imageUrl}" style="height: 35px"/>
            </a>
        </div>
    </div>
</div>
<div>
    <div class="index-main wrap">
        <h2 style="border-bottom: 1px solid #333">내 그룹</h2>
    </div>
</div>

</body>
</html>
