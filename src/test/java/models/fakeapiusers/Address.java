package models.fakeapiusers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address{

	@JsonProperty("zipcode")
	private String zipcode;

	@JsonProperty("number")
	private int number;

	@JsonProperty("city")
	private String city;

	@JsonProperty("street")
	private String street;

	@JsonProperty("geolocation")
	private Geolocation geolocation;

	public String getZipcode(){
		return zipcode;
	}

	public int getNumber(){
		return number;
	}

	public String getCity(){
		return city;
	}

	public String getStreet(){
		return street;
	}

	public Geolocation getGeolocation(){
		return geolocation;
	}
}