<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>스터디 만들기 | 스터디메이트</title>
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
        <div>
            <a href="${pageContext.request.contextPath}/my/profile">
                <img src="${pageContext.request.contextPath}${user.avatarUrl}" style="height: 35px"/>
            </a>
        </div>
    </div>
</div>
<div>
    <div class="study-main wrap">
        <div style="font-size:1.5em; margin-top:20px;">검색결과 ${count}</div>

        <c:forEach items="${result}" var="one">
            <div style="border-bottom: 1px solid  rgba(0,0,0,.08); padding : 20px 0px;">
                <h3 style="font-weight: 400">
                    <a href="${pageContext.request.contextPath}/study/${one.group.id}" style="text-underline-offset: 10px">${one.group.name}</a>
                </h3>
                <div>
                        ${one.group.goal}
                </div>
                <div style="display: flex; gap : 15px; margin-top: 2px; font-size: 0.85em">
                    <div style="color:#7f7f7f">
                        멤버 <span>${one.group.memberCount}</span>
                    </div>
                    <div style="color:#7f7f7f">
                        리더 <span>${one.creator.name}</span>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</div>
</body>
</html>
