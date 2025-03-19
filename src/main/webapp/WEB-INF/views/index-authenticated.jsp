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
<div>
    <div class="index-main wrap">
        <h2 style="border-bottom: 1px solid #333">내 그룹</h2>
    </div>
</div>

</body>
</html>
