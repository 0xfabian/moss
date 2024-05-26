import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Patient implements CSVSerializable {
    private int id;
    private String name;
    private String email;
    private String phone;
    private LocalDate dob;

    Patient() {}

    Patient(Scanner sc) {
        System.out.print("Name: ");
        this.name = sc.nextLine();
        System.out.print("Email: ");
        this.email = sc.nextLine();
        System.out.print("Phone: ");
        this.phone = sc.nextLine();
        System.out.print("DOB: ");
        this.dob = LocalDate.parse(sc.nextLine());
    }

    public int getId() {
        return id;
    }

    public void assignID() {
        List<Patient> patients = MedicalOffice.getInstance().patients.getList();
        this.id = patients.isEmpty() ? 1 : patients.get(patients.size() - 1).getId() + 1;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone(){
        return phone;
    }

    public void setDOB(LocalDate dob) {
        this.dob = dob;
    }

    public void setEmail(String email) {
        for (Patient patient : MedicalOffice.getInstance().patients.getList())
            if (patient.getEmail().equals(email))
                throw new RuntimeException("Email is already used");

        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void modifyField(String field, String value) {
        switch (field) {
            case "id" -> throw new RuntimeException("Cannot modify ID");
            case "name" -> setName(value);
            case "email" -> setEmail(value);
            case "phone" -> setPhone(value);
            case "dob"  -> setDOB(LocalDate.parse(value));
            default -> throw new InvalidFieldException(field);
        }

        MedicalOffice.getInstance().patients.writeCSV();
        LogService.log(LogAction.MODIFY_PATIENT, name + "'s " + field + " was modified by " + MedicalOffice.getInstance().getCurrentUser().getUsername());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        Patient patient = (Patient) obj;

        return name.equals(patient.name) && email.equals(patient.email) && phone.equals(patient.phone) && dob.equals(patient.dob);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, phone, dob);
    }

    @Override
    public String toCSV() {
        return id + "," + name + "," + email + "," + phone + "," + dob.toString();
    }

    @Override
    public void fromCSV(String csv) {
        String[] data = csv.split(",");
        id = Integer.parseInt(data[0]);
        name = data[1];
        email = data[2];
        phone = data[3];
        dob = LocalDate.parse(data[4]);
    }

    @Override
    public String[] getColumNames() {
        return new String[] {"ID", "Name", "Email", "Phone", "DOB"};
    }

    @Override
    public String[] getColumns() {
        return new String[] {String.valueOf(id), name, email, phone, dob.toString()};
    }
}
