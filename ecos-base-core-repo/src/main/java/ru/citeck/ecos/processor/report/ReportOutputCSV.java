package ru.citeck.ecos.processor.report;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import ru.citeck.ecos.processor.AbstractDataBundleLine;
import ru.citeck.ecos.processor.DataBundle;
import ru.citeck.ecos.processor.ProcessorConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create CSV Report from DataBundle
 *
 * @author Andrew Timokhin
 */
public class ReportOutputCSV extends AbstractDataBundleLine {

    private static final String CSV_MIMETYPE = "text/csv";
    private static final String REPORT_COLUMNS = "reportColumns";
    private static final String REPORT_DATA = "reportData";
    private static final String COLUMN_TITLE = "title";
    private static final String DEFAULT_DELIMETER = "\t";
    private static final String DEFAULT_SEPARATOR = "\r\n";

    private String delimeter = DEFAULT_DELIMETER;
    private String separator = DEFAULT_SEPARATOR;

    public void setDelimeter(String delimeter) {
        if (delimeter != null) {
            this.delimeter = delimeter;
        }
    }

    public void setSeparator(String separator) {
        if (separator != null) {
            this.separator = separator;
        }
    }

    @Override
    public DataBundle process(DataBundle input) {
        Map<String, Object> model = input.needModel();
        HashMap<String, Object> newModel = new HashMap<>();
        newModel.putAll(model);
        newModel.put(ProcessorConstants.KEY_MIMETYPE, CSV_MIMETYPE);
        ByteArrayOutputStream baos = getCSVReportStream(model);
        InputStream csvIS = null;
        if (baos != null) {
            csvIS = new ByteArrayInputStream(baos.toByteArray());
        }

        return new DataBundle(csvIS, newModel);
    }

    @SuppressWarnings("unchecked")
    private ByteArrayOutputStream getCSVReportStream(Map<String, Object> model) {
        StringBuilder builder = new StringBuilder();

        List<Map<String, String>> reportColumns = (List<Map<String, String>>) model.get(REPORT_COLUMNS);
        List<List<Map<String, Object>>> reportData = (List<List<Map<String, Object>>>) model.get(REPORT_DATA);

        createColumnTitlesRow(builder, reportColumns);
        createSheetData(builder, reportData);

        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            result.write(builder.toString().getBytes(StandardCharsets.UTF_8));
            return result;
        } catch (IOException exc) {
            Logger.getLogger(getClass()).error(exc.getMessage(), exc);
        }

        return null;
    }

    private void createColumnTitlesRow(StringBuilder builder, List<Map<String, String>> reportColumns) {
        if (reportColumns != null && !reportColumns.isEmpty()) {
            for (int i = 0; i < reportColumns.size(); i++) {
                Map<String, String> col = reportColumns.get(i);

                String title;

                if (col != null && col.get(COLUMN_TITLE) != null) {
                    title = col.get(COLUMN_TITLE);
                } else {
                    title = "";
                }

                builder.append(clean(title));

                if (i != reportColumns.size() - 1) {
                    builder.append(delimeter);
                } else {
                    builder.append(separator);
                }
            }
        }
    }

    private void createSheetData(StringBuilder builder, List<List<Map<String, Object>>> reportData) {
        if (reportData == null || reportData.isEmpty()) {
            return;
        }

        for (int i = 0; i < reportData.size(); i++) {
            List<Map<String, Object>> rowData = reportData.get(i);

            if (rowData == null || rowData.isEmpty()) {
                continue;
            }

            for (int j = 0; j < rowData.size(); j++) {
                Map<String, Object> cellData = rowData.get(j);

                String value = getCellValue(cellData);
                builder.append(clean(value));

                // add delimeter between cells in the row only if the cell is not the last
                if (j != rowData.size() - 1) {
                    builder.append(delimeter);
                }
            }

            // add separator only if the row is not the last
            if (i != reportData.size() - 1) {
                builder.append(separator);
            }
        }
    }

    private String getCellValue(Map<String, Object> cellData) {
        if (cellData == null) {
            return "";
        }

        String value = cellData.get(ReportProducer.DATA_VALUE_ATTR) != null ? cellData.get(ReportProducer.DATA_VALUE_ATTR).toString() : "";

        String dataType = getStr(cellData, ReportProducer.DATA_TYPE_ATTR);
        if (ReportProducer.DATA_TYPE_HYPERLINK.equals(dataType)) {
            String url = getStr(cellData, ReportProducer.DATA_HYPERLINK_URL_ATTR);
            if (StringUtils.isNotBlank(url)) {
                return value + " (" + url + ")";
            }
        }

        return value;
    }

    private String getStr(Map<String, Object> cellData, String attName) {
        return cellData.get(attName) != null ? cellData.get(attName).toString() : "";
    }

    /**
     * Clear original data from newline symbols. Prevents distortion of
     * csv-file structure.
     */
    private String clean(String source) {
        if (source != null) {
            return source.replaceAll("[\r\n]", " ").trim();
        }

        return null;
    }
}