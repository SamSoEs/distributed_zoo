package distributed.systems.cluster.management.manager;

public interface OnElectionCallback {

    void onElectedToBeLeader();

    void onWorker();
}
