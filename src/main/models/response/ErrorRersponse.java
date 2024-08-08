package models.response;

public class ErrorRersponse {

    private String message;

    public ErrorRersponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
