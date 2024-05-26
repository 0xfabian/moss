import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Appointment implements CSVSerializable {
    private int id;
    private int patientId;
    private int medicId;
    private LocalDateTime date;
    private Duration duration;
    private String problem;
    // TODO Mai bine enum ?
    private String comment;
    private AppointmentStatus status;

    Appointment() {}

    Appointment(Patient patient, String problem, String comment) {
        this.id = 0;
        this.patientId = patient.getId();
        this.medicId = 0;
        this.date = null;
        this.duration = Duration.ZERO;
        this.problem = problem;
        this.comment = comment;
        this.status = AppointmentStatus.PENDING;
    }

    public int getId() {
        return id;
    }

    public String getProblem() {
        return problem;
    }

    public void setMedicId(int medicId) {
        this.medicId = medicId;
    }

    public int getMedicId() {
        return medicId;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public LocalDateTime getEnd() {
        return date.plus(duration);
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getComment() { return comment; }

    public void setPatientId(int patientId) { this.patientId = patientId; }

    public void assignID() {
        List<Appointment> appointments = MedicalOffice.getInstance().appointments.getList();
        this.id = appointments.isEmpty() ? 1 : appointments.get(appointments.size() - 1).getId() + 1;
    }

    public Table getTable() {
        Patient patient = MedicalOffice.getInstance().getPatientById(patientId);

        Table tb = new Table(8, 2);
        tb.setRow(0, new String[]{"Name", patient.getName()});
        tb.setRow(1, new String[]{"Email", patient.getEmail()});
        tb.setRow(2, new String[]{"Phone", patient.getPhone()});
        tb.setRow(3, new String[]{"Date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))});
        tb.setRow(4, new String[]{"Time", date.format(DateTimeFormatter.ofPattern("HH:mm")) + "-" + date.plus(duration).format(DateTimeFormatter.ofPattern("HH:mm"))});
        tb.setRow(5, new String[]{"Problem", problem});
        tb.setRow(6, new String[]{"Comment", comment});
        tb.setRow(7, new String[]{"Status", status.toString()});

        return tb;
    }

    @Override
    public String toCSV() {
        return id + "," + patientId + "," + medicId + "," + date + "," + duration + "," + problem + "," + comment + "," + status;
    }

    @Override
    public void fromCSV(String csv) {
        String[] data = csv.split(",");
        id = Integer.parseInt(data[0]);
        patientId = Integer.parseInt(data[1]);
        medicId = Integer.parseInt(data[2]);
        date = LocalDateTime.parse(data[3]);
        duration = Duration.parse(data[4]);
        problem = data[5];
        comment = data[6];
        status = AppointmentStatus.valueOf(data[7]);
    }

    @Override
    public String[] getColumNames() {
        return new String[] {"ID", "PatientID", "MedicID", "Date", "Duration", "Problem", "Comment", "Status"};
    }

    @Override
    public String[] getColumns() {
        return new String[] {String.valueOf(id), String.valueOf(patientId), String.valueOf(medicId), date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), duration.toHours() + ((duration.toHours() > 1) ? " hours" : " hour"), problem, comment, status.toString()};
    }
}
