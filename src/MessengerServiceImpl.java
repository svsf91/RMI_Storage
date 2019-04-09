import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessengerServiceImpl implements MessengerService {
    private Map<String, String> map;
    private Logger logger;
    private List<Integer> ports;
    private List<MessengerService> messengerServices;
    private int num;
    private Vote acceptedVote;
    private Random random;

    public MessengerServiceImpl(int num, List<Integer> _ports) {
        this.num = num;
        map = new HashMap<>();
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        ports = _ports;
        messengerServices = new ArrayList<>();
        logger.log(Level.INFO, String.format("Current thread: %s", Thread.currentThread().getName()));
        acceptedVote = new Vote(0, Operation.GET, "", "");
        random = new Random();
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


    @Override
    public Vote prepare(Vote vote){
        if(random.nextFloat() < 0.2) {
            logger.log(Level.INFO, "Simulating server down");
            return null;
        }
        if (vote.num > acceptedVote.num) {
            acceptedVote = vote;
            logger.log(Level.INFO, String.format("Accept proposal: %s", vote.toString()));
            return vote;
        } else {
            logger.log(Level.INFO, String.format("Reject proposal: %s, accepted proposal: %s", vote.toString(), acceptedVote.toString()));
            return acceptedVote;
        }
    }

    @Override
    public Vote accept(Vote vote){
        if(random.nextFloat() < 0.2) {
            logger.log(Level.INFO, "Simulating server down");
            return null;
        }
        return acceptedVote;
    }

    @Override
    public String execute(Vote vote) {
        if (vote.operation == Operation.PUT) {
            map.put(vote.key, vote.val);
            return String.format("[PUT %s %s] successfully", vote.key, vote.val);
        } else if (vote.operation == Operation.DEL) {
            if (map.containsKey(vote.key)) {
                map.remove(vote.key);
                return String.format("[DEL %s] successfully", vote.key);
            } else {
                return String.format("[DEL %s] failed: no such key", vote.key);
            }
        } else {
            if(map.containsKey(vote.key)) {
                String val = map.get(vote.key);
                return String.format("[GET %s] successfully: value is %s", vote.key, val);
            } else {
                return String.format("[GET %s] failed: no such key", vote.key);
            }
        }
    }

    @Override
    public String propose(Operation operation, String key, String val) {
        init(true);
        Vote proposal = new Vote(num, operation, key, val);
        logger.log(Level.INFO, "Proposing: " + proposal.toString());
        int count = 0;
        int alive = 0;
        for (MessengerService messengerService : messengerServices) {
            try {
                Vote vote = messengerService.prepare(proposal);
                if(vote != null) {
                    if (vote.num > proposal.num) {
                        proposal = vote;
                        break;
                    } else {
                        count++;
                    }
                    alive++;
                }
            } catch (NullPointerException | RemoteException e) {
                logger.log(Level.WARNING, e.getMessage());
                return "Server down";
            }
        }

        if (proposal.num > num || count >= Math.ceil(alive / 2.0)) {
            if (proposal.num > num) {
                logger.log(Level.INFO, "Propose failed");
            } else {
                logger.log(Level.INFO, "Propose successfully");
            }
            count = 0;
            alive = 0;
            for (MessengerService messengerService : messengerServices) {
                try {
                    Vote vote = messengerService.accept(proposal);
                    if (vote != null) {
                        if(vote.equals(proposal)) {
                            count++;
                        }
                        alive++;
                    }
                } catch (NullPointerException | RemoteException e) {
                    logger.log(Level.WARNING, e.getMessage());
                    return "Server down";
                }
            }
            if (count >= Math.ceil(alive / 2.0)) {
                for (MessengerService messengerService : messengerServices) {
                    try {
                        messengerService.execute(proposal);
                    } catch (NullPointerException | RemoteException e) {
                        logger.log(Level.WARNING, e.getMessage());
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
