import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MedicalOffice {
    private static MedicalOffice instance;
    CSVConnection<Medic> medics;
    CSVConnection<Receptionist> receptionists;
    CSVConnection<Admin> admins;
    CSVConnection<Patient> patients;
    CSVConnection<Appointment> appointments;
    private User currentUser;

    private MedicalOffice() {
        medics = new CSVConnection<>("database/medics.csv", Medic::new);
        receptionists = new CSVConnection<>("database/receptionists.csv", Receptionist::new);
        admins = new CSVConnection<>("database/admins.csv", Admin::new);
        patients = new CSVConnection<>("database/patients.csv", Patient::new);
        appointments = new CSVConnection<>("database/appointments.csv", Appointment::new);
        currentUser = null;

        validateAppointments();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        LocalTime now = LocalTime.now();
        LocalTime nextHour = LocalTime.of(now.getHour(), 0).plusHours(1);
        long initialDelay = ChronoUnit.MILLIS.between(now, nextHour);
        executor.scheduleAtFixedRate(this::validateAppointments, initialDelay, 1, TimeUnit.HOURS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdownNow();

            if (isLoggedIn())
                logOut();
        }));
    }

    public static MedicalOffice getInstance() {
        if (instance == null)
            instance = new MedicalOffice();

        return instance;
    }

    public List<User> getUsers() {
        List<User> users = new ArrayList<>();

        users.addAll(medics.getList());
        users.addAll(receptionists.getList());
        users.addAll(admins.getList());

        users.sort(Comparator.comparingInt(User::getId));

        return users;
    }

    public Table getUserTable() {
        List<User> users = getUsers();

        if (users.isEmpty())
            return null;

        Table tb = new Table(1 + users.size(), 4);
        tb.setFirstRowDelimiter();
        tb.setRow(0, new String[]{"ID", "Username", "Password", "Role"});

        for (int i = 0; i < users.size(); i++) {
            tb.setData(i + 1, 0, String.valueOf(users.get(i).getId()));
            tb.setData(i + 1, 1, users.get(i).getUsername());
            tb.setData(i + 1, 2, users.get(i).getPassword());

            if (users.get(i).isAdmin())
                tb.setData(i + 1, 3, "ADMIN");
            else if (users.get(i).isReceptionist())
                tb.setData(i + 1, 3, "RECEPTIONIST");
            else if (users.get(i).isMedic())
                tb.setData(i + 1, 3, "MEDIC");
        }

        return tb;
    }

    public User getUserById(int id) {
        List<User> users = getUsers();

        for(User user : users)
            if(user.getId() == id)
                return user;

        return null;
    }

    public User getUserByName(String username) {
        List<User> users = getUsers();

        for(User user : users)
            if(user.getUsername().equals(username))
                return user;

        return null;
    }

    public int getUserIdFromString(String idOrUsername) {
        try {
            return Integer.parseUnsignedInt(idOrUsername);
        }
        catch (NumberFormatException e) {
            User user = getUserByName(idOrUsername);
            if (user != null)
                return user.getId();
        }

        return 0;
    }

    public Patient getPatientById(int id) {
        for (Patient patient : patients.getList())
            if (patient.getId() == id)
                return patient;

        return null;
    }

    public Appointment getAppointmentById(int id) {
        for (Appointment appointment : appointments.getList())
            if (appointment.getId() == id)
                return appointment;

        return null;
    }

    public void validateAppointments() {
        Random random = new Random();
        int updates = 0;

        for (Appointment appointment : appointments.getList())
            if (appointment.getStatus() == AppointmentStatus.SCHEDULED && LocalDateTime.now().isAfter(appointment.getEnd())) {
                appointment.setStatus((random.nextFloat() < 0.1) ? AppointmentStatus.MISSED : AppointmentStatus.COMPLETED);
                updates++;
            }

        appointments.writeCSV();
        LogService.log(LogAction.VALIDATION, updates + " appointments were updates");
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean logIn(String username, String password) {
        for(User user : getUsers())
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                if (isLoggedIn())
                    logOut();
                currentUser = user;
                LogService.log(LogAction.LOGIN, username + " logged in");
                return true;
            }

        return false;
    }

    public void logOut() {
        LogService.log(LogAction.LOGOUT, currentUser.getUsername() + " logged out");
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
