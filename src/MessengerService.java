import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface shared between Server and Client
 */
public interface MessengerService extends Remote {
    String put(String key, String val) throws RemoteException;
    String get(String key) throws RemoteException;
    String del(String key) throws RemoteException;
}

