package com.davidecolombo.noip.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"status",
	"description",
	"successful",
	"exitcode"
})
public class NoipResponse {

	@JsonProperty("status") private String status;
	@JsonProperty("description") private String description;
	@JsonProperty("successful") private boolean successful;
	@JsonProperty("exitcode") private int exitcode;
}
