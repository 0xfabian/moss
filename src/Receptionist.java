import java.util.Scanner;

public class Receptionist extends User implements CSVSerializable {
    private String name;
    private String email;
    private String workStation;

    Receptionist() {}

    Receptionist(Scanner sc) {
        super(sc);
        System.out.print("Name: ");
        this.name = sc.nextLine();
        System.out.print("Email: ");
        this.email = sc.nextLine();
        System.out.print("Work station: ");
        this.workStation = sc.nextLine();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setWorkStation(String workStation) {
        this.workStation = workStation;
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
                case "workStation" -> setWorkStation(value);
                default -> throw e;
            }

            MedicalOffice.getInstance().receptionists.writeCSV();
            LogService.log(LogAction.MODIFY_USER, username + "'s " + field + " was modified by " + MedicalOffice.getInstance().getCurrentUser().getUsername());
        }
    }

    public Patient addPatient(Patient newPatient) {
        for(Patient patient : MedicalOffice.getInstance().patients.getList())
            if (newPatient.equals(patient))
                return patient;
            else if(newPatient.getEmail().equals(patient.getEmail()))
                throw new RuntimeException("Email is already used");

        newPatient.assignID();
        MedicalOffice.getInstance().patients.add(newPatient);

        LogService.log(LogAction.ADD_PATIENT, newPatient.getName() + " was added by " + username);

        return newPatient;
    }

    public void removePatient(int id) {
        Patient patient = MedicalOffice.getInstance().getPatientById(id);

        if (patient == null)
            throw new RuntimeException("Patient not found");

        for (Appointment appointment : MedicalOffice.getInstance().appointments.getList())
            if (appointment.getPatientId() == patient.getId())
            {
                appointment.setPatientId(0);
                appointment.setStatus(AppointmentStatus.INVALID);
            }

        LogService.log(LogAction.REMOVE_PATIENT, patient.getName() + " was removed by " + username);
        MedicalOffice.getInstance().patients.remove(patient);
        MedicalOffice.getInstance().appointments.writeCSV();
    }

    public void modifyPatientField(int id, String field, String value) {
        Patient patient = MedicalOffice.getInstance().getPatientById(id);

        if (patient == null)
            throw new RuntimeException("Patient not found");

        patient.modifyField(field, value);
    }

    public Appointment scheduleAppointment(Patient patient, String problem, String comment) {
        Appointment appointment = new Appointment(patient, problem, comment);
        Scheduler.getInstance().schedule(appointment);
        appointment.assignID();
        MedicalOffice.getInstance().appointments.add(appointment);

        LogService.log(LogAction.SCHED_APPOINTMENT, username + " scheduled an appointment(" + appointment.getId() + ") for " + patient.getName());

        return appointment;
    }

    public Appointment rescheduleAppointment(int id) {
        Appointment appointment = MedicalOffice.getInstance().getAppointmentById(id);

        if (appointment == null)
            throw new RuntimeException("Appointment not found");

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED)
            throw new RuntimeException("Appointment not scheduled");

        Patient patient = MedicalOffice.getInstance().getPatientById(appointment.getPatientId());

        if (patient == null)
            throw new RuntimeException("Patient not found");

        Appointment newAppointment = new Appointment(patient, appointment.getProblem(), appointment.getComment());
        Scheduler.getInstance().schedule(newAppointment);
        newAppointment.assignID();
        MedicalOffice.getInstance().appointments.add(newAppointment);

        appointment.setStatus(AppointmentStatus.RESCHEDULED);
        MedicalOffice.getInstance().appointments.writeCSV();

        LogService.log(LogAction.RESCHED_APPOINTMENT, username + " rescheduled an appointment(" + newAppointment.getId() + ") for " + patient.getName());

        return newAppointment;
    }

    public void cancelAppointment(int id) {
        Appointment appointment = MedicalOffice.getInstance().getAppointmentById(id);

        if (appointment == null)
            throw new RuntimeException("Appointment not found");

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED)
            throw new RuntimeException("Appointment not scheduled");

        appointment.setStatus(AppointmentStatus.CANCELLED);
        MedicalOffice.getInstance().appointments.writeCSV();
        LogService.log(LogAction.CANCEL_APPOINTMENT, username + " cancelled an appointment(" + appointment.getId() + ")");
    }

    @Override
    public Table getTable() {
        Table tb = new Table(6, 2);
        tb.setRow(0, new String[]{"Role", "RECEPTIONIST"});
        tb.setRow(1, new String[]{"Username", username});
        tb.setRow(2, new String[]{"Password", password});
        tb.setRow(3, new String[]{"Name", name});
        tb.setRow(4, new String[]{"Email", email});
        tb.setRow(5, new String[]{"Work Station", workStation});

        return tb;
    }

    @Override
    public String toCSV() {
        return id + "," + username + "," + password + "," + name + "," + email + "," + workStation;
    }

    @Override
    public void fromCSV(String csv) {
        String[] data = csv.split(",");
        id = Integer.parseInt(data[0]);
        username = data[1];
        password = data[2];
        name = data[3];
        email = data[4];
        workStation = data[5];
    }

    @Override
    public String[] getColumNames() {
        return new String[]{"ID", "Username", "Password", "Name", "Email", "Work Station"};
    }

    @Override
    public String[] getColumns() {
        return new String[]{String.valueOf(id), username, password, name, email, workStation};
    }
}
