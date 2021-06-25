package cc.symplectic.monerado;

import android.app.Application;

import java.util.HashMap;

// Keep this around in case I need it in the future.
public class Monerado extends  Application {
    private HashMap<String, RemrigWorker> WorkersRemrig = new HashMap<String, RemrigWorker>();

    private static Monerado instance = new Monerado();

    public static Monerado getInstance() {
        return instance;
    }

    public HashMap<String, RemrigWorker> getWorkersRemrig() { return WorkersRemrig; }
    public void setWorkersRemrig(HashMap<String, RemrigWorker> WorkersRemrig) { this.WorkersRemrig = WorkersRemrig; }

}
