package me.ahoo.cosid.spring.boot.starter;

import me.ahoo.cosid.InstanceId;
import me.ahoo.cosid.MachineIdDistributor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

/**
 * @author ahoo wang
 */
public class LifecycleMachineIdDistributor implements SmartLifecycle {
    private final CosIdProperties cosIdProperties;
    private final InstanceId instanceId;
    private final MachineIdDistributor machineIdDistributor;
    private volatile boolean running;

    public LifecycleMachineIdDistributor(CosIdProperties cosIdProperties,
                                         InstanceId instanceId,
                                         MachineIdDistributor machineIdDistributor) {
        this.cosIdProperties = cosIdProperties;
        this.instanceId = instanceId;
        this.machineIdDistributor = machineIdDistributor;
    }

    /**
     * Start this component.
     * <p>Should not throw an exception if the component is already running.
     * <p>In the case of a container, this will propagate the start signal to all
     * components that apply.
     *
     * @see SmartLifecycle#isAutoStartup()
     */
    @Override
    public void start() {
        running = true;
    }

    /**
     * Stop this component, typically in a synchronous fashion, such that the component is
     * fully stopped upon return of this method. Consider implementing {@link SmartLifecycle}
     * and its {@code stop(Runnable)} variant when asynchronous stop behavior is necessary.
     * <p>Note that this stop notification is not guaranteed to come before destruction:
     * On regular shutdown, {@code Lifecycle} beans will first receive a stop notification
     * before the general destruction callbacks are being propagated; however, on hot
     * refresh during a context's lifetime or on aborted refresh attempts, a given bean's
     * destroy method will be called without any consideration of stop signals upfront.
     * <p>Should not throw an exception if the component is not running (not started yet).
     * <p>In the case of a container, this will propagate the stop signal to all components
     * that apply.
     *
     * @see SmartLifecycle#stop(Runnable)
     * @see DisposableBean#destroy()
     */
    @Override
    public void stop() {
        running = false;
        machineIdDistributor.revert(cosIdProperties.getNamespace(), this.instanceId);
    }

    /**
     * Check whether this component is currently running.
     * <p>In the case of a container, this will return {@code true} only if <i>all</i>
     * components that apply are currently running.
     *
     * @return whether the component is currently running
     */
    @Override
    public boolean isRunning() {
        return running;
    }
}