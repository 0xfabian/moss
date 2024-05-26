import java.util.List;
import java.util.Scanner;

public class User {
    protected int id;
    protected String username;
    protected String password;

    User() {}

    User(Scanner sc) {
        System.out.print("Username: ");
        this.username = sc.nextLine();
        System.out.print("Password: ");
        this.password = sc.nextLine();
    }

    public int getId() {
        return id;
    }

    public void assignId(){
        List<User> users = MedicalOffice.getInstance().getUsers();
        this.id = users.isEmpty() ? 1 : users.get(users.size() - 1).getId() + 1;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;

        if (isMedic())
            MedicalOffice.getInstance().medics.writeCSV();
        else if (isReceptionist())
            MedicalOffice.getInstance().receptionists.writeCSV();
        else if (isAdmin())
            MedicalOffice.getInstance().admins.writeCSV();

        LogService.log(LogAction.CHANGE_PASSWORD, MedicalOffice.getInstance().getCurrentUser().getUsername() + " changed password to '" + newPassword + "'");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        for (User user : MedicalOffice.getInstance().getUsers())
            if (user.getUsername().equals(username))
                throw new RuntimeException("Username is already taken");

        this.username = username;
    }

    public void modifyField(String field, String value) {
        switch (field) {
            case "id" -> throw new RuntimeException("Cannot modify ID");
            case "username" -> setUsername(value);
            case "password" -> setPassword(value);
            default -> throw new InvalidFieldException(field);
        }

        if (isMedic())
            MedicalOffice.getInstance().medics.writeCSV();
        else if (isReceptionist())
            MedicalOffice.getInstance().receptionists.writeCSV();
        else if (isAdmin())
            MedicalOffice.getInstance().admins.writeCSV();

        LogService.log(LogAction.MODIFY_USER, username + "'s " + field + " was modified by " + MedicalOffice.getInstance().getCurrentUser().getUsername());
    }

    public boolean isAdmin() {
        return this instanceof Admin;
    }

    public boolean isMedic() {
        return this instanceof Medic;
    }

    public boolean isReceptionist() {
        return this instanceof Receptionist;
    }

    public Table getTable() {
        Table tb = new Table(3, 2);
        tb.setRow(0, new String[]{"ID", String.valueOf(id)});
        tb.setRow(1, new String[]{"Username", username});
        tb.setRow(2, new String[]{"Password", password});

        return tb;
    }
}
