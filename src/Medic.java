import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Medic extends User implements CSVSerializable{
    private String name;
    private String email;
    private String specialization;
    private String office;
    private int shiftStart;
    private int shiftEnd;

    Medic() {}

    Medic(Scanner sc) {
        super(sc);
        System.out.print("Name: ");
        this.name = sc.nextLine();
        System.out.print("Email: ");
        this.email = sc.nextLine();
        System.out.print("Specialization: ");
        this.specialization = sc.nextLine();
        System.out.print("Office: ");
        this.office = sc.nextLine();
        System.out.print("Shift: ");
        String shift = sc.nextLine();
        this.shiftStart = Integer.parseInt(shift.split("-")[0]);
        this.shiftEnd = Integer.parseInt(shift.split("-")[1]);
        // TODO posibil shift invalid, mai bine folosim setter cred
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public int getShiftStart() {
        return shiftStart;
    }

    public int getShiftEnd() {
        return shiftEnd;
    }

    public void setShift(int shiftStart, int shiftEnd) {
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        // TODO throw pentru invalid shift
    }

    public void setShift(String value) {
        try {
            int startShift = Integer.parseInt(value.split("-")[0]);
            int endShift = Integer.parseInt(value.split("-")[1]);

            setShift(startShift, endShift);
        }
        catch (Exception ignored) {}
    }

    // TODO e mai bine daca dam ca parametru afterDate sau ceva
    public List<Appointment> getFutureAppointments() {
        List<Appointment> appointments = new ArrayList<>();

        for (Appointment appointment : MedicalOffice.getInstance().appointments.getList())
            if (appointment.getMedicId() == id && appointment.getStatus() == AppointmentStatus.SCHEDULED && appointment.getDate().isAfter(LocalDateTime.now()))
                appointments.add(appointment);

        return appointments;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();

        for (Appointment appointment : MedicalOffice.getInstance().appointments.getList())
            if (appointment.getMedicId() == id)
                appointments.add(appointment);

        return appointments;
    }

    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        List<Appointment> appointments = getAllAppointments();

        for (Appointment appointment : appointments) {
            Patient patient = MedicalOffice.getInstance().getPatientById(appointment.getPatientId());
            if (patient != null)
                patients.add(patient);
        }

        return patients.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public void modifyField(String field, String value) {
        try {
            super.modifyField(field, value);
        }
        catch (InvalidFieldException e) {
            switch (field) {
                case "name" -> setName(value);
                case "email" -> setEmail(value);
                case "specialization" -> setSpecialization(value);
                case "office" -> setOffice(value);
                case "shift" -> setShift(value);
                default -> throw e;
            }

            MedicalOffice.getInstance().medics.writeCSV();
            LogService.log(LogAction.MODIFY_USER, username + "'s " + field + " was modified by " + MedicalOffice.getInstance().getCurrentUser().getUsername());
        }
    }

    @Override
    public Table getTable() {
        Table tb = new Table(8, 2);
        tb.setRow(0, new String[]{"Role", "MEDIC"});
        tb.setRow(1, new String[]{"Username", username});
        tb.setRow(2, new String[]{"Password", password});
        tb.setRow(3, new String[]{"Name", name});
        tb.setRow(4, new String[]{"Email", email});
        tb.setRow(5, new String[]{"Specialization", specialization});
        tb.setRow(6, new String[]{"Office", office});
        tb.setRow(7, new String[]{"Shift", shiftStart + "-" + shiftEnd});

        return tb;
    }

    @Override
    public String toCSV() {
        return id + "," + username + "," + password + "," + name + "," + email + "," + specialization + "," + office + "," + shiftStart + "," + shiftEnd;
    }

    @Override
    public void fromCSV(String csv) {
        String[] data = csv.split(",");
        id = Integer.parseInt(data[0]);
        username = data[1];
        password = data[2];
        name = data[3];
        email = data[4];
        specialization = data[5];
        office = data[6];
        shiftStart = Integer.parseInt(data[7]);
        shiftEnd = Integer.parseInt(data[8]);
    }

    @Override
    public String[] getColumNames() {
        return new String[]{"ID", "Username", "Password", "Name", "Email", "Specialization", "Office Number", "Shift Start", "Shift End"};
    }

    @Override
    public String[] getColumns() {
        return new String[]{String.valueOf(id), username, password, name, email, specialization, office, String.valueOf(shiftStart), String.valueOf(shiftEnd)};
    }
}
