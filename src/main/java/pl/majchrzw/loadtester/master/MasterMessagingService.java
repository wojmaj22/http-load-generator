package pl.majchrzw.loadtester.master;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import pl.majchrzw.loadtester.dto.NodeExecutionStatistics;
import pl.majchrzw.loadtester.dto.NodeRequestConfig;
import pl.majchrzw.loadtester.dto.NodeStatusChange;

@Component
@Profile("master")
public class MasterMessagingService {
	
	private final String configurationTopic = "configuration";
	private final String readinessTopic = "readiness";
	private final String statisticsTopic = "statistics";
	
	private final MasterDao dao;
	private final JmsTemplate template;
	
	Logger logger = LoggerFactory.getLogger(MasterMessagingService.class);
	
	public MasterMessagingService(JmsTemplate template, MasterDao dao) {
		this.template = template;
		this.dao = dao;
		template.setPubSubDomain(true);
	}
	
	public void transmitConfiguration(NodeRequestConfig config) {
		template.convertAndSend(configurationTopic, config);
		logger.info("Transmitted configuration");
	}
	
	@JmsListener(destination = readinessTopic)
	public void receiveReadiness(NodeStatusChange msg) {
		switch (msg.action()) {
			case START -> {
				dao.registerNewNode(msg.id());
				logger.info("Node: " + msg.id() + " has confirmed readiness, ready nodes count: " + dao.numberOfReadyNodes());
			}
			case SENDING_REQUESTS -> {
				dao.setNodeStatusAsSendingRequests(msg.id());
				logger.info("Node: " + msg.id() + " has started sending requests.");
			}
			case FINISHED_JOB -> {
				dao.setNodeStatusAsFinishedSending(msg.id());
				logger.info("Node: " + msg.id() + " has finished sending requests.");
			}
			case STOP -> {
				dao.deleteNodeFromList(msg.id());
				logger.info("Node: " + msg.id() + " has closed, ready nodes count: " + dao.numberOfReadyNodes());
			}
		}
		
	}
	
	@JmsListener(destination = statisticsTopic)
	public void receiveStatistics(NodeExecutionStatistics statistics){
		logger.info("Received execution statistics from node: " + statistics);
		dao.addNodeExecutionStatistics(statistics);
	}
}
