<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>待处理</title>
    <style>
        html, body {
            margin: 0;
            padding: 0;
        }

        a {
            text-decoration: none;
            color: #000;
        }

        .active {
            font-weight: bold;
        }

        td {
            font-family: Arial, "Ping Fang SC", "Microsoft YaHei", Helvetica, sans-serif, "SimSun";
            height: 30px;
            line-height: 30px;
            white-space: nowrap;
            padding-left: 5px;
            font-size: 14px;
        }
    </style>
</head>
<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0">

    <tbody>
    <c:if test="${not empty contracts}">
        <c:forEach var="cont" items="${contracts}">
            <tr>
                <td width="50%" class="active">
                    <a href="javascript:void(0);" onclick="openTodo('${cont.taskUrl}','${detailParam}')">${cont.taskName}</a>
                </td>
                <td width="10%" align="center">
                    <span title="${cont.createUser}">${cont.createUser}</span>
                </td>
                <td width="30%" align="center">
                <span title="${cont.beginTime}">
                        ${cont.beginTime}
                </span>
                </td>
            </tr>
        </c:forEach>
    </c:if>
    <c:if test="${empty contracts}">
        <tr>
            <td width="50%" class="active" colspan="3">
                暂无数据...
            </td>
        </tr>
    </c:if>
    </tbody>
</table>
<script type="text/javascript">
    function openTodo(url, dToken) {
//        alert(url + dToken + "&Timespan=" + parseInt(new Date().getTime() / 1000));
//        console.log(url + dToken + "&Timespan=" + parseInt(new Date().getTime() / 1000));
        window.open(url + dToken + "&Timespan=" + parseInt(new Date().getTime() / 1000), "_blank");
    }
</script>
</body>
</html>
