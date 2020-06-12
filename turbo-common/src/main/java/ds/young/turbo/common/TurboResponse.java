package ds.young.turbo.common;

import java.io.Serializable;

public class TurboResponse implements Serializable {

	private String responseId;

	private int status;

	private Object data;

	public TurboResponse(){};


	public TurboResponse(String responseId, int status, Object data) {
		super();
		this.responseId = responseId;
		this.status = status;
		this.data = data;
	}

	public String getResponseId() {
		return responseId;
	}

	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int i) {
		this.status = i;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	
	

}
