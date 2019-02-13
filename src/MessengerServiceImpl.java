import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessengerServiceImpl implements MessengerService {
    private Map<String, String> map;
    private Logger logger;

    public MessengerServiceImpl() {
        map = new HashMap<>();
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.log(Level.INFO, String.format("Current thread: %s", Thread.currentThread().getName()));
    }

    /**
     * add key-value pair to HashMap
     * @param key
     * @param val
     * @return message of status
     */
    @Override
    public String put(String key, String val) {
        logger.log(Level.INFO, String.format("Current thread: %s", Thread.currentThread().getName()));
        if (map.containsKey(key)) {
            logger.log(Level.INFO, String.format("%s already exists", key));
        }
        logger.log(Level.INFO, String.format("PUT (%s, %s)", key, val));
        map.put(key, val);
        return String.format("PUT (%s, %s)", key, val);
    }

    /**
     * retrive value of given key
     * handle key not exist
     * @param key
     * @return message of status
     */
    @Override
    public String get(String key) {
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
     * @param key
     * @return message of status
     */
    @Override
    public String del(String key) {
        logger.log(Level.INFO, String.format("Current thread: %s", Thread.currentThread().getName()));
        if (map.containsKey(key)) {
            logger.log(Level.INFO, String.format("DELETE %s", key));
            map.remove(key);
            return String.format("%s deleted successfully", key);
        } else {
            logger.log(Level.INFO, String.format("%s does not exist", key));
            return String.format("%s does not exist");
        }
    }
}
