import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private Logger logger;

    public Server(int port) {
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        MessengerService messengerService = new MessengerServiceImpl();
        try {
            MessengerService stub = (MessengerService) UnicastRemoteObject.exportObject(messengerService, 1100);
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("MessengerService", stub);
        } catch (RemoteException e) {
            logger.log(Level.WARNING, e.getLocalizedMessage());
        }
    }


    public static void main(String[] args) {
        int port = 0;
        try {
            port = Integer.valueOf(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number");
            System.exit(0);
        }
        Server server = new Server(port);
    }
}
