public interface CSVSerializable {
    String toCSV();
    void fromCSV(String csv);
    String[] getColumNames();
    String[] getColumns();
}