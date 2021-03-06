package com.seeyon.apps.xkjt.manager;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.seeyon.apps.xkjt.po.EdocFormInfo;
import com.seeyon.apps.xkjt.po.XkjtLeaderBanJie;
import com.seeyon.apps.xkjt.po.XkjtLeaderDaiYue;
import com.seeyon.apps.xkjt.po.XkjtSummaryAttachment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public interface XkjtManager {
	/**
	 * chenqiang
	 * @param fi
	 * @param param 查询条件
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo findAllRoles(FlipInfo fi, Map param) throws BusinessException;
	
	/**
	 * 通过角色Id获取角色名称
	 * chenqiang
	 * @param roleIds权限id字符串
	 * @return
	 */
	public Map<String, String> checkRoles(String roleIds) throws BusinessException;
	
	/**
	 * chenqiang
	 * 保存节点的公开方式
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	public Map<String,String> saveNodeOpenMode(Map param) throws BusinessException;
	
	/**
	 * zelda
	 * 保存节点的公开方式
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	public Map<String,String> saveNodeOpenModes(Map param) throws BusinessException;
	
	/**
	 * 
	 * @Title: OpenModeByNodeId  
	 * @Description: 通过节点Id获取公开方式
	 * @author wxt.admin
	 * @param nodeId
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public Map getOpenModeByNodeId(Map param) throws BusinessException;
	
	/**
	 * 当前用户是否能看到节点流程，判断节点是否公开，用户是否有查看权限
	 * @param nodeId，affairId
	 * @author wxt.wgy 2019.3.11
	 * @return
	 */
	public boolean isOpen(Long affairId) throws BusinessException;
	
	/**
	 * 当前用户查看意见时根据权限是否可见
	 * @param list，type
	 * @author wxt.wgy 2019.3.11
	 * @return
	 */
	public List getOpenOpinions(List list, int type) throws BusinessException;
	/**
	 * 当前用户是否能看到节点流程，判断节点是否公开，用户是否有查看权限
	 * @param nodeId，affairId
	 * @author wxt.wgy 2019.3.11
	 * @return
	 */
	public boolean isOpenBySender(Long affairId) throws BusinessException;
	
	/**
	 * 
	 * @Title: saveAttachmentIsMain  
	 * @Description: 保存公文的或协同的主附件
	 * @author wxt.chenqiang
	 * @param param
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public Map<String, String> saveMainAttachment(XkjtSummaryAttachment xkjtSummaryAttachment) throws BusinessException;
	
	/**
	 * 
	 * @Title: saveMainAttachment  
	 * @Description: 通过summary_Id获取其主附件
	 * @author wxt.chenq
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public XkjtSummaryAttachment findMainAttachmentBySummaryId(Long summaryId) throws BusinessException;
	
	
	/**
	 * 
	 * @Title: updateMainAttachmentBySummaryId  
	 * @Description: 通过summary_Id更新其主附件
	 * @author wxt.chenq
	 * @param xkjtSummaryAttachment
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public void updateMainAttachment(XkjtSummaryAttachment xkjtSummaryAttachment) throws BusinessException;
	
	/**
	 * 
	 * @Title: saveXkjtLeaderDaiYue  
	 * @Description: 保存发给人员的回执信息
	 * @author wxt.chenq
	 * @param xkjtSummaryAttachment
	 * @throws BusinessException
	 * @throws
	 */
	public void saveXkjtLeaderDaiYue(XkjtLeaderDaiYue xkjtLeaderDaiYue) throws BusinessException;
	
	/**
	 * 
	 * @Title: findXkjtLeaderDaiYueByEdocId  
	 * @Description: 通过EdocId找到给各个人员发送的传阅件
	 * @author wxt.chenq
	 * @param edocId
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueByEdocId(Long edocId,Long leaderId) throws BusinessException;
	
	/**
	 * 
	 * @Title: findXkjtLeaderDaiYueByMemberId  
	 * @Description: 查找当前人的待阅件
	 * @author wxt.xiangrui
	 * @param memberId
	 * @return
	 * @throws BusinessException
	 */
	public List<Object> findXkjtLeaderDaiYueByMemberId(Long memberId) throws BusinessException;
	
	
	/**
	 * 
	 * @Title: updateXkjtLeaderDaiYue  
	 * @Description: 修改待阅状态
	 * @author wxt.chenq
	 * @param param
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public void updateXkjtLeaderDaiYueByCondition(String id,String status) throws BusinessException;
	/**
	 * 
	 * @Title: updateXkjtLeaderDaiYue  
	 * @Description: 修改待阅状态
	 * @author wxt.chenq
	 * @param param
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public void updateXkjtLeaderDaiYue(XkjtLeaderDaiYue xkjtLeaderDaiYue) throws BusinessException;
	
	/**
	 * @Title: findMoreXkjtLeaderDaiYue
	 * @Description: 分页查询待阅
	 * @author wxt.chenq
	 * @param fi
	 * @param params
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public FlipInfo findMoreXkjtLeaderDaiYue(FlipInfo fi,Map params) throws BusinessException;
	
	/**
	 * 
	 * @Title: findXkjtLeaderDaiYueByEdocId  
	 * @Description: 获取回执信息中领导阅读件
	 * @author wxt.chenq
	 * @param edocId
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueByEdocIdAndSendId(Long sendRecordId, Long edocId) throws BusinessException;
	
	/**
	 * 
	 * @Title: findXkjtLeaderDaiYueById  
	 * @Description: 通过Id查找待阅件
	 * @author wxt.chenq
	 * @param id
	 * @return
	 * @throws
	 */
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueById(Long id) throws BusinessException ;
	

	
	/**
	 * @Title: findXkjtLeaderYiYueByMemberId  
	 * @Description: 通过memberId找到已阅件
	 * @author wxt.chenq
	 * @param leaderName
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public List<XkjtLeaderDaiYue> findXkjtLeaderYiYueByMemberId(Long memberId) throws BusinessException;
	
	/**
	 * @Title: findMoreXkjtLeaderYiYueByMemberId  
	 * @Description: 分页查询已阅
	 * @author wxt.chenq
	 * @param fi
	 * @param params
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public FlipInfo findMoreXkjtLeaderYiYue(FlipInfo fi,Map params) throws BusinessException;
	
	
	/**
	 * @Description: 办结栏目
	 * @author wxt.xiangrui
	 * @param memberId
	 * @throws BusinessException
	 */
	public List<Object> findXkjtLeaderBanJieByMemberId(Long memberId) throws BusinessException;
	
	/**
	 * @Description: 办结栏目
	 * @author zelda
	 * @param memberId
	 * * @param 模板id
	 * @throws BusinessException
	 */
	public List<Object> findXkjtLeaderBanJieByMemberId(Long memberId, String templetIds) throws BusinessException;
	
	/**
	 *@Description: 办结栏目更多页
	 *@author wxt.xiangrui
	 *@param fi
	 *@param params
	 *@return
	 *@throws BusinessException
	 */
	public FlipInfo findMoreXkjtLeaderBanJie(FlipInfo fi, Map<String,Object> params) throws BusinessException;
	
	/**
	 * @author best
	 * @param edocId
	 *            公文id
	 * @return 返回当前公的正文信息
	 */
	public Map<String, Object> getEdocContentInfo(Long edocId);
	
	/**
	 * @author best
	 * @param params
	 *            参数
	 * @return 返回当前节点是否是最后节点
	 */
	public Map<String, Object> judgeAffairIsLast(Map<String, Object> params);

	/**
	 * @author best
	 * @param formId
	 *            文单id
	 * @return 返回文单配置的不需要展示的部门信息
	 */
	public EdocFormInfo getByFormId(Long formId);

	/**
	 * @author best
	 * @param info
	 *            更新文单配置信息
	 * @throws BusinessException
	 *             抛出异常
	 */
	public void updateEdocFormInfo(EdocFormInfo info) throws BusinessException;

	/**
	 * @author best
	 * @param info
	 *            保存文单配置信息
	 * @throws BusinessException
	 *             抛出异常
	 */
	public void saveEdocFormInfo(EdocFormInfo info) throws BusinessException;
		/***
	 * 判断当前节点操作是否发送消息值发起人
	 * <p>Title: issendmessage
	 * <p>Description: TODO
	 * @author jiangchenxi
	 * @date 2020年5月8日
	 * @param affairId
	 * @param memberId
	 * @return
	 * @throws BusinessException
	 */
	public boolean issendmessage(Long affairId,Long memberId)  throws BusinessException;
	
	/**
	 * zelda
	 * 获取默认指定公开角色
	 * @param param
	 * @return
	 * @throws BusinessException
	 */
	public Map<String,String> getDefaultRoll(Map param) throws BusinessException;
	/**
	 * 判断当前公文协同 详情是否有权限展示意见
	 * @param list
	 * @param affairId
	 * @param type
	 * @return
	 * @throws BusinessException
	 */
	public List getOpenOpinions1(List list,Long affairId, int type) throws BusinessException;
	
}
