package id.rizal.skripsi;

import com.anychart.chart.common.dataentry.ValueDataEntry;

public class DataModel extends ValueDataEntry {
    public DataModel(String x, Number value) {
        super(x, value);
    }

    public DataModel(Number x, Number value) {
        super(x, value);
    }
}
