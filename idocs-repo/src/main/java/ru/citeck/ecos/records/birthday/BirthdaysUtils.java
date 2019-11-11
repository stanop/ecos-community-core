package ru.citeck.ecos.records.birthday;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.model.EcosModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

import java.time.LocalDate;
import java.util.List;

@Component
public class BirthdaysUtils {

    private final SearchService searchService;

    @Autowired
    public BirthdaysUtils(SearchService searchService) {
        this.searchService = searchService;
    }

    public List<NodeRef> search() {
        Integer currentMonthDay = getCurrentMonthDay();
        return FTSQuery.create()
                .type(ContentModel.TYPE_PERSON).and()
                .not().aspect(ContentModel.ASPECT_PERSON_DISABLED).and()
                .range(EcosModel.PROP_BIRTH_MONTH_DAY, currentMonthDay, currentMonthDay + 100)
                .addSort(EcosModel.PROP_BIRTH_MONTH_DAY, true)
                .query(searchService);
    }

    private Integer getCurrentMonthDay() {
        LocalDate localDate = LocalDate.now();
        return localDate.getMonthValue() * 100 + localDate.getDayOfMonth();
    }

}
