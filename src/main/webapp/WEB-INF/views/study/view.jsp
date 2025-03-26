<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${group.name} | 스터디메이트</title>
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
<div style="padding : 20px 0px;background-color: #F5F6F8;">
    <div class="study-main wrap">
        <div style="display: flex; gap:20px">
            <div style="width: 200px; background-color: white ; padding : 4px">
                <h2>${group.name}</h2>
                <div style="font-size : 0.8em">
                    멤버 <span>${group.memberCount}</span> •
                    리더 <span>${group.creatorId}</span>
                </div>
                <div style="font-size : 0.8em">
                    개설일 <span>${group.createdAt.toString().replace('T', '  ')}</span>
                </div>
                ${status}
                <c:choose>
                    <c:when test="${status == 'NOT_JOINED'}">
                        <p>
                            <a href="${pageContext.request.contextPath}/study/${group.id}/join">
                                <button style="width: 100%; padding: 5px; font-size:1em;">스터디 가입하기</button>
                            </a>
                        </p>
                    </c:when>
                    <c:when test="${status == 'PENDING'}">
                        <p>
                            <button style="width: 100%; padding: 5px; font-size:1em;" disabled>승인 대기중</button>
                        </p>
                    </c:when>
                    <c:when test="${status == 'MEMBER'}">
                        <p>
                            <a href="${pageContext.request.contextPath}/study/${group.id}/leave">
                                <button style="width: 100%; padding: 5px; font-size:1em;">스터디 탈퇴하기</button>
                            </a>
                        </p>
                    </c:when>
                    <c:otherwise>
                        <p>
                            <a href="${pageContext.request.contextPath}/study/${group.id}/remove">
                                <button style="width: 100%; padding: 5px; font-size:1em;">스터디 해산하기</button>
                            </a>
                        </p>
                    </c:otherwise>
                </c:choose>
                <c:choose>
                    <c:when test="${group.type == '공개'}">
                        <div style="font-size: 0.75em">
                            누구나 스터디를 검색해 찾을 수 있고, <b>가입할 수 있습니다</b>.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div style="font-size: 0.75em">
                            누구나 스터디를 검색해 찾을 수 있지만, <b>가입에는 승인이 필요합니다</b>.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div style="flex:1">
                <h3 style="border-bottom: 1px solid rgba(0, 0, 0, .3); padding-bottom : 10px">게시글</h3>
                <form action="${pageContext.request.contextPath}/study/${group.id}/post">
                    <input type="hidden" name="groupId" value="${group.id}"/>
                    <textarea style="width: 100%; height:100px; resize: none; padding : 4px" name="content"
                              id="content"></textarea>
                    <p style="text-align: right">
                        <button type="submit" style="padding : 4px 12px;">게시</button>
                    </p>
                </form>

                <c:forEach items="${postMetas}" var="one">
                    <div style="margin: 6px 0px; background-color: white; padding: 16px;">
                        <div style="display: flex; gap: 10px; align-items: center; ">
                            <img src="${pageContext.request.contextPath}${one.writerAvatar}"
                                 style="width: 48px;"/>
                            <div>
                                <b>${one.writerName}</b>
                                <div style="font-size: small">${one.time}</div>
                            </div>
                        </div>
                        <p style="font-size: small">
                                ${one.id} | ${one.content}
                        </p>
                        <div>
                            <c:forEach items="${one.reactions}" var="t">
                                <c:choose>
                                    <c:when test="${t.feeling == 'happy'}"><span>😍</span></c:when>
                                    <c:when test="${t.feeling == 'excited'}"><span>😆</span></c:when>
                                    <c:when test="${t.feeling == 'sad'}"><span>😥</span></c:when>
                                    <c:when test="${t.feeling == 'angry'}"><span>😡</span></c:when>
                                    <c:otherwise><span>😐</span></c:otherwise>
                                </c:choose>
                                <span>${t.count}</span>
                            </c:forEach>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
</div>

<!--자바스크립트 줄바꿈-->
<script>
    console.log(document.querySelector("#content"));
    document.querySelector("#content").onkeydown = function (e) {
        console.log(e);
        if (e.key == "Enter" && (!e.shiftKey)) {
            e.preventDefault();
            console.log(e.target.parentNode);
            e.target.parentNode.submit();
        }
    };
</script>

</body>
</html>