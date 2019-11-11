package ru.citeck.ecos.behavior.common;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.model.EcosModel;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class FillMonthDayPersonPropertyBehaviour extends AbstractBehaviour
        implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    @Override
    protected void beforeInit() {
        setClassName(ContentModel.TYPE_PERSON);
    }

    @PolicyMethod(policy = NodeServicePolicies.OnUpdatePropertiesPolicy.class,
            frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
            runAsSystem = true)
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before,
                                   Map<QName, Serializable> after) {

        Serializable birthDateBefore = before.get(EcosModel.PROP_BIRTH_DATE);
        Serializable birthDateAfter = after.get(EcosModel.PROP_BIRTH_DATE);

        if (!Objects.equals(birthDateBefore, birthDateAfter)) {
            Date birthDate = null;
            if (birthDateAfter != null) {
                birthDate = (Date) birthDateAfter;
            }
            setMonthDayProp(nodeRef, birthDate);
        }
    }

    public void setMonthDayProp(NodeRef nodeRef, Date birthDate) {
        if (birthDate == null) {
            nodeService.setProperty(nodeRef, EcosModel.PROP_BIRTH_MONTH_DAY, null);
            return;
        }
        SimpleDateFormat df = new SimpleDateFormat("MMdd");
        String monthDayString = df.format(birthDate);
        Integer monthDay = null;
        try {
            monthDay = Integer.parseInt(monthDayString);
        } catch (NumberFormatException e) {
            logger.warn(e.getMessage());
        }
        nodeService.setProperty(nodeRef, EcosModel.PROP_BIRTH_MONTH_DAY, monthDay);
    }
}
