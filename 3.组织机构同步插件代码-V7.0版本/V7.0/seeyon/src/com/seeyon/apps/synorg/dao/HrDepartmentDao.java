package com.seeyon.apps.synorg.dao;

import java.util.List;

import com.seeyon.apps.synorg.po.hr.HrDepartment;

/**
 * 部门管理
 * 
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:23:17
 * @Copyright Beijing Seeyon Software Co.,LTD
 */
public interface HrDepartmentDao {

	/**
	 * 查找全部部门
	 */
	public List<HrDepartment> findAll();

}
