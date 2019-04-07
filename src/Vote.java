public class Vote {
    int num;
    Operation operation;
    String key;
    String val;

    public Vote(int num, Operation operation, String key, String val) {
        this.num = num;
        this.operation = operation;
        this.key = key;
        this.val = val;
    }

    @Override
    public String toString() {
        return "Proposal " + this.num + ": " + this.operation.toString() + " " + this.key + (this.val == null ? "" : " " + this.val);
    }

}

