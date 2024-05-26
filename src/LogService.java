public class LogService {
    private static LogService instance;
    CSVConnection<Log> logs;

    LogService() {
        logs = new CSVConnection<>("database/logs.csv", Log::new);
    }

    public static LogService getInstance() {
        if (instance == null)
            instance = new LogService();

        return instance;
    }

    public static void log(LogAction action, String message) {
        getInstance().logs.add(new Log(action, message));
    }
}
