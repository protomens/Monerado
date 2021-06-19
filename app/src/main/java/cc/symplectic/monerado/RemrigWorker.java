package cc.symplectic.monerado;

public class RemrigWorker {
    String URL;
    String Username;
    String Password;
    Boolean state;

    public RemrigWorker(String URL, String Username, String Password, Boolean state) {
        this.URL = URL;
        this.Username = Username;
        this.Password = Password;
        this.state = state;
    }

    public String getURL() { return this.URL; }
    public void setURL(String URL) { this.URL = URL; }

    public String getUsername() { return this.Username; }
    public void setUsername(String Username) { this.Username = Username; }

    public String getPassword() { return this.Password; }
    public void setPassword(String Password) { this.Password = Password; }

    public Boolean getState() { return this.state; }
    public void setState(Boolean state) { this.state = state; }

}
