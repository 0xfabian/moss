import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CSVConnection<T extends CSVSerializable> {
    private final List<T> list;
    private final Supplier<T> supplier;
    private final String fileName;

    CSVConnection(String fileName, Supplier<T> supplier) {
        this.fileName = fileName;
        this.supplier = supplier;
        this.list = readCSV();
    }

    private List<T> readCSV() {
        List<T> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while((line = br.readLine()) != null) {
                T item = supplier.get();
                item.fromCSV(line);
                items.add(item);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
        return items;
    }

    public void writeCSV() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (T item : list)
                bw.write(item.toCSV() + "\n");
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    public List<T> getList() {
        return list;
    }

    public void add(T item) {
        list.add(item);
        writeCSV();
    }

    public void remove(T value) {
        list.remove(value);
        writeCSV();
    }

    public Table getTable() {
        return Table.ofCSVList(list);
    }
}
