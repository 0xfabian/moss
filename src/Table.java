import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Table {
    private final String[][] data;
    int rowDelimiter;

    Table(int row, int col) {
        data = new String[row][col];
        rowDelimiter = -1;

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                data[i][j] = "";
            }
        }
    }

    Table(List<String[]> rows) {
        data = new String[rows.size()][rows.get(0).length];
        for (int i = 0; i < rows.size(); i++)
            setRow(i, rows.get(i));
    }

    public void setData(int row, int col, String data) {
        this.data[row][col] = data;
    }

    public void setRow(int row, String[] data) {
        this.data[row] = data;
    }

    public void setFirstRowDelimiter() {
        rowDelimiter = 0;
    }

    public void setRowDelimiter(int row) {
        rowDelimiter = row;
    }

    private String padWithSpaces(String s, int length) {
        return s + " ".repeat(length - getColoredStringLength(s));
    }

    private int getColoredStringLength(String s) {
        String uncolored = s.replaceAll("\\u001B\\[[;\\d]*m", "");
        return uncolored.length();
    }

    @Override
    public String toString() {
        int[] maxStringSize = new int[data[0].length];

        for (int col = 0; col < data[0].length; col++) {
            for (String[] datum : data) {
                if (getColoredStringLength(datum[col]) > maxStringSize[col]) {
                    maxStringSize[col] = getColoredStringLength(datum[col]);
                }
            }
        }

        StringBuilder lineTemplate = new StringBuilder();
        lineTemplate.append('a');

        for (int col = 0; col < data[0].length; col++) {
            lineTemplate.append("─".repeat(maxStringSize[col] + 2)).append('b');
        }
        lineTemplate.setCharAt(lineTemplate.length() - 1, 'c');

        String topLine = lineTemplate.toString().replace('a', '┌').replace('b', '┬').replace('c', '┐');
        String midLine = lineTemplate.toString().replace('a', '├').replace('b', '┼').replace('c', '┤');
        String botLine = lineTemplate.toString().replace('a', '└').replace('b', '┴').replace('c', '┘');

        StringBuilder result = new StringBuilder();

        result.append(topLine).append('\n');

        for (int row = 0; row < data.length; row++) {
            result.append("│ ");
            for (int col = 0; col < data[row].length; col++) {
                result.append(padWithSpaces(data[row][col], maxStringSize[col]));
                if (col < data[row].length - 1) {
                    result.append(" │ ");
                }
            }
            result.append(" │\n");

            if (row == rowDelimiter && row < data.length - 1)
                result.append(midLine).append('\n');
        }

        result.append(botLine);

        return result.toString();
    }

    public int getColumIndex(String column) {
        for (int col = 0; col < data[0].length; col++)
            if (data[0][col].equalsIgnoreCase(column))
                return col;

        return -1;
    }

    public Table filter(String column, String value) {
        int col = getColumIndex(column);

        if (col == -1)
            return null;

        List<String[]> newData = new ArrayList<>();

        for (int row = 1; row < data.length; row++)
            if (data[row][col].equals(value))
                newData.add(data[row]);

        if (newData.isEmpty())
            return null;

        newData.add(0, data[0]);

        Table tb = new Table(newData);
        tb.setRowDelimiter(rowDelimiter);

        return tb;
    }

    private String grepLine(Pattern pattern, String line) {
        Matcher matcher = pattern.matcher(line);

        StringBuilder sb = new StringBuilder();

        int lastPos = 0;
        while (matcher.find()) {
            sb.append(line, lastPos, matcher.start());
            sb.append("\033[91m").append(line, matcher.start(), matcher.end()).append("\033[0m");
            lastPos = matcher.end();
        }

        if (lastPos == 0)
            return null;

        sb.append(line.substring(lastPos));
        return sb.toString();
    }

    public Table grep(String column, String regex) {
        int col = getColumIndex(column);

        if (col == -1)
            return null;

        List<String[]> newData = new ArrayList<>();

        Pattern pattern = Pattern.compile(regex);

        for (int row = 1; row < data.length; row++) {
            String value = grepLine(pattern, data[row][col]);
            if (value != null) {
                String[] temp = data[row].clone();
                temp[col] = value;
                newData.add(temp);
            }
        }

        if (newData.isEmpty())
            return null;

        newData.add(0, data[0]);

        Table tb = new Table(newData);
        tb.setRowDelimiter(rowDelimiter);

        return tb;
    }

    public static <T extends CSVSerializable> Table ofCSVList(List<T> list) {
        if (list.isEmpty())
            return null;

        String[] names = list.get(0).getColumNames();
        Table tb = new Table(list.size() + 1, names.length);
        tb.setFirstRowDelimiter();
        tb.setRow(0, names);

        for (int i = 0; i < list.size(); i++)
            tb.setRow(i + 1, list.get(i).getColumns());

        return tb;
    }
}
