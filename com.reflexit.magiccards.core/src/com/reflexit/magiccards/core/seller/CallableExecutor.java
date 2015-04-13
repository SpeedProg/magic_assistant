package com.reflexit.magiccards.core.seller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Execute value-returning concurrent tasks in a thread pool, wait until all tasks
 * are executed and return the tasks and their value in a map.
 */
public class CallableExecutor<T> {
	private final ExecutorService executor;
	private final Map<Callable<T>, Future<T>> tasks;
	private final List<Callable<T>> actions;
	private final ICoreProgressMonitor monitor;

	/**
	 * Constructor to initialize the CallableExecutor
	 * @param monitor Right now the only thing that can stop the execution if it takes to long or runs endlessly.
	 * @param poolsize The size of the pool that execute the tasks. If the {@code poolsize} is as bigger as the size 
	 * of {@code actions} then the pool size will be reduced to {@code actions.size()}.
	 * @param actions A list of all tasks to execute.
	 */
	public CallableExecutor(final ICoreProgressMonitor monitor,	final int poolsize, final List<Callable<T>> actions) {
		this.monitor = monitor;
		this.actions = actions;
		if(poolsize > actions.size()){
			executor = Executors.newFixedThreadPool(actions.size());
		}else{
			executor = Executors.newFixedThreadPool(poolsize);
		}
		tasks = new HashMap<Callable<T>, Future<T>>();
	}

	/**
	 * Execute all actions that were specified by the constructor.
	 * 
	 * @return return the result of all value-returning tasks as a Map where the
	 *         key is the task and the value of the map is task result value.
	 */
	public Map<Callable<T>, T> exec() {
		// Submits all value-returning task
		for (Callable<T> action : actions) {
			tasks.put(action, executor.submit(action));
		}

		// Initiates an orderly shutdown in which previously submitted
		// tasks are executed, but no new tasks will be accepted.
		executor.shutdown();

		Map<Callable<T>, T> results = new HashMap<Callable<T>, T>();
		Iterator<Entry<Callable<T>, Future<T>>> entries;
		// Wait until all results are returned (same size) or monitor is canceled.
		while (results.size() != tasks.size()) {
			if (monitor.isCanceled()) {
				break;
			}
			entries = tasks.entrySet().iterator();
			while (entries.hasNext()) {
				Entry<Callable<T>, Future<T>> entry = entries.next();
				if (executor.isShutdown() && !results.containsKey(entry.getKey())) {
					try {
						// task is ready
						results.put(entry.getKey(), entry.getValue().get());
					} catch (InterruptedException e) {
						// continue
					} catch (ExecutionException e) {
						// continue
					}
				}
			}
		}
		return results;
	}
}