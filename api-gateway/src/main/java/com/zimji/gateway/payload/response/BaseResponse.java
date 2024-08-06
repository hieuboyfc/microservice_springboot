package com.zimji.gateway.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"code", "success", "title", "message", "description", "result"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseResponse<E> implements Serializable {

    @Serial
    private static final long serialVersionUID = -1082791442821228L;

    String code;

    String title;

    String message;

    String description;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
            timezone = "Asia/Ho_Chi_Minh"
    )
    Date timestamp;

    E result;

}
