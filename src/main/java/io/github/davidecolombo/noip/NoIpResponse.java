package io.github.davidecolombo.noip;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
	"status",
	"description",
	"successful",
	"exitCode"
})
public class NoIpResponse {

	@JsonProperty("status") private String status;
	@JsonProperty("description") private String description;
	@JsonProperty("successful") @NonNull private Boolean successful;
	@JsonProperty("exitCode") @NonNull private Integer exitCode;
}