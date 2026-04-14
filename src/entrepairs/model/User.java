package entrepairs.model;

public class User {

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String secretQuestion;
    private String secretAnswerHash;

    public User() {
        this(0, "", "", "", "", "");
    }

    public User(int id, String name, String email, String passwordHash, String secretQuestion, String secretAnswerHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.secretQuestion = secretQuestion;
        this.secretAnswerHash = secretAnswerHash;
    }

    public User copy() {
        return new User(id, name, email, passwordHash, secretQuestion, secretAnswerHash);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSecretQuestion() {
        return secretQuestion;
    }

    public void setSecretQuestion(String secretQuestion) {
        this.secretQuestion = secretQuestion;
    }

    public String getSecretAnswerHash() {
        return secretAnswerHash;
    }

    public void setSecretAnswerHash(String secretAnswerHash) {
        this.secretAnswerHash = secretAnswerHash;
    }
}
