package com.seeyon.apps.govdoc.manager.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.event.CollaborationAddCommentEvent;
import com.seeyon.apps.collaboration.event.CollaborationCancelEvent;
import com.seeyon.apps.collaboration.event.CollaborationStepBackEvent;
import com.seeyon.apps.collaboration.event.CollaborationStopEvent;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.doc.constants.DocConstants.PigeonholeType;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.constant.GovdocEnum.ExchangeDetailStatus;
import com.seeyon.apps.govdoc.constant.GovdocEnum.NewGovdocFrom;
import com.seeyon.apps.govdoc.constant.GovdocEnum.TransferStatus;
import com.seeyon.apps.govdoc.event.GovdocEventDispatcher;
import com.seeyon.apps.govdoc.helper.GovdocAffairHelper;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.helper.GovdocOrgHelper;
import com.seeyon.apps.govdoc.helper.GovdocSummaryHelper;
import com.seeyon.apps.govdoc.listener.GovdocWorkflowEventListener;
import com.seeyon.apps.govdoc.manager.GovdocAffairManager;
import com.seeyon.apps.govdoc.manager.GovdocCommentManager;
import com.seeyon.apps.govdoc.manager.GovdocContentManager;
import com.seeyon.apps.govdoc.manager.GovdocContinueManager;
import com.seeyon.apps.govdoc.manager.GovdocDocManager;
import com.seeyon.apps.govdoc.manager.GovdocExchangeManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocListManager;
import com.seeyon.apps.govdoc.manager.GovdocLockManager;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocManager;
import com.seeyon.apps.govdoc.manager.GovdocMessageManager;
import com.seeyon.apps.govdoc.manager.GovdocOpenManager;
import com.seeyon.apps.govdoc.manager.GovdocPishiManager;
import com.seeyon.apps.govdoc.manager.GovdocPubManager;
import com.seeyon.apps.govdoc.manager.GovdocQuarzManager;
import com.seeyon.apps.govdoc.manager.GovdocRegisterManager;
import com.seeyon.apps.govdoc.manager.GovdocSignetManager;
import com.seeyon.apps.govdoc.manager.GovdocStatManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.manager.GovdocTemplateManager;
import com.seeyon.apps.govdoc.manager.GovdocTrackManager;
import com.seeyon.apps.govdoc.manager.GovdocWorkflowManager;
import com.seeyon.apps.govdoc.manager.QwqpManager;
import com.seeyon.apps.govdoc.mark.helper.GovdocMarkHelper;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkManager;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.po.GovdocExchangeMain;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocComponentVO;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.apps.govdoc.vo.GovdocRepealVO;
import com.seeyon.apps.govdoc.vo.GovdocSummaryVO;
import com.seeyon.apps.govdoc.vo.NodePolicyVO;
import com.seeyon.apps.index.api.IndexApi;
//import com.seeyon.apps.ocip.util.OcipAxis2Client;
import com.seeyon.apps.project.api.ProjectApi;
import com.seeyon.apps.project.bo.ProjectBO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.PropertiesConfiger;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.affair.util.AttachmentEditUtil;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.ContentViewRet;
import com.seeyon.ctp.common.content.WFInfo;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.Comment.CommentType;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyService;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.enums.PermissionAction;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.processlog.po.ProcessLogDetail;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.trace.enums.WorkflowTraceEnums;
import com.seeyon.ctp.common.track.enums.TrackEnum;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.form.util.permission.factory.PermissionFatory;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.ChangeType;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.ocip.common.organization.IOrganizationManager;
import com.seeyon.v3x.common.security.AccessControlBean;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.enums.EdocOpenFrom;
import com.seeyon.v3x.edoc.manager.EdocMarkDefinitionManager;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.engine.wapi.WorkItem;

/**
 * 新公文管理类
 *
 * @author 唐桂林
 *
 */
@SuppressWarnings({ "rawtypes" })
public class GovdocManagerImpl implements GovdocManager {

	private static final Log LOGGER = LogFactory.getLog(GovdocManagerImpl.class);

	private GovdocPubManager govdocPubManager;
	private GovdocExchangeManager govdocExchangeManager;
	private GovdocContinueManager govdocContinueManager;
	private GovdocSummaryManager govdocSummaryManager;
	private GovdocRegisterManager govdocRegisterManager;
	private GovdocStatManager govdocStatManager;
	private GovdocMarkManager govdocMarkManager;
	private GovdocQuarzManager govdocQuarzManager;
	private GovdocMessageManager govdocMessageManager;
	private GovdocOpenManager govdocOpenManager;
	private GovdocPishiManager govdocPishiManager;
	
	private GovdocFormManager govdocFormManager;
	private GovdocCommentManager govdocCommentManager;
	private GovdocAffairManager govdocAffairManager;
	private GovdocWorkflowManager govdocWorkflowManager;
	private GovdocContentManager govdocContentManager;
	private GovdocSignetManager govdocSignetManager;
	private GovdocLogManager govdocLogManager;
	private GovdocDocManager govdocDocManager;
	private GovdocLockManager govdocLockManager;
	private GovdocTrackManager govdocTrackManager;
	private QwqpManager qwqpManager;
	
	protected TemplateManager templateManager;
	private SuperviseManager superviseManager;
	private PermissionManager permissionManager;
	private AttachmentManager attachmentManager;
	private FileManager fileManager;
	private OrgManager orgManager;
	private AffairManager affairManager;
	private MainbodyManager ctpMainbodyManager;
	private IndexApi indexApi;
	private WorkTimeManager workTimeManager;
	private ProjectApi projectApi;
	private DocApi docApi;
	private CollaborationApi collaborationApi;
	private IOrganizationManager organizationManager;
	
    private FormApi4Cap3 formApi4Cap3;
    protected GovdocTemplateManager govdocTemplateManager;
    private EdocMarkDefinitionManager edocMarkDefinitionManager;
    private GovdocListManager govdocListManager;
    private WorkflowApiManager wapi;

	/*************************** 11111 拟文界面填充数据 start ***************************/
	@Override
	public GovdocNewVO fillNewVoByTemplate(GovdocNewVO newVo) throws BusinessException {
		// 调用模板
		newVo.setFromTemplate(true);
		Long templateId = newVo.getTemplateId();
		if(newVo.getTemplateId() == null) {
			newVo.setErrorMsg("模板已经被删除，或者您已经没有该模板的使用权限");
			return newVo;
		}
		// 判断权限
		boolean isTemplate = templateManager.isTemplateEnabled(templateId, newVo.getCurrentUser().getId());
		if (!isTemplate) {
			newVo.setErrorMsg("模板已经被删除，或者您已经没有该模板的使用权限");
			return newVo;
		}
		CtpTemplate template = (CtpTemplate) templateManager.getCtpTemplate(templateId);
		if (template==null || template.isDelete()) {
			newVo.setErrorMsg("模板已经被删除，或者您已经没有该模板的使用权限");
			return newVo;
		}
		if (template != null) {
			
			//是否能复制正文,来自模板调用的正文需要复制
			newVo.setCloneOriginalAtts(true);
			
			AppContext.putRequestContext("curTemplateID", template.getId());

			//模板存在时，根据模板类型，校验是否有对应的发文拟文、收文登记或签报拟文的权限
			if(template.getModuleType()!=null){
				Integer moduleType=template.getModuleType();
				if(moduleType.intValue()==ModuleType.govdocSend.getKey() && !AppContext.hasResourceCode("F20_newSend")
						||moduleType.intValue()==ModuleType.govdocRec.getKey() && !AppContext.hasResourceCode("F20_newDengji")
						||moduleType.intValue()==ModuleType.govdocSign.getKey() && !AppContext.hasResourceCode("F20_newSign")){
					newVo.setErrorMsg(ResourceUtil.getString("govdoc.alert.nocreate"));
					return newVo;
				}
			}
			
			Long templeteFormparnetId = template.getFormParentid();
			if (null != templeteFormparnetId && Strings.isNotBlank(String.valueOf(templeteFormparnetId))) {
				newVo.setTemformParentId(String.valueOf(templeteFormparnetId));
			}

			newVo.setFormtitle(template.getSubject());

			// 基准时长
			Long standardDuration = template.getStandardDuration();
			newVo.setStandardDuration(WFComponentUtil.getStandardDuration(standardDuration == null ? null : standardDuration.toString()));

			EdocSummary summary = (EdocSummary) XMLCoder.decoder(template.getSummary());

			// 项目
			Long projectId = template.getProjectId();
			if (null != projectId) {
				ProjectBO ps = projectApi.getProject(projectId);
				if (null != ps && !(ProjectBO.STATE_DELETE.intValue() == ps.getProjectState())) {
					summary.setProjectId(projectId);
				} else {
					summary.setProjectId(null);
					template.setProjectId(null);
				}
			}

			if (!TemplateEnum.Type.template.name().equals(template.getType())) {
				summary.setCanForward(true);
				summary.setCanArchive(true);
				summary.setCanDueReminder(true);
				summary.setCanEditAttachment(true);
				summary.setCanEdit(true);
			}
			if (TemplateEnum.Type.text.name().equals(template.getType())) {
				summary.setCanModify(true);
			}
			// 老公文模板防护
			else if (template.getModuleType() != null
					&& (template.getModuleType().intValue() == 19 || template.getModuleType().intValue() == 20 || template.getModuleType().intValue() == 21)) {
				summary.setCanModify(true);
			}

			summary.setSubject(template.getSubject());

			List<Attachment> attachments = new ArrayList<Attachment>();
			if (TemplateEnum.Type.template.name().equals(template.getType()) || ("templete").equals(template.getType())) {
				//提取附件名称，页面附件说明可能用到
				attachments = attachmentManager.getByReference(templateId);
				//保存待发，调用模板时，需要保留调用前的附件
				if(newVo.getSummaryId() != null) {
					List<Attachment> att2List = attachmentManager.getByReference(newVo.getSummaryId(), newVo.getSummaryId());
					if(Strings.isNotEmpty(att2List)) {
						attachments.addAll(att2List);
					}
				}
				newVo.setCloneOriginalAtts(true);

				// 预归档
				if (summary.getArchiveId() != null) {
					boolean docResourceExist = docApi.isDocResourceExisted(summary.getArchiveId());// 判断改目录是否被删除
					if (docResourceExist) {
						String archiveName = EdocUtil.getAdvancePigeonholeName(summary.getArchiveId(), summary.getAdvancePigeonhole(), "template");
						if (Strings.isNotBlank(archiveName)) {
							newVo.setAdvancePigeonhole(summary.getAdvancePigeonhole());
							newVo.setArchiveName(archiveName);
						} else {
							newVo.setArchiveId(summary.getArchiveId());
							newVo.setArchiveName(docApi.getDocResourceName(summary.getArchiveId()));
						}
					} else {
						summary.setArchiveId(null);
					}
				}
				
				//附件归档
				if (summary.getAttachmentArchiveId() != null && AppContext.hasPlugin("doc")) {//chenxd
					boolean docResourceExist = docApi.isDocResourceExisted(summary.getAttachmentArchiveId());
					if(docResourceExist){
						newVo.setAttachmentArchiveId(summary.getAttachmentArchiveId());
						summary.setCanArchive(true);
					}
				}

				// 发起人意见
				List<Comment> commentSenderList = govdocCommentManager.getCommentList(ModuleType.edoc, templateId);// 附言
				AppContext.putRequestContext("commentSenderList", commentSenderList);
			}
			//需要考虑分办后再调用模板的情况,这里需要查询附件
			if(newVo.getSignSummaryId() != null){
				// 获取附件, 将分办带过来的附件也加过去
				List<Attachment> attachments1 = attachmentManager.getByReference(newVo.getSignSummaryId());
				if (attachments1 != null) {
					for (Attachment attachment : attachments1) {
						if (attachment.getType() != Constants.ATTACHMENT_TYPE.FILE.ordinal()) {
							continue;
						}
						
						if (attachment.getCategory() == ApplicationCategoryEnum.collaboration.getKey() || attachment.getCategory() == ApplicationCategoryEnum.edoc.getKey()) {
							//去重复
							boolean flag = false;
							for (Attachment attachment1 : attachments) {
								if(attachment1.getId().longValue() == attachment.getId().longValue()){
									flag = true;
									break;
								}
							}
							if(!flag){
								attachments.add(attachment);
							}
						}
					}
					newVo.setFenbanFile(1);
				}
			}
			String attListJSON = attachmentManager.getAttListJSON(attachments);
			newVo.setAttListJSON(attListJSON);
				
			// 正文区域附件
			List<Attachment> mainAtt = new ArrayList<Attachment>();
			//表单上的附件
			List<Attachment> formAtt = new ArrayList<Attachment>();
			for (Attachment att : attachments) {
				if(att.getReference().equals(att.getSubReference())
						&& Integer.valueOf(com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE.ordinal()).equals(att.getType())) {
						mainAtt.add(att);
				}else if(att.getCategory() == ApplicationCategoryEnum.form.getKey()){
					formAtt.add(att);
				}
			}
			
			//表单中附件的文件名(附件说明用到)
			String formAttsName = attachmentManager.getFileNameExcludeSuffix(formAtt);
			newVo.setFilesmFormAttsName(formAttsName);
			//正文区附件的文件名(附件说明用到)
			String contentAttsName = attachmentManager.getFileNameExcludeSuffix(mainAtt);
			newVo.setFilesmContentAttsName(contentAttsName);
			
			boolean isPersonFlag = false;
			if (templeteFormparnetId != null) {
				// 取个人表单内容的
				template = templateManager.getCtpTemplate(templeteFormparnetId);
				if (template == null) {
					newVo.setErrorMsg("模板已经被删除，或者您已经没有该模板的使用权限");
					return newVo;
				}

				isPersonFlag = true;
			}

			if (Strings.isNotBlank(template.getColSubject())) {
				newVo.setCollSubjectNotEdit(true);
				newVo.setCollSubject(template.getColSubject());
			}

			if (template.isSystem() != null && template.isSystem()) {
				newVo.setSystemTemplate(true);
				summary.setCanEdit(true);//系统模板默认可以修改正文
			} else {
				newVo.setSystemTemplate(false);
			}

			if ("FORM".equals(template.getBodyType())) {
				newVo.setForm(Boolean.TRUE);
			} else {
				newVo.setForm(Boolean.FALSE);
			}
			AccessControlBean.getInstance().addAccessControl(ApplicationCategoryEnum.edoc, String.valueOf(templateId), AppContext.currentUserId());// OA-66280

			if (TemplateEnum.Type.template.name().equals(template.getType())) {// 格式模板不存取督办信息
																				// 升级程序
																				// 流程模板也不保留
				superviseManager.parseTemplateSupervise4New(templateId,  AppContext.currentUserId(), template.getOrgAccountId());
			} else if (TemplateEnum.Type.workflow.name().equals(template.getType()) || TemplateEnum.Type.text.name().equals(template.getType())) {
				template.setStandardDuration(null);
				newVo.setStandardDuration(null);// 设置页面上取的基准时长
				summary.setDeadline(null);// 流程期限
				summary.setAdvanceRemind(null);// 提醒
			}
			// 个人模板父模板IDs
			newVo.setTemplate(template);
			String fsubject = govdocPubManager.getTemplateSubject(summary.getSubject());
			summary.setSubject(fsubject);
			newVo.setSummary(summary);

			EdocSummary tSummary = summary;
			if (isPersonFlag) {
				tSummary = (EdocSummary) XMLCoder.decoder(template.getSummary());
			}

			newVo.setTempleteHasDeadline(tSummary.getDeadline() != null && tSummary.getDeadline() != 0);
			newVo.setTempleteHasRemind(tSummary.getAdvanceRemind() != null && (tSummary.getAdvanceRemind() != 0 && tSummary.getAdvanceRemind() != -1));
			newVo.setCanEditColPigeonhole(tSummary.getArchiveId() != null);

			if (null == template.getFormParentid()) {
				newVo.setParentWrokFlowTemplete(template.isSystem() && "workflow".equals(template.getType()));
				newVo.setParentTextTemplete(template.isSystem() && "text".equals(template.getType()));
				newVo.setParentColTemplete(template.isSystem() && "template".equals(template.getType()));
			} else {
				newVo.setParentWrokFlowTemplete(GovdocHelper.isParentWrokFlowTemplete(template.getFormParentid()));
				newVo.setParentTextTemplete(GovdocHelper.isParentTextTemplete(template.getFormParentid()));
				newVo.setParentColTemplete(GovdocHelper.isParentColTemplete(template.getFormParentid()));
			}
			if (template.isSystem() != null) {
				newVo.setFromSystemTemplete(template.isSystem());// 最原始的是不是系统模板
			} else {
				newVo.setFromSystemTemplete(false);// 最原始的是不是系统模板
			}
			if (template.isSystem()) {
				newVo.setSystemTemplate(true);
			}
			// 个人模板也要判断是否允许自检流程
			if (!template.isSystem()) {
				if (!govdocOpenManager.isEdocSelfFlow()) {
					newVo.setNoselfflow("noselfflow1");
				}
			}

			newVo.setAction("template");
			GovdocMarkHelper.fillNewMarkParameter(newVo);

		}
		
		//表示系统模板
		if(newVo.getTemplate().isSystem() || newVo.getTemplate().getFormParentid() != null) {
			newVo.setNoselfflow("noselfflow");
		}
		
		return newVo;
	}

	@Override
	public GovdocNewVO fillNewVoByExchange(GovdocNewVO newVo) throws BusinessException {
		if(newVo.getSummary()!=null && newVo.getSummary().getGovdocType() != null && newVo.getSummary().getGovdocType().intValue()==2) {
			GovdocExchangeDetail detail = govdocExchangeManager.findDetailByRecSummaryId(newVo.getSummary().getId());
			if(detail != null) {
				newVo.setFromDistribute(true);
				if(!NewGovdocFrom.waitSend.name().equals(newVo.getFrom())){//分办待发里面的文件不读取原原件了
					GovdocExchangeMain main = govdocExchangeManager.getGovdocExchangeMainById(detail.getMainId());
					if(main != null){
						newVo.setExchangeFileId(main.getOriginalFileId());
					}
				}
			}
		}
		return newVo;
	}
	
	@Override
	public GovdocNewVO fillNewVoByResend(GovdocNewVO newVo) throws BusinessException {
		Long summaryId = newVo.getSummaryId();
		EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
		newVo.setSummary(summary);
		String attListJSON = govdocContentManager.getSummaryAttachmentJsonsIncludeSender(summaryId,newVo);
		newVo.setAttListJSON(attListJSON);
		if (summary.getProcessId() != null) {
			newVo.setProcessId(summary.getProcessId());
		}
		// 预归档
		if (Strings.isNotBlank(summary.getAdvancePigeonhole())) {
			String archiveName = EdocUtil.getAdvancePigeonholeName(summary.getArchiveId(), summary.getAdvancePigeonhole(), "col");
			// 页面上传原来的字符串，和现实名字，优先显示高级归档
			newVo.setArchiveName(archiveName);
		} else if (null != summary.getArchiveId() && Strings.isNotBlank(String.valueOf(summary.getArchiveId()))) {
			newVo.setArchiveId(summary.getArchiveId());
			newVo.setArchiveName(docApi.getDocResourceName(summary.getArchiveId()));
		}
		// 来自"重新发起" 不允许修改标题
		newVo.setReadOnly(Boolean.TRUE);
		boolean cloneOriginalAtts = true;
		newVo.setCloneOriginalAtts(cloneOriginalAtts);
		if (summary.getTempleteId() != null) {
			CtpTemplate ctpTemplate = templateManager.getCtpTemplate(summary.getTempleteId());
			newVo.setTemplate(ctpTemplate);
			newVo.setSystemTemplate(ctpTemplate.isSystem());
			EdocSummary colSummary = (EdocSummary) XMLCoder.decoder(ctpTemplate.getSummary());
			newVo.setTempleteHasRemind(colSummary.getAdvanceRemind() != null && (colSummary.getAdvanceRemind() != 0 && colSummary.getAdvanceRemind() != -1));
			newVo.setTempleteHasDeadline(colSummary.getDeadlineDatetime() != null);
			newVo.setTemplateId(summary.getTempleteId());
			newVo.setTemplate(templateManager.getCtpTemplate(summary.getTempleteId()));
			newVo.setFromTemplate(summary.getTempleteId() != null);
			newVo.setParentWrokFlowTemplete(GovdocHelper.isParentWrokFlowTemplete(summary.getTempleteId()));
			newVo.setParentTextTemplete(GovdocHelper.isParentTextTemplete(summary.getTempleteId()));
			newVo.setParentColTemplete(GovdocHelper.isParentColTemplete(summary.getTempleteId()));
			newVo.setFromSystemTemplete(GovdocHelper.isParentSystemTemplete(summary.getTempleteId()));
		}
		newVo.setProjectId(summary.getProjectId());
		newVo.setResendFlag(true);
		govdocPubManager.CopySuperviseFromSummary(newVo, summary.getId());

		// 重新赋值summaryId
		long newSummaryId = UUIDLong.longUUID();
		newVo.setSummaryId(newSummaryId);
		Long parentSummaryId = newVo.getSummary().getId();
		newVo.setParentSummaryId(parentSummaryId);
		EdocSummary parentSummary = govdocSummaryManager.getSummaryById(parentSummaryId);
		newVo.getSummary().setId(newSummaryId);
		Long parentTemplateId = parentSummary.getTempleteId();
		CtpTemplate ctpTemplate = null;
		if (parentTemplateId != null) {
			ctpTemplate = (CtpTemplate) templateManager.getCtpTemplate(parentTemplateId);
			if (ctpTemplate != null) {
				EdocSummary tSummary = (EdocSummary) XMLCoder.decoder(ctpTemplate.getSummary());
				newVo.setTempleteHasDeadline(tSummary.getDeadline() != null && tSummary.getDeadline() != 0);
				newVo.setTempleteHasRemind(tSummary.getAdvanceRemind() != null && (tSummary.getAdvanceRemind() != 0 && tSummary.getAdvanceRemind() != -1));
				newVo.setCanEditColPigeonhole(!(tSummary.getArchiveId() == null));

				newVo.setParentWrokFlowTemplete(GovdocHelper.isParentWrokFlowTemplete(ctpTemplate.getFormParentid()));
				newVo.setParentTextTemplete(GovdocHelper.isParentTextTemplete(ctpTemplate.getFormParentid()));
				newVo.setParentColTemplete(GovdocHelper.isParentColTemplete(ctpTemplate.getFormParentid()));
				newVo.setFromSystemTemplete(ctpTemplate.isSystem());// 最原始的是不是系统模板
			}
		}
		govdocPubManager.CopySuperviseFromSummary(newVo, summary.getId());
		
		govdocTrackManager.fillTrackInfo(newVo);
		
		return newVo;
	}
	
	@Override
	public GovdocNewVO fillNewVoByWaitSend(GovdocNewVO newVo) throws BusinessException {
		if (!this.lookEnable(newVo.getAffairId(), newVo.getCurrentUser().getId())) {
			newVo.setErrorMsg("您无权查看该主题!");
			return newVo;
		}
		
		// 来自待发
		long summaryId = newVo.getSummaryId();
		EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
		ArrayList<Integer> states = new ArrayList<Integer>();
		states.add(StateEnum.col_waitSend.key());
		CtpAffair affair = affairManager.getAffairsByObjectIdAndStates(new FlipInfo(), summaryId, states).get(0);
		boolean isSpecailBackSubmitTo = Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState());

		// 需要clone一份 不然对数据进行修改的时候 会改到数据库的内容
		EdocSummary summaryCloneColSummary = null;
		try {
			summaryCloneColSummary = (EdocSummary) summary.clone();
			summaryCloneColSummary.setId(summary.getId());
			summary = summaryCloneColSummary;
			if (null != summaryCloneColSummary.getParentformSummaryid() && !summaryCloneColSummary.getCanEdit()) {
				newVo.setParentSummaryId(summaryCloneColSummary.getParentformSummaryid());
				newVo.setHideContentType(1);
			}
		} catch (CloneNotSupportedException e1) {
			LOGGER.error("", e1);
		}
		newVo.setSummary(summary);
		String attListJSON = govdocContentManager.getSummaryAttachmentJsonsIncludeSender(summaryId,newVo);
		newVo.setAttListJSON(attListJSON);
		Long templateId = summary.getTempleteId();
		CtpTemplate template = null;
		if (templateId != null) {
			newVo.setTemplateId(templateId);
			
			// 判断权限
			boolean isTemplate = templateManager.isTemplateEnabled(templateId, newVo.getCurrentUser().getId());
			if (!isTemplate) {
				newVo.setErrorMsg("模板已经被删除，或者您已经没有该模板的使用权限");
				return newVo;
			}
			template = templateManager.getCtpTemplate(templateId);
			if (template==null || template.isDelete()) {
				newVo.setErrorMsg("模板已经被删除，或者您已经没有该模板的使用权限");
				return newVo;
			}
			if (template != null) {
				AppContext.putRequestContext("curTemplateID", template.getId());
				if (null == template.getFormParentid()) {
					newVo.setParentWrokFlowTemplete(template.isSystem() && "workflow".equals(template.getType()));
					newVo.setParentTextTemplete(template.isSystem() && "text".equals(template.getType()));
					newVo.setParentColTemplete(template.isSystem() && "template".equals(template.getType()));
				} else {
					newVo.setParentWrokFlowTemplete(GovdocHelper.isParentWrokFlowTemplete(template.getFormParentid()));
					newVo.setParentTextTemplete(GovdocHelper.isParentTextTemplete(template.getFormParentid()));
					newVo.setParentColTemplete(GovdocHelper.isParentColTemplete(template.getFormParentid()));
				}
				CtpTemplate pTemplate = GovdocHelper.getParentSystemTemplete(template.getId());
				Boolean isFromSystem = template.isSystem();
				if (isFromSystem != null && !isFromSystem) {// 个人模板
					isFromSystem = (pTemplate == null || pTemplate.isSystem() == null) ? false : pTemplate.isSystem();
				} else {
					isFromSystem = true;
				}
				newVo.setFromSystemTemplete(isFromSystem);
				newVo.setTemplate(template);
				newVo.setFromTemplate(template.isSystem());
				newVo.setSystemTemplate(template.isSystem());
				
				newVo.setTemplate(template);
				newVo.setGovdocSystemTemplate(true);
				// BDGW-2837公文模板选择正文类型之后，调用时正文类型还可以修改
				newVo.setGovdocTemplate(true);
				// 保存待发 检测是否是套红模板
				newVo.getBodyVo().setBindTHTemplete(template.getBindTHTemplateId() != null);

				EdocSummary tSummary = summary;
				if (pTemplate != null) {
					tSummary = (EdocSummary) XMLCoder.decoder(pTemplate.getSummary());
				}
				newVo.setTempleteHasDeadline(tSummary.getDeadline() != null && tSummary.getDeadline() != 0);
				newVo.setTempleteHasRemind(tSummary.getAdvanceRemind() != null && (tSummary.getAdvanceRemind() != 0 && tSummary.getAdvanceRemind() != -1));
				newVo.setCanEditColPigeonhole(tSummary.getArchiveId() != null);

				//String formtitle = template.getSubject();
				Long templeteFormparnetId = template.getFormParentid();
				if (null != templeteFormparnetId) {
					newVo.setTemformParentId(String.valueOf(templeteFormparnetId));
				}
				// 如果是模板的都重新生成标题
				// colSubject为表单用流程标题规则
				/*if (Strings.isNotBlank(template.getColSubject())) {
					newVo.setCollSubjectNotEdit(true);
					newVo.setCollSubject(template.getColSubject());
				}*/
				//newVo.setFormtitle(formtitle);
				//summary.setSubject(formtitle);
				//String fsubject = govdocPubManager.getTemplateSubject(summary.getSubject());
				//summary.setSubject(fsubject);
				Long standardDuration = template.getStandardDuration();
				if (null != standardDuration) {
					newVo.setStandardDuration(template.getStandardDuration().toString());
				}
				if (!"text".equals(template.getType())) {
					String formOperationId = "";
					try {
						Long rightTemplateId = templeteFormparnetId != null ? templeteFormparnetId : template.getId();
						formOperationId = govdocFormManager.findRightIdbyAffairIdOrTemplateId(null, rightTemplateId);
					} catch (BusinessException e) {
						LOGGER.error("", e);
						throw new BusinessException(e);
					}
					newVo.setFormOperationId(formOperationId);
				}
				if (isFromSystem && !isSpecailBackSubmitTo) {
					// 格式模板5个选项前台全部可以编辑，流程模板可以编辑4个，改变流程不能编辑，协同模板全部不能编辑
					// 这个地方重新取模板的原因是：保存待发的流程，没有发出去，重新读取模板的所有设置。
					if ("template".equals(template.getType())) {
						summary.setCanArchive(tSummary.getCanArchive());
						//summary.setCanEdit(tSummary.getCanEdit());
						summary.setCanEditAttachment(tSummary.getCanEditAttachment());
						summary.setCanModify(tSummary.getCanModify());
						summary.setCanForward(tSummary.getCanForward());
					}

					if ("workflow".equals(template.getType())) {
						summary.setCanEditAttachment(tSummary.getCanEditAttachment());
					}

					// 关联文档 预归档到
					if (null != tSummary.getProjectId()) {
						summary.setProjectId(tSummary.getProjectId());
					}
					if (null != tSummary.getArchiveId()) {
						summary.setArchiveId(tSummary.getArchiveId());
					}
					if (Strings.isNotBlank(tSummary.getAdvancePigeonhole())) {
						summary.setAdvancePigeonhole(tSummary.getAdvancePigeonhole());
					}
				}
			}
		}

		// 预归档
		String archiveName = EdocUtil.getAdvancePigeonholeName(summary.getArchiveId(), summary.getAdvancePigeonhole(), "template");
		if (Strings.isNotBlank(archiveName)) {
			if ("wendangisdeleted".equals(archiveName)) {
				newVo.setAdvancePigeonhole("");
				newVo.setArchiveName("");
				AppContext.putRequestContext("setDisabled", true);
			} else {
				newVo.setAdvancePigeonhole(summary.getAdvancePigeonhole());
				newVo.setArchiveName(archiveName);
			}
		} else if (summary.getArchiveId() != null) {
			newVo.setArchiveId(summary.getArchiveId());
			newVo.setArchiveName(docApi.getDocResourceName(summary.getArchiveId()));
		}
		if (summary.getProcessId() != null) {
			newVo.setProcessId(summary.getProcessId());
			newVo.setCaseId(summary.getCaseId());
		}

		Long projectId = summary.getProjectId();
		newVo.setProjectId(projectId);

		/*if (template != null && Strings.isNotBlank(template.getColSubject())) {
			newVo.setCollSubjectNotEdit(true);
			if (String.valueOf(SubStateEnum.col_pending_specialBacked.key()).equals(affair.getSubState().toString())) {
				newVo.setCollSubject(affair.getSubject());
			} else {
				newVo.setCollSubject(template.getColSubject());
			}
		}*/
		newVo.setAffair(affair);
		newVo.setSubState(affair.getSubState());

		if (summary.getGovdocType() != null
				&& (ApplicationSubCategoryEnum.edoc_fawen.getKey() == summary.getGovdocType()
						|| ApplicationSubCategoryEnum.edoc_shouwen.getKey() == summary.getGovdocType()
						|| ApplicationSubCategoryEnum.edoc_qianbao.getKey() == summary.getGovdocType() || ApplicationSubCategoryEnum.edoc_jiaohuan.getKey() == summary
						.getGovdocType())) {
			superviseManager.parseProcessSupervise(summary.getId(), templateId, summary.getStartMemberId(), SuperviseEnum.EntityType.govdoc);
		}
		govdocPubManager.CopySuperviseFromSummary(newVo, summary.getId());

		govdocTrackManager.fillTrackInfo(newVo);
		//如果是分办后 保存待发 再调用模板 ,需要将文单里面的内容覆盖模板内容 通过封装distributeContentTemplateId 和distributeContentDataId实现
		if ("2".equals(newVo.getSubApp())) {
			// 判断该收文是不是交换过来的数据
			GovdocExchangeDetail detail = govdocExchangeManager.findDetailByRecSummaryId(summary.getId());
			if (detail != null) {
				newVo.setContentDataId(summary.getFormRecordid());
				newVo.setContentTemplateId(summary.getFormAppid());
				newVo.setSignSummaryId(summary.getId());
			}
		}
		
		return newVo;
	}
	@Override
	public void fillFinishPermisssion(GovdocDealVO finishVO) throws BusinessException {
		try {
			// 封装节点权限
			CtpAffair affair = finishVO.getAffair();
			String configItem = collaborationApi.getPolicyByAffair(affair).getId();
			EdocSummary summary = finishVO.getSummary();
			String category = PermissionFatory.getPermBySubApp(summary.getGovdocType(), configItem).getCategorty();
			Permission permission = null;
			permission = permissionManager.getPermission(category, configItem,AppContext.currentAccountId());
			if (permission != null) {
				String baseAction = permission.getBasicOperation();
				if (baseAction.indexOf("FaDistribute") != -1) {
					finishVO.setFensong(true);
				}
				if (baseAction.indexOf("ReSign") != -1) {
					finishVO.setSign(true);
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new BusinessException(e);
		}
	}
	private void fillNiwenNode(GovdocComponentVO compVO) throws BusinessException {
		String policyCategory = EnumNameEnum.edoc_new_send_permission_policy.name();
		CtpAffair affair = compVO.getAffair();
		if (affair.getSubApp().intValue() == 1) {
			policyCategory = EnumNameEnum.edoc_new_send_permission_policy.name();
		} else if (affair.getSubApp().intValue() == 2) {
			policyCategory = EnumNameEnum.edoc_new_rec_permission_policy.name();
		} else if (affair.getSubApp().intValue() == 3) {
			policyCategory = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
		}
		Permission permission = permissionManager.getPermission(policyCategory, "niwen", compVO.getUser().getAccountId());
		AppContext.putRequestContext("newColNodePolicy", new NodePolicyVO(permission));
	}
	/*************************** 11111 拟文界面填充数据   end ***************************/

	
	/*************************** 22222 新建界面发送保存 start ***************************/
	@Override
	public void transSend(GovdocNewVO newVo, SendType sendType) throws BusinessException {
		/** 1、获取参数，组装对象 */
		govdocPubManager.fillSendObj(newVo, sendType);
		/** 2、填充文单隐映数据 */
		govdocPubManager.fillSummaryByMapping(newVo);
		/** 3、校验(发文权限、公文状态) */
		/** 4、暂存待办之类的需要先删除summary对象 */
		govdocPubManager.delSummary(newVo);
		/** 6、处理全文签批单 */
		qwqpManager.saveEdocFormFileRelations(newVo);
		/** 7、保存正文 */
		govdocContentManager.saveBodyContent(newVo);
		/** 8、保存附件 */
		govdocContentManager.saveAttachments(newVo, sendType);
		/** 9、保存已发事项 */
		govdocAffairManager.saveSenderAffair(newVo);
		/** 10、保存流程(该处需要回调GovdocWorkflowEventListener方法onWorkitemAssigned) */
		govdocWorkflowManager.sendRuncase(sendType, newVo);
		/** 11、保存当时待办人 */
		GovdocHelper.updateCurrentNodesInfoFromCache(newVo.getSummary(), null);
		/** 12、非编辑状态的附件说明需要单独处理*/
		govdocFormManager.dealCantEditFilesmName(newVo);
		/** 13、保存公文主表数据另外填充些数据，后续就不用再次UPDATE */
		govdocPubManager.fillSummaryBeforeSave(newVo);
		/** 14、保存公文主表数据 */
		govdocSummaryManager.saveOrUpdateEdocSummary(newVo.getSummary());
		/** 15、保存文号及跳号 */
		govdocMarkManager.saveSendMark(newVo);
		/** 16、保存收发文登记簿数据 */
		govdocRegisterManager.saveBySummary(newVo.getSummary());
		/** 17、保存统计数据 */
		govdocStatManager.saveOrUpdateEdocStat(newVo.getSummary(), newVo.getCurrentUser());
		/** 18、保存督办信息 */
		govdocPubManager.saveSupervise(newVo, sendType, true);
		/** 19、归档 */
		govdocDocManager.savePigeonhole(newVo, sendType);
		/** 20、保存附言 */
		govdocCommentManager.saveComment(newVo, newVo.getSenderAffair());
		/** 21、保存日志 */
		govdocLogManager.saveSendLogs(newVo, sendType);
		/** 22、保存跟踪信息 */
		govdocTrackManager.saveTrackInfo(newVo);
		/** 23、创建超期提醒和提前提醒 定时任务 */
		govdocQuarzManager.createSendQuartzJob(newVo);
		/** 24、发送自动跳过的事件(异步)，异步回调方法CollaborationListener.transOnAutoSkip */
		GovdocHelper.fireAutoSkipEvent(this);
		/** 25、如果是快速发文,则触发交换 */
		govdocPubManager.sendIsQuickGovdoc(newVo);
		/** 26、如果是续办,则保存续办相关信息 */
		govdocContinueManager.setCustomDealWith(newVo);
		/** 27、调用表单万能方法,更新状态，触发子流程等 */
		govdocFormManager.updateDataState(newVo.getSenderAffair(), newVo.getSummary(), ColHandleType.send);
		/** 28、全文检索 */
		// 全文检索
		if (AppContext.hasPlugin("index")) {
		    indexApi.add(newVo.getSummary().getId(), ApplicationCategoryEnum.edoc.getKey());
		}
	}
	
	@Override
	public boolean transSaveDraft(GovdocNewVO newVo, Map para) throws BusinessException {
		govdocPubManager.fillDraftObj(newVo, para);
		EdocSummary summary = newVo.getSummary();
		CtpAffair senderAffair = newVo.getSenderAffair();
		
		//公文待发流程
		if (!newVo.isSpecialBacked()) {
			WFInfo wfinfo = govdocWorkflowManager.draftRuncase(newVo);
			String processId = wfinfo.getProcessId();
			summary.setProcessId(processId);
			summary.setCaseId(null);
			summary.setStartTime(null);
		}
		
		// 保存附件
		if (!newVo.isNew()) {// 删除原来的
			if (!newVo.isSpecialBacked()) {//非回退的，需删除所有Affair，被回退的则不删除
				affairManager.deleteByObjectId(ApplicationCategoryEnum.edoc, summary.getId());
			}
			//删除公文所有附件
			attachmentManager.deleteByReference(summary.getId(), summary.getId());
		}
		//保存模板id
		summary.setTempleteId(newVo.getCurTemId());
		String attaFlag = govdocContentManager.saveAttachmentFromDomain(ApplicationCategoryEnum.edoc, summary.getId());
		if (com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag)) {
			summary.setHasAttachments(true);
		} else {
			summary.setHasAttachments(false);
		}
		
		newVo.setSummary(summary);
		senderAffair = GovdocAffairHelper.getDraftSenderAffair(newVo);
				
		// 保存待发删除以前的意见
		if(!newVo.isSpecialBacked()) {
			govdocCommentManager.deleteCommentAllByModuleId(ModuleType.edoc, summary.getId());
		} else {
			govdocCommentManager.deleteNibanCommentAllByModuleIdAndCtype(ModuleType.edoc, summary.getId());
		}
		//将前端文号相关参数传值到后台
		GovdocMarkHelper.fillSaveMarkParameter(newVo, para);
		//非编辑状态的附件说明需要单独处理
		govdocFormManager.dealCantEditFilesmName(newVo);
		//处理映射关系，将单子中的映射数据同步到summary
		govdocPubManager.fillSummaryByMapping(newVo);
		//保存EdocSummary主表数据
		govdocSummaryManager.saveOrUpdateEdocSummary(summary);
		// 保存待发新增拟办意见
		govdocCommentManager.saveComment(newVo, senderAffair);
		//保存待发Affair
		govdocAffairManager.saveDraftAffair(newVo);
		//保存公文正文
		govdocContentManager.saveBodyContent(newVo);
		//保存公文文号
		govdocMarkManager.saveDraftMark(newVo);
		//保存督办信息
		govdocPubManager.saveSupervise(newVo, null, false);
		//保存跟踪人员数据
		govdocTrackManager.saveTrackInfoByDraft(newVo);
		// 增加模板调用历史记录
		if (null != newVo.getCurTemId()) {
			templateManager.saveTemplateRecent(newVo.getCurTemId(),AppContext.currentUserId());
		} else if (summary.getTempleteId() != null) {
			templateManager.updateTempleteRecent(AppContext.currentUserId(),newVo.getCurTemId());
		}
		// 调用表单万能方法,更新状态，触发子流程等
		govdocFormManager.updateDataState(senderAffair, summary, ColHandleType.save);
		return true;
	}
	/*************************** 22222 新建界面发送保存   end ***************************/
	
	
	/*************************** 33333 公文查看界面参数 start ***************************/
	@Override
	public void transShowSummary(GovdocSummaryVO summaryVO) throws BusinessException {
		long l1 = System.currentTimeMillis();
		EdocSummary summary = summaryVO.getSummary();
		CtpAffair affair = summaryVO.getAffair();
		
		boolean isHistoryFlag = summaryVO.isHistoryFlag();
		// 点击更新affair状态
		if (!isHistoryFlag) {
			govdocAffairManager.updateAffairStateWhenClick(affair);
		}
		long l2 = System.currentTimeMillis();
		LOGGER.info("公文查看点击更新affair状态耗时：" + (l2 - l1));
		//设置公文开关
		GovdocSummaryHelper.fillSummaryVoBySwitchBefore(summaryVO);
		long l3 = System.currentTimeMillis();
		LOGGER.info("设置公文开关耗时：" + (l3 - l2));
		//封装节点权限及节点权限策略权限(包括正文打印、正文保存)
		govdocPubManager.fillSummaryVoByPermission(summaryVO);
		long l4 = System.currentTimeMillis();
		LOGGER.info("封装节点权限耗时：" + (l4 - l3));
		// 设置流程相关参数
		govdocWorkflowManager.fillSummaryVoByWf(summaryVO);
		long l5 = System.currentTimeMillis();
		LOGGER.info("设置流程相关参数耗时：" + (l5 - l4));
		//获取多级会签参数
		GovdocEventDispatcher.fireEventForCurJsonPerm(this, summary, affair);
		long l6 = System.currentTimeMillis();
		LOGGER.info("获取多级会签参数耗时：" + (l6 - l5));
		//封装公文附件数据
		govdocContentManager.fillSummaryVoByAtt(summaryVO);
		long l7 = System.currentTimeMillis();
		LOGGER.info("获取多级会签参数耗时：" + (l7 - l6));
		//封装公文交换数据
		govdocPubManager.fillSummaryVoByExchange(summaryVO);
		long l8 = System.currentTimeMillis();
		LOGGER.info("封装公文交换数据耗时：" + (l8 - l7));
		//文件正文内容的相关信息
		GovdocContentHelper.fillSummaryBodyData(summaryVO);
		long l9 = System.currentTimeMillis();
		LOGGER.info("文件正文内容的相关信息耗时：" + (l9 - l8));
		//封装公文文号参数对象
		GovdocMarkHelper.fillNewMarkParameter(summaryVO);
		long l10 = System.currentTimeMillis();
		LOGGER.info("封装公文文号参数对象耗时：" + (l10 - l9));
		//封装公文回退数据
		GovdocSummaryHelper.fillSummaryVoByBack(summaryVO);
		long l11 = System.currentTimeMillis();
		LOGGER.info("封装公文回退数据耗时：" + (l11 - l10));
		//封装公文意见
        govdocPubManager.fillSummaryVoByOpinions(summaryVO);
        long l12 = System.currentTimeMillis();
		LOGGER.info("封装公文意见耗时：" + (l12 - l11));
        //当前为处理状态
		if(summaryVO.getSwitchVo().isHasDealArea()) {
			// 获取跟踪，指定人信心
			govdocTrackManager.fillSummaryVoByTrack(summaryVO);
			long l13 = System.currentTimeMillis();
			LOGGER.info("获取跟踪耗时：" + (l13 - l12));
			// 处理页面续办功能、和默认显示
			govdocContinueManager.fillSummaryVoByXuban(summaryVO);
			long l14 = System.currentTimeMillis();
			LOGGER.info("处理页面续办功能耗时：" + (l14 - l13));
			// 处理领导批示编号相关数据
			govdocPishiManager.fillSummaryVoByLeaderPishiNo(summaryVO);
			long l15 = System.currentTimeMillis();
			LOGGER.info("处理领导批示编号相关数据耗时：" + (l15 - l14));
		}
		long l16 = System.currentTimeMillis();
		//全文签批加载
		qwqpManager.setQwqpParam(summaryVO);
		long l17 = System.currentTimeMillis();
		LOGGER.info("全文签批加载耗时：" + (l17 - l16));
		//设置公文查看开关
		GovdocSummaryHelper.fillSummaryVoBySwitchAfter(summaryVO);
		long l18 = System.currentTimeMillis();
		LOGGER.info("设置公文查看开关耗时：" + (l18 - l17));
	}

	@Override
	public void transComponentPage(GovdocComponentVO summaryVO, HttpServletRequest request) throws BusinessException {
		long l1 = System.currentTimeMillis();
		String openFrom = summaryVO.getOpenFrom();
		CtpAffair affair = summaryVO.getAffair();
		
		//设置文单签章数据
		govdocSignetManager.fillFormSignData(summaryVO);
		long l2 = System.currentTimeMillis();
		LOGGER.info("设置文单签章数据耗时：" + (l2 - l1));
		
		//设置公文查看文单权限
		govdocFormManager.fillSummaryVoByForm(summaryVO);
		long l3 = System.currentTimeMillis();
		LOGGER.info("设置公文查看文单权限耗时：" + (l3 - l2));
		
		// 设置文单编辑状态
		int viewState = CtpContentAllBean.viewState_readOnly;
		if (Integer.valueOf(StateEnum.col_pending.key()).equals(affair.getState()) && !"inform".equals(collaborationApi.getPolicyByAffair(affair).getId())
				&& !"F8reprot".equalsIgnoreCase(openFrom) && !"glwd".equals(openFrom) && !"listDone".equals(openFrom) && !AffairUtil.isFormReadonly(affair)) {// isFormReadonly表单只读
			viewState = CtpContentAllBean.viewState__editable;
		}
		summaryVO.setViewState(viewState);
		
		Map<String, Object> affairExtMap = AffairUtil.getExtProperty(affair);
		if (affairExtMap != null) {
			String isTakeback = null == affairExtMap.get(AffairExtPropEnums.edoc_lastOperateState.name()) ? "" : String.valueOf(affairExtMap
					.get(AffairExtPropEnums.edoc_lastOperateState.name()));
			// 是否是取回数据
			if (Strings.isNotBlank(isTakeback)) {
				summaryVO.setIsTakeback("1");
			}
		}

		Long moduleId = -1L;
		if (summaryVO.getSummary() != null) {
			moduleId = summaryVO.getSummary().getId();
		}
		summaryVO.setModuleId(moduleId);
		
		ContentViewRet context = new ContentViewRet();
		context.setModuleId(moduleId);
		context.setModuleType(ModuleType.edoc.getKey());
		context.setAffairId(summaryVO.getAffair().getId());
		request.setAttribute("contentContext", context);
		
		if("0".equals(summaryVO.getNewGovdocView())) {//经典布局
			long l4 = System.currentTimeMillis();
			this.transCommentPage(summaryVO, request);
			long l5 = System.currentTimeMillis();
			LOGGER.info("设置公文transCommentPage查看耗时：" + (l5 - l4));
		}
	}
	
	@Override
	public void transCommentPage(GovdocComponentVO summaryVO,HttpServletRequest request) throws BusinessException {
		long l1 = System.currentTimeMillis();
		CtpAffair affair = summaryVO.getAffair();
		EdocSummary summary = summaryVO.getSummary();
		govdocContentManager.contentViewForDetail(ModuleType.edoc, summaryVO);
		
		long l2 = System.currentTimeMillis();
		LOGGER.info("设置公文意见界面设置前端参数耗时：" + (l2 - l1));
		
		String openFrom = summaryVO.getOpenFrom();
        //人员操作日志
        Map<String,List<String>> logDescMap= govdocCommentManager.getCommentLog(summary.getProcessId());
        String jsonString = JSONUtil.toJSONString(logDescMap);
        summaryVO.setLogDescMap(jsonString);

		if (summary.getParentformSummaryid() != null && !summary.getCanEdit()) {
			summaryVO.setFromTransform(true);
		}
		if(WorkflowTraceEnums.workflowTrackType.step_back_repeal.getKey() == summaryVO.getTrackType() && "stepBackRecord".equals(openFrom)){
    		openFrom =  "repealRecord";
    	}

		summaryVO.setChangeOpinion(govdocOpenManager.hasEditAuth(summaryVO.getUser().getId(), summaryVO.getUser().getAccountId()));
		if(EdocOpenFrom.stepBackRecord.name().equals(summaryVO.getOpenFrom())){//OA-175540
			summaryVO.setChangeOpinion(false);
		}
		boolean  isAllowEditInForm= govdocOpenManager.isAllowEditInForm();
		boolean allowEditMyOpinions="true".equals(SystemEnvironment.getPluginDefinition("govdoc").getPluginProperty("govdoc.allowEditInForm"))
					&& isAllowEditInForm;
		summaryVO.setAllowEditMyOpinions(allowEditMyOpinions);
		// 是否可编辑意见
		summary = summaryVO.getSummary();
		summaryVO.setCanEditComment(summary.getState() == 0 || summary.getState() == EdocConstant.flowState.run.ordinal()
				|| summary.getState() == EdocConstant.flowState.finish.ordinal());
		summaryVO.setLederRelation("");

		/**
		 * 1: 撤销的不能进行回复 2：回退导致撤销的不能进行回复
		 */
		if (WorkflowTraceEnums.workflowTrackType.repeal.getKey() == summaryVO.getTrackType()
				|| WorkflowTraceEnums.workflowTrackType.step_back_repeal.getKey() == summaryVO.getTrackType()) {
			summaryVO.setReadonly(true);
		}
		ContentViewRet ret = (ContentViewRet) AppContext.getRequestContext("contentContext");
		ret.setContentSenderId(summary.getStartMemberId());
		// 设置意见评论是否可以回复
		if (summaryVO.isReadonly() || "edocStatics".equals(openFrom)) {
			ret.setCanReply(false);
		}

		// 控制表单查询：采用缩小权限策略：只要是设置了隐藏，不管有没有权限都隐藏（谭敏峰）
		if (EdocOpenFrom.formQuery.name().equals(openFrom) || EdocOpenFrom.formStatistical.name().equals(openFrom)) {
			AppContext.putThreadContext(Comment.THREAD_CTX_NO_HIDDEN_COMMENT, "true");
		}

		// 控制隐藏的评论对发起人可见
		AppContext.putThreadContext(Comment.THREAD_CTX_NOT_HIDE_TO_ID_KEY, summary.getStartMemberId());
		if (!EdocOpenFrom.supervise.name().equals(openFrom) && !EdocOpenFrom.repealRecord.name().equals(openFrom)) {
			AppContext.putThreadContext(Comment.THREAD_CTX_DOCUMENT_AFFAIR_MEMBER_ID, affair.getMemberId());
		}

		if (EdocOpenFrom.glwd.name().equals(openFrom)) {
			List<Long> memberIds = affairManager.getAffairMemberIds(ApplicationCategoryEnum.edoc, summary.getId());
			AppContext.putThreadContext(Comment.THREAD_CTX_PROCESS_MEMBERS, Strings.isNotEmpty(memberIds) ? memberIds : new ArrayList<Long>());
		}
		if (Integer.valueOf(EdocConstant.flowState.finish.ordinal()).equals(summary.getState())
				|| Integer.valueOf(EdocConstant.flowState.terminate.ordinal()).equals(summary.getState()) || EdocOpenFrom.glwd.name().equals(openFrom)
				|| summaryVO.isReadonly()) {
			AppContext.putThreadContext("_isffin", "1");
		}
		long l3 = System.currentTimeMillis();
		LOGGER.info("设置公文意见界面设置意见开关耗时：" + (l3 - l2));
		
		// 获取新建节点权限
		fillNiwenNode(summaryVO);
		long l4 = System.currentTimeMillis();
		LOGGER.info("设置公文意见界面获取新建节点权限耗时：" + (l4 - l3));
	}
	
	@Override
	public boolean lookEnable(Long affairId, Long userId) throws BusinessException {
		if (affairId != null) {
			CtpAffair valAffair = affairManager.get(affairId);
			return null != valAffair && valAffair.getMemberId().equals(userId);
		}
		return false;
	}	
	/*************************** 33333 公文查看界面参数 start ***************************/
	
	
	/*************************** 44444 查看界面处理保存 start ***************************/
	@Override
	public void transFinishWorkItem(GovdocDealVO finishVO) throws BusinessException {
		CtpAffair affair = finishVO.getAffair();
		EdocSummary summary = finishVO.getSummary();
		
		//处理时更新affair的续办扩展字段
		govdocContinueManager.setCustomAffairExt(finishVO);
		
		//保存督办设置
		govdocPubManager.saveSuperviseByDeal(summary, affair);
		
		//在保存summary前设置 一些属性值，防止后续再update影响性能
		govdocPubManager.fillSummaryBeforeSave(finishVO);
		
		//提交或暂存公共部分业务
		transFinishWorkItemPublic(finishVO, ColHandleType.finish);
		
		// 分送
		if (finishVO.isFensong()) {
			transDistribute(finishVO);
		}
		// 签收
		if (finishVO.isSign()) {
			transSign(finishVO);
		}
		
		//联合发文
		transJointlyIssued(finishVO);
	}
	
	@Override
	public void transDoZcdb(GovdocDealVO dealVo) throws BusinessException {
		CtpAffair affair = dealVo.getAffair();
		
		// 更新Affair的状态为暂存待办
		affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
		affair.setSubState(SubStateEnum.col_pending_ZCDB.key());
		affairManager.updateAffair(affair);
		
		//保存督办设置
		govdocPubManager.saveSuperviseByZcdb(dealVo.getSummary(), affair);

		//提交或暂存公共部分业务
		dealVo.setAction("zcdb");
		transFinishWorkItemPublic(dealVo, ColHandleType.wait);
	}
	
	/**
	 * 协同处理提交和暂存待办公用的方法
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void transFinishWorkItemPublic(GovdocDealVO dealVo,  ColHandleType handleType, Object... param) throws BusinessException {
		EdocSummary summary = dealVo.getSummary();
		CtpAffair affair = dealVo.getAffair();
		Comment comment = dealVo.getComment();
		if (handleType.ordinal() == ColHandleType.wait.ordinal()) {
			dealVo.setAction("zcdb");
			dealVo.setComment(comment);
		} else {
			dealVo.setAction("deal");
		}
		//添加 代录判断
		String pishiFlag = govdocPishiManager.checkLeaderPishi(AppContext.currentUserId(), affair.getMemberId());
		if("pishi".equals(pishiFlag) && "2".equals(dealVo.getLeaderPishiFlag())){
			comment.setContent(comment.getContent()+"(由"+AppContext.getCurrentUser().getName()+"代录)");
		}
		Map<String, String> colSummaryDomian = (Map<String, String>) ParamUtil.getJsonDomain("colSummaryData");

		// 保存附件
		String _flowPermAccountId = colSummaryDomian.get("flowPermAccountId");
		AttachmentEditUtil attUtil = new AttachmentEditUtil("attActionLogDomain");
		if (attUtil.hasEditAtt()) {
			govdocContentManager.saveAttachment(summary, affair,comment.getId());
		}
		Long flowPermAccountId = Strings.isBlank(_flowPermAccountId) ? summary.getOrgAccountId() : Long.valueOf(_flowPermAccountId);

		// 同步协同数据到公文
		if (Strings.isNotBlank(colSummaryDomian.get("jianbanType"))) {
			summary.setJianbanType(Integer.parseInt(colSummaryDomian.get("jianbanType")));
		}
		
		dealVo.setLastNode("true".equals(colSummaryDomian.get("workflow_last_input")));
		dealVo.setSummary(summary);
		dealVo.setSummaryId(summary.getId());
		dealVo.setCurrentUser(AppContext.getCurrentUser());
		dealVo.setFlowPermAccountId(flowPermAccountId);
		Map para = ParamUtil.getJsonDomain("markParam");
		if(!GovdocUtil.isH5() && para!=null && para.size()>0) {
			GovdocMarkHelper.fillSaveMarkParameter(dealVo, null);
		} else {
			GovdocMarkHelper.fillSaveMarkH5Parameter(dealVo, null);
		}
		
		// 处理映射关系 同步数据
		govdocPubManager.fillSummaryByMapping(dealVo);
		
		//保存公文文号
		govdocMarkManager.saveDealMark(dealVo);
		summary = dealVo.getSummary();

		// 为当前待办affair同步紧急程度
		affair.setImportantLevel(summary.getImportantLevel());
		// 检查协同标题
		//govdocFormManager.checkCollSubject(summary, affair, orgManager.getMemberById(AppContext.currentUserId()));
		if (null != summary.getFormAppid()) {
			FormBean formBean = govdocFormManager.getForm(summary.getFormAppid());
			if (null != formBean && GovdocUtil.isGovdocForm(formBean.getFormType())) {
				
				//保存暂存/处理意见
				govdocCommentManager.finishOpinion(summary, affair, formBean, comment, "finish", handleType, colSummaryDomian.get("chooseOpinionType"));

				// 处理拟办意见
				comment = govdocCommentManager.saveDealNibanComment(dealVo,comment, affair);
			}
			
			// 保存意见
			if (comment != null) {
				// 草稿意见先删除原来的的意见.需要从数据库里面查询
				if (comment.getId() != null) {
					CtpCommentAll c = govdocCommentManager.getDrfatComment(affair.getId());
					if (c != null) {
						govdocCommentManager.deleteComment(ModuleType.edoc, c.getId());
						attachmentManager.deleteByReference(summary.getId(), c.getId());
					}
				}
				// 不在这里面发消息，单独发消息
				comment.setPushMessage(false);
				comment.setId(UUIDLong.longUUID());
				if("{}".equals(comment.getRelateInfo())){//360极速提交问题。。
					comment.setRelateInfo(null);
				}
				govdocCommentManager.insertComment(comment, affair);
				// 保存批示编号相关内容
				if("fenban".equals(affair.getNodePolicy()) || handleType.ordinal() != ColHandleType.wait.ordinal()){
					govdocPishiManager.saveLeaderPishiNo(dealVo);
				}
				
				CollaborationAddCommentEvent commentEvent = new CollaborationAddCommentEvent(this);
				commentEvent.setCommentId(comment.getId());
				EventDispatcher.fireEvent(commentEvent);
			}
		}
		// 处理后归档
		String pigeonholeValue = dealVo.getPigeonholeValue();
		
		if (handleType.ordinal() == ColHandleType.wait.ordinal()) {// 暂存待办
			//暂存待办流程
			govdocWorkflowManager.zcdbRuncase(handleType, dealVo);
			// 记录暂存待办操作日志
			govdocLogManager.insertProcessLog(AppContext.getCurrentUser(), Long.parseLong(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.zcdb);
			// 暂存待办 发送消息
			govdocMessageManager.sendMessage4Zcdb(affair, comment);
		} else {//提交
			//提交流程
			WFInfo wfIinfo = govdocWorkflowManager.finishRuncase(handleType, dealVo);
			//记录提交操作日志
			List<ProcessLogDetail> allProcessLogDetailList= wapi.getAllWorkflowMatchLogAndRemoveCache();
			List<ProcessLogDetail> processLogDetails= wfIinfo.getProcessLogDetails();
            if(null!=processLogDetails && !processLogDetails.isEmpty()){
            	allProcessLogDetailList.addAll(processLogDetails);
            }
			//govdocLogManager.insertProcessLog(AppContext.getCurrentUser(), Long.parseLong(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.commit, wfIinfo.getMemberAndPolicys());
			govdocLogManager.insertProcessLog(AppContext.getCurrentUser(), Long.parseLong(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.commit,allProcessLogDetailList,wfIinfo.getMemberAndPolicys());
			if (Strings.isNotBlank(pigeonholeValue)) {
				govdocDocManager.transPigeonhole(summary, affair, Long.parseLong(pigeonholeValue), "handle");
			}
			if (Strings.isNotBlank(wfIinfo.getSubmitConfirmMsg())) {
				govdocMessageManager.sendSubmitConfirmMessage(summary.getId(), affair.getMemberId(), affair.getActivityId(), wfIinfo.getSubmitConfirmMsg());
			}
			
		}

		// 1.保存跟踪,流程不是结束状态就保存跟踪信息,流程已经结束了就不保存跟踪信息
		// 2.当前节点就是最后一个节点/流程已经结束后面的只会节点：设置为结束状态。
		boolean isFinished = Integer.valueOf(EdocConstant.flowState.finish.ordinal()).equals(summary.getState()) || Integer.valueOf(EdocConstant.flowState.terminate.ordinal()).equals(summary.getState());
		if (isFinished) {
			affair.setFinish(true);
			superviseManager.updateStatusBySummaryIdAndType(SuperviseEnum.superviseState.supervised, summary.getId(), SuperviseEnum.EntityType.govdoc);
			govdocPubManager.updateEdocSummaryTransferStatus(summary.getId(), TransferStatus.sendEnd);
			govdocExchangeManager.transUpdateLianheStatus(summary.getId(), ExchangeDetailStatus.ended);
		} else {
			govdocTrackManager.saveTrackInfo(dealVo, affair);
			govdocExchangeManager.transUpdateLianheStatus(summary.getId(), ExchangeDetailStatus.beingProcessed);
		}
		
		Map<String, Object> extParam = GovdocAffairHelper.createExtParam(summary);
		GovdocAffairHelper.addAffairExtParam(affair, extParam);
		affairManager.updateAffair(affair);

		// 更新当前处理人信息
		GovdocHelper.updateCurrentNodesInfo(summary, false);
		// 保存summary
		govdocSummaryManager.updateEdocSummary(summary);
		
		// 更新公文统计数据
		govdocStatManager.saveOrUpdateEdocStat(summary, dealVo.getCurrentUser());
		
		// 调用表单万能方法,更新状态，触发子流程等
		govdocFormManager.updateDataState(affair, summary, ColHandleType.finish);
		
		//如果有归档,且不是当前节点归档的 -> 同步归档数据
		if(summary.getHasArchive()) {
			List<CtpAffair> archiveAffair = affairManager.getAffairsByAppAndObjectIdAndArchiveState(ApplicationCategoryEnum.edoc, summary.getId(), true);
			if(!archiveAffair.isEmpty()){
				List<Long> affairIds = new ArrayList<Long>();
				for (CtpAffair ctpAffair : archiveAffair) {
					affairIds.add(ctpAffair.getId());
				}
				if(!affairIds.isEmpty()){
					docApi.updatePigeHoleFile(ApplicationCategoryEnum.edoc.getKey(),summary.getId(),affairIds,AppContext.currentUserId());
				}
			}
		}
		
		// 签收的时候会进行表单全文检索入库，通过这屏蔽了
		if ((!"qianshou".equals(affair.getNodePolicy())) && (!"fenban".equals(affair.getNodePolicy()))) {
			// 全文检索库 入库
			if (AppContext.hasPlugin("index")) {
			    indexApi.update(summary.getId(), ApplicationCategoryEnum.edoc.getKey());
			}
		}
		
		// 加签、减签、知会、会签 发送消息，记录操作日志
		Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
		String messageDataListJSON = wfdef.get("process_message_data");
		govdocMessageManager.sendOperationTypeMessage(messageDataListJSON, summary, affair ,comment.getId());
		
		// 发送自动跳过的事件
		GovdocHelper.fireAutoSkipEvent(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void transDoZcdbByPdf(GovdocDealVO dealVo) throws BusinessException {
		CtpAffair affair = dealVo.getAffair();
		// 更新Affair的状态为暂存待办
		affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
		affair.setSubState(SubStateEnum.col_pending_ZCDB.key());
		affairManager.updateAffair(affair);
		
		// 从request对象中对象中获取意见
		Comment comment = dealVo.getComment();
		EdocSummary summary = dealVo.getSummary();
		
		dealVo.setAction("zcdb");
		dealVo.setLastNode(false);
		dealVo.setSummary(summary);
		dealVo.setSummaryId(summary.getId());
		dealVo.setCurrentUser(AppContext.getCurrentUser());
		GovdocMarkHelper.fillSaveMarkParameter(dealVo, null);
		
		// 处理映射关系 同步数据
		govdocPubManager.fillSummaryByMapping(dealVo);
		
		//保存公文文号
		govdocMarkManager.saveDealMark(dealVo);
		
		// 保存附件
		Map<String, String> colSummaryDomian = (Map<String, String>) ParamUtil.getJsonDomain("colSummaryData");
		AttachmentEditUtil attUtil = new AttachmentEditUtil("attActionLogDomain");
		if (attUtil.hasEditAtt()) {
			govdocContentManager.saveAttachment(summary, affair,comment.getId());
		}
		
		// 保存意见
		if (comment != null) {
			// 草稿意见先删除原来的的意见.需要从数据库里面查询
			if (comment.getId() != null) {
				CtpCommentAll c = govdocCommentManager.getDrfatComment(affair.getId());
				if (c != null) {
					govdocCommentManager.deleteComment(ModuleType.edoc, c.getId());
					attachmentManager.deleteByReference(summary.getId(), c.getId());
				}
			}
			// 不在这里面发消息，单独发消息
			comment.setPushMessage(false);
			comment.setId(UUIDLong.longUUID());
			comment.setCtype(CommentType.draft.getKey());
			govdocCommentManager.insertComment(comment, affair);
		}
		
		// 同步协同数据到公文
		if (affair.getApp() == ApplicationCategoryEnum.edoc.getKey()) {
			if (Strings.isNotBlank(colSummaryDomian.get("jianbanType"))) {
				summary.setJianbanType(Integer.parseInt(colSummaryDomian.get("jianbanType")));
			}

			// 为当前待办affair同步紧急程度
			affair.setImportantLevel(summary.getImportantLevel());
			// 如果为签发节点，并且没有绑定签发人控件，提交后应签发人为当前人
			if ("true".equals(PropertiesConfiger.getInstance().getProperty("system.gz.edition"))) {
				if ("qianfa".equals(affair.getNodePolicy()) && "finish".equals(ColHandleType.save.toString())) {
					if (summary.getIssuer() == null) {
						summary.setIssuer(AppContext.getCurrentUser().getName());
						summary.setSigningDate(new Date());
						DBAgent.update(summary);
					}
				}
			}
		}
		affairManager.updateAffair(affair);
	}
	
	@Override
	public boolean transStepBack(GovdocDealVO dealVo) throws BusinessException {
		try {
			if (!govdocPubManager.fillStepBackObj(dealVo)) {
				return false;
			}
			EdocSummary summary = dealVo.getSummary();
			CtpAffair currentAffair = dealVo.getAffair();
			Comment comment = dealVo.getComment();
			
			List<CtpAffair> affairss = this.affairManager.getAffairs(currentAffair.getObjectId(),StateEnum.col_sent);
			
			if(dealVo.getTemplate() != null && Strings.isNotBlank(dealVo.getTemplate().getColSubject())) {
				String dynamicSubject = govdocFormManager.makeSubject(dealVo.getTemplate(), summary);
				dealVo.getSummary().setDynamicSubject(dynamicSubject);
			}
			
			// 保存回退意见
			dealVo.setComment(govdocCommentManager.saveOrUpdateComment(comment));
			// 保存批示编号相关内容
			govdocPishiManager.saveLeaderPishiNo(dealVo);
			// String[0]为0回退成功 1回退至撤销
			String[] result = govdocWorkflowManager.stepBackCase(dealVo);
			dealVo.setNodePolicy(result[1]);

			if ("-2".equals(result[0])) {// 流程不允许回退
				StringBuilder errorMsg = new StringBuilder();
				errorMsg.append("《").append(summary.getSubject()).append("》").append("\n");
				dealVo.setErrorMsg(ResourceUtil.getString("collaboration.takeBack.alert", errorMsg.toString()));
				return false;
			} else if ("-1".equals(result[0])) { // 表示流程已结束，不能回退
				StringBuilder errorMsg = new StringBuilder();
				errorMsg.append("《").append(summary.getSubject()).append("》").append("\n");
				dealVo.setErrorMsg(ResourceUtil.getString("collaboration.takeBack.alert.dimission", errorMsg.toString()));
				return false;
			}

			// 是否回退到发起节点
			boolean backToSender = "1".equals(result[0]);
			// 正常回退成功 或者 回退导致撤销，正常撤销成功
			if ("0".equals(result[0]) || backToSender) {
				//更新当前待办节点状态(以防当前待办事项状态未修改)
				/* 以下逻辑会导致 某些在GovdocWorkflowEventListener更新的字段被覆盖掉 如backfromId
				 * if("0".equals(result[0])) {
					currentAffair.setState(StateEnum.col_stepBack.key());
					currentAffair.setSubState(SubStateEnum.col_normal.key());
				} else {
					currentAffair.setState(StateEnum.col_cancel.key());
					currentAffair.setSubState(SubStateEnum.col_normal.key());
				}
				currentAffair.setUpdateDate(DateUtil.currentDate());
				affairManager.updateAffair(currentAffair);*/
				// 保存附件
				govdocContentManager.saveAttDatas(dealVo);
				// 记录操作时间
				affairManager.updateAffairAnalyzeData(currentAffair);
				// 流程回退事件
				CollaborationStepBackEvent backEvent = new CollaborationStepBackEvent(this);
				backEvent.setSummaryId(summary.getId());
				EventDispatcher.fireEvent(backEvent);
				// 发送回退消息
				List<CtpAffair> trackingAffairLists = affairManager.getValidTrackAffairs(summary.getId());
				govdocExchangeManager.exchangeReturn(summary.getId(), currentAffair, comment.getContent());
				govdocMessageManager.sendStepBackMessage(trackingAffairLists, currentAffair, summary.getId(), comment, "1".equals(dealVo.getIsWFTrace()), backToSender);
				// 记录回退日志
				govdocLogManager.saveStepbackLog(dealVo);
				// 流程撤销会回调transRepealCallback，删除全文检索，删除待办人，这里不再处理
				// 普通节点间回退，需更新当前待办人、全文检查
				if ("0".equals(result[0])) {
					GovdocHelper.updateCurrentNodesInfo(summary, false);
					govdocSummaryManager.updateEdocSummary(summary,false);
					//取回的时候删除续办人员
					govdocContinueManager.deleteCustomDealWidth(currentAffair);
					// 更新全文检索
					if (AppContext.hasPlugin("index")) {
					    indexApi.update(summary.getId(), ApplicationCategoryEnum.edoc.getKey());
					}
				}
				
				/*
				 * try { List<Comment> commentList =
				 * this.commentManager.getCommentAllByModuleId
				 * (ModuleType.collaboration, affair.getObjectId());
				 * formManager.
				 * updateDataState(summary,affair,"1".equals(result[0]) ?
				 * ColHandleType.repeal : ColHandleType.stepBack,commentList); }
				 * catch (Exception e) { LOGGER.error("更新表单相关信息异常",e); throw new
				 * BusinessException("更新表单相关信息异常",e); } }
				 */
				// 续办回退删除续办节点
				govdocContinueManager.deleteCustomDealWithStepBack(dealVo,affairss,result[0],"");
			} else {
				return false;
			}
		} finally {
			// govdocWorkflowManager.releaseWorkFlowProcessLock(deal.getProcessId(),
			// String.valueOf(AppContext.currentUserId()));
		}
		return true;
	}

	@Override
	public boolean transAppointStepBack(GovdocDealVO dealVo) throws BusinessException {
		EdocSummary summary = dealVo.getSummary();
		CtpAffair currentAffair = dealVo.getAffair();
		Comment comment = dealVo.getComment();
		//添加对代录的判断
		if(!dealVo.getCurrentUser().getId().equals(currentAffair.getMemberId()) ){
			String pishiFalg = govdocPishiManager.checkLeaderPishi(dealVo.getCurrentUser().getId(), currentAffair.getMemberId());
			if("pishi".equals(pishiFalg)){
				comment.setContent(comment.getContent()+"(由"+dealVo.getCurrentUser().getName()+"代录)");
				dealVo.setComment(comment);
			}
		}
		dealVo.setComment(govdocCommentManager.saveOrUpdateComment(comment));
		// 批示编号同步保存到批示编号表
		govdocPishiManager.saveLeaderPishiNo(dealVo);
		dealVo.setSenderAffair(affairManager.getSenderAffair(summary.getId()));
		
		// 更行附件和正文, 顺序很关键， 不要放到下面
		govdocContentManager.saveAttDatas(dealVo);
		
		// 受影响的事项
		List<CtpAffair> affairList = affairManager.getValidAffairs(ApplicationCategoryEnum.edoc, summary.getId());
		List<CtpAffair> validAffairList = new ArrayList<CtpAffair>();
		validAffairList.addAll(affairList);
		//设置动态标题
		if(dealVo.getTemplate() != null && Strings.isNotBlank(dealVo.getTemplate().getColSubject())) {
			String dynamicSubject = govdocFormManager.makeSubject(dealVo.getTemplate(), summary);
			dealVo.getSummary().setDynamicSubject(dynamicSubject);
		}
		//设置公文的紧急程度
		if(currentAffair.getImportantLevel()!=null){
			dealVo.getSummary().setImportantLevel(currentAffair.getImportantLevel());
		}
		//指定回退-流程操作
		String[] retValue = govdocWorkflowManager.appointStepBackCase(dealVo);
		dealVo.setSelectTargetNodeName(retValue[2]);
		// 指定回退给发起人
		if ("start".equals(dealVo.getSelectTargetNodeId())) {
			if ("1".equals(dealVo.getSubmitStyle())) {
				/** 直接提交给我 */
				// 把发起人的状态改为(是代办还是草稿箱什么的)
				CtpAffair affair = affairManager.getSenderAffair(dealVo.getSummaryId());
				affair.setSubState(SubStateEnum.col_pending_specialBacked.getKey());
				affair.setState(StateEnum.col_waitSend.getKey());
				affair.setUpdateDate(new java.util.Date());
				affair.setDelete(Boolean.FALSE);
				affair.setPreApprover(dealVo.getCurrentUser().getId());
				this.affairManager.updateAffair(affair);
			} else if ("0".equals(dealVo.getSubmitStyle())) {
				/** 流程重走 */
				// 将summary的状态改为待发,撤销已生成事项
				if (affairList != null) {
					for (int i = 0; i < affairList.size(); i++) {
						CtpAffair affair = (CtpAffair) affairList.get(i);
						boolean isSent = affair.getState().intValue() == StateEnum.col_sent.key() || affair.getState().intValue() == StateEnum.col_waitSend.key();
						if (isSent) {
							affair.setState(StateEnum.col_waitSend.key());
							affair.setSubState(SubStateEnum.col_pending_specialBackToSenderCancel.key());
							affair.setDelete(false);
							affair.setPreApprover(dealVo.getCurrentUser().getId());
							affairManager.updateAffair(affair);
						}
					}
				}
			}
		}
		if("1".equals(dealVo.getSubmitStyle())){//流程提交给我时 需要添加一条AFFAIR 用于查询 回退撤销记录
			CtpAffair affairRelease = new CtpAffair();
			com.seeyon.ctp.util.BeanUtils.convert(affairRelease,currentAffair);
            affairRelease.setUpdateDate(new Date());
            affairRelease.setState(StateEnum.col_stepBack.key());
            affairRelease.setSubState(SubStateEnum.col_normal.key());
            affairRelease.setBackFromId(currentAffair.getMemberId());//设置回退人的回退记录
            affairRelease.setNewId();
            this.affairManager.save(affairRelease);
		}

		if ("0".equals(dealVo.getSubmitStyle())) {
			//指定给我-流程重走时，需要更新当前待办人
			GovdocHelper.updateCurrentNodesInfo(summary, false);
			govdocSummaryManager.updateEdocSummary(summary,false);// 更新一下当前待办人信息
		
			//指定回退-流程重走时，删除续办节点
			govdocContinueManager.deleteCustomDealWithStepBack(dealVo, null, "", "Appoint");
		}
		
		// 暂存待办，记录第一次操作时间
		affairManager.updateAffairAnalyzeData(currentAffair);

		//记录指定回退日志
		govdocLogManager.saveAppointStepbackLog(dealVo);
		
		// 更新全文检索
		if (AppContext.hasPlugin("index")) {
		    indexApi.update(summary.getId(), ApplicationCategoryEnum.edoc.getKey());
		}
		
		// 指定回退事件(公文指定回退不调用协同事件)
//		CollaborationAppointStepBackEvent backEvent = new CollaborationAppointStepBackEvent(this);
//		backEvent.setSelectTargetNodeId(dealVo.getSelectTargetNodeId());
//		backEvent.setSummaryId(summary.getId());
//		EventDispatcher.fireEvent(backEvent);

		// 发送消息
		dealVo.setValidAffairList(validAffairList); 
		govdocMessageManager.sendAppointStepBackMsg(dealVo);
		GovdocHelper.updateCurrentNodesInfo(summary, true);
		return true;
	}

	@Override
	public boolean transStepStop(GovdocDealVO dealVo) throws BusinessException {
		Comment comment = dealVo.getComment();

		Long affairId = dealVo.getAffairId();
		CtpAffair affair = dealVo.getAffair();
		EdocSummary summary = dealVo.getSummary();
		User user = dealVo.getCurrentUser();
		
		// 如果是交换流程并且是签收
		GovdocExchangeDetail govdocExchangeDetail = govdocExchangeManager.findDetailBySummaryId(summary.getId());
		if (govdocExchangeDetail != null) {
			GovdocExchangeMain main = govdocExchangeManager.getGovdocExchangeMainById(govdocExchangeDetail.getMainId());
			if (main.getType() != GovdocExchangeMain.EXCHANGE_TYPE_JIAOHUAN) {// 交换没有终止
				govdocExchangeManager.GovdocExchangeStop(govdocExchangeDetail);
			}else{
				govdocExchangeManager.exchangeSign(dealVo);
			}
		}
		// 由代理人终止需要写入处理人ID
		Map<String, Object> columnValue = new HashMap<String, Object>();
		if (affairId != null) {
			if (!user.isAdmin() && !user.getId().equals(affair.getMemberId())) {
 				columnValue.put("transactorId", user.getId());
 			}
			if (!columnValue.isEmpty() && columnValue.size() > 0) {
				affairManager.update(affairId, columnValue);
			}
		}

		// 保存附件
		AttachmentEditUtil attUtil = new AttachmentEditUtil("attActionLogDomain");
		if (attUtil.hasEditAtt()) {
			govdocContentManager.saveAttachment(summary, affair,comment.getId());
		}

		// 设置消息推送
		DateSharedWithWorkflowEngineThreadLocal.setPushMessageMembers(comment.getPushMessageToMembersList());
		// 保存终止时的意见
		comment = govdocCommentManager.saveOrUpdateComment(comment);

		// 保存意见
		FormBean fromBean = null;
		if (null != summary.getFormAppid()) {
			fromBean = govdocFormManager.getForm(summary.getFormAppid());
			if (fromBean!=null && fromBean.isGovDocForm()) {
				govdocCommentManager.finishOpinion(summary, affair, fromBean, comment, "finish", ColHandleType.stepStop, "");
			}
		}
		
		// 将终止流程的当前summary放入ThreadLocal，便于工作流中发送消息时获取代理信息。
		DateSharedWithWorkflowEngineThreadLocal.setColSummary(summary);
		// 添加意见
		DateSharedWithWorkflowEngineThreadLocal.setFinishWorkitemOpinionId(comment.getId(), false, comment.getContent(), 2, false);

		// 调用表单万能方法,更新状态，触发子流程等
		govdocFormManager.updateDataState(affair, summary, ColHandleType.stepStop);
		
		govdocWorkflowManager.stepStopCase(user, affair.getSubObjectId(), null, null);
		
		// 工作流中更新了状态信息，重新获取，表单会用state字段
		summary = (EdocSummary) DateSharedWithWorkflowEngineThreadLocal.getColSummary();
		
		// 终止 流程结束时更新督办状态
		superviseManager.updateStatusBySummaryIdAndType(SuperviseEnum.superviseState.supervised, summary.getId(), SuperviseEnum.EntityType.govdoc);
		
		// 记录流程日志
		if (user.isAdministrator() || user.isGroupAdmin() || user.isSystemAdmin()) {
			govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()), 1l, ProcessLogAction.stepStop);
		} else {
			govdocLogManager.insertProcessLog(user, Long.parseLong(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.stepStop);
		}
		
		// 记录应用日志
		govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_STOP.key(), user.getName(), summary.getSubject());
		
		// 流程正常结束通知
		CollaborationStopEvent stopEvent = new CollaborationStopEvent(this);
		stopEvent.setSummaryId(summary.getId());
		stopEvent.setUserId(user.getId());
		EventDispatcher.fireEvent(stopEvent);

		// 若做过指定回退的操作.做过回退发起者则被回退者的状态要从待发改为已发
		CtpAffair sendAffair = affairManager.getSenderAffair(summary.getId());
		if (sendAffair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey()
				|| sendAffair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
			sendAffair.setState(StateEnum.col_sent.getKey());
			sendAffair.setUpdateDate(new java.util.Date());
			affairManager.updateAffair(sendAffair);
		}
		
		summary.setState(EdocConstant.flowState.terminate.ordinal());
		govdocStatManager.saveOrUpdateEdocStat(summary, user);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String transfer(Map<String, String> params) throws BusinessException {
		String message = "";
		User user = AppContext.getCurrentUser();
		Long affairId = Long.valueOf(params.get("affairId"));
		CtpAffair ctpAffair = affairManager.get(affairId);
		EdocSummary summary = null;
		try {
			//clone的目的是未了避免hibernate缓存
			summary = (EdocSummary) govdocSummaryManager.getSummaryById(ctpAffair.getObjectId()).clone();
			summary.setId(ctpAffair.getObjectId());
		} catch (CloneNotSupportedException e1) {
			LOGGER.error(e1);
			return "移交失败";
		}
		GovdocBaseVO baseVo = new GovdocBaseVO();
		baseVo.setSummary(summary);
		//同步summary数据
		govdocPubManager.fillSummaryByMapping(baseVo);
		//处理affair标题
		//govdocFormManager.checkCollSubject(summary, ctpAffair, orgManager.getMemberById(AppContext.currentUserId()));
		
		// 1、保存意见、附件

		// 从request对象中对象中获取意见
		Comment comment = govdocCommentManager.getCommnetFromRequest(null, ctpAffair.getMemberId(), ctpAffair.getObjectId());

		// 保存意见
		comment.setModuleId(summary.getId());
		comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
		comment.setCreateId(ctpAffair.getMemberId());
		comment.setAffairId(ctpAffair.getId());
		comment.setExtAtt3("collaboration.dealAttitude.transfer");
		if (!user.getId().equals(ctpAffair.getMemberId())) {
			comment.setExtAtt2(user.getName());
		}
		comment.setModuleType(ModuleType.edoc.getKey());
		comment.setPid(0L);

		/**
		 * m3转办过来的意见取得方式和PC不同
		 */
		Comment m3Comment = (Comment) AppContext.getRequestContext("m3Comment");
		if (m3Comment != null) {
			AppContext.removeSessionArrribute("m3Comment");
			comment = m3Comment;
			comment.setExtAtt3("collaboration.dealAttitude.transfer");
			if (!user.getId().equals(ctpAffair.getMemberId())) {
				comment.setExtAtt2(user.getName());
			}
			govdocCommentManager.saveOrUpdateComment(comment);
		} else {
			govdocCommentManager.saveOrUpdateComment(comment);
			// 保存附件
			govdocContentManager.saveAttDatas(user, summary, ctpAffair, comment.getId());
		}

		// 2、转办流程
		// 转给指定人id
		Long transferMemberId = Long.valueOf(params.get("transferMemberId"));
		String processId = summary.getProcessId();

		try {
			// *****判断当前转给指定人是否有效开始***********
			V3xOrgMember orgMember = orgManager.getMemberById(transferMemberId);
			if (!orgMember.isValid()) {
				message = "转办指定人无效，请重新指定！";
				return message;
			}

			// 删除原来的定时任务（超期提醒、提前提醒）
			if (ctpAffair.getRemindDate() != null && ctpAffair.getRemindDate() != 0) {
				QuartzHolder.deleteQuartzJob("Remind" + ctpAffair.getId());// 保证老数据不报错,
																			// 6.0后面的版本把这个删除
				QuartzHolder.deleteQuartzJob("Remind" + ctpAffair.getObjectId() + "_" + ctpAffair.getActivityId());
			}
			if ((ctpAffair.getDeadlineDate() != null && ctpAffair.getDeadlineDate() != 0) || ctpAffair.getExpectedProcessTime() != null) {
				QuartzHolder.deleteQuartzJob("DeadLine" + ctpAffair.getId());// 保证老数据不报错,
																				// 6.0后面的版本把这个删除
				QuartzHolder.deleteQuartzJob("DeadLine" + ctpAffair.getObjectId() + "_" + ctpAffair.getActivityId());
			}

			List<V3xOrgMember> nextMembers = new ArrayList<V3xOrgMember>();
			nextMembers.add(orgMember);
			// *****判断当前转给指定人是否有效结束***********

			// *****开始修改事项和转给指定人***************
			// 判断当前转给的人是否为空
			String newAffairMemeerId = "";
			if (nextMembers.size() > 0) {
				LOGGER.info("转办参数： 转办人：" + orgMember.getName() + ",id=" + orgMember.getId());
				LOGGER.info("转办参数：processId：" + processId + ",id=" + orgMember.getId());
				LOGGER.info("转办参数：affair: memberId=" + ctpAffair.getMemberId() + ",subObjectId=" + ctpAffair.getSubObjectId() + "activityId="
						+ ctpAffair.getActivityId());

				DateSharedWithWorkflowEngineThreadLocal.setColSummary(summary);
				Object[] result = govdocWorkflowManager.replaceWorkItemMembers(true, ctpAffair.getMemberId(), processId, ctpAffair.getSubObjectId(), ctpAffair.getActivityId()
						.toString(), nextMembers, true);
				List<WorkItem> workitems = (List<WorkItem>) result[1];
				BPMHumenActivity bpmActivity = (BPMHumenActivity) result[2];
				List<CtpAffair> newAffairs = new ArrayList<CtpAffair>();

				summary = (EdocSummary) DateSharedWithWorkflowEngineThreadLocal.getColSummary();
				for (int i = 0; i < workitems.size(); i++) {
					WorkItem workItem = workitems.get(i);
					CtpAffair newAffair = (CtpAffair) BeanUtils.cloneBean(ctpAffair);
					// 一、替换v3x_affair中的member_id为dealTermUserId

					newAffairMemeerId = workItem.getPerformer();

					newAffair.setMemberId(Long.parseLong(newAffairMemeerId));
					newAffair.setId(UUIDLong.longUUID());
					newAffair.setSubObjectId(workItem.getId());
					newAffair.setCoverTime(false);
					newAffair.setReceiveTime(new Date());
					newAffair.setUpdateDate(new Date());
					newAffair.setDelete(false);
					newAffair.setFromId(user.getId());
					newAffair.setFromType(ChangeType.Transfer.getKey());
					newAffair.setBackFromId(null);
					newAffair.setState(StateEnum.col_pending.getKey());
					newAffair.setSubState(SubStateEnum.col_pending_unRead.getKey());
					newAffair.setOverWorktime(null);
					newAffair.setRunWorktime(null);
					newAffair.setOverTime(null);
					newAffair.setRunTime(null);
					newAffair.setPreApprover(ctpAffair.getMemberId());
					V3xOrgMember nextMember = nextMembers.get(i);
					if (newAffair.getDeadlineDate() != null && newAffair.getDeadlineDate() != 0l) {
						newAffair.setExpectedProcessTime(workTimeManager.getCompleteDate4Nature(new Date(), newAffair.getDeadlineDate(),
								nextMember.getOrgAccountId()));
					}
					Long accountId = GovdocHelper.getFlowPermAccountId(AppContext.currentAccountId(), summary);
					WFComponentUtil.affairExcuteRemind(newAffair, accountId);
					newAffairs.add(newAffair);
				}
				affairManager.saveAffairs(newAffairs);
				// 发送消息和记录日志
				// 五、写流程日志和应用日志
				govdocLogManager.insertProcessLog(user, Long.valueOf(processId), Long.valueOf(bpmActivity.getId()), ProcessLogAction.transfer, comment.getId(),
						user.getName(), orgMember.getName());
				// {0}转办协同《{1}》给{2}
				govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_TRANSFER.key(), user.getName(), summary.getSubject(), orgMember.getName());

				// 发送移交消息
				govdocMessageManager.sendMessage4Transfer(user, summary, newAffairs, ctpAffair, comment);
			}
			ctpAffair.setDelete(true);
			ctpAffair.setActivityId(-1l);
			ctpAffair.setSubObjectId(-1l);
			ctpAffair.setObjectId(-1l);
			ctpAffair.setTempleteId(-1l);
			Long oldMemberId = ctpAffair.getMemberId();
			ctpAffair.setMemberId(-1l);

			// 更新操作时间
			if (ctpAffair.getSignleViewPeriod() == null && ctpAffair.getFirstViewDate() != null) {
				Date nowTime = new Date();
				long viewTime = workTimeManager.getDealWithTimeValue(ctpAffair.getFirstViewDate(), nowTime, ctpAffair.getOrgAccountId());
				ctpAffair.setSignleViewPeriod(viewTime);
			}

			affairManager.updateAffair(ctpAffair);
			// *****结束修改事项和转给指定人***************

//			ColUtil.addOneReplyCounts(summary);
			// 更新当前待办人
			String currentNodesInfo = summary.getCurrentNodesInfo().replaceFirst(String.valueOf(oldMemberId), String.valueOf(newAffairMemeerId));
			summary.setCurrentNodesInfo(currentNodesInfo);
			govdocSummaryManager.saveOrUpdateEdocSummary(summary);
			
			//移交转办、转发、联合发文数据
			govdocExchangeManager.saveTransferByExchange(summary.getId(), Long.parseLong(newAffairMemeerId));
			
		} catch (Exception e) {
			LOGGER.error("移交报错！", e);
		} finally {
			govdocLockManager.colDelLock(summary, ctpAffair);
		}
		return message;
	}
	
	/**
	 * 
	* @Title: recallNewflowSummary
	* @Description: 子流程撤销方法
	* @param caseId
	* @param user
	* @param operationType
	 */
	public int recallNewflowSummary(Long caseId, User user, String operationType) throws BusinessException {
        int result = 0;
        if(user == null){
        	return result;
        }
        EdocSummary summary =govdocSummaryManager.getSummaryByCaseId(caseId);
        if(summary == null){
            return 1;
        }
        //将summary的状态改为待发,撤销已生成事项
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("objectId", summary.getId());
        List<CtpAffair> affairs = affairManager.getValidAffairs(null, map);
        //撤销流程
        List<Long> affairIds = new ArrayList<Long>();
        if(affairs != null){
            for(int i=0;i<affairs.size();i++){
                CtpAffair affair = (CtpAffair) affairs.get(i);
                if(affair.getState().intValue() == StateEnum.col_sent.key()){
                    affair.setState(StateEnum.col_waitSend.key());
                    affair.setSubState(SubStateEnum.col_waitSend_cancel.key());
                    affair.setDelete(true);
                    affairManager.updateAffair(affair);
                }

                if(affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                    QuartzHolder.deleteQuartzJob("Remind" + affair.getId());
                    QuartzHolder.deleteQuartzJob("Remind" + affair.getObjectId() + "_" + affair.getActivityId());
                }

                if((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0)|| affair.getExpectedProcessTime() != null) {
                    QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());
                    QuartzHolder.deleteQuartzJob("DeadLine"  + affair.getObjectId() + "_" + affair.getActivityId());
                }

                //指定回退(回退节点删除多次消息提醒任务)
                WFComponentUtil.deleteCycleRemindQuartzJob(affair, false);

                affairIds.add(affair.getId());
            }

            this.affairManager.updateAffairsState2Cancel(summary.getId());
        }
        //子流程撤销时把数据改为删除状态
        summary.setState(EdocConstant.flowState.deleted.ordinal());
        summary.setCaseId(null);
        DBAgent.update(summary);

        GovdocHelper.deleteQuartzJobOfSummary(summary);

        //删除子流程的预归档
        if(AppContext.hasPlugin("doc")){
            docApi.deleteDocResources(user.getId(), affairIds);
        }
        String operation = "";
        //更明确的消息提示
        if("takeBack".equals(operationType)){
            operation = ResourceUtil.getString("collaboration.takeBack.label");
        }
        else if("stepBack".equals(operationType)){
            operation = ResourceUtil.getString("collaboration.stepBack.label");
        }
        else if("repeal".equals(operationType)){
            operation = ResourceUtil.getString("collaboration.repeal.2.label");
        }
        CtpSuperviseDetail detail = superviseManager.getSupervise(SuperviseEnum.EntityType.govdoc.ordinal(), summary.getId());
        //删除督办信息
        this.superviseManager.updateStatus2Cancel(summary.getId());
        //对发起人以外的所有执行人发消息通知
        try{
            govdocMessageManager.sendMessage4SubProcessRepeal(affairs, summary, detail, operation);
        }catch (Exception e) {
            LOGGER.error("召回新流程公文发送提醒消息异常", e);
            throw new BusinessException("send message failed");
        }
        ProcessLogManager processLogManager=(ProcessLogManager)AppContext.getBean("processLogManager");
        processLogManager.deleteLog(Long.parseLong(summary.getProcessId()));
        //流程撤销事件通知
        CollaborationCancelEvent cancelEvent = new CollaborationCancelEvent(this);
        cancelEvent.setSummaryId(summary.getId());
        cancelEvent.setUserId(user.getId());
        EventDispatcher.fireEvent(cancelEvent);
        return 0;
    }
	/*************************** 44444 查看界面处理保存   end ***************************/
	
	
	/*************************** 55555 公文流程回调方法 start ***************************/
	@Override
	public void transRepealCallback(GovdocBaseVO baseVo) throws BusinessException {
		Comment comment = new Comment();
		comment.setAffairId(baseVo.getAffairId());
		String repealCommentTOHTML = baseVo.getCommentContent();// Strings.toHTML(repealComment,false);
		try {
			User user = AppContext.getCurrentUser();
			CtpAffair currentAffair = baseVo.getAffair(); 
			// 删除公文流程相关定时器任务
			WFComponentUtil.deleteQuartzJob(baseVo.getSummaryId());

			List<CtpAffair> validAffairs = new ArrayList<CtpAffair>();
			// 将summary的状态改为待发,撤销已生成事项
			List<StateEnum> states = new ArrayList<StateEnum>();
			states.add(StateEnum.col_sent);
			states.add(StateEnum.col_pending);
			states.add(StateEnum.col_done);
			states.add(StateEnum.col_waitSend);
			List<CtpAffair> affairs = affairManager.getAffairs(baseVo.getSummaryId(), states);
			if (Strings.isNotEmpty(affairs)) {
				validAffairs.addAll(affairs);

				//参考colmanagerimpl  。updateSenderAffairStateToWaitSend 
				for (CtpAffair affair : affairs) {
					int state = affair.getState().intValue();
					int subState = affair.getSubState().intValue();
					if (state == StateEnum.col_sent.key()) {
						affair.setState(StateEnum.col_waitSend.key());
						if ("stepback".equals(baseVo.getAction())) {
							affair.setSubState(SubStateEnum.col_waitSend_stepBack.key());
						} else {
							affair.setSubState(SubStateEnum.col_waitSend_cancel.key());
						}
						affair.setBackFromId(user.getId());
						affair.setDelete(false);
						affair.setSummaryState(CollaborationEnum.flowState.cancel.ordinal());
						affairManager.updateAffair(affair);
					} else if (state == StateEnum.col_waitSend.key() // 撤销的时候，不应该处理待发的状态
							&& (subState == SubStateEnum.col_pending_specialBacked.key() 
								|| subState == SubStateEnum.col_pending_specialBackToSenderCancel.key())) {
						affair.setSubState(SubStateEnum.col_waitSend_stepBack.key());
						affair.setBackFromId(user.getId());
						affair.setDelete(false);
						affair.setSummaryState(CollaborationEnum.flowState.cancel.ordinal());
						affairManager.updateAffair(affair);
					}
					// 删除节点定时器任务
					if (affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) {
					    WFComponentUtil.deleteQuartzJobForNode(affair);
					}
				}
				
				//将待办已办数据全部设置为撤销状态   copy from colmanagerimpl  。updatePendingAndDoneAffairsToCancel
				updatePendingAndDoneAffairsToCancel(baseVo.getSummaryId(),"stepback".equals(baseVo.getAction()),user,currentAffair);
			}

			EdocSummary summary = govdocSummaryManager.getSummaryById(baseVo.getSummaryId());
			summary.setCaseId(null);
			summary.setState(EdocConstant.flowState.cancel.ordinal());
			summary.setCoverTime(Boolean.FALSE);
			// 更新当前处理人信息
			GovdocHelper.updateCurrentNodesInfo(summary, false);
			govdocSummaryManager.updateEdocSummary(summary,false);
			baseVo.setSummary(summary);
			
			// 撤销公文文号
			govdocMarkManager.saveCancelMark(baseVo);
			// 登记数据撤销-非交换
			if (summary.getGovdocType().intValue() == 2) {
				govdocRegisterManager.saveCancelRegister(summary);
			}
			// 撤销后删除公文统计数据
			govdocStatManager.deleteEdocStat(summary.getId());
			
			// 保存撤销意见,附件
			if ("cancel".equals(baseVo.getAction())) {//撤销时，导致流程撤销
				comment = govdocCommentManager.saveComment4Repeal(comment, repealCommentTOHTML, user, summary, currentAffair);
				// 发送消息 - 撤销
				govdocMessageManager.sendMessage4Repeal(validAffairs, currentAffair, repealCommentTOHTML, "1".equals(baseVo.getIsWFTrace()));
				// 保存撤销日志
				govdocLogManager.saveCancelLog(baseVo);
				// 流程撤销-追溯流程
				//createRepealTraceWfData(summary, validAffairs, currentAffair, baseVo.getIsWFTrace());
			} else {//回退/指定回退时，导致流程撤销
				// 指定回退至发起人，流程追溯显示指定回退
				/*if("1".equals(baseVo.getIsWFTrace())) {//选择了流程追溯
					//默认为：回退至流程撤销，类型为: 回退到发起人
					WorkflowTraceEnums.workflowTrackType traceType = WorkflowTraceEnums.workflowTrackType.step_back_repeal;
					if (Strings.isNotBlank(baseVo.getTrackWorkflowType())) {
						for(workflowTrackType e : workflowTrackType.values()) {
				            if(e.getKey()== Integer.parseInt(baseVo.getTrackWorkflowType())) {
				            	traceType = e;
				            	break;
				            }
				        }
						
					}
					//createRepealData2BeginNode(currentAffair, summary, validAffairs, traceType, baseVo.getIsWFTrace());
				}*/
			}

			//do4Repeal(user, comment.getContent(), baseVo.getSummaryId(), affairs, true);
			List<Long> affairIds = new ArrayList<Long>();
			for (CtpAffair bean : affairs) {
				if ((Integer.valueOf(StateEnum.col_sent.getKey()).equals(bean.getState()) 
						|| Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(bean.getState()))
						&& !bean.isDelete()) {
					continue;
				}
				DateSharedWithWorkflowEngineThreadLocal.addToAllStepBackAffectAffairMap(bean.getMemberId(), bean.getId());
				affairIds.add(bean.getId());
			}
			//删除跟踪数据
			govdocTrackManager.deleteTrackMembersByAffairIds(affairIds);
			//删除部门归档数据
			CtpAffair sendAffair = affairManager.getSenderAffair(baseVo.getSummaryId());
			if (sendAffair != null) {
				affairIds.add(sendAffair.getId());
			}
			if (AppContext.hasPlugin("doc")) {
				docApi.deleteDocResources(user.getId(), affairIds);
			}
			// 删除ISIgnatureHTML专业签章
			govdocSignetManager.deleteAllByDocumentId(baseVo.getSummaryId());
			// 流程撤销到首发，发送流程撤销事件
			CollaborationCancelEvent cancelEvent = new CollaborationCancelEvent(this);
			cancelEvent.setSummaryId(baseVo.getSummaryId());
			cancelEvent.setUserId(user.getId());
			cancelEvent.setMessage(comment.getContent());
			EventDispatcher.fireEvent(cancelEvent);
			
			// 回退到发起节点时，如果有督办则修改督办状态
			this.superviseManager.updateStatus2Cancel(baseVo.getSummaryId());
			
			// 调用表单万能方法,更新状态，触发子流程等
			govdocFormManager.updateDataState(currentAffair, summary, ColHandleType.repeal);
			
			// 全文检索
			if (AppContext.hasPlugin("index")) {
			    indexApi.delete(baseVo.getSummaryId(), ApplicationCategoryEnum.edoc.getKey());
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new BusinessException("", e);
		}
	}
	
	private void updatePendingAndDoneAffairsToCancel(long summaryId,boolean isBackOperation,User user, CtpAffair currentAffair) throws BusinessException {
		Map<String,Object> otherMap = new HashMap<String,Object>();
        //如果是回退那么就设置为回退状态否则就是撤销记录
        List<Integer> cancelStates = new ArrayList<Integer>();
        cancelStates.add(StateEnum.col_pending.key());
        cancelStates.add(StateEnum.col_done.key());
        cancelStates.add(StateEnum.col_pending_repeat_auto_deal.key());
        StringBuilder hql=new StringBuilder();
        hql.append(" update CtpAffair set state=:state,subState=:subState,updateDate=:updateDate"
                 + " ,backFromId=:backFromId,summaryState=:summaryState where objectId=:objectId "
                 + " and state in(:statesList) and app!=:app  ");
        //如果是回退那么就设置为回退状态否则就是撤销记录
        if(isBackOperation){
            otherMap.put("state", StateEnum.col_stepBack.key());
        }else{
            otherMap.put("state", StateEnum.col_cancel.key());
        }
        otherMap.put("objectId",summaryId);
        otherMap.put("subState", SubStateEnum.col_normal.key());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        otherMap.put("updateDate", timestamp);
        otherMap.put("backFromId",user.getId());
        otherMap.put("summaryState", CollaborationEnum.flowState.cancel.ordinal());
        otherMap.put("statesList", cancelStates);
        otherMap.put("app", ApplicationCategoryEnum.stepBackData.key());
        DBAgent.bulkUpdate(hql.toString(), otherMap);
        if (currentAffair.getState() == StateEnum.col_pending.getKey()) {
			if (isBackOperation) {
				currentAffair.setState(StateEnum.col_stepBack.key());
			} else {
				currentAffair.setState(StateEnum.col_cancel.key());
			}
			currentAffair.setSubState(SubStateEnum.col_normal.key());
			currentAffair.setUpdateDate(timestamp);
			currentAffair.setBackFromId(user.getId());
			currentAffair.setSummaryState(CollaborationEnum.flowState.cancel.ordinal());
		}
	}
	/**
	 * 联合发文
	 * @throws BusinessException 
	 */
	@Override
	public void transJointlyIssued(GovdocDealVO dealVo) throws BusinessException {
		if(Strings.isNotBlank(dealVo.getJointlyUnit())){
			ProcessLogManager processLogManager=(ProcessLogManager)AppContext.getBean("processLogManager");
			Map<String, String> unitInfo = GovdocOrgHelper.getUnitNameAndUnitId(dealVo.getJointlyUnit());
			processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(dealVo.getSummary().getProcessId()),
					dealVo.getAffair().getActivityId(),ProcessLogAction.jointlyIssued, unitInfo.get("unitName"));
			
			Map<String, Object> extendParam = new HashMap<String, Object>();
			extendParam.put("exchangeType", GovdocExchangeMain.EXCHANGE_TYPE_LIANHE);
			extendParam.put("sendToId", unitInfo.get("unitId"));
			govdocExchangeManager.exchangeSend(dealVo.getSummary(), dealVo.getCurrentUser().getId(), dealVo.getAffair().getId(), extendParam);
		}
	}

	@Override
	public void transProcessFinishCallback(GovdocBaseVO baseVo) throws BusinessException {
		EdocSummary summary = baseVo.getSummary();
		String action = baseVo.getAction();
		Integer summaryState = "finish".equals(action) ? EdocConstant.flowState.finish.ordinal() : baseVo.getSummary().getState();
		GovdocHelper.setTime2Summary(summary);
		summary.setFinished(true);
		summary.setCompleteTime(new Timestamp(System.currentTimeMillis()));
		summary.setState(summaryState);
		if(!summary.getIsQuickSend()){
			govdocSummaryManager.transSetFinishedFlag(summary);
			
			// 两个更新改成一个
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("summaryState", summary.getState());
			map.put("finish", Boolean.TRUE);
			map.put("track", Integer.valueOf(TrackEnum.no.ordinal()));
			affairManager.update(map, new Object[][]{{"objectId", summary.getId()}});
		}

		//affairManager.updateAffairSummaryState(summary.getId(), summary.getState());
		//affairManager.updateFinishFlag(summary.getId());

		// edocSuperviseManager.updateBySummaryId(baseVo.getSummary().getId());
		// edocStatManager.updateFlowState(summary.getId(),
		// EdocConstant.flowState.finish.ordinal());
		
        //附件归档,流程结束更新
        if(summary.getAttachmentArchiveId() != null &&  AppContext.hasPlugin("doc")) {
        	List<Attachment> attachment = attachmentManager.getByReference(summary.getId(),summary.getId());//标题区附件
        	List<Attachment> formAttachments = formApi4Cap3.getAllFormAttsByModuleId(summary.getId());//表单控件里面的附件
        	User currentUser = AppContext.getCurrentUser();
        	if(Strings.isNotEmpty(formAttachments)) {//归档表单控件中的附件    				
        		for(Attachment _formAtt : formAttachments){
				    if(_formAtt.getType() == com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE.ordinal()){
    				    V3XFile file = fileManager.getV3XFile(_formAtt.getFileUrl());
    					docApi.attachmentPigeonhole(file, summary.getAttachmentArchiveId(), currentUser.getId(), currentUser.getLoginAccount(), false, "", PigeonholeType.edoc_account.ordinal());
				    }
				}
			}
			if(Strings.isNotEmpty(attachment)){//归档标题区附件
				for(Attachment _Att : attachment){
				    if(_Att.getType() == com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE.ordinal()){
				        V3XFile file = fileManager.getV3XFile(_Att.getFileUrl());
				        docApi.attachmentPigeonhole(file, summary.getAttachmentArchiveId(), currentUser.getId(), currentUser.getLoginAccount(), true, "", PigeonholeType.edoc_account.ordinal());
				    }
				}
			}
        }
	}
	
	@Override
	public void transSyncParentMark(Long summaryId, Integer markType, String markstr) throws BusinessException {
		EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
		if(summary == null) {
			return;
		}
		
		if(markType.intValue() == 0) {
			if(Strings.isBlank(markstr) || markstr.equals(summary.getDocMark())) {
				return;
			}
		} else if(markType.intValue() == 1) {
			if(Strings.isBlank(markstr) || markstr.equals(summary.getSerialNo())) {
				return;
			}
		} else if(markType.intValue() == 2) {
			if(Strings.isBlank(markstr) || markstr.equals(summary.getDocMark2())) {
				return;
			}
		}
		
		try {
			FormBean formBean = govdocFormManager.getForm(summary.getFormAppid());
			FormDataMasterBean formDataMasterBean = formApi4Cap3.findDataById(summary.getFormRecordid(), summary.getFormAppid(), null);
	
			Map<String, Object> valueMap = new HashMap<String, Object>();
			
			boolean docMarkIsMapping = false;
			boolean serialNoIsMapping = false;
			boolean signMarkIsMapping = false;
			
			if (null != formBean && null != formDataMasterBean) {
				List<FormFieldBean> formFieldBeans = formBean.getAllFieldBeans();
				for (FormFieldBean formFieldBean : formFieldBeans) {
					if (formFieldBean.isMasterField()) {
						if (formFieldBean.getMappingField() != null) {
							if ("doc_mark".equals(formFieldBean.getMappingField())) {// 公文文号
								if(markType.intValue() == 0) {
									docMarkIsMapping = true;
									valueMap.put(formFieldBean.getName(), markstr);
									summary.setDocMark(markstr);
								}
							} else if ("serial_no".equals(formFieldBean.getMappingField())) {// 公文文号
								if(markType.intValue() == 1) {
									serialNoIsMapping = true;
									valueMap.put(formFieldBean.getName(), markstr);
									summary.setSerialNo(markstr);
								}
							} else if ("sign_mark".equals(formFieldBean.getMappingField())) {// 公文文号
								if(markType.intValue() == 1) {
									signMarkIsMapping = true;
									valueMap.put(formFieldBean.getName(), markstr);
									summary.setDocMark2(markstr);
								}
							}
						} else {//公文文号没有映射
							if ("edocDocMark".equals(formFieldBean.getInputType())) {
								if(markType.intValue() == 0) {
									valueMap.put(formFieldBean.getName(), markstr);
								}
							} else if ("edocInnerMark".equals(formFieldBean.getInputType())) {
								if(markType.intValue() == 1) {
									valueMap.put(formFieldBean.getName(), markstr);
								}
							} else if ("edocSignMark".equals(formFieldBean.getInputType())) {
								if(markType.intValue() == 2) {
									valueMap.put(formFieldBean.getName(), markstr);
								}
							}
						}
					}
				}
				formDataMasterBean.addFieldValue(valueMap);
				govdocFormManager.insertOrUpdateMasterData(formDataMasterBean);
				
				if(docMarkIsMapping || serialNoIsMapping || signMarkIsMapping) {
					govdocSummaryManager.updateEdocSummary(summary);
				}
				
				if(docMarkIsMapping || serialNoIsMapping || signMarkIsMapping) {
					Map<Long, Long> sourceIdMap = new HashMap<Long, Long>();
					sourceIdMap.put(summaryId, summary.getStartUserId());
					
					//修改Affair的文号 
					List<CtpAffair> affairList = affairManager.getAffairs(ApplicationCategoryEnum.edoc, summary.getId());
					if(Strings.isNotEmpty(affairList)) {
						for(CtpAffair affair : affairList) {
							sourceIdMap.put(affair.getId(), affair.getMemberId());
							if(docMarkIsMapping) {
								AffairUtil.addExtProperty(affair, AffairExtPropEnums.edoc_edocMark, summary.getDocMark());
							}
						}
						affairManager.updateAffairs(affairList);
					}
					
					//修改归档公文文号
					govdocDocManager.updateDocMetadata(sourceIdMap);
				}
			}
		} catch(Exception e) {
			LOGGER.error("");
		}
	}

	/**
	 * 回退的时候设置 已发-->待办 子状态 正常 ->（协同-待发-被回退）
	 *
	 * @param userId
	 * @param summaryId
	 * @param from
	 * @return
	 * @throws BusinessException
	 */
	public int stepBackSummary(long userId, long summaryId, int from) throws BusinessException {
		EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
		summary.setCaseId(null);

		// 更新当前处理人信息
		GovdocHelper.updateCurrentNodesInfo(summary, false);
		govdocSummaryManager.updateEdocSummary(summary);

		CtpAffair senderAffair = affairManager.getSenderAffair(summaryId);
		if (senderAffair != null) {
			// 将事项状态改为待发
			senderAffair.setState(StateEnum.col_waitSend.key());
			senderAffair.setSubState(SubStateEnum.col_waitSend_stepBack.key());
			senderAffair.setBackFromId(AppContext.currentUserId());
			senderAffair.setUpdateDate(new Date());
			senderAffair.setArchiveId(null);
			senderAffair.setNodePolicy("fenban");
			// 拟文续办 回退后 移动端打开待发送数据报错 正常回退的待发数据 ActivityId为空chenyq 2017-12-22
			senderAffair.setActivityId(null);
			affairManager.updateAffair(senderAffair);

			// affairManager.updateAffairsStateAndUpdateDate(summaryId);
			// TODO
		}
		return 0;
	}
	/*************************** 55555 公文流程回调方法   end ***************************/
	
	
	/*************************** 66666 公文交换相关方法 start ***************************/
	@Override
	public boolean transFinishWorkItemByDistrubite(GovdocDealVO dealVo) throws BusinessException {
		try {
			EdocSummary summary = dealVo.getSummary();
			CtpAffair affair = dealVo.getAffair();
			Comment comment = dealVo.getComment();
			
			//保存分办意见
			if (comment != null) {
				// 草稿意见先删除原来的的意见.需要从数据库里面查询
				CtpCommentAll c = govdocCommentManager.getDrfatComment(comment.getAffairId());
				if (c != null) {
					govdocCommentManager.deleteComment(ModuleType.edoc, c.getId());
					attachmentManager.deleteByReference(dealVo.getSummary().getId(), c.getId());
				}
				
				// 不在这里面发消息，单独发消息
				comment.setPushMessage(false);
				comment.setId(UUIDLong.longUUID());
				govdocCommentManager.insertComment(comment, affairManager.get(comment.getAffairId()));

				CollaborationAddCommentEvent commentEvent = new CollaborationAddCommentEvent(this);
				commentEvent.setCommentId(comment.getId());
				EventDispatcher.fireEvent(commentEvent);
			}
			
			dealVo.setFlowPermAccountId(summary.getOrgAccountId());
			//流程提交
			WFInfo wfIinfo = govdocWorkflowManager.finishRuncase(ColHandleType.finish, dealVo);
			//记录流程提交日志
			govdocLogManager.insertProcessLog(AppContext.getCurrentUser(), Long.parseLong(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.commit, wfIinfo.getMemberAndPolicys());
			
			// 1.保存跟踪,流程不是结束状态就保存跟踪信息,流程已经结束了就不保存跟踪信息
			// 2.当前节点就是最后一个节点/流程已经结束后面的只会节点：设置为结束状态。
			boolean isFinished = Integer.valueOf(EdocConstant.flowState.finish.ordinal()).equals(summary.getState())
					|| Integer.valueOf(EdocConstant.flowState.terminate.ordinal()).equals(summary.getState());
			if (isFinished) {
				affair.setFinish(true);
				superviseManager.updateStatusBySummaryIdAndType(SuperviseEnum.superviseState.supervised, summary.getId(), SuperviseEnum.EntityType.govdoc);
				govdocPubManager.updateEdocSummaryTransferStatus(summary.getId(), TransferStatus.sendEnd);
			}
			//更新affair状态
			affairManager.updateAffair(affair);
			
			// 登记数据撤销-非交换
			if (summary.getGovdocType().intValue() == 2) {
				//govdocRegisterManager.saveBySummary(summary);
			}
			// 发送自动跳过的事件
			GovdocHelper.fireAutoSkipEvent(this);
			// 更新当前处理人信息
			GovdocHelper.updateCurrentNodesInfo(summary, false);
			// 保存summary
			govdocSummaryManager.updateEdocSummary(summary);
			// 调用表单万能方法,更新状态，触发子流程等
			govdocFormManager.updateDataState(affair, summary, ColHandleType.finish);
			return true;
		} catch (Exception e) {
			LOGGER.error("公文分办后提交签收流程出错", e);
			throw new BusinessException();
		}
	}
	
	@Override
	public void transDistribute(GovdocDealVO finishVO) {
		try {
			// 分送
			if(!finishVO.getCurrentUser().getId().equals(finishVO.getAffair().getMemberId()) ){//代理
				govdocExchangeManager.exchangeSend(finishVO.getSummary(), finishVO.getAffair().getMemberId(), finishVO.getAffair().getId(), null);
			} else {
				govdocExchangeManager.exchangeSend(finishVO.getSummary(), finishVO.getCurrentUser().getId(), finishVO.getAffair().getId(), null);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void transSign(GovdocDealVO finishVO) {
		try {
			// 签收
			if(!finishVO.getCurrentUser().getId().equals(finishVO.getAffair().getMemberId()) ){
				govdocExchangeManager.exchangeSign(finishVO.getSummary(), finishVO.getAffair(), finishVO.getAffair().getMemberId());
			}else{
				govdocExchangeManager.exchangeSign(finishVO.getSummary(), finishVO.getAffair(), finishVO.getCurrentUser().getId());
			}
			govdocPubManager.updateEdocSummaryTransferStatus(finishVO.getSummary().getId(), TransferStatus.receiveSigned);// 更新交换流程对应的edocsummary的流转状态为“已签收且被分办了”
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
    /*************************** 66666 公文交换相关方法   end ***************************/

	
	/*************************** 88888 AJAX方法 start ***************************/
	@AjaxAccess
	@Override
	public String takeDeal(String summaryID, String affairID) throws BusinessException {
		EdocSummary summary = govdocSummaryManager.getSummaryById(Long.valueOf(summaryID));
		if(summary.getGovdocType().intValue() == 0){
			return "old";
		}
		GovdocExchangeDetail govdocExchangeDetail = govdocExchangeManager.findDetailByRecSummaryId(Long.valueOf(summaryID));
		if (govdocExchangeDetail != null) {
			List<CtpAffair> affairs = affairManager.getAffairs(govdocExchangeDetail.getSummaryId());
			summary = govdocSummaryManager.getSummaryById(Long.valueOf(govdocExchangeDetail.getSummaryId()));
			for (CtpAffair affair : affairs) {
				String category = PermissionFatory.getPermBySubApp(summary.getGovdocType()).getCategorty();
				String configItem = govdocWorkflowManager.getPolicyByAffair(affair, summary.getProcessId()).getId();
				Permission permission = permissionManager.getPermission(category, configItem, AppContext.currentAccountId());
				List<String> basicActionList = permissionManager.getActionList(permission, PermissionAction.basic);
				if (basicActionList.contains("Distribute") && affair.getMemberId().equals(AppContext.currentUserId()) && StateEnum.col_done.getKey() == affair.getState()) {
					StringBuilder resultSql = new StringBuilder();
					resultSql.append("workitemId:").append(affair.getSubObjectId()).append(";").append("processId:").append(summary.getProcessId()).append(";")
					.append("caseId:").append(summary.getCaseId()).append(";").append("summaryId:").append(summary.getId()).append(";").append("affairId:")
					.append(affair.getId()).append(";").append("bodyType:").append(affair.getBodyType()).append(";").append("nodeId:")
					.append(affair.getActivityId()).append(";").append("referenceId:").append(summaryID);
					return resultSql.toString();
				}
			}
			govdocRegisterManager.saveByDetail(govdocExchangeDetail);
		}
		return "no";
	}
	
	@Override
	public Map<String, Object> transTakeBack(Map<String, Object> ma) throws BusinessException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		EdocSummary summary = null;
		try {
			CtpAffair affair = affairManager.get(Long.valueOf((String) ma.get("affairId")));
			boolean stepBackDistribute = ma.get("stepBackDistribute")!=null?(Boolean)ma.get("stepBackDistribute"):false;
			//ocip 跨系统取回
            String sysCode = null;
            Long extMemberId = null;
        	Map<String,Object> affairExtMap = AffairUtil.getExtProperty(affair);
            if(affairExtMap !=null){
            	sysCode = (String)affairExtMap.get("ocipSystemCode");
            	extMemberId = (Long)affairExtMap.get("memberId_of_ocip_client");
            }
//			if(AppContext.hasPlugin("ocip")&&sysCode!=null&&sysCode.startsWith("0|system")){
//				try {
//					OcipOrgMember member = organizationManager.getMember(AppContext.getCurrentUser().getId().toString(),
//							Global.getSysCode());
//					if (null == member) {
//						throw new RuntimeException("当前人员可能未上报平台，请联系管理员");
//					}
//					String requestAddress = OcipConfiguration.getInstance().getExchangeSpi().getTransportService()
//							.requestAddress(sysCode);
//					OcipAxis2Client client = new OcipAxis2Client("http://" + requestAddress);
//					String[] invoke = client
//							.invoke("ocipEdocService", "takeBack", String[].class,
//									"http://impl.webservice.ocip.apps.seeyon.com",
//									new Object[] { ma.get("affairId").toString(), ma.get("isSaveOpinion").toString(),
//											extMemberId.toString(), ma.get("curSummaryId"), ma.get("caseId"),
//											ma.get("summaryId") });
//					// 处理返回结果
//					if (!Strings.isEmpty(invoke[0]) && !"null".equals(invoke[0])) {
//						resultMap.put("msg", invoke[0]);
//					}
//					if (!Strings.isEmpty(invoke[1]) && !"null".equals(invoke[1])) {
//						resultMap.put("caseId", invoke[1]);
//					}
//					return resultMap;
//				} catch (final Exception e) {
//					resultMap.put("msg", "取回跨系统事务失败:" + e.getMessage());
//					return resultMap;
//				}
//			}
			Long memberId = affair.getMemberId();
			List<CtpAffair> pendingAffairList = affairManager.getAffairs(affair.getObjectId(), StateEnum.col_pending);
			int result = govdocWorkflowManager.takeBackCase(affair.getSubObjectId());
			if (result == 1) {
				resultMap.put("msg", "流程已结束，不能取回；");
				return resultMap;
			} else if (result == 2) {
				resultMap.put("msg", "当前及后面节点有子流程已结束，不能取回");
				return resultMap;
			} else if (result == -1) {
				resultMap.put("msg", "后面节点任务事项已处理完成，不能取回");
				return resultMap;
			} else if (result == -2) {
				resultMap.put("msg", "当前任务事项所在节点为知会节点，不能取回");
				return resultMap;
			}
			
			// 0表示取回成功；1表示流程已结束，不能取回；2表示当前及后面节点有子流程已结束，不能取回；-1表示后面节点任务事项已处理完成，不能取回；-2表示当前任务事项所在节点为知会节点，不能取回；
			if (result == 0) {
				summary = govdocSummaryManager.getSummaryById(affair.getObjectId());			
				//对原意见进行修改
				if (ma.get("isSaveOpinion") != null && Strings.isNotBlank(ma.get("isSaveOpinion").toString()) && !(Boolean) ma.get("isSaveOpinion")) {
					govdocCommentManager.updateOpinion2Draft(affair.getId(), summary);
				}
				// 删除签章
				govdocSignetManager.deleteBySummaryIdAffairIdAndType(summary.getId(), affair.getId(), V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
				//取回的时候删除续办人员
				govdocContinueManager.deleteCustomDealWidth(affair);
				//修改流程实例状态,先存一条记录在wf_case_run里面（前台再调用deleteHistoryCase删除老数据）
				wapi.updateCaseRunState(summary.getCaseId(), 2);
				
				affair.setState(StateEnum.col_pending.key());
				affair.setSubState(SubStateEnum.col_pending_unRead.key());
				affair.setCompleteTime(null);
				affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
				AffairUtil.addExtProperty(affair, AffairExtPropEnums.edoc_lastOperateState, 1);
				affair.setTransactorId(null);// 取回后 将代理人清空
				affairManager.updateAffair(affair);
				
				// 改变交换状态
				govdocExchangeManager.exchangeTakeBack(summary.getId(), affair,stepBackDistribute);

				// 修改相关affair的结束标识
				List<Integer> states = new ArrayList<Integer>();
				states.add(StateEnum.col_sent.key());
				states.add(StateEnum.col_pending.key());
				states.add(StateEnum.col_done.key());
				Map<String, Object> columns = new HashMap<String, Object>();
				//columns.put("finish", true); //OA-175389风暴206：M3端取回一条已办公文后重新处理，已办下点击跟踪提示已结束
				Object[][] wheres = new Object[][] { { "objectId", affair.getObjectId() }, { "state", states } };
				affairManager.update(columns, wheres);
				
				// 修改流程结束状态
				summary.setFinished(false);
				summary.setState(EdocConstant.flowState.run.ordinal());
				summary.setCompleteTime(null);
				summary.setCurrentNodesInfo(summary.getCurrentNodesInfo());
				summary.setToEdocLibFlag(false);
				govdocSummaryManager.updateEdocSummary(summary,false);
				GovdocHelper.updateCurrentNodesInfo(summary, false);
				
				// 取回成功释放相应的领导批示编号
				Object pishiname = govdocPishiManager.getLSS(memberId);
				if(pishiname!=null && !"".equals(pishiname)){
					govdocPishiManager.emptyPishiNoByAffairId(Long.valueOf((String) ma.get("affairId")), pishiname.toString());
				}
				//回退分办 需要修改交换流程的case相关数据
				if (ma.get("curSummaryId") != null && Strings.isNotBlank(ma.get("curSummaryId").toString())) {
					govdocSummaryManager.deleteSummaryAndAffair(Long.valueOf(ma.get("curSummaryId").toString()));
					if (ma.get("caseId") != null) {
						 String caseId = ma.get("caseId").toString();
						 govdocWorkflowManager.transBackWfRunCase(caseId);
					}
//					if (ma.get("summaryId") != null) {
//						String summaryId = ma.get("summaryId").toString();
//						GovdocExchangeDetail detail = govdocExchangeManager.findDetailBySummaryId(Long.valueOf(summaryId));
//						detail.setStatus(ExchangeDetailStatus.hasSign.getKey());
//						govdocExchangeManager.updateDetail(detail);
//					}
				}
				
				// 调用表单万能方法,更新状态，触发子流程等
				govdocFormManager.updateDataState(affair, summary, ColHandleType.takeBack);
				// 流程日志
				govdocLogManager.insertProcessLog(AppContext.getCurrentUser(), Long.parseLong(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.takeBack);
				// 全文检索
				if (AppContext.hasPlugin("index")) {
				    indexApi.update(summary.getId(), ApplicationCategoryEnum.edoc.getKey());
				}
				//归档数据删除
				List<DocResourceBO> docResources = docApi.findDocResourcesBySourceId(affair.getId());
				if(!docResources.isEmpty()){
					List<Long> sourceId = new ArrayList<Long>();
					sourceId.add(affair.getId());
					docApi.deleteDocResources(memberId, sourceId);
					affair.setArchiveId(null);
					//判断是否还有其他已归档的affair，如果没有就改变summary的归档状态
					List<Long> afIds = affairManager.getAllAffairIdByAppAndObjectId(ApplicationCategoryEnum.edoc, affair.getObjectId());
					for (Long afId : afIds) {
						if(!afId.equals(affair.getId()) && !(docApi.findDocResourcesBySourceId(afId).isEmpty())){
							summary.setHasArchive(false);
							summary.setArchiveId(null);
							break;
						}
					}

				}
				
				//发送取回消息
				govdocMessageManager.sendTakeBackMsg(pendingAffairList, affair, summary.getId());
				
				resultMap.put("caseId", summary.getCaseId());
			}
		} catch(Exception e) {
			LOGGER.error("", e);
			resultMap.put("msg", e.getMessage());
			return resultMap;
		} finally {
			govdocLockManager.unlockAll(summary);
		}
		return resultMap;
	}

	@AjaxAccess
	@Override
	public String checkCanDelete(Map<String, String> param) throws BusinessException {
		String affairIds = param.get("affairIds");
		String result = "";
		String from = param.get("fromMethod");
		List<CtpAffair> affairList = null;
		if (Strings.isNotBlank(affairIds)) {
			affairList = new ArrayList<CtpAffair>();
			String[] affairs = affairIds.split("[*]");
			for (String affairId : affairs) {
				Long _affairId = new Long(affairId);
				CtpAffair affair = affairManager.get(_affairId);
				affairList.add(affair);
				int state = affair.getState();
				if (state != StateEnum.col_pending.getKey() && state != StateEnum.col_done.getKey() && state != StateEnum.col_sent.getKey()
						&& (state != StateEnum.col_waitSend.getKey() || "listSent".equals(from))) { // 已发里被回退的不能删除
					result = WFComponentUtil.getErrorMsgByAffair(affair);
				}
			}
		}

		if (!"".equals(result)) {
			return result;
		}
		return "success";
	}
	// 删除一个个人事项
	@AjaxAccess
	@Override
	public void deleteAffair(String pageType, long affairId) throws BusinessException {
		User user = AppContext.getCurrentUser();
		CtpAffair affair = affairManager.get(affairId);
		if (affair == null)
			return;
		EdocSummary summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
		if (null == summary) {
			return;
		}
		// 如果是保存待发，删除个人事项的同时删除整个协同
		if ("draft".equals(pageType)) {
			long summaryId = affair.getObjectId();
			List<CtpAffair> affairs = affairManager.getAffairs(ApplicationCategoryEnum.edoc, summaryId);
			this.attachmentManager.removeByReference(summaryId);
			govdocSummaryManager.deleteEdocSummary(summaryId);
			govdocMarkManager.saveUnbindMark(summaryId);
			affairManager.deletePhysicalByObjectId(summaryId);
			//如果是回退追溯之类的summary也需要删除掉
			govdocSummaryManager.deleteSummaryByFormRecordId(summary.getFormRecordid());
			List<Long> ids = new ArrayList<Long>();
			for (CtpAffair a : affairs) {
				ids.add(a.getId());
			}
			// 删除归档文档
			// docHierarchyManager.deleteDocByResources(ids, user);

			// 删除表单数据
			try {
				LOGGER.info("待发删除表单数据：summayId:" + summaryId);
				List<CtpContentAll> contentList = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.form, Long.valueOf(summaryId));
				if (!contentList.isEmpty()) {
					for (CtpContentAll content : contentList) {
						long formId = content.getContentTemplateId();
						if (formId != 0 && content.getContentDataId() != null) {
							long masterId = content.getContentDataId();
							formApi4Cap3.deleteFormData(masterId, formId);
							LOGGER.info("deleteFormDataId:" + DateUtil.get19DateAndTime() + "moduleType=" + ModuleType.form.getKey() + " delete formId=" + formId + "formDataId="
									+ masterId);
						}
					}
				}
				List<CtpContentAll> contents = GovdocContentHelper.getContentListByModuleId(summaryId);
				Iterator it = contents.iterator();
				while (it.hasNext()) {
					MainbodyService.getInstance().getContentManager().deleteById(((CtpContentAll) it.next()).getId());
				}
			} catch (Throwable e) {
				LOGGER.error("删除表单数据异常", e);
				throw new BusinessException(e);
			}

			// 删除流程日志
			if (Strings.isNotBlank(summary.getProcessId())) {
				govdocLogManager.deleteProcessLog(Long.parseLong(summary.getProcessId()));
			}
		} else {
			// 如果是待办，删除个人事项的同时finishWorkitem
			if ("pending".equals(pageType)) {

				Comment comment = new Comment();
				comment.setId(UUIDLong.longUUID());
				comment.setAffairId(affairId);
				comment.setModuleId(affair.getObjectId());
				comment.setClevel(1);
				comment.setCtype(0);
				comment.setHidden(false);
				comment.setContent("");
				comment.setModuleType(ApplicationCategoryEnum.edoc.getKey());
				comment.setPath("001");
				comment.setPid(0l);
				comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
				comment.setCreateId(user.getId());
				comment.setPushMessage(false);
				DateSharedWithWorkflowEngineThreadLocal.setIsNeedAutoSkip(false);
				GovdocDealVO dealVo = new GovdocDealVO();
				dealVo.setSummary(summary);
				dealVo.setAffair(affair);
				dealVo.setComment(comment);
				dealVo.setAction("deal");
				transFinishWorkItemPublic(dealVo, ColHandleType.finish);
			}
			affairManager.deleteAffair(affair.getId());
		}
		Integer actionId = GovdocAppLogAction.COLL_DELETE.key();
		if (affair.getSubApp().intValue() == ApplicationSubCategoryEnum.edoc_fawen.getKey()) {
			actionId = GovdocAppLogAction.EDOC_DELETE_SEND.key();
		}
		if (affair.getSubApp().intValue() == ApplicationSubCategoryEnum.edoc_shouwen.getKey()) {
			actionId = GovdocAppLogAction.EDOC_DELETE_REC.key();
		}
		if (affair.getSubApp().intValue() == ApplicationSubCategoryEnum.edoc_qianbao.getKey()) {
			actionId = GovdocAppLogAction.EDOC_DELETE_SIGN.key();
		}
		govdocLogManager.insertAppLog(user, actionId, user.getName(), affair.getSubject());
		// 删除事项更新全文检索库
		if (AppContext.hasPlugin("index")) {
			if (summary != null && summary.getGovdocType() != null) {
				// 点击列表删除的时候更新公文的全文检索库
			    indexApi.update(affair.getObjectId(), ApplicationCategoryEnum.edoc.getKey());
			}
		}
	}
	
	@AjaxAccess
	@Override
	public Map checkIsCanRepeal(Map params) throws BusinessException {
		String _summaryId = params.get("summaryId") != null ? ((String) params.get("summaryId")) : null;
		boolean onlyOne = false;
		Map<String, String> map = new HashMap<String, String>();
		if (_summaryId.indexOf(",") == -1) {
			onlyOne = true;
		}
		if (Strings.isBlank(_summaryId)) {
			map.put("msg", "程序或者数据异常，不可以撤销！");
			return map;
		}

		String[] split = _summaryId.split(",");
		List<String> err = new ArrayList<String>();
		for (String sId : split) {
			Long summaryId = Long.parseLong(sId);
			EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
			if (summary == null) {
				if (onlyOne) {
					map.put("msg", "程序或者数据异常，不可以撤销！");
					return map;
				} else {
					err.add(sId);
				}
			}
			// 判断流程是否结束，结束后不能撤销
			if (EdocConstant.flowState.terminate.ordinal() == summary.getState() || EdocConstant.flowState.finish.ordinal() == summary.getState()) {
				if (onlyOne) {
					map.put("msg", ResourceUtil.getString("collaboration.cannotRepeal_workflowIsFinished"));
					return map;
				} else {
					err.add(sId);
				}
			}
			if (summary.getGovdocType() != null) {
				if (summary.getGovdocType() == 1 || summary.getGovdocType() == 2 || summary.getGovdocType() == 4) {
					GovdocExchangeMain exchangeMain = govdocExchangeManager.findBySummaryId(summaryId, null);
					if (exchangeMain != null) {
						if (onlyOne) {
							map.put("msg", ResourceUtil.getString("govdoc.cannotRepeal_workflowHasDelivery"));
							return map;
						} else {
							err.add(sId);
						}
					}
				}
			}

			if (!CollectionUtils.isEmpty(err)) {
				// 如果当前是summaryIds，则返回如下格式
				map.put("errorSummaryIds", StringUtils.join(err, ","));
			}
		}
		return map;
	}
	@AjaxAccess
	@Override
	public String transRepal(GovdocRepealVO repealVO) throws BusinessException {
		if (repealVO.getCurrentUser() == null) {
			repealVO.setCurrentUser(AppContext.getCurrentUser());
		}
		if (repealVO.getAffairIdStr().indexOf(",") != -1) {// 撤销多个
			return transRepalFrontMultiple(repealVO);
		} else {
			return transRepalFront(Long.valueOf(repealVO.getSummaryIdStr()), Long.valueOf(repealVO.getAffairIdStr()), repealVO);
		}
	}
	/**
	 * 批量撤销(暂无用)
	 * @param repealVO
	 * @return
	 * @throws NumberFormatException
	 * @throws BusinessException
	 */
	private String transRepalFrontMultiple(GovdocRepealVO repealVO) throws NumberFormatException, BusinessException {
		String summaryIds = repealVO.getSummaryIdStr();
		if (Strings.isNotBlank(summaryIds)) {
			Map<String, String> res = new HashMap<String, String>();
			String[] sIds = summaryIds.split(",");
			if (sIds.length > 0) {
				String[] aIds = repealVO.getAffairIdStr().split(",");
				for (int i = 0; i < sIds.length; i++) {
					String errorMsg = transRepalFront(Long.valueOf(sIds[i]), Long.valueOf(aIds[i]), repealVO);
					if (Strings.isNotBlank(errorMsg)) {
						res.put(sIds[i], errorMsg);
					}
				}
				if (res.size() > 0) {
					return JSONUtil.toJSONString(res);
				}
			}
		}
		return null;
	}
	/**
	 * 撤销一条
	 * @param summaryId
	 * @param affairId
	 * @param repealVO
	 * @return
	 * @throws BusinessException
	 */
	private String transRepalFront(Long summaryId, Long affairId, GovdocRepealVO repealVO) throws BusinessException {
		String errorMsg = "";
		EdocSummary summary = null;
		try {
			int result = 0;
			summary = govdocSummaryManager.getSummaryById(summaryId);
			String subject = summary.getSubject();
			
			Long caseId = summary.getCaseId();
			if (caseId == null) {
				result = 1;
				errorMsg = "公文《" + subject + "》已经结束,不允许撤销";
				return errorMsg;
			}
			
			//开始撤销流程
			AffairData affairData = GovdocAffairHelper.getRepealAffairData(summaryId, affairId, repealVO);
			affairData.setFormRecordId(summary.getFormRecordid());
			affairData.addBusinessData(GovdocWorkflowEventListener.CURRENTUSER_CONSTANT, repealVO.getCurrentUser());
			result = govdocWorkflowManager.cancelCase(affairData, caseId);
			
			if (result == -1) {
				errorMsg = "公文《" + subject + "》未生成完毕，请稍后进行撤销操作";
				return errorMsg;
			} else if (result == -2) {// 新流程已结束不能撤销，已在manager里提示, 如果该流程触发的新流程已结束，不能撤销
				errorMsg = "该公文触发的子流程《" + subject + "》已结束，不允许撤销！";
				return errorMsg;
			} else if (result == 1) {
				errorMsg = "公文《" + subject + "》已经结束,不允许撤销";
				return errorMsg;
			}
			
			GovdocExchangeMain main = null;
			GovdocExchangeDetail detail = govdocExchangeManager.findDetailBySummaryId(summary.getId());
			if (detail != null) {
				main = govdocExchangeManager.getGovdocExchangeMainById(detail.getMainId());
			}
			
			//撤销成功，更新交换状态
			if (result == 0) {
				//公文交换或联合发文
				if (summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey() 
						|| (main != null && main.getType() == GovdocExchangeMain.EXCHANGE_TYPE_LIANHE)) {
					// 更新流转状态
					if (detail != null && detail.getStatus() != ExchangeDetailStatus.hasBack.getKey()) {
							govdocPubManager.directUpdateEdocSummaryAttr(summary.getId(), TransferStatus.repealWaitSend, null);
					}
					govdocExchangeManager.exchangeCancel(detail != null ? detail.getId() : null, repealVO.getRepealComment());
					//联合发文的协办流程撤销后,不能在待发里面
					if(main != null && main.getType() == GovdocExchangeMain.EXCHANGE_TYPE_LIANHE){
						List<CtpAffair> affairs = affairManager.getAffairs(summary.getId(), StateEnum.col_waitSend);
						for (CtpAffair ctpAffair : affairs) {
							ctpAffair.setDelete(true);
						}
						affairManager.updateAffairs(affairs);
					}
				} else if(detail == null && summary.getGovdocType() == ApplicationSubCategoryEnum.edoc_shouwen.getKey()){
					detail = govdocExchangeManager.findDetailByRecSummaryId(summary.getId());
					if (detail != null) {
						govdocExchangeManager.recSummaryCancelUpdateExchangeStatus(detail.getId(), repealVO.getRepealComment());
					}
				}
				
				//撤销后删除所有续办节点
				govdocContinueManager.deleteAllCustomDealWidth(summary.getId());
				//撤销后释放所有在本公文中被使用的编号
				govdocPishiManager.emptyPishiNo(summary.getId());
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new BusinessException("", e);
		} finally {
			govdocLockManager.unlockAll(summary);
		}
		return errorMsg;
	}
	
	@AjaxAccess	
	@Override
	public void recoidChangeWord(Map<String, String> map) {
		String affairId = map.get("affairId");
    	String summaryId = map.get("summaryId");
    	String changeType = map.get("changeType");
    	String userId = map.get("userId");
    	  if(affairId == null ||  "".equals(affairId) ||
    			  summaryId == null || "".equals(summaryId)
    			  || changeType == null || "".equals(changeType)){
    		  return ;
    	  }
    	  try{
        	  User user = AppContext.getCurrentUser();
        	  if(user == null) {
        		  user = new User() ;
        		  user.setId(Long.valueOf(userId)) ;
        	  }
        	  CtpAffair affair = affairManager.get(Long.valueOf(affairId));

        	  //记录修改正文的日志-------------------changyi add
        	  govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_CONTENT_UPDATE.key(), user.getName(),affair.getSubject());

        	  BPMActivity bPMActivity = GovdocHelper.getBPMActivityByAffair(affair) ;
    		  EdocSummary summary = govdocSummaryManager.getSummaryById(Long.valueOf(summaryId));
    		  String[] changeWords = null ;
    		  if(changeType.contains(",") ){
    			  changeWords =  changeType.split(",");
    		  }else{
    			  changeWords = new String[1] ;
    			  changeWords[0] = changeType ;
    		  }
    		  if(changeWords != null && changeWords.length > 0) {
    			  for(int i = 0 ; i < changeWords.length ; i++) {
    	    		  if("contentUpdate".equals(changeWords[i])){
    	    			  this.govdocLogManager.insertProcessLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.modifyBody.getKey()));
						  //TODO
    	    			  //saveBodyMessage(affairManager, userMessageManager, orgManager, summary);
    	    		  }else if("taohong".equals(changeWords[i])) {
    	    			  this.govdocLogManager.insertProcessLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.Body.getKey()));
    	    		  }else if("qianzhang".equals(changeWords[i])){
    	    			  this.govdocLogManager.insertProcessLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.signed.getKey()));
    	    		  }else if("taohongwendan".equals(changeWords[i])){
    	    			  this.govdocLogManager.insertProcessLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.bodyFromRed.getKey()));
    	    		  }else if("depPinghole" .equals(changeWords[i])){
    	    			  this.govdocLogManager.insertProcessLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.depHigeonhole.getKey()));
    	    		  }else if("duban" .equals(changeWords[i])) {
    	    	          this.govdocLogManager.insertProcessLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.duban.getKey()));
    	    		  }else if("wendanqianp" .equals(changeWords[i])){
    	    			  this.govdocLogManager.insertProcessLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.wendanqianp.getKey()));
    	    		  }else if("isLoadNewFile" .equals(changeWords[i])){//记录修改正文并且导入新的文件的操作的应用日志
    	    			  this.govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_CONTENT_EDIT_LOADNEWFILE.key(), user.getName(),affair.getSubject());
    	    		  }
    			  }
    		  }
    	  }catch(Exception e){
    		  LOGGER.error("记录修改正文时候出错:",e) ;
    	  }
		
	}
	
	@AjaxAccess
	@Override
	public String getRightByAction(String affairId, String actionKey)
			throws BusinessException {
		StringBuilder result = new StringBuilder();
		CtpAffair affair = null;
		EdocSummary summary = null;
		List<String> actions = null;
		affair = affairManager.get(Long.parseLong(affairId));
		summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
		long accountId = 0L;
		if (summary != null) {//老公文不做判断
			if(summary.getGovdocType()!=null && summary.getGovdocType().intValue()!=0) {
				accountId = GovdocHelper.getFlowPermAccountId(AppContext.currentAccountId(), summary);
				// 获取节点权限的策略
				if (null != affair.getSubApp() && affair.getSubApp() == ApplicationSubCategoryEnum.edoc_shouwen.getKey()) {
					actions = permissionManager.getActionList(EnumNameEnum.edoc_new_rec_permission_policy.name(), affair.getNodePolicy(), accountId);
				} else if (null != affair.getSubApp() && affair.getSubApp() == ApplicationSubCategoryEnum.edoc_jiaohuan.getKey()) {
					actions = permissionManager.getActionList(EnumNameEnum.edoc_new_change_permission_policy.name(), affair.getNodePolicy(), accountId);
				} else if (null != affair.getSubApp() && affair.getSubApp() == ApplicationSubCategoryEnum.edoc_qianbao.getKey()) {
					actions = permissionManager.getActionList(EnumNameEnum.edoc_new_qianbao_permission_policy.name(), affair.getNodePolicy(), accountId);
				} else {
					actions = permissionManager.getActionList(EnumNameEnum.edoc_new_send_permission_policy.name(), affair.getNodePolicy(), accountId);
				}
				//判断是否有对应的策略
				if (Strings.isNotEmpty(actions) && actions.contains(actionKey)) {
					result.append("true");
				} else {
					result.append("公文《" + summary.getSubject() + "》不允许撤销流程！\r\n");
				}
			} else {
				result.append("true");
			}
		}
		return result.toString();
	}
	
	@AjaxAccess
	@Override
	public String getNodePolicyName(Long affairId) throws BusinessException {
		if (affairId != null) {
			CtpAffair affair = affairManager.get(affairId);
			EdocSummary summary = govdocSummaryManager.getSummaryById(affair.getObjectId());
			return govdocWorkflowManager.getPolicyByAffair(affair, summary).getName();
		}
		return "";
	}
	
	/*************************** 88888 AJAX方法   end ***************************/

	@AjaxAccess
	public String getMutiDepAndAccount(String input,String primaryInput) throws BusinessException{
    	StringBuilder hiddenValue = new StringBuilder();
    	if (Strings.isNotBlank(primaryInput)) {
    		String[] oldData = primaryInput.split(",");
    		for(int i = 0; i<oldData.length; i++){
    			String temp = oldData[i];
    			if(temp.indexOf("|") != -1){
        			String orgType = temp.substring(0,temp.indexOf("|"));
        			Long orgId = -1l;
        			try {
        				orgId = Long.valueOf(temp.substring(temp.indexOf("|")+1));
					} catch (Exception e) {
						continue;
					}
        			String name = "";
        			if("Account".equals(orgType) || "Department".equals(orgType)){
        				V3xOrgEntity entity = orgManager.getEntityOnlyById(orgId);
        				name = entity.getName();
        			}else if("OrgTeam".equals(orgType)){
        				name = OrgHelper.showOrgEntities(orgId+"", "OrgTeam", "|");
        			}else if("ExchangeAccount".equals(orgType)){
        				name = OrgHelper.showOrgEntities(orgId+"", "ExchangeAccount", "|");
        			}
        			if (Strings.isBlank(name)) {
						continue;
					}        			
        			int nameIndex = input.indexOf(name);
        			if (nameIndex > -1) {        				
        				hiddenValue.append(temp);
        				if (i < oldData.length - 1) {
        					hiddenValue.append(",");
						}
    				}
        		}
    			
    		}
		}
		return hiddenValue.toString();
    }
	
	@Override
	public Map<String, Integer> getManagermentPlatformCount(Long accountId) throws BusinessException {
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		//统计文单数量的参数
		Map<String,Object> formCountParam = new HashMap<String,Object>();
		List<Integer> formTypeList = new ArrayList<Integer>();
		formTypeList.add(FormType.govDocSendForm.getKey());
		formTypeList.add(FormType.govDocReceiveForm.getKey());
		formTypeList.add(FormType.govDocExchangeForm.getKey());
		formTypeList.add(FormType.govDocSignForm.getKey());
		formCountParam.put("orgAccountId", accountId);
		formCountParam.put("formType", formTypeList);
		formCountParam.put("useFlag", 1);
		formCountParam.put("state", 2);
		formCountParam.put("deleteFlag", 0);
		int formCount = formApi4Cap3.getFormCount(formCountParam);
		result.put("formCount", formCount);
		
		//模板数量
		int templateCount = govdocTemplateManager.getAccountGovdocSysTemplateCount(accountId);
		result.put("templateCount", templateCount);
		
		//文号数量
		int markCount = edocMarkDefinitionManager.getAccountMarkCount(accountId);
		result.put("markCount", markCount);
		
		//发文数量
		Map<String, String> sendRegparam = new HashMap<String,String>();
		sendRegparam.put("orgAccountId", accountId+"");
		sendRegparam.put("govdocType", "1");
		sendRegparam.put("listType", "listSendRegister");
		result.put("sendRegCount", govdocListManager.getRegisterCount(sendRegparam));
		
		//收文数量
		Map<String, String> recRegParam = new HashMap<String,String>();
		recRegParam.put("orgAccountId", accountId+"");
		recRegParam.put("govdocType", "2,4");
		recRegParam.put("listType", "listRecRegister");
		int recRegCount = govdocListManager.getRegisterCount(recRegParam);
		result.put("recRegCount", recRegCount);
		
		return result;
	}
	
	/*************************** 99999 Spring注入，请将业务写在上面 start ******************************/
	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
	public void setGovdocContinueManager(GovdocContinueManager govdocContinueManager) {
		this.govdocContinueManager = govdocContinueManager;
	}
	public void setGovdocCommentManager(GovdocCommentManager govdocCommentManager) {
		this.govdocCommentManager = govdocCommentManager;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}
	public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }
	public void setProjectApi(ProjectApi projectApi) {
		this.projectApi = projectApi;
	}
	public void setGovdocPubManager(GovdocPubManager govdocPubManager) {
		this.govdocPubManager = govdocPubManager;
	}
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	public void setGovdocStatManager(GovdocStatManager govdocStatManager) {
		this.govdocStatManager = govdocStatManager;
	}
	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}
	public void setGovdocOpenManager(GovdocOpenManager govdocOpenManager) {
		this.govdocOpenManager = govdocOpenManager;
	}
	public void setIndexApi(IndexApi indexApi) {
        this.indexApi = indexApi;
    }
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public void setGovdocSignetManager(GovdocSignetManager govdocSignetManager) {
		this.govdocSignetManager = govdocSignetManager;
	}
	public void setGovdocContentManager(GovdocContentManager govdocContentManager) {
		this.govdocContentManager = govdocContentManager;
	}
	public void setGovdocWorkflowManager(GovdocWorkflowManager govdocWorkflowManager) {
		this.govdocWorkflowManager = govdocWorkflowManager;
	}
	public void setGovdocTrackManager(GovdocTrackManager govdocTrackManager) {
		this.govdocTrackManager = govdocTrackManager;
	}
	public void setGovdocRegisterManager(GovdocRegisterManager govdocRegisterManager) {
		this.govdocRegisterManager = govdocRegisterManager;
	}
	public void setGovdocQuarzManager(GovdocQuarzManager govdocQuarzManager) {
		this.govdocQuarzManager = govdocQuarzManager;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
	public void setGovdocMessageManager(GovdocMessageManager govdocMessageManager) {
		this.govdocMessageManager = govdocMessageManager;
	}
	public void setGovdocExchangeManager(GovdocExchangeManager govdocExchangeManager) {
		this.govdocExchangeManager = govdocExchangeManager;
	}
	public void setGovdocMarkManager(GovdocMarkManager govdocMarkManager) {
		this.govdocMarkManager = govdocMarkManager;
	}
	public void setGovdocLockManager(GovdocLockManager govdocLockManager) {
		this.govdocLockManager = govdocLockManager;
	}
	public void setWorkTimeManager(WorkTimeManager workTimeManager) {
		this.workTimeManager = workTimeManager;
	}
	public void setGovdocDocManager(GovdocDocManager govdocDocManager) {
		this.govdocDocManager = govdocDocManager;
	}
	public void setGovdocAffairManager(GovdocAffairManager govdocAffairManager) {
		this.govdocAffairManager = govdocAffairManager;
	}
	public void setGovdocPishiManager(GovdocPishiManager govdocPishiManager) {
		this.govdocPishiManager = govdocPishiManager;
	}
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	public IOrganizationManager getOrganizationManager() {
		return organizationManager;
	}
	public void setOrganizationManager(IOrganizationManager organizationManager) {
		this.organizationManager = organizationManager;
	}
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
	public void setGovdocTemplateManager(GovdocTemplateManager govdocTemplateManager) {
		this.govdocTemplateManager = govdocTemplateManager;
	}
	public void setEdocMarkDefinitionManager(EdocMarkDefinitionManager edocMarkDefinitionManager) {
		this.edocMarkDefinitionManager = edocMarkDefinitionManager;
	}
	public void setGovdocListManager(GovdocListManager govdocListManager) {
		this.govdocListManager = govdocListManager;
	}
	public WorkflowApiManager getWapi() {
		return wapi;
	}
	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}
    public void setQwqpManager(QwqpManager qwqpManager) {
		this.qwqpManager = qwqpManager;
	}
	/*************************** 99999 Spring注入，请将业务写在上面   end ******************************/

}
