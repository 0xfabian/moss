import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Log implements CSVSerializable {
    private LogAction action;
    private String message;
    private LocalDate date;
    private LocalTime time;

    Log() {}

    Log(LogAction action, String message) {
        this.action = action;
        this.message = message;
        this.date = LocalDate.now();
        this.time = LocalTime.now();
    }

    @Override
    public String toCSV() {
        return  action + "," + message + "," + date + "," + time;
    }

    @Override
    public void fromCSV(String csv) {
        String[] data = csv.split(",");
        this.action = LogAction.valueOf(data[0]);
        this.message = data[1];
        this.date = LocalDate.parse(data[2]);
        this.time = LocalTime.parse(data[3]);
    }

    @Override
    public String[] getColumNames() {
        return new String[]{"Action", "Message", "Date", "Time"};
    }

    @Override
    public String[] getColumns() {
        return new String[]{action.toString(), message, date.toString(), time.format(DateTimeFormatter.ofPattern("HH:mm"))};
    }
}
