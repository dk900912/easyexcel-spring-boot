package io.github.dk900912.easyexcel.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dukui
 */
public class EmptyReadListener<T> extends AnalysisEventListener<T> {

    /**
     * Thread-safe container is unnecessary
     */
    private final List<T> data = new ArrayList<>();

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        super.onException(exception, context);
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        this.data.add(data);
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        super.extra(extra, context);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        return super.hasNext(context);
    }

    public List<T> getData() {
        return data;
    }
}
