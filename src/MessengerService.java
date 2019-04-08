import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface shared between Server and Client
 */
public interface MessengerService extends Remote {
    void init(boolean recur) throws RemoteException;
    String put(String key, String val, boolean recur) throws RemoteException;
    String get(String key) throws RemoteException;
    String del(String key, boolean recur) throws RemoteException;
    String propose(Operation operation, String key, String val) throws RemoteException;
    Vote prepare(Vote vote) throws RemoteException;
    boolean accept(Vote vote) throws RemoteException;
    String execute(Vote vote) throws RemoteException;
}

