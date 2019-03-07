import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface shared between Server and Client
 */
public interface MessengerService extends Remote {
    boolean isReady() throws RemoteException;
    String put(String key, String val, boolean recur) throws RemoteException;
    String get(String key) throws RemoteException;
    String del(String key, boolean recur) throws RemoteException;
}

