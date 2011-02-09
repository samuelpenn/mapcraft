package uk.org.glendale.worldgen.test;

public class Message {
	private Long		id;
	private String		text;
	private Message 	nextMessage;
	
	Message() {}
	
	public Message(String text) {
		this.text = text;
	}
	
	public Long getId() {
		return id;
	}
	
	private void setId(Long id) {
		this.id = id;
	}
	
	public String getText() {
		return text;
	}
	
	private void setText(String text) {
		this.text = text;
	}
	
	public Message getNextMessage() {
		return nextMessage;
	}
	
	public void setNextMessage(Message nextMessage) {
		this.nextMessage = nextMessage;
	}
}
