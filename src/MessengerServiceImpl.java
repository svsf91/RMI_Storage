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
    int num;
    Vote acceptedVote;

    public MessengerServiceImpl(int num, List<Integer> _ports) {
        this.num = num;
        map = new HashMap<>();
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        ports = _ports;
        messengerServices = new ArrayList<>();
        logger.log(Level.INFO, String.format("Current thread: %s", Thread.currentThread().getName()));
        acceptedVote = new Vote(0, Operation.GET, "", null);
    }

    /**
     * init list messengerServices storing replicate servers' stubs
     */
    @Override
    public void init(boolean recur) {
        while (!isReady()) {
            try {
                List<MessengerService> l = new ArrayList<>();
                for (int port : ports) {
                    Registry registry = LocateRegistry.getRegistry(port);
                    MessengerService messengerService = (MessengerService) registry.lookup("MessengerService");
                    if(recur) {
                        messengerService.init(false);
                    }
                    l.add(messengerService);
                }
                messengerServices = l;
                logger.log(Level.WARNING, "Server ready");
                return;
            } catch (RemoteException | NotBoundException e) {
                logger.log(Level.WARNING, "Server not ready");
            }
        }
    }

    public boolean isReady() {
        return messengerServices.size() == ports.size();
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
        logger.log(Level.INFO, String.format("Current thread: %s", Thread.currentThread().getName()));
        if (map.containsKey(key)) {
            logger.log(Level.INFO, String.format("%s already exists", key));
        }
        logger.log(Level.INFO, String.format("PUT (%s, %s)", key, val));
        map.put(key, val);
        String successMsg = String.format("PUT (%s, %s)", key, val);
        // If request comes from a client, update other servers
        if (recur) {
            try {
                for (MessengerService ms : messengerServices) {
                    String ret = ms.put(key, val, false);
                    if (!ret.equals(successMsg)) {
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
        logger.log(Level.INFO, String.format("Current thread: %s", Thread.currentThread().getName()));
        if (map.containsKey(key)) {
            logger.log(Level.INFO, String.format("DELETE %s", key));
            map.remove(key);
            // If request comes from a client, update other servers
            String successMsg = String.format("%s deleted successfully", key);
            if (recur) {
                try {
                    for (MessengerService ms : messengerServices) {
                        String ret = ms.del(key, false);
                        if (!ret.equals(successMsg)) {
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

    @Override
    public Vote prepare(Vote vote){
        if (vote.num > acceptedVote.num) {
            acceptedVote = vote;
            logger.log(Level.INFO, String.format("Accept proposal: <%s>", vote.toString()));
            return vote;
        } else {
            logger.log(Level.INFO, String.format("Reject proposal: <%s>, accepted proposal: <%s>", vote.toString(), acceptedVote.toString()));
            return acceptedVote;
        }
    }

    @Override
    public boolean accept(Vote vote){
        return vote.equals(acceptedVote);
    }

    @Override
    public String execute(Vote vote) {
        if (vote.operation == Operation.PUT) {
            map.put(vote.key, vote.val);
            return String.format("Put %s, %s successfully", vote.key, vote.val);
        } else if (vote.operation == Operation.DEL) {
            if (map.containsKey(vote.key)) {
                map.remove(vote.key);
                return String.format("Delete %s successfully", vote.key);
            } else {
                return String.format("Delete %s failed: no such key", vote.key);
            }
        } else {
            if(map.containsKey(vote.key)) {
                String val = map.get(vote.key);
                return String.format("Get %s: value is %s", vote.key, val);
            } else {
                return String.format("Get %s failed: no such key", vote.key);
            }
        }
    }

    @Override
    public String propose(Operation operation, String key, String val) {
        init(true);
        Vote proposal = new Vote(num, operation, key, val);
        logger.log(Level.INFO, "Proposing: " + proposal.toString());
        int count = 0;
        for (MessengerService messengerService : messengerServices) {
            try {
                Vote vote = messengerService.prepare(proposal);
                if (vote.num > proposal.num) {
                    proposal = vote;
                    break;
                } else {
                    count++;
                }
            } catch (NullPointerException | RemoteException e) {
                logger.log(Level.WARNING, e.getLocalizedMessage());
                return "Server down";
            }
        }

        if (proposal.num > num || count >= Math.ceil(messengerServices.size() / 2.0)) {
            if (proposal.num > num) {
                logger.log(Level.INFO, "Propose failed");
            } else {
                logger.log(Level.INFO, "Propose successfully");
            }
            count = 0;
            for (MessengerService messengerService : messengerServices) {
                try {
                    boolean vote = messengerService.accept(proposal);
                    if (vote) {
                        count++;
                    }
                } catch (NullPointerException | RemoteException e) {
                    logger.log(Level.WARNING, "Server down");
                    return "Server down";
                }
            }
            if (count >= Math.ceil(messengerServices.size() / 2.0)) {
                for (MessengerService messengerService : messengerServices) {
                    try {
                        messengerService.execute(proposal);
                    } catch (NullPointerException | RemoteException e) {
                        logger.log(Level.WARNING, "Server down");
                        return "Server down";
                    }
                }
            }
            num += messengerServices.size() + 1;
            return execute(proposal);
        } else {
            num += messengerServices.size() + 1;
            return "Accept failed";
        }
    }
}
