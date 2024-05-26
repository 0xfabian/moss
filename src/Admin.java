import java.util.Scanner;

public class Admin extends User implements CSVSerializable{
    private boolean canAddUsers;
    private boolean canRemoveUsers;
    private boolean canViewLogs;
    // TODO poate trebuie sa avem canEditUsers

    Admin() {}

    Admin(Scanner sc) {
        super(sc);
        System.out.print("Can add users: ");
        this.canAddUsers = sc.nextLine().equalsIgnoreCase("yes");
        System.out.print("Can remove users: ");
        this.canRemoveUsers = sc.nextLine().equalsIgnoreCase("yes");
        System.out.print("Can view logs: ");
        this.canViewLogs = sc.nextLine().equalsIgnoreCase("yes");
    }

    public void setCanAddUsers(boolean value) {
        canAddUsers = value;
    }

    public void setCanRemoveUsers(boolean value) {
        canRemoveUsers = value;
    }

    public void setCanViewLogs(boolean value) {
        canViewLogs = value;
    }

    public boolean getCanViewLogs() {
        return canViewLogs;
    }

    @Override
    public void modifyField(String field, String value) {
        try {
            super.modifyField(field, value);
        }
        catch (InvalidFieldException e) {
            switch (field) {
                case "canAddUsers" -> setCanAddUsers(value.equalsIgnoreCase("yes"));
                case "canRemoveUsers" -> setCanRemoveUsers(value.equalsIgnoreCase("yes"));
                case "canViewLogs" -> setCanViewLogs(value.equalsIgnoreCase("yes"));
                default -> throw e;
            }

            MedicalOffice.getInstance().admins.writeCSV();
            LogService.log(LogAction.MODIFY_USER, username + "'s " + field + " was modified by " + MedicalOffice.getInstance().getCurrentUser().getUsername());
        }
    }

    public void addUser(User newUser) {
        if(!canAddUsers)
            throw new RuntimeException("You don't have permission to add users");

        for(User user : MedicalOffice.getInstance().getUsers())
            if (newUser.getUsername().equals(user.getUsername()))
                throw new RuntimeException("User already exists");

        newUser.assignId();

        if (newUser.isMedic())
            MedicalOffice.getInstance().medics.add((Medic)newUser);
        else if (newUser.isReceptionist())
            MedicalOffice.getInstance().receptionists.add((Receptionist)newUser);
        else if (newUser.isAdmin())
            MedicalOffice.getInstance().admins.add((Admin)newUser);

        LogService.log(LogAction.ADD_USER, newUser.getUsername() + " was added by " + username);
    }

    public void removeUser(int id) {
        if (!canRemoveUsers)
            throw new RuntimeException("You don't have permission to remove users");

        if (this.id == id)
            throw new RuntimeException("You cannot remove yourself");

        User user = MedicalOffice.getInstance().getUserById(id);

        if (user == null)
            throw new RuntimeException("User not found");

        LogService.log(LogAction.REMOVE_USER, user.getUsername() + " was removed by " + username);

        if (user.isMedic()) {
            for (Appointment appointment : MedicalOffice.getInstance().appointments.getList())
                if (appointment.getMedicId() == user.getId())
                {
                    appointment.setMedicId(0);
                    appointment.setStatus(AppointmentStatus.INVALID);
                }

            MedicalOffice.getInstance().medics.remove((Medic) user);
            MedicalOffice.getInstance().appointments.writeCSV();
        }
        else if (user.isReceptionist())
            MedicalOffice.getInstance().receptionists.remove((Receptionist)user);
        else if (user.isAdmin())
            MedicalOffice.getInstance().admins.remove((Admin)user);
    }

    public void modifyUserField(int id, String field, String value) {
        if (this.id == id) {
            modifyField(field, value);
            return;
        }

        User user = MedicalOffice.getInstance().getUserById(id);

        if (user == null)
            throw new RuntimeException("User not found");

        user.modifyField(field, value);
    }

    @Override
    public Table getTable() {
        Table tb = new Table(6, 2);
        tb.setRow(0, new String[]{"Role", "ADMIN"});
        tb.setRow(1, new String[]{"Username", username});
        tb.setRow(2, new String[]{"Password", password});
        tb.setRow(3, new String[]{"Can add users", canAddUsers ? "yes" : "no"});
        tb.setRow(4, new String[]{"Can remove users", canRemoveUsers ? "yes" : "no"});
        tb.setRow(5, new String[]{"Can view logs", canViewLogs ? "yes" : "no"});

        return tb;
    }

    @Override
    public String toCSV() {
        return id + "," + username + "," + password + "," + canAddUsers + "," + canRemoveUsers + "," + canViewLogs;
    }

    @Override
    public void fromCSV(String csv) {
        String[] data = csv.split(",");
        id = Integer.parseInt(data[0]);
        username = data[1];
        password = data[2];
        canAddUsers = Boolean.parseBoolean(data[3]);
        canRemoveUsers = Boolean.parseBoolean(data[4]);
        canViewLogs = Boolean.parseBoolean(data[5]);
    }

    @Override
    public String[] getColumNames() {
        return new String[]{"ID", "Username", "Password", "Name", "Email", "CanAddUsers", "CanRemoveUsers", "CanViewLogs"};
    }

    @Override
    public String[] getColumns() {
        return new String[]{String.valueOf(id), username, password, String.valueOf(canAddUsers), String.valueOf(canRemoveUsers), String.valueOf(canViewLogs)};
    }
}
