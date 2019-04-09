import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface shared between Server and Client
 */
public interface MessengerService extends Remote {
    void init(boolean recur) throws RemoteException;
    String propose(Operation operation, String key, String val) throws RemoteException;
    Vote prepare(Vote vote) throws RemoteException;
    Vote accept(Vote vote) throws RemoteException;
    String execute(Vote vote) throws RemoteException;
}

