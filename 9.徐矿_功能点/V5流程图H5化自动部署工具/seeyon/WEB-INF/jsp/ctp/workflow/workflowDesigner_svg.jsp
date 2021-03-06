<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ page isELIgnored="false" import="com.seeyon.ctp.common.AppContext"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="ctp" uri="http://www.seeyon.com/ctp"%>
<!DOCTYPE html>
<html class="h100b over_hidden">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=EDGE" />
<meta HTTP-EQUIV="Expires" CONTENT="-1">
<meta HTTP-EQUIV="Cache-Control" CONTENT="no-store"> 
<meta HTTP-EQUIV="Pragma" CONTENT="no-cache"> 
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>${dialogTitle}</title>

<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<%@ include file="/WEB-INF/jsp/ctp/workflow/workflowDesigner_js_svg.jsp" %>

<c:if test="${svgSupport eq 'true'}">
 <link rel="stylesheet" href="${path}/common/workflow/css/wf.css${ctp:resSuffix()}" type="text/css">
 <link rel="stylesheet" href="${path}/common/workflow/fonts/common/iconfont.css${ctp:resSuffix()}" type="text/css">
</c:if>


</head>
<c:choose>
<c:when test="${isEdoc=='true' }">
<c:set var="bodyOnUnloadFn" value="finish()"/>
</c:when>
<c:otherwise>
<c:set var="bodyOnUnloadFn" value="onUnloadEvent()"/>
</c:otherwise>
</c:choose>
<body onunload="${bodyOnUnloadFn}" marginheight="0" marginwidth="0" class="over_hidden h100b">
<table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
<c:if test="${svgSupport ne 'true'}">
<tr>
    <td colspan="3" style="height: 10px">
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td width="80%">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <c:if test="${(scene == '0' || scene == '1') && isCIPModel != 'true' }">
							<!-- 流程图操作帮助信息部分：开始 -->
							<tr><td  class="padding_10" style="font-size: 12px;font-weight: normal;">${editHelp }</td></tr>
							<!-- 流程图操作帮助信息部分：结束 -->
						</c:if>
						<c:if test="${canCopyFlow=='true'}">
							<tr>
								<td style="font-size: 12px;font-weight: normal;">
									<a href="javascript:void(0)" style="margin-left: 15px;" onclick="showWFTemplateList4Clone();" class="common_button common_button_emphasize">${ctp:i18n("workflow.copy.title.js")}</a>
							    </td>
							</tr>
						</c:if>
						<c:if test="${scene == '6'}">
							<tr>
								<td  style="font-size: 12px;font-weight: normal;" class="padding_5">
								${ctp:i18n('workflow.special.stepback.label1')}
								</td>
							</tr>
							<tr>
								<td style="font-size: 12px;font-weight: normal;" class="padding_5">
									<font color='red'>${ctp:i18n('workflow.special.stepback.label2')}</font>
								</td>
							</tr>
						</c:if>
                    </table>
                </td>
                <td align="right" nowrap="nowrap" height="30">
                      <table cellspacing="0" cellpadding="0" border="0">
                        <tr>
                        <%-- flash模式 --%>
                        <td align="left" class="padding_5">
                        <img id="showWorkflowBigger" onclick="showWorkflowBiggerFunction();" style="cursor:pointer;display:none;" title="${ctp:i18n('workflow.designer.menupanel.bigger.title')}" src="<c:url value='/common/images/workflow_big.png' />" >
                        </td>
                        <td align="left" class="padding_5">
                        <img id="showWorkflowSmaller" onclick="showWorkflowSmallerFunction();"  style="cursor:pointer;display:none;" title="${ctp:i18n('workflow.designer.menupanel.smaller.title')}" src="<c:url value='/common/images/workflow_small.png' />" >
                        </td>
                        <td class="padding_5">
                          <img style="cursor:pointer;display:block;" id="dragModeDiagramWorkflowDiagram" onclick="showDragModeWorkflowDiagram();"  title="${ctp:i18n('workflow.designer.menupanel.dragmode.title')}" src="<c:url value='/common/images/mode_drag.png' />" >
                        </td>
                        <td class="padding_5">
                            <img style="cursor:pointer;display:block;" id="scrollModeWorkflowDiagram" onclick="showScrollModeWorkflowDiagram();" title="${ctp:i18n('workflow.designer.menupanel.scrollmode.title')}" src="<c:url value='/common/images/mode_scroll_checked.png' />" >
                        </td>

                        <td class="padding_5" align="left"><img style="cursor:pointer;display:block" id="twoSeeMode" modeType="1"  onclick="toggleTwoSeeMode();" style="cursor:pointer;"  title="${ctp:i18n('workflow.designer.menupanel.size.window.title')}" src="<c:url value='/common/images/size_window.png' />" ></td>
                        <%-- <c:if test="${scene == '0'}">
                            <td class="padding_5" align="left">[<a href="#" onclick="openEventAdvancedSetting();">${ctp:i18n('workflow.advance.event.name')}</a>]</td>
                        </c:if>  --%>
                        <td class="padding_5">
                          <img style="cursor:pointer;display:none;height:26px;" id="exportWorkflowDiagramBtn" onclick="exportWorkflowDiagramFunc();"  title="${ctp:i18n('workflow.designer.menupanel.export.title')}" src="<c:url value='/common/workflow/images/335_ExportImage.png' />" >
                        </td>
                        <td width="5px"  nowrap="nowrap" class="padding_5" >
                        </td>
                        </tr>
                      </table>
                </td>
            </tr>
        </table>
    </td>
</tr>
</c:if>

<!-- flash流程图部分：开始 -->
<tr id="workflowMessageTD" style="display: none;">
<td colspan="3">
<div style="padding-left: 20px;font-size: 14px;">
<!-- 流程封装， 指定回退的时候有提示信息用这个TD来提示 -->
    <span class="ico16 risk_16" style="vertical-align: middle;"></span><span style="vertical-align: middle;" id="workflowMessageSpan"></span>
</div>
</td>
</tr>
<tr id="workflowContinerTD">
    <td id="flashContainerTD" height="100%" colspan="3" valign="top">
      <div id="flashContainer" style="width:100%; height:100%;overflow: hidden;">

      <c:if test="${svgSupport eq 'true'}">
        <!-- svg 流程图 -->
        <div class="wf_container">

            <c:if test="${(scene == '0' || scene == '1') && isCIPModel != 'true' }">
	            <div class="wf_info_bar">
		            <span class="wf_info_content wf_info_icon">${editHelp }</span>
		        </div>
            </c:if>
            <c:if test="${scene == '6'}">
                 <div class="wf_info_bar" style="text-align: left;">
                    <span class="wf_info_content margin_l_5">${ctp:i18n('workflow.special.stepback.label1')}</span><br/>
                    <font class="margin_l_5" color='red'>${ctp:i18n('workflow.special.stepback.label2')}</font>
                </div>
            </c:if>

	        <div id="toolbar" class="wf_toolbar"></div>
	        <div class="wf_content">
	            <div id="monitor" class="wf_svg_container"></div>
	        </div>
	        <div id="bottombar" class="wf_bottom_bar">
	            <div id="eagle_eye_switcher" style="margin-right: 10px;" class="wf_botton_btn pointer_btn syIcon sy-preview on"><!-- 鹰眼 --></div>
	            <div id="zoom_switcher" class="wf_botton_btn pointer_btn syIcon sy-show-all"<%--  title="${ctp:i18n('workflow.designer.menupanel.size.window.title')}" --%>><!-- 全屏 --></div>
	            <div id="zoom_value" style="width: 41px" class="wf_botton_btn">100%</div>
	            <div id="zoom_in" class="wf_botton_btn pointer_btn syIcon sy-zoom-in" <%-- title="${ctp:i18n('workflow.designer.menupanel.bigger.title')}" --%>><!-- 放大 --></div>
	            <div id="zoom_slider" class="wf_botton_btn wf_zoomslider"><span class="slider"></span><!-- 进度条 --></div>
	            <div id="zoom_out" class="wf_botton_btn pointer_btn syIcon sy-zoom-out" <%-- title="${ctp:i18n('workflow.designer.menupanel.smaller.title')}" --%>><!-- 缩小 --></div>
	        </div>
	        <div id="eagleEye" class="wf_eagle_eye">
	            <div id="eagle_eye_box" class="eagle_eye_box"></div>
	            <div id="eagle_eye_layer" class="wf_eagle_eye_layer"></div>
	        </div>

	        <c:if test="${scene == '0'}">
        <div style="display: none;" class="wf_search">
            <div class="wf_search_header" style="position: relative;">
                <input class="wf_search_input">
                <span class="syIcon sy-search" style="font-size: 16px;position: absolute;left: 284px;top:16px;color:#1F85EC;"></span>
                <span class="syIcon sy-close" style="position: absolute;left: 347px;top:18px"></span>
            </div>
            <div for="wf_search_node" class="wf_search_title" style="position: relative;">
                <span style="color: #2F88E4">${ctp:i18n('workflow.glossary.node')}<%-- 节点 --%></span>
                <span class="syIcon sy-arrow-down" style="position: absolute;left: 348px"></span>
            </div>
            <ol id="wf_search_node" class="wf_search_content"></ol>
            <div for="wf_search_conditon" class="wf_search_title" style="">
                <span style="color: #2F88E4">${ctp:i18n('workflow.glossary.branch')}<%-- 分支条件--%></span>
                <span class="syIcon sy-arrow-down" style="position: absolute;left: 348px"></span>
            </div>
            <ol id="wf_search_conditon" class="wf_search_content"></ol>
        </div>
        </c:if>
	    </div>
      </c:if>
      <c:if test="${svgSupport ne 'true'}">
      <!-- flash 流程图 -->
	      <object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" width="100%" height="100%" id="monitor" align="middle">
	            <param name="allowScriptAccess" value="always" />
	            <param name="movie" value="<c:url value='/common/workflow/monitor.swf${ctp:resSuffix()}' />" />
	            <param name="menu" value="false" />
	            <param name="quality" value="low" />
	            <param name="bgcolor" value="#ffffff" />
	            <param name="wmode" value="transparent" />
	            <param name="allowScriptAccess" value="always" />
	            <!-- 非IE浏览器:开始 -->
	            <script type="text/javascript">
	                if(isNewBrowser()){
	                  //IE11需要走这里
	                  document.write("<embed src=\"<c:url value='/common/workflow/monitor.swf${ctp:resSuffix()}' />\" quality=\"low\" bgcolor=\"#ffffff\" width=\"100%\" height=\"100%\" name=\"monitor\" wmode=\"transparent\" align=\"middle\" allowScriptAccess=\"always\" type=\"application/x-shockwave-flash\" />");
	                }
	            </script>
	            <!-- 非IE浏览器:结束 -->
	        </object>
      </c:if>
      </div>
    </td>
</tr>
<!-- flash流程图部分：结束 -->
<c:if test="${(scene != '2' && scene != '3' ) || isModalDialog=='true'}">
<!-- 流程规则部分：开始 -->
<c:if  test="${scene == '0' && appName == 'office'}">
<tr>
    <td width="80%" align="left" height="18px" colspan="2" nowrap="nowrap">
    <div id="ruleApanMsg" style="font-size: 12px;font-weight: normal;display: none;width: 100%"><a href="javascript:showRuleAndResetFlash();" class="color_blue">${ctp:i18n('workflow.designer.useinstructions.title')}</a>:</div>
    <div id='ruleApan' style="font-size: 12px;font-weight: normal;"><a href="javascript:showRuleAndResetFlash();" class="color_blue">${ctp:i18n('workflow.designer.useinstructions.title')}</a></div>
    </td>
    <td width="20%" align="right">
    <div id="ruleApanCloseImage" style="display: none" onclick="showRuleAndResetFlash()"><span class="ico16 revoked_process_16"></span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>
    </td>
</tr>
<tr id="ruleTR" style="display:none;">
    <td colspan="3" height="64px" class="bg-advance-middel">
        <div>
        <textarea id="ruleContent" style="width: 99%;height: 100%;" class="input-100per" inputName="${ctp:i18n('workflow.designer.useinstructions.title')}"></textarea>
        </div>
    </td>
</tr>
</c:if>
<!-- 流程规则部分：结束 -->
<c:if test="${isModalDialog=='false' && appName!='office' && 'true' != isCIPModel}">
<!-- 流程按钮操作部分：开始 -->
<tr id="workflowButtonTR">
    <td width="20%" style="height:42px">&nbsp;</td>
    <td align="right">
    <!-- 设计态:编辑模版流程 -->
    <c:if test="${scene == '0'}">
    <input id="confirmButton" name="confirmButton" type="button" onclick="saveWFContent()" value="${ctp:i18n('workflow.designer.page.button.ok')}" class="button-style">
    <input id= cancelButton" id="cancelButton" type="button" onclick="window.close()" value="${ctp:i18n('workflow.designer.page.button.cancel')}" class="button-style button-space">
    </c:if>
    <!-- 设计态:自由流程 -->
    <c:if test="${scene == '1'}">
    <input id="confirmButton" name="confirmButton" type="button" onclick="saveWFContent()" value="${ctp:i18n('workflow.designer.page.button.ok')}" class="button-style">
    <input id= cancelButton" id="cancelButton" type="button" onclick="window.close()" value="${ctp:i18n('workflow.designer.page.button.cancel')}" class="button-style button-space">
    </c:if>
    <!-- 设计态:查看模版流程 
    <c:if test="${scene == '2' && isModalDialog=='true'}">
    <input type="button" onclick="window.close()" value="${ctp:i18n('workflow.designer.page.button.close')}" class="button-style button-space">
    </c:if>
    <c:if test="${scene == '3' && isModalDialog=='true'}">
    <input type="button" onclick="window.close()" value="${ctp:i18n('workflow.designer.page.button.close')}" class="button-style button-space">
    </c:if>
            运行态:查看 -->
    <!-- 运行态:督办 -->
    <c:if test="${scene == '4'}">
    <input id="modifyButton" type="button" onclick="modify()" value="${ctp:i18n('workflow.designer.page.button.modify')}" style="display:none" class="button-style">
    <a id="submitButton" onclick="saveSuperviousContent()" class="common_button common_button_emphasize">${ctp:i18n('workflow.designer.page.button.ok')}</a>
    <a onclick="finish()" class="common_button common_button_gray">${ctp:i18n('workflow.designer.page.button.close')}</a>
    </c:if>
    <!-- 运行态:管控 -->
    <c:if test="${scene == '5'}">
    <input id="modifyButton" type="button" onclick="modify()" value="${ctp:i18n('workflow.designer.page.button.modify')}" class="button-style button-space">
    <input id="submitButton" type="button" onclick="saveWFContent()" value="${ctp:i18n('workflow.designer.page.button.ok')}" style="display:none" class="button-style">
    <input id="repealButton" type="button" onclick="repealWorkflow('${param.appEnumStr }', '${param.newflowType}')" value="${ctp:i18n('workflow.designer.page.button.repeal')}" class="button-style button-space">
    <input id="stopButton" type="button" onclick="stopWorkflow('${param.appEnumStr }', '${param.newflowType}')" value="${ctp:i18n('workflow.designer.page.button.stop')}" class="button-style button-space">
    <input id="closeButton" type="button" onclick="window.close()" value="${ctp:i18n('workflow.designer.page.button.close')}" class="button-style button-space">
    </c:if>
    </td>
    <td width="20%">&nbsp;</td>
</tr>
<!-- 流程按钮操作部分：结束 -->
</c:if>
</c:if>
<c:if test="${scene == '6'}">
<!-- 指定回退选项部分：开始 -->
<tr id="theStepBackNodeTR" style="display: none;">
    <td colspan="3" height="35px" style="font-size: 12px;font-weight: normal;" valign="top">
        <table align="center" width="100%" border="0" style="table-layout:fixed;">
            <tr>
                <td id="theStepBackNodeInfo" align="left" class="padding_5 text_overflow"  nowrap="nowrap" >${ctp:i18n("workflow.label.afterReturn")} <%-- 被回退节点处理后： --%></td>
            </tr>
            <tr>
                <td>
                    <table width="100%" border="0" style="<%--table-layout:fixed;--%>">
                        <td id="theStepBackNodeInfoAction" align="left"  class="padding_5 text_overflow" width="10%"  nowrap="nowrap">${ctp:i18n("workflow.label.afterReturnAction")}<%-- 被回退节点处理后动作：--%></td>
                        <c:set var="disableRego" value="${processState==2 || processState==3 || processState==5 || stepBackCount > 0}"></c:set>
                         <c:if test="${showReGo == 'true' }">
                             <td  class="padding_5" width="10%" nowrap="nowrap"><label class="margin_r_10 hand" for="rego"><input ${disableRego ? 'disabled' : 'checked' } onclick="onClickToMe(0);" type="radio" value="0" name="handleStyle" id="rego">${ctp:i18n('workflow.special.stepback.label6')}</label></td>
                         </c:if>
                         <c:if test="${showToMe == 'true'}">
                             <td  class="padding_5" width="10%" nowrap="nowrap" id="tomeTdId" onclick="showStepBackMsg();"><label class="margin_r_10 hand" for="tome" id="tomelabelId"><input ${(!showReGo || disableRego) ? 'checked' : '' } onclick="onClickToMe(1);" type="radio" value="1" name="handleStyle" id="tome">${ctp:i18n('workflow.special.stepback.label7')}</label></td>
                         </c:if>
                        <td  class="padding_5" id="tomeTipInfo" width="70%" align="left"></td>
                    </table>
                </td>
            </tr>
        </table>
    </td>   
</tr>
<!-- 指定回退选项部分：结束 -->
</c:if>
</table>
<div id="NewflowDIV" style="display: none"></div>
<form id="downLoadForm" target="downLoadIframe" style="display:none" method="post">
    <input name="method" value="exportDiagram">
    <input name="data" value="">
</form>
<iframe id="downLoadIframe" name="downLoadIframe" src="" style="display:none"></iframe>
</body>
<script type="text/javascript">
$(document).ready(function() {
    /* var traceSpanArea = parent.document.getElementById("traceSpanArea");
    if(_traceInput){
    	var _rego = document.getElementById("rego");
    	if(_rego && _rego.checked){  //不存在流程重走的时候或者没有选中的时候设置为灰色的。
    		traceSpanArea
    	}
    } */
    if(navigator.userAgent.toLowerCase().indexOf("macintosh")!=-1&&navigator.userAgent.toLowerCase().indexOf("safari")!=-1){
        getA8Top().$("iframe").css("margin-left","1px")
    }
});
</script>
</html>