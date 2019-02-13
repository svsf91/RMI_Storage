import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    public void execute() throws RemoteException {
        MessengerService messengerService = new MessengerServiceImpl();
        MessengerService stub = (MessengerService) UnicastRemoteObject.exportObject( messengerService, 1100);
        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("MessengerService", stub);
    }

    public static void main(String[] args) throws RemoteException {
        Server server = new Server();
        server.execute();
    }
}
