package pragmasoft.andriilupynos.js_executioner.domain;

import pragmasoft.andriilupynos.js_executioner.domain.exception.InvalidExecutionStateException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ScriptInfo {

    public final Script script;
    public final String name;
    protected final AtomicReference<Status> status;
    public final Instant created;
    private final ScriptOutput out;
    private final ScriptOutput err;
    private final PropertyChangeSupport observable = new PropertyChangeSupport(this);

    private final Map<Status, Collection<Status>> allowedStatusTransfer = Map.of(
            Status.SCHEDULED, List.of(),
            Status.RUNNING, List.of(Status.SCHEDULED),
            Status.COMPLETED, List.of(Status.RUNNING),
            Status.ERROR, List.of(Status.RUNNING)
    );

    ScriptInfo(Script script, String name, Status status, Instant created, ScriptOutput out, ScriptOutput err) {
        this.script = script;
        this.name = name;
        this.status = new AtomicReference<>(status);
        this.out = out;
        this.err = err;
        this.created = created;
    }

    public enum Status {
        SCHEDULED, RUNNING, COMPLETED, ERROR
    }

    public String getOut() {
        return out.toString();
    }

    public String getErr() {
        return err.toString();
    }

    protected final void setStatus(Status status) {
        Status currentStatus;
        do {
            currentStatus = this.status.get();
            this.checkStatusChangePossible(currentStatus, status);
        } while(!this.status.compareAndSet(currentStatus, status));
        Status old = this.status.getAndSet(status);
        this.observable.firePropertyChange("status", old, status);
    }

    private void checkStatusChangePossible(Status currentStatus, Status newStatus) {
        var allowed = this.allowedStatusTransfer.get(newStatus);

        if (!allowed.contains(currentStatus)) {
            throw new InvalidExecutionStateException(
                    this.name,
                    newStatus.name(),
                    currentStatus.name(),
                    allowed.stream().map(Enum::name).collect(Collectors.toList())
            );
        }
    }

    public Status getStatus() {
        return status.get();
    }

    @SuppressWarnings("unused")
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        observable.addPropertyChangeListener(listener);
    }

    @SuppressWarnings("unused")
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        observable.removePropertyChangeListener(listener);
    }

    public interface ScriptOutput {

        OutputStream asStream();

        @Override
        String toString();

    }

}
