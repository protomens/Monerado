package cc.symplectic.monerado;

import android.app.Application;

import java.util.HashMap;

public class Monerado extends  Application {
    private HashMap<String, RemrigWorker> WorkersRemrig = new HashMap<String, RemrigWorker>();

    private static Monerado instance = new Monerado();

    public static Monerado getInstance() {
        return instance;
    }

    public HashMap<String, RemrigWorker> getWorkersRemrig() { return WorkersRemrig; }
    public void setWorkersRemrig(HashMap<String, RemrigWorker> WorkersRemrig) { this.WorkersRemrig = WorkersRemrig; }

}
