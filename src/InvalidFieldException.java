public class InvalidFieldException extends RuntimeException {
    private final String field;

    public InvalidFieldException(String field) {
        super("'" + field + "' is not a valid field");
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
