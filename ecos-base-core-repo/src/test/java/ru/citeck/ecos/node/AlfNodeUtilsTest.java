package ru.citeck.ecos.node;

import org.junit.Test;
import ru.citeck.ecos.records.source.alf.meta.AlfNodeUtils;

import static org.junit.Assert.*;

public class AlfNodeUtilsTest {

    @Test
    public void test() {
        String path = AlfNodeUtils.resolveHasContentPathQuery("cm:someAssoc.cm:content");

        assertEquals(".att(n:\"cm:someAssoc\"){has(n:\"cm:content\")}", path);

        path = AlfNodeUtils.resolveHasContentPathQuery("cm:someAssoc.cm:contentAssoc.cm:other");
        assertEquals(".att(n:\"cm:someAssoc\"){att(n:\"cm:contentAssoc\"){has(n:\"cm:other\")}}", path);
    }
}
