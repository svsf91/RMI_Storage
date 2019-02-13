import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    private MessengerService messengerService;
    private BufferedReader systemBufferedReader;
    private Logger logger;

    public Client(int port) {
        systemBufferedReader = new BufferedReader(new InputStreamReader(System.in));
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            messengerService = (MessengerService) registry.lookup("MessengerService");
        } catch (RemoteException | NotBoundException e) {
            logger.log(Level.WARNING, e.getLocalizedMessage());
        }
        logger.log(Level.INFO, "Connected to port " + port);
    }

    public void execute() {
        while (true) {
            try {
                System.out.println("Enter operation");
                String operation = systemBufferedReader.readLine();
                if (!Arrays.asList("GET", "PUT", "DEL").contains(operation)) {
                    logger.log(Level.WARNING, String.format("Invalid operation: %s", operation));
                    continue;
                }
                System.out.println("Enter key");
                String key = systemBufferedReader.readLine();
                switch (operation) {
                    case "GET": {
                        String res = messengerService.get(key);
                        logger.log(Level.INFO, res);
                        break;
                    }
                    case "DEL": {
                        String res = messengerService.del(key);
                        logger.log(Level.INFO, res);
                        break;
                    }
                    case "PUT": {
                        System.out.println("Enter value");
                        String value = systemBufferedReader.readLine();
                        String res = messengerService.put(key, value);
                        logger.log(Level.INFO, res);
                        break;
                    }
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, e.getLocalizedMessage());
            }
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
        Client client = new Client(port);
        client.execute();
    }
}
