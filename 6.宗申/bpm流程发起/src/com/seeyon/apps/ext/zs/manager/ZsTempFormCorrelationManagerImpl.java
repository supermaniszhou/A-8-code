package com.seeyon.apps.ext.zs.manager;

import com.seeyon.apps.ext.zs.dao.ZsTempFormCorrelationDao;
import com.seeyon.apps.ext.zs.dao.ZsTempFormCorrelationDaoImpl;
import com.seeyon.apps.ext.zs.po.ZsTempFormCorrelation;

import java.util.List;
import java.util.Map;

public class ZsTempFormCorrelationManagerImpl implements ZsTempFormCorrelationManager {

    private ZsTempFormCorrelationDao dao = new ZsTempFormCorrelationDaoImpl();

    @Override
    public ZsTempFormCorrelation saveForm(ZsTempFormCorrelation zs) {
        return getDao().saveForm(zs);
    }

    @Override
    public List<ZsTempFormCorrelation> getFormInfoBySummaryId(Map<String, Object> summaryId) {
        return getDao().getFormInfoBySummaryId(summaryId);
    }

    public ZsTempFormCorrelationDao getDao() {
        return dao;
    }
}
