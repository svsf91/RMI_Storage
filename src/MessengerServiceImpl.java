import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessengerServiceImpl implements MessengerService {
    private Map<String, String> map;
    private Logger logger;
    private List<Integer> ports;
    private List<MessengerService> messengerServices;

    public MessengerServiceImpl(List<Integer> _ports) {
        map = new HashMap<>();
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        ports = _ports;
        messengerServices = new ArrayList<>();
        logger.log(Level.INFO, String.format("Current thread: %s", Thread.currentThread().getName()));
    }

    /**
     * init list messengerServices storing replicate servers' stubs
     */
    private synchronized void init() {
        while (true) {
            try {
                List<MessengerService> l = new ArrayList<>();
                for (int port : ports) {
                    Registry registry = LocateRegistry.getRegistry(port);
                    MessengerService messengerService = (MessengerService) registry.lookup("MessengerService");
                    l.add(messengerService);
                }
                messengerServices = l;
                return;
            } catch (RemoteException | NotBoundException e) {
                logger.log(Level.WARNING, "Server not ready");
            }
        }
    }

    /**
     * check whether replicate servers are ready
     * by invoking their isReady()
     * @return boolean
     */
    private synchronized boolean checkReady(boolean recur) {
        if (!isReady()) {
            return false;
        }
        if (recur) {
            for (MessengerService ms : messengerServices) {
                try {
                    if (!ms.isReady()) {
                        return false;
                    }
                } catch (RemoteException e) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public synchronized boolean isReady() {
        if (messengerServices.size() != ports.size()) {
            init();
        }
        return true;
    }

    /**
     * add key-value pair to HashMap
     *
     * @param key
     * @param val
     * @param recur a flag indicating whether the request comes from a client or a replicate server
     * @return message of status
     */
    @Override
    public synchronized String put(String key, String val, boolean recur) {
        if (!checkReady(recur)) {
            logger.log(Level.WARNING, "Servers not ready");
            return "Servers not ready";
        }
        logger.log(Level.INFO, String.format("Current thread: %s", Thread.currentThread().getName()));
        if (map.containsKey(key)) {
            logger.log(Level.INFO, String.format("%s already exists", key));
        }
        logger.log(Level.INFO, String.format("PUT (%s, %s)", key, val));
        map.put(key, val);
        String successMsg = String.format("PUT (%s, %s)", key, val);
        // If request comes from a client, update other servers
        if(recur) {
            try {
                for (MessengerService ms : messengerServices) {
                    String ret = ms.put(key, val, false);
                    if(!ret.equals(successMsg)) {
                        logger.log(Level.WARNING, "Inconsistent data");
                        return "Inconsistent data";
                    }
                }
            } catch (RemoteException e) {
                logger.log(Level.WARNING, "Servers not ready");
                return "Servers not ready";
            }
        }
        return successMsg;
    }

    /**
     * retrive value of given key
     * handle key not exist
     *
     * @param key
     * @return message of status
     */
    @Override
    public synchronized String get(String key) {
        logger.log(Level.INFO, String.format("Current thread: %s", Thread.currentThread().getName()));
        if (map.containsKey(key)) {
            logger.log(Level.INFO, String.format("GET %s, value is %s", key, map.get(key)));
            return String.format("GET %s, value is %s", key, map.get(key));
        } else {
            logger.log(Level.INFO, String.format("%s does not exist", key));
            return String.format("%s does not exist", key);
        }
    }

    /**
     * delete key-value pair given by key
     * handle key not exist
     *
     * @param key
     * @param recur a flag indicating whether the request comes from a client or a replicate server
     * @return message of status
     */
    @Override
    public synchronized String del(String key, boolean recur) {
        if (!checkReady(recur)) {
            logger.log(Level.WARNING, "Servers not ready");
            return "Servers not ready";
        }
        logger.log(Level.INFO, String.format("Current thread: %s", Thread.currentThread().getName()));
        if (map.containsKey(key)) {
            logger.log(Level.INFO, String.format("DELETE %s", key));
            map.remove(key);
            // If request comes from a client, update other servers
            String successMsg = String.format("%s deleted successfully", key);
            if(recur) {
                try {
                    for (MessengerService ms : messengerServices) {
                        String ret = ms.del(key, false);
                        if(!ret.equals(successMsg)) {
                            logger.log(Level.WARNING, "Inconsistent data");
                            return "Inconsistent data";
                        }
                    }
                } catch (RemoteException e) {
                    logger.log(Level.WARNING, "Servers not ready");
                    return "Servers not ready";
                }
            }
            return successMsg;
        } else {
            logger.log(Level.INFO, String.format("%s does not exist", key));
            return String.format("%s does not exist", key);
        }
    }
}
