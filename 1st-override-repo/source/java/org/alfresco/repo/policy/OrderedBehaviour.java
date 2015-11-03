package org.alfresco.repo.policy;

public class OrderedBehaviour extends JavaBehaviour implements
		TransactionBehaviourOrder {

	private int order = 50;

	public OrderedBehaviour(Object instance, String method,
			NotificationFrequency frequency, int order) {
		super(instance, method, frequency);
		this.order = order;
	}
	
	public OrderedBehaviour(Object instance, String method,
			NotificationFrequency frequency) {
		super(instance, method, frequency);
	}

	public OrderedBehaviour(Object instance, String method) {
		super(instance, method);
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
