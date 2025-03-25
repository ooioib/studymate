<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
        <div>
            <a href="${pageContext.request.contextPath}/my/profile">
                <img src="${pageContext.request.contextPath}${user.avatarUrl}" style="height: 35px"/>
            </a>
        </div>
    </div>
</div>
<div>
    <div class="wrap">
        <div style="background-color:#F5F6F8; border-radius: 20px; height: 150px; margin-top: 20px;
display: flex; align-items: center">
            <img src="${pageContext.request.contextPath}/image/logo.png" style="height:85%"/>
            <h2>함께 공부하면 더 성장한다! 스터디 메이트와 함께 만들어보세요</h2>
        </div>
        <h2 style="border-bottom: 1px solid rgba(0, 0, 0, .3); padding-bottom : 10px">내 스터디 그룹</h2>
        <div style="display: flex; overflow-x: auto; gap: 20px; width: 100%; padding-bottom: 20px;">
            <a href="${pageContext.request.contextPath}/study/create" style="display: block; text-decoration: none">
                <div style="border:1px solid rgba(0, 0, 0, .3); border-radius: 10px;
                            width: 156px; height: 156px;flex-shrink: 0; overflow: hidden">
                    <div style="background-color:#F5F6F8; display: flex; width: 100%; height: 100%; justify-content: center; align-items:center">
                        <h3 style="font-weight: 500">
                            스터디<br/>
                            만들기
                        </h3>
                    </div>
                </div>
            </a>
            <c:forEach items="${studyList}" var="one">
                <a href="${pageContext.request.contextPath}/study/${one.groupId}"
                   style="display: block; text-decoration: none">
                    <div style="border:1px solid rgba(0, 0, 0, .3); border-radius: 10px;
                                width: 156px; height: 156px;flex-shrink: 0; overflow: hidden">
                        <div style="background-color:#444; display: flex;
                            flex-direction: column;
                            width: 100%; height: 100%; justify-content: center; align-items:center">
                            <h4 style="color:#F5F6F8">
                                    ${one.groupName}
                            </h4>
                            <span style="display: inline-block; background-color: white;
                                padding : 3px 10px; border-radius: 8px; font-size: small">${one.groupType}</span>
                        </div>
                    </div>
                </a>
            </c:forEach>
        </div>
    </div>
</div>
</body>
</html>
