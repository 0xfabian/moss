import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CLI {
    private boolean quit = false;
    private final Scanner sc = new Scanner(System.in);
    private final MedicalOffice mo = MedicalOffice.getInstance();

    private final Map<String, Command> globalCommands = new HashMap<>();
    private final Map<String, Command> userCommands = new HashMap<>();
    private final Map<String, Command> adminCommands = new HashMap<>();
    private final Map<String, Command> medicCommands = new HashMap<>();
    private final Map<String, Command> receptionistCommands = new HashMap<>();

    public static final String RESET = "\033[0m";
    public static final String RED = "\033[91m";
    public static final String YELLOW = "\033[93m";
    public static final String BLUE = "\033[94m";
    public static final String GREEN = "\033[92m";

    public static final String[] colors = { "\033[101m", "\033[103m", "\033[104m" }; // { "\033[0;106m", "\033[0;46m" };

    CLI() {
        globalCommands.put("exit", (args) ->  quit = true);
        globalCommands.put("clear", (args) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        });
        globalCommands.put("login", (args) -> {
            if (args.length != 3) {
                System.out.println(YELLOW + "Usage: login <username> <password>" + RESET);
                return;
            }

            if (!mo.logIn(args[1], args[2]))
                System.out.println(RED + "Invalid username or password" + RESET);
        });

        userCommands.put("logout", (args) -> mo.logOut());
        userCommands.put("passwd", (args) -> {
            System.out.print("Current password: ");

            String passwd = sc.nextLine();
            if (!passwd.equals(mo.getCurrentUser().getPassword())) {
                System.out.println(RED + "Incorrect password" + RESET);
            }
            else {
                System.out.print("New password: ");
                passwd = sc.nextLine();
                mo.getCurrentUser().changePassword(passwd);
                System.out.println("Password changed successfully");
            }
        });
        userCommands.put("me", (args) -> System.out.println(mo.getCurrentUser().getTable()));

        adminCommands.put("add", (args) -> {
            if (args.length != 2) {
                System.out.println(YELLOW + "Usage: add <role>" + RESET);
                return;
            }

            Admin admin = (Admin)mo.getCurrentUser();
            String role = args[1];

            try {
                switch (role) {
                    case "medic", "m" -> admin.addUser(new Medic(sc));
                    case "receptionist", "r" -> admin.addUser(new Receptionist(sc));
                    case "admin", "a" -> admin.addUser(new Admin(sc));
                    default -> System.out.println(RED + "Invalid role '" + role + "'" + RESET);
                }
            }
            catch (Exception e) {
                System.out.println(RED + e.getMessage() + RESET);
            }
        });
        adminCommands.put("remove", (args) -> {
            if (args.length != 2) {
                System.out.println(YELLOW + "Usage: remove <id>" + RESET);
                return;
            }

            Admin admin = (Admin)mo.getCurrentUser();

            try {
                admin.removeUser(mo.getUserIdFromString(args[1]));
            }
            catch (Exception e) {
                System.out.println(RED + e.getMessage() + RESET);
            }
        });
        adminCommands.put("modify", (args) -> {
            if (args.length < 3) {
                System.out.println(YELLOW + "Usage: modify <id> <field>=<value> ..." + RESET);
                return;
            }

            Admin admin = (Admin) mo.getCurrentUser();
            int id = mo.getUserIdFromString(args[1]);

            for (int i = 2; i < args.length; i++) {
                try {
                    admin.modifyUserField(id, args[i].split("=")[0], args[i].split("=")[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(RED + "Argument '" + args[i] + "' is not in <field>=<value> form" + RESET);
                } catch (Exception e) {
                    System.out.println(RED + e.getMessage() + RESET);

                    if (e.getMessage().equals("User not found"))
                        return;
                }
            }
        });
        adminCommands.put("ls", (args) -> {
            if (args.length < 2) {
                System.out.println(YELLOW + "Usage: ls <table> [filter] ..." + RESET);
                return;
            }

            if ((args[1].equals("logs") || args[1].equals("l")) && !((Admin)mo.getCurrentUser()).getCanViewLogs()) {
                System.out.println(RED + "You don't have permission to view logs" + RESET);
                return;
            }

            Table tb = switch (args[1]) {
                case "users", "u" -> mo.getUserTable();
                case "logs", "l" -> LogService.getInstance().logs.getTable();
                default -> null;
            };

            for (int i = 2; i < args.length; i++) {
                if (tb == null)
                    return;

                try {
                    if (args[i].contains("="))
                        tb = tb.filter(args[i].split("=")[0], args[i].split("=")[1]);
                    else if (args[i].contains("~"))
                        tb = tb.grep(args[i].split("~")[0], args[i].split("~")[1]);
                    else
                        throw new RuntimeException();
                }
                catch (Exception e) {
                    System.out.println(RED + "Invalid filter '" + args[i] + "'" + RESET);
                    return;
                }
            }

            if (tb != null)
                System.out.println(tb);
        });
        adminCommands.put("info", (args) -> {
            if (args.length != 2) {
                System.out.println(YELLOW + "Usage: info <id>" + RESET);
                return;
            }

            User user = mo.getUserById(mo.getUserIdFromString(args[1]));

            if (user != null)
                System.out.println(user.getTable());
            else
                System.out.println(RED + "User not found" + RESET);
        });

        receptionistCommands.put("ls", (args) -> {
            if (args.length < 2) {
                System.out.println(YELLOW + "Usage: ls <table> [filter] ..." + RESET);
                return;
            }

            Table tb = switch (args[1]) {
                case "patients", "p" -> mo.patients.getTable();
                case "medics", "m" -> mo.medics.getTable();
                case "appointments", "a" -> mo.appointments.getTable();
                default -> null;
            };

            for (int i = 2; i < args.length; i++) {
                if (tb == null)
                    return;

                try {
                    if (args[i].contains("="))
                        tb = tb.filter(args[i].split("=")[0], args[i].split("=")[1]);
                    else if (args[i].contains("~"))
                        tb = tb.grep(args[i].split("~")[0], args[i].split("~")[1]);
                    else
                        throw new RuntimeException();
                }
                catch (Exception e) {
                    System.out.println(RED + "Invalid filter '" + args[i] + "'" + RESET);
                    return;
                }
            }

            if (tb != null)
                System.out.println(tb);
        });
        receptionistCommands.put("sched", (args) -> {
            Receptionist receptionist = (Receptionist)mo.getCurrentUser();

            try {
                Patient patient = new Patient(sc);
                patient = receptionist.addPatient(patient);

                System.out.print("Problem: ");
                String problem = sc.nextLine();
                System.out.print("Comment: ");
                String comment = sc.nextLine();

                Appointment appointment = receptionist.scheduleAppointment(patient, problem, comment);
                System.out.println("Appointment successfully scheduled for " + appointment.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm")));
            }
            catch (Exception e) {
                System.out.println(RED + e.getMessage() + RESET);
            }
        });
        receptionistCommands.put("resched", (args) -> {
            if (args.length != 2) {
                System.out.println(YELLOW + "Usage: resched <id>" + RESET);
                return;
            }

            Receptionist receptionist = (Receptionist)mo.getCurrentUser();

            try {
                Appointment appointment = receptionist.rescheduleAppointment(Integer.parseUnsignedInt(args[1]));
                System.out.println("Appointment successfully rescheduled for " + appointment.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm")));
            }
            catch (NumberFormatException e) {
                System.out.println(RED + "ID is invalid" + RESET);
            }
            catch (RuntimeException e) {
                System.out.println(RED + e.getMessage() + RESET);
            }
        });
        receptionistCommands.put("cancel", (args) -> {
            if (args.length != 2) {
                System.out.println(YELLOW + "Usage: cancel <id>" + RESET);
                return;
            }

            Receptionist receptionist = (Receptionist)mo.getCurrentUser();

            try {
                receptionist.cancelAppointment(Integer.parseUnsignedInt(args[1]));
            }
            catch (NumberFormatException e) {
                System.out.println(RED + "ID is invalid" + RESET);
            }
            catch (RuntimeException e) {
                System.out.println(RED + e.getMessage() + RESET);
            }
        });
        receptionistCommands.put("remove", (args) -> {
            if (args.length != 2) {
                System.out.println(YELLOW + "Usage: remove <id>" + RESET);
                return;
            }

            Receptionist receptionist = (Receptionist)mo.getCurrentUser();

            try {
                receptionist.removePatient(mo.getUserIdFromString(args[1]));
            }
            catch (Exception e) {
                System.out.println(RED + e.getMessage() + RESET);
            }
        });
        receptionistCommands.put("modify", (args) -> {
            if (args.length < 3) {
                System.out.println(YELLOW + "Usage: modify <id> <field>=<value> ..." + RESET);
                return;
            }

            Receptionist receptionist = (Receptionist) mo.getCurrentUser();
            int id = mo.getUserIdFromString(args[1]);

            for (int i = 2; i < args.length; i++) {
                try {
                    receptionist.modifyPatientField(id, args[i].split("=")[0], args[i].split("=")[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(RED + "Argument '" + args[i] + "' is not in <field>=<value> form" + RESET);
                } catch (Exception e) {
                    System.out.println(RED + e.getMessage() + RESET);

                    if (e.getMessage().equals("Patient not found"))
                        return;
                }
            }
        });

        medicCommands.put("week", (args) -> {
            if (args.length > 2) {
                System.out.println(YELLOW + "Usage: week [offset]" + RESET);
                return;
            }

            int weekOffset = 0;

            if (args.length == 2) {
                try {
                    weekOffset = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException e) {
                    System.out.println(RED + "Invalid week offset" + RESET);
                    return;
                }
            }

            LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(weekOffset);

            Medic medic = (Medic) mo.getCurrentUser();
            List<Appointment> appointments = new ArrayList<>();

            for (Appointment appointment : mo.appointments.getList()) {
                boolean correctStatus = (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.MISSED || appointment.getStatus() == AppointmentStatus.SCHEDULED);
                boolean correctWeek = appointment.getDate().isAfter(weekStart.atStartOfDay()) && appointment.getEnd().isBefore(weekStart.plusWeeks(1).atStartOfDay());

                if (appointment.getMedicId() == medic.getId() &&  correctStatus && correctWeek)
                    appointments.add(appointment);
            }

            int shiftHours = medic.getShiftEnd() - medic.getShiftStart();

            Table tb = new Table(1 + shiftHours, 1 + 5);
            tb.setFirstRowDelimiter();
            tb.setRow(0, new String[]{"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"});
            for (int i = 0; i < shiftHours; i++)
                tb.setData(i + 1, 0, (i + medic.getShiftStart()) + ":00");

            int colorIndex = 0;

            for (Appointment appointment : appointments) {
                int day = appointment.getDate().getDayOfWeek().getValue();
                int hour = appointment.getDate().getHour();
                Patient patient = mo.getPatientById(appointment.getPatientId());
                int startIndex = 1 + (hour - medic.getShiftStart());

                for (int i = 0; i < appointment.getDuration().toHours(); i++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(colors[colorIndex % colors.length]).append(" ").append(RESET);

                    if (i == 0) {
                        sb.append(" ").append(appointment.getId()).append(" ").append(patient.getName()).append(" ");

                        if (appointment.getStatus() == AppointmentStatus.COMPLETED)
                            sb.append(GREEN).append("■").append(RESET);
                        else if (appointment.getStatus() == AppointmentStatus.MISSED)
                            sb.append(RED).append("■").append(RESET);
                    }

                    tb.setData(i + startIndex, day, sb.toString());
                }

                colorIndex++;
            }

            System.out.println(tb);
        });
        medicCommands.put("info", (args) -> {
            if (args.length != 2) {
                System.out.println(YELLOW + "Usage: info <id>" + RESET);
                return;
            }

            try {
                Appointment appointment = mo.getAppointmentById(Integer.parseUnsignedInt(args[1]));

                if (appointment != null)
                    System.out.println(appointment.getTable());
                else
                    System.out.println(RED + "Appointment not found" + RESET);
            }
            catch (NumberFormatException e) {
                System.out.println(RED + "ID is invalid" + RESET);
            }
        });
        medicCommands.put("ls", (args) -> {
            if (args.length < 2) {
                System.out.println(YELLOW + "Usage: ls <table> [filter] ..." + RESET);
                return;
            }

            Medic medic = (Medic) mo.getCurrentUser();

            Table tb = switch (args[1]) {
                case "patients", "p" -> Table.ofCSVList(medic.getAllPatients());
                case "appointments", "a" -> Table.ofCSVList(medic.getAllAppointments());
                default -> null;
            };

            for (int i = 2; i < args.length; i++) {
                if (tb == null)
                    return;

                try {
                    if (args[i].contains("="))
                        tb = tb.filter(args[i].split("=")[0], args[i].split("=")[1]);
                    else if (args[i].contains("~"))
                        tb = tb.grep(args[i].split("~")[0], args[i].split("~")[1]);
                    else
                        throw new RuntimeException();
                }
                catch (Exception e) {
                    System.out.println(RED + "Invalid filter '" + args[i] + "'" + RESET);
                    return;
                }
            }

            if (tb != null)
                System.out.println(tb);
        });
    }

    public void prompt() {
        if (mo.isLoggedIn()) {
            System.out.print(BLUE + mo.getCurrentUser().getUsername() + RESET);
            System.out.print(mo.getCurrentUser().isAdmin() ? "$ " : "> ");
        }
        else
            System.out.print("> ");
    }

    public String[] getArgs(String input) {
        boolean inQuotes = false;
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        input = input.trim();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (!sb.isEmpty()) {
                    result.add(sb.toString());
                    sb.setLength(0);
                }
            } else
                sb.append(c);
        }

        if (!sb.isEmpty())
            result.add(sb.toString());

        return result.toArray(String[]::new);
    }

    public void execute(String line) {
        if (line.isEmpty())
            return;

        String[] args = getArgs(line);
        Command command = null;

        if (mo.isLoggedIn()) {
            if (mo.getCurrentUser().isAdmin())
                command = adminCommands.get(args[0]);
            else if (mo.getCurrentUser().isMedic())
                command = medicCommands.get(args[0]);
            else if (mo.getCurrentUser().isReceptionist())
                command = receptionistCommands.get(args[0]);

            if (command == null)
                command = userCommands.get(args[0]);
        }

        if(command == null)
            command = globalCommands.get(args[0]);

        if (command == null)
            System.out.println("Unknown command");
        else
            command.execute(args);
    }

    public void run() {
        while(!quit) {
            prompt();
            execute(sc.nextLine());
        }

        sc.close();
    }
}
