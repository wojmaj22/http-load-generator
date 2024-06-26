package pl.majchrzw.loadtester.master;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.majchrzw.loadtester.dto.Status;
import pl.majchrzw.loadtester.dto.config.NodeRequestConfig;
import pl.majchrzw.loadtester.dto.statistics.NodeExecutionStatistics;
import pl.majchrzw.loadtester.shared.DataRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Profile("master")
public class MasterDao implements DataRepository {
	
	private final HashMap<UUID, Status> nodeStatus;
	private final UUID masterId;
	private final HashMap<UUID, NodeExecutionStatistics> executionStatistics;
	private NodeRequestConfig requestConfig;
	
	public MasterDao() {
		this.masterId = UUID.randomUUID();
		this.nodeStatus = new HashMap<>();
		executionStatistics = new HashMap<>();
	}
	
	public void setNodeStatus(UUID nodeId, Status status) {
		nodeStatus.put(nodeId, status);
	}
	
	public void addNodeExecutionStatistics(NodeExecutionStatistics statistics) {
		executionStatistics.put(statistics.nodeId(), statistics);
	}
	
	public HashMap<UUID, NodeExecutionStatistics> getAllExecutionStatistics() {
		return executionStatistics;
	}
	
	public int numberOfReadyNodes() {
		Map<UUID, Status> statuses = Map.copyOf(nodeStatus);
		return (int) statuses.values()
				.stream()
				.filter(status -> status.equals(Status.NEW))
				.count();
	}
	
	public synchronized int numberOfFinishedNodes() {
		Map<UUID, Status> statuses = Map.copyOf(nodeStatus);
		return (int) statuses.values()
				.stream()
				.filter(status -> status.equals(Status.CLOSED))
				.count();
	}
	
	@Override
	public UUID getId() {
		return masterId;
	}
	
	@Override
	public NodeRequestConfig getRequestConfig() {
		return requestConfig;
	}
	
	@Override
	public void setRequestConfig(NodeRequestConfig requestConfig) {
		this.requestConfig = requestConfig;
	}
}
