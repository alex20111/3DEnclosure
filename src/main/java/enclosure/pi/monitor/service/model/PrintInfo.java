package enclosure.pi.monitor.service.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class PrintInfo {
	private String endDate = "";
	private boolean started = false;
	
	public LocalDateTime getEndDateConv(){
		 OffsetDateTime dateTime = OffsetDateTime.parse(endDate);
		
		return dateTime.atZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime();
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	@Override
	public String toString() {
		return "PrintInfo [endDate=" + endDate + ", started=" + started + "]";
	}
}
