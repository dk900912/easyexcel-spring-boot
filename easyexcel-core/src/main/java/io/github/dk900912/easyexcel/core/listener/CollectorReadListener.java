package io.github.dk900912.easyexcel.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dukui
 */
public class CollectorReadListener extends AnalysisEventListener<Object> {

    private static final Logger log = LoggerFactory.getLogger(CollectorReadListener.class);

    private final Multimap<Integer, Object> multiSheetData = Multimaps.synchronizedMultimap(LinkedListMultimap.create());

    @Override
    public void invoke(Object data, AnalysisContext context) {
        int sheetIndex = context.readSheetHolder().getSheetNo();
        multiSheetData.put(sheetIndex, data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("0===={===============>");
        int sheetIndex = context.readSheetHolder().getSheetNo();
        int totalRow = context.readSheetHolder().getRowIndex();
        log.info("sheet-index = {}, total-row = {}", sheetIndex, totalRow);
        log.info("<===============}====0");
    }

    public List<List<Object>> groupByHeadClazz() {
        return multiSheetData.asMap()
                .values()
                .stream()
                .map(collection -> (List<Object>) collection)
                .collect(Collectors.toList());
    }
}
