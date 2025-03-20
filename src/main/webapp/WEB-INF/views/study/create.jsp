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
<div >
    <div class="study-main wrap">
        <form style="padding: 20px 0px;" method="post"
              action="${pageContext.request.contextPath}/study/create/verify">
            <label class="label">스터디 이름</label>
            <div style="padding:8px 0px" class="bottom-border-div">
                <input type="text" placeholder="스터디 이름 입력" class="input-text" name="name"/>
            </div>
            <label class="label">스터디 목표</label>
            <div style="padding:8px 0px" class="bottom-border-div">
                <input type="text" placeholder="스터디 목표 입력" class="input-text" name="goal"/>
            </div>
            <label class="label">스터디 공개</label>
            <div class="type-box">
                <div >
                    <label style="display: block; font-weight:bold">
                        <input type="radio" name="type" value="공개" />
                        공개 스터디
                    </label>
                    <p>
                        누구나 스터디를 검색해 찾을 수 있고, 가입할 수 있습니다.
                    </p>
                </div>
                <div>
                    <label style="display: block; font-weight:bold">
                        <input type="radio" name="type" value="비공개"/>
                        비공개 스터디
                    </label>
                    <p>
                        누구나 스터디를 검색해 찾을 수 있지만, 가입에는 승인이 필요합니다.
                    </p>
                </div>
            </div>
            <div class="buttons">
                <button type="reset">취소</button>
                <button type="submit">완료</button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
