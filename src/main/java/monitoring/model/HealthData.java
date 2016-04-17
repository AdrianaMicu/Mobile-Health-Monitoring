package monitoring.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ektorp.support.CouchDbDocument;
import org.ektorp.support.TypeDiscriminator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthData extends CouchDbDocument implements Serializable,
		Comparable<HealthData> {

	private static final long serialVersionUID = 1L;

	@TypeDiscriminator
	private String timestamp;

	private String timestampStart;
	private String timestampEnd;

	private String type;
	private String value;
	private String user;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTimestampStart() {
		return timestampStart;
	}

	public void setTimestampStart(String timestampStart) {
		this.timestampStart = timestampStart;
	}

	public String getTimestampEnd() {
		return timestampEnd;
	}

	public void setTimestampEnd(String timestampEnd) {
		this.timestampEnd = timestampEnd;
	}

	@Override
	public int compareTo(HealthData healthData) {

		DateFormat dateFormatMilliseconds = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		DateFormat dateFormatSeconds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateThis = null;
		Date dateCompare = null;

		if (this.getTimestamp() != null) {
			
			try {
				dateThis = dateFormatSeconds.parse(this.getTimestamp());
			} catch (Exception e) {
				try {
					dateThis = dateFormatMilliseconds.parse(this.getTimestamp());
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
			try {
				dateCompare = dateFormatSeconds.parse(healthData.getTimestamp());
			} catch (Exception e) {
				try {
					dateCompare = dateFormatMilliseconds.parse(healthData.getTimestamp());
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		} else if (this.getTimestampStart() != null) {
			
			try {
				dateThis = dateFormatSeconds.parse(this.getTimestampStart());
			} catch (Exception e) {
				try {
					dateThis = dateFormatMilliseconds.parse(this.getTimestampStart());
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
			try {
				dateCompare = dateFormatSeconds.parse(healthData.getTimestampStart());
			} catch (Exception e) {
				try {
					dateCompare = dateFormatMilliseconds.parse(healthData.getTimestampStart());
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		} 
		
/*		else {

			try {
				dateThis = dateFormatSeconds.parse(this.getTimestampEnd());
				dateCompare = dateFormatSeconds.parse(healthData.getTimestampEnd());
			} catch (Exception e) {
				try {
					dateThis = dateFormatMilliseconds.parse(this.getTimestampEnd());
					dateCompare = dateFormatMilliseconds.parse(healthData.getTimestampEnd());
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		}*/

		if (dateCompare.after(dateThis)) {
			return -1;
		} else if (dateCompare.equals(dateThis)) {
			return 0;
		}

		return 1;
	}
}
