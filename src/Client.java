import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    private MessengerService messengerService;
    private BufferedReader systemBufferedReader;
    private Logger logger;

    public Client(int port) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(port);
        messengerService = (MessengerService) registry.lookup("MessengerService");
        systemBufferedReader = new BufferedReader(new InputStreamReader(System.in));
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public void execute() throws IOException {
        while (true) {
            System.out.println("Enter operation");
            String operation = systemBufferedReader.readLine();
            System.out.println("Enter key");
            String key = systemBufferedReader.readLine();
            if (operation.equals("GET")) {
                String res = messengerService.get(key);
                logger.log(Level.INFO, res);
            } else if (operation.equals("DEL")) {

            } else if (operation.equals("PUT")) {
                System.out.println("Enter value");
                String value = systemBufferedReader.readLine();
                String res = messengerService.put(key, value);
                logger.log(Level.INFO, res);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.valueOf(args[0]);
        Client client = new Client(port);
        client.execute();
    }
}
