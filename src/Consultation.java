public class Consultation implements CSVSerializable {
    String problem;
    String specialization;
    int duration;

    @Override
    public String toCSV() {
        return problem + "," + specialization + "," + duration;
    }

    @Override
    public void fromCSV(String csv) {
        String[] data = csv.split(",");
        problem = data[0];
        specialization = data[1];
        duration = Integer.parseInt(data[2]);
    }

    @Override
    public String[] getColumNames() {
        return new String[]{"Problem", "Specialization", "Duration"};
    }

    @Override
    public String[] getColumns() {
        return new String[]{problem, specialization, String.valueOf(duration)};
    }
}
