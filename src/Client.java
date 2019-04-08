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
    // Remote object
    protected MessengerService messengerService;
    private BufferedReader systemBufferedReader;
    private Logger logger;

    /**
     * Get MessengerService object from Registry
     * @param port Specify port used by Registry
     */
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

    /**
     * Get user input of operation, key and value
     * Call method of remote object accordingly
     */
    public void execute() {
        while (true) {
            try {
                System.out.println("Enter commands");
                String command = systemBufferedReader.readLine();
                String[] commands = command.split("\\s+");
                String res = "";
                if(commands[0].equals("GET")) {
                    String key = commands[1];
                    res = messengerService.propose(Operation.GET, key, null);
                } else if(commands[0].equals("PUT")) {
                    String key = commands[1];
                    String val = commands[2];
                    res = messengerService.propose(Operation.PUT, key, val);
                } else {
                    String key = commands[1];
                    res = messengerService.propose(Operation.DEL, key, null);
                }
                logger.log(Level.INFO, res);
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
