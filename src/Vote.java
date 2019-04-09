import java.io.Serializable;

public class Vote implements Serializable {
    int num;
    Operation operation;
    String key;
    String val;
    boolean isDown;

    public Vote(int num, Operation operation, String key, String val) {
        this.num = num;
        this.operation = operation;
        this.key = key;
        this.val = val;
    }

    @Override
    public String toString() {
        return String.format("<Vote: %d [%s %s %s]>", this.num, this.operation.toString(),
                this.key, this.val);
    }

}

