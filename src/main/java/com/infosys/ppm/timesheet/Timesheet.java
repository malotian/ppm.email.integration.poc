package com.infosys.ppm.timesheet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "name", "date", "hours" })
public class Timesheet implements Serializable {

	@JsonProperty("id")
	private String id;
	@JsonProperty("name")
	private String name;
	@JsonProperty("date")
	private String date;
	@JsonProperty("hours")
	private String hours;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();
	private final static long serialVersionUID = -4447971602214460255L;

	@JsonProperty("id")
	public String getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
	}

	public Timesheet withId(String id) {
		this.id = id;
		return this;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	public Timesheet withName(String name) {
		this.name = name;
		return this;
	}

	@JsonProperty("date")
	public String getDate() {
		return date;
	}

	@JsonProperty("date")
	public void setDate(String date) {
		this.date = date;
	}

	public Timesheet withDate(String date) {
		this.date = date;
		return this;
	}

	@JsonProperty("hours")
	public String getHours() {
		return hours;
	}

	@JsonProperty("hours")
	public void setHours(String hours) {
		this.hours = hours;
	}

	public Timesheet withHours(String hours) {
		this.hours = hours;
		return this;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	public Timesheet withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("name", name).append("date", date).append("hours", hours).append("additionalProperties", additionalProperties).toString();
	}

}