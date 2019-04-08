import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private Logger logger;
    private static int serverNum = 1;

    /**
     * export MessengerService object through RMI
     *
     * @param port
     */
    public Server(int port, List<Integer> ports) {
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        MessengerService messengerService = new MessengerServiceImpl(serverNum++, ports);
        try {
            MessengerService stub = (MessengerService) UnicastRemoteObject.exportObject(messengerService, 0);
            // create registry on given port
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("MessengerService", stub);
            logger.log(Level.INFO, "Listening to port: " + port);
        } catch (RemoteException e) {
            logger.log(Level.WARNING, e.getLocalizedMessage());
        }
    }


    public static void main(String[] args) {
        List<Integer> ports = new ArrayList<>(Arrays.asList(8080, 8081, 8082, 8083, 8084));
        try {
            int port = Integer.valueOf(args[0]);
            if (args.length > 1) {
                ports = new ArrayList<>();
                for (int i = 0; i < args.length; i++) {
                    int p = Integer.valueOf(args[i]);
                    ports.add(p);
                }
            }
            ports.remove((Integer)port);
            Server server = new Server(port, ports);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number");
            System.exit(0);
        }
    }
}
