package io.github.dk900912.easyexcel.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.excel.read.metadata.holder.ReadRowHolder;
import com.alibaba.excel.read.metadata.holder.ReadSheetHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author dukui
 */
public class CollectorReadListener extends AnalysisEventListener<Object> {

    private static final Logger log = LoggerFactory.getLogger(CollectorReadListener.class);

    private final List<Object> data = new CopyOnWriteArrayList<>();

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        super.onException(exception, context);
    }

    @Override
    public void invoke(Object data, AnalysisContext context) {
        this.data.add(data);
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        super.extra(extra, context);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("0===={===============>");
        int sheetIndex = Optional.ofNullable(context)
                .map(AnalysisContext::readSheetHolder)
                .map(ReadSheetHolder::getSheetNo)
                .orElse(-1);
        int totalRow = Optional.ofNullable(context)
                .map(AnalysisContext::readRowHolder)
                .map(ReadRowHolder::getRowIndex)
                .orElse(-1);
        log.info("sheet-index = {}, total-row = {}", sheetIndex, totalRow);
        log.info("<===============}====0");
    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        return super.hasNext(context);
    }

    public List<List<Object>> groupByHeadClazz() {
        return new ArrayList<>(data.stream()
                .collect(Collectors.groupingBy(Object::getClass, Collectors.toList()))
                .values());
    }
}
