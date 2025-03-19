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
        <div>
            <a href="${pageContext.request.contextPath}/index">
                <img src="${pageContext.request.contextPath}/image/header-logo.png" style="height: 35px"/>
            </a>

        </div>
        <div>
            <a href="${pageContext.request.contextPath}/my/profile">
                <img src="${pageContext.request.contextPath}${userAvatar.imageUrl}" style="height: 35px"/>
            </a>
        </div>
    </div>
</div>
<div style="background-color: #efefef">
    <div class="my-main wrap">

        <ul class="my-main-nav">
            <li class="active">내정보 ></li>
            <li>내가 쓴 글</li>
            <li>가입신청 중인 그룹</li>
            <li><a href="${pageContext.request.contextPath}/auth/logout">로그아웃</a></li>
        </ul>

        <div class="content">
            <div>
                <h2>내 정보</h2>
            </div>
            <div class="account">
                <h3>로그인 계정</h3>
                <ul>
                    <li>아이디 <span>${user.id }</span></li>
                    <li>활동명 <span>${user.name }</span></li>
                </ul>
            </div>
            <div class="info">
                <h3>로그인 내역</h3>
                <ul>
                    <li>로그인 회수 <span>${user.loginCount}</span></li>
                    <li>최근 로그인 <span>${latestLog.loginAt}</span></li>
                </ul>
            </div>
            <div class="info">
                <h3>활동 내역</h3>
                <ul>
                    <li>참여한 그룹 <span>-</span></li>
                    <li>총 공부시간 <span>${user.studyTime}</span>h</li>
                </ul>
            </div>
        </div>
    </div>
</div>

</body>
</html>
